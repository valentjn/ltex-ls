/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.org;

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bsplines.ltexls.parsing.CodeAnnotatedTextBuilder;
import org.bsplines.ltexls.parsing.DummyGenerator;
import org.bsplines.ltexls.settings.Settings;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class OrgAnnotatedTextBuilder extends CodeAnnotatedTextBuilder {
  private enum ElementType {
    HEADLINE,
    GREATER_CENTER_BLOCK,
    GREATER_QUOTE_BLOCK,
    GREATER_SPECIAL_BLOCK,
    COMMENT_BLOCK,
    EXAMPLE_BLOCK,
    EXPORT_BLOCK,
    SOURCE_BLOCK,
    VERSE_BLOCK,
    DRAWER,
    PROPERTY_DRAWER,
    DYNAMIC_BLOCK,
    LATEX_ENVIRONMENT,
    TABLE,
    PARAGRAPH,
  }

  private enum ObjectType {
    REGULAR_LINK_DESCRIPTION,
    BOLD,
    STRIKETHROUGH,
    ITALIC,
    VERBATIM,
    UNDERLINE,
    CODE,
  }

  private static final String regularLinkPathPatternString =
      "[ \\-/0-9A-Z\\\\a-z]+"
      + "|[A-Za-z]+:(//)?[^\r\n\\[\\]]+"
      + "|id:[-0-9A-Fa-f]+"
      + "|#[^\r\n\\[\\]]+"
      + "|\\([^\r\n\\[\\]]+\\)"
      + "|[^\r\n\\[\\]]+";

  private static final String timestampPatternString =
      "[0-9]{4}-[0-9]{2}-[0-9]{2}[ \t]+[^ \t\r\n+\\-0-9>\\]]+"
      + "([ \t]+[0-9]{1,2}:[0-9]{2})?"
      + "([ \t]+(\\+|\\+\\+|\\.\\+|-|--)[0-9]+[dhmwy]){0,2}";
  private static final String timestampRangePatternString =
      "[0-9]{4}-[0-9]{2}-[0-9]{2}[ \t]+[^ \t\r\n+\\-0-9>\\]]+"
      + "[ \t]+[0-9]{1,2}:[0-9]{2}-[0-9]{1,2}:[0-9]{2}"
      + "([ \t]+(\\+|\\+\\+|\\.\\+|-|--)[0-9]+[dhmwy]){0,2}";

  private static final Pattern whitespacePattern = Pattern.compile(
      "^[ \t]*", Pattern.CASE_INSENSITIVE);

  private static final Pattern headlinePattern = Pattern.compile(
      "^(\\*+(?= ))([ \t]+(?-i:TODO|DONE))?([ \t]+\\[#[A-Za-z]\\])?[ \t]*",
      Pattern.CASE_INSENSITIVE);
  private static final Pattern headlineCommentPattern = Pattern.compile(
      "^(\\*+(?= ))([ \t]+(?-i:TODO|DONE))?([ \t]+\\[#[A-Za-z]\\])?"
      + "[ \t]+COMMENT(?=[ \t]|\r?\n|$)[^\r\n]*(?=\r?\n|$)",
      Pattern.CASE_INSENSITIVE);
  private static final Pattern headlineTagsPattern = Pattern.compile(
      "^[ \t]*((:[#%0-9@A-Z_a-z]+)+:)?[ \t]*(?=\r?\n|$)", Pattern.CASE_INSENSITIVE);

  private static final Pattern affiliatedKeywordsPattern = Pattern.compile(
      "^#\\+((CAPTION|HEADER|NAME|PLOT|RESULTS)|"
      + "((CAPTION|RESULTS)\\[[^\r\n]*?\\])|ATTR_[-0-9A-Z_a-z]+): [^\r\n]*(?=\r?\n|$)",
      Pattern.CASE_INSENSITIVE);

  private static final Pattern blockBeginPattern = Pattern.compile(
      "^#\\+BEGIN_([^ \t\r\n]+)([ \t]+[^\r\n]*?)?[ \t]*(?=\r?\n|$)", Pattern.CASE_INSENSITIVE);
  private static final Pattern blockEndPattern = Pattern.compile(
      "^#\\+END_([^ \t\r\n]+)[ \t]*(?=\r?\n|$)", Pattern.CASE_INSENSITIVE);

  private static final Pattern drawerBeginPattern = Pattern.compile(
      "^:([-A-Z_a-z]+):[ \t]*(?=\r?\n|$)", Pattern.CASE_INSENSITIVE);
  private static final Pattern drawerEndPattern = Pattern.compile(
      "^:END:[ \t]*(?=\r?\n|$)", Pattern.CASE_INSENSITIVE);

  private static final Pattern dynamicBlockBeginPattern = Pattern.compile(
      "^#\\+BEGIN: ([^ \t\r\n]+)([ \t]+[^\r\n]*?)[ \t]*(?=\r?\n|$)", Pattern.CASE_INSENSITIVE);
  private static final Pattern dynamicBlockEndPattern = Pattern.compile(
      "^#\\+END:[ \t]*(?=\r?\n|$)", Pattern.CASE_INSENSITIVE);

  private static final Pattern footnoteDefinitionPattern = Pattern.compile(
      "^\\[fn:([0-9]+|[-A-Z_a-z]+)\\][ \t]*", Pattern.CASE_INSENSITIVE);

  private static final Pattern itemPattern = Pattern.compile(
      "^(\\*|-|\\+|([0-9]+|[A-Za-z])[.)])(?=[ \t]|$)([ \t]+\\[@([0-9]+|[A-Za-z])\\])?"
      + "([ \t]+\\[[- \tX]\\])?([ \t]+[^\r\n]*?[ \t]+::)?[ \t]*",
      Pattern.CASE_INSENSITIVE);

  private static final Pattern tableRowPattern = Pattern.compile(
      "^\\|[ \t]*", Pattern.CASE_INSENSITIVE);
  private static final Pattern ruleTableRowPattern = Pattern.compile(
      "^\\|-[^\r\n]*(?=\r?\n|$)", Pattern.CASE_INSENSITIVE);
  private static final Pattern tableCellSeparatorPattern = Pattern.compile(
      "^[ \t]*\\|[ \t]*", Pattern.CASE_INSENSITIVE);

  private static final Pattern babelCallPattern = Pattern.compile(
      "^#\\+CALL:[ \t]*([^\r\n]+?)[ \t]*(?=\r?\n|$)", Pattern.CASE_INSENSITIVE);

  private static final Pattern clockPattern = Pattern.compile(
      "^CLOCK:[ \t]*([^\r\n]+?)[ \t]*(?=\r?\n|$)", Pattern.CASE_INSENSITIVE);
  private static final Pattern diarySexpPattern = Pattern.compile(
      "^%%\\(([^\r\n]*)", Pattern.CASE_INSENSITIVE);
  private static final Pattern planningPattern = Pattern.compile(
      "^(DEADLINE|SCHEDULED|CLOSED):[ \t]*("
      + "<%%\\([^\r\n>]+\\)>"
      + "|<" + timestampPatternString + ">"
      + "|\\[" + timestampPatternString + "\\]"
      + "|<" + timestampPatternString + ">--<" + timestampPatternString + ">"
      + "|<" + timestampRangePatternString + ">"
      + "|\\[" + timestampPatternString + "\\]--\\[" + timestampPatternString + "\\]"
      + "|\\[" + timestampRangePatternString + "\\]"
      + ")[ \t]*(?=\r?\n|$)", Pattern.CASE_INSENSITIVE);

  private static final Pattern commentPattern = Pattern.compile(
      "^#([ \t]+[^\r\n]*?)?(\r?\n|$)", Pattern.CASE_INSENSITIVE);

  private static final Pattern fixedWidthLinePattern = Pattern.compile(
      "^:([ \t]+|(?=\r?\n|$))", Pattern.CASE_INSENSITIVE);

  private static final Pattern horizontalRulePattern = Pattern.compile(
      "^-{5,}[ \t]*(?=\r?\n|$)", Pattern.CASE_INSENSITIVE);

  private static final Pattern keywordPattern = Pattern.compile(
      "^#\\+([^ \t\r\n]+?):[ \t]*([^\r\n]+?)[ \t]*(?=\r?\n|$)", Pattern.CASE_INSENSITIVE);

  private static final Pattern latexEnvironmentBeginPattern = Pattern.compile(
      "^\\\\begin\\{([*0-9A-Za-z]+)\\}[ \t]*", Pattern.CASE_INSENSITIVE);
  private static final Pattern latexEnvironmentEndPattern = Pattern.compile(
      "^\\\\end\\{([*0-9A-Za-z]+)\\}[ \t]*", Pattern.CASE_INSENSITIVE);

  private static final Pattern latexFragmentPattern1 = Pattern.compile(
      "^\\\\[A-Za-z]+(\\[[^\r\n{}\\[\\]]*\\]|\\{[^\r\n{}]*\\})*", Pattern.CASE_INSENSITIVE);
  private static final Pattern latexFragmentPattern2 = Pattern.compile(
      "^\\\\\\((.|\r?\n)*?\\\\\\)", Pattern.CASE_INSENSITIVE);
  private static final Pattern latexFragmentPattern3 = Pattern.compile(
      "^\\\\\\[(.|\r?\n)*?\\\\\\]", Pattern.CASE_INSENSITIVE);
  private static final Pattern latexFragmentPattern4 = Pattern.compile(
      "^\\$\\$(.|\r?\n)*?\\$\\$", Pattern.CASE_INSENSITIVE);
  private static final Pattern latexFragmentPattern5 = Pattern.compile(
      "^\\$[^ \t\r\n\"',.;?]\\$(?=[ \t\"'(),.;<>?\\[\\]]|\r?\n|$)", Pattern.CASE_INSENSITIVE);
  private static final Pattern latexFragmentPattern6 = Pattern.compile(
      "^\\$[^ \t\r\n$,.;]([^\r\n$]|\r?\n)*[^ \t\r\n$,.]\\$(?=[ \t!\"'(),.;<>?\\[\\]]|\r?\n|$)",
      Pattern.CASE_INSENSITIVE);

  private static final Pattern exportSnippetPattern = Pattern.compile(
      "^@@[-0-9A-Za-z]+:[^\r\n]*?@@", Pattern.CASE_INSENSITIVE);

  private static final Pattern footnoteReferencePattern1 = Pattern.compile(
      "^\\[fn:[-0-9A-Z_a-z]*\\]", Pattern.CASE_INSENSITIVE);
  private static final Pattern footnoteReferencePattern2 = Pattern.compile(
      "^\\[fn:([-0-9A-Z_a-z]*)?:[^\r\n]*?\\]", Pattern.CASE_INSENSITIVE);

  private static final Pattern inlineBabelCallPattern = Pattern.compile(
      "^call_[^ \t\r\n()]+(\\[[^\r\n]*?\\])?\\([^\r\n]*?\\)(\\[[^\r\n]*?\\])?",
      Pattern.CASE_INSENSITIVE);
  private static final Pattern inlineSourceBlockPattern = Pattern.compile(
      "^src_[^ \t\r\n]+(\\[[^\r\n]*?\\])?\\{[^\r\n]*?\\}", Pattern.CASE_INSENSITIVE);

  private static final Pattern macroPattern = Pattern.compile(
      "^\\{\\{\\{[A-Za-z][-0-9A-Z_a-z]*(\\([^\r\n]*?\\))?\\}\\}\\}", Pattern.CASE_INSENSITIVE);

  private static final Pattern statisticsCookiePattern = Pattern.compile(
      "^\\[[0-9]*(%|/[0-9]*)\\]", Pattern.CASE_INSENSITIVE);

  private static final Pattern diaryTimestampPattern = Pattern.compile(
      "^<%%\\([^\r\n>]+\\)>", Pattern.CASE_INSENSITIVE);
  private static final Pattern activeTimestampRangePattern1 = Pattern.compile(
      "^<" + timestampPatternString + ">--<" + timestampPatternString + ">",
      Pattern.CASE_INSENSITIVE);
  private static final Pattern activeTimestampRangePattern2 = Pattern.compile(
      "^<" + timestampRangePatternString + ">", Pattern.CASE_INSENSITIVE);
  private static final Pattern inactiveTimestampRangePattern1 = Pattern.compile(
      "^\\[" + timestampPatternString + "\\]--\\[" + timestampPatternString + "\\]",
      Pattern.CASE_INSENSITIVE);
  private static final Pattern inactiveTimestampRangePattern2 = Pattern.compile(
      "^\\[" + timestampRangePatternString + "\\]", Pattern.CASE_INSENSITIVE);
  private static final Pattern activeTimestampPattern = Pattern.compile(
      "^<" + timestampPatternString + ">", Pattern.CASE_INSENSITIVE);
  private static final Pattern inactiveTimestampPattern = Pattern.compile(
      "^\\[" + timestampPatternString + "\\]", Pattern.CASE_INSENSITIVE);

  private static final Pattern angleLinkPattern = Pattern.compile(
      "^<[A-Za-z]+:[^\r\n<>\\]]+>", Pattern.CASE_INSENSITIVE);
  private static final Pattern plainLinkPattern = Pattern.compile(
      "^[A-Za-z]+:[^ \t\r\n()<>]+(?<=[A-Za-z]|[^ \t\r\n!,.;?]/)(?=[^\r\n0-9A-Za-z]|\r?\n|$)",
      Pattern.CASE_INSENSITIVE);

  private static final Pattern linkPrecedingPattern = Pattern.compile(
      "^[^\r\n0-9A-Za-z]", Pattern.CASE_INSENSITIVE);
  private static final Pattern radioTargetPattern = Pattern.compile(
      "^<<<(?![ \t])[^\r\n<>]+(?<![ \t])>>>", Pattern.CASE_INSENSITIVE);
  private static final Pattern targetPattern = Pattern.compile(
      "^<<(?![ \t])[^\r\n<>]+(?<![ \t])>>", Pattern.CASE_INSENSITIVE);

  private static final Pattern regularLinkPatternWithoutDescription = Pattern.compile(
      "^\\[\\[(" + regularLinkPathPatternString + ")\\]\\]",
      Pattern.CASE_INSENSITIVE);
  private static final Pattern regularLinkPatternWithDescription = Pattern.compile(
      "^\\[\\[(" + regularLinkPathPatternString + ")\\]\\[(?=[^\r\n\\[\\]]+\\]\\])",
      Pattern.CASE_INSENSITIVE);
  private static final Pattern regularLinkDescriptionEndPattern = Pattern.compile(
      "^\\]\\]", Pattern.CASE_INSENSITIVE);

  private static final Pattern textMarkupStartPrecedingPattern = Pattern.compile(
      "^[ \t\r\n\"'(\\-{]", Pattern.CASE_INSENSITIVE);
  private static final Pattern textMarkupStartFollowingPattern = Pattern.compile(
      "^[^ \t\r\n]", Pattern.CASE_INSENSITIVE);
  private static final Pattern textMarkupEndPrecedingPattern = Pattern.compile(
      "^[^ \t\r\n]", Pattern.CASE_INSENSITIVE);
  private static final Pattern textMarkupEndFollowingPattern = Pattern.compile(
      "^([ \t\r\n!\"'),\\-.:;?\\[}]|$)", Pattern.CASE_INSENSITIVE);
  private static final Pattern textMarkupMarkerPattern = Pattern.compile(
      "^[*+/=_~]", Pattern.CASE_INSENSITIVE);
  private static final Pattern textMarkupVerbatimEndPattern = Pattern.compile(
      "^=", Pattern.CASE_INSENSITIVE);
  private static final Pattern textMarkupCodeEndPattern = Pattern.compile(
      "^~", Pattern.CASE_INSENSITIVE);

  private String code;
  private int pos;
  private char curChar;
  private String curString;

  private DummyGenerator dummyGenerator;
  private int dummyCounter;
  private int indentation;
  private String appendAtEndOfLine;
  private Stack<ElementType> elementTypeStack;
  private @Nullable String latexEnvironmentName;
  private Stack<ObjectType> objectTypeStack;

  private String language;

  public OrgAnnotatedTextBuilder(String codeLanguageId) {
    super(codeLanguageId);
    this.language = "en-US";
    reinitialize();
  }

  @EnsuresNonNull({"code", "curString", "dummyGenerator", "appendAtEndOfLine",
      "elementTypeStack", "objectTypeStack"})
  public void reinitialize(@UnknownInitialization OrgAnnotatedTextBuilder this) {
    this.code = "";
    this.pos = 0;
    this.curString = "";

    this.dummyGenerator = new DummyGenerator();
    this.dummyCounter = 0;
    this.indentation = -1;
    this.appendAtEndOfLine = "";
    this.elementTypeStack = new Stack<>();
    this.elementTypeStack.push(ElementType.PARAGRAPH);
    this.latexEnvironmentName = null;
    this.objectTypeStack = new Stack<>();
  }

  @Override
  public void setSettings(Settings settings) {
    super.setSettings(settings);
    this.language = settings.getLanguageShortCode();
  }

  public OrgAnnotatedTextBuilder addText(@Nullable String text) {
    if ((text == null) || text.isEmpty()) return this;
    super.addText(text);
    this.pos += text.length();
    return this;
  }

  public OrgAnnotatedTextBuilder addMarkup(@Nullable String markup) {
    if ((markup == null) || markup.isEmpty()) return this;
    super.addMarkup(markup);
    this.pos += markup.length();
    return this;
  }

  public OrgAnnotatedTextBuilder addMarkup(@Nullable String markup, @Nullable String interpretAs) {
    if ((interpretAs == null) || interpretAs.isEmpty()) {
      return addMarkup(markup);
    } else {
      if (markup == null) markup = "";
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
      boolean isStartOfLine = ((this.pos == 0) || this.code.charAt(this.pos - 1) == '\n');

      if (isStartOfLine) {
        @Nullable Matcher whitespaceMatcher = matchFromPosition(whitespacePattern);
        String whitespace = ((whitespaceMatcher != null) ? whitespaceMatcher.group() : "");
        this.indentation = whitespace.length();
        addMarkup(whitespace);
        this.curChar = this.code.charAt(this.pos);
        this.curString = String.valueOf(this.curChar);
      }

      @Nullable Matcher matcher;

      if (this.objectTypeStack.contains(ObjectType.VERBATIM)) {
        if ((matcher = matchInlineEndFromPosition(textMarkupVerbatimEndPattern)) != null) {
          popObjectType();
          addMarkup(matcher.group(), generateDummy());
        } else {
          addMarkup(this.curString);
        }

        continue;
      } else if (this.objectTypeStack.contains(ObjectType.CODE)) {
        if ((matcher = matchInlineEndFromPosition(textMarkupCodeEndPattern)) != null) {
          popObjectType();
          addMarkup(matcher.group(), generateDummy());
        } else {
          addMarkup(this.curString);
        }

        continue;
      }

      if (isStartOfLine) {
        boolean elementFound = true;

        if (this.elementTypeStack.contains(ElementType.TABLE)
              && (matchFromPosition(tableRowPattern) == null)) {
          popElementType();
        }

        if (this.elementTypeStack.contains(ElementType.COMMENT_BLOCK)
              || this.elementTypeStack.contains(ElementType.EXAMPLE_BLOCK)
              || this.elementTypeStack.contains(ElementType.EXPORT_BLOCK)
              || this.elementTypeStack.contains(ElementType.SOURCE_BLOCK)) {
          if ((matcher = matchFromPosition(blockEndPattern)) != null) {
            popElementType();
            addMarkup(matcher.group());
          } else {
            addMarkup(this.curString);
          }
        } else if (this.elementTypeStack.contains(ElementType.PROPERTY_DRAWER)) {
          if ((matcher = matchFromPosition(drawerEndPattern)) != null) {
            popElementType();
            addMarkup(matcher.group());
          } else {
            addMarkup(this.curString);
          }
        } else if (this.elementTypeStack.contains(ElementType.LATEX_ENVIRONMENT)) {
          if (((matcher = matchFromPosition(latexEnvironmentEndPattern)) != null)
                && (this.latexEnvironmentName != null)
                && this.latexEnvironmentName.equals(matcher.group(1))) {
            popElementType();
            this.latexEnvironmentName = null;
            addMarkup(matcher.group());
          } else {
            addMarkup(this.curString);
          }
        } else if ((this.indentation == 0)
              && ((matcher = matchFromPosition(headlineCommentPattern)) != null)) {
          addMarkup(matcher.group(), "\n");
        } else if ((this.indentation == 0)
              && ((matcher = matchFromPosition(headlinePattern)) != null)) {
          this.elementTypeStack.push(ElementType.HEADLINE);
          this.appendAtEndOfLine = "\n";
          addMarkup(matcher.group(), "\n");
        } else if ((matcher = matchFromPosition(affiliatedKeywordsPattern)) != null) {
          addMarkup(matcher.group());
        } else if ((matcher = matchFromPosition(blockBeginPattern)) != null) {
          @Nullable String blockType = matcher.group(1);
          ElementType elementType = ElementType.GREATER_SPECIAL_BLOCK;

          if (blockType == null) {
            elementType = ElementType.GREATER_SPECIAL_BLOCK;
          } else if (blockType.equalsIgnoreCase("CENTER")) {
            elementType = ElementType.GREATER_CENTER_BLOCK;
          } else if (blockType.equalsIgnoreCase("QUOTE")) {
            elementType = ElementType.GREATER_QUOTE_BLOCK;
          } else if (blockType.equalsIgnoreCase("COMMENT")) {
            elementType = ElementType.COMMENT_BLOCK;
          } else if (blockType.equalsIgnoreCase("EXAMPLE")) {
            elementType = ElementType.EXAMPLE_BLOCK;
          } else if (blockType.equalsIgnoreCase("EXPORT")) {
            elementType = ElementType.EXPORT_BLOCK;
          } else if (blockType.equalsIgnoreCase("SRC")) {
            elementType = ElementType.SOURCE_BLOCK;
          } else if (blockType.equalsIgnoreCase("VERSE")) {
            elementType = ElementType.VERSE_BLOCK;
          }

          this.elementTypeStack.push(elementType);
          addMarkup(matcher.group());
        } else if ((matcher = matchFromPosition(blockEndPattern)) != null) {
          popElementType();
          addMarkup(matcher.group());
        } else if ((matcher = matchFromPosition(drawerBeginPattern)) != null) {
          @Nullable String drawerName = matcher.group(1);

          if ((drawerName != null) && drawerName.equalsIgnoreCase("PROPERTIES")) {
            this.elementTypeStack.push(ElementType.PROPERTY_DRAWER);
          } else {
            this.elementTypeStack.push(ElementType.DRAWER);
          }

          addMarkup(matcher.group());
        } else if ((matcher = matchFromPosition(drawerEndPattern)) != null) {
          popElementType();
          addMarkup(matcher.group());
        } else if ((matcher = matchFromPosition(dynamicBlockBeginPattern)) != null) {
          this.elementTypeStack.push(ElementType.DYNAMIC_BLOCK);
          addMarkup(matcher.group());
        } else if ((matcher = matchFromPosition(dynamicBlockEndPattern)) != null) {
          popElementType();
          addMarkup(matcher.group());
        } else if ((matcher = matchFromPosition(footnoteDefinitionPattern)) != null) {
          addMarkup(matcher.group());
        } else if (((matcher = matchFromPosition(ruleTableRowPattern)) != null)
              || ((matcher = matchFromPosition(tableRowPattern)) != null)) {
          if (!this.elementTypeStack.contains(ElementType.TABLE)) {
            this.elementTypeStack.push(ElementType.TABLE);
          }

          this.appendAtEndOfLine = "\n";
          addMarkup(matcher.group(), "\n");
        } else if ((matcher = matchFromPosition(itemPattern)) != null) {
          this.appendAtEndOfLine = "\n";
          addMarkup(matcher.group(), "\n");
        } else if ((matcher = matchFromPosition(babelCallPattern)) != null) {
          addMarkup(matcher.group());
        } else if ((matcher = matchFromPosition(clockPattern)) != null) {
          addMarkup(matcher.group());
        } else if ((matcher = matchFromPosition(diarySexpPattern)) != null) {
          addMarkup(matcher.group());
        } else if ((matcher = matchFromPosition(planningPattern)) != null) {
          addMarkup(matcher.group());
        } else if ((matcher = matchFromPosition(commentPattern)) != null) {
          addMarkup(matcher.group());
        } else if ((matcher = matchFromPosition(fixedWidthLinePattern)) != null) {
          addMarkup(matcher.group());
        } else if ((matcher = matchFromPosition(horizontalRulePattern)) != null) {
          addMarkup(matcher.group());
        } else if ((matcher = matchFromPosition(keywordPattern)) != null) {
          addMarkup(matcher.group());
        } else if ((matcher = matchFromPosition(latexEnvironmentBeginPattern)) != null) {
          this.elementTypeStack.push(ElementType.LATEX_ENVIRONMENT);
          this.latexEnvironmentName = matcher.group(1);
          addMarkup(matcher.group());
        } else {
          elementFound = false;
        }

        if (elementFound) continue;
      }

      if (this.elementTypeStack.contains(ElementType.COMMENT_BLOCK)
            || this.elementTypeStack.contains(ElementType.EXAMPLE_BLOCK)
            || this.elementTypeStack.contains(ElementType.EXPORT_BLOCK)
            || this.elementTypeStack.contains(ElementType.SOURCE_BLOCK)
            || this.elementTypeStack.contains(ElementType.PROPERTY_DRAWER)
            || this.elementTypeStack.contains(ElementType.LATEX_ENVIRONMENT)) {
        addMarkup(this.curString);
        continue;
      }

      if (this.elementTypeStack.contains(ElementType.HEADLINE)
            && ((matcher = matchFromPosition(headlineTagsPattern)) != null)) {
        addMarkup(matcher.group());
      } else if (this.elementTypeStack.contains(ElementType.TABLE)
            && ((matcher = matchFromPosition(tableCellSeparatorPattern)) != null)) {
        addMarkup(matcher.group(), "\n\n");
      } else if (this.objectTypeStack.contains(ObjectType.REGULAR_LINK_DESCRIPTION)
            && ((matcher = matchFromPosition(regularLinkDescriptionEndPattern)) != null)) {
        popObjectType();
        addMarkup(matcher.group());
      } else if ((matcher = matchFromPosition(latexFragmentPattern1)) != null) {
        addMarkup(matcher.group(), generateDummy());
      } else if ((matcher = matchFromPosition(latexFragmentPattern2)) != null) {
        addMarkup(matcher.group(), generateDummy());
      } else if ((matcher = matchFromPosition(latexFragmentPattern3)) != null) {
        addMarkup(matcher.group(), generateDummy());
      } else if ((matcher = matchFromPosition(latexFragmentPattern4)) != null) {
        addMarkup(matcher.group(), generateDummy());
      } else if ((isStartOfLine || this.code.charAt(this.pos - 1) != '$')
            && ((matcher = matchFromPosition(latexFragmentPattern5)) != null)) {
        addMarkup(matcher.group(), generateDummy());
      } else if ((isStartOfLine || this.code.charAt(this.pos - 1) != '$')
            && ((matcher = matchFromPosition(latexFragmentPattern6)) != null)) {
        addMarkup(matcher.group(), generateDummy());
      } else if ((matcher = matchFromPosition(exportSnippetPattern)) != null) {
        addMarkup(matcher.group());
      } else if ((matcher = matchFromPosition(footnoteReferencePattern1)) != null) {
        addMarkup(matcher.group());
      } else if ((matcher = matchFromPosition(footnoteReferencePattern2)) != null) {
        addMarkup(matcher.group());
      } else if ((matcher = matchFromPosition(inlineBabelCallPattern)) != null) {
        addMarkup(matcher.group(), generateDummy());
      } else if ((matcher = matchFromPosition(inlineSourceBlockPattern)) != null) {
        addMarkup(matcher.group(), generateDummy());
      } else if ((matcher = matchFromPosition(macroPattern)) != null) {
        addMarkup(matcher.group(), generateDummy());
      } else if ((matcher = matchFromPosition(statisticsCookiePattern)) != null) {
        addMarkup(matcher.group(), generateDummy());
      } else if ((matcher = matchFromPosition(diaryTimestampPattern)) != null) {
        addMarkup(matcher.group(), generateDummy());
      } else if ((matcher = matchFromPosition(activeTimestampRangePattern1)) != null) {
        addMarkup(matcher.group(), generateDummy());
      } else if ((matcher = matchFromPosition(activeTimestampRangePattern2)) != null) {
        addMarkup(matcher.group(), generateDummy());
      } else if ((matcher = matchFromPosition(inactiveTimestampRangePattern1)) != null) {
        addMarkup(matcher.group(), generateDummy());
      } else if ((matcher = matchFromPosition(inactiveTimestampRangePattern2)) != null) {
        addMarkup(matcher.group(), generateDummy());
      } else if ((matcher = matchFromPosition(activeTimestampPattern)) != null) {
        addMarkup(matcher.group(), generateDummy());
      } else if ((matcher = matchFromPosition(inactiveTimestampPattern)) != null) {
        addMarkup(matcher.group(), generateDummy());
      } else if (((matcher = matchFromPosition(radioTargetPattern)) != null)
            && (isStartOfLine || linkPrecedingPattern.matcher(
              String.valueOf(this.code.charAt(this.pos - 1))).find())) {
        addMarkup(matcher.group(), generateDummy());
      } else if (((matcher = matchFromPosition(targetPattern)) != null)
            && (isStartOfLine || linkPrecedingPattern.matcher(
              String.valueOf(this.code.charAt(this.pos - 1))).find())) {
        addMarkup(matcher.group(), generateDummy());
      } else if ((matcher = matchFromPosition(angleLinkPattern)) != null) {
        addMarkup(matcher.group(), generateDummy());
      } else if (((matcher = matchFromPosition(plainLinkPattern)) != null)
            && (isStartOfLine || linkPrecedingPattern.matcher(
              String.valueOf(this.code.charAt(this.pos - 1))).find())) {
        addMarkup(matcher.group(), generateDummy());
      } else if ((matcher = matchFromPosition(regularLinkPatternWithoutDescription)) != null) {
        addMarkup(matcher.group(), generateDummy());
      } else if ((matcher = matchFromPosition(regularLinkPatternWithDescription)) != null) {
        this.objectTypeStack.add(ObjectType.REGULAR_LINK_DESCRIPTION);
        addMarkup(matcher.group());
      } else if (((matcher = matchInlineStartFromPosition(textMarkupMarkerPattern)) != null)
            || ((matcher = matchInlineEndFromPosition(textMarkupMarkerPattern)) != null)) {
        String textMarkupMarker = matcher.group();

        if (textMarkupMarker.equals("*")) {
          toggleObjectType(ObjectType.BOLD);
          addMarkup(textMarkupMarker);
        } else if (textMarkupMarker.equals("+")) {
          toggleObjectType(ObjectType.STRIKETHROUGH);
          addMarkup(textMarkupMarker);
        } else if (textMarkupMarker.equals("/")) {
          toggleObjectType(ObjectType.ITALIC);
          addMarkup(textMarkupMarker);
        } else if (textMarkupMarker.equals("=")) {
          toggleObjectType(ObjectType.VERBATIM);
          addMarkup(textMarkupMarker);
        } else if (textMarkupMarker.equals("_")) {
          toggleObjectType(ObjectType.UNDERLINE);
          addMarkup(textMarkupMarker);
        } else if (textMarkupMarker.equals("~")) {
          toggleObjectType(ObjectType.CODE);
          addMarkup(textMarkupMarker);
        } else {
          addText(textMarkupMarker);
        }
      } else if (this.curChar == '\n') {
        addMarkup("\n", "\n" + this.appendAtEndOfLine);
        this.appendAtEndOfLine = "";
        if (this.elementTypeStack.contains(ElementType.HEADLINE)) popElementType();
      } else {
        addText(this.curString);
      }
    }

    addMarkup("", this.appendAtEndOfLine);

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
    if ((this.pos > 0)
          && (matchFromPosition(textMarkupStartPrecedingPattern, this.pos - 1) == null)) {
      return null;
    }

    @Nullable Matcher matcher = matchFromPosition(pattern);
    if (matcher == null) return null;
    if ((this.pos == 0) || (this.pos >= this.code.length() - 1)) return matcher;

    return ((matchFromPosition(textMarkupStartFollowingPattern,
        this.pos + matcher.group().length()) == null) ? null : matcher);
  }

  private @Nullable Matcher matchInlineEndFromPosition(Pattern pattern) {
    if ((this.pos == 0)
          || (matchFromPosition(textMarkupEndPrecedingPattern, this.pos - 1) == null)) {
      return null;
    }

    @Nullable Matcher matcher = matchFromPosition(pattern);
    if (matcher == null) return null;

    return ((matchFromPosition(textMarkupEndFollowingPattern,
        this.pos + matcher.group().length()) != null) ? matcher : null);
  }

  private String generateDummy() {
    return this.dummyGenerator.generate(this.language, this.dummyCounter++);
  }

  private void popElementType() {
    if (!this.elementTypeStack.isEmpty()) this.elementTypeStack.pop();
    if (this.elementTypeStack.isEmpty()) this.elementTypeStack.push(ElementType.PARAGRAPH);
  }

  private void popObjectType() {
    if (!this.objectTypeStack.isEmpty()) this.objectTypeStack.pop();
  }

  private void toggleObjectType(ObjectType objectType) {
    if (!this.objectTypeStack.isEmpty() && (this.objectTypeStack.peek() == objectType)) {
      popObjectType();
    } else {
      this.objectTypeStack.push(objectType);
    }
  }
}
