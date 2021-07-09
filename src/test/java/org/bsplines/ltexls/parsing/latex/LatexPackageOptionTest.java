/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.latex;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LatexPackageOptionTest {
  @Test
  public void testProperties() {
    LatexPackageOption packageOption = new LatexPackageOption(
        "\\usepackage[foo=bar]{foobar}", 12, 15, "abc", 16, 19, "def");
    Assertions.assertEquals(12, packageOption.getKeyFromPos());
    Assertions.assertEquals(15, packageOption.getKeyToPos());
    Assertions.assertEquals("foo", packageOption.getKey());
    Assertions.assertEquals("abc", packageOption.getKeyAsPlainText());
    Assertions.assertEquals(16, packageOption.getValueFromPos());
    Assertions.assertEquals(19, packageOption.getValueToPos());
    Assertions.assertEquals("bar", packageOption.getValue());
    Assertions.assertEquals("def", packageOption.getValueAsPlainText());
  }
}
