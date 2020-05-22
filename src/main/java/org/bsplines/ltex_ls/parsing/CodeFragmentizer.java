package org.bsplines.ltex_ls.parsing;

import java.util.*;
import java.util.function.Function;

import org.bsplines.ltex_ls.Settings;
import org.bsplines.ltex_ls.parsing.latex.LatexFragmentizer;
import org.bsplines.ltex_ls.parsing.markdown.MarkdownFragmentizer;
import org.bsplines.ltex_ls.parsing.plaintext.PlaintextFragmentizer;

public abstract class CodeFragmentizer {
  private static Map<String, Function<String, Function<Settings, CodeFragmentizer>>>
      constructorMap = new HashMap<>();

  protected String codeLanguageId;
  protected Settings originalSettings;

  static {
    constructorMap.put("latex", (String codeLanguageId) -> (Settings originalSettings) ->
        new LatexFragmentizer(codeLanguageId, originalSettings));
    constructorMap.put("markdown", (String codeLanguageId) -> (Settings originalSettings) ->
        new MarkdownFragmentizer(codeLanguageId, originalSettings));
    constructorMap.put("plaintext", (String codeLanguageId) -> (Settings originalSettings) ->
        new PlaintextFragmentizer(codeLanguageId, originalSettings));
    constructorMap.put("rsweave", (String codeLanguageId) -> (Settings originalSettings) ->
        new LatexFragmentizer(codeLanguageId, originalSettings));
  }

  public CodeFragmentizer(String codeLanguageId, Settings originalSettings) {
    this.codeLanguageId = codeLanguageId;
    this.originalSettings = originalSettings;
  }

  public static CodeFragmentizer create(String codeLanguageId, Settings originalSettings) {
    return constructorMap.get(codeLanguageId).apply(codeLanguageId).apply(originalSettings);
  }

  public abstract List<CodeFragment> fragmentize(String code);
}
