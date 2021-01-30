/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.junit.jupiter.api.Assertions;

@DefaultQualifier(NonNull.class)
public class LspMessage {
  public enum Source {
    Client,
    Server,
  }

  public enum Type {
    Notification,
    Request,
    Response,
  }

  private Source source;
  private JsonObject body;

  private static final Pattern logPattern = Pattern.compile(
      "\\[[^\\]]+\\] (\\S+) (\\S+) '([^' ]+)(?: - \\(([^\\)]+)\\))?'.*\\R(?:Params|Result):");
  private static final Pattern headerPattern = Pattern.compile("(\\S+): (.*)\r\n");

  public LspMessage(Source source, Type type, @Nullable String id, String method,
        JsonElement params) {
    this.source = source;
    this.body = new JsonObject();
    this.body.addProperty("jsonrpc", "2.0");

    if (type == Type.Notification) {
      this.body.addProperty("method", method);
      this.body.add("params", params);
    } else if (type == Type.Request) {
      if (id != null) this.body.addProperty("id", id);
      this.body.addProperty("method", method);
      this.body.add("params", params);
    } else if (type == Type.Response) {
      if (id != null) this.body.addProperty("id", id);
      this.body.add("result", params);
    }
  }

  public static LspMessage fromLogString(String str) {
    str = str.trim();
    Matcher matcher = logPattern.matcher(str);
    Assertions.assertTrue(matcher.find());

    @Nullable String sourceStr = matcher.group(1);
    if (sourceStr == null) throw new AssertionError("could not find source in '" + str + "'");
    Source source;

    if (sourceStr.equals("Sending")) {
      source = Source.Client;
    } else if (sourceStr.equals("Received")) {
      source = Source.Server;
    } else {
      throw new AssertionError("unknown source '" + sourceStr + "'");
    }

    @Nullable String typeStr = matcher.group(2);
    if (typeStr == null) throw new AssertionError("could not find type in '" + str + "'");
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

    @Nullable String method = matcher.group(3);
    if (method == null) throw new AssertionError("could not find method in '" + str + "'");
    @Nullable String id = matcher.group(4);
    String paramsStr = str.substring(matcher.end());
    JsonElement params = JsonParser.parseString(paramsStr);

    return new LspMessage(source, type, id, method, params);
  }

  public void sendToServer(OutputStream outputStream) throws IOException, InterruptedException {
    String bodyStr = this.body.toString();
    byte[] bodyBytes = bodyStr.getBytes("utf-8");
    String headerStr = "Content-Length: " + bodyBytes.length + "\r\n\r\n";
    byte[] headerBytes = headerStr.getBytes("ascii");

    Thread.sleep(100);
    outputStream.write(headerBytes);
    outputStream.write(bodyBytes);
    outputStream.flush();
    Thread.sleep(100);
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
      @Nullable String headerName = matcher.group(1);

      if (headerName == null)  {
        throw new AssertionError("could not find header name in '" + headerLine + "'");
      }

      @Nullable String headerValue = matcher.group(2);

      if (headerValue == null)  {
        throw new AssertionError("could not find header value in '" + headerLine + "'");
      }

      if (headerName.equals("Content-Length")) {
        contentLength = Integer.parseInt(headerValue);
      }
    }

    Assertions.assertTrue(contentLength >= 0);
    byte[] bodyBytes = read(inputStream, contentLength);
    JsonElement bodyJson = JsonParser.parseString(new String(bodyBytes, "utf-8"));
    Assertions.assertEquals(this.body, bodyJson);
  }

  public Source getSource() {
    return this.source;
  }
}
