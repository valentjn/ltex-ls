/* Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.settings

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.eclipse.lsp4j.DiagnosticSeverity
import java.util.logging.Level
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class SettingsTest {
  @Test
  @Suppress("LongMethod")
  fun testProperties() {
    var settings = Settings()
    var settings2 = Settings()

    settings = settings.copy(_enabled = setOf("markdown"))
    assertEquals(setOf("markdown"), settings.enabled)
    settings2 = compareSettings(settings, settings2, false)

    settings = settings.copy(_languageShortCode = "languageShortCode")
    assertEquals("languageShortCode", settings.languageShortCode)
    settings2 = compareSettings(settings, settings2, false)

    settings = settings.copy(_allDictionaries = settings.getModifiedDictionary(setOf("dictionary")))
    assertEquals(setOf("dictionary"), settings.dictionary)
    settings2 = compareSettings(settings, settings2, false)

    settings = settings.copy(
      _allDisabledRules = settings.getModifiedDisabledRules(setOf("disabledRules"))
    )
    assertEquals(setOf("disabledRules"), settings.disabledRules)
    settings2 = compareSettings(settings, settings2, false)

    settings = settings.copy(
      _allEnabledRules = settings.getModifiedEnabledRules(setOf("enabledRules"))
    )
    assertEquals(setOf("enabledRules"), settings.enabledRules)
    settings2 = compareSettings(settings, settings2, true)

    settings = settings.copy(_allHiddenFalsePositives = settings.getModifiedHiddenFalsePositives(
      setOf(HiddenFalsePositive("ruleId", "sentenceString"))
    ))
    assertEquals(
      setOf(HiddenFalsePositive("ruleId", "sentenceString")),
      settings.hiddenFalsePositives
    )
    settings2 = compareSettings(settings, settings2, false)

    settings = settings.copy(_bibtexFields = mapOf(Pair("bibtexField", false)))
    assertEquals(mapOf(Pair("bibtexField", false)), settings.bibtexFields)
    settings2 = compareSettings(settings, settings2, false)

    settings = settings.copy(_latexCommands = mapOf(Pair("latexCommand", "ignore")))
    assertEquals(mapOf(Pair("latexCommand", "ignore")), settings.latexCommands)
    settings2 = compareSettings(settings, settings2, false)

    settings = settings.copy(_latexEnvironments = mapOf(Pair("latexEnvironment", "ignore")))
    assertEquals(mapOf(Pair("latexEnvironment", "ignore")), settings.latexEnvironments)
    settings2 = compareSettings(settings, settings2, false)

    settings = settings.copy(_markdownNodes = mapOf(Pair("markdownNode", "ignore")))
    assertEquals(mapOf(Pair("markdownNode", "ignore")), settings.markdownNodes)
    settings2 = compareSettings(settings, settings2, false)

    settings = settings.copy(_enablePickyRules = true)
    assertEquals(true, settings.enablePickyRules)
    settings2 = compareSettings(settings, settings2, false)

    settings = settings.copy(_motherTongueShortCode = "motherTongueShortCode")
    assertEquals("motherTongueShortCode", settings.motherTongueShortCode)
    settings2 = compareSettings(settings, settings2, true)

    settings = settings.copy(_languageModelRulesDirectory = "languageModelRulesDirectory")
    assertEquals(
      "languageModelRulesDirectory",
      settings.languageModelRulesDirectory
    )
    settings2 = compareSettings(settings, settings2, true)

    settings = settings.copy(_neuralNetworkModelRulesDirectory = "neuralNetworkModelRulesDirectory")
    assertEquals(
      "neuralNetworkModelRulesDirectory",
      settings.neuralNetworkModelRulesDirectory
    )
    settings2 = compareSettings(settings, settings2, true)

    settings = settings.copy(_word2VecModelRulesDirectory = "word2VecModelRulesDirectory")
    assertEquals(
      "word2VecModelRulesDirectory",
      settings.word2VecModelRulesDirectory
    )
    settings2 = compareSettings(settings, settings2, true)

    settings = settings.copy(_languageToolHttpServerUri = "languageToolHttpServerUri")
    assertEquals("languageToolHttpServerUri", settings.languageToolHttpServerUri)
    settings2 = compareSettings(settings, settings2, true)

    settings = settings.copy(_languageToolOrgUsername = "languageToolOrgUsername")
    assertEquals("languageToolOrgUsername", settings.languageToolOrgUsername)
    settings2 = compareSettings(settings, settings2, false)

    settings = settings.copy(_languageToolOrgApiKey = "languageToolOrgApiKey")
    assertEquals("languageToolOrgApiKey", settings.languageToolOrgApiKey)
    settings2 = compareSettings(settings, settings2, false)

    settings = settings.copy(_logLevel = Level.FINEST)
    assertEquals(Level.FINEST, settings.logLevel)
    settings2 = compareSettings(settings, settings2, false)

    settings = settings.copy(_sentenceCacheSize = 1337)
    assertEquals(1337, settings.sentenceCacheSize)
    settings2 = compareSettings(settings, settings2, true)

    settings = settings.copy(_diagnosticSeverity = mapOf(Pair("ruleId", DiagnosticSeverity.Error)))
    assertEquals(mapOf(Pair("ruleId", DiagnosticSeverity.Error)), settings.diagnosticSeverity)
    settings2 = compareSettings(settings, settings2, false)

    settings = settings.copy(_checkFrequency = Settings.CheckFrequency.Manual)
    assertEquals(Settings.CheckFrequency.Manual, settings.checkFrequency)
    settings2 = compareSettings(settings, settings2, false)

    settings = settings.copy(_clearDiagnosticsWhenClosingFile = false)
    assertEquals(false, settings.clearDiagnosticsWhenClosingFile)
    @Suppress("UNUSED_VALUE")
    settings2 = compareSettings(settings, settings2, false)
  }

  @Test
  fun testTildeExpansion() {
    var settings = Settings()
    val originalDirPath = "~/tildeExpansion"
    settings = settings.copy(_languageModelRulesDirectory = originalDirPath)
    val expandedDirPath = settings.languageModelRulesDirectory
    assertTrue(expandedDirPath.endsWith("tildeExpansion"))
    assertNotEquals(originalDirPath, expandedDirPath)
  }

  companion object {
    private fun compareSettings(
      settings: Settings,
      otherSettings: Settings,
      differenceRelevant: Boolean
    ): Settings {
      val settings2: Settings = settings.copy()
      val otherSettings2: Settings = otherSettings.copy()

      assertEquals(settings.hashCode(), settings2.hashCode())
      assertEquals(otherSettings.hashCode(), otherSettings2.hashCode())

      assertEquals(settings2, settings)
      assertEquals(settings, settings2)
      assertEquals(otherSettings2, otherSettings)
      assertEquals(otherSettings, otherSettings2)
      assertNotEquals(otherSettings, settings)
      assertNotEquals(settings, otherSettings)
      assertNotEquals(otherSettings, settings2)
      assertNotEquals(settings2, otherSettings)
      assertNotEquals(otherSettings2, settings)
      assertNotEquals(settings, otherSettings2)
      assertNotEquals(otherSettings2, settings2)
      assertNotEquals(settings2, otherSettings2)

      assertTrue(settings.getDifferencesRelevantForLanguageTool(settings2).isEmpty())
      assertTrue(settings2.getDifferencesRelevantForLanguageTool(settings).isEmpty())
      assertTrue(otherSettings.getDifferencesRelevantForLanguageTool(otherSettings2).isEmpty())
      assertTrue(otherSettings2.getDifferencesRelevantForLanguageTool(otherSettings).isEmpty())
      assertEquals(
        !differenceRelevant,
        settings.getDifferencesRelevantForLanguageTool(otherSettings).isEmpty()
      )
      assertEquals(
        !differenceRelevant,
        otherSettings.getDifferencesRelevantForLanguageTool(settings).isEmpty()
      )
      assertEquals(
        !differenceRelevant,
        settings2.getDifferencesRelevantForLanguageTool(otherSettings).isEmpty()
      )
      assertEquals(
        !differenceRelevant,
        otherSettings.getDifferencesRelevantForLanguageTool(settings2).isEmpty()
      )
      assertEquals(
        !differenceRelevant,
        settings.getDifferencesRelevantForLanguageTool(otherSettings2).isEmpty()
      )
      assertEquals(
        !differenceRelevant,
        otherSettings2.getDifferencesRelevantForLanguageTool(settings).isEmpty()
      )
      assertEquals(
        !differenceRelevant,
        settings2.getDifferencesRelevantForLanguageTool(otherSettings2).isEmpty()
      )
      assertEquals(
        !differenceRelevant,
        otherSettings2.getDifferencesRelevantForLanguageTool(settings2).isEmpty()
      )

      return settings2
    }
  }

  @Test
  @Suppress("LongMethod")
  fun testFromJson() {
    val bibtexFields = JsonObject()
    bibtexFields.addProperty("bibtexField", false)

    val bibtex = JsonObject()
    bibtex.add("fields", bibtexFields)

    val latexEnvironments = JsonObject()
    latexEnvironments.addProperty("latexEnvironment", "ignore")

    val latexCommands = JsonObject()
    latexCommands.addProperty("\\latexCommand{}", "ignore")

    val latex = JsonObject()
    latex.add("commands", latexCommands)
    latex.add("environments", latexEnvironments)

    val markdownNodes = JsonObject()
    markdownNodes.addProperty("markdownNode", "ignore")

    val markdown = JsonObject()
    markdown.add("nodes", markdownNodes)

    val additionalRules = JsonObject()
    additionalRules.addProperty("enablePickyRules", true)

    val jsonSettings = JsonObject()
    jsonSettings.addProperty("enabled", false)
    jsonSettings.add("bibtex", bibtex)
    jsonSettings.add("latex", latex)
    jsonSettings.add("markdown", markdown)
    jsonSettings.add("additionalRules", additionalRules)

    val englishDictionary = JsonArray()
    englishDictionary.add("wordone")
    englishDictionary.add("wordtwo")
    englishDictionary.add("-wordone")

    val dictionary = JsonObject()
    dictionary.add("en-US", englishDictionary)

    val englishHiddenFalsePositives = JsonArray()
    englishHiddenFalsePositives.add("{\"rule\": \"rule\", \"sentence\": \"sentence\"}")

    val hiddenFalsePositives = JsonObject()
    hiddenFalsePositives.add("en-US", englishHiddenFalsePositives)

    val jsonWorkspaceSpecificSettings = JsonObject()
    jsonWorkspaceSpecificSettings.add("dictionary", dictionary)
    jsonWorkspaceSpecificSettings.add("hiddenFalsePositives", hiddenFalsePositives)

    var settings: Settings = Settings.fromJson(jsonSettings, jsonWorkspaceSpecificSettings)
    assertEquals(emptySet<Any>(), settings.enabled)
    assertEquals(setOf("wordtwo"), settings.dictionary)
    assertEquals(setOf(HiddenFalsePositive("rule", "sentence")), settings.hiddenFalsePositives)
    assertEquals(mapOf(Pair("bibtexField", false)), settings.bibtexFields)
    assertEquals(mapOf(Pair("\\latexCommand{}", "ignore")), settings.latexCommands)
    assertEquals(mapOf(Pair("latexEnvironment", "ignore")), settings.latexEnvironments)
    assertEquals(mapOf(Pair("markdownNode", "ignore")), settings.markdownNodes)
    assertEquals(true, settings.enablePickyRules)

    jsonSettings.addProperty("diagnosticSeverity", "error")
    settings = Settings.fromJson(jsonSettings, jsonWorkspaceSpecificSettings)
    assertEquals(mapOf(Pair("default", DiagnosticSeverity.Error)), settings.diagnosticSeverity)

    jsonSettings.addProperty("diagnosticSeverity", "warning")
    settings = Settings.fromJson(jsonSettings, jsonWorkspaceSpecificSettings)
    assertEquals(mapOf(Pair("default", DiagnosticSeverity.Warning)), settings.diagnosticSeverity)

    jsonSettings.addProperty("diagnosticSeverity", "information")
    settings = Settings.fromJson(jsonSettings, jsonWorkspaceSpecificSettings)
    assertEquals(
      mapOf(Pair("default", DiagnosticSeverity.Information)),
      settings.diagnosticSeverity,
    )

    jsonSettings.addProperty("diagnosticSeverity", "hint")
    settings = Settings.fromJson(jsonSettings, jsonWorkspaceSpecificSettings)
    assertEquals(mapOf(Pair("default", DiagnosticSeverity.Hint)), settings.diagnosticSeverity)

    val diagnosticSeverity = JsonObject()
    diagnosticSeverity.addProperty("ruleId", "warning")
    diagnosticSeverity.addProperty("default", "error")

    jsonSettings.add("diagnosticSeverity", diagnosticSeverity)
    settings = Settings.fromJson(jsonSettings, jsonWorkspaceSpecificSettings)
    assertEquals(
      mapOf(Pair("ruleId", DiagnosticSeverity.Warning), Pair("default", DiagnosticSeverity.Error)),
      settings.diagnosticSeverity,
    )

    jsonSettings.addProperty("checkFrequency", "edit")
    settings = Settings.fromJson(jsonSettings, jsonWorkspaceSpecificSettings)
    assertEquals(Settings.CheckFrequency.Edit, settings.checkFrequency)

    jsonSettings.addProperty("checkFrequency", "save")
    settings = Settings.fromJson(jsonSettings, jsonWorkspaceSpecificSettings)
    assertEquals(Settings.CheckFrequency.Save, settings.checkFrequency)

    jsonSettings.addProperty("checkFrequency", "manual")
    settings = Settings.fromJson(jsonSettings, jsonWorkspaceSpecificSettings)
    assertEquals(Settings.CheckFrequency.Manual, settings.checkFrequency)
  }
}
