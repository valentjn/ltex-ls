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
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import org.bsplines.ltexls.client.LtexLanguageClient;
import org.bsplines.ltexls.settings.SettingsManager;
import org.bsplines.ltexls.tools.Tools;
import org.checkerframework.checker.initialization.qual.NotOnlyInitialized;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.CodeActionOptions;
import org.eclipse.lsp4j.ExecuteCommandOptions;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.WindowClientCapabilities;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

public class LtexLanguageServer implements LanguageServer, LanguageClientAware {
  private @MonotonicNonNull LtexLanguageClient languageClient;
  private SettingsManager settingsManager;
  private DocumentChecker documentChecker;
  private CodeActionGenerator codeActionGenerator;
  private @NotOnlyInitialized LtexTextDocumentService ltexTextDocumentService;
  private @NotOnlyInitialized LtexWorkspaceService ltexWorkspaceService;
  private boolean clientSupportsWorkDoneProgress;
  private boolean clientSupportsWorkspaceSpecificConfiguration;
  private Instant startupInstant;

  /**
   * Constructor.
   * Note: The object cannot be used before @c connect() has been called.
   */
  public LtexLanguageServer() {
    this.settingsManager = new SettingsManager();
    this.documentChecker = new DocumentChecker(this.settingsManager);
    this.codeActionGenerator = new CodeActionGenerator(this.settingsManager);
    this.ltexTextDocumentService = new LtexTextDocumentService(this);
    this.ltexWorkspaceService = new LtexWorkspaceService(this);
    this.clientSupportsWorkDoneProgress = false;
    this.clientSupportsWorkspaceSpecificConfiguration = false;
    this.startupInstant = Instant.now();
  }

  @Override
  public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
    @Nullable Package ltexLsPackage = LtexLanguageServer.class.getPackage();
    @Nullable String ltexLsVersion = ((ltexLsPackage != null)
        ? ltexLsPackage.getImplementationVersion() : null);
    Tools.logger.info(Tools.i18n("initializingLtexLs",
        ((ltexLsVersion != null) ? ltexLsVersion : "null")));

    @Nullable ClientCapabilities clientCapabilities = params.getCapabilities();

    if (clientCapabilities != null) {
      @Nullable WindowClientCapabilities windowClientCapabilities = clientCapabilities.getWindow();

      if ((windowClientCapabilities != null) && (windowClientCapabilities.getWorkDoneProgress())) {
        this.clientSupportsWorkDoneProgress = true;
      }
    }

    Tools.logger.warning("clientSupportsWorkDoneProgress = " + this.clientSupportsWorkDoneProgress);
    @Nullable JsonObject initializationOptions = (JsonObject)params.getInitializationOptions();

    if (initializationOptions != null) {
      // Until it is specified in the LSP that the locale is automatically sent with
      // the initialization request, we have to do that manually.
      // See https://github.com/microsoft/language-server-protocol/issues/754.
      if (initializationOptions.has("locale")) {
        String localeLanguage = initializationOptions.get("locale").getAsString();
        Locale locale = Locale.forLanguageTag(localeLanguage);
        Tools.logger.info(Tools.i18n("settingLocale", locale.getLanguage()));
        Tools.setLocale(locale);
      }

      if (initializationOptions.has("customCapabilities")) {
        JsonObject customCapabilities = initializationOptions.getAsJsonObject("customCapabilities");

        if (customCapabilities.has("workspaceSpecificConfiguration")) {
          this.clientSupportsWorkspaceSpecificConfiguration =
              customCapabilities.get("workspaceSpecificConfiguration").getAsBoolean();
        }
      }
    }

    ServerCapabilities serverCapabilities = new ServerCapabilities();
    serverCapabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
    serverCapabilities.setCodeActionProvider(
        new CodeActionOptions(CodeActionGenerator.getCodeActions()));

    List<String> commandNames = new ArrayList<>();
    commandNames.addAll(LtexWorkspaceService.getCommandNames());
    serverCapabilities.setExecuteCommandProvider(new ExecuteCommandOptions(commandNames));

    return CompletableFuture.completedFuture(new InitializeResult(serverCapabilities));
  }

  @Override
  public CompletableFuture<Object> shutdown() {
    Tools.logger.info(Tools.i18n("shuttingDownLtexLs"));

    // Per https://github.com/eclipse/lsp4j/issues/18
    return CompletableFuture.completedFuture(new Object());
  }

  @Override
  public void exit() {
    Tools.logger.info(Tools.i18n("exitingLtexLs"));
    System.exit(0);
  }

  @Override
  public void connect(LanguageClient languageClient) {
    this.languageClient = (LtexLanguageClient)languageClient;
  }

  @JsonRequest("ltex/serverStatus")
  CompletableFuture<LtexServerStatusResult> ltexServerStatus() {
    long processId = ProcessHandle.current().pid();
    double wallClockDuration =
        Duration.between(this.startupInstant, Instant.now()).toMillis() / 1000.0;
    @Nullable Double cpuDuration = null;
    @Nullable Double cpuUsage = null;
    double totalMemory = Runtime.getRuntime().totalMemory();
    double usedMemory = totalMemory - Runtime.getRuntime().freeMemory();

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

    return CompletableFuture.completedFuture(new LtexServerStatusResult(
        processId, wallClockDuration, cpuUsage, cpuDuration, usedMemory, totalMemory));
  }

  @Override
  public TextDocumentService getTextDocumentService() {
    return this.ltexTextDocumentService;
  }

  @Override
  public WorkspaceService getWorkspaceService() {
    return this.ltexWorkspaceService;
  }

  public @Nullable LtexLanguageClient getLanguageClient() {
    return this.languageClient;
  }

  public SettingsManager getSettingsManager() {
    return this.settingsManager;
  }

  public DocumentChecker getDocumentChecker() {
    return this.documentChecker;
  }

  public CodeActionGenerator getCodeActionGenerator() {
    return this.codeActionGenerator;
  }

  public LtexTextDocumentService getLtexTextDocumentService() {
    return this.ltexTextDocumentService;
  }

  public boolean isClientSupportingWorkDoneProgress() {
    return this.clientSupportsWorkDoneProgress;
  }

  public boolean isClientSupportingWorkspaceSpecificConfiguration() {
    return this.clientSupportsWorkspaceSpecificConfiguration;
  }
}
