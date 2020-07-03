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
import org.bsplines.ltexls.Settings;
import org.bsplines.ltexls.Tools;
import org.bsplines.ltexls.parsing.latex.LatexAnnotatedTextBuilder;
import org.bsplines.ltexls.parsing.markdown.MarkdownAnnotatedTextBuilder;
import org.bsplines.ltexls.parsing.plaintext.PlaintextAnnotatedTextBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.languagetool.markup.AnnotatedTextBuilder;

public abstract class CodeAnnotatedTextBuilder extends AnnotatedTextBuilder {
  private static Map<String, Function<String, CodeAnnotatedTextBuilder>> constructorMap =
      new HashMap<>();

  static {
    constructorMap.put("latex", (String codeLanguageId) ->
        new LatexAnnotatedTextBuilder(codeLanguageId));
    constructorMap.put("markdown", (String codeLanguageId) ->
        new MarkdownAnnotatedTextBuilder());
    constructorMap.put("plaintext", (String codeLanguageId) ->
        new PlaintextAnnotatedTextBuilder());
    constructorMap.put("rsweave", (String codeLanguageId) ->
        new LatexAnnotatedTextBuilder(codeLanguageId));
  }

  /**
   * Create a @c CodeAnnotatedTextBuilder according to the given code langugage.
   *
   * @param codeLanguageId ID of the code language
   * @return corresponding @c CodeAnnotatedTextBuilder
   */
  public static CodeAnnotatedTextBuilder create(String codeLanguageId) {
    @Nullable Function<String, CodeAnnotatedTextBuilder> constructor =
        constructorMap.get(codeLanguageId);

    if (constructor != null) {
      return constructor.apply(codeLanguageId);
    } else {
      Tools.logger.warning(Tools.i18n("invalidCodeLanguageId", codeLanguageId));
      return new PlaintextAnnotatedTextBuilder();
    }
  }

  public abstract CodeAnnotatedTextBuilder addCode(String code);

  public void setSettings(Settings settings) {
  }
}
