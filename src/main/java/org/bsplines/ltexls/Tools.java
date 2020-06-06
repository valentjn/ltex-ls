package org.bsplines.ltexls;

import java.text.MessageFormat;

import java.util.*;
import java.util.logging.*;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class Tools {
  private static @MonotonicNonNull ResourceBundle messages = null;
  public static final Logger logger = Logger.getLogger("org.bsplines.ltex_ls");

  static {
    setDefaultLocale();

    logger.setUseParentHandlers(false);
    logger.addHandler(new ConsoleHandler());
  }

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
}
