package org.bsplines.ltexls;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.services.WorkspaceService;

class NoOpWorkspaceService implements WorkspaceService {
  @Override
  public CompletableFuture<List<? extends SymbolInformation>> symbol(WorkspaceSymbolParams params) {
    return CompletableFuture.completedFuture(Collections.emptyList());
  }

  @Override
  public void didChangeConfiguration(DidChangeConfigurationParams params) {
  }

  @Override
  public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
  }
}
