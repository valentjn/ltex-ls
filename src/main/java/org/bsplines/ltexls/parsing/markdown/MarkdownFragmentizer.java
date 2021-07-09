/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.markdown;

import java.util.regex.Pattern;
import org.bsplines.ltexls.parsing.RegexCodeFragmentizer;

public class MarkdownFragmentizer extends RegexCodeFragmentizer {
  private static final Pattern pattern = Pattern.compile(
      "^[ \t]*\\[[^\\]]+\\]:[ \t]*<>[ \t]*\"[ \t]*(?i)ltex(?-i):(.*?)\"[ \t]*$|"
      + "^[ \t]*<!--[ \t]*(?i)ltex(?-i):(.*?)[ \t]*-->[ \t]*$",
      Pattern.MULTILINE);

  public MarkdownFragmentizer(String codeLanguageId) {
    super(codeLanguageId, pattern);
  }
}
