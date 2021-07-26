/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.html

import org.bsplines.ltexls.parsing.CodeAnnotatedTextBuilder
import org.languagetool.markup.AnnotatedText
import kotlin.test.Test
import kotlin.test.assertEquals

class HtmlAnnotatedTextBuilderTest {
  @Test
  fun test() {
    assertPlainText(
      """
      <html>
        <head>
          <title>Title</title>
        </head>
        <body style="color:red;">
          This is a <b>test</b>.
          <!-- This is a comment. -->
        </body>
      </html>
      """,
      "Title\n\nThis is a test."
    )
    assertPlainText(
      "<html><body>This is a te<script>abc</script>st.</body></html>\n",
      "\n\nThis is a test."
    )
    assertPlainText(
      "<html><body>This is a te<br/>st.</body></html>\n",
      "\n\nThis is a te\nst."
    )
    assertPlainText(
      "<html><body>This is a test &amp; another test.</body></html>\n",
      "\n\nThis is a test & another test."
    )
  }

  companion object {
    private fun assertPlainText(code: String, expectedPlainText: String) {
      val annotatedText: AnnotatedText = buildAnnotatedText(code)
      assertEquals(expectedPlainText, annotatedText.plainText)
    }

    private fun buildAnnotatedText(code: String): AnnotatedText {
      val builder: CodeAnnotatedTextBuilder = CodeAnnotatedTextBuilder.create("html")
      return builder.addCode(code).build()
    }
  }
}
