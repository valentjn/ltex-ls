/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.tools

import org.junit.platform.suite.api.IncludeEngines
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

@IncludeEngines("junit-jupiter")
class I18nTest {
  @Test
  fun testFormat() {
    assertEquals("Add 'test' to dictionary", I18n.format("addWordToDictionary", "test"))

    assertEquals(
      "i18n message with key 'abc' not found, message arguments: 'def', '42', 'null'",
      I18n.format("abc", "def", 42, null)
    )

    assertContains(I18n.format(NullPointerException("abc")), Regex(
      "The following exception occurred:[\r\n]+java\\.lang\\.NullPointerException: abc[\r\n]+.*",
      RegexOption.DOT_MATCHES_ALL,
    ))
  }
}
