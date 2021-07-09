/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.tools;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VersionProviderTest {
  @Test
  public void testVersion() throws Exception {
    VersionProvider versionProvider = new VersionProvider();
    String[] version = versionProvider.getVersion();
    Assertions.assertEquals(1, version.length);

    JsonElement rootJsonElement = JsonParser.parseString(version[0]);
    Assertions.assertTrue(rootJsonElement.isJsonObject());

    JsonObject rootJsonObject = rootJsonElement.getAsJsonObject();
    Assertions.assertTrue(rootJsonObject.has("java"));

    JsonElement javaJsonElement = rootJsonObject.get("java");
    Assertions.assertTrue(javaJsonElement.isJsonPrimitive());
  }
}
