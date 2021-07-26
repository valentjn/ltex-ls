/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing

class DummyGenerator(
  val plural: Boolean = false,
) {
  fun generate(language: String, number: Int, startsWithVowel: Boolean = false): String {
    return when {
      language.equals("fr", ignoreCase = true) -> "Jimmy-$number"
      this.plural -> "Dummies"
      startsWithVowel -> "Ina$number"
      else -> "Dummy$number"
    }
  }

  companion object {
    private val instance = DummyGenerator()
    private val instancePlural = DummyGenerator(plural = true)

    fun getInstance(plural: Boolean = false): DummyGenerator {
      return if (plural) instancePlural else instance
    }
  }
}
