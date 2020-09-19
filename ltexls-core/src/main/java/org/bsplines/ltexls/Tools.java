/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

public class Tools {
  private static @MonotonicNonNull ResourceBundle messages = null;
  public static final Logger logger = Logger.getLogger("org.bsplines.ltexls");

  static {
    setDefaultLocale();

    logger.setUseParentHandlers(false);
    logger.addHandler(new ConsoleHandler());
  }

  /**
   * Format an internationalized message.
   *
   * @param key key of the internationalized message
   * @param messageArguments values to insert into the message
   * @return formatted message
   */
  public static String i18n(String key, @Nullable Object... messageArguments) {
    if (messages == null) return "could not get MessagesBundle";
    MessageFormat formatter = new MessageFormat("");
    formatter.applyPattern(messages.getString(key).replaceAll("'", "''"));
    Object[] args = new Object[messageArguments.length];

    for (int i = 0; i < messageArguments.length; i++) {
      args[i] = ((messageArguments[i] != null) ? messageArguments[i] : "null");
    }

    return formatter.format(args);
  }

  public static String i18n(String key, Exception e, @Nullable Object... messageArguments) {
    StringWriter stringWriter = new StringWriter();
    stringWriter.write(i18n(key, messageArguments));
    stringWriter.write(". ");
    stringWriter.write(i18n(e));
    return stringWriter.toString();
  }

  public static String i18n(Exception e) {
    StringWriter stringWriter = new StringWriter();
    stringWriter.write(i18n("followingExceptionOccurred"));
    stringWriter.write("\n");
    e.printStackTrace(new PrintWriter(stringWriter));
    return stringWriter.toString();
  }

  /**
   * Load the internationalized messages according to the default system locale.
   */
  public static void setDefaultLocale() {
    try {
      setLocale(Locale.getDefault());
    } catch (MissingResourceException e) {
      setLocale(Locale.ENGLISH);
    }
  }

  public static void setLocale(Locale locale) {
    Tools.messages = ResourceBundle.getBundle("MessagesBundle", locale);
  }

  /**
   * Check if two ranges are intersecting. This is false if and only if the second range
   * is completely before or completely after the first range.
   *
   * @param range1 first range
   * @param range2 second range
   * @return whether the two ranges are intersecting
   */
  public static boolean areRangesIntersecting(Range range1, Range range2) {
    return !(positionLower(range2.getEnd(), range1.getStart())
        || positionLower(range1.getEnd(), range2.getStart()));
  }

  private static boolean positionLower(Position position1, Position position2) {
    return ((position1.getLine() < position2.getLine())
        || ((position1.getLine() == position2.getLine())
        && (position1.getCharacter() < position2.getCharacter())));
  }

  public static @Nullable String readFile(Path filePath) {
    try {
      return new String(Files.readAllBytes(filePath), "utf-8");
    } catch (IOException e) {
      Tools.logger.warning(Tools.i18n("couldNotReadFile", e, filePath.toString()));
      return null;
    }
  }

  public static void writeFile(Path filePath, String string) {
    try {
      Files.write(filePath, string.getBytes("utf-8"), StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE,
          StandardOpenOption.SYNC);
    } catch (IOException e) {
      Tools.logger.warning(Tools.i18n("couldNotWriteFile", e, filePath.toString()));
    }
  }
}
