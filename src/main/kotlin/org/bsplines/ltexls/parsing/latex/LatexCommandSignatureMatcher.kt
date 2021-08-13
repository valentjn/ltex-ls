/* Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.latex

class LatexCommandSignatureMatcher(
  val commandSignatures: List<LatexCommandSignature>,
  escapeCommandPrefixes: Boolean = true,
) {
  private val commandRegex: Regex
  private var code: String? = null
  private var matchResultIterator: Iterator<MatchResult>? = null
  private var ignoreCommandPrototypes: Set<String>? = null

  init {
    val commandPatternStringBuilder = StringBuilder()
    var first = true

    for (commandSignature in commandSignatures) {
      if (first) {
        first = false
      } else {
        commandPatternStringBuilder.append("|")
      }

      val commandPrefix: String = commandSignature.prefix
      commandPatternStringBuilder.append(
        if (escapeCommandPrefixes) Regex.escape(commandPrefix) else commandPrefix
      )
    }

    commandRegex = Regex(commandPatternStringBuilder.toString())
  }

  fun startMatching(code: String, ignoreCommandPrototypes: Set<String>) {
    this.code = code
    this.matchResultIterator = this.commandRegex.findAll(code).iterator()
    this.ignoreCommandPrototypes = ignoreCommandPrototypes
  }

  fun findNextMatch(): LatexCommandSignatureMatch? {
    val code: String? = this.code
    val matchResultIterator: Iterator<MatchResult>? = this.matchResultIterator
    val ignoreCommandPrototypes: Set<String>? = this.ignoreCommandPrototypes

    if ((code == null) || (matchResultIterator == null) || (ignoreCommandPrototypes == null)) {
      return null
    }

    while (matchResultIterator.hasNext()) {
      val matchResult: MatchResult = matchResultIterator.next()
      val fromPos: Int = matchResult.range.first

      val lineStartPos: Int = code.lastIndexOf('\n', fromPos) + 1
      val precedingPartOfLine: String = code.substring(lineStartPos, fromPos)
      if (COMMENT_REGEX.find(precedingPartOfLine) != null) continue

      val bestMatch: LatexCommandSignatureMatch? =
          findBestMatchingCommandSignature(code, fromPos, ignoreCommandPrototypes)
      if (bestMatch != null) return bestMatch
    }

    return null
  }

  private fun findBestMatchingCommandSignature(
    code: String,
    fromPos: Int,
    ignoreCommandPrototypes: Set<String>,
  ): LatexCommandSignatureMatch? {
    var bestMatch: LatexCommandSignatureMatch? = null

    for (commandSignature in commandSignatures) {
      if (ignoreCommandPrototypes.contains(commandSignature.commandPrototype)) continue
      val arguments: List<Pair<Int, Int>>? =
        commandSignature.matchArgumentsFromPosition(code, fromPos)

      if (arguments != null) {
        val match = LatexCommandSignatureMatch(commandSignature, code, fromPos, arguments)
        if ((bestMatch == null) || (match.toPos > bestMatch.toPos)) bestMatch = match
      }
    }

    return bestMatch
  }

  companion object {
    private val COMMENT_REGEX = Regex("(?<!\\\\)(?:\\\\\\\\)*%")
  }
}
