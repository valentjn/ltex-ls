import com.google.gson.*;
import com.vladsch.flexmark.ast.Document;
import com.vladsch.flexmark.parser.Parser;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.*;
import org.jetbrains.annotations.NotNull;
import org.languagetool.*;
import org.languagetool.markup.*;
import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.rules.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.logging.*;

class LanguageToolLanguageServer implements LanguageServer, LanguageClientAware {

  HashMap<String, TextDocumentItem> documents = new HashMap<>();
  private LanguageClient client = null;

  private ResultCache resultCache =
      new ResultCache(resultCacheMaxSize, resultCacheExpireAfterMinutes, TimeUnit.MINUTES);

  private List<String> dictionary = null;
  private String languageShortCode = "en-US";
  private List<String> dummyCommandPrototypes = null;
  private List<String> ignoreCommandPrototypes = null;

  private static final long resultCacheMaxSize = 10000;
  private static final int resultCacheExpireAfterMinutes = 10;
  private static final String acceptSuggestionCommandName = "languageTool.acceptSuggestion";
  private static final Logger logger = Logger.getLogger("LanguageToolLanguageServer");

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

  @Override
  public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
    ServerCapabilities capabilities = new ServerCapabilities();
    capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
    capabilities.setCodeActionProvider(
        new CodeActionOptions(Arrays.asList(CodeActionKind.QuickFix)));
    capabilities.setExecuteCommandProvider(
        new ExecuteCommandOptions(Collections.singletonList(acceptSuggestionCommandName)));
    return CompletableFuture.completedFuture(new InitializeResult(capabilities));
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
        List<RuleMatch> matches = validateDocument(document);
        DocumentPositionCalculator positionCalculator =
            new DocumentPositionCalculator(document.getText());
        List<Either<Command, CodeAction>> result =
            new ArrayList<Either<Command, CodeAction>>();

        for (RuleMatch match : matches) {
          if (locationOverlaps(match, positionCalculator, params.getRange())) {
            Diagnostic diagnostic = createDiagnostic(match, positionCalculator);
            Range range = diagnostic.getRange();

            for (String newText : match.getSuggestedReplacements()) {
              Command command = new Command();
              command.setCommand(acceptSuggestionCommandName);
              command.setTitle(newText);
              command.setArguments(Collections.singletonList(new TextDocumentEdit(
                  textDocument, Collections.singletonList(new TextEdit(range, newText)))));

              CodeAction codeAction = new CodeAction();
              codeAction.setTitle(newText);
              codeAction.setKind(CodeActionKind.QuickFix);
              codeAction.setDiagnostics(Collections.singletonList(diagnostic));
              codeAction.setCommand(command);

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
    List<RuleMatch> matches = validateDocument(document);
    DocumentPositionCalculator positionCalculator =
        new DocumentPositionCalculator(document.getText());

    return matches.stream().map(
        match -> createDiagnostic(match, positionCalculator)).collect(Collectors.toList());
  }

  private List<RuleMatch> validateDocument(TextDocumentItem document) {
    // This setting is specific to VS Code behavior and maintaining it here
    // long term is not desirable because other clients may behave differently.
    // See: https://github.com/Microsoft/vscode/issues/28732
    String uri = document.getUri();
    Boolean isSupportedScheme = uri.startsWith("file:") || uri.startsWith("untitled:") ;
    Language language;

    if (Languages.isLanguageSupported(languageShortCode)) {
      language = Languages.getLanguageForShortCode(languageShortCode);
    } else {
      logger.warning("ERROR: " + languageShortCode + " is not a recognized language. " +
                     "Checking disabled.");
      language = null;
    }

    if (language == null || !isSupportedScheme) {
      return Collections.emptyList();
    } else {
      UserConfig userConfig = ((dictionary != null) ?
          new UserConfig(dictionary) : new UserConfig());
      JLanguageTool languageTool = new JLanguageTool(language, resultCache, userConfig);

      String codeLanguageId = document.getLanguageId();
      try {
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

            if (dummyCommandPrototypes != null) {
              for (String commandPrototype : dummyCommandPrototypes) {
                builder.commandSignatures.add(new latex.CommandSignature(commandPrototype,
                    latex.CommandSignature.Action.DUMMY));
              }
            }

            if (ignoreCommandPrototypes != null) {
              for (String commandPrototype : ignoreCommandPrototypes) {
                builder.commandSignatures.add(new latex.CommandSignature(commandPrototype,
                    latex.CommandSignature.Action.IGNORE));
              }
            }

            builder.addCode(document.getText());
            annotatedText = builder.getAnnotatedText();
            break;
          }
          default: {
            throw new UnsupportedOperationException(String.format(
                "Code language \"%s\" is not supported.", codeLanguageId));
          }
        }

        logger.info("Checking the following text in language \"" + languageShortCode +
            "\" via LanguageTool: <" + annotatedText.getPlainText() + ">");

        try {
          List<RuleMatch> result = languageTool.check(annotatedText);
          return result;
        } catch (RuntimeException e) {
          logger.severe("LanguageTool failed: " + e.getMessage());
          e.printStackTrace();
          return Collections.emptyList();
        }
      } catch (IOException e) {
        e.printStackTrace();
        return Collections.emptyList();
      }
    }
  }

