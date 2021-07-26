/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.latex

class LatexPackageOption(
  code: String,
  val keyInfo: KeyValueInfo,
  val valueInfo: KeyValueInfo,
) {
  val key: String = code.substring(keyInfo.fromPos, keyInfo.toPos)
  val value: String = if ((valueInfo.fromPos != -1) && (valueInfo.toPos != -1)) {
    code.substring(valueInfo.fromPos, valueInfo.toPos)
  } else {
    ""
  }

  data class KeyValueInfo(
    val fromPos: Int,
    val toPos: Int,
    val plainText: String,
  )
}
