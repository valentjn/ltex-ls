package org.bsplines.ltexls.parsing;

import java.util.*;
import java.util.function.Function;

import org.bsplines.ltexls.Settings;
import org.bsplines.ltexls.Tools;
import org.bsplines.ltexls.parsing.latex.LatexFragmentizer;
import org.bsplines.ltexls.parsing.markdown.MarkdownFragmentizer;
import org.bsplines.ltexls.parsing.plaintext.PlaintextFragmentizer;
import org.checkerframework.checker.nullness.qual.Nullable;

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
    @Nullable Function<String, Function<Settings, CodeFragmentizer>> constructor =
        constructorMap.get(codeLanguageId);

    if (constructor != null) {
      return constructor.apply(codeLanguageId).apply(originalSettings);
    } else {
      Tools.logger.warning(Tools.i18n("invalidCodeLanguageId", codeLanguageId));
      return new PlaintextFragmentizer("plaintext", originalSettings);
    }
  }

  public abstract List<CodeFragment> fragmentize(String code);
}
