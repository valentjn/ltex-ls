/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.server

import org.bsplines.ltexls.languagetool.LanguageToolRuleMatch
import org.bsplines.ltexls.parsing.AnnotatedTextFragment
import org.bsplines.ltexls.settings.Settings
import org.bsplines.ltexls.tools.I18n
import org.bsplines.ltexls.tools.Logging
import org.bsplines.ltexls.tools.Tools
import org.eclipse.lsp4j.CodeAction
import org.eclipse.lsp4j.CodeActionParams
import org.eclipse.lsp4j.CodeLens
import org.eclipse.lsp4j.CodeLensParams
import org.eclipse.lsp4j.Command
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionList
import org.eclipse.lsp4j.CompletionParams
import org.eclipse.lsp4j.DefinitionParams
import org.eclipse.lsp4j.DidChangeTextDocumentParams
import org.eclipse.lsp4j.DidCloseTextDocumentParams
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.DidSaveTextDocumentParams
import org.eclipse.lsp4j.DocumentFormattingParams
import org.eclipse.lsp4j.DocumentHighlight
import org.eclipse.lsp4j.DocumentHighlightParams
import org.eclipse.lsp4j.DocumentOnTypeFormattingParams
import org.eclipse.lsp4j.DocumentRangeFormattingParams
import org.eclipse.lsp4j.DocumentSymbol
import org.eclipse.lsp4j.DocumentSymbolParams
import org.eclipse.lsp4j.Hover
import org.eclipse.lsp4j.HoverParams
import org.eclipse.lsp4j.Location
import org.eclipse.lsp4j.LocationLink
import org.eclipse.lsp4j.PublishDiagnosticsParams
import org.eclipse.lsp4j.ReferenceParams
import org.eclipse.lsp4j.RenameParams
import org.eclipse.lsp4j.SignatureHelp
import org.eclipse.lsp4j.SignatureHelpParams
import org.eclipse.lsp4j.SymbolInformation
import org.eclipse.lsp4j.TextEdit
import org.eclipse.lsp4j.WorkspaceEdit
import org.eclipse.lsp4j.jsonrpc.CancelChecker
import org.eclipse.lsp4j.jsonrpc.CompletableFutures
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.TextDocumentService
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

