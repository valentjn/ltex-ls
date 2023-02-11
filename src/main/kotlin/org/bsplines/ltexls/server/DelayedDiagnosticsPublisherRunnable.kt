/* Copyright (C) 2019-2023 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.server

import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.PublishDiagnosticsParams
import org.eclipse.lsp4j.services.LanguageClient
import java.time.Duration
import java.time.Instant

class DelayedDiagnosticsPublisherRunnable(
  val languageClient: LanguageClient,
  val document: LtexTextDocumentItem,
) : Runnable {
  override fun run() {
    var sleepDuration: Duration = SHOW_CARET_DIAGNOSTICS_DURATION.minus(
      Duration.between(this.document.lastCaretChangeInstant, Instant.now()),
    )
    if (sleepDuration.isNegative) sleepDuration = Duration.ZERO
    sleepDuration = sleepDuration.plusMillis(SLEEP_DURATION_DELTA_MILLISECONDS)

    try {
      Thread.sleep(sleepDuration.toMillis())
    } catch (e: InterruptedException) {
      return
    }

    if (
      Duration.between(this.document.lastCaretChangeInstant, Instant.now())
      > SHOW_CARET_DIAGNOSTICS_DURATION
    ) {
      val diagnostics: List<Diagnostic>? = this.document.diagnosticsCache

      if (diagnostics != null) {
        this.languageClient.publishDiagnostics(
          PublishDiagnosticsParams(this.document.uri, diagnostics),
        )
      }
    }
  }

  companion object {
    private val SHOW_CARET_DIAGNOSTICS_DURATION: Duration = Duration.ofSeconds(2)
    private const val SLEEP_DURATION_DELTA_MILLISECONDS = 10L
  }
}
