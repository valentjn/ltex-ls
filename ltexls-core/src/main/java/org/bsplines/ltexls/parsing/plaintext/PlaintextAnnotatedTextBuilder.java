package org.bsplines.ltexls.parsing.plaintext;

import org.bsplines.ltexls.parsing.CodeAnnotatedTextBuilder;

public class PlaintextAnnotatedTextBuilder extends CodeAnnotatedTextBuilder {
  @Override
  public CodeAnnotatedTextBuilder addCode(String code) {
    addText(code);
    return this;
  }
}
