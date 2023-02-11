/* Copyright (C) 2019-2023 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.server

import org.eclipse.lsp4j.CompletionParams
import org.eclipse.lsp4j.DidChangeTextDocumentParams
import org.eclipse.lsp4j.DidCloseTextDocumentParams
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.DidSaveTextDocumentParams
import org.eclipse.lsp4j.TextDocumentContentChangeEvent
import org.eclipse.lsp4j.TextDocumentItem
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier
import kotlin.test.Test

class LtexTextDocumentServiceTest {
  @Test
  fun test() {
    val server = LtexLanguageServer()
    val service = LtexTextDocumentService(server)
    service.completion(CompletionParams())

    val document = TextDocumentItem("untitled:test.md", "markdown", 1, "")
    val versionedDocument = VersionedTextDocumentIdentifier(document.uri, 2)

    service.didOpen(DidOpenTextDocumentParams(document))
    service.didChange(
      DidChangeTextDocumentParams(versionedDocument, listOf(TextDocumentContentChangeEvent("abc"))),
    )
    service.didSave(DidSaveTextDocumentParams(versionedDocument))
    service.didClose(DidCloseTextDocumentParams(versionedDocument))
  }
}
