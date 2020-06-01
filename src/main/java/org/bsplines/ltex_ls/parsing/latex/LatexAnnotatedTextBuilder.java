package org.bsplines.ltex_ls.parsing.latex;

import java.util.*;
import java.util.regex.*;

import org.apache.commons.text.StringEscapeUtils;

import org.bsplines.ltex_ls.Settings;
import org.bsplines.ltex_ls.Tools;
import org.bsplines.ltex_ls.parsing.CodeAnnotatedTextBuilder;
import org.bsplines.ltex_ls.parsing.DummyGenerator;

public class LatexAnnotatedTextBuilder extends CodeAnnotatedTextBuilder {
  private enum Mode {
    PARAGRAPH_TEXT,
    INLINE_TEXT,
    HEADING,
    INLINE_MATH,
    DISPLAY_MATH,
    IGNORE_ENVIRONMENT,
    RSWEAVE,
  }

  private static final String[] defaultIgnoreEnvironments = {
    "lstlisting", "tikzpicture", "verbatim",
  };

  private static final Pattern commandPattern = Pattern.compile(
      "^\\\\(([^A-Za-z@]|([A-Za-z@]+))\\*?)");
  private static final Pattern argumentPattern = Pattern.compile("^\\{[^\\}]*?\\}");
  private static final Pattern commentPattern = Pattern.compile(
      "^%.*?($|((\n|\r|\r\n)[ \n\r\t]*))");
  private static final Pattern whiteSpacePattern = Pattern.compile(
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
      "^(\\\\[`'\\^~\"=\\.])(([A-Za-z]|\\\\i)|(\\{([A-Za-z]|\\\\i)\\}))");
  private static final Pattern accentPattern2 = Pattern.compile(
      "^(\\\\[cr])( *([A-Za-z])|\\{([A-Za-z])\\})");
  private static final Pattern displayMathPattern = Pattern.compile("^\\$\\$");
  private static final Pattern verbCommandPattern = Pattern.compile("^\\\\verb\\*?(.).*?\\1");
  private static final Pattern rsweaveBeginPattern = Pattern.compile("^<<.*?>>=");
  private static final Pattern rsweaveEndPattern = Pattern.compile("^@");

  private static final String[] mathEnvironments = {"align", "align*", "alignat", "alignat*",
      "displaymath", "eqnarray", "eqnarray*", "equation", "equation*", "flalign", "flalign*",
      "gather", "gather*", "math", "multline", "multline*"};

  private String code;
  private int pos;
  private int dummyCounter;
  private String lastSpace;
  private String lastPunctuation;
  private String dummyLastSpace;
  private String dummyLastPunctuation;
  private boolean isMathEmpty;
  private boolean preserveDummyLast;
  private boolean canInsertSpaceBeforeDummy;
  private boolean isMathCharTrivial;
  private Stack<Mode> modeStack;

  private char curChar;
  private String curString;
  private Mode curMode;

  public String language = "en-US";
  public String codeLanguageId = "latex";
  public List<LatexCommandSignature> commandSignatures =
      new ArrayList<>(Arrays.asList(DefaultLatexCommandSignatures.get()));
  public List<String> ignoreEnvironments =
      new ArrayList<>(Arrays.asList(defaultIgnoreEnvironments));
  public boolean isInStrictMode = false;

  public LatexAnnotatedTextBuilder(String codeLanguageId) {
    this.codeLanguageId = codeLanguageId;
  }

  private Map<String, List<LatexCommandSignature>> createCommandSignatureMap() {
    Map<String, List<LatexCommandSignature>> map = new HashMap<>();

    for (LatexCommandSignature commandSignature : commandSignatures) {
      String commandName = commandSignature.name;
      if (!map.containsKey(commandName)) map.put(commandName, new ArrayList<>());
      map.get(commandName).add(commandSignature);
    }

    return map;
  }

  private String matchFromPosition(Pattern pattern) {
    return matchFromPosition(pattern, pos);
  }

  private String matchFromPosition(Pattern pattern, int pos) {
    Matcher matcher = pattern.matcher(code.substring(pos));
    return (matcher.find() ? matcher.group() : "");
  }

