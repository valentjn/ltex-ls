/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.markdown;

import com.vladsch.flexmark.util.data.MutableDataSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LtexMarkdownOptionsTest {
  @Test
  public void testConstructor() {
    MutableDataSet dataSet = new MutableDataSet();
    dataSet.set(LtexMarkdownExtension.DISPLAY_MATH_PARSER, false);
    dataSet.set(LtexMarkdownExtension.INLINE_MATH_PARSER, false);
    LtexMarkdownOptions markdownOptions = new LtexMarkdownOptions(dataSet);
    Assertions.assertFalse(markdownOptions.displayMathParser);
    Assertions.assertFalse(markdownOptions.inlineMathParser);

    dataSet.set(LtexMarkdownExtension.DISPLAY_MATH_PARSER, true);
    dataSet.set(LtexMarkdownExtension.INLINE_MATH_PARSER, false);
    markdownOptions = new LtexMarkdownOptions(dataSet);
    Assertions.assertTrue(markdownOptions.displayMathParser);
    Assertions.assertFalse(markdownOptions.inlineMathParser);

    dataSet.set(LtexMarkdownExtension.DISPLAY_MATH_PARSER, false);
    dataSet.set(LtexMarkdownExtension.INLINE_MATH_PARSER, true);
    markdownOptions = new LtexMarkdownOptions(dataSet);
    Assertions.assertFalse(markdownOptions.displayMathParser);
    Assertions.assertTrue(markdownOptions.inlineMathParser);
  }

  @Test
  public void testSetIn() {
    MutableDataSet dataSet = new MutableDataSet();
    dataSet.set(LtexMarkdownExtension.DISPLAY_MATH_PARSER, true);
    dataSet.set(LtexMarkdownExtension.INLINE_MATH_PARSER, false);
    LtexMarkdownOptions markdownOptions = new LtexMarkdownOptions(dataSet);
    dataSet.set(LtexMarkdownExtension.DISPLAY_MATH_PARSER, false);
    dataSet.set(LtexMarkdownExtension.INLINE_MATH_PARSER, false);
    markdownOptions.setIn(dataSet);
    Assertions.assertTrue(markdownOptions.displayMathParser);
    Assertions.assertFalse(markdownOptions.inlineMathParser);

    dataSet.set(LtexMarkdownExtension.DISPLAY_MATH_PARSER, false);
    dataSet.set(LtexMarkdownExtension.INLINE_MATH_PARSER, true);
    markdownOptions = new LtexMarkdownOptions(dataSet);
    dataSet.set(LtexMarkdownExtension.DISPLAY_MATH_PARSER, false);
    dataSet.set(LtexMarkdownExtension.INLINE_MATH_PARSER, false);
    markdownOptions.setIn(dataSet);
    Assertions.assertFalse(markdownOptions.displayMathParser);
    Assertions.assertTrue(markdownOptions.inlineMathParser);
  }
}
