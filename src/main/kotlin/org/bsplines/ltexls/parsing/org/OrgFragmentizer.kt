/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.org

import org.bsplines.ltexls.parsing.RegexCodeFragmentizer

class OrgFragmentizer(
  codeLanguageId: String,
) : RegexCodeFragmentizer(codeLanguageId, REGEX) {
  companion object {
    private val REGEX = Regex(
      "^[ \t]*#[ \t]+(?i)ltex(?-i):(.*?)[ \t]*$",
      RegexOption.MULTILINE,
    )
  }
}
