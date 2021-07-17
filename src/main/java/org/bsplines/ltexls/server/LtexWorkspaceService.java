/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.server;

import com.google.gson.JsonObject;
import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.bsplines.ltexls.tools.Tools;
import org.checkerframework.checker.initialization.qual.NotOnlyInitialized;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.CompletableFutures;
import org.eclipse.lsp4j.services.WorkspaceService;

class LtexWorkspaceService implements WorkspaceService {
  private static final String checkDocumentCommandName = "_ltex.checkDocument";
  private static final String getServerStatusCommandName = "_ltex.getServerStatus";

  private @NotOnlyInitialized LtexLanguageServer languageServer;

  public LtexWorkspaceService(@UnknownInitialization LtexLanguageServer languageServer) {
    this.languageServer = languageServer;
  }

  @Override
  public CompletableFuture<List<? extends SymbolInformation>> symbol(WorkspaceSymbolParams params) {
    return CompletableFuture.completedFuture(Collections.emptyList());
  }

  @Override
  public void didChangeConfiguration(DidChangeConfigurationParams params) {
    this.languageServer.getLtexTextDocumentService().executeFunction(
        (LtexTextDocumentItem document) -> {
          if (document.isBeingChecked()) document.cancelCheck();

          this.languageServer.getSingleThreadExecutorService().execute(() -> {
            try {
              document.checkAndPublishDiagnosticsWithoutCache();
              document.raiseExceptionIfCanceled();
            } catch (InterruptedException | ExecutionException e) {
              Tools.rethrowCancellationException(e);
              Tools.logger.warning(Tools.i18n(e));
            }
          });
        });
  }

