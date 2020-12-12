/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.client;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.ConfigurationParams;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.services.LanguageClient;

public interface LtexLanguageClient extends LanguageClient {
  @JsonRequest("ltex/workspaceSpecificConfiguration")
  CompletableFuture<List<Object>> ltexWorkspaceSpecificConfiguration(ConfigurationParams params);
}
