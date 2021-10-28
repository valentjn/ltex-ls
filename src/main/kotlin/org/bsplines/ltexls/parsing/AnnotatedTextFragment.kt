/* Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing

import org.bsplines.ltexls.server.LtexTextDocumentItem
import org.bsplines.ltexls.tools.I18n
import org.bsplines.ltexls.tools.Logging
import org.languagetool.markup.AnnotatedText
import org.languagetool.markup.AnnotatedTextBuilder
import org.languagetool.markup.TextPart
import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.math.roundToInt

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
    val inverseAnnotatedText: AnnotatedText = this.inverseAnnotatedText ?: run {
      val inverseAnnotatedText: AnnotatedText = invertAnnotatedText(this.annotatedText)
      this.inverseAnnotatedText = inverseAnnotatedText
      inverseAnnotatedText
    }

    val plainTextFromPos: Int = getOriginalTextPosition(inverseAnnotatedText, fromPos, false)
    val plainTextToPos: Int = getOriginalTextPosition(inverseAnnotatedText, toPos, true)

    return if (plainTextFromPos <= plainTextToPos) {
      plainText.substring(plainTextFromPos, plainTextToPos)
    } else {
      Logging.logger.warning(
        I18n.format(
          "couldNotDeterminePlainTextPositions",
          fromPos,
          toPos,
          plainTextFromPos,
          plainTextToPos,
        )
      )
      ""
    }
  }

  companion object {
    private val annotatedTextMappingField: Field = run {
      val annotatedTextMappingField: Field = AnnotatedText::class.java.getDeclaredField("mapping")
      annotatedTextMappingField.isAccessible = true
      annotatedTextMappingField
    }

    private val mappingValueGetTotalPositionMethod: Method = run {
      val mappingValueClass: Class<*> = Class.forName("org.languagetool.markup.MappingValue")
      val mappingValueGetTotalPositionMethod: Method = mappingValueClass.getDeclaredMethod(
          "getTotalPosition")
      mappingValueGetTotalPositionMethod.isAccessible = true
      mappingValueGetTotalPositionMethod
    }

    fun getOriginalTextPosition(
      annotatedText: AnnotatedText,
      plainTextPos: Int,
      isToPos: Boolean,
    ): Int {
      @Suppress("UNCHECKED_CAST")
      val mapping: Map<Int, *> = annotatedTextMappingField.get(annotatedText) as Map<Int, *>

      val mappingList: MutableList<Pair<Int, Int>> = run {
        val mappingList = ArrayList<Pair<Int, Int>>()
        mappingList.add(Pair(0, 0))

        for ((key: Int, value: Any?) in mapping) {
          val totalPosition: Int = mappingValueGetTotalPositionMethod.invoke(value) as Int
          if (key == plainTextPos) return totalPosition
          mappingList.add(Pair(key, totalPosition))
        }

        mappingList
      }

      val i: Int = searchPlainTextPos(mappingList, plainTextPos, isToPos)
      val lowerNeighbor: Pair<Int, Int> = mappingList[i - 1]
      val upperNeighbor: Pair<Int, Int> = mappingList[i]

      if (!isToPos) {
        if (lowerNeighbor.first == plainTextPos) {
          return lowerNeighbor.second
        } else if (upperNeighbor.first == plainTextPos) {
          return upperNeighbor.second
        }
      } else {
        if (upperNeighbor.first == plainTextPos) {
          return upperNeighbor.second
        } else if (lowerNeighbor.first == plainTextPos) {
          return lowerNeighbor.second
        }
      }

      val t: Float = (
        (plainTextPos - lowerNeighbor.first).toFloat() /
        (upperNeighbor.first - lowerNeighbor.first).toFloat()
      )
      return ((1 - t) * lowerNeighbor.second + t * upperNeighbor.second).roundToInt()
    }

    private fun searchPlainTextPos(
      mappingList: MutableList<Pair<Int, Int>>,
      plainTextPos: Int,
      isToPos: Boolean,
    ): Int {
      // cannot use compareBy/thenBy, otherwise Jacoco tries to find a file "Comparisons.kt"
      // (probably due to inlining), which doesn't exist, and uploading to Coveralls fails
      val comparator = Comparator<Pair<Int, Int>> { a, b ->
        if (a.first < b.first) {
          -1
        } else if (a.first > b.first) {
          1
        } else if (a.second < b.second) {
          -1
        } else if (a.second > b.second) {
          1
        } else {
          0
        }
      }

      mappingList.sortWith(comparator)

      val pairToFind: Pair<Int, Int> = Pair(plainTextPos, Int.MAX_VALUE)
      var i: Int = -mappingList.binarySearch(pairToFind, comparator) - 1

      if (i <= 0) i = 1
      if (i >= mappingList.size) i = mappingList.size - 1

      if (isToPos) {
        while ((i < mappingList.size - 1) && (mappingList[i + 1].first <= plainTextPos)) i++
      } else {
        while ((i > 1) && (mappingList[i - 1].first >= plainTextPos)) i--
      }

      return i
    }

    fun invertAnnotatedText(annotatedText: AnnotatedText): AnnotatedText {
      val inverseAnnotatedTextBuilder = AnnotatedTextBuilder()
      val textParts: List<TextPart> = annotatedText.parts
      var i = 0

      while (i < textParts.size) {
        val textPart: TextPart = textParts[i]

        when (textPart.type) {
          TextPart.Type.TEXT -> inverseAnnotatedTextBuilder.addText(textPart.part)
          TextPart.Type.FAKE_CONTENT -> inverseAnnotatedTextBuilder.addMarkup(textPart.part)
          TextPart.Type.MARKUP -> {
            val markup: String = if (
              (i < textParts.size - 1) &&
              (textParts[i + 1].type == TextPart.Type.FAKE_CONTENT)
            ) {
              i++
              textParts[i].part
            } else {
              ""
            }

            inverseAnnotatedTextBuilder.addMarkup(markup, textPart.part)
          }
          null -> {}
        }

        i++
      }

      return inverseAnnotatedTextBuilder.build()
    }
  }
}
