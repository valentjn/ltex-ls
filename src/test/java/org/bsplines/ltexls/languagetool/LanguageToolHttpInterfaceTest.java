package org.bsplines.ltexls.languagetool;

import java.util.List;

import org.bsplines.ltexls.DocumentChecker;
import org.bsplines.ltexls.DocumentCheckerTest;
import org.bsplines.ltexls.LtexTextDocumentItem;
import org.bsplines.ltexls.Settings;
import org.bsplines.ltexls.SettingsManager;
import org.bsplines.ltexls.parsing.AnnotatedTextFragment;

import org.checkerframework.checker.nullness.NullnessUtil;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import org.eclipse.xtext.xbase.lib.Pair;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import org.languagetool.server.HTTPServer;

@TestInstance(Lifecycle.PER_CLASS)
public class LanguageToolHttpInterfaceTest {
  private SettingsManager settingsManager = new SettingsManager();
  private DocumentChecker documentChecker = new DocumentChecker(settingsManager);
  private @MonotonicNonNull Thread serverThread;

  /**
   * Set up test class.
   */
  @BeforeAll
  public void setUp() throws InterruptedException {
    serverThread = new Thread(() -> {
      HTTPServer.main(new String[]{"--port", "8081", "--allow-origin", "*"});
    });
    serverThread.start();

    // wait until LanguageTool has initialized itself
    Thread.sleep(5000);

    Settings settings = new Settings();
    settings.setLanguageToolHttpServerUri("http://localhost:8081");
    settingsManager.setSettings(settings);
  }

  /**
   * Tear down test class.
   */
  @AfterAll
  public void tearDown() {
    if (serverThread != null) serverThread.interrupt();
  }

  @Test
  public void testConstructor() {
    Assertions.assertTrue(new LanguageToolHttpInterface(
        "http://localhost:8081/", "en-US", "").isReady());
    Assertions.assertFalse(new LanguageToolHttpInterface(
        "http://localhost:80:81/", "en-US", "").isReady());
  }

  @Test
  public void testCheck() {
    LtexTextDocumentItem document = DocumentCheckerTest.createDocument("latex",
        "This is an \\textbf{test.}\n% LTeX: language=de-DE\nDies ist eine \\textbf{Test}.\n");
    Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>> checkingResult =
        documentChecker.check(document);
    DocumentCheckerTest.testMatches(checkingResult.getKey(), 8, 10, 58, 75);
  }

  @Test
  public void testOtherMethods() {
    LanguageToolInterface ltInterface = settingsManager.getLanguageToolInterface();
    Assertions.assertNotNull(NullnessUtil.castNonNull(ltInterface));
    Assertions.assertDoesNotThrow(() -> ltInterface.activateDefaultFalseFriendRules());
    Assertions.assertDoesNotThrow(() -> ltInterface.activateLanguageModelRules("foobar"));
    Assertions.assertDoesNotThrow(() -> ltInterface.activateNeuralNetworkRules("foobar"));
    Assertions.assertDoesNotThrow(() -> ltInterface.activateWord2VecModelRules("foobar"));
    Assertions.assertDoesNotThrow(() -> ltInterface.enableEasterEgg());
  }
}
