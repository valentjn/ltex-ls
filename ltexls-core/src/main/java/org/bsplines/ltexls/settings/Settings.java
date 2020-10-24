/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.settings;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.bsplines.ltexls.tools.Tools;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.lsp4j.DiagnosticSeverity;

public class Settings {
  private static final Set<String> defaultEnabled =
      new HashSet<>(Arrays.asList("markdown", "latex", "rsweave"));

  private @Nullable Set<String> enabled = null;
  private @Nullable String languageShortCode = null;
  private @Nullable Map<String, Set<String>> dictionary = null;
  private @Nullable Map<String, Set<String>> disabledRules = null;
  private @Nullable Map<String, Set<String>> enabledRules = null;
  private @Nullable String languageToolHttpServerUri = null;
  private @Nullable Level logLevel = null;
  private @Nullable Map<String, String> latexCommands = null;
  private @Nullable Map<String, String> latexEnvironments = null;
  private @Nullable Map<String, String> markdownNodes = null;
  private @Nullable Set<IgnoreRuleSentencePair> ignoreRuleSentencePairs = null;
  private @Nullable String motherTongueShortCode = null;
  private @Nullable String languageModelRulesDirectory = null;
  private @Nullable String neuralNetworkModelRulesDirectory = null;
  private @Nullable String word2VecModelRulesDirectory = null;
  private @Nullable Integer sentenceCacheSize = null;
  private @Nullable DiagnosticSeverity diagnosticSeverity = null;
  private @Nullable CheckFrequency checkFrequency = null;
  private @Nullable Boolean clearDiagnosticsWhenClosingFile = null;

  public Settings() {
  }

  /**
   * Copy constructor.
   *
   * @param obj object to copy
   */
  public Settings(Settings obj) {
    this.enabled = obj.enabled;
    this.languageShortCode = obj.languageShortCode;
    this.dictionary = ((obj.dictionary == null) ? null : copyMapOfSets(obj.dictionary));
    this.disabledRules = ((obj.disabledRules == null) ? null : copyMapOfSets(obj.disabledRules));
    this.enabledRules = ((obj.enabledRules == null) ? null : copyMapOfSets(obj.enabledRules));
    this.languageToolHttpServerUri = obj.languageToolHttpServerUri;
    this.logLevel = obj.logLevel;
    this.latexCommands = ((obj.latexCommands == null) ? null
        : new HashMap<>(obj.latexCommands));
    this.latexEnvironments = ((obj.latexEnvironments == null) ? null
        : new HashMap<>(obj.latexEnvironments));
    this.markdownNodes = ((obj.markdownNodes == null) ? null
        : new HashMap<>(obj.markdownNodes));
    this.ignoreRuleSentencePairs = ((obj.ignoreRuleSentencePairs == null) ? null
        : new HashSet<>(obj.ignoreRuleSentencePairs));
    this.motherTongueShortCode = obj.motherTongueShortCode;
    this.languageModelRulesDirectory = obj.languageModelRulesDirectory;
    this.neuralNetworkModelRulesDirectory = obj.neuralNetworkModelRulesDirectory;
    this.word2VecModelRulesDirectory = obj.word2VecModelRulesDirectory;
    this.sentenceCacheSize = obj.sentenceCacheSize;
    this.diagnosticSeverity = ((obj.diagnosticSeverity == null) ? null : obj.diagnosticSeverity);
    this.checkFrequency = ((obj.checkFrequency == null) ? null : obj.checkFrequency);
    this.clearDiagnosticsWhenClosingFile = ((obj.clearDiagnosticsWhenClosingFile == null) ? null
        : obj.clearDiagnosticsWhenClosingFile);
  }

  public Settings(JsonElement jsonSettings, JsonElement jsonWorkspaceSpecificSettings) {
    setSettings(jsonSettings, jsonWorkspaceSpecificSettings);
  }

