/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.server;

import com.google.gson.JsonObject;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.bsplines.ltexls.Tools;
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

  private static final String checkDocumentCommandName = "ltex.checkDocument";

  public LtexWorkspaceService(@UnknownInitialization LtexLanguageServer ltexLanguageServer) {
    this.ltexLanguageServer = ltexLanguageServer;
  }

  @Override
  public CompletableFuture<List<? extends SymbolInformation>> symbol(WorkspaceSymbolParams params) {
    return CompletableFuture.completedFuture(Collections.emptyList());
  }

  @Override
  public void didChangeConfiguration(DidChangeConfigurationParams params) {
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
    } else if (params.getCommand().equals(checkDocumentCommandName)) {
      return checkDocument((JsonObject)params.getArguments().get(0));
    } else {
      return CompletableFuture.completedFuture(false);
    }
  }

  public CompletableFuture<Object> checkDocument(JsonObject arguments) {
    if (this.ltexLanguageServer == null) {
      return failCommand(Tools.i18n("languageServerNotInitialized"));
    }

    String uriStr = arguments.get("uri").getAsString();
    @Nullable String codeLanguageId = (arguments.has("codeLanguageId")
        ? arguments.get("codeLanguageId").getAsString() : null);
    @Nullable String text = (arguments.has("text") ? arguments.get("text").getAsString() : null);

    if ((codeLanguageId == null) || (text == null)) {
      @Nullable Path path = null;

      try {
        path = Paths.get(new URI(uriStr));
      } catch (URISyntaxException | IllegalArgumentException e) {
        return failCommand(Tools.i18n("couldNotParseDocumentUri", e));
      }

      if (text == null) {
        text = Tools.readFile(path);
        if (text == null) return failCommand(Tools.i18n("couldNotReadFile", path.toString()));
      }

      if (codeLanguageId == null) {
        Path fileName = path.getFileName();
        String fileNameStr = ((fileName != null) ? fileName.toString() : "");
        codeLanguageId = "plaintext";

        if (fileNameStr.endsWith(".md")) {
          codeLanguageId = "markdown";
        } else if (fileNameStr.endsWith(".tex")) {
          codeLanguageId = "latex";
        }
      }
    }

    LtexTextDocumentItem document = new LtexTextDocumentItem(uriStr, codeLanguageId, 1, text);

    return this.ltexLanguageServer.publishDiagnostics(document).thenApply((Boolean success) -> {
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("success", success);
      return jsonObject;
    });
  }

  private static CompletableFuture<Object> failCommand(String errorMessage) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("success", false);
    jsonObject.addProperty("errorMessage", errorMessage);
    return CompletableFuture.completedFuture(jsonObject);
  }

  public static List<String> getCommandNames() {
    return Collections.singletonList(checkDocumentCommandName);
  }
}
