/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.html

import org.bsplines.ltexls.parsing.CodeFragment
import org.bsplines.ltexls.parsing.CodeFragmentizer
import org.bsplines.ltexls.settings.Settings
import kotlin.test.Test
import kotlin.test.assertEquals

class HtmlFragmentizerTest {
  @Test
  fun test() {
    val fragmentizer: CodeFragmentizer = CodeFragmentizer.create("html")
    val code = """Sentence 1

      <!-- ltex: language=de-DE-->      
    
    Sentence 2
    
    <!--			ltex:				language=en-US		-->
    
    Sentence 3
    """
    val codeFragments: List<CodeFragment> = fragmentizer.fragmentize(code, Settings())
    assertEquals(1, codeFragments.size)
    assertEquals("html", codeFragments[0].codeLanguageId)
    assertEquals(code, codeFragments[0].code)
    assertEquals(0, codeFragments[0].fromPos)
    assertEquals("en-US", codeFragments[0].settings.languageShortCode)
  }
}
