/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.settings;

import com.google.gson.JsonElement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.bsplines.ltexls.languagetool.LanguageToolHttpInterface;
import org.bsplines.ltexls.languagetool.LanguageToolInterface;
import org.bsplines.ltexls.languagetool.LanguageToolJavaInterface;
import org.bsplines.ltexls.tools.Tools;
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
    Tools.setLogLevel(settings.getLogLevel());
  }

  @RequiresNonNull({"settings"})
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
    return this.settings;
  }

  public @Nullable LanguageToolInterface getLanguageToolInterface() {
    return this.languageToolInterface;
  }

  public void setSettings(JsonElement newJsonSettings,
        @Nullable JsonElement newJsonWorkspaceSpecificSettings) {
    Settings newSettings = new Settings(newJsonSettings, newJsonWorkspaceSpecificSettings);
    setSettings(newSettings);
  }

  public void setSettings(Settings newSettings) {
    String newLanguage = newSettings.getLanguageShortCode();

    final @Nullable Settings oldSettings = this.settingsMap.get(newLanguage);

    setSettings(newLanguage, newSettings);

    Set<SettingsDifference> settingsDifferencesRelevantForLanguageTool =
        newSettings.getDifferencesRelevantForLanguageTool(oldSettings);

    if (settingsDifferencesRelevantForLanguageTool.isEmpty()) {
      this.languageToolInterface = this.languageToolInterfaceMap.get(newLanguage);
    } else {
      if (Tools.logger.isLoggable(Level.FINE)) {
        logDifferentSettings(newLanguage, settingsDifferencesRelevantForLanguageTool);
      }

      reinitializeLanguageToolInterface();
      this.languageToolInterfaceMap.put(newLanguage, this.languageToolInterface);
    }
  }

  private void setSettings(String newLanguage, Settings newSettings) {
    this.settings = newSettings;
    this.settingsMap.put(newLanguage, this.settings);
    Tools.setLogLevel(this.settings.getLogLevel());
  }

  private static void logDifferentSettings(String newLanguage,
        Set<SettingsDifference> settingsDifferencesRelevantForLanguageTool) {
    Set<SettingsDifference> differences = new HashSet<>(settingsDifferencesRelevantForLanguageTool);
    StringBuilder differencesStringBuilder = new StringBuilder();

    for (SettingsDifference difference : differences) {
      if (differencesStringBuilder.length() > 0) differencesStringBuilder.append("; ");
      differencesStringBuilder.append("setting '");
      differencesStringBuilder.append(difference.getName());
      differencesStringBuilder.append("', old '");
      differencesStringBuilder.append(difference.getOtherValue());
      differencesStringBuilder.append("', new '");
      differencesStringBuilder.append(difference.getValue());
      differencesStringBuilder.append("'");
    }

    Tools.logger.fine(Tools.i18n("reinitializingLanguageToolDueToDifferentSettings",
        newLanguage, differencesStringBuilder.toString()));
  }
}
