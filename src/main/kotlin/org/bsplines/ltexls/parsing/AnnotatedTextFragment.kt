/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing

import org.bsplines.ltexls.server.LtexTextDocumentItem
import org.languagetool.markup.AnnotatedText
import org.languagetool.markup.AnnotatedTextBuilder
import org.languagetool.markup.TextPart
import java.lang.reflect.Field
import java.lang.reflect.Method

class AnnotatedTextFragment(
  val annotatedText: AnnotatedText,
  val codeFragment: CodeFragment,
  val document: LtexTextDocumentItem,
) {
  private var plainText: String? = null
  private var inverseAnnotatedText: AnnotatedText? = null

  fun getSubstringOfPlainText(fromPos: Int, toPos: Int): String {
    val plainText: String = this.plainText ?: run {
      val plainText = this.annotatedText.plainText
      this.plainText = plainText
      plainText
    }
    val inverseAnnotatedText: AnnotatedText = (
        this.inverseAnnotatedText ?: getInverseAnnotatedText())

    return plainText.substring(
        getOriginalTextPosition(inverseAnnotatedText, fromPos, false),
        getOriginalTextPosition(inverseAnnotatedText, toPos, true))
  }

  private fun getInverseAnnotatedText(): AnnotatedText {
    val inverseAnnotatedTextBuilder = AnnotatedTextBuilder()
    val textParts: List<TextPart> = this.annotatedText.parts
    var i = 0

    while (i < textParts.size) {
      val textPart = textParts[i]

      when (textPart.type) {
        TextPart.Type.TEXT -> inverseAnnotatedTextBuilder.addText(textPart.part)
        TextPart.Type.FAKE_CONTENT -> inverseAnnotatedTextBuilder.addMarkup(textPart.part)
        TextPart.Type.MARKUP -> {
          var markup: String

          if ((i < textParts.size - 1) &&
            (textParts[i + 1].type == TextPart.Type.FAKE_CONTENT)
          ) {
            markup = textParts[i + 1].part
            i++
          } else {
            markup = ""
          }

          inverseAnnotatedTextBuilder.addMarkup(markup, textPart.part)
        }
        null -> {}
      }

      i++
    }

    val inverseAnnotatedText: AnnotatedText = inverseAnnotatedTextBuilder.build()
    this.inverseAnnotatedText = inverseAnnotatedText
    return inverseAnnotatedText
  }

  private fun getOriginalTextPosition(
    annotatedText: AnnotatedText,
    plainTextPosition: Int,
    isToPos: Boolean,
  ): Int {
    @Suppress("UNCHECKED_CAST")
    val mapping: Map<Int, *> = annotatedTextMappingField.get(annotatedText) as Map<Int, *>

    for (entry in mapping.entries) {
      if (entry.key == plainTextPosition) {
        return mappingValueGetTotalPositionMethod.invoke(entry.value) as Int
      }
    }

    return annotatedText.getOriginalTextPositionFor(plainTextPosition, isToPos)
  }

  companion object {
    private var annotatedTextMappingField: Field = run {
      val annotatedTextMappingField: Field = AnnotatedText::class.java.getDeclaredField("mapping")
      annotatedTextMappingField.isAccessible = true
      annotatedTextMappingField
    }

    private var mappingValueGetTotalPositionMethod: Method = run {
      val mappingValueClass: Class<*> = Class.forName("org.languagetool.markup.MappingValue")
      val mappingValueGetTotalPositionMethod: Method = mappingValueClass.getDeclaredMethod(
          "getTotalPosition")
      mappingValueGetTotalPositionMethod.isAccessible = true
      mappingValueGetTotalPositionMethod
    }
  }
}
