package org.bsplines.ltexls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentItem;

public class LtexTextDocumentItem extends TextDocumentItem {
  private List<Integer> lineStartPosList;

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
    lineStartPosList = new ArrayList<>();
    reinitializeLineStartPosList(text, lineStartPosList);
  }

  private void reinitializeLineStartPosList() {
    reinitializeLineStartPosList(getText(), lineStartPosList);
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
    if (!lineStartPosList.equals(other.lineStartPosList)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int hash = 3;

    hash = 53 * hash + super.hashCode();
    hash = 53 * hash + lineStartPosList.hashCode();

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
    int textLength = getText().length();

    if (line < 0) {
      return 0;
    } else if (line >= lineStartPosList.size()) {
      return textLength;
    } else {
      int lineStart = lineStartPosList.get(line);
      int nextLineStart = ((line < lineStartPosList.size() - 1)
          ? lineStartPosList.get(line + 1) : textLength);
      int lineLength = nextLineStart - lineStart;

      if (character < 0) {
        return lineStart;
      } else if (character >= lineLength) {
        return lineStart + lineLength;
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
    int line = Collections.binarySearch(lineStartPosList, pos);

    if (line < 0) {
      int insertionPoint = -line - 1;
      line = insertionPoint - 1;
    }

    return new Position(line, pos - lineStartPosList.get(line));
  }

  @Override
  public void setText(String text) {
    super.setText(text);
    reinitializeLineStartPosList();
  }

  /**
   * Apply a full or incremental text change event.
   *
   * @param textChangeEvent text change event to apply
   */
  public void applyTextChangeEvent(TextDocumentContentChangeEvent textChangeEvent) {
    Range range = textChangeEvent.getRange();

    if (range != null) {
      String text = getText();
      int fromPos = convertPosition(range.getStart());
      int toPos   = ((range.getEnd() != range.getStart())
          ? convertPosition(range.getEnd()) : fromPos);
      text = text.substring(0, fromPos) + textChangeEvent.getText() + text.substring(toPos);
      setText(text);
    } else {
      setText(textChangeEvent.getText());
    }
  }
}
