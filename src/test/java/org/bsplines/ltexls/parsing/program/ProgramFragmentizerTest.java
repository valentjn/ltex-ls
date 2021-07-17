/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.program;

import org.bsplines.ltexls.parsing.restructuredtext.RestructuredtextFragmentizerTest;
import org.junit.jupiter.api.Test;

public class ProgramFragmentizerTest {
  @Test
  public void testJava() {
    RestructuredtextFragmentizerTest.assertFragmentizer("java",
        "Sentence 1\n"
        + "\n// ltex: language=de-DE\n\nSentence 2\n"
        + "\n//\tltex:\tlanguage=en-US\n\nSentence 3\n");
  }

  @Test
  public void testPython() {
    RestructuredtextFragmentizerTest.assertFragmentizer("python",
        "Sentence 1\n"
        + "\n#  ltex: language=de-DE\n\nSentence 2\n"
        + "\n#\t\tltex:\tlanguage=en-US\n\nSentence 3\n");
  }

  @Test
  public void testPowerShell() {
    RestructuredtextFragmentizerTest.assertFragmentizer("powershell",
        "Sentence 1\n"
        + "\n#  ltex: language=de-DE\n\nSentence 2\n"
        + "\n#\t\tltex:\tlanguage=en-US\n\nSentence 3\n");
  }

  @Test
  public void testJulia() {
    RestructuredtextFragmentizerTest.assertFragmentizer("julia",
        "Sentence 1\n"
        + "\n#  ltex: language=de-DE\n\nSentence 2\n"
        + "\n#\t\tltex:\tlanguage=en-US\n\nSentence 3\n");
  }

  @Test
  public void testLua() {
    RestructuredtextFragmentizerTest.assertFragmentizer("lua",
        "Sentence 1\n"
        + "\n-- ltex: language=de-DE\n\nSentence 2\n"
        + "\n--\tltex:\tlanguage=en-US\n\nSentence 3\n");
  }

  @Test
  public void testHaskell() {
    RestructuredtextFragmentizerTest.assertFragmentizer("haskell",
        "Sentence 1\n"
        + "\n-- ltex: language=de-DE\n\nSentence 2\n"
        + "\n--\tltex:\tlanguage=en-US\n\nSentence 3\n");
  }

  @Test
  public void testSql() {
    RestructuredtextFragmentizerTest.assertFragmentizer("sql",
        "Sentence 1\n"
        + "\n-- ltex: language=de-DE\n\nSentence 2\n"
        + "\n--\tltex:\tlanguage=en-US\n\nSentence 3\n");
  }

  @Test
  public void testLisp() {
    RestructuredtextFragmentizerTest.assertFragmentizer("lisp",
        "Sentence 1\n"
        + "\n;  ltex: language=de-DE\n\nSentence 2\n"
        + "\n;\t\tltex:\tlanguage=en-US\n\nSentence 3\n");
  }

  @Test
  public void testMatlab() {
    RestructuredtextFragmentizerTest.assertFragmentizer("matlab",
        "Sentence 1\n"
        + "\n%  ltex: language=de-DE\n\nSentence 2\n"
        + "\n%\t\tltex:\tlanguage=en-US\n\nSentence 3\n");
  }

  @Test
  public void testErlang() {
    RestructuredtextFragmentizerTest.assertFragmentizer("erlang",
        "Sentence 1\n"
        + "\n%  ltex: language=de-DE\n\nSentence 2\n"
        + "\n%\t\tltex:\tlanguage=en-US\n\nSentence 3\n");
  }

  @Test
  public void testFortran() {
    RestructuredtextFragmentizerTest.assertFragmentizer("fortran-modern",
        "Sentence 1\n"
        + "\nc  ltex: language=de-DE\n\nSentence 2\n"
        + "\nc\t\tltex:\tlanguage=en-US\n\nSentence 3\n");
  }

  @Test
  public void testVisualBasic() {
    RestructuredtextFragmentizerTest.assertFragmentizer("vb",
        "Sentence 1\n"
        + "\n'  ltex: language=de-DE\n\nSentence 2\n"
        + "\n'\t\tltex:\tlanguage=en-US\n\nSentence 3\n");
  }
}
