/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.latex

import org.apache.commons.text.StringEscapeUtils
import org.bsplines.ltexls.parsing.CodeAnnotatedTextBuilder
import org.bsplines.ltexls.parsing.DummyGenerator
import org.bsplines.ltexls.settings.Settings
import org.bsplines.ltexls.tools.ExcludeFromGeneratedCoverage
import org.bsplines.ltexls.tools.I18n
import org.bsplines.ltexls.tools.Logging
import java.text.Normalizer

@Suppress("LargeClass", "TooManyFunctions")
class LatexAnnotatedTextBuilder(codeLanguageId: String) : CodeAnnotatedTextBuilder(codeLanguageId) {
  private var code = ""
  private var pos = 0
  private var dummyCounter = 0
  private var lastSpace = ""
  private var lastPunctuation = ""
  private var dummyLastSpace = ""
  private var dummyLastPunctuation = ""
  private var isMathEmpty = false
  private var mathVowelState = MathVowelState.Undecided
  private var preserveDummyLast = false
  private var canInsertSpaceBeforeDummy = false
  private var isMathCharTrivial = false
  private var ignoreEnvironmentEndRegex: Regex? = null
  private var modeStack = ArrayDeque(listOf(Mode.ParagraphText))
  private var curChar = '\u0000'
  private var curString = ""
  private var curMode: Mode = Mode.ParagraphText

  private var language = "en-US"

  private val commandSignatures: MutableList<LatexCommandSignature> =
      ArrayList(LatexAnnotatedTextBuilderDefaults.defaultLatexCommandSignatures)
  private var commandSignatureMap: Map<String, List<LatexCommandSignature>> =
      createCommandSignatureMap(commandSignatures)

  private val environmentSignatures: MutableList<LatexEnvironmentSignature> =
      ArrayList(LatexAnnotatedTextBuilderDefaults.defaultLatexEnvironmentSignatures)
  private var environmentSignatureMap: Map<String, List<LatexEnvironmentSignature>> =
      createCommandSignatureMap(environmentSignatures)

  var isInStrictMode = false

  private fun reinitialize() {
    this.code = ""
    this.pos = 0
    this.dummyCounter = 0
    this.lastSpace = ""
    this.lastPunctuation = ""
    this.dummyLastSpace = ""
    this.dummyLastPunctuation = ""
    this.isMathEmpty = true
    this.mathVowelState = MathVowelState.Undecided
    this.preserveDummyLast = false
    this.canInsertSpaceBeforeDummy = false
    this.isMathCharTrivial = false
    this.ignoreEnvironmentEndRegex = null
    this.modeStack.clear()
    this.modeStack.addLast(Mode.ParagraphText)
    this.curChar = ' '
    this.curString = ""
    this.curMode = Mode.ParagraphText
  }

  override fun setSettings(settings: Settings) {
    super.setSettings(settings)
    this.language = settings.languageShortCode

    this.commandSignatures.clear()
    this.commandSignatures.addAll(LatexAnnotatedTextBuilderDefaults.defaultLatexCommandSignatures)

    for ((key: String, actionString: String) in settings.latexCommands) {
      var action: LatexCommandSignature.Action
      var dummyGenerator: DummyGenerator = DummyGenerator.getInstance()

      if (actionString == "default") {
        action = LatexCommandSignature.Action.Default
      } else if (actionString == "ignore") {
        action = LatexCommandSignature.Action.Ignore
      } else if (actionString == "dummy") {
        action = LatexCommandSignature.Action.Dummy
      } else if (actionString == "pluralDummy") {
        action = LatexCommandSignature.Action.Dummy
        dummyGenerator = DummyGenerator.getInstance(plural = true)
      } else {
        continue
      }

      this.commandSignatures.add(LatexCommandSignature(key, action, dummyGenerator))
    }

    this.commandSignatureMap = createCommandSignatureMap(this.commandSignatures)

    this.environmentSignatures.clear()
    this.environmentSignatures.addAll(
      LatexAnnotatedTextBuilderDefaults.defaultLatexEnvironmentSignatures
    )

    for ((key, actionString) in settings.latexEnvironments) {
      val action: LatexCommandSignature.Action = if (actionString == "default") {
        LatexCommandSignature.Action.Default
      } else if (actionString == "ignore") {
        LatexCommandSignature.Action.Ignore
      } else {
        continue
      }

      this.environmentSignatures.add(LatexEnvironmentSignature(key, action))
    }

    this.environmentSignatureMap = createCommandSignatureMap(this.environmentSignatures)
  }

