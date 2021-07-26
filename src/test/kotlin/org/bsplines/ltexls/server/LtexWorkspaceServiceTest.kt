/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.server

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.bsplines.ltexls.tools.I18n
import org.bsplines.ltexls.tools.Logging
import org.eclipse.lsp4j.DidChangeConfigurationParams
import org.eclipse.lsp4j.DidChangeWatchedFilesParams
import org.eclipse.lsp4j.ExecuteCommandParams
import org.eclipse.lsp4j.WorkspaceSymbolParams
import org.junit.platform.suite.api.IncludeEngines
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@IncludeEngines("junit-jupiter")
class LtexWorkspaceServiceTest {
  @Test
  fun testMiscellaneous() {
    val server = LtexLanguageServer()
    val service = LtexWorkspaceService(server)
    service.symbol(WorkspaceSymbolParams())

    val settings = JsonObject()
    settings.add("ltex", JsonObject())
    service.didChangeConfiguration(DidChangeConfigurationParams(settings))

    service.didChangeWatchedFiles(DidChangeWatchedFilesParams())

    val params = ExecuteCommandParams("_ltex.foobar", emptyList())
    val result: JsonObject = (service.executeCommand(params).get() as JsonElement).asJsonObject
    assertFalse(result["success"].asBoolean)
  }

  @Test
  fun testCheckDocument() {
    assertCheckDocumentResult("invalid_uri", false)
    assertCheckDocumentResult("file:///non_existent_path", false)

    for (extension: String in listOf(".bib", ".md", ".org", ".Rnw", ".rst", ".tex")) {
      val tmpFile: File = File.createTempFile("ltex-", extension)

      try {
        assertCheckDocumentResult(tmpFile.toURI().toString(), true)
      } finally {
        if (!tmpFile.delete()) {
          Logging.logger.warning(
            I18n.format("couldNotDeleteTemporaryFile", tmpFile.toPath().toString())
          )
        }
      }
    }
  }

  @Test
  fun testGetServerStatus() {
    val server = LtexLanguageServer()
    val service = LtexWorkspaceService(server)
    val params = ExecuteCommandParams("_ltex.getServerStatus", emptyList())
    val result: JsonObject = (service.executeCommand(params).get() as JsonElement).asJsonObject

    assertTrue(result["success"].asBoolean)
    assertTrue(result["processId"].asLong >= 0)
    assertTrue(result["wallClockDuration"].asDouble >= 0)
    if (result.has("cpuUsage")) assertTrue(result["cpuUsage"].asDouble >= 0)
    if (result.has("cpuDuration")) assertTrue(result["cpuDuration"].asDouble >= 0)
    assertTrue(result["usedMemory"].asDouble >= 0)
    assertTrue(result["totalMemory"].asDouble >= 0)
  }

  companion object {
    private fun assertCheckDocumentResult(uri: String, expected: Boolean) {
      val server = LtexLanguageServer()
      val service = LtexWorkspaceService(server)

      val argument = JsonObject()
      argument.addProperty("uri", uri)

      val range = JsonObject()

      val rangeStart = JsonObject()
      rangeStart.addProperty("line", 0)
      rangeStart.addProperty("character", 1)

      val rangeEnd = JsonObject()
      rangeEnd.addProperty("line", 2)
      rangeEnd.addProperty("character", 3)

      range.add("start", rangeStart)
      range.add("end", rangeEnd)

      argument.add("range", range)

      val params = ExecuteCommandParams("_ltex.checkDocument", listOf(argument))
      val result: JsonObject = (service.executeCommand(params).get() as JsonElement).asJsonObject

      assertFalse(result["success"].asBoolean)
      assertEquals(!expected, result.has("errorMessage"))
    }
  }
}
