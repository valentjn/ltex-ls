/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.tools;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ToolsTest {
  @Test
  public void testI18n() {
    Assertions.assertEquals("Add 'test' to dictionary", Tools.i18n("addWordToDictionary", "test"));

    @SuppressWarnings("assignment.type.incompatible")
    @NonNull String nullString = null;
    Assertions.assertEquals(
        "could not get i18n message with null key, message arguments: 'def', '42', 'null'",
        Tools.i18n(nullString, "def", 42, null));

    Assertions.assertEquals(
        "i18n message with key 'abc' not found, message arguments: 'def', '42', 'null'",
        Tools.i18n("abc", "def", 42, null));

    Assertions.assertTrue(Tools.i18n(new NullPointerException("abc")).matches(
        "(?s)The following exception occurred:[\r\n]+"
        + "java\\.lang\\.NullPointerException: abc[\r\n]+.*"));
  }
}
