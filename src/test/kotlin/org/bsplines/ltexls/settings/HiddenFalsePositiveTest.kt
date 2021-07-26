/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.settings

import org.junit.platform.suite.api.IncludeEngines
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@IncludeEngines("junit-jupiter")
class HiddenFalsePositiveTest {
  @Test
  fun test() {
    val hiddenFalsePositive = HiddenFalsePositive("a", "b")
    assertEquals(
      hiddenFalsePositive, HiddenFalsePositive.fromJsonString(
        "{\"rule\":\"a\",\"sentence\":\"b\"}"
      )
    )
    assertNotEquals(hiddenFalsePositive, HiddenFalsePositive("X", "b"))
    assertNotEquals(hiddenFalsePositive, HiddenFalsePositive("a", "X"))
    assertEquals("a", hiddenFalsePositive.ruleId)
    assertEquals("b", hiddenFalsePositive.sentenceRegexString)
    assertEquals("b", hiddenFalsePositive.sentenceRegex.pattern)
  }
}
