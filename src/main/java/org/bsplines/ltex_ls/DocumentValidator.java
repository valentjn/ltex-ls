package org.bsplines.ltex_ls;

import java.util.*;

import org.apache.commons.text.StringEscapeUtils;

import org.bsplines.ltex_ls.languagetool.*;
import org.bsplines.ltex_ls.parsing.CodeAnnotatedTextBuilder;

import org.eclipse.lsp4j.TextDocumentItem;

import org.eclipse.xtext.xbase.lib.Pair;

import org.languagetool.markup.AnnotatedText;

public class DocumentValidator {
  private SettingsManager settingsManager;

  public DocumentValidator(SettingsManager settingsManager) {
    this.settingsManager = settingsManager;
  }

  private AnnotatedText buildAnnotatedText(TextDocumentItem document) {
    CodeAnnotatedTextBuilder builder = CodeAnnotatedTextBuilder.create(document.getLanguageId());
    builder.setSettings(settingsManager.getSettings());
    builder.addCode(document.getText());
    return builder.build();
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

  public Pair<List<LanguageToolRuleMatch>, AnnotatedText> validate(TextDocumentItem document) {
    Settings settings = settingsManager.getSettings();
    LanguageToolInterface languageToolInterface = settingsManager.getLanguageToolInterface();

    if (languageToolInterface == null) {
      Tools.logger.warning(Tools.i18n("skippingTextCheck"));
      return new Pair<>(Collections.emptyList(), null);
    }

    AnnotatedText annotatedText = buildAnnotatedText(document);

    if ((settings.getDictionary().size() >= 1) &&
        "BsPlInEs".equals(settings.getDictionary().get(0))) {
      languageToolInterface.enableEasterEgg();
    }

    {
      int logTextMaxLength = 100;
      String logText = annotatedText.getPlainText();
      String postfix = "";

      if (logText.length() > logTextMaxLength) {
        logText = logText.substring(0, logTextMaxLength);
        postfix = Tools.i18n("truncatedPostfix", logTextMaxLength);
      }

      Tools.logger.info(Tools.i18n("checkingText",
          settings.getLanguageShortCode(), StringEscapeUtils.escapeJava(logText), postfix));
    }

    try {
      List<LanguageToolRuleMatch> matches = languageToolInterface.check(annotatedText);
      Tools.logger.info((matches.size() == 1) ? Tools.i18n("obtainedRuleMatch") :
          Tools.i18n("obtainedRuleMatches", matches.size()));
      removeIgnoredMatches(matches);
      return new Pair<>(matches, annotatedText);
    } catch (RuntimeException e) {
      Tools.logger.severe(Tools.i18n("languageToolFailed", e.getMessage()));
      e.printStackTrace();
      return new Pair<>(Collections.emptyList(), annotatedText);
    }
  }
}
