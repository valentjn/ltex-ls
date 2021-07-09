/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.latex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.checkerframework.checker.nullness.qual.Nullable;

public class LatexEnvironmentSignature extends LatexCommandSignature {
  private static final Pattern prefixPattern = Pattern.compile("^\\\\begin\\{([^\\}]+)\\}");

  private boolean ignoreAllArguments;
  private String environmentName;

  public LatexEnvironmentSignature(String environmentPrototype) {
    this(environmentPrototype, Action.IGNORE);
  }

  public LatexEnvironmentSignature(String environmentPrototype, Action action) {
    super((prefixPattern.matcher(environmentPrototype).find()
          ? environmentPrototype : "\\begin{" + environmentPrototype + "}"),
        action);

    Matcher prefixMatcher = prefixPattern.matcher(environmentPrototype);
    boolean matchFound = prefixMatcher.find();
    @Nullable String environmentName = (matchFound ? prefixMatcher.group(1) : null);

    if (environmentName != null) {
      this.ignoreAllArguments = false;
      this.environmentName = environmentName;
    } else {
      this.ignoreAllArguments = true;
      this.environmentName = environmentPrototype;
    }
  }

  public boolean doesIgnoreAllArguments() {
    return this.ignoreAllArguments;
  }

  public String getEnvironmentName() {
    return this.environmentName;
  }
}
