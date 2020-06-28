package org.bsplines.ltexls;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.lsp4j.DiagnosticSeverity;

public class Settings {
  private static final List<String> defaultDummyMarkdownNodeTypes =
      Arrays.asList("AutoLink", "Code");
  private static final List<String> defaultIgnoreMarkdownNodeTypes =
      Arrays.asList("CodeBlock", "FencedCodeBlock", "IndentedCodeBlock");

  private @Nullable Boolean enabled = null;
  private @Nullable String languageShortCode = null;
  private @Nullable Map<String, List<String>> dictionary = null;
  private @Nullable Map<String, List<String>> disabledRules = null;
  private @Nullable Map<String, List<String>> enabledRules = null;
  private @Nullable String languageToolHttpServerUri = null;
  private @Nullable List<String> dummyCommandPrototypes = null;
  private @Nullable List<String> ignoreCommandPrototypes = null;
  private @Nullable List<String> ignoreEnvironments = null;
  private @Nullable List<String> dummyMarkdownNodeTypes = null;
  private @Nullable List<String> ignoreMarkdownNodeTypes = null;
  private @Nullable List<IgnoreRuleSentencePair> ignoreRuleSentencePairs = null;
  private @Nullable String motherTongueShortCode = null;
  private @Nullable String languageModelRulesDirectory = null;
  private @Nullable String neuralNetworkModelRulesDirectory = null;
  private @Nullable String word2VecModelRulesDirectory = null;
  private @Nullable Integer sentenceCacheSize = null;
  private @Nullable DiagnosticSeverity diagnosticSeverity = null;

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
    this.dictionary = ((obj.dictionary == null) ? null : copyMapOfLists(obj.dictionary));
    this.disabledRules = ((obj.disabledRules == null) ? null : copyMapOfLists(obj.disabledRules));
    this.enabledRules = ((obj.enabledRules == null) ? null : copyMapOfLists(obj.enabledRules));
    this.languageToolHttpServerUri = obj.languageToolHttpServerUri;
    this.dummyCommandPrototypes = ((obj.dummyCommandPrototypes == null) ? null
        : new ArrayList<>(obj.dummyCommandPrototypes));
    this.ignoreCommandPrototypes = ((obj.ignoreCommandPrototypes == null) ? null
        : new ArrayList<>(obj.ignoreCommandPrototypes));
    this.ignoreEnvironments = ((obj.ignoreEnvironments == null) ? null
        : new ArrayList<>(obj.ignoreEnvironments));
    this.dummyMarkdownNodeTypes = ((obj.dummyMarkdownNodeTypes == null) ? null
        : new ArrayList<>(obj.dummyMarkdownNodeTypes));
    this.ignoreMarkdownNodeTypes = ((obj.ignoreMarkdownNodeTypes == null) ? null
        : new ArrayList<>(obj.ignoreMarkdownNodeTypes));
    this.ignoreRuleSentencePairs = ((obj.ignoreRuleSentencePairs == null) ? null
        : new ArrayList<>(obj.ignoreRuleSentencePairs));
    this.motherTongueShortCode = obj.motherTongueShortCode;
    this.languageModelRulesDirectory = obj.languageModelRulesDirectory;
    this.neuralNetworkModelRulesDirectory = obj.neuralNetworkModelRulesDirectory;
    this.word2VecModelRulesDirectory = obj.word2VecModelRulesDirectory;
    this.sentenceCacheSize = obj.sentenceCacheSize;
    this.diagnosticSeverity = ((obj.diagnosticSeverity == null) ? null : obj.diagnosticSeverity);
  }

  public Settings(JsonElement jsonSettings) {
    setSettings(jsonSettings);
  }

  private static Map<String, List<String>> copyMapOfLists(Map<String, List<String>> map) {
    Map<String, List<String>> mapCopy = new HashMap<>();

    for (Map.Entry<String, List<String>> entry : map.entrySet()) {
      mapCopy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
    }

    return mapCopy;
  }

  private static JsonElement getSettingFromJson(JsonElement jsonSettings, String name) {
    for (String component : name.split("\\.")) {
      jsonSettings = jsonSettings.getAsJsonObject().get(component);
    }

    return jsonSettings;
  }

  private static List<String> convertJsonArrayToList(JsonArray array) {
    List<String> list = new ArrayList<>();
    for (JsonElement element : array) list.add(element.getAsString());
    return list;
  }

  private static Map<String, List<String>> convertJsonObjectToMapOfLists(JsonObject object) {
    Map<String, List<String>> map = new HashMap<>();

    for (String key : object.keySet()) {
      map.put(key, convertJsonArrayToList(object.get(key).getAsJsonArray()));
    }

    return map;
  }

  private static boolean mapOfListsEqual(@Nullable Map<String, List<String>> map1,
        @Nullable Map<String, List<String>> map2, @Nullable String key) {
    if (key == null) return true;

    if ((map1 != null) && (map2 != null)) {
      @Nullable List<String> list1 = map1.get(key);
      @Nullable List<String> list2 = map2.get(key);
      return ((list1 != null) ? list1.equals(list2) : (list2 == null));
    } else {
      return ((map1 != null) ? (map2 != null) : (map2 == null));
    }
  }

  private static int mapOfListsHashCode(
        @Nullable Map<String, List<String>> map, @Nullable String key) {
    return (((map != null) && (key != null) && map.containsKey(key)) ? map.get(key).hashCode() : 0);
  }

  /**
   * Set settings from JSON.
   *
   * @param jsonSettings JSON settings given by the language server
   */
  public void setSettings(@UnknownInitialization(Object.class) Settings this,
        JsonElement jsonSettings) {
    try {
      this.enabled = getSettingFromJson(jsonSettings, "enabled").getAsBoolean();
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
    Map<String, List<String>> dictionary = this.dictionary;
    Map<String, List<String>> disabledRules = this.disabledRules;
    Map<String, List<String>> enabledRules = this.enabledRules;

    try {
      dictionary.putAll(convertJsonObjectToMapOfLists(
          getSettingFromJson(jsonSettings, "dictionary").getAsJsonObject()));
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      // setting not set
    }

    try {
      disabledRules.putAll(convertJsonObjectToMapOfLists(
          getSettingFromJson(jsonSettings, "disabledRules").getAsJsonObject()));
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      // setting not set
    }

    try {
      enabledRules.putAll(convertJsonObjectToMapOfLists(
          getSettingFromJson(jsonSettings, "enabledRules").getAsJsonObject()));
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      // setting not set
    }

    if (this.languageShortCode != null) {
      // fixes false-positive argument.type.incompatible warnings
      String languageShortCode = this.languageShortCode;

      try {
        if (dictionary.get(languageShortCode) == null) throw new NullPointerException();
        dictionary.get(languageShortCode).addAll(convertJsonArrayToList(
            getSettingFromJson(jsonSettings,
            languageShortCode + ".dictionary").getAsJsonArray()));
      } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
        // setting not set
      }

      try {
        if (disabledRules.get(languageShortCode) == null) throw new NullPointerException();
        disabledRules.get(languageShortCode).addAll(convertJsonArrayToList(
            getSettingFromJson(jsonSettings,
            languageShortCode + ".disabledRules").getAsJsonArray()));
      } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
        // setting not set
      }

      try {
        if (enabledRules.get(languageShortCode) == null) throw new NullPointerException();
        enabledRules.get(languageShortCode).addAll(convertJsonArrayToList(
            getSettingFromJson(jsonSettings,
            languageShortCode + ".enabledRules").getAsJsonArray()));
      } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
        // setting not set
      }
    }

    try {
      this.languageToolHttpServerUri = getSettingFromJson(
          jsonSettings, "ltex-ls.languageToolHttpServerUri").getAsString();
    } catch (NullPointerException | UnsupportedOperationException e) {
      this.languageToolHttpServerUri = null;
    }

    try {
      this.dummyCommandPrototypes = convertJsonArrayToList(
          getSettingFromJson(jsonSettings, "commands.dummy").getAsJsonArray());
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      this.dummyCommandPrototypes = null;
    }

    try {
      this.ignoreCommandPrototypes = convertJsonArrayToList(
          getSettingFromJson(jsonSettings, "commands.ignore").getAsJsonArray());
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      this.ignoreCommandPrototypes = null;
    }

    try {
      this.ignoreEnvironments = convertJsonArrayToList(
          getSettingFromJson(jsonSettings, "environments.ignore").getAsJsonArray());
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      this.ignoreEnvironments = null;
    }

    try {
      this.dummyMarkdownNodeTypes = convertJsonArrayToList(
          getSettingFromJson(jsonSettings, "markdown.dummy").getAsJsonArray());
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      this.dummyMarkdownNodeTypes = null;
    }

    try {
      this.ignoreMarkdownNodeTypes = convertJsonArrayToList(
          getSettingFromJson(jsonSettings, "markdown.ignore").getAsJsonArray());
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      this.ignoreMarkdownNodeTypes = null;
    }

    try {
      this.ignoreRuleSentencePairs = new ArrayList<>();

      // fixes false-positive dereference.of.nullable warning
      List<IgnoreRuleSentencePair> ignoreRuleSentencePairs = this.ignoreRuleSentencePairs;

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
      try {
        this.sentenceCacheSize = getSettingFromJson(
            jsonSettings, "performance.sentenceCacheSize").getAsInt();
      } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e2) {
        this.sentenceCacheSize = null;
      }
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
    } catch (NullPointerException | UnsupportedOperationException e) {
      this.diagnosticSeverity = null;
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

    if (!mapOfListsEqual(this.dictionary, other.dictionary, this.languageShortCode)) {
      return false;
    }

    if (!mapOfListsEqual(this.disabledRules, other.disabledRules, this.languageShortCode)) {
      return false;
    }

    if (!mapOfListsEqual(this.enabledRules, other.enabledRules, this.languageShortCode)) {
      return false;
    }

    if ((this.languageToolHttpServerUri == null) ? (other.languageToolHttpServerUri != null) :
          !this.languageToolHttpServerUri.equals(other.languageToolHttpServerUri)) {
      return false;
    }

    if ((this.dummyCommandPrototypes == null) ? (other.dummyCommandPrototypes != null) :
          !this.dummyCommandPrototypes.equals(other.dummyCommandPrototypes)) {
      return false;
    }

    if ((this.ignoreCommandPrototypes == null) ? (other.ignoreCommandPrototypes != null) :
          !this.ignoreCommandPrototypes.equals(other.ignoreCommandPrototypes)) {
      return false;
    }

    if ((this.ignoreEnvironments == null) ? (other.ignoreEnvironments != null) :
          !this.ignoreEnvironments.equals(other.ignoreEnvironments)) {
      return false;
    }

    if ((this.dummyMarkdownNodeTypes == null) ? (other.dummyMarkdownNodeTypes != null) :
          !this.dummyMarkdownNodeTypes.equals(other.dummyMarkdownNodeTypes)) {
      return false;
    }

    if ((this.ignoreMarkdownNodeTypes == null) ? (other.ignoreMarkdownNodeTypes != null) :
          !this.ignoreMarkdownNodeTypes.equals(other.ignoreMarkdownNodeTypes)) {
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

    return true;
  }

  @Override
  public int hashCode() {
    int hash = 3;

    hash = 53 * hash + ((this.enabled != null) ? this.enabled.hashCode() : 0);
    hash = 53 * hash + ((this.languageShortCode != null) ? this.languageShortCode.hashCode() : 0);
    hash = 53 * hash + mapOfListsHashCode(this.dictionary, this.languageShortCode);
    hash = 53 * hash + mapOfListsHashCode(this.disabledRules, this.languageShortCode);
    hash = 53 * hash + mapOfListsHashCode(this.enabledRules, this.languageShortCode);
    hash = 53 * hash + ((this.languageToolHttpServerUri != null)
        ? this.languageToolHttpServerUri.hashCode() : 0);
    hash = 53 * hash + ((this.dummyCommandPrototypes != null)
        ? this.dummyCommandPrototypes.hashCode() : 0);
    hash = 53 * hash + ((this.ignoreCommandPrototypes != null)
        ? this.ignoreCommandPrototypes.hashCode() : 0);
    hash = 53 * hash + ((this.ignoreEnvironments != null) ? this.ignoreEnvironments.hashCode() : 0);
    hash = 53 * hash + ((this.dummyMarkdownNodeTypes != null)
        ? this.dummyMarkdownNodeTypes.hashCode() : 0);
    hash = 53 * hash + ((this.ignoreMarkdownNodeTypes != null)
        ? this.ignoreMarkdownNodeTypes.hashCode() : 0);
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

    return hash;
  }

  private static <T> T getDefault(@Nullable T obj, T defaultValue) {
    return ((obj != null) ? obj : defaultValue);
  }

  private static List<String> getDefault(@Nullable Map<String, List<String>> map, String key,
        List<String> defaultValue) {
    return (((map != null) && map.containsKey(key)) ? map.get(key) : defaultValue);
  }

  public Boolean isEnabled() {
    return getDefault(this.enabled, true);
  }

  public String getLanguageShortCode() {
    return getDefault(this.languageShortCode, "en-US");
  }

  public List<String> getDictionary() {
    return Collections.unmodifiableList(getDefault(
        this.dictionary, getLanguageShortCode(), Collections.emptyList()));
  }

  public List<String> getDisabledRules() {
    return Collections.unmodifiableList(getDefault(
        this.disabledRules, getLanguageShortCode(), Collections.emptyList()));
  }

  public List<String> getEnabledRules() {
    return Collections.unmodifiableList(getDefault(
        this.enabledRules, getLanguageShortCode(), Collections.emptyList()));
  }

  public String getLanguageToolHttpServerUri() {
    return getDefault(this.languageToolHttpServerUri, "");
  }

  public List<String> getDummyCommandPrototypes() {
    return Collections.unmodifiableList(
        getDefault(this.dummyCommandPrototypes, Collections.emptyList()));
  }

  public List<String> getIgnoreCommandPrototypes() {
    return Collections.unmodifiableList(
        getDefault(this.ignoreCommandPrototypes, Collections.emptyList()));
  }

  public List<String> getIgnoreEnvironments() {
    return Collections.unmodifiableList(
        getDefault(this.ignoreEnvironments, Collections.emptyList()));
  }

  public List<String> getDummyMarkdownNodeTypes() {
    return Collections.unmodifiableList(
        getDefault(this.dummyMarkdownNodeTypes, defaultDummyMarkdownNodeTypes));
  }

  public List<String> getIgnoreMarkdownNodeTypes() {
    return Collections.unmodifiableList(
        getDefault(this.ignoreMarkdownNodeTypes, defaultIgnoreMarkdownNodeTypes));
  }

  public List<IgnoreRuleSentencePair> getIgnoreRuleSentencePairs() {
    return Collections.unmodifiableList(
        getDefault(this.ignoreRuleSentencePairs, Collections.emptyList()));
  }

  public String getMotherTongueShortCode() {
    return getDefault(this.motherTongueShortCode, "");
  }

  public String getLanguageModelRulesDirectory() {
    return getDefault(this.languageModelRulesDirectory, "");
  }

  public String getNeuralNetworkModelRulesDirectory() {
    return getDefault(this.neuralNetworkModelRulesDirectory, "");
  }

  public String getWord2VecModelRulesDirectory() {
    return getDefault(this.word2VecModelRulesDirectory, "");
  }

  public Integer getSentenceCacheSize() {
    return getDefault(this.sentenceCacheSize, 2000);
  }

  public DiagnosticSeverity getDiagnosticSeverity() {
    return getDefault(this.diagnosticSeverity, DiagnosticSeverity.Information);
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  public void setLanguageShortCode(String languageShortCode) {
    this.languageShortCode = languageShortCode;
  }

  public void setDictionary(List<String> dictionary) {
    if (this.dictionary == null) this.dictionary = new HashMap<>();
    this.dictionary.put(getLanguageShortCode(), new ArrayList<>(dictionary));
  }

  public void setDisabledRules(List<String> disabledRules) {
    if (this.disabledRules == null) this.disabledRules = new HashMap<>();
    this.disabledRules.put(getLanguageShortCode(), new ArrayList<>(disabledRules));
  }

  public void setEnabledRules(List<String> enabledRules) {
    if (this.enabledRules == null) this.enabledRules = new HashMap<>();
    this.enabledRules.put(getLanguageShortCode(), new ArrayList<>(enabledRules));
  }

  public void setLanguageToolHttpServerUri(String languageToolHttpServerUri) {
    this.languageToolHttpServerUri = languageToolHttpServerUri;
  }

  public void setDummyCommandPrototypes(List<String> dummyCommandPrototypes) {
    this.dummyCommandPrototypes = new ArrayList<>(dummyCommandPrototypes);
  }

  public void setIgnoreCommandPrototypes(List<String> ignoreCommandPrototypes) {
    this.ignoreCommandPrototypes = new ArrayList<>(ignoreCommandPrototypes);
  }

  public void setIgnoreEnvironments(List<String> ignoreEnvironments) {
    this.ignoreEnvironments = new ArrayList<>(ignoreEnvironments);
  }

  public void setDummyMarkdownNodeTypes(List<String> dummyMarkdownNodeTypes) {
    this.dummyMarkdownNodeTypes = new ArrayList<>(dummyMarkdownNodeTypes);
  }

  public void setIgnoreMarkdownNodeTypes(List<String> ignoreMarkdownNodeTypes) {
    this.ignoreMarkdownNodeTypes = new ArrayList<>(ignoreMarkdownNodeTypes);
  }

  public void setIgnoreRuleSentencePairs(List<IgnoreRuleSentencePair> ignoreRuleSentencePairs) {
    this.ignoreRuleSentencePairs = new ArrayList<>(ignoreRuleSentencePairs);
  }

  public void setMotherTongueShortCode(String motherTongueShortCode) {
    this.motherTongueShortCode = motherTongueShortCode;
  }

  public void setLanguageModelRulesDirectory(String languageModelRulesDirectory) {
    this.languageModelRulesDirectory = languageModelRulesDirectory;
  }

  public void setNeuralNetworkModelRulesDirectory(String neuralNetworkModelRulesDirectory) {
    this.neuralNetworkModelRulesDirectory = neuralNetworkModelRulesDirectory;
  }

  public void setWord2VecModelRulesDirectory(String word2VecModelRulesDirectory) {
    this.word2VecModelRulesDirectory = word2VecModelRulesDirectory;
  }

  public void setSentenceCacheSize(Integer sentenceCacheSize) {
    this.sentenceCacheSize = sentenceCacheSize;
  }

  public void setDiagnosticSeverity(DiagnosticSeverity diagnosticSeverity) {
    this.diagnosticSeverity = diagnosticSeverity;
  }
}
