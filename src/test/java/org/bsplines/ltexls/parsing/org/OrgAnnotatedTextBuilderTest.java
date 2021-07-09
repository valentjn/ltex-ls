/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.org;

import org.bsplines.ltexls.parsing.CodeAnnotatedTextBuilder;
import org.bsplines.ltexls.settings.Settings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.languagetool.markup.AnnotatedText;

public class OrgAnnotatedTextBuilderTest {
  private static void assertPlainText(String code, String expectedPlainText) {
    AnnotatedText annotatedText = buildAnnotatedText(code);
    Assertions.assertEquals(expectedPlainText, annotatedText.getPlainText());
  }

  private static AnnotatedText buildAnnotatedText(String code) {
    CodeAnnotatedTextBuilder builder = CodeAnnotatedTextBuilder.create("org");
    Settings settings = new Settings();
    builder.setSettings(settings);
    return builder.addCode(code).build();
  }

  @Test
  public void testHeadlinesAndSections() {
    assertPlainText(
        "* \n"
        + "\n"
        + "** DONE\n"
        + "\n"
        + "*** This is a test\n"
        + "\n"
        + "**** TODO [#A] COMMENT Another test :tag:a2%:\n"
        + "\n"
        + "**** TODO [#A] Final test :tag:a2%:\n",
        "\n\n\n\n\n\n\n\n\nThis is a test\n\n\n\n\n\n\nFinal test\n\n");
  }

  @Test
  public void testAffiliatedKeywords() {
    assertPlainText(
        "This is a test.\n"
        + "\n"
        + "#+HEADER: test\n"
        + "#+CAPTION[abc]: def\n"
        + "Second sentence.\n"
        + "\n"
        + "  #+ATTR_foo01_bar: BOOM\n"
        + "Final sentence.\n",
        "This is a test.\n\n\n\nSecond sentence.\n\n\nFinal sentence.\n");
  }

  @Test
  public void testBlocks() {
    assertPlainText(
        "This is a test.\n"
        + "#+BEGIN_CENTER\n"
        + "Contents.\n"
        + "#+END_CENTER\n"
        + "This is another test.\n",
        "This is a test.\n\nContents.\n\nThis is another test.\n");
    assertPlainText(
        "This is a test.\n"
        + "#+BEGIN_QUOTE\n"
        + "Contents.\n"
        + "#+END_QUOTE\n"
        + "This is another test.\n",
        "This is a test.\n\nContents.\n\nThis is another test.\n");
    assertPlainText(
        "This is a test.\n"
        + "#+BEGIN_FOOBAR\n"
        + "Contents.\n"
        + "#+END_FOOBAR\n"
        + "This is another test.\n",
        "This is a test.\n\nContents.\n\nThis is another test.\n");
    assertPlainText(
        "This is a test.\n"
        + "#+BEGIN_COMMENT\n"
        + "Contents.\n"
        + "#+END_COMMENT\n"
        + "This is another test.\n",
        "This is a test.\n\nThis is another test.\n");
    assertPlainText(
        "This is a test.\n"
        + "#+BEGIN_EXAMPLE\n"
        + "Contents.\n"
        + "#+END_EXAMPLE\n"
        + "This is another test.\n",
        "This is a test.\n\nThis is another test.\n");
    assertPlainText(
        "This is a test.\n"
        + "#+BEGIN_EXPORT\n"
        + "Contents.\n"
        + "#+END_EXPORT\n"
        + "This is another test.\n",
        "This is a test.\n\nThis is another test.\n");
    assertPlainText(
        "This is a test.\n"
        + "#+BEGIN_SRC\n"
        + "Contents.\n"
        + "#+END_SRC\n"
        + "This is another test.\n",
        "This is a test.\n\nThis is another test.\n");
    assertPlainText(
        "This is a test.\n"
        + "#+BEGIN_VERSE\n"
        + "Contents.\n"
        + "#+END_VERSE\n"
        + "This is another test.\n",
        "This is a test.\n\nContents.\n\nThis is another test.\n");
  }

  @Test
  public void testDrawers() {
    assertPlainText(
        "This is a test.\n"
        + ":TEST_DRAWER:\n"
        + "Contents.\n"
        + ":END:\n"
        + "This is another test.\n",
        "This is a test.\n\nContents.\n\nThis is another test.\n");
    assertPlainText(
        "This is a test.\n"
        + ":PROPERTIES:\n"
        + "Contents.\n"
        + ":END:\n"
        + "This is another test.\n",
        "This is a test.\n\nThis is another test.\n");
  }

