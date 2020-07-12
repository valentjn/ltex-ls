/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class LtexLanguageServerLauncherTest {
  private @MonotonicNonNull Thread launcherThread;
  private PipedInputStream in = new PipedInputStream();
  private PipedOutputStream out = new PipedOutputStream();
  private PipedInputStream pipedInputStream = new PipedInputStream();
  private PipedOutputStream pipedOutputStream = new PipedOutputStream();

  private static class LtexLanguageServerLauncherRunnable implements Runnable {
    private InputStream in;
    private OutputStream out;

    public LtexLanguageServerLauncherRunnable(InputStream in, OutputStream out) {
      this.in = in;
      this.out = out;
    }

    @Override
    public void run() {
      try {
        LtexLanguageServerLauncher.launch(this.in, this.out);
      } catch (InterruptedException e) {
        // occurs when JUnit tears down class
      } catch (ExecutionException e) {
        throw new RuntimeException("ExecutionException thrown", e);
      }
    }
  }

  /**
   * Set up test class.
   */
  @BeforeAll
  public void setUp() throws InterruptedException, IOException {
    this.pipedOutputStream.connect(this.in);
    this.pipedInputStream.connect(this.out);

    this.launcherThread = new Thread(
        new LtexLanguageServerLauncherRunnable(this.in, this.out));
    this.launcherThread.start();

    // wait until LtexLanguageServer has initialized itself
    Thread.sleep(5000);
  }

  /**
   * Tear down test class.
   */
  @AfterAll
  public void tearDown() throws IOException {
    if (this.launcherThread != null) this.launcherThread.interrupt();
    this.pipedInputStream.close();
    this.pipedOutputStream.close();
    this.in.close();
    this.out.close();
  }

  private static List<LspMessage> convertLogToMessages(String log) {
    List<LspMessage> messages = new ArrayList<>();
    log = log.trim();

    for (String logMessage : log.split("\r\n\r\n|\n\n")) {
      messages.add(LspMessage.fromLogString(logMessage));
    }

    return messages;
  }

  @Test
  public void doTest() throws IOException, InterruptedException {
    Path path = Paths.get("src", "test", "resources", "LtexLanguageServerTestLog.txt");
    String log = new String(Files.readAllBytes(path), "utf-8");
    List<LspMessage> messages = convertLogToMessages(log);

    for (LspMessage message : messages) {
      if (message.source == LspMessage.Source.Client) {
        message.sendToServer(this.pipedOutputStream);
      } else if (message.source == LspMessage.Source.Server) {
        message.waitForServer(this.pipedInputStream);
      }
    }
  }

  @Test
  public void testVersion() {
    Assertions.assertDoesNotThrow(() -> LtexLanguageServerLauncher.main(new String[]{"--version"}));
  }
}
