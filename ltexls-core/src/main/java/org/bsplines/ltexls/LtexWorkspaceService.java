/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls;

import com.google.gson.JsonObject;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.checkerframework.checker.initialization.qual.NotOnlyInitialized;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.WorkspaceService;

class LtexWorkspaceService implements WorkspaceService {
  @NotOnlyInitialized LtexLanguageServer ltexLanguageServer;

  public LtexWorkspaceService(@UnknownInitialization LtexLanguageServer ltexLanguageServer) {
    this.ltexLanguageServer = ltexLanguageServer;
  }

  @Override
  public CompletableFuture<List<? extends SymbolInformation>> symbol(WorkspaceSymbolParams params) {
    return CompletableFuture.completedFuture(Collections.emptyList());
  }

  @Override
  public void didChangeConfiguration(DidChangeConfigurationParams params) {
    this.ltexLanguageServer.getSettingsManager().setSettings(
        ((JsonObject)params.getSettings()).get("ltex"));
    this.ltexLanguageServer.getLtexTextDocumentService().executeFunction(
        this.ltexLanguageServer::publishDiagnostics);
  }

  @Override
  public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
  }

  @Override
  public CompletableFuture<Object> executeCommand(ExecuteCommandParams params) {
    @Nullable LanguageClient languageClient = this.ltexLanguageServer.getLanguageClient();

    if (CodeActionGenerator.getCommandNames().contains(params.getCommand())
          && (languageClient != null)) {
      languageClient.telemetryEvent(params.getArguments().get(0));
      return CompletableFuture.completedFuture(true);
    } else {
      return CompletableFuture.completedFuture(false);
    }
  }
}
