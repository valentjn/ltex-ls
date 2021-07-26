/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.nop

import org.bsplines.ltexls.parsing.CodeFragment
import org.bsplines.ltexls.parsing.CodeFragmentizer
import org.bsplines.ltexls.settings.Settings
import org.junit.platform.suite.api.IncludeEngines
import kotlin.test.Test
import kotlin.test.assertEquals

@IncludeEngines("junit-jupiter")
class NopFragmentizerTest {
  @Test
  fun test() {
    val fragmentizer: CodeFragmentizer = CodeFragmentizer.create("nop")
    val code = "This is a test.\n# LTeX: language=de-DE\n% LTeX: language=en-US\n"
    val codeFragments: List<CodeFragment> = fragmentizer.fragmentize(code, Settings())
    assertEquals(1, codeFragments.size)
    assertEquals("nop", codeFragments[0].codeLanguageId)
    assertEquals(code, codeFragments[0].code)
    assertEquals(0, codeFragments[0].fromPos)
    assertEquals("en-US", codeFragments[0].settings.languageShortCode)
  }
}
