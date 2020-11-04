/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bsplines.ltexls.languagetool.LanguageToolRuleMatch;
import org.bsplines.ltexls.parsing.AnnotatedTextFragment;
import org.bsplines.ltexls.parsing.CodeFragment;
import org.bsplines.ltexls.settings.SettingsManager;
import org.bsplines.ltexls.tools.Tools;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ResourceOperation;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.xtext.xbase.lib.Pair;
import org.languagetool.markup.AnnotatedText;

public class CodeActionGenerator {
  private SettingsManager settingsManager;

  private static final String acceptSuggestionsCodeActionKind =
      CodeActionKind.QuickFix + ".ltex.acceptSuggestions";
  private static final String addToDictionaryCodeActionKind =
      CodeActionKind.QuickFix + ".ltex.addToDictionary";
  private static final String disableRulesCodeActionKind =
      CodeActionKind.QuickFix + ".ltex.disableRules";
  private static final String hideFalsePositivesCodeActionKind =
      CodeActionKind.QuickFix + ".ltex.hideFalsePositives";
  private static final String addToDictionaryCommandName = "ltex.addToDictionary";
  private static final String disableRulesCommandName = "ltex.disableRules";
  private static final String hideFalsePositivesCommandName = "ltex.hideFalsePositives";
  private static final String dummyPatternStr = "(?:Dummy|Ina|Jimmy-)[0-9]+";
  private static final Pattern dummyPattern = Pattern.compile(dummyPatternStr);

  public CodeActionGenerator(SettingsManager settingsManager) {
    this.settingsManager = settingsManager;
  }

  public Diagnostic createDiagnostic(
        LanguageToolRuleMatch match, LtexTextDocumentItem document) {
    Diagnostic ret = new Diagnostic();
    ret.setRange(new Range(document.convertPosition(match.getFromPos()),
        document.convertPosition(match.getToPos())));
    ret.setSeverity(this.settingsManager.getSettings().getDiagnosticSeverity());
    ret.setSource("LTeX - " + match.getRuleId());
    ret.setMessage(match.getMessage().replaceAll("<suggestion>(.*?)</suggestion>", "'$1'"));
    return ret;
  }

  private static int findAnnotatedTextFragmentWithMatch(
        List<AnnotatedTextFragment> annotatedTextFragments, LanguageToolRuleMatch match) {
    for (int i = 0; i < annotatedTextFragments.size(); i++) {
      if (annotatedTextFragments.get(i).getCodeFragment().contains(match)) return i;
    }

    return -1;
  }

  private static void addToMap(String key, String value,
        Map<String, List<String>> map, JsonObject jsonObject) {
    if (!map.containsKey(key)) {
      map.put(key, new ArrayList<>());
      jsonObject.add(key, new JsonArray());
    }

    List<String> unknownWordsList = map.get(key);
    JsonArray unknownWordsJsonArray = jsonObject.getAsJsonArray(key);

    if (!unknownWordsList.contains(value)) {
      unknownWordsList.add(value);
      unknownWordsJsonArray.add(value);
    }
  }

  private static @Nullable String getOnlyEntry(Map<String, List<String>> map) {
    if (map.size() == 1) {
      List<String> list = map.entrySet().stream().findFirst().get().getValue();
      if (list.size() == 1) return list.get(0);
    }

    return null;
  }

