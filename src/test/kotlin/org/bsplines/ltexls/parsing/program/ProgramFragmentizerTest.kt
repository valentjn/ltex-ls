/* Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.program

import org.bsplines.ltexls.parsing.restructuredtext.RestructuredtextFragmentizerTest
import org.junit.platform.suite.api.IncludeEngines
import kotlin.test.Test

@IncludeEngines("junit-jupiter")
class ProgramFragmentizerTest {
  @Test
  fun testJava() {
    RestructuredtextFragmentizerTest.assertFragmentizer(
      "java",
      """
            Sentence 1
            
            // ltex: language=de-DE
            
            Sentence 2
            
            //	ltex:	language=en-US
            
            Sentence 3
            
            """.trimIndent()
    )
  }

  @Test
  fun testPython() {
    RestructuredtextFragmentizerTest.assertFragmentizer(
      "python",
      """
            Sentence 1
            
            #  ltex: language=de-DE
            
            Sentence 2
            
            #		ltex:	language=en-US
            
            Sentence 3
            
            """.trimIndent()
    )
  }

  @Test
  fun testPowerShell() {
    RestructuredtextFragmentizerTest.assertFragmentizer(
      "powershell",
      """
            Sentence 1
            
            #  ltex: language=de-DE
            
            Sentence 2
            
            #		ltex:	language=en-US
            
            Sentence 3
            
            """.trimIndent()
    )
  }

  @Test
  fun testJulia() {
    RestructuredtextFragmentizerTest.assertFragmentizer(
      "julia",
      """
            Sentence 1
            
            #  ltex: language=de-DE
            
            Sentence 2
            
            #		ltex:	language=en-US
            
            Sentence 3
            
            """.trimIndent()
    )
  }

  @Test
  fun testLua() {
    RestructuredtextFragmentizerTest.assertFragmentizer(
      "lua",
      """
            Sentence 1
            
            -- ltex: language=de-DE
            
            Sentence 2
            
            --	ltex:	language=en-US
            
            Sentence 3
            
            """.trimIndent()
    )
  }

  @Test
  fun testHaskell() {
    RestructuredtextFragmentizerTest.assertFragmentizer(
      "haskell",
      """
            Sentence 1
            
            -- ltex: language=de-DE
            
            Sentence 2
            
            --	ltex:	language=en-US
            
            Sentence 3
            
            """.trimIndent()
    )
  }

  @Test
  fun testSql() {
    RestructuredtextFragmentizerTest.assertFragmentizer(
      "sql",
      """
            Sentence 1
            
            -- ltex: language=de-DE
            
            Sentence 2
            
            --	ltex:	language=en-US
            
            Sentence 3
            
            """.trimIndent()
    )
  }

  @Test
  fun testLisp() {
    RestructuredtextFragmentizerTest.assertFragmentizer(
      "lisp",
      """
            Sentence 1
            
            ;  ltex: language=de-DE
            
            Sentence 2
            
            ;		ltex:	language=en-US
            
            Sentence 3
            
            """.trimIndent()
    )
  }

  @Test
  fun testMatlab() {
    RestructuredtextFragmentizerTest.assertFragmentizer(
      "matlab",
      """
            Sentence 1
            
            %  ltex: language=de-DE
            
            Sentence 2
            
            %		ltex:	language=en-US
            
            Sentence 3
            
            """.trimIndent()
    )
  }

  @Test
  fun testErlang() {
    RestructuredtextFragmentizerTest.assertFragmentizer(
      "erlang",
      """
            Sentence 1
            
            %  ltex: language=de-DE
            
            Sentence 2
            
            %		ltex:	language=en-US
            
            Sentence 3
            
            """.trimIndent()
    )
  }

  @Test
  fun testFortran() {
    RestructuredtextFragmentizerTest.assertFragmentizer(
      "fortran-modern",
      """
            Sentence 1
            
            c  ltex: language=de-DE
            
            Sentence 2
            
            c		ltex:	language=en-US
            
            Sentence 3
            
            """.trimIndent()
    )
  }

  @Test
  fun testVisualBasic() {
    RestructuredtextFragmentizerTest.assertFragmentizer(
      "vb",
      """
            Sentence 1
            
            '  ltex: language=de-DE
            
            Sentence 2
            
            '		ltex:	language=en-US
            
            Sentence 3
            
            """.trimIndent()
    )
  }
}
