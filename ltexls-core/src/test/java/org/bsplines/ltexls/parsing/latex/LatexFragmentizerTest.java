/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.latex;

import java.util.Arrays;
import java.util.List;
import org.bsplines.ltexls.Settings;
import org.bsplines.ltexls.parsing.CodeFragment;
import org.bsplines.ltexls.parsing.CodeFragmentizer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LatexFragmentizerTest {
  private static void testCodeLanguage(String codeLanguageId) {
    CodeFragmentizer fragmentizer = CodeFragmentizer.create(codeLanguageId, new Settings());
    List<CodeFragment> codeFragments = fragmentizer.fragmentize(
        "Sentence\\footnote[abc]{Footnote} 1\n"
        + "\t\t  %\t ltex: language=de-DE\nSentence 2\\todo{Todo note}\n"
        + "%ltex:\tlanguage=en-US\n\nSentence 3\n");
    Assertions.assertEquals(5, codeFragments.size());

    Assertions.assertEquals(codeLanguageId, codeFragments.get(0).getCodeLanguageId());
    Assertions.assertEquals("Footnote", codeFragments.get(0).getCode());
    Assertions.assertEquals(23, codeFragments.get(0).getFromPos());
    Assertions.assertEquals("en-US", codeFragments.get(0).getSettings().getLanguageShortCode());

    Assertions.assertEquals(codeLanguageId, codeFragments.get(1).getCodeLanguageId());
    Assertions.assertEquals("Sentence\\footnote[abc]{Footnote} 1\n",
        codeFragments.get(1).getCode());
    Assertions.assertEquals(0, codeFragments.get(1).getFromPos());
    Assertions.assertEquals("en-US", codeFragments.get(1).getSettings().getLanguageShortCode());

    Assertions.assertEquals(codeLanguageId, codeFragments.get(2).getCodeLanguageId());
    Assertions.assertEquals("Todo note", codeFragments.get(2).getCode());
    Assertions.assertEquals(79, codeFragments.get(2).getFromPos());
    Assertions.assertEquals("de-DE", codeFragments.get(2).getSettings().getLanguageShortCode());

    Assertions.assertEquals(codeLanguageId, codeFragments.get(3).getCodeLanguageId());
    Assertions.assertEquals("\t\t  %\t ltex: language=de-DE\nSentence 2\\todo{Todo note}\n",
        codeFragments.get(3).getCode());
    Assertions.assertEquals(35, codeFragments.get(3).getFromPos());
    Assertions.assertEquals("de-DE", codeFragments.get(3).getSettings().getLanguageShortCode());

    Assertions.assertEquals(codeLanguageId, codeFragments.get(4).getCodeLanguageId());
    Assertions.assertEquals("%ltex:\tlanguage=en-US\n\nSentence 3\n",
        codeFragments.get(4).getCode());
    Assertions.assertEquals(90, codeFragments.get(4).getFromPos());
    Assertions.assertEquals("en-US", codeFragments.get(4).getSettings().getLanguageShortCode());
  }

  @Test
  public void testLatex() {
    testCodeLanguage("latex");

    {
      Settings settings = new Settings();
      settings.setIgnoreCommandPrototypes(Arrays.asList(new String[]{"\\todo{}"}));
      CodeFragmentizer fragmentizer = CodeFragmentizer.create("latex", settings);
      List<CodeFragment> codeFragments = fragmentizer.fragmentize(
          "Sentence\\footnote[abc]{Footnote} 1\n"
          + "\t\t  %\t ltex: language=de-DE\nSentence 2\\todo{Todo note}\n"
          + "%ltex:\tlanguage=en-US\n\nSentence 3\n");
      Assertions.assertEquals(4, codeFragments.size());
    }
  }

  @Test
  public void testRsweave() {
    testCodeLanguage("rsweave");
  }
}
