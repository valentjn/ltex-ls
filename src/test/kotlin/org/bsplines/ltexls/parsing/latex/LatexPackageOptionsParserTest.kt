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
class LatexPackageOptionsParserTest {
  @Test
  fun testParse() {
    val options = LatexPackageOptionsParser.parse(
      """
        option1\,=value1,
      option2 = \value2 ,% option3 = value3,
      option4 =% value4,
      {This is\} a \textbf{test}, option5 = value5.},
      
      """.trimIndent()
    )

    assertEquals(4, options.size)

    assertEquals("  option1\\,", options[0].key)
    assertEquals("option1\\,", options[0].keyInfo.plainText)
    assertEquals(12, options[0].valueInfo.fromPos)
    assertEquals("value1", options[0].value)
    assertEquals("value1", options[0].valueInfo.plainText)

    assertEquals("\noption2 ", options[1].key)
    assertEquals("option2", options[1].keyInfo.plainText)
    assertEquals(29, options[1].valueInfo.fromPos)
    assertEquals(" \\value2 ", options[1].value)
    assertEquals("\\value2", options[1].valueInfo.plainText)

    assertEquals("% option3 = value3,\noption4 ", options[2].key)
    assertEquals("option4", options[2].keyInfo.plainText)
    assertEquals(68, options[2].valueInfo.fromPos)
    assertEquals("% value4,\n{This is\\} a \\textbf{test}, option5 = value5.}", options[2].value)
    assertEquals("This is\\} a \\textbftest, option5 = value5.", options[2].valueInfo.plainText)

    assertEquals("\n", options[3].key)
    assertEquals("", options[3].keyInfo.plainText)
    assertEquals(-1, options[3].valueInfo.fromPos)
    assertEquals("", options[3].value)
    assertEquals("", options[3].valueInfo.plainText)
  }
}
