/* Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.org

import org.bsplines.ltexls.parsing.CodeAnnotatedTextBuilderTest
import org.junit.platform.suite.api.IncludeEngines
import kotlin.test.Test

@IncludeEngines("junit-jupiter")
class OrgAnnotatedTextBuilderTest : CodeAnnotatedTextBuilderTest("org") {
  @Test
  fun testHeadlinesAndSections() {
    assertPlainText(
      "* \n\n" +
      """
      ** DONE

      *** This is a test

      **** TODO [#A] COMMENT Another test :tag:a2%:

      **** TODO [#A] Final test :tag:a2%:

      """.trimIndent(),
      "\n\n\n\n\n\n\n\n\nThis is a test\n\n\n\n\n\n\nFinal test\n\n"
    )
  }

  @Test
  fun testAffiliatedKeywords() {
    assertPlainText(
      """
      This is a test.

      #+HEADER: test
      #+CAPTION[abc]: def
      Second sentence.

        #+ATTR_foo01_bar: BOOM
      Final sentence.

      """.trimIndent(),
      "This is a test.\n\n\n\nSecond sentence.\n\n\nFinal sentence.\n"
    )
  }

  @Test
  fun testCenterBlocks() {
    assertPlainText(
      """
      This is a test.
      #+BEGIN_CENTER
      Contents.
      #+END_CENTER
      This is another test.

      """.trimIndent(),
      "This is a test.\n\nContents.\n\nThis is another test.\n"
    )
  }

  @Test
  fun testQuoteBlocks() {
    assertPlainText(
      """
      This is a test.
      #+BEGIN_QUOTE
      Contents.
      #+END_QUOTE
      This is another test.

      """.trimIndent(),
      "This is a test.\n\nContents.\n\nThis is another test.\n"
    )
  }

  @Test
  fun testCustomBlocks() {
    assertPlainText(
      """
      This is a test.
      #+BEGIN_FOOBAR
      Contents.
      #+END_FOOBAR
      This is another test.

      """.trimIndent(),
      "This is a test.\n\nContents.\n\nThis is another test.\n"
    )
  }

  @Test
  fun testCommentBlocks() {
    assertPlainText(
      """
      This is a test.
      #+BEGIN_COMMENT
      Contents.
      #+END_COMMENT
      This is another test.

      """.trimIndent(),
      "This is a test.\n\nThis is another test.\n"
    )
  }

  @Test
  fun testExampleBlocks() {
    assertPlainText(
      """
      This is a test.
      #+BEGIN_EXAMPLE
      Contents.
      #+END_EXAMPLE
      This is another test.

      """.trimIndent(),
      "This is a test.\n\nThis is another test.\n"
    )
  }

  @Test
  fun testExportBlocks() {
    assertPlainText(
      """
      This is a test.
      #+BEGIN_EXPORT
      Contents.
      #+END_EXPORT
      This is another test.

      """.trimIndent(),
      "This is a test.\n\nThis is another test.\n"
    )
  }

  @Test
  fun testSourceBlocks() {
    assertPlainText(
      """
      This is a test.
      #+BEGIN_SRC
      Contents.
      #+END_SRC
      This is another test.

      """.trimIndent(),
      "This is a test.\n\nThis is another test.\n"
    )
  }

  @Test
  fun testVerseBlocks() {
    assertPlainText(
      """
      This is a test.
      #+BEGIN_VERSE
      Contents.
      #+END_VERSE
      This is another test.

      """.trimIndent(),
      "This is a test.\n\nContents.\n\nThis is another test.\n"
    )
  }

  @Test
  fun testDrawers() {
    assertPlainText(
      """
      This is a test.
      :TEST_DRAWER:
      Contents.
      :END:
      This is another test.

      """.trimIndent(),
      "This is a test.\n\nContents.\n\nThis is another test.\n"
    )
    assertPlainText(
      """
      This is a test.
      :PROPERTIES:
      Contents.
      :END:
      This is another test.

      """.trimIndent(),
      "This is a test.\n\nThis is another test.\n"
    )
  }

  @Test
  fun testDynamicBlocks() {
    assertPlainText(
      """
      This is a test.
      #+BEGIN: test-block :abc
      Contents.
      :END:
      This is another test.

      """.trimIndent(),
      "This is a test.\n\nContents.\n\nThis is another test.\n"
    )
  }

  @Test
  fun testFootnotes() {
    assertPlainText(
      """
      This is a test.
      [fn:1] Contents.
      This is another test.

      """.trimIndent(),
      "This is a test.\nContents.\nThis is another test.\n"
    )
  }

  @Test
  fun testLists() {
    assertPlainText(
      """
      1. Test 1.
      2. [X] Test 2.
         - Test tag :: Test 3.

      """.trimIndent(),
      "\nTest 1.\n\n\nTest 2.\n\n\nTest 3.\n\n"
    )
  }

  @Test
  fun testTables() {
    assertPlainText(
      """
      This is a test.
      | Test1 | Test2 | Test3 |
      |-------+-------+-------|
      | Test4 | Test5 | Test6 |
      | Test7 | Test8 | Test9 |
      This is another test.

      """.trimIndent(), """
      This is a test.

      Test1

      Test2

      Test3







      Test4

      Test5

      Test6




      Test7

      Test8

      Test9



      This is another test.

      """.trimIndent()
    )
  }

  @Test
  fun testBabelCalls() {
    assertPlainText(
      """
      This is a test.
      #+CALL: Contents.
      This is another test.

      """.trimIndent(),
      "This is a test.\n\nThis is another test.\n"
    )
  }

  @Test
  fun testClocks() {
    assertPlainText(
      """
      This is a test.
      CLOCK: [1234-05-06 07:08]
      CLOCK: [1234-05-06 07:08-09:10] => 1:23
      CLOCK: [1234-05-06 07:08]--[1234-05-06 07:08] => 1:23
      This is another test.

      """.trimIndent(),
      "This is a test.\n\n\n\nThis is another test.\n"
    )
  }

  @Test
  fun testDiarySexps() {
    assertPlainText(
      """
      This is a test.
      %%(Contents.
      This is another test.

      """.trimIndent(),
      "This is a test.\n\nThis is another test.\n"
    )
  }

  @Test
  fun testPlannings() {
    assertPlainText(
      """
      * Test
      DEADLINE: [1234-05-06 Sat 07:08]
      This is a test.

      """.trimIndent(),
      "\nTest\n\n\nThis is a test.\n"
    )
  }

  @Test
  fun testComments() {
    assertPlainText(
      """
      This is a test.
        #	Comment
      # Another comment
      This is another test.

      """.trimIndent(),
      "This is a test.\nThis is another test.\n"
    )
  }

  @Test
  fun testFixedWidthLines() {
    assertPlainText(
      """
      This is a test.
        :
      : Contents.
      This is another test.

      """.trimIndent(),
      "This is a test.\n\nContents.\nThis is another test.\n"
    )
  }

  @Test
  fun testHorizontalRules() {
    assertPlainText(
      """
      This is a test.
      -----
      This is another test.

      """.trimIndent(),
      "This is a test.\n\nThis is another test.\n"
    )
  }

  @Test
  fun testKeywords() {
    assertPlainText(
      """
      This is a test.
      #+TAGS: test1 test2
      This is another test.

      """.trimIndent(),
      "This is a test.\n\nThis is another test.\n"
    )
  }

  @Test
  fun testLatexEnvironments() {
    assertPlainText(
      """
      This is a test.
      \begin{test}
        Contents 1.
        \begin{equation}
          Contents 2.
        \end{equation}
        Contents 3.
      \end{test}
      This is another test.

      """.trimIndent(),
      "This is a test.\n\nThis is another test.\n"
    )
  }

  @Test
  fun testEntities() {
    assertPlainText(
      "This is a test: \\entityone, \\entitytwo{}.\n",
      "This is a test: Dummy0, Dummy1.\n"
    )
  }

  @Test
  fun testLatexFragments() {
    assertPlainText(
      "This is a test: \\test[abc][def]{ghi}.\n",
      "This is a test: Dummy0.\n"
    )
    assertPlainText(
      "This is a test: \\(E = mc^2\\).\n",
      "This is a test: Dummy0.\n"
    )
    assertPlainText(
      "This is a test: \\[E = mc^2\\].\n",
      "This is a test: Dummy0.\n"
    )
    assertPlainText(
      "This is a test: $\$E = mc^2$$.\n",
      "This is a test: Dummy0.\n"
    )
    assertPlainText(
      "This is a test: \$E$.\n",
      "This is a test: Dummy0.\n"
    )
    assertPlainText(
      "This is a test: \$E = mc^2$.\n",
      "This is a test: Dummy0.\n"
    )
  }

  @Test
  fun testExportSnippets() {
    assertPlainText(
      "This is a test: @@html:<b>@@Test@@html:</b>@@.\n",
      "This is a test: Test.\n"
    )
  }

  @Test
  fun testFootnoteReferences() {
    assertPlainText(
      "This is a test[fn:1].\n",
      "This is a test.\n"
    )
    assertPlainText(
      "This is a test[fn:1:contents].\n",
      "This is a test.\n"
    )
    assertPlainText(
      "This is a test[fn::contents].\n",
      "This is a test.\n"
    )
  }

  @Test
  fun testInlineBabelCalls() {
    assertPlainText(
      "This is a test: call_test(abc, def).\n",
      "This is a test: Dummy0.\n"
    )
    assertPlainText(
      "This is a test: call_test[foo](abc, def)[bar].\n",
      "This is a test: Dummy0.\n"
    )
  }

  @Test
  fun testInlineSourceBlocks() {
    assertPlainText(
      "This is a test: src_abc{def}.\n",
      "This is a test: Dummy0.\n"
    )
    assertPlainText(
      "This is a test: src_abc[foo]{def}.\n",
      "This is a test: Dummy0.\n"
    )
  }

  @Test
  fun testLinks() {
    assertPlainText(
      "This is a test: <<<test>>>.\n",
      "This is a test: Dummy0.\n"
    )
    assertPlainText(
      "This is a test: <<test>>.\n",
      "This is a test: Dummy0.\n"
    )
    assertPlainText(
      "This is a test: <https://bsplines.org/>.\n",
      "This is a test: Dummy0.\n"
    )
    assertPlainText(
      "This is a test: https://bsplines.org/.\n",
      "This is a test: Dummy0.\n"
    )
    assertPlainText(
      "This is a test: [[https://bsplines.org/]].\n",
      "This is a test: Dummy0.\n"
    )
    assertPlainText(
      "This is a test: [[https://bsplines.org/][a *good* test]].\n",
      "This is a test: a good test.\n"
    )
  }

  @Test
  fun testMacros() {
    assertPlainText(
      "This is a test: {{{test(abc, def)}}}.\n",
      "This is a test: Dummy0.\n"
    )
  }

  @Test
  fun testStatisticsCookies() {
    assertPlainText(
      "This is a test: [50%], [1/3].\n",
      "This is a test: Dummy0, Dummy1.\n"
    )
  }

  @Test
  fun testTimestamps() {
    assertPlainText(
      "This is a test: <%%(abv)>.\n",
      "This is a test: Dummy0.\n"
    )
    assertPlainText(
      "This is a test: <1234-05-06 Sat 07:08>.\n",
      "This is a test: Dummy0.\n"
    )
    assertPlainText(
      "This is a test: <1234-05-06 Sat 07:08 +1w>.\n",
      "This is a test: Dummy0.\n"
    )
    assertPlainText(
      "This is a test: <1234-05-06 Sat 07:08 -2d>.\n",
      "This is a test: Dummy0.\n"
    )
    assertPlainText(
      "This is a test: <1234-05-06 Sat 07:08 +1w -2d>.\n",
      "This is a test: Dummy0.\n"
    )
    assertPlainText(
      "This is a test: [1234-05-06 Sat 07:08].\n",
      "This is a test: Dummy0.\n"
    )
    assertPlainText(
      "This is a test: <1234-05-06 Sat 07:08>--<1234-05-06 Sat 07:08>.\n",
      "This is a test: Dummy0.\n"
    )
    assertPlainText(
      "This is a test: <1234-05-06 Sat 07:08-09:10>.\n",
      "This is a test: Dummy0.\n"
    )
    assertPlainText(
      "This is a test: [1234-05-06 Sat 07:08]--[1234-05-06 Sat 07:08].\n",
      "This is a test: Dummy0.\n"
    )
    assertPlainText(
      "This is a test: [1234-05-06 Sat 07:08-09:10].\n",
      "This is a test: Dummy0.\n"
    )
  }

  @Test
  fun testTextMarkup() {
    assertPlainText(
      "This is a test: *Test*.\n",
      "This is a test: Test.\n"
    )
    assertPlainText(
      "This is a test: +Test+.\n",
      "This is a test: Test.\n"
    )
    assertPlainText(
      "This is a test: /Test/.\n",
      "This is a test: Test.\n"
    )
    assertPlainText(
      "This is a test: =Test=.\n",
      "This is a test: Dummy0.\n"
    )
    assertPlainText(
      "This is a test: _Test_.\n",
      "This is a test: Test.\n"
    )
    assertPlainText(
      "This is a test: ~Test~.\n",
      "This is a test: Dummy0.\n"
    )
  }
}
