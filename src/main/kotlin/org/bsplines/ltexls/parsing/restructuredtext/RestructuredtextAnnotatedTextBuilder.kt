/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.restructuredtext

import org.bsplines.ltexls.parsing.CodeAnnotatedTextBuilder
import org.bsplines.ltexls.parsing.DummyGenerator
import org.bsplines.ltexls.settings.Settings

@Suppress("TooManyFunctions")
class RestructuredtextAnnotatedTextBuilder(
  codeLanguageId: String
) : CodeAnnotatedTextBuilder(codeLanguageId) {
  private var code = ""
  private var pos = 0
  private var curChar = '\u0000'
  private var curString = ""

  private var dummyGenerator = DummyGenerator.getInstance()
  private var dummyCounter = 0
  private var indentation = 0
  private var lastIndentation = 0
  private var isStartOfBlock = false
  private var isStartOfLine = false
  private var blockType = BlockType.Paragraph
  private var inIgnoredMarkup = false

  private var language: String = "en-US"

  init {
    reinitialize()
  }

  private fun reinitialize() {
    this.code = ""
    this.pos = 0
    this.curString = ""
    this.dummyGenerator = DummyGenerator()
    this.dummyCounter = 0
    this.indentation = -1
    this.lastIndentation = -1
    this.isStartOfBlock = false
    this.isStartOfLine = false
    this.blockType = BlockType.Paragraph
    this.inIgnoredMarkup = false
  }

  override fun setSettings(settings: Settings) {
    super.setSettings(settings)
    this.language = settings.languageShortCode
  }

  override fun addText(text: String?): RestructuredtextAnnotatedTextBuilder {
    if ((text != null) && text.isNotEmpty()) {
      super.addText(text)
      this.pos += text.length
    }

    return this
  }

  override fun addMarkup(markup: String?): RestructuredtextAnnotatedTextBuilder {
    if ((markup != null) && markup.isNotEmpty()) {
      super.addMarkup(markup)
      this.pos += markup.length
    }

    return this
  }

  override fun addMarkup(
    markup: String?,
    interpretAs: String?,
  ): RestructuredtextAnnotatedTextBuilder {
    if ((interpretAs != null) && interpretAs.isNotEmpty()) {
      super.addMarkup((markup ?: ""), interpretAs)
      this.pos += (markup?.length ?: 0)
    } else {
      addMarkup(markup)
    }

    return this
  }

  @Suppress("ComplexMethod", "LoopWithTooManyJumpStatements")
  override fun addCode(code: String): CodeAnnotatedTextBuilder {
    reinitialize()
    this.code = code

    while (this.pos < this.code.length) {
      this.curChar = this.code[this.pos]
      this.curString = this.curChar.toString()
      this.isStartOfBlock = false
      this.isStartOfLine = ((this.pos == 0) || this.code[this.pos - 1] == '\n')

      if (this.isStartOfLine) {
        processStartOfBlock()
        processWhitespaceAtStartOfLine()
        if (this.pos >= this.code.length) break
      }

      if (this.isStartOfBlock) {
        this.inIgnoredMarkup = false
        if (isParagraph()) this.blockType = BlockType.Paragraph
      }

      if (this.isStartOfLine && processStartOfLine()) continue

      if (
        (this.blockType == BlockType.Comment)
        || (this.blockType == BlockType.GridTable)
        || (this.blockType == BlockType.SimpleTable)
      ) {
        addMarkup(this.curString)
        continue
      }

      processInlineElement()
    }

    return this
  }

  private fun processStartOfBlock() {
    val blockSeparatorMatchResult: MatchResult? = matchFromPosition(BLOCK_SEPARATOR_REGEX)

    if ((this.pos == 0) || (blockSeparatorMatchResult != null)) {
      this.isStartOfBlock = true
      if (blockSeparatorMatchResult != null) addMarkup(blockSeparatorMatchResult.value, "\n")
    }
  }

  private fun processWhitespaceAtStartOfLine() {
    val whitespace: String = matchFromPosition(WHITESPACE_REGEX)?.value ?: ""
    this.lastIndentation = this.indentation
    this.indentation = whitespace.length
    addMarkup(whitespace)

    if (this.pos < this.code.length) {
      this.curChar = this.code[this.pos]
      this.curString = this.curChar.toString()
    }
  }

  @Suppress("ComplexMethod")
  private fun processStartOfLine(): Boolean {
    var matchResult: MatchResult? = null
    var blockFound = true

    when {
      matchFromPosition(FOOTNOTE_REGEX)?.also { matchResult = it } != null -> {
        this.blockType = BlockType.Footnote
        addMarkup(matchResult?.value)
      }
      matchFromPosition(DIRECTIVE_REGEX)?.also { matchResult = it } != null -> {
        this.blockType = BlockType.Directive
        addMarkup(matchResult?.value)
      }
      matchFromPosition(COMMENT_REGEX)?.also { matchResult = it } != null -> {
        this.blockType = BlockType.Comment
        addMarkup(matchResult?.value)
      }
      matchFromPosition(GRID_TABLE_START_REGEX)?.also { matchResult = it } != null -> {
        this.blockType = BlockType.GridTable
        addMarkup(matchResult?.value)
      }
      matchFromPosition(SIMPLE_TABLE_START_REGEX)?.also { matchResult = it } != null -> {
        this.blockType = BlockType.SimpleTable
        addMarkup(matchResult?.value)
      }
      matchFromPosition(SECTION_TITLE_ADORNMENT_REGEX)?.also { matchResult = it } != null -> {
        addMarkup(matchResult?.value)
      }
      matchFromPosition(LINE_BLOCK_REGEX)?.also { matchResult = it } != null -> {
        addMarkup(matchResult?.value)
      }
      matchFromPosition(BULLET_LIST_REGEX)?.also { matchResult = it } != null -> {
        addMarkup(matchResult?.value)
      }
      matchFromPosition(ENUMERATED_LIST_REGEX)?.also { matchResult = it } != null -> {
        addMarkup(matchResult?.value)
      }
      else -> {
        blockFound = false
      }
    }

    return blockFound
  }

  @Suppress("ComplexMethod", "LongMethod")
  private fun processInlineElement() {
    var matchResult: MatchResult? = null

    when {
      matchInlineStartFromPosition(STRONG_EMPHASIS_REGEX)?.also { matchResult = it } != null -> {
        addMarkup(matchResult?.value)
      }
      matchInlineEndFromPosition(STRONG_EMPHASIS_REGEX)?.also { matchResult = it } != null -> {
        addMarkup(matchResult?.value)
      }
      matchInlineStartFromPosition(EMPHASIS_REGEX)?.also { matchResult = it } != null -> {
        addMarkup(matchResult?.value)
      }
      matchInlineEndFromPosition(EMPHASIS_REGEX)?.also { matchResult = it } != null -> {
        addMarkup(matchResult?.value)
      }
      matchInlineStartFromPosition(INLINE_LITERAL_REGEX)?.also { matchResult = it } != null -> {
        addMarkup(matchResult?.value, generateDummy())
        this.inIgnoredMarkup = true
      }
      matchInlineEndFromPosition(INLINE_LITERAL_REGEX)?.also { matchResult = it } != null -> {
        addMarkup(matchResult?.value)
        this.inIgnoredMarkup = false
      }
      matchInlineStartFromPosition(INTERPRETED_TEXT_START_REGEX)?.also {
        matchResult = it
      } != null -> {
        addMarkup(matchResult?.value, generateDummy())
        this.inIgnoredMarkup = true
      }
      matchInlineEndFromPosition(INTERPRETED_TEXT_END_REGEX)?.also {
        matchResult = it
      } != null -> {
        addMarkup(matchResult?.value)
        this.inIgnoredMarkup = false
      }
      matchInlineStartFromPosition(INLINE_INTERNAL_TARGET_START_REGEX)?.also {
        matchResult = it
      } != null -> {
        addMarkup(matchResult?.value, generateDummy())
        this.inIgnoredMarkup = true
      }
      matchInlineEndFromPosition(INLINE_INTERNAL_TARGET_END_REGEX)?.also {
        matchResult = it
      } != null -> {
        addMarkup(matchResult?.value)
        this.inIgnoredMarkup = false
      }
      matchInlineStartFromPosition(FOOTNOTE_REFERENCE_START_REGEX)?.also {
        matchResult = it
      } != null -> {
        addMarkup(matchResult?.value, generateDummy())
        this.inIgnoredMarkup = true
      }
      matchInlineEndFromPosition(FOOTNOTE_REFERENCE_END_REGEX)?.also {
        matchResult = it
      } != null -> {
        addMarkup(matchResult?.value)
        this.inIgnoredMarkup = false
      }
      matchInlineStartFromPosition(HYPERLINK_REFERENCE_START_REGEX)?.also {
        matchResult = it
      } != null -> {
        addMarkup(matchResult?.value, generateDummy())
        this.inIgnoredMarkup = true
      }
      matchInlineEndFromPosition(HYPERLINK_REFERENCE_END_REGEX)?.also {
        matchResult = it
      } != null -> {
        addMarkup(matchResult?.value)
        this.inIgnoredMarkup = false
      }
      this.inIgnoredMarkup -> {
        addMarkup(this.curString)
      }
      else -> {
        addText(this.curString)
      }
    }
  }

  private fun isParagraph(): Boolean {
    return (
      (
        this.isExplicitBlockType()
          && ((this.indentation == 0) || (this.indentation < this.lastIndentation))
        ) || this.isTableBlockType()
      )
  }

  private fun matchFromPosition(regex: Regex, pos: Int = this.pos): MatchResult? {
    val matchResult: MatchResult? = regex.find(this.code.substring(pos))
    return if ((matchResult != null) && matchResult.value.isNotEmpty()) matchResult else null
  }

  @Suppress("ComplexMethod")
  private fun matchInlineStartFromPosition(regex: Regex): MatchResult? {
    if ((this.pos > 0) && (matchFromPosition(INLINE_START_PRECEDING_REGEX, this.pos - 1) == null)) {
      return null
    }

    val matchResult: MatchResult? = matchFromPosition(regex)

    if ((matchResult == null) || (this.pos == 0) || (this.pos >= this.code.length - 1)) {
      return matchResult
    } else if (
      matchFromPosition(INLINE_START_FOLLOWING_REGEX, this.pos + matchResult.value.length) == null
    ) {
      return null
    }

    val forbiddenFollowingChar: Char = when (this.code[pos - 1]) {
      '\'' -> '\''
      '"' -> '"'
      '<' -> '>'
      '(' -> ')'
      '[' -> ']'
      '{' -> '}'
      else -> return matchResult
    }

    return if (this.code[this.pos + 1] == forbiddenFollowingChar) null else matchResult
  }

  private fun matchInlineEndFromPosition(regex: Regex): MatchResult? {
    if ((this.pos == 0) || (matchFromPosition(INLINE_END_PRECEDING_REGEX, this.pos - 1) == null)) {
      return null
    }

    val matchResult: MatchResult? = matchFromPosition(regex)

    return if (
      (matchResult == null)
      || (matchFromPosition(INLINE_END_FOLLOWING_REGEX, this.pos + matchResult.value.length)
        != null)
    ) {
      matchResult
    } else {
      null
    }
  }

  private fun isExplicitBlockType(): Boolean {
    return (
      (blockType == BlockType.Footnote)
      || (blockType == BlockType.Directive)
      || (blockType == BlockType.Comment)
    )
  }

  private fun isTableBlockType(): Boolean {
    return (
      (blockType == BlockType.GridTable)
      || (blockType == BlockType.SimpleTable)
    )
  }

  private fun generateDummy(): String {
    return this.dummyGenerator.generate(this.language, this.dummyCounter++)
  }

  private enum class BlockType {
    Paragraph,
    Footnote,
    Directive,
    Comment,
    GridTable,
    SimpleTable,
  }

  companion object {
    private val BLOCK_SEPARATOR_REGEX = Regex("^([ \t]*\r?\n)+")
    private val WHITESPACE_REGEX = Regex("^[ \t]*")

    private val FOOTNOTE_REGEX = Regex(
      "^\\.\\. \\[([0-9]+|[#*]|#[0-9A-Za-z\\-_.:+]+)]([ \t\r\n]|$)"
    )
    private val DIRECTIVE_REGEX = Regex("^\\.\\. [0-9A-Za-z\\-_.:+]+::([ \t\r\n]|$)")
    private val COMMENT_REGEX = Regex("^\\.\\.([ \t\r\n]|$)")

    private val GRID_TABLE_START_REGEX = Regex("^(\\+-{3,}){2,}\\+\r?\n")
    private val SIMPLE_TABLE_START_REGEX = Regex("^={3,}( +={3,})+\r?\n")

    private val SECTION_TITLE_ADORNMENT_REGEX = Regex(
      "^(={3,}|-{3,}|`{3,}|:{3,}|\\.{3,}|'{3,}|\"{3,}|"
      + "~{3,}|\\^{3,}|_{3,}|\\*{3,}|\\+{3,}|#{3,})\r?\n"
    )
    private val LINE_BLOCK_REGEX = Regex("^\\|[ \t]+(?=.*?[^|](\r?\n|$))")

    private val BULLET_LIST_REGEX = Regex("^[*+\\-\u2022\u2023\u2043][ \t]+")
    private val ENUMERATED_LIST_REGEX = Regex(
      "^(([0-9]+|[A-Za-z#]|[IVXLCDM]+|[ivxlcdm]+)\\.|"
      + "\\(?([0-9]+|[A-Za-z#]|[IVXLCDM]+|[ivxlcdm]+)\\))[ \t]+"
    )

    private val INLINE_START_PRECEDING_REGEX = Regex("^[ \t\r\n\\-:/'\"<(\\[{]")
    private val INLINE_START_FOLLOWING_REGEX = Regex("^[^ \t\r\n]")
    private val INLINE_END_PRECEDING_REGEX = Regex("^[^ \t\r\n]")
    private val INLINE_END_FOLLOWING_REGEX = Regex("^([ \t\r\n-.,:;!?\\\\/'\")\\]}>]|$)")
    private val STRONG_EMPHASIS_REGEX = Regex("^\\*\\*")
    private val EMPHASIS_REGEX = Regex("^\\*")
    private val INLINE_LITERAL_REGEX = Regex("^``")
    private val INTERPRETED_TEXT_START_REGEX = Regex("^(:[0-9A-Za-z\\-_.:+]+:)?`")
    private val INTERPRETED_TEXT_END_REGEX = Regex("^`(:[0-9A-Za-z\\-_.:+]+:)?")
    private val INLINE_INTERNAL_TARGET_START_REGEX = Regex("^_`")
    private val INLINE_INTERNAL_TARGET_END_REGEX = Regex("^`")
    private val FOOTNOTE_REFERENCE_START_REGEX = Regex("^\\[")
    private val FOOTNOTE_REFERENCE_END_REGEX = Regex("^]_")
    private val HYPERLINK_REFERENCE_START_REGEX = Regex("^`")
    private val HYPERLINK_REFERENCE_END_REGEX = Regex("^`__?")
  }
}
