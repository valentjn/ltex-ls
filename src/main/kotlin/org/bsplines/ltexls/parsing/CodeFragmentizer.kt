/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing

import org.bsplines.ltexls.parsing.bibtex.BibtexFragmentizer
import org.bsplines.ltexls.parsing.html.HtmlFragmentizer
import org.bsplines.ltexls.parsing.latex.LatexFragmentizer
import org.bsplines.ltexls.parsing.markdown.MarkdownFragmentizer
import org.bsplines.ltexls.parsing.nop.NopFragmentizer
import org.bsplines.ltexls.parsing.org.OrgFragmentizer
import org.bsplines.ltexls.parsing.plaintext.PlaintextFragmentizer
import org.bsplines.ltexls.parsing.program.ProgramCommentRegexs
import org.bsplines.ltexls.parsing.program.ProgramFragmentizer
import org.bsplines.ltexls.parsing.restructuredtext.RestructuredtextFragmentizer
import org.bsplines.ltexls.settings.Settings
import org.bsplines.ltexls.tools.I18n
import org.bsplines.ltexls.tools.Logging

abstract class CodeFragmentizer(
  val codeLanguageId: String,
) {
  abstract fun fragmentize(code: String, originalSettings: Settings): List<CodeFragment>

  fun fragmentize(fragments: List<CodeFragment>): List<CodeFragment> {
    val newFragments = ArrayList<CodeFragment>()

    for (oldFragment: CodeFragment in fragments) {
      val curNewFragments: List<CodeFragment> = fragmentize(oldFragment.code, oldFragment.settings)

      for (newFragment: CodeFragment in curNewFragments) {
        newFragments.add(newFragment.copy(
            fromPos = newFragment.fromPos + oldFragment.fromPos))
      }
    }

    return newFragments
  }

  companion object {
    private val constructorMap:
          MutableMap<String, (codeLanguageId: String) -> CodeFragmentizer> = run {
      val constructorMap = HashMap<String, (codeLanguageId: String) -> CodeFragmentizer>()

      constructorMap["bibtex"] = { codeLanguageId: String -> BibtexFragmentizer(codeLanguageId) }
      constructorMap["html"] = { codeLanguageId: String -> HtmlFragmentizer(codeLanguageId) }
      constructorMap["latex"] = { codeLanguageId: String -> LatexFragmentizer(codeLanguageId) }
      constructorMap["markdown"] = { codeLanguageId: String -> MarkdownFragmentizer(codeLanguageId) }
      constructorMap["nop"] = { codeLanguageId: String -> NopFragmentizer(codeLanguageId) }
      constructorMap["org"] = { codeLanguageId: String -> OrgFragmentizer(codeLanguageId) }
      constructorMap["plaintext"] = { codeLanguageId: String -> PlaintextFragmentizer(codeLanguageId) }
      constructorMap["restructuredtext"] = { codeLanguageId: String -> RestructuredtextFragmentizer(codeLanguageId) }
      constructorMap["rsweave"] = { codeLanguageId: String -> LatexFragmentizer(codeLanguageId) }
      constructorMap["tex"] = { codeLanguageId: String -> LatexFragmentizer(codeLanguageId) }

      constructorMap
    }

    fun create(codeLanguageId: String): CodeFragmentizer {
      val constructor: ((codeLanguageId: String) -> CodeFragmentizer)? =
          constructorMap[codeLanguageId]

      return when {
        constructor != null -> constructor(codeLanguageId)
        ProgramCommentRegexs.isSupportedCodeLanguageId(codeLanguageId) -> ProgramFragmentizer(codeLanguageId)
        else -> {
          Logging.logger.warning(I18n.format("unsupportedCodeLanguageId", codeLanguageId))
          PlaintextFragmentizer("plaintext")
        }
      }
    }
  }
}
