import com.google.gson.*;
import com.vladsch.flexmark.ast.Document;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.Pair;

import org.apache.commons.text.StringEscapeUtils;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.*;
import org.languagetool.*;
import org.languagetool.markup.*;
import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.rules.*;

import java.io.IOException;
import java.lang.reflect.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.logging.*;

class LanguageToolLanguageServer implements LanguageServer, LanguageClientAware {

  private HashMap<String, TextDocumentItem> documents = new HashMap<>();
  private LanguageClient client = null;
  private ResourceBundle messages;

  {
    try {
      messages = ResourceBundle.getBundle("MessagesBundle", Locale.getDefault());
    } catch (MissingResourceException e) {
      messages = ResourceBundle.getBundle("MessagesBundle", Locale.ENGLISH);
    }
  }

  private JLanguageTool languageTool;
  private Settings settings = new Settings();

  private static final long resultCacheMaxSize = 10000;
  private static final int resultCacheExpireAfterMinutes = 10;
  private static final String acceptSuggestionCodeActionKind =
      CodeActionKind.QuickFix + ".ltex.acceptSuggestion";
  private static final String addToDictionaryCodeActionKind =
      CodeActionKind.QuickFix + ".ltex.addToDictionary";
  private static final String addToDictionaryCommandName = "ltex.addToDictionary";
  private static final Logger logger = Logger.getLogger("LanguageToolLanguageServer");

  // https://stackoverflow.com/a/23717493
  private static class DualConsoleHandler extends StreamHandler {
    private final ConsoleHandler stdErrHandler = new ConsoleHandler();

    public DualConsoleHandler() {
      super(System.out, new SimpleFormatter());
    }

    @Override
    public void publish(LogRecord record) {
      if (record.getLevel().intValue() <= Level.INFO.intValue()) {
        super.publish(record);
        super.flush();
      } else {
        stdErrHandler.publish(record);
        stdErrHandler.flush();
      }
    }
  }

  static {
    logger.setUseParentHandlers(false);
    logger.addHandler(new DualConsoleHandler());
  }

  private String i18n(String key, Object... messageArguments) {
    MessageFormat formatter = new MessageFormat("");
    formatter.applyPattern(messages.getString(key).replaceAll("'", "''"));
    return formatter.format(messageArguments);
  }

  private static boolean locationOverlaps(
      RuleMatch match, DocumentPositionCalculator positionCalculator, Range range) {
    return overlaps(range, createDiagnostic(match, positionCalculator).getRange());
  }

  private static boolean overlaps(Range r1, Range r2) {
    return r1.getStart().getCharacter() <= r2.getEnd().getCharacter() &&
        r1.getEnd().getCharacter() >= r2.getStart().getCharacter() &&
        r1.getStart().getLine() >= r2.getEnd().getLine() &&
        r1.getEnd().getLine() <= r2.getStart().getLine();
  }

  private static Diagnostic createDiagnostic(
      RuleMatch match, DocumentPositionCalculator positionCalculator) {
    Diagnostic ret = new Diagnostic();
    ret.setRange(new Range(
        positionCalculator.getPosition(match.getFromPos()),
        positionCalculator.getPosition(match.getToPos())));
    ret.setSeverity(DiagnosticSeverity.Warning);
    ret.setSource("LT - " + match.getRule().getDescription());
    ret.setMessage(match.getMessage().replaceAll("<suggestion>(.*?)</suggestion>", "'$1'"));
    return ret;
  }

