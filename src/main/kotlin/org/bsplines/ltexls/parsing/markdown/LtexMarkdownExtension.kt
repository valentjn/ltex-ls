/* Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.markdown

import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.parser.Parser.ParserExtension
import com.vladsch.flexmark.util.data.DataKey
import com.vladsch.flexmark.util.data.MutableDataHolder

class LtexMarkdownExtension private constructor() : ParserExtension {
  override fun parserOptions(options: MutableDataHolder) {
  }

  override fun extend(parserBuilder: Parser.Builder) {
    val options = LtexMarkdownOptions(parserBuilder)

    if (options.displayMathParser) {
      parserBuilder.customBlockParserFactory(LtexMarkdownDisplayMathParser.Factory())
    }

    if (options.inlineMathParser) {
      parserBuilder.customInlineParserExtensionFactory(LtexMarkdownInlineMathParser.Factory())
    }
  }

  companion object {
    val DISPLAY_MATH_PARSER = DataKey("DISPLAY_MATH_PARSER", true)
    val INLINE_MATH_PARSER = DataKey("INLINE_MATH_PARSER", true)

    fun create(): LtexMarkdownExtension {
      return LtexMarkdownExtension()
    }
  }
}
