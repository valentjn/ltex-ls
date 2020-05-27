package org.bsplines.ltex_ls.languagetool;

import java.util.List;

import org.bsplines.ltex_ls.*;

import org.eclipse.lsp4j.TextDocumentItem;

import org.eclipse.xtext.xbase.lib.Pair;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import org.languagetool.markup.AnnotatedText;
import org.languagetool.server.HTTPServer;

@TestInstance(Lifecycle.PER_CLASS)
public class LanguageToolHttpInterfaceTest {
  private DocumentChecker documentChecker;

  @BeforeAll
  public void setUp() {
    new Thread(() -> {
      HTTPServer.main(new String[]{"--port", "8081", "--allow-origin", "*"});
    }).start();

    SettingsManager settingsManager = new SettingsManager();
    Settings settings = new Settings();
    settings.setLanguageToolHttpServerUri("http://localhost:8081");
    settingsManager.setSettings(settings);
    documentChecker = new DocumentChecker(settingsManager);
  }

  @Test
  public void test() {
    TextDocumentItem document = DocumentCheckerTest.createDocument("latex",
        "This is an \\textbf{test.}\n% LTeX: language=de-DE\nDies ist eine \\textbf{Test}.\n");
    Pair<List<LanguageToolRuleMatch>, AnnotatedText> checkingResult =
        documentChecker.check(document);
    DocumentCheckerTest.testMatches(checkingResult.getKey(), 8, 10, 58, 75);
  }
}
