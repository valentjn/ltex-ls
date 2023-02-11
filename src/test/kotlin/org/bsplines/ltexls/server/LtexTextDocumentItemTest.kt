/* Copyright (C) 2019-2023 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.server

import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.TextDocumentContentChangeEvent
import java.time.Duration
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LtexTextDocumentItemTest {
  @Test
  fun testConvertPosition() {
    val languageServer = LtexLanguageServer()
    var document = LtexTextDocumentItem(
      languageServer,
      "untitled:test.md",
      "markdown",
      1,
      "Hello\nEnthusiastic\r\nReader\r!",
    )

    assertPosition(document, 0, Position(0, 0))
    assertPosition(document, 6, Position(1, 0))
    assertPosition(document, 7, Position(1, 1))
    assertPosition(document, 12, Position(1, 6))
    assertPosition(document, 18, Position(1, 12))
    assertPosition(document, 20, Position(2, 0))
    assertPosition(document, 26, Position(2, 6))
    assertPosition(document, 27, Position(3, 0))
    assertEquals(0, document.convertPosition(Position(-1, 0)))
    assertEquals(27, document.convertPosition(Position(3, 0)))
    assertEquals(6, document.convertPosition(Position(1, -1)))
    assertEquals(5, document.convertPosition(Position(0, 20)))
    assertEquals(18, document.convertPosition(Position(1, 20)))

    document = LtexTextDocumentItem(languageServer, "untitled:test.md", "markdown", 1, "\nHi")
    assertEquals(Position(1, 0), document.convertPosition(1))
    assertPosition(document, 1, Position(1, 0))
  }

  @Test
  fun testApplyIncrementalTextChangeEvents() {
    val languageServer = LtexLanguageServer()
    val document = LtexTextDocumentItem(
      languageServer,
      "untitled:test.md",
      "markdown",
      1,
      "abcdef",
    )
    val pastInstant: Instant = Instant.now().minus(Duration.ofSeconds(10))

    document.lastCaretChangeInstant = pastInstant
    document.applyTextChangeEvent(
      TextDocumentContentChangeEvent(Range(Position(0, 3), Position(0, 3)), 0, "1"),
    )
    assertEquals("abc1def", document.text)
    assertEquals(Position(0, 4), document.caretPosition)
    assertTrue(Duration.between(document.lastCaretChangeInstant, Instant.now()).toMillis() < 100)

    document.lastCaretChangeInstant = pastInstant
    document.applyTextChangeEvent(
      TextDocumentContentChangeEvent(Range(Position(0, 1), Position(0, 2)), 1, ""),
    )
    assertEquals("ac1def", document.text)
    assertEquals(Position(0, 1), document.caretPosition)
    assertTrue(Duration.between(document.lastCaretChangeInstant, Instant.now()).toMillis() < 100)

    document.applyTextChangeEvent(
      TextDocumentContentChangeEvent(Range(Position(0, 3), Position(0, 3)), 0, "23"),
    )
    assertEquals("ac123def", document.text)
    assertEquals(Position(0, 5), document.caretPosition)

    document.applyTextChangeEvents(
      listOf(TextDocumentContentChangeEvent(Range(Position(0, 5), Position(0, 5)), 0, "4")),
    )
    assertEquals("ac1234def", document.text)
    assertEquals(Position(0, 6), document.caretPosition)

    document.applyTextChangeEvents(
      listOf(
        TextDocumentContentChangeEvent(Range(Position(0, 6), Position(0, 6)), 0, "5"),
        TextDocumentContentChangeEvent(Range(Position(0, 7), Position(0, 7)), 0, "6"),
      ),
    )
    assertEquals("ac123456def", document.text)
    assertNull(document.caretPosition)
  }

  @Test
  fun testApplyFullTextChangeEvents() {
    val languageServer = LtexLanguageServer()
    val document = LtexTextDocumentItem(languageServer, "untitled:test.md", "markdown", 1, "abcdef")
    val pastInstant: Instant = Instant.now().minus(Duration.ofSeconds(10))

    document.lastCaretChangeInstant = pastInstant
    document.applyTextChangeEvent(TextDocumentContentChangeEvent("abc1def"))
    assertEquals("abc1def", document.text)
    assertEquals(Position(0, 4), document.caretPosition)
    assertTrue(Duration.between(document.lastCaretChangeInstant, Instant.now()).toMillis() < 100)

    document.lastCaretChangeInstant = pastInstant
    document.applyTextChangeEvent(TextDocumentContentChangeEvent("ac1def"))
    assertEquals("ac1def", document.text)
    assertEquals(Position(0, 1), document.caretPosition)
    assertTrue(Duration.between(document.lastCaretChangeInstant, Instant.now()).toMillis() < 100)

    document.applyTextChangeEvent(TextDocumentContentChangeEvent("ac123def"))
    assertEquals("ac123def", document.text)
    assertEquals(Position(0, 5), document.caretPosition)

    document.applyTextChangeEvent(TextDocumentContentChangeEvent("ac1234def"))
    assertEquals("ac1234def", document.text)
    assertEquals(Position(0, 6), document.caretPosition)

    document.applyTextChangeEvents(
      listOf(
        TextDocumentContentChangeEvent("ac12345def"),
        TextDocumentContentChangeEvent("ac123456def"),
      ),
    )
    assertEquals("ac123456def", document.text)
    assertNull(document.caretPosition)
  }

  @Test
  fun testProperties() {
    val languageServer = LtexLanguageServer()
    val originalDocument = LtexTextDocumentItem(
      languageServer,
      "untitled:test.md",
      "markdown",
      1,
      "abc",
    )
    assertEquals(originalDocument, originalDocument)
    originalDocument.hashCode()
    assertEquals(languageServer, originalDocument.languageServer)

    run {
      val document = LtexTextDocumentItem(languageServer, "untitled:test.md", "markdown", 1, "abc")
      document.text = "foobar"
      assertEquals("foobar", document.text)
      assertNotEquals(document, originalDocument)
      assertNotEquals(originalDocument, document)
    }

    run {
      val document = LtexTextDocumentItem(languageServer, "untitled:test.md", "markdown", 1, "abc")
      document.lastCaretChangeInstant = originalDocument.lastCaretChangeInstant
      document.caretPosition = Position(13, 37)
      assertEquals(Position(13, 37), document.caretPosition)
      assertNotEquals(document, originalDocument)
      assertNotEquals(originalDocument, document)
      document.caretPosition = Position(13, 42)
      assertEquals(Position(13, 42), document.caretPosition)
      document.caretPosition = null
      assertNull(document.caretPosition)
    }

    run {
      val document = LtexTextDocumentItem(languageServer, "untitled:test.md", "markdown", 1, "abc")
      val pastInstant: Instant = Instant.now().minus(Duration.ofSeconds(10))
      document.lastCaretChangeInstant = pastInstant
      assertEquals(pastInstant, document.lastCaretChangeInstant)
      assertNotEquals(document, originalDocument)
      assertNotEquals(originalDocument, document)
    }
  }

  companion object {
    private fun assertPosition(document: LtexTextDocumentItem, pos: Int, position: Position) {
      assertEquals(position, document.convertPosition(pos))
      assertEquals(pos, document.convertPosition(position))
    }
  }
}
