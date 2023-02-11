/* Copyright (C) 2019-2023 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.settings

import com.google.gson.JsonObject
import com.google.gson.JsonParser

data class HiddenFalsePositive(
  val ruleId: String,
  val sentenceRegexString: String,
) {
  val sentenceRegex = Regex(sentenceRegexString)

  companion object {
    fun fromJsonString(jsonString: String): HiddenFalsePositive {
      val jsonObject: JsonObject = JsonParser.parseString(jsonString).asJsonObject
      val ruleId: String = jsonObject.get("rule").asString
      val sentenceString: String = jsonObject.get("sentence").asString
      return HiddenFalsePositive(ruleId, sentenceString)
    }
  }
}
