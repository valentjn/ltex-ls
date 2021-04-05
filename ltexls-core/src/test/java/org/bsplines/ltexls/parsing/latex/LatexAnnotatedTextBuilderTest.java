/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.latex;

import java.util.Collections;
import org.bsplines.ltexls.parsing.CodeAnnotatedTextBuilder;
import org.bsplines.ltexls.settings.Settings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.languagetool.markup.AnnotatedText;

public class LatexAnnotatedTextBuilderTest {
  private static void assertPlainText(String code, String expectedPlainText) {
    assertPlainText(code, expectedPlainText, "en-US");
  }

  private static void assertPlainText(String code, String expectedPlainText, String language) {
    assertPlainText(code, expectedPlainText, (new Settings()).withLanguageShortCode(language));
  }

  private static void assertPlainText(String code, String expectedPlainText, Settings settings) {
    assertPlainText(code, expectedPlainText, settings, "latex");
  }

  private static void assertPlainText(String code, String expectedPlainText, Settings settings,
      String codeLanguageId) {
    AnnotatedText annotatedText = buildAnnotatedText(code, settings, codeLanguageId);
    Assertions.assertEquals(expectedPlainText, annotatedText.getPlainText());
  }

  private static AnnotatedText buildAnnotatedText(String code) {
    return buildAnnotatedText(code, new Settings(), "latex");
  }

  private static AnnotatedText buildAnnotatedText(String code, Settings settings,
        String codeLanguageId) {
    LatexAnnotatedTextBuilder builder =
        (LatexAnnotatedTextBuilder)CodeAnnotatedTextBuilder.create(codeLanguageId);
    builder.setSettings(settings);
    builder.setInStrictMode(true);
    return builder.addCode(code).build();
  }

