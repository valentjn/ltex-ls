/* Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.markdown

import org.bsplines.ltexls.parsing.CodeFragment
import org.bsplines.ltexls.parsing.CodeFragmentizer
import org.bsplines.ltexls.parsing.RegexCodeFragmentizer
import org.bsplines.ltexls.settings.Settings

class MarkdownFragmentizer(codeLanguageId: String) : CodeFragmentizer(codeLanguageId) {
  private val commentFragmentizer = RegexCodeFragmentizer(codeLanguageId, COMMENT_REGEX)

  override fun fragmentize(code: String, originalSettings: Settings): List<CodeFragment> {
    var fragments: List<CodeFragment> = fragmentizeYamlFrontMatter(code, originalSettings)
    fragments = commentFragmentizer.fragmentize(fragments)

    return fragments
  }

  private fun fragmentizeYamlFrontMatter(
    code: String,
    originalSettings: Settings,
  ): List<CodeFragment> {
    val matchResult: MatchResult? = YAML_FRONT_MATTER_REGEX.find(code)
    val settings: Settings = run {
      var firstGroupValue = true
      var languageShortCode = ""

      for (groupValue: String in matchResult?.groupValues ?: emptyList()) {
        if (firstGroupValue) {
          firstGroupValue = false
        } else if (groupValue.isNotEmpty()) {
          languageShortCode = groupValue
          break
        }
      }

      if (languageShortCode.isNotEmpty()) {
        originalSettings.copy(_languageShortCode = languageShortCode)
      } else {
        originalSettings
      }
    }

    return listOf(CodeFragment(codeLanguageId, code, 0, settings))
  }

  companion object {
    private val YAML_FRONT_MATTER_REGEX = Regex(
      "\\A---[ \t]*$.*?^lang:[ \t]+(?:\"(.+)\"|'(.+)'|(.+))$.*?^---[ \t]*$",
      setOf(RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL),
    )

    private val COMMENT_REGEX = Regex(
      "^[ \t]*\\[[^]]+]:[ \t]*<>[ \t]*\"[ \t]*(?i)ltex(?-i):(.*?)\"[ \t]*$|"
      + "^[ \t]*<!--[ \t]*(?i)ltex(?-i):(.*?)[ \t]*-->[ \t]*$",
      RegexOption.MULTILINE,
    )
  }
}
