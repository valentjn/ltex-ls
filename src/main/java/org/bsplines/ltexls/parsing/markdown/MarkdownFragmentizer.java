package org.bsplines.ltexls.parsing.markdown;

import java.util.regex.Pattern;

import org.bsplines.ltexls.Settings;
import org.bsplines.ltexls.parsing.RegexCodeFragmentizer;

public class MarkdownFragmentizer extends RegexCodeFragmentizer {
  private static Pattern pattern = Pattern.compile(
      "^\\s*\\[[^\\]]+\\]:\\s*<>\\s*\"\\s*(?i)ltex(?-i):(?<settings>.*?)\"\\s*$",
      Pattern.MULTILINE);

  public MarkdownFragmentizer(String codeLanguageId, Settings originalSettings) {
    super(codeLanguageId, originalSettings, pattern);
  }
}
