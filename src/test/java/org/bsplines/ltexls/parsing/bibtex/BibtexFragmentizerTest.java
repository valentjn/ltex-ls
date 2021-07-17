/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.bibtex;

import java.util.List;
import org.bsplines.ltexls.parsing.CodeFragment;
import org.bsplines.ltexls.parsing.CodeFragmentizer;
import org.bsplines.ltexls.settings.Settings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BibtexFragmentizerTest {
  @Test
  public void test() {
    Settings settings = new Settings();
    CodeFragmentizer fragmentizer = CodeFragmentizer.create("bibtex");
    List<CodeFragment> codeFragments = fragmentizer.fragmentize(
        "@article{some-label,\n"
        + "  name = {Some Name},\n"
        + "  description = {This is a test.}\n"
        + "}\n"
        + "\n"
        + "@entry{some-label2,\n"
        + "  name = {Some Other Name},\n"
        + "  description = {This is another\n"
        + "  test.},\n"
        + "}\n"
        + "\n"
        + "@abbreviation{some-label3,\n"
        + "  short =shortform,\n"
        + "  see   = {abc},\n"
        + "  long  = longform,\n"
        + " }\n",
        settings);
    Assertions.assertEquals(6, codeFragments.size());

    for (CodeFragment codeFragment : codeFragments) {
      Assertions.assertEquals("latex", codeFragment.getCodeLanguageId());
    }

    Assertions.assertEquals(" {Some Name}", codeFragments.get(0).getCode());
    Assertions.assertEquals(29, codeFragments.get(0).getFromPos());

    Assertions.assertEquals(" {This is a test.}\n", codeFragments.get(1).getCode());
    Assertions.assertEquals(58, codeFragments.get(1).getFromPos());

    Assertions.assertEquals(" {Some Other Name}", codeFragments.get(2).getCode());
    Assertions.assertEquals(108, codeFragments.get(2).getFromPos());

    Assertions.assertEquals(" {This is another\n  test.}", codeFragments.get(3).getCode());
    Assertions.assertEquals(143, codeFragments.get(3).getFromPos());

    Assertions.assertEquals("shortform", codeFragments.get(4).getCode());
    Assertions.assertEquals(210, codeFragments.get(4).getFromPos());

    Assertions.assertEquals(" longform", codeFragments.get(5).getCode());
    Assertions.assertEquals(247, codeFragments.get(5).getFromPos());
  }
}
