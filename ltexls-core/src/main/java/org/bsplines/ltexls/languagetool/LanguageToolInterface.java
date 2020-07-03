/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.languagetool;

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
