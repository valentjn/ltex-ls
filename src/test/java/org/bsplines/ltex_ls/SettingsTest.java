package org.bsplines.ltex_ls;

import java.util.Collections;

import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SettingsTest {
  @Test
  public void testGettersAndSetters() {
    Settings settings = new Settings();

    settings.setLanguageShortCode("languageShortCode");
    Assertions.assertEquals("languageShortCode", settings.getLanguageShortCode());

    settings.setDictionary(Collections.singletonList("dictionary"));
    Assertions.assertEquals(Collections.singletonList("dictionary"), settings.getDictionary());

    settings.setDisabledRules(Collections.singletonList("disabledRules"));
    Assertions.assertEquals(Collections.singletonList("disabledRules"),
        settings.getDisabledRules());

    settings.setEnabledRules(Collections.singletonList("enabledRules"));
    Assertions.assertEquals(Collections.singletonList("enabledRules"),
        settings.getEnabledRules());

    settings.setLanguageToolHttpServerUri("languageToolHttpServerUri");
    Assertions.assertEquals("languageToolHttpServerUri", settings.getLanguageToolHttpServerUri());

    settings.setDummyCommandPrototypes(Collections.singletonList("dummyCommandPrototypes"));
    Assertions.assertEquals(Collections.singletonList("dummyCommandPrototypes"),
        settings.getDummyCommandPrototypes());

    settings.setIgnoreCommandPrototypes(Collections.singletonList("ignoreCommandPrototypes"));
    Assertions.assertEquals(Collections.singletonList("ignoreCommandPrototypes"),
        settings.getIgnoreCommandPrototypes());

    settings.setIgnoreEnvironments(Collections.singletonList("ignoreEnvironments"));
    Assertions.assertEquals(Collections.singletonList("ignoreEnvironments"),
        settings.getIgnoreEnvironments());

    settings.setDummyMarkdownNodeTypes(Collections.singletonList("dummyMarkdownNodeTypes"));
    Assertions.assertEquals(Collections.singletonList("dummyMarkdownNodeTypes"),
        settings.getDummyMarkdownNodeTypes());

    settings.setIgnoreMarkdownNodeTypes(Collections.singletonList("ignoreMarkdownNodeTypes"));
    Assertions.assertEquals(Collections.singletonList("ignoreMarkdownNodeTypes"),
        settings.getIgnoreMarkdownNodeTypes());

    settings.setIgnoreRuleSentencePairs(Collections.singletonList(
        new IgnoreRuleSentencePair("ruleId", "sentenceString")));
    Assertions.assertEquals(Collections.singletonList(
        new IgnoreRuleSentencePair("ruleId", "sentenceString")),
        settings.getIgnoreRuleSentencePairs());

    settings.setMotherTongueShortCode("motherTongueShortCode");
    Assertions.assertEquals("motherTongueShortCode", settings.getMotherTongueShortCode());

    settings.setLanguageModelRulesDirectory("languageModelRulesDirectory");
    Assertions.assertEquals("languageModelRulesDirectory", settings.getLanguageModelRulesDirectory());

    settings.setNeuralNetworkModelRulesDirectory("neuralNetworkModelRulesDirectory");
    Assertions.assertEquals("neuralNetworkModelRulesDirectory",
        settings.getNeuralNetworkModelRulesDirectory());

    settings.setWord2VecModelRulesDirectory("word2VecModelRulesDirectory");
    Assertions.assertEquals("word2VecModelRulesDirectory",
        settings.getWord2VecModelRulesDirectory());

    settings.setSentenceCacheSize(1337);
    Assertions.assertEquals(1337, settings.getSentenceCacheSize());

    settings.setDiagnosticSeverity(DiagnosticSeverity.Error);
    Assertions.assertEquals(DiagnosticSeverity.Error, settings.getDiagnosticSeverity());
  }
}