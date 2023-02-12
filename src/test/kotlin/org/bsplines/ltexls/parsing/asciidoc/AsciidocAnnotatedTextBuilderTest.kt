/* Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.asciidoc

import org.bsplines.ltexls.parsing.CodeAnnotatedTextBuilderTest
import org.junit.platform.suite.api.IncludeEngines
import kotlin.test.Test

@IncludeEngines("junit-jupiter")
class AsciidocAnnotatedTextBuilderTest : CodeAnnotatedTextBuilderTest("asciidoc") {
  @Test
  fun testParagraphs() {
    assertPlainText(
      """
      Paragraph 1, line 1
      Paragraph 1, line 2

      Paragraph 2, line 1
      Paragraph 2, line 2

      """.trimIndent(),
      "Paragraph 1, line 1\nParagraph 1, line 2\n\nParagraph 2, line 1\nParagraph 2, line 2\n"
    )
    assertPlainText(
      "  Literal paragraph, line 1\n  Literal paragraph, line 2\n",
      "Literal paragraph, line 1\nLiteral paragraph, line 2\n"
    )
    assertPlainText(
      """
      Line 1 +
      Line 2

      """.trimIndent(),
      "Line 1\nLine 2\n"
    )
    assertPlainText(
      """
      [%hardbreaks]
      Line 1
      Line 2

      """.trimIndent(),
      "\nLine 1\nLine 2\n"
    )
    assertPlainText(
      """
      [.lead]
      Line 1
      Line 2

      """.trimIndent(),
      "\nLine 1\nLine 2\n"
    )
  }

  @Test
  fun testLinks() {
    assertPlainText(
      "https://bsplines.org, <https://bsplines.org>\n",
      "https://bsplines.org, https://bsplines.org\n"
    )
    assertPlainText(
      "https://bsplines.org[abc], https://bsplines.org[abc^,123]\n",
      "abc, abc\n"
    )
    assertPlainText(
      "abc@example.com, mailto:abc@example.com[test], mailto:abc@example.com[test,123]\n",
      "abc@example.com, test, test\n"
    )
    assertPlainText(
      "link:index.html[abc]\n",
      "abc\n"
    )
  }

  @Test
  fun testCrossReferences() {
    assertPlainText(
      "<<Test>>, <<test 123>>, <<test>>, <<test,123>>\n",
      "Test, test 123, Dummy0, 123\n"
    )
  }

  @Test
  fun testAnchors() {
    assertPlainText(
      "[[test]], [#test]\n",
      ", \n"
    )
  }

  @Test
  fun testDocumentHeader() {
    assertPlainText(
      """
      = Document Title
      Foo Bar <foobar@example.com>
      v1.0, 2001-02-03
      :toc:
      :homepage: https://bsplines.org
      :description: This is the description.

      This is the body.

      """.trimIndent(),
      "Document Title\nFoo Bar <foobar@example.com>\nv1.0, 2001-02-03\n\nhttps://bsplines.org\n"
      + "This is the description.\n\nThis is the body.\n"
    )
  }

  @Test
  fun testSectionTitles() {
    assertPlainText(
      """
      = Section Title 1

      == Section Title 2

      === Section Title 3

      """.trimIndent(),
      "Section Title 1\n\nSection Title 2\n\nSection Title 3\n"
    )
  }

  @Test
  fun testIncludes() {
    assertPlainText(
      """
      This is a test.

      include::index.html

      This is another test.

      """.trimIndent(),
      "This is a test.\n\n\n\nThis is another test.\n"
    )
  }

  @Test
  fun testLists() {
    assertPlainText(
      """
      .Title
      * Test 1
      ** Test 2
      *** Test 3

      """.trimIndent(),
      "Title\nTest 1\nTest 2\nTest 3\n"
    )
    assertPlainText(
      """
      .Title
      - Test 1
      - Test 2
      - Test 3

      """.trimIndent(),
      "Title\nTest 1\nTest 2\nTest 3\n"
    )
    assertPlainText(
      """
      .Title
      . Test 1
      .. Test 2
      ... Test 3

      """.trimIndent(),
      "Title\nTest 1\nTest 2\nTest 3\n"
    )
    assertPlainText(
      """
      .Title
      1. Test 1
      2. Test 2
      3. Test 3

      """.trimIndent(),
      "Title\nTest 1\nTest 2\nTest 3\n"
    )
  }

  @Test
  fun testDescriptionLists() {
    assertPlainText(
      """
      Test 1:: Foo
      Test 2::   Bar

      """.trimIndent(),
      "Test 1: Foo\nTest 2: Bar\n"
    )
    assertPlainText(
      """
      Test 1::
      - Foo
      - Bar
      Test 2::
      * Foobar

      """.trimIndent(),
      "Test 1:\nFoo\nBar\nTest 2:\nFoobar\n"
    )
  }

  @Test
  fun testImages() {
    assertPlainText(
      """
      Test 1

      image::https://example.com[foo]

      Test 2

      """.trimIndent(),
      "Test 1\n\n\n\nTest 2\n"
    )
    assertPlainText(
      """
      This is image:foo.png[] the first sentence.
      This is image:foo.png[title="bar"] the second sentence.

      """.trimIndent(),
      "This is Dummy0 the first sentence.\nThis is Dummy1 the second sentence.\n"
    )
  }

  @Test
  fun testVideos() {
    assertPlainText(
      """
      Test 1

      video::https://example.com[foo]

      Test 2

      """.trimIndent(),
      "Test 1\n\n\n\nTest 2\n"
    )
  }

  @Test
  fun testKeyboardButtonAndMenuMacros() {
    assertPlainText(
      "kbd:[F42], btn:[OK], menu:File[Open]\n",
      "Dummy0, Dummy1, Dummy2\n"
    )
  }

  @Test
  fun testTextFormatting() {
    assertPlainText(
      """
      This is *bold*. This is _italic_ text. This is `monospace`.
      This is a *_combination_*, and another `*_combination_*`.

      """.trimIndent(),
      "This is bold. This is italic text. This is Dummy0.\n"
      + "This is a combination, and another Dummy1.\n"
    )
    assertPlainText(
      """
      This is **bo**ld. This is __ita__lic text. This is mono``space``.
      This is a com**__bi__**nation, and other ``**__combination__**``s.

      """.trimIndent(),
      "This is bold. This is italic text. This is monoDummy0.\n"
      + "This is a combination, and other Dummy1s.\n"
    )
    assertPlainText(
      "This is #highlight#, high##light##, and [.abc]#highlight#.\n",
      "This is highlight, highlight, and highlight.\n"
    )
    assertPlainText(
      "This is ^super^script and sub~script~.\n",
      "This is superscript and subscript.\n"
    )
  }

  @Test
  fun testTextReplacement() {
    assertPlainText(
      """
      These are '`single`' and "`double`" curved quotes. This is LTeX's problem.
      This is ``list```'s size, a curved`' apostrophe, and a non-curved\'s apostrophe.

      """.trimIndent(),
      "These are \u2018single\u2019 and \u201cdouble\u201d curved quotes. "
      + "This is LTeX\u2019s problem.\n"
      + "This is Dummy0\u2019s size, a curved\u2019 apostrophe, and a non-curved's apostrophe.\n"
    )
    assertPlainText(
      """
      (C), (R), (TM), abc--def, abc--
      def, abc -- def, abc-- def, ..., ->, =>, <-, <=.

      """.trimIndent(),
      "\u00a9, \u00ae, \u2122, abc\u2014def, abc\u2014\n"
      + "def, abc\u2009\u2014\u2009def, abc-- def, \u2026, \u2192, \u21d2, \u2190, \u21d0.\n"
    )
    assertPlainText(
      "&amp;, &auml;, &ldquo;, &#8220;, &#x201c;.\n",
      "&, \u00e4, \u201c, \u201c, \u201c.\n"
    )
  }
}
