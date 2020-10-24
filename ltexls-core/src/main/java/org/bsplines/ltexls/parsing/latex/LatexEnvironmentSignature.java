/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.latex;

import org.checkerframework.checker.nullness.qual.Nullable;

public class LatexEnvironmentSignature {
  public enum Action {
    DEFAULT,
    IGNORE,
  }

  private String name;
  private Action action;

  public LatexEnvironmentSignature(String name) {
    this(name, Action.IGNORE);
  }

  public LatexEnvironmentSignature(String name, Action action) {
    this.name = name;
    this.action = action;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if ((obj == null) || !LatexEnvironmentSignature.class.isAssignableFrom(obj.getClass())) {
      return false;
    }

    LatexEnvironmentSignature other = (LatexEnvironmentSignature)obj;

    if (!this.name.equals(other.name)) return false;
    if (!this.action.equals(other.action)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int hash = 3;

    hash = 53 * hash + this.name.hashCode();
    hash = 53 * hash + this.action.hashCode();

    return hash;
  }

  public String getName() {
    return this.name;
  }

  public Action getAction() {
    return this.action;
  }
}