  @Test
  public void testDynamicBlocks() {
    assertPlainText(
        "This is a test.\n"
        + "#+BEGIN: test-block :abc\n"
        + "Contents.\n"
        + ":END:\n"
        + "This is another test.\n",
        "This is a test.\n\nContents.\n\nThis is another test.\n");
  }

  @Test
  public void testFootnotes() {
    assertPlainText(
        "This is a test.\n"
        + "[fn:1] Contents.\n"
        + "This is another test.\n",
        "This is a test.\nContents.\nThis is another test.\n");
  }

  @Test
  public void testLists() {
    assertPlainText(
        "1. Test 1.\n"
        + "2. [X] Test 2.\n"
        + "   - Test tag :: Test 3.\n",
        "\nTest 1.\n\n\nTest 2.\n\n\nTest 3.\n\n");
  }

  @Test
  public void testTables() {
    assertPlainText(
        "This is a test.\n"
        + "| Test1 | Test2 | Test3 |\n"
        + "|-------+-------+-------|\n"
        + "| Test4 | Test5 | Test6 |\n"
        + "| Test7 | Test8 | Test9 |\n"
        + "This is another test.\n",
        "This is a test.\n\nTest1\n\nTest2\n\nTest3\n\n\n\n\n\n\n\nTest4\n\nTest5\n\n"
        + "Test6\n\n\n\n\nTest7\n\nTest8\n\nTest9\n\n\n\nThis is another test.\n");
  }

  @Test
  public void testBabelCalls() {
    assertPlainText(
        "This is a test.\n"
        + "#+CALL: Contents.\n"
        + "This is another test.\n",
        "This is a test.\n\nThis is another test.\n");
  }

  @Test
  public void testClocks() {
    assertPlainText(
        "This is a test.\n"
        + "CLOCK: [1234-05-06 07:08]\n"
        + "CLOCK: [1234-05-06 07:08-09:10] => 1:23\n"
        + "CLOCK: [1234-05-06 07:08]--[1234-05-06 07:08] => 1:23\n"
        + "This is another test.\n",
        "This is a test.\n\n\n\nThis is another test.\n");
  }

  @Test
  public void testDiarySexps() {
    assertPlainText(
        "This is a test.\n"
        + "%%(Contents.\n"
        + "This is another test.\n",
        "This is a test.\n\nThis is another test.\n");
  }

  @Test
  public void testPlannings() {
    assertPlainText(
        "* Test\n"
        + "DEADLINE: [1234-05-06 Sat 07:08]\n"
        + "This is a test.\n",
        "\nTest\n\n\nThis is a test.\n");
  }

  @Test
  public void testComments() {
    assertPlainText(
        "This is a test.\n"
        + "\t#\tComment\n"
        + "# Another comment\n"
        + "This is another test.\n",
        "This is a test.\nThis is another test.\n");
  }

  @Test
  public void testFixedWidthLines() {
    assertPlainText(
        "This is a test.\n"
        + "\t:\n"
        + ": Contents.\n"
        + "This is another test.\n",
        "This is a test.\n\nContents.\nThis is another test.\n");
  }

  @Test
  public void testHorizontalRules() {
    assertPlainText(
        "This is a test.\n"
        + "-----\n"
        + "This is another test.\n",
        "This is a test.\n\nThis is another test.\n");
  }

  @Test
  public void testKeywords() {
    assertPlainText(
        "This is a test.\n"
        + "#+TAGS: test1 test2\n"
        + "This is another test.\n",
        "This is a test.\n\nThis is another test.\n");
  }

  @Test
  public void testLatexEnvironments() {
    assertPlainText(
        "This is a test.\n"
        + "\\begin{test}\n"
        + "  Contents 1.\n"
        + "  \\begin{equation}\n"
        + "    Contents 2.\n"
        + "  \\end{equation}\n"
        + "  Contents 3.\n"
        + "\\end{test}\n"
        + "This is another test.\n",
        "This is a test.\n\nThis is another test.\n");
  }

  @Test
  public void testEntities() {
    assertPlainText(
        "This is a test: \\entityone, \\entitytwo{}.\n",
        "This is a test: Dummy0, Dummy1.\n");
  }

  @Test
  public void testLatexFragments() {
    assertPlainText(
        "This is a test: \\test[abc][def]{ghi}.\n",
        "This is a test: Dummy0.\n");
    assertPlainText(
        "This is a test: \\(E = mc^2\\).\n",
        "This is a test: Dummy0.\n");
    assertPlainText(
        "This is a test: \\[E = mc^2\\].\n",
        "This is a test: Dummy0.\n");
    assertPlainText(
        "This is a test: $$E = mc^2$$.\n",
        "This is a test: Dummy0.\n");
    assertPlainText(
        "This is a test: $E$.\n",
        "This is a test: Dummy0.\n");
    assertPlainText(
        "This is a test: $E = mc^2$.\n",
        "This is a test: Dummy0.\n");
  }