  public List<Either<Command, CodeAction>> generate(
        CodeActionParams params, LtexTextDocumentItem document,
        Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>> checkingResult) {
    if (checkingResult.getValue() == null) return Collections.emptyList();

    List<AnnotatedTextFragment> annotatedTextFragments = checkingResult.getValue();
    List<Either<Command, CodeAction>> result =
        new ArrayList<Either<Command, CodeAction>>();

    List<LanguageToolRuleMatch> addToDictionaryMatches = new ArrayList<>();
    List<LanguageToolRuleMatch> hideFalsePositiveMatches = new ArrayList<>();
    List<LanguageToolRuleMatch> disableRuleMatches = new ArrayList<>();
    Map<String, List<LanguageToolRuleMatch>> useWordMatchesMap = new LinkedHashMap<>();

    for (LanguageToolRuleMatch match : checkingResult.getKey()) {
      if (match.isIntersectingWithRange(params.getRange(), document)) {
        if (match.isUnknownWordRule()) addToDictionaryMatches.add(match);
        if (match.getSentence() != null) hideFalsePositiveMatches.add(match);
        disableRuleMatches.add(match);

        for (String newWord : match.getSuggestedReplacements()) {
          useWordMatchesMap.putIfAbsent(newWord, new ArrayList<>());
          useWordMatchesMap.get(newWord).add(match);
        }
      }
    }

    if (!addToDictionaryMatches.isEmpty()
          && this.settingsManager.getSettings().getLanguageToolHttpServerUri().isEmpty()) {
      result.add(Either.forRight(getAddWordToDictionaryCodeAction(document,
          addToDictionaryMatches, annotatedTextFragments)));
    }

    if (!hideFalsePositiveMatches.isEmpty()) {
      result.add(Either.forRight(getHideFalsePositiveCodeAction(document,
          hideFalsePositiveMatches, annotatedTextFragments)));
    }

    if (!disableRuleMatches.isEmpty()) {
      result.add(Either.forRight(getDisableRuleCodeAction(document,
          disableRuleMatches, annotatedTextFragments)));
    }

    for (Map.Entry<String, List<LanguageToolRuleMatch>> entry : useWordMatchesMap.entrySet()) {
      result.add(Either.forRight(getUseWordCodeAction(document, entry.getKey(), entry.getValue())));
    }

    return result;
  }

  private static int getPlainTextPositionFor(int originalTextPosition,
        AnnotatedText inverseAnnotatedText) {
    return inverseAnnotatedText.getOriginalTextPositionFor(originalTextPosition);
  }

  private CodeAction getAddWordToDictionaryCodeAction(
        LtexTextDocumentItem document,
        List<LanguageToolRuleMatch> addToDictionaryMatches,
        List<AnnotatedTextFragment> annotatedTextFragments) {
    List<@Nullable AnnotatedText> invertedAnnotatedTexts = new ArrayList<>();
    List<@Nullable String> plainTexts = new ArrayList<>();

    for (int i = 0; i < annotatedTextFragments.size(); i++) {
      invertedAnnotatedTexts.add(null);
      plainTexts.add(null);
    }

    Map<String, List<String>> unknownWordsMap = new HashMap<>();
    JsonObject unknownWordsJsonObject = new JsonObject();
    List<Diagnostic> diagnostics = new ArrayList<>();

    for (LanguageToolRuleMatch match : addToDictionaryMatches) {
      int fragmentIndex = findAnnotatedTextFragmentWithMatch(
          annotatedTextFragments, match);

      if (fragmentIndex == -1) {
        Tools.logger.warning(Tools.i18n("couldNotFindFragmentForMatch"));
        continue;
      }

      AnnotatedTextFragment annotatedTextFragment = annotatedTextFragments.get(fragmentIndex);

      if (invertedAnnotatedTexts.get(fragmentIndex) == null) {
        invertedAnnotatedTexts.set(fragmentIndex, annotatedTextFragment.invert());
        plainTexts.set(fragmentIndex, annotatedTextFragment.getAnnotatedText().getPlainText());
      }

      @SuppressWarnings({"assignment.type.incompatible"})
      AnnotatedText inverseAnnotatedText = invertedAnnotatedTexts.get(fragmentIndex);
      @SuppressWarnings({"assignment.type.incompatible"})
      String plainText = plainTexts.get(fragmentIndex);
      CodeFragment codeFragment = annotatedTextFragment.getCodeFragment();
      int offset = codeFragment.getFromPos();

      String language = codeFragment.getSettings().getLanguageShortCode();
      String word = plainText.substring(
          getPlainTextPositionFor(match.getFromPos() - offset, inverseAnnotatedText),
          getPlainTextPositionFor(match.getToPos() - offset, inverseAnnotatedText));

      addToMap(language, word, unknownWordsMap, unknownWordsJsonObject);
      diagnostics.add(createDiagnostic(match, document));
    }

    JsonObject arguments = new JsonObject();
    arguments.addProperty("type", "command");
    arguments.addProperty("command", addToDictionaryCommandName);
    arguments.addProperty("uri", document.getUri());
    arguments.add("words", unknownWordsJsonObject);

    @Nullable String onlyUnknownWord = getOnlyEntry(unknownWordsMap);
    String commandTitle = ((onlyUnknownWord != null)
        ? Tools.i18n("addWordToDictionary", onlyUnknownWord)
        : Tools.i18n("addAllUnknownWordsInSelectionToDictionary"));
    Command command = new Command(commandTitle, addToDictionaryCommandName);
    command.setArguments(Collections.singletonList(arguments));

    CodeAction codeAction = new CodeAction(command.getTitle());
    codeAction.setKind(addToDictionaryCodeActionKind);
    codeAction.setDiagnostics(diagnostics);
    codeAction.setCommand(command);

    return codeAction;
  }

