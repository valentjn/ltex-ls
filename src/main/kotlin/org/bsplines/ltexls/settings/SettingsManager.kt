/* Copyright (C) 2019-2023 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.settings

import org.bsplines.ltexls.languagetool.LanguageToolHttpInterface
import org.bsplines.ltexls.languagetool.LanguageToolInterface
import org.bsplines.ltexls.languagetool.LanguageToolJavaInterface
import org.bsplines.ltexls.tools.I18n
import org.bsplines.ltexls.tools.Logging
import java.util.logging.Level

class SettingsManager {
  private val settingsMap: MutableMap<String, Settings> = HashMap()
  private val languageToolInterfaceMap: MutableMap<String, LanguageToolInterface?> = HashMap()

  var languageToolInterface: LanguageToolInterface? = null
    private set
  var settings = Settings()
    set(value) { // ktlint-disable experimental:trailing-comma
      val newLanguage: String = value.languageShortCode
      val oldSettings: Settings? = this.settingsMap[newLanguage]

      field = value
      this.settingsMap[newLanguage] = value
      Logging.setLogLevel(value.logLevel)

      val settingsDifferencesRelevantForLanguageTool: Set<SettingsDifference> =
          value.getDifferencesRelevantForLanguageTool(oldSettings)

      val languageToolInterface: LanguageToolInterface? =
      if (settingsDifferencesRelevantForLanguageTool.isEmpty()) {
        this.languageToolInterfaceMap[newLanguage]
      } else {
        if (Logging.logger.isLoggable(Level.FINE)) {
          logDifferentSettings(newLanguage, settingsDifferencesRelevantForLanguageTool)
        }

        reinitializeLanguageToolInterface()
        this.languageToolInterface
      }

      languageToolInterface?.dictionary = value.dictionary
      languageToolInterface?.disabledRules = value.disabledRules
      languageToolInterface?.languageToolOrgUsername = value.languageToolOrgUsername
      languageToolInterface?.languageToolOrgApiKey = value.languageToolOrgApiKey
      this.languageToolInterface = languageToolInterface
      this.languageToolInterfaceMap[newLanguage] = languageToolInterface
    }

  constructor() {
    initialize()
  }

  constructor(settings: Settings) {
    this.settings = settings
    initialize()
  }

  private fun initialize() {
    reinitializeLanguageToolInterface()

    val language: String = this.settings.languageShortCode
    this.settingsMap[language] = this.settings
    this.languageToolInterfaceMap[language] = this.languageToolInterface
    Logging.setLogLevel(settings.logLevel)
  }

  private fun reinitializeLanguageToolInterface() {
    val languageToolInterface: LanguageToolInterface =
    if (this.settings.languageToolHttpServerUri.isEmpty()) {
      LanguageToolJavaInterface(
        this.settings.languageShortCode,
        this.settings.motherTongueShortCode,
        this.settings.sentenceCacheSize,
        this.settings.dictionary,
      )
    } else {
      LanguageToolHttpInterface(
        this.settings.languageToolHttpServerUri,
        this.settings.languageShortCode,
        this.settings.motherTongueShortCode,
      )
    }

    if (!languageToolInterface.isInitialized()) {
      this.languageToolInterface = null
      return
    }

    if (this.settings.languageModelRulesDirectory.isNotEmpty()) {
      languageToolInterface.activateLanguageModelRules(this.settings.languageModelRulesDirectory)
    } else {
      if (this.settings.motherTongueShortCode.isNotEmpty()) {
        languageToolInterface.activateDefaultFalseFriendRules()
      }
    }

    if (this.settings.neuralNetworkModelRulesDirectory.isNotEmpty()) {
      languageToolInterface.activateNeuralNetworkRules(
        this.settings.neuralNetworkModelRulesDirectory,
      )
    }

    if (this.settings.word2VecModelRulesDirectory.isNotEmpty()) {
      languageToolInterface.activateWord2VecModelRules(this.settings.word2VecModelRulesDirectory)
    }

    languageToolInterface.enableRules(this.settings.enabledRules)

    this.languageToolInterface = languageToolInterface
  }

  companion object {
    fun logDifferentSettings(
      newLanguage: String,
      settingsDifferencesRelevantForLanguageTool: Set<SettingsDifference>,
    ) {
      val differences = HashSet<SettingsDifference>(settingsDifferencesRelevantForLanguageTool)
      val builder = StringBuilder()

      for (difference: SettingsDifference in differences) {
        if (builder.isNotEmpty()) builder.append("; ")
        builder.append("setting '")
        builder.append(difference.name)
        builder.append("', old '")
        builder.append(difference.otherValue)
        builder.append("', new '")
        builder.append(difference.thisValue)
        builder.append("'")
      }

      Logging.logger.fine(
        I18n.format(
          "reinitializingLanguageToolDueToDifferentSettings",
          newLanguage,
          builder.toString(),
        ),
      )
    }
  }
}
