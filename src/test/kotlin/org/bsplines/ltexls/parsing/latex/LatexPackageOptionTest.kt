/* Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.latex

import org.junit.platform.suite.api.IncludeEngines
import kotlin.test.Test
import kotlin.test.assertEquals

@IncludeEngines("junit-jupiter")
class LatexPackageOptionTest {
  @Test
  fun testProperties() {
    val packageOption = LatexPackageOption(
      "\\usepackage[foo=bar]{foobar}",
      LatexPackageOption.KeyValueInfo(12, 15, "abc"),
      LatexPackageOption.KeyValueInfo(16, 19, "def"),
    )

    assertEquals("foo", packageOption.key)
    assertEquals(12, packageOption.keyInfo.fromPos)
    assertEquals(15, packageOption.keyInfo.toPos)
    assertEquals("abc", packageOption.keyInfo.plainText)
    assertEquals("bar", packageOption.value)
    assertEquals(16, packageOption.valueInfo.fromPos)
    assertEquals(19, packageOption.valueInfo.toPos)
    assertEquals("def", packageOption.valueInfo.plainText)
  }
}
