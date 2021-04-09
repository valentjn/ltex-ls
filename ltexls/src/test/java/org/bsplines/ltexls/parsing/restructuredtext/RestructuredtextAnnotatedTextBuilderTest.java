/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.restructuredtext;

import org.bsplines.ltexls.parsing.CodeAnnotatedTextBuilder;
import org.bsplines.ltexls.settings.Settings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.languagetool.markup.AnnotatedText;

public class RestructuredtextAnnotatedTextBuilderTest {
  private static void assertPlainText(String code, String expectedPlainText) {
    AnnotatedText annotatedText = buildAnnotatedText(code);
    Assertions.assertEquals(expectedPlainText, annotatedText.getPlainText());
  }

  private static AnnotatedText buildAnnotatedText(String code) {
    CodeAnnotatedTextBuilder builder = CodeAnnotatedTextBuilder.create("restructuredtext");
    Settings settings = new Settings();
    builder.setSettings(settings);
    return builder.addCode(code).build();
  }

  @Test
  public void testFootnoteBlocks() {
    assertPlainText(
        ".. [1] A footnote contains body elements, consistently indented by at\n"
        + "   least 3 spaces.\n"
        + "\n"
        + "   This is the footnote's second paragraph.\n"
        + "\n"
        + ".. [#label] Footnotes may be numbered, either manually (as in [1]_) or\n"
        + "   automatically using a \"#\"-prefixed label.  This footnote has a\n"
        + "   label so it can be referred to from multiple places, both as a\n"
        + "   footnote reference ([#label]_) and as a hyperlink reference\n"
        + "   (label_).\n"
        + "\n"
        + ".. [#] This footnote is numbered automatically and anonymously using a\n"
        + "   label of \"#\" only.\n"
        + "\n"
        + ".. [*] Footnotes may also use symbols, specified with a \"*\" label.\n"
        + "   Here's a reference to the next footnote: [*]_.\n",
        "A footnote contains body elements, consistently indented by at\n"
        + "least 3 spaces.\n"
        + "\n"
        + "This is the footnote's second paragraph.\n"
        + "\n"
        + "Footnotes may be numbered, either manually (as in Dummy0) or\n"
        + "automatically using a \"#\"-prefixed label.  This footnote has a\n"
        + "label so it can be referred to from multiple places, both as a\n"
        + "footnote reference (Dummy1) and as a hyperlink reference\n"
        + "(label_).\n"
        + "\n"
        + "This footnote is numbered automatically and anonymously using a\n"
        + "label of \"#\" only.\n"
        + "\n"
        + "Footnotes may also use symbols, specified with a \"\" label.\n"
        + "Here's a reference to the next footnote: Dummy2.\n");
  }

  @Test
  public void testCitationBlocks() {
    assertPlainText(
        "This is a test.\n"
        + "\n"
        + ".. [CIT2002] Citations are text-labeled footnotes. They may be\n"
        + "rendered separately and differently from footnotes.\n"
        + "\n"
        + "This is another test.\n",
        "This is a test.\n\n\nThis is another test.\n");
  }

  @Test
  public void testHyperlinkTargetBlocks() {
    assertPlainText(
        "This is a test.\n"
        + "\n"
        + ".. _Python: http://www.python.org/\n"
        + "\n"
        + "This is another test.\n",
        "This is a test.\n\n\nThis is another test.\n");
  }

  @Test
  public void testDirectiveBlocks() {
    assertPlainText(
        "This is a test.\n"
        + "\n"
        + ".. image:: images/title.png\n"
        + "   :target: directives_\n"
        + "\n"
        + "This is another test.\n",
        "This is a test.\n\nimages/title.png\n:target: directives_\n\nThis is another test.\n");
  }

  @Test
  public void testSubstitutionDefinitionBlocks() {
    assertPlainText(
        "This is a test.\n"
        + "\n"
        + ".. |EXAMPLE| image:: images/biohazard.png\n"
        + "\n"
        + "This is another test.\n",
        "This is a test.\n\n\nThis is another test.\n");
  }

