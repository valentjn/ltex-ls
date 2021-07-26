/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.latex

class LatexCommandSignatureMatch(
  val commandSignature: LatexCommandSignature,
  private val code: String,
  val fromPos: Int,
  val argumentPos: List<Pair<Int, Int>>,
) {
  val toPos: Int = (if (argumentPos.isEmpty()) fromPos + commandSignature.prefix.length else
      argumentPos[argumentPos.size - 1].second)

  fun getArgumentContents(index: Int): String {
    val argument: Pair<Int, Int> = argumentPos[index]
    val argumentFromPos: Int = argument.first + 1
    val argumentToPos: Int = argument.second - 1
    return this.code.substring(argumentFromPos, argumentToPos)
  }

  fun getArgumentContentsFromPos(index: Int): Int {
    return argumentPos[index].first + 1
  }

  fun getArgumentsSize(): Int {
    return argumentPos.size
  }
}
