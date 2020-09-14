/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.settings;

import java.util.regex.Pattern;
import org.checkerframework.checker.nullness.qual.Nullable;

public class IgnoreRuleSentencePair {
  private final String ruleId;
  private final String sentenceString;
  private final Pattern sentencePattern;

  /**
   * Constructor.
   *
   * @param ruleId ID of the LanguageTool rule
   * @param sentenceString regular expression of the sentence to ignore
   */
  public IgnoreRuleSentencePair(String ruleId, String sentenceString) {
    this.ruleId = ruleId;
    this.sentenceString = sentenceString;
    this.sentencePattern = Pattern.compile(sentenceString);
  }

  public IgnoreRuleSentencePair(IgnoreRuleSentencePair obj) {
    this(obj.ruleId, obj.sentenceString);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if ((obj == null) || !IgnoreRuleSentencePair.class.isAssignableFrom(obj.getClass())) {
      return false;
    }

    IgnoreRuleSentencePair other = (IgnoreRuleSentencePair)obj;

    if ((this.ruleId == null) ? (other.ruleId != null) : !this.ruleId.equals(other.ruleId)) {
      return false;
    }

    if ((this.sentenceString == null) ? (other.sentenceString != null) :
          !this.sentenceString.equals(other.sentenceString)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hash = 3;

    hash = 53 * hash + ((this.ruleId != null) ? this.ruleId.hashCode() : 0);
    hash = 53 * hash + ((this.sentenceString != null) ? this.sentenceString.hashCode() : 0);

    return hash;
  }

  public String getRuleId() {
    return this.ruleId;
  }

  public String getSentenceString() {
    return this.sentenceString;
  }

  public Pattern getSentencePattern() {
    return this.sentencePattern;
  }
}
