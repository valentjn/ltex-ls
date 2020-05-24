package org.bsplines.ltex_ls.parsing.plaintext;

import java.util.List;

import org.bsplines.ltex_ls.Settings;
import org.bsplines.ltex_ls.parsing.CodeFragment;
import org.bsplines.ltex_ls.parsing.CodeFragmentizer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PlaintextFragmentizerTest {
  @Test
  public void test() {
    CodeFragmentizer fragmentizer = CodeFragmentizer.create("plaintext", new Settings());
    String code = "This is a test.\n# LTeX: language=de-DE\n% LTeX: language=en-US\n";
    List<CodeFragment> codeFragments = fragmentizer.fragmentize(code);
    Assertions.assertEquals(1, codeFragments.size());
    Assertions.assertEquals("plaintext", codeFragments.get(0).getCodeLanguageId());
    Assertions.assertEquals(code, codeFragments.get(0).getCode());
    Assertions.assertEquals(0, codeFragments.get(0).getFromPos());
    Assertions.assertEquals("en-US", codeFragments.get(0).getSettings().getLanguageShortCode());
  }
}
