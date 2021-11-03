/* Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
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
    @Suppress("ComplexMethod")
    fun create(codeLanguageId: String): CodeAnnotatedTextBuilder {
      return when (codeLanguageId) {
        "bib" -> LatexAnnotatedTextBuilder(codeLanguageId)
        "bibtex" -> LatexAnnotatedTextBuilder(codeLanguageId)
        "html" -> HtmlAnnotatedTextBuilder(codeLanguageId)
        "latex" -> LatexAnnotatedTextBuilder(codeLanguageId)
        "markdown" -> MarkdownAnnotatedTextBuilder(codeLanguageId)
        "nop" -> NopAnnotatedTextBuilder(codeLanguageId)
        "org" -> OrgAnnotatedTextBuilder(codeLanguageId)
        "plaintex" -> LatexAnnotatedTextBuilder(codeLanguageId)
        "plaintext" -> PlaintextAnnotatedTextBuilder(codeLanguageId)
        "restructuredtext" -> RestructuredtextAnnotatedTextBuilder(codeLanguageId)
        "rsweave" -> LatexAnnotatedTextBuilder(codeLanguageId)
        "tex" -> LatexAnnotatedTextBuilder(codeLanguageId)
        "xhtml" -> HtmlAnnotatedTextBuilder(codeLanguageId)
        else -> {
          if (ProgramCommentRegexs.isSupportedCodeLanguageId(codeLanguageId)) {
            ProgramAnnotatedTextBuilder(codeLanguageId)
          } else {
            Logging.logger.warning(I18n.format("unsupportedCodeLanguageId", codeLanguageId))
            PlaintextAnnotatedTextBuilder("plaintext")
          }
        }
      }
    }
  }
}
