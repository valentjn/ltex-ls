/* Copyright (C) 2019-2023 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.languagetool

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.bsplines.ltexls.parsing.AnnotatedTextFragment
import org.bsplines.ltexls.tools.I18n
import org.bsplines.ltexls.tools.Logging
import org.languagetool.markup.AnnotatedText
import org.languagetool.markup.TextPart
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.MalformedURLException
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers

class LanguageToolHttpInterface(
  uriString: String,
  private val languageShortCode: String,
  private val motherTongueShortCode: String,
) : LanguageToolInterface() {
  private val enabledRules: MutableList<String> = ArrayList()
  private val httpClient: HttpClient = HttpClient.newHttpClient()
  private val uri: URI?

  init {
    var exception: Exception? = null
    this.uri = try {
      URL(URL(uriString), "v2/check").toURI()
    } catch (e: MalformedURLException) {
      exception = e
      null
    } catch (e: URISyntaxException) {
      exception = e
      null
    }

    if (exception != null) {
      Logging.LOGGER.severe(I18n.format("couldNotParseHttpServerUri", exception, uriString))
    }
  }

  override fun isInitialized(): Boolean {
    return (this.uri != null)
  }

  override fun checkInternal(
    annotatedTextFragment: AnnotatedTextFragment,
  ): List<LanguageToolRuleMatch> {
    if (!isInitialized()) return emptyList()

    val requestBody: String = createRequestBody(annotatedTextFragment) ?: return emptyList()
    val httpRequest: HttpRequest = HttpRequest.newBuilder(this.uri)
        .header("Content-Type", "application/json")
        .POST(BodyPublishers.ofString(requestBody))
        .build()
    val httpResponse: HttpResponse<String> = try {
      this.httpClient.send(httpRequest, BodyHandlers.ofString())
    } catch (e: InterruptedException) {
      Logging.LOGGER.severe(I18n.format("couldNotSendHttpRequestToLanguageTool", e))
      return emptyList()
    } catch (e: IOException) {
      Logging.LOGGER.severe(I18n.format("couldNotSendHttpRequestToLanguageTool", e))
      return emptyList()
    }

    val statusCode: Int = httpResponse.statusCode()

    if (statusCode != STATUS_CODE_SUCCESS) {
      Logging.LOGGER.severe(I18n.format("languageToolFailedWithStatusCode", statusCode))
      return emptyList()
    }

    val responseBody: String = httpResponse.body()
    val jsonResponse: JsonObject = JsonParser.parseString(responseBody).asJsonObject
    val jsonMatches: JsonArray = jsonResponse.get("matches").asJsonArray
    val result = ArrayList<LanguageToolRuleMatch>()

    for (jsonElement: JsonElement in jsonMatches) {
      result.add(
        LanguageToolRuleMatch.fromLanguageTool(jsonElement.asJsonObject, annotatedTextFragment),
      )
    }

    return result
  }

  private fun createRequestBody(annotatedTextFragment: AnnotatedTextFragment): String? {
    val jsonData = JsonObject()
    jsonData.add("annotation", convertAnnotatedTextToJson(annotatedTextFragment.annotatedText))

    val requestEntries = HashMap<String, String>()
    requestEntries["language"] = this.languageShortCode
    requestEntries["data"] = jsonData.toString()

    if (this.languageToolOrgUsername.isNotEmpty()) {
      requestEntries["username"] = this.languageToolOrgUsername
    }

    if (this.languageToolOrgApiKey.isNotEmpty()) {
      requestEntries["apiKey"] = this.languageToolOrgApiKey
    }

    if (annotatedTextFragment.codeFragment.settings.enablePickyRules) {
      requestEntries["level"] = "picky"
    }

    if (this.motherTongueShortCode.isNotEmpty()) {
      requestEntries["motherTongue"] = this.motherTongueShortCode
    }

    if (this.enabledRules.isNotEmpty()) {
      requestEntries["enabledRules"] = this.enabledRules.joinToString(",")
    }

    val builder = StringBuilder()

    for ((requestKey: String, requestValue: String) in requestEntries) {
      if (builder.isNotEmpty()) builder.append("&")

      try {
        builder.append(URLEncoder.encode(requestKey, "utf-8"))
            .append("=").append(URLEncoder.encode(requestValue, "utf-8"))
      } catch (e: UnsupportedEncodingException) {
        Logging.LOGGER.severe(I18n.format(e))
        return null
      }
    }

    return builder.toString()
  }

  override fun activateDefaultFalseFriendRules() {
    // handled by LanguageTool HTTP server
  }

  override fun activateLanguageModelRules(languageModelRulesDirectory: String) {
    // handled by LanguageTool HTTP server
  }

  override fun activateNeuralNetworkRules(neuralNetworkRulesDirectory: String) {
    // handled by LanguageTool HTTP server
  }

  override fun activateWord2VecModelRules(word2vecRulesDirectory: String) {
    // handled by LanguageTool HTTP server
  }

  override fun enableRules(ruleIds: Set<String>) {
    this.enabledRules.addAll(ruleIds)
  }

  override fun enableEasterEgg() {
    // not possible with LanguageTool HTTP server
  }

  companion object {
    private const val STATUS_CODE_SUCCESS = 200

    private fun convertAnnotatedTextToJson(annotatedText: AnnotatedText): JsonElement {
      val jsonDataAnnotation = JsonArray()
      val parts: List<TextPart> = annotatedText.parts
      var i = 0

      while (i < parts.size) {
        val jsonPart = JsonObject()

        if (parts[i].type == TextPart.Type.TEXT) {
          jsonPart.addProperty("text", parts[i].part)
        } else if (parts[i].type == TextPart.Type.MARKUP) {
          jsonPart.addProperty("markup", parts[i].part)

          if ((i < parts.size - 1) && (parts[i + 1].type == TextPart.Type.FAKE_CONTENT)) {
            i++
            jsonPart.addProperty("interpretAs", parts[i].part)
          }
        } else {
          // should not happen
          i++
          continue
        }

        jsonDataAnnotation.add(jsonPart)
        i++
      }

      return jsonDataAnnotation
    }
  }
}
