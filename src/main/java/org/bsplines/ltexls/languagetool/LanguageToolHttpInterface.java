/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.languagetool;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bsplines.ltexls.parsing.AnnotatedTextFragment;
import org.bsplines.ltexls.tools.Tools;
import org.checkerframework.checker.nullness.qual.EnsuresNonNullIf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.languagetool.markup.TextPart;
import org.languagetool.rules.RuleMatch;

public class LanguageToolHttpInterface extends LanguageToolInterface {
  private String languageShortCode;
  private String motherTongueShortCode;
  private List<String> enabledRuleIds;
  private List<String> disabledRuleIds;
  private HttpClient httpClient;
  private @MonotonicNonNull URI uri;

  public LanguageToolHttpInterface(String uri, String languageShortCode,
        String motherTongueShortCode) {
    this.languageShortCode = languageShortCode;
    this.motherTongueShortCode = motherTongueShortCode;
    this.enabledRuleIds = new ArrayList<>();
    this.disabledRuleIds = new ArrayList<>();
    this.httpClient = HttpClient.newHttpClient();

    try {
      this.uri = (new URL(new URL(uri), "v2/check")).toURI();
    } catch (MalformedURLException | URISyntaxException e) {
      Tools.logger.severe(Tools.i18n("couldNotParseHttpServerUri", e, uri));
    }
  }

  @EnsuresNonNullIf(expression = "this.uri", result = true)
  @Override
  public boolean isInitialized() {
    return (this.uri != null);
  }

  @Override
  public List<LanguageToolRuleMatch> check(AnnotatedTextFragment annotatedTextFragment) {
    if (!isInitialized()) return Collections.emptyList();

    JsonArray jsonDataAnnotation = new JsonArray();
    List<TextPart> parts = annotatedTextFragment.getAnnotatedText().getParts();

    for (int i = 0; i < parts.size(); i++) {
      JsonObject jsonPart = new JsonObject();

      if (parts.get(i).getType() == TextPart.Type.TEXT) {
        jsonPart.addProperty("text", parts.get(i).getPart());
      } else if (parts.get(i).getType() == TextPart.Type.MARKUP) {
        jsonPart.addProperty("markup", parts.get(i).getPart());

        if ((i < parts.size() - 1) && (parts.get(i + 1).getType() == TextPart.Type.FAKE_CONTENT)) {
          i++;
          jsonPart.addProperty("interpretAs", parts.get(i).getPart());
        }
      } else {
        // should not happen
        continue;
      }

      jsonDataAnnotation.add(jsonPart);
    }

    JsonObject jsonData = new JsonObject();
    jsonData.add("annotation", jsonDataAnnotation);

    Map<String, String> requestEntries = new HashMap<>();
    requestEntries.put("language", this.languageShortCode);
    requestEntries.put("data", jsonData.toString());

    if (annotatedTextFragment.getCodeFragment().getSettings().getEnablePickyRules()) {
      requestEntries.put("level", "picky");
    }

    if (!this.motherTongueShortCode.isEmpty()) {
      requestEntries.put("motherTongue", this.motherTongueShortCode);
    }

    if (!this.enabledRuleIds.isEmpty()) {
      requestEntries.put("enabledRules", String.join(",", this.enabledRuleIds));
    }

    if (!this.disabledRuleIds.isEmpty()) {
      requestEntries.put("disabledRules", String.join(",", this.disabledRuleIds));
    }

    StringBuilder builder = new StringBuilder();

    for (Map.Entry<String, String> requestEntry : requestEntries.entrySet()) {
      if (requestEntry.getValue() == null) {
        continue;
      }

      if (builder.length() > 0) {
        builder.append("&");
      }

      try {
        builder.append(URLEncoder.encode(requestEntry.getKey(), "utf-8"))
            .append("=").append(URLEncoder.encode(requestEntry.getValue(), "utf-8"));
      } catch (UnsupportedEncodingException e) {
        Tools.logger.severe(Tools.i18n(e));
        return Collections.emptyList();
      }
    }

    String requestBody = builder.toString();
    HttpRequest httpRequest = HttpRequest.newBuilder(this.uri)
        .header("Content-Type", "application/json")
        .POST(BodyPublishers.ofString(requestBody))
        .build();
    HttpResponse<String> httpResponse;

    try {
      httpResponse = this.httpClient.send(httpRequest, BodyHandlers.ofString());
    } catch (IOException | InterruptedException e) {
      Tools.logger.severe(Tools.i18n("couldNotSendHttpRequestToLanguageTool", e));
      return Collections.emptyList();
    }

    int statusCode = httpResponse.statusCode();

    if (statusCode != 200) {
      Tools.logger.severe(Tools.i18n("languageToolFailedWithStatusCode", statusCode));
      return Collections.emptyList();
    }

    String responseBody = httpResponse.body();
    JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
    JsonArray jsonMatches = jsonResponse.get("matches").getAsJsonArray();
    List<LanguageToolRuleMatch> result = new ArrayList<>();

    for (JsonElement jsonElement : jsonMatches) {
      JsonObject jsonMatch = jsonElement.getAsJsonObject();
      String ruleId = jsonMatch.get("rule").getAsJsonObject().get("id").getAsString();
      String sentence = jsonMatch.get("sentence").getAsString();
      int fromPos = jsonMatch.get("offset").getAsInt();
      int toPos = fromPos + jsonMatch.get("length").getAsInt();
      String message = jsonMatch.get("message").getAsString();
      List<String> suggestedReplacements = new ArrayList<>();

      for (JsonElement replacement : jsonMatch.get("replacements").getAsJsonArray()) {
        suggestedReplacements.add(replacement.getAsJsonObject().get("value").getAsString());
      }

      result.add(new LanguageToolRuleMatch(ruleId, sentence, fromPos, toPos, message,
          suggestedReplacements, RuleMatch.Type.Hint, annotatedTextFragment));
    }

    return result;
  }

  @Override
  public void activateDefaultFalseFriendRules() {
    // handled by LanguageTool HTTP server
  }

  @Override
  public void activateLanguageModelRules(String languageModelRulesDirectory) {
    // handled by LanguageTool HTTP server
  }

  @Override
  public void activateNeuralNetworkRules(String neuralNetworkRulesDirectory) {
    // handled by LanguageTool HTTP server
  }

  @Override
  public void activateWord2VecModelRules(String word2vecRulesDirectory) {
    // handled by LanguageTool HTTP server
  }

  @Override
  public void enableRules(Set<String> ruleIds) {
    this.enabledRuleIds.addAll(ruleIds);
    this.disabledRuleIds.removeAll(ruleIds);
  }

  @Override public void disableRules(Set<String> ruleIds) {
    this.enabledRuleIds.removeAll(ruleIds);
    this.disabledRuleIds.addAll(ruleIds);
  }

  @Override public void enableEasterEgg() {
    // not possible with LanguageTool HTTP server
  }
}
