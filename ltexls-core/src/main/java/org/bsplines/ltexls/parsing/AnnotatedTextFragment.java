/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

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
    return this.annotatedText;
  }

  public CodeFragment getCodeFragment() {
    return this.codeFragment;
  }

  /**
   * Compute the inverse of the annotated text, where plain text and original text switch roles.
   *
   * @return inverted annotated text
   */
  public AnnotatedText invert() {
    List<Map.Entry<Integer, Integer>> mapping = this.annotatedText.getMapping();
    List<Map.Entry<Integer, Integer>> inverseMapping = new ArrayList<>();

    for (Map.Entry<Integer, Integer> entry : mapping) {
      inverseMapping.add(new AbstractMap.SimpleEntry<>(entry.getValue(), entry.getKey()));
    }

    return new AnnotatedText(Collections.emptyList(), inverseMapping, Collections.emptyMap(),
        Collections.emptyMap());
  }
}
