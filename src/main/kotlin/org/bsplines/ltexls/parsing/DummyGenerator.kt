/* Copyright (C) 2019-2023 Julian Valentin, LTeX Development Community
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
      this.plural && language.startsWith("de", ignoreCase = true) -> "Dummys-$number"
      this.plural -> "Dummies"
      vowel || this.vowel -> "Ina$number"
      else -> "Dummy$number"
    }
  }

  companion object {
    private val INSTANCE = DummyGenerator()
    private val INSTANCE_PLURAL = DummyGenerator(plural = true)
    private val INSTANCE_VOWEL = DummyGenerator(vowel = true)
    private val INSTANCE_PLURAL_VOWEL = DummyGenerator(plural = true, vowel = true)

    fun getInstance(plural: Boolean = false, vowel: Boolean = false): DummyGenerator {
      return when {
        plural && !vowel -> INSTANCE_PLURAL
        !plural && vowel -> INSTANCE_VOWEL
        plural && vowel -> INSTANCE_PLURAL_VOWEL
        else -> INSTANCE
      }
    }
  }
}
