/* Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.server

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import org.bsplines.ltexls.languagetool.LanguageToolRuleMatch
import org.bsplines.ltexls.parsing.AnnotatedTextFragment
import org.bsplines.ltexls.parsing.CodeFragment
import org.bsplines.ltexls.settings.SettingsManager
import org.bsplines.ltexls.tools.I18n
import org.bsplines.ltexls.tools.Logging
import org.eclipse.lsp4j.CodeAction
import org.eclipse.lsp4j.CodeActionKind
import org.eclipse.lsp4j.CodeActionParams
import org.eclipse.lsp4j.Command
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DiagnosticSeverity
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.ResourceOperation
import org.eclipse.lsp4j.TextDocumentEdit
import org.eclipse.lsp4j.TextEdit
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier
import org.eclipse.lsp4j.WorkspaceEdit
import org.eclipse.lsp4j.jsonrpc.messages.Either

class CodeActionGenerator(
  val settingsManager: SettingsManager,
) {
  fun createDiagnostic(match: LanguageToolRuleMatch, document: LtexTextDocumentItem): Diagnostic {
    val diagnostic = Diagnostic()
    diagnostic.range = Range(document.convertPosition(match.fromPos),
        document.convertPosition(match.toPos))

    val diagnosticSeverityMap: Map<String, DiagnosticSeverity> =
        this.settingsManager.settings.diagnosticSeverity
    var diagnosticSeverity: DiagnosticSeverity? = diagnosticSeverityMap[match.ruleId]
    if (diagnosticSeverity == null) diagnosticSeverity = diagnosticSeverityMap["default"]
    if (diagnosticSeverity == null) diagnosticSeverity = DiagnosticSeverity.Information
    diagnostic.severity = diagnosticSeverity

    diagnostic.source = "LTeX"
    diagnostic.message = match.message.replace(SUGGESTION_REGEX, "'$1'") + " \u2013 " + match.ruleId
    return diagnostic
  }

  @Suppress("ComplexMethod")
  fun generate(
    params: CodeActionParams,
    document: LtexTextDocumentItem,
    checkingResult: Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>>,
  ): List<Either<Command, CodeAction>> {
    val annotatedTextFragments: List<AnnotatedTextFragment> = checkingResult.second
    val result = ArrayList<Either<Command, CodeAction>>()

    val acceptSuggestionsMatchesMap = LinkedHashMap<String, ArrayList<LanguageToolRuleMatch>>()
    val addToDictionaryMatches = ArrayList<LanguageToolRuleMatch>()
    val hideFalsePositivesMatches = ArrayList<LanguageToolRuleMatch>()
    val disableRulesMatches = ArrayList<LanguageToolRuleMatch>()

    for (match: LanguageToolRuleMatch in checkingResult.first) {
      if (!match.isIntersectingWithRange(params.range, document)) continue

      for (newWord: String in match.suggestedReplacements) {
        if ((!acceptSuggestionsMatchesMap.containsKey(newWord))
              && (acceptSuggestionsMatchesMap.size
                >= MAX_NUMBER_OF_ACCEPT_SUGGESTIONS_CODE_ACTIONS)) {
          continue
        }

        val acceptSuggestionsMatches: ArrayList<LanguageToolRuleMatch> =
        acceptSuggestionsMatchesMap[newWord] ?: run {
          val acceptSuggestionsMatches = ArrayList<LanguageToolRuleMatch>()
          acceptSuggestionsMatchesMap[newWord] = acceptSuggestionsMatches
          acceptSuggestionsMatches
        }

        acceptSuggestionsMatches.add(match)
      }

      if (match.isUnknownWordRule()) addToDictionaryMatches.add(match)
      if (match.sentence != null) hideFalsePositivesMatches.add(match)
      disableRulesMatches.add(match)
    }

    for (
      (newWord: String, acceptSuggestionsMatches: List<LanguageToolRuleMatch>)
      in acceptSuggestionsMatchesMap
    ) {
      result.add(Either.forRight(getAcceptSuggestionsCodeAction(
          document, newWord, acceptSuggestionsMatches)))
    }

    if (addToDictionaryMatches.isNotEmpty()
          && this.settingsManager.settings.languageToolHttpServerUri.isEmpty()) {
      result.add(Either.forRight(getAddWordToDictionaryCodeAction(document,
          addToDictionaryMatches, annotatedTextFragments)))
    }

    if (hideFalsePositivesMatches.isNotEmpty()) {
      result.add(Either.forRight(getHideFalsePositivesCodeAction(document,
          hideFalsePositivesMatches, annotatedTextFragments)))
    }

    if (disableRulesMatches.isNotEmpty()) {
      result.add(Either.forRight(getDisableRulesCodeAction(document,
          disableRulesMatches, annotatedTextFragments)))
    }

    return result
  }

  private fun getAcceptSuggestionsCodeAction(
    document: LtexTextDocumentItem,
    newWord: String,
    acceptSuggestionsMatches: List<LanguageToolRuleMatch>,
  ): CodeAction {
    val textDocument = VersionedTextDocumentIdentifier(document.uri, document.version)
    val diagnostics = ArrayList<Diagnostic>()
    val documentChanges = ArrayList<Either<TextDocumentEdit, ResourceOperation>>()

    for (match: LanguageToolRuleMatch in acceptSuggestionsMatches) {
      val diagnostic: Diagnostic = createDiagnostic(match, document)
      val range: Range = diagnostic.range

      diagnostics.add(diagnostic)
      documentChanges.add(Either.forLeft(TextDocumentEdit(textDocument,
          listOf(TextEdit(range, newWord)))))
    }

    val codeAction = CodeAction(if (acceptSuggestionsMatches.size == 1)
        I18n.format("useWord", newWord) else I18n.format("useWordAllSelectedMatches", newWord))
    codeAction.kind = ACCEPT_SUGGESTIONS_CODE_ACTION_KIND
    codeAction.diagnostics = diagnostics
    codeAction.edit = WorkspaceEdit(documentChanges)

    return codeAction
  }

  private fun getAddWordToDictionaryCodeAction(
    document: LtexTextDocumentItem,
    addToDictionaryMatches: List<LanguageToolRuleMatch>,
    annotatedTextFragments: List<AnnotatedTextFragment>,
  ): CodeAction {
    val unknownWordsMap = HashMap<String, MutableList<String>>()
    val unknownWordsJsonObject = JsonObject()
    val diagnostics = ArrayList<Diagnostic>()

    for (match: LanguageToolRuleMatch in addToDictionaryMatches) {
      val fragmentIndex: Int = findAnnotatedTextFragmentWithMatch(
          annotatedTextFragments, match)

      if (fragmentIndex == -1) {
        Logging.logger.warning(I18n.format("couldNotFindFragmentForMatch"))
        continue
      }

      val annotatedTextFragment: AnnotatedTextFragment = annotatedTextFragments[fragmentIndex]
      val codeFragment: CodeFragment = annotatedTextFragment.codeFragment
      val language: String = codeFragment.settings.languageShortCode
      val offset: Int = codeFragment.fromPos
      val word: String = annotatedTextFragment.getSubstringOfPlainText(
          match.fromPos - offset, match.toPos - offset)

      addToMap(language, word, unknownWordsMap, unknownWordsJsonObject)
      diagnostics.add(createDiagnostic(match, document))
    }

    val arguments = JsonObject()
    arguments.addProperty("uri", document.uri)
    arguments.add("words", unknownWordsJsonObject)

    val onlyUnknownWord: String? = getOnlyEntry(unknownWordsMap)
    val commandTitle: String = (if (onlyUnknownWord != null)
        I18n.format("addWordToDictionary", onlyUnknownWord) else
        I18n.format("addAllUnknownWordsInSelectionToDictionary"))
    val command = Command(commandTitle, ADD_TO_DICTIONARY_COMMAND_NAME)
    command.arguments = listOf(arguments)

    val codeAction = CodeAction(command.title)
    codeAction.kind = ADD_TO_DICTIONARY_CODE_ACTION_KIND
    codeAction.diagnostics = diagnostics
    codeAction.command = command

    return codeAction
  }

  @Suppress("LoopWithTooManyJumpStatements")
  private fun getHideFalsePositivesCodeAction(
    document: LtexTextDocumentItem,
    hideFalsePositivesMatches: List<LanguageToolRuleMatch>,
    annotatedTextFragments: List<AnnotatedTextFragment>,
  ): CodeAction {
    val ruleIdSentencePairs = ArrayList<Pair<String, String>>()
    val hiddenFalsePositivesMap = HashMap<String, MutableList<String>>()
    val falsePositivesJsonObject = JsonObject()
    val diagnostics = ArrayList<Diagnostic>()

    for (match: LanguageToolRuleMatch in hideFalsePositivesMatches) {
      val ruleId: String? = match.ruleId
      val sentence: String? = match.sentence?.trim()
      if ((ruleId == null) || (sentence == null)) continue

      val pair: Pair<String, String> = Pair(ruleId, sentence)

      if (!ruleIdSentencePairs.contains(pair)) {
        val fragmentIndex: Int = findAnnotatedTextFragmentWithMatch(
            annotatedTextFragments, match)

        if (fragmentIndex == -1) {
          Logging.logger.warning(I18n.format("couldNotFindFragmentForMatch"))
          continue
        }

        val annotatedTextFragment: AnnotatedTextFragment = annotatedTextFragments[fragmentIndex]
        val codeFragment: CodeFragment = annotatedTextFragment.codeFragment
        val language: String = codeFragment.settings.languageShortCode

        val sentencePatternStringBuilder = StringBuilder()
        var lastEnd = 0

        for (matchResult: MatchResult in DUMMY_REGEX.findAll(sentence)) {
          sentencePatternStringBuilder.append(Regex.escape(
              sentence.substring(lastEnd, matchResult.range.first)))
          sentencePatternStringBuilder.append(DUMMY_REGEX_STRING)
          lastEnd = matchResult.range.last + 1
        }

        if (lastEnd < sentence.length) {
          sentencePatternStringBuilder.append(Regex.escape(sentence.substring(lastEnd)))
        }

        ruleIdSentencePairs.add(pair)

        val falsePositiveJson = JsonObject()
        falsePositiveJson.add("rule", JsonPrimitive(ruleId))
        val sentencePatternString = "^$sentencePatternStringBuilder$"
        falsePositiveJson.add("sentence", JsonPrimitive(sentencePatternString))

        addToMap(language, falsePositiveJson.toString(), hiddenFalsePositivesMap,
            falsePositivesJsonObject)
      }

      diagnostics.add(createDiagnostic(match, document))
    }

    val arguments = JsonObject()
    arguments.addProperty("uri", document.uri)
    arguments.add("falsePositives", falsePositivesJsonObject)

    val command = Command((if (ruleIdSentencePairs.size == 1)
          I18n.format("hideFalsePositive") else
          I18n.format("hideAllFalsePositivesInTheSelectedSentences")),
        HIDE_FALSE_POSITIVES_COMMAND_NAME)
    command.arguments = listOf(arguments)

    val codeAction = CodeAction(command.title)
    codeAction.kind = HIDE_FALSE_POSITIVES_CODE_ACTION_KIND
    codeAction.diagnostics = diagnostics
    codeAction.command = command

    return codeAction
  }

  private fun getDisableRulesCodeAction(
    document: LtexTextDocumentItem,
    disableRuleMatches: List<LanguageToolRuleMatch>,
    annotatedTextFragments: List<AnnotatedTextFragment>,
  ): CodeAction {
    val ruleIdsMap = HashMap<String, MutableList<String>>()
    val ruleIdsJsonObject = JsonObject()
    val diagnostics = ArrayList<Diagnostic>()

    for (match: LanguageToolRuleMatch in disableRuleMatches) {
      val ruleId: String? = match.ruleId

      if (ruleId != null) {
        val fragmentIndex: Int = findAnnotatedTextFragmentWithMatch(
            annotatedTextFragments, match)

        if (fragmentIndex == -1) {
          Logging.logger.warning(I18n.format("couldNotFindFragmentForMatch"))
          continue
        }

        val annotatedTextFragment: AnnotatedTextFragment = annotatedTextFragments[fragmentIndex]
        val language: String = annotatedTextFragment.codeFragment.settings.languageShortCode
        addToMap(language, ruleId, ruleIdsMap, ruleIdsJsonObject)
      }

      diagnostics.add(createDiagnostic(match, document))
    }

    val arguments = JsonObject()
    arguments.addProperty("uri", document.uri)
    arguments.add("ruleIds", ruleIdsJsonObject)

    val commandTitle: String = (if (getOnlyEntry(ruleIdsMap) != null)
        I18n.format("disableRule") else
        I18n.format("disableAllRulesWithMatchesInSelection"))
    val command = Command(commandTitle, DISABLE_RULES_COMMAND_NAME)
    command.arguments = listOf(arguments)

    val codeAction = CodeAction(command.title)
    codeAction.kind = DISABLE_RULES_CODE_ACTION_KIND
    codeAction.diagnostics = diagnostics
    codeAction.command = command

    return codeAction
  }

  companion object {
    val SUGGESTION_REGEX = Regex("<suggestion>(.*?)</suggestion>")

    private const val ACCEPT_SUGGESTIONS_CODE_ACTION_KIND =
        CodeActionKind.QuickFix + ".ltex.acceptSuggestions"
    private const val ADD_TO_DICTIONARY_CODE_ACTION_KIND =
        CodeActionKind.QuickFix + ".ltex.addToDictionary"
    private const val DISABLE_RULES_CODE_ACTION_KIND =
        CodeActionKind.QuickFix + ".ltex.disableRules"
    private const val HIDE_FALSE_POSITIVES_CODE_ACTION_KIND =
        CodeActionKind.QuickFix + ".ltex.hideFalsePositives"
    private const val ADD_TO_DICTIONARY_COMMAND_NAME = "_ltex.addToDictionary"
    private const val DISABLE_RULES_COMMAND_NAME = "_ltex.disableRules"
    private const val HIDE_FALSE_POSITIVES_COMMAND_NAME = "_ltex.hideFalsePositives"
    private const val DUMMY_REGEX_STRING = "(?:Dummy|Ina|Jimmy-)[0-9]+"
    private val DUMMY_REGEX = Regex(DUMMY_REGEX_STRING)

    private const val MAX_NUMBER_OF_ACCEPT_SUGGESTIONS_CODE_ACTIONS = 5

    fun getCodeActionKinds(): List<String> {
      return listOf(ACCEPT_SUGGESTIONS_CODE_ACTION_KIND)
    }

    fun findAnnotatedTextFragmentWithMatch(
      annotatedTextFragments: List<AnnotatedTextFragment>,
      match: LanguageToolRuleMatch,
    ): Int {
      for (i in annotatedTextFragments.indices) {
        if (annotatedTextFragments[i].codeFragment.contains(match)) return i
      }

      return -1
    }

    private fun addToMap(
      key: String,
      value: String,
      map: MutableMap<String, MutableList<String>>,
      jsonObject: JsonObject,
    ) {
      val unknownWordsList: MutableList<String> = map[key] ?: run {
        val unknownWordsList = ArrayList<String>()
        map[key] = unknownWordsList
        jsonObject.add(key, JsonArray())
        unknownWordsList
      }

      val unknownWordsJsonArray: JsonArray = jsonObject.getAsJsonArray(key)

      if (!unknownWordsList.contains(value)) {
        unknownWordsList.add(value)
        unknownWordsJsonArray.add(value)
      }
    }

    private fun getOnlyEntry(map: Map<String, List<String>>): String? {
      return if (map.size == 1) {
        val list: List<String> = map.values.toList()[0]
        if (list.size == 1) list[0] else null
      } else {
        null
      }
    }
  }
}