  @Override
  public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
  }

  @Override
  public CompletableFuture<Object> executeCommand(ExecuteCommandParams params) {
    if (params.getCommand().equals(checkDocumentCommandName)) {
      return executeCheckDocumentCommand((JsonObject)params.getArguments().get(0));
    } else if (params.getCommand().equals(getServerStatusCommandName)) {
      return executeGetServerStatusCommand();
    } else {
      return failCommand(Tools.i18n("unknownCommand", params.getCommand()));
    }
  }

  public CompletableFuture<Object> executeCheckDocumentCommand(JsonObject arguments) {
    if (this.languageServer == null) return failCommand(Tools.i18n("languageServerNotInitialized"));

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

        if (fileNameStr.endsWith(".bib")) {
          codeLanguageId = "bibtex";
        } else if (fileNameStr.endsWith(".c")
              || fileNameStr.endsWith(".h")) {
          codeLanguageId = "c";
        } else if (fileNameStr.endsWith(".clj")) {
          codeLanguageId = "clojure";
        } else if (fileNameStr.endsWith(".coffee")) {
          codeLanguageId = "coffeescript";
        } else if (fileNameStr.endsWith(".cc")
              || fileNameStr.endsWith(".cpp")
              || fileNameStr.endsWith(".cxx")
              || fileNameStr.endsWith(".hh")
              || fileNameStr.endsWith(".hpp")
              || fileNameStr.endsWith(".inl")) {
          codeLanguageId = "cpp";
        } else if (fileNameStr.endsWith(".cs")) {
          codeLanguageId = "csharp";
        } else if (fileNameStr.endsWith(".dart")) {
          codeLanguageId = "dart";
        } else if (fileNameStr.endsWith(".ex")) {
          codeLanguageId = "elixir";
        } else if (fileNameStr.endsWith(".elm")) {
          codeLanguageId = "elm";
        } else if (fileNameStr.endsWith(".erl")) {
          codeLanguageId = "erlang";
        } else if (fileNameStr.endsWith(".f90")) {
          codeLanguageId = "fortran-modern";
        } else if (fileNameStr.endsWith(".fs")) {
          codeLanguageId = "fsharp";
        } else if (fileNameStr.endsWith(".go")) {
          codeLanguageId = "go";
        } else if (fileNameStr.endsWith(".groovy")) {
          codeLanguageId = "groovy";
        } else if (fileNameStr.endsWith(".hs")) {
          codeLanguageId = "haskell";
        } else if (fileNameStr.endsWith(".htm")
              || fileNameStr.endsWith(".html")
              || fileNameStr.endsWith(".xht")
              || fileNameStr.endsWith(".xhtml")) {
          codeLanguageId = "html";
        } else if (fileNameStr.endsWith(".java")) {
          codeLanguageId = "java";
        } else if (fileNameStr.endsWith(".js")) {
          codeLanguageId = "javascript";
        } else if (fileNameStr.endsWith(".jl")) {
          codeLanguageId = "julia";
        } else if (fileNameStr.endsWith(".kt")) {
          codeLanguageId = "kotlin";
        } else if (fileNameStr.endsWith(".tex")) {
          codeLanguageId = "latex";
        } else if (fileNameStr.endsWith(".lisp")) {
          codeLanguageId = "lisp";
        } else if (fileNameStr.endsWith(".lua")) {
          codeLanguageId = "lua";
        } else if (fileNameStr.endsWith(".md")) {
          codeLanguageId = "markdown";
        } else if (fileNameStr.endsWith(".m")) {
          codeLanguageId = "matlab";
        } else if (fileNameStr.endsWith(".org")) {
          codeLanguageId = "org";
        } else if (fileNameStr.endsWith(".pl")) {
          codeLanguageId = "perl";
        } else if (fileNameStr.endsWith(".php")) {
          codeLanguageId = "php";
        } else if (fileNameStr.endsWith(".ps1")) {
          codeLanguageId = "powershell";
        } else if (fileNameStr.endsWith(".pp")) {
          codeLanguageId = "puppet";
        } else if (fileNameStr.endsWith(".py")) {
          codeLanguageId = "python";
        } else if (fileNameStr.endsWith(".r")) {
          codeLanguageId = "r";
        } else if (fileNameStr.endsWith(".rst")) {
          codeLanguageId = "restructuredtext";
        } else if (fileNameStr.endsWith(".Rnw")
              || fileNameStr.endsWith(".rnw")) {
          codeLanguageId = "rsweave";
        } else if (fileNameStr.endsWith(".rb")) {
          codeLanguageId = "ruby";
        } else if (fileNameStr.endsWith(".rs")) {
          codeLanguageId = "rust";
        } else if (fileNameStr.endsWith(".scala")) {
          codeLanguageId = "scala";
        } else if (fileNameStr.endsWith(".sh")) {
          codeLanguageId = "shellscript";
        } else if (fileNameStr.endsWith(".sql")) {
          codeLanguageId = "sql";
        } else if (fileNameStr.endsWith(".swift")) {
          codeLanguageId = "swift";
        } else if (fileNameStr.endsWith(".ts")) {
          codeLanguageId = "typescript";
        } else if (fileNameStr.endsWith(".vb")) {
          codeLanguageId = "vb";
        } else if (fileNameStr.endsWith(".v")) {
          codeLanguageId = "verilog";
        }
      }
    }

    LtexTextDocumentItem document = new LtexTextDocumentItem(
        this.languageServer, uriStr, codeLanguageId, 1, text);

    @Nullable Range nonFinalRange = null;

    if (arguments.has("range")) {
      JsonObject jsonRange = arguments.getAsJsonObject("range");
      JsonObject jsonStart = jsonRange.getAsJsonObject("start");
      JsonObject jsonEnd = jsonRange.getAsJsonObject("end");

      nonFinalRange = new Range(
          new Position(jsonStart.get("line").getAsInt(), jsonStart.get("character").getAsInt()),
          new Position(jsonEnd.get("line").getAsInt(), jsonEnd.get("character").getAsInt()));
    }

    final @Nullable Range range = nonFinalRange;
    if (document.isBeingChecked()) document.cancelCheck();

    return CompletableFutures.computeAsync(this.languageServer.getSingleThreadExecutorService(),
        (CancelChecker lspCancelChecker) -> {
          document.setLspCancelChecker(lspCancelChecker);

          try {
            boolean success = document.checkAndPublishDiagnosticsWithoutCache(range);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("success", success);
            document.raiseExceptionIfCanceled();
            return jsonObject;
          } catch (InterruptedException | ExecutionException e) {
            Tools.rethrowCancellationException(e);
            Tools.logger.warning(Tools.i18n(e));
            return Collections.emptyList();
          }
        });
  }

  public CompletableFuture<Object> executeGetServerStatusCommand() {
    if (this.languageServer == null) return failCommand(Tools.i18n("languageServerNotInitialized"));

    final long processId = ProcessHandle.current().pid();
    final double wallClockDuration = Duration.between(
        this.languageServer.getStartupInstant(), Instant.now()).toMillis() / 1000.0;
    @Nullable Double cpuDuration = null;
    @Nullable Double cpuUsage = null;
    final double totalMemory = Runtime.getRuntime().totalMemory();
    final double usedMemory = totalMemory - Runtime.getRuntime().freeMemory();

    try {
      OperatingSystemMXBean operatingSystemMxBean =
          (OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
      cpuUsage = operatingSystemMxBean.getProcessCpuLoad();
      if (cpuUsage == -1) cpuUsage = null;
      long cpuDurationLong = operatingSystemMxBean.getProcessCpuTime();
      cpuDuration = ((cpuDurationLong != -1) ? (cpuDurationLong / 1e9) : null);
    } catch (ClassCastException e) {
      // do nothing
    }

    Future<Boolean> singleThreadTestFuture =
        this.languageServer.getSingleThreadExecutorService().submit(() -> true);
    boolean isChecking = true;

    try {
      if (singleThreadTestFuture.get(10, TimeUnit.MILLISECONDS)) isChecking = false;
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      // do nothing
    }

    @Nullable String documentUriBeingChecked = null;

    if (isChecking) {
      @Nullable LtexTextDocumentItem documentBeingChecked =
          this.languageServer.getDocumentChecker().getLastCheckedDocument();
      if (documentBeingChecked != null) documentUriBeingChecked = documentBeingChecked.getUri();
    }

    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("success", true);
    jsonObject.addProperty("processId", processId);
    jsonObject.addProperty("wallClockDuration", wallClockDuration);
    if (cpuUsage != null) jsonObject.addProperty("cpuUsage", cpuUsage);
    if (cpuDuration != null) jsonObject.addProperty("cpuDuration", cpuDuration);
    jsonObject.addProperty("usedMemory", usedMemory);
    jsonObject.addProperty("totalMemory", totalMemory);
    jsonObject.addProperty("isChecking", isChecking);

    if (documentUriBeingChecked != null) {
      jsonObject.addProperty("documentUriBeingChecked", documentUriBeingChecked);
    }

    return CompletableFuture.completedFuture(jsonObject);
  }

  private static CompletableFuture<Object> failCommand(String errorMessage) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("success", false);
    jsonObject.addProperty("errorMessage", errorMessage);
    return CompletableFuture.completedFuture(jsonObject);
  }

  public static List<String> getCommandNames() {
    return Arrays.asList(checkDocumentCommandName, getServerStatusCommandName);
  }
}
