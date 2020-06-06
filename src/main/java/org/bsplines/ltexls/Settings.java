package org.bsplines.ltexls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.gson.*;

import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.checker.nullness.qual.Nullable;

import org.eclipse.lsp4j.DiagnosticSeverity;

public class Settings {
  private static final List<String> defaultDummyMarkdownNodeTypes =
      Arrays.asList("AutoLink", "Code");
  private static final List<String> defaultIgnoreMarkdownNodeTypes =
      Arrays.asList("CodeBlock", "FencedCodeBlock", "IndentedCodeBlock");

  private @Nullable String languageShortCode = null;
  private @Nullable List<String> dictionary = null;
  private @Nullable List<String> disabledRules = null;
  private @Nullable List<String> enabledRules = null;
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

  public Settings(Settings obj) {
    this.languageShortCode = obj.languageShortCode;
    this.dictionary = ((obj.dictionary == null) ? null : new ArrayList<>(obj.dictionary));
    this.disabledRules = ((obj.disabledRules == null) ? null : new ArrayList<>(obj.disabledRules));
    this.enabledRules = ((obj.enabledRules == null) ? null : new ArrayList<>(obj.enabledRules));
    this.languageToolHttpServerUri = obj.languageToolHttpServerUri;
    this.dummyCommandPrototypes = ((obj.dummyCommandPrototypes == null) ? null :
        new ArrayList<>(obj.dummyCommandPrototypes));
    this.ignoreCommandPrototypes = ((obj.ignoreCommandPrototypes == null) ? null :
        new ArrayList<>(obj.ignoreCommandPrototypes));
    this.ignoreEnvironments = ((obj.ignoreEnvironments == null) ? null :
        new ArrayList<>(obj.ignoreEnvironments));
    this.dummyMarkdownNodeTypes = ((obj.dummyMarkdownNodeTypes == null) ? null :
        new ArrayList<>(obj.dummyMarkdownNodeTypes));
    this.ignoreMarkdownNodeTypes = ((obj.ignoreMarkdownNodeTypes == null) ? null :
        new ArrayList<>(obj.ignoreMarkdownNodeTypes));
    this.ignoreRuleSentencePairs = ((obj.ignoreRuleSentencePairs == null) ? null :
        new ArrayList<>(obj.ignoreRuleSentencePairs));
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

  private static JsonElement getSettingFromJSON(JsonElement jsonSettings, String name) {
    for (String component : name.split("\\.")) {
      jsonSettings = jsonSettings.getAsJsonObject().get(component);
    }

    return jsonSettings;
  }

  private static List<String> convertJsonArrayToList(JsonArray array) {
    List<String> result = new ArrayList<>();
    for (JsonElement element : array) result.add(element.getAsString());
    return result;
  }

  public void setSettings(@UnknownInitialization(Object.class) Settings this,
        JsonElement jsonSettings) {
    try {
      this.languageShortCode = getSettingFromJSON(jsonSettings, "language").getAsString();
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      this.languageShortCode = null;
    }

    this.dictionary = new ArrayList<>();
    this.disabledRules = new ArrayList<>();
    this.enabledRules = new ArrayList<>();

    if (languageShortCode != null) {
      // fixes false-positive argument.type.incompatible warnings
      String languageShortCode = this.languageShortCode;
      List<String> dictionary = this.dictionary;
      List<String> disabledRules = this.disabledRules;
      List<String> enabledRules = this.enabledRules;

      try {
        dictionary.addAll(convertJsonArrayToList(
            getSettingFromJSON(jsonSettings, "dictionary").getAsJsonObject().
            get(languageShortCode).getAsJsonArray()));
      } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      }

      try {
        dictionary.addAll(convertJsonArrayToList(
            getSettingFromJSON(jsonSettings,
            languageShortCode + ".dictionary").getAsJsonArray()));
      } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      }

      try {
        disabledRules.addAll(convertJsonArrayToList(
            getSettingFromJSON(jsonSettings, "disabledRules").getAsJsonObject().
            get(languageShortCode).getAsJsonArray()));
      } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      }

