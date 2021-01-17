/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import org.bsplines.ltexls.tools.Tools;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LtexWorkspaceServiceTest {
  @Test
  public void test() {
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

  private static void assertCheckDocumentResult(String uri, boolean expected)
        throws InterruptedException, ExecutionException {
    LtexLanguageServer server = new LtexLanguageServer();
    LtexWorkspaceService service = new LtexWorkspaceService(server);
    ExecuteCommandParams params = new ExecuteCommandParams("ltex.checkDocument",
        Collections.singletonList(JsonParser.parseString("{\"uri\": \"" + uri + "\"}")));
    JsonObject result = ((JsonElement)service.executeCommand(params).get()).getAsJsonObject();
    Assertions.assertEquals(false, result.get("success").getAsBoolean());
    Assertions.assertEquals(!expected, result.has("errorMessage"));
  }

  @Test
  public void testExecuteCommand() throws IOException, InterruptedException, ExecutionException,
        URISyntaxException {
    assertCheckDocumentResult("invalid_uri", false);
    assertCheckDocumentResult("file:///non_existent_path", false);
    File tmpFile = File.createTempFile("ltex-", ".tex");

    try {
      assertCheckDocumentResult(tmpFile.toURI().toString(), true);
    } finally {
      if (!tmpFile.delete()) {
        Tools.logger.warning(Tools.i18n(
            "couldNotDeleteTemporaryFile", tmpFile.toPath().toString()));
      }
    }
  }
}
