/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.settings;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.regex.Pattern;
import org.bsplines.ltexls.tools.Tools;
import org.checkerframework.checker.nullness.qual.Nullable;

public class HiddenFalsePositive {
  private final String ruleId;
  private final String sentenceString;
  private final Pattern sentencePattern;

  public HiddenFalsePositive(String ruleId, String sentenceString) {
    this.ruleId = ruleId;
    this.sentenceString = sentenceString;
    this.sentencePattern = Pattern.compile(sentenceString);
  }

  public HiddenFalsePositive(HiddenFalsePositive obj) {
    this(obj.ruleId, obj.sentenceString);
  }

  public static HiddenFalsePositive fromJsonString(String jsonString) {
    JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
    String ruleId = jsonObject.get("rule").getAsString();
    String sentenceString = jsonObject.get("sentence").getAsString();
    return new HiddenFalsePositive(ruleId, sentenceString);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if ((obj == null) || !HiddenFalsePositive.class.isAssignableFrom(obj.getClass())) return false;
    HiddenFalsePositive other = (HiddenFalsePositive)obj;

    if (!Tools.equals(this.ruleId, other.ruleId)) return false;
    if (!Tools.equals(this.sentenceString, other.sentenceString)) return false;

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
