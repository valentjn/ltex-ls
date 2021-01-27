/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.markdown;

import com.vladsch.flexmark.util.ast.DelimitedNode;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.jetbrains.annotations.NotNull;

public class LtexMarkdownInlineMath extends Node implements DelimitedNode {
  protected BasedSequence openingMarker = BasedSequence.NULL;
  protected BasedSequence text = BasedSequence.NULL;
  protected BasedSequence closingMarker = BasedSequence.NULL;

  @Override
  public @NotNull BasedSequence[] getSegments() {
    return new BasedSequence[] { this.openingMarker, this.text, this.closingMarker };
  }

  @Override
  public void getAstExtra(@NotNull StringBuilder out) {
    delimitedSegmentSpanChars(out, this.openingMarker, this.text, this.closingMarker, "text");
  }

  public LtexMarkdownInlineMath() {
  }

  public LtexMarkdownInlineMath(BasedSequence chars) {
    super(chars);
  }

  public LtexMarkdownInlineMath(BasedSequence openingMarker, BasedSequence text,
        BasedSequence closingMarker) {
    super(openingMarker.baseSubSequence(
        openingMarker.getStartOffset(), closingMarker.getEndOffset()));
    this.openingMarker = openingMarker;
    this.text = text;
    this.closingMarker = closingMarker;
  }

  public BasedSequence getOpeningMarker() {
    return this.openingMarker;
  }

  public void setOpeningMarker(BasedSequence openingMarker) {
    this.openingMarker = openingMarker;
  }

  public BasedSequence getText() {
    return this.text;
  }

  public void setText(BasedSequence text) {
    this.text = text;
  }

  public BasedSequence getClosingMarker() {
    return this.closingMarker;
  }

  public void setClosingMarker(BasedSequence closingMarker) {
    this.closingMarker = closingMarker;
  }
}
