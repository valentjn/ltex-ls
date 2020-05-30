package org.bsplines.ltex_ls.parsing.latex;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bsplines.ltex_ls.Settings;
import org.bsplines.ltex_ls.parsing.*;

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

  public LatexFragmentizer(String codeLanguageId, Settings originalSettings) {
    super(codeLanguageId, originalSettings);
    commentFragmentizer = new RegexCodeFragmentizer(
        codeLanguageId, originalSettings, commentPattern);
  }

  @Override
  public List<CodeFragment> fragmentize(String code) {
    List<CodeFragment> commentFragments = commentFragmentizer.fragmentize(code);
    ArrayList<CodeFragment> fragments = new ArrayList<>();

    for (CodeFragment commentFragment : commentFragments) {
      Matcher extraMatcher = extraCommandPattern.matcher(commentFragment.getCode());

      while (extraMatcher.find()) {
        int fromPos = commentFragment.getFromPos() + extraMatcher.start();
        List<Pair<Integer, Integer>> arguments = null;

        for (LatexCommandSignature extraCommandSignature : extraCommandSignatures) {
          arguments = extraCommandSignature.matchArgumentsFromPosition(code, fromPos);
          if (arguments != null) break;
        }

        if (arguments == null) continue;
        Pair<Integer, Integer> lastArgument = arguments.get(arguments.size() - 1);
        fromPos = lastArgument.getKey() + 1;
        int toPos = lastArgument.getValue() - 1;
        fragments.add(new CodeFragment(codeLanguageId, code.substring(fromPos, toPos), fromPos,
            commentFragment.getSettings()));
      }

      fragments.add(commentFragment);
    }

    return fragments;
  }
}