  override fun addText(text: String): LatexAnnotatedTextBuilder {
    if (text.isEmpty()) return this
    super.addText(text)
    this.pos += text.length
    textAdded(text)
    return this
  }

  override fun addMarkup(markup: String): LatexAnnotatedTextBuilder {
    if (markup.isEmpty()) return this
    super.addMarkup(markup)
    this.pos += markup.length

    if (this.preserveDummyLast) {
      this.preserveDummyLast = false
    } else {
      this.dummyLastSpace = ""
      this.dummyLastPunctuation = ""
    }

    return this
  }

  override fun addMarkup(markup: String, interpretAs: String): LatexAnnotatedTextBuilder {
    if (interpretAs.isEmpty()) return addMarkup(markup)
    super.addMarkup(markup, interpretAs)
    this.pos += markup.length
    this.preserveDummyLast = false
    textAdded(interpretAs)
    return this
  }

  @Suppress("ComplexMethod")
  override fun addCode(code: String): LatexAnnotatedTextBuilder {
    reinitialize()
    this.code = code
    var lastPos: Int

    while (this.pos < this.code.length) {
      this.curChar = this.code[this.pos]
      this.curString = this.curChar.toString()
      this.curMode = this.modeStack.last()
      this.isMathCharTrivial = false
      lastPos = this.pos

      if (isIgnoreEnvironmentMode(this.curMode)) {
        processIgnoredEnvironmentContents()
      } else if ((this.codeLanguageId == "rsweave") && isRsweaveMode(this.curMode)) {
        val rsweaveEnd: String = matchFromPosition(RSWEAVE_END_REGEX)

        if (rsweaveEnd.isNotEmpty()) {
          popMode()
          addMarkup(rsweaveEnd)
        } else {
          addMarkup(this.curString)
        }
      } else {
        when (this.curChar) {
          '\\' -> processBackslash()
          '{' -> processOpeningBrace()
          '}' -> processClosingBrace()
          '$' -> processDollar()
          '%' -> processPercentage()
          ' ', '&', '~', '\n', '\r', '\t' -> processWhitespace()
          '`', '\'', '"' -> processQuotationMark()
          else -> processDefaultCharacter()
        }
      }

      if (!this.isMathCharTrivial) {
        this.canInsertSpaceBeforeDummy = false
        this.isMathEmpty = false
      }

      if (this.pos == lastPos) onInfiniteLoop()
    }

    return this
  }

  private fun processIgnoredEnvironmentContents() {
    val ignoreEnvironmentEndRegex: Regex? = this.ignoreEnvironmentEndRegex

    if (ignoreEnvironmentEndRegex != null) {
      val ignoreEnvironmentEnd: String = matchFromPosition(ignoreEnvironmentEndRegex)

      if (ignoreEnvironmentEnd.isNotEmpty()) {
        popMode()
        addMarkup(ignoreEnvironmentEnd)
      } else {
        addMarkup(this.curString)
      }
    } else {
      Logging.logger.warning(I18n.format("ignoreEnvironmentEndPatternNotSet"))
      popMode()
    }
  }

