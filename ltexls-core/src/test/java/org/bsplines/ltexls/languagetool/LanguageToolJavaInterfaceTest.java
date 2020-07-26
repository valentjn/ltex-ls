/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.languagetool;

import java.util.Collections;
import java.util.List;
import org.bsplines.ltexls.DocumentChecker;
import org.bsplines.ltexls.DocumentCheckerTest;
import org.bsplines.ltexls.LtexTextDocumentItem;
import org.bsplines.ltexls.Settings;
import org.bsplines.ltexls.SettingsManager;
import org.bsplines.ltexls.parsing.AnnotatedTextFragment;
import org.checkerframework.checker.nullness.NullnessUtil;
import org.eclipse.xtext.xbase.lib.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class LanguageToolJavaInterfaceTest {
  @Test
  public void testCheck() {
    SettingsManager settingsManager = new SettingsManager();
    DocumentChecker documentChecker = new DocumentChecker(settingsManager);
    LtexTextDocumentItem document = DocumentCheckerTest.createDocument("latex",
        "This is an \\textbf{test.}\n% LTeX: language=de-DE\nDies ist eine \\textbf{Test}.\n");
    Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>> checkingResult =
        documentChecker.check(document);
    DocumentCheckerTest.assertMatches(checkingResult.getKey(), 8, 10, 58, 75);
  }

  @Test
  public void testEasterEgg() {
    Settings settings = (new Settings()).withDictionary(Collections.singletonList("BsPlInEs"));
    SettingsManager settingsManager = new SettingsManager(settings);
    DocumentChecker documentChecker = new DocumentChecker(settingsManager);
    LtexTextDocumentItem document = DocumentCheckerTest.createDocument("latex",
        "Hat functions is for a beginner.\n");
    Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>> checkingResult =
        documentChecker.check(document);
    Assertions.assertEquals(2, checkingResult.getKey().size());
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
