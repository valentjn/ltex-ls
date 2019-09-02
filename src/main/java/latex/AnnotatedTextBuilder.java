package latex;

import org.languagetool.markup.AnnotatedText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.regex.*;

public class AnnotatedTextBuilder {
  private enum Mode {
    TEXT,
    HEADING,
    INLINE_MATH,
    DISPLAY_MATH,
    TIKZ,
  }

  private static final CommandSignature[] defaultCommandSignatures = {
    new CommandSignature("\\addtocontents{}"),
    new CommandSignature("\\algnewcommand{}"),
    new CommandSignature("\\algrenewcommand{}"),
    new CommandSignature("\\bibliography{}"),
    new CommandSignature("\\bibliographystyle{}"),
    new CommandSignature("\\cite{}", CommandSignature.Action.DUMMY),
    new CommandSignature("\\cite[]{}", CommandSignature.Action.DUMMY),
    new CommandSignature("\\cref{}", CommandSignature.Action.DUMMY),
    new CommandSignature("\\Cref{}", CommandSignature.Action.DUMMY),
    new CommandSignature("\\crefname{}{}{}"),
    new CommandSignature("\\DeclareMathOperator{}"),
    new CommandSignature("\\definecolor{}{}{}"),
    new CommandSignature("\\documentclass{}"),
    new CommandSignature("\\documentclass[]{}"),
    new CommandSignature("\\eqref{}", CommandSignature.Action.DUMMY),
    new CommandSignature("\\graphicspath{}"),
    new CommandSignature("\\hypersetup{}"),
    new CommandSignature("\\hyperref[]"),
    new CommandSignature("\\include{}"),
    new CommandSignature("\\includegraphics{}"),
    new CommandSignature("\\includegraphics[]{}"),
    new CommandSignature("\\input{}"),
    new CommandSignature("\\label{}"),
    new CommandSignature("\\newcommand{}{}"),
    new CommandSignature("\\newcommand[]{}{}"),
    new CommandSignature("\\newcommand*{}{}"),
    new CommandSignature("\\newcommand*[]{}{}"),
    new CommandSignature("\\newcounter{}"),
    new CommandSignature("\\raisebox{}"),
    new CommandSignature("\\ref{}", CommandSignature.Action.DUMMY),
    new CommandSignature("\\ref*{}", CommandSignature.Action.DUMMY),
    new CommandSignature("\\renewcommand{}{}"),
    new CommandSignature("\\renewcommand[]{}{}"),
    new CommandSignature("\\renewcommand*{}{}"),
    new CommandSignature("\\renewcommand*[]{}{}"),
    new CommandSignature("\\textcolor{}"),
    new CommandSignature("\\tikzset{}"),
    new CommandSignature("\\sisetup{}"),
    new CommandSignature("\\todo{}"),
    new CommandSignature("\\todo[]{}"),
    new CommandSignature("\\usetikzlibrary{}"),
    new CommandSignature("\\usepackage{}"),
    new CommandSignature("\\usepackage[]{}"),
    new CommandSignature("\\vspace{}"),
    new CommandSignature("\\vspace*{}"),
  };

  private static final Pattern commandPattern = Pattern.compile(
      "^\\\\(([^A-Za-z]|([A-Za-z]+))\\*?)");
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
  private static final Pattern umlautCommandPattern = Pattern.compile(
      "^\\\\\"([AEIOUaeiou]|(\\{([AEIOUaeiou])\\}))");
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
          dummyLastPunctuation + " ";
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

  private AnnotatedTextBuilder addMarkup(String markup, String interpretAs) {
    if (interpretAs.isEmpty()) {
      return addMarkup(markup);
    } else {
      builder.addMarkup(markup, interpretAs);
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
    if (modeStack.isEmpty()) modeStack.push(Mode.TEXT);
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

  public AnnotatedTextBuilder addCode(String text) {
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
    modeStack.push(Mode.TEXT);

    while (pos < text.length()) {
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
          } else if (command.equals("\\$") || command.equals("\\%") || command.equals("\\&")) {
            addMarkup(command, command.substring(1));
          } else if (command.equals("\\[") || command.equals("\\]") ||
              command.equals("\\(") || command.equals("\\)")) {
            if (command.equals("\\[")) {
              enterDisplayMath();
            } else if (command.equals("\\(")) {
              enterInlineMath();
            } else {
              popMode();
              addMarkup(command, generateDummy());
            }
          } else if (command.equals("\\ss")) {
            addMarkup(command, "\u00df");
          } else if (command.equals("\\\"")) {
            String umlautCommand = matchFromPosition(umlautCommandPattern);

            if (!umlautCommand.isEmpty()) {
              String vowel = ((umlautCommand.length() <= 3) ?
                  umlautCommand.substring(umlautCommand.length() - 1) :
                  umlautCommand.substring(3, 4));
              String interpretAs = "";

              if (vowel.equals("A")) interpretAs = "\u00c4";
              else if (vowel.equals("E")) interpretAs = "\u00cb";
              else if (vowel.equals("I")) interpretAs = "\u00cf";
              else if (vowel.equals("O")) interpretAs = "\u00d6";
              else if (vowel.equals("U")) interpretAs = "\u00dc";
              else if (vowel.equals("a")) interpretAs = "\u00e4";
              else if (vowel.equals("e")) interpretAs = "\u00eb";
              else if (vowel.equals("i")) interpretAs = "\u00ef";
              else if (vowel.equals("o")) interpretAs = "\u00f6";
              else if (vowel.equals("u")) interpretAs = "\u00fc";

              addMarkup(umlautCommand, interpretAs);
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
            modeStack.push(Mode.TEXT);
            String interpretAs = (isMathMode(curMode) ? generateDummy() : "");
            addMarkup(command + "{", interpretAs);
          } else {
            String match = "";
            CommandSignature matchingCommand = null;

            for (CommandSignature commandSignature : commandSignatures) {
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
