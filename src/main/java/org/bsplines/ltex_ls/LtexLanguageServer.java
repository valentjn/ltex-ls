package org.bsplines.ltex_ls;

import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.*;

import org.bsplines.ltex_ls.languagetool.*;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.*;

import org.eclipse.xtext.xbase.lib.Pair;

import org.languagetool.markup.AnnotatedText;

public class LtexLanguageServer implements LanguageServer, LanguageClientAware {
  private LanguageClient languageClient;
  private HashMap<String, TextDocumentItem> documents;
  private SettingsManager settingsManager;
  private DocumentValidator documentValidator;

  private static final String acceptSuggestionCodeActionKind =
      CodeActionKind.QuickFix + ".ltex.acceptSuggestion";
  private static final String addToDictionaryCodeActionKind =
      CodeActionKind.QuickFix + ".ltex.addToDictionary";
  private static final String disableRuleCodeActionKind =
      CodeActionKind.QuickFix + ".ltex.disableRule";
  private static final String ignoreRuleInSentenceCodeActionKind =
      CodeActionKind.QuickFix + ".ltex.ignoreRuleInSentence";
  private static final String addToDictionaryCommandName = "ltex.addToDictionary";
  private static final String disableRuleCommandName = "ltex.disableRule";
  private static final String ignoreRuleInSentenceCommandName = "ltex.ignoreRuleInSentence";

  private static boolean matchIntersectsWithRange(LanguageToolRuleMatch match, Range range,
      DocumentPositionCalculator positionCalculator) {
    // false iff match is completely before range or completely after range
    return !(positionLower(positionCalculator.getPosition(match.getToPos()), range.getStart()) ||
        positionLower(range.getEnd(), positionCalculator.getPosition(match.getFromPos())));
  }

  private static boolean positionLower(Position position1, Position position2) {
    return ((position1.getLine() < position2.getLine()) ||
        ((position1.getLine() == position2.getLine()) &&
        (position1.getCharacter() < position2.getCharacter())));
  }

  private Diagnostic createDiagnostic(
        LanguageToolRuleMatch match, DocumentPositionCalculator positionCalculator) {
    Diagnostic ret = new Diagnostic();
    ret.setRange(new Range(
        positionCalculator.getPosition(match.getFromPos()),
        positionCalculator.getPosition(match.getToPos())));
    ret.setSeverity(settingsManager.getSettings().getDiagnosticSeverity());
    ret.setSource("LTeX - " + match.getRuleId());
    ret.setMessage(match.getMessage().replaceAll("<suggestion>(.*?)</suggestion>", "'$1'"));
    return ret;
  }

  private static AnnotatedText invertAnnotatedText(AnnotatedText annotatedText) {
    List<Map.Entry<Integer, Integer>> mapping = annotatedText.getMapping();
    List<Map.Entry<Integer, Integer>> inverseMapping = new ArrayList<>();

    for (Map.Entry<Integer, Integer> entry : mapping) {
      inverseMapping.add(new AbstractMap.SimpleEntry<>(entry.getValue(), entry.getKey()));
    }

    return new AnnotatedText(Collections.emptyList(), inverseMapping, Collections.emptyMap(),
        Collections.emptyMap());
  }

  private static int getPlainTextPositionFor(int originalTextPosition,
      AnnotatedText inverseAnnotatedText) {
    return inverseAnnotatedText.getOriginalTextPositionFor(originalTextPosition);
  }

  @Override
  public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
    ServerCapabilities capabilities = new ServerCapabilities();
    capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
    capabilities.setCodeActionProvider(
        new CodeActionOptions(Arrays.asList(
          acceptSuggestionCodeActionKind, addToDictionaryCodeActionKind,
          disableRuleCodeActionKind, ignoreRuleInSentenceCodeActionKind)));
    capabilities.setExecuteCommandProvider(
        new ExecuteCommandOptions(Arrays.asList(
          addToDictionaryCommandName, disableRuleCommandName, ignoreRuleInSentenceCommandName)));

    // Until it is specified in the LSP that the locale is automatically sent with
    // the initialization request, we have to do that manually.
    // See https://github.com/microsoft/language-server-protocol/issues/754.
    JsonObject initializationOptions = (JsonObject) params.getInitializationOptions();

    if ((initializationOptions != null) && initializationOptions.has("locale")) {
      String localeLanguage = initializationOptions.get("locale").getAsString();
      Locale locale = Locale.forLanguageTag(localeLanguage);
      Tools.logger.info(Tools.i18n("settingLocale", locale.getLanguage()));
      Tools.setLocale(locale);
    }