  @Suppress("ComplexCondition", "ComplexMethod", "LongMethod", "NestedBlockDepth")
  private fun processBackslash() {
    var command = matchFromPosition(COMMAND_REGEX)

    if ((command == "\\begin") || (command == "\\end")) {
      this.preserveDummyLast = true
      val argument: String = matchFromPosition(ARGUMENT_REGEX, this.pos + command.length)
      val environmentName: String =
        if (argument.length >= 2) argument.substring(1, argument.length - 1) else ""
      var argumentsProcessed = false
      var interpretAs = ""

      if (MATH_ENVIRONMENTS.contains(environmentName)) {
        addMarkup(command)

        if (command == "\\begin") {
          if (environmentName == "math") {
            enterInlineMath()
          } else {
            enterDisplayMath()
          }
        } else {
          popMode()
          interpretAs = generateDummy()
        }
      } else if (command == "\\begin") {
        val possibleEnvironmentSignatures: List<LatexEnvironmentSignature> =
            this.environmentSignatureMap[command + argument] ?: emptyList()

        var match = ""
        var matchingEnvironmentSignature: LatexEnvironmentSignature? = null

        for (latexEnvironmentSignature in possibleEnvironmentSignatures) {
          val curMatch: String = latexEnvironmentSignature.matchFromPosition(this.code, this.pos)

          if (
            curMatch.isNotEmpty()
            && ((curMatch.length >= match.length) || latexEnvironmentSignature.ignoreAllArguments)
          ) {
            match = curMatch
            matchingEnvironmentSignature = latexEnvironmentSignature
          }
        }

        if (matchingEnvironmentSignature != null) {
          if (matchingEnvironmentSignature.action == LatexCommandSignature.Action.Ignore) {
            this.modeStack.add(Mode.IgnoreEnvironment)
            this.ignoreEnvironmentEndRegex =
                Regex("^\\\\end\\{" + Regex.escape(environmentName) + "}")
          }

          if (matchingEnvironmentSignature.ignoreAllArguments) {
            addMarkup(command)
          } else {
            addMarkup(match)
            argumentsProcessed = true
          }
        } else {
          addMarkup(command)
          this.modeStack.addLast(this.curMode)
        }
      } else {
        addMarkup(command)
        popMode()
      }

      if (!isIgnoreEnvironmentMode(this.modeStack.lastOrNull())) {
        this.isMathCharTrivial = true
        this.preserveDummyLast = true

        if (!argumentsProcessed) {
          addMarkup(argument, interpretAs)
          if (command == "\\begin") processEnvironmentArguments()
        }
      }
    } else if ((command == "\\$") || (command == "\\%") || (command == "\\&")) {
      addMarkup(command, command.substring(1))
    } else if (command == "\\[") {
      enterDisplayMath()
      addMarkup(command)
    } else if (command == "\\(") {
      enterInlineMath()
      addMarkup(command)
    } else if ((command == "\\]") || (command == "\\)")) {
      popMode()
      addMarkup(command, generateDummy())
    } else if (command == "\\AA") {
      // capital A with ring above
      addMarkup(command, if (isMathMode(this.curMode)) "" else "\u00c5")
    } else if (command == "\\L") {
      // capital L with stroke
      addMarkup(command, if (isMathMode(this.curMode)) "" else "\u0141")
    } else if (command == "\\O") {
      // capital O with stroke
      addMarkup(command, if (isMathMode(this.curMode)) "" else "\u00d8")
    } else if (command == "\\SS") {
      // capital sharp S
      addMarkup(command, if (isMathMode(this.curMode)) "" else "\u1e9e")
    } else if (command == "\\aa") {
      // small a with ring above
      addMarkup(command, if (isMathMode(this.curMode)) "" else "\u00e5")
    } else if (command == "\\i") {
      // small i without dot
      addMarkup(command, if (isMathMode(this.curMode)) "" else "\u0131")
    } else if (command == "\\j") {
      // small j without dot
      addMarkup(command, if (isMathMode(this.curMode)) "" else "\u0237")
    } else if (command == "\\l") {
      // small l with stroke
      addMarkup(command, if (isMathMode(this.curMode)) "" else "\u0142")
    } else if (command == "\\o") {
      // small o with stroke
      addMarkup(command, if (isMathMode(this.curMode)) "" else "\u00f8")
    } else if (command == "\\ss") {
      // small sharp s
      addMarkup(command, if (isMathMode(this.curMode)) "" else "\u00df")
    } else if (
      (command == "\\`")
      || (command == "\\'")
      || (command == "\\^")
      || (command == "\\~")
      || (command == "\\\"")
      || (command == "\\=")
      || (command == "\\.")
      || (command == "\\H")
      || (command == "\\b")
      || (command == "\\c")
      || (command == "\\d")
      || (command == "\\k")
      || (command == "\\r")
      || (command == "\\u")
      || (command == "\\v")
    ) {
      if (!isMathMode(this.curMode)) {
        val matchResult: MatchResult? = (
          ACCENT_REGEX1.find(this.code.substring(this.pos))
          ?: ACCENT_REGEX2.find(this.code.substring(this.pos))
        )

        if (matchResult != null) {
          val accentCommand: String? = matchResult.groups["accentCommand"]?.value
          val letter: String? = if (matchResult.groups["letter1"] != null) {
            matchResult.groups["letter1"]?.value
          } else {
            matchResult.groups["letter2"]?.value
          }

          val interpretAs: String = if ((accentCommand != null) && (letter != null)) {
            convertAccentCommandToUnicode(accentCommand, letter)
          } else {
            ""
          }

          addMarkup(matchResult.value, interpretAs)
        } else {
          addMarkup(command)
        }
      } else {
        addMarkup(command)
      }
    } else if (command == "\\-") {
      addMarkup(command)
    } else if (
      (command == "\\ ")
      || (command == "\\,")
      || (command == "\\;")
      || (command == "\\\\")
      || (command == "\\hfill")
      || (command == "\\hspace")
      || (command == "\\hspace*")
      || (command == "\\quad")
      || (command == "\\qquad")
      || (command == "\\newline")
    ) {
      if ((command == "\\hspace") || (command == "\\hspace*")) {
        val argument: String = matchFromPosition(ARGUMENT_REGEX, this.pos + command.length)
        command += argument
      }

      if (isMathMode(this.curMode) && this.lastSpace.isEmpty() && this.canInsertSpaceBeforeDummy) {
        addMarkup(command, " ")
      } else {
        this.preserveDummyLast = true

        if (isMathMode(this.curMode)) {
          addMarkup(command)
          this.dummyLastSpace = " "
        } else {
          var space = " "

          if (this.lastSpace.isNotEmpty()) {
            space = ""
          } else if (command == "\\,") {
            space = "\u202f"
          }

          addMarkup(command, space)
        }
      }
    } else if (
      (command == "\\dots")
      || (command == "\\eg")
      || (command == "\\egc")
      || (command == "\\euro")
      || (command == "\\ie")
      || (command == "\\iec")
    ) {
      val interpretAs: String = if (!isMathMode(this.curMode)) {
        when (command) {
          "\\dots" -> "\u2026"
          "\\eg" -> "e.g."
          "\\egc" -> "e.g.,"
          "\\euro" -> "\u20ac"
          "\\ie" -> "i.e."
          "\\iec" -> "i.e.,"
          else -> ""
        }
      } else {
        ""
      }

      addMarkup(command, interpretAs)
    } else if ((command == "\\notag") || (command == "\\qed")) {
      this.preserveDummyLast = true
      addMarkup(command)
    } else if (
      (command == "\\part")
      || (command == "\\chapter")
      || (command == "\\section")
      || (command == "\\subsection")
      || (command == "\\subsubsection")
      || (command == "\\paragraph")
      || (command == "\\subparagraph")
      || (command == "\\part*")
      || (command == "\\chapter*")
      || (command == "\\section*")
      || (command == "\\subsection*")
      || (command == "\\subsubsection*")
      || (command == "\\paragraph*")
      || (command == "\\subparagraph*")
    ) {
      addMarkup(command)

      val headingArgument: String = LatexCommandSignature.matchArgumentFromPosition(
        this.code, this.pos, LatexCommandSignature.ArgumentType.Bracket
      )

      if (headingArgument.isNotEmpty()) addMarkup(headingArgument)
      this.modeStack.addLast(Mode.Heading)
      addMarkup("{")
    } else if ((command == "\\text") || (command == "\\intertext")) {
      this.modeStack.addLast(Mode.InlineText)
      val interpretAs: String = if (isMathMode(this.curMode)) generateDummy() else ""
      addMarkup("$command{", interpretAs)
    } else if (command == "\\verb") {
      val verbCommand: String = matchFromPosition(VERB_COMMAND_REGEX)
      addMarkup(verbCommand, generateDummy())
    } else {
      val possibleCommandSignatures: List<LatexCommandSignature> =
          this.commandSignatureMap[command] ?: emptyList()
      var match = ""
      var matchingCommandSignature: LatexCommandSignature? = null

      for (latexCommandSignature in possibleCommandSignatures) {
        val curMatch: String = latexCommandSignature.matchFromPosition(this.code, this.pos)

        if (curMatch.isNotEmpty() && (curMatch.length >= match.length)) {
          match = curMatch
          matchingCommandSignature = latexCommandSignature
        }
      }

      if (
        (matchingCommandSignature != null)
        && (matchingCommandSignature.action != LatexCommandSignature.Action.Default)
      ) {
        when (matchingCommandSignature.action) {
          LatexCommandSignature.Action.Ignore -> {
            addMarkup(match)
          }
          LatexCommandSignature.Action.Dummy -> {
            addMarkup(match, generateDummy(matchingCommandSignature.dummyGenerator))
          }
          else -> {
            addMarkup(match)
          }
        }
      } else {
        if (isMathMode(curMode) && (this.mathVowelState == MathVowelState.Undecided)) {
          this.mathVowelState = when (command) {
            "\\bm",
            "\\boldsymbol",
            "\\hat",
            "\\mathbb",
            "\\mathbf",
            "\\mathcal",
            "\\mathfrak",
            "\\mathit",
            "\\mathnormal",
            "\\mathsf",
            "\\mathtt",
            "\\mathop",
            "\\operatorname",
            "\\overbrace",
            "\\overleftarrow",
            "\\overleftrightarrow",
            "\\overline",
            "\\overrightarrow",
            "\\tilde",
            "\\underbrace",
            "\\underline",
            "\\vec",
            "\\widetilde",
            "\\widehat" -> this.mathVowelState
            "\\alpha",
            "\\ell",
            "\\epsilon",
            "\\eta",
            "\\iota",
            "\\Omega",
            "\\omega",
            "\\varepsilon" -> MathVowelState.StartsWithVowel
            else -> MathVowelState.StartsWithConsonant
          }
        }
        addMarkup(command)
      }
    }
  }

