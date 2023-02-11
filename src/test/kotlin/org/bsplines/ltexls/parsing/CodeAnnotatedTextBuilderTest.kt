/* Copyright (C) 2019-2023 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing

import org.bsplines.ltexls.settings.Settings
import org.languagetool.markup.AnnotatedText
import kotlin.test.assertEquals

open class CodeAnnotatedTextBuilderTest(
  val codeLanguageId: String,
) {
  fun assertPlainText(code: String, expectedPlainText: String, settings: Settings = Settings()) {
    val annotatedText: AnnotatedText = buildAnnotatedText(code, settings)
    assertEquals(expectedPlainText, annotatedText.plainText)
  }

  fun assertPlainText(
    code: String,
    expectedPlainText: String,
    codeLanguageId: String = this.codeLanguageId,
    settings: Settings = Settings(),
  ) {
    val annotatedText: AnnotatedText = buildAnnotatedText(code, settings, codeLanguageId)
    assertEquals(expectedPlainText, annotatedText.plainText)
  }

  fun buildAnnotatedText(
    code: String,
    settings: Settings = Settings(),
    codeLanguageId: String = this.codeLanguageId,
  ): AnnotatedText {
    val builder: CodeAnnotatedTextBuilder =
      CodeAnnotatedTextBuilder.create(codeLanguageId)
    builder.setSettings(settings)
    if (builder is CharacterBasedCodeAnnotatedTextBuilder) builder.isPreventingInfiniteLoops = false
    return builder.addCode(code).build()
  }
}
