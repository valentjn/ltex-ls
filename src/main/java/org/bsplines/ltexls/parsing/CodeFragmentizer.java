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
import org.bsplines.ltexls.parsing.bibtex.BibtexFragmentizer;
import org.bsplines.ltexls.parsing.html.HtmlFragmentizer;
import org.bsplines.ltexls.parsing.latex.LatexFragmentizer;
import org.bsplines.ltexls.parsing.markdown.MarkdownFragmentizer;
import org.bsplines.ltexls.parsing.org.OrgFragmentizer;
import org.bsplines.ltexls.parsing.plaintext.PlaintextFragmentizer;
import org.bsplines.ltexls.parsing.restructuredtext.RestructuredtextFragmentizer;
import org.bsplines.ltexls.settings.Settings;
import org.bsplines.ltexls.tools.Tools;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class CodeFragmentizer {
  private static Map<String, Function<String, CodeFragmentizer>>
      constructorMap = new HashMap<>();

  static {
    constructorMap.put("bibtex", (String codeLanguageId) ->
        new BibtexFragmentizer(codeLanguageId));
    constructorMap.put("html", (String codeLanguageId) ->
        new HtmlFragmentizer(codeLanguageId));
    constructorMap.put("latex", (String codeLanguageId) ->
        new LatexFragmentizer(codeLanguageId));
    constructorMap.put("markdown", (String codeLanguageId) ->
        new MarkdownFragmentizer(codeLanguageId));
    constructorMap.put("org", (String codeLanguageId) ->
        new OrgFragmentizer(codeLanguageId));
    constructorMap.put("plaintext", (String codeLanguageId) ->
        new PlaintextFragmentizer(codeLanguageId));
    constructorMap.put("restructuredtext", (String codeLanguageId) ->
        new RestructuredtextFragmentizer(codeLanguageId));
    constructorMap.put("rsweave", (String codeLanguageId) ->
        new LatexFragmentizer(codeLanguageId));
    constructorMap.put("tex", (String codeLanguageId) ->
        new LatexFragmentizer(codeLanguageId));
  }

  protected String codeLanguageId;

  protected CodeFragmentizer(String codeLanguageId) {
    this.codeLanguageId = codeLanguageId;
  }

  public static CodeFragmentizer create(String codeLanguageId) {
    @Nullable Function<String, CodeFragmentizer> constructor = constructorMap.get(codeLanguageId);

    if (constructor != null) {
      return constructor.apply(codeLanguageId);
    } else {
      Tools.logger.warning(Tools.i18n("unsupportedCodeLanguageId", codeLanguageId));
      return new PlaintextFragmentizer("plaintext");
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
