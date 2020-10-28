/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.settings;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HiddenFalsePositiveTest {
  @Test
  public void doTest() {
    HiddenFalsePositive pair = new HiddenFalsePositive("a", "b");
    Assertions.assertEquals(pair, new HiddenFalsePositive(pair));
    Assertions.assertEquals(pair, HiddenFalsePositive.fromJsonString(
        "{\"rule\":\"a\",\"sentence\":\"b\"}"));
    Assertions.assertNotEquals(pair, new HiddenFalsePositive("X", "b"));
    Assertions.assertNotEquals(pair, new HiddenFalsePositive("a", "X"));
    Assertions.assertDoesNotThrow(() -> pair.hashCode());
    Assertions.assertEquals("a", pair.getRuleId());
    Assertions.assertEquals("b", pair.getSentenceString());
    Assertions.assertEquals("b", pair.getSentencePattern().pattern());
  }
}
