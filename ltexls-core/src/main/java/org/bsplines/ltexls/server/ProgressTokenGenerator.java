/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.server;

import com.google.gson.JsonObject;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

public class ProgressTokenGenerator {
  private int counter;

  public ProgressTokenGenerator() {
    this.counter = 0;
  }

  public Either<String, Number> generate(String uri, String operation) {
    JsonObject jsonToken = new JsonObject();
    jsonToken.addProperty("uri", uri);
    jsonToken.addProperty("operation", operation);
    jsonToken.addProperty("counter", this.counter);
    this.counter++;
    return Either.forLeft(jsonToken.toString());
  }
}
