/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.org;

import org.bsplines.ltexls.parsing.restructuredtext.RestructuredtextFragmentizerTest;
import org.bsplines.ltexls.settings.Settings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OrgFragmentizerTest {
  @Test
  public void testFragmentizer() {
    RestructuredtextFragmentizerTest.assertFragmentizer("org",
        "Sentence 1\n"
        + "\n # ltex: language=de-DE\n\nSentence 2\n"
        + "\n\t#\tltex:\tlanguage=en-US\n\nSentence 3\n");
  }

  @Test
  public void testWrongSettings() {
    OrgFragmentizer fragmentizer = new OrgFragmentizer("org");
    Assertions.assertDoesNotThrow(() -> fragmentizer.fragmentize(
        "Sentence 1\n\n# ltex: languagede-DE\n\nSentence 2\n", new Settings()));
    Assertions.assertDoesNotThrow(() -> fragmentizer.fragmentize(
        "Sentence 1\n\n# ltex: unknownKey=abc\n\nSentence 2\n", new Settings()));
  }
}
