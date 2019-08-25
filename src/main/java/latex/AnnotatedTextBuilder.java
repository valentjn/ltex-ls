package latex;

import org.languagetool.markup.AnnotatedText;

import java.util.Arrays;
import java.util.Stack;
import java.util.regex.*;

public class AnnotatedTextBuilder {
  private enum Mode {
    TEXT,
    MATH,
  }

  private org.languagetool.markup.AnnotatedTextBuilder builder =
      new org.languagetool.markup.AnnotatedTextBuilder();

  private String text;
  private int pos;
  private int pseudoCounter;
  private String lastSpace;
  private String dummyLastSpace;
  private String dummyLastPunctuation;
  private boolean preserveDummyLast;
  private Stack<Mode> modeStack;

  private char curChar;
  private String curString;
  private Mode curMode;

  private String matchFromPosition(Pattern pattern) {
    Matcher matcher = pattern.matcher(text.substring(pos));
    return (matcher.find() ? matcher.group() : "");
  }

  private String constructDummy() {
    String dummy;

    if (curMode == Mode.TEXT) {
      dummy = "Abc" + (pseudoCounter++);
    } else {
      dummy = "Abc" + (pseudoCounter++) + dummyLastPunctuation + dummyLastSpace;
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
  }

  public AnnotatedTextBuilder addCode(String text) {
    Pattern commandPattern = Pattern.compile("^\\\\([^A-Za-z]|([A-Za-z]+))");
    Pattern argumentPattern = Pattern.compile("^\\{[^\\}]*?\\}");
    Pattern optionalArgumentPattern = Pattern.compile("^\\[[^\\]]*?\\]");
    Pattern commentPattern = Pattern.compile("^%.*?($|(\n[ \n\r\t]*))");
    Pattern whiteSpacePattern = Pattern.compile("^[ \n\r\t]+(%.*?\n[ \n\r\t]*)?");

    String[] mathEnvironments = {"equation", "equation*", "align", "align*",
        "gather", "gather*", "alignat", "alignat*", "multline", "multline*",
        "flalign", "flalign*"};

    this.text = text;
    pos = 0;
    pseudoCounter = 0;
    lastSpace = "";
    dummyLastSpace = "";
    dummyLastPunctuation = "";
    preserveDummyLast = false;

    modeStack = new Stack<Mode>();
    modeStack.push(Mode.TEXT);

    boolean canInsertSpaceBeforeDummy = false;
    boolean preserveCanInsertSpaceBeforeDummy = false;

    while (pos < text.length()) {
      curChar = text.charAt(pos);
      curString = String.valueOf(curChar);
      curMode = modeStack.peek();
      preserveCanInsertSpaceBeforeDummy = false;

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
                modeStack.push(Mode.MATH);
                canInsertSpaceBeforeDummy = true;
                preserveCanInsertSpaceBeforeDummy = true;
              } else {
                modeStack.pop();
                if (modeStack.isEmpty()) modeStack.push(Mode.TEXT);
                interpretAs = constructDummy();
              }
            } else {
              if (command.equals("\\begin")) {
                modeStack.push(curMode);
              } else {
                modeStack.pop();
                if (modeStack.isEmpty()) modeStack.push(Mode.TEXT);
              }
            }

            preserveDummyLast = true;
            addMarkup(argument, interpretAs);
          } else if (command.equals("\\$") || command.equals("\\%") || command.equals("\\&")) {
            addMarkup(command, command.substring(1));
          } else if (command.equals("\\,") || command.equals("\\;") || command.equals("\\quad")) {
            if ((curMode == Mode.MATH) && (canInsertSpaceBeforeDummy)) {
              addMarkup(command, " ");
            } else {
              preserveDummyLast = true;
              addMarkup(command);
              dummyLastSpace = " ";
            }
          } else if (command.equals("\\cite") || command.equals("\\cref") ||
              command.equals("\\Cref") || command.equals("\\includegraphics") ||
              command.equals("\\ref")) {
            addMarkup(command, constructDummy());
            addMarkup(matchFromPosition(optionalArgumentPattern));
            addMarkup(matchFromPosition(argumentPattern));
          } else if (command.equals("\\hspace") || command.equals("\\vspace")) {
            addMarkup(command);
            addMarkup(matchFromPosition(argumentPattern));
          } else if (command.equals("\\footnote")) {
            if (lastSpace.isEmpty()) {
              addMarkup(command, " ");
              lastSpace = " ";
            } else {
              addMarkup(command);
            }
          } else if (command.equals("\\text") || command.equals("\\intertext")) {
            modeStack.push(Mode.TEXT);
            String interpretAs = ((curMode == Mode.MATH) ? constructDummy() : "");
            addMarkup(command + "{", interpretAs);
          } else {
            addMarkup(command);
          }

          break;
        }
        case '{': {
          modeStack.push(curMode);
          addMarkup(curString);
          break;
        }
        case '}': {
          modeStack.pop();
          addMarkup(curString);
          canInsertSpaceBeforeDummy = true;
          preserveCanInsertSpaceBeforeDummy = true;
          break;
        }
        case '$': {
          if (curMode == Mode.TEXT) {
            modeStack.push(Mode.MATH);
            addMarkup(curString);
            canInsertSpaceBeforeDummy = true;
            preserveCanInsertSpaceBeforeDummy = true;
          } else {
            modeStack.pop();
            addMarkup(curString, constructDummy());
          }

          break;
        }
        case '%': {
          String comment = matchFromPosition(commentPattern);
          preserveDummyLast = true;
          preserveCanInsertSpaceBeforeDummy = true;
          addMarkup(comment);
          break;
        }
        case ' ':
        case '\n':
        case '\r':
        case '\t': {
          String whiteSpace = matchFromPosition(whiteSpacePattern);
          preserveDummyLast = true;
          preserveCanInsertSpaceBeforeDummy = true;

          if (curMode == Mode.TEXT) {
            if (lastSpace.isEmpty()) {
              addMarkup(whiteSpace, " ");
              lastSpace = " ";
            } else {
              addMarkup(whiteSpace);
            }
          } else {
            addMarkup(whiteSpace);
          }

          break;
        }
        case '`':
        case '\'':
        case '"': {
          if (curMode == Mode.TEXT) {
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
        default: {
          if (curMode == Mode.TEXT) {
            addText(curString);
          } else {
            addMarkup(curString);

            if ((curChar == '.') || (curChar == ',') || (curChar == ':') ||
                (curChar == ';')) {
              dummyLastPunctuation = curString;
            }
          }

          break;
        }
      }

      if (!preserveCanInsertSpaceBeforeDummy) canInsertSpaceBeforeDummy = false;
    }

    return this;
  }

  public AnnotatedText getAnnotatedText() {
    return builder.build();
  }
}
