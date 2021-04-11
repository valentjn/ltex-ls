/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bsplines.ltexls.settings.Settings;
import org.bsplines.ltexls.tools.Tools;
import org.checkerframework.checker.nullness.qual.Nullable;

public class RegexCodeFragmentizer extends CodeFragmentizer {
  private static final Pattern splitSettingsPattern = Pattern.compile("[ \t]+");

  private Pattern pattern;

  public RegexCodeFragmentizer(String codeLanguageId, Pattern pattern) {
    super(codeLanguageId);
    this.pattern = pattern;
  }

  @Override
  public List<CodeFragment> fragmentize(String code, Settings originalSettings) {
    List<CodeFragment> codeFragments = new ArrayList<>();
    Matcher matcher = this.pattern.matcher(code);
    Settings curSettings = originalSettings;
    int curFromPos = 0;

    while (matcher.find()) {
      int lastFromPos = curFromPos;
      curFromPos = matcher.start();
      String lastCode = code.substring(lastFromPos, curFromPos);
      Settings lastSettings = curSettings;
      codeFragments.add(new CodeFragment(codeLanguageId, lastCode, lastFromPos, lastSettings));

      @Nullable String settingsLine = null;

      for (int groupIndex = 1; groupIndex <= matcher.groupCount(); groupIndex++) {
        settingsLine = matcher.group(groupIndex);
        if (settingsLine != null) break;
      }

      if (settingsLine == null) {
        Tools.logger.warning(Tools.i18n("couldNotFindSettingsInMatch"));
        continue;
      }

      Map<String, String> settingsMap = RegexCodeFragmentizer.parseSettings(
          settingsLine, splitSettingsPattern);
      settingsLine = settingsLine.trim();

      for (Map.Entry<String, String> setting : settingsMap.entrySet()) {
        if (setting.getKey().equalsIgnoreCase("enabled")) {
          curSettings = curSettings.withEnabled(setting.getValue().equals("true"));
        } else if (setting.getKey().equalsIgnoreCase("language")) {
          curSettings = curSettings.withLanguageShortCode(setting.getValue());
        } else {
          Tools.logger.warning(Tools.i18n("ignoringUnknownInlineSetting",
              setting.getKey(), setting.getValue()));
        }
      }
    }

    codeFragments.add(new CodeFragment(
        codeLanguageId, code.substring(curFromPos), curFromPos, curSettings));

    return codeFragments;
  }

  public static Map<String, String> parseSettings(
        String settingsLine, Pattern splitSettingsPattern) {
    Map<String, String> settingsMap = new HashMap<>();
    settingsLine = settingsLine.trim();

    for (String settingsChange : splitSettingsPattern.split(settingsLine)) {
      int settingKeyLength = settingsChange.indexOf('=');

      if (settingKeyLength == -1) {
        Tools.logger.warning(Tools.i18n("ignoringMalformedInlineSetting", settingsChange));
        continue;
      }

      String settingKey = settingsChange.substring(0, settingKeyLength).trim();
      String settingValue = settingsChange.substring(settingKeyLength + 1).trim();
      settingsMap.put(settingKey, settingValue);
    }

    return settingsMap;
  }
}
