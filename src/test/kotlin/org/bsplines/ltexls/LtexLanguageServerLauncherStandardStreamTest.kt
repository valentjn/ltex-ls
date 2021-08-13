/* Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls

import org.bsplines.ltexls.tools.Tools
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.platform.suite.api.IncludeEngines
import java.io.InputStream
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.concurrent.ExecutionException
import kotlin.test.Test

@IncludeEngines("junit-jupiter")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LtexLanguageServerLauncherStandardStreamTest {
  @Suppress("UnusedPrivateMember")
  private val `in` = PipedInputStream()
  private val out = PipedOutputStream()
  private val pipedInputStream = PipedInputStream()
  private val pipedOutputStream = PipedOutputStream()
  private var launcherThread: Thread? = null

  @BeforeAll
  fun setUp() {
    this.pipedOutputStream.connect(this.`in`)
    this.pipedInputStream.connect(this.out)

    val launcherThread = Thread(LtexLanguageServerLauncherRunnable(this.`in`, this.out))
    launcherThread.start()
    this.launcherThread = launcherThread

    // wait until LtexLanguageServer has initialized itself
    Thread.sleep(5000)
  }

  @AfterAll
  fun tearDown() {
    this.launcherThread?.interrupt()
    this.pipedInputStream.close()
    this.pipedOutputStream.close()
    this.`in`.close()
    this.out.close()
  }

  @Test
  fun test() {
    LspMessage.communicateWithList(
      LspMessage.fromLogFile(),
      this.pipedInputStream,
      this.pipedOutputStream,
    )
  }

  private class LtexLanguageServerLauncherRunnable(
    @Suppress("UnusedPrivateMember")
    private val `in`: InputStream,
    private val out: OutputStream,
  ) : Runnable {
    @Suppress("TooGenericExceptionThrown")
    override fun run() {
      try {
        Tools.randomNumberGenerator.setSeed(42)
        LtexLanguageServerLauncher.launch(this.`in`, this.out)
      } catch (e: InterruptedException) {
        // occurs when JUnit tears down class
      } catch (e: ExecutionException) {
        throw RuntimeException("ExecutionException thrown", e)
      }
    }
  }
}
