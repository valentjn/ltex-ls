/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.server;

import com.google.gson.JsonElement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.bsplines.ltexls.client.LtexLanguageClient;
import org.bsplines.ltexls.client.LtexProgressParams;
import org.bsplines.ltexls.languagetool.LanguageToolRuleMatch;
import org.bsplines.ltexls.parsing.AnnotatedTextFragment;
import org.bsplines.ltexls.tools.Tools;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.lsp4j.ConfigurationItem;
import org.eclipse.lsp4j.ConfigurationParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.xtext.xbase.lib.Pair;

public class LtexTextDocumentItem extends TextDocumentItem {
  private LtexLanguageServer languageServer;
  private List<Integer> lineStartPosList;
  private List<Diagnostic> diagnostics;
  private @Nullable Position caretPosition;
  private Instant lastCaretChangeInstant;

  /**
   * Constructor.
   *
   * @param uri URI
   * @param codeLanguageId ID of the code language
   * @param version version
   * @param text text
   */
  public LtexTextDocumentItem(LtexLanguageServer languageServer,
        String uri, String codeLanguageId, int version, String text) {
    super(uri, codeLanguageId, version, text);
    this.languageServer = languageServer;
    this.lineStartPosList = new ArrayList<>();
    this.diagnostics = new ArrayList<>();
    this.caretPosition = null;
    this.lastCaretChangeInstant = Instant.now();
    reinitializeLineStartPosList(text, this.lineStartPosList);
  }

  public LtexTextDocumentItem(LtexTextDocumentItem document) {
    this(document.getLanguageServer(), document.getUri(), document.getLanguageId(),
        document.getVersion(), document.getText());
  }

  private void reinitializeLineStartPosList() {
    reinitializeLineStartPosList(getText(), this.lineStartPosList);
  }

  private static void reinitializeLineStartPosList(String text, List<Integer> lineStartPosList) {
    lineStartPosList.clear();
    lineStartPosList.add(0);

    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);

