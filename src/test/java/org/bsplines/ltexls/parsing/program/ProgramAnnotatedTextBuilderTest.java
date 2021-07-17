/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.program;

import org.bsplines.ltexls.parsing.CodeAnnotatedTextBuilder;
import org.bsplines.ltexls.settings.Settings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.languagetool.markup.AnnotatedText;

public class ProgramAnnotatedTextBuilderTest {
  private static void assertPlainText(
        String codeLanguageId, String code, String expectedPlainText) {
    AnnotatedText annotatedText = buildAnnotatedText(codeLanguageId, code);
    Assertions.assertEquals(expectedPlainText, annotatedText.getPlainText());
  }

  private static AnnotatedText buildAnnotatedText(String codeLanguageId, String code) {
    CodeAnnotatedTextBuilder builder = CodeAnnotatedTextBuilder.create(codeLanguageId);
    Settings settings = new Settings();
    builder.setSettings(settings);
    return builder.addCode(code).build();
  }

  @Test
  public void testJava() {
    assertPlainText("java",
        "Sentence 1 - no check // Sentence 2 - no check\n//Sentence 3 - no check\n"
        + "// Sentence 4 -\n// check\n\nSentence 5 - no check /* Sentence 6 - no check */\n"
        + "/* Sentence 7 - no check */ Sentence 8 - no check\n/*Sentence 9 - no check */\n"
        + "/* Sentence 10 - check */\n/** Sentence 11 -\n  * check */\n",
        "\n\n\nSentence 4 -\ncheck\n\n\nSentence 10 - check\n\n\n\nSentence 11 -\ncheck\n");
  }

  @Test
  public void testPython() {
    assertPlainText("python",
        "Sentence 1 - no check # Sentence 2 - no check\n#Sentence 3 - no check\n"
        + "# Sentence 4 -\n# check\n\nSentence 5 - no check \"\"\" Sentence 6 - no check \"\"\"\n"
        + "\"\"\" Sentence 7 - no check \"\"\" Sentence 8 - no check\n"
        + "\"\"\"Sentence 9 - no check \"\"\"\n"
        + "\"\"\" Sentence 10 - check \"\"\"\n\"\"\" Sentence 11 -\ncheck \"\"\"\n",
        "\n\n\nSentence 4 -\ncheck\n\n\nSentence 10 - check\n\n\n\nSentence 11 -\ncheck\n");
  }

  @Test
  public void testPowerShell() {
    assertPlainText("powershell",
        "Sentence 1 - no check # Sentence 2 - no check\n#Sentence 3 - no check\n"
        + "# Sentence 4 -\n# check\n\nSentence 5 - no check <# Sentence 6 - no check #>\n"
        + "<# Sentence 7 - no check #> Sentence 8 - no check\n<#Sentence 9 - no check #>\n"
        + "<# Sentence 10 - check #>\n<# Sentence 11 -\n # check #>\n",
        "\n\n\nSentence 4 -\ncheck\n\n\nSentence 10 - check\n\n\n\nSentence 11 -\ncheck\n");
  }

  @Test
  public void testJulia() {
    assertPlainText("julia",
        "Sentence 1 - no check # Sentence 2 - no check\n#Sentence 3 - no check\n"
        + "## Sentence 4 -\n## check\n",
        "\n\n\nSentence 4 -\ncheck");
  }

  @Test
  public void testLua() {
    assertPlainText("lua",
        "Sentence 1 - no check -- Sentence 2 - no check\n--Sentence 3 - no check\n"
        + "--- Sentence 4 -\n--- check\n\nSentence 5 - no check --[[ Sentence 6 - no check ]]\n"
        + "--[[ Sentence 7 - no check ]] Sentence 8 - no check\n--[[Sentence 9 - no check ]]\n"
        + "--[[ Sentence 10 - check ]]\n--[[ Sentence 11 -\ncheck ]]\n",
        "\n\n\nSentence 4 -\ncheck\n\n\nSentence 10 - check\n\n\n\nSentence 11 -\ncheck\n");
  }

  @Test
  public void testHaskell() {
    assertPlainText("haskell",
        "Sentence 1 - no check -- Sentence 2 - no check\n--Sentence 3 - no check\n"
        + "--- Sentence 4 -\n--- check\n\nSentence 5 - no check {- Sentence 6 - no check -}\n"
        + "{- Sentence 7 - no check -} Sentence 8 - no check\n{-Sentence 9 - no check -}\n"
        + "{- Sentence 10 - check -}\n{- Sentence 11 -\ncheck -}\n",
        "\n\n\nSentence 4 -\ncheck\n\n\nSentence 10 - check\n\n\n\nSentence 11 -\ncheck\n");
  }

  @Test
  public void testSql() {
    assertPlainText("sql",
        "Sentence 1 - no check -- Sentence 2 - no check\n--Sentence 3 - no check\n"
        + "--- Sentence 4 -\n--- check\n",
        "\n\n\nSentence 4 -\ncheck");
  }

  @Test
  public void testLisp() {
    assertPlainText("lisp",
        "Sentence 1 - no check ; Sentence 2 - no check\n;Sentence 3 - no check\n"
        + ";; Sentence 4 -\n;; check\n",
        "\n\n\nSentence 4 -\ncheck");
  }

  @Test
  public void testMatlab() {
    assertPlainText("matlab",
        "Sentence 1 - no check % Sentence 2 - no check\n%Sentence 3 - no check\n"
        + "%% Sentence 4 -\n%% check\n\nSentence 5 - no check %{ Sentence 6 - no check %}\n"
        + "%{ Sentence 7 - no check %} Sentence 8 - no check\n%{Sentence 9 - no check %}\n"
        + "%{ Sentence 10 - check %}\n%{ Sentence 11 -\ncheck %}\n",
        "\n\n\nSentence 4 -\ncheck\n\n\nSentence 10 - check\n\n\n\nSentence 11 -\ncheck\n");
  }

  @Test
  public void testErlang() {
    assertPlainText("erlang",
        "Sentence 1 - no check % Sentence 2 - no check\n%Sentence 3 - no check\n"
        + "%% Sentence 4 -\n%% check\n",
        "\n\n\nSentence 4 -\ncheck");
  }

  @Test
  public void testFortran() {
    assertPlainText("fortran-modern",
        "Sentence 1 - no check c Sentence 2 - no check\ncSentence 3 - no check\n"
        + "c Sentence 4 -\nc check\n",
        "\n\n\nSentence 4 -\ncheck");
  }

  @Test
  public void testVisualBasic() {
    assertPlainText("vb",
        "Sentence 1 - no check ' Sentence 2 - no check\n'Sentence 3 - no check\n"
        + "'' Sentence 4 -\n'' check\n",
        "\n\n\nSentence 4 -\ncheck");
  }
}
