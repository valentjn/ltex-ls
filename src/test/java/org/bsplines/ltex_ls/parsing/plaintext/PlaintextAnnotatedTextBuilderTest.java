package org.bsplines.ltex_ls.parsing.plaintext;

import org.bsplines.ltex_ls.Settings;
import org.bsplines.ltex_ls.parsing.CodeAnnotatedTextBuilder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.languagetool.markup.AnnotatedText;

public class PlaintextAnnotatedTextBuilderTest {
  @Test
  public void test() {
    CodeAnnotatedTextBuilder builder = CodeAnnotatedTextBuilder.create("plaintext");
    builder.addCode("This is \\textbf{a} `test`.\n");
    AnnotatedText annotatedText = builder.build();
    Assertions.assertEquals("This is \\textbf{a} `test`.\n", annotatedText.getPlainText());
    Assertions.assertDoesNotThrow(() -> builder.setSettings(new Settings()));
  }
}