  private CodeAction getHideFalsePositiveCodeAction(
        LtexTextDocumentItem document,
        List<LanguageToolRuleMatch> hideFalsePositiveMatches,
        List<AnnotatedTextFragment> annotatedTextFragments) {
    List<Pair<String, String>> ruleIdSentencePairs = new ArrayList<>();
    Map<String, List<String>> hiddenFalsePositivesMap = new HashMap<>();
    JsonObject falsePositivesJsonObject = new JsonObject();
    List<Diagnostic> diagnostics = new ArrayList<>();

    for (LanguageToolRuleMatch match : hideFalsePositiveMatches) {
      @Nullable String ruleId = match.getRuleId();
      @Nullable String sentence = match.getSentence();
      if ((ruleId == null) || (sentence == null)) continue;
      sentence = sentence.trim();
      Pair<String, String> pair = new Pair<>(ruleId, sentence);

      if (!ruleIdSentencePairs.contains(pair)) {
        int fragmentIndex = findAnnotatedTextFragmentWithMatch(
            annotatedTextFragments, match);

        if (fragmentIndex == -1) {
          Tools.logger.warning(Tools.i18n("couldNotFindFragmentForMatch"));
          continue;
        }

        AnnotatedTextFragment annotatedTextFragment = annotatedTextFragments.get(fragmentIndex);
        CodeFragment codeFragment = annotatedTextFragment.getCodeFragment();
        final String language = codeFragment.getSettings().getLanguageShortCode();

        Matcher matcher = dummyPattern.matcher(sentence);
        StringBuilder sentencePatternStringBuilder = new StringBuilder();
        int lastEnd = 0;

        while (matcher.find()) {
          sentencePatternStringBuilder.append(Pattern.quote(
              sentence.substring(lastEnd, matcher.start())));
          sentencePatternStringBuilder.append(dummyPatternStr);
          lastEnd = matcher.end();
        }

        if (lastEnd < sentence.length()) {
          sentencePatternStringBuilder.append(Pattern.quote(sentence.substring(lastEnd)));
        }

        ruleIdSentencePairs.add(pair);

        JsonObject falsePositiveJson = new JsonObject();
        falsePositiveJson.add("rule", new JsonPrimitive(ruleId));
        String sentencePatternString = "^" + sentencePatternStringBuilder.toString() + "$";
        falsePositiveJson.add("sentence", new JsonPrimitive(sentencePatternString));

        addToMap(language, falsePositiveJson.toString(), hiddenFalsePositivesMap,
            falsePositivesJsonObject);
      }

      diagnostics.add(createDiagnostic(match, document));
    }

    JsonObject arguments = new JsonObject();
    arguments.addProperty("type", "command");
    arguments.addProperty("command", hideFalsePositivesCommandName);
    arguments.addProperty("uri", document.getUri());
    arguments.add("falsePositives", falsePositivesJsonObject);
    Command command = new Command(((ruleIdSentencePairs.size() == 1)
        ? Tools.i18n("hideFalsePositive")
        : Tools.i18n("hideAllFalsePositivesInTheSelectedSentences")),
        hideFalsePositivesCommandName);
    command.setArguments(Collections.singletonList(arguments));

    CodeAction codeAction = new CodeAction(command.getTitle());
    codeAction.setKind(hideFalsePositivesCodeActionKind);
    codeAction.setDiagnostics(diagnostics);
    codeAction.setCommand(command);

    return codeAction;
  }

