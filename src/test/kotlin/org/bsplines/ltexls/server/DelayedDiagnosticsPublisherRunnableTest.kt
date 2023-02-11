/* Copyright (C) 2019-2023 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.server

import org.bsplines.ltexls.client.MockLtexLanguageClient
import org.eclipse.lsp4j.PublishDiagnosticsParams
import kotlin.test.Test
import kotlin.test.assertEquals

class DelayedDiagnosticsPublisherRunnableTest {
  @Test
  fun testRun() {
    val languageServer = LtexLanguageServer()
    val languageClient = MockLtexLanguageClient()
    languageServer.connect(languageClient)

    val document = LtexTextDocumentItem(
      languageServer,
      "untitled:test.md",
      "markdown",
      1,
      "This is an test.\n",
    )
    document.checkAndPublishDiagnosticsWithCache()

    val runnable = DelayedDiagnosticsPublisherRunnable(languageClient, document)
    runnable.run()

    assertEquals(2, languageClient.publishDiagnosticsParamsList.size)

    for (
      publishDiagnosticsParams: PublishDiagnosticsParams in
      languageClient.publishDiagnosticsParamsList
    ) {
      assertEquals("untitled:test.md", publishDiagnosticsParams.uri)
      assertEquals(1, publishDiagnosticsParams.diagnostics.size)
    }
  }
}
