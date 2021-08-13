/* Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.markdown

import org.bsplines.ltexls.parsing.RegexCodeFragmentizer

class MarkdownFragmentizer(codeLanguageId: String) : RegexCodeFragmentizer(codeLanguageId, REGEX) {
  companion object {
    private val REGEX = Regex(
      "^[ \t]*\\[[^]]+]:[ \t]*<>[ \t]*\"[ \t]*(?i)ltex(?-i):(.*?)\"[ \t]*$|"
      + "^[ \t]*<!--[ \t]*(?i)ltex(?-i):(.*?)[ \t]*-->[ \t]*$",
      RegexOption.MULTILINE,
    )
  }
}
