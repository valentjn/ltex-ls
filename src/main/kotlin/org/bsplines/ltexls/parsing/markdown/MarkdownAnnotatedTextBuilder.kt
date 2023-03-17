/* Copyright (C) 2019-2023 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.markdown

import com.vladsch.flexmark.ext.definition.DefinitionExtension
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension
import com.vladsch.flexmark.ext.gitlab.GitLabExtension
import com.vladsch.flexmark.ext.tables.TablesExtension
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.test.util.AstCollectingVisitor
import com.vladsch.flexmark.util.ast.Document
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.data.DataHolder
import com.vladsch.flexmark.util.data.MutableDataSet
import com.vladsch.flexmark.util.sequence.Escaping
import com.vladsch.flexmark.ast.FencedCodeBlock
import org.bsplines.ltexls.parsing.CodeAnnotatedTextBuilder
import org.bsplines.ltexls.parsing.DummyGenerator
import org.bsplines.ltexls.settings.Settings
import org.bsplines.ltexls.tools.Logging
import java.util.logging.Level

@Suppress("TooManyFunctions")
class MarkdownAnnotatedTextBuilder(
  codeLanguageId: String,
) : CodeAnnotatedTextBuilder(codeLanguageId) {
  private val parser: Parser = Parser.builder(PARSER_OPTIONS).build()
  private var code = ""
  private var pos = 0
  private var dummyCounter = 0
  private var firstCellInTableRow = false
  private val nodeTypeStack = ArrayDeque<String>()
  private var language: String = "en-US"
  private var shadowMarkups = listOf<Triple<String, Int, Int>>()
  private var shadowOffset = 0
  private val nodeSignatures: MutableList<MarkdownNodeSignature> = ArrayList(
    MarkdownAnnotatedTextBuilderDefaults.DEFAULT_MARKDOWN_NODE_SIGNATURES,
  )

  private fun isInNodeType(nodeType: String): Boolean {
    return this.nodeTypeStack.contains(nodeType)
  }

  private fun isInIgnoredNodeType(): Boolean {
    var result = false

    for (nodeType: String in nodeTypeStack) {
      for (nodeSignature: MarkdownNodeSignature in this.nodeSignatures) {
        if (nodeSignature.name == nodeType) {
          result = (nodeSignature.action == MarkdownNodeSignature.Action.Ignore)
        }
      }
    }

    return result
  }

  private fun isDummyNodeType(nodeType: String): Boolean {
    var result = false

    for (nodeSignature: MarkdownNodeSignature in this.nodeSignatures) {
      if (nodeSignature.name == nodeType) {
        result = (nodeSignature.action == MarkdownNodeSignature.Action.Dummy)
      }
    }

    return result
  }

  private fun addMarkup(finalPos: Int) {
    var newPos = finalPos

    val inParagraph: Boolean = isInNodeType("Paragraph")

    while ((this.pos < this.code.length) && (this.pos < newPos)) {
      var curPos: Int = this.code.indexOf("\r\n", this.pos)
      if (curPos != -1) curPos += 1

      if ((curPos == -1) || (curPos >= newPos)) {
        curPos = this.code.indexOf('\n', this.pos)
        if ((curPos == -1) || (curPos >= newPos)) break
      }

      if (curPos > this.pos) super.addMarkup(this.code.substring(this.pos, curPos))
      this.pos = curPos
      val tmpShadowOffset = shadowOffset
      if (removeComment()) {
        newPos += (shadowOffset - tmpShadowOffset)
      } else {
        super.addMarkup(this.code.substring(curPos, curPos + 1), (if (inParagraph) " " else "\n"))
        this.pos += 1
      }
    }

    if (newPos > pos) {
      super.addMarkup(this.code.substring(this.pos, newPos))
      this.pos = newPos
      removeComment()
    }
  }

  private fun addMarkup(node: Node, interpretAs: String) {
    addMarkup(node.startOffset + shadowOffset)
    val newPos: Int = node.endOffset + shadowOffset
    super.addMarkup(this.code.substring(this.pos, newPos), interpretAs)
    this.pos = newPos
  }

  private fun addText(newPos: Int) {
    if (newPos > pos) {
      super.addText(this.code.substring(this.pos, newPos))
      this.pos = newPos
    }
  }

  private fun generateDummy(): String {
    return DummyGenerator.getInstance().generate(this.language, this.dummyCounter++)
  }

  override fun addCode(code: String): MarkdownAnnotatedTextBuilder {
    val document: Document = this.parser.parse(code)

    if (Logging.LOGGER.isLoggable(Level.FINEST)) {
      Logging.LOGGER.finest(
        "flexmarkAst = "
          + AstCollectingVisitor().collectAndGetAstText(document),
      )
    }

    this.code = code
    this.pos = 0
    visitChildren(document)
    if (this.pos < this.code.length) addMarkup(this.code.length)

    return this
  }

  override fun addComment(
    code: Array<String>,
    markups: Array<Triple<String, Int, Int>>,
  ): CodeAnnotatedTextBuilder {
    var fullCode = ""
    var clearCode = ""

    for ((index, markup) in markups.withIndex()) {
      fullCode += markup.first + code[index]
      clearCode += code[index] + "\n"
    }

    this.code = fullCode
    this.pos = 0
    this.shadowMarkups = markups.toList()
    this.shadowOffset = 0

    visitChildren(this.parser.parse(clearCode))

    return this
  }

  private fun visit(node: Node) {
    val nodeType: String = node.javaClass.simpleName

    if ((nodeType == "TableRow")) {
      this.firstCellInTableRow = true
    } else if ((nodeType == "TableCell")) {
      if (this.firstCellInTableRow) {
        this.firstCellInTableRow = false
      } else {
        super.addMarkup("", " ")
      }
    }

    if (isInIgnoredNodeType()) {
      addMarkup(node.endOffset + shadowOffset)
    } else if (isDummyNodeType(nodeType)) {
      addMarkup(node, generateDummy())
    } else if (nodeType == "Text") {
      addMarkup(node.startOffset + shadowOffset)
      addText(node.endOffset + shadowOffset)
    } else if (nodeType == "HtmlEntity") {
      addMarkup(node, Escaping.unescapeHtml(node.chars))
    } else {
      if (nodeType == "Paragraph") {
        addMarkup(node.startOffset + shadowOffset)
      } else if (nodeType == "FencedCodeBlock") {
        val block = node as FencedCodeBlock
        addMarkup(pos + block.openingMarker.count() + block.info.count())
      }
      this.nodeTypeStack.addLast(nodeType)
      visitChildren(node)
      this.nodeTypeStack.removeLastOrNull()
      if (nodeType == "FencedCodeBlock") {
        addMarkup(pos + (node as FencedCodeBlock).closingMarker.count() - 1)
      }
      if (nodeType == "DefinitionTerm") super.addMarkup("", ".")
    }
  }

  private fun visitChildren(node: Node) {
    for (child: Node in node.children) {
      removeComment()
      visit(child)
    }
  }

  private fun removeComment(): Boolean {
    var removed = false;

    while (shadowMarkups.isNotEmpty()) {
      val shadowMarkup = shadowMarkups.first()

      if (shadowMarkup.second == pos) {
        removed = true

        super.addMarkup(shadowMarkup.first, "\n")

        val offset = shadowMarkup.third - shadowMarkup.second
        // we add new line in markdown code
        shadowOffset += if (shadowMarkup.first.firstOrNull() == '\n') {
          offset - 1
        } else {
          offset
        }

        pos += offset
        shadowMarkups = shadowMarkups.drop(1)
      } else {
        break
      }
    }

    return removed
  }

  override fun setSettings(settings: Settings) {
    this.language = settings.languageShortCode

    for ((nodeName: String, actionString: String) in settings.markdownNodes) {
      var dummyGenerator: DummyGenerator = DummyGenerator.getInstance()

      val action: MarkdownNodeSignature.Action = when (actionString) {
        "default" -> MarkdownNodeSignature.Action.Default
        "ignore" -> MarkdownNodeSignature.Action.Ignore
        "dummy", "pluralDummy", "vowelDummy" -> {
          val plural: Boolean = (actionString == "pluralDummy")
          val vowel: Boolean = (actionString == "vowelDummy")
          dummyGenerator = DummyGenerator.getInstance(plural = plural, vowel = vowel)
          MarkdownNodeSignature.Action.Dummy
        }

        else -> continue
      }

      this.nodeSignatures.add(MarkdownNodeSignature(nodeName, action, dummyGenerator))
    }
  }

  companion object {
    private val PARSER_OPTIONS: DataHolder = MutableDataSet().set(
      Parser.EXTENSIONS,
      listOf(
        DefinitionExtension.create(),
        GitLabExtension.create(),
        LtexMarkdownExtension.create(),
        StrikethroughExtension.create(),
        TablesExtension.create(),
        YamlFrontMatterExtension.create(),
      ),
    )
  }
}
