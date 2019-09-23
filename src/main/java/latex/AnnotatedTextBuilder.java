package latex;

import org.languagetool.markup.AnnotatedText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.regex.*;

public class AnnotatedTextBuilder {
  private enum Mode {
    PARAGRAPH_TEXT,
    INLINE_TEXT,
    HEADING,
    INLINE_MATH,
    DISPLAY_MATH,
    TIKZ,
  }

  private static final CommandSignature[] defaultCommandSignatures = {
    new CommandSignature("\\addtotheorempostheadhook{}"),
    new CommandSignature("\\addxcontentsline{}{}{}"),
    new CommandSignature("\\AtBeginEnvironment{}{}"),
    new CommandSignature("\\AtEndEnvironment{}{}"),
    new CommandSignature("\\addbibresource{}"),
    new CommandSignature("\\addtocontents{}"),
    new CommandSignature("\\addtocounter{}{}"),
    new CommandSignature("\\addtokomafont{}{}"),
    new CommandSignature("\\algdef{}[]{}{}"),
    new CommandSignature("\\algnewcommand{}{}"),
    new CommandSignature("\\algrenewcommand{}{}"),
    new CommandSignature("\\arabic{}", CommandSignature.Action.DUMMY),
    new CommandSignature("\\bibliography{}"),
    new CommandSignature("\\bibliographystyle{}"),
    new CommandSignature("\\captionsetup{}"),
    new CommandSignature("\\captionsetup[]{}"),
    new CommandSignature("\\cite{}", CommandSignature.Action.DUMMY),
    new CommandSignature("\\cite[]{}", CommandSignature.Action.DUMMY),
    new CommandSignature("\\clearfield{}"),
    new CommandSignature("\\cref{}", CommandSignature.Action.DUMMY),
    new CommandSignature("\\Cref{}", CommandSignature.Action.DUMMY),
    new CommandSignature("\\crefname{}{}{}"),
    new CommandSignature("\\Crefname{}{}{}"),
    new CommandSignature("\\DeclareCaptionFormat{}{}"),
    new CommandSignature("\\DeclareCaptionLabelFormat{}{}"),
    new CommandSignature("\\DeclareCiteCommand{}{}{}{}{}"),
    new CommandSignature("\\DeclareCiteCommand{}[]{}{}{}{}"),
    new CommandSignature("\\DeclareFieldFormat{}{}"),
    new CommandSignature("\\DeclareFieldFormat[]{}{}"),
    new CommandSignature("\\DeclareGraphicsExtensions{}"),
    new CommandSignature("\\DeclareMathOperator{}{}"),
    new CommandSignature("\\DeclareMathOperator*{}{}"),
    new CommandSignature("\\DeclareNameAlias{}{}"),
    new CommandSignature("\\DeclareNewTOC{}"),
    new CommandSignature("\\DeclareNewTOC[]{}"),
    new CommandSignature("\\declaretheorem{}"),
    new CommandSignature("\\declaretheorem[]{}"),
    new CommandSignature("\\declaretheoremstyle{}"),
    new CommandSignature("\\declaretheoremstyle[]{}"),
    new CommandSignature("\\DeclareTOCStyleEntry{}"),
    new CommandSignature("\\DeclareTOCStyleEntry[]{}{}"),
    new CommandSignature("\\defbibheading{}{}"),
    new CommandSignature("\\defbibheading{}[]{}"),
    new CommandSignature("\\defbibnote{}{}"),
    new CommandSignature("\\definecolor{}{}{}"),
    new CommandSignature("\\DisableLigatures{}"),
    new CommandSignature("\\documentclass{}"),
    new CommandSignature("\\documentclass[]{}"),
    new CommandSignature("\\email{}", CommandSignature.Action.DUMMY),
    new CommandSignature("\\eqref{}", CommandSignature.Action.DUMMY),
    new CommandSignature("\\etocsetnexttocdepth{}"),
    new CommandSignature("\\etocsettocstyle{}{}"),
    new CommandSignature("\\GenericWarning{}{}"),
    new CommandSignature("\\geometry{}"),
    new CommandSignature("\\glsaddstoragekey{}{}{}"),
    new CommandSignature("\\graphicspath{}"),
    new CommandSignature("\\hypersetup{}"),
    new CommandSignature("\\hyperref[]"),
    new CommandSignature("\\ifcurrentfield{}"),
    new CommandSignature("\\ifentrytype{}"),
    new CommandSignature("\\iftoggle{}"),
    new CommandSignature("\\include{}"),
    new CommandSignature("\\includegraphics{}"),
    new CommandSignature("\\includegraphics[]{}"),
    new CommandSignature("\\input{}"),
    new CommandSignature("\\KOMAoptions{}"),
    new CommandSignature("\\label{}"),
    new CommandSignature("\\lettrine{}{}", CommandSignature.Action.DUMMY),
    new CommandSignature("\\lettrine[]{}{}", CommandSignature.Action.DUMMY),
    new CommandSignature("\\luadirect{}"),
    new CommandSignature("\\luaexec{}"),
    new CommandSignature("\\linespread{}"),
    new CommandSignature("\\mdfdefinestyle{}{}"),
    new CommandSignature("\\multicolumn{}{}"),
    new CommandSignature("\\multirow{}{}"),
    new CommandSignature("\\newcolumntype{}{}"),
    new CommandSignature("\\newcommand{}{}"),
    new CommandSignature("\\newcommand{}[]{}"),
    new CommandSignature("\\newcommand*{}{}"),
    new CommandSignature("\\newcommand*{}[]{}"),
    new CommandSignature("\\newcounter{}"),
    new CommandSignature("\\newglossaryentry{}{}"),
    new CommandSignature("\\newglossarystyle{}{}"),
    new CommandSignature("\\nolinkurl{}", CommandSignature.Action.DUMMY),
    new CommandSignature("\\PackageWarning{}{}"),
    new CommandSignature("\\pgfdeclaredecoration{}{}{}"),
    new CommandSignature("\\pgfmathsetseed{}"),
    new CommandSignature("\\raisebox{}"),
    new CommandSignature("\\RedeclareSectionCommand{}"),
    new CommandSignature("\\RedeclareSectionCommand[]{}"),
    new CommandSignature("\\RedeclareSectionCommands{}"),
    new CommandSignature("\\RedeclareSectionCommands[]{}"),
    new CommandSignature("\\ref{}", CommandSignature.Action.DUMMY),
    new CommandSignature("\\ref*{}", CommandSignature.Action.DUMMY),
    new CommandSignature("\\renewbibmacro{}{}"),
    new CommandSignature("\\renewbibmacro*{}{}"),
    new CommandSignature("\\renewcommand{}{}"),
    new CommandSignature("\\renewcommand{}[]{}"),
    new CommandSignature("\\renewcommand*{}{}"),
    new CommandSignature("\\renewcommand*{}[]{}"),
    new CommandSignature("\\setenumerate{}"),
    new CommandSignature("\\setglossarystyle{}"),
    new CommandSignature("\\setitemize{}"),
    new CommandSignature("\\setkomafont{}{}"),
    new CommandSignature("\\setkomavar{}{}"),
    new CommandSignature("\\setkomavar{}[]{}"),
    new CommandSignature("\\setkomavar*{}{}"),
    new CommandSignature("\\setkomavar*{}[]{}"),
    new CommandSignature("\\setlist{}"),
    new CommandSignature("\\@setplength{}{}"),
    new CommandSignature("\\setstretch{}"),
    new CommandSignature("\\sisetup{}"),
    new CommandSignature("\\setcounter{}{}"),
    new CommandSignature("\\stepcounter{}"),
    new CommandSignature("\\textcolor{}"),
    new CommandSignature("\\tikz{}"),
    new CommandSignature("\\tikzset{}"),
    new CommandSignature("\\todo{}"),
    new CommandSignature("\\todo[]{}"),
    new CommandSignature("\\togglefalse{}"),
    new CommandSignature("\\toggletrue{}"),
    new CommandSignature("\\url{}", CommandSignature.Action.DUMMY),
    new CommandSignature("\\usebibmacro{}"),
    new CommandSignature("\\usekomafont{}"),
    new CommandSignature("\\usepackage{}"),
    new CommandSignature("\\usepackage[]{}"),
    new CommandSignature("\\usetikzlibrary{}"),
    new CommandSignature("\\value{}"),
    new CommandSignature("\\vspace{}"),
    new CommandSignature("\\vspace*{}"),
    new CommandSignature("\\WarningFilter{}{}"),
  };

