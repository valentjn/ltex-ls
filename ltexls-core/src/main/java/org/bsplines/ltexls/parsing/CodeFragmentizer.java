/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.bsplines.ltexls.Tools;
import org.bsplines.ltexls.parsing.latex.LatexFragmentizer;
import org.bsplines.ltexls.parsing.markdown.MarkdownFragmentizer;
import org.bsplines.ltexls.parsing.plaintext.PlaintextFragmentizer;
import org.bsplines.ltexls.settings.Settings;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class CodeFragmentizer {
  private static Map<String, Function<String, CodeFragmentizer>>
      constructorMap = new HashMap<>();

  protected String codeLanguageId;

  static {
    constructorMap.put("latex", (String codeLanguageId) ->
        new LatexFragmentizer(codeLanguageId));
    constructorMap.put("markdown", (String codeLanguageId) ->
        new MarkdownFragmentizer(codeLanguageId));
    constructorMap.put("plaintext", (String codeLanguageId) ->
        new PlaintextFragmentizer(codeLanguageId));
    constructorMap.put("rsweave", (String codeLanguageId) ->
        new LatexFragmentizer(codeLanguageId));
  }

  public CodeFragmentizer(String codeLanguageId) {
    this.codeLanguageId = codeLanguageId;
  }

  /**
   * Create a @c CodeFragmentizer according to the given code langugage.
   *
   * @param codeLanguageId ID of the code language
   * @return corresponding @c CodeFragmentizer
   */
  public static CodeFragmentizer create(String codeLanguageId) {
    @Nullable Function<String, CodeFragmentizer> constructor = constructorMap.get(codeLanguageId);

    if (constructor != null) {
      return constructor.apply(codeLanguageId);
    } else {
      Tools.logger.warning(Tools.i18n("unsupportedCodeLanguageId", codeLanguageId));
      return new PlaintextFragmentizer(codeLanguageId);
    }
  }

  public abstract List<CodeFragment> fragmentize(String code, Settings originalSettings);

  public List<CodeFragment> fragmentize(List<CodeFragment> fragments) {
    List<CodeFragment> newFragments = new ArrayList<>();

    for (CodeFragment oldFragment : fragments) {
      List<CodeFragment> curNewFragments = fragmentize(
          oldFragment.getCode(), oldFragment.getSettings());

      for (CodeFragment newFragment : curNewFragments) {
        newFragments.add(newFragment.withFromPos(
            newFragment.getFromPos() + oldFragment.getFromPos()));
      }
    }

    return newFragments;
  }
}