  @SuppressWarnings("unchecked")
  private AnnotatedText invertAnnotatedText(AnnotatedText annotatedText) {
    Field field;

    try {
      field = annotatedText.getClass().getDeclaredField("mapping");
    } catch (NoSuchFieldException | SecurityException e) {
      e.printStackTrace();
      return null;
    }

    field.setAccessible(true);
    Map<Integer, Integer> map;

    try {
      map = (Map<Integer, Integer>) field.get(annotatedText);
    } catch (IllegalArgumentException | IllegalAccessException e) {
      e.printStackTrace();
      return null;
    }

    Map<Integer, Integer> inverseMapping = new HashMap<>();
    Iterator<Map.Entry<Integer, Integer>> it = map.entrySet().iterator();

    while (it.hasNext()) {
      Map.Entry<Integer, Integer> pair = it.next();
      inverseMapping.put(pair.getValue(), pair.getKey());
    }

    Constructor<AnnotatedText> constructor = (Constructor<AnnotatedText>)
        AnnotatedText.class.getDeclaredConstructors()[0];
    constructor.setAccessible(true);
    AnnotatedText inverseAnnotatedText;

    try {
      inverseAnnotatedText = constructor.newInstance(Collections.emptyList(), inverseMapping,
          Collections.emptyMap(), Collections.emptyMap());
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException |
        InvocationTargetException e) {
      e.printStackTrace();
      return null;
    }

    return inverseAnnotatedText;
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
          acceptSuggestionCodeActionKind, addToDictionaryCodeActionKind)));
    capabilities.setExecuteCommandProvider(
        new ExecuteCommandOptions(Collections.singletonList(addToDictionaryCommandName)));

    // Until it is specified in the LSP that the locale is automatically sent with
    // the initialization request, we have to do that manually.
    // See https://github.com/microsoft/language-server-protocol/issues/754.
    JsonObject initializationOptions = (JsonObject) params.getInitializationOptions();
    String localeLanguage = initializationOptions.get("locale").getAsString();
    Locale locale = Locale.forLanguageTag(localeLanguage);
    logger.info("Setting locale to " + locale.getLanguage() + ".");
    messages = ResourceBundle.getBundle("MessagesBundle", locale);

    reinitialize();

    return CompletableFuture.completedFuture(new InitializeResult(capabilities));
  }

  private void reinitialize() {
    languageTool = null;

    if (!Languages.isLanguageSupported(settings.getLanguageShortCode())) {
      logger.severe(settings.getLanguageShortCode() + " is not a recognized language. " +
          "Leaving LanguageTool uninitialized, checking disabled.");
      return;
    }

    Language language = Languages.getLanguageForShortCode(settings.getLanguageShortCode());
    ResultCache resultCache = new ResultCache(resultCacheMaxSize, resultCacheExpireAfterMinutes,
        TimeUnit.MINUTES);
    UserConfig userConfig = ((settings.getDictionary() != null) ?
        new UserConfig(settings.getDictionary()) : new UserConfig());
    languageTool = new JLanguageTool(language, resultCache, userConfig);

    documents.values().forEach(this::publishIssues);
  }

  @Override
  public CompletableFuture<Object> shutdown() {
    // Per https://github.com/eclipse/lsp4j/issues/18
    return CompletableFuture.completedFuture(new Object());
  }

  @Override
  public void exit() {
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
        Pair<List<RuleMatch>, AnnotatedText> validateResult = validateDocument(document);
        String text = document.getText();
        String plainText = validateResult.getSecond().getPlainText();
        AnnotatedText inverseAnnotatedText = null;
        DocumentPositionCalculator positionCalculator = new DocumentPositionCalculator(text);
        List<Either<Command, CodeAction>> result = new ArrayList<Either<Command, CodeAction>>();

        for (RuleMatch match : validateResult.getFirst()) {
          if (locationOverlaps(match, positionCalculator, params.getRange())) {
            String ruleId = match.getRule().getId();
            Diagnostic diagnostic = createDiagnostic(match, positionCalculator);
            Range range = diagnostic.getRange();

            if (ruleId.startsWith("MORFOLOGIK_") || ruleId.startsWith("HUNSPELL_") ||
                ruleId.startsWith("GERMAN_SPELLER_")) {
              if (inverseAnnotatedText == null) {
                inverseAnnotatedText = invertAnnotatedText(validateResult.getSecond());
              }

              String word = plainText.substring(
                  getPlainTextPositionFor(match.getFromPos(), inverseAnnotatedText),
                  getPlainTextPositionFor(match.getToPos(), inverseAnnotatedText));
              Command command = new Command(i18n("addWordToDictionary", word),
                  addToDictionaryCommandName);
              command.setCommand(addToDictionaryCommandName);
              command.setArguments(Arrays.asList(new Object[] { word }));

              CodeAction codeAction = new CodeAction(command.getTitle());
              codeAction.setKind(addToDictionaryCodeActionKind);
              codeAction.setDiagnostics(Collections.singletonList(diagnostic));
              codeAction.setCommand(command);
              result.add(Either.forRight(codeAction));
            }

            for (String newWord : match.getSuggestedReplacements()) {
              CodeAction codeAction = new CodeAction(i18n("useWord", newWord));
              codeAction.setKind(acceptSuggestionCodeActionKind);
              codeAction.setDiagnostics(Collections.singletonList(diagnostic));
              codeAction.setEdit(new WorkspaceEdit(Collections.singletonList(
                Either.forLeft(new TextDocumentEdit(textDocument,
                  Collections.singletonList(new TextEdit(range, newWord)))))));
              result.add(Either.forRight(codeAction));
            }
          }
        }

        return CompletableFuture.completedFuture(result);
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

      private void publishIssues(String uri) {
        TextDocumentItem document = this.documents.get(uri);
        LanguageToolLanguageServer.this.publishIssues(document);
      }
    };
  }

  private void publishIssues(TextDocumentItem document) {
    List<Diagnostic> diagnostics = getIssues(document);

    client.publishDiagnostics(new PublishDiagnosticsParams(document.getUri(), diagnostics));
  }

  private List<Diagnostic> getIssues(TextDocumentItem document) {
    List<RuleMatch> matches = validateDocument(document).getFirst();
    DocumentPositionCalculator positionCalculator =
        new DocumentPositionCalculator(document.getText());

    return matches.stream().map(
        match -> createDiagnostic(match, positionCalculator)).collect(Collectors.toList());
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

  private Pair<List<RuleMatch>, AnnotatedText> validateDocument(TextDocumentItem document) {
    if (languageTool == null) {
      logger.warning("Skipping check of text, because LanguageTool has not been initialized " +
          "(see above).");
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

        markdown.AnnotatedTextBuilder builder =
            new markdown.AnnotatedTextBuilder();
        builder.visit(mdDocument);

        annotatedText = builder.getAnnotatedText();
        break;
      }
      case "latex": {
        latex.AnnotatedTextBuilder builder = new latex.AnnotatedTextBuilder();

        if (settings.getDummyCommandPrototypes() != null) {
          for (String commandPrototype : settings.getDummyCommandPrototypes()) {
            builder.commandSignatures.add(new latex.CommandSignature(commandPrototype,
                latex.CommandSignature.Action.DUMMY));
          }
        }

        if (settings.getIgnoreCommandPrototypes() != null) {
          for (String commandPrototype : settings.getIgnoreCommandPrototypes()) {
            builder.commandSignatures.add(new latex.CommandSignature(commandPrototype,
                latex.CommandSignature.Action.IGNORE));
          }
        }

        ExecutorService executor = Executors.newCachedThreadPool();
        Future<Object> future = executor.submit(new Callable<Object>() {
          public Object call() {
            builder.addCode(document.getText());
            return null;
          }
        });

        try {
          future.get(10, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
          throw new RuntimeException(i18n("latexAnnotatedTextBuilderFailed"), e);
        } finally {
          future.cancel(true); // may or may not desire this
        }

        annotatedText = builder.getAnnotatedText();
        break;
      }
      default: {
        throw new UnsupportedOperationException(i18n("codeLanguageNotSupported", codeLanguageId));
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
        postfix = "... (truncated to " + logTextMaxLength + " characters)";
      }

      logger.info("Checking the following text in language \"" + settings.getLanguageShortCode() +
          "\" via LanguageTool: \"" + StringEscapeUtils.escapeJava(logText) + "\"" + postfix);
    }

    try {
      List<RuleMatch> result = languageTool.check(annotatedText);
      logger.info("Obtained " + result.size() + " rule match" +
          ((result.size() != 1) ? "es" : ""));
      return new Pair<>(result, annotatedText);
    } catch (RuntimeException | IOException e) {
      logger.severe("LanguageTool failed: " + e.getMessage());
      e.printStackTrace();
      return new Pair<>(Collections.emptyList(), annotatedText);
    }
  }

  @Override
  public WorkspaceService getWorkspaceService() {
    return new NoOpWorkspaceService() {
      @Override
      public void didChangeConfiguration(DidChangeConfigurationParams params) {
        super.didChangeConfiguration(params);
        setSettings((JsonElement) params.getSettings());
      }

      @Override
      public CompletableFuture<Object> executeCommand(ExecuteCommandParams params) {
        if (Objects.equals(params.getCommand(), addToDictionaryCommandName)) {
          String word = ((JsonElement) params.getArguments().get(0)).getAsString();
          client.telemetryEvent(addToDictionaryCommandName + " " + word);
          return CompletableFuture.completedFuture(true);
        }

        return CompletableFuture.completedFuture(false);
      }
    };
  }

  private void setSettings(JsonElement jsonSettings) {
    Settings oldSettings = (Settings) settings.clone();
    settings.setSettings(jsonSettings);
    if (!settings.equals(oldSettings)) reinitialize();
  }

  @Override
  public void connect(LanguageClient client) {
    this.client = client;
  }
}
