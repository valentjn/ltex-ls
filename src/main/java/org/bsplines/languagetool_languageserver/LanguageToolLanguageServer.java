package org.bsplines.languagetool_languageserver;

import com.google.gson.*;
import com.vladsch.flexmark.ast.Document;
import com.vladsch.flexmark.parser.Parser;

import org.apache.commons.text.StringEscapeUtils;
import org.bsplines.languagetool_languageserver.latex.*;
import org.bsplines.languagetool_languageserver.markdown.*;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.*;
import org.eclipse.xtext.xbase.lib.Pair;
import org.languagetool.*;
import org.languagetool.markup.*;
import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.rules.*;
import org.languagetool.rules.patterns.AbstractPatternRule;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

public class LanguageToolLanguageServer implements LanguageServer, LanguageClientAware {

  private HashMap<String, TextDocumentItem> documents = new HashMap<>();
  private LanguageClient client = null;

  private HashMap<String, JLanguageTool> languageToolMap = new HashMap<>();
  private HashMap<String, Settings> settingsMap = new HashMap<>();

  private JLanguageTool languageTool;
  private Settings settings = new Settings();

  private static final long resultCacheMaxSize = 10000;
  private static final int resultCacheExpireAfterMinutes = 10;
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

  private static boolean locationOverlaps(
      RuleMatch match, DocumentPositionCalculator positionCalculator, Range range) {
    return overlaps(range, new Range(
        positionCalculator.getPosition(match.getFromPos()),
        positionCalculator.getPosition(match.getToPos())));
  }

  private static boolean overlaps(Range r1, Range r2) {
    return r1.getStart().getCharacter() <= r2.getEnd().getCharacter() &&
        r1.getEnd().getCharacter() >= r2.getStart().getCharacter() &&
        r1.getStart().getLine() >= r2.getEnd().getLine() &&
        r1.getEnd().getLine() <= r2.getStart().getLine();
  }

  private Diagnostic createDiagnostic(
      RuleMatch match, DocumentPositionCalculator positionCalculator) {
    Diagnostic ret = new Diagnostic();
    ret.setRange(new Range(
        positionCalculator.getPosition(match.getFromPos()),
        positionCalculator.getPosition(match.getToPos())));
    ret.setSeverity(settings.getDiagnosticSeverity());
    ret.setSource("LTeX - " + match.getRule().getId());
    ret.setMessage(match.getMessage().replaceAll("<suggestion>(.*?)</suggestion>", "'$1'"));
    return ret;
  }

