/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.server;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentItem;

public class LtexTextDocumentItem extends TextDocumentItem {
  private List<Integer> lineStartPosList;
  private List<Diagnostic> diagnostics;
  private @Nullable Position caretPosition;
  private Instant lastCaretChangeInstant;

  public LtexTextDocumentItem(TextDocumentItem document) {
    this(document.getUri(), document.getLanguageId(), document.getVersion(), document.getText());
  }

  /**
   * Constructor.
   *
   * @param uri URI
   * @param codeLanguageId ID of the code language
   * @param version version
   * @param text text
   */
  public LtexTextDocumentItem(String uri, String codeLanguageId, int version, String text) {
    super(uri, codeLanguageId, version, text);
    this.lineStartPosList = new ArrayList<>();
    this.diagnostics = new ArrayList<>();
    this.caretPosition = null;
    this.lastCaretChangeInstant = Instant.now();
    reinitializeLineStartPosList(text, this.lineStartPosList);
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
}
