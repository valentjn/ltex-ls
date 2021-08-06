/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.server

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.bsplines.ltexls.languagetool.LanguageToolRuleMatch
import org.bsplines.ltexls.parsing.AnnotatedTextFragment
import org.bsplines.ltexls.settings.Settings
import org.bsplines.ltexls.tools.FileIo
import org.eclipse.lsp4j.Position
import org.fusesource.jansi.Ansi
import org.fusesource.jansi.Ansi.Color
import org.fusesource.jansi.AnsiConsole
import org.languagetool.rules.RuleMatch
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.logging.Level
import java.util.stream.Collectors
import kotlin.math.ceil

class NonServerChecker {
  val languageServer = LtexLanguageServer()

  init {
    this.languageServer.settingsManager.settings =
        this.languageServer.settingsManager.settings.copy(_logLevel = Level.WARNING)
  }

  fun loadSettings(settingsFilePath: Path) {
    val settingsJson: String = FileIo.readFileWithException(settingsFilePath)
    val jsonSettings: JsonObject = JsonParser.parseString(settingsJson).asJsonObject

    if (!jsonSettings.has("ltex-ls")) jsonSettings.add("ltex-ls", JsonObject())

    val ltexLsJsonSettings: JsonObject = jsonSettings.getAsJsonObject("ltex-ls")
    if (!ltexLsJsonSettings.has("logLevel")) ltexLsJsonSettings.addProperty("logLevel", "warning")

    this.languageServer.settingsManager.settings = Settings.fromJson(jsonSettings)
  }

  fun check(paths: List<Path>): Int {
    var numberOfMatches = 0
    for (path: Path in paths) numberOfMatches += check(path)
    return numberOfMatches
  }

  fun check(path: Path): Int {
    val text: String
    val file: File = path.toFile()
    val codeLanguageId: String

    if (path.toString() == "-") {
      val outputStream = ByteArrayOutputStream()
      val buffer = ByteArray(STANDARD_INPUT_BUFFER_SIZE)

      while (true) {
        val length = System.`in`.read(buffer)
        if (length == -1) break
        outputStream.write(buffer, 0, length)
      }

      text = outputStream.toString(StandardCharsets.UTF_8)
      codeLanguageId = "plaintext"
    } else if (file.isDirectory) {
      return checkDirectory(path)
    } else {
      text = FileIo.readFileWithException(file.toPath())
      codeLanguageId = FileIo.getCodeLanguageIdFromPath(path) ?: "plaintext"
    }

    val document = LtexTextDocumentItem(this.languageServer,
        path.toUri().toString(), codeLanguageId, 1, text)

    val checkingResult: Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>> =
        this.languageServer.documentChecker.check(document)

    var terminalWidth: Int = AnsiConsole.getTerminalWidth()
    if (terminalWidth <= 1) terminalWidth = Integer.MAX_VALUE

    for (match: LanguageToolRuleMatch in checkingResult.first) {
      printMatch(path, document, match, terminalWidth)
    }

    return checkingResult.first.size
  }

  private fun checkDirectory(path: Path): Int {
    var numberOfMatches = 0
    val plainTextEnabled: Boolean =
        this.languageServer.settingsManager.settings.enabled.contains("plaintext")

    for (childPath: Path in Files.walk(path).collect(Collectors.toList())) {
      if (childPath.toFile().isFile) {
        val curCodeLanguageId: String? = FileIo.getCodeLanguageIdFromPath(childPath)

        if (
          (curCodeLanguageId != null)
          && ((curCodeLanguageId != "plaintext") || plainTextEnabled)
        ) {
          numberOfMatches += check(childPath)
        }
      }
    }

    return numberOfMatches
  }

  companion object {
    private val TRAILING_WHITESPACE_REGEX = Regex("[ \t\r\n]+$")

    private const val MAX_NUMBER_OF_SUGGESTIONS = 5
    private const val STANDARD_INPUT_BUFFER_SIZE = 1024
    private const val TAB_SIZE = 8

    private fun printMatch(
      path: Path,
      document: LtexTextDocumentItem,
      match: LanguageToolRuleMatch,
      terminalWidth: Int,
    ) {
      val text: String = document.text
      val fromPos: Int = match.fromPos
      val toPos: Int = match.toPos
      val fromPosition: Position = document.convertPosition(fromPos)
      val message: String = match.message.replace(CodeActionGenerator.SUGGESTION_REGEX, "'$1'")

      var color: Color = when (match.type) {
        RuleMatch.Type.UnknownWord -> Color.RED
        RuleMatch.Type.Hint -> Color.BLUE
        RuleMatch.Type.Other -> Color.YELLOW
        else -> Color.BLUE
      }
      var typeString: String = when (match.type) {
        RuleMatch.Type.UnknownWord -> "spelling"
        RuleMatch.Type.Hint -> "style"
        RuleMatch.Type.Other -> "grammar"
        else -> "style"
      }

      if (match.isUnknownWordRule()) {
        color = Color.RED
        typeString = "spelling"
      }

      val ruleId: String = match.ruleId ?: ""

      println(Ansi.ansi().bold().a(path.toString()).a(":")
          .a(fromPosition.line + 1).a(":").a(fromPosition.character + 1).a(": ")
          .fg(color).a(typeString).a(":").reset().bold().a(" ").a(message)
          .a(" [").a(ruleId).a("]").reset())

      val lineStartPos: Int = document.convertPosition(Position(fromPosition.line, 0))
      val lineEndPos: Int = document.convertPosition(Position(fromPosition.line + 1, 0))
      val line: String = text.substring(lineStartPos, lineEndPos)

      println(Ansi.ansi().a(line.substring(0, fromPos - lineStartPos)).bold().fg(color)
          .a(line.substring(fromPos - lineStartPos, toPos - lineStartPos)).reset()
          .a(line.substring(toPos - lineStartPos).replaceFirst(TRAILING_WHITESPACE_REGEX, "")))

      var indentationSize = guessIndentationSize(text, lineStartPos, fromPos, terminalWidth)
      val suggestedReplacements = ArrayList<String>()

      for (i in 0 until Integer.min(match.suggestedReplacements.size, MAX_NUMBER_OF_SUGGESTIONS)) {
        val suggestedReplacement: String = match.suggestedReplacements[i]
        if (indentationSize + suggestedReplacement.length > terminalWidth) indentationSize = 0
        suggestedReplacements.add(suggestedReplacement)
      }

      val indentation: String = " ".repeat(indentationSize)

      for (suggestedReplacement: String in suggestedReplacements) {
        println(Ansi.ansi().a(indentation).fg(Color.GREEN)
            .a(suggestedReplacement).reset())
      }
    }

    private fun guessIndentationSize(
      text: String,
      lineStartPos: Int,
      fromPos: Int,
      terminalWidth: Int,
    ): Int {
      var indentationSize = 0

      for (pos in lineStartPos until fromPos) {
        if (text[pos] == '\t') {
          indentationSize = (ceil((indentationSize + 1.0) / TAB_SIZE) * TAB_SIZE).toInt()
        } else {
          indentationSize++
        }

        if (indentationSize >= terminalWidth) indentationSize = 0
      }

      return indentationSize
    }
  }
}
