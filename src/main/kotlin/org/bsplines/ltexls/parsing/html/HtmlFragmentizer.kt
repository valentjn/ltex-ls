/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.html

import org.bsplines.ltexls.parsing.CodeFragment
import org.bsplines.ltexls.parsing.CodeFragmentizer
import org.bsplines.ltexls.settings.Settings

class HtmlFragmentizer(
  codeLanguageId: String,
) : CodeFragmentizer(codeLanguageId) {
  override fun fragmentize(code: String, originalSettings: Settings): List<CodeFragment> {
    return listOf(CodeFragment(codeLanguageId, code, 0, originalSettings))
  }
}