  private static Map<String, Set<String>> copyMapOfSets(Map<String, Set<String>> map) {
    Map<String, Set<String>> mapCopy = new HashMap<>();

    for (Map.Entry<String, Set<String>> entry : map.entrySet()) {
      mapCopy.put(entry.getKey(), new HashSet<>(entry.getValue()));
    }

    return mapCopy;
  }

  private static JsonElement getSettingFromJson(JsonElement jsonSettings, String name) {
    for (String component : name.split("\\.")) {
      jsonSettings = jsonSettings.getAsJsonObject().get(component);
    }

    return jsonSettings;
  }

  private static Set<String> convertJsonArrayToSet(JsonArray array) {
    Set<String> list = new HashSet<>();
    for (JsonElement element : array) list.add(element.getAsString());
    return list;
  }

  private static Map<String, String> convertJsonObjectToMap(JsonObject object) {
    Map<String, String> map = new HashMap<>();

    for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
      map.put(entry.getKey(), entry.getValue().getAsString());
    }

    return map;
  }

  private static Map<String, Set<String>> convertJsonObjectToMapOfSets(JsonObject object) {
    Map<String, Set<String>> map = new HashMap<>();

    for (String key : object.keySet()) {
      map.put(key, convertJsonArrayToSet(object.get(key).getAsJsonArray()));
    }

    return map;
  }

  private static void mergeMapOfSets(Map<String, Set<String>> map1, Map<String, Set<String>> map2) {
    for (Map.Entry<String, Set<String>> entry2 : map2.entrySet()) {
      String key = entry2.getKey();
      if (!map1.containsKey(key)) map1.put(key, new HashSet<>());
      Set<String> set1 = map1.get(key);
      Set<String> set2 = entry2.getValue();

      for (String string : set2) {
        if (string.startsWith("-")) {
          set1.remove(string.substring(1));
        } else {
          set1.add(string);
        }
      }
    }
  }

  private static boolean mapOfSetsEqual(@Nullable Map<String, Set<String>> map1,
        @Nullable Map<String, Set<String>> map2, @Nullable String key) {
    if (key == null) return true;

    if ((map1 != null) && (map2 != null)) {
      @Nullable Set<String> set1 = map1.get(key);
      @Nullable Set<String> set2 = map2.get(key);
      return ((set1 != null) ? set1.equals(set2) : (set2 == null));
    } else {
      return ((map1 != null) ? (map2 != null) : (map2 == null));
    }
  }

  private static int mapOfSetsHashCode(
        @Nullable Map<String, Set<String>> map, @Nullable String key) {
    return (((map != null) && (key != null) && map.containsKey(key)) ? map.get(key).hashCode() : 0);
  }

  private void setSettings(@UnknownInitialization(Object.class) Settings this,
        JsonElement jsonSettings, JsonElement jsonWorkspaceSpecificSettings) {
    try {
      JsonElement jsonElement = getSettingFromJson(jsonSettings, "enabled");

      if (jsonElement.isJsonArray()) {
        this.enabled = convertJsonArrayToSet(jsonElement.getAsJsonArray());
      } else {
        this.enabled = (jsonElement.getAsBoolean() ? defaultEnabled : Collections.emptySet());
      }
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      this.enabled = null;
    }

    try {
      this.languageShortCode = getSettingFromJson(jsonSettings, "language").getAsString();
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      this.languageShortCode = null;
    }

    this.dictionary = new HashMap<>();
    this.disabledRules = new HashMap<>();
    this.enabledRules = new HashMap<>();

    // fixes false-positive argument.type.incompatible warnings
    Map<String, Set<String>> dictionary = this.dictionary;
    Map<String, Set<String>> disabledRules = this.disabledRules;
    Map<String, Set<String>> enabledRules = this.enabledRules;

    try {
      mergeMapOfSets(dictionary, convertJsonObjectToMapOfSets(
          getSettingFromJson(jsonWorkspaceSpecificSettings, "dictionary").getAsJsonObject()));
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      // setting not set
    }

    try {
      mergeMapOfSets(disabledRules, convertJsonObjectToMapOfSets(
          getSettingFromJson(jsonSettings, "disabledRules").getAsJsonObject()));
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      // setting not set
    }

    try {
      mergeMapOfSets(enabledRules, convertJsonObjectToMapOfSets(
          getSettingFromJson(jsonSettings, "enabledRules").getAsJsonObject()));
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      // setting not set
    }

    try {
      this.languageToolHttpServerUri = getSettingFromJson(
          jsonSettings, "ltex-ls.languageToolHttpServerUri").getAsString();
    } catch (NullPointerException | UnsupportedOperationException e) {
      this.languageToolHttpServerUri = null;
    }

    try {
      this.logLevel = Level.parse(
          getSettingFromJson(jsonSettings, "ltex-ls.logLevel").getAsString().toUpperCase());
    } catch (NullPointerException | UnsupportedOperationException | IllegalArgumentException e) {
      this.logLevel = null;
    }

    this.latexCommands = new HashMap<>();
    this.latexEnvironments = new HashMap<>();
    this.markdownNodes = new HashMap<>();

    // fixes false-positive argument.type.incompatible warnings
    Map<String, String> latexCommands = this.latexCommands;
    Map<String, String> latexEnvironments = this.latexEnvironments;
    Map<String, String> markdownNodes = this.markdownNodes;

    try {
      Set<String> ignoreCommands = convertJsonArrayToSet(
          getSettingFromJson(jsonSettings, "commands.ignore").getAsJsonArray());
      for (String command : ignoreCommands) latexCommands.put(command, "ignore");
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      // setting not set
    }

    try {
      Set<String> dummyCommands = convertJsonArrayToSet(
          getSettingFromJson(jsonSettings, "commands.dummy").getAsJsonArray());
      for (String command : dummyCommands) latexCommands.put(command, "dummy");
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      // setting not set
    }

    try {
      latexCommands.putAll(convertJsonObjectToMap(
          getSettingFromJson(jsonSettings, "latex.commands").getAsJsonObject()));
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      // setting not set
    }

    try {
      Set<String> ignoreEnvironments = convertJsonArrayToSet(
          getSettingFromJson(jsonSettings, "environments.ignore").getAsJsonArray());
      for (String environment : ignoreEnvironments) latexEnvironments.put(environment, "ignore");
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      // setting not set
    }

    try {
      latexEnvironments.putAll(convertJsonObjectToMap(
          getSettingFromJson(jsonSettings, "latex.environments").getAsJsonObject()));
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      // setting not set
    }

    try {
      Set<String> ignoreNodes = convertJsonArrayToSet(
          getSettingFromJson(jsonSettings, "markdown.ignore").getAsJsonArray());
      for (String command : ignoreNodes) markdownNodes.put(command, "ignore");
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      // setting not set
    }

    try {
      Set<String> dummyNodes = convertJsonArrayToSet(
          getSettingFromJson(jsonSettings, "markdown.dummy").getAsJsonArray());
      for (String command : dummyNodes) markdownNodes.put(command, "dummy");
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      // setting not set
    }

    try {
      markdownNodes.putAll(convertJsonObjectToMap(
          getSettingFromJson(jsonSettings, "markdown.nodes").getAsJsonObject()));
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      // setting not set
    }

    try {
      this.ignoreRuleSentencePairs = new HashSet<>();

      // fixes false-positive dereference.of.nullable warning
      Set<IgnoreRuleSentencePair> ignoreRuleSentencePairs = this.ignoreRuleSentencePairs;

      for (JsonElement element :
            getSettingFromJson(jsonSettings, "ignoreRuleInSentence").getAsJsonArray()) {
        JsonObject elementObject = element.getAsJsonObject();
        ignoreRuleSentencePairs.add(new IgnoreRuleSentencePair(
            elementObject.get("rule").getAsString(), elementObject.get("sentence").getAsString()));
      }
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      this.ignoreRuleSentencePairs = null;
    }

    try {
      this.motherTongueShortCode = getSettingFromJson(
          jsonSettings, "additionalRules.motherTongue").getAsString();
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      this.motherTongueShortCode = null;
    }

    try {
      this.languageModelRulesDirectory = getSettingFromJson(
          jsonSettings, "additionalRules.languageModel").getAsString();
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      this.languageModelRulesDirectory = null;
    }

    try {
      this.neuralNetworkModelRulesDirectory = getSettingFromJson(
          jsonSettings, "additionalRules.neuralNetworkModel").getAsString();
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      this.neuralNetworkModelRulesDirectory = null;
    }

    try {
      this.word2VecModelRulesDirectory = getSettingFromJson(
          jsonSettings, "additionalRules.word2VecModel").getAsString();
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      this.word2VecModelRulesDirectory = null;
    }

    try {
      this.sentenceCacheSize = getSettingFromJson(
          jsonSettings, "sentenceCacheSize").getAsInt();
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      this.sentenceCacheSize = null;
    }

    try {
      String diagnosticSeverityString =
          getSettingFromJson(jsonSettings, "diagnosticSeverity").getAsString();

      if (diagnosticSeverityString.equals("error")) {
        this.diagnosticSeverity = DiagnosticSeverity.Error;
      } else if (diagnosticSeverityString.equals("warning")) {
        this.diagnosticSeverity = DiagnosticSeverity.Warning;
      } else if (diagnosticSeverityString.equals("information")) {
        this.diagnosticSeverity = DiagnosticSeverity.Information;
      } else if (diagnosticSeverityString.equals("hint")) {
        this.diagnosticSeverity = DiagnosticSeverity.Hint;
      } else {
        this.diagnosticSeverity = null;
      }
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      this.diagnosticSeverity = null;
    }

    try {
      String checkFrequencyString =
          getSettingFromJson(jsonSettings, "checkFrequency").getAsString();

      if (checkFrequencyString.equals("edit")) {
        this.checkFrequency = CheckFrequency.EDIT;
      } else if (checkFrequencyString.equals("save")) {
        this.checkFrequency = CheckFrequency.SAVE;
      } else if (checkFrequencyString.equals("manual")) {
        this.checkFrequency = CheckFrequency.MANUAL;
      } else {
        this.checkFrequency = null;
      }
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      this.checkFrequency = null;
    }

    try {
      this.clearDiagnosticsWhenClosingFile = getSettingFromJson(
          jsonSettings, "clearDiagnosticsWhenClosingFile").getAsBoolean();
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      this.clearDiagnosticsWhenClosingFile = null;
    }
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if ((obj == null) || !Settings.class.isAssignableFrom(obj.getClass())) return false;
    Settings other = (Settings)obj;

    if ((this.enabled == null) ? (other.enabled != null) : !this.enabled.equals(other.enabled)) {
      return false;
    }

    if ((this.languageShortCode == null) ? (other.languageShortCode != null) :
          !this.languageShortCode.equals(other.languageShortCode)) {
      return false;
    }

    if (!mapOfSetsEqual(this.dictionary, other.dictionary, this.languageShortCode)) {
      return false;
    }

    if (!mapOfSetsEqual(this.disabledRules, other.disabledRules, this.languageShortCode)) {
      return false;
    }

    if (!mapOfSetsEqual(this.enabledRules, other.enabledRules, this.languageShortCode)) {
      return false;
    }

    if ((this.languageToolHttpServerUri == null) ? (other.languageToolHttpServerUri != null) :
          !this.languageToolHttpServerUri.equals(other.languageToolHttpServerUri)) {
      return false;
    }

    if ((this.logLevel == null) ? (other.logLevel != null) :
          !this.logLevel.equals(other.logLevel)) {
      return false;
    }

    if ((this.latexCommands == null) ? (other.latexCommands != null) :
          ((other.latexCommands == null) || !this.latexCommands.equals(other.latexCommands))) {
      return false;
    }

    if ((this.latexEnvironments == null) ? (other.latexEnvironments != null) :
          ((other.latexEnvironments == null)
            || !this.latexEnvironments.equals(other.latexEnvironments))) {
      return false;
    }

    if ((this.markdownNodes == null) ? (other.markdownNodes != null) :
          ((other.markdownNodes == null) || !this.markdownNodes.equals(other.markdownNodes))) {
      return false;
    }

    if ((this.ignoreRuleSentencePairs == null) ? (other.ignoreRuleSentencePairs != null) :
          !this.ignoreRuleSentencePairs.equals(other.ignoreRuleSentencePairs)) {
      return false;
    }

    if ((this.motherTongueShortCode == null) ? (other.motherTongueShortCode != null) :
          !this.motherTongueShortCode.equals(other.motherTongueShortCode)) {
      return false;
    }

    if ((this.languageModelRulesDirectory == null) ? (other.languageModelRulesDirectory != null) :
          !this.languageModelRulesDirectory.equals(other.languageModelRulesDirectory)) {
      return false;
    }

    if ((this.neuralNetworkModelRulesDirectory == null)
          ? (other.neuralNetworkModelRulesDirectory != null)
          : !this.neuralNetworkModelRulesDirectory.equals(other.neuralNetworkModelRulesDirectory)) {
      return false;
    }

    if ((this.word2VecModelRulesDirectory == null) ? (other.word2VecModelRulesDirectory != null) :
          !this.word2VecModelRulesDirectory.equals(other.word2VecModelRulesDirectory)) {
      return false;
    }

    if ((this.sentenceCacheSize == null) ? (other.sentenceCacheSize != null) :
          !this.sentenceCacheSize.equals(other.sentenceCacheSize)) {
      return false;
    }

    if ((this.diagnosticSeverity == null) ? (other.diagnosticSeverity != null) :
          (this.diagnosticSeverity != other.diagnosticSeverity)) {
      return false;
    }

    if ((this.checkFrequency == null) ? (other.checkFrequency != null) :
          (this.checkFrequency != other.checkFrequency)) {
      return false;
    }

    if ((this.clearDiagnosticsWhenClosingFile == null)
          ? (other.clearDiagnosticsWhenClosingFile != null)
          : !this.clearDiagnosticsWhenClosingFile.equals(other.clearDiagnosticsWhenClosingFile)) {
      return false;
    }

    return true;
  }

  public Set<SettingsDifference> getDifferencesRelevantForLanguageTool(@Nullable Settings other) {
    Set<SettingsDifference> differences = new HashSet<>();

    if (other == null) {
      differences.add(new SettingsDifference("settings", "non-null", "null"));
      return differences;
    }

    if (!mapOfSetsEqual(this.dictionary, other.dictionary, this.languageShortCode)) {
      differences.add(new SettingsDifference("dictionary", this.dictionary, other.dictionary));
    }

    if (!mapOfSetsEqual(this.disabledRules, other.disabledRules, this.languageShortCode)) {
      differences.add(new SettingsDifference("disabledRules",
          this.disabledRules, other.disabledRules));
    }

    if (!mapOfSetsEqual(this.enabledRules, other.enabledRules, this.languageShortCode)) {
      differences.add(new SettingsDifference("enabledRules",
          this.enabledRules, other.enabledRules));
    }

    if ((this.languageToolHttpServerUri == null) ? (other.languageToolHttpServerUri != null) :
          !this.languageToolHttpServerUri.equals(other.languageToolHttpServerUri)) {
      differences.add(new SettingsDifference("ltex-ls.languageToolHttpServerUri",
          this.languageToolHttpServerUri, other.languageToolHttpServerUri));
    }

    if ((this.motherTongueShortCode == null) ? (other.motherTongueShortCode != null) :
          !this.motherTongueShortCode.equals(other.motherTongueShortCode)) {
      differences.add(new SettingsDifference("additionalRules.motherTongue",
          this.motherTongueShortCode, other.motherTongueShortCode));
    }

    if ((this.languageModelRulesDirectory == null) ? (other.languageModelRulesDirectory != null) :
          !this.languageModelRulesDirectory.equals(other.languageModelRulesDirectory)) {
      differences.add(new SettingsDifference("additionalRules.languageModel",
          this.languageModelRulesDirectory, other.languageModelRulesDirectory));
    }

    if ((this.neuralNetworkModelRulesDirectory == null)
          ? (other.neuralNetworkModelRulesDirectory != null)
          : !this.neuralNetworkModelRulesDirectory.equals(other.neuralNetworkModelRulesDirectory)) {
      differences.add(new SettingsDifference("additionalRules.neuralNetworkModel",
          this.neuralNetworkModelRulesDirectory, other.neuralNetworkModelRulesDirectory));
    }

    if ((this.word2VecModelRulesDirectory == null) ? (other.word2VecModelRulesDirectory != null) :
          !this.word2VecModelRulesDirectory.equals(other.word2VecModelRulesDirectory)) {
      differences.add(new SettingsDifference("additionalRules.word2VecModel",
          this.word2VecModelRulesDirectory, other.word2VecModelRulesDirectory));
    }

    if ((this.sentenceCacheSize == null) ? (other.sentenceCacheSize != null) :
          !this.sentenceCacheSize.equals(other.sentenceCacheSize)) {
      differences.add(new SettingsDifference("sentenceCacheSize",
          this.sentenceCacheSize, other.sentenceCacheSize));
    }

    return differences;
  }

  @Override
  public int hashCode() {
    int hash = 3;

    hash = 53 * hash + ((this.enabled != null) ? this.enabled.hashCode() : 0);
    hash = 53 * hash + ((this.languageShortCode != null) ? this.languageShortCode.hashCode() : 0);
    hash = 53 * hash + mapOfSetsHashCode(this.dictionary, this.languageShortCode);
    hash = 53 * hash + mapOfSetsHashCode(this.disabledRules, this.languageShortCode);
    hash = 53 * hash + mapOfSetsHashCode(this.enabledRules, this.languageShortCode);
    hash = 53 * hash + ((this.languageToolHttpServerUri != null)
        ? this.languageToolHttpServerUri.hashCode() : 0);
    hash = 53 * hash + ((this.logLevel != null) ? this.logLevel.hashCode() : 0);
    hash = 53 * hash + ((this.latexCommands != null) ? this.latexCommands.hashCode() : 0);
    hash = 53 * hash + ((this.latexEnvironments != null) ? this.latexEnvironments.hashCode() : 0);
    hash = 53 * hash + ((this.markdownNodes != null) ? this.markdownNodes.hashCode() : 0);
    hash = 53 * hash + ((this.ignoreRuleSentencePairs != null)
        ? this.ignoreRuleSentencePairs.hashCode() : 0);
    hash = 53 * hash + ((this.motherTongueShortCode != null)
        ? this.motherTongueShortCode.hashCode() : 0);
    hash = 53 * hash + ((this.languageModelRulesDirectory != null)
        ? this.languageModelRulesDirectory.hashCode() : 0);
    hash = 53 * hash + ((this.neuralNetworkModelRulesDirectory != null)
        ? this.neuralNetworkModelRulesDirectory.hashCode() : 0);
    hash = 53 * hash + ((this.word2VecModelRulesDirectory != null)
        ? this.word2VecModelRulesDirectory.hashCode() : 0);
    hash = 53 * hash + ((this.sentenceCacheSize != null)
        ? this.sentenceCacheSize.hashCode() : 0);
    hash = 53 * hash + ((this.diagnosticSeverity != null) ? this.diagnosticSeverity.hashCode() : 0);
    hash = 53 * hash + ((this.checkFrequency != null) ? this.checkFrequency.hashCode() : 0);
    hash = 53 * hash + ((this.clearDiagnosticsWhenClosingFile != null)
        ? this.clearDiagnosticsWhenClosingFile.hashCode() : 0);

    return hash;
  }

  private static <T> T getDefault(@Nullable T obj, T defaultValue) {
    return ((obj != null) ? obj : defaultValue);
  }

  private static Set<String> getDefault(@Nullable Map<String, Set<String>> map, String key,
        Set<String> defaultValue) {
    return (((map != null) && map.containsKey(key)) ? map.get(key) : defaultValue);
  }

  public Set<String> getEnabled() {
    return getDefault(this.enabled, defaultEnabled);
  }

  public String getLanguageShortCode() {
    return getDefault(this.languageShortCode, "en-US");
  }

  public Set<String> getDictionary() {
    return Collections.unmodifiableSet(getDefault(
        this.dictionary, getLanguageShortCode(), Collections.emptySet()));
  }

  public Set<String> getDisabledRules() {
    return Collections.unmodifiableSet(getDefault(
        this.disabledRules, getLanguageShortCode(), Collections.emptySet()));
  }

  public Set<String> getEnabledRules() {
    return Collections.unmodifiableSet(getDefault(
        this.enabledRules, getLanguageShortCode(), Collections.emptySet()));
  }

  public String getLanguageToolHttpServerUri() {
    return getDefault(this.languageToolHttpServerUri, "");
  }

  public Level getLogLevel() {
    return getDefault(this.logLevel, Level.FINE);
  }

  public Map<String, String> getLatexCommands() {
    return Collections.unmodifiableMap(
        getDefault(this.latexCommands, Collections.emptyMap()));
  }

  public Map<String, String> getLatexEnvironments() {
    return Collections.unmodifiableMap(
        getDefault(this.latexEnvironments, Collections.emptyMap()));
  }

  public Map<String, String> getMarkdownNodes() {
    return Collections.unmodifiableMap(
        getDefault(this.markdownNodes, Collections.emptyMap()));
  }

  public Set<IgnoreRuleSentencePair> getIgnoreRuleSentencePairs() {
    return Collections.unmodifiableSet(
        getDefault(this.ignoreRuleSentencePairs, Collections.emptySet()));
  }

  public String getMotherTongueShortCode() {
    return getDefault(this.motherTongueShortCode, "");
  }

  public String getLanguageModelRulesDirectory() {
    return Tools.normalizePath(getDefault(this.languageModelRulesDirectory, ""));
  }

  public String getNeuralNetworkModelRulesDirectory() {
    return Tools.normalizePath(getDefault(this.neuralNetworkModelRulesDirectory, ""));
  }

  public String getWord2VecModelRulesDirectory() {
    return Tools.normalizePath(getDefault(this.word2VecModelRulesDirectory, ""));
  }

  public Integer getSentenceCacheSize() {
    return getDefault(this.sentenceCacheSize, 2000);
  }

  public DiagnosticSeverity getDiagnosticSeverity() {
    return getDefault(this.diagnosticSeverity, DiagnosticSeverity.Information);
  }

  public CheckFrequency getCheckFrequency() {
    return getDefault(this.checkFrequency, CheckFrequency.EDIT);
  }

  public Boolean getClearDiagnosticsWhenClosingFile() {
    return getDefault(this.clearDiagnosticsWhenClosingFile, true);
  }

  public Settings withEnabled(Set<String> enabled) {
    Settings obj = new Settings(this);
    obj.enabled = enabled;
    return obj;
  }

  public Settings withEnabled(boolean enabled) {
    return withEnabled(enabled ? defaultEnabled : Collections.emptySet());
  }

  public Settings withLanguageShortCode(String languageShortCode) {
    Settings obj = new Settings(this);
    obj.languageShortCode = languageShortCode;
    return obj;
  }

  public Settings withDictionary(Set<String> dictionary) {
    Settings obj = new Settings(this);
    if (obj.dictionary == null) obj.dictionary = new HashMap<>();
    obj.dictionary.put(getLanguageShortCode(), new HashSet<>(dictionary));
    return obj;
  }

  public Settings withDisabledRules(Set<String> disabledRules) {
    Settings obj = new Settings(this);
    if (obj.disabledRules == null) obj.disabledRules = new HashMap<>();
    obj.disabledRules.put(getLanguageShortCode(), new HashSet<>(disabledRules));
    return obj;
  }

  public Settings withEnabledRules(Set<String> enabledRules) {
    Settings obj = new Settings(this);
    if (obj.enabledRules == null) obj.enabledRules = new HashMap<>();
    obj.enabledRules.put(getLanguageShortCode(), new HashSet<>(enabledRules));
    return obj;
  }

  public Settings withLanguageToolHttpServerUri(String languageToolHttpServerUri) {
    Settings obj = new Settings(this);
    obj.languageToolHttpServerUri = languageToolHttpServerUri;
    return obj;
  }

  public Settings withLogLevel(Level logLevel) {
    Settings obj = new Settings(this);
    obj.logLevel = logLevel;
    return obj;
  }

  public Settings withLatexCommands(Map<String, String> latexCommands) {
    Settings obj = new Settings(this);
    obj.latexCommands = new HashMap<>(latexCommands);
    return obj;
  }

  public Settings withLatexEnvironments(Map<String, String> latexEnvironments) {
    Settings obj = new Settings(this);
    obj.latexEnvironments = new HashMap<>(latexEnvironments);
    return obj;
  }

  public Settings withMarkdownNodes(Map<String, String> markdownNodes) {
    Settings obj = new Settings(this);
    obj.markdownNodes = new HashMap<>(markdownNodes);
    return obj;
  }

  public Settings withIgnoreRuleSentencePairs(Set<IgnoreRuleSentencePair> ignoreRuleSentencePairs) {
    Settings obj = new Settings(this);
    obj.ignoreRuleSentencePairs = new HashSet<>(ignoreRuleSentencePairs);
    return obj;
  }

  public Settings withMotherTongueShortCode(String motherTongueShortCode) {
    Settings obj = new Settings(this);
    obj.motherTongueShortCode = motherTongueShortCode;
    return obj;
  }

  public Settings withLanguageModelRulesDirectory(String languageModelRulesDirectory) {
    Settings obj = new Settings(this);
    obj.languageModelRulesDirectory = languageModelRulesDirectory;
    return obj;
  }

  public Settings withNeuralNetworkModelRulesDirectory(String neuralNetworkModelRulesDirectory) {
    Settings obj = new Settings(this);
    obj.neuralNetworkModelRulesDirectory = neuralNetworkModelRulesDirectory;
    return obj;
  }

  public Settings withWord2VecModelRulesDirectory(String word2VecModelRulesDirectory) {
    Settings obj = new Settings(this);
    obj.word2VecModelRulesDirectory = word2VecModelRulesDirectory;
    return obj;
  }

  public Settings withSentenceCacheSize(Integer sentenceCacheSize) {
    Settings obj = new Settings(this);
    obj.sentenceCacheSize = sentenceCacheSize;
    return obj;
  }

  public Settings withDiagnosticSeverity(DiagnosticSeverity diagnosticSeverity) {
    Settings obj = new Settings(this);
    obj.diagnosticSeverity = diagnosticSeverity;
    return obj;
  }

  public Settings withCheckFrequency(CheckFrequency checkFrequency) {
    Settings obj = new Settings(this);
    obj.checkFrequency = checkFrequency;
    return obj;
  }

  public Settings withClearDiagnosticsWhenClosingFile(Boolean clearDiagnosticsWhenClosingFile) {
    Settings obj = new Settings(this);
    obj.clearDiagnosticsWhenClosingFile = clearDiagnosticsWhenClosingFile;
    return obj;
  }
}
