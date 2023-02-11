/* Copyright (C) 2019-2023 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.markdown

import com.vladsch.flexmark.parser.InlineParser
import com.vladsch.flexmark.parser.InlineParserExtension
import com.vladsch.flexmark.parser.InlineParserExtensionFactory
import com.vladsch.flexmark.parser.LightInlineParser
import com.vladsch.flexmark.util.sequence.BasedSequence
import java.util.regex.Matcher
import java.util.regex.Pattern

class LtexMarkdownInlineMathParser(
  private val inlineParser: LightInlineParser?,
) : InlineParserExtension {
  class Factory : InlineParserExtensionFactory {
    override fun getAfterDependents(): Set<Class<*>>? {
      return null
    }

    override fun getCharacters(): CharSequence {
      return "$"
    }

    override fun getBeforeDependents(): Set<Class<*>>? {
      return null
    }

    override fun apply(lightInlineParser: LightInlineParser): InlineParserExtension {
      return LtexMarkdownInlineMathParser(lightInlineParser)
    }

    override fun affectsGlobalScope(): Boolean {
      return false
    }
  }

  override fun finalizeDocument(inlineParser: InlineParser) {
  }

  override fun finalizeBlock(inlineParser: InlineParser) {
  }

  override fun parse(inlineParser: LightInlineParser): Boolean {
    if ((inlineParser.peek() == '$') && (inlineParser.peek(1) != ' ')) {
      val input: BasedSequence = inlineParser.input
      val matcher: Matcher? = inlineParser.matcher(MATH_PATTERN)

      if (matcher != null) {
        inlineParser.flushTextNode()
        val openingMarker: BasedSequence = input.subSequence(matcher.start(), matcher.start(1))
        val closingMarker: BasedSequence = input.subSequence(matcher.end(1), matcher.end())

        val inlineMath = LtexMarkdownInlineMath(
          openingMarker,
          openingMarker.baseSubSequence(openingMarker.endOffset, closingMarker.startOffset),
          closingMarker,
        )

        inlineParser.block.appendChild(inlineMath)
        return true
      }
    }

    return false
  }

  companion object {
    private val MATH_PATTERN = Pattern.compile(
      "\\$([^ ]|[^ ](?:.|\n)*?[^ ])(?<!\\\\)(?:\\\${2,}|\\$(?![0-9]))",
    )
  }
}
