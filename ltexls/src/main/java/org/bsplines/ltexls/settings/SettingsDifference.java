/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.settings;

import org.checkerframework.checker.nullness.qual.Nullable;

public class SettingsDifference {
  private String name;
  private @Nullable Object value;
  private @Nullable Object otherValue;

  public SettingsDifference(String name, @Nullable Object value, @Nullable Object otherValue) {
    this.name = name;
    this.value = value;
    this.otherValue = otherValue;
  }

  public String getName() {
    return this.name;
  }

  public @Nullable Object getValue() {
    return this.value;
  }

  public @Nullable Object getOtherValue() {
    return this.otherValue;
  }
}
