/* Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.asciidoc

import org.apache.commons.text.StringEscapeUtils
import org.bsplines.ltexls.parsing.CharacterBasedCodeAnnotatedTextBuilder

class AsciidocAnnotatedTextBuilder(
  codeLanguageId: String,
) : CharacterBasedCodeAnnotatedTextBuilder(codeLanguageId) {
  private val inlineElementTypeStack = ArrayDeque<InlineElementType>()
  private var inDocumentHeader = true

  override fun addMarkup(
    markup: String?,
    interpretAs: String?,
  ): CharacterBasedCodeAnnotatedTextBuilder {
    return super.addMarkup(markup, (if (isInMonospace()) "" else interpretAs))
  }

  override fun addText(text: String?): CharacterBasedCodeAnnotatedTextBuilder {
    return if (isInMonospace()) super.addMarkup(text) else super.addText(text)
  }

  override fun processCharacter() {
    if (this.isStartOfLine) {
      processWhitespaceAtStartOfLine()
      if (this.pos >= this.code.length) return
    }

    if (this.isStartOfLine && processStartOfLine()) {
      // skip
    } else {
      processCharacterInternal()
    }
  }

  private fun processWhitespaceAtStartOfLine() {
    if (matchFromPosition(LINE_BOUNDARY) != null) this.inDocumentHeader = false
    val whitespace: String = matchFromPosition(WHITESPACE_REGEX)?.value ?: ""
    addMarkup(whitespace)

    if (this.pos < this.code.length) {
      this.curChar = this.code[this.pos]
      this.curString = this.curChar.toString()
    }
  }

  private fun processStartOfLine(): Boolean {
    var matchResult: MatchResult? = null
    var elementFound = true

    if (matchFromPosition(ANCHOR_REGEX)?.also { matchResult = it } != null) {
      addMarkup(matchResult?.value)
    } else if (matchFromPosition(ATTRIBUTE_LIST_REGEX)?.also { matchResult = it } != null) {
      addMarkup(matchResult?.value)
    } else if (
      this.inDocumentHeader
      && (
        matchFromPosition(DOCUMENT_HEADER_ATTRIBUTE_ENTRY_REGEX)?.also { matchResult = it } != null
      )
    ) {
      addMarkup(matchResult?.value)
    } else if (matchFromPosition(SECTION_TITLE_REGEX)?.also { matchResult = it } != null) {
      addMarkup(matchResult?.value)
    } else if (matchFromPosition(INCLUDE_REGEX)?.also { matchResult = it } != null) {
      addMarkup(matchResult?.value)
    } else if (matchFromPosition(LIST_TITLE_REGEX)?.also { matchResult = it } != null) {
      addMarkup(matchResult?.value)
    } else if (matchFromPosition(LIST_ITEM_REGEX)?.also { matchResult = it } != null) {
      addMarkup(matchResult?.value)
    } else if (matchFromPosition(LIST_CONTINUATION_REGEX)?.also { matchResult = it } != null) {
      addMarkup(matchResult?.value)
    } else if (matchFromPosition(DESCRIPTION_LIST_ITEM_REGEX)?.also { matchResult = it } != null) {
      addText(matchResult?.groups?.get("term")?.value)
      addMarkup(matchResult?.groups?.get("separator")?.value, ":")
      val space: String? = matchResult?.groups?.get("space")?.value
      if (!space.isNullOrEmpty()) addMarkup(space, " ")
    } else if (matchFromPosition(BLOCK_IMAGE_VIDEO_REGEX)?.also { matchResult = it } != null) {
      addMarkup(matchResult?.value)
    } else {
      elementFound = false
    }

    return elementFound
  }

  private fun processCharacterInternal() {
    var matchResult: MatchResult? = null

    if (matchFromPosition(HARD_LINE_BREAK_REGEX)?.also { matchResult = it } != null) {
      addMarkup(matchResult?.value)
    } else if (matchFromPosition(LINK_ANGLE_BRACKET_REGEX)?.also { matchResult = it } != null) {
      addMarkup(matchResult?.value, matchResult?.groups?.get(1)?.value)
    } else if (matchFromPosition(LINK_MACRO_REGEX)?.also { matchResult = it } != null) {
      var interpretAs: String = matchResult?.groups?.get("text1")?.value ?: ""
      if (interpretAs.isEmpty()) interpretAs = matchResult?.groups?.get("text2")?.value ?: ""
      if (interpretAs.isEmpty()) interpretAs = matchResult?.groups?.get("link")?.value ?: ""
      addMarkup(matchResult?.value, interpretAs)
    } else if (matchFromPosition(CROSS_REFERENCE_REGEX)?.also { matchResult = it } != null) {
      val crossReferenceContents: String = matchResult?.groups?.get(1)?.value ?: ""
      val crossReferenceContentsMatchResult: MatchResult? =
        CROSS_REFERENCE_CONTENTS_REGEX.find(crossReferenceContents)
      val argument2: String? = crossReferenceContentsMatchResult?.groups?.get("argument2")?.value

      val interpretAs: String = if (argument2 != null) {
        argument2
      } else {
        val naturalCrossReferenceContentsMatchResult: MatchResult? =
          CROSS_REFERENCE_CONTENTS_NATURAL_REGEX.find(crossReferenceContents)

        if (naturalCrossReferenceContentsMatchResult != null) {
          crossReferenceContents
        } else {
          generateDummy()
        }
      }

      addMarkup(matchResult?.value, interpretAs)
    } else if (matchFromPosition(ANCHOR_REGEX)?.also { matchResult = it } != null) {
      addMarkup(matchResult?.value)
    } else if (matchFromPosition(INLINE_IMAGE_REGEX)?.also { matchResult = it } != null) {
      addMarkup(matchResult?.value, generateDummy())
    } else if (
      matchFromPosition(KEYBOARD_BUTTON_MENU_MACRO_REGEX)?.also { matchResult = it } != null
    ) {
      addMarkup(matchResult?.value, generateDummy())
    } else if (processTextReplacement() || processEntity()) {
      // skip
    } else if (matchTextFormattingMark()?.also { matchResult = it } != null) {
      val inlineElementType: InlineElementType? = when (matchResult?.groups?.get(1)?.value) {
        "*" -> InlineElementType.ConstrainedBold
        "_" -> InlineElementType.ConstrainedItalic
        "`" -> InlineElementType.ConstrainedMonospace
        "#" -> InlineElementType.ConstrainedHighlight
        "**" -> InlineElementType.UnconstrainedBold
        "__" -> InlineElementType.UnconstrainedItalic
        "``" -> InlineElementType.UnconstrainedMonospace
        "##" -> InlineElementType.UnconstrainedHighlight
        "~" -> InlineElementType.UnconstrainedSubscript
        "^" -> InlineElementType.UnconstrainedSuperscript
        else -> null
      }

      if (inlineElementType != null) {
        val interpretAs: String = if (
          (
            (this.inlineElementTypeStack.lastOrNull() == InlineElementType.UnconstrainedMonospace)
            && (inlineElementType == InlineElementType.UnconstrainedMonospace)
          )
          || (
            (this.inlineElementTypeStack.lastOrNull() == InlineElementType.ConstrainedMonospace)
            && (inlineElementType == InlineElementType.ConstrainedMonospace)
          )
        ) {
          generateDummy()
        } else {
          ""
        }

        toggleInlineElementType(inlineElementType)
        addMarkup(matchResult?.value, interpretAs)
      } else {
        addText(matchResult?.value)
      }
    } else {
      addText(this.curString)
    }
  }

  private fun processTextReplacement(): Boolean {
    val match: String? = matchFromPosition(TEXT_REPLACEMENT_REGEX)?.value
    val interpretAs: String? = when (match) {
      "'`" -> "\u2018"
      "`'" -> "\u2019"
      "\"`" -> "\u201c"
      "`\"" -> "\u201d"
      "'" -> if (checkConditionsForApostropheReplacment()) "\u2019" else null
      "\\'" -> "'"
      "(C)" -> "\u00a9"
      "(R)" -> "\u00ae"
      "(TM)" -> "\u2122"
      "--" -> if (checkConditionsForEmDashReplacment()) "\u2014" else null
      " -- " -> "\u2009\u2014\u2009"
      "..." -> "\u2026"
      "->" -> "\u2192"
      "=>" -> "\u21d2"
      "<-" -> "\u2190"
      "<=" -> "\u21d0"
      else -> null
    }

    return if (interpretAs != null) {
      addMarkup(match, interpretAs)
      true
    } else {
      false
    }
  }

  private fun checkConditionsForApostropheReplacment(): Boolean {
    return (
      (this.pos > 0)
      && (this.pos < this.code.length - 1)
      && (matchFromPosition(WORD_CHARACTER, this.pos - 1) != null)
      && (matchFromPosition(WORD_CHARACTER, this.pos + 1) != null)
    )
  }

  private fun checkConditionsForEmDashReplacment(): Boolean {
    return (
      (this.pos > 0)
      && (this.pos < this.code.length - 2)
      && (matchFromPosition(WORD_CHARACTER, this.pos - 1) != null)
      && (
        (matchFromPosition(WORD_CHARACTER, this.pos + 2) != null)
        || (matchFromPosition(LINE_BOUNDARY, this.pos + 2) != null)
      )
    )
  }

  private fun processEntity(): Boolean {
    val entity: String = matchFromPosition(HTML_ENTITY_REGEX)?.value ?: return false
    val interpretAs: String = StringEscapeUtils.unescapeHtml4(entity)

    return if (interpretAs != entity) {
      addMarkup(entity, interpretAs)
      true
    } else {
      false
    }
  }

  private fun matchTextFormattingMark(): MatchResult? {
    return (
      matchFromPosition(TEXT_FORMATTING_UNCONSTRAINED_OPENING_MARK_REGEX)
      ?: matchFromPosition(TEXT_FORMATTING_UNCONSTRAINED_CLOSING_MARK_REGEX)
      ?: matchTextFormattingConstrainedOpeningFromPosition()
      ?: matchTextFormattingConstrainedClosingFromPosition(
        TEXT_FORMATTING_CONSTRAINED_CLOSING_MARK_REGEX
      )
    )
  }

  private fun matchTextFormattingConstrainedOpeningFromPosition(): MatchResult? {
    if (
      (this.pos > 0)
      && (
        matchFromPosition(TEXT_FORMATTING_CONSTRAINED_OPENING_PRECEDING_REGEX, this.pos - 1) == null
      )
    ) {
      return null
    }

    val matchResult: MatchResult? =
        matchFromPosition(TEXT_FORMATTING_CONSTRAINED_OPENING_MARK_REGEX)

    return if (
      (matchResult == null)
      || (this.pos == 0)
      || (this.pos >= this.code.length - 1)
      || (
        matchFromPosition(
          TEXT_FORMATTING_CONSTRAINED_OPENING_FOLLOWING_REGEX,
          this.pos + matchResult.value.length,
        ) != null
      )
    ) {
      matchResult
    } else {
      null
    }
  }

  private fun matchTextFormattingConstrainedClosingFromPosition(regex: Regex): MatchResult? {
    if (
      (this.pos == 0)
      || (
        matchFromPosition(TEXT_FORMATTING_CONSTRAINED_CLOSING_PRECEDING_REGEX, this.pos - 1) == null
      )
    ) {
      return null
    }

    val matchResult: MatchResult? = matchFromPosition(regex)

    return if (
      (matchResult == null)
      || (
        matchFromPosition(
          TEXT_FORMATTING_CONSTRAINED_CLOSING_FOLLOWING_REGEX,
          this.pos + matchResult.value.length,
        ) != null
      )
    ) {
      matchResult
    } else {
      null
    }
  }

  private fun isInMonospace(): Boolean {
    return (
      this.inlineElementTypeStack.contains(InlineElementType.ConstrainedMonospace)
      || this.inlineElementTypeStack.contains(InlineElementType.UnconstrainedMonospace)
    )
  }

  private fun popObjectType() {
    this.inlineElementTypeStack.removeLastOrNull()
  }

  private fun toggleInlineElementType(inlineElementType: InlineElementType) {
    if (this.inlineElementTypeStack.lastOrNull() == inlineElementType) {
      popObjectType()
    } else {
      this.inlineElementTypeStack.addLast(inlineElementType)
    }
  }

  private enum class InlineElementType {
    ConstrainedBold,
    ConstrainedItalic,
    ConstrainedMonospace,
    ConstrainedHighlight,
    UnconstrainedBold,
    UnconstrainedItalic,
    UnconstrainedMonospace,
    UnconstrainedHighlight,
    UnconstrainedSubscript,
    UnconstrainedSuperscript,
  }

  companion object {
    private val WHITESPACE_REGEX = Regex("^[ \t]*")
    private val HARD_LINE_BREAK_REGEX = Regex("^ \\+(?=\r?\n)")

    private val ATTRIBUTE_LIST_REGEX = Regex("^\\[.*](?=\r?\n|$)")

    private val LINK_ANGLE_BRACKET_REGEX = Regex("^<([A-Za-z]+:[^>]*?)>")
    private val LINK_MACRO_REGEX = Regex(
      "^(?<link>(?:http|https|ftp|irc|link|mailto|xref):[^]]*?)"
      + "\\[(?:(?<text1>[^,\\]]+?)|\"(?<text2>[^\"\\]]+?)\")\\^?(?:,[^]]*)?]"
    )

    private val CROSS_REFERENCE_REGEX = Regex("^<<(.+?)>>")
    private val CROSS_REFERENCE_CONTENTS_REGEX = Regex("^(?<argument1>.+?)(?:,(?<argument2>.+?))?$")
    private val CROSS_REFERENCE_CONTENTS_NATURAL_REGEX = Regex("^.*?[ A-Z].*?$")

    private val ANCHOR_REGEX = Regex(
      "^(\\[\\[[:A-Z_a-z][:A-Z_a-z-.0-9]+?]]|\\[#[:A-Z_a-z][:A-Z_a-z-.0-9]+?])"
    )

    private val DOCUMENT_HEADER_ATTRIBUTE_ENTRY_REGEX = Regex("^:[A-Za-z-]+:[ \t]*")

    private val SECTION_TITLE_REGEX = Regex("^={1,6}[ \t]+")

    private val INCLUDE_REGEX = Regex("^include::.*?(?=\r?\n|$)")

    private val LIST_TITLE_REGEX = Regex("^\\.(?=[A-Za-z])")
    private val LIST_ITEM_REGEX = Regex(
      "^(?:(?:\\*+|-|\\.+)(?:[ \t]+\\[[*x ]])?|[0-9]+\\.)[ \t]+(?=[^ \t\r\n])"
    )
    private val LIST_CONTINUATION_REGEX = Regex("^\\+(?=\r?\n)")

    private val DESCRIPTION_LIST_ITEM_REGEX = Regex(
      "^(?<term>.*?)(?<separator>::+)(?<space>[ \t]+|(?=\r?\n))"
    )

    private val BLOCK_IMAGE_VIDEO_REGEX = Regex("^(?:image|video)::.*?(?=\r?\n|$)")
    private val INLINE_IMAGE_REGEX = Regex("^image:[^\\[]+\\[[^]]*]")

    private val KEYBOARD_BUTTON_MENU_MACRO_REGEX = Regex("^(?:kbd:|btn:|menu:[A-Za-z]+)\\[[^]]+]")

    private val TEXT_FORMATTING_CONSTRAINED_OPENING_PRECEDING_REGEX = Regex("^[ \t\r\n*_`#~^]")
    private val TEXT_FORMATTING_CONSTRAINED_OPENING_FOLLOWING_REGEX = Regex("^[^ \t\r\n]")
    private val TEXT_FORMATTING_CONSTRAINED_CLOSING_PRECEDING_REGEX = Regex("^[^ \t\r\n]")
    private val TEXT_FORMATTING_CONSTRAINED_CLOSING_FOLLOWING_REGEX = Regex(
      "^([ \t\r\n*_`#~^,;\".?!]|$)"
    )
    private val TEXT_FORMATTING_CONSTRAINED_OPENING_MARK_REGEX = Regex("^(?:\\[.*?])?([*_`#])")
    private val TEXT_FORMATTING_CONSTRAINED_CLOSING_MARK_REGEX = Regex("^([*_`#])")
    private val TEXT_FORMATTING_CONSTRAINED_CLOSING_MONOSPACE_MARK_REGEX = Regex("^`")
    private val TEXT_FORMATTING_UNCONSTRAINED_OPENING_MARK_REGEX = Regex(
      "^(?:\\[.*?])?(\\*\\*|__|``|##|[~^])"
    )
    private val TEXT_FORMATTING_UNCONSTRAINED_CLOSING_MARK_REGEX = Regex("^(\\*\\*|__|``|##|[~^])")
    private val TEXT_FORMATTING_UNCONSTRAINED_CLOSING_MONOSPACE_MARK_REGEX = Regex("^``")

    private val TEXT_REPLACEMENT_REGEX = Regex(
      "^('`|`'|\"`|`\"|\\\\?'|\\((?:C|R|TM)\\)|--| -- |\\.\\.\\.|->|=>|<-|<=)"
    )
    private val WORD_CHARACTER = Regex("^[0-9A-Za-z]")
    private val LINE_BOUNDARY = Regex("^\r?\n")

    private val HTML_ENTITY_REGEX = Regex("^&(?:#[0-9]+|#x[0-9A-Fa-f]+|[0-9A-Za-z]+);")
  }
}
