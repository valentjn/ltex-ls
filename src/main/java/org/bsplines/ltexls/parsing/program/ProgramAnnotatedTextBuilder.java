/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.program;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bsplines.ltexls.parsing.CodeAnnotatedTextBuilder;
import org.bsplines.ltexls.parsing.markdown.MarkdownAnnotatedTextBuilder;
import org.bsplines.ltexls.tools.Tools;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.languagetool.markup.AnnotatedText;

public class ProgramAnnotatedTextBuilder extends CodeAnnotatedTextBuilder {
  private static final Pattern lineSeparatorPattern = Pattern.compile("\r?\n");
  private static final Pattern firstCharacterPattern = Pattern.compile(
      "^[ \t]*(?:([#$%*+\\-/])|(.))");

  private MarkdownAnnotatedTextBuilder markdownAnnotatedTextBuilder;
  private Pattern commentBlockPattern;
  private @Nullable String lineCommentPatternString;

  public ProgramAnnotatedTextBuilder(String codeLanguageId) {
    super(codeLanguageId);

    this.markdownAnnotatedTextBuilder = new MarkdownAnnotatedTextBuilder("markdown");

    ProgramCommentPatterns commentPatterns = new ProgramCommentPatterns(codeLanguageId);
    this.commentBlockPattern = commentPatterns.getCommentBlockPattern();
    this.lineCommentPatternString = commentPatterns.getLineCommentPatternString();
  }

  @Override
  public CodeAnnotatedTextBuilder addCode(String code) {
    Matcher commentBlockMatcher = this.commentBlockPattern.matcher(code);
    int curPos = 0;

    while (commentBlockMatcher.find()) {
      int lastPos = curPos;
      boolean isLineComment = (commentBlockMatcher.group("lineComment") != null);
      String groupName = (isLineComment ? "lineComment" : "blockComment");
      curPos = commentBlockMatcher.start(groupName);
      this.markdownAnnotatedTextBuilder.addMarkup(code.substring(lastPos, curPos), "\n\n");
      @Nullable String comment = commentBlockMatcher.group(groupName);

      if (comment == null) {
        Tools.logger.warning(Tools.i18n(
            "couldNotFindExpectedGroupInRegularExpressionMatch", groupName));
        continue;
      }

      addComment(comment, isLineComment);
      curPos = commentBlockMatcher.end(groupName);
    }

    if (curPos < code.length()) this.markdownAnnotatedTextBuilder.addMarkup(code.substring(curPos));

    return this;
  }

  private CodeAnnotatedTextBuilder addComment(String comment, boolean isLineComment) {
    String commonFirstCharacter = "";

    for (String line : lineSeparatorPattern.split(comment)) {
      Matcher firstCharacterMatcher = firstCharacterPattern.matcher(line);
      if (!firstCharacterMatcher.find()) continue;
      @Nullable String firstCharacter = firstCharacterMatcher.group(1);

      if (firstCharacter == null) {
        commonFirstCharacter = "";
        break;
      }

      if (commonFirstCharacter.isEmpty()) {
        commonFirstCharacter = firstCharacter;
      } else if (!firstCharacter.equals(commonFirstCharacter)) {
        commonFirstCharacter = "";
        break;
      }
    }

    Pattern lineContentsPattern = Pattern.compile(
        "[ \t]*"
        + ((isLineComment && (this.lineCommentPatternString != null))
          ? this.lineCommentPatternString : "")
        + "(?:" + Pattern.quote(commonFirstCharacter) + ")?[ \t]*(.*?)(?:\r?\n|$)");
    Matcher lineContentsMatcher = lineContentsPattern.matcher(comment);
    int curPos = 0;

    while (lineContentsMatcher.find()) {
      int lastPos = curPos;
      curPos = lineContentsMatcher.start(1);
      this.markdownAnnotatedTextBuilder.addMarkup(comment.substring(lastPos, curPos), "\n");

      lastPos = curPos;
      curPos = lineContentsMatcher.end(1);
      this.markdownAnnotatedTextBuilder.addCode(comment.substring(lastPos, curPos));
    }

    if (curPos < comment.length()) {
      this.markdownAnnotatedTextBuilder.addMarkup(comment.substring(curPos));
    }

    return this;
  }

  @Override
  public AnnotatedText build() {
    return this.markdownAnnotatedTextBuilder.build();
  }
}
