/* Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.markdown

import com.vladsch.flexmark.util.data.DataHolder
import com.vladsch.flexmark.util.data.MutableDataHolder
import com.vladsch.flexmark.util.data.MutableDataSetter

class LtexMarkdownOptions(options: DataHolder) : MutableDataSetter {
  var displayMathParser: Boolean = LtexMarkdownExtension.DISPLAY_MATH_PARSER.get(options)
  var inlineMathParser: Boolean = LtexMarkdownExtension.INLINE_MATH_PARSER.get(options)

  override fun setIn(dataHolder: MutableDataHolder): MutableDataHolder {
    dataHolder.set(LtexMarkdownExtension.DISPLAY_MATH_PARSER, this.displayMathParser)
    dataHolder.set(LtexMarkdownExtension.INLINE_MATH_PARSER, this.inlineMathParser)
    return dataHolder
  }
}
