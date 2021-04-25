/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.server;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import org.bsplines.ltexls.languagetool.LanguageToolRuleMatch;
import org.bsplines.ltexls.parsing.AnnotatedTextFragment;
import org.bsplines.ltexls.settings.CheckFrequency;
import org.bsplines.ltexls.tools.Tools;
import org.checkerframework.checker.initialization.qual.NotOnlyInitialized;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DefinitionParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightParams;
import org.eclipse.lsp4j.DocumentOnTypeFormattingParams;
import org.eclipse.lsp4j.DocumentRangeFormattingParams;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureHelpParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.CompletableFutures;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.xtext.xbase.lib.Pair;

public class LtexTextDocumentService implements TextDocumentService {
  private @NotOnlyInitialized LtexLanguageServer languageServer;
  private Map<String, LtexTextDocumentItem> documents;

  public LtexTextDocumentService(@UnknownInitialization LtexLanguageServer languageServer) {
    this.languageServer = languageServer;
    this.documents = new HashMap<>();
  }

  @Override
  public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(
        CompletionParams position) {
    return CompletableFuture.completedFuture(Either.forLeft(Collections.emptyList()));
  }

  @Override
  public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem unresolved) {
    return CompletableFuture.completedFuture(new CompletionItem());
  }

  @Override
  public CompletableFuture<Hover> hover(HoverParams position) {
    return CompletableFuture.completedFuture(new Hover());
  }

  @Override
  public CompletableFuture<SignatureHelp> signatureHelp(SignatureHelpParams position) {
    return CompletableFuture.completedFuture(new SignatureHelp());
  }

  @Override
  public CompletableFuture<Either<List<? extends Location>,List<? extends LocationLink>>>
        definition(DefinitionParams position) {
    return CompletableFuture.completedFuture(Either.forLeft(Collections.emptyList()));
  }

  @Override
  public CompletableFuture<List<? extends Location>> references(ReferenceParams params) {
    return CompletableFuture.completedFuture(Collections.emptyList());
  }

  @Override
  public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(
        DocumentHighlightParams position) {
    return CompletableFuture.completedFuture(Collections.emptyList());
  }

  @Override
  public CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> documentSymbol(
        DocumentSymbolParams params) {
    return CompletableFuture.completedFuture(Collections.emptyList());
  }

  @Override
  public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
    return CompletableFuture.completedFuture(Collections.emptyList());
  }

  @Override
  public CompletableFuture<CodeLens> resolveCodeLens(CodeLens unresolved) {
    return CompletableFuture.completedFuture(new CodeLens());
  }

  @Override
  public CompletableFuture<List<? extends TextEdit>> formatting(DocumentFormattingParams params) {
    return CompletableFuture.completedFuture(Collections.emptyList());
  }

  @Override
  public CompletableFuture<List<? extends TextEdit>> rangeFormatting(
        DocumentRangeFormattingParams params) {
    return CompletableFuture.completedFuture(Collections.emptyList());
  }

  @Override
  public CompletableFuture<List<? extends TextEdit>> onTypeFormatting(
        DocumentOnTypeFormattingParams params) {
    return CompletableFuture.completedFuture(Collections.emptyList());
  }

  @Override
  public CompletableFuture<WorkspaceEdit> rename(RenameParams params) {
    return CompletableFuture.completedFuture(new WorkspaceEdit());
  }

  @Override
  public void didOpen(DidOpenTextDocumentParams params) {
    String uri = params.getTextDocument().getUri();
    this.documents.put(uri,
        new LtexTextDocumentItem(this.languageServer, params.getTextDocument()));
    @Nullable LtexTextDocumentItem document = getDocument(uri);
    if (document == null) return;

    if ((this.languageServer.getSettingsManager().getSettings().getCheckFrequency()
            != CheckFrequency.MANUAL)) {
      this.languageServer.getSingleThreadExecutorService().execute(() -> {
        try {
          document.checkAndPublishDiagnosticsWithoutCache();
          document.raiseExceptionIfCanceled();
        } catch (InterruptedException | ExecutionException e) {
          Tools.rethrowCancellationException(e);
          Tools.logger.warning(Tools.i18n(e));
        }
      });
    }
  }

  @Override
  public void didClose(DidCloseTextDocumentParams params) {
    String uri = params.getTextDocument().getUri();
    @Nullable LtexTextDocumentItem document = getDocument(uri);
    if (document == null) return;

    this.documents.remove(uri);

    if (this.languageServer.getSettingsManager().getSettings()
          .getClearDiagnosticsWhenClosingFile()) {
      this.languageServer.getSingleThreadExecutorService().execute(() -> {
        @Nullable LanguageClient languageClient = this.languageServer.getLanguageClient();

        if (languageClient != null) {
          languageClient.publishDiagnostics(
              new PublishDiagnosticsParams(uri, Collections.emptyList()));
        }
      });
    }
  }

  @Override
  public void didSave(DidSaveTextDocumentParams params) {
    if (this.languageServer.getSettingsManager().getSettings().getCheckFrequency()
          != CheckFrequency.SAVE) {
      return;
    }

    String uri = params.getTextDocument().getUri();
    @Nullable LtexTextDocumentItem document = getDocument(uri);
    if (document == null) return;

    if (document.isBeingChecked()) document.cancelCheck();

    this.languageServer.getSingleThreadExecutorService().execute(() -> {
      try {
        document.checkAndPublishDiagnosticsWithoutCache();
        document.raiseExceptionIfCanceled();
      } catch (InterruptedException | ExecutionException e) {
        Tools.rethrowCancellationException(e);
        Tools.logger.warning(Tools.i18n(e));
      }
    });
  }

  @Override
  public void didChange(DidChangeTextDocumentParams params) {
    String uri = params.getTextDocument().getUri();
    @Nullable LtexTextDocumentItem document = getDocument(uri);
    if (document == null) return;

    if (document.isBeingChecked()) document.cancelCheck();

    this.languageServer.getSingleThreadExecutorService().execute(() -> {
      document.applyTextChangeEvents(params.getContentChanges());
      document.setVersion(params.getTextDocument().getVersion());

      if (this.languageServer.getSettingsManager().getSettings().getCheckFrequency()
            == CheckFrequency.EDIT) {
        try {
          document.checkAndPublishDiagnosticsWithoutCache();
          document.raiseExceptionIfCanceled();
        } catch (InterruptedException | ExecutionException e) {
          Tools.rethrowCancellationException(e);
          Tools.logger.warning(Tools.i18n(e));
        }
      }
    });
  }

  @Override
  public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(
        CodeActionParams params) {
    if (params.getContext().getDiagnostics().isEmpty()) {
      return CompletableFuture.completedFuture(Collections.emptyList());
    }

    String uri = params.getTextDocument().getUri();
    @Nullable LtexTextDocumentItem document = this.documents.get(uri);

    if (document == null) {
      Tools.logger.warning(Tools.i18n("couldNotFindDocumentWithUri", uri));
      return CompletableFuture.completedFuture(Collections.emptyList());
    }

    return CompletableFutures.computeAsync(this.languageServer.getSingleThreadExecutorService(),
        (CancelChecker lspCancelChecker) -> {
          document.setLspCancelChecker(lspCancelChecker);

          try {
            Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>> checkingResult =
                document.checkWithCache();
            List<Either<Command, CodeAction>> codeActions =
                this.languageServer.getCodeActionGenerator().generate(
                params, document, checkingResult);
            document.raiseExceptionIfCanceled();
            return codeActions;
          } catch (InterruptedException | ExecutionException e) {
            Tools.rethrowCancellationException(e);
            Tools.logger.warning(Tools.i18n(e));
            return Collections.emptyList();
          }
        });
  }

  private @Nullable LtexTextDocumentItem getDocument(String uri) {
    @Nullable LtexTextDocumentItem document = this.documents.get(uri);

    if (document != null) {
      return document;
    } else {
      Tools.logger.warning(Tools.i18n("couldNotFindDocumentWithUri", uri));
      return null;
    }
  }

  public void executeFunction(Consumer<? super LtexTextDocumentItem> function) {
    this.documents.values().forEach(function);
  }
}
