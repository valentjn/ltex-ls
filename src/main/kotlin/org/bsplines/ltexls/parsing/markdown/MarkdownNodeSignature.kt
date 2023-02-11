/* Copyright (C) 2019-2023 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.markdown

import org.bsplines.ltexls.parsing.DummyGenerator

data class MarkdownNodeSignature(
  val name: String,
  val action: Action = Action.Ignore,
  val dummyGenerator: DummyGenerator = DummyGenerator.getInstance(),
) {
  enum class Action {
    Default,
    Ignore,
    Dummy,
  }
}