    documents = new HashMap<>();
    settingsManager = new SettingsManager();
    documentValidator = new DocumentValidator(settingsManager);

    return CompletableFuture.completedFuture(new InitializeResult(capabilities));
  }

  @Override
  public CompletableFuture<Object> shutdown() {
    // Per https://github.com/eclipse/lsp4j/issues/18
    return CompletableFuture.completedFuture(new Object());
  }

  @Override
  public void exit() {
    System.exit(0);
  }

  @Override
  public TextDocumentService getTextDocumentService() {
    return new FullTextDocumentService(documents) {

      @Override
      public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(
            CodeActionParams params) {
        if (params.getContext().getDiagnostics().isEmpty()) {
          return CompletableFuture.completedFuture(Collections.emptyList());
        }

        TextDocumentItem document = documents.get(params.getTextDocument().getUri());

        return validateDocument(document).thenApply(
              (Pair<List<LanguageToolRuleMatch>, AnnotatedText> validateResult) -> {
          return getCodeActions(params, document, validateResult);
        });
      }

      @Override
      public void didOpen(DidOpenTextDocumentParams params) {
        super.didOpen(params);
        publishIssues(params.getTextDocument().getUri());
      }

      @Override
      public void didChange(DidChangeTextDocumentParams params) {
        super.didChange(params);
        publishIssues(params.getTextDocument().getUri());
      }

      @Override
      public void didClose(DidCloseTextDocumentParams params) {
        super.didClose(params);
        String uri = params.getTextDocument().getUri();
        languageClient.publishDiagnostics(
            new PublishDiagnosticsParams(uri, Collections.emptyList()));
      }

      private void publishIssues(String uri) {
        TextDocumentItem document = documents.get(uri);
        LtexLanguageServer.this.publishIssues(document);
      }
    };
  }

  private List<Either<Command, CodeAction>> getCodeActions(CodeActionParams params,
        TextDocumentItem document,
        Pair<List<LanguageToolRuleMatch>, AnnotatedText> validateResult) {
    if (validateResult.getValue() == null) return Collections.emptyList();

    VersionedTextDocumentIdentifier textDocument = new VersionedTextDocumentIdentifier(
        document.getUri(), document.getVersion());
    String text = document.getText();
    String plainText = validateResult.getValue().getPlainText();
    DocumentPositionCalculator positionCalculator = new DocumentPositionCalculator(text);
    List<Either<Command, CodeAction>> result =
        new ArrayList<Either<Command, CodeAction>>();

    List<LanguageToolRuleMatch> addWordToDictionaryMatches = new ArrayList<>();
    List<LanguageToolRuleMatch> ignoreRuleInThisSentenceMatches = new ArrayList<>();
    List<LanguageToolRuleMatch> disableRuleMatches = new ArrayList<>();
    Map<String, List<LanguageToolRuleMatch>> useWordMatchesMap = new LinkedHashMap<>();

    for (LanguageToolRuleMatch match : validateResult.getKey()) {
      if (matchIntersectsWithRange(match, params.getRange(), positionCalculator)) {
        String ruleId = match.getRuleId();

        if (ruleId.startsWith("MORFOLOGIK_") || ruleId.startsWith("HUNSPELL_") ||
            ruleId.startsWith("GERMAN_SPELLER_")) {
          addWordToDictionaryMatches.add(match);
        }

        if (match.getSentence() != null) {
          ignoreRuleInThisSentenceMatches.add(match);
        }

        disableRuleMatches.add(match);

        for (String newWord : match.getSuggestedReplacements()) {
          useWordMatchesMap.putIfAbsent(newWord, new ArrayList<>());
          useWordMatchesMap.get(newWord).add(match);
        }
      }
    }

    if (!addWordToDictionaryMatches.isEmpty() &&
          settingsManager.getSettings().getLanguageToolHttpServerUri().isEmpty()) {
      AnnotatedText inverseAnnotatedText = invertAnnotatedText(validateResult.getValue());
      List<String> unknownWords = new ArrayList<>();
      JsonArray unknownWordsJson = new JsonArray();
      List<Diagnostic> diagnostics = new ArrayList<>();

      for (LanguageToolRuleMatch match : addWordToDictionaryMatches) {
        String word = plainText.substring(
            getPlainTextPositionFor(match.getFromPos(), inverseAnnotatedText),
            getPlainTextPositionFor(match.getToPos(), inverseAnnotatedText));

        if (!unknownWords.contains(word)) {
          unknownWords.add(word);
          unknownWordsJson.add(word);
        }

        diagnostics.add(createDiagnostic(match, positionCalculator));
      }

      Command command = new Command(((unknownWords.size() == 1) ?
          Tools.i18n("addWordToDictionary", unknownWords.get(0)) :
          Tools.i18n("addAllUnknownWordsInSelectionToDictionary")),
          addToDictionaryCommandName);
      JsonObject arguments = new JsonObject();
      arguments.addProperty("type", "command");
      arguments.addProperty("command", addToDictionaryCommandName);
      arguments.addProperty("uri", document.getUri());
      arguments.add("word", unknownWordsJson);
      command.setArguments(Arrays.asList(arguments));

      CodeAction codeAction = new CodeAction(command.getTitle());
      codeAction.setKind(addToDictionaryCodeActionKind);
      codeAction.setDiagnostics(diagnostics);
      codeAction.setCommand(command);
      result.add(Either.forRight(codeAction));
    }

    if (!ignoreRuleInThisSentenceMatches.isEmpty()) {
      List<Pair<String, String>> ruleIdSentencePairs = new ArrayList<>();
      List<String> ruleIds = new ArrayList<>();
      JsonArray ruleIdsJson = new JsonArray();
      List<String> sentencePatternStrings = new ArrayList<>();
      JsonArray sentencePatternStringsJson = new JsonArray();
      List<Diagnostic> diagnostics = new ArrayList<>();

      for (LanguageToolRuleMatch match : ignoreRuleInThisSentenceMatches) {
        String ruleId = match.getRuleId();
        String sentence = match.getSentence().trim();
        Pair<String, String> pair = new Pair<>(ruleId, sentence);

        if (!ruleIdSentencePairs.contains(pair)) {
          Matcher matcher = Pattern.compile("Dummy[0-9]+").matcher(sentence);
          StringBuilder sentencePatternStringBuilder = new StringBuilder();
          int lastEnd = 0;

          while (matcher.find()) {
            sentencePatternStringBuilder.append(Pattern.quote(
                sentence.substring(lastEnd, matcher.start())));
            sentencePatternStringBuilder.append("Dummy[0-9]+");
            lastEnd = matcher.end();
          }

          if (lastEnd < sentence.length()) {
            sentencePatternStringBuilder.append(Pattern.quote(sentence.substring(lastEnd)));
          }

          ruleIdSentencePairs.add(pair);
          ruleIds.add(ruleId);
          ruleIdsJson.add(ruleId);
          String sentencePatternString = "^" + sentencePatternStringBuilder.toString() + "$";
          sentencePatternStrings.add(sentencePatternString);
          sentencePatternStringsJson.add(sentencePatternString);
        }

        diagnostics.add(createDiagnostic(match, positionCalculator));
      }

      Command command = new Command(((ruleIdSentencePairs.size() == 1) ?
          Tools.i18n("ignoreRuleInThisSentence") :
          Tools.i18n("ignoreAllRulesInTheSelectedSentences")),
          ignoreRuleInSentenceCommandName);
      JsonObject arguments = new JsonObject();
      arguments.addProperty("type", "command");
      arguments.addProperty("command", ignoreRuleInSentenceCommandName);
      arguments.addProperty("uri", document.getUri());
      arguments.add("ruleId", ruleIdsJson);
      arguments.add("sentencePattern", sentencePatternStringsJson);
      command.setArguments(Arrays.asList(arguments));

      CodeAction codeAction = new CodeAction(command.getTitle());
      codeAction.setKind(ignoreRuleInSentenceCodeActionKind);
      codeAction.setDiagnostics(diagnostics);
      codeAction.setCommand(command);
      result.add(Either.forRight(codeAction));
    }

    if (!disableRuleMatches.isEmpty()) {
      List<String> ruleIds = new ArrayList<>();
      JsonArray ruleIdsJson = new JsonArray();
      List<Diagnostic> diagnostics = new ArrayList<>();

      for (LanguageToolRuleMatch match : disableRuleMatches) {
        String ruleId = match.getRuleId();

        if (!ruleIds.contains(ruleId)) {
          ruleIds.add(ruleId);
          ruleIdsJson.add(ruleId);
        }

        diagnostics.add(createDiagnostic(match, positionCalculator));
      }

      Command command = new Command(((ruleIds.size() == 1) ?
          Tools.i18n("disableRule") : Tools.i18n("disableAllRulesWithMatchesInSelection")),
          disableRuleCommandName);
      JsonObject arguments = new JsonObject();
      arguments.addProperty("type", "command");
      arguments.addProperty("command", disableRuleCommandName);
      arguments.addProperty("uri", document.getUri());
      arguments.add("ruleId", ruleIdsJson);
      command.setArguments(Arrays.asList(arguments));

      CodeAction codeAction = new CodeAction(command.getTitle());
      codeAction.setKind(disableRuleCodeActionKind);
      codeAction.setDiagnostics(diagnostics);
      codeAction.setCommand(command);
      result.add(Either.forRight(codeAction));
    }

    for (Map.Entry<String, List<LanguageToolRuleMatch>> entry : useWordMatchesMap.entrySet()) {
      String newWord = entry.getKey();
      List<LanguageToolRuleMatch> useWordMatches = entry.getValue();
      List<Diagnostic> diagnostics = new ArrayList<>();
      List<Either<TextDocumentEdit, ResourceOperation>> documentChanges = new ArrayList<>();

      for (LanguageToolRuleMatch match : useWordMatches) {
        Diagnostic diagnostic = createDiagnostic(match, positionCalculator);
        Range range = diagnostic.getRange();

        diagnostics.add(diagnostic);
        documentChanges.add(Either.forLeft(new TextDocumentEdit(textDocument,
            Collections.singletonList(new TextEdit(range, newWord)))));
      }

      CodeAction codeAction = new CodeAction((useWordMatches.size() == 1) ?
          Tools.i18n("useWord", newWord) : Tools.i18n("useWordAllSelectedMatches", newWord));
      codeAction.setKind(acceptSuggestionCodeActionKind);
      codeAction.setDiagnostics(diagnostics);
      codeAction.setEdit(new WorkspaceEdit(documentChanges));
      result.add(Either.forRight(codeAction));
    }

    return result;
  }

  private CompletableFuture<Void> publishIssues(TextDocumentItem document) {
    return getIssues(document).thenApply(
        (List<Diagnostic> diagnostics) -> {
          languageClient.publishDiagnostics(
              new PublishDiagnosticsParams(document.getUri(), diagnostics));
          return null;
        });
  }

  private CompletableFuture<List<Diagnostic>> getIssues(TextDocumentItem document) {
    return validateDocument(document).thenApply(
        (Pair<List<LanguageToolRuleMatch>, AnnotatedText> validateResult) -> {
          List<LanguageToolRuleMatch> matches = validateResult.getKey();
          DocumentPositionCalculator positionCalculator =
              new DocumentPositionCalculator(document.getText());

          return matches.stream().map(
              match -> createDiagnostic(match, positionCalculator)).collect(Collectors.toList());
        });
  }

  private void sendProgressEvent(String uri, String operation, double progress) {
    // real progress events from LSP 3.15 are not implemented yet in LSP4J
    // (see https://github.com/eclipse/lsp4j/issues/370)
    JsonObject arguments = new JsonObject();
    arguments.addProperty("type", "progress");
    arguments.addProperty("uri", uri);
    arguments.addProperty("operation", operation);
    arguments.addProperty("progress", progress);
    languageClient.telemetryEvent(arguments);
  }

  private CompletableFuture<Pair<List<LanguageToolRuleMatch>, AnnotatedText>> validateDocument(
        TextDocumentItem document) {
    ConfigurationItem configurationItem = new ConfigurationItem();
    configurationItem.setSection("ltex");
    configurationItem.setScopeUri(document.getUri());
    CompletableFuture<List<Object>> configurationFuture = languageClient.configuration(
        new ConfigurationParams(Arrays.asList(configurationItem)));
    sendProgressEvent(document.getUri(), "validateDocument", 0);

    return configurationFuture.thenApply((List<Object> configuration) -> {
      try {
        settingsManager.setSettings((JsonElement)configuration.get(0));
        return documentValidator.validate(document);
      } finally {
        sendProgressEvent(document.getUri(), "validateDocument", 1);
      }
    });
  }

  @Override
  public WorkspaceService getWorkspaceService() {
    return new NoOpWorkspaceService() {
      @Override
      public void didChangeConfiguration(DidChangeConfigurationParams params) {
        super.didChangeConfiguration(params);
        settingsManager.setSettings(((JsonObject)params.getSettings()).get("ltex"));
        documents.values().forEach(LtexLanguageServer.this::publishIssues);
      }

      @Override
      public CompletableFuture<Object> executeCommand(ExecuteCommandParams params) {
        if (params.getCommand().equals(addToDictionaryCommandName) ||
              params.getCommand().equals(disableRuleCommandName) ||
              params.getCommand().equals(ignoreRuleInSentenceCommandName)) {
          languageClient.telemetryEvent(params.getArguments().get(0));
          return CompletableFuture.completedFuture(true);
        } else {
          return CompletableFuture.completedFuture(false);
        }
      }
    };
  }

  @Override
  public void connect(LanguageClient languageClient) {
    this.languageClient = languageClient;
  }
}
