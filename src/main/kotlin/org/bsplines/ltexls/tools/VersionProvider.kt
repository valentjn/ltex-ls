/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.tools

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import org.bsplines.ltexls.server.LtexLanguageServer
import picocli.CommandLine

class VersionProvider : CommandLine.IVersionProvider {
  override fun getVersion(): Array<String> {
    val ltexLsPackage: Package? = LtexLanguageServer::class.java.getPackage()
    val jsonObject = JsonObject()

    if (ltexLsPackage != null) {
      val ltexLsVersion: String? = ltexLsPackage.implementationVersion
      if (ltexLsVersion != null) jsonObject.addProperty("ltex-ls", ltexLsVersion)
    }

    val javaVersion: String? = System.getProperty("java.version")
    if (javaVersion != null) jsonObject.addProperty("java", javaVersion)

    val gsonBuilder: Gson = GsonBuilder().setPrettyPrinting().create()
    return arrayOf(gsonBuilder.toJson(jsonObject))
  }
}
