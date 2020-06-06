package org.bsplines.ltexls;

import java.util.Collections;
import java.util.HashMap;

import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DefinitionParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.DocumentHighlightParams;
import org.eclipse.lsp4j.DocumentOnTypeFormattingParams;
import org.eclipse.lsp4j.DocumentRangeFormattingParams;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.SignatureHelpParams;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FullTextDocumentServiceTest {
  @Test
  public void doTest() {
    FullTextDocumentService service = new FullTextDocumentService(new HashMap<>());
    Assertions.assertDoesNotThrow(() -> new FullTextDocumentService());

    Assertions.assertDoesNotThrow(() -> service.codeAction(new CodeActionParams()));
    Assertions.assertDoesNotThrow(() -> service.codeLens(new CodeLensParams()));
    Assertions.assertDoesNotThrow(() -> service.completion(new CompletionParams()));
    Assertions.assertDoesNotThrow(() -> service.definition(new DefinitionParams()));
    Assertions.assertDoesNotThrow(() -> service.documentHighlight(new DocumentHighlightParams()));
    Assertions.assertDoesNotThrow(() -> service.documentSymbol(new DocumentSymbolParams()));
    Assertions.assertDoesNotThrow(() -> service.formatting(new DocumentFormattingParams()));
    Assertions.assertDoesNotThrow(() -> service.hover(new HoverParams()));
    Assertions.assertDoesNotThrow(() -> service.onTypeFormatting(
        new DocumentOnTypeFormattingParams()));
    Assertions.assertDoesNotThrow(() -> service.references(new ReferenceParams()));
    Assertions.assertDoesNotThrow(() -> service.rangeFormatting(
        new DocumentRangeFormattingParams()));
    Assertions.assertDoesNotThrow(() -> service.rename(new RenameParams()));
    Assertions.assertDoesNotThrow(() -> service.resolveCodeLens(new CodeLens()));
    Assertions.assertDoesNotThrow(() -> service.resolveCompletionItem(new CompletionItem()));
    Assertions.assertDoesNotThrow(() -> service.signatureHelp(new SignatureHelpParams()));

    TextDocumentItem document = new TextDocumentItem("untitled:test.txt", "plaintext", 1, "");
    VersionedTextDocumentIdentifier versionedDocument =
        new VersionedTextDocumentIdentifier(document.getUri(), 2);

    Assertions.assertDoesNotThrow(() -> service.didOpen(new DidOpenTextDocumentParams(document)));

    service.didChange(new DidChangeTextDocumentParams(versionedDocument,
        Collections.singletonList(new TextDocumentContentChangeEvent("abc"))));
    Assertions.assertThrows(UnsupportedOperationException.class, () ->
        service.didChange(new DidChangeTextDocumentParams(versionedDocument,
          Collections.singletonList(new TextDocumentContentChangeEvent(new Range(), 0, "abc")))));

    Assertions.assertDoesNotThrow(() ->
        service.didSave(new DidSaveTextDocumentParams(versionedDocument)));
    Assertions.assertDoesNotThrow(() ->
        service.didClose(new DidCloseTextDocumentParams(versionedDocument)));
  }
}
