package org.bsplines.ltex_ls;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

import org.jetbrains.annotations.NotNull;

/**
* `TextDocumentService` that only supports `TextDocumentSyncKind.Full` updates.
* Override members to add functionality.
*/
class FullTextDocumentService implements TextDocumentService {

  @NotNull
  HashMap<String, TextDocumentItem> documents;

  public FullTextDocumentService(HashMap<String, TextDocumentItem> documents) {
    this.documents = documents;
  }

  public FullTextDocumentService() {
    this.documents = new HashMap<>();
  }

  @Override
  public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams position) {
    return null;
  }

  @Override
  public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem unresolved) {
    return null;
  }

  @Override
  public CompletableFuture<Hover> hover(HoverParams position) {
    return null;
  }

  @Override
  public CompletableFuture<SignatureHelp> signatureHelp(SignatureHelpParams position) {
    return null;
  }

  @Override
  public CompletableFuture<Either<List<? extends Location>,List<? extends LocationLink>>> definition(DefinitionParams position) {
    return null;
  }

  @Override
  public CompletableFuture<List<? extends Location>> references(ReferenceParams params) {
    return null;
  }

  @Override
  public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(DocumentHighlightParams position) {
    return null;
  }

  @Override
  public CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> documentSymbol(DocumentSymbolParams params) {
    return null;
  }

  @Override
  public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(CodeActionParams params) {
    return null;
  }

  @Override
  public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
    return null;
  }

  @Override
  public CompletableFuture<CodeLens> resolveCodeLens(CodeLens unresolved) {
    return null;
  }

  @Override
  public CompletableFuture<List<? extends TextEdit>> formatting(DocumentFormattingParams params) {
    return null;
  }

  @Override
  public CompletableFuture<List<? extends TextEdit>> rangeFormatting(DocumentRangeFormattingParams params) {
    return null;
  }

  @Override
  public CompletableFuture<List<? extends TextEdit>> onTypeFormatting(DocumentOnTypeFormattingParams params) {
    return null;
  }

  @Override
  public CompletableFuture<WorkspaceEdit> rename(RenameParams params) {
    return null;
  }

  @Override
  public void didOpen(DidOpenTextDocumentParams params) {
    documents.put(params.getTextDocument().getUri(), params.getTextDocument());
  }

  @Override
  public void didChange(DidChangeTextDocumentParams params) {
    String uri = params.getTextDocument().getUri();
    for (TextDocumentContentChangeEvent changeEvent : params.getContentChanges()) {
      // Will be full update because we specified that is all we support
      if (changeEvent.getRange() != null) {
        throw new UnsupportedOperationException(Tools.i18n("rangeShouldBeNull"));
      }

      documents.get(uri).setText(changeEvent.getText());
      documents.get(uri).setVersion(params.getTextDocument().getVersion());
    }
  }

  @Override
  public void didClose(DidCloseTextDocumentParams params) {
    String uri = params.getTextDocument().getUri();
    documents.remove(uri);
  }

  @Override
  public void didSave(DidSaveTextDocumentParams params) {
  }
}
