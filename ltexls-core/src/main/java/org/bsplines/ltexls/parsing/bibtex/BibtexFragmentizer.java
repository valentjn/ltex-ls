/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.bibtex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bsplines.ltexls.parsing.CodeFragment;
import org.bsplines.ltexls.parsing.CodeFragmentizer;
import org.bsplines.ltexls.parsing.DummyGenerator;
import org.bsplines.ltexls.parsing.latex.LatexCommandSignature;
import org.bsplines.ltexls.parsing.latex.LatexCommandSignatureMatch;
import org.bsplines.ltexls.parsing.latex.LatexCommandSignatureMatcher;
import org.bsplines.ltexls.parsing.latex.LatexFragmentizer;
import org.bsplines.ltexls.parsing.latex.LatexPackageOption;
import org.bsplines.ltexls.parsing.latex.LatexPackageOptionsParser;
import org.bsplines.ltexls.settings.Settings;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BibtexFragmentizer extends CodeFragmentizer {
  private static final LatexCommandSignature bibtexEntryCommandSignature =
      new LatexCommandSignature("@[A-Za-z]+{}", LatexCommandSignature.Action.IGNORE,
        DummyGenerator.getDefault(), false);
  private static final LatexCommandSignatureMatcher bibtexEntryCommandSignatureMatcher =
      new LatexCommandSignatureMatcher(bibtexEntryCommandSignature, false);

  private LatexFragmentizer latexFragmentizer;

  public BibtexFragmentizer(String codeLanguageId) {
    super(codeLanguageId);
    this.latexFragmentizer = new LatexFragmentizer(codeLanguageId);
  }

  @Override
  public List<CodeFragment> fragmentize(String code, Settings originalSettings) {
    List<CodeFragment> fragments = Collections.singletonList(new CodeFragment(
          this.codeLanguageId, code, 0, originalSettings));

    fragments = this.latexFragmentizer.fragmentize(fragments);
    fragments = fragmentizeBibtexFields(fragments);

    return fragments;
  }

  private List<CodeFragment> fragmentizeBibtexFields(List<CodeFragment> fragments) {
    ArrayList<CodeFragment> newFragments = new ArrayList<>();

    for (CodeFragment oldFragment : fragments) {
      String oldFragmentCode = oldFragment.getCode();
      Settings oldFragmentSettings = oldFragment.getSettings();
      bibtexEntryCommandSignatureMatcher.startMatching(oldFragmentCode, Collections.emptySet());
      @Nullable LatexCommandSignatureMatch match = null;
      @Nullable Map<String, Boolean> bibtexFields = null;

      while ((match = bibtexEntryCommandSignatureMatcher.findNextMatch()) != null) {
        if (bibtexFields == null) {
          bibtexFields = new HashMap<>(BibtexFragmentizerDefaults.getDefaultBibtexFields());
          bibtexFields.putAll(oldFragmentSettings.getBibtexFields());
        }

        String argumentContents = match.getArgumentContents(match.getArgumentsSize() - 1);
        int argumentContentsFromPos = match.getArgumentContentsFromPos(
            match.getArgumentsSize() - 1);
        List<LatexPackageOption> keyValuePairs = LatexPackageOptionsParser.parse(argumentContents);

        for (LatexPackageOption keyValuePair : keyValuePairs) {
          String fieldName = keyValuePair.getKeyAsPlainText();

          if ((keyValuePair.getValueFromPos() == -1)
                || (bibtexFields.containsKey(fieldName) && !bibtexFields.get(fieldName))) {
            continue;
          }

          newFragments.add(new CodeFragment("latex", keyValuePair.getValue(),
              oldFragment.getFromPos() + argumentContentsFromPos + keyValuePair.getValueFromPos(),
              oldFragmentSettings));
        }
      }
    }

    return newFragments;
  }
}
