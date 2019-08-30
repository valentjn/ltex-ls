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
  }

  private org.languagetool.markup.AnnotatedTextBuilder builder =
      new org.languagetool.markup.AnnotatedTextBuilder();

  private String text;
  private int pos;
  private int pseudoCounter;
  private String lastSpace;
  private String lastPunctuation;
  private String dummyLastSpace;
  private String dummyLastPunctuation;
  private boolean isMathEmpty;
  private boolean preserveDummyLast;
  private Stack<Mode> modeStack;

  private char curChar;
  private String curString;
  private Mode curMode;

  private static final CommandSignature[] defaultCommandSignatures = {
    new CommandSignature("\\bibliography{}", CommandSignature.Action.IGNORE),
    new CommandSignature("\\bibliographystyle{}", CommandSignature.Action.IGNORE),
    new CommandSignature("\\cite{}", CommandSignature.Action.DUMMY),
    new CommandSignature("\\cite[]{}", CommandSignature.Action.DUMMY),
    new CommandSignature("\\cref{}", CommandSignature.Action.DUMMY),
    new CommandSignature("\\Cref{}", CommandSignature.Action.DUMMY),
    new CommandSignature("\\documentclass{}", CommandSignature.Action.IGNORE),
    new CommandSignature("\\documentclass[]{}", CommandSignature.Action.IGNORE),
    new CommandSignature("\\draw[]", CommandSignature.Action.IGNORE),
    new CommandSignature("\\eqref{}", CommandSignature.Action.DUMMY),
    new CommandSignature("\\hypersetup{}", CommandSignature.Action.IGNORE),
    new CommandSignature("\\hyperref[]", CommandSignature.Action.IGNORE),
    new CommandSignature("\\include{}", CommandSignature.Action.IGNORE),
    new CommandSignature("\\includegraphics{}", CommandSignature.Action.IGNORE),
    new CommandSignature("\\includegraphics[]{}", CommandSignature.Action.IGNORE),
    new CommandSignature("\\input{}", CommandSignature.Action.IGNORE),
    new CommandSignature("\\node[]", CommandSignature.Action.IGNORE),
    new CommandSignature("\\label{}", CommandSignature.Action.IGNORE),
    new CommandSignature("\\raisebox{}", CommandSignature.Action.IGNORE),
    new CommandSignature("\\ref{}", CommandSignature.Action.DUMMY),
    new CommandSignature("\\ref*{}", CommandSignature.Action.DUMMY),
    new CommandSignature("\\textcolor{}", CommandSignature.Action.IGNORE),
    new CommandSignature("\\todo{}", CommandSignature.Action.IGNORE),
    new CommandSignature("\\todo[]{}", CommandSignature.Action.IGNORE),
    new CommandSignature("\\vspace{}", CommandSignature.Action.IGNORE),
    new CommandSignature("\\vspace*{}", CommandSignature.Action.IGNORE),
  };

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
      dummy = "Dummy" + (pseudoCounter++);
    } else if (isMathEmpty) {
      dummy = "";
    } else if (curMode == Mode.DISPLAY_MATH) {
      dummy = ((lastSpace.isEmpty() ? " " : "")) + "Dummy" + (pseudoCounter++) +
          dummyLastPunctuation + " ";
    } else {
      dummy = "Dummy" + (pseudoCounter++) + dummyLastPunctuation + dummyLastSpace;
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
    lastSpace = ((lastChar == ' ') ? " " : "");
    lastPunctuation = (isPunctuation(lastChar) ? " " : "");
  }

  private static boolean isPunctuation(char ch) {
    return ((ch == '.') || (ch == ',') || (ch == ':') || (ch == ';'));
  }

  private static boolean isMathMode(Mode mode) {
    return ((mode == Mode.INLINE_MATH) || (mode == Mode.DISPLAY_MATH));
  }

  private static boolean isTextMode(Mode mode) {
    return !isMathMode(mode);
  }

  public AnnotatedTextBuilder addCode(String text) {
    Pattern commandPattern = Pattern.compile("^\\\\(([^A-Za-z]|([A-Za-z]+))\\*?)");
    Pattern argumentPattern = Pattern.compile("^\\{[^\\}]*?\\}");
    Pattern commentPattern = Pattern.compile("^%.*?($|(\n[ \n\r\t]*))");
    Pattern whiteSpacePattern = Pattern.compile("^[ \n\r\t]+(%.*?\n[ \n\r\t]*)?");
    Pattern lengthPattern = Pattern.compile("-?[0-9]*(\\.[0-9]+)?(pt|mm|cm|ex|em|bp|dd|pc|in)");
    Pattern lengthInBracePattern = Pattern.compile("^\\{" + lengthPattern.pattern() + "\\}");
    Pattern lengthInBracketPattern = Pattern.compile("^\\[" + lengthPattern.pattern() + "\\]");
    Pattern emDashPattern = Pattern.compile("^---");
    Pattern enDashPattern = Pattern.compile("^--");

    String[] mathEnvironments = {"equation", "equation*", "align", "align*",
        "gather", "gather*", "alignat", "alignat*", "multline", "multline*",
        "flalign", "flalign*"};

    this.text = text;
    pos = 0;
    pseudoCounter = 0;
    lastSpace = "";
    lastPunctuation = "";
    dummyLastSpace = "";
    dummyLastPunctuation = "";
    isMathEmpty = true;
    preserveDummyLast = false;

    modeStack = new Stack<>();
    modeStack.push(Mode.TEXT);

    boolean canInsertSpaceBeforeDummy = false;
    boolean isMathCharTrivial = false;

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
                modeStack.push(Mode.DISPLAY_MATH);
                isMathEmpty = true;
                canInsertSpaceBeforeDummy = true;
              } else {
                modeStack.pop();
                if (modeStack.isEmpty()) modeStack.push(Mode.TEXT);
                interpretAs = generateDummy();
              }
            } else {
              if (command.equals("\\begin")) {
                modeStack.push(curMode);
              } else {
                modeStack.pop();
                if (modeStack.isEmpty()) modeStack.push(Mode.TEXT);
              }
            }

            isMathCharTrivial = true;
            preserveDummyLast = true;
            addMarkup(argument, interpretAs);
          } else if (command.equals("\\$") || command.equals("\\%") || command.equals("\\&")) {
            addMarkup(command, command.substring(1));
          } else if (command.equals("\\-")) {
            addMarkup(command);
          } else if (command.equals("\\ ") || command.equals("\\,") || command.equals("\\;") ||
              command.equals("\\\\") || command.equals("\\quad") || command.equals("\\hfill") ||
              command.equals("\\hspace") || command.equals("\\hspace*")) {
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
          } else if (command.equals("\\qed")) {
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
          modeStack.pop();
          if (modeStack.isEmpty()) modeStack.push(Mode.TEXT);
          addMarkup(curString, interpretAs);
          canInsertSpaceBeforeDummy = true;
          if (isTextMode(curMode) && isMathMode(modeStack.peek())) isMathEmpty = true;
          isMathCharTrivial = true;
          break;
        }
        case '$': {
          if (isTextMode(curMode)) {
            modeStack.push(Mode.INLINE_MATH);
            addMarkup(curString);
            isMathEmpty = true;
            canInsertSpaceBeforeDummy = true;
            isMathCharTrivial = true;
          } else {
            modeStack.pop();
            if (modeStack.isEmpty()) modeStack.push(Mode.TEXT);
            addMarkup(curString, generateDummy());
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
        case '[': {
          String length = matchFromPosition(lengthInBracketPattern);

          if (!length.isEmpty()) {
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