  @Test
  public void testCommentBlocks() {
    assertPlainText(
        "This is a test.\n"
        + "\n"
        + ".. Comments begin with two dots and a space. Anything may\n"
        + "   follow, except for the syntax of footnotes, hyperlink\n"
        + "   targets, directives, or substitution definitions.\n"
        + "\n"
        + "   Double-dashes -- \"--\" -- must be escaped somehow in HTML output.\n"
        + "\n"
        + "This is another test.\n",
        "This is a test.\n\n\n\nThis is another test.\n");
  }

  @Test
  public void testGridTableBlocks() {
    assertPlainText(
        "This is a test.\n"
        + "\n"
        + "+------------------------+------------+----------+----------+\n"
        + "| Header row, column 1   | Header 2   | Header 3 | Header 4 |\n"
        + "| (header rows optional) |            |          |          |\n"
        + "+========================+============+==========+==========+\n"
        + "| body row 1, column 1   | column 2   | column 3 | column 4 |\n"
        + "+------------------------+------------+----------+----------+\n"
        + "| body row 2             | Cells may span columns.          |\n"
        + "+------------------------+------------+---------------------+\n"
        + "| body row 3             | Cells may  | - Table cells       |\n"
        + "+------------------------+ span rows. | - contain           |\n"
        + "| body row 4             |            | - body elements.    |\n"
        + "+------------------------+------------+----------+----------+\n"
        + "| body row 5             | Cells may also be     |          |\n"
        + "|                        | empty: ``-->``        |          |\n"
        + "+------------------------+-----------------------+----------+\n"
        + "\n"
        + "This is another test.\n",
        "This is a test.\n\n\nThis is another test.\n");
  }

  @Test
  public void testSimpleTableBlocks() {
    assertPlainText(
        "This is a test.\n"
        + "\n"
        + "=====  =====  ======\n"
        + "   Inputs     Output\n"
        + "------------  ------\n"
        + "  A      B    A or B\n"
        + "=====  =====  ======\n"
        + "False  False  False\n"
        + "True   False  True\n"
        + "False  True   True\n"
        + "True   True   True\n"
        + "=====  =====  ======\n"
        + "\n"
        + "This is another test.\n",
        "This is a test.\n\n\nThis is another test.\n");
  }

  @Test
  public void testSectionTitleBlocks() {
    assertPlainText(
        "This is a test.\n"
        + "\n"
        + "Body Elements\n"
        + "=============\n"
        + "\n"
        + "----------\n"
        + "Paragraphs\n"
        + "----------\n"
        + "\n"
        + "A paragraph.\n"
        + "\n"
        + "Inline Markup\n"
        + "`````````````\n"
        + "\n"
        + "This is another test.\n",
        "This is a test.\n\nBody Elements\n\nParagraphs\n\nA paragraph.\n\nInline Markup\n\n"
        + "This is another test.\n");
  }

  @Test
  public void testLineBlocks() {
    assertPlainText(
        "| This is a line block.  It ends with a blank line.\n"
        + "|     Each new line begins with a vertical bar (\"|\").\n"
        + "|     Line breaks and initial indents are preserved.\n"
        + "| Continuation lines are wrapped portions of long lines;\n"
        + "  they begin with a space in place of the vertical bar.\n"
        + "|     The left edge of a continuation line need not be aligned with\n"
        + "  the left edge of the text above it.\n"
        + "\n"
        + "| This is a second line block.\n"
        + "|\n"
        + "| Blank lines are permitted internally, but they must begin with a \"|\".\n",
        "This is a line block.  It ends with a blank line.\n"
        + "Each new line begins with a vertical bar (\"|\").\n"
        + "Line breaks and initial indents are preserved.\n"
        + "Continuation lines are wrapped portions of long lines;\n"
        + "they begin with a space in place of the vertical bar.\n"
        + "The left edge of a continuation line need not be aligned with\n"
        + "the left edge of the text above it.\n"
        + "\n"
        + "This is a second line block.\n"
        + "|\n"
        + "Blank lines are permitted internally, but they must begin with a \"|\".\n");
  }

