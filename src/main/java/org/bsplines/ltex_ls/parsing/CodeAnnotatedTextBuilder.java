package org.bsplines.ltex_ls.parsing;

import java.util.Map;
import java.util.HashMap;
import java.util.function.Function;

import org.bsplines.ltex_ls.Settings;
import org.bsplines.ltex_ls.parsing.latex.LatexAnnotatedTextBuilder;
import org.bsplines.ltex_ls.parsing.markdown.MarkdownAnnotatedTextBuilder;
import org.bsplines.ltex_ls.parsing.plaintext.PlaintextAnnotatedTextBuilder;

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

  public static CodeAnnotatedTextBuilder create(String codeLanguageId) {
    return constructorMap.get(codeLanguageId).apply(codeLanguageId);
  }

  public abstract CodeAnnotatedTextBuilder addCode(String code);
  public void setSettings(Settings settings) {}
}
