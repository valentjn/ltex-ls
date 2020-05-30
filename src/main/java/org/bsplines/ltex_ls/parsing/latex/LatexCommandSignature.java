package org.bsplines.ltex_ls.parsing.latex;

import java.util.ArrayList;
import java.util.Stack;
import java.util.regex.*;

public class LatexCommandSignature {
  public enum ArgumentType {
    BRACE,
    BRACKET,
  }

  public enum Action {
    IGNORE,
    DUMMY,
  }

  private static final Pattern commandPattern = Pattern.compile(
      "^\\\\([^A-Za-z@]|([A-Za-z@]+))\\*?");
  private static final Pattern argumentPattern = Pattern.compile("^((\\{\\})|(\\[\\]))");
  private static final Pattern commentPattern = Pattern.compile("^%.*?($|(\n[ \n\r\t]*))");

  public String name = "";
  public ArrayList<ArgumentType> argumentTypes = new ArrayList<ArgumentType>();
  public Action action = Action.IGNORE;

  private Pattern thisCommandPattern;

  public LatexCommandSignature(String commandPrototype) {
    this(commandPrototype, Action.IGNORE);
  }

  public LatexCommandSignature(String commandPrototype, Action action) {
    Matcher commandMatcher = commandPattern.matcher(commandPrototype);
    if (!commandMatcher.find()) return;
    name = commandMatcher.group();
    int pos = commandMatcher.end();

    while (true) {
      Matcher argumentMatcher = argumentPattern.matcher(commandPrototype.substring(pos));
      if (!argumentMatcher.find()) break;

      LatexCommandSignature.ArgumentType argumentType = null;

      if (argumentMatcher.group(2) != null) {
        argumentType = LatexCommandSignature.ArgumentType.BRACE;
      } else if (argumentMatcher.group(3) != null) {
        argumentType = LatexCommandSignature.ArgumentType.BRACKET;
      }

      argumentTypes.add(argumentType);
      pos += argumentMatcher.group().length();
      assert argumentMatcher.group().length() > 0;
    }

    this.action = action;
    thisCommandPattern = Pattern.compile("^" + Pattern.quote(name));
  }

  private static String matchFromPosition(String code, int pos, Pattern pattern) {
    Matcher matcher = pattern.matcher(code.substring(pos));
    return (matcher.find() ? matcher.group() : "");
  }

  public static String matchArgumentFromPosition(String code, int pos, ArgumentType argumentType) {
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

    if (code.charAt(pos) != openChar) return "";
    pos++;
    argumentTypeStack.push(argumentType);

    while (pos < code.length()) {
      switch (code.charAt(pos)) {
        case '\\': {
          if (pos + 1 < code.length()) pos++;
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
                code.substring(startPos, pos + 1) : "");
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
                code.substring(startPos, pos + 1) : "");
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

  public String matchFromPosition(String code, int pos) {
    int startPos = pos;
    String match = matchFromPosition(code, pos, thisCommandPattern);
    pos += match.length();

    for (ArgumentType argumentType : argumentTypes) {
      match = matchFromPosition(code, pos, commentPattern);
      pos += match.length();

      match = matchArgumentFromPosition(code, pos, argumentType);
      if (match.isEmpty()) return "";
      pos += match.length();
    }

    return code.substring(startPos, pos);
  }
}