  private static final Pattern commandPattern = Pattern.compile(
      "^\\\\(([^A-Za-z@]|([A-Za-z@]+))\\*?)");
  private static final Pattern argumentPattern = Pattern.compile("^\\{[^\\}]*?\\}");
  private static final Pattern commentPattern = Pattern.compile("^%.*?($|(\n[ \n\r\t]*))");
  private static final Pattern whiteSpacePattern = Pattern.compile(
      "^[ \n\r\t]+(%.*?\n[ \n\r\t]*)?");
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

  private static final String[] mathEnvironments = {"equation", "equation*", "align", "align*",
      "gather", "gather*", "alignat", "alignat*", "multline", "multline*",
      "flalign", "flalign*"};

  private org.languagetool.markup.AnnotatedTextBuilder builder =
      new org.languagetool.markup.AnnotatedTextBuilder();

  private String text;
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

  public List<CommandSignature> commandSignatures =
      new ArrayList<>(Arrays.asList(defaultCommandSignatures));

  private String matchFromPosition(Pattern pattern) {
    return matchFromPosition(pattern, pos);
  }

  private String matchFromPosition(Pattern pattern, int pos) {
    Matcher matcher = pattern.matcher(text.substring(pos));
    return (matcher.find() ? matcher.group() : "");
  }

