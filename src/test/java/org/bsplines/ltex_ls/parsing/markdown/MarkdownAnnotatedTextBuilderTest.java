package org.bsplines.ltex_ls.parsing.markdown;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.languagetool.markup.AnnotatedText;

import java.io.IOException;

public class MarkdownAnnotatedTextBuilderTest {
  @Test
  void test() throws IOException {
    MarkdownAnnotatedTextBuilder builder = new MarkdownAnnotatedTextBuilder();
    builder.addCode("# Heading\n" +
        "Paragraph with\n" +
        "multiple lines and [link](example.com)");
    AnnotatedText text = builder.getAnnotatedText();

    Assertions.assertEquals("Heading\nParagraph with multiple lines and link", text.getPlainText());
  }
}
