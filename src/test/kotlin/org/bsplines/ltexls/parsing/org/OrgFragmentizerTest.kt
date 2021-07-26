/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.org

import org.bsplines.ltexls.parsing.restructuredtext.RestructuredtextFragmentizerTest
import org.bsplines.ltexls.settings.Settings
import org.junit.platform.suite.api.IncludeEngines
import kotlin.test.Test

@IncludeEngines("junit-jupiter")
class OrgFragmentizerTest {
  @Test
  fun testFragmentizer() {
    RestructuredtextFragmentizerTest.assertFragmentizer(
      "org",
      """
      Sentence 1
      
       # ltex: language=de-DE
      
      Sentence 2
      
      	#	ltex:	language=en-US
      
      Sentence 3
      
      """.trimIndent()
    )
  }

  @Test
  fun testWrongSettings() {
    val fragmentizer = OrgFragmentizer("org")
    fragmentizer.fragmentize("Sentence 1\n\n# ltex: languagede-DE\n\nSentence 2\n", Settings())
    fragmentizer.fragmentize("Sentence 1\n\n# ltex: unknownKey=abc\n\nSentence 2\n", Settings())
  }
}
