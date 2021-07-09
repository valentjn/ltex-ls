/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.server;

import java.util.concurrent.ExecutionException;
import org.bsplines.ltexls.client.MockLtexLanguageClient;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DelayedDiagnosticsPublisherRunnableTest {
  @Test
  public void testRun() throws InterruptedException, ExecutionException {
    LtexLanguageServer languageServer = new LtexLanguageServer();
    MockLtexLanguageClient languageClient = new MockLtexLanguageClient();
    languageServer.connect(languageClient);

    LtexTextDocumentItem document = new LtexTextDocumentItem(
        languageServer,"untitled:text.md", "markdown", 1, "This is an test.\n");
    document.checkAndPublishDiagnosticsWithCache();

    DelayedDiagnosticsPublisherRunnable runnable = new DelayedDiagnosticsPublisherRunnable(
        languageClient, document);
    runnable.run();

    Assertions.assertEquals(2, languageClient.getPublishDiagnosticsParamsList().size());

    for (PublishDiagnosticsParams publishDiagnosticsParams :
          languageClient.getPublishDiagnosticsParamsList()) {
      Assertions.assertEquals("untitled:text.md", publishDiagnosticsParams.getUri());
      Assertions.assertEquals(1, publishDiagnosticsParams.getDiagnostics().size());
    }
  }
}
