/* Copyright (C) 2019-2023 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.client

import org.eclipse.lsp4j.ConfigurationParams
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest
import org.eclipse.lsp4j.services.LanguageClient
import java.util.concurrent.CompletableFuture

interface LtexLanguageClient : LanguageClient {
  @JsonRequest("ltex/workspaceSpecificConfiguration")
  fun ltexWorkspaceSpecificConfiguration(
    configurationParams: ConfigurationParams,
  ): CompletableFuture<List<Any?>>
}
