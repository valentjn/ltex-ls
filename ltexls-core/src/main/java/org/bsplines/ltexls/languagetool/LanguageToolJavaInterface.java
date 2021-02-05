/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.languagetool;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.text.StringEscapeUtils;
import org.bsplines.ltexls.parsing.AnnotatedTextFragment;
import org.bsplines.ltexls.tools.Tools;
import org.checkerframework.checker.nullness.qual.EnsuresNonNullIf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.ResultCache;
import org.languagetool.RuleMatchListener;
import org.languagetool.UserConfig;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.AbstractPatternRule;
import org.xml.sax.SAXException;

public class LanguageToolJavaInterface extends LanguageToolInterface {
  private Set<String> dictionary;
  private @MonotonicNonNull ResultCache resultCache;
  private @MonotonicNonNull JLanguageTool languageTool;

  private static final int resultCacheExpireAfterMinutes = 60;

  /**
   * Constructor.
   *
   * @param languageShortCode short code of the checking language
   * @param motherTongueShortCode short code of the mother tongue language
   * @param sentenceCacheSize size of the sentence cache in sentences
   * @param dictionary list of words of the user dictionary
   */
  public LanguageToolJavaInterface(String languageShortCode, String motherTongueShortCode,
        int sentenceCacheSize, Set<String> dictionary) {
    this.dictionary = dictionary;

    if (!Languages.isLanguageSupported(languageShortCode)) {
      Tools.logger.severe(Tools.i18n("notARecognizedLanguage", languageShortCode));
      return;
    }

    Language language = Languages.getLanguageForShortCode(languageShortCode);
    @Nullable Language motherTongue = ((!motherTongueShortCode.isEmpty())
        ? Languages.getLanguageForShortCode(motherTongueShortCode) : null);
    this.resultCache = new ResultCache(
        sentenceCacheSize, resultCacheExpireAfterMinutes, TimeUnit.MINUTES);
    UserConfig userConfig = new UserConfig(new ArrayList<>(dictionary));

    @SuppressWarnings("argument.type.incompatible")
    JLanguageTool languageTool = new JLanguageTool(
        language, motherTongue, this.resultCache, userConfig);
    this.languageTool = languageTool;
  }

  @EnsuresNonNullIf(expression = "this.resultCache", result = true)
  @EnsuresNonNullIf(expression = "this.languageTool", result = true)
  @Override
  public boolean isReady() {
    return (this.resultCache != null) && (this.languageTool != null);
  }

  private static String mapToString(@Nullable Map<?, ?> map) {
    if (map == null) return "null";
    StringBuilder builder = new StringBuilder("{");

    for (Map.Entry<?, ?> entry : map.entrySet()) {
      if (builder.length() > 1) builder.append(", ");
      appendObjectToBuilder(entry.getKey(), builder);
      builder.append(": ");
      appendObjectToBuilder(entry.getValue(), builder);
    }

    builder.append("}");
    return builder.toString();
  }

  private static void appendObjectToBuilder(@Nullable Object object, StringBuilder builder) {
    if (object != null) {
      builder.append("\"");
      builder.append(StringEscapeUtils.escapeJava(object.toString()));
      builder.append("\"");
    } else {
      builder.append("null");
    }
  }

  @Override
  public List<LanguageToolRuleMatch> check(AnnotatedTextFragment annotatedTextFragment) {
    if (!isReady()) {
      Tools.logger.warning(Tools.i18n("skippingTextCheckAsLanguageToolHasNotBeenInitialized"));
      return Collections.emptyList();
    }

    if (Tools.logger.isLoggable(Level.FINER)) {
      Tools.logger.finer("matchesCache.size() = " + this.resultCache.getMatchesCache().size());
      Tools.logger.finer("remoteMatchesCache.size() = "
          + this.resultCache.getRemoteMatchesCache().size());
      Tools.logger.finer("sentenceCache.size() = " + this.resultCache.getSentenceCache().size());

      if (Tools.logger.isLoggable(Level.FINEST)) {
        Tools.logger.finest("matchesCache = "
            + mapToString(this.resultCache.getMatchesCache().asMap()));
        Tools.logger.finest("remoteMatchesCache = "
            + mapToString(this.resultCache.getRemoteMatchesCache().asMap()));
        Tools.logger.finest("sentenceCache = "
            + mapToString(this.resultCache.getSentenceCache().asMap()));
      }
    }

    @SuppressWarnings("assignment.type.incompatible")
    RuleMatchListener ruleMatchListener = null;
    JLanguageTool.Level ruleLevel = (
        annotatedTextFragment.getCodeFragment().getSettings().getEnablePickyRules()
        ? JLanguageTool.Level.PICKY : JLanguageTool.Level.DEFAULT);

    List<RuleMatch> matches;

    try {
      // workaround bugs like https://github.com/languagetool-org/languagetool/issues/3181,
      // in which LT prints to stdout instead of stderr (this messes up the LSP communication
      // and results in a deadlock) => temporarily discard output to stdout
      PrintStream stdout = System.out;
      System.setOut(new PrintStream(new OutputStream() {
            public void write(int b) {
            }
          }, false, "utf-8"));

      try {
        matches = this.languageTool.check(annotatedTextFragment.getAnnotatedText(),
            true, JLanguageTool.ParagraphHandling.NORMAL, ruleMatchListener, JLanguageTool.Mode.ALL,
            ruleLevel);
      } finally {
        System.setOut(stdout);
      }
    } catch (RuntimeException | IOException e) {
      Tools.logger.severe(Tools.i18n("languageToolFailed", e));
      return Collections.emptyList();
    }

    List<LanguageToolRuleMatch> result = new ArrayList<>();

    for (RuleMatch match : matches) {
      LanguageToolRuleMatch languageToolRuleMatch =
          new LanguageToolRuleMatch(match, annotatedTextFragment);

      if (languageToolRuleMatch.isUnknownWordRule()
            && this.dictionary.contains(annotatedTextFragment.getSubstringOfPlainText(
              languageToolRuleMatch.getFromPos(), languageToolRuleMatch.getToPos()))) {
        continue;
      }

      result.add(languageToolRuleMatch);
    }

    return result;
  }

