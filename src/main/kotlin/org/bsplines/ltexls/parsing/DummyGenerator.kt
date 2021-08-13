/* Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing

class DummyGenerator private constructor(
  val plural: Boolean = false,
  val vowel: Boolean = false,
) {
  fun generate(language: String, number: Int, vowel: Boolean = false): String {
    return when {
      language.equals("fr", ignoreCase = true) -> "Jimmy-$number"
      this.plural -> "Dummies"
      vowel || this.vowel -> "Ina$number"
      else -> "Dummy$number"
    }
  }

  companion object {
    private val instance = DummyGenerator()
    private val instancePlural = DummyGenerator(plural = true)
    private val instanceVowel = DummyGenerator(vowel = true)
    private val instancePluralVowel = DummyGenerator(plural = true, vowel = true)

    fun getInstance(plural: Boolean = false, vowel: Boolean = false): DummyGenerator {
      return when {
        plural && !vowel -> instancePlural
        !plural && vowel -> instanceVowel
        plural && vowel -> instancePluralVowel
        else -> instance
      }
    }
  }
}
