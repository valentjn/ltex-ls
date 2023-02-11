/* Copyright (C) 2019-2023 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.server

import com.google.gson.JsonObject
import com.sun.management.OperatingSystemMXBean
import org.bsplines.ltexls.tools.FileIo
import org.bsplines.ltexls.tools.I18n
import org.bsplines.ltexls.tools.Logging
import org.bsplines.ltexls.tools.Tools
import org.eclipse.lsp4j.DidChangeConfigurationParams
import org.eclipse.lsp4j.DidChangeWatchedFilesParams
import org.eclipse.lsp4j.ExecuteCommandParams
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.SymbolInformation
import org.eclipse.lsp4j.WorkspaceSymbolParams
import org.eclipse.lsp4j.jsonrpc.CancelChecker
import org.eclipse.lsp4j.jsonrpc.CompletableFutures
import org.eclipse.lsp4j.services.WorkspaceService
import java.lang.management.ManagementFactory
import java.net.URI
import java.net.URISyntaxException
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Duration
import java.time.Instant
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class LtexWorkspaceService(
  val languageServer: LtexLanguageServer,
) : WorkspaceService {
  override fun symbol(params: WorkspaceSymbolParams): CompletableFuture<List<SymbolInformation>> {
    return CompletableFuture.completedFuture(emptyList())
  }

  override fun didChangeConfiguration(params: DidChangeConfigurationParams) {
    this.languageServer.ltexTextDocumentService.executeFunctionForEachDocument {
      document: LtexTextDocumentItem ->
      if (document.beingChecked) document.cancelCheck()

      this.languageServer.singleThreadExecutorService.execute {
        var exception: Exception? = null

        try {
          document.checkAndPublishDiagnosticsWithoutCache()
          document.raiseExceptionIfCanceled()
        } catch (e: ExecutionException) {
          exception = e
        } catch (e: InterruptedException) {
          exception = e
        }

        if (exception != null) {
          Tools.rethrowCancellationException(exception)
          Logging.logger.warning(I18n.format(exception))
        }
      }
    }
  }

  override fun didChangeWatchedFiles(params: DidChangeWatchedFilesParams) {
  }

  override fun executeCommand(params: ExecuteCommandParams): CompletableFuture<Any> {
    return when (params.command) {
      CHECK_DOCUMENT_COMMAND_NAME -> executeCheckDocumentCommand(params.arguments[0] as JsonObject)
      GET_SERVER_STATUS_COMMAND_NAME -> executeGetServerStatusCommand()
      else -> failCommand(I18n.format("unknownCommand", params.command))
    }
  }

  fun executeCheckDocumentCommand(arguments: JsonObject): CompletableFuture<Any> {
    val uriStr: String = arguments.get("uri").asString
    var codeLanguageId: String? = arguments.get("codeLanguageId")?.asString
    var text: String? = arguments.get("text")?.asString

    if ((codeLanguageId == null) || (text == null)) {
      val path: Path = try {
        Paths.get(URI(uriStr))
      } catch (e: IllegalArgumentException) {
        return failCommand(I18n.format("couldNotParseDocumentUri", e))
      } catch (e: URISyntaxException) {
        return failCommand(I18n.format("couldNotParseDocumentUri", e))
      }

      if (text == null) {
        text = FileIo.readFile(path)
        if (text == null) return failCommand(I18n.format("couldNotReadFile", path.toString()))
      }

      codeLanguageId = codeLanguageId ?: FileIo.getCodeLanguageIdFromPath(path)
      codeLanguageId = codeLanguageId ?: "plaintext"
    }

    val document = LtexTextDocumentItem(this.languageServer, uriStr, codeLanguageId, 1, text)

    val range: Range? = if (arguments.has("range")) {
      val jsonRange: JsonObject = arguments.getAsJsonObject("range")
      val jsonStart: JsonObject = jsonRange.getAsJsonObject("start")
      val jsonEnd: JsonObject = jsonRange.getAsJsonObject("end")
      Range(
        Position(jsonStart.get("line").asInt, jsonStart.get("character").asInt),
        Position(jsonEnd.get("line").asInt, jsonEnd.get("character").asInt),
      )
    } else {
      null
    }

    if (document.beingChecked) document.cancelCheck()

    return CompletableFutures.computeAsync(this.languageServer.singleThreadExecutorService) {
      lspCancelChecker: CancelChecker ->
      document.lspCancelChecker = lspCancelChecker

      try {
        val success: Boolean = document.checkAndPublishDiagnosticsWithoutCache(range)
        val jsonObject = JsonObject()
        jsonObject.addProperty("success", success)
        document.raiseExceptionIfCanceled()
        jsonObject
      } catch (e: ExecutionException) {
        Tools.rethrowCancellationException(e)
        Logging.logger.warning(I18n.format(e))
        emptyList<Any>()
      } catch (e: InterruptedException) {
        Tools.rethrowCancellationException(e)
        Logging.logger.warning(I18n.format(e))
        emptyList<Any>()
      }
    }
  }

  @Suppress("SwallowedException")
  fun executeGetServerStatusCommand(): CompletableFuture<Any> {
    val processId: Long = ProcessHandle.current().pid()
    val wallClockDuration: Double = Duration.between(
      this.languageServer.startupInstant,
      Instant.now(),
    ).toMillis() / MILLISECONDS_PER_SECOND
    val cpuDuration: Double?
    var cpuUsage: Double?
    val totalMemory: Double = Runtime.getRuntime().totalMemory().toDouble()
    val usedMemory: Double = totalMemory - Runtime.getRuntime().freeMemory()

    if (ManagementFactory.getOperatingSystemMXBean() is OperatingSystemMXBean) {
      val operatingSystemMxBean: OperatingSystemMXBean =
          ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean
      cpuUsage = operatingSystemMxBean.processCpuLoad
      if (cpuUsage == -1.0) cpuUsage = null
      val cpuDurationLong: Long = operatingSystemMxBean.processCpuTime
      cpuDuration = (
          if (cpuDurationLong != -1L) (cpuDurationLong / NANOSECONDS_PER_SECOND) else null)
    } else {
      cpuDuration = null
      cpuUsage = null
    }

    val singleThreadTestFuture: Future<Boolean> =
        this.languageServer.singleThreadExecutorService.submit(Callable { true })
    val isChecking: Boolean = try {
      !singleThreadTestFuture.get(CHECK_CHECKING_STATUS_MILLISECONDS, TimeUnit.MILLISECONDS)
    } catch (e: ExecutionException) {
      true
    } catch (e: InterruptedException) {
      true
    } catch (e: TimeoutException) {
      true
    }

    val documentUriBeingChecked: String? = if (isChecking) {
      languageServer.documentChecker.lastCheckedDocument?.uri
    } else {
      null
    }

    val jsonObject = JsonObject()
    jsonObject.addProperty("success", true)
    jsonObject.addProperty("processId", processId)
    jsonObject.addProperty("wallClockDuration", wallClockDuration)
    if (cpuUsage != null) jsonObject.addProperty("cpuUsage", cpuUsage)
    if (cpuDuration != null) jsonObject.addProperty("cpuDuration", cpuDuration)
    jsonObject.addProperty("usedMemory", usedMemory)
    jsonObject.addProperty("totalMemory", totalMemory)
    jsonObject.addProperty("isChecking", isChecking)

    if (documentUriBeingChecked != null) {
      jsonObject.addProperty("documentUriBeingChecked", documentUriBeingChecked)
    }

    return CompletableFuture.completedFuture(jsonObject)
  }

  companion object {
    private const val CHECK_DOCUMENT_COMMAND_NAME = "_ltex.checkDocument"
    private const val GET_SERVER_STATUS_COMMAND_NAME = "_ltex.getServerStatus"

    private const val CHECK_CHECKING_STATUS_MILLISECONDS = 10L
    private const val MILLISECONDS_PER_SECOND = 1e3
    private const val NANOSECONDS_PER_SECOND = 1e9

    private fun failCommand(errorMessage: String): CompletableFuture<Any> {
      val jsonObject = JsonObject()
      jsonObject.addProperty("success", false)
      jsonObject.addProperty("errorMessage", errorMessage)
      return CompletableFuture.completedFuture(jsonObject)
    }

    fun getCommandNames(): List<String> {
      return listOf(CHECK_DOCUMENT_COMMAND_NAME, GET_SERVER_STATUS_COMMAND_NAME)
    }
  }
}
