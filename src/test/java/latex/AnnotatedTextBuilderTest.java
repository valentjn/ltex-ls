package latex;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.languagetool.markup.AnnotatedText;

public class AnnotatedTextBuilderTest {
  static AnnotatedText buildAnnotatedText(String code) {
    return (new latex.AnnotatedTextBuilder()).addCode(code).getAnnotatedText();
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
        "    a^2 + b^2 = c^2.\\quad\\qed\n" +
        "\\end{equation}\n",
        "This is the proof: Dummy0. ");
    assertPlainText(
        "This equals\n" +
        "\\begin{equation}\n" +
        "  \\begin{split}\n" +
        "    abcdef.\n" +
        "  \\end{split}\n" +
        "\\end{equation}\n" +
        "This is the next sentence.\n",
        "This equals Dummy0. This is the next sentence. ");
  }
}
