/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

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
    CodeAnnotatedTextBuilder builder = CodeAnnotatedTextBuilder.create("markdown");
    Settings settings = (new Settings()).withIgnoreMarkdownNodeTypes(ignoreNodeTypes)
        .withDummyMarkdownNodeTypes(dummyNodeTypes);
    builder.setSettings(settings);
    return builder.addCode(code).build();
  }

  @Test
  public void test() throws IOException {
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
    assertPlainText(
        "---\n"
        + "# This is YAML front matter\n"
        + "- test\n"
        + "---\n\n"
        + "# Heading\n"
        + "Test sentence\n",
        "\nHeading\nTest sentence\n");
    assertPlainText(
        "---\n"
        + "test: This is a test.\n"
        + "---\n\n"
        + "# Heading\n"
        + "Test sentence\n",
        "\nHeading\nTest sentence\n");
  }
}