  private CodeAction getDisableRuleCodeAction(
        LtexTextDocumentItem document,
        List<LanguageToolRuleMatch> disableRuleMatches,
        List<AnnotatedTextFragment> annotatedTextFragments) {
    Map<String, List<String>> ruleIdsMap = new HashMap<>();
    JsonObject ruleIdsJsonObject = new JsonObject();
    List<Diagnostic> diagnostics = new ArrayList<>();

    for (LanguageToolRuleMatch match : disableRuleMatches) {
      @Nullable String ruleId = match.getRuleId();

      if (ruleId != null) {
        int fragmentIndex = findAnnotatedTextFragmentWithMatch(
            annotatedTextFragments, match);

        if (fragmentIndex == -1) {
          Tools.logger.warning(Tools.i18n("couldNotFindFragmentForMatch"));
          continue;
        }

        AnnotatedTextFragment annotatedTextFragment = annotatedTextFragments.get(fragmentIndex);
        String language =
            annotatedTextFragment.getCodeFragment().getSettings().getLanguageShortCode();
        addToMap(language, ruleId, ruleIdsMap, ruleIdsJsonObject);
      }

      diagnostics.add(createDiagnostic(match, document));
    }

    JsonObject arguments = new JsonObject();
    arguments.addProperty("type", "command");
    arguments.addProperty("command", disableRulesCommandName);
    arguments.addProperty("uri", document.getUri());
    arguments.add("ruleIds", ruleIdsJsonObject);
    String commandTitle = ((getOnlyEntry(ruleIdsMap) != null)
        ? Tools.i18n("disableRule")
        : Tools.i18n("disableAllRulesWithMatchesInSelection"));
    Command command = new Command(commandTitle, disableRulesCommandName);
    command.setArguments(Collections.singletonList(arguments));

    CodeAction codeAction = new CodeAction(command.getTitle());
    codeAction.setKind(disableRulesCodeActionKind);
    codeAction.setDiagnostics(diagnostics);
    codeAction.setCommand(command);

    return codeAction;
  }

  private CodeAction getUseWordCodeAction(
        LtexTextDocumentItem document, String newWord, List<LanguageToolRuleMatch> useWordMatches) {
    VersionedTextDocumentIdentifier textDocument = new VersionedTextDocumentIdentifier(
        document.getUri(), document.getVersion());
    List<Diagnostic> diagnostics = new ArrayList<>();
    List<Either<TextDocumentEdit, ResourceOperation>> documentChanges = new ArrayList<>();

    for (LanguageToolRuleMatch match : useWordMatches) {
      Diagnostic diagnostic = createDiagnostic(match, document);
      Range range = diagnostic.getRange();

      diagnostics.add(diagnostic);
      documentChanges.add(Either.forLeft(new TextDocumentEdit(textDocument,
          Collections.singletonList(new TextEdit(range, newWord)))));
    }

    CodeAction codeAction = new CodeAction((useWordMatches.size() == 1)
        ? Tools.i18n("useWord", newWord) : Tools.i18n("useWordAllSelectedMatches", newWord));
    codeAction.setKind(acceptSuggestionsCodeActionKind);
    codeAction.setDiagnostics(diagnostics);
    codeAction.setEdit(new WorkspaceEdit(documentChanges));

    return codeAction;
  }

  public static List<String> getCodeActions() {
    return Collections.singletonList(acceptSuggestionsCodeActionKind);
  }
}