  @Test
  public void testExportSnippets() {
    assertPlainText(
        "This is a test: @@html:<b>@@Test@@html:</b>@@.\n",
        "This is a test: Test.\n");
  }

  @Test
  public void testFootnoteReferences() {
    assertPlainText(
        "This is a test[fn:1].\n",
        "This is a test.\n");
    assertPlainText(
        "This is a test[fn:1:contents].\n",
        "This is a test.\n");
    assertPlainText(
        "This is a test[fn::contents].\n",
        "This is a test.\n");
  }

  @Test
  public void testInlineBabelCalls() {
    assertPlainText(
        "This is a test: call_test(abc, def).\n",
        "This is a test: Dummy0.\n");
    assertPlainText(
        "This is a test: call_test[foo](abc, def)[bar].\n",
        "This is a test: Dummy0.\n");
  }

  @Test
  public void testInlineSourceBlocks() {
    assertPlainText(
        "This is a test: src_abc{def}.\n",
        "This is a test: Dummy0.\n");
    assertPlainText(
        "This is a test: src_abc[foo]{def}.\n",
        "This is a test: Dummy0.\n");
  }

  @Test
  public void testLinks() {
    assertPlainText(
        "This is a test: <<<test>>>.\n",
        "This is a test: Dummy0.\n");
    assertPlainText(
        "This is a test: <<test>>.\n",
        "This is a test: Dummy0.\n");
    assertPlainText(
        "This is a test: <https://bsplines.org/>.\n",
        "This is a test: Dummy0.\n");
    assertPlainText(
        "This is a test: https://bsplines.org/.\n",
        "This is a test: Dummy0.\n");
    assertPlainText(
        "This is a test: [[https://bsplines.org/]].\n",
        "This is a test: Dummy0.\n");
    assertPlainText(
        "This is a test: [[https://bsplines.org/][a *good* test]].\n",
        "This is a test: a good test.\n");
  }

  @Test
  public void testMacros() {
    assertPlainText(
        "This is a test: {{{test(abc, def)}}}.\n",
        "This is a test: Dummy0.\n");
  }

  @Test
  public void testStatisticsCookies() {
    assertPlainText(
        "This is a test: [50%], [1/3].\n",
        "This is a test: Dummy0, Dummy1.\n");
  }

  @Test
  public void testTimestamps() {
    assertPlainText(
        "This is a test: <%%(abv)>.\n",
        "This is a test: Dummy0.\n");
    assertPlainText(
        "This is a test: <1234-05-06 Sat 07:08>.\n",
        "This is a test: Dummy0.\n");
    assertPlainText(
        "This is a test: <1234-05-06 Sat 07:08 +1w>.\n",
        "This is a test: Dummy0.\n");
    assertPlainText(
        "This is a test: <1234-05-06 Sat 07:08 -2d>.\n",
        "This is a test: Dummy0.\n");
    assertPlainText(
        "This is a test: <1234-05-06 Sat 07:08 +1w -2d>.\n",
        "This is a test: Dummy0.\n");
    assertPlainText(
        "This is a test: [1234-05-06 Sat 07:08].\n",
        "This is a test: Dummy0.\n");
    assertPlainText(
        "This is a test: <1234-05-06 Sat 07:08>--<1234-05-06 Sat 07:08>.\n",
        "This is a test: Dummy0.\n");
    assertPlainText(
        "This is a test: <1234-05-06 Sat 07:08-09:10>.\n",
        "This is a test: Dummy0.\n");
    assertPlainText(
        "This is a test: [1234-05-06 Sat 07:08]--[1234-05-06 Sat 07:08].\n",
        "This is a test: Dummy0.\n");
    assertPlainText(
        "This is a test: [1234-05-06 Sat 07:08-09:10].\n",
        "This is a test: Dummy0.\n");
  }

  @Test
  public void testTextMarkup() {
    assertPlainText(
        "This is a test: *Test*.\n",
        "This is a test: Test.\n");
    assertPlainText(
        "This is a test: +Test+.\n",
        "This is a test: Test.\n");
    assertPlainText(
        "This is a test: /Test/.\n",
        "This is a test: Test.\n");
    assertPlainText(
        "This is a test: =Test=.\n",
        "This is a test: Dummy0.\n");
    assertPlainText(
        "This is a test: _Test_.\n",
        "This is a test: Test.\n");
    assertPlainText(
        "This is a test: ~Test~.\n",
        "This is a test: Dummy0.\n");
  }
}
