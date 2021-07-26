/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.org

import org.bsplines.ltexls.parsing.CodeAnnotatedTextBuilder
import org.bsplines.ltexls.parsing.DummyGenerator
import org.bsplines.ltexls.settings.Settings

@Suppress("TooManyFunctions")
class OrgAnnotatedTextBuilder(
  codeLanguageId: String,
) : CodeAnnotatedTextBuilder(codeLanguageId) {
  private var code = ""
  private var pos = 0
  private var curChar = '\u0000'
  private var curString = ""

  private var dummyGenerator = DummyGenerator.getInstance()
  private var dummyCounter = 0
  private var indentation = -1
  private var isStartOfLine = false
  private var appendAtEndOfLine = ""
  private val elementTypeStack = ArrayDeque<ElementType>()
  private var latexEnvironmentName: String? = null
  private val objectTypeStack = ArrayDeque<ObjectType>()

  private var language = "en-US"

  init {
    reinitialize()
  }

  private fun reinitialize() {
    this.code = ""
    this.pos = 0
    this.curString = ""

    this.dummyCounter = 0
    this.indentation = -1
    this.isStartOfLine = false
    this.appendAtEndOfLine = ""
    this.elementTypeStack.clear()
    this.elementTypeStack.addLast(ElementType.Paragraph)
    this.latexEnvironmentName = null
    this.objectTypeStack.clear()
  }

  override fun setSettings(settings: Settings) {
    super.setSettings(settings)
    this.language = settings.languageShortCode
  }

  override fun addText(text: String?): OrgAnnotatedTextBuilder {
    if ((text != null) && text.isNotEmpty()) {
      super.addText(text)
      this.pos += text.length
    }

    return this
  }

  override fun addMarkup(markup: String?): OrgAnnotatedTextBuilder {
    if ((markup != null) && markup.isNotEmpty()) {
      super.addMarkup(markup)
      this.pos += markup.length
    }

    return this
  }

  override fun addMarkup(markup: String?, interpretAs: String?): OrgAnnotatedTextBuilder {
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
      this.isStartOfLine = ((this.pos == 0) || this.code[this.pos - 1] == '\n')

      if (this.isStartOfLine) {
        processWhitespaceAtStartOfLine()
        if (this.pos >= this.code.length) break
      }

      if (this.objectTypeStack.contains(ObjectType.Verbatim)) {
        when (
          val matchResult: MatchResult? = matchInlineEndFromPosition(TEXT_MARKUP_VERBATIM_END_REGEX)
        ) {
          null -> addMarkup(this.curString)
          else -> {
            popObjectType()
            addMarkup(matchResult.value, generateDummy())
          }
        }

        continue
      } else if (this.objectTypeStack.contains(ObjectType.Code)) {
        when (val matchResult: MatchResult? =
              matchInlineEndFromPosition(TEXT_MARKUP_CODE_END_REGEX)) {
          null -> addMarkup(this.curString)
          else -> {
            popObjectType()
            addMarkup(matchResult.value, generateDummy())
          }
        }

        continue
      }

      if (this.isStartOfLine && processStartOfLine()) continue

      if (isInIgnoredElementType()) {
        addMarkup(this.curString)
        continue
      }

      processCharacter()
    }

    addMarkup("", this.appendAtEndOfLine)

    return this
  }

  private fun processWhitespaceAtStartOfLine() {
    val whitespace: String = matchFromPosition(WHITESPACE_REGEX)?.value ?: ""
    this.indentation = whitespace.length
    addMarkup(whitespace)

    if (this.pos < this.code.length) {
      this.curChar = this.code[this.pos]
      this.curString = this.curChar.toString()
    }
  }

  @Suppress("ComplexMethod", "LongMethod")
  private fun processStartOfLine(): Boolean {
    var matchResult: MatchResult? = null
    var elementFound = true

    if (
      this.elementTypeStack.contains(ElementType.Table)
      && (matchFromPosition(TABLE_ROW_REGEX) == null)
    ) {
      popElementType()
    }

    if (isInBlockElementType()) {
      matchResult = matchFromPosition(BLOCK_END_REGEX)

      when (matchResult) {
        null -> addMarkup(this.curString)
        else -> {
          popElementType()
          addMarkup(matchResult.value)
        }
      }
    } else if (this.elementTypeStack.contains(ElementType.PropertyDrawer)) {
      matchResult = matchFromPosition(DRAWER_END_REGEX)

      when (matchResult) {
        null -> addMarkup(this.curString)
        else -> {
          popElementType()
          addMarkup(matchResult.value)
        }
      }
    } else if (this.elementTypeStack.contains(ElementType.LatexEnvironment)) {
      if ((matchFromPosition(LATEX_ENVIRONMENT_END_REGEX)?.also { matchResult = it } != null)
        && (this.latexEnvironmentName != null)
        && (this.latexEnvironmentName == matchResult?.groupValues?.get(1))
      ) {
        popElementType()
        this.latexEnvironmentName = null
        addMarkup(matchResult?.value)
      } else {
        addMarkup(this.curString)
      }
    } else if (
      (this.indentation == 0)
      && (matchFromPosition(HEADLINE_COMMENT_REGEX)?.also { matchResult = it } != null)
    ) {
      addMarkup(matchResult?.value, "\n")
    } else if (
      (this.indentation == 0)
      && (matchFromPosition(HEADLINE_REGEX)?.also { matchResult = it } != null)
    ) {
      this.elementTypeStack.addLast(ElementType.Headline)
      this.appendAtEndOfLine = "\n"
      addMarkup(matchResult?.value, "\n")
    } else if (
      matchFromPosition(AFFILIATED_KEYWORDS_REGEX)?.also { matchResult = it } != null
    ) {
      addMarkup(matchResult?.value)
    } else if (matchFromPosition(BLOCK_BEGIN_REGEX)?.also { matchResult = it } != null) {
      val blockType: String? = matchResult?.groups?.get(1)?.value

      val elementType: ElementType = when {
        blockType == null -> ElementType.GreaterSpecialBlock
        blockType.equals("CENTER", ignoreCase = true) -> ElementType.GreaterCenterBlock
        blockType.equals("QUOTE", ignoreCase = true) -> ElementType.GreaterQuoteBlock
        blockType.equals("COMMENT", ignoreCase = true) -> ElementType.CommentBlock
        blockType.equals("EXAMPLE", ignoreCase = true) -> ElementType.ExampleBlock
        blockType.equals("EXPORT", ignoreCase = true) -> ElementType.ExportBlock
        blockType.equals("SRC", ignoreCase = true) -> ElementType.SourceBlock
        blockType.equals("VERSE", ignoreCase = true) -> ElementType.VerseBlock
        else -> ElementType.GreaterSpecialBlock
      }

      this.elementTypeStack.addLast(elementType)
      addMarkup(matchResult?.value)
    } else if (matchFromPosition(BLOCK_END_REGEX)?.also { matchResult = it } != null) {
      popElementType()
      addMarkup(matchResult?.value)
    } else if (matchFromPosition(DRAWER_BEGIN_REGEX)?.also { matchResult = it } != null) {
      val drawerName: String? = matchResult?.groups?.get(1)?.value

      if ((drawerName != null) && drawerName.equals("PROPERTIES", ignoreCase = true)) {
        this.elementTypeStack.addLast(ElementType.PropertyDrawer)
      } else {
        this.elementTypeStack.addLast(ElementType.Drawer)
      }

      addMarkup(matchResult?.value)
    } else if (matchFromPosition(DRAWER_END_REGEX)?.also { matchResult = it } != null) {
      popElementType()
      addMarkup(matchResult?.value)
    } else if (
      matchFromPosition(DYNAMIC_BLOCK_BEGIN_REGEX)?.also { matchResult = it } != null
    ) {
      this.elementTypeStack.addLast(ElementType.DynamicBlock)
      addMarkup(matchResult?.value)
    } else if (
      matchFromPosition(DYNAMIC_BLOCK_END_REGEX)?.also { matchResult = it } != null
    ) {
      popElementType()
      addMarkup(matchResult?.value)
    } else if (
      matchFromPosition(FOOTNOTE_DEFINITION_REGEX)?.also { matchResult = it } != null
    ) {
      addMarkup(matchResult?.value)
    } else if (
      (matchFromPosition(RULE_TABLE_ROW_REGEX)?.also { matchResult = it } != null)
      || (matchFromPosition(TABLE_ROW_REGEX)?.also { matchResult = it } != null)
    ) {
      if (!this.elementTypeStack.contains(ElementType.Table)) {
        this.elementTypeStack.addLast(ElementType.Table)
      }

      this.appendAtEndOfLine = "\n"
      addMarkup(matchResult?.value, "\n")
    } else if (matchFromPosition(ITEM_REGEX)?.also { matchResult = it } != null) {
      this.appendAtEndOfLine = "\n"
      addMarkup(matchResult?.value, "\n")
    } else if (matchFromPosition(BABEL_CALL_REGEX)?.also { matchResult = it } != null) {
      addMarkup(matchResult?.value)
    } else if (matchFromPosition(CLOCK_REGEX)?.also { matchResult = it } != null) {
      addMarkup(matchResult?.value)
    } else if (matchFromPosition(DIARY_SEXP_REGEX)?.also { matchResult = it } != null) {
      addMarkup(matchResult?.value)
    } else if (matchFromPosition(PLANNING_REGEX)?.also { matchResult = it } != null) {
      addMarkup(matchResult?.value)
    } else if (matchFromPosition(COMMENT_REGEX)?.also { matchResult = it } != null) {
      addMarkup(matchResult?.value)
    } else if (matchFromPosition(FIXED_WIDTH_LINE_REGEX)?.also { matchResult = it } != null) {
      addMarkup(matchResult?.value)
    } else if (matchFromPosition(HORIZONTAL_RULE_REGEX)?.also { matchResult = it } != null) {
      addMarkup(matchResult?.value)
    } else if (matchFromPosition(KEYWORD_REGEX)?.also { matchResult = it } != null) {
      addMarkup(matchResult?.value)
    } else if (
      matchFromPosition(LATEX_ENVIRONMENT_BEGIN_REGEX)?.also { matchResult = it } != null
    ) {
      this.elementTypeStack.addLast(ElementType.LatexEnvironment)
      this.latexEnvironmentName = matchResult?.groups?.get(1)?.value
      addMarkup(matchResult?.value)
    } else {
      elementFound = false
    }

    return elementFound
  }

  @Suppress("ComplexMethod", "LongMethod")
  private fun processCharacter() {
    var matchResult: MatchResult? = null

    if (
      this.elementTypeStack.contains(ElementType.Headline)
      && (matchFromPosition(HEADLINE_TAGS_REGEX)?.also { matchResult = it } != null)
    ) {
      addMarkup(matchResult?.value)
    } else if (
      this.elementTypeStack.contains(ElementType.Table)
      && (matchFromPosition(TABLE_CELL_SEPARATOR_REGEX)?.also { matchResult = it } != null)
    ) {
      addMarkup(matchResult?.value, "\n\n")
    } else if (
      this.objectTypeStack.contains(ObjectType.RegularLinkDescription)
      && (matchFromPosition(REGULAR_LINK_DESCRIPTION_END_REGEX)?.also { matchResult = it }
        != null)
    ) {
      popObjectType()
      addMarkup(matchResult?.value)
    } else if (matchFromPosition(LATEX_FRAGMENT_REGEX1)?.also { matchResult = it } != null) {
      addMarkup(matchResult?.value, generateDummy())
    } else if (matchFromPosition(LATEX_FRAGMENT_REGEX2)?.also { matchResult = it } != null) {
      addMarkup(matchResult?.value, generateDummy())
    } else if (matchFromPosition(LATEX_FRAGMENT_REGEX3)?.also { matchResult = it } != null) {
      addMarkup(matchResult?.value, generateDummy())
    } else if (matchFromPosition(LATEX_FRAGMENT_REGEX4)?.also { matchResult = it } != null) {
      addMarkup(matchResult?.value, generateDummy())
    } else if (
      (this.isStartOfLine || (this.code[this.pos - 1] != '$'))
      && (matchFromPosition(LATEX_FRAGMENT_REGEX5)?.also { matchResult = it } != null)
    ) {
      addMarkup(matchResult?.value, generateDummy())
    } else if (
      (this.isStartOfLine || (this.code[this.pos - 1] != '$'))
      && (matchFromPosition(LATEX_FRAGMENT_REGEX6)?.also { matchResult = it } != null)
    ) {
      addMarkup(matchResult?.value, generateDummy())
    } else if (matchFromPosition(EXPORT_SNIPPET_REGEX)?.also { matchResult = it } != null) {
      addMarkup(matchResult?.value)
    } else if (matchFromPosition(FOOTNOTE_REFERENCE_REGEX1)?.also { matchResult = it } != null) {
      addMarkup(matchResult?.value)
    } else if (matchFromPosition(FOOTNOTE_REFERENCE_REGEX2)?.also { matchResult = it } != null) {
      addMarkup(matchResult?.value)
    } else if (matchFromPosition(INLINE_BABEL_CALL_REGEX)?.also { matchResult = it } != null) {
      addMarkup(matchResult?.value, generateDummy())
    } else if (matchFromPosition(INLINE_SOURCE_BLOCK_REGEX)?.also { matchResult = it } != null) {
      addMarkup(matchResult?.value, generateDummy())
    } else if (matchFromPosition(MACRO_REGEX)?.also { matchResult = it } != null) {
      addMarkup(matchResult?.value, generateDummy())
    } else if (matchFromPosition(STATISTICS_COOKIE_REGEX)?.also { matchResult = it } != null) {
      addMarkup(matchResult?.value, generateDummy())
    } else if (matchFromPosition(DIARY_TIMESTAMP_REGEX)?.also { matchResult = it } != null) {
      addMarkup(matchResult?.value, generateDummy())
    } else if (
      matchFromPosition(ACTIVE_TIMESTAMP_RANGE_REGEX1)?.also { matchResult = it } != null
    ) {
      addMarkup(matchResult?.value, generateDummy())
    } else if (
      matchFromPosition(ACTIVE_TIMESTAMP_RANGE_REGEX2)?.also { matchResult = it } != null
    ) {
      addMarkup(matchResult?.value, generateDummy())
    } else if (
      matchFromPosition(INACTIVE_TIMESTAMP_RANGE_REGEX1)?.also { matchResult = it } != null
    ) {
      addMarkup(matchResult?.value, generateDummy())
    } else if (
      matchFromPosition(INACTIVE_TIMESTAMP_RANGE_REGEX2)?.also { matchResult = it } != null
    ) {
      addMarkup(matchResult?.value, generateDummy())
    } else if (matchFromPosition(ACTIVE_TIMESTAMP_REGEX)?.also { matchResult = it } != null) {
      addMarkup(matchResult?.value, generateDummy())
    } else if (matchFromPosition(INACTIVE_TIMESTAMP_REGEX)?.also { matchResult = it } != null) {
      addMarkup(matchResult?.value, generateDummy())
    } else if (
      (matchFromPosition(RADIO_TARGET_REGEX)?.also { matchResult = it } != null)
      && (
        this.isStartOfLine
        || (LINK_PRECEDING_REGEX.find(this.code[this.pos - 1].toString()) != null)
      )
    ) {
      addMarkup(matchResult?.value, generateDummy())
    } else if (
      (matchFromPosition(TARGET_REGEX)?.also { matchResult = it } != null)
      && (
        this.isStartOfLine
        || (LINK_PRECEDING_REGEX.find(this.code[this.pos - 1].toString()) != null)
      )
    ) {
      addMarkup(matchResult?.value, generateDummy())
    } else if (matchFromPosition(ANGLE_LINK_REGEX)?.also { matchResult = it } != null) {
      addMarkup(matchResult?.value, generateDummy())
    } else if (
      (matchFromPosition(PLAIN_LINK_REGEX)?.also { matchResult = it } != null)
      && (
        this.isStartOfLine
        || (LINK_PRECEDING_REGEX.find(this.code[this.pos - 1].toString()) != null)
      )
    ) {
      addMarkup(matchResult?.value, generateDummy())
    } else if (
      matchFromPosition(REGULAR_LINK_WITHOUT_DESCRIPTION_REGEX)?.also { matchResult = it }
      != null
    ) {
      addMarkup(matchResult?.value, generateDummy())
    } else if (
      matchFromPosition(REGULAR_LINK_WITH_DESCRIPTION_REGEX)?.also { matchResult = it } != null
    ) {
      this.objectTypeStack.add(ObjectType.RegularLinkDescription)
      addMarkup(matchResult?.value)
    } else if (
      (matchInlineStartFromPosition(TEXT_MARKUP_MARKER_REGEX)?.also { matchResult = it } != null)
      || (matchInlineEndFromPosition(TEXT_MARKUP_MARKER_REGEX)?.also { matchResult = it } != null)
    ) {
      when (val textMarkupMarker: String? = matchResult?.value) {
        "*" -> {
          toggleObjectType(ObjectType.Bold)
          addMarkup(textMarkupMarker)
        }
        "+" -> {
          toggleObjectType(ObjectType.Strikethrough)
          addMarkup(textMarkupMarker)
        }
        "/" -> {
          toggleObjectType(ObjectType.Italic)
          addMarkup(textMarkupMarker)
        }
        "=" -> {
          toggleObjectType(ObjectType.Verbatim)
          addMarkup(textMarkupMarker)
        }
        "_" -> {
          toggleObjectType(ObjectType.Underline)
          addMarkup(textMarkupMarker)
        }
        "~" -> {
          toggleObjectType(ObjectType.Code)
          addMarkup(textMarkupMarker)
        }
        else -> {
          addText(textMarkupMarker)
        }
      }
    } else if (this.curChar == '\n') {
      addMarkup("\n", "\n" + this.appendAtEndOfLine)
      this.appendAtEndOfLine = ""
      if (this.elementTypeStack.contains(ElementType.Headline)) popElementType()
    } else {
      addText(this.curString)
    }
  }

  private fun isInIgnoredElementType(): Boolean {
    return (
      this.elementTypeStack.contains(ElementType.CommentBlock)
        || this.elementTypeStack.contains(ElementType.ExampleBlock)
        || this.elementTypeStack.contains(ElementType.ExportBlock)
        || this.elementTypeStack.contains(ElementType.SourceBlock)
        || this.elementTypeStack.contains(ElementType.PropertyDrawer)
        || this.elementTypeStack.contains(ElementType.LatexEnvironment)
      )
  }

  private fun isInBlockElementType(): Boolean {
    return (
      this.elementTypeStack.contains(ElementType.CommentBlock)
        || this.elementTypeStack.contains(ElementType.ExampleBlock)
        || this.elementTypeStack.contains(ElementType.ExportBlock)
        || this.elementTypeStack.contains(ElementType.SourceBlock)
      )
  }

  private fun matchFromPosition(regex: Regex, pos: Int = this.pos): MatchResult? {
    val matchResult: MatchResult? = regex.find(this.code.substring(pos))
    return if ((matchResult != null) && matchResult.value.isNotEmpty()) matchResult else null
  }

  @Suppress("ComplexCondition")
  private fun matchInlineStartFromPosition(
    @Suppress("SameParameterValue") regex: Regex,
  ): MatchResult? {
    if ((this.pos > 0)
          && (matchFromPosition(TEXT_MARKUP_START_PRECEDING_REGEX, this.pos - 1) == null)) {
      return null
    }

    val matchResult: MatchResult? = matchFromPosition(regex)

    return if (
      (matchResult == null)
      || (this.pos == 0)
      || (this.pos >= this.code.length - 1)
      || (matchFromPosition(
        TEXT_MARKUP_START_FOLLOWING_REGEX,
        this.pos + matchResult.value.length,
      ) != null)
    ) {
      matchResult
    } else {
      null
    }
  }

  private fun matchInlineEndFromPosition(regex: Regex): MatchResult? {
    if ((this.pos == 0)
          || (matchFromPosition(TEXT_MARKUP_END_PRECEDING_REGEX, this.pos - 1) == null)) {
      return null
    }

    val matchResult: MatchResult? = matchFromPosition(regex)

    return if (
      (matchResult == null)
      || (matchFromPosition(TEXT_MARKUP_END_FOLLOWING_REGEX,
        this.pos + matchResult.value.length) != null)
    ) {
      matchResult
    } else {
      null
    }
  }

  private fun generateDummy(): String {
    return this.dummyGenerator.generate(this.language, this.dummyCounter++)
  }

  private fun popElementType() {
    this.elementTypeStack.removeLastOrNull()
    if (this.elementTypeStack.isEmpty()) this.elementTypeStack.addLast(ElementType.Paragraph)
  }

  private fun popObjectType() {
    this.objectTypeStack.removeLastOrNull()
  }

  private fun toggleObjectType(objectType: ObjectType) {
    if (this.objectTypeStack.lastOrNull() == objectType) {
      popObjectType()
    } else {
      this.objectTypeStack.addLast(objectType)
    }
  }

  private enum class ElementType {
    Headline,
    GreaterCenterBlock,
    GreaterQuoteBlock,
    GreaterSpecialBlock,
    CommentBlock,
    ExampleBlock,
    ExportBlock,
    SourceBlock,
    VerseBlock,
    Drawer,
    PropertyDrawer,
    DynamicBlock,
    LatexEnvironment,
    Table,
    Paragraph,
  }

  private enum class ObjectType {
    RegularLinkDescription,
    Bold,
    Strikethrough,
    Italic,
    Verbatim,
    Underline,
    Code,
  }

  companion object {
    private const val REGULAR_LINK_PATH_REGEX_STRING = (
        "[ \\-/0-9A-Z\\\\a-z]+"
        + "|[A-Za-z]+:(//)?[^\r\n\\[\\]]+"
        + "|id:[-0-9A-Fa-f]+"
        + "|#[^\r\n\\[\\]]+"
        + "|\\([^\r\n\\[\\]]+\\)"
        + "|[^\r\n\\[\\]]+")

    private const val TIMESTAMP_REGEX_STRING = (
        "[0-9]{4}-[0-9]{2}-[0-9]{2}[ \t]+[^ \t\r\n+\\-0-9>\\]]+"
        + "([ \t]+[0-9]{1,2}:[0-9]{2})?"
        + "([ \t]+(\\+|\\+\\+|\\.\\+|-|--)[0-9]+[dhmwy]){0,2}")
    private const val TIMESTAMP_RANGE_REGEX_STRING = (
        "[0-9]{4}-[0-9]{2}-[0-9]{2}[ \t]+[^ \t\r\n+\\-0-9>\\]]+"
        + "[ \t]+[0-9]{1,2}:[0-9]{2}-[0-9]{1,2}:[0-9]{2}"
        + "([ \t]+(\\+|\\+\\+|\\.\\+|-|--)[0-9]+[dhmwy]){0,2}")

    private val WHITESPACE_REGEX = Regex(
        "^[ \t]*", RegexOption.IGNORE_CASE)

    private val HEADLINE_REGEX = Regex(
        "^(\\*+(?= ))([ \t]+(?-i:TODO|DONE))?([ \t]+\\[#[A-Za-z]])?[ \t]*",
        RegexOption.IGNORE_CASE)
    private val HEADLINE_COMMENT_REGEX = Regex(
        "^(\\*+(?= ))([ \t]+(?-i:TODO|DONE))?([ \t]+\\[#[A-Za-z]])?"
        + "[ \t]+COMMENT(?=[ \t]|\r?\n|$)[^\r\n]*(?=\r?\n|$)",
        RegexOption.IGNORE_CASE)
    private val HEADLINE_TAGS_REGEX = Regex(
        "^[ \t]*((:[#%0-9@A-Z_a-z]+)+:)?[ \t]*(?=\r?\n|$)", RegexOption.IGNORE_CASE)

    private val AFFILIATED_KEYWORDS_REGEX = Regex(
        "^#\\+((CAPTION|HEADER|NAME|PLOT|RESULTS)|"
        + "((CAPTION|RESULTS)\\[[^\r\n]*?])|ATTR_[-0-9A-Z_a-z]+): [^\r\n]*(?=\r?\n|$)",
        RegexOption.IGNORE_CASE)

    private val BLOCK_BEGIN_REGEX = Regex(
        "^#\\+BEGIN_([^ \t\r\n]+)([ \t]+[^\r\n]*?)?[ \t]*(?=\r?\n|$)", RegexOption.IGNORE_CASE)
    private val BLOCK_END_REGEX = Regex(
        "^#\\+END_([^ \t\r\n]+)[ \t]*(?=\r?\n|$)", RegexOption.IGNORE_CASE)

    private val DRAWER_BEGIN_REGEX = Regex(
        "^:([-A-Z_a-z]+):[ \t]*(?=\r?\n|$)", RegexOption.IGNORE_CASE)
    private val DRAWER_END_REGEX = Regex(
        "^:END:[ \t]*(?=\r?\n|$)", RegexOption.IGNORE_CASE)

    private val DYNAMIC_BLOCK_BEGIN_REGEX = Regex(
        "^#\\+BEGIN: ([^ \t\r\n]+)([ \t]+[^\r\n]*?)[ \t]*(?=\r?\n|$)", RegexOption.IGNORE_CASE)
    private val DYNAMIC_BLOCK_END_REGEX = Regex(
        "^#\\+END:[ \t]*(?=\r?\n|$)", RegexOption.IGNORE_CASE)

    private val FOOTNOTE_DEFINITION_REGEX = Regex(
        "^\\[fn:([0-9]+|[-A-Z_a-z]+)][ \t]*", RegexOption.IGNORE_CASE)

    private val ITEM_REGEX = Regex(
        "^(\\*|-|\\+|([0-9]+|[A-Za-z])[.)])(?=[ \t]|$)([ \t]+\\[@([0-9]+|[A-Za-z])])?"
        + "([ \t]+\\[[- \tX]])?([ \t]+[^\r\n]*?[ \t]+::)?[ \t]*",
        RegexOption.IGNORE_CASE)

    private val TABLE_ROW_REGEX = Regex(
        "^\\|[ \t]*", RegexOption.IGNORE_CASE)
    private val RULE_TABLE_ROW_REGEX = Regex(
        "^\\|-[^\r\n]*(?=\r?\n|$)", RegexOption.IGNORE_CASE)
    private val TABLE_CELL_SEPARATOR_REGEX = Regex(
        "^[ \t]*\\|[ \t]*", RegexOption.IGNORE_CASE)

    private val BABEL_CALL_REGEX = Regex(
        "^#\\+CALL:[ \t]*([^\r\n]+?)[ \t]*(?=\r?\n|$)", RegexOption.IGNORE_CASE)

    private val CLOCK_REGEX = Regex(
        "^CLOCK:[ \t]*([^\r\n]+?)[ \t]*(?=\r?\n|$)", RegexOption.IGNORE_CASE)
    private val DIARY_SEXP_REGEX = Regex(
        "^%%\\(([^\r\n]*)", RegexOption.IGNORE_CASE)
    private val PLANNING_REGEX = Regex(
        "^(DEADLINE|SCHEDULED|CLOSED):[ \t]*("
        + "<%%\\([^\r\n>]+\\)>"
        + "|<" + TIMESTAMP_REGEX_STRING + ">"
        + "|\\[" + TIMESTAMP_REGEX_STRING + "]"
        + "|<" + TIMESTAMP_REGEX_STRING + ">--<" + TIMESTAMP_REGEX_STRING + ">"
        + "|<" + TIMESTAMP_RANGE_REGEX_STRING + ">"
        + "|\\[" + TIMESTAMP_REGEX_STRING + "]\\[" + TIMESTAMP_REGEX_STRING + "]"
        + "|\\[" + TIMESTAMP_RANGE_REGEX_STRING + "]"
        + ")]*(?=\r?\n|$)", RegexOption.IGNORE_CASE)

    private val COMMENT_REGEX = Regex(
        "^#([ \t]+[^\r\n]*?)?(\r?\n|$)", RegexOption.IGNORE_CASE)

    private val FIXED_WIDTH_LINE_REGEX = Regex(
        "^:([ \t]+|(?=\r?\n|$))", RegexOption.IGNORE_CASE)

    private val HORIZONTAL_RULE_REGEX = Regex(
        "^-{5,}[ \t]*(?=\r?\n|$)", RegexOption.IGNORE_CASE)

    private val KEYWORD_REGEX = Regex(
        "^#\\+([^ \t\r\n]+?):[ \t]*([^\r\n]+?)[ \t]*(?=\r?\n|$)", RegexOption.IGNORE_CASE)

    private val LATEX_ENVIRONMENT_BEGIN_REGEX = Regex(
        "^\\\\begin\\{([*0-9A-Za-z]+)}[ \t]*", RegexOption.IGNORE_CASE)
    private val LATEX_ENVIRONMENT_END_REGEX = Regex(
        "^\\\\end\\{([*0-9A-Za-z]+)}[ \t]*", RegexOption.IGNORE_CASE)

    private val LATEX_FRAGMENT_REGEX1 = Regex(
        "^\\\\[A-Za-z]+(\\[[^\r\n{}\\[\\]]*]|\\{[^\r\n{}]*})*", RegexOption.IGNORE_CASE)
    private val LATEX_FRAGMENT_REGEX2 = Regex(
        "^\\\\\\((.|\r?\n)*?\\\\\\)", RegexOption.IGNORE_CASE)
    private val LATEX_FRAGMENT_REGEX3 = Regex(
        "^\\\\\\[(.|\r?\n)*?\\\\]", RegexOption.IGNORE_CASE)
    private val LATEX_FRAGMENT_REGEX4 = Regex(
        "^\\$\\$(.|\r?\n)*?\\$\\$", RegexOption.IGNORE_CASE)
    private val LATEX_FRAGMENT_REGEX5 = Regex(
        "^\\$[^ \t\r\n\"',.;?]\\$(?=[ \t\"'(),.;<>?\\[\\]]|\r?\n|$)", RegexOption.IGNORE_CASE)
    private val LATEX_FRAGMENT_REGEX6 = Regex(
        "^\\$[^ \t\r\n$,.;]([^\r\n$]|\r?\n)*[^ \t\r\n$,.]\\$(?=[ \t!\"'(),.;<>?\\[\\]]|\r?\n|$)",
        RegexOption.IGNORE_CASE)

    private val EXPORT_SNIPPET_REGEX = Regex(
        "^@@[-0-9A-Za-z]+:[^\r\n]*?@@", RegexOption.IGNORE_CASE)

    private val FOOTNOTE_REFERENCE_REGEX1 = Regex(
        "^\\[fn:[-0-9A-Z_a-z]*]", RegexOption.IGNORE_CASE)
    private val FOOTNOTE_REFERENCE_REGEX2 = Regex(
        "^\\[fn:([-0-9A-Z_a-z]*)?:[^\r\n]*?]", RegexOption.IGNORE_CASE)

    private val INLINE_BABEL_CALL_REGEX = Regex(
        "^call_[^ \t\r\n()]+(\\[[^\r\n]*?])?\\([^\r\n]*?\\)(\\[[^\r\n]*?])?",
        RegexOption.IGNORE_CASE)
    private val INLINE_SOURCE_BLOCK_REGEX = Regex(
        "^src_[^ \t\r\n]+(\\[[^\r\n]*?])?\\{[^\r\n]*?}", RegexOption.IGNORE_CASE)

    private val MACRO_REGEX = Regex(
        "^\\{\\{\\{[A-Za-z][-0-9A-Z_a-z]*(\\([^\r\n]*?\\))?}}}",
        RegexOption.IGNORE_CASE)

    private val STATISTICS_COOKIE_REGEX = Regex(
        "^\\[[0-9]*(%|/[0-9]*)]", RegexOption.IGNORE_CASE)

    private val DIARY_TIMESTAMP_REGEX = Regex(
        "^<%%\\([^\r\n>]+\\)>", RegexOption.IGNORE_CASE)
    private val ACTIVE_TIMESTAMP_RANGE_REGEX1 = Regex(
        "^<$TIMESTAMP_REGEX_STRING>--<$TIMESTAMP_REGEX_STRING>",
        RegexOption.IGNORE_CASE)
    private val ACTIVE_TIMESTAMP_RANGE_REGEX2 = Regex(
        "^<$TIMESTAMP_RANGE_REGEX_STRING>", RegexOption.IGNORE_CASE)
    private val INACTIVE_TIMESTAMP_RANGE_REGEX1 = Regex(
        "^\\[$TIMESTAMP_REGEX_STRING]--\\[$TIMESTAMP_REGEX_STRING]", RegexOption.IGNORE_CASE)
    private val INACTIVE_TIMESTAMP_RANGE_REGEX2 = Regex(
        "^\\[$TIMESTAMP_RANGE_REGEX_STRING]", RegexOption.IGNORE_CASE)
    private val ACTIVE_TIMESTAMP_REGEX = Regex(
        "^<$TIMESTAMP_REGEX_STRING>", RegexOption.IGNORE_CASE)
    private val INACTIVE_TIMESTAMP_REGEX = Regex(
        "^\\[$TIMESTAMP_REGEX_STRING]", RegexOption.IGNORE_CASE)

    private val ANGLE_LINK_REGEX = Regex(
        "^<[A-Za-z]+:[^\r\n<>\\]]+>", RegexOption.IGNORE_CASE)
    private val PLAIN_LINK_REGEX = Regex(
        "^[A-Za-z]+:[^ \t\r\n()<>]+(?<=[A-Za-z]|[^ \t\r\n!,.;?]/)(?=[^\r\n0-9A-Za-z]|\r?\n|$)",
        RegexOption.IGNORE_CASE)

    private val LINK_PRECEDING_REGEX = Regex(
        "^[^\r\n0-9A-Za-z]", RegexOption.IGNORE_CASE)
    private val RADIO_TARGET_REGEX = Regex(
        "^<<<(?![ \t])[^\r\n<>]+(?<![ \t])>>>", RegexOption.IGNORE_CASE)
    private val TARGET_REGEX = Regex(
        "^<<(?![ \t])[^\r\n<>]+(?<![ \t])>>", RegexOption.IGNORE_CASE)

    private val REGULAR_LINK_WITHOUT_DESCRIPTION_REGEX = Regex(
        "^\\[\\[($REGULAR_LINK_PATH_REGEX_STRING)]]", RegexOption.IGNORE_CASE)
    private val REGULAR_LINK_WITH_DESCRIPTION_REGEX = Regex(
        "^\\[\\[($REGULAR_LINK_PATH_REGEX_STRING)]\\[(?=[^\r\n\\[\\]]+]])",
        RegexOption.IGNORE_CASE)
    private val REGULAR_LINK_DESCRIPTION_END_REGEX = Regex(
        "^]]", RegexOption.IGNORE_CASE)

    private val TEXT_MARKUP_START_PRECEDING_REGEX = Regex(
        "^[ \t\r\n\"'(\\-{]", RegexOption.IGNORE_CASE)
    private val TEXT_MARKUP_START_FOLLOWING_REGEX = Regex(
        "^[^ \t\r\n]", RegexOption.IGNORE_CASE)
    private val TEXT_MARKUP_END_PRECEDING_REGEX = Regex(
        "^[^ \t\r\n]", RegexOption.IGNORE_CASE)
    private val TEXT_MARKUP_END_FOLLOWING_REGEX = Regex(
        "^([ \t\r\n!\"'),\\-.:;?\\[}]|$)", RegexOption.IGNORE_CASE)
    private val TEXT_MARKUP_MARKER_REGEX = Regex(
        "^[*+/=_~]", RegexOption.IGNORE_CASE)
    private val TEXT_MARKUP_VERBATIM_END_REGEX = Regex(
        "^=", RegexOption.IGNORE_CASE)
    private val TEXT_MARKUP_CODE_END_REGEX = Regex(
        "^~", RegexOption.IGNORE_CASE)
  }
}
