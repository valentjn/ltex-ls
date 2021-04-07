/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.markdown;

import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.util.ast.BlockContent;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LtexMarkdownDisplayMathTest {
  @Test
  public void testConstructors() {
    LtexMarkdownDisplayMath node = new LtexMarkdownDisplayMath();
    Assertions.assertTrue(node.getOpeningMarker().isNull());
    Assertions.assertTrue(node.getOpeningTrailing().isNull());
    Assertions.assertTrue(node.getClosingMarker().isNull());
    Assertions.assertTrue(node.getClosingTrailing().isNull());

    node = new LtexMarkdownDisplayMath(BasedSequence.NULL);
    Assertions.assertTrue(node.getOpeningMarker().isNull());
    Assertions.assertTrue(node.getOpeningTrailing().isNull());
    Assertions.assertTrue(node.getClosingMarker().isNull());
    Assertions.assertTrue(node.getClosingTrailing().isNull());

    node = new LtexMarkdownDisplayMath(BasedSequence.NULL, Collections.emptyList());
    Assertions.assertTrue(node.getOpeningMarker().isNull());
    Assertions.assertTrue(node.getOpeningTrailing().isNull());
    Assertions.assertTrue(node.getClosingMarker().isNull());
    Assertions.assertTrue(node.getClosingTrailing().isNull());

    node = new LtexMarkdownDisplayMath(new BlockContent());
    Assertions.assertTrue(node.getOpeningMarker().isNull());
    Assertions.assertTrue(node.getOpeningTrailing().isNull());
    Assertions.assertTrue(node.getClosingMarker().isNull());
    Assertions.assertTrue(node.getClosingTrailing().isNull());
  }

  @Test
  public void testGetSegments() {
    LtexMarkdownDisplayMath node = new LtexMarkdownDisplayMath();
    node.setOpeningMarker(BasedSequence.of("$$"));
    node.setOpeningTrailing(BasedSequence.of("abc"));
    node.setClosingMarker(BasedSequence.of("$$"));
    node.setClosingTrailing(BasedSequence.of("def"));
    Assertions.assertEquals(Arrays.asList(BasedSequence.of("$$"), BasedSequence.of("abc"),
          BasedSequence.of("$$"), BasedSequence.of("def")),
        Arrays.asList(node.getSegments()));
  }

  @Test
  public void testGetAstExtra() {
    LtexMarkdownDisplayMath node = new LtexMarkdownDisplayMath();
    node.setOpeningMarker(BasedSequence.of("$$"));
    node.setOpeningTrailing(BasedSequence.of("abc"));
    node.setClosingMarker(BasedSequence.of("$$"));
    node.setClosingTrailing(BasedSequence.of("def"));

    StringBuilder stringBuilder = new StringBuilder();
    node.getAstExtra(stringBuilder);

    Assertions.assertEquals(" open:[0, 2, \"$$\"] openTrail:[0, 3, \"abc\"] "
        + "close:[0, 2, \"$$\"] closeTrail:[0, 3, \"def\"]",
        stringBuilder.toString());
  }

  @Test
  public void testIsParagraphEndWrappingDisabled() {
    LtexMarkdownDisplayMath node = new LtexMarkdownDisplayMath();
    Paragraph paragraph = new Paragraph();
    Assertions.assertFalse(node.isParagraphEndWrappingDisabled(paragraph));
  }

  @Test
  public void testIsParagraphStartWrappingDisabled() {
    LtexMarkdownDisplayMath node = new LtexMarkdownDisplayMath();
    Paragraph paragraph = new Paragraph();
    Assertions.assertFalse(node.isParagraphStartWrappingDisabled(paragraph));
  }

  @Test
  public void testProperties() {
    LtexMarkdownDisplayMath node = new LtexMarkdownDisplayMath();
    node.setOpeningMarker(BasedSequence.of("$$"));
    Assertions.assertEquals(BasedSequence.of("$$"), node.getOpeningMarker());

    node = new LtexMarkdownDisplayMath();
    node.setOpeningTrailing(BasedSequence.of("abc"));
    Assertions.assertEquals(BasedSequence.of("abc"), node.getOpeningTrailing());

    node = new LtexMarkdownDisplayMath();
    node.setClosingMarker(BasedSequence.of("$$"));
    Assertions.assertEquals(BasedSequence.of("$$"), node.getClosingMarker());

    node = new LtexMarkdownDisplayMath();
    node.setClosingTrailing(BasedSequence.of("def"));
    Assertions.assertEquals(BasedSequence.of("def"), node.getClosingTrailing());
  }
}
