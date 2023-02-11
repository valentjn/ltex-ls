/* Copyright (C) 2019-2023 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.client

import com.google.gson.JsonObject
import org.eclipse.lsp4j.ConfigurationParams
import org.eclipse.lsp4j.MessageActionItem
import org.eclipse.lsp4j.MessageParams
import org.eclipse.lsp4j.PublishDiagnosticsParams
import org.eclipse.lsp4j.ShowMessageRequestParams
import java.util.concurrent.CompletableFuture

class MockLtexLanguageClient : LtexLanguageClient {
  private val _publishDiagnosticsParamsList: MutableList<PublishDiagnosticsParams> = ArrayList()
  val publishDiagnosticsParamsList: List<PublishDiagnosticsParams>
    get() = _publishDiagnosticsParamsList

  override fun telemetryEvent(`object`: Any) {
  }

  override fun publishDiagnostics(publishDiagnosticsParams: PublishDiagnosticsParams) {
    this._publishDiagnosticsParamsList.add(publishDiagnosticsParams)
  }

  override fun showMessage(messageParams: MessageParams) {
  }

  override fun showMessageRequest(
    showMessageRequestParams: ShowMessageRequestParams,
  ): CompletableFuture<MessageActionItem> {
    return CompletableFuture.completedFuture(MessageActionItem("foobar"))
  }

  override fun logMessage(messageParams: MessageParams) {
  }

  override fun configuration(
    configurationParams: ConfigurationParams,
  ): CompletableFuture<List<Any>> {
    return CompletableFuture.completedFuture(listOf(JsonObject()))
  }

  override fun ltexWorkspaceSpecificConfiguration(
    configurationParams: ConfigurationParams,
  ): CompletableFuture<List<Any?>> {
    return CompletableFuture.completedFuture(listOf(JsonObject()))
  }
}
