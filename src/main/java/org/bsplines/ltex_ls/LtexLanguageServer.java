package org.bsplines.ltex_ls;

import java.util.*;
import java.util.concurrent.*;

import com.google.gson.*;

import org.bsplines.ltex_ls.languagetool.*;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.*;

import org.eclipse.xtext.xbase.lib.Pair;

import org.languagetool.markup.AnnotatedText;

public class LtexLanguageServer implements LanguageServer, LanguageClientAware {
  private HashMap<String, TextDocumentItem> documents = new HashMap<>();
  private LanguageClient languageClient;
  private SettingsManager settingsManager;
  private DocumentValidator documentValidator;
  private CodeActionGenerator codeActionGenerator;

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

    settingsManager = new SettingsManager();
    documentValidator = new DocumentValidator(settingsManager);
    codeActionGenerator = new CodeActionGenerator();

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

        TextDocumentItem document = documents.get(params.getTextDocument().getUri());

        return validateDocument(document).thenApply(
              (Pair<List<LanguageToolRuleMatch>, AnnotatedText> validateResult) -> {
          return codeActionGenerator.generate(params, document, validateResult);
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
        languageClient.publishDiagnostics(
            new PublishDiagnosticsParams(uri, Collections.emptyList()));
      }

      private void publishIssues(String uri) {
        TextDocumentItem document = documents.get(uri);
        LtexLanguageServer.this.publishIssues(document);
      }
    };
  }

  private CompletableFuture<Void> publishIssues(TextDocumentItem document) {
    return getIssues(document).thenApply((List<Diagnostic> diagnostics) -> {
      languageClient.publishDiagnostics(new PublishDiagnosticsParams(
          document.getUri(), diagnostics));
      return null;
    });
  }

  private CompletableFuture<List<Diagnostic>> getIssues(TextDocumentItem document) {
    return validateDocument(document).thenApply(
          (Pair<List<LanguageToolRuleMatch>, AnnotatedText> validateResult) -> {
      List<LanguageToolRuleMatch> matches = validateResult.getKey();
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
    languageClient.telemetryEvent(arguments);
  }

  private CompletableFuture<Pair<List<LanguageToolRuleMatch>, AnnotatedText>> validateDocument(
        TextDocumentItem document) {
    ConfigurationItem configurationItem = new ConfigurationItem();
    configurationItem.setSection("ltex");
    configurationItem.setScopeUri(document.getUri());
    CompletableFuture<List<Object>> configurationFuture = languageClient.configuration(
        new ConfigurationParams(Arrays.asList(configurationItem)));
    sendProgressEvent(document.getUri(), "validateDocument", 0);

    return configurationFuture.thenApply((List<Object> configuration) -> {
      try {
        settingsManager.setSettings((JsonElement)configuration.get(0));
        return documentValidator.validate(document);
      } finally {
        sendProgressEvent(document.getUri(), "validateDocument", 1);
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
        if (CodeActionGenerator.getCommandNames().contains(params.getCommand())) {
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
