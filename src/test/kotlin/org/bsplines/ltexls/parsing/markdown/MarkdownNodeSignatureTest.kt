/* Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.markdown

import org.bsplines.ltexls.parsing.DummyGenerator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class MarkdownNodeSignatureTest {
  @Test
  fun testProperties() {
    var nodeSignature = MarkdownNodeSignature("abc")
    assertEquals("abc", nodeSignature.name)
    assertEquals(MarkdownNodeSignature.Action.Ignore, nodeSignature.action)
    assertFalse(nodeSignature.dummyGenerator.plural)

    nodeSignature = MarkdownNodeSignature("abc", MarkdownNodeSignature.Action.Dummy)
    assertEquals("abc", nodeSignature.name)
    assertEquals(MarkdownNodeSignature.Action.Dummy, nodeSignature.action)
    assertFalse(nodeSignature.dummyGenerator.plural)

    nodeSignature = MarkdownNodeSignature(
      "abc",
      MarkdownNodeSignature.Action.Dummy,
      DummyGenerator.getInstance(plural = true),
    )
    assertEquals("abc", nodeSignature.name)
    assertEquals(MarkdownNodeSignature.Action.Dummy, nodeSignature.action)
    assertTrue(nodeSignature.dummyGenerator.plural)
  }

  @Test
  fun testEquals() {
    val nodeSignature = MarkdownNodeSignature("abc")

    assertEquals(nodeSignature, MarkdownNodeSignature("abc"))
    assertNotEquals(nodeSignature, MarkdownNodeSignature("def"))

    assertEquals(nodeSignature, MarkdownNodeSignature("abc", MarkdownNodeSignature.Action.Ignore))
    assertNotEquals(nodeSignature, MarkdownNodeSignature("abc", MarkdownNodeSignature.Action.Dummy))

    assertEquals(nodeSignature, MarkdownNodeSignature("abc", MarkdownNodeSignature.Action.Ignore))
    assertNotEquals(
      nodeSignature,
      MarkdownNodeSignature(
        "abc",
        MarkdownNodeSignature.Action.Ignore,
        DummyGenerator.getInstance(plural = true),
      ),
    )
  }
}
