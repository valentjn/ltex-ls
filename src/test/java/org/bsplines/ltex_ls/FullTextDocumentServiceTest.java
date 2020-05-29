package org.bsplines.ltex_ls;

import java.util.Collections;
import java.util.HashMap;

import org.eclipse.lsp4j.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FullTextDocumentServiceTest {
  @Test
  public void doTest() {
    FullTextDocumentService service = new FullTextDocumentService(new HashMap<>());
    Assertions.assertDoesNotThrow(() -> new FullTextDocumentService());

    Assertions.assertDoesNotThrow(() -> service.codeAction(null));
    Assertions.assertDoesNotThrow(() -> service.codeLens(null));
    Assertions.assertDoesNotThrow(() -> service.completion(null));
    Assertions.assertDoesNotThrow(() -> service.definition(null));
    Assertions.assertDoesNotThrow(() -> service.documentHighlight(null));
    Assertions.assertDoesNotThrow(() -> service.documentSymbol(null));
    Assertions.assertDoesNotThrow(() -> service.formatting(null));
    Assertions.assertDoesNotThrow(() -> service.hover(null));
    Assertions.assertDoesNotThrow(() -> service.onTypeFormatting(null));
    Assertions.assertDoesNotThrow(() -> service.references(null));
    Assertions.assertDoesNotThrow(() -> service.rangeFormatting(null));
    Assertions.assertDoesNotThrow(() -> service.rename(null));
    Assertions.assertDoesNotThrow(() ->  service.resolveCodeLens(null));
    Assertions.assertDoesNotThrow(() -> service.resolveCompletionItem(null));
    Assertions.assertDoesNotThrow(() -> service.signatureHelp(null));

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
