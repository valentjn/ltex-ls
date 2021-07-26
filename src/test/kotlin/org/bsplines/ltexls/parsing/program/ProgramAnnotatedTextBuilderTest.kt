/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.program

import org.bsplines.ltexls.parsing.CodeAnnotatedTextBuilder
import org.bsplines.ltexls.settings.Settings
import org.junit.platform.suite.api.IncludeEngines
import org.languagetool.markup.AnnotatedText
import kotlin.test.Test
import kotlin.test.assertEquals

@IncludeEngines("junit-jupiter")
class ProgramAnnotatedTextBuilderTest {
  @Test
  fun testJava() {
    assertPlainText(
      "java",
      """
      Sentence 1 - no check // Sentence 2 - no check
      //Sentence 3 - no check
      // Sentence 4 -
      // check
      
      Sentence 5 - no check /* Sentence 6 - no check */
      /* Sentence 7 - no check */ Sentence 8 - no check
      /*Sentence 9 - no check */
      /* Sentence 10 - check */
      /** Sentence 11 -
        * check */
        
      """.trimIndent(),
      "\n\n\nSentence 4 -\ncheck\n\n\nSentence 10 - check\n\n\n\nSentence 11 -\ncheck\n"
    )
  }

  @Test
  fun testPython() {
    assertPlainText(
      "python",
      "Sentence 1 - no check # Sentence 2 - no check\n#Sentence 3 - no check\n# Sentence 4 -\n"
      + "# check\n\nSentence 5 - no check \"\"\" Sentence 6 - no check \"\"\"\n"
      + "\"\"\" Sentence 7 - no check \"\"\" Sentence 8 - no check\n"
      + "\"\"\"Sentence 9 - no check \"\"\"\n\"\"\" Sentence 10 - check \"\"\"\n"
      + "\"\"\" Sentence 11 -\ncheck \"\"\"\n",
      "\n\n\nSentence 4 -\ncheck\n\n\nSentence 10 - check\n\n\n\nSentence 11 -\ncheck\n"
    )
  }

  @Test
  fun testPowerShell() {
    assertPlainText(
      "powershell",
      """
      Sentence 1 - no check # Sentence 2 - no check
      #Sentence 3 - no check
      # Sentence 4 -
      # check
      
      Sentence 5 - no check <# Sentence 6 - no check #>
      <# Sentence 7 - no check #> Sentence 8 - no check
      <#Sentence 9 - no check #>
      <# Sentence 10 - check #>
      <# Sentence 11 -
       # check #>
       
      """.trimIndent(),
      "\n\n\nSentence 4 -\ncheck\n\n\nSentence 10 - check\n\n\n\nSentence 11 -\ncheck\n"
    )
  }

  @Test
  fun testJulia() {
    assertPlainText(
      "julia",
      """
      Sentence 1 - no check # Sentence 2 - no check
      #Sentence 3 - no check
      ## Sentence 4 -
      ## check
      
      """.trimIndent(),
      "\n\n\nSentence 4 -\ncheck"
    )
  }

  @Test
  fun testLua() {
    assertPlainText(
      "lua",
      """
      Sentence 1 - no check -- Sentence 2 - no check
      --Sentence 3 - no check
      --- Sentence 4 -
      --- check
      
      Sentence 5 - no check --[[ Sentence 6 - no check ]]
      --[[ Sentence 7 - no check ]] Sentence 8 - no check
      --[[Sentence 9 - no check ]]
      --[[ Sentence 10 - check ]]
      --[[ Sentence 11 -
      check ]]
      
      """.trimIndent(),
      "\n\n\nSentence 4 -\ncheck\n\n\nSentence 10 - check\n\n\n\nSentence 11 -\ncheck\n"
    )
  }

  @Test
  fun testHaskell() {
    assertPlainText(
      "haskell",
      """
      Sentence 1 - no check -- Sentence 2 - no check
      --Sentence 3 - no check
      --- Sentence 4 -
      --- check
      
      Sentence 5 - no check {- Sentence 6 - no check -}
      {- Sentence 7 - no check -} Sentence 8 - no check
      {-Sentence 9 - no check -}
      {- Sentence 10 - check -}
      {- Sentence 11 -
      check -}
      
      """.trimIndent(),
      "\n\n\nSentence 4 -\ncheck\n\n\nSentence 10 - check\n\n\n\nSentence 11 -\ncheck\n"
    )
  }

  @Test
  fun testSql() {
    assertPlainText(
      "sql",
      """
      Sentence 1 - no check -- Sentence 2 - no check
      --Sentence 3 - no check
      --- Sentence 4 -
      --- check
      
      """.trimIndent(),
      "\n\n\nSentence 4 -\ncheck"
    )
  }

  @Test
  fun testLisp() {
    assertPlainText(
      "lisp",
      """
      Sentence 1 - no check ; Sentence 2 - no check
      ;Sentence 3 - no check
      ;; Sentence 4 -
      ;; check
      
      """.trimIndent(),
      "\n\n\nSentence 4 -\ncheck"
    )
  }

  @Test
  fun testMatlab() {
    assertPlainText(
      "matlab",
      """
      Sentence 1 - no check % Sentence 2 - no check
      %Sentence 3 - no check
      %% Sentence 4 -
      %% check
      
      Sentence 5 - no check %{ Sentence 6 - no check %}
      %{ Sentence 7 - no check %} Sentence 8 - no check
      %{Sentence 9 - no check %}
      %{ Sentence 10 - check %}
      %{ Sentence 11 -
      check %}
      
      """.trimIndent(),
      "\n\n\nSentence 4 -\ncheck\n\n\nSentence 10 - check\n\n\n\nSentence 11 -\ncheck\n"
    )
  }

  @Test
  fun testErlang() {
    assertPlainText(
      "erlang",
      """
      Sentence 1 - no check % Sentence 2 - no check
      %Sentence 3 - no check
      %% Sentence 4 -
      %% check
      
      """.trimIndent(),
      "\n\n\nSentence 4 -\ncheck"
    )
  }

  @Test
  fun testFortran() {
    assertPlainText(
      "fortran-modern",
      """
      Sentence 1 - no check c Sentence 2 - no check
      cSentence 3 - no check
      c Sentence 4 -
      c check
      
      """.trimIndent(),
      "\n\n\nSentence 4 -\ncheck"
    )
  }

  @Test
  fun testVisualBasic() {
    assertPlainText(
      "vb",
      """
      Sentence 1 - no check ' Sentence 2 - no check
      'Sentence 3 - no check
      '' Sentence 4 -
      '' check
      
      """.trimIndent(),
      "\n\n\nSentence 4 -\ncheck"
    )
  }

  companion object {
    private fun assertPlainText(codeLanguageId: String, code: String, expectedPlainText: String) {
      val annotatedText: AnnotatedText = buildAnnotatedText(codeLanguageId, code)
      assertEquals(expectedPlainText, annotatedText.plainText)
    }

    private fun buildAnnotatedText(codeLanguageId: String, code: String): AnnotatedText {
      val builder: CodeAnnotatedTextBuilder = CodeAnnotatedTextBuilder.create(codeLanguageId)
      val settings = Settings()
      builder.setSettings(settings)
      return builder.addCode(code).build()
    }
  }
}
