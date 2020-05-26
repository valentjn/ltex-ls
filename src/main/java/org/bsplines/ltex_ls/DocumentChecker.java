package org.bsplines.ltex_ls;

import java.util.*;

import org.apache.commons.text.StringEscapeUtils;

import org.bsplines.ltex_ls.languagetool.*;
import org.bsplines.ltex_ls.parsing.*;
import org.eclipse.lsp4j.TextDocumentItem;

import org.eclipse.xtext.xbase.lib.Pair;

import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedText.MetaDataKey;
import org.languagetool.markup.TextPart;

public class DocumentChecker {
  private SettingsManager settingsManager;

  public DocumentChecker(SettingsManager settingsManager) {
    this.settingsManager = settingsManager;
  }

  private List<CodeFragment> fragmentizeDocument(TextDocumentItem document) {
    CodeFragmentizer codeFragmentizer = CodeFragmentizer.create(
        document.getLanguageId(), settingsManager.getSettings());
    return codeFragmentizer.fragmentize(document.getText());
  }

  private List<AnnotatedTextFragment> buildAnnotatedTextFragments(
        List<CodeFragment> codeFragments) {
    List<AnnotatedTextFragment> annotatedTextFragments = new ArrayList<>();

    for (CodeFragment codeFragment : codeFragments) {
      CodeAnnotatedTextBuilder builder = CodeAnnotatedTextBuilder.create(
          codeFragment.getCodeLanguageId());
      builder.setSettings(codeFragment.getSettings());
      builder.addCode(codeFragment.getCode());
      AnnotatedText curAnnotatedText = builder.build();
      annotatedTextFragments.add(new AnnotatedTextFragment(curAnnotatedText, codeFragment));
    }

    return annotatedTextFragments;
  }

  private List<LanguageToolRuleMatch> checkAnnotatedTextFragments(
        List<AnnotatedTextFragment> annotatedTextFragments) {
    List<LanguageToolRuleMatch> matches = new ArrayList<>();

    for (AnnotatedTextFragment annotatedTextFragment : annotatedTextFragments) {
      matches.addAll(checkAnnotatedTextFragment(annotatedTextFragment));
    }

    return matches;
  }

  private List<LanguageToolRuleMatch> checkAnnotatedTextFragment(
        AnnotatedTextFragment annotatedTextFragment) {
    Settings settings = annotatedTextFragment.getCodeFragment().getSettings();
    settingsManager.setSettings(settings);
    LanguageToolInterface languageToolInterface = settingsManager.getLanguageToolInterface();

    if (languageToolInterface == null) {
      Tools.logger.warning(Tools.i18n("skippingTextCheck"));
      return Collections.emptyList();
    } else if ((settings.getDictionary().size() >= 1) &&
        "BsPlInEs".equals(settings.getDictionary().get(0))) {
      languageToolInterface.enableEasterEgg();
    }

    {
      int logTextMaxLength = 100;
      String logText = annotatedTextFragment.getAnnotatedText().getPlainText();
      String postfix = "";

      if (logText.length() > logTextMaxLength) {
        logText = logText.substring(0, logTextMaxLength);
        postfix = Tools.i18n("truncatedPostfix", logTextMaxLength);
      }

      Tools.logger.info(Tools.i18n("checkingText",
          settings.getLanguageShortCode(), StringEscapeUtils.escapeJava(logText), postfix));
    }

    List<LanguageToolRuleMatch> matches = Collections.emptyList();

    try {
      matches = languageToolInterface.check(annotatedTextFragment.getAnnotatedText());
      Tools.logger.info((matches.size() == 1) ? Tools.i18n("obtainedRuleMatch") :
          Tools.i18n("obtainedRuleMatches", matches.size()));
      removeIgnoredMatches(matches);
    } catch (RuntimeException e) {
      Tools.logger.severe(Tools.i18n("languageToolFailed", e.getMessage()));
      e.printStackTrace();
      return matches;
    }

    for (LanguageToolRuleMatch match : matches) {
      match.setFromPos(match.getFromPos() + annotatedTextFragment.getCodeFragment().getFromPos());
      match.setToPos(match.getToPos() + annotatedTextFragment.getCodeFragment().getFromPos());
    }

    return matches;
  }

  public void removeIgnoredMatches(List<LanguageToolRuleMatch> matches) {
    Settings settings = settingsManager.getSettings();
    List<IgnoreRuleSentencePair> ignoreRuleSentencePairs = settings.getIgnoreRuleSentencePairs();

    if (!matches.isEmpty() && !ignoreRuleSentencePairs.isEmpty()) {
      List<LanguageToolRuleMatch> ignoreMatches = new ArrayList<>();

      for (LanguageToolRuleMatch match : matches) {
        if (match.getSentence() == null) continue;
        String ruleId = match.getRuleId();
        String sentence = match.getSentence().trim();

        for (IgnoreRuleSentencePair pair : ignoreRuleSentencePairs) {
          if (pair.getRuleId().equals(ruleId) &&
                pair.getSentencePattern().matcher(sentence).find()) {
            Tools.logger.info(Tools.i18n("removingIgnoredRuleMatch", ruleId, sentence));
            ignoreMatches.add(match);
            break;
          }
        }
      }

      if (!ignoreMatches.isEmpty()) {
        Tools.logger.info((ignoreMatches.size() == 1) ?
            Tools.i18n("removedIgnoredRuleMatch") :
            Tools.i18n("removedIgnoredRuleMatches", ignoreMatches.size()));
        for (LanguageToolRuleMatch match : ignoreMatches) matches.remove(match);
      }
    }
  }

  private AnnotatedText joinAnnotatedTextFragments(
        List<AnnotatedTextFragment> annotatedTextFragments) {
    int plainTextLength = 0;
    List<TextPart> parts = new ArrayList<>();
    List<Map.Entry<Integer, Integer>> mapping = new ArrayList<>();
    Map<MetaDataKey, String> metaData = new HashMap<>();
    Map<String, String> customMetaData = new HashMap<>();

    for (AnnotatedTextFragment annotatedTextFragment : annotatedTextFragments) {
      AnnotatedText curAnnotatedText = annotatedTextFragment.getAnnotatedText();
      CodeFragment curCodeFragment = annotatedTextFragment.getCodeFragment();
      String curPlainText = curAnnotatedText.getPlainText();

      for (Map.Entry<Integer, Integer> entry : curAnnotatedText.getMapping()) {
        mapping.add(new AbstractMap.SimpleEntry<>(
            entry.getKey() + plainTextLength, entry.getValue() + curCodeFragment.getFromPos()));
      }

      parts.addAll(curAnnotatedText.getParts());
      metaData.putAll(curAnnotatedText.getMetaData());
      customMetaData.putAll(curAnnotatedText.getCustomMetaData());
      plainTextLength += curPlainText.length();
    }

    return new AnnotatedText(parts, mapping, metaData, customMetaData);
  }

  public Pair<List<LanguageToolRuleMatch>, AnnotatedText> check(TextDocumentItem document) {
    Settings originalSettings = settingsManager.getSettings();

    try {
      List<CodeFragment> codeFragments = fragmentizeDocument(document);
      List<AnnotatedTextFragment> annotatedTextFragments =
          buildAnnotatedTextFragments(codeFragments);
      List<LanguageToolRuleMatch> matches = checkAnnotatedTextFragments(annotatedTextFragments);
      AnnotatedText annotatedText = joinAnnotatedTextFragments(annotatedTextFragments);
      return new Pair<>(matches, annotatedText);
    } finally {
      settingsManager.setSettings(originalSettings);
    }
  }
}
