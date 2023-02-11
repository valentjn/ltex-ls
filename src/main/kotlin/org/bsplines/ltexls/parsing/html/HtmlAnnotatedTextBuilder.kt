/* Copyright (C) 2019-2023 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.html

import com.ctc.wstx.api.WstxInputProperties
import org.bsplines.ltexls.parsing.CodeAnnotatedTextBuilder
import org.bsplines.ltexls.tools.Logging
import java.io.StringReader
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamException
import javax.xml.stream.XMLStreamReader

class HtmlAnnotatedTextBuilder(
  codeLanguageId: String,
) : CodeAnnotatedTextBuilder(codeLanguageId) {
  private val xmlInputFactory = XMLInputFactory.newInstance()

  private var code = ""
  private var pos = 0
  private val elementNameStack: ArrayDeque<String> = ArrayDeque(listOf("html"))
  private var nextText = ""
  private var lastSpace = ""

  init {
    this.xmlInputFactory.setProperty(WstxInputProperties.P_MIN_TEXT_SEGMENT, 1)
    this.xmlInputFactory.setProperty(WstxInputProperties.P_TREAT_CHAR_REFS_AS_ENTS, true)
    this.xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, true)
    this.xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false)
    this.xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false)
    this.xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false)
  }

  override fun addText(text: String?): HtmlAnnotatedTextBuilder {
    super.addText(text)
    if (text?.isNotEmpty() == true) textAdded(text)
    return this
  }

  override fun addMarkup(markup: String?, interpretAs: String?): HtmlAnnotatedTextBuilder {
    super.addMarkup(markup, interpretAs)
    if (interpretAs?.isNotEmpty() == true) textAdded(interpretAs)
    return this
  }

  private fun textAdded(text: String) {
    if (text.isEmpty()) return
    this.lastSpace = when (text[text.length - 1]) {
      ' ', '\n', '\r' -> " "
      else -> ""
    }
  }

  @Suppress("SwallowedException")
  override fun addCode(code: String): CodeAnnotatedTextBuilder {
    this.code = code
    this.pos = 0

    try {
      val xmlStreamReader: XMLStreamReader =
          this.xmlInputFactory.createXMLStreamReader(StringReader(code))

      while (xmlStreamReader.hasNext()) {
        processXmlStreamReaderEvent(xmlStreamReader)
      }
    } catch (e: XMLStreamException) {
      // ignore parser errors
    }

    if (this.pos < code.length) addTextWithWhitespace(code.substring(this.pos))
    return this
  }

  private fun processXmlStreamReaderEvent(xmlStreamReader: XMLStreamReader) {
    val eventType: Int = xmlStreamReader.next()
    val oldPos: Int = this.pos
    this.pos = xmlStreamReader.location.characterOffset
    var skippedCode: String = this.code.substring(oldPos, this.pos)
    var interpretAs = ""

    Logging.LOGGER.finest(
      "Position " + this.pos + " ("
      + xmlStreamReader.location.lineNumber
      + "," + xmlStreamReader.location.columnNumber + "): Event type = "
      + eventType + ", skippedCode = '" + skippedCode + "'",
    )

    if (this.nextText.isNotEmpty()) {
      if (this.nextText.replace("\r\n", "\n") == skippedCode.replace("\r\n", "\n")) {
        addTextWithWhitespace(skippedCode)
      } else {
        addMarkup(skippedCode, this.nextText)
      }

      skippedCode = ""
      this.nextText = ""
    }

    when (eventType) {
      XMLStreamReader.START_ELEMENT -> {
        val elementName: String = xmlStreamReader.localName
        this.elementNameStack.addLast(elementName)
        Logging.LOGGER.finest("START_ELEMENT: elementName = '" + xmlStreamReader.localName + "'")

        when (elementName) {
          "body", "div", "h1", "h2", "h3", "h4", "h5", "h6", "p", "table", "tr" -> {
            interpretAs += "\n\n"
          }
          "br", "li" -> {
            interpretAs += "\n"
          }
        }
      }
      XMLStreamReader.END_ELEMENT -> {
        Logging.LOGGER.finest("END_ELEMENT")
        this.elementNameStack.removeLastOrNull()
      }
      XMLStreamReader.CHARACTERS -> {
        val elementName: String =
            if (this.elementNameStack.isEmpty()) "" else this.elementNameStack.last()
        val text: String = xmlStreamReader.text
        Logging.LOGGER.finest("CHARACTERS: text = '$text'")
        if ((elementName != "script") && (elementName != "style")) this.nextText = text
      }
      XMLStreamReader.ENTITY_REFERENCE -> {
        this.nextText = xmlStreamReader.text
        Logging.LOGGER.finest("ENTITY_REFERENCE: text = '" + this.nextText + "'")
      }
      else -> {
        // ignore other event types
      }
    }

    addMarkup(skippedCode, interpretAs)
  }

  private fun addTextWithWhitespace(text: String): CodeAnnotatedTextBuilder {
    var pos = 0

    for (matchResult: MatchResult in WHITESPACE_REGEX.findAll(text)) {
      if (matchResult.range.first > pos) addText(text.substring(pos, matchResult.range.first))
      val space: String = (if (this.lastSpace.isEmpty()) " " else "")
      addMarkup(matchResult.value, space)
      pos = matchResult.range.last + 1
    }

    if (pos < text.length) addText(text.substring(pos))
    return this
  }

  companion object {
    private val WHITESPACE_REGEX = Regex("(?: |\r?\n)+")
  }
}
