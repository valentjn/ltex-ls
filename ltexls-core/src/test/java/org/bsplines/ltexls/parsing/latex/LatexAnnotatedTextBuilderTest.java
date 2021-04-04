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
        "\\`A\\`E\\`I\\`O\\`U",
        "\u00c0\u00c8\u00cc\u00d2\u00d9");
    assertPlainText(
        "\\`a\\`e\\`i\\`\\i\\`o\\`u",
        "\u00e0\u00e8\u00ec\u00ec\u00f2\u00f9");
    assertPlainText(
        "\\'A\\'E\\'I\\'O\\'U\\'Y",
        "\u00c1\u00c9\u00cd\u00d3\u00da\u00dd");
    assertPlainText(
        "\\'a\\'e\\'i\\'\\i\\'o\\'u\\'y",
        "\u00e1\u00e9\u00ed\u00ed\u00f3\u00fa\u00fd");
    assertPlainText(
        "\\^A\\^E\\^I\\^O\\^U\\^Y",
        "\u00c2\u00ca\u00ce\u00d4\u00db\u0176");
    assertPlainText(
        "\\^a\\^e\\^i\\^\\i\\^j\\^\\j\\^o\\^u\\^y",
        "\u00e2\u00ea\u00ee\u00ee\u0135\u0135\u00f4\u00fb\u0177");
    assertPlainText(
        "\\~A\\~E\\~I\\~N\\~O\\~U",
        "\u00c3\u1ebc\u0128\u00d1\u00d5\u0168");
    assertPlainText(
        "\\~a\\~e\\~i\\~\\i\\~n\\~o\\~u",
        "\u00e3\u1ebd\u0129\u0129\u00f1\u00f5\u0169");
    assertPlainText(
        "\\\"A\\\"E\\\"I\\\"O\\\"U\\\"Y",
        "\u00c4\u00cb\u00cf\u00d6\u00dc\u0178");
    assertPlainText(
        "\\\"a\\\"e\\\"i\\\"\\i\\\"o\\\"u\\\"y",
        "\u00e4\u00eb\u00ef\u00ef\u00f6\u00fc\u00ff");
    assertPlainText(
        "\\=A\\=E\\=I\\=O\\=U\\=Y",
        "\u0100\u0112\u012a\u014c\u016a\u0232");
    assertPlainText(
        "\\=a\\=e\\=i\\=\\i\\=o\\=u\\=y",
        "\u0101\u0113\u012b\u012b\u014d\u016b\u0233");
    assertPlainText(
        "\\.A\\.E\\.I\\.O",
        "\u0226\u0116\u0130\u022e");
    assertPlainText(
        "\\.a\\.e\\.o",
        "\u0227\u0117\u022f");
    assertPlainText(
        "\\H{O}\\H{U}\\H{o}\\H{u}",
        "\u0150\u0170\u0151\u0171");
    assertPlainText(
        "\\b{B}\\b{D}\\b{K}\\b{L}\\b{N}\\b{R}\\b{T}\\b{Z}",
        "\u1e06\u1e0e\u1e34\u1e3a\u1e48\u1e5e\u1e6e\u1e94");
    assertPlainText(
        "\\b{b}\\b{d}\\b{h}\\b{k}\\b{l}\\b{n}\\b{r}\\b{t}\\b{z}",
        "\u1e07\u1e0f\u1e96\u1e35\u1e3b\u1e49\u1e5f\u1e6f\u1e95");
    assertPlainText(
        "\\c{C}\\c{D}\\c{E}\\c{G}\\c{H}\\c{K}\\c{L}\\c{N}\\c{R}\\c{S}\\c{T}",
        "\u00c7\u1e10\u0228\u0122\u1e28\u0136\u013b\u0145\u0156\u015e\u0162");
    assertPlainText(
        "\\c{c}\\c{d}\\c{e}\\c{g}\\c{h}\\c{k}\\c{l}\\c{n}\\c{r}\\c{s}\\c{t}",
        "\u00e7\u1e11\u0229\u0123\u1e29\u0137\u013c\u0146\u0157\u015f\u0163");
    assertPlainText(
        "\\d{A}\\d{E}\\d{I}\\d{O}\\d{U}\\d{Y}",
        "\u1ea0\u1eb8\u1eca\u1ecc\u1ee4\u1ef4");
    assertPlainText(
        "\\d{a}\\d{e}\\d{i}\\d{o}\\d{u}\\d{y}",
        "\u1ea1\u1eb9\u1ecb\u1ecd\u1ee5\u1ef5");
    assertPlainText(
        "\\k{A}\\k{E}\\k{I}\\k{O}\\k{U}",
        "\u0104\u0118\u012e\u01ea\u0172");
    assertPlainText(
        "\\k{a}\\k{e}\\k{i}\\k{o}\\k{u}",
        "\u0105\u0119\u012f\u01eb\u0173");
    assertPlainText(
        "\\r{A}\\r{U}",
        "\u00c5\u016e");
    assertPlainText(
        "\\r{a}\\r{u}",
        "\u00e5\u016f");
    assertPlainText(
        "\\u{A}\\u{E}\\u{G}\\u{I}\\u{O}\\u{U}",
        "\u0102\u0114\u011e\u012c\u014e\u016c");
    assertPlainText(
        "\\v{C}\\v{D}\\v{E}\\v{L}\\v{N}\\v{R}\\v{S}\\v{T}\\v{Z}",
        "\u010c\u010e\u011a\u013d\u0147\u0158\u0160\u0164\u017d");
    assertPlainText(
        "\\v{c}\\v{d}\\v{e}\\v{i}\\v{\\i}\\v{j}\\v{\\j}\\v{l}\\v{n}\\v{r}\\v{s}\\v{t}\\v{z}",
        "\u010d\u010f\u011b\u01d0\u01d0\u01f0\u01f0\u013e\u0148\u0159\u0161\u0165\u017e");
    assertPlainText(
        "\\u{a}\\u{e}\\u{g}\\u{i}\\u{\\i}\\u{o}\\u{u}",
        "\u0103\u0115\u011f\u012d\u012d\u014f\u016d");
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
