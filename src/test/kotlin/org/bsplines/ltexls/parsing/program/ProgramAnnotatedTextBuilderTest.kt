/* Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.program

import org.bsplines.ltexls.parsing.CodeAnnotatedTextBuilderTest
import org.junit.platform.suite.api.IncludeEngines
import kotlin.test.Test

@IncludeEngines("junit-jupiter")
class ProgramAnnotatedTextBuilderTest : CodeAnnotatedTextBuilderTest("") {
  @Test
  fun testJava() {
    assertPlainText(
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
      "\n\n\nSentence 4 -\ncheck\n\n\nSentence 10 - check\n\n\n\nSentence 11 -\ncheck\n",
      "java",
    )
  }

  @Test
  fun testPython() {
    assertPlainText(
      "Sentence 1 - no check # Sentence 2 - no check\n#Sentence 3 - no check\n# Sentence 4 -\n"
      + "# check\n\nSentence 5 - no check \"\"\" Sentence 6 - no check \"\"\"\n"
      + "\"\"\" Sentence 7 - no check \"\"\" Sentence 8 - no check\n"
      + "\"\"\"Sentence 9 - no check \"\"\"\n\"\"\" Sentence 10 - check \"\"\"\n"
      + "\"\"\" Sentence 11 -\ncheck \"\"\"\n",
      "\n\n\nSentence 4 -\ncheck\n\n\nSentence 10 - check\n\n\n\nSentence 11 -\ncheck\n",
      "python",
    )
  }

  @Test
  fun testPowerShell() {
    assertPlainText(
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
      "\n\n\nSentence 4 -\ncheck\n\n\nSentence 10 - check\n\n\n\nSentence 11 -\ncheck\n",
      "powershell",
    )
  }

  @Test
  fun testJulia() {
    assertPlainText(
      """
      Sentence 1 - no check # Sentence 2 - no check
      #Sentence 3 - no check
      ## Sentence 4 -
      ## check

      """.trimIndent(),
      "\n\n\nSentence 4 -\ncheck",
      "julia",
    )
  }

  @Test
  fun testLua() {
    assertPlainText(
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
      "\n\n\nSentence 4 -\ncheck\n\n\nSentence 10 - check\n\n\n\nSentence 11 -\ncheck\n",
      "lua",
    )
  }

  @Test
  fun testHaskell() {
    assertPlainText(
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
      "\n\n\nSentence 4 -\ncheck\n\n\nSentence 10 - check\n\n\n\nSentence 11 -\ncheck\n",
      "haskell",
    )
  }

  @Test
  fun testSql() {
    assertPlainText(
      """
      Sentence 1 - no check -- Sentence 2 - no check
      --Sentence 3 - no check
      --- Sentence 4 -
      --- check

      """.trimIndent(),
      "\n\n\nSentence 4 -\ncheck",
      "sql",
    )
  }

  @Test
  fun testLisp() {
    assertPlainText(
      """
      Sentence 1 - no check ; Sentence 2 - no check
      ;Sentence 3 - no check
      ;; Sentence 4 -
      ;; check

      """.trimIndent(),
      "\n\n\nSentence 4 -\ncheck",
      "lisp",
    )
  }

  @Test
  fun testMatlab() {
    assertPlainText(
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
      "\n\n\nSentence 4 -\ncheck\n\n\nSentence 10 - check\n\n\n\nSentence 11 -\ncheck\n",
      "matlab",
    )
  }

  @Test
  fun testErlang() {
    assertPlainText(
      """
      Sentence 1 - no check % Sentence 2 - no check
      %Sentence 3 - no check
      %% Sentence 4 -
      %% check

      """.trimIndent(),
      "\n\n\nSentence 4 -\ncheck",
      "erlang",
    )
  }

  @Test
  fun testFortran() {
    assertPlainText(
      """
      Sentence 1 - no check c Sentence 2 - no check
      cSentence 3 - no check
      c Sentence 4 -
      c check

      """.trimIndent(),
      "\n\n\nSentence 4 -\ncheck",
      "fortran-modern",
    )
  }

  @Test
  fun testVisualBasic() {
    assertPlainText(
      """
      Sentence 1 - no check ' Sentence 2 - no check
      'Sentence 3 - no check
      '' Sentence 4 -
      '' check

      """.trimIndent(),
      "\n\n\nSentence 4 -\ncheck",
      "vb",
    )
  }
}
