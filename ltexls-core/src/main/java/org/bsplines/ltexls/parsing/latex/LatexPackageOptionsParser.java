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
import org.eclipse.xtext.xbase.lib.Pair;

public class LatexPackageOptionsParser {
  private enum Mode {
    KEY,
    VALUE,
  }

  private static final Pattern whitespacePattern = Pattern.compile(
      "^[ \n\r\t]+(%.*?($|((\n|\r|\r\n)[ \n\r\t]*)))?");
  private static final Pattern commentPattern = Pattern.compile(
      "^%.*?($|((\n|\r|\r\n)[ \n\r\t]*))");

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

  public static List<Pair<String, String>> parse(String optionsString) {
    List<Pair<String, String>> options = new ArrayList<>();
    Mode mode = Mode.KEY;
    int groupDepth = 0;
    StringBuilder keyBuilder = new StringBuilder();
    StringBuilder valueBuilder = new StringBuilder();
    int pos = 0;

    while (pos < optionsString.length()) {
      int oldPos = pos;
      char curChar = optionsString.charAt(pos);

      switch (curChar) {
        case '%': {
          String comment = matchFromPosition(commentPattern, optionsString, pos);

          if (!comment.isEmpty()) {
            pos += comment.length();
            break;
          }
        }
        // fall through
        case ',': {
          if (groupDepth == 0) {
            if (keyBuilder.length() != 0) {
              options.add(new Pair<>(
                  keyBuilder.toString().trim(), valueBuilder.toString().trim()));
            }

            keyBuilder.setLength(0);
            valueBuilder.setLength(0);
            pos++;
            break;
          }
        }
        // fall through
        case '\\': {
          if (pos < optionsString.length() - 1) {
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
          }
        }
        // fall through
        case '{': {
          groupDepth++;
          pos++;
          break;
        }
        case '}': {
          groupDepth--;
          if (groupDepth < 0) groupDepth = 0;
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

    if (keyBuilder.length() != 0) {
      options.add(new Pair<>(
          keyBuilder.toString().trim(), valueBuilder.toString().trim()));
    }

    return options;
  }
}
