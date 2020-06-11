package org.bsplines.ltexls.parsing.plaintext;

import java.util.Collections;
import java.util.List;

import org.bsplines.ltexls.Settings;
import org.bsplines.ltexls.parsing.CodeFragment;
import org.bsplines.ltexls.parsing.CodeFragmentizer;

public class PlaintextFragmentizer extends CodeFragmentizer {
  public PlaintextFragmentizer(String codeLanguageId, Settings originalSettings) {
    super(codeLanguageId, originalSettings);
  }

  @Override
  public List<CodeFragment> fragmentize(String code) {
    return Collections.singletonList(new CodeFragment(codeLanguageId, code, 0, originalSettings));
  }
}
