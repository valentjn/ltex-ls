/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.server

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.bsplines.ltexls.client.LtexLanguageClient
import org.bsplines.ltexls.settings.SettingsManager
import org.bsplines.ltexls.tools.I18n
import org.bsplines.ltexls.tools.Logging
import org.eclipse.lsp4j.ClientCapabilities
import org.eclipse.lsp4j.CodeActionOptions
import org.eclipse.lsp4j.ExecuteCommandOptions
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.InitializeResult
import org.eclipse.lsp4j.ServerCapabilities
import org.eclipse.lsp4j.TextDocumentSyncKind
import org.eclipse.lsp4j.WindowClientCapabilities
import org.eclipse.lsp4j.services.LanguageClient
import org.eclipse.lsp4j.services.LanguageClientAware
import org.eclipse.lsp4j.services.LanguageServer
import org.eclipse.lsp4j.services.TextDocumentService
import org.eclipse.lsp4j.services.WorkspaceService
import java.time.Instant
import java.util.Locale
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.system.exitProcess

class LtexLanguageServer : LanguageServer, LanguageClientAware {
  var languageClient: LtexLanguageClient? = null
  val singleThreadExecutorService: ExecutorService = Executors.newSingleThreadScheduledExecutor()
  val settingsManager = SettingsManager()
  val documentChecker = DocumentChecker(this.settingsManager)
  val codeActionGenerator = CodeActionGenerator(this.settingsManager)
  val ltexTextDocumentService = LtexTextDocumentService(this)
  val ltexWorkspaceService = LtexWorkspaceService(this)
  val startupInstant: Instant = Instant.now()

  var clientSupportsWorkDoneProgress: Boolean = false
    private set
  var clientSupportsWorkspaceSpecificConfiguration: Boolean = false
    private set

  override fun initialize(params: InitializeParams): CompletableFuture<InitializeResult> {
    val ltexLsPackage: Package? = LtexLanguageServer::class.java.getPackage()
    val ltexLsVersion: String = ltexLsPackage?.implementationVersion ?: "null"
    Logging.logger.info(I18n.format("initializingLtexLs", ltexLsVersion))

    val clientCapabilities: ClientCapabilities? = params.capabilities
    this.clientSupportsWorkDoneProgress = false

    if (clientCapabilities != null) {
      val windowClientCapabilities: WindowClientCapabilities? = clientCapabilities.window

      if ((windowClientCapabilities != null) && (windowClientCapabilities.workDoneProgress)) {
        this.clientSupportsWorkDoneProgress = true
      }
    }

    val initializationOptions: JsonElement? =
        params.initializationOptions as JsonElement?

    if ((initializationOptions != null) && initializationOptions.isJsonObject) {
      val initializationOptionsObject: JsonObject = initializationOptions.asJsonObject

      // LSP 3.16 has built-in locale support
      // (see https://github.com/microsoft/language-server-protocol/issues/754)
      // but currently we only require LSP 3.15.
      if (initializationOptionsObject.has("locale")) {
        val localeLanguage: String = initializationOptionsObject.get("locale").asString
        val locale: Locale = Locale.forLanguageTag(localeLanguage)
        Logging.logger.info(I18n.format("settingLocale", locale.language))
        I18n.setLocale(locale)
      }

      if (initializationOptionsObject.has("customCapabilities")) {
        val customCapabilities: JsonObject =
            initializationOptionsObject.getAsJsonObject("customCapabilities")

        if (customCapabilities.has("workspaceSpecificConfiguration")) {
          this.clientSupportsWorkspaceSpecificConfiguration =
              customCapabilities.get("workspaceSpecificConfiguration").asBoolean
        }
      }
    }

    val serverCapabilities = ServerCapabilities()
    serverCapabilities.setTextDocumentSync(TextDocumentSyncKind.Full)
    serverCapabilities.setCodeActionProvider(
        CodeActionOptions(CodeActionGenerator.getCodeActionKinds()))

    val commandNames = ArrayList<String>()
    commandNames.addAll(LtexWorkspaceService.getCommandNames())
    serverCapabilities.executeCommandProvider = ExecuteCommandOptions(commandNames)

    return CompletableFuture.completedFuture(InitializeResult(serverCapabilities))
  }

  override fun shutdown(): CompletableFuture<Any> {
    Logging.logger.info(I18n.format("shuttingDownLtexLs"))
    this.singleThreadExecutorService.shutdown()

    // should return null according to LSP specification, but return empty object instead,
    // see https://github.com/eclipse/lsp4j/issues/18
    return CompletableFuture.completedFuture(Object())
  }

  override fun exit() {
    Logging.logger.info(I18n.format("exitingLtexLs"))
    exitProcess(0)
  }

  override fun connect(languageClient: LanguageClient) {
    this.languageClient = languageClient as LtexLanguageClient
  }

  override fun getTextDocumentService(): TextDocumentService {
    return this.ltexTextDocumentService
  }

  override fun getWorkspaceService(): WorkspaceService {
    return this.ltexWorkspaceService
  }
}