  private String generateDummy() {
    String dummy;

    if (isTextMode(curMode)) {
      dummy = "Dummy" + (dummyCounter++);
    } else if (isMathEmpty) {
      if (curMode == Mode.DISPLAY_MATH) {
        dummy = (lastSpace.isEmpty() ? " " : "");
      } else {
        dummy = "";
      }
    } else if (curMode == Mode.DISPLAY_MATH) {
      dummy = ((lastSpace.isEmpty() ? " " : "")) + "Dummy" + (dummyCounter++) +
          dummyLastPunctuation + ((modeStack.peek() == Mode.INLINE_TEXT) ? dummyLastSpace : " ");
    } else {
      dummy = "Dummy" + (dummyCounter++) + dummyLastPunctuation + dummyLastSpace;
    }

    dummyLastSpace = "";
    dummyLastPunctuation = "";
    return dummy;
  }

  private AnnotatedTextBuilder addText(String text) {
    if (text.isEmpty()) return this;
    builder.addText(text);
    pos += text.length();
    textAdded(text);
    return this;
  }

  private AnnotatedTextBuilder addMarkup(String markup) {
    if (markup.isEmpty()) return this;
    builder.addMarkup(markup);
    pos += markup.length();

    if (preserveDummyLast) {
      preserveDummyLast = false;
    } else {
      dummyLastSpace = "";
      dummyLastPunctuation = "";
    }

    return this;
  }

  private AnnotatedTextBuilder addMarkup(String markup, String interpretAs)
      throws InterruptedException {
    if (interpretAs.isEmpty()) {
      return addMarkup(markup);
    } else {
      if (markup.length() >= interpretAs.length()) {
        builder.addMarkup(markup, interpretAs);
      } else {
        // LanguageTool's AnnotatedText interpolates/extrapolates linearly between missing plain
        // text positions in AnnotatedText.getOriginalTextPositionFor, this might lead to the error
        // "fromPos (x) must be less than toPos (y)" when interpretAs is longer than markup
        // ==> work around this by first adding interpretAs up to the length of markup,
        // and then the rest character by character
        builder.addMarkup(markup, interpretAs.substring(0, markup.length()));

        for (int i = markup.length(); i < interpretAs.length(); i++) {
          if (Thread.currentThread().isInterrupted()) throw new InterruptedException();
          builder.addMarkup("",
            ((i < interpretAs.length()) ? interpretAs.substring(i, i + 1) : ""));
        }
      }

      pos += markup.length();
      preserveDummyLast = false;
      textAdded(interpretAs);
      return this;
    }
  }

