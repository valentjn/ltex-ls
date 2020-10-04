/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import org.bsplines.ltexls.Tools;
import org.bsplines.ltexls.client.LtexLanguageClient;
import org.bsplines.ltexls.languagetool.LanguageToolRuleMatch;
import org.bsplines.ltexls.parsing.AnnotatedTextFragment;
import org.bsplines.ltexls.settings.SettingsManager;
import org.checkerframework.checker.initialization.qual.NotOnlyInitialized;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.lsp4j.CodeActionOptions;
import org.eclipse.lsp4j.ConfigurationItem;
import org.eclipse.lsp4j.ConfigurationParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.ExecuteCommandOptions;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.eclipse.xtext.xbase.lib.Pair;

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
    capabilities.setTextDocumentSync(TextDocumentSyncKind.Incremental);
    capabilities.setCodeActionProvider(new CodeActionOptions(CodeActionGenerator.getCodeActions()));

    List<String> commandNames = new ArrayList<>();
    commandNames.addAll(CodeActionGenerator.getCommandNames());
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

  /**
   * Check a document and publish the resulting diagnostics.
   *
   * @param document document to check
   * @return completable future of type Boolean: true iff successful
   */
  public CompletableFuture<Boolean> publishDiagnostics(LtexTextDocumentItem document) {
    return publishDiagnostics(document, null);
  }

  /**
   * Check a document and publish the resulting diagnostics. If the current position of the caret
   * is given, the diagnostics that are at the caret position are withhold and will be published
   * after a short amount of time.
   *
   * @param document document to check
   * @param caretPosition optional position of the caret
   * @return completable future of type void
   */
  public CompletableFuture<Boolean> publishDiagnostics(LtexTextDocumentItem document,
        @Nullable Position caretPosition) {
    return getDiagnostics(document).thenApply((List<Diagnostic> diagnostics) -> {
      if (this.languageClient == null) return false;

      List<Diagnostic> diagnosticsNotAtCaret =
          extractDiagnosticsNotAtCaret(diagnostics, caretPosition);
      this.languageClient.publishDiagnostics(new PublishDiagnosticsParams(
          document.getUri(), diagnosticsNotAtCaret));
      document.setDiagnostics(diagnostics);

      if (diagnosticsNotAtCaret.size() < diagnostics.size()) {
        Thread thread = new Thread(new DelayedDiagnosticsPublisherRunnable(
            this.languageClient, document));
        thread.start();
      }

      return true;
    });
  }

  private CompletableFuture<List<Diagnostic>> getDiagnostics(LtexTextDocumentItem document) {
    return checkDocument(document)
        .thenApply((Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>>
        checkingResult) -> {
          List<LanguageToolRuleMatch> matches = checkingResult.getKey();
          List<Diagnostic> diagnostics = new ArrayList<>();

          for (LanguageToolRuleMatch match : matches) {
            diagnostics.add(this.codeActionGenerator.createDiagnostic(match, document));
          }

          return diagnostics;
        });
  }

  private List<Diagnostic> extractDiagnosticsNotAtCaret(
        List<Diagnostic> diagnostics, @Nullable Position caretPosition) {
    if (caretPosition == null) return Collections.unmodifiableList(diagnostics);
    List<Diagnostic> diagnosticsNotAtCaret = new ArrayList<>();
    int character = caretPosition.getCharacter();
    Position beforeCaretPosition = new Position(caretPosition.getLine(),
        ((character >= 1) ? (character - 1) : 0));
    Range caretRange = new Range(beforeCaretPosition, caretPosition);

    for (Diagnostic diagnostic : diagnostics) {
      if (!Tools.areRangesIntersecting(diagnostic.getRange(), caretRange)) {
        diagnosticsNotAtCaret.add(diagnostic);
      }
    }

    return diagnosticsNotAtCaret;
  }

  private void sendProgressEvent(String uri, String operation, double progress) {
    // real progress events from LSP 3.15 are not implemented yet in LSP4J
    // (see https://github.com/eclipse/lsp4j/issues/370)
    JsonObject arguments = new JsonObject();
    arguments.addProperty("type", "progress");
    arguments.addProperty("uri", uri);
    arguments.addProperty("operation", operation);
    arguments.addProperty("progress", progress);
    if (this.languageClient == null) return;
    this.languageClient.telemetryEvent(arguments);
  }

  /**
   * Check a document for diagnostics.
   *
   * @param document document to check
   * @return completable future with lists of rule matches and annotated text fragments
   */
  public CompletableFuture<Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>>>
        checkDocument(TextDocumentItem document) {
    ConfigurationItem configurationItem = new ConfigurationItem();
    configurationItem.setSection("ltex");
    configurationItem.setScopeUri(document.getUri());

    if (this.languageClient == null) {
      return CompletableFuture.completedFuture(
          Pair.of(Collections.emptyList(), Collections.emptyList()));
    }

    CompletableFuture<List<Object>> configurationFuture = this.languageClient.configuration(
        new ConfigurationParams(Collections.singletonList(configurationItem)));
    sendProgressEvent(document.getUri(), "checkDocument", 0);

    return configurationFuture.thenApply((List<Object> configuration) -> {
      try {
        this.settingsManager.setSettings((JsonElement)configuration.get(0));
        return this.documentChecker.check(document);
      } finally {
        sendProgressEvent(document.getUri(), "checkDocument", 1);
      }
    });
  }

  @Override
  public TextDocumentService getTextDocumentService() {
    return this.ltexTextDocumentService;
  }

  @Override
  public WorkspaceService getWorkspaceService() {
    return this.ltexWorkspaceService;
  }

  public @Nullable LanguageClient getLanguageClient() {
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
