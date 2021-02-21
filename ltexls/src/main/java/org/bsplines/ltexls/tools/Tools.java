/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.tools;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

public class Tools {
  private static @MonotonicNonNull ResourceBundle messages = null;
  private static final ConsoleHandler loggerConsoleHandler = new ConsoleHandler();
  public static final Logger logger = Logger.getLogger("org.bsplines.ltexls");
  private static final Pattern tildePathPattern = Pattern.compile("^~($|/|\\\\)");
  public static final Random randomNumberGenerator = new Random();
  public static final ExecutorService executorService = Executors.newFixedThreadPool(8);

  static {
    setDefaultLocale();

    logger.setUseParentHandlers(false);
    logger.addHandler(loggerConsoleHandler);
    setLogLevel(Level.FINE);
  }

  public static void setLogLevel(Level logLevel) {
    logger.setLevel(logLevel);
    loggerConsoleHandler.setLevel(logLevel);
  }

  public static String i18n(String key, @Nullable Object... messageArguments) {
    String message;

    if ((messages != null) && (key != null) && messages.containsKey(key)) {
      message = messages.getString(key);
    } else {
      StringWriter stringWriter = new StringWriter();

      if (key == null) {
        stringWriter.write("could not get i18n message with null key");
      } else if (messages == null) {
        stringWriter.write("MessagesBundle is null while trying to get i18n message with key '");
        stringWriter.write(key);
        stringWriter.write("'");
      } else {
        stringWriter.write("i18n message with key '");
        stringWriter.write(key);
        stringWriter.write("' not found");
      }

      stringWriter.write(", message arguments: ");

      for (int i = 0; i < messageArguments.length; i++) {
        if (i > 0) stringWriter.write(", ");
        stringWriter.write("'{");
        stringWriter.write(Integer.toString(i));
        stringWriter.write("}'");
      }

      message = stringWriter.toString();
    }

    MessageFormat formatter = new MessageFormat("");
    formatter.applyPattern(message.replaceAll("'", "''"));
    String[] stringArguments = new String[messageArguments.length];

    for (int i = 0; i < messageArguments.length; i++) {
      stringArguments[i] = ((messageArguments[i] != null)
          ? messageArguments[i].toString() : "null");
    }

    return formatter.format(stringArguments);
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

  public static boolean areRangesIntersecting(Range range1, Range range2) {
    return !(positionLower(range2.getEnd(), range1.getStart())
        || positionLower(range1.getEnd(), range2.getStart()));
  }

  private static boolean positionLower(Position position1, Position position2) {
    return ((position1.getLine() < position2.getLine())
        || ((position1.getLine() == position2.getLine())
        && (position1.getCharacter() < position2.getCharacter())));
  }

  public static boolean equals(@Nullable Object object1, @Nullable Object object2) {
    return ((object1 == null) ? (object2 == null) : ((object2 != null) && object1.equals(object2)));
  }

  public static String normalizePath(String path) {
    @Nullable String homeDirPath = System.getProperty("user.home");

    if (homeDirPath != null) {
      path = tildePathPattern.matcher(path).replaceFirst(
          Matcher.quoteReplacement(homeDirPath) + "$1");
    }

    return path;
  }

  public static @Nullable String readFile(Path filePath) {
    try {
      return new String(Files.readAllBytes(filePath), "utf-8");
    } catch (IOException e) {
      Tools.logger.warning(Tools.i18n("couldNotReadFile", e, filePath.toString()));
      return null;
    }
  }

  /*
  public static void writeFile(Path filePath, String string) {
    try {
      Files.write(filePath, string.getBytes("utf-8"), StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE,
          StandardOpenOption.SYNC);
    } catch (IOException e) {
      Tools.logger.warning(Tools.i18n("couldNotWriteFile", e, filePath.toString()));
    }
  }
  */

  public static String getRandomUuid() {
    return (new UUID(randomNumberGenerator.nextLong(),
        randomNumberGenerator.nextLong())).toString();
  }

  public static @Nullable Throwable getRootCauseOfThrowable(@Nullable Throwable throwable) {
    if (throwable == null) return null;
    Set<Throwable> ancestors = new HashSet<>();
    @Nullable Throwable cause = throwable;
    ancestors.add(throwable);

    for (int i = 0; i < 100; i++) {
      @Nullable Throwable newCause = cause.getCause();
      if ((newCause == null) || ancestors.contains(newCause)) break;
      cause = newCause;
      ancestors.add(newCause);
    }

    return cause;
  }

  public static void rethrowCancellationException(Throwable throwable) {
    @Nullable Throwable rootCause = getRootCauseOfThrowable(throwable);

    if ((rootCause != null) && (rootCause instanceof CancellationException)) {
      throw (CancellationException)rootCause;
    }
  }
}
