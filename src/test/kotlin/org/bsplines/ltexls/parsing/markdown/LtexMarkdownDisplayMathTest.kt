/* Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.markdown

import com.vladsch.flexmark.ast.Paragraph
import com.vladsch.flexmark.util.ast.BlockContent
import com.vladsch.flexmark.util.sequence.BasedSequence
import org.junit.platform.suite.api.IncludeEngines
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@IncludeEngines("junit-jupiter")
class LtexMarkdownDisplayMathTest {
  @Test
  fun testConstructors() {
    var node = LtexMarkdownDisplayMath()
    assertTrue(node.openingMarker.isNull)
    assertTrue(node.openingTrailing.isNull)
    assertTrue(node.closingMarker.isNull)
    assertTrue(node.closingTrailing.isNull)

    node = LtexMarkdownDisplayMath(BasedSequence.NULL)
    assertTrue(node.openingMarker.isNull)
    assertTrue(node.openingTrailing.isNull)
    assertTrue(node.closingMarker.isNull)
    assertTrue(node.closingTrailing.isNull)

    node = LtexMarkdownDisplayMath(BasedSequence.NULL, emptyList())
    assertTrue(node.openingMarker.isNull)
    assertTrue(node.openingTrailing.isNull)
    assertTrue(node.closingMarker.isNull)
    assertTrue(node.closingTrailing.isNull)

    node = LtexMarkdownDisplayMath(BlockContent())
    assertTrue(node.openingMarker.isNull)
    assertTrue(node.openingTrailing.isNull)
    assertTrue(node.closingMarker.isNull)
    assertTrue(node.closingTrailing.isNull)
  }

  @Test
  fun testGetSegments() {
    val node = LtexMarkdownDisplayMath()

    node.openingMarker = BasedSequence.of("$$")
    node.openingTrailing = BasedSequence.of("abc")
    node.closingMarker = BasedSequence.of("$$")
    node.closingTrailing = BasedSequence.of("def")

    assertEquals(
      listOf(
        BasedSequence.of("$$"),
        BasedSequence.of("abc"),
        BasedSequence.of("$$"),
        BasedSequence.of("def"),
      ),
      node.segments.asList(),
    )
  }

  @Test
  fun testGetAstExtra() {
    val node = LtexMarkdownDisplayMath()
    node.openingMarker = BasedSequence.of("$$")
    node.openingTrailing = BasedSequence.of("abc")
    node.closingMarker = BasedSequence.of("$$")
    node.closingTrailing = BasedSequence.of("def")

    val stringBuilder = StringBuilder()
    node.getAstExtra(stringBuilder)

    assertEquals(
      " open:[0, 2, \"$$\"] openTrail:[0, 3, \"abc\"] "
          + "close:[0, 2, \"$$\"] closeTrail:[0, 3, \"def\"]",
      stringBuilder.toString()
    )
  }

  @Test
  fun testIsParagraphEndWrappingDisabled() {
    val node = LtexMarkdownDisplayMath()
    val paragraph = Paragraph()
    assertFalse(node.isParagraphEndWrappingDisabled(paragraph))
  }

  @Test
  fun testIsParagraphStartWrappingDisabled() {
    val node = LtexMarkdownDisplayMath()
    val paragraph = Paragraph()
    assertFalse(node.isParagraphStartWrappingDisabled(paragraph))
  }

  @Test
  fun testProperties() {
    var node = LtexMarkdownDisplayMath()
    node.openingMarker = BasedSequence.of("$$")
    assertEquals(BasedSequence.of("$$"), node.openingMarker)

    node = LtexMarkdownDisplayMath()
    node.openingTrailing = BasedSequence.of("abc")
    assertEquals(BasedSequence.of("abc"), node.openingTrailing)

    node = LtexMarkdownDisplayMath()
    node.closingMarker = BasedSequence.of("$$")
    assertEquals(BasedSequence.of("$$"), node.closingMarker)

    node = LtexMarkdownDisplayMath()
    node.closingTrailing = BasedSequence.of("def")
    assertEquals(BasedSequence.of("def"), node.closingTrailing)
  }
}
