/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.client;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.lsp4j.ConfigurationParams;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;

public class MockLtexLanguageClient implements LtexLanguageClient {
  private List<PublishDiagnosticsParams> publishDiagnosticsParamsList;

  public MockLtexLanguageClient() {
    this.publishDiagnosticsParamsList = new ArrayList<>();
  }

  @Override
  public void telemetryEvent(Object object) {
  }

  @Override
  public void publishDiagnostics(PublishDiagnosticsParams publishDiagnosticsParams) {
    this.publishDiagnosticsParamsList.add(publishDiagnosticsParams);
  }

  @Override
  public void showMessage(MessageParams messageParams) {
  }

  @Override
  public CompletableFuture<MessageActionItem> showMessageRequest(
        ShowMessageRequestParams showMessageRequestParams) {
    return CompletableFuture.completedFuture(new MessageActionItem("foobar"));
  }

  @Override
  public void logMessage(MessageParams messageParams) {
  }

  @Override
  public CompletableFuture<List<Object>> configuration(ConfigurationParams configurationParams) {
    return CompletableFuture.completedFuture(Collections.singletonList(new JsonObject()));
  }

  @Override
  public CompletableFuture<List<@Nullable Object>> ltexWorkspaceSpecificConfiguration(
        ConfigurationParams configurationParams) {
    return CompletableFuture.completedFuture(Collections.singletonList(new JsonObject()));
  }

  public List<PublishDiagnosticsParams> getPublishDiagnosticsParamsList() {
    return Collections.unmodifiableList(this.publishDiagnosticsParamsList);
  }
}
