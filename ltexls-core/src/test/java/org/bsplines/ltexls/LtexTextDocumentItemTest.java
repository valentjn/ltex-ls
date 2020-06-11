package org.bsplines.ltexls;

import org.eclipse.lsp4j.Position;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LtexTextDocumentItemTest {
  private static void assertPosition(LtexTextDocumentItem document, int pos, Position position) {
    Assertions.assertEquals(position, document.convertPosition(pos));
    Assertions.assertEquals(pos, document.convertPosition(position));
  }

  @Test
  public void testConvertPosition() {
    LtexTextDocumentItem document;

    document = new LtexTextDocumentItem("untitled:test.txt", "plaintext", 1,
        "Hello\nEnthusiastic\r\nReader!");
    assertPosition(document, 0, new Position(0, 0));
    assertPosition(document, 6, new Position(1, 0));
    assertPosition(document, 7, new Position(1, 1));
    assertPosition(document, 12, new Position(1, 6));
    assertPosition(document, 18, new Position(1, 12));
    assertPosition(document, 20, new Position(2, 0));

    Assertions.assertEquals(0, document.convertPosition(new Position(-1, 0)));
    Assertions.assertEquals(27, document.convertPosition(new Position(3, 0)));
    Assertions.assertEquals(6, document.convertPosition(new Position(1, -1)));
    Assertions.assertEquals(5, document.convertPosition(new Position(0, 20)));
    Assertions.assertEquals(18, document.convertPosition(new Position(1, 20)));

    document = new LtexTextDocumentItem("untitled:test.txt", "plaintext", 1, "\nHi");
    Assertions.assertEquals(new Position(1, 0), document.convertPosition(1));
    assertPosition(document, 1, new Position(1, 0));
  }
}
