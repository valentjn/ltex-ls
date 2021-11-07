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
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

class LspMessage(
  val source: Source,
  type: Type,
  id: String?,
  method: String?,
  params: JsonElement?,
) {
  private val body = JsonObject()

  init {
    body.addProperty("jsonrpc", "2.0")

    when (type) {
      Type.Notification -> {
        body.addProperty("method", method)
        body.add("params", params)
      }
      Type.Request -> {
        if (id != null) body.addProperty("id", id)
        body.addProperty("method", method)
        body.add("params", params)
      }
      Type.Response -> {
        if (id != null) body.addProperty("id", id)
        body.add("result", params)
      }
    }
  }

  fun sendToServer(outputStream: OutputStream) {
    val bodyStr: String = body.toString()
    val bodyBytes: ByteArray = bodyStr.toByteArray(StandardCharsets.UTF_8)
    val headerStr = """
    Content-Length: ${bodyBytes.size}


    """.trimIndent()
    val headerBytes: ByteArray = headerStr.toByteArray(StandardCharsets.US_ASCII)

    org.bsplines.ltexls.tools.Logging.logger.fine(
      String(headerBytes, StandardCharsets.US_ASCII) + String(bodyBytes, StandardCharsets.UTF_8),
    )

    Thread.sleep(100)
    outputStream.write(headerBytes)
    outputStream.write(bodyBytes)
    outputStream.flush()
    Thread.sleep(100)
  }

  fun waitForServer(inputStream: InputStream) {
    var contentLength = -1
    var headerBytes = ByteArray(0)

    while (true) {
      val headerLineBytes: ByteArray = readLine(inputStream, 1024)
      headerBytes += headerLineBytes

      val headerLine = String(headerLineBytes, StandardCharsets.US_ASCII)
      if (headerLine == "\r\n") break

      val matchResult: MatchResult? = HEADER_REGEX.find(headerLine)
      assertNotNull(matchResult)
      val headerName: String = (
        matchResult.groups[1]?.value
        ?: throw AssertionError("could not find header name in '$headerLine'")
      )
      val headerValue: String = (
        matchResult.groups[2]?.value
        ?: throw AssertionError("could not find header value in '$headerLine'")
      )

      if (headerName == "Content-Length") contentLength = headerValue.toInt()
    }

    assertTrue(contentLength >= 0)

    val bodyBytes: ByteArray = read(inputStream, contentLength)
    org.bsplines.ltexls.tools.Logging.logger.fine(
      String(headerBytes, StandardCharsets.US_ASCII) + String(bodyBytes, StandardCharsets.UTF_8),
    )

    val bodyJson: JsonElement = JsonParser.parseString(String(bodyBytes, StandardCharsets.UTF_8))
    assertEquals(body, bodyJson)
  }

  enum class Source {
    Client,
    Server,
  }

  enum class Type {
    Notification,
    Request,
    Response,
  }

  companion object {
    private val EMPTY_LINE_REGEX = Regex("\r\n\r\n|\n\n")
    private val LOG_REGEX = Regex(
      "\\[[^]]+] (\\S+) (\\S+) '([^' ]+)(?: - \\(([^)]+)\\))?'.*\\R(?:Params|Result):",
    )
    private val HEADER_REGEX = Regex("(\\S+): (.*)\r\n")

    fun fromLogFile(
      logFilePath: Path = Paths.get(
        "src",
        "test",
        "resources",
        "LtexLanguageServerTestLog.txt",
      ),
    ): List<LspMessage> {
      var log: String? = FileIo.readFile(logFilePath)
      assertNotNull(log)

      log = log.trim { it <= ' ' }
      val messages: MutableList<LspMessage> = ArrayList()

      for (logMessage: String in log.split(EMPTY_LINE_REGEX)) {
        messages.add(fromLogString(logMessage))
      }

      return messages
    }

    fun fromLogString(logString: String): LspMessage {
      val trimmedLogString: String = logString.trim { it <= ' ' }
      val matchResult: MatchResult? = LOG_REGEX.find(trimmedLogString)
      assertNotNull(matchResult)

      val sourceStr: String? = matchResult.groups[1]?.value
      assertNotNull(sourceStr)

      val source: Source = when (sourceStr) {
        "Sending" -> Source.Client
        "Received" -> Source.Server
        else -> fail()
      }

      val typeStr: String? = matchResult.groups[2]?.value
      assertNotNull(typeStr)

      val type: Type = when (typeStr) {
        "notification" -> Type.Notification
        "request" -> Type.Request
        "response" -> Type.Response
        else -> fail()
      }

      val method: String? = matchResult.groups[3]?.value
      assertNotNull(method)

      val id: String? = matchResult.groups[4]?.value

      val paramsStr: String = trimmedLogString.substring(matchResult.range.last + 1)
      val params: JsonElement = JsonParser.parseString(paramsStr)

      return LspMessage(source, type, id, method, params)
    }

    fun communicateWithList(
      messages: List<LspMessage>,
      inputStream: InputStream,
      outputStream: OutputStream,
    ) {
      for (message in messages) {
        when (message.source) {
          Source.Client -> message.sendToServer(outputStream)
          Source.Server -> message.waitForServer(inputStream)
        }
      }
    }

    private fun read(inputStream: InputStream, numberOfBytes: Int): ByteArray {
      val buffer = ByteArray(numberOfBytes)
      var offset = 0

      while (offset < numberOfBytes) {
        val numberOfBytesRead: Int = inputStream.read(buffer, offset, numberOfBytes - offset)
        assertTrue(numberOfBytesRead >= 1)
        offset += numberOfBytesRead
      }

      return buffer
    }

    private fun readLine(inputStream: InputStream, bufferSize: Int): ByteArray {
      val buffer = ByteArray(bufferSize)
      var offset = 0

      while (
        (offset < 2)
        || (buffer[offset - 2] != 13.toByte())
        || (buffer[offset - 1] != 10.toByte())
      ) {
        val numberOfBytesRead: Int = inputStream.read(buffer, offset, 1)
        assertTrue(numberOfBytesRead >= 1)
        offset += numberOfBytesRead
        assertTrue(offset < bufferSize)
      }

      val result = ByteArray(offset)

      for (i in 0 until offset) {
        result[i] = buffer[i]
      }

      return result
    }
  }
}
