/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.markdown;

import com.vladsch.flexmark.parser.InlineParser;
import com.vladsch.flexmark.parser.InlineParserExtension;
import com.vladsch.flexmark.parser.InlineParserExtensionFactory;
import com.vladsch.flexmark.parser.LightInlineParser;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LtexMarkdownInlineMathParser implements InlineParserExtension {
  private static final Pattern MATH_PATTERN = Pattern.compile(
      "\\$([^ ](?:.|\n)*?[^ ]|[^ ])(?<!\\\\)\\$(?![0-9])");

  public LtexMarkdownInlineMathParser(LightInlineParser inlineParser) {
  }

  @Override
  public void finalizeDocument(@NotNull InlineParser inlineParser) {
  }

  @Override
  public void finalizeBlock(@NotNull InlineParser inlineParser) {
  }

  @Override
  public boolean parse(@NotNull LightInlineParser inlineParser) {
    if ((inlineParser.peek() == '$') && (inlineParser.peek(1) != ' ')) {
      BasedSequence input = inlineParser.getInput();
      @Nullable Matcher matcher = inlineParser.matcher(MATH_PATTERN);

      if (matcher != null) {
        inlineParser.flushTextNode();

        BasedSequence openingMarker = input.subSequence(matcher.start(), matcher.start(1));
        BasedSequence closingMarker = input.subSequence(matcher.end(1), matcher.end());
        LtexMarkdownInlineMath inlineMath = new LtexMarkdownInlineMath(
            openingMarker,
            openingMarker.baseSubSequence(
              openingMarker.getEndOffset(), closingMarker.getStartOffset()),
            closingMarker);
        inlineParser.getBlock().appendChild(inlineMath);

        return true;
      }
    }

    return false;
  }

  public static class Factory implements InlineParserExtensionFactory {
    @Override
    public @Nullable Set<Class<?>> getAfterDependents() {
      return null;
    }

    @Override
    public @NotNull CharSequence getCharacters() {
      return "$";
    }

    @Override
    public @Nullable Set<Class<?>> getBeforeDependents() {
      return null;
    }

    @Override
    public @NotNull InlineParserExtension apply(@NotNull LightInlineParser lightInlineParser) {
      return new LtexMarkdownInlineMathParser(lightInlineParser);
    }

    @Override
    public boolean affectsGlobalScope() {
      return false;
    }
  }
}
