import com.vladsch.flexmark.ast.Document;
import com.vladsch.flexmark.parser.Parser;
import markdown.AnnotatedTextBuildingVisitor;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class LanguageToolLanguageServer implements LanguageServer, LanguageClientAware {

  HashMap<String, TextDocumentItem> documents = new HashMap<>();
  private LanguageClient client = null;
  @Nullable
  private Language language;

  private static boolean locationOverlaps(RuleMatch match, DocumentPositionCalculator positionCalculator, Range range) {
    return overlaps(range, createDiagnostic(match, positionCalculator).getRange());
  }

  private static boolean overlaps(Range r1, Range r2) {
    return r1.getStart().getCharacter() <= r2.getEnd().getCharacter() &&
    r1.getEnd().getCharacter() >= r2.getStart().getCharacter() &&
    r1.getStart().getLine() >= r2.getEnd().getLine() &&
    r1.getEnd().getLine() <= r2.getStart().getLine();
  }

  private static Diagnostic createDiagnostic(RuleMatch match, DocumentPositionCalculator positionCalculator) {
    Diagnostic ret = new Diagnostic();
    ret.setRange(
    new Range(
    positionCalculator.getPosition(match.getFromPos()),
    positionCalculator.getPosition(match.getToPos())));
    ret.setSeverity(DiagnosticSeverity.Warning);
    ret.setSource(String.format("LanguageTool: %s", match.getRule().getDescription()));
    ret.setMessage(match.getMessage());
    return ret;
  }

  @Override
  public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
    ServerCapabilities capabilities = new ServerCapabilities();
    capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
    capabilities.setCodeActionProvider(true);
    capabilities.setExecuteCommandProvider(
    new ExecuteCommandOptions(Collections.singletonList(TextEditCommand.CommandName)));
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
      public CompletableFuture<List<? extends Command>> codeAction(CodeActionParams params) {
        if (params.getContext().getDiagnostics().isEmpty()) {
          return CompletableFuture.completedFuture(Collections.emptyList());
        }

        TextDocumentItem document = documents.get(params.getTextDocument().getUri());

        List<RuleMatch> matches = validateDocument(document);

        DocumentPositionCalculator positionCalculator = new DocumentPositionCalculator(document.getText());

        Stream<RuleMatch> relevant =
        matches.stream().filter(m -> locationOverlaps(m, positionCalculator, params.getRange()));

        List<TextEditCommand> commands = relevant.flatMap(m -> getEditCommands(m, document, positionCalculator)).collect(Collectors.toList());

        return CompletableFuture.completedFuture(commands);
      }

      @NotNull
      private Stream<TextEditCommand> getEditCommands(RuleMatch match, TextDocumentItem document, DocumentPositionCalculator positionCalculator) {
        Range range = createDiagnostic(match, positionCalculator).getRange();
        return match.getSuggestedReplacements().stream().map(str -> new TextEditCommand(str, range, document));
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

    DocumentPositionCalculator positionCalculator = new DocumentPositionCalculator(document.getText());

    return matches.stream().map(match -> createDiagnostic(match, positionCalculator)).collect(Collectors.toList());
  }

  private List<RuleMatch> validateDocument(TextDocumentItem document) {
    // This setting is specific to VS Code behavior and maintaining it here
    // long term is not desirable because other clients may behave differently.
    // See: https://github.com/Microsoft/vscode/issues/28732
    String uri = document.getUri();
    Boolean isSupportedScheme = uri.startsWith("file:") || uri.startsWith("untitled:") ;

    if (language == null || !isSupportedScheme) {
      return Collections.emptyList();
    } else {
      JLanguageTool languageTool = new JLanguageTool(language);

      String languageId = document.getLanguageId();
      try {
        switch (languageId) {
          case "plaintext": {
            return languageTool.check(document.getText());
          }
          case "markdown": {
            Parser p = Parser.builder().build();
            Document mdDocument = (Document) p.parse(document.getText());

            AnnotatedTextBuildingVisitor builder = new AnnotatedTextBuildingVisitor();
            builder.visit(mdDocument);

            return languageTool.check(builder.getText());
          }
          case "latex": {
            return languageTool.check(document.getText());
          }
          default: {
            throw new UnsupportedOperationException(String.format("Language, %s, is not supported.", languageId));
          }
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

        setLanguage(params.getSettings());
      }

      @SuppressWarnings({"unchecked", "rawtypes"})
      @Override
      public CompletableFuture<Object> executeCommand(ExecuteCommandParams params) {
        if (Objects.equals(params.getCommand(), TextEditCommand.CommandName)) {
          return ((CompletableFuture<Object>) (CompletableFuture) client.applyEdit(
          new ApplyWorkspaceEditParams(
          new WorkspaceEdit(
          (List<TextDocumentEdit>) (List) params.getArguments()))));
        }
        return CompletableFuture.completedFuture(false);
      }
    };
  }

  @SuppressWarnings("unchecked")
  private void setLanguage(@NotNull Object settingsObject) {
    Map<String, Object> settings = (Map<String, Object>) settingsObject;
    Map<String, Object> languageServerExample = (Map<String, Object>) settings.get("languageTool");
    String shortCode = ((String) languageServerExample.get("language"));

    setLanguage(shortCode);
  }

  private void setLanguage(String shortCode) {
    if (Languages.isLanguageSupported(shortCode)) {
      language = Languages.getLanguageForShortCode(shortCode);
    } else {
      System.out.println("ERROR: " + shortCode + " is not a recognized language.  Checking disabled.");
      language = null;
    }

    documents.values().forEach(this::publishIssues);
  }

  @Override
  public void connect(LanguageClient client) {
    this.client = client;
  }
}
