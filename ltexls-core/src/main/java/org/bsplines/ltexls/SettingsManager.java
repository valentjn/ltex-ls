/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls;

import com.google.gson.JsonElement;
import java.util.HashMap;
import org.bsplines.ltexls.languagetool.LanguageToolHttpInterface;
import org.bsplines.ltexls.languagetool.LanguageToolInterface;
import org.bsplines.ltexls.languagetool.LanguageToolJavaInterface;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.RequiresNonNull;

public class SettingsManager {
  private HashMap<String, Settings> settingsMap;
  private HashMap<String, @Nullable LanguageToolInterface> languageToolInterfaceMap;

  private Settings settings;
  private @Nullable LanguageToolInterface languageToolInterface;

  public SettingsManager() {
    this(new Settings());
  }

  public SettingsManager(Settings settings) {
    this.settings = settings;
    reinitializeLanguageToolInterface();
    String language = this.settings.getLanguageShortCode();
    this.settingsMap = new HashMap<>();
    this.settingsMap.put(language, this.settings);
    this.languageToolInterfaceMap = new HashMap<>();
    this.languageToolInterfaceMap.put(language, this.languageToolInterface);
  }

  @RequiresNonNull("settings")
  private void reinitializeLanguageToolInterface(
        @UnknownInitialization(Object.class) SettingsManager this) {
    if (this.settings.getLanguageToolHttpServerUri().isEmpty()) {
      this.languageToolInterface = new LanguageToolJavaInterface(
          this.settings.getLanguageShortCode(),
          this.settings.getMotherTongueShortCode(), this.settings.getSentenceCacheSize(),
          this.settings.getDictionary());
    } else {
      this.languageToolInterface = new LanguageToolHttpInterface(
          this.settings.getLanguageToolHttpServerUri(), this.settings.getLanguageShortCode(),
          this.settings.getMotherTongueShortCode());
    }

    if (!this.languageToolInterface.isReady()) {
      this.languageToolInterface = null;
      return;
    }

    // fixes false-positive dereference.of.nullable warnings
    LanguageToolInterface languageToolInterface = this.languageToolInterface;

    if (!this.settings.getLanguageModelRulesDirectory().isEmpty()) {
      languageToolInterface.activateLanguageModelRules(
          this.settings.getLanguageModelRulesDirectory());
    } else {
      if (!this.settings.getMotherTongueShortCode().isEmpty()) {
        languageToolInterface.activateDefaultFalseFriendRules();
      }
    }

    if (!this.settings.getNeuralNetworkModelRulesDirectory().isEmpty()) {
      languageToolInterface.activateNeuralNetworkRules(
          this.settings.getNeuralNetworkModelRulesDirectory());
    }

    if (!this.settings.getWord2VecModelRulesDirectory().isEmpty()) {
      languageToolInterface.activateWord2VecModelRules(
          this.settings.getWord2VecModelRulesDirectory());
    }

    languageToolInterface.enableRules(this.settings.getEnabledRules());
    languageToolInterface.disableRules(this.settings.getDisabledRules());
  }

  public Settings getSettings() {
    return new Settings(this.settings);
  }

  public @Nullable LanguageToolInterface getLanguageToolInterface() {
    return this.languageToolInterface;
  }

  public void setSettings(JsonElement newJsonSettings) {
    Settings newSettings = new Settings(newJsonSettings);
    setSettings(newSettings);
  }

  /**
   * Set settings with a @c Settings object. Reinitialize the LanguageTool interface if necessary.
   *
   * @param newSettings new settings to use
   */
  public void setSettings(Settings newSettings) {
    String newLanguage = newSettings.getLanguageShortCode();
    @Nullable Settings oldSettings = this.settingsMap.get(newLanguage);

    this.settings = new Settings(newSettings);
    this.settingsMap.put(newLanguage, this.settings);

    if (newSettings.equals(oldSettings)) {
      this.languageToolInterface = this.languageToolInterfaceMap.get(newLanguage);
    } else {
      reinitializeLanguageToolInterface();
      this.languageToolInterfaceMap.put(newLanguage, this.languageToolInterface);
    }
  }
}
