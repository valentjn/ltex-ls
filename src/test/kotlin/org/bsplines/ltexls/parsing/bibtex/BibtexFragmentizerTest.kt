/* Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.bibtex

import org.bsplines.ltexls.parsing.CodeFragment
import org.bsplines.ltexls.parsing.CodeFragmentizer
import org.bsplines.ltexls.settings.Settings
import org.junit.platform.suite.api.IncludeEngines
import kotlin.test.Test
import kotlin.test.assertEquals

@IncludeEngines("junit-jupiter")
class BibtexFragmentizerTest {
  @Test
  fun testFragmentizer() {
    val settings = Settings()
    val fragmentizer: CodeFragmentizer = CodeFragmentizer.create("bibtex")
    val codeFragments: List<CodeFragment> = fragmentizer.fragmentize(
      """
      @article{some-label,
        name = {Some Name},
        description = {This is a test.}
      }

      @entry{some-label2,
        name = {Some Other Name},
        description = {This is another
        test.},
      }

      @abbreviation{some-label3,
        short =shortform,
        see   = {abc},
        long  = longform,
       }

      """.trimIndent(),
      settings
    )
    assertEquals(6, codeFragments.size)

    for ((codeLanguageId: String) in codeFragments) {
      assertEquals("latex", codeLanguageId)
    }

    assertEquals(" {Some Name}", codeFragments[0].code)
    assertEquals(29, codeFragments[0].fromPos)

    assertEquals(" {This is a test.}\n", codeFragments[1].code)
    assertEquals(58, codeFragments[1].fromPos)

    assertEquals(" {Some Other Name}", codeFragments[2].code)
    assertEquals(108, codeFragments[2].fromPos)

    assertEquals(" {This is another\n  test.}", codeFragments[3].code)
    assertEquals(143, codeFragments[3].fromPos)

    assertEquals("shortform", codeFragments[4].code)
    assertEquals(210, codeFragments[4].fromPos)

    assertEquals(" longform", codeFragments[5].code)
    assertEquals(247, codeFragments[5].fromPos)
  }
}
