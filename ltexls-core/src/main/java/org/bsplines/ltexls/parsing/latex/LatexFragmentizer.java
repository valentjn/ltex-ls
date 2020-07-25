/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.latex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Pattern;
import org.bsplines.ltexls.Settings;
import org.bsplines.ltexls.Tools;
import org.bsplines.ltexls.parsing.CodeFragment;
import org.bsplines.ltexls.parsing.CodeFragmentizer;
import org.bsplines.ltexls.parsing.RegexCodeFragmentizer;
import org.checkerframework.checker.nullness.qual.Nullable;

public class LatexFragmentizer extends CodeFragmentizer {
  private static Pattern commentPattern = Pattern.compile(
      "^\\s*%\\s*(?i)ltex(?-i):(?<settings>.*?)$", Pattern.MULTILINE);

  private static LatexCommandSignature[] extraCommandSignatures = {
      new LatexCommandSignature("\\footnote{}"),
      new LatexCommandSignature("\\footnote[]{}"),
      new LatexCommandSignature("\\todo{}"),
      new LatexCommandSignature("\\todo[]{}"),
      };
  private static LatexCommandSignatureMatcher extraCommandSignatureList =
      new LatexCommandSignatureMatcher(Arrays.asList(extraCommandSignatures));

  private static Pattern languageTagReplacementPattern = Pattern.compile("[^A-Za-z]+");
  private static Map<String, String> babelLanguageMap = createBabelLanguageMap();

  private static LatexCommandSignature babelSwitchCommandSignature =
      new LatexCommandSignature("\\selectlanguage{}");
  private static LatexCommandSignatureMatcher babelSwitchCommandSignatureList =
      new LatexCommandSignatureMatcher(Collections.singletonList(babelSwitchCommandSignature));

  private static Map<LatexCommandSignature, String> babelInlineCommandSignatureMap =
      createBabelInlineCommandSignatureMap();
  private static LatexCommandSignatureMatcher babelInlineCommandSignatureList =
      new LatexCommandSignatureMatcher(babelInlineCommandSignatureMap.keySet());

  private static Map<LatexCommandSignature, String> babelEnvironmentCommandSignatureMap =
      createBabelEnvironmentCommandSignatureMap();
  private static LatexCommandSignatureMatcher babelEnvironmentCommandSignatureList =
      new LatexCommandSignatureMatcher(babelEnvironmentCommandSignatureMap.keySet());

  private RegexCodeFragmentizer commentFragmentizer;

