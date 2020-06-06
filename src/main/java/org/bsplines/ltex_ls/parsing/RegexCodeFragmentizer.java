package org.bsplines.ltex_ls.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bsplines.ltex_ls.Settings;
import org.bsplines.ltex_ls.Tools;

public class RegexCodeFragmentizer extends CodeFragmentizer {
  private static Pattern splitSettingsPattern = Pattern.compile("\\s+");

  private Pattern pattern;

  public RegexCodeFragmentizer(String codeLanguageId, Settings originalSettings, Pattern pattern) {
    super(codeLanguageId, originalSettings);
    this.pattern = pattern;
  }

  @Override
  public List<CodeFragment> fragmentize(String code) {
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

      curSettings = new Settings(curSettings);
      String settingsLine = matcher.group("settings");

      if (settingsLine == null) {
        Tools.logger.warning(Tools.i18n("couldNotFindSettingsInMatch"));
        continue;
      }

      settingsLine = settingsLine.trim();

      for (String settingsChange : splitSettingsPattern.split(settingsLine)) {
        int settingNameLength = settingsChange.indexOf('=');

        if (settingNameLength == -1) {
          Tools.logger.warning(Tools.i18n("ignoringMalformedInlineSetting", settingsChange));
          continue;
        }

        String settingName = settingsChange.substring(0, settingNameLength);
        String settingValue = settingsChange.substring(settingNameLength + 1);

        if (settingName.equalsIgnoreCase("language")) {
          curSettings.setLanguageShortCode(settingValue);
        } else {
          Tools.logger.warning(Tools.i18n("ignoringUnknownInlineSetting",
              settingName, settingValue));
        }
      }
    }

    codeFragments.add(new CodeFragment(
        codeLanguageId, code.substring(curFromPos), curFromPos, curSettings));

    return codeFragments;
  }
}