      try {
        disabledRules.addAll(convertJsonArrayToList(
            getSettingFromJSON(jsonSettings,
            languageShortCode + ".disabledRules").getAsJsonArray()));
      } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      }

      try {
        enabledRules.addAll(convertJsonArrayToList(
            getSettingFromJSON(jsonSettings, "enabledRules").getAsJsonObject().
            get(languageShortCode).getAsJsonArray()));
      } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      }

      try {
        enabledRules.addAll(convertJsonArrayToList(
            getSettingFromJSON(jsonSettings,
            languageShortCode + ".enabledRules").getAsJsonArray()));
      } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      }
    }

    try {
      this.languageToolHttpServerUri = getSettingFromJSON(
          jsonSettings, "ltex-ls.languageToolHttpServerUri").getAsString();
    } catch (NullPointerException | UnsupportedOperationException e) {
      this.languageToolHttpServerUri = null;
    }

    try {
      this.dummyCommandPrototypes = convertJsonArrayToList(
          getSettingFromJSON(jsonSettings, "commands.dummy").getAsJsonArray());
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      this.dummyCommandPrototypes = null;
    }

    try {
      this.ignoreCommandPrototypes = convertJsonArrayToList(
          getSettingFromJSON(jsonSettings, "commands.ignore").getAsJsonArray());
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      this.ignoreCommandPrototypes = null;
    }

    try {
      this.ignoreEnvironments = convertJsonArrayToList(
          getSettingFromJSON(jsonSettings, "environments.ignore").getAsJsonArray());
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      this.ignoreEnvironments = null;
    }

    try {
      this.dummyMarkdownNodeTypes = convertJsonArrayToList(
          getSettingFromJSON(jsonSettings, "markdown.dummy").getAsJsonArray());
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      this.dummyMarkdownNodeTypes = null;
    }

    try {
      this.ignoreMarkdownNodeTypes = convertJsonArrayToList(
          getSettingFromJSON(jsonSettings, "markdown.ignore").getAsJsonArray());
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      this.ignoreMarkdownNodeTypes = null;
    }

    try {
      this.ignoreRuleSentencePairs = new ArrayList<>();

      // fixes false-positive dereference.of.nullable warning
      List<IgnoreRuleSentencePair> ignoreRuleSentencePairs = this.ignoreRuleSentencePairs;

      for (JsonElement element :
            getSettingFromJSON(jsonSettings, "ignoreRuleInSentence").getAsJsonArray()) {
        JsonObject elementObject = element.getAsJsonObject();
        ignoreRuleSentencePairs.add(new IgnoreRuleSentencePair(
            elementObject.get("rule").getAsString(), elementObject.get("sentence").getAsString()));
      }
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      this.ignoreRuleSentencePairs = null;
    }

    try {
      this.motherTongueShortCode = getSettingFromJSON(
          jsonSettings, "additionalRules.motherTongue").getAsString();
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      this.motherTongueShortCode = null;
    }

    try {
      this.languageModelRulesDirectory = getSettingFromJSON(
          jsonSettings, "additionalRules.languageModel").getAsString();
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      this.languageModelRulesDirectory = null;
    }

    try {
      this.neuralNetworkModelRulesDirectory = getSettingFromJSON(
          jsonSettings, "additionalRules.neuralNetworkModel").getAsString();
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      this.neuralNetworkModelRulesDirectory = null;
    }

    try {
      this.word2VecModelRulesDirectory = getSettingFromJSON(
          jsonSettings, "additionalRules.word2VecModel").getAsString();
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      this.word2VecModelRulesDirectory = null;
    }

    try {
      this.sentenceCacheSize = getSettingFromJSON(
          jsonSettings, "sentenceCacheSize").getAsInt();
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      try {
        this.sentenceCacheSize = getSettingFromJSON(
            jsonSettings, "performance.sentenceCacheSize").getAsInt();
      } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e2) {
        this.sentenceCacheSize = null;
      }
    }

    try {
      String diagnosticSeverityString =
          getSettingFromJSON(jsonSettings, "diagnosticSeverity").getAsString();

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

    if ((languageShortCode == null) ? (other.languageShortCode != null) :
          !languageShortCode.equals(other.languageShortCode)) {
      return false;
    }

    if ((dictionary == null) ? (other.dictionary != null) :
          !dictionary.equals(other.dictionary)) {
      return false;
    }

    if ((disabledRules == null) ? (other.disabledRules != null) :
          !disabledRules.equals(other.disabledRules)) {
      return false;
    }

    if ((enabledRules == null) ? (other.enabledRules != null) :
          !enabledRules.equals(other.enabledRules)) {
      return false;
    }

    if ((languageToolHttpServerUri == null) ? (other.languageToolHttpServerUri != null) :
          !languageToolHttpServerUri.equals(other.languageToolHttpServerUri)) {
      return false;
    }

    if ((dummyCommandPrototypes == null) ? (other.dummyCommandPrototypes != null) :
          !dummyCommandPrototypes.equals(other.dummyCommandPrototypes)) {
      return false;
    }

    if ((ignoreCommandPrototypes == null) ? (other.ignoreCommandPrototypes != null) :
          !ignoreCommandPrototypes.equals(other.ignoreCommandPrototypes)) {
      return false;
    }

    if ((ignoreEnvironments == null) ? (other.ignoreEnvironments != null) :
          !ignoreEnvironments.equals(other.ignoreEnvironments)) {
      return false;
    }

    if ((dummyMarkdownNodeTypes == null) ? (other.dummyMarkdownNodeTypes != null) :
          !dummyMarkdownNodeTypes.equals(other.dummyMarkdownNodeTypes)) {
      return false;
    }

    if ((ignoreMarkdownNodeTypes == null) ? (other.ignoreMarkdownNodeTypes != null) :
          !ignoreMarkdownNodeTypes.equals(other.ignoreMarkdownNodeTypes)) {
      return false;
    }

    if ((ignoreRuleSentencePairs == null) ? (other.ignoreRuleSentencePairs != null) :
          !ignoreRuleSentencePairs.equals(other.ignoreRuleSentencePairs)) {
      return false;
    }

    if ((motherTongueShortCode == null) ? (other.motherTongueShortCode != null) :
          !motherTongueShortCode.equals(other.motherTongueShortCode)) {
      return false;
    }

    if ((languageModelRulesDirectory == null) ? (other.languageModelRulesDirectory != null) :
          !languageModelRulesDirectory.equals(other.languageModelRulesDirectory)) {
      return false;
    }

    if ((neuralNetworkModelRulesDirectory == null) ?
          (other.neuralNetworkModelRulesDirectory != null) :
          !neuralNetworkModelRulesDirectory.equals(other.neuralNetworkModelRulesDirectory)) {
      return false;
    }

    if ((word2VecModelRulesDirectory == null) ? (other.word2VecModelRulesDirectory != null) :
          !word2VecModelRulesDirectory.equals(other.word2VecModelRulesDirectory)) {
      return false;
    }

    if ((sentenceCacheSize == null) ? (other.sentenceCacheSize != null) :
          !sentenceCacheSize.equals(other.sentenceCacheSize)) {
      return false;
    }

    if ((diagnosticSeverity == null) ? (other.diagnosticSeverity != null) :
          (diagnosticSeverity != other.diagnosticSeverity)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hash = 3;

    hash = 53 * hash + ((languageShortCode != null) ? languageShortCode.hashCode() : 0);
    hash = 53 * hash + ((dictionary != null) ? dictionary.hashCode() : 0);
    hash = 53 * hash + ((disabledRules != null) ? disabledRules.hashCode() : 0);
    hash = 53 * hash + ((enabledRules != null) ? enabledRules.hashCode() : 0);
    hash = 53 * hash + ((languageToolHttpServerUri != null) ?
        languageToolHttpServerUri.hashCode() : 0);
    hash = 53 * hash + ((dummyCommandPrototypes != null) ? dummyCommandPrototypes.hashCode() : 0);
    hash = 53 * hash + ((ignoreCommandPrototypes != null) ? ignoreCommandPrototypes.hashCode() : 0);
    hash = 53 * hash + ((ignoreEnvironments != null) ? ignoreEnvironments.hashCode() : 0);
    hash = 53 * hash + ((dummyMarkdownNodeTypes != null) ?
        dummyMarkdownNodeTypes.hashCode() : 0);
    hash = 53 * hash + ((ignoreMarkdownNodeTypes != null) ?
        ignoreMarkdownNodeTypes.hashCode() : 0);
    hash = 53 * hash + ((ignoreRuleSentencePairs != null) ? ignoreRuleSentencePairs.hashCode() : 0);
    hash = 53 * hash + ((motherTongueShortCode != null) ? motherTongueShortCode.hashCode() : 0);
    hash = 53 * hash + ((languageModelRulesDirectory != null) ?
        languageModelRulesDirectory.hashCode() : 0);
    hash = 53 * hash + ((neuralNetworkModelRulesDirectory != null) ?
        neuralNetworkModelRulesDirectory.hashCode() : 0);
    hash = 53 * hash + ((word2VecModelRulesDirectory != null) ?
        word2VecModelRulesDirectory.hashCode() : 0);
    hash = 53 * hash + ((sentenceCacheSize != null) ?
        sentenceCacheSize.hashCode() : 0);
    hash = 53 * hash + ((diagnosticSeverity != null) ? diagnosticSeverity.hashCode() : 0);

    return hash;
  }

  private static <T> T getDefault(@Nullable T obj, T default_) {
    return ((obj != null) ? obj : default_);
  }

  public String getLanguageShortCode() {
    return getDefault(languageShortCode, "en-US");
  }

  public List<String> getDictionary() {
    return Collections.unmodifiableList(getDefault(dictionary, Collections.emptyList()));
  }

  public List<String> getDisabledRules() {
    return Collections.unmodifiableList(getDefault(disabledRules, Collections.emptyList()));
  }

  public List<String> getEnabledRules() {
    return Collections.unmodifiableList(getDefault(enabledRules, Collections.emptyList()));
  }

  public String getLanguageToolHttpServerUri() {
    return getDefault(languageToolHttpServerUri, "");
  }

  public List<String> getDummyCommandPrototypes() {
    return Collections.unmodifiableList(
        getDefault(dummyCommandPrototypes, Collections.emptyList()));
  }

  public List<String> getIgnoreCommandPrototypes() {
    return Collections.unmodifiableList(
        getDefault(ignoreCommandPrototypes, Collections.emptyList()));
  }

  public List<String> getIgnoreEnvironments() {
    return Collections.unmodifiableList(
        getDefault(ignoreEnvironments, Collections.emptyList()));
  }

  public List<String> getDummyMarkdownNodeTypes() {
    return Collections.unmodifiableList(
        getDefault(dummyMarkdownNodeTypes, defaultDummyMarkdownNodeTypes));
  }

  public List<String> getIgnoreMarkdownNodeTypes() {
    return Collections.unmodifiableList(
        getDefault(ignoreMarkdownNodeTypes, defaultIgnoreMarkdownNodeTypes));
  }

  public List<IgnoreRuleSentencePair> getIgnoreRuleSentencePairs() {
    return Collections.unmodifiableList(
        getDefault(ignoreRuleSentencePairs, Collections.emptyList()));
  }

  public String getMotherTongueShortCode() {
    return getDefault(motherTongueShortCode, "");
  }

  public String getLanguageModelRulesDirectory() {
    return getDefault(languageModelRulesDirectory, "");
  }

  public String getNeuralNetworkModelRulesDirectory() {
    return getDefault(neuralNetworkModelRulesDirectory, "");
  }

  public String getWord2VecModelRulesDirectory() {
    return getDefault(word2VecModelRulesDirectory, "");
  }

  public Integer getSentenceCacheSize() {
    return getDefault(sentenceCacheSize, 2000);
  }

  public DiagnosticSeverity getDiagnosticSeverity() {
    return getDefault(diagnosticSeverity, DiagnosticSeverity.Information);
  }

  public void setLanguageShortCode(String languageShortCode) {
    this.languageShortCode = languageShortCode;
  }

  public void setDictionary(List<String> dictionary) {
    this.dictionary = new ArrayList<>(dictionary);
  }

  public void setDisabledRules(List<String> disabledRules) {
    this.disabledRules = new ArrayList<>(disabledRules);
  }

  public void setEnabledRules(List<String> enabledRules) {
    this.enabledRules = new ArrayList<>(enabledRules);
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