      if (c == '\r') {
        if ((i + 1 < text.length()) && (text.charAt(i + 1) == '\n')) i++;
        lineStartPosList.add(i + 1);
      } else if (c == '\n') {
        lineStartPosList.add(i + 1);
      }
    }
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if ((obj == null) || !LtexTextDocumentItem.class.isAssignableFrom(obj.getClass())) return false;
    LtexTextDocumentItem other = (LtexTextDocumentItem)obj;

    if (!super.equals(other)) return false;
    if (!this.lineStartPosList.equals(other.lineStartPosList)) return false;
    if (!this.diagnostics.equals(other.diagnostics)) return false;

    if ((this.caretPosition == null) ? (other.caretPosition != null) :
          ((other.caretPosition == null) || !this.caretPosition.equals(other.caretPosition))) {
      return false;
    }

    if (!this.lastCaretChangeInstant.equals(other.lastCaretChangeInstant)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int hash = 3;

    hash = 53 * hash + super.hashCode();
    hash = 53 * hash + this.lineStartPosList.hashCode();
    hash = 53 * hash + this.diagnostics.hashCode();
    if (this.caretPosition != null) hash = 53 * hash + this.caretPosition.hashCode();
    hash = 53 * hash + this.lastCaretChangeInstant.hashCode();

    return hash;
  }

  public LtexLanguageServer getLanguageServer() {
    return this.languageServer;
  }

  /**
   * Convert a line/column Position object to an integer position.
   *
   * @param position line/column Position object
   * @return integer position
   */
  public int convertPosition(Position position) {
    int line = position.getLine();
    int character = position.getCharacter();
    String text = getText();

    if (line < 0) {
      return 0;
    } else if (line >= this.lineStartPosList.size()) {
      return text.length();
    } else {
      int lineStart = this.lineStartPosList.get(line);
      int nextLineStart = ((line < this.lineStartPosList.size() - 1)
          ? this.lineStartPosList.get(line + 1) : text.length());
      int lineLength = nextLineStart - lineStart;

      if (character < 0) {
        return lineStart;
      } else if (character >= lineLength) {
        int pos = lineStart + lineLength;

        if (pos >= 1) {
          if (text.charAt(pos - 1) == '\r') {
            pos--;
          } else if (text.charAt(pos - 1) == '\n') {
            pos--;
            if ((pos >= 1) && (text.charAt(pos - 1) == '\r')) pos--;
          }
        }

        return pos;
      } else {
        return lineStart + character;
      }
    }
  }

  /**
   * Convert an integer position to a line/column Position object.
   *
   * @param pos integer position
   * @return line/column Position object
   */
  public Position convertPosition(int pos) {
    int line = Collections.binarySearch(this.lineStartPosList, pos);

    if (line < 0) {
      int insertionPoint = -line - 1;
      line = insertionPoint - 1;
    }

    return new Position(line, pos - this.lineStartPosList.get(line));
  }

  public List<Diagnostic> getDiagnostics() {
    return Collections.unmodifiableList(this.diagnostics);
  }

  public void setDiagnostics(List<Diagnostic> diagnostics) {
    this.diagnostics = new ArrayList<>(diagnostics);
  }

  public @Nullable Position getCaretPosition() {
    return ((this.caretPosition != null)
        ? new Position(this.caretPosition.getLine(), this.caretPosition.getCharacter()) : null);
  }

  public void setCaretPosition(@Nullable Position caretPosition) {
    if (caretPosition != null) {
      if (this.caretPosition != null) {
        this.caretPosition.setLine(caretPosition.getLine());
        this.caretPosition.setCharacter(caretPosition.getCharacter());
      } else {
        this.caretPosition = new Position(caretPosition.getLine(), caretPosition.getCharacter());
      }
    } else {
      this.caretPosition = null;
    }
  }

  public Instant getLastCaretChangeInstant() {
    return this.lastCaretChangeInstant;
  }

  public void setLastCaretChangeInstant(Instant lastCaretChangeInstant) {
    this.lastCaretChangeInstant = lastCaretChangeInstant;
  }

  @Override
  public void setText(String text) {
    super.setText(text);
    this.caretPosition = null;
    reinitializeLineStartPosList();
  }

  /**
   * Apply a list of full or incremental text change events.
   *
   * @param textChangeEvents list of text change events to apply
   */
  public void applyTextChangeEvents(List<TextDocumentContentChangeEvent> textChangeEvents) {
    Instant oldLastCaretChangeInstant = this.lastCaretChangeInstant;

    for (TextDocumentContentChangeEvent textChangeEvent : textChangeEvents) {
      applyTextChangeEvent(textChangeEvent);
    }

    if (textChangeEvents.size() > 1) {
      this.caretPosition = null;
      this.lastCaretChangeInstant = oldLastCaretChangeInstant;
    }
  }

  /**
   * Apply a full or incremental text change event.
   *
   * @param textChangeEvent text change event to apply
   */
  public void applyTextChangeEvent(TextDocumentContentChangeEvent textChangeEvent) {
    Range changeRange = textChangeEvent.getRange();
    String changeText = textChangeEvent.getText();

    if (changeRange != null) {
      String text = getText();
      int fromPos = convertPosition(changeRange.getStart());
      int toPos   = ((changeRange.getEnd() != changeRange.getStart())
          ? convertPosition(changeRange.getEnd()) : fromPos);
      text = text.substring(0, fromPos) + changeText + text.substring(toPos);
      setText(text);

      if ((fromPos == toPos) && (changeText.length() == 1)) {
        this.caretPosition = convertPosition(toPos + 1);
        this.lastCaretChangeInstant = Instant.now();
      } else if ((fromPos == toPos - 1) && changeText.isEmpty()) {
        if (this.caretPosition == null) this.caretPosition = new Position();
        this.caretPosition.setLine(changeRange.getStart().getLine());
        this.caretPosition.setCharacter(changeRange.getStart().getCharacter());
        this.lastCaretChangeInstant = Instant.now();
      } else {
        this.caretPosition = null;
      }
    } else {
      setText(changeText);
      this.caretPosition = null;
    }
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
    @Nullable LtexLanguageClient languageClient = this.languageServer.getLanguageClient();

    return getDiagnostics(document).thenApply((List<Diagnostic> diagnostics) -> {
      if (languageClient == null) return false;

      List<Diagnostic> diagnosticsNotAtCaret =
          extractDiagnosticsNotAtCaret(diagnostics, caretPosition);
      languageClient.publishDiagnostics(new PublishDiagnosticsParams(
          document.getUri(), diagnosticsNotAtCaret));
      document.setDiagnostics(diagnostics);

      if (diagnosticsNotAtCaret.size() < diagnostics.size()) {
        Thread thread = new Thread(new DelayedDiagnosticsPublisherRunnable(
            languageClient, document));
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
            diagnostics.add(this.languageServer.getCodeActionGenerator().createDiagnostic(
                match, document));
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


  /**
   * Check a document for diagnostics.
   *
   * @param document document to check
   * @return completable future with lists of rule matches and annotated text fragments
   */
  public CompletableFuture<Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>>>
        checkDocument(TextDocumentItem document) {
    @Nullable LtexLanguageClient languageClient = this.languageServer.getLanguageClient();

    if (languageClient == null) {
      return CompletableFuture.completedFuture(
          Pair.of(Collections.emptyList(), Collections.emptyList()));
    }

    ConfigurationItem configurationItem = new ConfigurationItem();
    configurationItem.setSection("ltex");
    configurationItem.setScopeUri(document.getUri());
    CompletableFuture<List<Object>> configurationFuture = languageClient.configuration(
        new ConfigurationParams(Collections.singletonList(configurationItem)));
    languageClient.ltexProgress(new LtexProgressParams(
        document.getUri(), "checkDocument", 0));

    return configurationFuture.thenApply((List<Object> configuration) -> {
      try {
        this.languageServer.getSettingsManager().setSettings((JsonElement)configuration.get(0));
        return this.languageServer.getDocumentChecker().check(document);
      } finally {
        if (languageClient != null) {
          languageClient.ltexProgress(new LtexProgressParams(
              document.getUri(), "checkDocument", 1));
        }
      }
    });
  }
}
