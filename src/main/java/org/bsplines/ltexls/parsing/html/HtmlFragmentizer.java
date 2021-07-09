/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.html;

import java.util.regex.Pattern;
import org.bsplines.ltexls.parsing.RegexCodeFragmentizer;

public class HtmlFragmentizer extends RegexCodeFragmentizer {
  private static final Pattern pattern = Pattern.compile(
      "^[ \t]*<!--[ \t]*(?i)ltex(?-i):(.*?)[ \t]*-->[ \t]*$",
      Pattern.MULTILINE);

  public HtmlFragmentizer(String codeLanguageId) {
    super(codeLanguageId, pattern);
  }
}
