package latex;

import org.languagetool.markup.AnnotatedText;

import java.util.Arrays;
import java.util.Stack;
import java.util.regex.*;

public class AnnotatedTextBuilder {
  private org.languagetool.markup.AnnotatedTextBuilder builder =
      new org.languagetool.markup.AnnotatedTextBuilder();

  /*private static String parseCommand(String text, int pos) {
    for (int i = pos + 2; i < text.length(); i++) {
      char ch = text.charAt(i);
      if (!(((ch >= 'A') && (ch <= 'Z')) || ((ch >= 'a') && (ch <= 'z')))) {
        return text.substring(pos + 1, i);
      }
    }

    return text.substring(pos + 1, text.length());
  }*/

  private static String matchFromPosition(String text, int pos, Pattern pattern) {
    Matcher matcher = pattern.matcher(text.substring(pos));
    return (matcher.find() ? matcher.group() : "");
  }

  private enum Mode {
    TEXT,
    MATH,
  }

  public void addCode(String text) {
    Pattern commandPattern = Pattern.compile("^\\\\([^A-Za-z]|([A-Za-z]+))");
    Pattern argumentPattern = Pattern.compile("^\\{[^\\}]*?\\}");
    Pattern commentPattern = Pattern.compile("^%.*?($|(\n[ \n\r\t]*))");
    Pattern whiteSpacePattern = Pattern.compile("^[ \n\r\t]+(%.*?\n[ \n\r\t]*)?");

    String[] mathEnvironments = {"equation", "equation*", "align", "align*",
        "gather", "gather*", "alignat", "alignat*", "multline", "multline*",
        "flalign", "flalign*", "split"};

    int pos = 0;
    int pseudoCounter = 0;
    String lastPunctuation = "";
    String lastSpace = "";

    Stack<Mode> modeStack = new Stack<Mode>();
    modeStack.push(Mode.TEXT);

    while (pos < text.length()) {
      char curChar = text.charAt(pos);
      String curString = String.valueOf(curChar);
      Mode curMode = modeStack.peek();
      boolean keepLastPunctuation = false;
      boolean keepLastSpace = false;

      switch (curChar) {
        case '\\': {
          String command = matchFromPosition(text, pos, commandPattern);

          if (command.equals("\\$") || command.equals("\\%") || command.equals("\\&")) {
            builder.addMarkup(command, command.substring(1));
            pos += command.length();
          } else if (command.equals("\\begin") || command.equals("\\end")) {
            builder.addMarkup(command);
            pos += command.length();

            String argument = matchFromPosition(text, pos, argumentPattern);
            String environment = argument.substring(1, argument.length() - 1);
            String interpretAs = "";

            if (Arrays.asList(mathEnvironments).contains(environment)) {
              if (command.equals("\\begin")) {
                modeStack.push(Mode.MATH);
              } else {
                modeStack.pop();
                if (modeStack.isEmpty()) modeStack.push(Mode.TEXT);
                interpretAs = "Abc" + (pseudoCounter++) + lastPunctuation;
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
          } else if (command.equals("\\text")) {
            modeStack.push(Mode.TEXT);
            builder.addMarkup(command + "{");
            keepLastSpace = true;
            pos += command.length() + 1;
          } else if (command.equals("\\footnote")) {
            if (lastSpace.isEmpty()) {
              builder.addMarkup(command, " ");
              lastSpace = " ";
            } else {
              builder.addMarkup(command);
            }

            keepLastSpace = true;
            pos += command.length();
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
          String comment = matchFromPosition(text, pos, commentPattern);
          builder.addMarkup(comment);
          keepLastSpace = true;
          pos += comment.length();
          break;
        }
        case ' ':
        case '\n':
        case '\r':
        case '\t': {
          String whiteSpace = matchFromPosition(text, pos, whiteSpacePattern);

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
