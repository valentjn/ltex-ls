/* Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.languagetool

import org.bsplines.ltexls.parsing.AnnotatedTextFragment
import org.bsplines.ltexls.server.DocumentChecker
import org.bsplines.ltexls.server.DocumentCheckerTest
import org.bsplines.ltexls.server.LtexTextDocumentItem
import org.bsplines.ltexls.settings.Settings
import org.bsplines.ltexls.settings.SettingsManager
import org.junit.platform.suite.api.IncludeEngines
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@IncludeEngines("junit-jupiter")
class LanguageToolJavaInterfaceTest {
  @Test
  fun testCheck() {
    assertMatches(Settings(), true)
  }

  @Test
  fun testEasterEgg() {
    val settings = Settings(_allDictionaries = mapOf(Pair("en-US", setOf("BsPlInEs"))))
    val settingsManager = SettingsManager(settings)
    val documentChecker = DocumentChecker(settingsManager)
    val document: LtexTextDocumentItem = DocumentCheckerTest.createDocument(
      "latex",
      "Hat functions is for a beginner.\n"
    )
    val checkingResult: Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>> =
        documentChecker.check(document)
    assertEquals(3, checkingResult.first.size)
  }

  @Test
  fun testOtherMethods() {
    val settingsManager = SettingsManager()
    val ltInterface: LanguageToolInterface? = settingsManager.languageToolInterface
    assertNotNull(ltInterface)
    ltInterface.activateDefaultFalseFriendRules()
    ltInterface.activateLanguageModelRules("foobar")
    ltInterface.activateNeuralNetworkRules("foobar")
    ltInterface.activateWord2VecModelRules("foobar")
    ltInterface.enableEasterEgg()
  }

  companion object {
    private fun checkDocument(settings: Settings, code: String): List<LanguageToolRuleMatch> {
      val settingsManager = SettingsManager(settings)
      val documentChecker = DocumentChecker(settingsManager)
      val document: LtexTextDocumentItem = DocumentCheckerTest.createDocument("latex", code)
      val checkingResult: Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>> =
          documentChecker.check(document)
      return checkingResult.first
    }

    private fun assertMatchesCompare(
      oldSettings: Settings,
      newSettings: Settings,
      oldNumberOfMatches: Int,
      newNumberOfMatches: Int,
      code: String,
    ) {
      var matches = checkDocument(oldSettings, code)
      assertEquals(oldNumberOfMatches, matches.size)
      matches = checkDocument(newSettings, code)
      assertEquals(newNumberOfMatches, matches.size)
    }

    fun assertMatches(settings: Settings, checkMotherTongue: Boolean) {
      val matches: List<LanguageToolRuleMatch> = checkDocument(
        settings,
        "This is an \\textbf{test.}\n% LTeX: language=de-DE\nDies ist eine \\textbf{Test.}\n"
      )
      DocumentCheckerTest.assertMatches(matches, 8, 10, 58, 75)

      assertMatchesCompare(
        settings,
        settings.copy(
          _allDisabledRules = settings.getModifiedDisabledRules(setOf("UPPERCASE_SENTENCE_START")),
        ),
        1,
        0,
        "this is a test.\n",
      )

      assertMatchesCompare(
        settings,
        settings.copy(_allEnabledRules = settings.getModifiedDisabledRules(setOf("CAN_NOT"))),
        0,
        1,
        "You can not use the keyboard to select an item.\n",
      )

      assertMatchesCompare(
        settings,
        settings.copy(_enablePickyRules = true),
        0,
        1,
        "Her work will not have been finished by tonight.\n",
      )

      if (checkMotherTongue) {
        // mother tongue requires loading false-friends.xml, but loading of custom rules doesn't
        // seem to be supported by servers (only via the Java API)
        assertMatchesCompare(
          settings,
          settings.copy(_motherTongueShortCode = "de-DE", _enablePickyRules = true),
          0,
          1,
          "I'm holding my handy.\n",
        )
      }
    }
  }
}
