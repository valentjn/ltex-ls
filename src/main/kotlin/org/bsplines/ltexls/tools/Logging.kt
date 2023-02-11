/* Copyright (C) 2019-2023 Julian Valentin, LTeX Development Community
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
  val LOGGER: Logger = Logger.getLogger("org.bsplines.ltexls")
  private val LOGGER_CONSOLE_HANDLER = ConsoleHandler()

  init {
    LOGGER.useParentHandlers = false
    LOGGER.addHandler(LOGGER_CONSOLE_HANDLER)
    setLogLevel(Level.FINE)
  }

  fun setLogLevel(logLevel: Level) {
    LOGGER.level = logLevel
    LOGGER_CONSOLE_HANDLER.level = logLevel
  }
}
