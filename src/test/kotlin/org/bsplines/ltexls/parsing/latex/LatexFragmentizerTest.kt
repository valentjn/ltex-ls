/* Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.latex

import org.bsplines.ltexls.parsing.CodeFragment
import org.bsplines.ltexls.parsing.CodeFragmentizer
import org.bsplines.ltexls.settings.Settings
import org.junit.platform.suite.api.IncludeEngines
import kotlin.test.Test
import kotlin.test.assertEquals

@IncludeEngines("junit-jupiter")
class LatexFragmentizerTest {
  @Test
  fun testLatex() {
    assertFragmentizer("latex")

    val settings = Settings(_latexCommands = mapOf(Pair("\\todo{}", "ignore")))
    val fragmentizer: CodeFragmentizer = CodeFragmentizer.create("latex")
    val codeFragments = fragmentizer.fragmentize(
      """
      Sentence\footnote[abc]{Footnote} 1
		  %	 ltex: language=de-DE
		  Sentence 2\todo{Todo note}
		  		  %	 ltex:	language=en-US

		  Sentence 3

		  """.trimIndent(), settings
    )
    assertEquals(6, codeFragments.size)
  }

  @Test
  fun testRsweave() {
    assertFragmentizer("rsweave")
  }

  @Test
  fun testTex() {
    assertFragmentizer("tex")
  }

  @Test
  @Suppress("LongMethod")
  fun testBabel() {
    val fragmentizer: CodeFragmentizer = CodeFragmentizer.create("latex")
    var codeFragments: List<CodeFragment> = fragmentizer.fragmentize(
      "This is a \\begin{otherlanguage*}{de-DE}Beispiel\\end{otherlanguage*}.\n", Settings()
    )
    assertEquals(2, codeFragments.size)

    codeFragments = fragmentizer.fragmentize(
      """
      This is a test.
      \usepackage[
        american,  % American English
        ngerman,   % German
        dummy={abc,def}
      ]{babel}
      Dies ist ein Test.

      """.trimIndent(), Settings()
    )
    assertEquals(2, codeFragments.size)
    assertEquals(16, codeFragments[0].code.length)
    assertEquals(113, codeFragments[1].code.length)
    assertEquals("en-US", codeFragments[0].settings.languageShortCode)
    assertEquals("de-DE", codeFragments[1].settings.languageShortCode)

    codeFragments = fragmentizer.fragmentize(
      """
      This is a test.
      \usepackage[
        main=ngerman,  % German
        american,      % American English
        dummy={abc,def}
      ]{babel}
      Dies ist ein Test.

      """.trimIndent(), Settings()
    )
    assertEquals(2, codeFragments.size)
    assertEquals(16, codeFragments[0].code.length)
    assertEquals(121, codeFragments[1].code.length)
    assertEquals("en-US", codeFragments[0].settings.languageShortCode)
    assertEquals("de-DE", codeFragments[1].settings.languageShortCode)

    codeFragments = fragmentizer.fragmentize(
      """
      This is a test.
        \usepackage[ngerman]{babel}
      This is another test.
      """.trimIndent(), Settings()
    )
    assertEquals(2, codeFragments.size)

    codeFragments = fragmentizer.fragmentize(
      """
      This is a test.
        % \usepackage[ngerman]{babel}
      This is another test.
      """.trimIndent(), Settings()
    )
    assertEquals(1, codeFragments.size)

    codeFragments = fragmentizer.fragmentize(
      "This is a \\begin{de-DE}Beispiel\\end{de-DE}.\n", Settings()
    )
    assertEquals(2, codeFragments.size)

    codeFragments = fragmentizer.fragmentize(
      "This is a \\begin{de-DE}Beispiel.\n", Settings()
    )
    assertEquals(2, codeFragments.size)
    assertEquals(10, codeFragments[0].code.length)
    assertEquals(33, codeFragments[1].code.length)
    codeFragments = fragmentizer.fragmentize(
      "This is a Beispiel\\end{de-DE}.\n", Settings()
    )
    assertEquals(1, codeFragments.size)
    assertEquals(31, codeFragments[0].code.length)
  }

  companion object {
    @Suppress("LongMethod")
    private fun assertFragmentizer(codeLanguageId: String) {
      val fragmentizer: CodeFragmentizer = CodeFragmentizer.create(codeLanguageId)
      var codeFragments: List<CodeFragment> = fragmentizer.fragmentize(
        """
        Sentence\footnote[abc]{Footnote} 1
        		  %	 ltex: language=de-DE
        Sentence 2\todo{Todo note}
        %ltex:	language=en-US

        Sentence 3

        """.trimIndent(), Settings()
      )
      assertEquals(7, codeFragments.size)

      for ((curCodeLanguageId: String) in codeFragments) {
        assertEquals(codeLanguageId, curCodeLanguageId)
      }

      assertEquals("Footnote", codeFragments[0].code)
      assertEquals(23, codeFragments[0].fromPos)
      assertEquals("en-US", codeFragments[0].settings.languageShortCode)

      assertEquals(
        "Sentence\\footnote[abc]{Footnote} 1\n",
        codeFragments[1].code
      )
      assertEquals(0, codeFragments[1].fromPos)
      assertEquals("en-US", codeFragments[1].settings.languageShortCode)
      assertEquals("\t\t  %\t ltex: language=de-DE", codeFragments[2].code)

      assertEquals(35, codeFragments[2].fromPos)
      assertEquals("de-DE", codeFragments[2].settings.languageShortCode)

      assertEquals("Todo note", codeFragments[3].code)
      assertEquals(79, codeFragments[3].fromPos)
      assertEquals("de-DE", codeFragments[3].settings.languageShortCode)

      assertEquals(
        "\nSentence 2\\todo{Todo note}\n",
        codeFragments[4].code
      )
      assertEquals(62, codeFragments[4].fromPos)
      assertEquals("de-DE", codeFragments[4].settings.languageShortCode)

      assertEquals(
        "%ltex:\tlanguage=en-US",
        codeFragments[5].code
      )
      assertEquals(90, codeFragments[5].fromPos)
      assertEquals("en-US", codeFragments[5].settings.languageShortCode)

      assertEquals(
        "\n\nSentence 3\n",
        codeFragments[6].code
      )
      assertEquals(111, codeFragments[6].fromPos)
      assertEquals("en-US", codeFragments[6].settings.languageShortCode)

      codeFragments = fragmentizer.fragmentize(
        """
        This is a \foreignlanguage{ngerman}{Beispiel}.
        \selectlanguage{french}
        C'est un autre \textenUS{example}.
        \selectlanguage{german}
        Dies ist weiterer \begin{otherlanguage*}{UKenglish}test\end{otherlanguage*}.
        Und schließlich ein abschließender \begin{american}[abc]
          sentence
          \begin{french}[abc]
            phrase
          \end{french}
        \end{american}.

        """.trimIndent(), Settings()
      )

      assertEquals(8, codeFragments.size)

      for ((curCodeLanguageId: String) in codeFragments) {
        assertEquals(codeLanguageId, curCodeLanguageId)
      }

      assertEquals("Beispiel", codeFragments[0].code)
      assertEquals(36, codeFragments[0].fromPos)
      assertEquals("de-DE", codeFragments[0].settings.languageShortCode)

      assertEquals(
        "This is a \\foreignlanguage{ngerman}{Beispiel}.\n",
        codeFragments[1].code
      )
      assertEquals(0, codeFragments[1].fromPos)
      assertEquals("en-US", codeFragments[1].settings.languageShortCode)
      assertEquals("example", codeFragments[2].code)
      assertEquals(96, codeFragments[2].fromPos)
      assertEquals("en-US", codeFragments[2].settings.languageShortCode)

      assertEquals(
        "\\selectlanguage{french}\nC'est un autre \\textenUS{example}.\n",
        codeFragments[3].code
      )
      assertEquals(47, codeFragments[3].fromPos)
      assertEquals("fr", codeFragments[3].settings.languageShortCode)

      assertEquals("test", codeFragments[4].code)
      assertEquals(181, codeFragments[4].fromPos)
      assertEquals("en-GB", codeFragments[4].settings.languageShortCode)

      assertEquals("\n    phrase\n  ", codeFragments[5].code)
      assertEquals(296, codeFragments[5].fromPos)
      assertEquals("fr", codeFragments[5].settings.languageShortCode)

      assertEquals(
        """
  sentence
  \begin{french}[abc]
    phrase
  \end{french}
""", codeFragments[6].code
      )
      assertEquals(263, codeFragments[6].fromPos)
      assertEquals("en-US", codeFragments[6].settings.languageShortCode)

      assertEquals(
        """
        \selectlanguage{german}
        Dies ist weiterer \begin{otherlanguage*}{UKenglish}test\end{otherlanguage*}.
        Und schließlich ein abschließender \begin{american}[abc]
          sentence
          \begin{french}[abc]
            phrase
          \end{french}
        \end{american}.

        """.trimIndent(), codeFragments[7].code
      )
      assertEquals(106, codeFragments[7].fromPos)
      assertEquals("de-DE", codeFragments[7].settings.languageShortCode)
    }
  }
}