  private fun processOpeningBrace() {
    val length: String = matchFromPosition(LENGTH_IN_BRACE_REGEX)

    if (length.isNotEmpty()) {
      addMarkup(length)
    } else {
      this.modeStack.addLast(this.curMode)
      addMarkup(this.curString)
    }
  }

  private fun processClosingBrace() {
    val interpretAs: String = if (
      (this.curMode == Mode.Heading)
      && this.lastPunctuation.isEmpty()
    ) {
      "."
    } else if (
      isTextMode(this.curMode)
      && (this.pos + 1 < this.code.length)
      && (this.code[this.pos + 1] == '{')
    ) {
      " "
    } else {
      ""
    }

    popMode()
    addMarkup(this.curString, interpretAs)
    this.canInsertSpaceBeforeDummy = true

    if (isTextMode(curMode) && isMathMode(this.modeStack.lastOrNull())) {
      this.isMathEmpty = true
    }

    this.isMathCharTrivial = true
  }

  private fun processDollar() {
    val displayMath: String = matchFromPosition(DISPLAY_MATH_REGEX)

    if (displayMath.isNotEmpty()) {
      if (this.curMode == Mode.DisplayMath) {
        popMode()
        addMarkup(displayMath, generateDummy())
      } else {
        enterDisplayMath()
        addMarkup(displayMath)
      }
    } else {
      if (this.curMode == Mode.InlineMath) {
        popMode()
        addMarkup(this.curString, generateDummy())
      } else {
        enterInlineMath()
        addMarkup(this.curString)
      }
    }
  }

