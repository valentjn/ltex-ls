/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.latex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.text.StringEscapeUtils;
import org.bsplines.ltexls.parsing.CodeAnnotatedTextBuilder;
import org.bsplines.ltexls.parsing.DummyGenerator;
import org.bsplines.ltexls.settings.Settings;
import org.bsplines.ltexls.tools.ExcludeFromGeneratedCoverage;
import org.bsplines.ltexls.tools.Tools;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class LatexAnnotatedTextBuilder extends CodeAnnotatedTextBuilder {
  private enum MathVowelState {
    UNDECIDED,
    STARTS_WITH_VOWEL,
    STARTS_WITH_CONSONANT,
  }

  private enum Mode {
    PARAGRAPH_TEXT,
    INLINE_TEXT,
    HEADING,
    INLINE_MATH,
    DISPLAY_MATH,
    IGNORE_ENVIRONMENT,
    RSWEAVE,
  }

  private static final Pattern commandPattern = Pattern.compile(
      "^\\\\(([^A-Za-z@]|([A-Za-z@]+))\\*?)");
  private static final Pattern argumentPattern = Pattern.compile("^\\{[^\\}]*?\\}");
  private static final Pattern commentPattern = Pattern.compile(
      "^%.*?($|((\n|\r|\r\n)[ \n\r\t]*))");
  private static final Pattern whitespacePattern = Pattern.compile(
      "^[ \n\r\t]+(%.*?($|((\n|\r|\r\n)[ \n\r\t]*)))?");
  private static final Pattern lengthPattern = Pattern.compile(
      "-?[0-9]*(\\.[0-9]+)?(pt|mm|cm|ex|em|bp|dd|pc|in)");
  private static final Pattern lengthInBracePattern = Pattern.compile(
      "^\\{" + lengthPattern.pattern() + "\\}");
  private static final Pattern lengthInBracketPattern = Pattern.compile(
      "^\\[" + lengthPattern.pattern() + "\\]");
  private static final Pattern emDashPattern = Pattern.compile("^---");
  private static final Pattern enDashPattern = Pattern.compile("^--");
  private static final Pattern accentPattern1 = Pattern.compile(
      "^(\\\\[`'\\^~\"=\\.])(([A-Za-z]|\\\\i|\\\\j)|(\\{([A-Za-z]|\\\\i|\\\\j)\\}))");
  private static final Pattern accentPattern2 = Pattern.compile(
      "^(\\\\[Hbcdkruv])( *([A-Za-z]|\\\\i|\\\\j)|\\{([A-Za-z]|\\\\i|\\\\j)\\})");
  private static final Pattern displayMathPattern = Pattern.compile("^\\$\\$");
  private static final Pattern verbCommandPattern = Pattern.compile("^\\\\verb\\*?(.).*?\\1");
  private static final Pattern rsweaveBeginPattern = Pattern.compile("^<<.*?>>=");
  private static final Pattern rsweaveEndPattern = Pattern.compile("^@");

  private static final List<String> mathEnvironments = Arrays.asList(
      "align", "align*", "alignat", "alignat*",
      "displaymath", "eqnarray", "eqnarray*", "equation", "equation*", "flalign", "flalign*",
      "gather", "gather*", "math", "multline", "multline*");

  private String code;
  private int pos;
  private int dummyCounter;
  private String lastSpace;
  private String lastPunctuation;
  private String dummyLastSpace;
  private String dummyLastPunctuation;
  private boolean isMathEmpty;
  private MathVowelState mathVowelState;
  private boolean preserveDummyLast;
  private boolean canInsertSpaceBeforeDummy;
  private boolean isMathCharTrivial;
  private @Nullable Pattern ignoreEnvironmentEndPattern;
  private Stack<Mode> modeStack;

  private char curChar;
  private String curString;
  private Mode curMode;

  private String language;
  private String codeLanguageId;
  private List<LatexCommandSignature> commandSignatures;
  private Map<String, List<LatexCommandSignature>> commandSignatureMap;
  private List<LatexEnvironmentSignature> environmentSignatures;
  private Map<String, List<LatexEnvironmentSignature>> environmentSignatureMap;
  private boolean isInStrictMode;

  public LatexAnnotatedTextBuilder(String codeLanguageId) {
    this.language = "en-US";
    this.codeLanguageId = codeLanguageId;
    this.commandSignatures = new ArrayList<>(
        LatexAnnotatedTextBuilderDefaults.getDefaultLatexCommandSignatures());
    this.commandSignatureMap = createCommandSignatureMap(this.commandSignatures);
    this.environmentSignatures = new ArrayList<>(
        LatexAnnotatedTextBuilderDefaults.getDefaultLatexEnvironmentSignatures());
    this.environmentSignatureMap = createCommandSignatureMap(this.environmentSignatures);
    this.isInStrictMode = false;

    reinitialize();
  }

  @EnsuresNonNull({"code", "lastSpace", "lastPunctuation", "dummyLastSpace", "dummyLastPunctuation",
      "mathVowelState", "modeStack", "curString", "curMode"})
  public void reinitialize(@UnknownInitialization LatexAnnotatedTextBuilder this) {
    this.code = "";
    this.pos = 0;
    this.dummyCounter = 0;
    this.lastSpace = "";
    this.lastPunctuation = "";
    this.dummyLastSpace = "";
    this.dummyLastPunctuation = "";
    this.isMathEmpty = true;
    this.mathVowelState = MathVowelState.UNDECIDED;
    this.preserveDummyLast = false;
    this.canInsertSpaceBeforeDummy = false;
    this.isMathCharTrivial = false;
    this.ignoreEnvironmentEndPattern = null;

    this.modeStack = new Stack<>();
    this.modeStack.push(Mode.PARAGRAPH_TEXT);

    this.curChar = ' ';
    this.curString = "";
    this.curMode = Mode.PARAGRAPH_TEXT;
  }

  private static <T extends LatexCommandSignature> Map<String, List<T>> createCommandSignatureMap(
        List<T> commandSignatures) {
    Map<String, List<T>> map = new HashMap<>();

    for (T commandSignature : commandSignatures) {
      String commandPrefix = commandSignature.getPrefix();
      if (!map.containsKey(commandPrefix)) map.put(commandPrefix, new ArrayList<>());
      map.get(commandPrefix).add(commandSignature);
    }

    return map;
  }

  private static boolean isPunctuation(char ch) {
    return ((ch == '.') || (ch == ',') || (ch == ':') || (ch == ';') || (ch == '\u2026'));
  }

  private static boolean isVowel(char ch) {
    ch = Character.toLowerCase(ch);
    return ((ch == 'a') || (ch == 'e') || (ch == 'f') || (ch == 'h') || (ch == 'i') || (ch == 'l')
        || (ch == 'm') || (ch == 'n') || (ch == 'o') || (ch == 'r') || (ch == 's') || (ch == 'x'));
  }

  private static boolean isMathMode(Mode mode) {
    return ((mode == Mode.INLINE_MATH) || (mode == Mode.DISPLAY_MATH));
  }

  private static boolean isIgnoreEnvironmentMode(Mode mode) {
    return (mode == Mode.IGNORE_ENVIRONMENT);
  }

  private static boolean isRsweaveMode(Mode mode) {
    return (mode == Mode.RSWEAVE);
  }

  private static boolean isTextMode(Mode mode) {
    return !isMathMode(mode) && !isIgnoreEnvironmentMode(mode);
  }

  private static boolean containsTwoEndsOfLine(String text) {
    return (text.contains("\n\n") || text.contains("\r\r") || text.contains("\r\n\r\n"));
  }

  @Override
  public void setSettings(Settings settings) {
    this.language = settings.getLanguageShortCode();

    for (Map.Entry<String, String> entry : settings.getLatexCommands().entrySet()) {
      String actionString = entry.getValue();
      LatexCommandSignature.Action action;
      @Nullable DummyGenerator dummyGenerator = null;

      if (actionString.equals("default")) {
        action = LatexCommandSignature.Action.DEFAULT;
      } else if (actionString.equals("ignore")) {
        action = LatexCommandSignature.Action.IGNORE;
      } else if (actionString.equals("dummy")) {
        action = LatexCommandSignature.Action.DUMMY;
      } else if (actionString.equals("pluralDummy")) {
        action = LatexCommandSignature.Action.DUMMY;
        dummyGenerator = DummyGenerator.getDefault(true);
      } else {
        continue;
      }

      if (dummyGenerator == null) dummyGenerator = DummyGenerator.getDefault();
      this.commandSignatures.add(new LatexCommandSignature(entry.getKey(), action, dummyGenerator));
    }

    this.commandSignatureMap = createCommandSignatureMap(this.commandSignatures);

    for (Map.Entry<String, String> entry : settings.getLatexEnvironments().entrySet()) {
      String actionString = entry.getValue();
      LatexEnvironmentSignature.Action action;

      if (actionString.equals("default")) {
        action = LatexEnvironmentSignature.Action.DEFAULT;
      } else if (actionString.equals("ignore")) {
        action = LatexEnvironmentSignature.Action.IGNORE;
      } else {
        continue;
      }

      this.environmentSignatures.add(new LatexEnvironmentSignature(entry.getKey(), action));
    }

    this.environmentSignatureMap = createCommandSignatureMap(this.environmentSignatures);
  }

  public void setInStrictMode(boolean isInStrictMode) {
    this.isInStrictMode = isInStrictMode;
  }

  public LatexAnnotatedTextBuilder addText(String text) {
    if (text.isEmpty()) return this;
    super.addText(text);
    this.pos += text.length();
    textAdded(text);
    return this;
  }

  public LatexAnnotatedTextBuilder addMarkup(String markup) {
    if (markup.isEmpty()) return this;
    super.addMarkup(markup);
    this.pos += markup.length();

    if (this.preserveDummyLast) {
      this.preserveDummyLast = false;
    } else {
      this.dummyLastSpace = "";
      this.dummyLastPunctuation = "";
    }

    return this;
  }

  public LatexAnnotatedTextBuilder addMarkup(String markup, String interpretAs) {
    if (interpretAs.isEmpty()) {
      return addMarkup(markup);
    } else {
      super.addMarkup(markup, interpretAs);
      this.pos += markup.length();
      this.preserveDummyLast = false;
      textAdded(interpretAs);
      return this;
    }
  }

  public LatexAnnotatedTextBuilder addCode(String code) {
    reinitialize();
    this.code = code;

    int lastPos = -1;

    while (this.pos < code.length()) {
      this.curChar = code.charAt(this.pos);
      this.curString = String.valueOf(this.curChar);
      this.curMode = this.modeStack.peek();
      this.isMathCharTrivial = false;
      lastPos = this.pos;

      if (isIgnoreEnvironmentMode(this.curMode)) {
        if (this.ignoreEnvironmentEndPattern != null) {
          String ignoreEnvironmentEnd = matchFromPosition(this.ignoreEnvironmentEndPattern);

          if (ignoreEnvironmentEnd.isEmpty()) {
            addMarkup(this.curString);
          } else {
            popMode();
            addMarkup(ignoreEnvironmentEnd);
          }
        } else {
          Tools.logger.warning(Tools.i18n("ignoreEnvironmentEndPatternNotSet"));
          popMode();
        }
      } else if (this.codeLanguageId.equals("rsweave") && isRsweaveMode(this.curMode)) {
        String rsweaveEnd = matchFromPosition(rsweaveEndPattern);

        if (rsweaveEnd.isEmpty()) {
          addMarkup(this.curString);
        } else {
          popMode();
          addMarkup(rsweaveEnd);
        }
      } else {
        switch (this.curChar) {
          case '\\': {
            processBackslash();
            break;
          }
          case '{': {
            processOpeningBrace();
            break;
          }
          case '}': {
            processClosingBrace();
            break;
          }
          case '$': {
            processDollar();
            break;
          }
          case '%': {
            processPercentage();
            break;
          }
          case ' ':
          case '&':
          case '~':
          case '\n':
          case '\r':
          case '\t': {
            processWhitespace();
            break;
          }
          case '`':
          case '\'':
          case '"': {
            processQuotationMark();
            break;
          }
          default: {
            processDefaultCharacter();
            break;
          }
        }
      }

      if (!this.isMathCharTrivial) {
        this.canInsertSpaceBeforeDummy = false;
        this.isMathEmpty = false;
      }

      if (this.pos == lastPos) this.onInfiniteLoop();
    }

    return this;
  }

  private void processBackslash() {
    String command = matchFromPosition(commandPattern);

    if (command.equals("\\begin") || command.equals("\\end")) {
      this.preserveDummyLast = true;

      String argument = matchFromPosition(argumentPattern, this.pos + command.length());
      String environmentName = ((argument.length() >= 2)
          ? argument.substring(1, argument.length() - 1) : "");
      boolean argumentsProcessed = false;
      String interpretAs = "";

      if (mathEnvironments.contains(environmentName)) {
        addMarkup(command);

        if (command.equals("\\begin")) {
          if (environmentName.equals("math")) {
            enterInlineMath();
          } else {
            enterDisplayMath();
          }
        } else {
          popMode();
          interpretAs = generateDummy();
        }
      } else if (command.equals("\\begin")) {
        @Nullable List<LatexEnvironmentSignature> possibleEnvironmentSignatures =
            this.environmentSignatureMap.get(command + argument);

        if (possibleEnvironmentSignatures == null) {
          possibleEnvironmentSignatures = Collections.emptyList();
        }

        String match = "";
        @Nullable LatexEnvironmentSignature matchingEnvironmentSignature = null;

        for (LatexEnvironmentSignature latexEnvironmentSignature : possibleEnvironmentSignatures) {
          String curMatch = latexEnvironmentSignature.matchFromPosition(this.code, this.pos);

          if (!curMatch.isEmpty() && ((curMatch.length() >= match.length())
                || latexEnvironmentSignature.doesIgnoreAllArguments())) {
            match = curMatch;
            matchingEnvironmentSignature = latexEnvironmentSignature;
          }
        }

        if (matchingEnvironmentSignature != null) {
          if (matchingEnvironmentSignature.getAction() == LatexEnvironmentSignature.Action.IGNORE) {
            this.modeStack.push(Mode.IGNORE_ENVIRONMENT);
            this.ignoreEnvironmentEndPattern = Pattern.compile(
                "^\\\\end\\{" + Pattern.quote(environmentName) + "\\}");
          }

          if (matchingEnvironmentSignature.doesIgnoreAllArguments()) {
            addMarkup(command);
          } else {
            addMarkup(match);
            argumentsProcessed = true;
          }
        } else {
          addMarkup(command);
          this.modeStack.push(this.curMode);
        }
      } else {
        addMarkup(command);
        popMode();
      }

      if (!isIgnoreEnvironmentMode(this.modeStack.peek())) {
        this.isMathCharTrivial = true;
        this.preserveDummyLast = true;

        if (!argumentsProcessed) {
          addMarkup(argument, interpretAs);
          if (command.equals("\\begin")) processEnvironmentArguments();
        }
      }
    } else if (command.equals("\\$") || command.equals("\\%") || command.equals("\\&")) {
      addMarkup(command, command.substring(1));
    } else if (command.equals("\\[")) {
      enterDisplayMath();
      addMarkup(command);
    } else if (command.equals("\\(")) {
      enterInlineMath();
      addMarkup(command);
    } else if (command.equals("\\]") || command.equals("\\)")) {
      popMode();
      addMarkup(command, generateDummy());
    } else if (command.equals("\\AA")) {
      // capital A with ring above
      addMarkup(command, (isMathMode(this.curMode) ? "" : "\u00c5"));
    } else if (command.equals("\\L")) {
      // capital L with stroke
      addMarkup(command, (isMathMode(this.curMode) ? "" : "\u0141"));
    } else if (command.equals("\\O")) {
      // capital O with stroke
      addMarkup(command, (isMathMode(this.curMode) ? "" : "\u00d8"));
    } else if (command.equals("\\SS")) {
      // capital sharp S
      addMarkup(command, (isMathMode(this.curMode) ? "" : "\u1e9e"));
    } else if (command.equals("\\aa")) {
      // small a with ring above
      addMarkup(command, (isMathMode(this.curMode) ? "" : "\u00e5"));
    } else if (command.equals("\\i")) {
      // small i without dot
      addMarkup(command, (isMathMode(this.curMode) ? "" : "\u0131"));
    } else if (command.equals("\\j")) {
      // small j without dot
      addMarkup(command, (isMathMode(this.curMode) ? "" : "\u0237"));
    } else if (command.equals("\\l")) {
      // small l with stroke
      addMarkup(command, (isMathMode(this.curMode) ? "" : "\u0142"));
    } else if (command.equals("\\o")) {
      // small o with stroke
      addMarkup(command, (isMathMode(this.curMode) ? "" : "\u00f8"));
    } else if (command.equals("\\ss")) {
      // small sharp s
      addMarkup(command, (isMathMode(this.curMode) ? "" : "\u00df"));
    } else if (command.equals("\\`") || command.equals("\\'") || command.equals("\\^")
          || command.equals("\\~") || command.equals("\\\"") || command.equals("\\=")
          || command.equals("\\.")) {
      Matcher matcher = accentPattern1.matcher(this.code.substring(this.pos));

      if (!isMathMode(this.curMode) && matcher.find()) {
        @Nullable String accentCommand = matcher.group(1);
        @Nullable String letter = ((matcher.group(3) != null)
            ? matcher.group(3) : matcher.group(5));
        String interpretAs = (((accentCommand != null) && (letter != null))
            ? convertAccentCommandToUnicode(accentCommand, letter) : "");
        addMarkup(matcher.group(), interpretAs);
      } else {
        addMarkup(command);
      }
    } else if (command.equals("\\H") || command.equals("\\b") || command.equals("\\c")
          || command.equals("\\d") || command.equals("\\k") || command.equals("\\r")
          || command.equals("\\u") || command.equals("\\v")) {
      Matcher matcher = accentPattern2.matcher(this.code.substring(this.pos));

      if (!isMathMode(this.curMode) && matcher.find()) {
        @Nullable String accentCommand = matcher.group(1);
        @Nullable String letter = ((matcher.group(3) != null)
            ? matcher.group(3) : matcher.group(4));
        String interpretAs = (((accentCommand != null) && (letter != null))
            ? convertAccentCommandToUnicode(accentCommand, letter) : "");
        addMarkup(matcher.group(), interpretAs);
      } else {
        addMarkup(command);
      }
    } else if (command.equals("\\-")) {
      addMarkup(command);
    } else if (command.equals("\\ ") || command.equals("\\,") || command.equals("\\;")
          || command.equals("\\\\") || command.equals("\\hfill")
          || command.equals("\\hspace") || command.equals("\\hspace*")
          || command.equals("\\quad") || command.equals("\\qquad")
          || command.equals("\\newline")) {
      if (command.equals("\\hspace") || command.equals("\\hspace*")) {
        String argument = matchFromPosition(argumentPattern, this.pos + command.length());
        command += argument;
      }

      if (isMathMode(this.curMode) && this.lastSpace.isEmpty()
            && this.canInsertSpaceBeforeDummy) {
        addMarkup(command, " ");
      } else {
        this.preserveDummyLast = true;

        if (isMathMode(this.curMode)) {
          addMarkup(command);
          this.dummyLastSpace = " ";
        } else {
          String space = " ";

          if (!this.lastSpace.isEmpty()) {
            space = "";
          } else if (command.equals("\\,")) {
            space = "\u202f";
          }

          addMarkup(command, space);
        }
      }
    } else if (command.equals("\\dots")
          || command.equals("\\eg") || command.equals("\\egc") || command.equals("\\euro")
          || command.equals("\\ie") || command.equals("\\iec")) {
      String interpretAs = "";

      if (!isMathMode(this.curMode)) {
        if (command.equals("\\dots")) {
          interpretAs = "\u2026";
        } else if (command.equals("\\eg")) {
          interpretAs = "e.g.";
        } else if (command.equals("\\egc")) {
          interpretAs = "e.g.,";
        } else if (command.equals("\\euro")) {
          interpretAs = "\u20ac";
        } else if (command.equals("\\ie")) {
          interpretAs = "i.e.";
        } else if (command.equals("\\iec")) {
          interpretAs = "i.e.,";
        }
      }

      addMarkup(command, interpretAs);
    } else if (command.equals("\\notag") || command.equals("\\qed")) {
      this.preserveDummyLast = true;
      addMarkup(command);
    } else if (command.equals("\\part") || command.equals("\\chapter")
          || command.equals("\\section") || command.equals("\\subsection")
          || command.equals("\\subsubsection") || command.equals("\\paragraph")
          || command.equals("\\subparagraph")
          || command.equals("\\part*") || command.equals("\\chapter*")
          || command.equals("\\section*") || command.equals("\\subsection*")
          || command.equals("\\subsubsection*") || command.equals("\\paragraph*")
          || command.equals("\\subparagraph*")) {
      addMarkup(command);
      String headingArgument = LatexCommandSignature.matchArgumentFromPosition(
          this.code, this.pos, LatexCommandSignature.ArgumentType.BRACKET);
      if (!headingArgument.isEmpty()) addMarkup(headingArgument);
      this.modeStack.push(Mode.HEADING);
      addMarkup("{");
    } else if (command.equals("\\text") || command.equals("\\intertext")) {
      this.modeStack.push(Mode.INLINE_TEXT);
      String interpretAs = (isMathMode(this.curMode) ? generateDummy() : "");
      addMarkup(command + "{", interpretAs);
    } else if (command.equals("\\verb")) {
      String verbCommand = matchFromPosition(verbCommandPattern);
      addMarkup(verbCommand, generateDummy());
    } else {
      @Nullable List<LatexCommandSignature> possibleCommandSignatures =
          this.commandSignatureMap.get(command);

      if (possibleCommandSignatures == null) {
        possibleCommandSignatures = Collections.emptyList();
      }

      String match = "";
      @Nullable LatexCommandSignature matchingCommandSignature = null;

      for (LatexCommandSignature latexCommandSignature : possibleCommandSignatures) {
        String curMatch = latexCommandSignature.matchFromPosition(this.code, this.pos);

        if (!curMatch.isEmpty() && (curMatch.length() >= match.length())) {
          match = curMatch;
          matchingCommandSignature = latexCommandSignature;
        }
      }

      if ((matchingCommandSignature != null)
            && (matchingCommandSignature.getAction() != LatexCommandSignature.Action.DEFAULT)) {
        switch (matchingCommandSignature.getAction()) {
          case IGNORE: {
            addMarkup(match);
            break;
          }
          case DUMMY: {
            addMarkup(match, generateDummy(matchingCommandSignature.getDummyGenerator()));
            break;
          }
          default: {
            addMarkup(match);
            break;
          }
        }
      } else {
        if (isMathMode(this.curMode) && (this.mathVowelState == MathVowelState.UNDECIDED)) {
          if (command.equals("\\mathbb") || command.equals("\\mathbf")
                || command.equals("\\mathcal") || command.equals("\\mathfrak")
                || command.equals("\\mathit") || command.equals("\\mathnormal")
                || command.equals("\\mathsf") || command.equals("\\mathtt")) {
            // leave this.mathVowelState as MathVowelState.UNDECIDED
          } else if (command.equals("\\ell")) {
            this.mathVowelState = MathVowelState.STARTS_WITH_VOWEL;
          } else {
            this.mathVowelState = MathVowelState.STARTS_WITH_CONSONANT;
          }
        }

        addMarkup(command);
      }
    }
  }

  private void processOpeningBrace() {
    String length = matchFromPosition(lengthInBracePattern);

    if (!length.isEmpty()) {
      addMarkup(length);
    } else {
      this.modeStack.push(this.curMode);
      addMarkup(this.curString);
    }
  }

  private void processClosingBrace() {
    String interpretAs = "";

    if ((this.curMode == Mode.HEADING) && this.lastPunctuation.isEmpty()) {
      interpretAs = ".";
    } else if (isTextMode(this.curMode) && (this.pos + 1 < this.code.length())
          && (this.code.charAt(this.pos + 1) == '{')) {
      interpretAs = " ";
    }

    popMode();
    addMarkup(this.curString, interpretAs);
    this.canInsertSpaceBeforeDummy = true;

    if (isTextMode(this.curMode) && isMathMode(this.modeStack.peek())) {
      this.isMathEmpty = true;
    }

    this.isMathCharTrivial = true;
  }

  private void processDollar() {
    String displayMath = matchFromPosition(displayMathPattern);

    if (!displayMath.isEmpty()) {
      if (this.curMode == Mode.DISPLAY_MATH) {
        popMode();
        addMarkup(displayMath, generateDummy());
      } else {
        enterDisplayMath();
        addMarkup(displayMath);
      }
    } else {
      if (this.curMode == Mode.INLINE_MATH) {
        popMode();
        addMarkup(this.curString, generateDummy());
      } else {
        enterInlineMath();
        addMarkup(this.curString);
      }
    }
  }

  private void processPercentage() {
    String comment = matchFromPosition(commentPattern);
    this.preserveDummyLast = true;
    this.isMathCharTrivial = true;
    addMarkup(comment, (containsTwoEndsOfLine(comment) ? "\n\n" : ""));
  }

  private void processWhitespace() {
    String whitespace = (((this.curChar != '~') && (this.curChar != '&'))
        ? matchFromPosition(whitespacePattern) : this.curString);
    this.preserveDummyLast = true;
    this.isMathCharTrivial = true;

    if (isTextMode(this.curMode)) {
      if (containsTwoEndsOfLine(whitespace)) {
        addMarkup(whitespace, "\n\n");
      } else if (this.curChar == '~') {
        addMarkup(whitespace, (this.lastSpace.isEmpty() ? "\u00a0" : ""));
      } else {
        addMarkup(whitespace, (this.lastSpace.isEmpty() ? " " : ""));
      }
    } else {
      addMarkup(whitespace);
    }

    if ((this.curChar == '~') || (this.curChar == '&')) {
      this.dummyLastSpace = " ";
    }
  }

  private void processQuotationMark() {
    if (isTextMode(this.curMode)) {
      String quote = "";
      String smartQuote = "";

      if (this.pos + 1 < this.code.length()) {
        quote = this.code.substring(this.pos, this.pos + 2);

        if (quote.equals("``") || quote.equals("\"'")) {
          smartQuote = "\u201c";
        } else if (quote.equals("''")) {
          smartQuote = "\u201d";
        } else if (quote.equals("\"`")) {
          smartQuote = "\u201e";
        } else if (quote.equals("\"-") || quote.equals("\"\"") || quote.equals("\"|")) {
          smartQuote = "";
        } else if (quote.equals("\"=") || quote.equals("\"~")) {
          smartQuote = "-";
        } else {
          quote = "";
        }
      }

      if (quote.isEmpty()) addText(this.curString);
      else addMarkup(quote, smartQuote);
    } else {
      addMarkup(this.curString);
    }
  }

  private void processDefaultCharacter() {
    if (this.curChar == '-') {
      String emDash = matchFromPosition(emDashPattern);

      if (isTextMode(this.curMode)) {
        if (!emDash.isEmpty()) {
          addMarkup(emDash, "\u2014");
          return;
        } else {
          String enDash = matchFromPosition(enDashPattern);

          if (!enDash.isEmpty()) {
            addMarkup(enDash, "\u2013");
            return;
          }
        }
      }
    } else if (this.curChar == '[') {
      String length = matchFromPosition(lengthInBracketPattern);

      if (!length.isEmpty()) {
        this.isMathCharTrivial = true;
        this.preserveDummyLast = true;
        addMarkup(length);
        return;
      }
    } else if (this.curChar == '<') {
      if (this.codeLanguageId.equals("rsweave")) {
        String rsweaveBegin = matchFromPosition(rsweaveBeginPattern);

        if (!rsweaveBegin.isEmpty()) {
          this.modeStack.push(Mode.RSWEAVE);
          addMarkup(rsweaveBegin);
          return;
        }
      }
    }

    if (isTextMode(this.curMode)) {
      addText(this.curString);
      if (isPunctuation(this.curChar)) this.lastPunctuation = this.curString;
    } else {
      addMarkup(this.curString);
      if (isPunctuation(this.curChar)) this.dummyLastPunctuation = this.curString;

      if (this.mathVowelState == MathVowelState.UNDECIDED) {
        this.mathVowelState = (isVowel(this.curChar) ? MathVowelState.STARTS_WITH_VOWEL
            : MathVowelState.STARTS_WITH_CONSONANT);
      }
    }
  }

  private String matchFromPosition(Pattern pattern) {
    return matchFromPosition(pattern, this.pos);
  }

  private String matchFromPosition(Pattern pattern, int pos) {
    Matcher matcher = pattern.matcher(this.code.substring(pos));
    return (matcher.find() ? matcher.group() : "");
  }

  private String generateDummy() {
    return generateDummy(DummyGenerator.getDefault());
  }

  private String generateDummy(DummyGenerator dummyGenerator) {
    boolean startsWithVowel = (this.mathVowelState == MathVowelState.STARTS_WITH_VOWEL);
    String dummy;

    if (isTextMode(this.curMode)) {
      dummy = dummyGenerator.generate(this.language, this.dummyCounter++, startsWithVowel);
    } else if (this.isMathEmpty) {
      if (this.curMode == Mode.DISPLAY_MATH) {
        dummy = (this.lastSpace.isEmpty() ? " " : "");
      } else {
        dummy = "";
      }
    } else if (this.curMode == Mode.DISPLAY_MATH) {
      dummy = ((this.lastSpace.isEmpty() ? " " : ""))
          + dummyGenerator.generate(this.language, this.dummyCounter++)
          + this.dummyLastPunctuation + ((this.modeStack.peek() == Mode.INLINE_TEXT)
          ? this.dummyLastSpace : " ");
    } else {
      dummy = dummyGenerator.generate(this.language, this.dummyCounter++, startsWithVowel)
          + this.dummyLastPunctuation + this.dummyLastSpace;
    }

    this.dummyLastSpace = "";
    this.dummyLastPunctuation = "";
    this.mathVowelState = MathVowelState.UNDECIDED;
    return dummy;
  }

  private void textAdded(String text) {
    if (text.isEmpty()) return;
    char lastChar = text.charAt(text.length() - 1);
    this.lastSpace = (((lastChar == ' ') || (lastChar == '\n') || (lastChar == '\r')) ? " " : "");
    this.lastPunctuation = (isPunctuation(lastChar) ? " " : "");
  }

  private void popMode() {
    this.modeStack.pop();
    if (this.modeStack.isEmpty()) this.modeStack.push(Mode.PARAGRAPH_TEXT);
  }

  private void enterDisplayMath() {
    this.modeStack.push(Mode.DISPLAY_MATH);
    this.isMathEmpty = true;
    this.mathVowelState = MathVowelState.UNDECIDED;
    this.canInsertSpaceBeforeDummy = true;
  }

  private void enterInlineMath() {
    this.modeStack.push(Mode.INLINE_MATH);
    this.isMathEmpty = true;
    this.mathVowelState = MathVowelState.UNDECIDED;
    this.canInsertSpaceBeforeDummy = true;
    this.isMathCharTrivial = true;
  }

  @ExcludeFromGeneratedCoverage
  private void onInfiniteLoop() {
    if (this.isInStrictMode) {
      throw new RuntimeException(Tools.i18n(
          "latexAnnotatedTextBuilderInfiniteLoop", getDebugInformation()));
    } else {
      Tools.logger.warning(Tools.i18n(
          "latexAnnotatedTextBuilderPreventedInfiniteLoop", getDebugInformation()));
      this.pos++;
    }
  }

  @ExcludeFromGeneratedCoverage
  private String getDebugInformation() {
    String remainingCode = StringEscapeUtils.escapeJava(
        this.code.substring(this.pos, Math.min(this.pos + 100, this.code.length())));
    return "Remaining code = \"" + remainingCode
        + "\", pos = " + this.pos
        + ", dummyCounter = " + this.dummyCounter
        + ", lastSpace = \"" + this.lastSpace
        + "\", lastPunctuation = \"" + this.lastPunctuation
        + "\", dummyLastSpace = \"" + this.dummyLastSpace
        + "\", dummyLastPunctuation = \"" + this.dummyLastPunctuation
        + "\", isMathEmpty = " + this.isMathEmpty
        + ", mathVowelState = " + this.mathVowelState
        + ", preserveDummyLast = " + this.preserveDummyLast
        + ", canInsertSpaceBeforeDummy = " + this.canInsertSpaceBeforeDummy
        + ", isMathCharTrivial = " + this.isMathCharTrivial
        + ", modeStack = " + this.modeStack
        + ", curChar = \"" + this.curChar
        + "\", curString = \"" + this.curString
        + "\", curMode = " + this.curMode;
  }

  private String convertAccentCommandToUnicode(String accentCommand, String letter) {
    String unicode = "";

    switch (accentCommand.charAt(1)) {
      // grave
      case '`': {
        if (letter.equals("A")) unicode = "\u00c0";
        else if (letter.equals("E")) unicode = "\u00c8";
        else if (letter.equals("I")) unicode = "\u00cc";
        else if (letter.equals("O")) unicode = "\u00d2";
        else if (letter.equals("U")) unicode = "\u00d9";
        else if (letter.equals("a")) unicode = "\u00e0";
        else if (letter.equals("e")) unicode = "\u00e8";
        else if (letter.equals("i") || letter.equals("\\i")) unicode = "\u00ec";
        else if (letter.equals("o")) unicode = "\u00f2";
        else if (letter.equals("u")) unicode = "\u00f9";
        break;
      }
      // acute
      case '\'': {
        if (letter.equals("A")) unicode = "\u00c1";
        else if (letter.equals("E")) unicode = "\u00c9";
        else if (letter.equals("I")) unicode = "\u00cd";
        else if (letter.equals("O")) unicode = "\u00d3";
        else if (letter.equals("U")) unicode = "\u00da";
        else if (letter.equals("Y")) unicode = "\u00dd";
        else if (letter.equals("a")) unicode = "\u00e1";
        else if (letter.equals("e")) unicode = "\u00e9";
        else if (letter.equals("i") || letter.equals("\\i")) unicode = "\u00ed";
        else if (letter.equals("o")) unicode = "\u00f3";
        else if (letter.equals("u")) unicode = "\u00fa";
        else if (letter.equals("y")) unicode = "\u00fd";
        break;
      }
      // circumflex
      case '^': {
        if (letter.equals("A")) unicode = "\u00c2";
        else if (letter.equals("E")) unicode = "\u00ca";
        else if (letter.equals("I")) unicode = "\u00ce";
        else if (letter.equals("O")) unicode = "\u00d4";
        else if (letter.equals("U")) unicode = "\u00db";
        else if (letter.equals("Y")) unicode = "\u0176";
        else if (letter.equals("a")) unicode = "\u00e2";
        else if (letter.equals("e")) unicode = "\u00ea";
        else if (letter.equals("i") || letter.equals("\\i")) unicode = "\u00ee";
        else if (letter.equals("j") || letter.equals("\\j")) unicode = "\u0135";
        else if (letter.equals("o")) unicode = "\u00f4";
        else if (letter.equals("u")) unicode = "\u00fb";
        else if (letter.equals("y")) unicode = "\u0177";
        break;
      }
      // tilde
      case '~': {
        if (letter.equals("A")) unicode = "\u00c3";
        else if (letter.equals("E")) unicode = "\u1ebc";
        else if (letter.equals("I")) unicode = "\u0128";
        else if (letter.equals("N")) unicode = "\u00d1";
        else if (letter.equals("O")) unicode = "\u00d5";
        else if (letter.equals("U")) unicode = "\u0168";
        else if (letter.equals("a")) unicode = "\u00e3";
        else if (letter.equals("e")) unicode = "\u1ebd";
        else if (letter.equals("i") || letter.equals("\\i")) unicode = "\u0129";
        else if (letter.equals("n")) unicode = "\u00f1";
        else if (letter.equals("o")) unicode = "\u00f5";
        else if (letter.equals("u")) unicode = "\u0169";
        break;
      }
      // diaeresis/umlaut
      case '"': {
        if (letter.equals("A")) unicode = "\u00c4";
        else if (letter.equals("E")) unicode = "\u00cb";
        else if (letter.equals("I")) unicode = "\u00cf";
        else if (letter.equals("O")) unicode = "\u00d6";
        else if (letter.equals("U")) unicode = "\u00dc";
        else if (letter.equals("Y")) unicode = "\u0178";
        else if (letter.equals("a")) unicode = "\u00e4";
        else if (letter.equals("e")) unicode = "\u00eb";
        else if (letter.equals("i") || letter.equals("\\i")) unicode = "\u00ef";
        else if (letter.equals("o")) unicode = "\u00f6";
        else if (letter.equals("u")) unicode = "\u00fc";
        else if (letter.equals("y")) unicode = "\u00ff";
        break;
      }
      // macron
      case '=': {
        if (letter.equals("A")) unicode = "\u0100";
        else if (letter.equals("E")) unicode = "\u0112";
        else if (letter.equals("I")) unicode = "\u012a";
        else if (letter.equals("O")) unicode = "\u014c";
        else if (letter.equals("U")) unicode = "\u016a";
        else if (letter.equals("Y")) unicode = "\u0232";
        else if (letter.equals("a")) unicode = "\u0101";
        else if (letter.equals("e")) unicode = "\u0113";
        else if (letter.equals("i") || letter.equals("\\i")) unicode = "\u012b";
        else if (letter.equals("o")) unicode = "\u014d";
        else if (letter.equals("u")) unicode = "\u016b";
        else if (letter.equals("y")) unicode = "\u0233";
        break;
      }
      // dot above
      case '.': {
        if (letter.equals("A")) unicode = "\u0226";
        else if (letter.equals("E")) unicode = "\u0116";
        else if (letter.equals("I")) unicode = "\u0130";
        else if (letter.equals("O")) unicode = "\u022e";
        else if (letter.equals("a")) unicode = "\u0227";
        else if (letter.equals("e")) unicode = "\u0117";
        else if (letter.equals("o")) unicode = "\u022f";
        break;
      }
      // double acute
      case 'H': {
        if (letter.equals("O")) unicode = "\u0150";
        else if (letter.equals("U")) unicode = "\u0170";
        else if (letter.equals("o")) unicode = "\u0151";
        else if (letter.equals("u")) unicode = "\u0171";
        break;
      }
      // line below
      case 'b': {
        if (letter.equals("B")) unicode = "\u1e06";
        else if (letter.equals("D")) unicode = "\u1e0e";
        else if (letter.equals("K")) unicode = "\u1e34";
        else if (letter.equals("L")) unicode = "\u1e3a";
        else if (letter.equals("N")) unicode = "\u1e48";
        else if (letter.equals("R")) unicode = "\u1e5e";
        else if (letter.equals("T")) unicode = "\u1e6e";
        else if (letter.equals("Z")) unicode = "\u1e94";
        else if (letter.equals("b")) unicode = "\u1e07";
        else if (letter.equals("d")) unicode = "\u1e0f";
        else if (letter.equals("h")) unicode = "\u1e96";
        else if (letter.equals("k")) unicode = "\u1e35";
        else if (letter.equals("l")) unicode = "\u1e3b";
        else if (letter.equals("n")) unicode = "\u1e49";
        else if (letter.equals("r")) unicode = "\u1e5f";
        else if (letter.equals("t")) unicode = "\u1e6f";
        else if (letter.equals("z")) unicode = "\u1e95";
        break;
      }
      // cedilla
      case 'c': {
        if (letter.equals("C")) unicode = "\u00c7";
        else if (letter.equals("D")) unicode = "\u1e10";
        else if (letter.equals("E")) unicode = "\u0228";
        else if (letter.equals("G")) unicode = "\u0122";
        else if (letter.equals("H")) unicode = "\u1e28";
        else if (letter.equals("K")) unicode = "\u0136";
        else if (letter.equals("L")) unicode = "\u013b";
        else if (letter.equals("N")) unicode = "\u0145";
        else if (letter.equals("R")) unicode = "\u0156";
        else if (letter.equals("S")) unicode = "\u015e";
        else if (letter.equals("T")) unicode = "\u0162";
        else if (letter.equals("c")) unicode = "\u00e7";
        else if (letter.equals("d")) unicode = "\u1e11";
        else if (letter.equals("e")) unicode = "\u0229";
        else if (letter.equals("g")) unicode = "\u0123";
        else if (letter.equals("h")) unicode = "\u1e29";
        else if (letter.equals("k")) unicode = "\u0137";
        else if (letter.equals("l")) unicode = "\u013c";
        else if (letter.equals("n")) unicode = "\u0146";
        else if (letter.equals("r")) unicode = "\u0157";
        else if (letter.equals("s")) unicode = "\u015f";
        else if (letter.equals("t")) unicode = "\u0163";
        break;
      }
      // dot below
      case 'd': {
        if (letter.equals("A")) unicode = "\u1ea0";
        else if (letter.equals("E")) unicode = "\u1eb8";
        else if (letter.equals("I")) unicode = "\u1eca";
        else if (letter.equals("O")) unicode = "\u1ecc";
        else if (letter.equals("U")) unicode = "\u1ee4";
        else if (letter.equals("Y")) unicode = "\u1ef4";
        else if (letter.equals("a")) unicode = "\u1ea1";
        else if (letter.equals("e")) unicode = "\u1eb9";
        else if (letter.equals("i")) unicode = "\u1ecb";
        else if (letter.equals("o")) unicode = "\u1ecd";
        else if (letter.equals("u")) unicode = "\u1ee5";
        else if (letter.equals("y")) unicode = "\u1ef5";
        break;
      }
      // ogonek
      case 'k': {
        if (letter.equals("A")) unicode = "\u0104";
        else if (letter.equals("E")) unicode = "\u0118";
        else if (letter.equals("I")) unicode = "\u012e";
        else if (letter.equals("O")) unicode = "\u01ea";
        else if (letter.equals("U")) unicode = "\u0172";
        else if (letter.equals("a")) unicode = "\u0105";
        else if (letter.equals("e")) unicode = "\u0119";
        else if (letter.equals("i")) unicode = "\u012f";
        else if (letter.equals("o")) unicode = "\u01eb";
        else if (letter.equals("u")) unicode = "\u0173";
        break;
      }
      // ring above
      case 'r': {
        if (letter.equals("A")) unicode = "\u00c5";
        else if (letter.equals("U")) unicode = "\u016e";
        else if (letter.equals("a")) unicode = "\u00e5";
        else if (letter.equals("u")) unicode = "\u016f";
        break;
      }
      // breve
      case 'u': {
        if (letter.equals("A")) unicode = "\u0102";
        else if (letter.equals("E")) unicode = "\u0114";
        else if (letter.equals("G")) unicode = "\u011e";
        else if (letter.equals("I")) unicode = "\u012c";
        else if (letter.equals("O")) unicode = "\u014e";
        else if (letter.equals("U")) unicode = "\u016c";
        else if (letter.equals("a")) unicode = "\u0103";
        else if (letter.equals("e")) unicode = "\u0115";
        else if (letter.equals("g")) unicode = "\u011f";
        else if (letter.equals("i") || letter.equals("\\i")) unicode = "\u012d";
        else if (letter.equals("o")) unicode = "\u014f";
        else if (letter.equals("u")) unicode = "\u016d";
        break;
      }
      // caron
      case 'v': {
        if (letter.equals("C")) unicode = "\u010c";
        else if (letter.equals("D")) unicode = "\u010e";
        else if (letter.equals("E")) unicode = "\u011a";
        else if (letter.equals("L")) unicode = "\u013d";
        else if (letter.equals("N")) unicode = "\u0147";
        else if (letter.equals("R")) unicode = "\u0158";
        else if (letter.equals("S")) unicode = "\u0160";
        else if (letter.equals("T")) unicode = "\u0164";
        else if (letter.equals("Z")) unicode = "\u017d";
        else if (letter.equals("c")) unicode = "\u010d";
        else if (letter.equals("d")) unicode = "\u010f";
        else if (letter.equals("e")) unicode = "\u011b";
        else if (letter.equals("i") || letter.equals("\\i")) unicode = "\u01d0";
        else if (letter.equals("j") || letter.equals("\\j")) unicode = "\u01f0";
        else if (letter.equals("l")) unicode = "\u013e";
        else if (letter.equals("n")) unicode = "\u0148";
        else if (letter.equals("r")) unicode = "\u0159";
        else if (letter.equals("s")) unicode = "\u0161";
        else if (letter.equals("t")) unicode = "\u0165";
        else if (letter.equals("z")) unicode = "\u017e";
        break;
      }
      default: {
        break;
      }
    }

    return unicode;
  }

  private void processEnvironmentArguments() {
    while (this.pos < this.code.length()) {
      String environmentArgument = LatexCommandSignature.matchArgumentFromPosition(
          this.code, this.pos, LatexCommandSignature.ArgumentType.BRACE);

      if (!environmentArgument.isEmpty()) {
        addMarkup(environmentArgument);
        continue;
      }

      environmentArgument = LatexCommandSignature.matchArgumentFromPosition(
          this.code, this.pos, LatexCommandSignature.ArgumentType.BRACKET);

      if (!environmentArgument.isEmpty()) {
        addMarkup(environmentArgument);
        continue;
      }

      environmentArgument = LatexCommandSignature.matchArgumentFromPosition(
          this.code, this.pos, LatexCommandSignature.ArgumentType.PARENTHESIS);

      if (!environmentArgument.isEmpty()) {
        addMarkup(environmentArgument);
        continue;
      }

      break;
    }
  }
}
