/* Copyright (C) 2019-2023 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.server

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.bsplines.ltexls.languagetool.LanguageToolRuleMatch
import org.bsplines.ltexls.parsing.AnnotatedTextFragment
import org.bsplines.ltexls.settings.HiddenFalsePositive
import org.bsplines.ltexls.settings.Settings
import org.bsplines.ltexls.settings.SettingsManager
import org.eclipse.lsp4j.CodeAction
import org.eclipse.lsp4j.CodeActionContext
import org.eclipse.lsp4j.CodeActionParams
import org.eclipse.lsp4j.Command
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.jsonrpc.messages.Either
import java.util.logging.Level
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DocumentCheckerTest {
  @Test
  @Suppress("LongMethod")
  fun testLatex() {
    var document: LtexTextDocumentItem = createDocument(
      "latex",
      "This is an \\textbf{test.}\n% LTeX: language=de-DE\nDies ist eine \\textbf{Test.}\n",
    )
    var checkingResult: Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>> =
        checkDocument(document)
    assertMatches(checkingResult.first, 8, 10, 58, 75)

    document = createDocument(
      "latex",
      """
      This is a qwertyzuiopa\footnote{This is another qwertyzuiopb.}.
      % ltex: language=de-DE
      Dies ist ein Qwertyzuiopc\todo[name]{Dies ist ein weiteres Qwertyzuiopd.}.

      """.trimIndent(),
    )
    checkingResult = checkDocument(document)

    val matches: List<LanguageToolRuleMatch> = checkingResult.first
    val annotatedTextFragments: List<AnnotatedTextFragment> = checkingResult.second
    assertEquals(4, matches.size)
    assertEquals(5, annotatedTextFragments.size)

    assertEquals("MORFOLOGIK_RULE_EN_US", matches[0].ruleId)
    assertEquals("This is another qwertyzuiopb.", matches[0].sentence)
    assertEquals(48, matches[0].fromPos)
    assertEquals(60, matches[0].toPos)

    assertEquals("MORFOLOGIK_RULE_EN_US", matches[1].ruleId)
    assertEquals("This is a qwertyzuiopa. ", matches[1].sentence)
    assertEquals(10, matches[1].fromPos)
    assertEquals(22, matches[1].toPos)

    assertEquals("GERMAN_SPELLER_RULE", matches[2].ruleId)
    assertEquals("Dies ist ein weiteres Qwertyzuiopd.", matches[2].sentence)
    assertEquals(146, matches[2].fromPos)
    assertEquals(158, matches[2].toPos)

    assertEquals("GERMAN_SPELLER_RULE", matches[3].ruleId)
    assertEquals(" Dies ist ein Qwertyzuiopc. ", matches[3].sentence)
    assertEquals(100, matches[3].fromPos)
    assertEquals(112, matches[3].toPos)

    assertEquals("This is another qwertyzuiopb.", annotatedTextFragments[0].codeFragment.code)
    assertEquals("This is another qwertyzuiopb.", annotatedTextFragments[0].annotatedText.plainText)

    assertEquals(
      "This is a qwertyzuiopa\\footnote{This is another qwertyzuiopb.}.\n",
      annotatedTextFragments[1].codeFragment.code,
    )
    assertEquals("This is a qwertyzuiopa. ", annotatedTextFragments[1].annotatedText.plainText)

    assertEquals("% ltex: language=de-DE", annotatedTextFragments[2].codeFragment.code)
    assertEquals("", annotatedTextFragments[2].annotatedText.plainText)

    assertEquals("Dies ist ein weiteres Qwertyzuiopd.", annotatedTextFragments[3].codeFragment.code)
    assertEquals(
      "Dies ist ein weiteres Qwertyzuiopd.",
      annotatedTextFragments[3].annotatedText.plainText,
    )

    assertEquals(
      """

      Dies ist ein Qwertyzuiopc\todo[name]{Dies ist ein weiteres Qwertyzuiopd.}.

      """.trimIndent(),
      annotatedTextFragments[4].codeFragment.code,
    )
    assertEquals(
      " Dies ist ein Qwertyzuiopc. ",
      annotatedTextFragments[4].annotatedText.plainText,
    )
    assertOriginalAndPlainTextWords("latex", "The \\v{S}ekki\n", "\\v{S}ekki", "\u0160ekki")
    assertOriginalAndPlainTextWords("latex", "The Sekk\\v{S}\n", "Sekk\\v{S}", "Sekk\u0160")
    assertOriginalAndPlainTextWords("latex", "This is \\textbf{an} test.\n", "an", "an")
  }

  @Test
  fun testMarkdown() {
    val document: LtexTextDocumentItem = createDocument(
      "markdown",
      """
      This is an **test.**

      <!-- LTeX: language=de-DE -->

      Dies ist eine **Test**.

      """.trimIndent(),
    )
    assertMatches(checkDocument(document).first, 8, 10, 62, 73)
  }

  @Test
  fun testLanguageDetection() {
    val document: LtexTextDocumentItem = createDocument(
      "markdown",
      """
      This is an **test.**

      <!-- LTeX: language=auto -->

      Dies ist eine **Test**.

      """.trimIndent(),
    )
    assertMatches(checkDocument(document).first, 8, 10, 61, 72)
  }

  @Test
  fun testRange() {
    var document: LtexTextDocumentItem = createDocument(
      "markdown",
      "# Test\n\nThis is an **test.**\n\nThis is an **test.**\n",
    )
    val settingsManager = SettingsManager(Settings(_logLevel = Level.FINEST))
    val documentChecker = DocumentChecker(settingsManager)
    var checkingResult: Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>> =
        documentChecker.check(document, Range(Position(4, 0), Position(4, 20)))
    var matches: List<LanguageToolRuleMatch> = checkingResult.first
    assertEquals(1, matches.size)
    assertEquals("EN_A_VS_AN", matches[0].ruleId)
    assertEquals("This is an test.", matches[0].sentence?.trim())
    assertEquals(38, matches[0].fromPos)
    assertEquals(40, matches[0].toPos)

    document = createDocument(
      "cpp",
      """
      #include <iostream>

      int main() {
        std::cout << "This is an test." << std::endl;
        return 0;
      }

      """.trimIndent(),
    )
    checkingResult = documentChecker.check(document, Range(Position(3, 16), Position(3, 32)))
    matches = checkingResult.first
    assertEquals(1, matches.size)
    assertEquals("EN_A_VS_AN", matches[0].ruleId)
    assertEquals("This is an test.", matches[0].sentence?.trim())
    assertEquals(58, matches[0].fromPos)
    assertEquals(60, matches[0].toPos)
  }

  @Test
  fun testCodeActionGenerator() {
    val document: LtexTextDocumentItem = createDocument(
      "markdown",
      "This is an unknownword.\n",
    )
    val checkingResult: Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>> =
        checkDocument(document)
    val params = CodeActionParams(
      TextDocumentIdentifier(document.uri),
      Range(Position(0, 0), Position(100, 0)),
      CodeActionContext(emptyList()),
    )
    val settingsManager = SettingsManager()
    val codeActionProvider = CodeActionProvider(settingsManager)
    val result: List<Either<Command, CodeAction>> =
        codeActionProvider.generate(params, document, checkingResult)
    assertEquals(4, result.size)
  }

  @Test
  fun testEnabled() {
    val document: LtexTextDocumentItem = createDocument(
      "latex",
      """
      This is a firstunknownword.
      % ltex: enabled=false
      This is a secondunknownword.
      % ltex: enabled=true
      This is a thirdunknownword.

      """.trimIndent(),
    )
    val checkingResult: Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>> =
        checkDocument(document)
    assertEquals(2, checkingResult.first.size)
  }

  @Test
  fun testDictionary() {
    val jsonDictionaryArray = JsonArray()
    jsonDictionaryArray.add("unbekannteswort")

    val jsonDictionaryObject = JsonObject()
    jsonDictionaryObject.add("de-DE", jsonDictionaryArray)

    val jsonSettings = JsonObject()
    val jsonWorkspaceSpecificSettings = JsonObject()
    jsonWorkspaceSpecificSettings.add("dictionary", jsonDictionaryObject)

    var document = createDocument(
      "latex",
      "This is an unknownword.\n% ltex: language=de-DE\nDies ist ein unbekannteswort.\n",
    )
    var settings: Settings = Settings.fromJson(jsonSettings, jsonWorkspaceSpecificSettings)
    var checkingResult: Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>> =
      checkDocument(document, settings)
    assertEquals(1, checkingResult.first.size)

    document = createDocument("latex", "S pekn\u00e9 inteligentn\u00fdmi dubmi.\n")
    settings = Settings(_languageShortCode = "sk-SK")
    checkingResult = checkDocument(document, settings)
    assertEquals(1, checkingResult.first.size)
    settings = settings.copy(_allDictionaries = mapOf(Pair("sk-SK", setOf("pekn\u00e9"))))
    checkingResult = checkDocument(document, settings)
    assertEquals(0, checkingResult.first.size)

    document = createDocument("latex", "On trouve des mmots inconnus.\n")
    settings = Settings(_languageShortCode = "fr")
    checkingResult = checkDocument(document, settings)
    assertEquals(1, checkingResult.first.size)
    settings = settings.copy(_allDictionaries = mapOf(Pair("fr", setOf("mmots"))))
    checkingResult = checkDocument(document, settings)
    assertEquals(0, checkingResult.first.size)

    document = createDocument("markdown", "This is LT<sub>E</sub>X LS.\n")
    settings = Settings()
    checkingResult = checkDocument(document, settings)
    assertEquals(1, checkingResult.first.size)
    settings = settings.copy(_allDictionaries = mapOf(Pair("en-US", setOf("LTEX LS"))))
    checkingResult = checkDocument(document, settings)
    assertEquals(0, checkingResult.first.size)
  }

  @Test
  fun testHiddenFalsePositives() {
    val document: LtexTextDocumentItem = createDocument("markdown", "This is an unknownword.\n")
    val settings = Settings(
      _allHiddenFalsePositives = mapOf(
        Pair(
          "en-US",
          setOf(HiddenFalsePositive("MORFOLOGIK_RULE_EN_US", "This is an unknownword\\.")),
        ),
      ),
    )
    val checkingResult: Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>> =
      checkDocument(document, settings)
    assertTrue(checkingResult.first.isEmpty())
  }

  companion object {
    private fun checkDocument(
      document: LtexTextDocumentItem,
      settings: Settings = Settings(),
    ): Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>> {
      val settingsManager = SettingsManager(settings.copy(_logLevel = Level.FINEST))
      val documentChecker = DocumentChecker(settingsManager)
      return documentChecker.check(document)
    }

    fun createDocument(codeLanguageId: String, code: String): LtexTextDocumentItem {
      val languageServer = LtexLanguageServer()
      return LtexTextDocumentItem(languageServer, "untitled:test.txt", codeLanguageId, 1, code)
    }

    private fun assertOriginalAndPlainTextWords(
      codeLanguageId: String,
      code: String,
      expectedOriginalTextWord: String,
      expectedPlainTextWord: String,
    ) {
      val document: LtexTextDocumentItem = createDocument(codeLanguageId, code)
      val checkingResult: Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>> =
        checkDocument(document)
      val matches: List<LanguageToolRuleMatch> = checkingResult.first
      val annotatedTextFragments: List<AnnotatedTextFragment> = checkingResult.second
      assertEquals(1, matches.size)
      assertEquals(1, annotatedTextFragments.size)
      assertEquals(
        expectedOriginalTextWord,
        code.substring(matches[0].fromPos, matches[0].toPos),
      )
      assertEquals(
        expectedPlainTextWord,
        annotatedTextFragments[0].getSubstringOfPlainText(matches[0].fromPos, matches[0].toPos),
      )
    }

    @Suppress("SwallowedException")
    fun assertMatches(
      matches: List<LanguageToolRuleMatch>,
      fromPos1: Int,
      toPos1: Int,
      fromPos2: Int,
      toPos2: Int,
    ) {
      assertEquals(2, matches.size)
      assertEquals("EN_A_VS_AN", matches[0].ruleId)
      assertEquals("This is an test.", matches[0].sentence?.trim())
      assertEquals(fromPos1, matches[0].fromPos)
      assertEquals(toPos1, matches[0].toPos)

      try {
        assertEquals(
          "Use <suggestion>a</suggestion> instead of 'an' if the following "
          + "word doesn't start with a vowel sound, e.g. "
          + "'a sentence', 'a university'.",
          matches[0].message,
        )
      } catch (e: AssertionError) {
        assertEquals(
          "Use \u201ca\u201d instead of \u2018an\u2019 if the following "
          + "word doesn\u2019t start with a vowel sound, e.g.\u00a0"
          + "\u2018a sentence\u2019, \u2018a university\u2019.",
          matches[0].message,
        )
      }

      assertEquals(1, matches[0].suggestedReplacements.size)
      assertEquals("a", matches[0].suggestedReplacements[0])
      assertEquals("DE_AGREEMENT", matches[1].ruleId)
      assertEquals("Dies ist eine Test.", matches[1].sentence?.trim())
      assertEquals(fromPos2, matches[1].fromPos)
      assertEquals(toPos2, matches[1].toPos)

      try {
        assertEquals(
          "M\u00f6glicherweise fehlende grammatische \u00dcbereinstimmung des "
          + "Genus (m\u00e4nnlich, weiblich, s\u00e4chlich - "
          + "Beispiel: 'der Fahrrad' statt 'das Fahrrad').",
          matches[1].message,
        )
      } catch (e: AssertionError) {
        assertEquals(
          "M\u00f6glicherweise fehlende grammatische \u00dcbereinstimmung des "
          + "Genus (m\u00e4nnlich, weiblich, s\u00e4chlich - "
          + "Beispiel: \u201ader Fahrrad\u2018 statt \u201adas Fahrrad\u2018).",
          matches[1].message,
        )
      }

      assertEquals(3, matches[1].suggestedReplacements.size)
      assertEquals("ein Test", matches[1].suggestedReplacements[0])
      assertEquals("einem Test", matches[1].suggestedReplacements[1])
      assertEquals("einen Test", matches[1].suggestedReplacements[2])
    }
  }
}