  private fun processPercentage() {
    val comment: String = matchFromPosition(COMMENT_REGEX)
    this.preserveDummyLast = true
    this.isMathCharTrivial = true
    addMarkup(comment, (if (containsTwoEndsOfLine(comment)) "\n\n" else ""))
  }

  private fun processWhitespace() {
    val whitespace: String = if ((this.curChar != '~') && (this.curChar != '&')) {
      matchFromPosition(WHITESPACE_REGEX)
    } else {
      this.curString
    }

    this.preserveDummyLast = true
    this.isMathCharTrivial = true

    if (isTextMode(this.curMode)) {
      when {
        containsTwoEndsOfLine(whitespace) -> {
          addMarkup(whitespace, "\n\n")
        }
        this.curChar == '~' -> {
          addMarkup(whitespace, (if (this.lastSpace.isEmpty()) "\u00a0" else ""))
        }
        else -> {
          addMarkup(whitespace, (if (this.lastSpace.isEmpty()) " " else ""))
        }
      }
    } else {
      addMarkup(whitespace)
    }

    if ((this.curChar == '~') || (this.curChar == '&')) {
      this.dummyLastSpace = " "
    }
  }

  private fun processQuotationMark() {
    if (isTextMode(this.curMode)) {

      var quote = ""
      var smartQuote = ""

      if (this.pos + 1 < this.code.length) {
        quote = this.code.substring(this.pos, this.pos + 2)

        when (quote) {
          "``", "\"'" -> smartQuote = "\u201c"
          "''" -> smartQuote = "\u201d"
          "\"`" -> smartQuote = "\u201e"
          "\"-", "\"\"", "\"|" -> smartQuote = ""
          "\"=", "\"~" -> smartQuote = "-"
          else -> quote = ""
        }
      }

      if (quote.isEmpty()) addText(this.curString) else addMarkup(quote, smartQuote)
    } else {
      addMarkup(this.curString)
    }
  }

