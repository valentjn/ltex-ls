/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.languagetool

import org.bsplines.ltexls.settings.Settings
import org.bsplines.ltexls.settings.SettingsManager
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.platform.suite.api.IncludeEngines
import org.languagetool.server.HTTPServer
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@IncludeEngines("junit-jupiter")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LanguageToolHttpInterfaceTest {
  private var serverThread: Thread? = null
  private var defaultSettings = Settings()

  @BeforeAll
  fun setUp() {
    val serverThread = Thread { HTTPServer.main(arrayOf("--port", "8081", "--allow-origin", "*")) }
    serverThread.start()
    this.serverThread = serverThread

    // wait until LanguageTool has initialized itself
    Thread.sleep(5000)
    this.defaultSettings = defaultSettings.copy(
      _languageToolHttpServerUri = "http://localhost:8081"
    )
  }

  @AfterAll
  fun tearDown() {
    this.serverThread?.interrupt()
  }

  @Test
  fun testConstructor() {
    assertTrue(LanguageToolHttpInterface("http://localhost:8081/", "en-US", "").isInitialized())
    assertFalse(LanguageToolHttpInterface("http://localhost:80:81/", "en-US", "").isInitialized())
  }

  @Test
  fun testCheck() {
    LanguageToolJavaInterfaceTest.assertMatches(this.defaultSettings, false)
  }

  @Test
  fun testOtherMethods() {
    val settingsManager = SettingsManager(this.defaultSettings)
    val ltInterface: LanguageToolInterface? = settingsManager.languageToolInterface
    assertNotNull(ltInterface)
    ltInterface.activateDefaultFalseFriendRules()
    ltInterface.activateLanguageModelRules("foobar")
    ltInterface.activateNeuralNetworkRules("foobar")
    ltInterface.activateWord2VecModelRules("foobar")
    ltInterface.enableEasterEgg()
  }
}
