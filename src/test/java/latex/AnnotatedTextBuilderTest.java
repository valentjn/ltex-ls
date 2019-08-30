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
  }
}
