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
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.net.UnknownHostException
import java.util.concurrent.ExecutionException
import kotlin.test.Test
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LtexLanguageServerLauncherTcpSocketTest {
  private var socket: Socket? = null
  private var inputStream: InputStream? = null
  private var outputStream: OutputStream? = null
  private var launcherThread: Thread? = null

  private class LtexLanguageServerLauncherRunnable : Runnable {
    @Suppress("TooGenericExceptionThrown")
    override fun run() {
      try {
        Tools.randomNumberGenerator.setSeed(42)
        val exitCode: Int = LtexLanguageServerLauncher.mainWithoutExit(
          arrayOf("--server-type=tcpSocket", "--host=$HOST", "--port=$PORT")
        )

        assertEquals(0, exitCode)
      } catch (e: InterruptedException) {
        // occurs when JUnit tears down class
      } catch (e: ExecutionException) {
        throw RuntimeException("ExecutionException thrown", e)
      } catch (e: UnknownHostException) {
        throw RuntimeException("UnknownHostException thrown", e)
      } catch (e: IOException) {
        throw RuntimeException("IOException thrown", e)
      }
    }
  }

  @BeforeAll
  fun setUp() {
    val launcherThread = Thread(LtexLanguageServerLauncherRunnable())
    launcherThread.start()
    this.launcherThread = launcherThread

    // wait until server is listening for connections
    Thread.sleep(2000)
    val socket = Socket(HOST, PORT)
    this.inputStream = socket.getInputStream()
    this.outputStream = socket.getOutputStream()
    this.socket = socket

    // wait until LtexLanguageServer has initialized itself
    Thread.sleep(5000)
  }

  @AfterAll
  fun tearDown() {
    socket?.close()
    launcherThread?.interrupt()
  }

  @Test
  fun test() {
    LspMessage.communicateWithList(
      LspMessage.fromLogFile(),
      this.inputStream!!,
      this.outputStream!!,
    )
  }

  companion object {
    private const val HOST = "localhost"
    private const val PORT = 52714
  }
}
