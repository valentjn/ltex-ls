/* Copyright (C) 2019-2023 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.languagetool

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.bsplines.ltexls.parsing.AnnotatedTextFragment
import org.bsplines.ltexls.server.LtexTextDocumentItem
import org.bsplines.ltexls.tools.Tools
import org.eclipse.lsp4j.Range
import org.languagetool.rules.RuleMatch

data class LanguageToolRuleMatch(
  val ruleId: String?,
  val sentence: String?,
  val fromPos: Int,
  val toPos: Int,
  val message: String,
  val suggestedReplacements: List<String>,
  val type: RuleMatch.Type,
  val languageShortCode: String,
) {
  fun isIntersectingWithRange(range: Range, document: LtexTextDocumentItem): Boolean {
    return Tools.areRangesIntersecting(
      Range(document.convertPosition(this.fromPos), document.convertPosition(this.toPos)),
      range,
    )
  }

  fun isUnknownWordRule(): Boolean {
    return isUnknownWordRule(this.ruleId)
  }

  companion object {
    private val TWO_OR_MORE_SPACES_REGEX = Regex("[ \n]{2,}")

    fun fromLanguageTool(
      match: RuleMatch,
      annotatedTextFragment: AnnotatedTextFragment,
    ): LanguageToolRuleMatch {
      return fromLanguageTool(
        match.rule?.id,
        match.sentence?.text,
        match.fromPos,
        match.toPos,
        match.message,
        match.suggestedReplacements,
        match.type,
        annotatedTextFragment,
      )
    }

    fun fromLanguageTool(
      jsonMatch: JsonObject,
      annotatedTextFragment: AnnotatedTextFragment,
    ): LanguageToolRuleMatch {
      val fromPos: Int = jsonMatch.get("offset").asInt
      val suggestedReplacements = ArrayList<String>()

      for (replacement: JsonElement in jsonMatch.get("replacements").asJsonArray) {
        suggestedReplacements.add(replacement.asJsonObject.get("value").asString)
      }

      return fromLanguageTool(
        jsonMatch.get("rule").asJsonObject.get("id").asString,
        jsonMatch.get("sentence").asString,
        fromPos,
        fromPos + jsonMatch.get("length").asInt,
        jsonMatch.get("message").asString,
        suggestedReplacements,
        RuleMatch.Type.Hint,
        annotatedTextFragment,
      )
    }

    @Suppress("LongParameterList")
    fun fromLanguageTool(
      ruleId: String?,
      sentence: String?,
      fromPos: Int,
      toPos: Int,
      languageToolMessage: String,
      suggestedReplacements: List<String>,
      type: RuleMatch.Type,
      annotatedTextFragment: AnnotatedTextFragment,
    ): LanguageToolRuleMatch {
      val messageBuilder = StringBuilder()

      if (isUnknownWordRule(ruleId)) {
        messageBuilder.append("'")
        messageBuilder.append(annotatedTextFragment.getSubstringOfPlainText(fromPos, toPos))
        messageBuilder.append("': ")
      }

      messageBuilder.append(languageToolMessage)
      val message = messageBuilder.toString().replace(TWO_OR_MORE_SPACES_REGEX, " ").trim()

      return LanguageToolRuleMatch(
        ruleId,
        sentence,
        fromPos,
        toPos,
        message,
        suggestedReplacements,
        type,
        annotatedTextFragment.codeFragment.languageShortCode,
      )
    }

    fun isUnknownWordRule(ruleId: String?): Boolean {
      return ((ruleId != null) && (
          ruleId.startsWith("MORFOLOGIK_")
          || ruleId.startsWith("HUNSPELL_")
          || ruleId.endsWith("_SPELLER_RULE")
          || ruleId.endsWith("_SPELLING_RULE")
          || (ruleId == "MUZSKY_ROD_NEZIV_A")
          || (ruleId == "ZENSKY_ROD_A")
          || (ruleId == "STREDNY_ROD_A")))
    }
  }
}