  private String generateDummy() {
    return generateDummy(DummyGenerator.getDefault());
  }

  private String generateDummy(DummyGenerator dummyGenerator) {
    String dummy;

    if (isTextMode(curMode)) {
      dummy = dummyGenerator.generate(language, dummyCounter++);
    } else if (isMathEmpty) {
      if (curMode == Mode.DISPLAY_MATH) {
        dummy = (lastSpace.isEmpty() ? " " : "");
      } else {
        dummy = "";
      }
    } else if (curMode == Mode.DISPLAY_MATH) {
      dummy = ((lastSpace.isEmpty() ? " " : "")) +
          dummyGenerator.generate(language, dummyCounter++) +
          dummyLastPunctuation + ((modeStack.peek() == Mode.INLINE_TEXT) ? dummyLastSpace : " ");
    } else {
      dummy = dummyGenerator.generate(language, dummyCounter++) + dummyLastPunctuation +
          dummyLastSpace;
    }

    dummyLastSpace = "";
    dummyLastPunctuation = "";
    return dummy;
  }

  public LatexAnnotatedTextBuilder addText(String text) {
    if (text.isEmpty()) return this;
    super.addText(text);
    pos += text.length();
    textAdded(text);
    return this;
  }

  public LatexAnnotatedTextBuilder addMarkup(String markup) {
    if (markup.isEmpty()) return this;
    super.addMarkup(markup);
    pos += markup.length();

    if (preserveDummyLast) {
      preserveDummyLast = false;
    } else {
      dummyLastSpace = "";
      dummyLastPunctuation = "";
    }

    return this;
  }

  public LatexAnnotatedTextBuilder addMarkup(String markup, String interpretAs) {
    if (interpretAs.isEmpty()) {
      return addMarkup(markup);
    } else {
      super.addMarkup(markup, interpretAs);
      pos += markup.length();
      preserveDummyLast = false;
      textAdded(interpretAs);
      return this;
    }
  }

  private void textAdded(String text) {
    if (text.isEmpty()) return;
    char lastChar = text.charAt(text.length() - 1);
    lastSpace = (((lastChar == ' ') || (lastChar == '\n') || (lastChar == '\r')) ? " " : "");
    lastPunctuation = (isPunctuation(lastChar) ? " " : "");
  }

  private void popMode() {
    modeStack.pop();
    if (modeStack.isEmpty()) modeStack.push(Mode.PARAGRAPH_TEXT);
  }

