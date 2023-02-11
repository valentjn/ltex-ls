/* Copyright (C) 2019-2023 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.tools

import java.io.PrintWriter
import java.io.StringWriter
import java.text.MessageFormat
import java.util.Locale
import java.util.MissingResourceException
import java.util.ResourceBundle

object I18n {
  private var messages: ResourceBundle? = null

  init {
    setDefaultLocale()
  }

  @Suppress("SwallowedException")
  fun setDefaultLocale() {
    try {
      setLocale(Locale.getDefault(), false)
    } catch (e: MissingResourceException) {
      setLocale(Locale.ENGLISH, false)
    }
  }

  fun setLocale(locale: Locale, log: Boolean = true) {
    if (log) Logging.LOGGER.info(format("settingLocale", locale.language))
    messages = ResourceBundle.getBundle("LtexLsMessagesBundle", locale)
  }

  fun format(key: String, vararg messageArguments: Any?): String {
    val messages: ResourceBundle? = messages

    val message: String = if ((messages != null) && messages.containsKey(key)) {
      messages.getString(key)
    } else {
      val builder = StringBuilder()

      if (messages == null) {
        builder.append("MessagesBundle is null while trying to get i18n message with key '")
        builder.append(key)
        builder.append("'")
      } else {
        builder.append("i18n message with key '")
        builder.append(key)
        builder.append("' not found")
      }

      builder.append(", message arguments: ")

      for (i in messageArguments.indices) {
        if (i > 0) builder.append(", ")
        builder.append("'{")
        builder.append(i.toString())
        builder.append("}'")
      }

      builder.toString()
    }

    val formatter = MessageFormat("")
    formatter.applyPattern(message.replace("'", "''"))
    val stringArguments: Array<String> = Array(messageArguments.size) { "" }

    for (i in messageArguments.indices) {
      stringArguments[i] = (messageArguments[i]?.toString() ?: "null")
    }

    return formatter.format(stringArguments)
  }

  fun format(key: String, e: Exception, vararg messageArguments: Any?): String {
    val builder = StringBuilder()
    builder.append(format(key, messageArguments))
    builder.append(". ")
    builder.append(format(e))
    return builder.toString()
  }

  fun format(e: Exception): String {
    val writer = StringWriter()
    writer.write(format("followingExceptionOccurred"))
    writer.write("\n")
    e.printStackTrace(PrintWriter(writer))
    return writer.toString()
  }
}