  @Override
  public void activateDefaultFalseFriendRules() {
    if (!isReady()) return;

    // from JLanguageTool.activateDefaultFalseFriendRules (which is private)
    String falseFriendRulePath = JLanguageTool.getDataBroker().getRulesDir() + "/"
        + JLanguageTool.FALSE_FRIEND_FILE;

    try {
      List<AbstractPatternRule> falseFriendRules = this.languageTool.loadFalseFriendRules(
          falseFriendRulePath);
      for (Rule rule : falseFriendRules) this.languageTool.addRule(rule);
    } catch (ParserConfigurationException | SAXException | IOException e) {
      Tools.logger.warning(Tools.i18n("couldNotLoadFalseFriendRules", e, falseFriendRulePath));
    }
  }

  @Override
  public void activateLanguageModelRules(String languageModelRulesDirectory) {
    if (!isReady()) return;

    try {
      this.languageTool.activateLanguageModelRules(new File(languageModelRulesDirectory));
    } catch (IOException | RuntimeException e) {
      Tools.logger.warning(Tools.i18n("couldNotLoadLanguageModel", e, languageModelRulesDirectory));
    }
  }

  @Override
  public void activateNeuralNetworkRules(String neuralNetworkRulesDirectory) {
    if (!isReady()) return;

    try {
      this.languageTool.activateNeuralNetworkRules(new File(neuralNetworkRulesDirectory));
    } catch (IOException | RuntimeException e) {
      Tools.logger.warning(Tools.i18n("couldNotLoadNeuralNetworkModel", e,
          neuralNetworkRulesDirectory));
    }
  }

  @Override
  public void activateWord2VecModelRules(String word2vecRulesDirectory) {
    if (!isReady()) return;

    try {
      this.languageTool.activateWord2VecModelRules(new File(word2vecRulesDirectory));
    } catch (IOException | RuntimeException e) {
      Tools.logger.warning(Tools.i18n("couldNotLoadWord2VecModel", e, word2vecRulesDirectory));
    }
  }

  @Override
  public void enableRules(Set<String> ruleIds) {
    if (!isReady()) return;

    // for strange reasons there is no JLanguageTool.enableRules
    for (String ruleId : ruleIds) {
      this.languageTool.enableRule(ruleId);
    }
  }

  @Override
  public void disableRules(Set<String> ruleIds) {
    if (!isReady()) return;
    this.languageTool.disableRules(new ArrayList<>(ruleIds));
  }

  @Override
  public void enableEasterEgg() {
    if (!isReady()) return;

    this.languageTool.addRule(new Rule() {
      public String getId() {
        return "bspline";
      }

      public String getDescription() {
        return "Unknown basis function";
      }

      public RuleMatch[] match(AnalyzedSentence sentence) {
        List<RuleMatch> matches = new ArrayList<>();
        for (AnalyzedTokenReadings token : sentence.getTokens()) {
          if (token.getToken().equalsIgnoreCase("hat")) {
            matches.add(new RuleMatch(this, sentence, token.getStartPos(), token.getEndPos(),
                "Unknown basis function. Did you mean <suggestion>B-spline</suggestion>?"));
          }
        }
        return matches.toArray(new RuleMatch[]{});
      }
    });

    this.languageTool.addRule(new Rule() {
      public String getId() {
        return "ungendered";
      }

      public String getDescription() {
        return "Ungendered variant";
      }

      public RuleMatch[] match(AnalyzedSentence sentence) {
        List<RuleMatch> matches = new ArrayList<>();
        for (AnalyzedTokenReadings token : sentence.getTokens()) {
          String s = token.getToken();
          if ((s.length() >= 2)
                && (s.substring(s.length() - 2, s.length()).equalsIgnoreCase("er"))) {
            matches.add(new RuleMatch(this, sentence, token.getStartPos(), token.getEndPos(),
                "Ungendered variant detected. "
                + "Did you mean <suggestion>" + s + "*in</suggestion>?"));
          }
        }
        return matches.toArray(new RuleMatch[]{});
      }
    });
  }
}
