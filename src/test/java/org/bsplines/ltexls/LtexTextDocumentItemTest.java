package org.bsplines.ltexls;

import org.eclipse.lsp4j.Position;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LtexTextDocumentItemTest {
  @Test
  public void testConvertPosition() {
    LtexTextDocumentItem document;

    document = new LtexTextDocumentItem("untitled:test.txt", "plaintext", 1,
        "Hello\nEnthusiastic\r\nReader!");
    Assertions.assertEquals(new Position(0, 0), document.convertPosition(0));
    Assertions.assertEquals(new Position(1, 0), document.convertPosition(6));
    Assertions.assertEquals(new Position(1, 1), document.convertPosition(7));
    Assertions.assertEquals(new Position(1, 6), document.convertPosition(12));

    document = new LtexTextDocumentItem("untitled:test.txt", "plaintext", 1, "\nHi");
    Assertions.assertEquals(new Position(1, 0), document.convertPosition(1));
  }
}
