/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.restructuredtext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bsplines.ltexls.parsing.CodeAnnotatedTextBuilder;
import org.bsplines.ltexls.parsing.DummyGenerator;
import org.bsplines.ltexls.settings.Settings;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class RestructuredtextAnnotatedTextBuilder extends CodeAnnotatedTextBuilder {
  private static final Pattern blockSeparatorPattern = Pattern.compile("^([ \t]*\r?\n)+");
  private static final Pattern whitespacePattern = Pattern.compile("^[ \t]*");

  private static final Pattern footnotePattern = Pattern.compile(
      "^\\.\\. \\[([0-9]+|[#*]|#[0-9A-Za-z\\-_.:+]+)\\]([ \t\r\n]|$)");
  private static final Pattern directivePattern = Pattern.compile(
      "^\\.\\. [0-9A-Za-z\\-_.:+]+::([ \t\r\n]|$)");
  private static final Pattern commentPattern = Pattern.compile(
      "^\\.\\.([ \t\r\n]|$)");

  private static final Pattern gridTableStartPattern = Pattern.compile(
      "^(\\+\\-{3,}){2,}\\+\r?\n");
  private static final Pattern simpleTableStartPattern = Pattern.compile(
      "^={3,}( +={3,})+\r?\n");

  private static final Pattern sectionTitleAdronmentPattern = Pattern.compile(
      "^(={3,}|-{3,}|`{3,}|:{3,}|\\.{3,}|'{3,}|\"{3,}|"
      + "~{3,}|\\^{3,}|_{3,}|\\*{3,}|\\+{3,}|#{3,})\r?\n");
  private static final Pattern lineBlockPattern = Pattern.compile(
      "^\\|[ \t]+(?=.*?[^|](\r?\n|$))");

  private static final Pattern bulletListPattern = Pattern.compile(
      "^[*+\\-\u2022\u2023\u2043][ \t]+");
  private static final Pattern enumeratedListPattern = Pattern.compile(
      "^(([0-9]+|[A-Za-z#]|[IVXLCDM]+|[ivxlcdm]+)\\.|"
      + "\\(?([0-9]+|[A-Za-z#]|[IVXLCDM]+|[ivxlcdm]+)\\))[ \t]+");

  private static final Pattern inlineStartPrecedingPattern = Pattern.compile(
      "^[ \t\r\n\\-:/'\"<(\\[{]");
  private static final Pattern inlineEndPrecedingPattern = Pattern.compile(
      "^[^ \t\r\n]");
  private static final Pattern strongEmphasisStartPattern = Pattern.compile(
      "^\\*\\*(?=[^ \t\r\n])");
  private static final Pattern strongEmphasisEndPattern = Pattern.compile(
      "^\\*\\*(?=[ \t\r\n-.,:;!?\\\\/'\")\\]}>]|$)");
  private static final Pattern emphasisStartPattern = Pattern.compile(
      "^\\*(?=[^ \t\r\n])");
  private static final Pattern emphasisEndPattern = Pattern.compile(
      "^\\*(?=[ \t\r\n-.,:;!?\\\\/'\")\\]}>]|$)");
  private static final Pattern inlineLiteralStartPattern = Pattern.compile(
      "^``(?=[^ \t\r\n])");
  private static final Pattern inlineLiteralEndPattern = Pattern.compile(
      "^``(?=[ \t\r\n-.,:;!?\\\\/'\")\\]}>]|$)");
  private static final Pattern interpretedTextStartPattern = Pattern.compile(
      "^(:[0-9A-Za-z\\-_.:+]+:)?`(?=[^ \t])");
  private static final Pattern interpretedTextEndPattern = Pattern.compile(
      "^`(:[0-9A-Za-z\\-_.:+]+:)?(?=[ \t\r\n-.,:;!?\\\\/'\")\\]}>]|$)");
  private static final Pattern inlineInternalTargetStartPattern = Pattern.compile(
      "^_`(?=[^ \t\r\n])");
  private static final Pattern inlineInternalTargetEndPattern = Pattern.compile(
      "^`(?=[ \t\r\n-.,:;!?\\\\/'\")\\]}>]|$)");
  private static final Pattern footnoteReferenceStartPattern = Pattern.compile(
      "^\\[(?=[^ \t\r\n])");
  private static final Pattern footnoteReferenceEndPattern = Pattern.compile(
      "^\\]_(?=[ \t\r\n-.,:;!?\\\\/'\")\\]}>]|$)");
  private static final Pattern hyperlinkReferenceStartPattern = Pattern.compile(
      "^`(?=[^ \t\r\n])");
  private static final Pattern hyperlinkReferenceEndPattern = Pattern.compile(
      "^`__?(?=[ \t\r\n-.,:;!?\\\\/'\")\\]}>]|$)");

  private String code;
  private int pos;
  private char curChar;
  private String curString;

  private DummyGenerator dummyGenerator;
  private int dummyCounter;
  private int indentation;
  private int lastIndentation;
  private RestructuredtextBlockSignature.Type blockType;
  private boolean inIgnoredMarkup;

  private String language;
  private List<RestructuredtextBlockSignature> blockSignatures;
  private Map<RestructuredtextBlockSignature.Type, RestructuredtextBlockSignature>
      blockSignatureMap;

  public RestructuredtextAnnotatedTextBuilder(String codeLanguageId) {
    super(codeLanguageId);

    this.language = "en-US";
    this.blockSignatures = new ArrayList<>();
    this.blockSignatures.add(new RestructuredtextBlockSignature(
        RestructuredtextBlockSignature.Type.COMMENT,
        RestructuredtextBlockSignature.Action.IGNORE));
    this.blockSignatures.add(new RestructuredtextBlockSignature(
        RestructuredtextBlockSignature.Type.GRID_TABLE,
        RestructuredtextBlockSignature.Action.IGNORE));
    this.blockSignatures.add(new RestructuredtextBlockSignature(
        RestructuredtextBlockSignature.Type.SIMPLE_TABLE,
        RestructuredtextBlockSignature.Action.IGNORE));
    this.blockSignatureMap = createBlockSignatureMap(this.blockSignatures);

    reinitialize();
  }

  @EnsuresNonNull({"code", "curString", "dummyGenerator", "blockType"})
  public void reinitialize(@UnknownInitialization RestructuredtextAnnotatedTextBuilder this) {
    this.code = "";
    this.pos = 0;
    this.curString = "";

    this.dummyGenerator = new DummyGenerator();
    this.dummyCounter = 0;
    this.indentation = -1;
    this.lastIndentation = -1;
    this.blockType = RestructuredtextBlockSignature.Type.PARAGRAPH;
    this.inIgnoredMarkup = false;
  }

  private static Map<RestructuredtextBlockSignature.Type, RestructuredtextBlockSignature>
        createBlockSignatureMap(List<RestructuredtextBlockSignature> blockSignatures) {
    Map<RestructuredtextBlockSignature.Type, RestructuredtextBlockSignature> map =
        new HashMap<>();

    for (RestructuredtextBlockSignature blockSignature : blockSignatures) {
      map.put(blockSignature.getType(), blockSignature);
    }

    return map;
  }

  @Override
  public void setSettings(Settings settings) {
    super.setSettings(settings);
    this.language = settings.getLanguageShortCode();
  }

  public RestructuredtextAnnotatedTextBuilder addText(String text) {
    if (text.isEmpty()) return this;
    super.addText(text);
    this.pos += text.length();
    return this;
  }

  public RestructuredtextAnnotatedTextBuilder addMarkup(String markup) {
    if (markup.isEmpty()) return this;
    super.addMarkup(markup);
    this.pos += markup.length();
    return this;
  }

  public RestructuredtextAnnotatedTextBuilder addMarkup(String markup, String interpretAs) {
    if (interpretAs.isEmpty()) {
      return addMarkup(markup);
    } else {
      super.addMarkup(markup, interpretAs);
      this.pos += markup.length();
      return this;
    }
  }

  @Override
  public CodeAnnotatedTextBuilder addCode(String code) {
    reinitialize();
    this.code = code;

    while (this.pos < this.code.length()) {
      this.curChar = this.code.charAt(this.pos);
      this.curString = String.valueOf(this.curChar);
      boolean isStartOfBlock = false;
      boolean isStartOfLine = ((this.pos == 0) || this.code.charAt(this.pos - 1) == '\n');

      if (isStartOfLine) {
        @Nullable Matcher blockSeparatorMatcher = matchFromPosition(blockSeparatorPattern);

        if ((this.pos == 0) || (blockSeparatorMatcher != null)) {
          isStartOfBlock = true;
          if (blockSeparatorMatcher != null) addMarkup(blockSeparatorMatcher.group(), "\n");
        }

        @Nullable Matcher whitespaceMatcher = matchFromPosition(whitespacePattern);
        String whitespace = ((whitespaceMatcher != null) ? whitespaceMatcher.group() : "");
        this.lastIndentation = this.indentation;
        this.indentation = whitespace.length();
        addMarkup(whitespace);
        this.curChar = this.code.charAt(this.pos);
        this.curString = String.valueOf(this.curChar);
      }

      if (isStartOfBlock) {
        this.inIgnoredMarkup = false;

        if ((isExplicitBlockType()
                && ((this.indentation == 0) || (this.indentation < this.lastIndentation)))
              || isTableBlockType()) {
          this.blockType = RestructuredtextBlockSignature.Type.PARAGRAPH;
        }
      }

      if (isStartOfLine) {
        @Nullable Matcher matcher;

        if (matchExplicitBlock()) {
          continue;
        } else if ((matcher = matchFromPosition(gridTableStartPattern)) != null) {
          this.blockType = RestructuredtextBlockSignature.Type.GRID_TABLE;
          addMarkup(matcher.group());
          continue;
        } else if ((matcher = matchFromPosition(simpleTableStartPattern)) != null) {
          this.blockType = RestructuredtextBlockSignature.Type.SIMPLE_TABLE;
          addMarkup(matcher.group());
          continue;
        } else if ((matcher = matchFromPosition(sectionTitleAdronmentPattern)) != null) {
          addMarkup(matcher.group());
          continue;
        } else if ((matcher = matchFromPosition(lineBlockPattern)) != null) {
          addMarkup(matcher.group());
          continue;
        } else if ((matcher = matchFromPosition(bulletListPattern)) != null) {
          addMarkup(matcher.group());
          continue;
        } else if ((matcher = matchFromPosition(enumeratedListPattern)) != null) {
          addMarkup(matcher.group());
          continue;
        }
      }

      RestructuredtextBlockSignature.Action action =
          RestructuredtextBlockSignature.Action.DEFAULT;

      if (this.blockSignatureMap.containsKey(this.blockType)) {
        action = this.blockSignatureMap.get(this.blockType).getAction();
      }

      if (action == RestructuredtextBlockSignature.Action.IGNORE) {
        addMarkup(this.curString);
        continue;
      }

      @Nullable Matcher matcher;

      if ((matcher = matchInlineStartFromPosition(strongEmphasisStartPattern)) != null) {
        addMarkup(matcher.group());
        continue;
      } else if ((matcher = matchInlineEndFromPosition(strongEmphasisEndPattern)) != null) {
        addMarkup(matcher.group());
        continue;
      } else if ((matcher = matchInlineStartFromPosition(emphasisStartPattern)) != null) {
        addMarkup(matcher.group());
        continue;
      } else if ((matcher = matchInlineEndFromPosition(emphasisEndPattern)) != null) {
        addMarkup(matcher.group());
        continue;
      } else if ((matcher = matchInlineStartFromPosition(inlineLiteralStartPattern)) != null) {
        addMarkup(matcher.group(), generateDummy());
        this.inIgnoredMarkup = true;
        continue;
      } else if ((matcher = matchInlineEndFromPosition(inlineLiteralEndPattern)) != null) {
        addMarkup(matcher.group());
        this.inIgnoredMarkup = false;
        continue;
      } else if ((matcher = matchInlineStartFromPosition(interpretedTextStartPattern)) != null) {
        addMarkup(matcher.group(), generateDummy());
        this.inIgnoredMarkup = true;
        continue;
      } else if ((matcher = matchInlineEndFromPosition(interpretedTextEndPattern)) != null) {
        addMarkup(matcher.group());
        this.inIgnoredMarkup = false;
        continue;
      } else if (
            (matcher = matchInlineStartFromPosition(inlineInternalTargetStartPattern)) != null) {
        addMarkup(matcher.group(), generateDummy());
        this.inIgnoredMarkup = true;
        continue;
      } else if ((matcher = matchInlineEndFromPosition(inlineInternalTargetEndPattern)) != null) {
        addMarkup(matcher.group());
        this.inIgnoredMarkup = false;
        continue;
      } else if ((matcher = matchInlineStartFromPosition(footnoteReferenceStartPattern)) != null) {
        addMarkup(matcher.group(), generateDummy());
        this.inIgnoredMarkup = true;
        continue;
      } else if ((matcher = matchInlineEndFromPosition(footnoteReferenceEndPattern)) != null) {
        addMarkup(matcher.group());
        this.inIgnoredMarkup = false;
        continue;
      } else if ((matcher = matchInlineStartFromPosition(hyperlinkReferenceStartPattern)) != null) {
        addMarkup(matcher.group(), generateDummy());
        this.inIgnoredMarkup = true;
        continue;
      } else if ((matcher = matchInlineEndFromPosition(hyperlinkReferenceEndPattern)) != null) {
        addMarkup(matcher.group());
        this.inIgnoredMarkup = false;
        continue;
      }

      if (this.inIgnoredMarkup) {
        addMarkup(this.curString);
      } else {
        addText(this.curString);
      }
    }

    return this;
  }

  private @Nullable Matcher matchFromPosition(Pattern pattern) {
    return matchFromPosition(pattern, this.pos);
  }

  private @Nullable Matcher matchFromPosition(Pattern pattern, int pos) {
    Matcher matcher = pattern.matcher(this.code.substring(pos));
    return ((matcher.find() && !matcher.group().isEmpty()) ? matcher : null);
  }

  private @Nullable Matcher matchInlineStartFromPosition(Pattern pattern) {
    if ((this.pos > 0) && (matchFromPosition(inlineStartPrecedingPattern, this.pos - 1) == null)) {
      return null;
    }

    @Nullable Matcher matcher = matchFromPosition(pattern);
    if (matcher == null) return null;
    if ((this.pos == 0) || (this.pos >= this.code.length() - 1)) return matcher;

    char forbiddenFollowingChar;

    switch (this.code.charAt(this.pos - 1)) {
      case '\'': {
        forbiddenFollowingChar = '\'';
        break;
      }
      case '"': {
        forbiddenFollowingChar = '"';
        break;
      }
      case '<': {
        forbiddenFollowingChar = '>';
        break;
      }
      case '(': {
        forbiddenFollowingChar = ')';
        break;
      }
      case '[': {
        forbiddenFollowingChar = ']';
        break;
      }
      case '{': {
        forbiddenFollowingChar = '}';
        break;
      }
      default: {
        return matcher;
      }
    }

    return ((this.code.charAt(this.pos + 1) == forbiddenFollowingChar) ? null : matcher);
  }

  private @Nullable Matcher matchInlineEndFromPosition(Pattern pattern) {
    if ((this.pos == 0) || (matchFromPosition(inlineEndPrecedingPattern, this.pos - 1) == null)) {
      return null;
    }

    return matchFromPosition(pattern);
  }

  private boolean isExplicitBlockType() {
    return ((this.blockType == RestructuredtextBlockSignature.Type.FOOTNOTE)
        || (this.blockType == RestructuredtextBlockSignature.Type.DIRECTIVE)
        || (this.blockType == RestructuredtextBlockSignature.Type.COMMENT));
  }

  private boolean isTableBlockType() {
    return ((this.blockType == RestructuredtextBlockSignature.Type.GRID_TABLE)
        || (this.blockType == RestructuredtextBlockSignature.Type.SIMPLE_TABLE));
  }

  private boolean matchExplicitBlock() {
    @Nullable Matcher matcher;

    if ((matcher = matchFromPosition(footnotePattern)) != null) {
      this.blockType = RestructuredtextBlockSignature.Type.FOOTNOTE;
      addMarkup(matcher.group());
      return true;
    } else if ((matcher = matchFromPosition(directivePattern)) != null) {
      this.blockType = RestructuredtextBlockSignature.Type.DIRECTIVE;
      addMarkup(matcher.group());
      return true;
    } else if ((matcher = matchFromPosition(commentPattern)) != null) {
      this.blockType = RestructuredtextBlockSignature.Type.COMMENT;
      addMarkup(matcher.group());
      return true;
    }

    return false;
  }

  private String generateDummy() {
    return this.dummyGenerator.generate(this.language, this.dummyCounter++);
  }
}
