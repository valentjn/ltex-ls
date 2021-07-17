/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.html;

import java.util.List;
import org.bsplines.ltexls.parsing.CodeFragment;
import org.bsplines.ltexls.parsing.CodeFragmentizer;
import org.bsplines.ltexls.settings.Settings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HtmlFragmentizerTest {
  @Test
  public void test() {
    CodeFragmentizer fragmentizer = CodeFragmentizer.create("html");
    String code =
        "Sentence 1\n"
        + "\n  <!-- ltex: language=de-DE-->      \n\nSentence 2\n"
        + "\n<!--\t\t\tltex:\t\t\t\tlanguage=en-US\t\t-->\n\nSentence 3\n";
    List<CodeFragment> codeFragments = fragmentizer.fragmentize(code, new Settings());
    Assertions.assertEquals(1, codeFragments.size());
    Assertions.assertEquals("html", codeFragments.get(0).getCodeLanguageId());
    Assertions.assertEquals(code, codeFragments.get(0).getCode());
    Assertions.assertEquals(0, codeFragments.get(0).getFromPos());
    Assertions.assertEquals("en-US", codeFragments.get(0).getSettings().getLanguageShortCode());
  }
}
