/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.bibtex

import org.bsplines.ltexls.parsing.CodeFragment
import org.bsplines.ltexls.parsing.CodeFragmentizer
import org.bsplines.ltexls.parsing.DummyGenerator
import org.bsplines.ltexls.parsing.latex.LatexCommandSignature
import org.bsplines.ltexls.parsing.latex.LatexCommandSignatureMatch
import org.bsplines.ltexls.parsing.latex.LatexCommandSignatureMatcher
import org.bsplines.ltexls.parsing.latex.LatexFragmentizer
import org.bsplines.ltexls.parsing.latex.LatexPackageOption
import org.bsplines.ltexls.parsing.latex.LatexPackageOptionsParser
import org.bsplines.ltexls.settings.Settings

class BibtexFragmentizer(codeLanguageId: String) : CodeFragmentizer(codeLanguageId) {
  private val latexFragmentizer = LatexFragmentizer(codeLanguageId)

  override fun fragmentize(code: String, originalSettings: Settings): List<CodeFragment> {
    var fragments: List<CodeFragment> = listOf(
      CodeFragment(codeLanguageId, code, 0, originalSettings),
    )

    fragments = latexFragmentizer.fragmentize(fragments)
    fragments = fragmentizeBibtexFields(fragments)

    return fragments
  }

  private fun fragmentizeBibtexFields(fragments: List<CodeFragment>): List<CodeFragment> {
    val newFragments = ArrayList<CodeFragment>()

    for ((_, oldFragmentCode: String, fromPos: Int, oldFragmentSettings: Settings) in fragments) {
      val newFragmentDisabledRules = HashSet(oldFragmentSettings.disabledRules)
      newFragmentDisabledRules.add("UPPERCASE_SENTENCE_START")

      val newFragmentSettings: Settings = oldFragmentSettings.copy(
        _allDisabledRules = oldFragmentSettings.getModifiedDisabledRules(newFragmentDisabledRules)
      )

      BIBTEX_ENTRY_COMMAND_SIGNATURE_MATCHER.startMatching(oldFragmentCode, emptySet())

      var bibtexFields: MutableMap<String, Boolean>? = null

      while (true) {
        val match: LatexCommandSignatureMatch =
            BIBTEX_ENTRY_COMMAND_SIGNATURE_MATCHER.findNextMatch() ?: break

        if (bibtexFields == null) {
          bibtexFields = HashMap(BibtexFragmentizerDefaults.defaultBibtexFields)
          bibtexFields.putAll(oldFragmentSettings.bibtexFields)
        }

        val argumentContents: String = match.getArgumentContents(match.getArgumentsSize() - 1)
        val argumentContentsFromPos: Int =
            match.getArgumentContentsFromPos(match.getArgumentsSize() - 1)
        val keyValuePairs: List<LatexPackageOption> =
            LatexPackageOptionsParser.parse(argumentContents)

        processKeyValuePairs(
          newFragments,
          newFragmentSettings,
          bibtexFields,
          fromPos,
          argumentContentsFromPos,
          keyValuePairs,
        )
      }
    }

    return newFragments
  }

  @Suppress("LongParameterList")
  private fun processKeyValuePairs(
    newFragments: MutableList<CodeFragment>,
    newFragmentSettings: Settings,
    bibtexFields: Map<String, Boolean>,
    fromPos: Int,
    argumentContentsFromPos: Int,
    keyValuePairs: List<LatexPackageOption>,
  ) {
    for (keyValuePair: LatexPackageOption in keyValuePairs) {
      val fieldName: String = keyValuePair.keyInfo.plainText
      val inBibtexFields: Boolean? = bibtexFields[fieldName]

      if (
        (keyValuePair.valueInfo.fromPos == -1)
        || ((inBibtexFields != null) && !inBibtexFields)
      ) {
        continue
      }

      newFragments.add(
        CodeFragment(
          "latex", keyValuePair.value,
          fromPos + argumentContentsFromPos + keyValuePair.valueInfo.fromPos,
          newFragmentSettings
        )
      )
    }
  }

  companion object {
    private val BIBTEX_ENTRY_COMMAND_SIGNATURE = LatexCommandSignature(
      "@[A-Za-z]+{}", LatexCommandSignature.Action.Ignore, DummyGenerator.getInstance(), false
    )

    private val BIBTEX_ENTRY_COMMAND_SIGNATURE_MATCHER = LatexCommandSignatureMatcher(
      listOf(BIBTEX_ENTRY_COMMAND_SIGNATURE), false
    )
  }
}
