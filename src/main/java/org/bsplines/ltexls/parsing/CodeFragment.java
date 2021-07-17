/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing;

import org.bsplines.ltexls.languagetool.LanguageToolRuleMatch;
import org.bsplines.ltexls.settings.Settings;

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

  public CodeFragment(CodeFragment obj) {
    this(obj.codeLanguageId, obj.code, obj.fromPos, obj.settings);
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

  public CodeFragment withFromPos(int fromPos) {
    CodeFragment obj = new CodeFragment(this);
    obj.fromPos = fromPos;
    return obj;
  }

  public CodeFragment withSettings(Settings settings) {
    CodeFragment obj = new CodeFragment(this);
    obj.settings = settings;
    return obj;
  }

  public boolean contains(LanguageToolRuleMatch match) {
    return ((match.getFromPos() >= this.fromPos)
        && (match.getToPos() <= this.fromPos + this.code.length()));
  }
}
