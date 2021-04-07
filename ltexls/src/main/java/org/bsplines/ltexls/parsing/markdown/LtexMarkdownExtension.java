/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.markdown;

import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.DataKey;
import com.vladsch.flexmark.util.data.MutableDataHolder;

public class LtexMarkdownExtension implements Parser.ParserExtension {
  public static final DataKey<Boolean> DISPLAY_MATH_PARSER =
      new DataKey<>("DISPLAY_MATH_PARSER", true);
  public static final DataKey<Boolean> INLINE_MATH_PARSER =
      new DataKey<>("INLINE_MATH_PARSER", true);

  private LtexMarkdownExtension() {
  }

  public static LtexMarkdownExtension create() {
    return new LtexMarkdownExtension();
  }

  @Override
  public void parserOptions(MutableDataHolder options) {
  }

  @Override
  public void extend(Parser.Builder parserBuilder) {
    LtexMarkdownOptions options = new LtexMarkdownOptions(parserBuilder);

    if (options.displayMathParser) {
      parserBuilder.customBlockParserFactory(new LtexMarkdownDisplayMathParser.Factory());
    }

    if (options.inlineMathParser) {
      parserBuilder.customInlineParserExtensionFactory(new LtexMarkdownInlineMathParser.Factory());
    }
  }
}
