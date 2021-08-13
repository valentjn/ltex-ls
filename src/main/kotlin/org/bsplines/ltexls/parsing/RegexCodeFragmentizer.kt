/* Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing

import org.bsplines.ltexls.settings.Settings
import org.bsplines.ltexls.tools.I18n
import org.bsplines.ltexls.tools.Logging

open class RegexCodeFragmentizer(
  codeLanguageId: String,
  val regex: Regex,
) : CodeFragmentizer(codeLanguageId) {
  override fun fragmentize(code: String, originalSettings: Settings): List<CodeFragment> {
    val codeFragments = ArrayList<CodeFragment>()
    var curSettings: Settings = originalSettings
    var curPos = 0

    for (matchResult: MatchResult in this.regex.findAll(code)) {
      var lastPos: Int = curPos
      curPos = matchResult.range.first
      var lastCode: String = code.substring(lastPos, curPos)
      var lastSettings: Settings = curSettings
      codeFragments.add(CodeFragment(codeLanguageId, lastCode, lastPos, lastSettings))

      var settingsLine: String? = null

      for (groupIndex in 1 until matchResult.groups.size) {
        if (matchResult.groups[groupIndex] != null) {
          settingsLine = matchResult.groupValues[groupIndex]
          break
        }
      }

      if (settingsLine == null) {
        Logging.logger.warning(I18n.format("couldNotFindSettingsInMatch"))
        continue
      }

      val settingsMap: Map<String, String> = parseSettings(settingsLine)

      for ((settingKey: String, settingValue: String) in settingsMap) {
        when {
          settingKey.equals("enabled", ignoreCase = true) -> {
            curSettings = curSettings.copy(_enabled =
            (if (settingValue == "true") Settings.DEFAULT_ENABLED else emptySet()))
          }
          settingKey.equals("language", ignoreCase = true) -> {
            curSettings = curSettings.copy(_languageShortCode = settingValue)
          }
          else -> {
            Logging.logger.warning(I18n.format("ignoringUnknownInlineSetting",
                settingKey, settingValue))
          }
        }
      }

      lastPos = curPos
      curPos = matchResult.range.last + 1
      lastCode = code.substring(lastPos, curPos)
      lastSettings = curSettings
      codeFragments.add(CodeFragment("nop", lastCode, lastPos, lastSettings))
    }

    codeFragments.add(CodeFragment(
        codeLanguageId, code.substring(curPos), curPos, curSettings))

    return codeFragments
  }

  companion object {
    private val SPLIT_SETTINGS_REGEX = Regex("[ \t]+")

    private fun parseSettings(settingsLine: String): Map<String, String> {
      val settingsMap = HashMap<String, String>()

      for (settingsChange: String in settingsLine.trim().split(SPLIT_SETTINGS_REGEX)) {
        val settingKeyLength: Int = settingsChange.indexOf('=')

        if (settingKeyLength == -1) {
          Logging.logger.warning(I18n.format("ignoringMalformedInlineSetting", settingsChange))
          continue
        }

        val settingKey: String = settingsChange.substring(0, settingKeyLength).trim()
        val settingValue: String = settingsChange.substring(settingKeyLength + 1).trim()
        settingsMap[settingKey] = settingValue
      }

      return settingsMap
    }
  }
}
