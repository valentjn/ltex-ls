/* Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.bsplines.ltexls.tools.FileIo
import org.bsplines.ltexls.tools.I18n
import org.bsplines.ltexls.tools.Logging
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.io.PrintStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LtexLanguageServerLauncherTest {
  @Test
  fun testHelp() {
    val result: Pair<Int, String> = captureStdout {
      LtexLanguageServerLauncher.mainWithoutExit(arrayOf("--help"))
    }
    assertEquals(0, result.first)
    val output: String = result.second
    assertTrue(output.contains("Usage:"))
    assertTrue(output.contains("LTeX LS - LTeX Language Server"))
    assertTrue(output.contains("--help"))
    assertTrue(output.contains("Show this help message and exit."))
  }

  @Test
  fun testVersion() {
    val result: Pair<Int, String> = captureStdout {
      LtexLanguageServerLauncher.mainWithoutExit(arrayOf("--version"))
    }
    assertEquals(0, result.first)
    val output: String = result.second

    val rootJsonElement: JsonElement = JsonParser.parseString(output)
    assertTrue(rootJsonElement.isJsonObject)

    val rootJsonObject: JsonObject = rootJsonElement.asJsonObject
    assertTrue(rootJsonObject.has("java"))

    val javaJsonElement: JsonElement = rootJsonObject.get("java")
    assertTrue(javaJsonElement.isJsonPrimitive)
  }

  @Test
  fun testInputDocuments() {
    val inputDocumentFile: File = File.createTempFile("ltex-", ".tex")
    FileIo.writeFileWithException(inputDocumentFile.toPath(), "This is \\textbf{an test.}\n")

    try {
      val result: Pair<Int, String> = captureStdout {
        LtexLanguageServerLauncher.mainWithoutExit(
          arrayOf("--input-documents", inputDocumentFile.toPath().toString())
        )
      }
      assertEquals(0, result.first)
      val output: String = result.second
      assertTrue(output.contains("Use 'a' instead of 'an'"))
    } finally {
      if (!inputDocumentFile.delete()) {
        Logging.logger.warning(I18n.format(
            "couldNotDeleteTemporaryFile", inputDocumentFile.toPath().toString()))
      }
    }
  }

  companion object {
    fun captureStdout(function: (() -> Int)): Pair<Int, String> {
      val stdout: PrintStream = System.out
      val outputStream = ByteArrayOutputStream()
      val charset: Charset = StandardCharsets.UTF_8

      val exitCode = PrintStream(outputStream, true, charset.name()).use {
        printStream: PrintStream ->
        System.setOut(printStream)

        try {
          function()
        } finally {
          System.setOut(stdout)
        }
      }

      return Pair(exitCode, String(outputStream.toByteArray(), charset))
    }

    fun mockStdin(text: String, function: (() -> Int)): Int {
      val stdin: InputStream = System.`in`

      return ByteArrayInputStream(text.toByteArray(StandardCharsets.UTF_8)).use {
        inputStream: ByteArrayInputStream ->
        System.setIn(inputStream)

        try {
          function()
        } finally {
          System.setIn(stdin)
        }
      }
    }
  }
}
