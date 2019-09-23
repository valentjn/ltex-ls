package org.bsplines.languagetool_languageserver;

import java.text.MessageFormat;
import java.util.*;

public class Tools {
  private static ResourceBundle messages = null;

  static {
    setDefaultLocale();
  }

  public static String i18n(String key, Object... messageArguments) {
    MessageFormat formatter = new MessageFormat("");
    formatter.applyPattern(messages.getString(key).replaceAll("'", "''"));
    return formatter.format(messageArguments);
  }

  public static void setDefaultLocale() {
    try {
      setLocale(Locale.getDefault());
    } catch (MissingResourceException e) {
      setLocale(Locale.ENGLISH);
    }
  }

  public static void setLocale(Locale locale) {
    messages = ResourceBundle.getBundle("MessagesBundle", locale);
  }
}
