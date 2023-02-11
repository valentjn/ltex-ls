/* Copyright (C) 2019-2023 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.server

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.bsplines.ltexls.client.LtexLanguageClient
import org.bsplines.ltexls.languagetool.LanguageToolRuleMatch
import org.bsplines.ltexls.parsing.AnnotatedTextFragment
import org.bsplines.ltexls.settings.Settings
import org.bsplines.ltexls.tools.I18n
import org.bsplines.ltexls.tools.Logging
import org.bsplines.ltexls.tools.Tools
import org.eclipse.lsp4j.ConfigurationItem
import org.eclipse.lsp4j.ConfigurationParams
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.ProgressParams
import org.eclipse.lsp4j.PublishDiagnosticsParams
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.TextDocumentContentChangeEvent
import org.eclipse.lsp4j.TextDocumentItem
import org.eclipse.lsp4j.WorkDoneProgressBegin
import org.eclipse.lsp4j.WorkDoneProgressCreateParams
import org.eclipse.lsp4j.WorkDoneProgressEnd
import org.eclipse.lsp4j.jsonrpc.CancelChecker
import org.eclipse.lsp4j.jsonrpc.messages.Either
import java.time.Instant
import java.util.concurrent.CancellationException

@Suppress("JoinDeclarationAndAssignment", "TooManyFunctions")
class LtexTextDocumentItem(
  val languageServer: LtexLanguageServer,
  uri: String,
  codeLanguageId: String,
  version: Int,
  text: String,
) : TextDocumentItem(uri, codeLanguageId, version, text) {
  var caretPosition: Position? = null
  var lastCaretChangeInstant: Instant = Instant.now()
  var diagnosticsCache: List<Diagnostic>? = null
    private set
  var beingChecked: Boolean = false
    private set
  var lspCancelChecker: CancelChecker? = null

  private val lineStartPosList: MutableList<Int> = ArrayList()
  private var checkingResult: Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>>? = null
  private var cancellationCounter = 0

  init {
    reinitializeLineStartPosList(text)
  }

  constructor(languageServer: LtexLanguageServer, document: TextDocumentItem) :
      this(languageServer, document.uri, document.languageId, document.version, document.text)

  private fun reinitializeLineStartPosList(text: String) {
    this.lineStartPosList.clear()
    this.lineStartPosList.add(0)

    var i = 0

    while (i < text.length) {
      val c: Char = text[i]

      if (c == '\r') {
        if ((i + 1 < text.length) && (text[i + 1] == '\n')) i++
        this.lineStartPosList.add(i + 1)
      } else if (c == '\n') {
        this.lineStartPosList.add(i + 1)
      }

      i++
    }
  }

  override fun equals(other: Any?): Boolean {
    if (other !is LtexTextDocumentItem) return false
    if (!super.equals(other)) return false
    if (this.lineStartPosList != other.lineStartPosList) return false
    if (this.checkingResult != other.checkingResult) return false
    if (this.diagnosticsCache != other.diagnosticsCache) return false
    if (this.caretPosition != other.caretPosition) return false
    if (this.lastCaretChangeInstant != other.lastCaretChangeInstant) return false

    return true
  }

  override fun hashCode(): Int {
    var hash = 3

    hash = 53 * hash + super.hashCode()
    hash = 53 * hash + this.lineStartPosList.hashCode()
    if (this.checkingResult != null) hash = 53 * hash + this.checkingResult.hashCode()
    hash = 53 * hash + this.diagnosticsCache.hashCode()
    if (this.caretPosition != null) hash = 53 * hash + this.caretPosition.hashCode()
    hash = 53 * hash + this.lastCaretChangeInstant.hashCode()

    return hash
  }

  @Suppress("NestedBlockDepth")
  fun convertPosition(position: Position): Int {
    val line: Int = position.line
    val character: Int = position.character
    val text: String = text

    return when {
      line < 0 -> 0
      line >= this.lineStartPosList.size -> text.length
      else -> {
        val lineStart: Int = this.lineStartPosList[line]
        val nextLineStart: Int = if (line < this.lineStartPosList.size - 1) {
          this.lineStartPosList[line + 1]
        } else {
          text.length
        }
        val lineLength: Int = nextLineStart - lineStart

        when {
          character < 0 -> lineStart
          character >= lineLength -> {
            var pos: Int = lineStart + lineLength

            if (pos >= 1) {
              if (text[pos - 1] == '\r') {
                pos--
              } else if (text[pos - 1] == '\n') {
                pos--
                if ((pos >= 1) && (text[pos - 1] == '\r')) pos--
              }
            }

            pos
          }
          else -> lineStart + character
        }
      }
    }
  }

  fun convertPosition(pos: Int): Position {
    var line: Int = this.lineStartPosList.binarySearch(pos)

    if (line < 0) {
      val insertionPoint: Int = -line - 1
      line = insertionPoint - 1
    }

    return Position(line, pos - this.lineStartPosList[line])
  }

  override fun setText(text: String) {
    val oldText: String = getText()
    super.setText(text)
    reinitializeLineStartPosList(text)
    this.checkingResult = null
    this.diagnosticsCache = null
    this.caretPosition = guessCaretPositionInFullUpdate(oldText)
    if (this.caretPosition != null) this.lastCaretChangeInstant = Instant.now()
  }

  fun applyTextChangeEvents(textChangeEvents: List<TextDocumentContentChangeEvent>) {
    val oldLastCaretChangeInstant: Instant = this.lastCaretChangeInstant

    for (textChangeEvent: TextDocumentContentChangeEvent in textChangeEvents) {
      applyTextChangeEvent(textChangeEvent)
    }

    if (textChangeEvents.size > 1) {
      this.caretPosition = null
      this.lastCaretChangeInstant = oldLastCaretChangeInstant
    }
  }

  fun applyTextChangeEvent(textChangeEvent: TextDocumentContentChangeEvent) {
    val changeRange: Range? = textChangeEvent.range
    val changeText: String = textChangeEvent.text
    val oldText: String = text
    val fromPos: Int
    val toPos: Int
    val newText: String

    if (changeRange != null) {
      fromPos = convertPosition(changeRange.start)
      toPos = if (changeRange.end != changeRange.start) {
        convertPosition(changeRange.end)
      } else {
        fromPos
      }
      newText = oldText.substring(0, fromPos) + changeText + oldText.substring(toPos)
    } else {
      fromPos = -1
      toPos = -1
      newText = changeText
    }

    super.setText(newText)
    reinitializeLineStartPosList(newText)
    this.checkingResult = null
    this.diagnosticsCache = null

    if (changeRange != null) {
      this.caretPosition =
          guessCaretPositionInIncrementalUpdate(changeRange, changeText, fromPos, toPos)
    } else {
      this.caretPosition = guessCaretPositionInFullUpdate(oldText)
    }

    if (this.caretPosition != null) this.lastCaretChangeInstant = Instant.now()
  }

  private fun guessCaretPositionInIncrementalUpdate(
    changeRange: Range,
    changeText: String,
    fromPos: Int,
    toPos: Int,
  ): Position? {
    return when {
      fromPos == toPos -> convertPosition(toPos + changeText.length)
      changeText.isEmpty() -> Position(changeRange.start.line, changeRange.start.character)
      else -> null
    }
  }

  private fun guessCaretPositionInFullUpdate(oldText: String): Position? {
    val newText: String = text
    var numberOfEqualCharsAtStart = 0

    while ((numberOfEqualCharsAtStart < oldText.length)
          && (numberOfEqualCharsAtStart < newText.length)
          && (oldText[numberOfEqualCharsAtStart] == newText[numberOfEqualCharsAtStart])) {
      numberOfEqualCharsAtStart++
    }

    var numberOfEqualCharsAtEnd = 0

    while ((numberOfEqualCharsAtEnd < oldText.length - numberOfEqualCharsAtStart)
          && (numberOfEqualCharsAtEnd < newText.length - numberOfEqualCharsAtStart)
          && (oldText[oldText.length - numberOfEqualCharsAtEnd - 1]
            == newText[newText.length - numberOfEqualCharsAtEnd - 1])) {
      numberOfEqualCharsAtEnd++
    }

    val numberOfEqualChars: Int = numberOfEqualCharsAtStart + numberOfEqualCharsAtEnd

    if ((numberOfEqualChars < oldText.length / 2.0)
          || (numberOfEqualChars < newText.length / 2.0)) {
      return null
    }

    return convertPosition(newText.length - numberOfEqualCharsAtEnd)
  }

  @Suppress("unused")
  fun checkAndPublishDiagnosticsWithCache(range: Range? = null): Boolean {
    return checkAndPublishDiagnostics(range, true)
  }

  fun checkAndPublishDiagnosticsWithoutCache(range: Range? = null): Boolean {
    return checkAndPublishDiagnostics(range, false)
  }

  private fun checkAndPublishDiagnostics(range: Range?, useCache: Boolean): Boolean {
    val languageClient: LtexLanguageClient? = this.languageServer.languageClient
    checkAndGetDiagnostics(range, useCache)

    if (languageClient == null) return false
    val diagnosticsNotAtCaret: List<Diagnostic> = extractDiagnosticsNotAtCaret() ?: return false

    languageClient.publishDiagnostics(PublishDiagnosticsParams(uri, diagnosticsNotAtCaret))
    val diagnostics: List<Diagnostic>? = this.diagnosticsCache

    if ((diagnostics != null) && (diagnosticsNotAtCaret.size < diagnostics.size)) {
      val thread = Thread(DelayedDiagnosticsPublisherRunnable(languageClient, this))
      thread.start()
    }

    return true
  }

  private fun checkAndGetDiagnostics(range: Range?, useCache: Boolean): List<Diagnostic> {
    if (useCache) {
      val diagnostics: List<Diagnostic>? = this.diagnosticsCache
      if (diagnostics != null) return diagnostics.toList()
    }

    val checkingResult: Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>> =
        check(range, useCache)

    val matches: List<LanguageToolRuleMatch> = checkingResult.first
    val diagnostics = ArrayList<Diagnostic>()

    for (match: LanguageToolRuleMatch in matches) {
      diagnostics.add(this.languageServer.codeActionProvider.createDiagnostic(match, this))
    }

    this.diagnosticsCache = diagnostics
    return diagnostics
  }

  private fun extractDiagnosticsNotAtCaret(): List<Diagnostic>? {
    val diagnosticsCache: List<Diagnostic> = this.diagnosticsCache ?: return null
    val caretPosition: Position = this.caretPosition ?: return diagnosticsCache.toList()
    val diagnosticsNotAtCaret = ArrayList<Diagnostic>()
    val character: Int = caretPosition.character
    val beforeCaretPosition = Position(
      caretPosition.line,
      (if (character >= 1) (character - 1) else 0),
    )
    val caretRange = Range(beforeCaretPosition, this.caretPosition)

    for (diagnostic: Diagnostic in diagnosticsCache) {
      if (!Tools.areRangesIntersecting(diagnostic.range, caretRange)) {
        diagnosticsNotAtCaret.add(diagnostic)
      }
    }

    return diagnosticsNotAtCaret
  }

  fun checkWithCache(
    range: Range? = null,
  ): Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>> {
    return check(range, true)
  }

  @Suppress("unused")
  fun checkWithoutCache(
    range: Range? = null,
  ): Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>> {
    return check(range, false)
  }

  @Suppress("LongMethod")
  private fun check(
    range: Range?,
    useCache: Boolean,
  ): Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>> {
    if (useCache) {
      val checkingResult: Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>>? =
          this.checkingResult

      if (checkingResult != null) {
        return Pair(checkingResult.first.toList(), checkingResult.second.toList())
      }
    }

    val languageClient: LtexLanguageClient = this.languageServer.languageClient ?:
        return Pair(emptyList(), emptyList())
    var progressToken: Either<String, Int>? = null

    try {
      this.raiseExceptionIfCanceled()
      this.beingChecked = true
      val uri: String = uri

      if (this.languageServer.clientSupportsWorkDoneProgress) {
        val progressJsonToken = JsonObject()
        progressJsonToken.addProperty("uri", uri)
        progressJsonToken.addProperty("operation", "checkDocument")
        progressJsonToken.addProperty("uuid", Tools.getRandomUuid())
        progressToken = Either.forLeft(progressJsonToken.toString())

        languageClient.createProgress(WorkDoneProgressCreateParams(progressToken)).get()

        this.raiseExceptionIfCanceled()
        val workDoneProgressBegin = WorkDoneProgressBegin()
        workDoneProgressBegin.title = I18n.format("checkingDocument")
        workDoneProgressBegin.message = uri
        workDoneProgressBegin.cancellable = false
        languageClient.notifyProgress(
          ProgressParams(progressToken, Either.forLeft(workDoneProgressBegin)),
        )
      }

      val configurationItem = ConfigurationItem()
      configurationItem.scopeUri = uri
      configurationItem.section = "ltex"
      val configurationParams = ConfigurationParams(listOf(configurationItem))

      this.raiseExceptionIfCanceled()
      val configurationResult: List<Any> =
          languageClient.configuration(configurationParams).get()

      this.raiseExceptionIfCanceled()
      val workspaceSpecificConfigurationResult: List<Any?> =
      if (this.languageServer.clientSupportsWorkspaceSpecificConfiguration) {
        languageClient.ltexWorkspaceSpecificConfiguration(configurationParams).get()
      } else {
        listOf(null)
      }

      this.raiseExceptionIfCanceled()
      val jsonConfiguration: JsonElement = configurationResult[0] as JsonElement

      val workspaceSpecificConfiguration: Any? = workspaceSpecificConfigurationResult[0]
      val jsonWorkspaceSpecificConfiguration: JsonElement? =
          workspaceSpecificConfiguration as JsonElement?

      this.raiseExceptionIfCanceled()
      this.languageServer.settingsManager.settings =
          Settings.fromJson(jsonConfiguration, jsonWorkspaceSpecificConfiguration)

      this.raiseExceptionIfCanceled()
      val checkingResult: Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>> =
          this.languageServer.documentChecker.check(this, range)
      this.checkingResult = checkingResult

      return checkingResult
    } finally {
      if (progressToken != null) {
        languageClient.notifyProgress(
          ProgressParams(progressToken, Either.forLeft(WorkDoneProgressEnd())),
        )
      }

      this.beingChecked = false
    }
  }

  fun raiseExceptionIfCanceled() {
    val lspCancelChecker: CancelChecker? = this.lspCancelChecker

    if ((lspCancelChecker != null) && lspCancelChecker.isCanceled) {
      this.lspCancelChecker = null
      Logging.logger.fine(I18n.format("cancelingCheckDueToLspCancelNotification"))
    } else if (this.cancellationCounter > 0) {
      this.cancellationCounter--
      Logging.logger.fine(I18n.format("cancelingCheckDueToIncomingCheckRequest"))
    } else {
      return
    }

    this.beingChecked = false
    throw CancellationException()
  }

  fun cancelCheck() {
    this.cancellationCounter++
  }
}
