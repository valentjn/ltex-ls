package org.bsplines.ltexls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.text.StringEscapeUtils;
import org.bsplines.ltexls.languagetool.LanguageToolInterface;
import org.bsplines.ltexls.languagetool.LanguageToolRuleMatch;
import org.bsplines.ltexls.parsing.AnnotatedTextFragment;
import org.bsplines.ltexls.parsing.CodeAnnotatedTextBuilder;
import org.bsplines.ltexls.parsing.CodeFragment;
import org.bsplines.ltexls.parsing.CodeFragmentizer;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.xtext.xbase.lib.Pair;
import org.languagetool.markup.AnnotatedText;

public class DocumentChecker {
  private SettingsManager settingsManager;

  public DocumentChecker(SettingsManager settingsManager) {
    this.settingsManager = settingsManager;
  }

  private List<CodeFragment> fragmentizeDocument(TextDocumentItem document) {
    CodeFragmentizer codeFragmentizer = CodeFragmentizer.create(
        document.getLanguageId(), this.settingsManager.getSettings());
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
    this.settingsManager.setSettings(settings);
    @Nullable LanguageToolInterface languageToolInterface =
        this.settingsManager.getLanguageToolInterface();

    if (languageToolInterface == null) {
      Tools.logger.warning(Tools.i18n("skippingTextCheckAsLanguageToolHasNotBeenInitialized"));
      return Collections.emptyList();
    } else if (!settings.isEnabled()) {
      Tools.logger.info(Tools.i18n("skippingTextCheckAsLtexHasBeenDisabled"));
      return Collections.emptyList();
    } else if ((settings.getDictionary().size() >= 1)
          && "BsPlInEs".equals(settings.getDictionary().get(0))) {
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

  private void removeIgnoredMatches(List<LanguageToolRuleMatch> matches) {
    Settings settings = this.settingsManager.getSettings();
    List<IgnoreRuleSentencePair> ignoreRuleSentencePairs = settings.getIgnoreRuleSentencePairs();

    if (!matches.isEmpty() && !ignoreRuleSentencePairs.isEmpty()) {
      List<LanguageToolRuleMatch> ignoreMatches = new ArrayList<>();

      for (LanguageToolRuleMatch match : matches) {
        @Nullable String ruleId = match.getRuleId();
        @Nullable String sentence = match.getSentence();
        if ((ruleId == null) || (sentence == null)) continue;
        sentence = sentence.trim();

        for (IgnoreRuleSentencePair pair : ignoreRuleSentencePairs) {
          if (pair.getRuleId().equals(ruleId)
                && pair.getSentencePattern().matcher(sentence).find()) {
            Tools.logger.info(Tools.i18n("removingIgnoredRuleMatch", ruleId, sentence));
            ignoreMatches.add(match);
            break;
          }
        }
      }

      if (!ignoreMatches.isEmpty()) {
        Tools.logger.info((ignoreMatches.size() == 1)
            ? Tools.i18n("removedIgnoredRuleMatch")
            : Tools.i18n("removedIgnoredRuleMatches", ignoreMatches.size()));
        for (LanguageToolRuleMatch match : ignoreMatches) matches.remove(match);
      }
    }
  }

  /**
   * Check document with LanguageTool by splitting the document into fragments, parse each of
   * the fragments according to the code language, and passing the fragments to LanguageTool.
   *
   * @param document document to check
   * @return lists of rule matches and annotated text fragments
   */
  public Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>> check(
        TextDocumentItem document) {
    Settings originalSettings = this.settingsManager.getSettings();

    try {
      List<CodeFragment> codeFragments = fragmentizeDocument(document);
      List<AnnotatedTextFragment> annotatedTextFragments =
          buildAnnotatedTextFragments(codeFragments);
      List<LanguageToolRuleMatch> matches = checkAnnotatedTextFragments(annotatedTextFragments);
      return new Pair<>(matches, annotatedTextFragments);
    } finally {
      this.settingsManager.setSettings(originalSettings);
    }
  }
}
