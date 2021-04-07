/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.plaintext;

import org.bsplines.ltexls.parsing.CodeAnnotatedTextBuilder;
import org.bsplines.ltexls.settings.Settings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.languagetool.markup.AnnotatedText;

public class PlaintextAnnotatedTextBuilderTest {
  @Test
  public void test() {
    CodeAnnotatedTextBuilder builder = CodeAnnotatedTextBuilder.create("plaintext");
    builder.addCode("This is \\textbf{a} `test`.\n");
    AnnotatedText annotatedText = builder.build();
    Assertions.assertEquals("This is \\textbf{a} `test`.\n", annotatedText.getPlainText());
    Assertions.assertDoesNotThrow(() -> builder.setSettings(new Settings()));
  }
}
