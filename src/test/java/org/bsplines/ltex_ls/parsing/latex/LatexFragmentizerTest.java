package org.bsplines.ltex_ls.parsing.latex;

import java.util.List;

import org.bsplines.ltex_ls.Settings;
import org.bsplines.ltex_ls.parsing.CodeFragment;
import org.bsplines.ltex_ls.parsing.CodeFragmentizer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LatexFragmentizerTest {
  private static void testCodeLanguage(String codeLanguageId) {
    CodeFragmentizer fragmentizer = CodeFragmentizer.create(codeLanguageId, new Settings());
    List<CodeFragment> codeFragments = fragmentizer.fragmentize(
        "Sentence 1\n" +
        "\t\t  %\t ltex: language=de-DE\nSentence 2\n" +
        "%ltex:\tlanguage=en-US\n\nSentence 3\n");
    Assertions.assertEquals(3, codeFragments.size());

    Assertions.assertEquals(codeLanguageId, codeFragments.get(0).getCodeLanguageId());
    Assertions.assertEquals("Sentence 1\n",
        codeFragments.get(0).getCode());
    Assertions.assertEquals(0, codeFragments.get(0).getFromPos());
    Assertions.assertEquals("en-US", codeFragments.get(0).getSettings().getLanguageShortCode());

    Assertions.assertEquals(codeLanguageId, codeFragments.get(1).getCodeLanguageId());
    Assertions.assertEquals("\t\t  %\t ltex: language=de-DE\nSentence 2\n",
        codeFragments.get(1).getCode());
    Assertions.assertEquals(11, codeFragments.get(1).getFromPos());
    Assertions.assertEquals("de-DE", codeFragments.get(1).getSettings().getLanguageShortCode());

    Assertions.assertEquals(codeLanguageId, codeFragments.get(2).getCodeLanguageId());
    Assertions.assertEquals("%ltex:\tlanguage=en-US\n\nSentence 3\n",
        codeFragments.get(2).getCode());
    Assertions.assertEquals(50, codeFragments.get(2).getFromPos());
    Assertions.assertEquals("en-US", codeFragments.get(2).getSettings().getLanguageShortCode());
  }

  @Test
  public void testLatex() {
    testCodeLanguage("latex");
  }

  @Test
  public void testRsweave() {
    testCodeLanguage("rsweave");
  }
}
