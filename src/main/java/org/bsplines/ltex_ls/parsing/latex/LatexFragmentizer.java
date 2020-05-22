package org.bsplines.ltex_ls.parsing.latex;

import org.bsplines.ltex_ls.Settings;
import org.bsplines.ltex_ls.parsing.RegexCodeFragmentizer;

public class LatexFragmentizer extends RegexCodeFragmentizer {
  public LatexFragmentizer(String codeLanguageId, Settings originalSettings) {
    super(codeLanguageId, originalSettings, "^\\s*%\\s*(?i)ltex(?-i):(?<settings>.*?)$");
  }
}
