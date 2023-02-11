/* Copyright (C) 2019-2023 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.gitcommit

import org.bsplines.ltexls.parsing.CharacterBasedCodeAnnotatedTextBuilder

class GitCommitAnnotatedTextBuilder(
  codeLanguageId: String,
) : CharacterBasedCodeAnnotatedTextBuilder(codeLanguageId) {
  override fun processCharacter() {
    var matchResult: MatchResult? = null

    if (
      this.isStartOfLine
      && (matchFromPosition(COMMENT_REGEX)?.also { matchResult = it } != null)
    ) {
      addMarkup(matchResult?.value, "\n")
    } else {
      addText(this.curString)
    }
  }

  companion object {
    private val COMMENT_REGEX = Regex("^[ \t]*#([^\r\n]*?)(\r?\n|$)")
  }
}
