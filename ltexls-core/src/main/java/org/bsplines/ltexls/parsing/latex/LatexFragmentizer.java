/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.latex;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bsplines.ltexls.Settings;
import org.bsplines.ltexls.parsing.CodeFragment;
import org.bsplines.ltexls.parsing.CodeFragmentizer;
import org.bsplines.ltexls.parsing.RegexCodeFragmentizer;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.xtext.xbase.lib.Pair;

public class LatexFragmentizer extends CodeFragmentizer {
  private static Pattern commentPattern = Pattern.compile(
      "^\\s*%\\s*(?i)ltex(?-i):(?<settings>.*?)$", Pattern.MULTILINE);
  private static LatexCommandSignature[] extraCommandSignatures = {
      new LatexCommandSignature("\\footnote{}"),
      new LatexCommandSignature("\\footnote[]{}"),
      new LatexCommandSignature("\\todo{}"),
      new LatexCommandSignature("\\todo[]{}"),
      };
  private static Pattern extraCommandPattern = Pattern.compile("\\\\(footnote|todo)[^A-Za-z]");

  private RegexCodeFragmentizer commentFragmentizer;

  /**
   * Constructor.
   *
   * @param codeLanguageId ID of the code language
   * @param originalSettings settings at the beginning of the document
   */
  public LatexFragmentizer(String codeLanguageId, Settings originalSettings) {
    super(codeLanguageId, originalSettings);
    this.commentFragmentizer = new RegexCodeFragmentizer(
        codeLanguageId, originalSettings, commentPattern);
  }

  @Override
  public List<CodeFragment> fragmentize(String code) {
    List<CodeFragment> commentFragments = this.commentFragmentizer.fragmentize(code);
    ArrayList<CodeFragment> fragments = new ArrayList<>();

    for (CodeFragment commentFragment : commentFragments) {
      Matcher extraMatcher = extraCommandPattern.matcher(commentFragment.getCode());

      while (extraMatcher.find()) {
        int fromPos = commentFragment.getFromPos() + extraMatcher.start();
        @Nullable List<Pair<Integer, Integer>> arguments = null;
        Settings fragmentSettings = commentFragment.getSettings();

        for (LatexCommandSignature extraCommandSignature : extraCommandSignatures) {
          if (fragmentSettings.getIgnoreCommandPrototypes().contains(
                extraCommandSignature.getCommandPrototype())) {
            continue;
          }

          arguments = extraCommandSignature.matchArgumentsFromPosition(code, fromPos);
          if (arguments != null) break;
        }

        if (arguments == null) continue;
        Pair<Integer, Integer> lastArgument = arguments.get(arguments.size() - 1);
        fromPos = lastArgument.getKey() + 1;
        int toPos = lastArgument.getValue() - 1;
        fragments.add(new CodeFragment(codeLanguageId, code.substring(fromPos, toPos), fromPos,
            fragmentSettings));
      }

      fragments.add(commentFragment);
    }

    return fragments;
  }
}
