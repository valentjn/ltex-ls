/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
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
  private static void assertCheckDocumentResult(String uri, boolean expected)
        throws InterruptedException, ExecutionException {
    LtexLanguageServer server = new LtexLanguageServer();
    final LtexWorkspaceService service = new LtexWorkspaceService(server);

    JsonObject argument = new JsonObject();
    argument.addProperty("uri", uri);

    final JsonObject range = new JsonObject();
    JsonObject rangeStart = new JsonObject();
    rangeStart.addProperty("line", 0);
    rangeStart.addProperty("character", 1);
    JsonObject rangeEnd = new JsonObject();
    rangeEnd.addProperty("line", 2);
    rangeEnd.addProperty("character", 3);
    range.add("start", rangeStart);
    range.add("end", rangeEnd);
    argument.add("range", range);

    ExecuteCommandParams params = new ExecuteCommandParams("_ltex.checkDocument",
        Collections.singletonList(argument));

    JsonObject result = ((JsonElement)service.executeCommand(params).get()).getAsJsonObject();

    Assertions.assertEquals(false, result.get("success").getAsBoolean());
    Assertions.assertEquals(!expected, result.has("errorMessage"));
  }

  @Test
  public void testMiscellaneous() throws InterruptedException, ExecutionException {
    LtexLanguageServer server = new LtexLanguageServer();
    LtexWorkspaceService service = new LtexWorkspaceService(server);
    Assertions.assertDoesNotThrow(() -> service.symbol(new WorkspaceSymbolParams()));

    JsonObject settings = new JsonObject();
    settings.add("ltex", new JsonObject());
    Assertions.assertDoesNotThrow(() -> service.didChangeConfiguration(
        new DidChangeConfigurationParams(settings)));

    Assertions.assertDoesNotThrow(() -> service.didChangeWatchedFiles(
        new DidChangeWatchedFilesParams()));

    ExecuteCommandParams params = new ExecuteCommandParams("_ltex.foobar", Collections.emptyList());
    JsonObject result = ((JsonElement)service.executeCommand(params).get()).getAsJsonObject();
    Assertions.assertFalse(result.get("success").getAsBoolean());
  }

  @Test
  public void testCheckDocument() throws IOException, InterruptedException, ExecutionException,
        URISyntaxException {
    assertCheckDocumentResult("invalid_uri", false);
    assertCheckDocumentResult("file:///non_existent_path", false);

    for (String extension : Arrays.asList(".bib", ".md", ".rst", ".tex")) {
      File tmpFile = File.createTempFile("ltex-", extension);

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

  @Test
  public void testGetServerStatus() throws InterruptedException, ExecutionException {
    LtexLanguageServer server = new LtexLanguageServer();
    LtexWorkspaceService service = new LtexWorkspaceService(server);
    ExecuteCommandParams params = new ExecuteCommandParams("_ltex.getServerStatus",
        Collections.emptyList());
    JsonObject result = ((JsonElement)service.executeCommand(params).get()).getAsJsonObject();

    Assertions.assertTrue(result.get("success").getAsBoolean());
    Assertions.assertTrue(result.get("processId").getAsLong() >= 0);
    Assertions.assertTrue(result.get("wallClockDuration").getAsDouble() >= 0);

    if (result.has("cpuUsage")) {
      Assertions.assertTrue(result.get("cpuUsage").getAsDouble() >= 0);
    }

    if (result.has("cpuDuration")) {
      Assertions.assertTrue(result.get("cpuDuration").getAsDouble() >= 0);
    }

    Assertions.assertTrue(result.get("usedMemory").getAsDouble() >= 0);
    Assertions.assertTrue(result.get("totalMemory").getAsDouble() >= 0);
  }
}