  @Override
  public WorkspaceService getWorkspaceService() {
    return new NoOpWorkspaceService() {
      @Override
      public void didChangeConfiguration(DidChangeConfigurationParams params) {
        super.didChangeConfiguration(params);
        setSettings(params.getSettings());
      }

      @SuppressWarnings({"unchecked", "rawtypes"})
      @Override
      public CompletableFuture<Object> executeCommand(ExecuteCommandParams params) {
        if (Objects.equals(params.getCommand(), acceptSuggestionCommandName)) {
          List<Either<TextDocumentEdit, ResourceOperation>> documentChanges = new ArrayList<>();

          for (JsonObject json : (List<JsonObject>) (List) params.getArguments()) {
            VersionedTextDocumentIdentifier textDocument =
                new VersionedTextDocumentIdentifier(
                  json.getAsJsonObject("textDocument").get("uri").getAsString(),
                  json.getAsJsonObject("textDocument").get("version").getAsInt());
            List<TextEdit> edits = new ArrayList<>();

            for (JsonElement jsonEdit : json.getAsJsonArray("edits")) {
              JsonObject jsonRange = jsonEdit.getAsJsonObject().getAsJsonObject("range");
              Range range = new Range(
                  new Position(jsonRange.getAsJsonObject("start").get("line").getAsInt(),
                    jsonRange.getAsJsonObject("start").get("character").getAsInt()),
                  new Position(jsonRange.getAsJsonObject("end").get("line").getAsInt(),
                    jsonRange.getAsJsonObject("end").get("character").getAsInt()));
              String newText = jsonEdit.getAsJsonObject().get("newText").getAsString();
              edits.add(new TextEdit(range, newText));
            }

            documentChanges.add(Either.forLeft(new TextDocumentEdit(textDocument, edits)));
          }

          return (CompletableFuture<Object>) (CompletableFuture) client.applyEdit(
              new ApplyWorkspaceEditParams(new WorkspaceEdit(documentChanges)));
        }
        return CompletableFuture.completedFuture(false);
      }
    };
  }

  @SuppressWarnings("unchecked")
  private static Object getSettingFromObject(@NotNull Object settings, String name) {
    for (String component : name.split("\\.")) {
      try {
        settings = ((Map<String, Object>) settings).get(component);
      } catch (ClassCastException e) {
        return null;
      }
    }

    return settings;
  }

  @SuppressWarnings("unchecked")
  private void setSettings(@NotNull Object settings) {
    List<String> dictionary =
        (List<String>) getSettingFromObject(settings, "languageTool.dictionary");
    if (dictionary != null) this.dictionary = dictionary;
    String languageShortCode = (String) getSettingFromObject(settings, "languageTool.language");
    if (languageShortCode != null) this.languageShortCode = languageShortCode;
    List<String> dummyCommandPrototypes = (List<String>) getSettingFromObject(settings,
        "languageTool.latex.dummyCommands");
    if (dummyCommandPrototypes != null) this.dummyCommandPrototypes = dummyCommandPrototypes;
    List<String> ignoreCommandPrototypes = (List<String>) getSettingFromObject(settings,
        "languageTool.latex.ignoreCommands");
    if (ignoreCommandPrototypes != null) this.ignoreCommandPrototypes = ignoreCommandPrototypes;

    resultCache = new ResultCache(resultCacheMaxSize, resultCacheExpireAfterMinutes,
        TimeUnit.MINUTES);
    documents.values().forEach(this::publishIssues);
  }

  @Override
  public void connect(LanguageClient client) {
    this.client = client;
  }
}
