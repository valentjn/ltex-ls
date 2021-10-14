/* Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.restructuredtext

import org.bsplines.ltexls.parsing.CodeAnnotatedTextBuilderTest
import kotlin.test.Test

class RestructuredtextAnnotatedTextBuilderTest : CodeAnnotatedTextBuilderTest("restructuredtext") {
  @Test
  fun testFootnoteBlocks() {
    assertPlainText(
      """
      .. [1] A footnote contains body elements, consistently indented by at
         least 3 spaces.

         This is the footnote's second paragraph.

      .. [#label] Footnotes may be numbered, either manually (as in [1]_) or
         automatically using a "#"-prefixed label.  This footnote has a
         label so it can be referred to from multiple places, both as a
         footnote reference ([#label]_) and as a hyperlink reference
         (label_).

      .. [#] This footnote is numbered automatically and anonymously using a
         label of "#" only.

      .. [*] Footnotes may also use symbols, specified with a "*" label.
         Here's a reference to the next footnote: [*]_.

      """.trimIndent(),
      """
      A footnote contains body elements, consistently indented by at
      least 3 spaces.

      This is the footnote's second paragraph.

      Footnotes may be numbered, either manually (as in Dummy0) or
      automatically using a "#"-prefixed label.  This footnote has a
      label so it can be referred to from multiple places, both as a
      footnote reference (Dummy1) and as a hyperlink reference
      (label_).

      This footnote is numbered automatically and anonymously using a
      label of "#" only.

      Footnotes may also use symbols, specified with a "" label.
      Here's a reference to the next footnote: Dummy2.

      """.trimIndent()
    )
  }

  @Test
  fun testCitationBlocks() {
    assertPlainText(
      """
      This is a test.

      .. [CIT2002] Citations are text-labeled footnotes. They may be
      rendered separately and differently from footnotes.

      This is another test.

      """.trimIndent(),
      "This is a test.\n\n\nThis is another test.\n"
    )
  }

  @Test
  fun testHyperlinkTargetBlocks() {
    assertPlainText(
      """
      This is a test.

      .. _Python: http://www.python.org/

      This is another test.

      """.trimIndent(),
      "This is a test.\n\n\nThis is another test.\n"
    )
  }

  @Test
  fun testDirectiveBlocks() {
    assertPlainText(
      """
      This is a test.

      .. image:: images/title.png
         :target: directives_

      This is another test.

      """.trimIndent(),
      "This is a test.\n\nimages/title.png\n:target: directives_\n\nThis is another test.\n"
    )
  }

  @Test
  fun testSubstitutionDefinitionBlocks() {
    assertPlainText(
      """
      This is a test.

      .. |EXAMPLE| image:: images/biohazard.png

      This is another test.

      """.trimIndent(),
      "This is a test.\n\n\nThis is another test.\n"
    )
  }

  @Test
  fun testCommentBlocks() {
    assertPlainText(
      """
      This is a test.

      .. Comments begin with two dots and a space. Anything may
         follow, except for the syntax of footnotes, hyperlink
         targets, directives, or substitution definitions.

         Double-dashes -- "--" -- must be escaped somehow in HTML output.

      This is another test.

      """.trimIndent(),
      "This is a test.\n\n\n\nThis is another test.\n"
    )
  }

  @Test
  fun testGridTableBlocks() {
    assertPlainText(
      """
      This is a test.

      +------------------------+------------+----------+----------+
      | Header row, column 1   | Header 2   | Header 3 | Header 4 |
      | (header rows optional) |            |          |          |
      +========================+============+==========+==========+
      | body row 1, column 1   | column 2   | column 3 | column 4 |
      +------------------------+------------+----------+----------+
      | body row 2             | Cells may span columns.          |
      +------------------------+------------+---------------------+
      | body row 3             | Cells may  | - Table cells       |
      +------------------------+ span rows. | - contain           |
      | body row 4             |            | - body elements.    |
      +------------------------+------------+----------+----------+
      | body row 5             | Cells may also be     |          |
      |                        | empty: ``-->``        |          |
      +------------------------+-----------------------+----------+

      This is another test.

      """.trimIndent(),
      "This is a test.\n\n\nThis is another test.\n"
    )
  }

  @Test
  fun testSimpleTableBlocks() {
    assertPlainText(
      """
      This is a test.

      =====  =====  ======
         Inputs     Output
      ------------  ------
        A      B    A or B
      =====  =====  ======
      False  False  False
      True   False  True
      False  True   True
      True   True   True
      =====  =====  ======

      This is another test.

      """.trimIndent(),
      "This is a test.\n\n\nThis is another test.\n"
    )
  }

  @Test
  fun testSectionTitleBlocks() {
    assertPlainText(
      """
      This is a test.

      Body Elements
      =============

      ----------
      Paragraphs
      ----------

      A paragraph.

      Inline Markup
      `````````````

      This is another test.

      """.trimIndent(),
      """
      This is a test.

      Body Elements

      Paragraphs

      A paragraph.

      Inline Markup

      This is another test.

      """.trimIndent()
    )
  }

  @Test
  fun testLineBlocks() {
    assertPlainText(
      """
      | This is a line block.  It ends with a blank line.
      |     Each new line begins with a vertical bar ("|").
      |     Line breaks and initial indents are preserved.
      | Continuation lines are wrapped portions of long lines;
        they begin with a space in place of the vertical bar.
      |     The left edge of a continuation line need not be aligned with
        the left edge of the text above it.

      | This is a second line block.
      |
      | Blank lines are permitted internally, but they must begin with a "|".

      """.trimIndent(),
      """
      This is a line block.  It ends with a blank line.
      Each new line begins with a vertical bar ("|").
      Line breaks and initial indents are preserved.
      Continuation lines are wrapped portions of long lines;
      they begin with a space in place of the vertical bar.
      The left edge of a continuation line need not be aligned with
      the left edge of the text above it.

      This is a second line block.
      |
      Blank lines are permitted internally, but they must begin with a "|".

      """.trimIndent()
    )
  }

  @Test
  fun testBulletListBlocks() {
    assertPlainText(
      """
      - A bullet list

        + Nested bullet list.
        + Nested item 2.

      - Item 2.

        Paragraph 2 of item 2.

        * Nested bullet list.
        * Nested item 2.

          - Third level.
          - Item 2.

        * Nested item 3.

      """.trimIndent(),
      """
      A bullet list

      Nested bullet list.
      Nested item 2.

      Item 2.

      Paragraph 2 of item 2.

      Nested bullet list.
      Nested item 2.

      Third level.
      Item 2.

      Nested item 3.

      """.trimIndent()
    )
  }

  @Test
  fun testEnumeratedListBlocks() {
    assertPlainText(
      """
      1. Arabic numerals.

         a) lower alpha)

            (i) (lower roman)

                A. upper alpha.

                   I) upper roman)

      """.trimIndent(),
      """
      Arabic numerals.

      lower alpha)

      (lower roman)

      upper alpha.

      upper roman)

      """.trimIndent()
    )
  }

  @Test
  fun testInlineMarkup() {
    assertPlainText(
      """
      Paragraphs contain text and may contain inline markup: *emphasis*,
      **strong emphasis**, ``inline literals``, standalone hyperlinks
      (http://www.python.org), external hyperlinks (Python_), internal
      cross-references (example_), external hyperlinks with embedded URIs
      (`Python web site <http://www.python.org>`__), footnote references
      (manually numbered [1]_, anonymous auto-numbered [#]_, labeled
      auto-numbered [#label]_, or symbolic [*]_), citation references
      ([CIT2002]_), substitution references (|example|), and _`inline
      hyperlink targets` (see Targets_ below for a reference back to here).
      Character-level inline markup is also possible (although exceedingly
      ugly!) in *re*\ ``Structured``\ *Text*.  Problems are indicated by
      |problematic| text (generated by processing errors; this one is
      intentional).

      The default role for interpreted text is `Title Reference`.  Here are
      some explicit interpreted text roles: a PEP reference (:PEP:`287`); an
      RFC reference (:RFC:`2822`); a :sub:`subscript`; a :sup:`superscript`;
      and explicit roles for :emphasis:`standard` :strong:`inline`
      :literal:`markup`.

      """.trimIndent(),
      """
      Paragraphs contain text and may contain inline markup: emphasis,
      strong emphasis, Dummy0, standalone hyperlinks
      (http://www.python.org), external hyperlinks (Python_), internal
      cross-references (example_), external hyperlinks with embedded URIs
      (Dummy1), footnote references
      (manually numbered Dummy2, anonymous auto-numbered Dummy3, labeled
      auto-numbered Dummy4, or symbolic Dummy5), citation references
      (Dummy6), substitution references (|example|), and
      """.trimIndent()
      + " " + """
      Dummy7 (see Targets_ below for a reference back to here).
      Character-level inline markup is also possible (although exceedingly
      ugly!) in re\ Dummy8\ Text.  Problems are indicated by
      |problematic| text (generated by processing errors; this one is
      intentional).

      The default role for interpreted text is Dummy9.  Here are
      some explicit interpreted text roles: a PEP reference (Dummy10); an
      RFC reference (Dummy11); a Dummy12; a Dummy13;
      and explicit roles for Dummy14 Dummy15
      Dummy16.

      """.trimIndent()
    )
  }
}
