package org.bsplines.ltex_ls.parsing.markdown;

import java.util.regex.Pattern;

import org.bsplines.ltex_ls.Settings;
import org.bsplines.ltex_ls.parsing.RegexCodeFragmentizer;

public class MarkdownFragmentizer extends RegexCodeFragmentizer {
  private static Pattern pattern = Pattern.compile(
      "^\\s*\\[[^\\]]+\\]:\\s*<>\\s*\"\\s*(?i)ltex(?-i):(?<settings>.*?)\"\\s*$",
      Pattern.MULTILINE);

  public MarkdownFragmentizer(String codeLanguageId, Settings originalSettings) {
    super(codeLanguageId, originalSettings, pattern);
  }
}
