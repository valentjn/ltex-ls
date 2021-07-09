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
import java.util.concurrent.ExecutionException;
import org.bsplines.ltexls.tools.Tools;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
@DefaultQualifier(NonNull.class)
public class LtexLanguageServerLauncherStandardStreamTest {
  private PipedInputStream in;
  private PipedOutputStream out;
  private PipedInputStream pipedInputStream;
  private PipedOutputStream pipedOutputStream;
  private @MonotonicNonNull Thread launcherThread;

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
        Tools.randomNumberGenerator.setSeed(42);
        LtexLanguageServerLauncher.launch(this.in, this.out);
      } catch (InterruptedException e) {
        // occurs when JUnit tears down class
      } catch (ExecutionException e) {
        throw new RuntimeException("ExecutionException thrown", e);
      }
    }
  }

  public LtexLanguageServerLauncherStandardStreamTest() {
    this.in = new PipedInputStream();
    this.out = new PipedOutputStream();
    this.pipedInputStream = new PipedInputStream();
    this.pipedOutputStream = new PipedOutputStream();
  }

  @BeforeAll
  public void setUp() throws InterruptedException, IOException {
    this.pipedOutputStream.connect(this.in);
    this.pipedInputStream.connect(this.out);

    this.launcherThread = new Thread(new LtexLanguageServerLauncherRunnable(this.in, this.out));
    this.launcherThread.start();

    // wait until LtexLanguageServer has initialized itself
    Thread.sleep(5000);
  }

  @AfterAll
  public void tearDown() throws IOException {
    if (this.launcherThread != null) this.launcherThread.interrupt();
    this.pipedInputStream.close();
    this.pipedOutputStream.close();
    this.in.close();
    this.out.close();
  }

  @Test
  public void test() throws IOException, InterruptedException {
    LspMessage.communicateWithList(LspMessage.fromLogFile(),
        this.pipedInputStream, this.pipedOutputStream);
  }
}
