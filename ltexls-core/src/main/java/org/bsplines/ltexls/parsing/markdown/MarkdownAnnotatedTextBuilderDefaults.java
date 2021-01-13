/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.markdown;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class MarkdownAnnotatedTextBuilderDefaults {
  private static final List<MarkdownNodeSignature> defaultMarkdownNodeSignatures =
      createDefaultMarkdownNodeSignatures();

  private static List<MarkdownNodeSignature> createDefaultMarkdownNodeSignatures() {
    List<MarkdownNodeSignature> list = new ArrayList<>();

    list.add(new MarkdownNodeSignature("AutoLink", MarkdownNodeSignature.Action.DUMMY));
    list.add(new MarkdownNodeSignature("Code", MarkdownNodeSignature.Action.DUMMY));
    list.add(new MarkdownNodeSignature("CodeBlock"));
    list.add(new MarkdownNodeSignature("FencedCodeBlock"));
    list.add(new MarkdownNodeSignature("GitLabInlineMath", MarkdownNodeSignature.Action.DUMMY));
    list.add(new MarkdownNodeSignature("IndentedCodeBlock"));

    return list;
  }

  private MarkdownAnnotatedTextBuilderDefaults() {
  }

  public static List<MarkdownNodeSignature> getDefaultMarkdownNodeSignatures() {
    return Collections.unmodifiableList(defaultMarkdownNodeSignatures);
  }
}