  private void textAdded(String text) {
    if (text.isEmpty()) return;
    char lastChar = text.charAt(text.length() - 1);
    lastSpace = (((lastChar == ' ') || (lastChar == '\n')) ? " " : "");
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

  private static boolean isTikzMode(Mode mode) {
    return (mode == Mode.TIKZ);
  }

  private static boolean isTextMode(Mode mode) {
    return !isMathMode(mode) && !isTikzMode(mode);
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

  public AnnotatedTextBuilder addCode(String text) throws InterruptedException {
    this.text = text;
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

    while (pos < text.length()) {
      if (Thread.currentThread().isInterrupted()) throw new InterruptedException();

      curChar = text.charAt(pos);
      curString = String.valueOf(curChar);
      curMode = modeStack.peek();
      isMathCharTrivial = false;

      switch (curChar) {
        case '\\': {
          String command = matchFromPosition(commandPattern);

          if (command.equals("\\begin") || command.equals("\\end")) {
            preserveDummyLast = true;
            addMarkup(command);

            String argument = matchFromPosition(argumentPattern);
            String environment = argument.substring(1, argument.length() - 1);
            String interpretAs = "";

            if (Arrays.asList(mathEnvironments).contains(environment)) {
              if (command.equals("\\begin")) {
                enterDisplayMath();
              } else {
                popMode();
                interpretAs = generateDummy();
              }
            } else if (environment.equals("tikzpicture")) {
              if (command.equals("\\begin")) modeStack.push(Mode.TIKZ);
              else popMode();
            } else {
              if (command.equals("\\begin")) modeStack.push(curMode);
              else popMode();
            }

            isMathCharTrivial = true;
            preserveDummyLast = true;
            addMarkup(argument, interpretAs);

            if (environment.equals("tabular")) {
              String environmentArgument = CommandSignature.matchArgumentFromPosition(
                  text, pos, CommandSignature.ArgumentType.BRACE);
              addMarkup(environmentArgument);
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
            Matcher matcher = accentPattern1.matcher(text.substring(pos));

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
              }

              addMarkup(matcher.group(), interpretAs);
            } else {
              addMarkup(command);
            }
          } else if (command.equals("\\c") || command.equals("\\r")) {
            Matcher matcher = accentPattern2.matcher(text.substring(pos));

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
              }

              addMarkup(matcher.group(), interpretAs);
            } else {
              addMarkup(command);
            }
          } else if (command.equals("\\-")) {
            addMarkup(command);
          } else if (command.equals("\\ ") || command.equals("\\,") || command.equals("\\;") ||
              command.equals("\\\\") || command.equals("\\quad") || command.equals("\\qquad") ||
              command.equals("\\hfill") || command.equals("\\hspace") ||
              command.equals("\\hspace*")) {
            if (command.equals("\\hspace") || command.equals("\\hspace*")) {
              String argument = matchFromPosition(argumentPattern, pos + command.length());
              command += argument;
            }

            if (isMathMode(curMode) && lastSpace.isEmpty() && canInsertSpaceBeforeDummy) {
              addMarkup(command, " ");
            } else {
              preserveDummyLast = true;

              if (isMathMode(curMode) || isTikzMode(curMode)) {
                addMarkup(command);
                dummyLastSpace = " ";
              } else {
                addMarkup(command, (lastSpace.isEmpty() ? " " : ""));
              }
            }
          } else if (command.equals("\\dots")) {
            addMarkup(command, "...");
          } else if (command.equals("\\footnote")) {
            if (lastSpace.isEmpty()) {
              addMarkup(command, " ");
            } else {
              addMarkup(command);
            }
          } else if (command.equals("\\notag") || command.equals("\\qed")) {
            preserveDummyLast = true;
            addMarkup(command);
          } else if (command.equals("\\part") || command.equals("\\chapter") ||
              command.equals("\\section") || command.equals("\\subsection") ||
              command.equals("\\subsubsection") || command.equals("\\paragraph") ||
              command.equals("\\subparagraph")) {
            modeStack.push(Mode.HEADING);
            addMarkup(command + "{");
          } else if (command.equals("\\text") || command.equals("\\intertext")) {
            modeStack.push(Mode.INLINE_TEXT);
            String interpretAs = (isMathMode(curMode) ? generateDummy() : "");
            addMarkup(command + "{", interpretAs);
          } else {
            String match = "";
            CommandSignature matchingCommand = null;

            for (CommandSignature commandSignature : commandSignatures) {
              if (Thread.currentThread().isInterrupted()) throw new InterruptedException();

              if (commandSignature.name.equals(command)) {
                String curMatch = commandSignature.matchFromPosition(text, pos);

                if (curMatch.length() > match.length()) {
                  match = curMatch;
                  matchingCommand = commandSignature;
                }
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
                  addMarkup(match, generateDummy());
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
          addMarkup(comment, (comment.contains("\n\n") ? "\n\n" : ""));
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
            if (whiteSpace.contains("\n\n")) {
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

            if (pos + 1 < text.length()) {
              quote = text.substring(pos, pos + 2);

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

      if (!isMathCharTrivial) {
        canInsertSpaceBeforeDummy = false;
        isMathEmpty = false;
      }
    }

    return this;
  }

  public AnnotatedText getAnnotatedText() {
    return builder.build();
  }
}
