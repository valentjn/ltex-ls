/* Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.tools

import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import java.util.Random
import java.util.UUID
import java.util.concurrent.CancellationException

object Tools {
  private const val MAX_EXCEPTION_DEPTH = 100

  val randomNumberGenerator = Random()

  fun areRangesIntersecting(range1: Range, range2: Range): Boolean {
    return !(positionLower(range2.end, range1.start)
        || positionLower(range1.end, range2.start))
  }

  fun positionLower(position1: Position, position2: Position): Boolean {
    return ((position1.line < position2.line)
        || ((position1.line == position2.line)
        && (position1.character < position2.character)))
  }

  fun getRandomUuid(): String {
    return UUID(randomNumberGenerator.nextLong(), randomNumberGenerator.nextLong()).toString()
  }

  @Suppress("UnusedPrivateMember")
  fun getRootCauseOfThrowable(throwable: Throwable?): Throwable? {
    if (throwable == null) return null
    val ancestors = HashSet<Throwable>()
    var cause: Throwable? = throwable
    ancestors.add(throwable)

    for (i in 0 until MAX_EXCEPTION_DEPTH) {
      val newCause: Throwable? = cause?.cause
      if ((newCause == null) || ancestors.contains(newCause)) break
      cause = newCause
      ancestors.add(newCause)
    }

    return cause
  }

  fun rethrowCancellationException(throwable: Throwable) {
    val rootCause: Throwable? = getRootCauseOfThrowable(throwable)
    if ((rootCause != null) && (rootCause is CancellationException)) throw rootCause
  }
}
