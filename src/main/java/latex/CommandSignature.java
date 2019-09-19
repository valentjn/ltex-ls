package latex;

import java.util.ArrayList;
import java.util.Stack;
import java.util.regex.*;

public class CommandSignature {
  public enum ArgumentType {
    BRACE,
    BRACKET,
  }

  public enum Action {
    IGNORE,
    DUMMY,
  }

  public String name = "";
  public ArrayList<ArgumentType> argumentTypes = new ArrayList<ArgumentType>();
  public Action action = Action.IGNORE;

  public CommandSignature(String commandPrototype) {
    this(commandPrototype, Action.IGNORE);
  }

  public CommandSignature(String commandPrototype, Action action) {
    Pattern commandPattern = Pattern.compile("^\\\\([^A-Za-z@]|([A-Za-z@]+))\\*?");
    Pattern argumentPattern = Pattern.compile("^((\\{\\})|(\\[\\]))");

    Matcher commandMatcher = commandPattern.matcher(commandPrototype);
    if (!commandMatcher.find()) return;
    name = commandMatcher.group();
    int pos = commandMatcher.end();

    while (true) {
      Matcher argumentMatcher = argumentPattern.matcher(commandPrototype.substring(pos));
      if (!argumentMatcher.find()) break;

      CommandSignature.ArgumentType argumentType = null;

      if (argumentMatcher.group(2) != null) {
        argumentType = CommandSignature.ArgumentType.BRACE;
      } else if (argumentMatcher.group(3) != null) {
        argumentType = CommandSignature.ArgumentType.BRACKET;
      }

      argumentTypes.add(argumentType);
      pos += argumentMatcher.group().length();
      assert argumentMatcher.group().length() > 0;
    }

    this.action = action;
  }

  private static String matchFromPosition(String text, int pos, Pattern pattern) {
    Matcher matcher = pattern.matcher(text.substring(pos));
    return (matcher.find() ? matcher.group() : "");
  }

  public static String matchArgumentFromPosition(String text, int pos, ArgumentType argumentType)
      throws InterruptedException {
    int startPos = pos;
    Stack<ArgumentType> argumentTypeStack = new Stack<>();
    char openChar = '\0';

    switch (argumentType) {
      case BRACE: {
        openChar = '{';
        break;
      }
      case BRACKET: {
        openChar = '[';
        break;
      }
    }

    if (text.charAt(pos) != openChar) return "";
    pos++;
    argumentTypeStack.push(argumentType);

    while (pos < text.length()) {
      if (Thread.currentThread().isInterrupted()) throw new InterruptedException();

      switch (text.charAt(pos)) {
        case '\\': {
          if (pos + 1 < text.length()) pos++;
          break;
        }
        case '{': {
          argumentTypeStack.push(ArgumentType.BRACE);
          break;
        }
        case '[': {
          argumentTypeStack.push(ArgumentType.BRACKET);
          break;
        }
        case '}': {
          if (argumentTypeStack.peek() != ArgumentType.BRACE) {
            return "";
          } else if (argumentTypeStack.size() == 1) {
            return ((argumentType == ArgumentType.BRACE) ?
                text.substring(startPos, pos + 1) : "");
          } else {
            argumentTypeStack.pop();
          }

          break;
        }
        case ']': {
          if (argumentTypeStack.peek() != ArgumentType.BRACKET) {
            return "";
          } else if (argumentTypeStack.size() == 1) {
            return ((argumentType == ArgumentType.BRACKET) ?
                text.substring(startPos, pos + 1) : "");
          } else {
            argumentTypeStack.pop();
          }

          break;
        }
      }

      pos++;
    }

    return "";
  }

  public String matchFromPosition(String text, int pos) throws InterruptedException {
    Pattern commandPattern = Pattern.compile("^" + Pattern.quote(name));
    Pattern commentPattern = Pattern.compile("^%.*?($|(\n[ \n\r\t]*))");

    int startPos = pos;
    String match = matchFromPosition(text, pos, commandPattern);
    pos += match.length();

    for (ArgumentType argumentType : argumentTypes) {
      if (Thread.currentThread().isInterrupted()) throw new InterruptedException();

      match = matchFromPosition(text, pos, commentPattern);
      pos += match.length();

      match = matchArgumentFromPosition(text, pos, argumentType);
      if (match.isEmpty()) return "";
      pos += match.length();
    }

    return text.substring(startPos, pos);
  }
}
