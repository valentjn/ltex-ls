package org.bsplines.ltex_ls.languagetool;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.*;

import org.bsplines.ltex_ls.Tools;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.TextPart;

public class LanguageToolHttpInterface extends LanguageToolInterface {
  private HttpClient httpClient = HttpClient.newHttpClient();
  private String languageShortCode;
  private String motherTongueShortCode;

  private URL url;
  private List<String> enabledRuleIds = new ArrayList<>();
  private List<String> disabledRuleIds = new ArrayList<>();

  public LanguageToolHttpInterface(String uri, String languageShortCode,
        String motherTongueShortCode) {
    try {
      this.url = new URL(new URL(uri), "v2/check");
    } catch (MalformedURLException e) {
      Tools.logger.severe(Tools.i18n("couldNotParseHttpServerUri", uri, e.getMessage()));
      e.printStackTrace();
      this.url = null;
      return;
    }

    this.languageShortCode = languageShortCode;
    this.motherTongueShortCode = motherTongueShortCode;
  }

  @Override
  public boolean isReady() {
    return (this.url != null);
  }

  @Override
  public List<LanguageToolRuleMatch> check(AnnotatedText annotatedText) {
    JsonArray jsonDataAnnotation = new JsonArray();
    List<TextPart> parts = annotatedText.getParts();

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
    requestEntries.put("language", languageShortCode);
    requestEntries.put("motherTongue", motherTongueShortCode);
    requestEntries.put("enabledRules", String.join(",", enabledRuleIds));
    requestEntries.put("disabledRules", String.join(",", disabledRuleIds));
    requestEntries.put("data", jsonData.toString());

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
        e.printStackTrace();
      }
    }

    String requestBody = builder.toString();

    HttpRequest httpRequest;

    try {
      httpRequest = HttpRequest.newBuilder(url.toURI()).header("Content-Type", "application/json")
          .POST(BodyPublishers.ofString(requestBody)).build();
    } catch (URISyntaxException e) {
      Tools.logger.severe(Tools.i18n("couldNotParseHttpServerUri", url.toString(), e.getMessage()));
      e.printStackTrace();
      return Collections.emptyList();
    }

    HttpResponse<String> httpResponse;

    try {
      httpResponse = httpClient.send(httpRequest, BodyHandlers.ofString());
    } catch (IOException | InterruptedException e) {
      Tools.logger.severe(Tools.i18n("couldNotSendHttpRequestToLanguageTool", e.getMessage()));
      e.printStackTrace();
      return Collections.emptyList();
    }

    if (httpResponse.statusCode() != 200) {
      Tools.logger.severe(Tools.i18n("languageToolFailed", "Received status code " +
          httpResponse.statusCode() + " from the LanguageTool HTTP server"));
      return Collections.emptyList();
    }

    JsonObject jsonResponse = JsonParser.parseString(httpResponse.body()).getAsJsonObject();
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
          suggestedReplacements));
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
  public void enableRules(List<String> ruleIds) {
    enabledRuleIds.addAll(ruleIds);
    disabledRuleIds.removeAll(ruleIds);
  }

  @Override public void disableRules(List<String> ruleIds) {
    enabledRuleIds.removeAll(ruleIds);
    disabledRuleIds.addAll(ruleIds);
  }

  @Override public void enableEasterEgg() {
    // not possible with LanguageTool HTTP server
  }
}