/* Copyright (C) 2019-2023 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.tools

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VersionProviderTest {
  @Test
  fun testVersion() {
    val versionProvider = VersionProvider()
    val version: Array<String> = versionProvider.version
    assertEquals(1, version.size)

    val rootJsonElement: JsonElement = JsonParser.parseString(version[0])
    assertTrue(rootJsonElement.isJsonObject)

    val rootJsonObject: JsonObject = rootJsonElement.asJsonObject
    assertTrue(rootJsonObject.has("java"))

    val javaJsonElement: JsonElement = rootJsonObject["java"]
    assertTrue(javaJsonElement.isJsonPrimitive)
  }
}
