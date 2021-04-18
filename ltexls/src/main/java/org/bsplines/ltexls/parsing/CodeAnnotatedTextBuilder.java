/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.bsplines.ltexls.parsing.latex.LatexAnnotatedTextBuilder;
import org.bsplines.ltexls.parsing.markdown.MarkdownAnnotatedTextBuilder;
import org.bsplines.ltexls.parsing.org.OrgAnnotatedTextBuilder;
import org.bsplines.ltexls.parsing.plaintext.PlaintextAnnotatedTextBuilder;
import org.bsplines.ltexls.parsing.restructuredtext.RestructuredtextAnnotatedTextBuilder;
import org.bsplines.ltexls.settings.Settings;
import org.bsplines.ltexls.tools.Tools;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.languagetool.markup.AnnotatedTextBuilder;

public abstract class CodeAnnotatedTextBuilder extends AnnotatedTextBuilder {
  private static final Map<String, Function<String, CodeAnnotatedTextBuilder>> constructorMap =
      new HashMap<>();

  static {
    constructorMap.put("bibtex", (String codeLanguageId) ->
        new LatexAnnotatedTextBuilder(codeLanguageId));
    constructorMap.put("latex", (String codeLanguageId) ->
        new LatexAnnotatedTextBuilder(codeLanguageId));
    constructorMap.put("markdown", (String codeLanguageId) ->
        new MarkdownAnnotatedTextBuilder(codeLanguageId));
    constructorMap.put("org", (String codeLanguageId) ->
        new OrgAnnotatedTextBuilder(codeLanguageId));
    constructorMap.put("plaintext", (String codeLanguageId) ->
        new PlaintextAnnotatedTextBuilder(codeLanguageId));
    constructorMap.put("restructuredtext", (String codeLanguageId) ->
        new RestructuredtextAnnotatedTextBuilder(codeLanguageId));
    constructorMap.put("rsweave", (String codeLanguageId) ->
        new LatexAnnotatedTextBuilder(codeLanguageId));
    constructorMap.put("tex", (String codeLanguageId) ->
        new LatexAnnotatedTextBuilder(codeLanguageId));
  }

  protected String codeLanguageId;

  protected CodeAnnotatedTextBuilder(String codeLanguageId) {
    this.codeLanguageId = codeLanguageId;
  }

  public static CodeAnnotatedTextBuilder create(String codeLanguageId) {
    @Nullable Function<String, CodeAnnotatedTextBuilder> constructor =
        constructorMap.get(codeLanguageId);

    if (constructor != null) {
      return constructor.apply(codeLanguageId);
    } else {
      Tools.logger.warning(Tools.i18n("unsupportedCodeLanguageId", codeLanguageId));
      return new PlaintextAnnotatedTextBuilder("plaintext");
    }
  }

  public abstract CodeAnnotatedTextBuilder addCode(String code);

  public void setSettings(Settings settings) {
  }
}
