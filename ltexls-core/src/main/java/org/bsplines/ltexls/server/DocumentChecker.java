/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.server;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.apache.commons.text.StringEscapeUtils;
import org.bsplines.ltexls.languagetool.LanguageToolInterface;
import org.bsplines.ltexls.languagetool.LanguageToolRuleMatch;
import org.bsplines.ltexls.parsing.AnnotatedTextFragment;
import org.bsplines.ltexls.parsing.CodeAnnotatedTextBuilder;
import org.bsplines.ltexls.parsing.CodeFragment;
import org.bsplines.ltexls.parsing.CodeFragmentizer;
import org.bsplines.ltexls.settings.HiddenFalsePositive;
import org.bsplines.ltexls.settings.Settings;
import org.bsplines.ltexls.settings.SettingsManager;
import org.bsplines.ltexls.tools.Tools;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.xtext.xbase.lib.Pair;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.TextPart;

public class DocumentChecker {
  private SettingsManager settingsManager;

  public DocumentChecker(SettingsManager settingsManager) {
    this.settingsManager = settingsManager;
  }

  private List<CodeFragment> fragmentizeDocument(TextDocumentItem document) {
    CodeFragmentizer codeFragmentizer = CodeFragmentizer.create(document.getLanguageId());
    return codeFragmentizer.fragmentize(document.getText(), this.settingsManager.getSettings());
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
    CodeFragment codeFragment = annotatedTextFragment.getCodeFragment();
    Settings settings = codeFragment.getSettings();
    this.settingsManager.setSettings(settings);
    @Nullable LanguageToolInterface languageToolInterface =
        this.settingsManager.getLanguageToolInterface();

    if (languageToolInterface == null) {
      Tools.logger.warning(Tools.i18n("skippingTextCheckAsLanguageToolHasNotBeenInitialized"));
      return Collections.emptyList();
    } else if (!settings.getEnabled().contains(codeFragment.getCodeLanguageId())) {
      Tools.logger.fine(Tools.i18n("skippingTextCheckAsLtexHasBeenDisabled",
          codeFragment.getCodeLanguageId()));
      return Collections.emptyList();
    } else if (settings.getDictionary().contains("BsPlInEs")) {
      languageToolInterface.enableEasterEgg();
    }

    AnnotatedText annotatedText = annotatedTextFragment.getAnnotatedText();

    if (Tools.logger.isLoggable(Level.FINER)) {
      Tools.logger.finer(Tools.i18n("checkingText", settings.getLanguageShortCode(),
          StringEscapeUtils.escapeJava(annotatedText.getPlainText()),
          ""));

      if (Tools.logger.isLoggable(Level.FINEST)) {
        StringBuilder builder = new StringBuilder("annotatedTextParts = [");

        for (TextPart textPart : annotatedText.getParts()) {
          if (builder.length() > 22) builder.append(", ");
          builder.append(textPart.getType().toString());
          builder.append("(\"");
          builder.append(StringEscapeUtils.escapeJava(textPart.getPart()));
          builder.append("\")");
        }

        builder.append("]");
        Tools.logger.finest(builder.toString());
      }
    } else if (Tools.logger.isLoggable(Level.FINE)) {
      int logTextMaxLength = 100;
      String logText = annotatedText.getPlainText();
      String postfix = "";

      if (logText.length() > logTextMaxLength) {
        logText = logText.substring(0, logTextMaxLength);
        postfix = Tools.i18n("truncatedPostfix", logTextMaxLength);
      }

      Tools.logger.fine(Tools.i18n("checkingText",
          settings.getLanguageShortCode(), StringEscapeUtils.escapeJava(logText), postfix));
    }

    Instant beforeCheckingInstant = Instant.now();
    List<LanguageToolRuleMatch> matches = Collections.emptyList();

    try {
      matches = languageToolInterface.check(annotatedText);
    } catch (RuntimeException e) {
      Tools.logger.severe(Tools.i18n("languageToolFailed", e));
      return matches;
    }

    if (Tools.logger.isLoggable(Level.FINER)) {
      Tools.logger.finer(Tools.i18n("checkingDone",
          Duration.between(beforeCheckingInstant, Instant.now()).toMillis()));
    }

    Tools.logger.fine((matches.size() == 1) ? Tools.i18n("obtainedRuleMatch") :
        Tools.i18n("obtainedRuleMatches", matches.size()));
    removeIgnoredMatches(matches);

    for (LanguageToolRuleMatch match : matches) {
      match.setFromPos(match.getFromPos() + annotatedTextFragment.getCodeFragment().getFromPos());
      match.setToPos(match.getToPos() + annotatedTextFragment.getCodeFragment().getFromPos());
    }

    return matches;
  }

  private void removeIgnoredMatches(List<LanguageToolRuleMatch> matches) {
    Settings settings = this.settingsManager.getSettings();
    Set<HiddenFalsePositive> hiddenFalsePositives = settings.getHiddenFalsePositives();

    if (!matches.isEmpty() && !hiddenFalsePositives.isEmpty()) {
      List<LanguageToolRuleMatch> ignoreMatches = new ArrayList<>();

      for (LanguageToolRuleMatch match : matches) {
        @Nullable String ruleId = match.getRuleId();
        @Nullable String sentence = match.getSentence();
        if ((ruleId == null) || (sentence == null)) continue;
        sentence = sentence.trim();

        for (HiddenFalsePositive pair : hiddenFalsePositives) {
          if (pair.getRuleId().equals(ruleId)
                && pair.getSentencePattern().matcher(sentence).find()) {
            Tools.logger.fine(Tools.i18n("hidingFalsePositive", ruleId, sentence));
            ignoreMatches.add(match);
            break;
          }
        }
      }

      if (!ignoreMatches.isEmpty()) {
        Tools.logger.fine((ignoreMatches.size() == 1)
            ? Tools.i18n("hidFalsePositive")
            : Tools.i18n("hidFalsePositives", ignoreMatches.size()));
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
