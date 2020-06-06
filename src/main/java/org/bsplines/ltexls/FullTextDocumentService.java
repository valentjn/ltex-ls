package org.bsplines.ltexls;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

/**
* `TextDocumentService` that only supports `TextDocumentSyncKind.Full` updates.
* Override members to add functionality.
*/
class FullTextDocumentService implements TextDocumentService {
  HashMap<String, TextDocumentItem> documents;

  public FullTextDocumentService(HashMap<String, TextDocumentItem> documents) {
    this.documents = documents;
  }

  public FullTextDocumentService() {
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
  public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(CodeActionParams params) {
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
    documents.put(params.getTextDocument().getUri(), params.getTextDocument());
  }

  @Override
  public void didChange(DidChangeTextDocumentParams params) {
    String uri = params.getTextDocument().getUri();
    TextDocumentItem document = documents.get(uri);

    if (document == null) {
      Tools.logger.warning(Tools.i18n("couldNotFindDocumentWithUri", uri));
      return;
    }

    for (TextDocumentContentChangeEvent changeEvent : params.getContentChanges()) {
      // Will be full update because we specified that is all we support
      if (changeEvent.getRange() != null) {
        throw new UnsupportedOperationException(Tools.i18n("rangeShouldBeNull"));
      }

      document.setText(changeEvent.getText());
      document.setVersion(params.getTextDocument().getVersion());
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
