/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.markdown;

import org.bsplines.ltexls.parsing.DummyGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MarkdownNodeSignatureTest {
  @Test
  public void testProperties() {
    MarkdownNodeSignature nodeSignature = new MarkdownNodeSignature("abc");
    Assertions.assertEquals("abc", nodeSignature.getName());
    Assertions.assertEquals(MarkdownNodeSignature.Action.IGNORE, nodeSignature.getAction());
    Assertions.assertFalse(nodeSignature.getDummyGenerator().isPlural());

    nodeSignature = new MarkdownNodeSignature("abc", MarkdownNodeSignature.Action.DUMMY);
    Assertions.assertEquals("abc", nodeSignature.getName());
    Assertions.assertEquals(MarkdownNodeSignature.Action.DUMMY, nodeSignature.getAction());
    Assertions.assertFalse(nodeSignature.getDummyGenerator().isPlural());

    nodeSignature = new MarkdownNodeSignature("abc", MarkdownNodeSignature.Action.DUMMY,
        DummyGenerator.getDefault(true));
    Assertions.assertEquals("abc", nodeSignature.getName());
    Assertions.assertEquals(MarkdownNodeSignature.Action.DUMMY, nodeSignature.getAction());
    Assertions.assertTrue(nodeSignature.getDummyGenerator().isPlural());
  }

  @Test
  public void testEquals() {
    MarkdownNodeSignature nodeSignature = new MarkdownNodeSignature("abc");
    Assertions.assertDoesNotThrow(() -> nodeSignature.hashCode());
    Assertions.assertFalse(nodeSignature.equals(null));

    Assertions.assertEquals(nodeSignature, new MarkdownNodeSignature("abc"));
    Assertions.assertNotEquals(nodeSignature, new MarkdownNodeSignature("def"));

    Assertions.assertEquals(nodeSignature,
        new MarkdownNodeSignature("abc", MarkdownNodeSignature.Action.IGNORE));
    Assertions.assertNotEquals(nodeSignature,
        new MarkdownNodeSignature("abc", MarkdownNodeSignature.Action.DUMMY));

    Assertions.assertEquals(nodeSignature,
        new MarkdownNodeSignature("abc", MarkdownNodeSignature.Action.IGNORE));
    Assertions.assertNotEquals(nodeSignature,
        new MarkdownNodeSignature("abc", MarkdownNodeSignature.Action.IGNORE,
          DummyGenerator.getDefault(true)));
  }
}
