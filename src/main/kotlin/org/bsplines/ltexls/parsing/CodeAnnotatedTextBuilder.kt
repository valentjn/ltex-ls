/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing

import org.bsplines.ltexls.parsing.html.HtmlAnnotatedTextBuilder
import org.bsplines.ltexls.parsing.latex.LatexAnnotatedTextBuilder
import org.bsplines.ltexls.parsing.markdown.MarkdownAnnotatedTextBuilder
import org.bsplines.ltexls.parsing.nop.NopAnnotatedTextBuilder
import org.bsplines.ltexls.parsing.org.OrgAnnotatedTextBuilder
import org.bsplines.ltexls.parsing.plaintext.PlaintextAnnotatedTextBuilder
import org.bsplines.ltexls.parsing.program.ProgramAnnotatedTextBuilder
import org.bsplines.ltexls.parsing.program.ProgramCommentRegexs
import org.bsplines.ltexls.parsing.restructuredtext.RestructuredtextAnnotatedTextBuilder
import org.bsplines.ltexls.settings.Settings
import org.bsplines.ltexls.tools.I18n
import org.bsplines.ltexls.tools.Logging
import org.languagetool.markup.AnnotatedTextBuilder

abstract class CodeAnnotatedTextBuilder(
  val codeLanguageId: String,
) : AnnotatedTextBuilder() {
  abstract fun addCode(code: String): CodeAnnotatedTextBuilder

  @Suppress("UNUSED_PARAMETER")
  open fun setSettings(settings: Settings) {
  }

  companion object {
    private val constructorMap:
          MutableMap<String, (codeLanguageId: String) -> CodeAnnotatedTextBuilder> = run {
      val constructorMap = HashMap<String, (codeLanguageId: String) -> CodeAnnotatedTextBuilder>()

      constructorMap["bibtex"] = {
          codeLanguageId: String -> LatexAnnotatedTextBuilder(codeLanguageId)
      }
      constructorMap["html"] = {
        codeLanguageId: String -> HtmlAnnotatedTextBuilder(codeLanguageId)
      }
      constructorMap["latex"] = {
          codeLanguageId: String -> LatexAnnotatedTextBuilder(codeLanguageId)
      }
      constructorMap["markdown"] = {
        codeLanguageId: String -> MarkdownAnnotatedTextBuilder(codeLanguageId)
      }
      constructorMap["nop"] = {
        codeLanguageId: String -> NopAnnotatedTextBuilder(codeLanguageId)
      }
      constructorMap["org"] = {
        codeLanguageId: String -> OrgAnnotatedTextBuilder(codeLanguageId)
      }
      constructorMap["plaintext"] = {
        codeLanguageId: String -> PlaintextAnnotatedTextBuilder(codeLanguageId)
      }
      constructorMap["restructuredtext"] = {
        codeLanguageId: String -> RestructuredtextAnnotatedTextBuilder(codeLanguageId)
      }
      constructorMap["rsweave"] = {
          codeLanguageId: String -> LatexAnnotatedTextBuilder(codeLanguageId)
      }
      constructorMap["tex"] = {
          codeLanguageId: String -> LatexAnnotatedTextBuilder(codeLanguageId)
      }

      constructorMap
    }

    fun create(codeLanguageId: String): CodeAnnotatedTextBuilder {
      val constructor: ((codeLanguageId: String) -> CodeAnnotatedTextBuilder)? =
          constructorMap[codeLanguageId]

      return when {
        constructor != null -> {
          constructor(codeLanguageId)
        }
        ProgramCommentRegexs.isSupportedCodeLanguageId(codeLanguageId) -> {
          ProgramAnnotatedTextBuilder(codeLanguageId)
        }
        else -> {
          Logging.logger.warning(I18n.format("unsupportedCodeLanguageId", codeLanguageId))
          PlaintextAnnotatedTextBuilder("plaintext")
        }
      }
    }
  }
}
