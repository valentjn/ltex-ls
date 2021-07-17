/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.server;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import org.bsplines.ltexls.LtexLanguageServerLauncherTest;
import org.bsplines.ltexls.tools.Tools;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.xtext.xbase.lib.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NonServerCheckerTest {
  @Test
  public void testCheck() throws Exception {
    NonServerChecker nonServerChecker = new NonServerChecker();
    @Nullable File settingsFile = null;
    @Nullable File inputFile = null;
    @Nullable Path inputDirectory = null;
    @Nullable File inputDirectoryChildFile = null;

    try {
      settingsFile = File.createTempFile("ltex-", ".json");
      Tools.writeFileWithException(settingsFile.toPath(),
          "{\"dictionary\":{\"en-US\":[\"LTeX\"]}}\n");
      nonServerChecker.loadSettings(settingsFile.toPath());

      inputFile = File.createTempFile("ltex-", ".tex");
      Path inputFilePath = inputFile.toPath();
      Tools.writeFileWithException(inputFilePath, "This is \\textbf{an test.} This is LTeX.\n");

      inputDirectory = Files.createTempDirectory("ltex-");
      Path inputDirectory2 = inputDirectory;
      inputDirectoryChildFile = File.createTempFile("ltex-", ".md", inputDirectory.toFile());
      Tools.writeFileWithException(inputDirectoryChildFile.toPath(),
          "This is [an test.](qwert) This is LTeX.\n");

      Pair<Integer, String> result = LtexLanguageServerLauncherTest.captureStdout(
          () -> LtexLanguageServerLauncherTest.mockStdin("This is an test. This is LTeX.\n",
            () -> nonServerChecker.check(Arrays.asList(
              inputFilePath, inputDirectory2, Path.of("-")))));

      Assertions.assertEquals(3, result.getKey());

      String message = "\\Q\u001b[33mgrammar:\u001b[0;1m Use 'a' instead of 'an' if the following "
          + "word doesn't start with a vowel sound, e.g. 'a sentence', 'a university'. "
          + "[EN_A_VS_AN]\u001b[m\\E";
      Assertions.assertLinesMatch(Arrays.asList(
            "\u001b\\[1m.*?:1:17: " + message,
            "This is \\textbf{\u001b[1;33man\u001b[m test.} This is LTeX.",
            "                \u001b[32ma\u001b[m",
            ".*?:1:10: " + message,
            "This is [\u001b[1;33man\u001b[m test.](qwert) This is LTeX.",
            "         \u001b[32ma\u001b[m",
            ".*?:1:9: " + message,
            "This is \u001b[1;33man\u001b[m test. This is LTeX.",
            "        \u001b[32ma\u001b[m"),
          Arrays.asList(result.getValue().split("\r?\n")));
    } finally {
      if ((settingsFile != null) && !settingsFile.delete()) {
        Tools.logger.warning(Tools.i18n(
            "couldNotDeleteTemporaryFile", settingsFile.toPath().toString()));
      }

      if ((inputFile != null) && !inputFile.delete()) {
        Tools.logger.warning(Tools.i18n(
            "couldNotDeleteTemporaryFile", inputFile.toPath().toString()));
      }

      if ((inputDirectoryChildFile != null) && !inputDirectoryChildFile.delete()) {
        Tools.logger.warning(Tools.i18n(
            "couldNotDeleteTemporaryFile", inputDirectoryChildFile.toPath().toString()));
      } else if ((inputDirectory != null) && !inputDirectory.toFile().delete()) {
        Tools.logger.warning(Tools.i18n(
            "couldNotDeleteTemporaryDirectory", inputDirectory.toString()));
      }
    }
  }
}
