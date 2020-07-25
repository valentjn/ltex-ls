/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.plaintext;

import java.util.Collections;
import java.util.List;
import org.bsplines.ltexls.Settings;
import org.bsplines.ltexls.parsing.CodeFragment;
import org.bsplines.ltexls.parsing.CodeFragmentizer;

public class PlaintextFragmentizer extends CodeFragmentizer {
  public PlaintextFragmentizer(String codeLanguageId) {
    super(codeLanguageId);
  }

  @Override
  public List<CodeFragment> fragmentize(String code, Settings originalSettings) {
    return Collections.singletonList(new CodeFragment(codeLanguageId, code, 0, originalSettings));
  }
}