@Suppress("TooManyFunctions")
class LtexTextDocumentService(
  val languageServer: LtexLanguageServer,
) : TextDocumentService {
  val documents: MutableMap<String, LtexTextDocumentItem> = HashMap()

  override fun completion(params: CompletionParams):
        CompletableFuture<Either<List<CompletionItem>, CompletionList>> {
    return CompletableFuture.completedFuture(Either.forLeft(emptyList()))
  }

  override fun resolveCompletionItem(completionItem: CompletionItem):
        CompletableFuture<CompletionItem> {
    return CompletableFuture.completedFuture(CompletionItem())
  }

  override fun hover(params: HoverParams): CompletableFuture<Hover> {
    return CompletableFuture.completedFuture(Hover())
  }

  override fun signatureHelp(params: SignatureHelpParams): CompletableFuture<SignatureHelp> {
    return CompletableFuture.completedFuture(SignatureHelp())
  }

  override fun definition(params: DefinitionParams):
        CompletableFuture<Either<List<Location>, List<LocationLink>>> {
    return CompletableFuture.completedFuture(Either.forLeft(emptyList()))
  }

  override fun references(params: ReferenceParams): CompletableFuture<List<Location>> {
    return CompletableFuture.completedFuture(emptyList())
  }

  override fun documentHighlight(params: DocumentHighlightParams):
        CompletableFuture<List<DocumentHighlight>> {
    return CompletableFuture.completedFuture(emptyList())
  }

  override fun documentSymbol(params: DocumentSymbolParams):
        CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> {
    return CompletableFuture.completedFuture(emptyList())
  }

  override fun codeLens(params: CodeLensParams): CompletableFuture<List<CodeLens>> {
    return CompletableFuture.completedFuture(emptyList())
  }

  override fun resolveCodeLens(codeLens: CodeLens): CompletableFuture<CodeLens> {
    return CompletableFuture.completedFuture(CodeLens())
  }

  override fun formatting(params: DocumentFormattingParams): CompletableFuture<List<TextEdit>> {
    return CompletableFuture.completedFuture(emptyList())
  }

  override fun rangeFormatting(params: DocumentRangeFormattingParams):
        CompletableFuture<List<TextEdit>> {
    return CompletableFuture.completedFuture(emptyList())
  }

  override fun onTypeFormatting(params: DocumentOnTypeFormattingParams):
        CompletableFuture<List<TextEdit>> {
    return CompletableFuture.completedFuture(emptyList())
  }

  override fun rename(params: RenameParams): CompletableFuture<WorkspaceEdit> {
    return CompletableFuture.completedFuture(WorkspaceEdit())
  }

  override fun didOpen(params: DidOpenTextDocumentParams) {
    val uri: String = params.textDocument.uri
    this.documents[uri] = LtexTextDocumentItem(this.languageServer, params.textDocument)
    val document: LtexTextDocumentItem = getDocument(uri) ?: return

    if (
      this.languageServer.settingsManager.settings.checkFrequency != Settings.CheckFrequency.Manual
    ) {
      this.languageServer.singleThreadExecutorService.execute {
        var exception: Exception? = null

        try {
          document.checkAndPublishDiagnosticsWithoutCache()
          document.raiseExceptionIfCanceled()
        } catch (e: ExecutionException) {
          exception = e
        } catch (e: InterruptedException) {
          exception = e
        }

        if (exception != null) {
          Tools.rethrowCancellationException(exception)
          Logging.logger.warning(I18n.format(exception))
        }
      }
    }
  }

  override fun didClose(params: DidCloseTextDocumentParams) {
    val uri: String = params.textDocument.uri
    if (getDocument(uri) == null) return

    this.documents.remove(uri)

    if (this.languageServer.settingsManager.settings.clearDiagnosticsWhenClosingFile) {
      languageServer.languageClient?.publishDiagnostics(PublishDiagnosticsParams(uri, emptyList()))
    }
  }

  override fun didSave(params: DidSaveTextDocumentParams) {
    if (
      this.languageServer.settingsManager.settings.checkFrequency != Settings.CheckFrequency.Save
    ) {
      return
    }

    val uri: String = params.textDocument.uri
    val document: LtexTextDocumentItem = getDocument(uri) ?: return
    if (document.beingChecked) document.cancelCheck()

    this.languageServer.singleThreadExecutorService.execute {
      var exception: Exception? = null

      try {
        document.checkAndPublishDiagnosticsWithoutCache()
        document.raiseExceptionIfCanceled()
      } catch (e: ExecutionException) {
        exception = e
      } catch (e: InterruptedException) {
        exception = e
      }

      if (exception != null) {
        Tools.rethrowCancellationException(exception)
        Logging.logger.warning(I18n.format(exception))
      }
    }
  }

  override fun didChange(params: DidChangeTextDocumentParams) {
    val uri: String = params.textDocument.uri
    val document: LtexTextDocumentItem = getDocument(uri) ?: return
    if (document.beingChecked) document.cancelCheck()

    this.languageServer.singleThreadExecutorService.execute {
      document.applyTextChangeEvents(params.contentChanges)
      document.version = params.textDocument.version

      if (
        this.languageServer.settingsManager.settings.checkFrequency == Settings.CheckFrequency.Edit
      ) {
        var exception: Exception? = null

        try {
          document.checkAndPublishDiagnosticsWithoutCache()
          document.raiseExceptionIfCanceled()
        } catch (e: ExecutionException) {
          exception = e
        } catch (e: InterruptedException) {
          exception = e
        }

        if (exception != null) {
          Tools.rethrowCancellationException(exception)
          Logging.logger.warning(I18n.format(exception))
        }
      }
    }
  }

  override fun codeAction(params: CodeActionParams):
        CompletableFuture<List<Either<Command, CodeAction>>> {
    if (params.context.diagnostics.isEmpty()) {
      return CompletableFuture.completedFuture(emptyList())
    }

    val uri: String = params.textDocument.uri
    val document: LtexTextDocumentItem = getDocument(uri) ?: run {
      Logging.logger.warning(I18n.format("couldNotFindDocumentWithUri", uri))
      return CompletableFuture.completedFuture(emptyList())
    }

    return CompletableFutures.computeAsync(this.languageServer.singleThreadExecutorService) {
      lspCancelChecker: CancelChecker ->
      document.lspCancelChecker = lspCancelChecker

      try {
        val checkingResult: Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>> =
            document.checkWithCache()
        val codeActions: List<Either<Command, CodeAction>> =
            this.languageServer.codeActionGenerator.generate(
              params, document, checkingResult)
        document.raiseExceptionIfCanceled()
        codeActions
      } catch (e: ExecutionException) {
        Tools.rethrowCancellationException(e)
        Logging.logger.warning(I18n.format(e))
        emptyList()
      } catch (e: InterruptedException) {
        Tools.rethrowCancellationException(e)
        Logging.logger.warning(I18n.format(e))
        emptyList()
      }
    }
  }

  private fun getDocument(uri: String): LtexTextDocumentItem? {
    return this.documents[uri] ?: run {
      Logging.logger.warning(I18n.format("couldNotFindDocumentWithUri", uri))
      null
    }
  }

  fun executeFunctionForEachDocument(function: (LtexTextDocumentItem) -> Unit) {
    this.documents.values.forEach(function)
  }
}