  @Suppress("ComplexMethod", "NestedBlockDepth")
  private fun processDefaultCharacter() {
    when (this.curChar) {
      '-' -> {
        val emDash: String = matchFromPosition(EM_DASH_REGEX)

        if (isTextMode(this.curMode)) {
          if (emDash.isNotEmpty()) {
            addMarkup(emDash, "\u2014")
            return
          } else {
            val enDash: String = matchFromPosition(EN_DASH_REGEX)

            if (enDash.isNotEmpty()) {
              addMarkup(enDash, "\u2013")
              return
            }
          }
        }
      }
      '[' -> {
        val length: String = matchFromPosition(LENGTH_IN_BRACKET_REGEX)

        if (length.isNotEmpty()) {
          this.isMathCharTrivial = true
          this.preserveDummyLast = true
          addMarkup(length)
          return
        }
      }
      '<' -> {
        if (this.codeLanguageId == "rsweave") {
          val rsweaveBegin: String = matchFromPosition(RSWEAVE_BEGIN_REGEX)

          if (rsweaveBegin.isNotEmpty()) {
            this.modeStack.addLast(Mode.Rsweave)
            addMarkup(rsweaveBegin)
            return
          }
        }
      }
    }

    if (isTextMode(this.curMode)) {
      addText(this.curString)
      if (isPunctuation(this.curChar)) this.lastPunctuation = this.curString
    } else {
      addMarkup(this.curString)
      if (isPunctuation(this.curChar)) this.dummyLastPunctuation = this.curString

      if (this.mathVowelState == MathVowelState.Undecided) {
        this.mathVowelState = if (isVowel(this.curChar)) {
          MathVowelState.StartsWithVowel
        } else {
          MathVowelState.StartsWithConsonant
        }
      }
    }
  }

  private fun matchFromPosition(regex: Regex, pos: Int = this.pos): String {
    return regex.find(this.code.substring(pos))?.value ?: ""
  }

  private fun generateDummy(dummyGenerator: DummyGenerator = DummyGenerator.getInstance()): String {
    val startsWithVowel: Boolean = (this.mathVowelState == MathVowelState.StartsWithVowel)

    val dummy: String = if (isTextMode(this.curMode)) {
      dummyGenerator.generate(this.language, this.dummyCounter++, startsWithVowel)
    } else if (this.isMathEmpty) {
      if (this.curMode == Mode.DisplayMath) {
        if (this.lastSpace.isEmpty()) " " else ""
      } else {
        ""
      }
    } else if (this.curMode == Mode.DisplayMath) {
      (
        (if (this.lastSpace.isEmpty()) " " else "")
        + dummyGenerator.generate(this.language, this.dummyCounter++)
        + this.dummyLastPunctuation
        + (if (this.modeStack.lastOrNull() == Mode.InlineText) this.dummyLastSpace else " ")
      )
    } else {
      (dummyGenerator.generate(language, dummyCounter++, startsWithVowel)
          + this.dummyLastPunctuation + this.dummyLastSpace)
    }

    this.dummyLastSpace = ""
    this.dummyLastPunctuation = ""
    this.mathVowelState = MathVowelState.Undecided
    return dummy
  }

  private fun textAdded(text: String) {
    if (text.isEmpty()) return
    val lastChar: Char = text[text.length - 1]
    this.lastSpace = when (lastChar) {
      ' ', '\n', '\r' -> " "
      else -> ""
    }
    this.lastPunctuation = (if (isPunctuation(lastChar)) " " else "")
  }

  private fun popMode() {
    this.modeStack.removeLastOrNull()
    if (this.modeStack.isEmpty()) this.modeStack.addLast(Mode.ParagraphText)
  }

  private fun enterDisplayMath() {
    this.modeStack.addLast(Mode.DisplayMath)
    this.isMathEmpty = true
    this.mathVowelState = MathVowelState.Undecided
    this.canInsertSpaceBeforeDummy = true
  }