  private AnnotatedText invertAnnotatedText(AnnotatedText annotatedText) {
    Map<Integer, Integer> mapping = annotatedText.getMapping();
    Map<Integer, Integer> inverseMapping = new HashMap<>();

    for (Map.Entry<Integer, Integer> entry : mapping.entrySet()) {
      inverseMapping.put(entry.getValue(), entry.getKey());
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
    languageToolMap.put(language, languageTool);

    return CompletableFuture.completedFuture(new InitializeResult(capabilities));
  }

  private void reinitialize() {
    languageTool = null;

    if (!Languages.isLanguageSupported(settings.getLanguageShortCode())) {
      Tools.logger.severe(Tools.i18n("notARecognizedLanguage", settings.getLanguageShortCode()));
      return;
    }

    Language language = Languages.getLanguageForShortCode(settings.getLanguageShortCode());
    String motherTongueShortCode = settings.getMotherTongueShortCode();
    Language motherTongue = ((motherTongueShortCode != null) ?
        Languages.getLanguageForShortCode(motherTongueShortCode) : null);
    ResultCache resultCache = new ResultCache(resultCacheMaxSize, resultCacheExpireAfterMinutes,
        TimeUnit.MINUTES);
    UserConfig userConfig = new UserConfig(settings.getDictionary());
    languageTool = new JLanguageTool(language, motherTongue, resultCache, userConfig);

    if (settings.getLanguageModelRulesDirectory() == null) {
      if (motherTongue != null) {
        // from JLanguageTool.activateDefaultFalseFriendRules (which is private)
        String falseFriendRulePath = JLanguageTool.getDataBroker().getRulesDir() + "/" +
            JLanguageTool.FALSE_FRIEND_FILE;

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
    } else {
      try {
        languageTool.activateLanguageModelRules(
            new File(settings.getLanguageModelRulesDirectory()));
      } catch (IOException | RuntimeException e) {
        Tools.logger.warning(Tools.i18n("couldNotLoadLanguageModel",
            settings.getLanguageModelRulesDirectory(), e.getMessage()));
        e.printStackTrace();
      }
    }

    if (settings.getNeuralNetworkModelRulesDirectory() != null) {
      try {
        languageTool.activateNeuralNetworkRules(
            new File(settings.getNeuralNetworkModelRulesDirectory()));
      } catch (IOException | RuntimeException e) {
        Tools.logger.warning(Tools.i18n("couldNotLoadNeuralNetworkModel",
            settings.getNeuralNetworkModelRulesDirectory(), e.getMessage()));
        e.printStackTrace();
      }
    }

    if (settings.getWord2VecModelRulesDirectory() != null) {
      try {
        languageTool.activateWord2VecModelRules(
            new File(settings.getWord2VecModelRulesDirectory()));
      } catch (IOException | RuntimeException e) {
        Tools.logger.warning(Tools.i18n("couldNotLoadWord2VecModel",
            settings.getWord2VecModelRulesDirectory(), e.getMessage()));
        e.printStackTrace();
      }
    }

    // for strange reasons there is no JLanguageTool.enableRules
    for (String ruleId : settings.getEnabledRules()) {
      languageTool.enableRule(ruleId);
    }

    languageTool.disableRules(settings.getDisabledRules());
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
            (Pair<List<RuleMatch>, AnnotatedText> validateResult) -> {
              if (validateResult.getValue() == null) {
                return Collections.emptyList();
              }

              String text = document.getText();
              String plainText = validateResult.getValue().getPlainText();
              AnnotatedText inverseAnnotatedText = null;
              DocumentPositionCalculator positionCalculator = new DocumentPositionCalculator(text);
              List<Either<Command, CodeAction>> result =
                  new ArrayList<Either<Command, CodeAction>>();

              for (RuleMatch match : validateResult.getKey()) {
                if (locationOverlaps(match, positionCalculator, params.getRange())) {
                  String ruleId = match.getRule().getId();
                  Diagnostic diagnostic = createDiagnostic(match, positionCalculator);
                  Range range = diagnostic.getRange();

                  if (ruleId.startsWith("MORFOLOGIK_") || ruleId.startsWith("HUNSPELL_") ||
                      ruleId.startsWith("GERMAN_SPELLER_")) {
                    if (inverseAnnotatedText == null) {
                      inverseAnnotatedText = invertAnnotatedText(validateResult.getValue());
                    }

                    String word = plainText.substring(
                        getPlainTextPositionFor(match.getFromPos(), inverseAnnotatedText),
                        getPlainTextPositionFor(match.getToPos(), inverseAnnotatedText));
                    Command command = new Command(Tools.i18n("addWordToDictionary", word),
                        addToDictionaryCommandName);
                    JsonObject arguments = new JsonObject();
                    arguments.addProperty("commandName", addToDictionaryCommandName);
                    arguments.addProperty("uri", document.getUri());
                    arguments.addProperty("word", word);
                    command.setArguments(Arrays.asList(arguments));

                    CodeAction codeAction = new CodeAction(command.getTitle());
                    codeAction.setKind(addToDictionaryCodeActionKind);
                    codeAction.setDiagnostics(Collections.singletonList(diagnostic));
                    codeAction.setCommand(command);
                    result.add(Either.forRight(codeAction));
                  }

                  if (match.getSentence() != null) {
                    String sentence = match.getSentence().getText().trim();
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

                    String sentencePatternString = "^" + sentencePatternStringBuilder.toString() +
                        "$";
                    Command command = new Command(Tools.i18n("ignoreRuleInThisSentence"),
                        ignoreRuleInSentenceCommandName);
                    JsonObject arguments = new JsonObject();
                    arguments.addProperty("commandName", ignoreRuleInSentenceCommandName);
                    arguments.addProperty("uri", document.getUri());
                    arguments.addProperty("ruleId", ruleId);
                    arguments.addProperty("sentencePattern", sentencePatternString);
                    command.setArguments(Arrays.asList(arguments));

                    CodeAction codeAction = new CodeAction(command.getTitle());
                    codeAction.setKind(ignoreRuleInSentenceCodeActionKind);
                    codeAction.setDiagnostics(Collections.singletonList(diagnostic));
                    codeAction.setCommand(command);
                    result.add(Either.forRight(codeAction));
                  }

                  {
                    Command command = new Command(Tools.i18n("disableRule"),
                        disableRuleCommandName);
                    JsonObject arguments = new JsonObject();
                    arguments.addProperty("commandName", disableRuleCommandName);
                    arguments.addProperty("uri", document.getUri());
                    arguments.addProperty("ruleId", ruleId);
                    command.setArguments(Arrays.asList(arguments));

                    CodeAction codeAction = new CodeAction(command.getTitle());
                    codeAction.setKind(disableRuleCodeActionKind);
                    codeAction.setDiagnostics(Collections.singletonList(diagnostic));
                    codeAction.setCommand(command);
                    result.add(Either.forRight(codeAction));
                  }

                  for (String newWord : match.getSuggestedReplacements()) {
                    CodeAction codeAction = new CodeAction(Tools.i18n("useWord", newWord));
                    codeAction.setKind(acceptSuggestionCodeActionKind);
                    codeAction.setDiagnostics(Collections.singletonList(diagnostic));
                    codeAction.setEdit(new WorkspaceEdit(Collections.singletonList(
                      Either.forLeft(new TextDocumentEdit(textDocument,
                        Collections.singletonList(new TextEdit(range, newWord)))))));
                    result.add(Either.forRight(codeAction));
                  }
                }
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
        LanguageToolLanguageServer.this.publishIssues(document);
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
        (Pair<List<RuleMatch>, AnnotatedText> validateResult) -> {
          List<RuleMatch> matches = validateResult.getKey();
          DocumentPositionCalculator positionCalculator =
              new DocumentPositionCalculator(document.getText());

          return matches.stream().map(
              match -> createDiagnostic(match, positionCalculator)).collect(Collectors.toList());
        });
  }

  private void enableEasterEgg(JLanguageTool languageTool) {
    languageTool.addRule(new Rule() {
      public String getId() { return "bspline"; };
      public String getDescription() { return "Unknown basis function"; };
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
      public String getId() { return "ungendered"; };
      public String getDescription() { return "Ungendered variant"; };
      public RuleMatch[] match(AnalyzedSentence sentence) {
        List<RuleMatch> matches = new ArrayList<>();
        for (AnalyzedTokenReadings token : sentence.getTokens()) {
          String s = token.getToken();
          if ((s.length() >= 2) &&
              (s.substring(s.length() - 2, s.length()).equalsIgnoreCase("er"))) {
            matches.add(new RuleMatch(this, sentence, token.getStartPos(), token.getEndPos(),
                "Ungendered variant detected. " +
                "Did you mean <suggestion>" + s + "*in</suggestion>?"));
          }
        }
        return matches.toArray(new RuleMatch[]{});
      }
    });
  }

  private CompletableFuture<Pair<List<RuleMatch>, AnnotatedText>> validateDocument(
      TextDocumentItem document) {
    ConfigurationItem configurationItem = new ConfigurationItem();
    configurationItem.setSection("ltex");
    configurationItem.setScopeUri(document.getUri());
    CompletableFuture<List<Object>> configurationFuture = client.configuration(
        new ConfigurationParams(Arrays.asList(configurationItem)));

    return configurationFuture.thenApply(
        (List<Object> configuration) -> {
          setSettings((JsonElement) configuration.get(0));

          if (languageTool == null) {
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
              Document mdDocument = (Document) p.parse(document.getText());

              MarkdownAnnotatedTextBuilder builder = new MarkdownAnnotatedTextBuilder();
              builder.visit(mdDocument);

              annotatedText = builder.getAnnotatedText();
              break;
            }
            case "latex": {
              LatexAnnotatedTextBuilder builder = new LatexAnnotatedTextBuilder();

              for (String commandPrototype : settings.getDummyCommandPrototypes()) {
                builder.commandSignatures.add(new LatexCommandSignature(commandPrototype,
                    LatexCommandSignature.Action.DUMMY));
              }

              for (String commandPrototype : settings.getIgnoreCommandPrototypes()) {
                builder.commandSignatures.add(new LatexCommandSignature(commandPrototype,
                    LatexCommandSignature.Action.IGNORE));
              }

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

          if (settings.getDictionary().stream().anyMatch("BsPlInEs"::equals)) {
            enableEasterEgg(languageTool);
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
            List<RuleMatch> result = languageTool.check(annotatedText);

            Tools.logger.info((result.size() == 1) ? Tools.i18n("obtainedRuleMatch") :
                Tools.i18n("obtainedRuleMatches", result.size()));

            List<Pair<String, Pattern>> ignoreRuleSentencePairs =
                settings.getIgnoreRuleSentencePairs();

            if (!result.isEmpty() && !ignoreRuleSentencePairs.isEmpty()) {
              List<RuleMatch> ignoreMatches = new ArrayList<>();

              for (RuleMatch match : result) {
                if (match.getSentence() != null) {
                  String ruleId = match.getRule().getId();
                  String sentence = match.getSentence().getText().trim();

                  for (Pair<String, Pattern> pair : ignoreRuleSentencePairs) {
                    if (pair.getKey().equals(ruleId) && pair.getValue().matcher(sentence).find()) {
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
                for (RuleMatch match : ignoreMatches) result.remove(match);
              }
            }

            return new Pair<>(result, annotatedText);
          } catch (RuntimeException | IOException e) {
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
        setSettings(((JsonObject) params.getSettings()).get("ltex"));
        documents.values().forEach(LanguageToolLanguageServer.this::publishIssues);
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
      languageTool = languageToolMap.get(newLanguage);
    } else {
      settingsMap.put(newLanguage, newSettings);
      settings = newSettings;
      reinitialize();
      languageToolMap.put(newLanguage, languageTool);
    }
  }

  @Override
  public void connect(LanguageClient client) {
    this.client = client;
  }
}