  @Test
  public void testBulletListBlocks() {
    assertPlainText(
        "- A bullet list\n"
        + "\n"
        + "  + Nested bullet list.\n"
        + "  + Nested item 2.\n"
        + "\n"
        + "- Item 2.\n"
        + "\n"
        + "  Paragraph 2 of item 2.\n"
        + "\n"
        + "  * Nested bullet list.\n"
        + "  * Nested item 2.\n"
        + "\n"
        + "    - Third level.\n"
        + "    - Item 2.\n"
        + "\n"
        + "  * Nested item 3.\n",
        "A bullet list\n"
        + "\n"
        + "Nested bullet list.\n"
        + "Nested item 2.\n"
        + "\n"
        + "Item 2.\n"
        + "\n"
        + "Paragraph 2 of item 2.\n"
        + "\n"
        + "Nested bullet list.\n"
        + "Nested item 2.\n"
        + "\n"
        + "Third level.\n"
        + "Item 2.\n"
        + "\n"
        + "Nested item 3.\n");
  }

  @Test
  public void testEnumeratedListBlocks() {
    assertPlainText(
        "1. Arabic numerals.\n"
        + "\n"
        + "   a) lower alpha)\n"
        + "\n"
        + "      (i) (lower roman)\n"
        + "\n"
        + "          A. upper alpha.\n"
        + "\n"
        + "             I) upper roman)\n",
        "Arabic numerals.\n"
        + "\n"
        + "lower alpha)\n"
        + "\n"
        + "(lower roman)\n"
        + "\n"
        + "upper alpha.\n"
        + "\n"
        + "upper roman)\n");
  }

  @Test
  public void testInlineMarkup() {
    assertPlainText(
        "Paragraphs contain text and may contain inline markup: *emphasis*,\n"
        + "**strong emphasis**, ``inline literals``, standalone hyperlinks\n"
        + "(http://www.python.org), external hyperlinks (Python_), internal\n"
        + "cross-references (example_), external hyperlinks with embedded URIs\n"
        + "(`Python web site <http://www.python.org>`__), footnote references\n"
        + "(manually numbered [1]_, anonymous auto-numbered [#]_, labeled\n"
        + "auto-numbered [#label]_, or symbolic [*]_), citation references\n"
        + "([CIT2002]_), substitution references (|example|), and _`inline\n"
        + "hyperlink targets` (see Targets_ below for a reference back to here).\n"
        + "Character-level inline markup is also possible (although exceedingly\n"
        + "ugly!) in *re*\\ ``Structured``\\ *Text*.  Problems are indicated by\n"
        + "|problematic| text (generated by processing errors; this one is\n"
        + "intentional).\n"
        + "\n"
        + "The default role for interpreted text is `Title Reference`.  Here are\n"
        + "some explicit interpreted text roles: a PEP reference (:PEP:`287`); an\n"
        + "RFC reference (:RFC:`2822`); a :sub:`subscript`; a :sup:`superscript`;\n"
        + "and explicit roles for :emphasis:`standard` :strong:`inline`\n"
        + ":literal:`markup`.\n",
        "Paragraphs contain text and may contain inline markup: emphasis,\n"
        + "strong emphasis, Dummy0, standalone hyperlinks\n"
        + "(http://www.python.org), external hyperlinks (Python_), internal\n"
        + "cross-references (example_), external hyperlinks with embedded URIs\n"
        + "(Dummy1), footnote references\n"
        + "(manually numbered Dummy2, anonymous auto-numbered Dummy3, labeled\n"
        + "auto-numbered Dummy4, or symbolic Dummy5), citation references\n"
        + "(Dummy6), substitution references (|example|), and Dummy7 "
        + "(see Targets_ below for a reference back to here).\n"
        + "Character-level inline markup is also possible (although exceedingly\n"
        + "ugly!) in re\\ Dummy8\\ Text.  Problems are indicated by\n"
        + "|problematic| text (generated by processing errors; this one is\n"
        + "intentional).\n"
        + "\n"
        + "The default role for interpreted text is Dummy9.  Here are\n"
        + "some explicit interpreted text roles: a PEP reference (Dummy10); an\n"
        + "RFC reference (Dummy11); a Dummy12; a Dummy13;\n"
        + "and explicit roles for Dummy14 Dummy15\n"
        + "Dummy16.\n");
  }
}
