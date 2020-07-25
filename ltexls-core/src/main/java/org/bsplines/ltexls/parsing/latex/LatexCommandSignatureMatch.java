/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.latex;

import java.util.List;
import org.eclipse.xtext.xbase.lib.Pair;

public class LatexCommandSignatureMatch {
  private LatexCommandSignature commandSignature;
  private String code;
  private int fromPos;
  private int toPos;
  private List<Pair<Integer, Integer>> argumentPos;

  public LatexCommandSignatureMatch(LatexCommandSignature commandSignature, String code,
        int fromPos, List<Pair<Integer, Integer>> argumentPos) {
    this.commandSignature = commandSignature;
    this.code = code;
    this.fromPos = fromPos;
    this.toPos = (argumentPos.isEmpty()
        ? (fromPos + commandSignature.getName().length())
        : argumentPos.get(argumentPos.size() - 1).getValue());
    this.argumentPos = argumentPos;
  }

  public LatexCommandSignature getCommandSignature() {
    return this.commandSignature;
  }

  public int getFromPos() {
    return this.fromPos;
  }

  public int getToPos() {
    return this.toPos;
  }

  public String getArgumentContents(int index) {
    Pair<Integer, Integer> argument = this.argumentPos.get(index);
    int argumentFromPos = argument.getKey() + 1;
    int argumentToPos = argument.getValue() - 1;
    return this.code.substring(argumentFromPos, argumentToPos);
  }

  public int getArgumentContentsFromPos(int index) {
    Pair<Integer, Integer> argument = this.argumentPos.get(index);
    return argument.getKey() + 1;
  }

  public int getArgumentsSize() {
    return this.argumentPos.size();
  }
}