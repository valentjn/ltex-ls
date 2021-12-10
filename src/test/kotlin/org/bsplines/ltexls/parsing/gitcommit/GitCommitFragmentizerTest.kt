/* Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.gitcommit

import org.bsplines.ltexls.parsing.restructuredtext.RestructuredtextFragmentizerTest
import org.bsplines.ltexls.settings.Settings
import kotlin.test.Test

class GitCommitFragmentizerTest {
  @Test
  fun testFragmentizer() {
    RestructuredtextFragmentizerTest.assertFragmentizer(
      "git-commit",
      """
      Sentence 1

       # ltex: language=de-DE

      Sentence 2

      	#	ltex:	language=en-US

      Sentence 3

      """.trimIndent(),
    )
  }

  @Test
  fun testWrongSettings() {
    val fragmentizer = GitCommitFragmentizer("git-commit")
    fragmentizer.fragmentize("Sentence 1\n\n# ltex: languagede-DE\n\nSentence 2\n", Settings())
    fragmentizer.fragmentize("Sentence 1\n\n# ltex: unknownKey=abc\n\nSentence 2\n", Settings())
  }
}
