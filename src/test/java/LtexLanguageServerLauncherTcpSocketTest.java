/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import org.bsplines.ltexls.tools.Tools;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.util.NullnessUtil;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
@DefaultQualifier(NonNull.class)
public class LtexLanguageServerLauncherTcpSocketTest {
  private static final String host = "localhost";
  private static final int port = 52714;

  private @MonotonicNonNull Socket socket;
  private @MonotonicNonNull InputStream inputStream;
  private @MonotonicNonNull OutputStream outputStream;
  private @MonotonicNonNull Thread launcherThread;

  private static class LtexLanguageServerLauncherRunnable implements Runnable {
    @Override
    public void run() {
      try {
        Tools.randomNumberGenerator.setSeed(42);
        int exitCode = LtexLanguageServerLauncher.mainWithoutExit(new String[]{
            "--server-type=tcpSocket", "--host=" + host, "--port=" + port});
        Assertions.assertEquals(0, exitCode);
      } catch (InterruptedException e) {
        // occurs when JUnit tears down class
      } catch (ExecutionException e) {
        throw new RuntimeException("ExecutionException thrown", e);
      } catch (UnknownHostException e) {
        throw new RuntimeException("UnknownHostException thrown", e);
      } catch (IOException e) {
        throw new RuntimeException("IOException thrown", e);
      }
    }
  }

  @BeforeAll
  public void setUp() throws InterruptedException, IOException {
    this.launcherThread = new Thread(new LtexLanguageServerLauncherRunnable());
    this.launcherThread.start();

    // wait until server is listening for connections
    Thread.sleep(2000);

    this.socket = new Socket(host, port);
    this.inputStream = this.socket.getInputStream();
    this.outputStream = this.socket.getOutputStream();

    // wait until LtexLanguageServer has initialized itself
    Thread.sleep(5000);
  }

  @AfterAll
  public void tearDown() throws IOException {
    if (this.socket != null) this.socket.close();
    if (this.launcherThread != null) this.launcherThread.interrupt();
  }

  @Test
  public void test() throws IOException, InterruptedException {
    Assertions.assertNotNull(NullnessUtil.castNonNull(this.inputStream));
    Assertions.assertNotNull(NullnessUtil.castNonNull(this.outputStream));
    LspMessage.communicateWithList(LspMessage.fromLogFile(),
        NullnessUtil.castNonNull(this.inputStream), NullnessUtil.castNonNull(this.outputStream));
  }
}
