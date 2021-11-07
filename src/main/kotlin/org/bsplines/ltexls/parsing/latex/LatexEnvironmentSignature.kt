/* Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.latex

class LatexEnvironmentSignature(
  environmentPrototype: String,
  action: Action = Action.Ignore,
) : LatexCommandSignature(
  if (PREFIX_REGEX.find(environmentPrototype) != null) {
    environmentPrototype
  } else {
    "\\begin{$environmentPrototype}"
  },
  action,
) {
  val ignoreAllArguments: Boolean
  val environmentName: String?

  init {
    val environmentName: String? = PREFIX_REGEX.find(environmentPrototype)?.groups?.get(1)?.value

    if (environmentName != null) {
      this.ignoreAllArguments = false
      this.environmentName = environmentName
    } else {
      this.ignoreAllArguments = true
      this.environmentName = environmentPrototype
    }
  }

  companion object {
    private val PREFIX_REGEX = Regex("^\\\\begin\\{([^}]+)}")
  }
}
