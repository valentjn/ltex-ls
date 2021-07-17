/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.languagetool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.bsplines.ltexls.parsing.AnnotatedTextFragment;
import org.bsplines.ltexls.server.LtexTextDocumentItem;
import org.bsplines.ltexls.tools.Tools;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.lsp4j.Range;
import org.languagetool.rules.RuleMatch;

public class LanguageToolRuleMatch {
  private static final Pattern twoOrMoreSpacesPattern = Pattern.compile("[ \n]{2,}");

  private @MonotonicNonNull String ruleId;
  private @MonotonicNonNull String sentence;
  private int fromPos;
  private int toPos;
  private String message;
  private List<String> suggestedReplacements;
  private RuleMatch.Type type;

  public LanguageToolRuleMatch(RuleMatch match, AnnotatedTextFragment annotatedTextFragment) {
    this(((match.getRule() != null) ? match.getRule().getId() : null),
        ((match.getSentence() != null) ? match.getSentence().getText() : null),
        match.getFromPos(), match.getToPos(), match.getMessage(), match.getSuggestedReplacements(),
        match.getType(), annotatedTextFragment);
  }

  public LanguageToolRuleMatch(@Nullable String ruleId, @Nullable String sentence,
        int fromPos, int toPos, String message, List<String> suggestedReplacements,
        RuleMatch.Type type, AnnotatedTextFragment annotatedTextFragment) {
    if (ruleId != null) this.ruleId = ruleId;
    if (sentence != null) this.sentence = sentence;
    this.fromPos = fromPos;
    this.toPos = toPos;
    this.message = message;
    this.suggestedReplacements = new ArrayList<>(suggestedReplacements);
    this.type = type;

    if (this.isUnknownWordRule()) {
      String unknownWord = annotatedTextFragment.getSubstringOfPlainText(fromPos, toPos);
      this.message = "'" + unknownWord + "': " + this.message;
    }

    this.message = twoOrMoreSpacesPattern.matcher(this.message).replaceAll(" ").trim();
  }

  public @Nullable String getRuleId() {
    return this.ruleId;
  }

  public @Nullable String getSentence() {
    return this.sentence;
  }

  public int getFromPos() {
    return this.fromPos;
  }

  public int getToPos() {
    return this.toPos;
  }

  public String getMessage() {
    return this.message;
  }

  public List<String> getSuggestedReplacements() {
    return Collections.unmodifiableList(this.suggestedReplacements);
  }

  public RuleMatch.Type getType() {
    return this.type;
  }

  public void setFromPos(int fromPos) {
    this.fromPos = fromPos;
  }

  public void setToPos(int toPos) {
    this.toPos = toPos;
  }

  public boolean isIntersectingWithRange(Range range, LtexTextDocumentItem document) {
    return Tools.areRangesIntersecting(new Range(document.convertPosition(this.fromPos),
        document.convertPosition(this.toPos)), range);
  }

  public boolean isUnknownWordRule(@UnknownInitialization LanguageToolRuleMatch this) {
    return ((this.ruleId != null) && (
        this.ruleId.startsWith("MORFOLOGIK_")
        || this.ruleId.startsWith("HUNSPELL_")
        || this.ruleId.startsWith("GERMAN_SPELLER_")
        || this.ruleId.equals("MUZSKY_ROD_NEZIV_A")
        || this.ruleId.equals("ZENSKY_ROD_A")
        || this.ruleId.equals("STREDNY_ROD_A")
        || this.ruleId.equals("FR_SPELLING_RULE")));
  }
}
