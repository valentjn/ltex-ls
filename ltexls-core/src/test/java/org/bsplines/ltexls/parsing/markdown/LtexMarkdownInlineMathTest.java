/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.markdown;

import com.vladsch.flexmark.util.sequence.BasedSequence;
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LtexMarkdownInlineMathTest {
  @Test
  public void testConstructors() {
    LtexMarkdownInlineMath node = new LtexMarkdownInlineMath();
    Assertions.assertTrue(node.getOpeningMarker().isNull());
    Assertions.assertTrue(node.getText().isNull());
    Assertions.assertTrue(node.getClosingMarker().isNull());

    node = new LtexMarkdownInlineMath(BasedSequence.NULL);
    Assertions.assertTrue(node.getOpeningMarker().isNull());
    Assertions.assertTrue(node.getText().isNull());
    Assertions.assertTrue(node.getClosingMarker().isNull());

    node = new LtexMarkdownInlineMath(
        BasedSequence.of("$"), BasedSequence.of("E = mc^2"), BasedSequence.of("$"));
    Assertions.assertEquals(BasedSequence.of("$"), node.getOpeningMarker());
    Assertions.assertEquals(BasedSequence.of("E = mc^2"), node.getText());
    Assertions.assertEquals(BasedSequence.of("$"), node.getClosingMarker());
  }

  @Test
  public void testGetSegments() {
    LtexMarkdownInlineMath node = new LtexMarkdownInlineMath(
        BasedSequence.of("$"), BasedSequence.of("E = mc^2"), BasedSequence.of("$"));
    Assertions.assertEquals(Arrays.asList(BasedSequence.of("$"),
          BasedSequence.of("E = mc^2"), BasedSequence.of("$")),
        Arrays.asList(node.getSegments()));
  }

  @Test
  public void testGetAstExtra() {
    LtexMarkdownInlineMath node = new LtexMarkdownInlineMath(
        BasedSequence.of("$"), BasedSequence.of("E = mc^2"), BasedSequence.of("$"));
    StringBuilder stringBuilder = new StringBuilder();
    node.getAstExtra(stringBuilder);
    Assertions.assertEquals(
        " textOpen:[0, 1, \"$\"] text:[0, 8, \"E = mc^2\"] textClose:[0, 1, \"$\"]",
        stringBuilder.toString());
  }

  @Test
  public void testProperties() {
    LtexMarkdownInlineMath node = new LtexMarkdownInlineMath();
    node.setOpeningMarker(BasedSequence.of("$"));
    Assertions.assertEquals(BasedSequence.of("$"), node.getOpeningMarker());

    node = new LtexMarkdownInlineMath();
    node.setText(BasedSequence.of("E = mc^2"));
    Assertions.assertEquals(BasedSequence.of("E = mc^2"), node.getText());

    node = new LtexMarkdownInlineMath();
    node.setClosingMarker(BasedSequence.of("$"));
    Assertions.assertEquals(BasedSequence.of("$"), node.getClosingMarker());
  }
}
