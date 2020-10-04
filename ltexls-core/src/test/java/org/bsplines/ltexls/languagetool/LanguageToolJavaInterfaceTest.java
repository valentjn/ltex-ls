/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.languagetool;

import java.util.Collections;
import java.util.List;
import org.bsplines.ltexls.parsing.AnnotatedTextFragment;
import org.bsplines.ltexls.server.DocumentChecker;
import org.bsplines.ltexls.server.DocumentCheckerTest;
import org.bsplines.ltexls.server.LtexTextDocumentItem;
import org.bsplines.ltexls.settings.Settings;
import org.bsplines.ltexls.settings.SettingsManager;
import org.checkerframework.checker.nullness.NullnessUtil;
import org.eclipse.xtext.xbase.lib.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class LanguageToolJavaInterfaceTest {
  private static List<LanguageToolRuleMatch> checkDocument(Settings settings, String code) {
    SettingsManager settingsManager = new SettingsManager(settings);
    DocumentChecker documentChecker = new DocumentChecker(settingsManager);
    LtexTextDocumentItem document = DocumentCheckerTest.createDocument("latex", code);
    Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>> checkingResult =
        documentChecker.check(document);
    return checkingResult.getKey();
  }

  private static void assertMatchesCompare(Settings oldSettings, Settings newSettings,
        int oldNumberOfMatches, int newNumberOfMatches, String code) {
    List<LanguageToolRuleMatch> matches = checkDocument(oldSettings, code);
    Assertions.assertEquals(oldNumberOfMatches, matches.size());
    matches = checkDocument(newSettings, code);
    Assertions.assertEquals(newNumberOfMatches, matches.size());
  }

  public static void assertMatches(Settings settings, boolean checkMotherTongue) {
    List<LanguageToolRuleMatch> matches = checkDocument(settings,
        "This is an \\textbf{test.}\n% LTeX: language=de-DE\nDies ist eine \\textbf{Test}.\n");
    DocumentCheckerTest.assertMatches(matches, 8, 10, 58, 75);

    assertMatchesCompare(settings, settings.withDisabledRules(
        Collections.singleton("UPPERCASE_SENTENCE_START")), 1, 0,
        "this is a test.\n");
    assertMatchesCompare(settings, settings.withEnabledRules(
        Collections.singleton("CAN_NOT")), 0, 1,
        "You can not use the keyboard to select an item.\n");

    if (checkMotherTongue) {
      // mother tongue requires loading false-friends.xml, but loading of custom rules doesn't
      // seem to be supported by servers (only via the Java API)
      assertMatchesCompare(settings, settings.withMotherTongueShortCode("de-DE"), 0, 1,
          "I'm holding my handy.\n");
    }
  }

  @Test
  public void testCheck() {
    assertMatches(new Settings(), true);
  }

  @Test
  public void testEasterEgg() {
    Settings settings = (new Settings()).withDictionary(Collections.singleton("BsPlInEs"));
    SettingsManager settingsManager = new SettingsManager(settings);
    DocumentChecker documentChecker = new DocumentChecker(settingsManager);
    LtexTextDocumentItem document = DocumentCheckerTest.createDocument("latex",
        "Hat functions is for a beginner.\n");
    Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>> checkingResult =
        documentChecker.check(document);
    Assertions.assertEquals(3, checkingResult.getKey().size());
  }

  @Test
  public void testOtherMethods() {
    SettingsManager settingsManager = new SettingsManager();
    LanguageToolInterface ltInterface = settingsManager.getLanguageToolInterface();
    Assertions.assertNotNull(NullnessUtil.castNonNull(ltInterface));
    Assertions.assertDoesNotThrow(() -> ltInterface.activateDefaultFalseFriendRules());
    Assertions.assertDoesNotThrow(() -> ltInterface.activateLanguageModelRules("foobar"));
    Assertions.assertDoesNotThrow(() -> ltInterface.activateNeuralNetworkRules("foobar"));
    Assertions.assertDoesNotThrow(() -> ltInterface.activateWord2VecModelRules("foobar"));
    Assertions.assertDoesNotThrow(() -> ltInterface.enableEasterEgg());
  }
}