  private static boolean isPunctuation(char ch) {
    return ((ch == '.') || (ch == ',') || (ch == ':') || (ch == ';'));
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

  private void enterDisplayMath() {
    modeStack.push(Mode.DISPLAY_MATH);
    isMathEmpty = true;
    canInsertSpaceBeforeDummy = true;
  }

  private void enterInlineMath() {
    modeStack.push(Mode.INLINE_MATH);
    isMathEmpty = true;
    canInsertSpaceBeforeDummy = true;
    isMathCharTrivial = true;
  }

  private String getDebugInformation(String code) {
    String remainingCode = StringEscapeUtils.escapeJava(
        code.substring(pos, Math.min(pos + 100, code.length())));
    return "Remaining code = \"" + remainingCode +
        "\", pos = " + pos + ", dummyCounter = " + dummyCounter + ", lastSpace = \"" + lastSpace +
        "\", lastPunctuation = \"" + lastPunctuation + "\", dummyLastSpace = \"" + dummyLastSpace +
        "\", dummyLastPunctuation = \"" + dummyLastPunctuation +
        "\", isMathEmpty = " + isMathEmpty + ", preserveDummyLast = " + preserveDummyLast +
        ", canInsertSpaceBeforeDummy = " + canInsertSpaceBeforeDummy +
        ", isMathCharTrivial = " + isMathCharTrivial + ", modeStack = " + modeStack +
        ", curChar = \"" + curChar + "\", curString = \"" + curString + "\", curMode = " + curMode;
  }

  private static boolean containsTwoEndsOfLine(String text) {
    return (text.contains("\n\n") || text.contains("\r\r") || text.contains("\r\n\r\n"));
  }

  public LatexAnnotatedTextBuilder addCode(String code) {
    this.code = code;
    pos = 0;
    dummyCounter = 0;
    lastSpace = "";
    lastPunctuation = "";
    dummyLastSpace = "";
    dummyLastPunctuation = "";
    isMathEmpty = true;
    preserveDummyLast = false;
    canInsertSpaceBeforeDummy = false;
    isMathCharTrivial = false;

    modeStack = new Stack<>();
    modeStack.push(Mode.PARAGRAPH_TEXT);

    Map<String, List<LatexCommandSignature>> commandSignatureMap = createCommandSignatureMap();
    Pattern ignoreEnvironmentEndPattern = null;
    int lastPos = -1;

    while (pos < code.length()) {
      curChar = code.charAt(pos);
      curString = String.valueOf(curChar);
      curMode = modeStack.peek();
      isMathCharTrivial = false;
      lastPos = pos;

      if (isIgnoreEnvironmentMode(curMode)) {
        String ignoreEnvironmentEnd = matchFromPosition(ignoreEnvironmentEndPattern);

        if (ignoreEnvironmentEnd.isEmpty()) {
          addMarkup(curString);
        } else {
          popMode();
          addMarkup(ignoreEnvironmentEnd);
        }
      } else if (codeLanguageId.equals("rsweave") && isRsweaveMode(curMode)) {
        String rsweaveEnd = matchFromPosition(rsweaveEndPattern);

        if (rsweaveEnd.isEmpty()) {
          addMarkup(curString);
        } else {
          popMode();
          addMarkup(rsweaveEnd);
        }
      } else {
        switch (curChar) {
          case '\\': {
            String command = matchFromPosition(commandPattern);

            if (command.equals("\\begin") || command.equals("\\end")) {
              preserveDummyLast = true;
              addMarkup(command);

              String argument = matchFromPosition(argumentPattern);
              String environmentName = argument.substring(1, argument.length() - 1);
              String interpretAs = "";

              if (Arrays.asList(mathEnvironments).contains(environmentName)) {
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
              } else if (command.equals("\\begin") &&
                  ignoreEnvironments.contains(environmentName)) {
                modeStack.push(Mode.IGNORE_ENVIRONMENT);
                ignoreEnvironmentEndPattern = Pattern.compile(
                    "^\\\\end\\{" + Pattern.quote(environmentName) + "\\}");
              } else {
                if (command.equals("\\begin")) modeStack.push(curMode);
                else popMode();
              }

              if (!isIgnoreEnvironmentMode(modeStack.peek())) {
                isMathCharTrivial = true;
                preserveDummyLast = true;
                addMarkup(argument, interpretAs);

                if (command.equals("\\begin")) {
                  while (pos < code.length()) {
                    String environmentArgument = LatexCommandSignature.matchArgumentFromPosition(
                        code, pos, LatexCommandSignature.ArgumentType.BRACE);

                    if (!environmentArgument.isEmpty()) {
                      addMarkup(environmentArgument);
                    } else {
                      environmentArgument = LatexCommandSignature.matchArgumentFromPosition(
                          code, pos, LatexCommandSignature.ArgumentType.BRACKET);

                      if (!environmentArgument.isEmpty()) {
                        addMarkup(environmentArgument);
                      } else {
                        break;
                      }
                    }
                  }
                }
              }
            } else if (command.equals("\\$") || command.equals("\\%") || command.equals("\\&")) {
              addMarkup(command, command.substring(1));
            } else if (command.equals("\\[") || command.equals("\\]") ||
                command.equals("\\(") || command.equals("\\)")) {
              if (command.equals("\\[")) {
                enterDisplayMath();
                addMarkup(command);
              } else if (command.equals("\\(")) {
                enterInlineMath();
                addMarkup(command);
              } else {
                popMode();
                addMarkup(command, generateDummy());
              }
            } else if (command.equals("\\AA")) {
              addMarkup(command, "\u00c5");
            } else if (command.equals("\\O")) {
              addMarkup(command, "\u00d8");
            } else if (command.equals("\\aa")) {
              addMarkup(command, "\u00e5");
            } else if (command.equals("\\ss")) {
              addMarkup(command, "\u00df");
            } else if (command.equals("\\o")) {
              addMarkup(command, "\u00f8");
            } else if (command.equals("\\`") || command.equals("\\'") || command.equals("\\^") ||
                command.equals("\\~") || command.equals("\\\"") || command.equals("\\=") ||
                command.equals("\\.")) {
              Matcher matcher = accentPattern1.matcher(code.substring(pos));

              if (matcher.find()) {
                String accentCommand = matcher.group(1);
                String letter = ((matcher.group(3) != null) ? matcher.group(3) : matcher.group(5));
                String interpretAs = "";

                switch (accentCommand.charAt(1)) {
                  case '`': {
                    if (letter.equals("A")) interpretAs = "\u00c0";
                    else if (letter.equals("E")) interpretAs = "\u00c8";
                    else if (letter.equals("I")) interpretAs = "\u00cc";
                    else if (letter.equals("O")) interpretAs = "\u00d2";
                    else if (letter.equals("U")) interpretAs = "\u00d9";
                    else if (letter.equals("a")) interpretAs = "\u00e0";
                    else if (letter.equals("e")) interpretAs = "\u00e8";
                    else if (letter.equals("i") || letter.equals("\\i")) interpretAs = "\u00ec";
                    else if (letter.equals("o")) interpretAs = "\u00f2";
                    else if (letter.equals("u")) interpretAs = "\u00f9";
                    break;
                  }
                  case '\'': {
                    if (letter.equals("A")) interpretAs = "\u00c1";
                    else if (letter.equals("E")) interpretAs = "\u00c9";
                    else if (letter.equals("I")) interpretAs = "\u00cd";
                    else if (letter.equals("O")) interpretAs = "\u00d3";
                    else if (letter.equals("U")) interpretAs = "\u00da";
                    else if (letter.equals("Y")) interpretAs = "\u00dd";
                    else if (letter.equals("a")) interpretAs = "\u00e1";
                    else if (letter.equals("e")) interpretAs = "\u00e9";
                    else if (letter.equals("i") || letter.equals("\\i")) interpretAs = "\u00ed";
                    else if (letter.equals("o")) interpretAs = "\u00f3";
                    else if (letter.equals("u")) interpretAs = "\u00fa";
                    else if (letter.equals("y")) interpretAs = "\u00fd";
                    break;
                  }
                  case '^': {
                    if (letter.equals("A")) interpretAs = "\u00c2";
                    else if (letter.equals("E")) interpretAs = "\u00ca";
                    else if (letter.equals("I")) interpretAs = "\u00ce";
                    else if (letter.equals("O")) interpretAs = "\u00d4";
                    else if (letter.equals("U")) interpretAs = "\u00db";
                    else if (letter.equals("Y")) interpretAs = "\u0176";
                    else if (letter.equals("a")) interpretAs = "\u00e2";
                    else if (letter.equals("e")) interpretAs = "\u00ea";
                    else if (letter.equals("i") || letter.equals("\\i")) interpretAs = "\u00ee";
                    else if (letter.equals("o")) interpretAs = "\u00f4";
                    else if (letter.equals("u")) interpretAs = "\u00fb";
                    else if (letter.equals("y")) interpretAs = "\u0177";
                    break;
                  }
                  case '~': {
                    if (letter.equals("A")) interpretAs = "\u00c3";
                    else if (letter.equals("E")) interpretAs = "\u1ebc";
                    else if (letter.equals("I")) interpretAs = "\u0128";
                    else if (letter.equals("N")) interpretAs = "\u00d1";
                    else if (letter.equals("O")) interpretAs = "\u00d5";
                    else if (letter.equals("U")) interpretAs = "\u0168";
                    else if (letter.equals("a")) interpretAs = "\u00e3";
                    else if (letter.equals("e")) interpretAs = "\u1ebd";
                    else if (letter.equals("i") || letter.equals("\\i")) interpretAs = "\u0129";
                    else if (letter.equals("n")) interpretAs = "\u00f1";
                    else if (letter.equals("o")) interpretAs = "\u00f5";
                    else if (letter.equals("u")) interpretAs = "\u0169";
                    break;
                  }
                  case '"': {
                    if (letter.equals("A")) interpretAs = "\u00c4";
                    else if (letter.equals("E")) interpretAs = "\u00cb";
                    else if (letter.equals("I")) interpretAs = "\u00cf";
                    else if (letter.equals("O")) interpretAs = "\u00d6";
                    else if (letter.equals("U")) interpretAs = "\u00dc";
                    else if (letter.equals("Y")) interpretAs = "\u0178";
                    else if (letter.equals("a")) interpretAs = "\u00e4";
                    else if (letter.equals("e")) interpretAs = "\u00eb";
                    else if (letter.equals("i") || letter.equals("\\i")) interpretAs = "\u00ef";
                    else if (letter.equals("o")) interpretAs = "\u00f6";
                    else if (letter.equals("u")) interpretAs = "\u00fc";
                    else if (letter.equals("y")) interpretAs = "\u00ff";
                    break;
                  }
                  case '=': {
                    if (letter.equals("A")) interpretAs = "\u0100";
                    else if (letter.equals("E")) interpretAs = "\u0112";
                    else if (letter.equals("I")) interpretAs = "\u012a";
                    else if (letter.equals("O")) interpretAs = "\u014c";
                    else if (letter.equals("U")) interpretAs = "\u016a";
                    else if (letter.equals("Y")) interpretAs = "\u0232";
                    else if (letter.equals("a")) interpretAs = "\u0101";
                    else if (letter.equals("e")) interpretAs = "\u0113";
                    else if (letter.equals("i") || letter.equals("\\i")) interpretAs = "\u012b";
                    else if (letter.equals("o")) interpretAs = "\u014d";
                    else if (letter.equals("u")) interpretAs = "\u016b";
                    else if (letter.equals("y")) interpretAs = "\u0233";
                    break;
                  }
                  case '.': {
                    if (letter.equals("A")) interpretAs = "\u0226";
                    else if (letter.equals("E")) interpretAs = "\u0116";
                    else if (letter.equals("I")) interpretAs = "\u0130";
                    else if (letter.equals("O")) interpretAs = "\u022e";
                    else if (letter.equals("a")) interpretAs = "\u0227";
                    else if (letter.equals("e")) interpretAs = "\u0117";
                    else if (letter.equals("o")) interpretAs = "\u022f";
                    break;
                  }
                  default: {
                    break;
                  }
                }

                addMarkup(matcher.group(), interpretAs);
              } else {
                addMarkup(command);
              }
            } else if (command.equals("\\c") || command.equals("\\r")) {
              Matcher matcher = accentPattern2.matcher(code.substring(pos));

              if (matcher.find()) {
                String accentCommand = matcher.group(1);
                String letter = ((matcher.group(3) != null) ? matcher.group(3) : matcher.group(4));
                String interpretAs = "";

                switch (accentCommand.charAt(1)) {
                  case 'c': {
                    if (letter.equals("C")) interpretAs = "\u00c7";
                    else if (letter.equals("c")) interpretAs = "\u00e7";
                    break;
                  }
                  case 'r': {
                    if (letter.equals("A")) interpretAs = "\u00c5";
                    else if (letter.equals("U")) interpretAs = "\u016e";
                    else if (letter.equals("a")) interpretAs = "\u00e5";
                    else if (letter.equals("u")) interpretAs = "\u016f";
                    break;
                  }
                  default: {
                    break;
                  }
                }

                addMarkup(matcher.group(), interpretAs);
              } else {
                addMarkup(command);
              }
            } else if (command.equals("\\-")) {
              addMarkup(command);
            } else if (command.equals("\\ ") || command.equals("\\,") || command.equals("\\;") ||
                command.equals("\\\\") || command.equals("\\hfill") || command.equals("\\hspace") ||
                command.equals("\\hspace*") || command.equals("\\quad") ||
                command.equals("\\qquad") || command.equals("\\newline")) {
              if (command.equals("\\hspace") || command.equals("\\hspace*")) {
                String argument = matchFromPosition(argumentPattern, pos + command.length());
                command += argument;
              }

              if (isMathMode(curMode) && lastSpace.isEmpty() && canInsertSpaceBeforeDummy) {
                addMarkup(command, " ");
              } else {
                preserveDummyLast = true;

                if (isMathMode(curMode)) {
                  addMarkup(command);
                  dummyLastSpace = " ";
                } else {
                  String space = " ";

                  if (!lastSpace.isEmpty()) {
                    space = "";
                  } else if (command.equals("\\,")) {
                    space = "\u202f";
                  }

                  addMarkup(command, space);
                }
              }
            } else if (command.equals("\\dots") ||
                  command.equals("\\eg") || command.equals("\\egc") || command.equals("\\euro") ||
                  command.equals("\\ie") || command.equals("\\iec")) {
              String interpretAs = "";

              if (!isMathMode(curMode)) {
                if (command.equals("\\dots")) {
                  interpretAs = "...";
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
              preserveDummyLast = true;
              addMarkup(command);
            } else if (command.equals("\\part") || command.equals("\\chapter") ||
                  command.equals("\\section") || command.equals("\\subsection") ||
                  command.equals("\\subsubsection") || command.equals("\\paragraph") ||
                  command.equals("\\subparagraph") ||
                  command.equals("\\part*") || command.equals("\\chapter*") ||
                  command.equals("\\section*") || command.equals("\\subsection*") ||
                  command.equals("\\subsubsection*") || command.equals("\\paragraph*") ||
                  command.equals("\\subparagraph*")) {
              modeStack.push(Mode.HEADING);
              addMarkup(command + "{");
            } else if (command.equals("\\text") || command.equals("\\intertext")) {
              modeStack.push(Mode.INLINE_TEXT);
              String interpretAs = (isMathMode(curMode) ? generateDummy() : "");
              addMarkup(command + "{", interpretAs);
            } else if (command.equals("\\verb")) {
              String verbCommand = matchFromPosition(verbCommandPattern);
              addMarkup(verbCommand, generateDummy());
            } else {
              String match = "";
              List<LatexCommandSignature> possibleCommandSignatures =
                  commandSignatureMap.get(command);
              LatexCommandSignature matchingCommand = null;

              if (possibleCommandSignatures == null) {
                possibleCommandSignatures = Collections.emptyList();
              }

              for (LatexCommandSignature latexCommandSignature : possibleCommandSignatures) {
                String curMatch = latexCommandSignature.matchFromPosition(code, pos);

                if (curMatch.length() > match.length()) {
                  match = curMatch;
                  matchingCommand = latexCommandSignature;
                }
              }

              if (matchingCommand == null) {
                addMarkup(command);
              } else {
                switch (matchingCommand.action) {
                  case IGNORE: {
                    addMarkup(match);
                    break;
                  }
                  case DUMMY: {
                    addMarkup(match, generateDummy(matchingCommand.dummyGenerator));
                    break;
                  }
                }
              }
            }

            break;
          }
          case '{': {
            String length = matchFromPosition(lengthInBracePattern);

            if (!length.isEmpty()) {
              addMarkup(length);
            } else {
              modeStack.push(curMode);
              addMarkup(curString);
            }

            break;
          }
          case '}': {
            String interpretAs = "";
            if ((curMode == Mode.HEADING) && lastPunctuation.isEmpty()) interpretAs = ".";
            popMode();
            addMarkup(curString, interpretAs);
            canInsertSpaceBeforeDummy = true;
            if (isTextMode(curMode) && isMathMode(modeStack.peek())) isMathEmpty = true;
            isMathCharTrivial = true;
            break;
          }
          case '$': {
            String displayMath = matchFromPosition(displayMathPattern);

            if (!displayMath.isEmpty()) {
              if (curMode == Mode.DISPLAY_MATH) {
                popMode();
                addMarkup(displayMath, generateDummy());
              } else {
                enterDisplayMath();
                addMarkup(displayMath);
              }
            } else {
              if (curMode == Mode.INLINE_MATH) {
                popMode();
                addMarkup(curString, generateDummy());
              } else {
                enterInlineMath();
                addMarkup(curString);
              }
            }

            break;
          }
          case '%': {
            String comment = matchFromPosition(commentPattern);
            preserveDummyLast = true;
            isMathCharTrivial = true;
            addMarkup(comment, (containsTwoEndsOfLine(comment) ? "\n\n" : ""));
            break;
          }
          case ' ':
          case '&':
          case '~':
          case '\n':
          case '\r':
          case '\t': {
            String whiteSpace = (((curChar != '~') && (curChar != '&')) ?
                matchFromPosition(whiteSpacePattern) : curString);
            preserveDummyLast = true;
            isMathCharTrivial = true;

            if (isTextMode(curMode)) {
              if (containsTwoEndsOfLine(whiteSpace)) {
                addMarkup(whiteSpace, "\n\n");
              } else {
                addMarkup(whiteSpace, (lastSpace.isEmpty() ? " " : ""));
              }
            } else {
              addMarkup(whiteSpace);
            }

            if ((curChar == '~') || (curChar == '&')) {
              dummyLastSpace = " ";
            }

            break;
          }
          case '`':
          case '\'':
          case '"': {
            if (isTextMode(curMode)) {
              String quote = "";
              String smartQuote = "";

              if (pos + 1 < code.length()) {
                quote = code.substring(pos, pos + 2);

                if (quote.equals("``") || quote.equals("\"'")) {
                  smartQuote = "\u201c";
                } else if (quote.equals("''")) {
                  smartQuote = "\u201d";
                } else if (quote.equals("\"`")) {
                  smartQuote = "\u201e";
                } else {
                  quote = "";
                }
              }

              if (quote.isEmpty()) addText(curString);
              else addMarkup(quote, smartQuote);
            } else {
              addMarkup(curString);
            }

            break;
          }
          case '-': {
            String emDash = matchFromPosition(emDashPattern);

            if (isTextMode(curMode)) {
              if (!emDash.isEmpty()) {
                addMarkup(emDash, "\u2014");
                break;
              } else {
                String enDash = matchFromPosition(enDashPattern);

                if (!enDash.isEmpty()) {
                  addMarkup(enDash, "\u2013");
                  break;
                }
              }
            }
          }
          case '[': {
            String length = matchFromPosition(lengthInBracketPattern);

            if (!length.isEmpty()) {
              isMathCharTrivial = true;
              preserveDummyLast = true;
              addMarkup(length);
              break;
            }
          }
          case '<': {
            if (codeLanguageId.equals("rsweave")) {
              String rsweaveBegin = matchFromPosition(rsweaveBeginPattern);

              if (!rsweaveBegin.isEmpty()) {
                modeStack.push(Mode.RSWEAVE);
                addMarkup(rsweaveBegin);
                break;
              }
            }
          }
          default: {
            if (isTextMode(curMode)) {
              addText(curString);
              if (isPunctuation(curChar)) lastPunctuation = curString;
            } else {
              addMarkup(curString);
              if (isPunctuation(curChar)) dummyLastPunctuation = curString;
            }

            break;
          }
        }
      }

      if (!isMathCharTrivial) {
        canInsertSpaceBeforeDummy = false;
        isMathEmpty = false;
      }

      if (pos == lastPos) {
        if (isInStrictMode) {
          throw new RuntimeException(Tools.i18n(
              "latexAnnotatedTextBuilderInfiniteLoop", getDebugInformation(code)));
        } else {
          Tools.logger.warning(Tools.i18n(
              "latexAnnotatedTextBuilderPreventedInfiniteLoop", getDebugInformation(code)));
          pos++;
        }
      }
    }

    return this;
  }

  public void setSettings(Settings settings) {
    language = settings.getLanguageShortCode();

    for (String commandPrototype : settings.getDummyCommandPrototypes()) {
      commandSignatures.add(new LatexCommandSignature(commandPrototype,
          LatexCommandSignature.Action.DUMMY));
    }

    for (String commandPrototype : settings.getIgnoreCommandPrototypes()) {
      commandSignatures.add(new LatexCommandSignature(commandPrototype,
          LatexCommandSignature.Action.IGNORE));
    }

    ignoreEnvironments.addAll(settings.getIgnoreEnvironments());
  }
}
