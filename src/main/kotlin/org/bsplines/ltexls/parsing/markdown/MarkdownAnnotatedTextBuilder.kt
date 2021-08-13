/* Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.markdown

import com.vladsch.flexmark.ext.definition.DefinitionExtension
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
import org.bsplines.ltexls.parsing.CodeAnnotatedTextBuilder
import org.bsplines.ltexls.parsing.DummyGenerator
import org.bsplines.ltexls.settings.Settings
import org.bsplines.ltexls.tools.Logging
import java.util.logging.Level

@Suppress("TooManyFunctions")
class MarkdownAnnotatedTextBuilder(
  codeLanguageId: String,
) : CodeAnnotatedTextBuilder(codeLanguageId) {
  private val parser: Parser = Parser.builder(parserOptions).build()
  private var code = ""
  private var pos = 0
  private var dummyCounter = 0
  private var firstCellInTableRow = false
  private val nodeTypeStack = ArrayDeque<String>()
  private var language: String = "en-US"
  private val nodeSignatures: MutableList<MarkdownNodeSignature> = ArrayList(
    MarkdownAnnotatedTextBuilderDefaults.defaultMarkdownNodeSignatures
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

  private fun addMarkup(newPos: Int) {
    val inParagraph: Boolean = isInNodeType("Paragraph")

    while ((this.pos < this.code.length) && (this.pos < newPos)) {
      var curPos: Int = this.code.indexOf("\r\n", this.pos)
      if (curPos != -1) curPos += 1

      if ((curPos == -1) || (curPos >= newPos)) {
        curPos = this.code.indexOf('\n', this.pos)
        if ((curPos == -1) || (curPos >= newPos)) break
      }

      if (curPos > this.pos) super.addMarkup(this.code.substring(this.pos, curPos))
      super.addMarkup(this.code.substring(curPos, curPos + 1), (if (inParagraph) " " else "\n"))
      this.pos = curPos + 1
    }

    if (newPos > pos) {
      super.addMarkup(this.code.substring(this.pos, newPos))
      this.pos = newPos
    }
  }

  private fun addMarkup(node: Node, interpretAs: String) {
    addMarkup(node.startOffset)
    val newPos: Int = node.endOffset
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

    if (Logging.logger.isLoggable(Level.FINEST)) {
      Logging.logger.finest(
        "flexmarkAst = "
          + AstCollectingVisitor().collectAndGetAstText(document)
      )
    }

    this.code = code
    this.pos = 0
    visitChildren(document)
    if (this.pos < this.code.length) addMarkup(this.code.length)

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
      addMarkup(node.endOffset)
    } else if (isDummyNodeType(nodeType)) {
      addMarkup(node, generateDummy())
    } else if (nodeType == "Text") {
      addMarkup(node.startOffset)
      addText(node.endOffset)
    } else if (nodeType == "HtmlEntity") {
      addMarkup(node, Escaping.unescapeHtml(node.chars))
    } else {
      if (nodeType == "Paragraph") addMarkup(node.startOffset)
      this.nodeTypeStack.addLast(nodeType)
      visitChildren(node)
      this.nodeTypeStack.removeLastOrNull()
      if (nodeType == "DefinitionTerm") super.addMarkup("", ".")
    }
  }

  private fun visitChildren(node: Node) {
    for (child: Node in node.children) {
      visit(child)
    }
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
    private val parserOptions: DataHolder = MutableDataSet().set(
      Parser.EXTENSIONS,
      listOf(
        DefinitionExtension.create(),
        GitLabExtension.create(),
        TablesExtension.create(),
        YamlFrontMatterExtension.create(),
        LtexMarkdownExtension.create(),
      )
    )
  }
}
