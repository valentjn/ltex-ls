/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.restructuredtext;

import org.checkerframework.checker.nullness.qual.Nullable;

public class RestructuredtextBlockSignature {
  public enum Type {
    PARAGRAPH,
    FOOTNOTE,
    DIRECTIVE,
    COMMENT,
    GRID_TABLE,
    SIMPLE_TABLE,
  }

  public enum Action {
    DEFAULT,
    IGNORE,
  }

  private Type type;
  private Action action;

  public RestructuredtextBlockSignature(Type type) {
    this(type, Action.IGNORE);
  }

  public RestructuredtextBlockSignature(Type type, Action action) {
    this.type = type;
    this.action = action;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if ((obj == null) || !RestructuredtextBlockSignature.class.isAssignableFrom(obj.getClass())) {
      return false;
    }

    RestructuredtextBlockSignature other = (RestructuredtextBlockSignature)obj;

    if (!this.type.equals(other.type)) return false;
    if (!this.action.equals(other.action)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int hash = 3;

    hash = 53 * hash + this.type.hashCode();
    hash = 53 * hash + this.action.hashCode();

    return hash;
  }

  public Type getType() {
    return this.type;
  }

  public Action getAction() {
    return this.action;
  }
}
