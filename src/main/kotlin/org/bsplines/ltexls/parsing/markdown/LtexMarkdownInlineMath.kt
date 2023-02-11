/* Copyright (C) 2019-2023 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.markdown

import com.vladsch.flexmark.util.ast.DelimitedNode
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.sequence.BasedSequence

class LtexMarkdownInlineMath : DelimitedNode, Node {
  private var openingMarker: BasedSequence = BasedSequence.NULL
  private var text: BasedSequence = BasedSequence.NULL
  private var closingMarker: BasedSequence = BasedSequence.NULL

  @Suppress("unused")
  constructor()

  @Suppress("unused")
  constructor(chars: BasedSequence) : super(chars)

  constructor(
    openingMarker: BasedSequence,
    text: BasedSequence,
    closingMarker: BasedSequence,
  ) : super(openingMarker.baseSubSequence(openingMarker.startOffset, closingMarker.endOffset)) {
    this.openingMarker = openingMarker
    this.text = text
    this.closingMarker = closingMarker
  }

  override fun getSegments(): Array<BasedSequence> {
    return arrayOf(this.openingMarker, this.text, this.closingMarker)
  }

  override fun getAstExtra(out: StringBuilder) {
    delimitedSegmentSpanChars(out, this.openingMarker, this.text, this.closingMarker, "text")
  }

  override fun getOpeningMarker(): BasedSequence {
    return this.openingMarker
  }

  override fun setOpeningMarker(openingMarker: BasedSequence) {
    this.openingMarker = openingMarker
  }

  override fun getText(): BasedSequence {
    return this.text
  }

  override fun setText(text: BasedSequence) {
    this.text = text
  }

  override fun getClosingMarker(): BasedSequence {
    return this.closingMarker
  }

  override fun setClosingMarker(closingMarker: BasedSequence) {
    this.closingMarker = closingMarker
  }
}
