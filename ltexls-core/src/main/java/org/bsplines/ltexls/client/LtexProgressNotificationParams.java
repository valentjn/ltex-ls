/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.client;

import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

// real progress events from LSP 3.15 are not implemented yet in LSP4J
// (see https://github.com/eclipse/lsp4j/issues/370)
public class LtexProgressNotificationParams {
  private String uri;
  private String operation;
  private double progress;

  public LtexProgressNotificationParams(String uri, String operation, double progress) {
    this.uri = uri;
    this.operation = operation;
    this.progress = progress;
  }

  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this);
    builder.add("uri", this.uri);
    builder.add("operation", this.operation);
    builder.add("progress", this.progress);
    return builder.toString();
  }
}
