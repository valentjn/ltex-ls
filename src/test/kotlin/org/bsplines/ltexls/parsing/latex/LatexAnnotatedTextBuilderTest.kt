/* Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.latex

import org.bsplines.ltexls.parsing.AnnotatedTextFragment
import org.bsplines.ltexls.parsing.CodeAnnotatedTextBuilderTest
import org.bsplines.ltexls.settings.Settings
import org.languagetool.markup.AnnotatedText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LatexAnnotatedTextBuilderTest : CodeAnnotatedTextBuilderTest("latex") {
  @Test
  @Suppress("LongMethod")
  fun testTextMode() {
    assertPlainText(
      """
      We can do
      \begin{itemize}[first-{test}]{[second]-test}
        \item this or
        \item that.
      \end{itemize}

      """.trimIndent(),
      "We can do this or that. ",
    )
    assertPlainText(
      """
      This is a test.
      \begin{frame}{Test}{Another Test}
        Inside the frame!
      \end{frame}
      Final sentence.

      """.trimIndent(),
      "This is a test. Test Another Test Inside the frame! Final sentence. ",
    )
    assertPlainText("This is good\\dots No, it isn't.\n", "This is good\u2026 No, it isn't. ")
    assertPlainText("This is a test of\\\\line breaks.\n", "This is a test of line breaks. ")
    assertPlainText(
      """
      This is a sentence.%

      This is another sentence.

      """.trimIndent(),
      "This is a sentence.\n\nThis is another sentence. ",
    )
    assertPlainText(
      """
      This is a sentence.%

      This is another sentence.

      """.trimIndent(),
      "This is a sentence.\n\nThis is another sentence. ",
    )
    assertPlainText("This is a \\textcolor{mittelblau}{test}.\n", "This is a test. ")
    assertPlainText("This is a \\raisebox{-0.5\\height-0.5mm}{test}.\n", "This is a test. ")
    assertPlainText("This is a &test.\n", "This is a test. ")
    assertPlainText(
      "You can see this in \\hyperref[alg:abc]{Sec.\\ \\ref*{alg:abc}}.\n",
      "You can see this in Sec. Dummy0. ",
    )
    assertPlainText(
      "This is a te\\-st. Another te\"-st. Donau\"=Dampf\"\"schiff\"~Fahrt.\n",
      "This is a test. Another test. Donau-Dampfschiff-Fahrt. ",
    )
    assertPlainText(
      "Ich hei\\ss{}e anders. Das Wasser ist hei\\ss.\n",
      "Ich hei\u00dfe anders. Das Wasser ist hei\u00df. ",
    )
    assertPlainText(
      "Das macht dann 10 \\euro. Oder z.\\,B. vielleicht doch 12~\\euro{}?\n",
      "Das macht dann 10 \u20ac. Oder z.\u202fB. vielleicht doch 12\u00a0\u20ac? ",
    )
    assertPlainText(
      """
      \"E\"in T\"ext m\"{i}t v\"i\"{e}l\"en \"{U}ml\"a\"{u}t\"en.

      """.trimIndent(),
      "\u00cb\u00efn T\u00ebxt m\u00eft v\u00ef\u00ebl\u00ebn \u00dcml\u00e4\u00fct\u00ebn. ",
    )
    assertPlainText(
      "\\AA\\L\\O\\SS",
      "\u00c5\u0141\u00d8\u1e9e",
    )
    assertPlainText(
      "\\aa\\i\\j\\l\\o\\ss",
      "\u00e5\u0131\u0237\u0142\u00f8\u00df",
    )
    assertPlainText(
      "\\`A\\'A\\^A\\~A\\\"A\\=A\\.A"
      + "\\H{O}\\b{B}\\c{C}\\d{A}\\k{A}\\r{A}\\u{A}\\v{C}",
      "\u00c0\u00c1\u00c2\u00c3\u00c4\u0100\u0226"
      + "\u0150\u1e06\u00c7\u1ea0\u0104\u00c5\u0102\u010c",
    )
    assertPlainText(
      "\\`a\\'a\\^a\\~a\\\"a\\=a\\.a"
          + "\\H{o}\\b{b}\\c{c}\\d{a}\\k{a}\\r{a}\\u{a}\\v{c}",
      "\u00e0\u00e1\u00e2\u00e3\u00e4\u0101\u0227"
          + "\u0151\u1e07\u00e7\u1ea1\u0105\u00e5\u0103\u010d",
    )
    assertPlainText(
      "Nih\\'o\u014b\\=go \\u{y}",
      "Nih\u00f3\u014b\u1e21o y\u0306",
    )
    assertPlainText(
      "This is a test: a, b, \\dots, c.\n",
      "This is a test: a, b, \u2026, c. ",
    )
    assertPlainText(
      "This is a test -- this is another test --- this is the final test.\n",
      "This is a test \u2013 this is another test \u2014 this is the final test. ",
    )
    assertPlainText(
      "This ``is'' a \"`test.\"'\n",
      "This \u201cis\u201d a \u201etest.\u201c ",
    )
    assertPlainText(
      """
      \section{Heading}
      This is a test.
      \subsection[abc]{This is another heading.}
      This is another test.

      """.trimIndent(),
      "Heading. This is a test. This is another heading. This is another test. ",
    )
    assertPlainText(
      """
      This is a test: \cite{test1}, \cite[a]{test2}, \cite[a][b]{test3}.
      \textcites{test1}{test2}{test3} shows that this should be plural.
      \textcites(a)(b)[c][]{test1}[][d]{test2}[e][f]{test3} proves another error.

      """.trimIndent(),
      "This is a test: Dummy0, Dummy1, Dummy2. "
      + "Dummies shows that this should be plural. "
      + "Dummies proves another error. ",
    )
    assertPlainText(
      "\\cites{test}",
      "Dummies",
    )
    assertPlainText(
      """
      This is a test, \egc an actual test \eg{} test.
      This is a test, \iec an actual test \ie{} test.

      """.trimIndent(),
      "This is a test, e.g., an actual test e.g. test. "
      + "This is a test, i.e., an actual test i.e. test. ",
    )
    assertPlainText(
      """
      This is a test.
      \begin{textblock*}{1mm}[2mm,3mm](4mm,5mm)
        abc\end{textblock*}
      This is another test.

      """.trimIndent(),
      "This is a test. abc This is another test. ",
    )
    assertPlainText(
      "\\setcounter{a}[b]{c} This is an test.\n",
      "a[b]c This is an test. ",
    )
    assertPlainText(
      "This is a test: \\colorbox{abc}{def}.\n",
      "This is a test: def. ",
    )
    assertPlainText(
      "This is a test: \\colorbox{abc}{def}.\n",
      "This is a test: Dummy0def. ",
      Settings(_latexCommands = mapOf(Pair("\\colorbox{}", "dummy"))),
    )
    assertPlainText(
      "This is a test: \\foobar{abc}{def}.\n",
      "This is a test: abc def. ",
    )
    assertPlainText(
      "This is a test: \\foobar{abc}{def}.\n",
      "This is a test: abc def. ",
      Settings(_latexCommands = mapOf(Pair("\\foobar{}{}", "default"))),
    )
    assertPlainText(
      "This is a test: \\foobar{abc}{def}.\n",
      "This is a test: . ",
      Settings(_latexCommands = mapOf(Pair("\\foobar{}{}", "ignore"))),
    )
    assertPlainText(
      "This is a test: \\foobar{abc}{def}.\n",
      "This is a test: Dummy0. ",
      Settings(_latexCommands = mapOf(Pair("\\foobar{}{}", "dummy"))),
    )
    assertPlainText(
      "This is a test: \\foobar{abc}{def}.\n",
      "This is a test: Dummies. ",
      Settings(_latexCommands = mapOf(Pair("\\foobar{}{}", "pluralDummy"))),
    )
    assertPlainText(
      "This is a test: \\foobar{abc}{def}.\n",
      "This is a test: Ina0. ",
      Settings(_latexCommands = mapOf(Pair("\\foobar{}{}", "vowelDummy"))),
    )
    assertPlainText(
      "This is a test: \\begin{foobar}{abc}def\\end{foobar}.\n",
      "This is a test: def. ",
    )
    assertPlainText(
      "This is a test: \\begin{foobar}{abc}def\\end{foobar}.\n",
      "This is a test: def. ",
      Settings(_latexEnvironments = mapOf(Pair("foobar", "default"))),
    )
    assertPlainText(
      "This is a test: \\begin{foobar}{abc}def\\end{foobar}.\n",
      "This is a test: abcdef. ",
      Settings(_latexEnvironments = mapOf(Pair("\\begin{foobar}", "default"))),
    )
    assertPlainText(
      "This is a test: \\begin{foobar}{abc}def\\end{foobar}.\n",
      "This is a test: def. ",
      Settings(_latexEnvironments = mapOf(Pair("\\begin{foobar}{}", "default"))),
    )
    assertPlainText(
      "This is a test: \\begin{foobar}{abc}def\\end{foobar}.\n",
      "This is a test: . ",
      Settings(_latexEnvironments = mapOf(Pair("foobar", "ignore"))),
    )

    run {
      val annotatedText = buildAnnotatedText("\\cite{Kubota}*{Theorem 3.7}\n")
      val start = annotatedText.getOriginalTextPositionFor(5, false)
      val end = annotatedText.getOriginalTextPositionFor(8, true)
      assertTrue(start < end, "$start not smaller than $end")
    }

    run {
      val annotatedText = buildAnnotatedText("\\v{S}ekki\n")
      assertEquals(0, annotatedText.getOriginalTextPositionFor(0, false))
      assertEquals(5, annotatedText.getOriginalTextPositionFor(1, false))
      assertEquals(6, annotatedText.getOriginalTextPositionFor(2, false))
      assertEquals(7, annotatedText.getOriginalTextPositionFor(3, false))
      assertEquals(8, annotatedText.getOriginalTextPositionFor(4, false))
    }
  }

  @Test
  fun testTikzMode() {
    assertPlainText("This is a \\tikzset{bla}test.\n", "This is a test. ")
    assertPlainText(
      """
      This is a test.
      \begin{tikzpicture}
        \node[color=mittelblau] at (42mm,0mm) {qwerty};
      \end{tikzpicture}
      This is another sentence.

      """.trimIndent(),
      "This is a test. This is another sentence. ",
    )
    assertPlainText(
      """
      This is a test:
      \begin{tikzpicture}
        \node {$\dots$};
        \node {${"$"}a$};
      \end{tikzpicture}

      """.trimIndent(),
      "This is a test: ",
    )
  }

  @Test
  @Suppress("LongMethod")
  fun testMathMode() {
    assertPlainText(
      """
      Recall that
      \begin{equation*}
        \begin{cases}
          a&\text{if ${"$"}b$,}\\
          c&\text{otherwise.}
        \end{cases}
      \end{equation*}
      Now we argue.

      """.trimIndent(),
      "Recall that Dummy0 if Dummy1, Dummy2 otherwise. Now we argue. ",
    )
    assertPlainText("This equals \$a^{b}$.\n", "This equals Ina0. ")
    assertPlainText(
      """
      This is the proof:
      \begin{equation}
          a^2 + b^2 = c^2\hspace*{10mm}.\quad\qed
      \end{equation}

      """.trimIndent(),
      "This is the proof: Dummy0. ",
    )
    assertPlainText(
      """
      This is another proof:
      \begin{equation}
          a^2 + b^2 = c^2.\\[-6.4em]\qquad\notag
      \end{equation}

      """.trimIndent(),
      "This is another proof: Dummy0. ",
    )
    assertPlainText(
      """
      This equals
      \begin{equation}
        \begin{split}
          abcdef.
        \end{split}
      \end{equation}
      This is the next sentence.

      """.trimIndent(),
      "This equals Dummy0. This is the next sentence. ",
    )
    assertPlainText(
      """
      This is an equation:
      \begin{equation}
          a^2 + b^2 = c^2,\qquad\text{which proves the theorem.}\end{equation}%
      This is a sentence.

      """.trimIndent(),
      "This is an equation: Dummy0, which proves the theorem. This is a sentence. ",
    )
    assertPlainText(
      """
      This is a test:
      \begin{equation*}
        a \text{,~and} b.
      \end{equation*}

      """.trimIndent(),
      "This is a test: Dummy0,\u00a0and Dummy1. ",
    )
    assertPlainText(
      """
      This is a test:
      \begin{equation}
          Gau\ss{}: \O(n^2).
      \end{equation}
      This is another test: ${"$"}Gau\ss{}: \O(n^2)$.

      """.trimIndent(),
      "This is a test: Dummy0. This is another test: Dummy1. ",
    )
    assertPlainText(
      """
      This is a test:
      \[
        E = mc^2.
      \]
      And this is another one: \(c^2\).

      """.trimIndent(),
      "This is a test: Dummy0. And this is another one: Dummy1. ",
    )
    assertPlainText(
      """
      This is a test: ${"$"}a = b \footnote{This is another test: ${"$"}c$.}$.
      This is the next sentence: ${"$"}E = mc^2$.

      """.trimIndent(),
      "This is a test: Ina0. This is the next sentence: Ina1. ",
    )
    assertPlainText(
      """
      This is a test: ${"$"}a, b, \dots, c$.
      Second sentence: a, b, $\dots$, c.

      """.trimIndent(),
      "This is a test: Ina0. Second sentence: a, b, Dummy1, c. ",
    )
    assertPlainText(
      "C'est un test: \$E = mc^2$.\n",
      "C'est un test: Jimmy-0. ",
      Settings(_languageShortCode = "fr"),
    )
    assertPlainText(
      """
      This is an ${"$"}A$, ${"$"}e$, ${"$"}F$, ${"$"}h$, ${"$"}I$, ${"$"}l$, ${"$"}M$,
      ${"$"}n$, ${"$"}O$, ${"$"}r$, ${"$"}S$, ${"$"}X$, $\ell$, $\mathcal{r}$.
      This is not a ${"$"}b$, ${"$"}C$, $\ella$, $\test a$, $\mathcal{b}$.

      """.trimIndent(),
      "This is an Ina0, Ina1, Ina2, Ina3, Ina4, Ina5, Ina6, "
      + "Ina7, Ina8, Ina9, Ina10, Ina11, Ina12, Ina13. "
      + "This is not a Dummy14, Dummy15, Dummy16, Dummy17, Dummy18. ",
    )

    assertOriginalTextPositions(
      """
      This is a test:
      \begin{equation}
        \scalebox{0.92}{${"$"}a$}.
      \end{equation}
      This is a sentence.

      """.trimIndent(),
      29,
      31,
    )
    assertOriginalTextPositions(
      """
      This is a test:
      \begin{equation*}
        a \text{,~and} b.
      \end{equation*}

      """.trimIndent(),
      22,
      24,
    )
    assertOriginalTextPositions(
      """
      abc. Let ${"$"}$\footnote{${"$"}a$.}${"$"}$

      abc

      """.trimIndent(),
      16,
      18,
    )
    assertPlainTextPositions(
      """
      \begin{equation}
        X
      \end{equation}
      ${"$"}a$ ${"$"}b$ ${"$"}c$ ${"$"}d$ ${"$"}e$ ${"$"}f$ ${"$"}g$
      \ref{foo} \ref{bar} 5${"$"}\times$5

      """.trimIndent(),
      84,
      90,
      Settings(_languageShortCode = "fr"),
    )
  }

  @Test
  fun testRsweaveMode() {
    assertPlainText(
      """
      \SweaveOpts{prefix.string=figures}
      This is a first sentence.

      <<import-packages, echo=false>>=
      library(tidyverse, quietly = T)
      @

      This is a second sentence.
      <<mca-graph, fig=true, echo=false>>=
      plot(1:1000, rnorm(1000))
      @

      """.trimIndent(),
      " This is a first sentence.\n\n\n\nThis is a second sentence. ",
      "rsweave",
    )
    assertPlainText(
      """
      <<import-packages>>=
      library(tidyverse)
      @

      """.trimIndent(),
      " ",
      "rsweave",
    )
    assertPlainText(
      """
      <<import-packages>>=
      library(tidyverse)
      @

      """.trimIndent(),
      "<<import-packages>>= library(tidyverse) @ ",
    )
  }

  private fun assertOriginalTextPositions(
    code: String,
    plainTextStartPos: Int,
    plainTextEndPos: Int,
    settings: Settings = Settings(),
  ) {
    val annotatedText: AnnotatedText = buildAnnotatedText(code, settings)
    val originalTextStartPos: Int =
        annotatedText.getOriginalTextPositionFor(plainTextStartPos, false)
    val originalTextEndPos: Int =
        annotatedText.getOriginalTextPositionFor(plainTextEndPos, true)
    assertTrue(originalTextStartPos < originalTextEndPos)
  }

  private fun assertPlainTextPositions(
    code: String,
    originalTextStartPos: Int,
    originalTextEndPos: Int,
    settings: Settings = Settings(),
  ) {
    val invertedAnnotatedText: AnnotatedText =
        AnnotatedTextFragment.invertAnnotatedText(buildAnnotatedText(code, settings))
    val plainStartPos: Int = AnnotatedTextFragment.getOriginalTextPosition(
      invertedAnnotatedText,
      originalTextStartPos,
      false,
    )
    val plainEndPos: Int = AnnotatedTextFragment.getOriginalTextPosition(
      invertedAnnotatedText,
      originalTextEndPos,
      true,
    )
    assertTrue(plainStartPos < plainEndPos)
  }
}
