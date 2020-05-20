package org.bsplines.ltex_ls;

import com.google.gson.*;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.parser.Parser;

import org.apache.commons.text.StringEscapeUtils;
import org.bsplines.ltex_ls.languagetool.*;
import org.bsplines.ltex_ls.latex.*;
import org.bsplines.ltex_ls.markdown.*;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.*;
import org.eclipse.xtext.xbase.lib.Pair;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedTextBuilder;

import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LtexLanguageServer implements LanguageServer, LanguageClientAware {

  private HashMap<String, TextDocumentItem> documents = new HashMap<>();
  private LanguageClient client = null;

  private HashMap<String, LanguageToolInterface> languageToolInterfaceMap = new HashMap<>();
  private HashMap<String, Settings> settingsMap = new HashMap<>();

  private LanguageToolInterface languageToolInterface;
  private Settings settings = new Settings();

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
    ret.setSeverity(settings.getDiagnosticSeverity());
    ret.setSource("LTeX - " + match.getRuleId());
    ret.setMessage(match.getMessage().replaceAll("<suggestion>(.*?)</suggestion>", "'$1'"));
    return ret;
  }

  private AnnotatedText invertAnnotatedText(AnnotatedText annotatedText) {
    List<Map.Entry<Integer, Integer>> mapping = annotatedText.getMapping();
    List<Map.Entry<Integer, Integer>> inverseMapping = new ArrayList<>();

    for (Map.Entry<Integer, Integer> entry : mapping) {
      inverseMapping.add(new AbstractMap.SimpleEntry<>(entry.getValue(), entry.getKey()));
    }

    return new AnnotatedText(Collections.emptyList(), inverseMapping, Collections.emptyMap(),
        Collections.emptyMap());
  }

  private int getPlainTextPositionFor(int originalTextPosition,
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

    reinitialize();
    String language = settings.getLanguageShortCode();
    settingsMap.put(language, settings);
    languageToolInterfaceMap.put(language, languageToolInterface);

    return CompletableFuture.completedFuture(new InitializeResult(capabilities));
  }

  private void reinitialize() {
    if (settings.getLanguageToolHttpServerUri() == null) {
      languageToolInterface = new LanguageToolJavaInterface(settings.getLanguageShortCode(),
          settings.getMotherTongueShortCode(), settings.getSentenceCacheSize(),
          settings.getDictionary());
    } else {
      languageToolInterface = new LanguageToolHttpInterface(settings.getLanguageToolHttpServerUri(),
          settings.getLanguageShortCode(),
          settings.getMotherTongueShortCode());
    }

    if (!languageToolInterface.isReady()) {
      languageToolInterface = null;
      return;
    }

    if (settings.getLanguageModelRulesDirectory() == null) {
      if (settings.getMotherTongueShortCode() != null) {
        languageToolInterface.activateDefaultFalseFriendRules();
      }
    } else {
      languageToolInterface.activateLanguageModelRules(
          settings.getLanguageModelRulesDirectory());
    }

    if (settings.getNeuralNetworkModelRulesDirectory() != null) {
      languageToolInterface.activateNeuralNetworkRules(
          settings.getNeuralNetworkModelRulesDirectory());
    }

    if (settings.getWord2VecModelRulesDirectory() != null) {
      languageToolInterface.activateWord2VecModelRules(
          settings.getWord2VecModelRulesDirectory());
    }

    languageToolInterface.enableRules(settings.getEnabledRules());
    languageToolInterface.disableRules(settings.getDisabledRules());
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
        VersionedTextDocumentIdentifier textDocument = new VersionedTextDocumentIdentifier(
            document.getUri(), document.getVersion());

        return validateDocument(document).thenApply(
            (Pair<List<LanguageToolRuleMatch>, AnnotatedText> validateResult) -> {
              if (validateResult.getValue() == null) {
                return Collections.emptyList();
              }

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
                    settings.getLanguageToolHttpServerUri().isEmpty()) {
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
                arguments.addProperty("commandName", addToDictionaryCommandName);
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
                      sentencePatternStringBuilder.append(Pattern.quote(
                          sentence.substring(lastEnd)));
                    }

                    ruleIdSentencePairs.add(pair);
                    ruleIds.add(ruleId);
                    ruleIdsJson.add(ruleId);
                    String sentencePatternString =
                        "^" + sentencePatternStringBuilder.toString() + "$";
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
                arguments.addProperty("commandName", ignoreRuleInSentenceCommandName);
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
                    Tools.i18n("disableRule") :
                    Tools.i18n("disableAllRulesWithMatchesInSelection")),
                    disableRuleCommandName);
                JsonObject arguments = new JsonObject();
                arguments.addProperty("commandName", disableRuleCommandName);
                arguments.addProperty("uri", document.getUri());
                arguments.add("ruleId", ruleIdsJson);
                command.setArguments(Arrays.asList(arguments));

                CodeAction codeAction = new CodeAction(command.getTitle());
                codeAction.setKind(disableRuleCodeActionKind);
                codeAction.setDiagnostics(diagnostics);
                codeAction.setCommand(command);
                result.add(Either.forRight(codeAction));
              }

              for (Map.Entry<String, List<LanguageToolRuleMatch>> entry :
                    useWordMatchesMap.entrySet()) {
                String newWord = entry.getKey();
                List<LanguageToolRuleMatch> useWordMatches = entry.getValue();
                List<Diagnostic> diagnostics = new ArrayList<>();
                List<Either<TextDocumentEdit, ResourceOperation>> documentChanges =
                    new ArrayList<>();

                for (LanguageToolRuleMatch match : useWordMatches) {
                  Diagnostic diagnostic = createDiagnostic(match, positionCalculator);
                  Range range = diagnostic.getRange();

                  diagnostics.add(diagnostic);
                  documentChanges.add(Either.forLeft(new TextDocumentEdit(textDocument,
                      Collections.singletonList(new TextEdit(range, newWord)))));
                }

                CodeAction codeAction = new CodeAction((useWordMatches.size() == 1) ?
                    Tools.i18n("useWord", newWord) :
                    Tools.i18n("useWordAllSelectedMatches", newWord));
                codeAction.setKind(acceptSuggestionCodeActionKind);
                codeAction.setDiagnostics(diagnostics);
                codeAction.setEdit(new WorkspaceEdit(documentChanges));
                result.add(Either.forRight(codeAction));
              }

              return result;
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
        client.publishDiagnostics(new PublishDiagnosticsParams(uri, Collections.emptyList()));
      }

      private void publishIssues(String uri) {
        TextDocumentItem document = documents.get(uri);
        LtexLanguageServer.this.publishIssues(document);
      }
    };
  }

  private CompletableFuture<Void> publishIssues(TextDocumentItem document) {
    return getIssues(document).thenApply(
        (List<Diagnostic> diagnostics) -> {
          client.publishDiagnostics(new PublishDiagnosticsParams(document.getUri(), diagnostics));
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

  private CompletableFuture<Pair<List<LanguageToolRuleMatch>, AnnotatedText>> validateDocument(
      TextDocumentItem document) {
    ConfigurationItem configurationItem = new ConfigurationItem();
    configurationItem.setSection("ltex");
    configurationItem.setScopeUri(document.getUri());
    CompletableFuture<List<Object>> configurationFuture = client.configuration(
        new ConfigurationParams(Arrays.asList(configurationItem)));

    return configurationFuture.thenApply(
        (List<Object> configuration) -> {
          setSettings((JsonElement) configuration.get(0));

          if (languageToolInterface == null) {
            Tools.logger.warning(Tools.i18n("skippingTextCheck"));
            return new Pair<>(Collections.emptyList(), null);
          }

          String codeLanguageId = document.getLanguageId();
          AnnotatedText annotatedText;

          switch (codeLanguageId) {
            case "plaintext": {
              AnnotatedTextBuilder builder = new AnnotatedTextBuilder();
              annotatedText = builder.addText(document.getText()).build();
              break;
            }
            case "markdown": {
              Parser p = Parser.builder().build();
              Document mdDocument = p.parse(document.getText());

              MarkdownAnnotatedTextBuilder builder = new MarkdownAnnotatedTextBuilder();
              builder.language = settings.getLanguageShortCode();
              builder.dummyNodeTypes.addAll(settings.getDummyMarkdownNodeTypes());
              builder.ignoreNodeTypes.addAll(settings.getIgnoreMarkdownNodeTypes());

              builder.visit(mdDocument);

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
            List<LanguageToolRuleMatch> result = languageToolInterface.check(annotatedText);

            Tools.logger.info((result.size() == 1) ? Tools.i18n("obtainedRuleMatch") :
                Tools.i18n("obtainedRuleMatches", result.size()));

            List<IgnoreRuleSentencePair> ignoreRuleSentencePairs =
                settings.getIgnoreRuleSentencePairs();

            if (!result.isEmpty() && !ignoreRuleSentencePairs.isEmpty()) {
              List<LanguageToolRuleMatch> ignoreMatches = new ArrayList<>();

              for (LanguageToolRuleMatch match : result) {
                if (match.getSentence() != null) {
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
              }

              if (!ignoreMatches.isEmpty()) {
                Tools.logger.info((ignoreMatches.size() == 1) ?
                    Tools.i18n("removedIgnoredRuleMatch") :
                    Tools.i18n("removedIgnoredRuleMatches", ignoreMatches.size()));
                for (LanguageToolRuleMatch match : ignoreMatches) result.remove(match);
              }
            }

            return new Pair<>(result, annotatedText);
          } catch (RuntimeException e) {
            Tools.logger.severe(Tools.i18n("languageToolFailed", e.getMessage()));
            e.printStackTrace();
            return new Pair<>(Collections.emptyList(), annotatedText);
          }
        });
  }

  @Override
  public WorkspaceService getWorkspaceService() {
    return new NoOpWorkspaceService() {
      @Override
      public void didChangeConfiguration(DidChangeConfigurationParams params) {
        super.didChangeConfiguration(params);
        setSettings(((JsonObject)params.getSettings()).get("ltex"));
        documents.values().forEach(LtexLanguageServer.this::publishIssues);
      }

      @Override
      public CompletableFuture<Object> executeCommand(ExecuteCommandParams params) {
        if (params.getCommand().equals(addToDictionaryCommandName) ||
              params.getCommand().equals(disableRuleCommandName) ||
              params.getCommand().equals(ignoreRuleInSentenceCommandName)) {
          client.telemetryEvent(params.getArguments().get(0));
          return CompletableFuture.completedFuture(true);
        } else {
          return CompletableFuture.completedFuture(false);
        }
      }
    };
  }

  private void setSettings(JsonElement jsonSettings) {
    Settings newSettings = new Settings(jsonSettings);
    String newLanguage = newSettings.getLanguageShortCode();
    Settings oldSettings = settingsMap.getOrDefault(newLanguage, null);

    if (newSettings.equals(oldSettings)) {
      settings = oldSettings;
      languageToolInterface = languageToolInterfaceMap.get(newLanguage);
    } else {
      settingsMap.put(newLanguage, newSettings);
      settings = newSettings;
      reinitialize();
      languageToolInterfaceMap.put(newLanguage, languageToolInterface);
    }
  }

  @Override
  public void connect(LanguageClient client) {
    this.client = client;
  }
}
