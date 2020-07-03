/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls;

import com.google.gson.JsonObject;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LtexWorkspaceServiceTest {
  @Test
  public void doTest() {
    LtexLanguageServer server = new LtexLanguageServer();
    LtexWorkspaceService service = new LtexWorkspaceService(server);
    Assertions.assertDoesNotThrow(() -> service.symbol(new WorkspaceSymbolParams()));
    JsonObject settings = new JsonObject();
    settings.add("ltex", new JsonObject());
    Assertions.assertDoesNotThrow(() -> service.didChangeConfiguration(
        new DidChangeConfigurationParams(settings)));
    Assertions.assertDoesNotThrow(() -> service.didChangeWatchedFiles(
        new DidChangeWatchedFilesParams()));
  }
}
