package org.bsplines.ltexls;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import org.bsplines.ltexls.languagetool.LanguageToolRuleMatch;
import org.bsplines.ltexls.parsing.AnnotatedTextFragment;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionOptions;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.ConfigurationItem;
import org.eclipse.lsp4j.ConfigurationParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.ExecuteCommandOptions;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

import org.eclipse.xtext.xbase.lib.Pair;

public class LtexLanguageServer implements LanguageServer, LanguageClientAware {
  private HashMap<String, TextDocumentItem> documents = new HashMap<>();
  private @MonotonicNonNull LanguageClient languageClient;
  private SettingsManager settingsManager;
  private DocumentChecker documentChecker;
  private CodeActionGenerator codeActionGenerator;

  public LtexLanguageServer() {
    this.documents = new HashMap<>();
    this.settingsManager = new SettingsManager();
    this.documentChecker = new DocumentChecker(settingsManager);
    this.codeActionGenerator = new CodeActionGenerator(settingsManager);
  }

  @Override
  public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
    ServerCapabilities capabilities = new ServerCapabilities();
    capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
    capabilities.setCodeActionProvider(new CodeActionOptions(CodeActionGenerator.getCodeActions()));
    capabilities.setExecuteCommandProvider(new ExecuteCommandOptions(
        CodeActionGenerator.getCommandNames()));

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
    // Per https://github.com/eclipse/lsp4j/issues/18
    return CompletableFuture.completedFuture(new Object());
  }

  @Override
  public void exit() {
    System.exit(0);
  }

  @Override
  public TextDocumentService getTextDocumentService() {
    return new FullTextDocumentService(documents) {

      @Override
      public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(
            CodeActionParams params) {
        if (params.getContext().getDiagnostics().isEmpty()) {
          return CompletableFuture.completedFuture(Collections.emptyList());
        }

        String uri = params.getTextDocument().getUri();
        TextDocumentItem document = documents.get(uri);

        if (document == null) {
          Tools.logger.warning(Tools.i18n("couldNotFindDocumentWithUri", uri));
          return CompletableFuture.completedFuture(Collections.emptyList());
        }

        return checkDocument(document).thenApply(
              (Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>> checkingResult) -> {
          return codeActionGenerator.generate(params, document, checkingResult);
        });
      }

      @Override
      public void didOpen(DidOpenTextDocumentParams params) {
        super.didOpen(params);
        publishIssues(params.getTextDocument().getUri());
      }

      @Override
      public void didChange(DidChangeTextDocumentParams params) {
        super.didChange(params);
        publishIssues(params.getTextDocument().getUri());
      }

      @Override
      public void didClose(DidCloseTextDocumentParams params) {
        super.didClose(params);
        String uri = params.getTextDocument().getUri();
        if (languageClient == null) return;
        languageClient.publishDiagnostics(
            new PublishDiagnosticsParams(uri, Collections.emptyList()));
      }

      private void publishIssues(String uri) {
        TextDocumentItem document = documents.get(uri);

        if (document != null) {
          LtexLanguageServer.this.publishIssues(document);
        } else {
          Tools.logger.warning(Tools.i18n("couldNotFindDocumentWithUri", uri));
        }
      }
    };
  }

  private CompletableFuture<Void> publishIssues(TextDocumentItem document) {
    return getIssues(document).thenApply((List<Diagnostic> diagnostics) -> {
      if (languageClient == null) return null;
      languageClient.publishDiagnostics(new PublishDiagnosticsParams(
          document.getUri(), diagnostics));
      return null;
    });
  }

  private CompletableFuture<List<Diagnostic>> getIssues(TextDocumentItem document) {
    return checkDocument(document).thenApply(
          (Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>> checkingResult) -> {
      List<LanguageToolRuleMatch> matches = checkingResult.getKey();
      DocumentPositionCalculator positionCalculator =
          new DocumentPositionCalculator(document.getText());
      List<Diagnostic> diagnostics = new ArrayList<>();

      for (LanguageToolRuleMatch match : matches) {
        diagnostics.add(codeActionGenerator.createDiagnostic(match, positionCalculator));
      }

      return diagnostics;
    });
  }

  private void sendProgressEvent(String uri, String operation, double progress) {
    // real progress events from LSP 3.15 are not implemented yet in LSP4J
    // (see https://github.com/eclipse/lsp4j/issues/370)
    JsonObject arguments = new JsonObject();
    arguments.addProperty("type", "progress");
    arguments.addProperty("uri", uri);
    arguments.addProperty("operation", operation);
    arguments.addProperty("progress", progress);
    if (languageClient == null) return;
    languageClient.telemetryEvent(arguments);
  }

  private CompletableFuture<Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>>>
        checkDocument(TextDocumentItem document) {
    ConfigurationItem configurationItem = new ConfigurationItem();
    configurationItem.setSection("ltex");
    configurationItem.setScopeUri(document.getUri());

    if (languageClient == null) {
      return CompletableFuture.completedFuture(
          Pair.of(Collections.emptyList(), Collections.emptyList()));
    }

    CompletableFuture<List<Object>> configurationFuture = languageClient.configuration(
        new ConfigurationParams(Arrays.asList(configurationItem)));
    sendProgressEvent(document.getUri(), "checkDocument", 0);

    return configurationFuture.thenApply((List<Object> configuration) -> {
      try {
        settingsManager.setSettings((JsonElement)configuration.get(0));
        return documentChecker.check(document);
      } finally {
        sendProgressEvent(document.getUri(), "checkDocument", 1);
      }
    });
  }

  @Override
  public WorkspaceService getWorkspaceService() {
    return new NoOpWorkspaceService() {
      @Override
      public void didChangeConfiguration(DidChangeConfigurationParams params) {
        super.didChangeConfiguration(params);
        settingsManager.setSettings(((JsonObject)params.getSettings()).get("ltex"));
        documents.values().forEach(LtexLanguageServer.this::publishIssues);
      }

      @Override
      public CompletableFuture<Object> executeCommand(ExecuteCommandParams params) {
        if (CodeActionGenerator.getCommandNames().contains(params.getCommand())
              && (languageClient != null)) {
          languageClient.telemetryEvent(params.getArguments().get(0));
          return CompletableFuture.completedFuture(true);
        } else {
          return CompletableFuture.completedFuture(false);
        }
      }
    };
  }

  @Override
  public void connect(LanguageClient languageClient) {
    this.languageClient = languageClient;
  }
}
