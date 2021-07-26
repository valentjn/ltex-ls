/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.markdown

import com.vladsch.flexmark.ast.Paragraph
import com.vladsch.flexmark.ast.ParagraphContainer
import com.vladsch.flexmark.util.ast.Block
import com.vladsch.flexmark.util.ast.BlockContent
import com.vladsch.flexmark.util.sequence.BasedSequence

class LtexMarkdownDisplayMath : Block, ParagraphContainer {
  var openingMarker: BasedSequence = BasedSequence.NULL
  var openingTrailing: BasedSequence = BasedSequence.NULL
  var closingMarker: BasedSequence = BasedSequence.NULL
  var closingTrailing: BasedSequence = BasedSequence.NULL

  constructor()

  @Suppress("unused")
  constructor(chars: BasedSequence) : super(chars)

  @Suppress("unused")
  constructor(chars: BasedSequence, segments: List<BasedSequence>) : super(chars, segments)

  @Suppress("unused")
  constructor(blockContent: BlockContent) : super(blockContent)

  override fun getSegments(): Array<BasedSequence> {
    return arrayOf(
      this.openingMarker,
      this.openingTrailing,
      this.closingMarker,
      this.closingTrailing,
    )
  }

  override fun getAstExtra(out: StringBuilder) {
    segmentSpanChars(out, this.openingMarker, "open")
    segmentSpanChars(out, this.openingTrailing, "openTrail")
    segmentSpanChars(out, this.closingMarker, "close")
    segmentSpanChars(out, this.closingTrailing, "closeTrail")
  }

  override fun isParagraphEndWrappingDisabled(node: Paragraph): Boolean {
    return (node === lastChild) || (node.next is LtexMarkdownDisplayMath)
  }

  override fun isParagraphStartWrappingDisabled(node: Paragraph): Boolean {
    return (node === firstChild) || (node.previous is LtexMarkdownDisplayMath)
  }
}