  private fun enterInlineMath() {
    this.modeStack.addLast(Mode.InlineMath)
    this.isMathEmpty = true
    this.mathVowelState = MathVowelState.Undecided
    this.canInsertSpaceBeforeDummy = true
    this.isMathCharTrivial = true
  }

  private fun onInfiniteLoop() {
    if (this.isInStrictMode) {
      throw IllegalStateException(
        I18n.format("latexAnnotatedTextBuilderInfiniteLoop", getDebugInformation())
      )
    } else {
      Logging.logger.warning(
        I18n.format("latexAnnotatedTextBuilderPreventedInfiniteLoop", getDebugInformation())
      )
      this.pos++
    }
  }

  @ExcludeFromGeneratedCoverage
  private fun getDebugInformation(): String {
    val remainingCode: String = StringEscapeUtils.escapeJava(
      this.code.substring(
        this.pos,
        (this.pos + DEBUG_INFORMATION_REMAINING_CODE_LENGTH).coerceAtMost(this.code.length),
      )
    )

    return (
      "Remaining code = \"" + remainingCode
      + "\", pos = " + this.pos
      + ", dummyCounter = " + this.dummyCounter
      + ", lastSpace = \"" + this.lastSpace
      + "\", lastPunctuation = \"" + this.lastPunctuation
      + "\", dummyLastSpace = \"" + this.dummyLastSpace
      + "\", dummyLastPunctuation = \"" + this.dummyLastPunctuation
      + "\", isMathEmpty = " + this.isMathEmpty
      + ", mathVowelState = " + this.mathVowelState
      + ", preserveDummyLast = " + this.preserveDummyLast
      + ", canInsertSpaceBeforeDummy = " + this.canInsertSpaceBeforeDummy
      + ", isMathCharTrivial = " + this.isMathCharTrivial
      + ", modeStack = " + this.modeStack
      + ", curChar = \"" + this.curChar
      + "\", curString = \"" + this.curString
      + "\", curMode = " + this.curMode
    )
  }

  @Suppress("ComplexMethod")
  private fun convertAccentCommandToUnicode(accentCommand: String, letter: String): String {
    var unicode: String = when (letter) {
      "\\i" -> "\u0131"
      "\\j" -> "\u0237"
      else -> letter
    }

    unicode += when (accentCommand[1]) {
      // grave
      '`' -> "\u0300"
      // acute
      '\'' -> "\u0301"
      // circumflex
      '^' -> "\u0302"
      // tilde
      '~' -> "\u0303"
      // diaeresis/umlaut
      '"' -> "\u0308"
      // macron
      '=' -> "\u0304"
      // dot above
      '.' -> "\u0307"
      // double acute
      'H' -> "\u030b"
      // macron below
      'b' -> "\u0331"
      // cedilla
      'c' -> "\u0327"
      // dot below
      'd' -> "\u0323"
      // ogonek
      'k' -> "\u0328"
      // ring above
      'r' -> "\u030a"
      // breve
      'u' -> "\u0306"
      // caron
      'v' -> "\u030c"
      else -> ""
    }

    unicode = Normalizer.normalize(unicode, Normalizer.Form.NFC)
    return unicode
  }

  @Suppress("LoopWithTooManyJumpStatements")
  private fun processEnvironmentArguments() {
    while (this.pos < this.code.length) {
      var environmentArgument: String = LatexCommandSignature.matchArgumentFromPosition(
        this.code, this.pos, LatexCommandSignature.ArgumentType.Brace
      )

      if (environmentArgument.isNotEmpty()) {
        addMarkup(environmentArgument)
        continue
      }

      environmentArgument = LatexCommandSignature.matchArgumentFromPosition(
        this.code, this.pos, LatexCommandSignature.ArgumentType.Bracket
      )

      if (environmentArgument.isNotEmpty()) {
        addMarkup(environmentArgument)
        continue
      }

      environmentArgument = LatexCommandSignature.matchArgumentFromPosition(
        this.code, this.pos, LatexCommandSignature.ArgumentType.Parenthesis
      )

      if (environmentArgument.isNotEmpty()) {
        addMarkup(environmentArgument)
        continue
      }

      break
    }
  }

  private enum class MathVowelState {
    Undecided,
    StartsWithVowel,
    StartsWithConsonant,
  }