  @Test
  public void testTextMode() {
    assertPlainText(
        "We can do\n"
        + "\\begin{itemize}[first-{test}]{[second]-test}\n"
        + "  \\item this or\n"
        + "  \\item that.\n"
        + "\\end{itemize}\n",
        "We can do this or that. ");
    assertPlainText(
        "This is a test.\n"
        + "\\begin{frame}{Test}{Another Test}\n"
        + "  Inside the frame!\n"
        + "\\end{frame}\n"
        + "Final sentence.\n",
        "This is a test. Test Another Test Inside the frame! Final sentence. ");
    assertPlainText("This is good\\dots No, it isn't.\n", "This is good\u2026 No, it isn't. ");
    assertPlainText("This is a test of\\\\line breaks.\n", "This is a test of line breaks. ");
    assertPlainText(
        "This is a sentence.%\n"
        + "\n"
        + "This is another sentence.\n",
        "This is a sentence.\n\nThis is another sentence. ");
    assertPlainText(
        "This is a sentence.%\r\n"
        + "\r\n"
        + "This is another sentence.\r\n",
        "This is a sentence.\n\nThis is another sentence. ");
    assertPlainText("This is a \\textcolor{mittelblau}{test}.\n", "This is a test. ");
    assertPlainText("This is a \\raisebox{-0.5\\height-0.5mm}{test}.\n", "This is a test. ");
    assertPlainText("This is a &test.\n", "This is a test. ");
    assertPlainText("You can see this in \\hyperref[alg:abc]{Sec.\\ \\ref*{alg:abc}}.\n",
        "You can see this in Sec. Dummy0. ");
    assertPlainText("This is a te\\-st. Another te\"-st. Donau\"=Dampf\"\"schiff\"~Fahrt.\n",
        "This is a test. Another test. Donau-Dampfschiff-Fahrt. ");
    assertPlainText("Ich hei\\ss{}e anders. Das Wasser ist hei\\ss.\n",
        "Ich hei\u00dfe anders. Das Wasser ist hei\u00df. ");
    assertPlainText("Das macht dann 10 \\euro. Oder z.\\,B. vielleicht doch 12~\\euro{}?\n",
        "Das macht dann 10 \u20ac. Oder z.\u202fB. vielleicht doch 12\u00a0\u20ac? ");
    assertPlainText(
        "\\\"E\\\"in T\\\"ext m\\\"{i}t v\\\"i\\\"{e}l\\\"en "
        + "\\\"{U}ml\\\"a\\\"{u}t\\\"en.\n",
        "\u00cb\u00efn T\u00ebxt m\u00eft v\u00ef\u00ebl\u00ebn "
        + "\u00dcml\u00e4\u00fct\u00ebn. ");
    assertPlainText(
        "\\AA\\L\\O\\SS",
        "\u00c5\u0141\u00d8\u1e9e");
    assertPlainText(
        "\\aa\\i\\j\\l\\o\\ss",
        "\u00e5\u0131\u0237\u0142\u00f8\u00df");
    assertPlainText(
        "\\`A\\'A\\^A\\~A\\\"A\\=A\\.A"
        + "\\H{O}\\b{B}\\c{C}\\d{A}\\k{A}\\r{A}\\u{A}\\v{C}",
        "\u00c0\u00c1\u00c2\u00c3\u00c4\u0100\u0226"
        + "\u0150\u1e06\u00c7\u1ea0\u0104\u00c5\u0102\u010c");
    assertPlainText(
        "\\`a\\'a\\^a\\~a\\\"a\\=a\\.a"
        + "\\H{o}\\b{b}\\c{c}\\d{a}\\k{a}\\r{a}\\u{a}\\v{c}",
        "\u00e0\u00e1\u00e2\u00e3\u00e4\u0101\u0227"
        + "\u0151\u1e07\u00e7\u1ea1\u0105\u00e5\u0103\u010d");
    assertPlainText(
        "Nih\\'o\u014b\\=go \\u{y}",
        "Nih\u00f3\u014b\u1e21o y\u0306");
    assertPlainText(
        "This is a test: a, b, \\dots, c.\n",
        "This is a test: a, b, \u2026, c. ");
    assertPlainText(
        "This is a test -- this is another test --- this is the final test.\n",
        "This is a test \u2013 this is another test \u2014 this is the final test. ");
    assertPlainText(
        "This ``is'' a \"`test.\"'\n",
        "This \u201cis\u201d a \u201etest.\u201c ");
    assertPlainText(
        "\\section{Heading}\n"
        + "This is a test.\n"
        + "\\subsection[abc]{This is another heading.}\n"
        + "This is another test.\n",
        "Heading. This is a test. This is another heading. This is another test. ");
    assertPlainText(
        "This is a test: \\cite{test1}, \\cite[a]{test2}, \\cite[a][b]{test3}.\n"
        + "\\textcites{test1}{test2}{test3} shows that this should be plural.\n"
        + "\\textcites(a)(b)[c][]{test1}[][d]{test2}[e][f]{test3} proves another error.\n",
        "This is a test: Dummy0, Dummy1, Dummy2. "
        + "Dummies shows that this should be plural. "
        + "Dummies proves another error. ");
    assertPlainText(
        "This is a test, \\egc an actual test \\eg{} test.\n"
        + "This is a test, \\iec an actual test \\ie{} test.\n",
        "This is a test, e.g., an actual test e.g. test. "
        + "This is a test, i.e., an actual test i.e. test. ");
    assertPlainText(
        "This is a test.\n"
        + "\\begin{textblock*}{1mm}[2mm,3mm](4mm,5mm)\n"
        + "  abc"
        + "\\end{textblock*}\n"
        + "This is another test.\n",
        "This is a test. abc This is another test. ");
    assertPlainText(
        "\\setcounter{a}[b]{c} This is an test.\n",
        "a[b]c This is an test. ");
    assertPlainText(
        "This is a test: \\colorbox{abc}{def}.\n",
        "This is a test: def. ");
    assertPlainText(
        "This is a test: \\colorbox{abc}{def}.\n",
        "This is a test: Dummy0def. ",
        (new Settings()).withLatexCommands(Collections.singletonMap("\\colorbox{}", "dummy")));
    assertPlainText(
        "This is a test: \\foobar{abc}{def}.\n",
        "This is a test: abc def. ");
    assertPlainText(
        "This is a test: \\foobar{abc}{def}.\n",
        "This is a test: abc def. ",
        (new Settings()).withLatexCommands(Collections.singletonMap("\\foobar{}{}", "default")));
    assertPlainText(
        "This is a test: \\foobar{abc}{def}.\n",
        "This is a test: . ",
        (new Settings()).withLatexCommands(Collections.singletonMap("\\foobar{}{}", "ignore")));
    assertPlainText(
        "This is a test: \\foobar{abc}{def}.\n",
        "This is a test: Dummy0. ",
        (new Settings()).withLatexCommands(Collections.singletonMap("\\foobar{}{}", "dummy")));
    assertPlainText(
        "This is a test: \\foobar{abc}{def}.\n",
        "This is a test: Dummies. ",
        (new Settings()).withLatexCommands(Collections.singletonMap(
          "\\foobar{}{}", "pluralDummy")));
    assertPlainText(
        "This is a test: \\begin{foobar}{abc}def\\end{foobar}.\n",
        "This is a test: def. ");
    assertPlainText(
        "This is a test: \\begin{foobar}{abc}def\\end{foobar}.\n",
        "This is a test: def. ",
        (new Settings()).withLatexEnvironments(Collections.singletonMap("foobar", "default")));
    assertPlainText(
        "This is a test: \\begin{foobar}{abc}def\\end{foobar}.\n",
        "This is a test: abcdef. ",
        (new Settings()).withLatexEnvironments(Collections.singletonMap(
          "\\begin{foobar}", "default")));
    assertPlainText(
        "This is a test: \\begin{foobar}{abc}def\\end{foobar}.\n",
        "This is a test: def. ",
        (new Settings()).withLatexEnvironments(Collections.singletonMap(
          "\\begin{foobar}{}", "default")));
    assertPlainText(
        "This is a test: \\begin{foobar}{abc}def\\end{foobar}.\n",
        "This is a test: . ",
        (new Settings()).withLatexEnvironments(Collections.singletonMap("foobar", "ignore")));

    {
      AnnotatedText annotatedText = buildAnnotatedText("\\cite{Kubota}*{Theorem 3.7}\n");
      int start = annotatedText.getOriginalTextPositionFor(5);
      int end = annotatedText.getOriginalTextPositionFor(8 - 1) + 1;
      Assertions.assertTrue(start < end, start + " not smaller than " + end);
    }
  }

