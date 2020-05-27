package org.bsplines.ltex_ls;

import java.util.Collections;
import java.util.List;

import org.bsplines.ltex_ls.languagetool.LanguageToolRuleMatch;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import org.eclipse.xtext.xbase.lib.Pair;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import org.languagetool.markup.AnnotatedText;

@TestInstance(Lifecycle.PER_CLASS)
public class DocumentCheckerTest {
  private SettingsManager settingsManager;
  private DocumentChecker documentChecker;
  private CodeActionGenerator codeActionGenerator;

  @BeforeAll
  public void setUp() {
    settingsManager = new SettingsManager();
    documentChecker = new DocumentChecker(settingsManager);
    codeActionGenerator = new CodeActionGenerator(settingsManager);
  }

  private TextDocumentItem createDocument(String codeLanguageId, String code) {
    return new TextDocumentItem("untitled:test.txt", codeLanguageId, 1, code);
  }

  private void testMatches(List<LanguageToolRuleMatch> matches, int fromPos1, int toPos1,
        int fromPos2, int toPos2) {
    Assertions.assertEquals(2, matches.size());

    Assertions.assertEquals("EN_A_VS_AN", matches.get(0).getRuleId());
    Assertions.assertEquals("This is an test.", matches.get(0).getSentence().trim());
    Assertions.assertEquals(fromPos1, matches.get(0).getFromPos());
    Assertions.assertEquals(toPos1, matches.get(0).getToPos());
    Assertions.assertEquals("Use <suggestion>a</suggestion> instead of 'an' if the following " +
        "word doesn't start with a vowel sound, e.g. 'a sentence', 'a university'",
        matches.get(0).getMessage());
    Assertions.assertEquals(1, matches.get(0).getSuggestedReplacements().size());
    Assertions.assertEquals("a", matches.get(0).getSuggestedReplacements().get(0));

    Assertions.assertEquals("DE_AGREEMENT", matches.get(1).getRuleId());
    Assertions.assertEquals("Dies ist eine Test.", matches.get(1).getSentence().trim());
    Assertions.assertEquals(fromPos2, matches.get(1).getFromPos());
    Assertions.assertEquals(toPos2, matches.get(1).getToPos());
    Assertions.assertEquals("M\u00f6glicherweise fehlende grammatische \u00DCbereinstimmung des " +
        "Genus (m\u00e4nnlich, weiblich, sächlich - Beispiel: 'der Fahrrad' statt 'das Fahrrad').",
        matches.get(1).getMessage());
    Assertions.assertEquals(3, matches.get(1).getSuggestedReplacements().size());
    Assertions.assertEquals("ein Test", matches.get(1).getSuggestedReplacements().get(0));
    Assertions.assertEquals("einem Test", matches.get(1).getSuggestedReplacements().get(1));
    Assertions.assertEquals("einen Test", matches.get(1).getSuggestedReplacements().get(2));
  }

  @Test
  public void testLatex() {
    TextDocumentItem document = createDocument("latex",
        "This is an \\textbf{test.}\n% LTeX: language=de-DE\nDies ist eine \\textbf{Test}.\n");
    Pair<List<LanguageToolRuleMatch>, AnnotatedText> checkingResult =
        documentChecker.check(document);
    testMatches(checkingResult.getKey(), 8, 10, 58, 75);
  }

  @Test
  public void testMarkdown() {
    TextDocumentItem document = createDocument("markdown",
        "This is an **test.**\n\n[comment]: <> \"LTeX: language=de-DE\"\n\n" +
        "Dies ist eine **Test**.\n");
    Pair<List<LanguageToolRuleMatch>, AnnotatedText> checkingResult =
        documentChecker.check(document);
    testMatches(checkingResult.getKey(), 8, 10, 69, 80);
  }

  @Test
  public void testCodeActionGenerator() {
    TextDocumentItem document = createDocument("plaintext",
        "This is an unknownword.\n");
    Pair<List<LanguageToolRuleMatch>, AnnotatedText> checkingResult =
        documentChecker.check(document);
    CodeActionParams params = new CodeActionParams(new TextDocumentIdentifier(document.getUri()),
        new Range(new Position(0, 0), new Position(100, 0)),
        new CodeActionContext(Collections.emptyList()));
    List<Either<Command, CodeAction>> result = codeActionGenerator.generate(
        params, document, checkingResult);
    Assertions.assertEquals(4, result.size());
  }
}
