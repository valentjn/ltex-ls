/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.bsplines.ltexls.Tools;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SettingsTest {
  private static Settings compareSettings(Settings settings, Settings otherSettings) {
    Settings settings2 = new Settings(settings);
    Settings otherSettings2 = new Settings(otherSettings);
    Assertions.assertTrue(settings2.equals(settings));
    Assertions.assertTrue(settings.equals(settings2));
    Assertions.assertEquals(settings.hashCode(), settings2.hashCode());
    Assertions.assertFalse(otherSettings.equals(settings2));
    Assertions.assertFalse(settings.equals(otherSettings2));
    return settings2;
  }

  @Test
  public void testJsonSettings() {
    JsonElement jsonSettings = new JsonObject();
    Assertions.assertDoesNotThrow(() -> new Settings(jsonSettings));
  }

  @Test
  public void testProperties() {
    Settings settings = new Settings();
    Settings settings2 = new Settings();

    settings = settings.withEnabled(false);
    Assertions.assertEquals(Collections.emptySet(), settings.getEnabled());
    settings2 = compareSettings(settings, settings2);

    settings = settings.withEnabled(Collections.singleton("markdown"));
    Assertions.assertEquals(Collections.singleton("markdown"), settings.getEnabled());
    settings2 = compareSettings(settings, settings2);

    settings = settings.withLanguageShortCode("languageShortCode");
    Assertions.assertEquals("languageShortCode", settings.getLanguageShortCode());
    settings2 = compareSettings(settings, settings2);

    settings = settings.withDictionary(Collections.singleton("dictionary"));
    Assertions.assertEquals(Collections.singleton("dictionary"), settings.getDictionary());
    settings2 = compareSettings(settings, settings2);

    settings = settings.withDisabledRules(Collections.singleton("disabledRules"));
    Assertions.assertEquals(Collections.singleton("disabledRules"),
        settings.getDisabledRules());
    settings2 = compareSettings(settings, settings2);

    settings = settings.withEnabledRules(Collections.singleton("enabledRules"));
    Assertions.assertEquals(Collections.singleton("enabledRules"),
        settings.getEnabledRules());
    settings2 = compareSettings(settings, settings2);

    settings = settings.withLanguageToolHttpServerUri("languageToolHttpServerUri");
    Assertions.assertEquals("languageToolHttpServerUri", settings.getLanguageToolHttpServerUri());
    settings2 = compareSettings(settings, settings2);

    settings = settings.withDummyCommandPrototypes(
        Collections.singleton("dummyCommandPrototypes"));
    Assertions.assertEquals(Collections.singleton("dummyCommandPrototypes"),
        settings.getDummyCommandPrototypes());
    settings2 = compareSettings(settings, settings2);

    settings = settings.withIgnoreCommandPrototypes(
        Collections.singleton("ignoreCommandPrototypes"));
    Assertions.assertEquals(Collections.singleton("ignoreCommandPrototypes"),
        settings.getIgnoreCommandPrototypes());
    settings2 = compareSettings(settings, settings2);

    settings = settings.withIgnoreEnvironments(Collections.singleton("ignoreEnvironments"));
    Assertions.assertEquals(Collections.singleton("ignoreEnvironments"),
        settings.getIgnoreEnvironments());
    settings2 = compareSettings(settings, settings2);

    settings = settings.withDummyMarkdownNodeTypes(
        Collections.singleton("dummyMarkdownNodeTypes"));
    Assertions.assertEquals(Collections.singleton("dummyMarkdownNodeTypes"),
        settings.getDummyMarkdownNodeTypes());
    settings2 = compareSettings(settings, settings2);

    settings = settings.withIgnoreMarkdownNodeTypes(
        Collections.singleton("ignoreMarkdownNodeTypes"));
    Assertions.assertEquals(Collections.singleton("ignoreMarkdownNodeTypes"),
        settings.getIgnoreMarkdownNodeTypes());
    settings2 = compareSettings(settings, settings2);

    settings = settings.withIgnoreRuleSentencePairs(Collections.singleton(
        new IgnoreRuleSentencePair("ruleId", "sentenceString")));
    Assertions.assertEquals(Collections.singleton(
        new IgnoreRuleSentencePair("ruleId", "sentenceString")),
        settings.getIgnoreRuleSentencePairs());
    settings2 = compareSettings(settings, settings2);

    settings = settings.withMotherTongueShortCode("motherTongueShortCode");
    Assertions.assertEquals("motherTongueShortCode", settings.getMotherTongueShortCode());
    settings2 = compareSettings(settings, settings2);

    settings = settings.withLanguageModelRulesDirectory("languageModelRulesDirectory");
    Assertions.assertEquals("languageModelRulesDirectory",
        settings.getLanguageModelRulesDirectory());
    settings2 = compareSettings(settings, settings2);

    settings = settings.withNeuralNetworkModelRulesDirectory("neuralNetworkModelRulesDirectory");
    Assertions.assertEquals("neuralNetworkModelRulesDirectory",
        settings.getNeuralNetworkModelRulesDirectory());
    settings2 = compareSettings(settings, settings2);

    settings = settings.withWord2VecModelRulesDirectory("word2VecModelRulesDirectory");
    Assertions.assertEquals("word2VecModelRulesDirectory",
        settings.getWord2VecModelRulesDirectory());
    settings2 = compareSettings(settings, settings2);

    settings = settings.withSentenceCacheSize(1337);
    Assertions.assertEquals(1337, settings.getSentenceCacheSize());
    settings2 = compareSettings(settings, settings2);

    settings = settings.withDiagnosticSeverity(DiagnosticSeverity.Error);
    Assertions.assertEquals(DiagnosticSeverity.Error, settings.getDiagnosticSeverity());
    settings2 = compareSettings(settings, settings2);

    settings = settings.withClearDiagnosticsWhenClosingFile(false);
    Assertions.assertEquals(false, settings.getClearDiagnosticsWhenClosingFile());
    settings2 = compareSettings(settings, settings2);
  }

  @Test
  public void testTildeExpansion() {
    Settings settings = new Settings();
    String originalDirPath = "~/tildeExpansion";
    settings = settings.withLanguageModelRulesDirectory(originalDirPath);
    String expandedDirPath = settings.getLanguageModelRulesDirectory();
    Assertions.assertTrue(expandedDirPath.endsWith("tildeExpansion"));
    Assertions.assertNotEquals(originalDirPath, expandedDirPath);
  }

  @Test
  public void testDictionaryFiles() throws InterruptedException, IOException {
    SettingsManager settingsManager = new SettingsManager();

    Thread.sleep(200);
    Assertions.assertTrue(settingsManager.getFullDictionary().isEmpty());

    Set<String> incompleteFullDictionary = new HashSet<>();
    incompleteFullDictionary.add("Test1");
    incompleteFullDictionary.add("Test2");

    Set<String> fullDictionary = new HashSet<>(incompleteFullDictionary);
    fullDictionary.add("Test3");
    fullDictionary.add("Test4");

    Set<String> dictionary = new HashSet<>(incompleteFullDictionary);

    File tmpFile = File.createTempFile("ltex-", ".txt");

    try {
      dictionary.add(":" + tmpFile.toPath().toString());

      settingsManager.setSettings((new Settings()).withDictionary(dictionary));
      Thread.sleep(200);
      Assertions.assertEquals(incompleteFullDictionary, settingsManager.getFullDictionary());

      Tools.writeFile(tmpFile.toPath(), "Test3\nTest4\n");
      Thread.sleep(200);
      Assertions.assertEquals(fullDictionary, settingsManager.getFullDictionary());
    } finally {
      if (!tmpFile.delete()) {
        Tools.logger.warning(Tools.i18n(
            "couldNotDeleteTemporaryFile", tmpFile.toPath().toString()));
      }
    }

    Thread.sleep(200);
    Assertions.assertEquals(dictionary, settingsManager.getFullDictionary());
  }
}
