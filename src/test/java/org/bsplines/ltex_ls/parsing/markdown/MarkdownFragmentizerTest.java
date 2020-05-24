package org.bsplines.ltex_ls.parsing.markdown;

import java.util.List;

import org.bsplines.ltex_ls.Settings;
import org.bsplines.ltex_ls.parsing.CodeFragment;
import org.bsplines.ltex_ls.parsing.CodeFragmentizer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MarkdownFragmentizerTest {
  @Test
  public void test() {
    CodeFragmentizer fragmentizer = CodeFragmentizer.create("markdown", new Settings());
    List<CodeFragment> codeFragments = fragmentizer.fragmentize(
        "Sentence 1\n" +
        "\n[comment]: # ltex: language=de-DE\n\nSentence 2\n" +
        "\n[comment]:\t#ltex:\tlanguage=en-US\n\nSentence 3\n");
    Assertions.assertEquals(3, codeFragments.size());

    Assertions.assertEquals("markdown", codeFragments.get(0).getCodeLanguageId());
    Assertions.assertEquals("Sentence 1\n\n[comment]: ",
        codeFragments.get(0).getCode());
    Assertions.assertEquals(0, codeFragments.get(0).getFromPos());
    Assertions.assertEquals("en-US", codeFragments.get(0).getSettings().getLanguageShortCode());

    Assertions.assertEquals("markdown", codeFragments.get(1).getCodeLanguageId());
    Assertions.assertEquals("# ltex: language=de-DE\n\nSentence 2\n\n[comment]:\t",
        codeFragments.get(1).getCode());
    Assertions.assertEquals(23, codeFragments.get(1).getFromPos());
    Assertions.assertEquals("de-DE", codeFragments.get(1).getSettings().getLanguageShortCode());

    Assertions.assertEquals("markdown", codeFragments.get(2).getCodeLanguageId());
    Assertions.assertEquals("#ltex:\tlanguage=en-US\n\nSentence 3\n",
        codeFragments.get(2).getCode());
    Assertions.assertEquals(70, codeFragments.get(2).getFromPos());
    Assertions.assertEquals("en-US", codeFragments.get(2).getSettings().getLanguageShortCode());
  }

  @Test
  public void testWrongSettings() {
    MarkdownFragmentizer markdownFragmentizer =
        new MarkdownFragmentizer("markdown", new Settings());
    Assertions.assertDoesNotThrow(() -> markdownFragmentizer.fragmentize(
        "Sentence 1\n[comment]: # ltex: languagede-DE\n\nSentence 2\n"));
    Assertions.assertDoesNotThrow(() -> markdownFragmentizer.fragmentize(
        "Sentence 1\n[comment]: # ltex: unknownKey=abc\n\nSentence 2\n"));
  }
}
