package org.bsplines.ltexls;

import java.util.regex.Pattern;

import org.checkerframework.checker.nullness.qual.Nullable;

public class IgnoreRuleSentencePair {
  private final String ruleId;
  private final String sentenceString;
  private final Pattern sentencePattern;

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

    if ((ruleId == null) ? (other.ruleId != null) : !ruleId.equals(other.ruleId)) {
      return false;
    }

    if ((sentenceString == null) ? (other.sentenceString != null) :
          !sentenceString.equals(other.sentenceString)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hash = 3;

    hash = 53 * hash + ((ruleId != null) ? ruleId.hashCode() : 0);
    hash = 53 * hash + ((sentenceString != null) ? sentenceString.hashCode() : 0);

    return hash;
  }

  public String getRuleId() {
    return ruleId;
  }

  public String getSentenceString() {
    return sentenceString;
  }

  public Pattern getSentencePattern() {
    return sentencePattern;
  }
}
