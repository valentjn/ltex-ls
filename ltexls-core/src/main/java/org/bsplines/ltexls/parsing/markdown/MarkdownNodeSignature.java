/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.markdown;

import org.bsplines.ltexls.parsing.DummyGenerator;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MarkdownNodeSignature {
  public enum Action {
    DEFAULT,
    IGNORE,
    DUMMY,
  }

  private String name;
  private Action action;
  private DummyGenerator dummyGenerator;

  public MarkdownNodeSignature(String name) {
    this(name, Action.IGNORE, DummyGenerator.getDefault());
  }

  public MarkdownNodeSignature(String name, Action action) {
    this(name, action, DummyGenerator.getDefault());
  }

  public MarkdownNodeSignature(String name, Action action, DummyGenerator dummyGenerator) {
    this.name = name;
    this.action = action;
    this.dummyGenerator = dummyGenerator;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if ((obj == null) || !MarkdownNodeSignature.class.isAssignableFrom(obj.getClass())) {
      return false;
    }

    MarkdownNodeSignature other = (MarkdownNodeSignature)obj;

    if (!this.name.equals(other.name)) return false;
    if (!this.action.equals(other.action)) return false;
    if (!this.dummyGenerator.equals(other.dummyGenerator)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int hash = 3;

    hash = 53 * hash + this.name.hashCode();
    hash = 53 * hash + this.action.hashCode();
    hash = 53 * hash + this.dummyGenerator.hashCode();

    return hash;
  }

  public String getName() {
    return this.name;
  }

  public Action getAction() {
    return this.action;
  }

  public DummyGenerator getDummyGenerator() {
    return this.dummyGenerator;
  }
}
