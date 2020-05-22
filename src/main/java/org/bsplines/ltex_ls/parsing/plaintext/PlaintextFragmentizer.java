package org.bsplines.ltex_ls.parsing.plaintext;

import java.util.Collections;
import java.util.List;

import org.bsplines.ltex_ls.Settings;
import org.bsplines.ltex_ls.parsing.CodeFragment;
import org.bsplines.ltex_ls.parsing.CodeFragmentizer;

public class PlaintextFragmentizer extends CodeFragmentizer {
  public PlaintextFragmentizer(String codeLanguageId, Settings originalSettings) {
    super(codeLanguageId, originalSettings);
  }

  @Override
  public List<CodeFragment> fragmentize(String code) {
    return Collections.singletonList(new CodeFragment(codeLanguageId, code, 0, originalSettings));
  }
}
