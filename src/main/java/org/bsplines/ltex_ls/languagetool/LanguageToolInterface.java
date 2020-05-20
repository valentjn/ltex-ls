package org.bsplines.ltex_ls.languagetool;

import java.util.List;

import org.languagetool.markup.AnnotatedText;

public abstract class LanguageToolInterface {
  public abstract boolean isReady();
  public abstract List<LanguageToolRuleMatch> check(AnnotatedText annotatedText);

  public abstract void activateDefaultFalseFriendRules();
  public abstract void activateLanguageModelRules(String languageModelRulesDirectory);
  public abstract void activateNeuralNetworkRules(String neuralNetworkRulesDirectory);
  public abstract void activateWord2VecModelRules(String word2vecRulesDirectory);
  public abstract void enableRules(List<String> ruleIds);
  public abstract void disableRules(List<String> ruleIds);
  public abstract void enableEasterEgg();
}
