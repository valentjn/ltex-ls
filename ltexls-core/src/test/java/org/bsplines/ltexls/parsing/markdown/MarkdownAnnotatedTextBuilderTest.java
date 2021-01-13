/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.markdown;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.bsplines.ltexls.parsing.CodeAnnotatedTextBuilder;
import org.bsplines.ltexls.settings.Settings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.languagetool.markup.AnnotatedText;

public class MarkdownAnnotatedTextBuilderTest {
  private static void assertPlainText(String code, String expectedPlainText) {
    assertPlainText(code, expectedPlainText, Collections.emptyMap());
  }

  private static void assertPlainText(String code, String expectedPlainText,
        Map<String, String> markdownNodes) {
    AnnotatedText annotatedText = buildAnnotatedText(code, markdownNodes);
    Assertions.assertEquals(expectedPlainText, annotatedText.getPlainText());
  }

  private static AnnotatedText buildAnnotatedText(String code, Map<String, String> markdownNodes) {
    CodeAnnotatedTextBuilder builder = CodeAnnotatedTextBuilder.create("markdown");
    Settings settings = (new Settings()).withMarkdownNodes(markdownNodes);
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
        "# This is a &copy; Test\n"
        + "Another [day &ndash; another](example.com) sentence\n",
        "This is a \u00a9 Test\nAnother day \u2013 another sentence\n");
    assertPlainText(
        "This is a test: `inline code`.\n\n```\ncode block\n```\n\nThis is another sentence.\n",
        "This is a test: Dummy0.\n\n\n\n\n\nThis is another sentence.\n");
    assertPlainText(
        "This is a test: $`E = mc^2`$.\n\n```math\na^2 + b^2 = c^2\n```\n\nThis is another test.\n",
        "This is a test: Dummy0.\n\n\n\n\n\nThis is another test.\n");
    assertPlainText(
        "This is a test.\n"
        + "\n"
        + "| First Column | Second Column |\n"
        + "| ------------ | ------------- |\n"
        + "| Interesting  | Super         |\n"
        + "\n"
        + "This is another sentence.\n",
        "This is a test.\n\nFirst Column Second Column\n\nInteresting Super\n\n"
        + "This is another sentence.\n");

    Map<String, String> markdownNodes = new HashMap<>();
    markdownNodes.put("Code", "default");
    markdownNodes.put("FencedCodeBlock", "default");
    assertPlainText(
        "This is a test: `inline code`.\n\n```\ncode block\n```\n\nThis is another sentence.\n",
        "This is a test: inline code.\n\n\ncode block\n\n\nThis is another sentence.\n",
        markdownNodes);

    assertPlainText(
        "---\n"
        + "# This is YAML front matter\n"
        + "- test\n"
        + "---\n\n"
        + "# Heading\n"
        + "Test sentence\n",
        "\n\n\n\n\nHeading\nTest sentence\n");
    assertPlainText(
        "---\n"
        + "test: This is a test.\n"
        + "---\n\n"
        + "# Heading\n"
        + "Test sentence\n",
        "\n\n\n\nHeading\nTest sentence\n");
  }
}
