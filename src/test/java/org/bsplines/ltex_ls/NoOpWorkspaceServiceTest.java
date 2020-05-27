package org.bsplines.ltex_ls;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NoOpWorkspaceServiceTest {
  @Test
  public void test() {
    NoOpWorkspaceService service = new NoOpWorkspaceService();
    Assertions.assertDoesNotThrow(() -> service.symbol(null));
    Assertions.assertDoesNotThrow(() -> service.didChangeConfiguration(null));
    Assertions.assertDoesNotThrow(() -> service.didChangeWatchedFiles(null));
  }
}
