/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.program

import org.bsplines.ltexls.parsing.CodeFragment
import org.bsplines.ltexls.parsing.RegexCodeFragmentizer
import org.bsplines.ltexls.settings.Settings

class ProgramFragmentizer(
  codeLanguageId: String,
) : RegexCodeFragmentizer(
  codeLanguageId,
  ProgramCommentRegexs.fromCodeLanguageId(codeLanguageId).getMagicCommentRegex(),
) {
  override fun fragmentize(code: String, originalSettings: Settings): List<CodeFragment> {
    val oldCodeFragments: List<CodeFragment> = super.fragmentize(code, originalSettings)
    val result = ArrayList<CodeFragment>()

    for (oldCodeFragment: CodeFragment in oldCodeFragments) {
      val settings: Settings = oldCodeFragment.settings

      val dictionary = HashSet<String>(settings.dictionary)
      dictionary.addAll(DICTIONARY)

      val disabledRules = HashSet<String>(settings.disabledRules)

      for (ruleId: String in DISABLED_RULES) {
        if (!settings.enabledRules.contains(ruleId)) {
          disabledRules.add(ruleId)
        }
      }

      result.add(oldCodeFragment.copy(settings = settings.copy(
        _allDictionaries = settings.getModifiedDictionary(dictionary),
        _allDisabledRules = settings.getModifiedDisabledRules(disabledRules),
      )))
    }

    return result
  }

  companion object {
    val DICTIONARY = setOf(
      "@param",
      "@return",
      "param",
    )

    val DISABLED_RULES = setOf(
      "COPYRIGHT",
      "DASH_RULE",
      "R_SYMBOL",
      "UPPERCASE_SENTENCE_START",
      "WHITESPACE_RULE",
    )
  }
}
