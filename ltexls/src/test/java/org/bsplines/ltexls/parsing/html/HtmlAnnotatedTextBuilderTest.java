/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.html;

import org.bsplines.ltexls.parsing.CodeAnnotatedTextBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.languagetool.markup.AnnotatedText;

public class HtmlAnnotatedTextBuilderTest {
  private static void assertPlainText(String code, String expectedPlainText) {
    AnnotatedText annotatedText = buildAnnotatedText(code);
    Assertions.assertEquals(expectedPlainText, annotatedText.getPlainText());
  }

  private static AnnotatedText buildAnnotatedText(String code) {
    CodeAnnotatedTextBuilder builder = CodeAnnotatedTextBuilder.create("html");
    return builder.addCode(code).build();
  }

  @Test
  public void test() {
    assertPlainText(
        "<html>\n"
        + "  <head>\n"
        + "    <title>Title</title>\n"
        + "  </head>\n"
        + "  <body style=\"color:red;\">\n"
        + "    This is a <b>test</b>.\n"
        + "    <!-- This is a comment. -->\n"
        + "  </body>\n"
        + "</html>\n",
        "Title\n\nThis is a test.");
    assertPlainText(
        "<html><body>This is a te<script>abc</script>st.</body></html>\n",
        "\n\nThis is a test.");
    assertPlainText(
        "<html><body>This is a te<br/>st.</body></html>\n",
        "\n\nThis is a te\nst.");
    assertPlainText(
        "<html><body>This is a test &amp; another test.</body></html>\n",
        "\n\nThis is a test & another test.");
  }
}
