package org.bsplines.ltexls;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Collections;

import org.eclipse.lsp4j.DiagnosticSeverity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SettingsTest {
  private static Settings compareSettings(Settings settings, Settings otherSettings) {
    Assertions.assertTrue(new Settings(settings).equals(settings));
    Assertions.assertTrue(settings.equals(new Settings(settings)));
    Assertions.assertFalse(otherSettings.equals(new Settings(settings)));
    Assertions.assertFalse(settings.equals(new Settings(otherSettings)));
    return new Settings(settings);
  }

  @Test
  public void testSetSettings() {
    Settings settings = new Settings();
    JsonElement jsonSettings = new JsonObject();
    Assertions.assertDoesNotThrow(() -> settings.setSettings(jsonSettings));
  }

  @Test
  public void testProperties() {
    Settings settings = new Settings();
    Settings settings2 = new Settings();

    settings.setLanguageShortCode("languageShortCode");
    Assertions.assertEquals("languageShortCode", settings.getLanguageShortCode());
    settings2 = compareSettings(settings, settings2);

    settings.setDictionary(Collections.singletonList("dictionary"));
    Assertions.assertEquals(Collections.singletonList("dictionary"), settings.getDictionary());
    settings2 = compareSettings(settings, settings2);

    settings.setDisabledRules(Collections.singletonList("disabledRules"));
    Assertions.assertEquals(Collections.singletonList("disabledRules"),
        settings.getDisabledRules());
    settings2 = compareSettings(settings, settings2);

    settings.setEnabledRules(Collections.singletonList("enabledRules"));
    Assertions.assertEquals(Collections.singletonList("enabledRules"),
        settings.getEnabledRules());
    settings2 = compareSettings(settings, settings2);

    settings.setLanguageToolHttpServerUri("languageToolHttpServerUri");
    Assertions.assertEquals("languageToolHttpServerUri", settings.getLanguageToolHttpServerUri());
    settings2 = compareSettings(settings, settings2);

    settings.setDummyCommandPrototypes(Collections.singletonList("dummyCommandPrototypes"));
    Assertions.assertEquals(Collections.singletonList("dummyCommandPrototypes"),
        settings.getDummyCommandPrototypes());
    settings2 = compareSettings(settings, settings2);

    settings.setIgnoreCommandPrototypes(Collections.singletonList("ignoreCommandPrototypes"));
    Assertions.assertEquals(Collections.singletonList("ignoreCommandPrototypes"),
        settings.getIgnoreCommandPrototypes());
    settings2 = compareSettings(settings, settings2);

    settings.setIgnoreEnvironments(Collections.singletonList("ignoreEnvironments"));
    Assertions.assertEquals(Collections.singletonList("ignoreEnvironments"),
        settings.getIgnoreEnvironments());
    settings2 = compareSettings(settings, settings2);

    settings.setDummyMarkdownNodeTypes(Collections.singletonList("dummyMarkdownNodeTypes"));
    Assertions.assertEquals(Collections.singletonList("dummyMarkdownNodeTypes"),
        settings.getDummyMarkdownNodeTypes());
    settings2 = compareSettings(settings, settings2);

    settings.setIgnoreMarkdownNodeTypes(Collections.singletonList("ignoreMarkdownNodeTypes"));
    Assertions.assertEquals(Collections.singletonList("ignoreMarkdownNodeTypes"),
        settings.getIgnoreMarkdownNodeTypes());
    settings2 = compareSettings(settings, settings2);

    settings.setIgnoreRuleSentencePairs(Collections.singletonList(
        new IgnoreRuleSentencePair("ruleId", "sentenceString")));
    Assertions.assertEquals(Collections.singletonList(
        new IgnoreRuleSentencePair("ruleId", "sentenceString")),
        settings.getIgnoreRuleSentencePairs());
    settings2 = compareSettings(settings, settings2);

    settings.setMotherTongueShortCode("motherTongueShortCode");
    Assertions.assertEquals("motherTongueShortCode", settings.getMotherTongueShortCode());
    settings2 = compareSettings(settings, settings2);

    settings.setLanguageModelRulesDirectory("languageModelRulesDirectory");
    Assertions.assertEquals("languageModelRulesDirectory",
        settings.getLanguageModelRulesDirectory());
    settings2 = compareSettings(settings, settings2);

    settings.setNeuralNetworkModelRulesDirectory("neuralNetworkModelRulesDirectory");
    Assertions.assertEquals("neuralNetworkModelRulesDirectory",
        settings.getNeuralNetworkModelRulesDirectory());
    settings2 = compareSettings(settings, settings2);

    settings.setWord2VecModelRulesDirectory("word2VecModelRulesDirectory");
    Assertions.assertEquals("word2VecModelRulesDirectory",
        settings.getWord2VecModelRulesDirectory());
    settings2 = compareSettings(settings, settings2);

    settings.setSentenceCacheSize(1337);
    Assertions.assertEquals(1337, settings.getSentenceCacheSize());
    settings2 = compareSettings(settings, settings2);

    settings.setDiagnosticSeverity(DiagnosticSeverity.Error);
    Assertions.assertEquals(DiagnosticSeverity.Error, settings.getDiagnosticSeverity());
    settings2 = compareSettings(settings, settings2);
  }
}