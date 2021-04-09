/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
@DefaultQualifier(NonNull.class)
public class LtexLanguageServerLauncherTest {
  private static String captureStdout(Callable<Integer> callable) throws Exception {
    PrintStream stdout = System.out;
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Charset charset = StandardCharsets.UTF_8;

    try (PrintStream printStream = new PrintStream(outputStream, true, charset.name())) {
      try {
        System.setOut(printStream);
        callable.call();
      } finally {
        System.setOut(stdout);
      }
    }

    return new String(outputStream.toByteArray(), charset);
  }

  @Test
  public void testHelp() throws Exception {
    String output = captureStdout(() -> LtexLanguageServerLauncher.mainWithoutExit(
        new String[]{"--help"}));
    Assertions.assertTrue(output.contains("Usage: ltex-ls"));
    Assertions.assertTrue(output.contains("LTeX LS - LTeX Language Server"));
    Assertions.assertTrue(output.contains("--help"));
    Assertions.assertTrue(output.contains("Show this help message and exit."));
  }

  @Test
  public void testVersion() throws Exception {
    String output = captureStdout(() -> LtexLanguageServerLauncher.mainWithoutExit(
        new String[]{"--version"}));

    JsonElement rootJsonElement = JsonParser.parseString(output);
    Assertions.assertTrue(rootJsonElement.isJsonObject());

    JsonObject rootJsonObject = rootJsonElement.getAsJsonObject();
    Assertions.assertTrue(rootJsonObject.has("java"));

    JsonElement javaJsonElement = rootJsonObject.get("java");
    Assertions.assertTrue(javaJsonElement.isJsonPrimitive());
  }
}
