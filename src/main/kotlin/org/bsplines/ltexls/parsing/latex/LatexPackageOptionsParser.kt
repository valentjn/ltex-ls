/* Copyright (C) 2019-2023 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.latex

object LatexPackageOptionsParser {
  private val WHITESPACE_REGEX = Regex("^[ \n\r\t]+(%.*?($|(\r?\n[ \n\r\t]*)))?")
  private val COMMENT_REGEX = Regex("^%.*?($|(\r?\n[ \n\r\t]*))")

  private fun matchFromPosition(regex: Regex, string: String, pos: Int): String {
    val matchResult: MatchResult? = regex.find(string.substring(pos))
    return matchResult?.value ?: ""
  }

  private fun appendSpace(builder: StringBuilder) {
    val length = builder.length
    if ((length == 0) || (builder[length - 1] != ' ')) builder.append(" ")
  }

  @Suppress("ComplexMethod", "LongMethod", "NestedBlockDepth")
  fun parse(optionsString: String): List<LatexPackageOption> {
    val options = ArrayList<LatexPackageOption>()
    var mode = Mode.Key
    var groupDepth = 0
    var keyFromPos = 0
    var keyToPos = -1
    val keyBuilder = StringBuilder()
    var valueFromPos = -1
    var valueToPos = -1
    val valueBuilder = StringBuilder()
    var pos = 0

    while (pos < optionsString.length) {
      val oldPos = pos

      when (val curChar = optionsString[pos]) {
        '{' -> {
          groupDepth++
          pos++
        }
        '}' -> {
          groupDepth--
          pos++
        }
        ' ', '\n', '\r', '\t' -> {
          val whitespace = matchFromPosition(WHITESPACE_REGEX, optionsString, pos)

          if (mode == Mode.Key) {
            appendSpace(keyBuilder)
          } else if (mode == Mode.Value) {
            appendSpace(valueBuilder)
          }

          pos += whitespace.length
        }
        else -> {
          var addCurCharToOption = true

          if (curChar == '%') {
            val comment = matchFromPosition(COMMENT_REGEX, optionsString, pos)

            if (comment.isNotEmpty()) {
              pos += comment.length
              addCurCharToOption = false
            }
          } else if ((curChar == ',') && (groupDepth == 0)) {
            if (mode == Mode.Key) {
              keyToPos = pos
            } else if (mode == Mode.Value) {
              valueToPos = pos
            }

            options.add(
              LatexPackageOption(
                optionsString,
                LatexPackageOption.KeyValueInfo(
                  keyFromPos,
                  keyToPos,
                  keyBuilder.toString().trim { it <= ' ' },
                ),
                LatexPackageOption.KeyValueInfo(
                  valueFromPos,
                  valueToPos,
                  valueBuilder.toString().trim { it <= ' ' },
                ),
              ),
            )

            mode = Mode.Key
            keyFromPos = pos + 1
            keyToPos = -1
            keyBuilder.setLength(0)
            valueFromPos = -1
            valueToPos = -1
            valueBuilder.setLength(0)
            addCurCharToOption = false
          } else if ((curChar == '\\') && (pos < optionsString.length - 1)) {
            when (val nextChar = optionsString[pos + 1]) {
              '%', ',', '\\', '{', '}', ' ' -> {
                if (mode == Mode.Key) {
                  keyBuilder.append(curChar)
                  keyBuilder.append(nextChar)
                } else if (mode == Mode.Value) {
                  valueBuilder.append(curChar)
                  valueBuilder.append(nextChar)
                }

                pos += 2
                addCurCharToOption = false
              }
            }
          } else if ((curChar == '=') && (mode == Mode.Key)) {
            mode = Mode.Value
            keyToPos = pos
            valueFromPos = pos + 1
            addCurCharToOption = false
          }

          if (addCurCharToOption) {
            when (mode) {
              Mode.Key -> keyBuilder.append(curChar)
              Mode.Value -> valueBuilder.append(curChar)
            }

            pos++
          }
        }
      }

      if (pos == oldPos) pos++
    }

    if (keyFromPos < optionsString.length) {
      if (mode == Mode.Key) {
        keyToPos = optionsString.length
      } else if (mode == Mode.Value) {
        valueToPos = optionsString.length
      }

      options.add(
        LatexPackageOption(
          optionsString,
          LatexPackageOption.KeyValueInfo(
            keyFromPos,
            keyToPos,
            keyBuilder.toString().trim { it <= ' ' },
          ),
          LatexPackageOption.KeyValueInfo(
            valueFromPos,
            valueToPos,
            valueBuilder.toString().trim { it <= ' ' },
          ),
        ),
      )
    }

    return options
  }

  private enum class Mode {
    Key,
    Value,
  }
}