  private enum class Mode {
    ParagraphText,
    InlineText,
    Heading,
    InlineMath,
    DisplayMath,
    IgnoreEnvironment,
    Rsweave,
  }

  companion object {
    private val COMMAND_REGEX = Regex("^\\\\(([^A-Za-z@]|([A-Za-z@]+))\\*?)")
    private val ARGUMENT_REGEX = Regex("^\\{[^}]*?}")
    private val COMMENT_REGEX = Regex("^%.*?($|(\r?\n[ \n\r\t]*))")
    private val WHITESPACE_REGEX = Regex("^[ \n\r\t]+(%.*?($|(\r?\n[ \n\r\t]*)))?")
    private val LENGTH_REGEX = Regex("-?[0-9]*(\\.[0-9]+)?(pt|mm|cm|ex|em|bp|dd|pc|in)")
    private val LENGTH_IN_BRACE_REGEX = Regex("^\\{" + LENGTH_REGEX.pattern + "}")
    private val LENGTH_IN_BRACKET_REGEX = Regex("^\\[" + LENGTH_REGEX.pattern + "]")
    private val EM_DASH_REGEX = Regex("^---")
    private val EN_DASH_REGEX = Regex("^--")
    private val ACCENT_REGEX1 = Regex(
      "^(?<accentCommand>\\\\[`'^~\"=.])"
      + "(?:(?<letter1>[A-Za-z]|\\\\i|\\\\j)|\\{(?<letter2>[A-Za-z]|\\\\i|\\\\j)})"
    )
    private val ACCENT_REGEX2 = Regex(
      "^(?<accentCommand>\\\\[Hbcdkruv])"
      + "(?: *(?<letter1>[A-Za-z]|\\\\i|\\\\j)|\\{(?<letter2>[A-Za-z]|\\\\i|\\\\j)})"
    )
    private val DISPLAY_MATH_REGEX = Regex("^\\$\\$")
    private val VERB_COMMAND_REGEX = Regex("^\\\\verb\\*?(.).*?\\1")
    private val RSWEAVE_BEGIN_REGEX = Regex("^<<.*?>>=")
    private val RSWEAVE_END_REGEX = Regex("^@")

    private val MATH_ENVIRONMENTS: List<String> = listOf(
      "align",
      "align*",
      "alignat",
      "alignat*",
      "displaymath",
      "eqnarray",
      "eqnarray*",
      "equation",
      "equation*",
      "flalign",
      "flalign*",
      "gather",
      "gather*",
      "math",
      "multline",
      "multline*",
    )

    private const val DEBUG_INFORMATION_REMAINING_CODE_LENGTH = 100

    private fun <T : LatexCommandSignature> createCommandSignatureMap(
      commandSignatures: List<T>
    ): Map<String, List<T>> {
      val map = HashMap<String, ArrayList<T>>()

      for (commandSignature in commandSignatures) {
        val commandPrefix: String = commandSignature.prefix
        val list: MutableList<T> = map[commandPrefix] ?: run {
          val list = ArrayList<T>()
          map[commandPrefix] = list
          list
        }

        list.add(commandSignature)
      }

      return map
    }

    private fun isPunctuation(ch: Char): Boolean {
      return when (ch) {
        '.', ',', ':', ';', '\u2026' -> true
        else -> false
      }
    }

    private fun isVowel(ch: Char): Boolean {
      return when (Character.toLowerCase(ch)) {
        'a', 'e', 'f', 'h', 'i', 'l', 'm', 'n', 'o', 'r', 's', 'x' -> true
        else -> false
      }
    }

    private fun isMathMode(mode: Mode?): Boolean {
      return ((mode == Mode.InlineMath) || (mode == Mode.DisplayMath))
    }

    private fun isIgnoreEnvironmentMode(mode: Mode?): Boolean {
      return (mode == Mode.IgnoreEnvironment)
    }

    private fun isRsweaveMode(mode: Mode?): Boolean {
      return (mode == Mode.Rsweave)
    }

    private fun isTextMode(mode: Mode?): Boolean {
      return (!isMathMode(mode) && !isIgnoreEnvironmentMode(mode))
    }

    private fun containsTwoEndsOfLine(text: String): Boolean {
      return (text.contains("\n\n") || text.contains("\r\n\r\n"))
    }
  }
}
