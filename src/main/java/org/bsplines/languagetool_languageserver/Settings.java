package org.bsplines.languagetool_languageserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import com.google.gson.*;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.xtext.xbase.lib.Pair;

public class Settings {
  private String languageShortCode = null;
  private DiagnosticSeverity diagnosticSeverity = null;
  private List<String> dictionary = null;
  private List<String> disabledRules = null;
  private List<String> enabledRules = null;
  private List<String> dummyCommandPrototypes = null;
  private List<String> ignoreCommandPrototypes = null;
  private List<Pair<String, Pattern>> ignoreRuleSentencePairs = null;
  private String motherTongueShortCode = null;
  private String languageModelRulesDirectory = null;
  private String neuralNetworkModelRulesDirectory = null;
  private String word2VecModelRulesDirectory = null;

  public Settings() {
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

  public void setSettings(JsonElement jsonSettings) {
    try {
      languageShortCode = getSettingFromJSON(jsonSettings, "language").getAsString();
    } catch (NullPointerException | UnsupportedOperationException e) {
      languageShortCode = null;
    }

    try {
      String diagnosticSeverityString =
          getSettingFromJSON(jsonSettings, "diagnosticSeverity").getAsString();

      if (diagnosticSeverityString.equals("error")) {
        diagnosticSeverity = DiagnosticSeverity.Error;
      } else if (diagnosticSeverityString.equals("warning")) {
        diagnosticSeverity = DiagnosticSeverity.Warning;
      } else if (diagnosticSeverityString.equals("information")) {
        diagnosticSeverity = DiagnosticSeverity.Information;
      } else if (diagnosticSeverityString.equals("hint")) {
        diagnosticSeverity = DiagnosticSeverity.Hint;
      } else {
        diagnosticSeverity = null;
      }
    } catch (NullPointerException | UnsupportedOperationException e) {
      diagnosticSeverity = null;
    }

    try {
      dictionary = convertJsonArrayToList(
          getSettingFromJSON(jsonSettings, languageShortCode + ".dictionary").
          getAsJsonArray());
    } catch (NullPointerException | UnsupportedOperationException e) {
      dictionary = null;
    }

    try {
      disabledRules = convertJsonArrayToList(
          getSettingFromJSON(jsonSettings, languageShortCode + ".disabledRules").
          getAsJsonArray());
    } catch (NullPointerException | UnsupportedOperationException e) {
      disabledRules = null;
    }

    try {
      enabledRules = convertJsonArrayToList(
          getSettingFromJSON(jsonSettings, languageShortCode + ".enabledRules").
          getAsJsonArray());
    } catch (NullPointerException | UnsupportedOperationException e) {
      enabledRules = null;
    }

    try {
      dummyCommandPrototypes = convertJsonArrayToList(
          getSettingFromJSON(jsonSettings, "commands.dummy").getAsJsonArray());
    } catch (NullPointerException | UnsupportedOperationException e) {
      dummyCommandPrototypes = null;
    }

    try {
      ignoreCommandPrototypes = convertJsonArrayToList(
          getSettingFromJSON(jsonSettings, "commands.ignore").getAsJsonArray());
    } catch (NullPointerException | UnsupportedOperationException e) {
      ignoreCommandPrototypes = null;
    }

    try {
      ignoreRuleSentencePairs = new ArrayList<>();

      for (JsonElement element :
          getSettingFromJSON(jsonSettings, "ignoreRuleInSentence").getAsJsonArray()) {
        JsonObject elementObject = element.getAsJsonObject();
        ignoreRuleSentencePairs.add(new Pair<>(elementObject.get("rule").getAsString(),
            Pattern.compile(elementObject.get("sentence").getAsString())));
      }
    } catch (NullPointerException | UnsupportedOperationException e) {
      ignoreRuleSentencePairs = null;
    }

    try {
      motherTongueShortCode = getSettingFromJSON(
          jsonSettings, "additionalRules.motherTongue").getAsString();
    } catch (NullPointerException | UnsupportedOperationException e) {
      motherTongueShortCode = null;
    }

    try {
      languageModelRulesDirectory = getSettingFromJSON(
          jsonSettings, "additionalRules.languageModel").getAsString();
    } catch (NullPointerException | UnsupportedOperationException e) {
      languageModelRulesDirectory = null;
    }

    try {
      neuralNetworkModelRulesDirectory = getSettingFromJSON(
          jsonSettings, "additionalRules.neuralNetworkModel").getAsString();
    } catch (NullPointerException | UnsupportedOperationException e) {
      neuralNetworkModelRulesDirectory = null;
    }

    try {
      word2VecModelRulesDirectory = getSettingFromJSON(
          jsonSettings, "additionalRules.word2VecModel").getAsString();
    } catch (NullPointerException | UnsupportedOperationException e) {
      word2VecModelRulesDirectory = null;
    }
  }

  @Override
  public Object clone() {
    Settings obj = new Settings();
    obj.languageShortCode = languageShortCode;
    obj.diagnosticSeverity = ((diagnosticSeverity == null) ? null : diagnosticSeverity);
    obj.dictionary = ((dictionary == null) ? null : new ArrayList<>(dictionary));
    obj.disabledRules = ((disabledRules == null) ? null : new ArrayList<>(disabledRules));
    obj.enabledRules = ((enabledRules == null) ? null : new ArrayList<>(enabledRules));
    obj.dummyCommandPrototypes = ((dummyCommandPrototypes == null) ? null :
        new ArrayList<>(dummyCommandPrototypes));
    obj.ignoreCommandPrototypes = ((ignoreCommandPrototypes == null) ? null :
        new ArrayList<>(ignoreCommandPrototypes));
    obj.ignoreRuleSentencePairs = null;

    if (ignoreRuleSentencePairs != null) {
      obj.ignoreRuleSentencePairs = new ArrayList<>();

      for (Pair<String, Pattern> pair : ignoreRuleSentencePairs) {
        obj.ignoreRuleSentencePairs.add((pair == null) ? null :
            new Pair<String, Pattern>(pair.getKey(), pair.getValue()));
      }
    }

    obj.motherTongueShortCode = motherTongueShortCode;
    obj.languageModelRulesDirectory = languageModelRulesDirectory;
    obj.neuralNetworkModelRulesDirectory = neuralNetworkModelRulesDirectory;
    obj.word2VecModelRulesDirectory = word2VecModelRulesDirectory;
    return obj;
  }

  @Override
  public boolean equals(Object obj) {
    if ((obj == null) || !Settings.class.isAssignableFrom(obj.getClass())) return false;
    Settings other = (Settings) obj;

    if ((languageShortCode == null) ? (other.languageShortCode != null) :
        !languageShortCode.equals(other.languageShortCode)) {
      return false;
    }

    if ((diagnosticSeverity == null) ? (other.diagnosticSeverity != null) :
        (diagnosticSeverity != other.diagnosticSeverity)) {
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

    if ((dummyCommandPrototypes == null) ? (other.dummyCommandPrototypes != null) :
        !dummyCommandPrototypes.equals(other.dummyCommandPrototypes)) {
      return false;
    }

    if ((ignoreCommandPrototypes == null) ? (other.ignoreCommandPrototypes != null) :
        !ignoreCommandPrototypes.equals(other.ignoreCommandPrototypes)) {
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

    return true;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 53 * hash + ((languageShortCode != null) ? languageShortCode.hashCode() : 0);
    hash = 53 * hash + ((diagnosticSeverity != null) ? diagnosticSeverity.hashCode() : 0);
    hash = 53 * hash + ((dictionary != null) ? dictionary.hashCode() : 0);
    hash = 53 * hash + ((disabledRules != null) ? disabledRules.hashCode() : 0);
    hash = 53 * hash + ((enabledRules != null) ? enabledRules.hashCode() : 0);
    hash = 53 * hash + ((dummyCommandPrototypes != null) ? dummyCommandPrototypes.hashCode() : 0);
    hash = 53 * hash + ((ignoreCommandPrototypes != null) ? ignoreCommandPrototypes.hashCode() : 0);
    hash = 53 * hash + ((ignoreRuleSentencePairs != null) ? ignoreRuleSentencePairs.hashCode() : 0);
    hash = 53 * hash + ((motherTongueShortCode != null) ? motherTongueShortCode.hashCode() : 0);
    hash = 53 * hash + ((languageModelRulesDirectory != null) ?
        languageModelRulesDirectory.hashCode() : 0);
    hash = 53 * hash + ((neuralNetworkModelRulesDirectory != null) ?
        neuralNetworkModelRulesDirectory.hashCode() : 0);
    hash = 53 * hash + ((word2VecModelRulesDirectory != null) ?
        word2VecModelRulesDirectory.hashCode() : 0);
    return hash;
  }

  private static <T> T getDefault(T obj, T default_) {
    return ((obj != null) ? obj : default_);
  }

  public String getLanguageShortCode() {
    return getDefault(languageShortCode, "en-US");
  }

  public DiagnosticSeverity getDiagnosticSeverity() {
    return getDefault(diagnosticSeverity, DiagnosticSeverity.Information);
  }

  public List<String> getDictionary() {
    return getDefault(dictionary, Collections.emptyList());
  }

  public List<String> getDisabledRules() {
    return getDefault(disabledRules, Collections.emptyList());
  }

  public List<String> getEnabledRules() {
    return getDefault(enabledRules, Collections.emptyList());
  }

  public List<String> getDummyCommandPrototypes() {
    return getDefault(dummyCommandPrototypes, Collections.emptyList());
  }

  public List<String> getIgnoreCommandPrototypes() {
    return getDefault(ignoreCommandPrototypes, Collections.emptyList());
  }

  public List<Pair<String, Pattern>> getIgnoreRuleSentencePairs() {
    return getDefault(ignoreRuleSentencePairs, Collections.emptyList());
  }

  public String getMotherTongueShortCode() {
    return getDefault(motherTongueShortCode, null);
  }

  public String getLanguageModelRulesDirectory() {
    return getDefault(languageModelRulesDirectory, null);
  }

  public String getNeuralNetworkModelRulesDirectory() {
    return getDefault(neuralNetworkModelRulesDirectory, null);
  }

  public String getWord2VecModelRulesDirectory() {
    return getDefault(word2VecModelRulesDirectory, null);
  }
}
