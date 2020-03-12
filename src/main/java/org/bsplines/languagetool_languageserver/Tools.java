package org.bsplines.languagetool_languageserver;

import java.text.MessageFormat;
import java.util.*;
import java.util.logging.*;

public class Tools {
  private static ResourceBundle messages = null;
  public static final Logger logger = Logger.getLogger("org.bsplines.languagetool_languageserver");

  // https://stackoverflow.com/a/23717493
  private static class DualConsoleHandler extends StreamHandler {
    private final ConsoleHandler stdErrHandler = new ConsoleHandler();

    public DualConsoleHandler() {
      super(System.out, new SimpleFormatter());
    }

    @Override
    public void publish(LogRecord record) {
      if (record.getLevel().intValue() <= Level.INFO.intValue()) {
        super.publish(record);
        super.flush();
      } else {
        stdErrHandler.publish(record);
        stdErrHandler.flush();
      }
    }
  }

  static {
    setDefaultLocale();

    logger.setUseParentHandlers(false);
    logger.addHandler(new DualConsoleHandler());
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

  public static String generateDummy(String language, int number) {
    if (language.equalsIgnoreCase("fr")) {
      return "Jimmy-" + number;
    } else {
      return "Dummy" + number;
    }
  }
}
