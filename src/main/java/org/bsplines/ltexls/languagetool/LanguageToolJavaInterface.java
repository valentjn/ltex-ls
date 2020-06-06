package org.bsplines.ltexls.languagetool;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.ParserConfigurationException;

import org.bsplines.ltexls.Tools;

import org.checkerframework.checker.nullness.qual.EnsuresNonNullIf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.ResultCache;
import org.languagetool.UserConfig;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.AbstractPatternRule;

import org.xml.sax.SAXException;

public class LanguageToolJavaInterface extends LanguageToolInterface {
  private @MonotonicNonNull JLanguageTool languageTool;

  private static final int resultCacheExpireAfterMinutes = 10;

  /**
   * Constructor.
   *
   * @param languageShortCode short code of the checking language
   * @param motherTongueShortCode short code of the mother tongue language
   * @param sentenceCacheSize size of the sentence cache in sentences
   * @param dictionary list of words of the user dictionary
   */
  public LanguageToolJavaInterface(String languageShortCode, String motherTongueShortCode,
        int sentenceCacheSize, List<String> dictionary) {
    if (!Languages.isLanguageSupported(languageShortCode)) {
      Tools.logger.severe(Tools.i18n("notARecognizedLanguage", languageShortCode));
      return;
    }

    Language language = Languages.getLanguageForShortCode(languageShortCode);
    Language motherTongue = ((!motherTongueShortCode.isEmpty())
        ? Languages.getLanguageForShortCode(motherTongueShortCode) : null);
    ResultCache resultCache = new ResultCache(sentenceCacheSize,
        resultCacheExpireAfterMinutes, TimeUnit.MINUTES);
    UserConfig userConfig = new UserConfig(dictionary);

    @SuppressWarnings("argument.type.incompatible")
    JLanguageTool languageTool = new JLanguageTool(language, motherTongue, resultCache, userConfig);
    this.languageTool = languageTool;
  }

  @EnsuresNonNullIf(expression = "this.languageTool", result = true)
  @Override
  public boolean isReady() {
    return (languageTool != null);
  }

  @Override
  public List<LanguageToolRuleMatch> check(AnnotatedText annotatedText) {
    if (!isReady()) {
      Tools.logger.warning(Tools.i18n("skippingTextCheck"));
      return Collections.emptyList();
    }

    List<RuleMatch> matches;

    try {
      matches = languageTool.check(annotatedText);
    } catch (RuntimeException | IOException e) {
      Tools.logger.severe(Tools.i18n("languageToolFailed", e.getMessage()));
      e.printStackTrace();
      return Collections.emptyList();
    }

    List<LanguageToolRuleMatch> result = new ArrayList<>();

    for (RuleMatch match : matches) {
      int fromPos = match.getFromPos();
      int toPos = match.getToPos();
      String ruleId = ((match.getRule() != null) ? match.getRule().getId() : null);
      String message = match.getMessage();
      String sentence = ((match.getSentence() != null) ? match.getSentence().getText() : null);
      List<String> suggestedReplacements = match.getSuggestedReplacements();
      result.add(new LanguageToolRuleMatch(ruleId, sentence, fromPos, toPos, message,
          suggestedReplacements));
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
      List<AbstractPatternRule> falseFriendRules = languageTool.loadFalseFriendRules(
          falseFriendRulePath);
      for (Rule rule : falseFriendRules) languageTool.addRule(rule);
    } catch (ParserConfigurationException | SAXException | IOException e) {
      Tools.logger.warning(Tools.i18n("couldNotLoadFalseFriendRules",
          falseFriendRulePath, e.getMessage()));
      e.printStackTrace();
    }
  }

  @Override
  public void activateLanguageModelRules(String languageModelRulesDirectory) {
    if (!isReady()) return;

    try {
      languageTool.activateLanguageModelRules(new File(languageModelRulesDirectory));
    } catch (IOException | RuntimeException e) {
      Tools.logger.warning(Tools.i18n("couldNotLoadLanguageModel",
          languageModelRulesDirectory, e.getMessage()));
      e.printStackTrace();
    }
  }

  @Override
  public void activateNeuralNetworkRules(String neuralNetworkRulesDirectory) {
    if (!isReady()) return;

    try {
      languageTool.activateNeuralNetworkRules(new File(neuralNetworkRulesDirectory));
    } catch (IOException | RuntimeException e) {
      Tools.logger.warning(Tools.i18n("couldNotLoadNeuralNetworkModel",
          neuralNetworkRulesDirectory, e.getMessage()));
      e.printStackTrace();
    }
  }

  @Override
  public void activateWord2VecModelRules(String word2vecRulesDirectory) {
    if (!isReady()) return;

    try {
      languageTool.activateWord2VecModelRules(new File(word2vecRulesDirectory));
    } catch (IOException | RuntimeException e) {
      Tools.logger.warning(Tools.i18n("couldNotLoadWord2VecModel",
          word2vecRulesDirectory, e.getMessage()));
      e.printStackTrace();
    }
  }

  @Override
  public void enableRules(List<String> ruleIds) {
    if (!isReady()) return;

    // for strange reasons there is no JLanguageTool.enableRules
    for (String ruleId : ruleIds) {
      languageTool.enableRule(ruleId);
    }
  }

  @Override
  public void disableRules(List<String> ruleIds) {
    if (!isReady()) return;
    languageTool.disableRules(ruleIds);
  }

  @Override
  public void enableEasterEgg() {
    if (!isReady()) return;

    languageTool.addRule(new Rule() {
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

    languageTool.addRule(new Rule() {
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
