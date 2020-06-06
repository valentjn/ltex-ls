package org.bsplines.ltex_ls.languagetool;

import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class LanguageToolRuleMatch {
  private @MonotonicNonNull String ruleId;
  private @MonotonicNonNull String sentence;
  private int fromPos;
  private int toPos;
  private String message;
  private List<String> suggestedReplacements;

  public LanguageToolRuleMatch(@Nullable String ruleId, @Nullable String sentence,
        int fromPos, int toPos, String message, List<String> suggestedReplacements) {
    if (ruleId != null) this.ruleId = ruleId;
    if (sentence != null) this.sentence = sentence;
    this.fromPos = fromPos;
    this.toPos = toPos;
    this.message = message;
    this.suggestedReplacements = new ArrayList<>(suggestedReplacements);
  }

  public @Nullable String getRuleId() { return ruleId; }
  public @Nullable String getSentence() { return sentence; }
  public int getFromPos() { return fromPos; }
  public int getToPos() { return toPos; }
  public String getMessage() { return message; }
  public List<String> getSuggestedReplacements() { return suggestedReplacements; }

  public void setFromPos(int fromPos) { this.fromPos = fromPos; }
  public void setToPos(int toPos) { this.toPos = toPos; }
}
