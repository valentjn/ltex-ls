package org.bsplines.ltex_ls.parsing;

import org.bsplines.ltex_ls.Settings;

public class CodeFragment {
  private String codeLanguageId;
  private String code;
  private int fromPos;
  private Settings settings;

  public CodeFragment(String codeLanguageId, String code, int fromPos, Settings settings) {
    this.codeLanguageId = codeLanguageId;
    this.code = code;
    this.fromPos = fromPos;
    this.settings = settings;
  }

  public String getCodeLanguageId() { return codeLanguageId; }
  public String getCode() { return code; }
  public int getFromPos() { return fromPos; }
  public Settings getSettings() { return settings; }
}
