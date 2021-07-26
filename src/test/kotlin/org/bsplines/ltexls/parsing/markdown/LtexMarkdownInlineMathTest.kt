/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.markdown

import com.vladsch.flexmark.util.sequence.BasedSequence
import org.junit.platform.suite.api.IncludeEngines
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@IncludeEngines("junit-jupiter")
class LtexMarkdownInlineMathTest {
  @Test
  fun testConstructors() {
    var node = LtexMarkdownInlineMath()
    assertTrue(node.openingMarker.isNull)
    assertTrue(node.text.isNull)
    assertTrue(node.closingMarker.isNull)

    node = LtexMarkdownInlineMath(BasedSequence.NULL)
    assertTrue(node.openingMarker.isNull)
    assertTrue(node.text.isNull)
    assertTrue(node.closingMarker.isNull)

    node = LtexMarkdownInlineMath(
      BasedSequence.of("$"),
      BasedSequence.of("E = mc^2"),
      BasedSequence.of("$"),
    )
    assertEquals(BasedSequence.of("$"), node.openingMarker)
    assertEquals(BasedSequence.of("E = mc^2"), node.text)
    assertEquals(BasedSequence.of("$"), node.closingMarker)
  }

  @Test
  fun testGetSegments() {
    val node = LtexMarkdownInlineMath(
      BasedSequence.of("$"),
      BasedSequence.of("E = mc^2"),
      BasedSequence.of("$"),
    )

    assertEquals(
      listOf(
        BasedSequence.of("$"),
        BasedSequence.of("E = mc^2"),
        BasedSequence.of("$"),
      ),
      node.segments.asList(),
    )
  }

  @Test
  fun testGetAstExtra() {
    val node = LtexMarkdownInlineMath(
      BasedSequence.of("$"),
      BasedSequence.of("E = mc^2"),
      BasedSequence.of("$"),
    )
    val stringBuilder = StringBuilder()
    node.getAstExtra(stringBuilder)
    assertEquals(
      " textOpen:[0, 1, \"$\"] text:[0, 8, \"E = mc^2\"] textClose:[0, 1, \"$\"]",
      stringBuilder.toString()
    )
  }

  @Test
  fun testProperties() {
    var node = LtexMarkdownInlineMath()
    node.openingMarker = BasedSequence.of("$")
    assertEquals(BasedSequence.of("$"), node.openingMarker)

    node = LtexMarkdownInlineMath()
    node.text = BasedSequence.of("E = mc^2")
    assertEquals(BasedSequence.of("E = mc^2"), node.text)

    node = LtexMarkdownInlineMath()
    node.closingMarker = BasedSequence.of("$")
    assertEquals(BasedSequence.of("$"), node.closingMarker)
  }
}
