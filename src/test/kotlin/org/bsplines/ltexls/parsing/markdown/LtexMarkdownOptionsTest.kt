/* Copyright (C) 2019-2023 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.markdown

import com.vladsch.flexmark.util.data.MutableDataSet
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LtexMarkdownOptionsTest {
  @Test
  fun testConstructor() {
    val dataSet = MutableDataSet()
    dataSet.set(LtexMarkdownExtension.DISPLAY_MATH_PARSER, false)
    dataSet.set(LtexMarkdownExtension.INLINE_MATH_PARSER, false)

    var markdownOptions = LtexMarkdownOptions(dataSet)
    assertFalse(markdownOptions.displayMathParser)
    assertFalse(markdownOptions.inlineMathParser)
    dataSet.set(LtexMarkdownExtension.DISPLAY_MATH_PARSER, true)
    dataSet.set(LtexMarkdownExtension.INLINE_MATH_PARSER, false)

    markdownOptions = LtexMarkdownOptions(dataSet)
    assertTrue(markdownOptions.displayMathParser)
    assertFalse(markdownOptions.inlineMathParser)
    dataSet.set(LtexMarkdownExtension.DISPLAY_MATH_PARSER, false)
    dataSet.set(LtexMarkdownExtension.INLINE_MATH_PARSER, true)

    markdownOptions = LtexMarkdownOptions(dataSet)
    assertFalse(markdownOptions.displayMathParser)
    assertTrue(markdownOptions.inlineMathParser)
  }

  @Test
  fun testSetIn() {
    val dataSet = MutableDataSet()
    dataSet.set(LtexMarkdownExtension.DISPLAY_MATH_PARSER, true)
    dataSet.set(LtexMarkdownExtension.INLINE_MATH_PARSER, false)
    var markdownOptions = LtexMarkdownOptions(dataSet)
    dataSet.set(LtexMarkdownExtension.DISPLAY_MATH_PARSER, false)
    dataSet.set(LtexMarkdownExtension.INLINE_MATH_PARSER, false)
    markdownOptions.setIn(dataSet)
    assertTrue(markdownOptions.displayMathParser)
    assertFalse(markdownOptions.inlineMathParser)

    dataSet.set(LtexMarkdownExtension.DISPLAY_MATH_PARSER, false)
    dataSet.set(LtexMarkdownExtension.INLINE_MATH_PARSER, true)
    markdownOptions = LtexMarkdownOptions(dataSet)
    dataSet.set(LtexMarkdownExtension.DISPLAY_MATH_PARSER, false)
    dataSet.set(LtexMarkdownExtension.INLINE_MATH_PARSER, false)
    markdownOptions.setIn(dataSet)
    assertFalse(markdownOptions.displayMathParser)
    assertTrue(markdownOptions.inlineMathParser)
  }
}
