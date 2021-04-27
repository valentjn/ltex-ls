/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import org.bsplines.ltexls.languagetool.LanguageToolRuleMatch;
import org.bsplines.ltexls.parsing.AnnotatedTextFragment;
import org.bsplines.ltexls.settings.HiddenFalsePositive;
import org.bsplines.ltexls.settings.Settings;
import org.bsplines.ltexls.settings.SettingsManager;
import org.checkerframework.checker.nullness.util.NullnessUtil;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.xtext.xbase.lib.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class DocumentCheckerTest {
  private static Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>> checkDocument(
        LtexTextDocumentItem document) {
    return checkDocument(document, new Settings());
  }

  private static Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>> checkDocument(
        LtexTextDocumentItem document, Settings settings) {
    SettingsManager settingsManager = new SettingsManager(settings.withLogLevel(Level.FINEST));
    DocumentChecker documentChecker = new DocumentChecker(settingsManager);
    return documentChecker.check(document);
  }

  public static LtexTextDocumentItem createDocument(String codeLanguageId, String code) {
    LtexLanguageServer languageServer = new LtexLanguageServer();
    return new LtexTextDocumentItem(languageServer, "untitled:test.txt", codeLanguageId, 1, code);
  }

  public static void assertMatches(List<LanguageToolRuleMatch> matches, int fromPos1, int toPos1,
        int fromPos2, int toPos2) {
    Assertions.assertEquals(2, matches.size());

    Assertions.assertEquals("EN_A_VS_AN", NullnessUtil.castNonNull(matches.get(0).getRuleId()));
    Assertions.assertEquals("This is an test.",
        NullnessUtil.castNonNull(matches.get(0).getSentence()).trim());
    Assertions.assertEquals(fromPos1, matches.get(0).getFromPos());
    Assertions.assertEquals(toPos1, matches.get(0).getToPos());

    try {
      Assertions.assertEquals("Use <suggestion>a</suggestion> instead of 'an' if the following "
          + "word doesn't start with a vowel sound, e.g. "
          + "'a sentence', 'a university'.",
          matches.get(0).getMessage());
    } catch (AssertionError e) {
      Assertions.assertEquals("Use \u201ca\u201d instead of \u2018an\u2019 if the following "
          + "word doesn\u2019t start with a vowel sound, e.g.\u00a0"
          + "\u2018a sentence\u2019, \u2018a university\u2019.",
          matches.get(0).getMessage());
    }

    Assertions.assertEquals(1, matches.get(0).getSuggestedReplacements().size());
    Assertions.assertEquals("a", matches.get(0).getSuggestedReplacements().get(0));

    Assertions.assertEquals("DE_AGREEMENT", NullnessUtil.castNonNull(matches.get(1).getRuleId()));
    Assertions.assertEquals("Dies ist eine Test.",
        NullnessUtil.castNonNull(matches.get(1).getSentence()).trim());
    Assertions.assertEquals(fromPos2, matches.get(1).getFromPos());
    Assertions.assertEquals(toPos2, matches.get(1).getToPos());

    try {
      Assertions.assertEquals("M\u00f6glicherweise fehlende grammatische \u00dcbereinstimmung des "
          + "Genus (m\u00e4nnlich, weiblich, s\u00e4chlich - "
          + "Beispiel: 'der Fahrrad' statt 'das Fahrrad').",
          matches.get(1).getMessage());
    } catch (AssertionError e) {
      Assertions.assertEquals("M\u00f6glicherweise fehlende grammatische \u00dcbereinstimmung des "
          + "Genus (m\u00e4nnlich, weiblich, s\u00e4chlich - "
          + "Beispiel: \u201ader Fahrrad\u2018 statt \u201adas Fahrrad\u2018).",
          matches.get(1).getMessage());
    }

    Assertions.assertEquals(3, matches.get(1).getSuggestedReplacements().size());
    Assertions.assertEquals("ein Test", matches.get(1).getSuggestedReplacements().get(0));
    Assertions.assertEquals("einem Test", matches.get(1).getSuggestedReplacements().get(1));
    Assertions.assertEquals("einen Test", matches.get(1).getSuggestedReplacements().get(2));
  }

  @Test
  public void testLatex() {
    LtexTextDocumentItem document;
    Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>> checkingResult;

    document = createDocument("latex",
        "This is an \\textbf{test.}\n% LTeX: language=de-DE\nDies ist eine \\textbf{Test}.\n");
    checkingResult = checkDocument(document);
    assertMatches(checkingResult.getKey(), 8, 10, 58, 75);

    document = createDocument("latex",
        "This is a qwertyzuiopa\\footnote{This is another qwertyzuiopb.}.\n"
        + "% ltex: language=de-DE\n"
        + "Dies ist ein Qwertyzuiopc\\todo[name]{Dies ist ein weiteres Qwertyzuiopd.}.\n");
    checkingResult = checkDocument(document);
    List<LanguageToolRuleMatch> matches = checkingResult.getKey();
    List<AnnotatedTextFragment> annotatedTextFragments = checkingResult.getValue();

    Assertions.assertEquals(4, matches.size());
    Assertions.assertEquals(4, annotatedTextFragments.size());

    Assertions.assertEquals("MORFOLOGIK_RULE_EN_US",
        NullnessUtil.castNonNull(matches.get(0).getRuleId()));
    Assertions.assertEquals("This is another qwertyzuiopb.",
        NullnessUtil.castNonNull(matches.get(0).getSentence()));
    Assertions.assertEquals(48, matches.get(0).getFromPos());
    Assertions.assertEquals(60, matches.get(0).getToPos());

    Assertions.assertEquals("MORFOLOGIK_RULE_EN_US",
        NullnessUtil.castNonNull(matches.get(1).getRuleId()));
    Assertions.assertEquals("This is a qwertyzuiopa. ",
        NullnessUtil.castNonNull(matches.get(1).getSentence()));
    Assertions.assertEquals(10, matches.get(1).getFromPos());
    Assertions.assertEquals(22, matches.get(1).getToPos());

    Assertions.assertEquals("GERMAN_SPELLER_RULE",
        NullnessUtil.castNonNull(matches.get(2).getRuleId()));
    Assertions.assertEquals("Dies ist ein weiteres Qwertyzuiopd.",
        NullnessUtil.castNonNull(matches.get(2).getSentence()));
    Assertions.assertEquals(146, matches.get(2).getFromPos());
    Assertions.assertEquals(158, matches.get(2).getToPos());

    Assertions.assertEquals("GERMAN_SPELLER_RULE",
        NullnessUtil.castNonNull(matches.get(3).getRuleId()));
    Assertions.assertEquals("Dies ist ein Qwertyzuiopc. ",
        NullnessUtil.castNonNull(matches.get(3).getSentence()));
    Assertions.assertEquals(100, matches.get(3).getFromPos());
    Assertions.assertEquals(112, matches.get(3).getToPos());

    Assertions.assertEquals("This is another qwertyzuiopb.",
        annotatedTextFragments.get(0).getCodeFragment().getCode());
    Assertions.assertEquals("This is another qwertyzuiopb.",
        annotatedTextFragments.get(0).getAnnotatedText().getPlainText());

    Assertions.assertEquals("This is a qwertyzuiopa\\footnote{This is another qwertyzuiopb.}.\n",
        annotatedTextFragments.get(1).getCodeFragment().getCode());
    Assertions.assertEquals("This is a qwertyzuiopa. ",
        annotatedTextFragments.get(1).getAnnotatedText().getPlainText());

    Assertions.assertEquals("Dies ist ein weiteres Qwertyzuiopd.",
        annotatedTextFragments.get(2).getCodeFragment().getCode());
    Assertions.assertEquals("Dies ist ein weiteres Qwertyzuiopd.",
        annotatedTextFragments.get(2).getAnnotatedText().getPlainText());

    Assertions.assertEquals("% ltex: language=de-DE\n"
        + "Dies ist ein Qwertyzuiopc\\todo[name]{Dies ist ein weiteres Qwertyzuiopd.}.\n",
        annotatedTextFragments.get(3).getCodeFragment().getCode());
    Assertions.assertEquals("Dies ist ein Qwertyzuiopc. ",
        annotatedTextFragments.get(3).getAnnotatedText().getPlainText());

    document = createDocument("latex", "The \\v{S}ekki\n");
    checkingResult = checkDocument(document);
    matches = checkingResult.getKey();
    annotatedTextFragments = checkingResult.getValue();

    Assertions.assertEquals(1, matches.size());
    Assertions.assertEquals(1, annotatedTextFragments.size());
    String word = annotatedTextFragments.get(0).getSubstringOfPlainText(
        matches.get(0).getFromPos(), matches.get(0).getToPos());
    Assertions.assertEquals("\u0160ekki", word);
  }

  @Test
  public void testMarkdown() {
    LtexTextDocumentItem document = createDocument("markdown",
        "This is an **test.**\n\n[comment]: <> \"LTeX: language=de-DE\"\n\n"
        + "Dies ist eine **Test**.\n");
    Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>> checkingResult =
        checkDocument(document);
    assertMatches(checkingResult.getKey(), 8, 10, 69, 80);
  }

  @Test
  public void testRange() {
    LtexTextDocumentItem document = createDocument("markdown",
        "# Test\n\nThis is an **test.**\n\nThis is an **test.**\n");
    SettingsManager settingsManager =
        new SettingsManager((new Settings()).withLogLevel(Level.FINEST));
    DocumentChecker documentChecker = new DocumentChecker(settingsManager);
    Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>> checkingResult =
        documentChecker.check(document, new Range(new Position(4, 0), new Position(4, 20)));
    List<LanguageToolRuleMatch> matches = checkingResult.getKey();

    Assertions.assertEquals(1, matches.size());
    Assertions.assertEquals("EN_A_VS_AN", NullnessUtil.castNonNull(matches.get(0).getRuleId()));
    Assertions.assertEquals("This is an test.",
        NullnessUtil.castNonNull(matches.get(0).getSentence()).trim());
    Assertions.assertEquals(38, matches.get(0).getFromPos());
    Assertions.assertEquals(40, matches.get(0).getToPos());
  }

  @Test
  public void testCodeActionGenerator() {
    LtexTextDocumentItem document = createDocument("markdown",
        "This is an unknownword.\n");
    Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>> checkingResult =
        checkDocument(document);
    CodeActionParams params = new CodeActionParams(new TextDocumentIdentifier(document.getUri()),
        new Range(new Position(0, 0), new Position(100, 0)),
        new CodeActionContext(Collections.emptyList()));
    SettingsManager settingsManager = new SettingsManager();
    CodeActionGenerator codeActionGenerator = new CodeActionGenerator(settingsManager);
    List<Either<Command, CodeAction>> result = codeActionGenerator.generate(
        params, document, checkingResult);
    Assertions.assertEquals(4, result.size());
  }

  @Test
  public void testEnabled() {
    LtexTextDocumentItem document = createDocument("latex",
        "This is a firstunknownword.\n"
        + "% ltex: enabled=false\n"
        + "This is a secondunknownword.\n"
        + "% ltex: enabled=true\n"
        + "This is a thirdunknownword.\n");
    Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>> checkingResult =
        checkDocument(document);
    Assertions.assertEquals(2, checkingResult.getKey().size());
  }

  @Test
  public void testDictionary() {
    JsonArray jsonDictionaryArray = new JsonArray();
    jsonDictionaryArray.add("unbekannteswort");
    JsonObject jsonDictionaryObject = new JsonObject();
    jsonDictionaryObject.add("de-DE", jsonDictionaryArray);
    JsonObject jsonSettings = new JsonObject();
    JsonObject jsonWorkspaceSpecificSettings = new JsonObject();
    jsonWorkspaceSpecificSettings.add("dictionary", jsonDictionaryObject);
    LtexTextDocumentItem document = createDocument("latex",
        "This is an unknownword.\n% ltex: language=de-DE\nDies ist ein unbekannteswort.\n");
    Settings settings = new Settings(jsonSettings, jsonWorkspaceSpecificSettings);
    Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>> checkingResult =
        checkDocument(document, settings);
    Assertions.assertEquals(1, checkingResult.getKey().size());

    document = createDocument("latex", "S pekn\u00e9 inteligentn\u00fdmi dubmi.\n");
    settings = (new Settings()).withLanguageShortCode("sk-SK");
    checkingResult = checkDocument(document, settings);
    Assertions.assertEquals(1, checkingResult.getKey().size());

    settings = settings.withDictionary(Collections.singleton("pekn\u00e9"));
    checkingResult = checkDocument(document, settings);
    Assertions.assertEquals(0, checkingResult.getKey().size());

    document = createDocument("latex", "On trouve des mmots inconnus.\n");
    settings = (new Settings()).withLanguageShortCode("fr");
    checkingResult = checkDocument(document, settings);
    Assertions.assertEquals(1, checkingResult.getKey().size());

    settings = settings.withDictionary(Collections.singleton("mmots"));
    checkingResult = checkDocument(document, settings);
    Assertions.assertEquals(0, checkingResult.getKey().size());

    document = createDocument("markdown", "This is LT<sub>E</sub>X LS.\n");
    settings = new Settings();
    checkingResult = checkDocument(document, settings);
    Assertions.assertEquals(1, checkingResult.getKey().size());

    settings = settings.withDictionary(Collections.singleton("LTEX LS"));
    checkingResult = checkDocument(document, settings);
    Assertions.assertEquals(0, checkingResult.getKey().size());
  }

  @Test
  public void testHiddenFalsePositives() {
    LtexTextDocumentItem document = createDocument("markdown",
        "This is an unknownword.\n");
    Settings settings = (new Settings()).withHiddenFalsePositives(Collections.singleton(
        new HiddenFalsePositive("MORFOLOGIK_RULE_EN_US", "This is an unknownword\\.")));
    Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>> checkingResult =
        checkDocument(document, settings);
    Assertions.assertTrue(checkingResult.getKey().isEmpty());
  }
}
