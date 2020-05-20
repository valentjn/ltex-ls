package org.bsplines.ltex_ls.languagetool;

import java.util.ArrayList;
import java.util.List;

public class LanguageToolRuleMatch {
  private String ruleId;
  private String sentence;
  private int fromPos;
  private int toPos;
  private String message;
  private List<String> suggestedReplacements;

  public LanguageToolRuleMatch(String ruleId, String sentence, int fromPos, int toPos,
        String message, List<String> suggestedReplacements) {
    this.ruleId = ruleId;
    this.sentence = sentence;
    this.fromPos = fromPos;
    this.toPos = toPos;
    this.message = message;
    this.suggestedReplacements = new ArrayList<>(suggestedReplacements);
  }

  public String getRuleId() { return ruleId; }
  public String getSentence() { return sentence; }
  public int getFromPos() { return fromPos; }
  public int getToPos() { return toPos; }
  public String getMessage() { return message; }
  public List<String> getSuggestedReplacements() { return suggestedReplacements; }
}
