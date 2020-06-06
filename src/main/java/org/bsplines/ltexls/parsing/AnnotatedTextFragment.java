package org.bsplines.ltexls.parsing;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.languagetool.markup.AnnotatedText;

public class AnnotatedTextFragment {
  private AnnotatedText annotatedText;
  private CodeFragment codeFragment;

  public AnnotatedTextFragment(AnnotatedText annotatedText, CodeFragment codeFragment) {
    this.annotatedText = annotatedText;
    this.codeFragment = codeFragment;
  }

  public AnnotatedText getAnnotatedText() {
    return annotatedText;
  }

  public CodeFragment getCodeFragment() {
    return codeFragment;
  }

  /**
   * Compute the inverse of the annotated text, where plain text and original text switch roles.
   *
   * @return inverted annotated text
   */
  public AnnotatedText invert() {
    List<Map.Entry<Integer, Integer>> mapping = annotatedText.getMapping();
    List<Map.Entry<Integer, Integer>> inverseMapping = new ArrayList<>();

    for (Map.Entry<Integer, Integer> entry : mapping) {
      inverseMapping.add(new AbstractMap.SimpleEntry<>(entry.getValue(), entry.getKey()));
    }

    return new AnnotatedText(Collections.emptyList(), inverseMapping, Collections.emptyMap(),
        Collections.emptyMap());
  }
}
