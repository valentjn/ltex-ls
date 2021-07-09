/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.latex;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LatexPackageOptionsParser {
  private enum Mode {
    KEY,
    VALUE,
  }

  private static final Pattern whitespacePattern = Pattern.compile(
      "^[ \n\r\t]+(%.*?($|(\r?\n[ \n\r\t]*)))?");
  private static final Pattern commentPattern = Pattern.compile(
      "^%.*?($|(\r?\n[ \n\r\t]*))");

  private LatexPackageOptionsParser() {
  }

  private static String matchFromPosition(Pattern pattern, String string, int pos) {
    Matcher matcher = pattern.matcher(string.substring(pos));
    return (matcher.find() ? matcher.group() : "");
  }

  private static void appendSpace(StringBuilder builder) {
    int length = builder.length();
    if ((length == 0) || (builder.charAt(length - 1) != ' ')) builder.append(" ");
  }

  public static List<LatexPackageOption> parse(String optionsString) {
    List<LatexPackageOption> options = new ArrayList<>();
    Mode mode = Mode.KEY;
    int groupDepth = 0;
    int keyFromPos = 0;
    int keyToPos = -1;
    StringBuilder keyBuilder = new StringBuilder();
    int valueFromPos = -1;
    int valueToPos = -1;
    StringBuilder valueBuilder = new StringBuilder();
    int pos = 0;

    while (pos < optionsString.length()) {
      int oldPos = pos;
      char curChar = optionsString.charAt(pos);

      switch (curChar) {
        case '{': {
          groupDepth++;
          pos++;
          break;
        }
        case '}': {
          groupDepth--;
          pos++;
          break;
        }
        case ' ':
        case '\n':
        case '\r':
        case '\t': {
          String whitespace = matchFromPosition(whitespacePattern, optionsString, pos);

          if (mode == Mode.KEY) {
            appendSpace(keyBuilder);
          } else if (mode == Mode.VALUE) {
            appendSpace(valueBuilder);
          }

          pos += whitespace.length();
          break;
        }
        default: {
          if (curChar == '%') {
            String comment = matchFromPosition(commentPattern, optionsString, pos);

            if (!comment.isEmpty()) {
              pos += comment.length();
              break;
            }
          } else if ((curChar == ',') && (groupDepth == 0)) {
            if (mode == Mode.KEY) {
              keyToPos = pos;
            } else if (mode == Mode.VALUE) {
              valueToPos = pos;
            }

            options.add(new LatexPackageOption(optionsString,
                keyFromPos, keyToPos, keyBuilder.toString().trim(),
                valueFromPos, valueToPos, valueBuilder.toString().trim()));

            mode = Mode.KEY;
            keyFromPos = pos + 1;
            keyToPos = -1;
            keyBuilder.setLength(0);
            valueFromPos = -1;
            valueToPos = -1;
            valueBuilder.setLength(0);
            pos++;
            break;
          } else if ((curChar == '\\') && (pos < optionsString.length() - 1)) {
            char nextChar = optionsString.charAt(pos + 1);

            if ((nextChar == '%') || (nextChar == ',') || (nextChar == '\\') || (nextChar == '{')
                  || (nextChar == '}') || (nextChar == ' ')) {
              if (mode == Mode.KEY) {
                keyBuilder.append(curChar);
                keyBuilder.append(nextChar);
              } else if (mode == Mode.VALUE) {
                valueBuilder.append(curChar);
                valueBuilder.append(nextChar);
              }

              pos += 2;
              break;
            }
          } else if ((curChar == '=') && (mode == Mode.KEY)) {
            mode = Mode.VALUE;
            keyToPos = pos;
            valueFromPos = pos + 1;
            pos++;
            break;
          }

          if (mode == Mode.KEY) {
            keyBuilder.append(curChar);
          } else if (mode == Mode.VALUE) {
            valueBuilder.append(curChar);
          }

          pos++;
          break;
        }
      }

      if (pos == oldPos) pos++;
    }

    if (keyFromPos < optionsString.length()) {
      if (mode == Mode.KEY) {
        keyToPos = optionsString.length();
      } else if (mode == Mode.VALUE) {
        valueToPos = optionsString.length();
      }

      options.add(new LatexPackageOption(optionsString,
          keyFromPos, keyToPos, keyBuilder.toString().trim(),
          valueFromPos, valueToPos, valueBuilder.toString().trim()));
    }

    return options;
  }
}
