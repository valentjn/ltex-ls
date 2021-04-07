/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.latex;

public class LatexPackageOption {
  private int keyFromPos;
  private int keyToPos;
  private String key;
  private String keyAsPlainText;
  private int valueFromPos;
  private int valueToPos;
  private String value;
  private String valueAsPlainText;

  public LatexPackageOption(String code, int keyFromPos, int keyToPos, String keyAsPlainText,
        int valueFromPos, int valueToPos, String valueAsPlainText) {
    this.keyFromPos = keyFromPos;
    this.keyToPos = keyToPos;
    this.key = code.substring(keyFromPos, keyToPos);
    this.keyAsPlainText = keyAsPlainText;
    this.valueFromPos = valueFromPos;
    this.valueToPos = valueToPos;
    this.value = (((valueFromPos != -1) && (valueToPos != -1))
        ? code.substring(valueFromPos, valueToPos) : "");
    this.valueAsPlainText = valueAsPlainText;
  }

  public int getKeyFromPos() {
    return this.keyFromPos;
  }

  public int getKeyToPos() {
    return this.keyToPos;
  }

  public String getKey() {
    return this.key;
  }

  public String getKeyAsPlainText() {
    return this.keyAsPlainText;
  }

  public int getValueFromPos() {
    return this.valueFromPos;
  }

  public int getValueToPos() {
    return this.valueToPos;
  }

  public String getValue() {
    return this.value;
  }

  public String getValueAsPlainText() {
    return this.valueAsPlainText;
  }
}
