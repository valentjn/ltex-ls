/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.program;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.bsplines.ltexls.parsing.CodeFragment;
import org.bsplines.ltexls.parsing.RegexCodeFragmentizer;
import org.bsplines.ltexls.settings.Settings;

public class ProgramFragmentizer extends RegexCodeFragmentizer {
  public ProgramFragmentizer(String codeLanguageId) {
    super(codeLanguageId, (new ProgramCommentPatterns(codeLanguageId)).getMagicCommentPattern());
  }

  @Override
  public List<CodeFragment> fragmentize(String code, Settings originalSettings) {
    List<CodeFragment> oldCodeFragments = super.fragmentize(code, originalSettings);
    ArrayList<CodeFragment> result = new ArrayList<>();

    String[] ruleIdsToDisable = {
      "COPYRIGHT",
      "DASH_RULE",
      "R_SYMBOL",
      "UPPERCASE_SENTENCE_START",
      "WHITESPACE_RULE",
    };

    for (CodeFragment oldCodeFragment : oldCodeFragments) {
      Settings settings = oldCodeFragment.getSettings();

      HashSet<String> dictionary = new HashSet<>(settings.getDictionary());
      dictionary.add("@param");
      dictionary.add("param");
      dictionary.add("@return");

      HashSet<String> disabledRules = new HashSet<>(settings.getDisabledRules());

      for (String ruleId : ruleIdsToDisable) {
        if (!settings.getEnabledRules().contains(ruleId)) {
          disabledRules.add(ruleId);
        }
      }

      result.add(oldCodeFragment.withSettings(
          settings.withDictionary(dictionary).withDisabledRules(disabledRules)));
    }

    return result;
  }
}
