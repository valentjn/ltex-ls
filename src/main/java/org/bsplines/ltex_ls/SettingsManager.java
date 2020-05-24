package org.bsplines.ltex_ls;

import java.util.*;

import com.google.gson.JsonElement;

import org.bsplines.ltex_ls.languagetool.*;

public class SettingsManager {
  private HashMap<String, Settings> settingsMap;
  private HashMap<String, LanguageToolInterface> languageToolInterfaceMap;

  private Settings settings;
  private LanguageToolInterface languageToolInterface;

  public SettingsManager() {
    settings = new Settings();
    reinitializeLanguageToolInterface();
    String language = settings.getLanguageShortCode();
    settingsMap = new HashMap<>();
    settingsMap.put(language, settings);
    languageToolInterfaceMap = new HashMap<>();
    languageToolInterfaceMap.put(language, languageToolInterface);
  }

  private void reinitializeLanguageToolInterface() {
    if (settings.getLanguageToolHttpServerUri().isEmpty()) {
      languageToolInterface = new LanguageToolJavaInterface(settings.getLanguageShortCode(),
          settings.getMotherTongueShortCode(), settings.getSentenceCacheSize(),
          settings.getDictionary());
    } else {
      languageToolInterface = new LanguageToolHttpInterface(settings.getLanguageToolHttpServerUri(),
          settings.getLanguageShortCode(), settings.getMotherTongueShortCode());
    }

    if (!languageToolInterface.isReady()) {
      languageToolInterface = null;
      return;
    }

    if (!settings.getLanguageModelRulesDirectory().isEmpty()) {
      languageToolInterface.activateLanguageModelRules(
          settings.getLanguageModelRulesDirectory());
    } else {
      if (!settings.getMotherTongueShortCode().isEmpty()) {
        languageToolInterface.activateDefaultFalseFriendRules();
      }
    }

    if (!settings.getNeuralNetworkModelRulesDirectory().isEmpty()) {
      languageToolInterface.activateNeuralNetworkRules(
          settings.getNeuralNetworkModelRulesDirectory());
    }

    if (!settings.getWord2VecModelRulesDirectory().isEmpty()) {
      languageToolInterface.activateWord2VecModelRules(
          settings.getWord2VecModelRulesDirectory());
    }

    languageToolInterface.enableRules(settings.getEnabledRules());
    languageToolInterface.disableRules(settings.getDisabledRules());
  }

  public Settings getSettings() {
    return settings;
  }

  public LanguageToolInterface getLanguageToolInterface() {
    return languageToolInterface;
  }

  public void setSettings(JsonElement newJsonSettings) {
    Settings newSettings = new Settings(newJsonSettings);
    setSettings(newSettings);
  }

  public void setSettings(Settings newSettings) {
    String newLanguage = newSettings.getLanguageShortCode();
    Settings oldSettings = settingsMap.getOrDefault(newLanguage, null);

    if (newSettings.equals(oldSettings)) {
      settings = oldSettings;
      languageToolInterface = languageToolInterfaceMap.get(newLanguage);
    } else {
      settingsMap.put(newLanguage, newSettings);
      settings = newSettings;
      reinitializeLanguageToolInterface();
      languageToolInterfaceMap.put(newLanguage, languageToolInterface);
    }
  }
}
