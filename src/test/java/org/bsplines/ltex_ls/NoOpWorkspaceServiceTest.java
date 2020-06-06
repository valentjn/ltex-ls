package org.bsplines.ltex_ls;

import org.eclipse.lsp4j.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NoOpWorkspaceServiceTest {
  @Test
  public void doTest() {
    NoOpWorkspaceService service = new NoOpWorkspaceService();
    Assertions.assertDoesNotThrow(() -> service.symbol(new WorkspaceSymbolParams()));
    Assertions.assertDoesNotThrow(() -> service.didChangeConfiguration(
        new DidChangeConfigurationParams()));
    Assertions.assertDoesNotThrow(() -> service.didChangeWatchedFiles(
        new DidChangeWatchedFilesParams()));
  }
}
