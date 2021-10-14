/* Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing

import org.bsplines.ltexls.languagetool.LanguageToolRuleMatch
import org.bsplines.ltexls.settings.Settings

data class CodeFragment(
  val codeLanguageId: String,
  val code: String,
  val fromPos: Int,
  val settings: Settings,
  var languageShortCode: String = settings.languageShortCode,
) {
  fun contains(match: LanguageToolRuleMatch): Boolean {
    return ((match.fromPos >= this.fromPos)
        && (match.toPos <= this.fromPos + this.code.length))
  }
}
