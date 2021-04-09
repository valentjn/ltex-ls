/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.restructuredtext;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RestructuredtextBlockSignatureTest {
  @Test
  public void testProperties() {
    RestructuredtextBlockSignature nodeSignature = new RestructuredtextBlockSignature(
        RestructuredtextBlockSignature.Type.COMMENT);
    Assertions.assertEquals(RestructuredtextBlockSignature.Type.COMMENT, nodeSignature.getType());
    Assertions.assertEquals(RestructuredtextBlockSignature.Action.IGNORE,
        nodeSignature.getAction());

    nodeSignature = new RestructuredtextBlockSignature(
        RestructuredtextBlockSignature.Type.COMMENT, RestructuredtextBlockSignature.Action.DEFAULT);
    Assertions.assertEquals(RestructuredtextBlockSignature.Type.COMMENT, nodeSignature.getType());
    Assertions.assertEquals(RestructuredtextBlockSignature.Action.DEFAULT,
        nodeSignature.getAction());
  }

  @Test
  public void testEquals() {
    RestructuredtextBlockSignature nodeSignature = new RestructuredtextBlockSignature(
        RestructuredtextBlockSignature.Type.COMMENT);
    Assertions.assertDoesNotThrow(() -> nodeSignature.hashCode());
    Assertions.assertFalse(nodeSignature.equals(null));

    Assertions.assertEquals(nodeSignature, new RestructuredtextBlockSignature(
        RestructuredtextBlockSignature.Type.COMMENT));
    Assertions.assertNotEquals(nodeSignature, new RestructuredtextBlockSignature(
        RestructuredtextBlockSignature.Type.GRID_TABLE));

    Assertions.assertEquals(nodeSignature,
        new RestructuredtextBlockSignature(RestructuredtextBlockSignature.Type.COMMENT,
          RestructuredtextBlockSignature.Action.IGNORE));
    Assertions.assertNotEquals(nodeSignature,
        new RestructuredtextBlockSignature(RestructuredtextBlockSignature.Type.COMMENT,
          RestructuredtextBlockSignature.Action.DEFAULT));
  }
}
