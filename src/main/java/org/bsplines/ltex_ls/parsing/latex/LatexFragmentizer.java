package org.bsplines.ltex_ls.parsing.latex;

import java.util.regex.Pattern;

import org.bsplines.ltex_ls.Settings;
import org.bsplines.ltex_ls.parsing.RegexCodeFragmentizer;

public class LatexFragmentizer extends RegexCodeFragmentizer {
  private static Pattern pattern = Pattern.compile("^\\s*%\\s*(?i)ltex(?-i):(?<settings>.*?)$",
      Pattern.MULTILINE);

  public LatexFragmentizer(String codeLanguageId, Settings originalSettings) {
    super(codeLanguageId, originalSettings, pattern);
  }
}
