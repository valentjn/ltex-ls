/* Copyright (C) 2019-2023 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.plaintext

import org.bsplines.ltexls.parsing.CodeAnnotatedTextBuilder
import org.bsplines.ltexls.settings.Settings
import org.languagetool.markup.AnnotatedText
import kotlin.test.Test
import kotlin.test.assertEquals

class PlaintextAnnotatedTextBuilderTest {
  @Test
  fun test() {
    val builder: CodeAnnotatedTextBuilder = CodeAnnotatedTextBuilder.create("plaintext")
    builder.addCode("This is \\textbf{a} `test`.\n")
    val annotatedText: AnnotatedText = builder.build()
    assertEquals("This is \\textbf{a} `test`.\n", annotatedText.plainText)

    builder.setSettings(Settings())
  }
}
