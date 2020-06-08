package org.bsplines.ltexls;

import java.util.Collections;
import java.util.List;

import org.bsplines.ltexls.languagetool.LanguageToolRuleMatch;
import org.bsplines.ltexls.parsing.AnnotatedTextFragment;

import org.checkerframework.checker.nullness.NullnessUtil;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import org.eclipse.xtext.xbase.lib.Pair;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class DocumentCheckerTest {
  private static Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>> checkDocument(
        TextDocumentItem document) {
    return checkDocument(document, new Settings());
  }

  private static Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>> checkDocument(
        TextDocumentItem document, Settings settings) {
    SettingsManager settingsManager = new SettingsManager();
    settingsManager.setSettings(settings);
    DocumentChecker documentChecker = new DocumentChecker(settingsManager);
    return documentChecker.check(document);
  }

  public static LtexTextDocumentItem createDocument(String codeLanguageId, String code) {
    return new LtexTextDocumentItem("untitled:test.txt", codeLanguageId, 1, code);
  }

  /**
   * Test matches of a standard document.
   *
   * @param matches list of matches as returned by LanguageTool
   * @param fromPos1 actual from position of the first diagnostic (inclusive)
   * @param toPos1 actual to position of the first diagnostic (exclusive)
   * @param fromPos2 actual from position of the second diagnostic (inclusive)
   * @param toPos2 actual to position of the second diagnostic (exclusive)
   */
  public static void testMatches(List<LanguageToolRuleMatch> matches, int fromPos1, int toPos1,
        int fromPos2, int toPos2) {
    Assertions.assertEquals(2, matches.size());

    Assertions.assertEquals("EN_A_VS_AN", NullnessUtil.castNonNull(matches.get(0).getRuleId()));
    Assertions.assertEquals("This is an test.",
        NullnessUtil.castNonNull(matches.get(0).getSentence()).trim());
    Assertions.assertEquals(fromPos1, matches.get(0).getFromPos());
    Assertions.assertEquals(toPos1, matches.get(0).getToPos());

    try {
      Assertions.assertEquals("Use <suggestion>a</suggestion> instead of 'an' if the following "
          + "word doesn't start with a vowel sound, e.g. 'a sentence', 'a university'",
          matches.get(0).getMessage());
    } catch (AssertionError e) {
      Assertions.assertEquals("Use \"a\" instead of 'an' if the following "
          + "word doesn't start with a vowel sound, e.g. 'a sentence', 'a university'",
          matches.get(0).getMessage());
    }

    Assertions.assertEquals(1, matches.get(0).getSuggestedReplacements().size());
    Assertions.assertEquals("a", matches.get(0).getSuggestedReplacements().get(0));

    Assertions.assertEquals("DE_AGREEMENT", NullnessUtil.castNonNull(matches.get(1).getRuleId()));
    Assertions.assertEquals("Dies ist eine Test.",
        NullnessUtil.castNonNull(matches.get(1).getSentence()).trim());
    Assertions.assertEquals(fromPos2, matches.get(1).getFromPos());
    Assertions.assertEquals(toPos2, matches.get(1).getToPos());
    Assertions.assertEquals("M\u00f6glicherweise fehlende grammatische \u00dcbereinstimmung des "
        + "Genus (m\u00e4nnlich, weiblich, s\u00e4chlich - Beispiel: 'der Fahrrad' statt 'das "
        + "Fahrrad').",
        matches.get(1).getMessage());
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
    testMatches(checkingResult.getKey(), 8, 10, 58, 75);

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
  }

  @Test
  public void testMarkdown() {
    LtexTextDocumentItem document = createDocument("markdown",
        "This is an **test.**\n\n[comment]: <> \"LTeX: language=de-DE\"\n\n"
        + "Dies ist eine **Test**.\n");
    Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>> checkingResult =
        checkDocument(document);
    testMatches(checkingResult.getKey(), 8, 10, 69, 80);
  }

  @Test
  public void testCodeActionGenerator() {
    LtexTextDocumentItem document = createDocument("plaintext",
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
  public void testIgnoreRuleSentencePairs() {
    LtexTextDocumentItem document = createDocument("plaintext",
        "This is an unknownword.\n");
    Settings settings = new Settings();
    settings.setIgnoreRuleSentencePairs(Collections.singletonList(
        new IgnoreRuleSentencePair("MORFOLOGIK_RULE_EN_US", "This is an unknownword\\.")));
    Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>> checkingResult =
        checkDocument(document, settings);
    Assertions.assertTrue(checkingResult.getKey().isEmpty());
  }
}
