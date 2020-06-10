package org.bsplines.ltexls;

import java.time.Duration;
import java.time.Instant;

import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.services.LanguageClient;

public class DelayedDiagnosticsPublisherRunnable implements Runnable {
  private static final Duration showCaretDiagnosticsDuration = Duration.ofSeconds(2);

  private LanguageClient languageClient;
  private LtexTextDocumentItem document;

  public DelayedDiagnosticsPublisherRunnable(LanguageClient languageClient,
        LtexTextDocumentItem document) {
    this.languageClient = languageClient;
    this.document = document;
  }

  @Override
  public void run() {
    try {
      Thread.sleep(showCaretDiagnosticsDuration.toMillis() + 10);
    } catch (InterruptedException e) {
      return;
    }

    if (Duration.between(this.document.getLastCaretChangeInstant(),
          Instant.now()).compareTo(showCaretDiagnosticsDuration) > 0) {
      this.languageClient.publishDiagnostics(new PublishDiagnosticsParams(
          this.document.getUri(), this.document.getDiagnostics()));
    }
  }
}
