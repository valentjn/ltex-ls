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
import org.bsplines.ltexls.server.LtexTextDocumentItem;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.languagetool.markup.AnnotatedText;

public class AnnotatedTextFragment {
  private AnnotatedText annotatedText;
  private CodeFragment codeFragment;
  private LtexTextDocumentItem document;
  private @MonotonicNonNull String plainText;
  private @MonotonicNonNull AnnotatedText inverseAnnotatedText;

  public AnnotatedTextFragment(AnnotatedText annotatedText, CodeFragment codeFragment,
        LtexTextDocumentItem document) {
    this.annotatedText = annotatedText;
    this.codeFragment = codeFragment;
    this.document = document;
  }

  public AnnotatedText getAnnotatedText() {
    return this.annotatedText;
  }

  public CodeFragment getCodeFragment() {
    return this.codeFragment;
  }

  public LtexTextDocumentItem getDocument() {
    return this.document;
  }

  public String getSubstringOfPlainText(int fromPos, int toPos) {
    if (this.plainText == null) this.plainText = this.annotatedText.getPlainText();

    if (this.inverseAnnotatedText == null) {
      List<Map.Entry<Integer, Integer>> mapping = this.annotatedText.getMapping();
      List<Map.Entry<Integer, Integer>> inverseMapping = new ArrayList<>();

      for (Map.Entry<Integer, Integer> entry : mapping) {
        inverseMapping.add(new AbstractMap.SimpleEntry<>(entry.getValue(), entry.getKey()));
      }

      this.inverseAnnotatedText = new AnnotatedText(Collections.emptyList(), inverseMapping,
          Collections.emptyMap(), Collections.emptyMap());
    }

    return this.plainText.substring(
        this.inverseAnnotatedText.getOriginalTextPositionFor(fromPos, true),
        this.inverseAnnotatedText.getOriginalTextPositionFor(toPos, true));
  }
}
