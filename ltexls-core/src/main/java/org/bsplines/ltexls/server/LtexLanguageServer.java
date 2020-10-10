/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.server;

import com.google.gson.JsonObject;
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
import org.eclipse.lsp4j.CodeActionOptions;
import org.eclipse.lsp4j.ExecuteCommandOptions;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
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
  }

  @Override
  public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
    @Nullable Package ltexLsPackage = LtexLanguageServer.class.getPackage();
    @Nullable String ltexLsVersion = ((ltexLsPackage != null)
        ? ltexLsPackage.getImplementationVersion() : null);
    Tools.logger.info(Tools.i18n("initializingLtexLs",
        ((ltexLsVersion != null) ? ltexLsVersion : "null")));

    ServerCapabilities capabilities = new ServerCapabilities();
    capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
    capabilities.setCodeActionProvider(new CodeActionOptions(CodeActionGenerator.getCodeActions()));

    List<String> commandNames = new ArrayList<>();
    commandNames.addAll(LtexWorkspaceService.getCommandNames());
    capabilities.setExecuteCommandProvider(new ExecuteCommandOptions(commandNames));

    // Until it is specified in the LSP that the locale is automatically sent with
    // the initialization request, we have to do that manually.
    // See https://github.com/microsoft/language-server-protocol/issues/754.
    JsonObject initializationOptions = (JsonObject)params.getInitializationOptions();

    if ((initializationOptions != null) && initializationOptions.has("locale")) {
      String localeLanguage = initializationOptions.get("locale").getAsString();
      Locale locale = Locale.forLanguageTag(localeLanguage);
      Tools.logger.info(Tools.i18n("settingLocale", locale.getLanguage()));
      Tools.setLocale(locale);
    }

    return CompletableFuture.completedFuture(new InitializeResult(capabilities));
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
}
