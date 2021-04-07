/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.latex;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LatexPackageOptionsParserTest {
  @Test
  public void testParse() {
    List<LatexPackageOption> options = LatexPackageOptionsParser.parse(
        "  option1\\,=value1,\n"
        + "option2 = \\value2 ,% option3 = value3,\n"
        + "option4 =% value4,\n"
        + "{This is\\} a \\textbf{test}, option5 = value5.},\n");

    Assertions.assertEquals(4, options.size());

    Assertions.assertEquals(0, options.get(0).getKeyFromPos());
    Assertions.assertEquals(11, options.get(0).getKeyToPos());
    Assertions.assertEquals("  option1\\,", options.get(0).getKey());
    Assertions.assertEquals("option1\\,", options.get(0).getKeyAsPlainText());
    Assertions.assertEquals(12, options.get(0).getValueFromPos());
    Assertions.assertEquals(18, options.get(0).getValueToPos());
    Assertions.assertEquals("value1", options.get(0).getValue());
    Assertions.assertEquals("value1", options.get(0).getValueAsPlainText());

    Assertions.assertEquals(19, options.get(1).getKeyFromPos());
    Assertions.assertEquals(28, options.get(1).getKeyToPos());
    Assertions.assertEquals("\noption2 ", options.get(1).getKey());
    Assertions.assertEquals("option2", options.get(1).getKeyAsPlainText());
    Assertions.assertEquals(29, options.get(1).getValueFromPos());
    Assertions.assertEquals(38, options.get(1).getValueToPos());
    Assertions.assertEquals(" \\value2 ", options.get(1).getValue());
    Assertions.assertEquals("\\value2", options.get(1).getValueAsPlainText());

    Assertions.assertEquals(39, options.get(2).getKeyFromPos());
    Assertions.assertEquals(67, options.get(2).getKeyToPos());
    Assertions.assertEquals("% option3 = value3,\noption4 ", options.get(2).getKey());
    Assertions.assertEquals("option4", options.get(2).getKeyAsPlainText());
    Assertions.assertEquals(68, options.get(2).getValueFromPos());
    Assertions.assertEquals(124, options.get(2).getValueToPos());
    Assertions.assertEquals("% value4,\n{This is\\} a \\textbf{test}, option5 = value5.}",
        options.get(2).getValue());
    Assertions.assertEquals("This is\\} a \\textbftest, option5 = value5.",
        options.get(2).getValueAsPlainText());

    Assertions.assertEquals(125, options.get(3).getKeyFromPos());
    Assertions.assertEquals(126, options.get(3).getKeyToPos());
    Assertions.assertEquals("\n", options.get(3).getKey());
    Assertions.assertEquals("", options.get(3).getKeyAsPlainText());
    Assertions.assertEquals(-1, options.get(3).getValueFromPos());
    Assertions.assertEquals(-1, options.get(3).getValueToPos());
    Assertions.assertEquals("", options.get(3).getValue());
    Assertions.assertEquals("", options.get(3).getValueAsPlainText());
  }
}