  private static Map<String, String> createBabelLanguageMap() {
    Map<String, String> babelLanguageMap = new HashMap<>();

    babelLanguageMap.put("ar", "ar");
    babelLanguageMap.put("ast", "ast-ES");
    babelLanguageMap.put("ast-ES", "ast-ES");
    babelLanguageMap.put("be", "be-BY");
    babelLanguageMap.put("be-BY", "be-BY");
    babelLanguageMap.put("br", "br-FR");
    babelLanguageMap.put("br-FR", "br-FR");
    babelLanguageMap.put("ca", "ca-ES");
    babelLanguageMap.put("ca-ES", "ca-ES");
    babelLanguageMap.put("ca-ES-valencia", "ca-ES-valencia");
    babelLanguageMap.put("da", "da-DK");
    babelLanguageMap.put("da-DK", "da-DK");
    babelLanguageMap.put("de", "de");
    babelLanguageMap.put("de-AT", "de-AT");
    babelLanguageMap.put("de-CH", "de-CH");
    babelLanguageMap.put("de-DE", "de-DE");
    babelLanguageMap.put("de-DE-x-simple-language", "de-DE-x-simple-language");
    babelLanguageMap.put("el", "el-GR");
    babelLanguageMap.put("el-GR", "el-GR");
    babelLanguageMap.put("en", "en");
    babelLanguageMap.put("en-AU", "en-AU");
    babelLanguageMap.put("en-CA", "en-CA");
    babelLanguageMap.put("en-GB", "en-GB");
    babelLanguageMap.put("en-NZ", "en-NZ");
    babelLanguageMap.put("en-US", "en-US");
    babelLanguageMap.put("en-ZA", "en-ZA");
    babelLanguageMap.put("eo", "eo");
    babelLanguageMap.put("es", "es");
    babelLanguageMap.put("fa", "fa");
    babelLanguageMap.put("fr", "fr");
    babelLanguageMap.put("ga", "ga-IE");
    babelLanguageMap.put("ga-IE", "ga-IE");
    babelLanguageMap.put("gl", "gl-ES");
    babelLanguageMap.put("gl-ES", "gl-ES");
    babelLanguageMap.put("it", "it");
    babelLanguageMap.put("ja", "ja-JP");
    babelLanguageMap.put("ja-JP", "ja-JP");
    babelLanguageMap.put("km", "km-KH");
    babelLanguageMap.put("km-KH", "km-KH");
    babelLanguageMap.put("nl", "nl");
    babelLanguageMap.put("pl", "pl-PL");
    babelLanguageMap.put("pl-PL", "pl-PL");
    babelLanguageMap.put("pt", "pt");
    babelLanguageMap.put("pt-AO", "pt-AO");
    babelLanguageMap.put("pt-BR", "pt-BR");
    babelLanguageMap.put("pt-MZ", "pt-MZ");
    babelLanguageMap.put("pt-PT", "pt-PT");
    babelLanguageMap.put("ro", "ro-RO");
    babelLanguageMap.put("ro-RO", "ro-RO");
    babelLanguageMap.put("ru", "ru-RU");
    babelLanguageMap.put("ru-RU", "ru-RU");
    babelLanguageMap.put("sk", "sk-SK");
    babelLanguageMap.put("sk-SK", "sk-SK");
    babelLanguageMap.put("sl", "sl-SI");
    babelLanguageMap.put("sl-SI", "sl-SI");
    babelLanguageMap.put("sv", "sv");
    babelLanguageMap.put("ta", "ta-IN");
    babelLanguageMap.put("ta-IN", "ta-IN");
    babelLanguageMap.put("tl", "tl-PH");
    babelLanguageMap.put("tl-PH", "tl-PH");
    babelLanguageMap.put("uk", "uk-UA");
    babelLanguageMap.put("uk-UA", "uk-UA");
    babelLanguageMap.put("zh", "zh-CN");
    babelLanguageMap.put("zh-CN", "zh-CN");

    babelLanguageMap.put("arabic", "ar");
    babelLanguageMap.put("asturian", "ast-ES");
    babelLanguageMap.put("belarusian", "be-BY");
    babelLanguageMap.put("catalan", "ca-ES");
    babelLanguageMap.put("danish", "da-DK");
    babelLanguageMap.put("german", "de-DE");
    babelLanguageMap.put("greek", "el-GR");
    babelLanguageMap.put("british", "en-GB");
    babelLanguageMap.put("american", "en-US");
    babelLanguageMap.put("english", "en-US");
    babelLanguageMap.put("esperanto", "eo");
    babelLanguageMap.put("estonian", "es");
    babelLanguageMap.put("persian", "fa");
    babelLanguageMap.put("french", "fr");
    babelLanguageMap.put("irish", "ga-IE");
    babelLanguageMap.put("galician", "gl-ES");
    babelLanguageMap.put("italian", "it");
    babelLanguageMap.put("japanese", "ja-JP");
    babelLanguageMap.put("khmer", "km-KH");
    babelLanguageMap.put("dutch", "nl");
    babelLanguageMap.put("polish", "pl-PL");
    babelLanguageMap.put("portugese", "pt");
    babelLanguageMap.put("romanian", "ro-RO");
    babelLanguageMap.put("russian", "ru-RU");
    babelLanguageMap.put("slovak", "sk-SK");
    babelLanguageMap.put("slovenian", "sl-SI");
    babelLanguageMap.put("swedish", "sv");
    babelLanguageMap.put("tamil", "ta-IN");
    babelLanguageMap.put("ukranian", "uk-UA");
    babelLanguageMap.put("chinese", "zh-CN");

    return babelLanguageMap;
  }

  private static Map<LatexCommandSignature, String> createBabelInlineCommandSignatureMap() {
    Map<LatexCommandSignature, String> babelInlineCommandSignatureMap = new HashMap<>();

    babelInlineCommandSignatureMap.put(new LatexCommandSignature("\\foreignlanguage{}{}"), "");
    babelInlineCommandSignatureMap.put(new LatexCommandSignature("\\foreignlanguage[]{}{}"), "");

    for (Map.Entry<String, String> entry : babelLanguageMap.entrySet()) {
      String languageTag = convertBabelLanguageToLanguageTag(entry.getKey());
      babelInlineCommandSignatureMap.put(new LatexCommandSignature("\\text" + languageTag + "{}"),
          entry.getValue());
    }

    return babelInlineCommandSignatureMap;
  }

