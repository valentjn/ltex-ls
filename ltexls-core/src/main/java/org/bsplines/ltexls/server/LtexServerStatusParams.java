/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.server;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

public class LtexServerStatusParams {
  private long processId;
  private double wallClockDuration;
  private @Nullable Double cpuUsage;
  private @Nullable Double cpuDuration;
  private double usedMemory;
  private double totalMemory;

  public LtexServerStatusParams(long processId, double wallClockDuration, @Nullable Double cpuUsage,
        @Nullable Double cpuDuration, double usedMemory, double totalMemory) {
    this.processId = processId;
    this.wallClockDuration = wallClockDuration;
    this.cpuUsage = cpuUsage;
    this.cpuDuration = cpuDuration;
    this.usedMemory = usedMemory;
    this.totalMemory = totalMemory;
  }

  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this);
    builder.add("processId", this.processId);
    builder.add("wallClockDuration", this.wallClockDuration);
    if (this.cpuUsage != null) builder.add("cpuUsage", this.cpuUsage);
    if (this.cpuDuration != null) builder.add("cpuDuration", this.cpuDuration);
    builder.add("usedMemory", this.usedMemory);
    builder.add("totalMemory", this.totalMemory);
    return builder.toString();
  }
}
