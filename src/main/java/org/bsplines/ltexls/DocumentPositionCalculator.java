package org.bsplines.ltexls;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.lsp4j.Position;

class DocumentPositionCalculator {
  private final int[] lineStartPositions;

  public DocumentPositionCalculator(String text) {
    lineStartPositions = getLineStartPositions(text);
  }

  private static Position getPosition(int pos, int[] lineStarts) {
    int line = Arrays.binarySearch(lineStarts, pos);

    if (line < 0) {
      int insertionPoint = -1 * line - 1;
      line = insertionPoint - 1;
    }

    return new Position(line, pos - lineStarts[line]);
  }

  private static int[] getLineStartPositions(String text) {
    ArrayList<Integer> lineStartPositions = new ArrayList<>();
    lineStartPositions.add(0);
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);

      if (c == '\r') {
        if (i + 1 < text.length() && text.charAt(i + 1) == '\n') {
          i++;
        }

        lineStartPositions.add(i + 1);
      } else if (c == '\n') {
        lineStartPositions.add(i + 1);
      }
    }

    return lineStartPositions.stream().mapToInt(i -> i).toArray();
  }

  public Position getPosition(int pos) {
    return getPosition(pos, lineStartPositions);
  }
}