  private static Map<LatexCommandSignature, String> createBabelEnvironmentCommandSignatureMap() {
    Map<LatexCommandSignature, String> babelEnvironmentCommandSignatureMap = new HashMap<>();

    babelEnvironmentCommandSignatureMap.put(
        new LatexCommandSignature("\\begin{otherlanguage}{}"), "");
    babelEnvironmentCommandSignatureMap.put(
        new LatexCommandSignature("\\begin{otherlanguage*}{}"), "");
    babelEnvironmentCommandSignatureMap.put(
        new LatexCommandSignature("\\begin{otherlanguage*}[]{}"), "");
    babelEnvironmentCommandSignatureMap.put(
        new LatexCommandSignature("\\end{otherlanguage}"), "");
    babelEnvironmentCommandSignatureMap.put(
        new LatexCommandSignature("\\end{otherlanguage*}"), "");

    for (Map.Entry<String, String> entry : babelLanguageMap.entrySet()) {
      String languageTag0 = entry.getKey();
      String languageTag1 = convertBabelLanguageToLanguageTag(languageTag0);

      for (int i = 0; i < 2; i++) {
        String languageTag = ((i == 0) ? languageTag0 : languageTag1);
        if ((i == 1) && (languageTag0.length() == languageTag1.length())) continue;
        babelEnvironmentCommandSignatureMap.put(new LatexCommandSignature(
            "\\begin{" + languageTag + "}"), entry.getValue());
        babelEnvironmentCommandSignatureMap.put(new LatexCommandSignature(
            "\\begin{" + languageTag + "}[]"), entry.getValue());
        babelEnvironmentCommandSignatureMap.put(new LatexCommandSignature(
            "\\end{" + languageTag + "}"), entry.getValue());
      }
    }

    return babelEnvironmentCommandSignatureMap;
  }

  /**
   * Constructor.
   *
   * @param codeLanguageId ID of the code language
   */
  public LatexFragmentizer(String codeLanguageId) {
    super(codeLanguageId);
    this.commentFragmentizer = new RegexCodeFragmentizer(
        codeLanguageId, commentPattern);
  }

  @Override
  public List<CodeFragment> fragmentize(String code, Settings originalSettings) {
    List<CodeFragment> fragments = Collections.singletonList(new CodeFragment(
          this.codeLanguageId, code, 0, originalSettings));

    fragments = this.commentFragmentizer.fragmentize(fragments);
    fragments = fragmentizeBabelSwitchCommands(fragments);
    fragments = fragmentizeBabelInlineCommands(fragments);
    fragments = fragmentizeBabelEnvironments(fragments);
    fragments = fragmentizeExtraCommands(fragments);

    return fragments;
  }

  private List<CodeFragment> fragmentizeExtraCommands(List<CodeFragment> fragments) {
    ArrayList<CodeFragment> newFragments = new ArrayList<>();

    for (CodeFragment oldFragment : fragments) {
      String oldFragmentCode = oldFragment.getCode();
      Settings oldFragmentSettings = oldFragment.getSettings();
      extraCommandSignatureList.startMatching(oldFragmentCode,
          oldFragmentSettings.getIgnoreCommandPrototypes());
      @Nullable LatexCommandSignatureMatch match;

      while ((match = extraCommandSignatureList.findNextMatch()) != null) {
        String contents = match.getArgumentContents(match.getArgumentsSize() - 1);
        int contentsFromPos = match.getArgumentContentsFromPos(match.getArgumentsSize() - 1);
        newFragments.add(new CodeFragment(this.codeLanguageId, contents,
            oldFragment.getFromPos() + contentsFromPos, oldFragmentSettings));
      }

      newFragments.add(oldFragment);
    }

    return newFragments;
  }

  private List<CodeFragment> fragmentizeBabelSwitchCommands(List<CodeFragment> fragments) {
    ArrayList<CodeFragment> newFragments = new ArrayList<>();

    for (CodeFragment oldFragment : fragments) {
      String oldFragmentCode = oldFragment.getCode();
      Settings oldFragmentSettings = oldFragment.getSettings();
      babelSwitchCommandSignatureList.startMatching(oldFragmentCode,
          oldFragmentSettings.getIgnoreCommandPrototypes());
      int prevFromPos = 0;
      Settings prevSettings = oldFragmentSettings;
      @Nullable LatexCommandSignatureMatch match;

      while ((match = babelSwitchCommandSignatureList.findNextMatch()) != null) {
        String babelLanguage = match.getArgumentContents(0);
        @Nullable String languageShortCode = babelLanguageMap.get(babelLanguage);

        if (languageShortCode == null) {
          Tools.logger.warning(Tools.i18n("unknownBabelLanguage", babelLanguage));
          continue;
        }

        int nextFromPos = match.getFromPos();
        Settings nextSettings = new Settings(prevSettings);
        nextSettings.setLanguageShortCode(languageShortCode);

        newFragments.add(new CodeFragment(this.codeLanguageId,
            oldFragmentCode.substring(prevFromPos, nextFromPos),
            oldFragment.getFromPos() + prevFromPos, prevSettings));

        prevFromPos = nextFromPos;
        prevSettings = nextSettings;
      }

      newFragments.add(new CodeFragment(this.codeLanguageId,
          oldFragmentCode.substring(prevFromPos),
          oldFragment.getFromPos() + prevFromPos, prevSettings));
    }

    return newFragments;
  }

