/* Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.asciidoc

import org.bsplines.ltexls.parsing.CodeAnnotatedTextBuilderTest
import org.languagetool.markup.AnnotatedText
import kotlin.test.Test

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
      "This results in a visible line break (e.g., `<br>`) between the lines.\n",
      "This results in a visible line break (e.g., Dummy0) between the lines.\n"
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

  @Test
  fun test() {
    val code = """
// tag::para[]
This journey begins one late Monday afternoon in Antwerp.

Our team desperately needs coffee, but none of us dare open the office door.
// end::para[]

// used in qr
// FIXME this should be in the blocks module
// tag::b-para[]
Paragraphs don't require special markup in AsciiDoc.
A paragraph is defined by one or more consecutive lines of text.
Newlines within a paragraph are not displayed.

Leave at least one blank line to begin a new paragraph.
// end::b-para[]

//used in qr
// tag::hb-all[]
Roses are red, +
violets are blue.

[%hardbreaks]
A ruby is red.
Java is black.
// end::hb-all[]

// tag::hb[]
Roses are red, +
violets are blue.
// end::hb[]

// tag::hb-p[]
[%hardbreaks]
A ruby is red.
Java is black.
// end::hb-p[]

// tag::b-hb[]
To preserve a line break, end the line in a space followed by a plus sign. +
This results in a visible line break (e.g., `<br>`) between the lines.
// end::b-hb[]

// tag::hb-attr[]
= Line Break Doc Title
:hardbreaks-option:

Roses are red,
violets are blue.
// end::hb-attr[]

// tag::lead[]
[.lead]
This is the ultimate paragraph.
// end::lead[]

// tag::b-lead[]
[.lead]
This text will be styled as a lead paragraph (i.e., larger font).
// end::b-lead[]

// tag::qr-lead[]
[.lead]
This text will be styled as a lead paragraph (i.e., larger font).

This paragraph will not be.
// end::qr-lead[]

// tag::b-i[]
_To tame_ the wild wolpertingers we needed to build a *charm*.
But **u**ltimate victory could only be won if we divined the
*_true name_* of the __war__lock.
// end::b-i[]

// tag::b-i-n[]
_To tame_ the wild wolpertingers we needed to build a *charm*.
But **u**ltimate victory could only be won if we divined the
*_true name_* of the __war__lock.
// end::b-i-n[]

// used in qr
// tag::b-bold-italic-mono[]
bold *constrained* & **un**constrained

italic _constrained_ & __un__constrained

bold italic *_constrained_* & **__un__**constrained

monospace `constrained` & ``un``constrained

monospace bold `*constrained*` & ``**un**``constrained

monospace italic `_constrained_` & ``__un__``constrained

monospace bold italic `*_constrained_*` & ``**__un__**``constrained
// end::b-bold-italic-mono[]

// tag::constrained-bold-italic-mono[]
It has *strong* significance to me.

I _cannot_ stress this enough.

Type `OK` to accept.

That *_really_* has to go.

Can't pick one? Let's use them `*_all_*`.
// end::constrained-bold-italic-mono[]

// tag::unconstrained-bold-italic-mono[]
**C**reate, **R**ead, **U**pdate, and **D**elete (CRUD)

That's fan__freakin__tastic!

Don't pass generic ``Object``s to methods that accept ``String``s!

It was Beatle**__mania__**!
// end::unconstrained-bold-italic-mono[]

// used in qr
// tag::monospace-vs-codespan[]
`{cpp}` is valid syntax in the programming language by the same name.

`+WHERE id <= 20 AND value = "{name}"+` is a SQL WHERE clause.
// end::monospace-vs-codespan[]

// tag::c-quote-co[]
"`What kind of charm?`" Lazarus asked.
"`An odoriferous one or a mineral one?`" <.>

Kizmet shrugged.
"`The note from Olaf's desk says '`wormwood and licorice,`'
but these could be normal groceries for werewolves.`" <.>
// end::c-quote-co[]

// tag::c-quote[]
"`What kind of charm?`" Lazarus asked.
"`An odoriferous one or a mineral one?`"

Kizmet shrugged.
"`The note from Olaf's desk says '`wormwood and licorice,`'
but these could be normal groceries for werewolves.`"
// end::c-quote[]

// used in qr
// tag::b-c-quote[]
"`double curved quotes`"

'`single curved quotes`'

Olaf's desk was a mess.

A ``std::vector```'s size is the number of items it contains.

All of the werewolves`' desks were a mess.

Olaf had been with the company since the `'00s.
// end::b-c-quote[]

// tag::apos[]
Olaf had been with the company since the `'00s.
His desk overflowed with heaps of paper, apple cores and squeaky toys.
We couldn't find Olaf's keyboard.
The state of his desk was replicated, in triplicate, across all of
the werewolves`' desks.
// end::apos[]

// tag::sub-sup[]
"`Well the H~2~O formula written on their whiteboard could be part
of a shopping list, but I don't think the local bodega sells
E=mc^2^,`" Lazarus replied.
// end::sub-sup[]

//used in qr
// tag::b-sub-sup[]
^super^script phrase

~sub~script phrase
// end::b-sub-sup[]

// tag::mono[]
"`Wait!`" Indigo plucked a small vial from her desk's top drawer
and held it toward us.
The vial's label read: `E=mc^2^`; the `E` represents _energy_,
but also pure _genius!_
// end::mono[]

// tag::literal-mono[]
You can reference the value of a document attribute using
the syntax `+{name}+`, where `name` is the attribute name.
// end::literal-mono[]

// tag::literal-mono-with-plus[]
`pass:[++]` is the increment operator in C.
// end::literal-mono-with-plus[]

// used in qr
// tag::b-mono-code[]
Reference code like `types` or `methods` inline.

Do not pass arbitrary ``Object``s to methods that accept ``String``s!
// end::b-mono-code[]

// tag::highlight[]
Werewolves are #allergic to cinnamon#.
// end::highlight[]

// tag::highlight-html[]
<mark>mark element</mark>
// end::highlight-html[]

// tag::text-span[]
The text [.underline]#underline me# is underlined.
// end::text-span[]

// tag::text-span-html[]
The text <span class="underline">underline me</span> is underlined.
// end::text-span-html[]

// tag::css-co[]
Do werewolves believe in [.small]#small print#? <.>

[.big]##O##nce upon an infinite loop.
// end::css-co[]

// tag::css[]
Do werewolves believe in [.small]#small print#?

[big]##O##nce upon an infinite loop.
// end::css[]

// used in qr
// tag::qr-all[]
Werewolves are allergic to #cinnamon#.

##Mark##up refers to text that contains formatting ##mark##s.

Where did all the [.underline]#cores# go?

We need [.line-through]#ten# twenty VMs.

A [.myrole]#custom role# must be fulfilled by the theme.
// end::qr-all[]

// tag::css-custom[]
Type the word [.userinput]#asciidoctor# into the search bar.
// end::css-custom[]

// tag::css-custom-html[]
<span class="userinput">asciidoctor</span>
// end::css-custom-html[]

////
phrase styled by CSS class .small#
////
"""
    val annotatedText: AnnotatedText = buildAnnotatedText(code)
    System.err.println(annotatedText.plainText)
  }
}
