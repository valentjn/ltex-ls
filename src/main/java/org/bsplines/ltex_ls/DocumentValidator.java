package org.bsplines.ltex_ls;

import java.util.*;
import java.util.concurrent.*;

import org.apache.commons.text.StringEscapeUtils;

import org.bsplines.ltex_ls.languagetool.*;
import org.bsplines.ltex_ls.parsing.latex.*;
import org.bsplines.ltex_ls.parsing.markdown.*;

import org.eclipse.lsp4j.TextDocumentItem;

import org.eclipse.xtext.xbase.lib.Pair;

import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedTextBuilder;

public class DocumentValidator {
  private SettingsManager settingsManager;

  public DocumentValidator(SettingsManager settingsManager) {
    this.settingsManager = settingsManager;
  }

  private AnnotatedText buildAnnotatedText(TextDocumentItem document) {
    Settings settings = settingsManager.getSettings();
    String codeLanguageId = document.getLanguageId();
    AnnotatedText annotatedText;

    switch (codeLanguageId) {
      case "plaintext": {
        AnnotatedTextBuilder builder = new AnnotatedTextBuilder();
        annotatedText = builder.addText(document.getText()).build();
        break;
      }
      case "markdown": {
        MarkdownAnnotatedTextBuilder builder = new MarkdownAnnotatedTextBuilder();
        builder.language = settings.getLanguageShortCode();
        builder.dummyNodeTypes.addAll(settings.getDummyMarkdownNodeTypes());
        builder.ignoreNodeTypes.addAll(settings.getIgnoreMarkdownNodeTypes());

        builder.addCode(document.getText());
        annotatedText = builder.getAnnotatedText();
        break;
      }
      case "latex":
      case "rsweave": {
        LatexAnnotatedTextBuilder builder = new LatexAnnotatedTextBuilder();
        builder.language = settings.getLanguageShortCode();
        builder.codeLanguageId = codeLanguageId;

        for (String commandPrototype : settings.getDummyCommandPrototypes()) {
          builder.commandSignatures.add(new LatexCommandSignature(commandPrototype,
              LatexCommandSignature.Action.DUMMY));
        }

        for (String commandPrototype : settings.getIgnoreCommandPrototypes()) {
          builder.commandSignatures.add(new LatexCommandSignature(commandPrototype,
              LatexCommandSignature.Action.IGNORE));
        }

        builder.ignoreEnvironments.addAll(settings.getIgnoreEnvironments());

        ExecutorService executor = Executors.newCachedThreadPool();
        Future<Object> builderFuture = executor.submit(new Callable<Object>() {
          public Object call() throws InterruptedException {
            builder.addCode(document.getText());
            return null;
          }
        });

        try {
          builderFuture.get(10, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
          throw new RuntimeException(Tools.i18n("latexAnnotatedTextBuilderFailed"), e);
        } finally {
          builderFuture.cancel(true);
        }

        annotatedText = builder.getAnnotatedText();
        break;
      }
      default: {
        throw new UnsupportedOperationException(Tools.i18n(
            "codeLanguageNotSupported", codeLanguageId));
      }
    }

    return annotatedText;
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