  private List<CodeFragment> fragmentizeBabelInlineCommands(List<CodeFragment> fragments) {
    ArrayList<CodeFragment> newFragments = new ArrayList<>();

    for (CodeFragment oldFragment : fragments) {
      String oldFragmentCode = oldFragment.getCode();
      Settings oldFragmentSettings = oldFragment.getSettings();
      babelInlineCommandSignatureList.startMatching(oldFragmentCode,
          oldFragmentSettings.getIgnoreCommandPrototypes());
      Settings curSettings = oldFragmentSettings;
      @Nullable LatexCommandSignatureMatch match;

      while ((match = babelInlineCommandSignatureList.findNextMatch()) != null) {
        @Nullable String languageShortCode =
            babelInlineCommandSignatureMap.get(match.getCommandSignature());
        String babelLanguage = "";

        if (languageShortCode == null) {
          String commandPrototype = match.getCommandSignature().getCommandPrototype();
          Tools.logger.warning(Tools.i18n("invalidBabelInlineCommand", commandPrototype));
          continue;
        } else if (languageShortCode.isEmpty()) {
          babelLanguage = match.getArgumentContents(match.getArgumentsSize() - 2);
          languageShortCode = babelLanguageMap.get(babelLanguage);
        }

        curSettings = new Settings(curSettings);

        if (languageShortCode == null) {
          Tools.logger.warning(Tools.i18n("unknownBabelLanguage", babelLanguage));
        } else {
          curSettings.setLanguageShortCode(languageShortCode);
        }

        String contents = match.getArgumentContents(match.getArgumentsSize() - 1);
        int contentsFromPos = match.getArgumentContentsFromPos(match.getArgumentsSize() - 1);
        newFragments.add(new CodeFragment(this.codeLanguageId, contents,
            oldFragment.getFromPos() + contentsFromPos, curSettings));
      }

      newFragments.add(oldFragment);
    }

    return newFragments;
  }

  private List<CodeFragment> fragmentizeBabelEnvironments(List<CodeFragment> fragments) {
    ArrayList<CodeFragment> newFragments = new ArrayList<>();

    for (CodeFragment oldFragment : fragments) {
      String oldFragmentCode = oldFragment.getCode();
      Settings oldFragmentSettings = oldFragment.getSettings();
      babelEnvironmentCommandSignatureList.startMatching(oldFragmentCode,
          oldFragmentSettings.getIgnoreCommandPrototypes());
      Stack<Settings> settingsStack = new Stack<>();
      Stack<Integer> fromPosStack = new Stack<>();
      settingsStack.push(oldFragmentSettings);
      fromPosStack.push(0);
      @Nullable LatexCommandSignatureMatch match;

      while (((match = babelEnvironmentCommandSignatureList.findNextMatch()) != null)
            && !settingsStack.isEmpty()) {
        String commandPrototype = match.getCommandSignature().getCommandPrototype();
        boolean isBegin = commandPrototype.startsWith("\\begin");

        if (isBegin) {
          @Nullable String languageShortCode =
              babelEnvironmentCommandSignatureMap.get(match.getCommandSignature());
          String babelLanguage = "";

          if (languageShortCode == null) {
            Tools.logger.warning(Tools.i18n("invalidBabelEnvironment", commandPrototype));
            continue;
          } else if (languageShortCode.isEmpty()) {
            babelLanguage = match.getArgumentContents(match.getArgumentsSize() - 1);
            languageShortCode = babelLanguageMap.get(babelLanguage);
          }

          Settings newSettings = new Settings(settingsStack.peek());

          if (languageShortCode == null) {
            Tools.logger.warning(Tools.i18n("unknownBabelLanguage", babelLanguage));
          } else {
            newSettings.setLanguageShortCode(languageShortCode);
          }

          settingsStack.push(newSettings);
          fromPosStack.push(match.getToPos());

        } else if (settingsStack.size() <= 1) {
          // shouldn't happen, as then there is an unmatched \end
          break;

        } else {
          Settings prevSettings = settingsStack.pop();
          int prevFromPos = fromPosStack.pop();
          newFragments.add(new CodeFragment(this.codeLanguageId,
              oldFragmentCode.substring(prevFromPos, match.getFromPos()),
              oldFragment.getFromPos() + prevFromPos, prevSettings));
        }
      }

      newFragments.add(oldFragment);
    }

    return newFragments;
  }

  public static String convertBabelLanguageToLanguageTag(String language) {
    return languageTagReplacementPattern.matcher(language).replaceAll("");
  }

  public static Map<String, String> getBabelLanguageMap() {
    return Collections.unmodifiableMap(babelLanguageMap);
  }
}
