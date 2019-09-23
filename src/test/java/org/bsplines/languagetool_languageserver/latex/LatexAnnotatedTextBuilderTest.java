package org.bsplines.languagetool_languageserver.latex;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.languagetool.markup.AnnotatedText;

public class LatexAnnotatedTextBuilderTest {
  static AnnotatedText buildAnnotatedText(String code) {
    try {
      return (new latex.AnnotatedTextBuilder()).addCode(code).getAnnotatedText();
    } catch (InterruptedException e) {
      e.printStackTrace();
      return null;
    }
  }

  static void assertPlainText(String code, String expectedPlainText) {
    AnnotatedText annotatedText = buildAnnotatedText(code);
    Assertions.assertEquals(expectedPlainText, annotatedText.getPlainText());
  }

  @Test
  void testTextMode() {
    assertPlainText(
        "We can do\n" +
        "\\begin{itemize}\n" +
        "  \\item this or\n" +
        "  \\item that.\n" +
        "\\end{itemize}\n",
        "We can do this or that. ");
    assertPlainText("This is good\\dots No, it isn't.\n", "This is good... No, it isn't. ");
    assertPlainText("This is a test of\\\\line breaks.\n", "This is a test of line breaks. ");
    assertPlainText(
        "This is a sentence.%\n" +
        "\n" +
        "This is another sentence.\n",
        "This is a sentence.\n\nThis is another sentence. ");
    assertPlainText("This is a \\textcolor{mittelblau}{test}.\n", "This is a test. ");
    assertPlainText("This is a \\raisebox{-0.5\\height-0.5mm}{test}.\n", "This is a test. ");
    assertPlainText("This is a &test.\n", "This is a test. ");
    assertPlainText("You can see this in \\hyperref[alg:abc]{Sec.\\ \\ref*{alg:abc}}.\n",
        "You can see this in Sec. Dummy0. ");
    assertPlainText("This is a te\\-st.\n", "This is a test. ");
    assertPlainText("Ich hei\\ss{}e anders. Das Wasser ist hei\\ss.\n",
        "Ich hei\u00dfe anders. Das Wasser ist hei\u00df. ");
    assertPlainText(
        "\\\"E\\\"in T\\\"ext m\\\"{i}t v\\\"i\\\"{e}l\\\"en " +
        "\\\"{U}ml\\\"a\\\"{u}t\\\"en.\n",
        "\u00cb\u00efn T\u00ebxt m\u00eft v\u00ef\u00ebl\u00ebn " +
        "\u00dcml\u00e4\u00fct\u00ebn. ");
  }

  @Test
  void testTikzMode() {
    assertPlainText("This is a \\tikzset{bla}test.\n", "This is a test. ");
    assertPlainText(
        "This is a test.\n" +
        "\\begin{tikzpicture}\n" +
        "  \\node[color=mittelblau] at (42mm,0mm) {qwerty};\n" +
        "\\end{tikzpicture}\n" +
        "This is another sentence.\n",
        "This is a test. This is another sentence. ");
  }

  @Test
  void testMathMode() {
    assertPlainText(
        "Recall that\n" +
        "\\begin{equation*}\n" +
        "  \\begin{cases}\n" +
        "    a&\\text{if $b$,}\\\\\n" +
        "    c&\\text{otherwise.}\n" +
        "  \\end{cases}\n" +
        "\\end{equation*}\n" +
        "Now we argue.\n",
        "Recall that Dummy0 if Dummy1, Dummy2 otherwise. Now we argue. ");
    assertPlainText("This equals $a^{b}$.\n", "This equals Dummy0. ");
    assertPlainText(
        "This is the proof:\n" +
        "\\begin{equation}\n" +
        "    a^2 + b^2 = c^2\\hspace*{10mm}.\\quad\\qed\n" +
        "\\end{equation}\n",
        "This is the proof: Dummy0. ");
    assertPlainText(
        "This is another proof:\n" +
        "\\begin{equation}\n" +
        "    a^2 + b^2 = c^2.\\\\[-6.4em]\\qquad\\notag\n" +
        "\\end{equation}\n",
        "This is another proof: Dummy0. ");
    assertPlainText(
        "This equals\n" +
        "\\begin{equation}\n" +
        "  \\begin{split}\n" +
        "    abcdef.\n" +
        "  \\end{split}\n" +
        "\\end{equation}\n" +
        "This is the next sentence.\n",
        "This equals Dummy0. This is the next sentence. ");
    assertPlainText(
        "This is an equation:\n" +
        "\\begin{equation}\n" +
        "    a^2 + b^2 = c^2,\\qquad\\text{which proves the theorem.}" +
        "\\end{equation}%\n" +
        "This is a sentence.\n",
        "This is an equation: Dummy0, which proves the theorem. " +
        "This is a sentence. ");
    assertPlainText(
        "This is a test:\n" +
        "\\begin{equation*}\n" +
        "  a \\text{,~and} b.\n" +
        "\\end{equation*}\n",
        "This is a test: Dummy0, and Dummy1. ");

    {
      AnnotatedText annotatedText = buildAnnotatedText(
          "This is a test:\n" +
          "\\begin{equation}\n" +
          "  \\scalebox{0.92}{$a$}.\n" +
          "\\end{equation}\n" +
          "This is a sentence.\n");
      int start = annotatedText.getOriginalTextPositionFor(29);
      int end = annotatedText.getOriginalTextPositionFor(31 - 1) + 1;
      Assertions.assertTrue(start < end, start + " not smaller than " + end);
    }

    {
      AnnotatedText annotatedText = buildAnnotatedText(
          "This is a test:\n" +
          "\\begin{equation*}\n" +
          "  a \\text{,~and} b.\n" +
          "\\end{equation*}\n");
      int start = annotatedText.getOriginalTextPositionFor(22);
      int end = annotatedText.getOriginalTextPositionFor(24 - 1) + 1;
      Assertions.assertTrue(start < end, start + " not smaller than " + end);
    }
  }
}
