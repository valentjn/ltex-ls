/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.markdown;

import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.data.MutableDataSetter;
import org.jetbrains.annotations.NotNull;

public class LtexMarkdownOptions implements MutableDataSetter {
  public boolean displayMathParser;
  public boolean inlineMathParser;

  public LtexMarkdownOptions(DataHolder options) {
    this.displayMathParser = LtexMarkdownExtension.DISPLAY_MATH_PARSER.get(options);
    this.inlineMathParser = LtexMarkdownExtension.INLINE_MATH_PARSER.get(options);
  }

  @Override
  public @NotNull MutableDataHolder setIn(@NotNull MutableDataHolder dataHolder) {
    dataHolder.set(LtexMarkdownExtension.DISPLAY_MATH_PARSER, this.displayMathParser);
    dataHolder.set(LtexMarkdownExtension.INLINE_MATH_PARSER, this.inlineMathParser);

    return dataHolder;
  }
}
