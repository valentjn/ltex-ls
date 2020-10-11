/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.server;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import org.checkerframework.checker.nullness.NullnessUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LtexTextDocumentItemTest {
  private static void assertPosition(LtexTextDocumentItem document, int pos, Position position) {
    Assertions.assertEquals(position, document.convertPosition(pos));
    Assertions.assertEquals(pos, document.convertPosition(position));
  }

  private static void assertNull(@Nullable Object actual) {
    @SuppressWarnings("assignment.type.incompatible")
    @NonNull Object actualNonNull = actual;
    Assertions.assertNull(actualNonNull);
  }

  @Test
  public void testConvertPosition() {
    LtexLanguageServer languageServer = new LtexLanguageServer();
    LtexTextDocumentItem document;

    document = new LtexTextDocumentItem(languageServer, "untitled:test.md", "markdown", 1,
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

    document = new LtexTextDocumentItem(languageServer, "untitled:test.md", "markdown", 1, "\nHi");
    Assertions.assertEquals(new Position(1, 0), document.convertPosition(1));
    assertPosition(document, 1, new Position(1, 0));
  }

  @Test
  public void testApplyIncrementalTextChangeEvents() {
    LtexLanguageServer languageServer = new LtexLanguageServer();
    LtexTextDocumentItem document = new LtexTextDocumentItem(
        languageServer,"untitled:text.md", "markdown", 1, "abcdef");
    Instant pastInstant = Instant.now().minus(Duration.ofSeconds(10));

    document.setLastCaretChangeInstant(pastInstant);
    document.applyTextChangeEvent(new TextDocumentContentChangeEvent(
        new Range(new Position(0, 3), new Position(0, 3)), 0, "1"));
    Assertions.assertEquals("abc1def", document.getText());
    Assertions.assertEquals(new Position(0, 4),
        NullnessUtil.castNonNull(document.getCaretPosition()));
    Assertions.assertTrue(
        Duration.between(document.getLastCaretChangeInstant(), Instant.now()).toMillis() < 100);

    document.setLastCaretChangeInstant(pastInstant);
    document.applyTextChangeEvent(new TextDocumentContentChangeEvent(
        new Range(new Position(0, 1), new Position(0, 2)), 1, ""));
    Assertions.assertEquals("ac1def", document.getText());
    Assertions.assertEquals(new Position(0, 1),
        NullnessUtil.castNonNull(document.getCaretPosition()));
    Assertions.assertTrue(
        Duration.between(document.getLastCaretChangeInstant(), Instant.now()).toMillis() < 100);

    document.applyTextChangeEvent(new TextDocumentContentChangeEvent(
        new Range(new Position(0, 3), new Position(0, 3)), 0, "23"));
    Assertions.assertEquals("ac123def", document.getText());
    Assertions.assertEquals(new Position(0, 5),
        NullnessUtil.castNonNull(document.getCaretPosition()));

    document.applyTextChangeEvents(Collections.singletonList(new TextDocumentContentChangeEvent(
        new Range(new Position(0, 5), new Position(0, 5)), 0, "4")));
    Assertions.assertEquals("ac1234def", document.getText());
    Assertions.assertEquals(new Position(0, 6),
        NullnessUtil.castNonNull(document.getCaretPosition()));

    document.applyTextChangeEvents(Arrays.asList(
        new TextDocumentContentChangeEvent(
          new Range(new Position(0, 6), new Position(0, 6)), 0, "5"),
        new TextDocumentContentChangeEvent(
          new Range(new Position(0, 7), new Position(0, 7)), 0, "6")));
    Assertions.assertEquals("ac123456def", document.getText());
    assertNull(document.getCaretPosition());
  }

  @Test
  public void testApplyFullTextChangeEvents() {
    LtexLanguageServer languageServer = new LtexLanguageServer();
    LtexTextDocumentItem document = new LtexTextDocumentItem(
        languageServer,"untitled:text.md", "markdown", 1, "abcdef");
    Instant pastInstant = Instant.now().minus(Duration.ofSeconds(10));

    document.setLastCaretChangeInstant(pastInstant);
    document.applyTextChangeEvent(new TextDocumentContentChangeEvent("abc1def"));
    Assertions.assertEquals("abc1def", document.getText());
    Assertions.assertEquals(new Position(0, 4),
        NullnessUtil.castNonNull(document.getCaretPosition()));
    Assertions.assertTrue(
        Duration.between(document.getLastCaretChangeInstant(), Instant.now()).toMillis() < 100);

    document.setLastCaretChangeInstant(pastInstant);
    document.applyTextChangeEvent(new TextDocumentContentChangeEvent("ac1def"));
    Assertions.assertEquals("ac1def", document.getText());
    Assertions.assertEquals(new Position(0, 1),
        NullnessUtil.castNonNull(document.getCaretPosition()));
    Assertions.assertTrue(
        Duration.between(document.getLastCaretChangeInstant(), Instant.now()).toMillis() < 100);

    document.applyTextChangeEvent(new TextDocumentContentChangeEvent("ac123def"));
    Assertions.assertEquals("ac123def", document.getText());
    Assertions.assertEquals(new Position(0, 5),
        NullnessUtil.castNonNull(document.getCaretPosition()));

    document.applyTextChangeEvent(new TextDocumentContentChangeEvent("ac1234def"));
    Assertions.assertEquals("ac1234def", document.getText());
    Assertions.assertEquals(new Position(0, 6),
        NullnessUtil.castNonNull(document.getCaretPosition()));

    document.applyTextChangeEvents(Arrays.asList(
        new TextDocumentContentChangeEvent("ac12345def"),
        new TextDocumentContentChangeEvent("ac123456def")));
    Assertions.assertEquals("ac123456def", document.getText());
    assertNull(document.getCaretPosition());
  }

  @Test
  public void testProperties() {
    LtexLanguageServer languageServer = new LtexLanguageServer();

    LtexTextDocumentItem origDocument = new LtexTextDocumentItem(
        languageServer,"untitled:text.md", "markdown", 1, "abc");
    Assertions.assertTrue(origDocument.equals(origDocument));
    Assertions.assertDoesNotThrow(() -> origDocument.hashCode());

    {
      LtexTextDocumentItem document = new LtexTextDocumentItem(
          languageServer,"untitled:text.md", "markdown", 1, "abc");
      document.setLastCaretChangeInstant(origDocument.getLastCaretChangeInstant());
      document.setCaretPosition(new Position(13, 37));
      Assertions.assertEquals(new Position(13, 37),
          NullnessUtil.castNonNull(document.getCaretPosition()));
      Assertions.assertFalse(document.equals(origDocument));
      Assertions.assertFalse(origDocument.equals(document));
    }

    {
      LtexTextDocumentItem document = new LtexTextDocumentItem(
          languageServer, "untitled:text.md", "markdown", 1, "abc");
      Instant pastInstant = Instant.now().minus(Duration.ofSeconds(10));
      document.setLastCaretChangeInstant(pastInstant);
      Assertions.assertEquals(pastInstant, document.getLastCaretChangeInstant());
      Assertions.assertFalse(document.equals(origDocument));
      Assertions.assertFalse(origDocument.equals(document));
    }
  }
}
