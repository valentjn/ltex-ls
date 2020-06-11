import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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
  private PipedOutputStream out = new PipedOutputStream();
  private PipedInputStream in = new PipedInputStream();
  private PipedInputStream pipedInputStream = new PipedInputStream();
  private PipedOutputStream pipedOutputStream = new PipedOutputStream();

  /**
   * Set up test class.
   */
  @BeforeAll
  public void setUp() throws InterruptedException, IOException {
    this.pipedInputStream.connect(this.out);
    this.pipedOutputStream.connect(this.in);

    this.launcherThread = new Thread(() -> {
      Assertions.assertDoesNotThrow(() -> LtexLanguageServerLauncher.launch(this.in, this.out));
    });
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
    this.out.close();
    this.in.close();
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
  public void doTest() throws IOException {
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
