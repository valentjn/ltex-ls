/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.markdown;

import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.ast.ParagraphContainer;
import com.vladsch.flexmark.util.ast.Block;
import com.vladsch.flexmark.util.ast.BlockContent;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class LtexMarkdownDisplayMath extends Block implements ParagraphContainer {
  private BasedSequence openingMarker = BasedSequence.NULL;
  private BasedSequence openingTrailing = BasedSequence.NULL;
  private BasedSequence closingMarker = BasedSequence.NULL;
  private BasedSequence closingTrailing = BasedSequence.NULL;

  @Override
  public void getAstExtra(@NotNull StringBuilder out) {
    segmentSpanChars(out, this.openingMarker, "open");
    segmentSpanChars(out, this.openingTrailing, "openTrail");
    segmentSpanChars(out, this.closingMarker, "close");
    segmentSpanChars(out, this.closingTrailing, "closeTrail");
  }

  @Override
  public @NotNull BasedSequence[] getSegments() {
    return new BasedSequence[] {
      this.openingMarker,
      this.openingTrailing,
      this.closingMarker,
      this.closingTrailing
    };
  }

  @Override
  public boolean isParagraphEndWrappingDisabled(Paragraph node) {
    return ((node == getLastChild()) || (node.getNext() instanceof LtexMarkdownDisplayMath));
  }

  @Override
  public boolean isParagraphStartWrappingDisabled(Paragraph node) {
    return ((node == getFirstChild()) || (node.getPrevious() instanceof LtexMarkdownDisplayMath));
  }

  public LtexMarkdownDisplayMath() {
  }

  public LtexMarkdownDisplayMath(BasedSequence chars) {
    super(chars);
  }

  public LtexMarkdownDisplayMath(BasedSequence chars, List<BasedSequence> segments) {
    super(chars, segments);
  }

  public LtexMarkdownDisplayMath(BlockContent blockContent) {
    super(blockContent);
  }

  public BasedSequence getOpeningMarker() {
    return this.openingMarker;
  }

  public void setOpeningMarker(BasedSequence openingMarker) {
    this.openingMarker = openingMarker;
  }

  public BasedSequence getClosingMarker() {
    return this.closingMarker;
  }

  public void setClosingMarker(BasedSequence closingMarker) {
    this.closingMarker = closingMarker;
  }

  public BasedSequence getOpeningTrailing() {
    return this.openingTrailing;
  }

  public void setOpeningTrailing(BasedSequence openingTrailing) {
    this.openingTrailing = openingTrailing;
  }

  public BasedSequence getClosingTrailing() {
    return this.closingTrailing;
  }

  public void setClosingTrailing(BasedSequence closingTrailing) {
    this.closingTrailing = closingTrailing;
  }
}
