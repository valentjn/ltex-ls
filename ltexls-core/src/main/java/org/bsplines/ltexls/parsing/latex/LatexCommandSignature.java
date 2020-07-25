/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.latex;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bsplines.ltexls.Tools;
import org.bsplines.ltexls.parsing.DummyGenerator;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.xtext.xbase.lib.Pair;

public class LatexCommandSignature {
  public enum ArgumentType {
    BRACE,
    BRACKET,
    PARENTHESIS,
  }

  public enum Action {
    IGNORE,
    DUMMY,
  }

  private static final Pattern commandPattern = Pattern.compile(
      "^(\\\\.+?)(\\{\\}|\\[\\]|\\(\\))*$");
  private static final Pattern argumentPattern = Pattern.compile("^((\\{\\})|(\\[\\])|(\\(\\)))");
  private static final Pattern commentPattern = Pattern.compile("^%.*?($|(\n[ \n\r\t]*))");

  public String name = "";
  public ArrayList<ArgumentType> argumentTypes = new ArrayList<ArgumentType>();
  public Action action = Action.IGNORE;
  public DummyGenerator dummyGenerator;

  private String thisCommandPrototype;
  private Pattern thisCommandPattern;

  public LatexCommandSignature(String commandPrototype) {
    this(commandPrototype, Action.IGNORE, DummyGenerator.getDefault());
  }

  public LatexCommandSignature(String commandPrototype, Action action) {
    this(commandPrototype, action, DummyGenerator.getDefault());
  }

  /**
   * Constructor.
   *
   * @param commandPrototype prototype of the command including backslash and empty arguments
   * @param action action to perform when the command is matched
   * @param dummyGenerator dummy generator, can be used to generate plural dummies
   */
  public LatexCommandSignature(String commandPrototype, Action action,
        DummyGenerator dummyGenerator) {
    this.dummyGenerator = dummyGenerator;
    this.thisCommandPrototype = commandPrototype;
    Matcher commandMatcher = commandPattern.matcher(commandPrototype);
    boolean found = commandMatcher.find();
    @Nullable String name = (found ? commandMatcher.group(1) : null);

    if (name == null) {
      Tools.logger.warning(Tools.i18n("invalidCommandPrototype", commandPrototype));
      this.thisCommandPattern = Pattern.compile(" ^$");
      return;
    }

    this.name = name;
    int pos = commandMatcher.end(1);

    while (true) {
      Matcher argumentMatcher = argumentPattern.matcher(commandPrototype.substring(pos));
      if (!argumentMatcher.find()) break;

      LatexCommandSignature.ArgumentType argumentType;

      if (argumentMatcher.group(2) != null) {
        argumentType = LatexCommandSignature.ArgumentType.BRACE;
      } else if (argumentMatcher.group(3) != null) {
        argumentType = LatexCommandSignature.ArgumentType.BRACKET;
      } else if (argumentMatcher.group(4) != null) {
        argumentType = LatexCommandSignature.ArgumentType.PARENTHESIS;
      } else {
        argumentType = LatexCommandSignature.ArgumentType.BRACE;
      }

      this.argumentTypes.add(argumentType);
      pos += argumentMatcher.group().length();
      assert argumentMatcher.group().length() > 0;
    }

    this.action = action;
    this.thisCommandPattern = Pattern.compile("^" + Pattern.quote(this.name));
  }

  private static String matchPatternFromPosition(String code, int fromPos, Pattern pattern) {
    Matcher matcher = pattern.matcher(code.substring(fromPos));
    return (matcher.find() ? matcher.group() : "");
  }

  /**
   * Try to match a specific argument type against the given code, starting from the given position.
   *
   * @param code LaTeX code to match against
   * @param fromPos from position to start matching (inclusive, should be the position of the
   *                opening brace/bracket/parenthesis)
   * @param argumentType type of the argument to match
   * @return matched argument including braces/brackets/parenthesis;
   *         empty string if matching failed
   */
  public static String matchArgumentFromPosition(
        String code, int fromPos, ArgumentType argumentType) {
    int pos = fromPos;
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
      case PARENTHESIS: {
        openChar = '(';
        break;
      }
      default: {
        openChar = '{';
        break;
      }
    }

    if (code.charAt(pos) != openChar) return "";
    pos++;

    Stack<ArgumentType> argumentTypeStack = new Stack<>();
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
            return code.substring(fromPos, pos + 1);
          } else {
            argumentTypeStack.pop();
          }

          break;
        }
        case ']': {
          if (argumentTypeStack.peek() != ArgumentType.BRACKET) {
            return "";
          } else if (argumentTypeStack.size() == 1) {
            return code.substring(fromPos, pos + 1);
          } else {
            argumentTypeStack.pop();
          }

          break;
        }
        case ')': {
          if ((argumentTypeStack.peek() == ArgumentType.PARENTHESIS)
                && (argumentTypeStack.size() == 1)) {
            return code.substring(fromPos, pos + 1);
          }

          break;
        }
        default: {
          break;
        }
      }

      pos++;
    }

    return "";
  }

  /**
   * Try to match the LaTeX command against the given code, starting from the given position.
   *
   * @param code LaTeX code to match against
   * @param fromPos from position to start matching (inclusive, should be the position of the
   *                backslash)
   * @return list of from/to position pairs of the arguments (inclusive/exclusive);
   *         @c null if matching failed
   */
  public @Nullable List<Pair<Integer, Integer>> matchArgumentsFromPosition(
        String code, int fromPos) {
    List<Pair<Integer, Integer>> arguments = new ArrayList<>();
    int toPos = matchFromPosition(code, fromPos, arguments);
    return ((toPos > -1) ? arguments : null);
  }

  public String matchFromPosition(String code, int fromPos) {
    int toPos = matchFromPosition(code, fromPos, null);
    return ((toPos > -1) ? code.substring(fromPos, toPos) : "");
  }

  private int matchFromPosition(String code, int fromPos,
        @Nullable List<Pair<Integer, Integer>> arguments) {
    int pos = fromPos;
    String match = matchPatternFromPosition(code, pos, this.thisCommandPattern);
    if (match.isEmpty()) return -1;
    pos += match.length();

    for (ArgumentType argumentType : this.argumentTypes) {
      match = matchPatternFromPosition(code, pos, commentPattern);
      pos += match.length();

      match = matchArgumentFromPosition(code, pos, argumentType);
      if (match.isEmpty()) return -1;
      if (arguments != null) arguments.add(new Pair<>(pos, pos + match.length()));
      pos += match.length();
    }

    return pos;
  }

  public String getName() {
    return this.name;
  }

  public String getCommandPrototype() {
    return this.thisCommandPrototype;
  }
}
