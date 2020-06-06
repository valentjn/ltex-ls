package org.bsplines.ltex_ls;

import java.util.*;

import com.google.gson.JsonElement;

import org.bsplines.ltex_ls.languagetool.*;

import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class SettingsManager {
  private HashMap<String, Settings> settingsMap;
  private HashMap<String, @Nullable LanguageToolInterface> languageToolInterfaceMap;

  private Settings settings;
  private @Nullable LanguageToolInterface languageToolInterface;

  public SettingsManager() {
    reinitializeLanguageToolInterface();
    String language = settings.getLanguageShortCode();
    this.settingsMap = new HashMap<>();
    settingsMap.put(language, settings);
    this.languageToolInterfaceMap = new HashMap<>();
    languageToolInterfaceMap.put(language, languageToolInterface);
  }

  @EnsuresNonNull("settings")
  private void reinitializeLanguageToolInterface(
        @UnknownInitialization(Object.class) SettingsManager this) {
    if (settings == null) this.settings = new Settings();

    if (settings.getLanguageToolHttpServerUri().isEmpty()) {
      this.languageToolInterface = new LanguageToolJavaInterface(settings.getLanguageShortCode(),
          settings.getMotherTongueShortCode(), settings.getSentenceCacheSize(),
          settings.getDictionary());
    } else {
      this.languageToolInterface = new LanguageToolHttpInterface(
          settings.getLanguageToolHttpServerUri(), settings.getLanguageShortCode(),
          settings.getMotherTongueShortCode());
    }

    if (!languageToolInterface.isReady()) {
      this.languageToolInterface = null;
      return;
    }

    // fixes false-positive dereference.of.nullable warnings
    LanguageToolInterface languageToolInterface = this.languageToolInterface;

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

  public @Nullable LanguageToolInterface getLanguageToolInterface() {
    return languageToolInterface;
  }

  public void setSettings(JsonElement newJsonSettings) {
    Settings newSettings = new Settings(newJsonSettings);
    setSettings(newSettings);
  }

  public void setSettings(Settings newSettings) {
    String newLanguage = newSettings.getLanguageShortCode();
    @Nullable Settings oldSettings = settingsMap.get(newLanguage);

    if (newSettings.equals(oldSettings)) {
      this.settings = oldSettings;
      this.languageToolInterface = languageToolInterfaceMap.get(newLanguage);
    } else {
      settingsMap.put(newLanguage, newSettings);
      this.settings = newSettings;
      reinitializeLanguageToolInterface();
      languageToolInterfaceMap.put(newLanguage, languageToolInterface);
    }
  }
}
