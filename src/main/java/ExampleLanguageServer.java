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

class ExampleLanguageServer implements LanguageServer, LanguageClientAware {

    private LanguageClient client = null;

    private final Language language = new AmericanEnglish();

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        System.out.println("ExampleLanguageServer.initialize");
        ServerCapabilities capabilities = new ServerCapabilities();
        capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
        capabilities.setCodeActionProvider(true);
        return CompletableFuture.completedFuture(new InitializeResult(capabilities));
    }

    @Override
    public CompletableFuture<Object> shutdown() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void exit() {
    }

    @Override
    public TextDocumentService getTextDocumentService() {
        return new FullTextDocumentService() {

            class TextEditCommand extends Command{
                public TextEditCommand(String title, Range range, TextDocumentItem document) {
                    this.setCommand("cSpell.editText");
                    this.setArguments(
                        Arrays.asList(
                            document.getUri(),
                            document.getVersion(),
                            Collections.singletonList(new TextEdit(range, title))));
                    this.setTitle(title);
                }
            }

            @Override
            public CompletableFuture<List<? extends Command>> codeAction(CodeActionParams params) {
                System.out.println("ExampleLanguageServer.codeAction");
                if (params.getContext().getDiagnostics().isEmpty()) {
                    return CompletableFuture.completedFuture(Collections.emptyList());
                }
                System.out.println("non-empty");
                TextDocumentItem document = documents.get(params.getTextDocument().getUri());

                JLanguageTool languageTool = new JLanguageTool(language);
                List<RuleMatch> matches;
                try {
                    matches = languageTool.check(document.getText());
                } catch (IOException e) {
                    e.printStackTrace();
                    matches = new ArrayList<>();
                }

                int[] lineStartPositions = PositionHelper.getLineStartPositions(document.getText());

                List<RuleMatch> relevant =
                        matches.stream().filter(m -> locationOverlaps(m, params.getRange(), lineStartPositions)).collect(Collectors.toList());

                List<TextEditCommand> commands = relevant.stream().flatMap(m -> getEditCommands(m, document, lineStartPositions)).collect(Collectors.toList());

                return CompletableFuture.completedFuture(commands);
            }

            @NotNull
            private Stream<TextEditCommand> getEditCommands(RuleMatch match, TextDocumentItem document, int[] lineStartPositions) {
                Range range = matchToDiagnostic(match, lineStartPositions).getRange();
                return match.getSuggestedReplacements().stream().map(str -> new TextEditCommand(str, range, document));
            }

            private boolean locationOverlaps(RuleMatch match, Range range, int[] lineStartPositions) {
                return overlaps(range, matchToDiagnostic(match, lineStartPositions).getRange());
            }

            private boolean overlaps(Range r1, Range r2) {
                return r1.getStart().getCharacter() <= r2.getEnd().getCharacter() &&
                        r1.getEnd().getCharacter() >= r2.getStart().getCharacter() &&
                        r1.getStart().getLine() >= r2.getEnd().getLine() &&
                        r1.getEnd().getLine() <= r2.getStart().getLine();
            }

            @Override
            public void didChange(DidChangeTextDocumentParams params) {
                System.out.println("ExampleLanguageServer.didChange");
                super.didChange(params);

                TextDocumentItem document = this.documents.get(params.getTextDocument().getUri());

                JLanguageTool languageTool = new JLanguageTool(language);
                List<RuleMatch> matches;
                try {
                    matches = languageTool.check(document.getText());
                } catch (IOException e) {
                    e.printStackTrace();
                    matches = new ArrayList<>();
                }

                int[] lineStartPositions = PositionHelper.getLineStartPositions(document.getText());

                List<Diagnostic> diagnostics = matches.stream().map(match -> matchToDiagnostic(match, lineStartPositions)).collect(Collectors.toList());

                client.publishDiagnostics(new PublishDiagnosticsParams(document.getUri(), diagnostics));
            }

            private Diagnostic matchToDiagnostic(RuleMatch match, int[] lineStartPositions) {
                Diagnostic ret = new Diagnostic();
                ret.setRange(
                        new Range(
                                PositionHelper.getPosition(match.getFromPos(), lineStartPositions),
                                PositionHelper.getPosition(match.getToPos(), lineStartPositions)));
                ret.setSeverity(DiagnosticSeverity.Warning);
                ret.setSource(String.format("LanguageTool: %s", match.getRule().getDescription()));
                ret.setMessage(match.getMessage());
                return ret;
            }
        };
    }


    @Override
    public WorkspaceService getWorkspaceService() {

        return new WorkspaceService() {
            @Override
            public CompletableFuture<List<? extends SymbolInformation>> symbol(WorkspaceSymbolParams params) {
                return null;
            }

            @Override
            public void didChangeConfiguration(DidChangeConfigurationParams params) {
            }

            @Override
            public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
            }
        };
    }

    @Override
    public void connect(LanguageClient client) {
        this.client = client;
    }
}