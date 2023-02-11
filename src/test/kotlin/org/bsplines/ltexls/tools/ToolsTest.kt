/* Copyright (C) 2019-2023 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.tools

import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import kotlin.test.Test
import kotlin.test.assertFailsWith

class ToolsTest {
  @Test
  fun testRethrowCancellationException() {
    val cancellationException: Throwable = CancellationException()
    val executionException: Throwable = ExecutionException("abc", cancellationException)
    val runtimeException: Throwable = RuntimeException()

    assertFailsWith(CancellationException::class) {
      Tools.rethrowCancellationException(cancellationException)
    }

    assertFailsWith(CancellationException::class) {
      Tools.rethrowCancellationException(executionException)
    }

    Tools.rethrowCancellationException(runtimeException)
  }
}
