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
  private String lastPunctuation;
  private String lastSpace;
  private Stack<Mode> modeStack;

  private char curChar;
  private String curString;
  private Mode curMode;
  private boolean keepLastPunctuation;
  private boolean keepLastSpace;

  private String matchFromPosition(Pattern pattern) {
    Matcher matcher = pattern.matcher(text.substring(pos));
    return (matcher.find() ? matcher.group() : "");
  }


  public void addCode(String text) {
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
    lastPunctuation = "";
    lastSpace = "";

    modeStack = new Stack<Mode>();
    modeStack.push(Mode.TEXT);

    while (pos < text.length()) {
      curChar = text.charAt(pos);
      curString = String.valueOf(curChar);
      curMode = modeStack.peek();
      keepLastPunctuation = false;
      keepLastSpace = false;

      switch (curChar) {
        case '\\': {
          String command = matchFromPosition(commandPattern);

          if (command.equals("\\begin") || command.equals("\\end")) {
            builder.addMarkup(command);
            keepLastPunctuation = true;
            pos += command.length();

            String argument = matchFromPosition(argumentPattern);
            String environment = argument.substring(1, argument.length() - 1);
            String interpretAs = "";

            if (Arrays.asList(mathEnvironments).contains(environment)) {
              if (command.equals("\\begin")) {
                modeStack.push(Mode.MATH);
              } else {
                modeStack.pop();
                if (modeStack.isEmpty()) modeStack.push(Mode.TEXT);
                interpretAs = "Abc" + (pseudoCounter++) + lastPunctuation;
                keepLastPunctuation = false;
              }
            } else {
              if (command.equals("\\begin")) {
                modeStack.push(curMode);
              } else {
                modeStack.pop();
                if (modeStack.isEmpty()) modeStack.push(Mode.TEXT);
              }
            }

            builder.addMarkup(argument, interpretAs);
            keepLastSpace = interpretAs.isEmpty();
            pos += argument.length();
          } else if (command.equals("\\$") || command.equals("\\%") || command.equals("\\&")) {
            builder.addMarkup(command, command.substring(1));
            pos += command.length();
          } else if (command.equals("\\cite") || command.equals("\\cref") ||
              command.equals("\\Cref") || command.equals("\\includegraphics") ||
              command.equals("\\ref")) {
            builder.addMarkup(command, "Abc" + (pseudoCounter++));
            pos += command.length();

            String optionalArgument = matchFromPosition(optionalArgumentPattern);
            builder.addMarkup(optionalArgument);
            pos += optionalArgument.length();

            String argument = matchFromPosition(argumentPattern);
            builder.addMarkup(argument);
            pos += argument.length();

            keepLastSpace = true;
          } else if (command.equals("\\footnote")) {
            if (lastSpace.isEmpty()) {
              builder.addMarkup(command, " ");
              lastSpace = " ";
            } else {
              builder.addMarkup(command);
            }

            keepLastSpace = true;
            pos += command.length();
          } else if (command.equals("\\text")) {
            modeStack.push(Mode.TEXT);
            String interpretAs = "";

            if (curMode == Mode.MATH) {
              interpretAs = "Abc" + (pseudoCounter++) + lastPunctuation;
            }

            builder.addMarkup(command + "{", interpretAs);
            keepLastSpace = interpretAs.isEmpty();
            pos += command.length() + 1;
          } else {
            builder.addMarkup(command);
            keepLastSpace = true;
            pos += command.length();
          }

          break;
        }
        case '{': {
          modeStack.push(curMode);
          builder.addMarkup(curString);
          keepLastSpace = true;
          pos++;
          break;
        }
        case '}': {
          modeStack.pop();
          builder.addMarkup(curString);
          keepLastSpace = true;
          pos++;
          break;
        }
        case '$': {
          if (curMode == Mode.TEXT) {
            modeStack.push(Mode.MATH);
            builder.addMarkup(curString);
            keepLastSpace = true;
          } else {
            modeStack.pop();
            builder.addMarkup(curString, "Abc" + (pseudoCounter++) + lastPunctuation);
          }

          pos++;
          break;
        }
        case '%': {
          String comment = matchFromPosition(commentPattern);
          builder.addMarkup(comment);
          keepLastSpace = true;
          pos += comment.length();
          break;
        }
        case ' ':
        case '\n':
        case '\r':
        case '\t': {
          String whiteSpace = matchFromPosition(whiteSpacePattern);

          if (curMode == Mode.TEXT) {
            if (lastSpace.isEmpty()) {
              builder.addMarkup(whiteSpace, " ");
              lastSpace = " ";
            } else {
              builder.addMarkup(whiteSpace);
            }

            keepLastSpace = true;
          } else {
            builder.addMarkup(whiteSpace);
            keepLastPunctuation = true;
          }

          pos += whiteSpace.length();
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

            if (quote.isEmpty()) {
              builder.addText(curString);
              pos++;
            } else {
              builder.addMarkup(quote, smartQuote);
              pos += 2;
            }
          } else {
            builder.addMarkup(curString);
            keepLastSpace = true;
            pos++;
          }

          break;
        }
        default: {
          if (curMode == Mode.TEXT) {
            builder.addText(curString);
          } else {
            if ((curChar == '.') || (curChar == ',') || (curChar == ':') ||
                (curChar == ';')) {
              lastPunctuation = curString;
              keepLastPunctuation = true;
            }

            builder.addMarkup(curString);
            keepLastSpace = true;
          }

          pos++;
          break;
        }
      }

      if (!keepLastPunctuation) lastPunctuation = "";
      if (!keepLastSpace) lastSpace = "";
    }
  }

  public AnnotatedText getAnnotatedText() {
    return builder.build();
  }
}