  @Test
  public void testTikzMode() {
    assertPlainText("This is a \\tikzset{bla}test.\n", "This is a test. ");
    assertPlainText(
        "This is a test.\n"
        + "\\begin{tikzpicture}\n"
        + "  \\node[color=mittelblau] at (42mm,0mm) {qwerty};\n"
        + "\\end{tikzpicture}\n"
        + "This is another sentence.\n",
        "This is a test. This is another sentence. ");
    assertPlainText(
        "This is a test:\n"
        + "\\begin{tikzpicture}\n"
        + "  \\node {$\\dots$};\n"
        + "  \\node {$a$};\n"
        + "\\end{tikzpicture}\n",
        "This is a test: ");
  }

  @Test
  public void testMathMode() {
    assertPlainText(
        "Recall that\n"
        + "\\begin{equation*}\n"
        + "  \\begin{cases}\n"
        + "    a&\\text{if $b$,}\\\\\n"
        + "    c&\\text{otherwise.}\n"
        + "  \\end{cases}\n"
        + "\\end{equation*}\n"
        + "Now we argue.\n",
        "Recall that Dummy0 if Dummy1, Dummy2 otherwise. Now we argue. ");
    assertPlainText("This equals $a^{b}$.\n", "This equals Ina0. ");
    assertPlainText(
        "This is the proof:\n"
        + "\\begin{equation}\n"
        + "    a^2 + b^2 = c^2\\hspace*{10mm}.\\quad\\qed\n"
        + "\\end{equation}\n",
        "This is the proof: Dummy0. ");
    assertPlainText(
        "This is another proof:\n"
        + "\\begin{equation}\n"
        + "    a^2 + b^2 = c^2.\\\\[-6.4em]\\qquad\\notag\n"
        + "\\end{equation}\n",
        "This is another proof: Dummy0. ");
    assertPlainText(
        "This equals\n"
        + "\\begin{equation}\n"
        + "  \\begin{split}\n"
        + "    abcdef.\n"
        + "  \\end{split}\n"
        + "\\end{equation}\n"
        + "This is the next sentence.\n",
        "This equals Dummy0. This is the next sentence. ");
    assertPlainText(
        "This is an equation:\n"
        + "\\begin{equation}\n"
        + "    a^2 + b^2 = c^2,\\qquad\\text{which proves the theorem.}"
        + "\\end{equation}%\n"
        + "This is a sentence.\n",
        "This is an equation: Dummy0, which proves the theorem. "
        + "This is a sentence. ");
    assertPlainText(
        "This is a test:\n"
        + "\\begin{equation*}\n"
        + "  a \\text{,~and} b.\n"
        + "\\end{equation*}\n",
        "This is a test: Dummy0,\u00a0and Dummy1. ");
    assertPlainText(
        "This is a test:\n"
        + "\\begin{equation}\n"
        + "    Gau\\ss{}: \\O(n^2).\n"
        + "\\end{equation}\n"
        + "This is another test: $Gau\\ss{}: \\O(n^2)$.\n",
        "This is a test: Dummy0. This is another test: Dummy1. ");
    assertPlainText(
        "This is a test:\n"
        + "\\[\n"
        + "  E = mc^2.\n"
        + "\\]\n"
        + "And this is another one: \\(c^2\\).\n",
        "This is a test: Dummy0. And this is another one: Dummy1. ");
    assertPlainText(
        "This is a test: $a = b \\footnote{This is another test: $c$.}$.\n"
        + "This is the next sentence: $E = mc^2$.\n",
        "This is a test: Ina0. This is the next sentence: Ina1. ");
    assertPlainText(
        "This is a test: $a, b, \\dots, c$.\n"
        + "Second sentence: a, b, $\\dots$, c.\n",
        "This is a test: Ina0. Second sentence: a, b, Dummy1, c. ");
    assertPlainText(
        "C'est un test: $E = mc^2$.\n",
        "C'est un test: Jimmy-0. ",
        "fr");
    assertPlainText(
        "This is an $A$, $e$, $F$, $h$, $I$, $l$, $M$, "
        + "$n$, $O$, $r$, $S$, $X$, $\\ell$, $\\mathcal{r}$.\n"
        + "This is not a $b$, $C$, $\\ella$, $\\test a$, $\\mathcal{b}$.\n",
        "This is an Ina0, Ina1, Ina2, Ina3, Ina4, Ina5, Ina6, "
        + "Ina7, Ina8, Ina9, Ina10, Ina11, Ina12, Ina13. "
        + "This is not a Dummy14, Dummy15, Dummy16, Dummy17, Dummy18. ");

    {
      AnnotatedText annotatedText = buildAnnotatedText(
          "This is a test:\n"
          + "\\begin{equation}\n"
          + "  \\scalebox{0.92}{$a$}.\n"
          + "\\end{equation}\n"
          + "This is a sentence.\n");
      int start = annotatedText.getOriginalTextPositionFor(29);
      int end = annotatedText.getOriginalTextPositionFor(31 - 1) + 1;
      Assertions.assertTrue(start < end, start + " not smaller than " + end);
    }

    {
      AnnotatedText annotatedText = buildAnnotatedText(
          "This is a test:\n"
          + "\\begin{equation*}\n"
          + "  a \\text{,~and} b.\n"
          + "\\end{equation*}\n");
      int start = annotatedText.getOriginalTextPositionFor(22);
      int end = annotatedText.getOriginalTextPositionFor(24 - 1) + 1;
      Assertions.assertTrue(start < end, start + " not smaller than " + end);
    }

    {
      AnnotatedText annotatedText = buildAnnotatedText(
          "abc. Let $$\\footnote{$a$.}$$\n"
          + "\n"
          + "abc\n");
      int start = annotatedText.getOriginalTextPositionFor(16);
      int end = annotatedText.getOriginalTextPositionFor(18 - 1) + 1;
      Assertions.assertTrue(start < end, start + " not smaller than " + end);
    }
  }

  @Test
  public void testRsweaveMode() {
    assertPlainText(
        "\\SweaveOpts{prefix.string=figures}\n"
        + "This is a first sentence.\n"
        + "\n"
        + "<<import-packages, echo=false>>=\n"
        + "library(tidyverse, quietly = T)\n"
        + "@\n"
        + "\n"
        + "This is a second sentence.\n"
        + "<<mca-graph, fig=true, echo=false>>=\n"
        + "plot(1:1000, rnorm(1000))\n"
        + "@\n",
        " This is a first sentence.\n\n\n\nThis is a second sentence. ",
        new Settings(), "rsweave");
    assertPlainText("<<import-packages>>=\n"
        + "library(tidyverse)\n"
        + "@\n",
        " ",
        new Settings(), "rsweave");
    assertPlainText("<<import-packages>>=\n"
        + "library(tidyverse)\n"
        + "@\n",
        "<<import-packages>>= library(tidyverse) @ ");
  }
}
