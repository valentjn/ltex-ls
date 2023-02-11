/* Copyright (C) 2019-2023 Julian Valentin, LTeX Development Community
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
    val matchResult: MatchResult? = PREFIX_REGEX.find(environmentPrototype)
    val environmentName: String = matchResult?.groups?.get(1)?.value.orEmpty().ifEmpty {
      matchResult?.groups?.get(2)?.value.orEmpty()
    }

    if (environmentName.isNotEmpty()) {
      this.ignoreAllArguments = false
      this.environmentName = environmentName
    } else {
      this.ignoreAllArguments = true
      this.environmentName = environmentPrototype
    }
  }

  companion object {
    private val PREFIX_REGEX = Regex("^(?:\\\\begin\\{([^}]+)}|\\\\start([A-Za-z]+))")
  }
}
