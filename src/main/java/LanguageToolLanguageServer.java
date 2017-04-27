import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.jetbrains.annotations.NotNull;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class LanguageToolLanguageServer implements LanguageServer, LanguageClientAware {

    private LanguageClient client = null;

    private final Language language = new AmericanEnglish();

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        System.out.println("LanguageToolLanguageServer.initialize");
        ServerCapabilities capabilities = new ServerCapabilities();
        capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
        capabilities.setCodeActionProvider(true);
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
        return new FullTextDocumentService() {

            @Override
            public CompletableFuture<List<? extends Command>> codeAction(CodeActionParams params) {
                System.out.println("LanguageToolLanguageServer.codeAction");
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

                List<RuleMatch> matches = validateDocument(document);

                DocumentPositionCalculator positionCalculator = new DocumentPositionCalculator(document.getText());

                List<Diagnostic> diagnostics = matches.stream().map(match -> createDiagnostic(match, positionCalculator)).collect(Collectors.toList());

                client.publishDiagnostics(new PublishDiagnosticsParams(document.getUri(), diagnostics));
            }
        };
    }

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

    private List<RuleMatch> validateDocument(TextDocumentItem document) {
        JLanguageTool languageTool = new JLanguageTool(language);
        List<RuleMatch> matches;
        try {
            matches = languageTool.check(document.getText());
        } catch (IOException e) {
            e.printStackTrace();
            matches = new ArrayList<>();
        }
        return matches;
    }


    @Override
    public WorkspaceService getWorkspaceService() {
        return new EmptyWorkspaceService();
    }

    @Override
    public void connect(LanguageClient client) {
        this.client = client;
    }
}