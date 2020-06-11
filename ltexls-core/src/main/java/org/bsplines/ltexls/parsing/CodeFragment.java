package org.bsplines.ltexls.parsing;

import org.bsplines.ltexls.Settings;
import org.bsplines.ltexls.languagetool.LanguageToolRuleMatch;

public class CodeFragment {
  private String codeLanguageId;
  private String code;
  private int fromPos;
  private Settings settings;

  /**
   * Constructor.
   *
   * @param codeLanguageId ID of the code language
   * @param code code of the fragment
   * @param fromPos from position of the fragment (inclusive)
   * @param settings settings to apply to this fragment
   */
  public CodeFragment(String codeLanguageId, String code, int fromPos, Settings settings) {
    this.codeLanguageId = codeLanguageId;
    this.code = code;
    this.fromPos = fromPos;
    this.settings = settings;
  }

  public String getCodeLanguageId() {
    return this.codeLanguageId;
  }

  public String getCode() {
    return this.code;
  }

  public int getFromPos() {
    return this.fromPos;
  }

  public Settings getSettings() {
    return this.settings;
  }

  public boolean contains(LanguageToolRuleMatch match) {
    return ((match.getFromPos() >= this.fromPos)
        && (match.getToPos() <= this.fromPos + this.code.length()));
  }
}
