/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import org.eclipse.xtext.xbase.lib.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LtexLanguageServerLauncherTest {
  public static Pair<Integer, String> captureStdout(
        Callable<Integer> callable) throws Exception {
    PrintStream stdout = System.out;
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Charset charset = StandardCharsets.UTF_8;
    Integer exitCode;

    try (PrintStream printStream = new PrintStream(outputStream, true, charset.name())) {
      System.setOut(printStream);

      try {
        exitCode = callable.call();
      } finally {
        System.setOut(stdout);
      }
    }

    return Pair.of(exitCode, new String(outputStream.toByteArray(), charset));
  }

  public static int mockStdin(String text, Callable<Integer> callable) throws Exception {
    InputStream stdin = System.in;

    try (ByteArrayInputStream inputStream =
          new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8))) {
      System.setIn(inputStream);

      try {
        return callable.call();
      } finally {
        System.setIn(stdin);
      }
    }
  }

  @Test
  public void testHelp() throws Exception {
    Pair<Integer, String> result = captureStdout(
        () -> LtexLanguageServerLauncher.mainWithoutExit(new String[]{"--help"}));
    Assertions.assertEquals(0, result.getKey());
    String output = result.getValue();
    Assertions.assertTrue(output.contains("Usage: ltex-ls"));
    Assertions.assertTrue(output.contains("LTeX LS - LTeX Language Server"));
    Assertions.assertTrue(output.contains("--help"));
    Assertions.assertTrue(output.contains("Show this help message and exit."));
  }

  @Test
  public void testVersion() throws Exception {
    Pair<Integer, String> result = captureStdout(
        () -> LtexLanguageServerLauncher.mainWithoutExit(new String[]{"--version"}));
    Assertions.assertEquals(0, result.getKey());
    String output = result.getValue();

    JsonElement rootJsonElement = JsonParser.parseString(output);
    Assertions.assertTrue(rootJsonElement.isJsonObject());

    JsonObject rootJsonObject = rootJsonElement.getAsJsonObject();
    Assertions.assertTrue(rootJsonObject.has("java"));

    JsonElement javaJsonElement = rootJsonObject.get("java");
    Assertions.assertTrue(javaJsonElement.isJsonPrimitive());
  }
}
