/* Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.latex

import org.bsplines.ltexls.parsing.DummyGenerator
import org.bsplines.ltexls.tools.I18n
import org.bsplines.ltexls.tools.Logging

open class LatexCommandSignature(
  val commandPrototype: String,
  val action: Action = Action.Ignore,
  val dummyGenerator: DummyGenerator = DummyGenerator.getInstance(),
  escapeCommandPrefix: Boolean = true
) {
  var prefix = ""
  val argumentTypes: List<ArgumentType>
  private var commandRegex: Regex

  init {
    val commandMatchGroup: MatchGroup? =
        GENERIC_COMMAND_REGEX.find(commandPrototype)?.groups?.get(1)

    if (commandMatchGroup != null) {
      this.prefix = commandMatchGroup.value
      val argumentTypes = ArrayList<ArgumentType>()
      var pos: Int = commandMatchGroup.range.last + 1

      while (true) {
        val argumentMatchResult: MatchResult =
            ARGUMENT_REGEX.find(commandPrototype.substring(pos)) ?: break

        val argumentType: ArgumentType = when {
          argumentMatchResult.groups["brace"] != null -> ArgumentType.Brace
          argumentMatchResult.groups["bracket"] != null -> ArgumentType.Bracket
          argumentMatchResult.groups["parenthesis"] != null -> ArgumentType.Parenthesis
          else -> ArgumentType.Brace
        }

        argumentTypes.add(argumentType)
        pos += argumentMatchResult.value.length
        assert(argumentMatchResult.value.isNotEmpty())
      }

      this.argumentTypes = argumentTypes
      this.commandRegex = Regex(
        "^" + (if (escapeCommandPrefix) Regex.escape(this.prefix) else this.prefix)
      )
    } else {
      Logging.logger.warning(I18n.format("invalidCommandPrototype", commandPrototype))
      this.argumentTypes = ArrayList()
      @Suppress("RegExpUnexpectedAnchor")
      this.commandRegex = Regex(" ^$")
    }
  }

  fun matchArgumentsFromPosition(code: String, fromPos: Int): List<Pair<Int, Int>>? {
    val arguments = ArrayList<Pair<Int, Int>>()
    val toPos: Int = matchFromPosition(code, fromPos, arguments)
    return if (toPos > -1) arguments else null
  }

  fun matchFromPosition(code: String, fromPos: Int): String {
    val toPos: Int = matchFromPosition(code, fromPos, null)
    return if (toPos > -1) code.substring(fromPos, toPos) else ""
  }

  private fun matchFromPosition(
    code: String,
    fromPos: Int,
    arguments: MutableList<Pair<Int, Int>>?,
  ): Int {
    var pos: Int = fromPos
    var match: String = matchPatternFromPosition(code, pos, this.commandRegex)
    if (match.isEmpty()) return -1
    pos += match.length

    for (argumentType in argumentTypes) {
      match = matchPatternFromPosition(code, pos, COMMENT_REGEX)
      pos += match.length
      match = matchArgumentFromPosition(code, pos, argumentType)
      if (match.isEmpty()) return -1
      arguments?.add(Pair(pos, pos + match.length))
      pos += match.length
    }

    return pos
  }

  enum class ArgumentType {
    Brace,
    Bracket,
    Parenthesis,
  }

  enum class Action {
    Default,
    Ignore,
    Dummy,
  }

  companion object {
    private val GENERIC_COMMAND_REGEX = Regex("^(.+?)(\\{}|\\[]|\\(\\))*$")
    private val ARGUMENT_REGEX =
        Regex("^(?:(?<brace>\\{})|(?<bracket>\\[])|(?<parenthesis>\\(\\)))")
    private val COMMENT_REGEX = Regex("^%.*?($|(\n[ \n\r\t]*))")

    private fun matchPatternFromPosition(code: String, fromPos: Int, regex: Regex): String {
      if (fromPos >= code.length) return ""
      val matchResult: MatchResult? = regex.find(code.substring(fromPos))
      return matchResult?.value ?: ""
    }

    @Suppress("ComplexMethod", "NestedBlockDepth")
    fun matchArgumentFromPosition(code: String, fromPos: Int, argumentType: ArgumentType): String {
      if (fromPos >= code.length) return ""
      var pos: Int = fromPos
      val openChar: Char = when (argumentType) {
        ArgumentType.Brace -> '{'
        ArgumentType.Bracket -> '['
        ArgumentType.Parenthesis -> '('
      }

      if (code[pos] != openChar) return ""
      pos++
      val argumentTypeStack = ArrayDeque<ArgumentType>()
      argumentTypeStack.addLast(argumentType)

      while (pos < code.length) {
        when (val curChar: Char = code[pos]) {
          '\\' -> if (pos + 1 < code.length) pos++
          '{' -> argumentTypeStack.addLast(ArgumentType.Brace)
          '[' -> argumentTypeStack.addLast(ArgumentType.Bracket)
          '}', ']' -> {
            val curArgumentType: ArgumentType = when (curChar) {
              '}' -> ArgumentType.Brace
              ']' -> ArgumentType.Bracket
              else -> ArgumentType.Brace
            }

            when {
              argumentTypeStack.isNotEmpty() && (argumentTypeStack.last() != curArgumentType) -> {
                return ""
              }
              (argumentTypeStack.size == 1) -> return code.substring(fromPos, pos + 1)
              else -> argumentTypeStack.removeLastOrNull()
            }
          }
          ')' -> {
            if (
              (argumentTypeStack.size == 1)
              && (argumentTypeStack.last() == ArgumentType.Parenthesis)
            ) {
              return code.substring(fromPos, pos + 1)
            }
          }
          else -> {}
        }

        pos++
      }

      return ""
    }
  }
}
