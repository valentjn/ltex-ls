package org.bsplines.ltex_ls.parsing.markdown;

import org.bsplines.ltex_ls.Settings;
import org.bsplines.ltex_ls.parsing.RegexCodeFragmentizer;

public class MarkdownFragmentizer extends RegexCodeFragmentizer {
  public MarkdownFragmentizer(String codeLanguageId, Settings originalSettings) {
    super(codeLanguageId, originalSettings,
        "^\\s*\\[[^\\]]+\\]:\\s*<>\\s*\"\\s*(?i)ltex(?-i):(?<settings>.*?)\"\\s*$");
  }
}
