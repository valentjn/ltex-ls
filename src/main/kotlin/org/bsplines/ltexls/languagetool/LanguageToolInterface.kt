/* Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.languagetool

import org.bsplines.ltexls.parsing.AnnotatedTextFragment

interface LanguageToolInterface {
  fun isInitialized(): Boolean
  fun check(annotatedTextFragment: AnnotatedTextFragment): List<LanguageToolRuleMatch>
  fun activateDefaultFalseFriendRules()
  fun activateLanguageModelRules(languageModelRulesDirectory: String)
  fun activateNeuralNetworkRules(neuralNetworkRulesDirectory: String)
  fun activateWord2VecModelRules(word2vecRulesDirectory: String)
  fun enableRules(ruleIds: Set<String>)
  fun disableRules(ruleIds: Set<String>)
  fun enableEasterEgg()
}
