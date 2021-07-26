/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.tools

import java.util.logging.ConsoleHandler
import java.util.logging.Level
import java.util.logging.Logger

object Logging {
  val logger: Logger = Logger.getLogger("org.bsplines.ltexls")
  private val loggerConsoleHandler = ConsoleHandler()

  init {
    logger.useParentHandlers = false
    logger.addHandler(loggerConsoleHandler)
    setLogLevel(Level.FINE)
  }

  fun setLogLevel(logLevel: Level) {
    logger.level = logLevel
    loggerConsoleHandler.level = logLevel
  }
}
