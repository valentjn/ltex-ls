/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.markdown;

import java.util.regex.Pattern;
import org.bsplines.ltexls.Settings;
import org.bsplines.ltexls.parsing.RegexCodeFragmentizer;

public class MarkdownFragmentizer extends RegexCodeFragmentizer {
  private static Pattern pattern = Pattern.compile(
      "^\\s*\\[[^\\]]+\\]:\\s*<>\\s*\"\\s*(?i)ltex(?-i):(?<settings>.*?)\"\\s*$",
      Pattern.MULTILINE);

  public MarkdownFragmentizer(String codeLanguageId, Settings originalSettings) {
    super(codeLanguageId, originalSettings, pattern);
  }
}
