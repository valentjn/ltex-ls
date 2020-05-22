package org.bsplines.ltex_ls.parsing;

import org.languagetool.markup.AnnotatedText;

public class AnnotatedTextFragment {
  private AnnotatedText annotatedText;
  private CodeFragment codeFragment;

  public AnnotatedTextFragment(AnnotatedText annotatedText, CodeFragment codeFragment) {
    this.annotatedText = annotatedText;
    this.codeFragment = codeFragment;
  }

  public AnnotatedText getAnnotatedText() { return annotatedText; }
  public CodeFragment getCodeFragment() { return codeFragment; }
}
