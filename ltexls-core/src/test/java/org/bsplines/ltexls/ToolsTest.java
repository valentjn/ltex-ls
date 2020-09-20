/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ToolsTest {
  @Test
  public void testI18n() {
    Assertions.assertEquals("Add 'test' to dictionary", Tools.i18n("addWordToDictionary", "test"));
    Assertions.assertTrue(Tools.i18n(new NullPointerException("abc")).matches(
        "(?s)The following exception occurred:[\r\n]+"
        + "java\\.lang\\.NullPointerException: abc[\r\n]+.*"));
  }
}
