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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
                public TextEditCommand(String title, Range range) {
                    this.setCommand("cSpell.editText");
                    this.setArguments(Collections.singletonList(new TextEdit(range, title)));
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

                System.out.println("matches = " + matches + " count = " + matches.size());

                List<RuleMatch> relevant =
                        matches.stream().filter(m -> locationOverlaps(m, params.getRange())).collect(Collectors.toList());

                System.out.println("relevant = " + relevant + " count = " + relevant.size());

                List<TextEditCommand> commands = relevant.stream().flatMap(this::getEditCommands).collect(Collectors.toList());

                System.out.println("commands = " + commands + " count = " + commands.size());

                return CompletableFuture.completedFuture(commands);
            }

            @NotNull
            private Stream<TextEditCommand> getEditCommands(RuleMatch match) {
                Range range = matchToDiagnostic(match).getRange();
                return match.getSuggestedReplacements().stream().map(str -> new TextEditCommand(str, range));
            }

            private boolean locationOverlaps(RuleMatch match, Range range) {
                return overlaps(range, matchToDiagnostic(match).getRange());
            }

            private boolean overlaps(Range r1, Range r2) {
                System.out.println("ExampleLanguageServer.overlaps");
                System.out.println("r1 = " + r1);
                System.out.println("r2 = " + r2);
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

                List<Diagnostic> diagnostics = matches.stream().map(this::matchToDiagnostic).collect(Collectors.toList());

                client.publishDiagnostics(new PublishDiagnosticsParams(document.getUri(), diagnostics));
            }

            private Diagnostic matchToDiagnostic(RuleMatch match) {
                Diagnostic ret = new Diagnostic();
                ret.setRange(
                        new Range(
                                new Position(match.getLine(), match.getColumn()),
                                new Position(match.getEndLine(), match.getEndColumn())));
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