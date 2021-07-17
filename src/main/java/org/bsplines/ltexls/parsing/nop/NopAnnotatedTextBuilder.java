/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.nop;

import org.bsplines.ltexls.parsing.CodeAnnotatedTextBuilder;

public class NopAnnotatedTextBuilder extends CodeAnnotatedTextBuilder {
  public NopAnnotatedTextBuilder(String codeLanguageId) {
    super(codeLanguageId);
  }

  @Override
  public CodeAnnotatedTextBuilder addCode(String code) {
    addMarkup(code);
    return this;
  }
}
