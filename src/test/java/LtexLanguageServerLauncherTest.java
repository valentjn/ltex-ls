import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.*;

import org.checkerframework.checker.nullness.NullnessUtil;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class LtexLanguageServerLauncherTest {
  private @MonotonicNonNull Thread launcherThread;
  private PipedOutputStream out = new PipedOutputStream();
  private PipedInputStream in = new PipedInputStream();
  private PipedInputStream pipedInputStream = new PipedInputStream();
  private PipedOutputStream pipedOutputStream = new PipedOutputStream();

  @BeforeAll
  public void setUp() throws InterruptedException, IOException {
    pipedInputStream.connect(out);
    pipedOutputStream.connect(in);

    launcherThread = new Thread(() -> {
      Assertions.assertDoesNotThrow(() -> LtexLanguageServerLauncher.launch(in, out));
    });
    launcherThread.start();

    // wait until LtexLanguageServer has initialized itself
    Thread.sleep(5000);
  }

  @AfterAll
  public void tearDown() throws IOException {
    if (launcherThread != null) launcherThread.interrupt();
    pipedInputStream.close();
    pipedOutputStream.close();
    out.close();
    in.close();
  }

  private static class Message {
    public enum Source {
      Client,
      Server,
    };

    public enum Type {
      Notification,
      Request,
      Response,
    };

    public Source source;
    public JsonObject body;

    private static Pattern logPattern = Pattern.compile(
        "\\[[^\\]]+\\] (\\S+) (\\S+) '([^' ]+)(?: - \\(([^\\)]+)\\))?'.*\\R" +
        "(?:Params|Result): ");
    private static Pattern headerPattern = Pattern.compile("(\\S+): (.*)\r\n");

    public Message(Source source, Type type, @Nullable String id, String method,
          JsonElement params) {
      this.source = source;
      body = new JsonObject();
      body.addProperty("jsonrpc", "2.0");

      if (type == Type.Notification) {
        body.addProperty("method", method);
        body.add("params", params);
      } else if (type == Type.Request) {
        if (id != null) body.addProperty("id", id);
        body.addProperty("method", method);
        body.add("params", params);
      } else if (type == Type.Response) {
        if (id != null) body.addProperty("id", id);
        body.add("result", params);
      }
    }

    public static Message fromLogString(String str) {
      str = str.trim();
      Matcher matcher = logPattern.matcher(str);
      Assertions.assertTrue(matcher.find());

      String sourceStr = matcher.group(1);
      Assertions.assertNotNull(NullnessUtil.castNonNull(sourceStr));
      Source source;

      if (sourceStr.equals("Sending")) {
        source = Source.Client;
      } else if (sourceStr.equals("Received")) {
        source = Source.Server;
      } else {
        throw new AssertionError("unknown source '" + sourceStr + "'");
      }

      String typeStr = matcher.group(2);
      Assertions.assertNotNull(NullnessUtil.castNonNull(typeStr));
      Type type;

      if (typeStr.equals("notification")) {
        type = Type.Notification;
      } else if (typeStr.equals("request")) {
        type = Type.Request;
      } else if (typeStr.equals("response")) {
        type = Type.Response;
      } else {
        throw new AssertionError("unknown type '" + typeStr + "'");
      }

      String method = matcher.group(3);
      Assertions.assertNotNull(NullnessUtil.castNonNull(method));
      String id = matcher.group(4);
      String paramsStr = str.substring(matcher.end());
      JsonElement params = JsonParser.parseString(paramsStr);

      return new Message(source, type, id, method, params);
    }

    public void sendToServer(OutputStream outputStream) throws IOException {
      String bodyStr = body.toString();
      byte[] bodyBytes = bodyStr.getBytes("utf-8");
      String headerStr = "Content-Length: " + bodyBytes.length + "\r\n\r\n";
      byte[] headerBytes = headerStr.getBytes("ascii");

      outputStream.write(headerBytes);
      outputStream.write(bodyBytes);
      outputStream.flush();
    }

    private static byte[] read(InputStream inputStream, int numberOfBytes) throws IOException {
      byte[] buffer = new byte[numberOfBytes];
      int offset = 0;

      while (offset < numberOfBytes) {
        int numberOfBytesRead = inputStream.read(buffer, offset, numberOfBytes - offset);
        Assertions.assertTrue(numberOfBytesRead >= 1);
        offset += numberOfBytesRead;
      }

      return buffer;
    }

    private static byte[] readLine(InputStream inputStream, int bufferSize) throws IOException {
      byte[] buffer = new byte[bufferSize];
      int offset = 0;

      while ((offset < 2) || (buffer[offset - 2] != 13) || (buffer[offset - 1] != 10)) {
        int numberOfBytesRead = inputStream.read(buffer, offset, 1);
        Assertions.assertTrue(numberOfBytesRead >= 1);
        offset += numberOfBytesRead;
        Assertions.assertTrue(offset < bufferSize);
      }

      byte[] result = new byte[offset];

      for (int i = 0; i < offset; i++) {
        result[i] = buffer[i];
      }

      return result;
    }

    public void waitForServer(InputStream inputStream) throws IOException {
      int contentLength = -1;

      while (true) {
        String headerLine = new String(readLine(inputStream, 1024), "ascii");
        if (headerLine.equals("\r\n")) break;

        Matcher matcher = headerPattern.matcher(headerLine);
        Assertions.assertTrue(matcher.matches());
        String headerName = matcher.group(1);
        Assertions.assertNotNull(NullnessUtil.castNonNull(headerName));
        String headerValue = matcher.group(2);
        Assertions.assertNotNull(NullnessUtil.castNonNull(headerValue));

        if (headerName.equals("Content-Length")) {
          contentLength = Integer.parseInt(headerValue);
        }
      }

      Assertions.assertTrue(contentLength >= 0);
      byte[] bodyBytes = read(inputStream, contentLength);
      JsonElement bodyJson = JsonParser.parseString(new String(bodyBytes, "utf-8"));
      Assertions.assertEquals(body, bodyJson);
    }
  }

  private static List<Message> convertLogToMessages(String log) {
    List<Message> messages = new ArrayList<>();
    log = log.trim();

    for (String logMessage : log.split("\r\n\r\n|\n\n")) {
      messages.add(Message.fromLogString(logMessage));
    }

    return messages;
  }

  @Test
  public void doTest() throws IOException {
    Path path = Paths.get("src", "test", "resources", "LtexLanguageServerTestLog.txt");
    String log = new String(Files.readAllBytes(path), "utf-8");
    List<Message> messages = convertLogToMessages(log);

    for (Message message : messages) {
      if (message.source == Message.Source.Client) {
        message.sendToServer(pipedOutputStream);
      } else if (message.source == Message.Source.Server) {
        message.waitForServer(pipedInputStream);
      }
    }
  }

  @Test
  public void testVersion() {
    Assertions.assertDoesNotThrow(() -> LtexLanguageServerLauncher.main(new String[]{"--version"}));
  }
}
