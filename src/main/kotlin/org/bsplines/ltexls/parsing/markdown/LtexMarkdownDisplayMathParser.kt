/* Copyright (C) 2019-2023 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.markdown

import com.vladsch.flexmark.parser.InlineParser
import com.vladsch.flexmark.parser.block.AbstractBlockParser
import com.vladsch.flexmark.parser.block.AbstractBlockParserFactory
import com.vladsch.flexmark.parser.block.BlockContinue
import com.vladsch.flexmark.parser.block.BlockParser
import com.vladsch.flexmark.parser.block.BlockParserFactory
import com.vladsch.flexmark.parser.block.BlockStart
import com.vladsch.flexmark.parser.block.CustomBlockParserFactory
import com.vladsch.flexmark.parser.block.MatchedBlockParser
import com.vladsch.flexmark.parser.block.ParserState
import com.vladsch.flexmark.util.ast.Block
import com.vladsch.flexmark.util.ast.BlockContent
import com.vladsch.flexmark.util.data.DataHolder
import com.vladsch.flexmark.util.sequence.BasedSequence

class LtexMarkdownDisplayMathParser(
  private val options: DataHolder,
  openMarker: BasedSequence,
  openTrailing: BasedSequence,
) : AbstractBlockParser() {
  private val block = LtexMarkdownDisplayMath()
  private var content: BlockContent? = BlockContent()
  private var hadClose = false

  init {
    this.block.openingMarker = openMarker
    this.block.openingTrailing = openTrailing
  }

  override fun getBlock(): Block {
    return this.block
  }

  override fun tryContinue(state: ParserState): BlockContinue? {
    if (this.hadClose) return BlockContinue.none()

    val index: Int = state.index
    val line: BasedSequence = state.lineWithEOL
    val matchGroup: MatchGroup? =
        DISPLAY_MATH_END_REGEX.matchEntire(line.subSequence(index))?.groups?.get(1)

    return if (matchGroup != null) {
      val lastChild = this.block.lastChild

      if ((lastChild != null) && (lastChild is LtexMarkdownDisplayMath)) {
        val parser = state.getActiveBlockParser(lastChild as Block?)

        if ((parser is LtexMarkdownDisplayMathParser) && !parser.hadClose) {
          return BlockContinue.atIndex(index)
        }
      }

      this.hadClose = true
      this.block.closingMarker = state.line.subSequence(index, index + 2)

      this.block.closingTrailing = state.lineWithEOL.subSequence(
        matchGroup.range.first,
        matchGroup.range.last + 1,
      )

      BlockContinue.atIndex(state.lineEndIndex)
    } else {
      BlockContinue.atIndex(index)
    }
  }

  override fun addLine(state: ParserState, line: BasedSequence) {
    this.content?.add(line, state.indent)
  }

  override fun closeBlock(state: ParserState) {
    val content: BlockContent = this.content ?: return
    this.block.setContent(content)
    this.block.setCharsFromContent()
    this.content = null
  }

  override fun isContainer(): Boolean {
    return false
  }

  override fun canContain(state: ParserState, blockParser: BlockParser, block: Block): Boolean {
    return false
  }

  override fun parseInlines(inlineParser: InlineParser) {
  }

  class Factory : CustomBlockParserFactory {
    override fun getAfterDependents(): Set<Class<*>>? {
      return null
    }

    override fun getBeforeDependents(): Set<Class<*>>? {
      return null
    }

    override fun affectsGlobalScope(): Boolean {
      return false
    }

    override fun apply(options: DataHolder): BlockParserFactory {
      return BlockFactory(options)
    }
  }

  private class BlockFactory(options: DataHolder?) : AbstractBlockParserFactory(options) {
    override fun tryStart(state: ParserState, matchedBlockParser: MatchedBlockParser): BlockStart? {
      if (!haveDisplayMathParser(state)) {
        val line = state.lineWithEOL
        val matchGroup: MatchGroup? = DISPLAY_MATH_START_REGEX.matchEntire(line)?.groups?.get(1)

        if (matchGroup != null) {
          val parser = LtexMarkdownDisplayMathParser(
            state.properties,
            line.subSequence(0, 2),
            line.subSequence(matchGroup.range.first, matchGroup.range.last + 1),
          )

          return BlockStart.of(parser).atIndex(state.lineEndIndex)
        }
      }

      return BlockStart.none()
    }

    companion object {
      private fun haveDisplayMathParser(state: ParserState): Boolean {
        for (parser: BlockParser in state.activeBlockParsers.reversed()) {
          if (parser is LtexMarkdownDisplayMathParser) return true
        }

        return false
      }
    }
  }

  companion object {
    private val DISPLAY_MATH_START_REGEX = Regex("\\$\\$(\\s*$)")
    private val DISPLAY_MATH_END_REGEX = Regex("\\$\\$(\\s*$)")
  }
}
