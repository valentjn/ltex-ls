package org.bsplines.ltexls.parsing.markdown;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.bsplines.ltexls.Settings;
import org.bsplines.ltexls.parsing.CodeAnnotatedTextBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.languagetool.markup.AnnotatedText;

public class MarkdownAnnotatedTextBuilderTest {
  private static void assertPlainText(String code, String expectedPlainText) {
    assertPlainText(code, expectedPlainText, Collections.emptyList(), Collections.emptyList());
  }

  private static void assertPlainText(String code, String expectedPlainText,
        List<String> ignoreNodeTypes, List<String> dummyNodeTypes) {
    AnnotatedText annotatedText = buildAnnotatedText(code, ignoreNodeTypes, dummyNodeTypes);
    Assertions.assertEquals(expectedPlainText, annotatedText.getPlainText());
  }

  private static AnnotatedText buildAnnotatedText(String code,
        List<String> ignoreNodeTypes, List<String> dummyNodeTypes) {
    MarkdownAnnotatedTextBuilder builder =
        (MarkdownAnnotatedTextBuilder)CodeAnnotatedTextBuilder.create("markdown");
    Settings settings = new Settings();
    settings.setIgnoreMarkdownNodeTypes(ignoreNodeTypes);
    settings.setDummyMarkdownNodeTypes(dummyNodeTypes);
    builder.setSettings(settings);
    return builder.addCode(code).build();
  }

  @Test
  public void doTest() throws IOException {
    assertPlainText(
        "# Heading\n"
        + "Paragraph with\n"
        + "multiple lines and [link](example.com)\n",
        "Heading\nParagraph with multiple lines and link\n");
    assertPlainText(
        "This is a test: `inline code`.\n\n```\ncode block\n```\n\nThis is another sentence.\n",
        "This is a test: inline code.\n\n\ncode block\n\n\nThis is another sentence.\n");
    assertPlainText(
        "This is a test: `inline code`.\n\n```\ncode block\n```\n\nThis is another sentence.\n",
        "This is a test: Dummy0.\n\n\n\n\n\nThis is another sentence.\n",
        Collections.singletonList("FencedCodeBlock"), Collections.singletonList("Code"));
  }
}
