/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.html;

import com.ctc.wstx.api.WstxInputProperties;
import java.io.StringReader;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.bsplines.ltexls.parsing.CodeAnnotatedTextBuilder;
import org.bsplines.ltexls.tools.Tools;

public class HtmlAnnotatedTextBuilder extends CodeAnnotatedTextBuilder {
  private static final Pattern whitespacePattern = Pattern.compile(" *\r?\n *");

  private XMLInputFactory xmlInputFactory;

  public HtmlAnnotatedTextBuilder(String codeLanguageId) {
    super(codeLanguageId);

    this.xmlInputFactory = XMLInputFactory.newInstance();
    this.xmlInputFactory.setProperty(WstxInputProperties.P_MIN_TEXT_SEGMENT, 1);
    this.xmlInputFactory.setProperty(WstxInputProperties.P_TREAT_CHAR_REFS_AS_ENTS, true);
    this.xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, true);
    this.xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
    this.xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);
    this.xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
  }

  @Override
  public CodeAnnotatedTextBuilder addCode(String code) {
    int pos = 0;
    Stack<String> elementNameStack = new Stack<>();
    elementNameStack.push("html");
    String nextText = "";

    try {
      XMLStreamReader xmlStreamReader =
          this.xmlInputFactory.createXMLStreamReader(new StringReader(code));

      while (xmlStreamReader.hasNext()) {
        int eventType = xmlStreamReader.next();
        int oldPos = pos;
        pos = xmlStreamReader.getLocation().getCharacterOffset();
        String skippedCode = code.substring(oldPos, pos);
        String interpretAs = "";

        Tools.logger.finest("Position " + pos + " (" + xmlStreamReader.getLocation().getLineNumber()
            + "," + xmlStreamReader.getLocation().getColumnNumber() + "): Event type = "
            + eventType + ", skippedCode = '" + skippedCode + "'");

        if (!nextText.isEmpty()) {
          if (nextText.equals(skippedCode)) {
            addTextWithWhitespace(nextText);
          } else {
            addMarkup(skippedCode, nextText);
          }

          skippedCode = "";
          nextText = "";
        }

        if (eventType == XMLStreamReader.START_ELEMENT) {
          String elementName = xmlStreamReader.getLocalName();
          elementNameStack.push(elementName);
          Tools.logger.finest("START_ELEMENT: elementName = '" + xmlStreamReader.getLocalName()
              + "'");

          if ((elementName == "body") || (elementName == "div")
                || (elementName == "h1") || (elementName == "h2") || (elementName == "h3")
                || (elementName == "h4") || (elementName == "h5") || (elementName == "h6")
                || (elementName == "p") || (elementName == "table") || (elementName == "tr")) {
            interpretAs += "\n\n";
          } else if ((elementName == "br") || (elementName == "li")) {
            interpretAs += "\n";
          }
        } else if (eventType == XMLStreamReader.END_ELEMENT) {
          Tools.logger.finest("END_ELEMENT");
          if (!elementNameStack.isEmpty()) elementNameStack.pop();
        } else if (eventType == XMLStreamReader.CHARACTERS) {
          String elementName = (elementNameStack.isEmpty() ? "" : elementNameStack.peek());
          String text = xmlStreamReader.getText();
          Tools.logger.finest("CHARACTERS: text = '" + text + "'");
          if ((elementName != "script") && (elementName != "style")) nextText = text;
        } else if (eventType == XMLStreamReader.ENTITY_REFERENCE) {
          nextText = xmlStreamReader.getText();
          Tools.logger.finest("ENTITY_REFERENCE: text = '" + nextText + "'");
        }

        addMarkup(skippedCode, interpretAs);
      }
    } catch (XMLStreamException e) {
      // ignore parser errors
    }

    if (pos < code.length()) addTextWithWhitespace(code.substring(pos));

    return this;
  }

  protected CodeAnnotatedTextBuilder addTextWithWhitespace(String text) {
    Matcher matcher = whitespacePattern.matcher(text);
    int pos = 0;

    while (matcher.find()) {
      if (matcher.start() > 0) addText(text.substring(pos, matcher.start()));
      addMarkup(matcher.group());
      pos = matcher.end();
    }

    if (pos < text.length()) addText(text.substring(pos));

    return this;
  }
}
