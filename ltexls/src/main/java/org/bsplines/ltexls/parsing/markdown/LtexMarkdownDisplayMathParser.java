/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.markdown;

import com.vladsch.flexmark.parser.InlineParser;
import com.vladsch.flexmark.parser.block.AbstractBlockParser;
import com.vladsch.flexmark.parser.block.AbstractBlockParserFactory;
import com.vladsch.flexmark.parser.block.BlockContinue;
import com.vladsch.flexmark.parser.block.BlockParser;
import com.vladsch.flexmark.parser.block.BlockParserFactory;
import com.vladsch.flexmark.parser.block.BlockStart;
import com.vladsch.flexmark.parser.block.CustomBlockParserFactory;
import com.vladsch.flexmark.parser.block.MatchedBlockParser;
import com.vladsch.flexmark.parser.block.ParserState;
import com.vladsch.flexmark.util.ast.Block;
import com.vladsch.flexmark.util.ast.BlockContent;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LtexMarkdownDisplayMathParser extends AbstractBlockParser {
  private static final Pattern DISPLAY_MATH_START_PATTERN = Pattern.compile("\\$\\$(\\s*$)");
  private static final Pattern DISPLAY_MATH_END_PATTERN = Pattern.compile("\\$\\$(\\s*$)");

  private LtexMarkdownDisplayMath block;
  private @Nullable BlockContent content;
  private boolean hadClose;

  public LtexMarkdownDisplayMathParser(DataHolder options, BasedSequence openMarker,
        BasedSequence openTrailing) {
    this.block = new LtexMarkdownDisplayMath();
    this.block.setOpeningMarker(openMarker);
    this.block.setOpeningTrailing(openTrailing);

    this.content = new BlockContent();
    this.hadClose = false;
  }

  @Override
  public Block getBlock() {
    return this.block;
  }

  @Override
  public BlockContinue tryContinue(ParserState state) {
    if (this.hadClose) return BlockContinue.none();

    int index = state.getIndex();
    BasedSequence line = state.getLineWithEOL();
    Matcher matcher = DISPLAY_MATH_END_PATTERN.matcher(line.subSequence(index));

    if (!matcher.matches()) {
      return BlockContinue.atIndex(index);
    } else {
      @Nullable Node lastChild = this.block.getLastChild();

      if ((lastChild != null) && (lastChild instanceof LtexMarkdownDisplayMath)) {
        BlockParser parser = state.getActiveBlockParser((Block)lastChild);

        if ((parser instanceof LtexMarkdownDisplayMathParser)
              && !((LtexMarkdownDisplayMathParser)parser).hadClose) {
          return BlockContinue.atIndex(index);
        }
      }

      this.hadClose = true;
      this.block.setClosingMarker(state.getLine().subSequence(index, index + 2));
      this.block.setClosingTrailing(
          state.getLineWithEOL().subSequence(matcher.start(1), matcher.end(1)));

      return BlockContinue.atIndex(state.getLineEndIndex());
    }
  }

  @Override
  public void addLine(ParserState state, BasedSequence line) {
    if (this.content == null) return;
    this.content.add(line, state.getIndent());
  }

  @Override
  public void closeBlock(ParserState state) {
    if (this.content == null) return;
    this.block.setContent(this.content);
    this.block.setCharsFromContent();
    this.content = null;
  }

  @Override
  public boolean isContainer() {
    return false;
  }

  @Override
  public boolean canContain(ParserState state, BlockParser blockParser, Block block) {
    return false;
  }

  @Override
  public void parseInlines(InlineParser inlineParser) {
  }

  public static class Factory implements CustomBlockParserFactory {
    @Override
    public @Nullable Set<Class<?>> getAfterDependents() {
      return null;
    }

    @Override
    public @Nullable Set<Class<?>> getBeforeDependents() {
      return null;
    }

    @Override
    public boolean affectsGlobalScope() {
      return false;
    }

    @Override
    public @NotNull BlockParserFactory apply(@NotNull DataHolder options) {
      return new BlockFactory(options);
    }
  }

  private static class BlockFactory extends AbstractBlockParserFactory {
    BlockFactory(DataHolder options) {
      super(options);
    }

    private static boolean haveDisplayMathParser(ParserState state) {
      List<BlockParser> parsers = state.getActiveBlockParsers();

      for (int i = parsers.size() - 1; i >= 0; i--) {
        if (parsers.get(i) instanceof LtexMarkdownDisplayMathParser) return true;
      }

      return false;
    }

    @Override
    public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
      if (!haveDisplayMathParser(state)) {
        BasedSequence line = state.getLineWithEOL();
        Matcher matcher = DISPLAY_MATH_START_PATTERN.matcher(line);

        if (matcher.matches()) {
          LtexMarkdownDisplayMathParser parser = new LtexMarkdownDisplayMathParser(
              state.getProperties(),
              line.subSequence(0, 2),
              line.subSequence(matcher.start(1), matcher.end(1)));
          return BlockStart.of(parser).atIndex(state.getLineEndIndex());
        }
      }

      return BlockStart.none();
    }
  }
}