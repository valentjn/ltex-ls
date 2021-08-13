/* Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.server

import org.bsplines.ltexls.LtexLanguageServerLauncherTest
import org.bsplines.ltexls.tools.FileIo
import org.bsplines.ltexls.tools.I18n
import org.bsplines.ltexls.tools.Logging
import org.junit.platform.suite.api.IncludeEngines
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

@IncludeEngines("junit-jupiter")
class NonServerCheckerTest {
  @Test
  fun testCheck() {
    val nonServerChecker = NonServerChecker()
    var settingsFile: File? = null
    var inputFile: File? = null
    var inputDirectory: Path? = null
    var inputDirectoryChildFile: File? = null

    try {
      settingsFile = File.createTempFile("ltex-", ".json")
      FileIo.writeFileWithException(settingsFile.toPath(),
          "{\"dictionary\":{\"en-US\":[\"LTeX\"]}}\n")
      nonServerChecker.loadSettings(settingsFile.toPath())

      inputFile = File.createTempFile("ltex-", ".tex")
      val inputFilePath: Path = inputFile.toPath()
      FileIo.writeFileWithException(inputFilePath, "This is \\textbf{an test.} This is LTeX.\n")

      inputDirectory = Files.createTempDirectory("ltex-")
      val inputDirectory2: Path = inputDirectory
      inputDirectoryChildFile = File.createTempFile("ltex-", ".md", inputDirectory.toFile())
      FileIo.writeFileWithException(inputDirectoryChildFile.toPath(),
          "This is [an test.](qwert) This is LTeX.\n")

      val result: Pair<Int, String> = LtexLanguageServerLauncherTest.captureStdout {
        LtexLanguageServerLauncherTest.mockStdin("This is an test. This is LTeX.\n") {
          nonServerChecker.check(listOf(inputFilePath, inputDirectory2, Path.of("-")))
        }
      }

      assertEquals(3, result.first)

      val message: String = (
        "\\Q\u001b[33mgrammar:\u001b[0;1m Use 'a' instead of 'an' if the "
        + "following word doesn't start with a vowel sound, e.g. 'a sentence', 'a university'. "
        + "[EN_A_VS_AN]\u001b[m\\E"
      )
      assertContains(result.second, Regex(
        "^\u001b\\[1m.*?:1:17: $message\r?\n"
        + "This is \\\\textbf\\{\u001b\\[1;33man\u001b\\[m test\\.} This is LTeX\\.\r?\n"
        + " {16}\u001b\\[32ma\u001b\\[m\r?\n"
        + ".*?:1:10: $message\r?\n"
        + "This is \\[\u001b\\[1;33man\u001b\\[m test\\.]\\(qwert\\) This is LTeX\\.\r?\n"
        + " {9}\u001b\\[32ma\u001b\\[m\r?\n"
        + ".*?:1:9: $message\r?\n"
        + "This is \u001b\\[1;33man\u001b\\[m test\\. This is LTeX\\.\r?\n"
        + " {8}\u001b\\[32ma\u001b\\[m\r?\n$"
      ))
    } finally {
      if ((settingsFile != null) && !settingsFile.delete()) {
        Logging.logger.warning(I18n.format(
            "couldNotDeleteTemporaryFile", settingsFile.toPath().toString()))
      }

      if ((inputFile != null) && !inputFile.delete()) {
        Logging.logger.warning(I18n.format(
            "couldNotDeleteTemporaryFile", inputFile.toPath().toString()))
      }

      if ((inputDirectoryChildFile != null) && !inputDirectoryChildFile.delete()) {
        Logging.logger.warning(I18n.format(
            "couldNotDeleteTemporaryFile", inputDirectoryChildFile.toPath().toString()))
      } else if ((inputDirectory != null) && !inputDirectory.toFile().delete()) {
        Logging.logger.warning(I18n.format(
            "couldNotDeleteTemporaryDirectory", inputDirectory.toString()))
      }
    }
  }
}
