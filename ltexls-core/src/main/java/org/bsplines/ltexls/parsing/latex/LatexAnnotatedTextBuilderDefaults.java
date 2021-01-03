/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.latex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.bsplines.ltexls.parsing.DummyGenerator;

class LatexAnnotatedTextBuilderDefaults {
  private static final List<LatexCommandSignature> defaultLatexCommandSignatures =
      createDefaultLatexCommandSignatures();
  private static final List<LatexEnvironmentSignature> defaultLatexEnvironmentSignatures =
      createDefaultLatexEnvironmentSignatures();

  private static List<LatexCommandSignature> createDefaultLatexCommandSignatures() {
    List<LatexCommandSignature> list = new ArrayList<>();

    list.add(new LatexCommandSignature("\\ac{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Ac{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\ac[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Ac[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\ac*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Ac*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\ac*[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Ac*[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\aca{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Aca{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\aca[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Aca[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\aca*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Aca*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\aca*[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Aca*[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\acap{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Acap{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\acap[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Acap[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\acap*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Acap*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\acap*[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Acap*[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\acf{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Acf{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\acf[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Acf[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\acf*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Acf*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\acf*[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Acf*[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\acfp{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Acfp{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\acfp[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Acfp[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\acfp*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Acfp*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\acfp*[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Acfp*[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\acl{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Acl{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\acl[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Acl[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\acl*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Acl*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\acl*[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Acl*[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\aclp{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Aclp{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\aclp[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Aclp[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\aclp*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Aclp*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\aclp*[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Aclp*[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\acp{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Acp{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\acp[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Acp[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\acp*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Acp*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\acp*[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Acp*[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\acreset{}"));
    list.add(new LatexCommandSignature("\\acresetall"));
    list.add(new LatexCommandSignature("\\acs{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Acs{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\acs[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Acs[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\acs*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Acs*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\acs*[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Acs*[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\acsetup{}"));
    list.add(new LatexCommandSignature("\\acsp{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Acsp{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\acsp[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Acsp[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\acsp*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Acsp*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\acsp*[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Acsp*[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\acuse{}"));
    list.add(new LatexCommandSignature("\\acuseall"));
    list.add(new LatexCommandSignature("\\addbibresource{}"));
    list.add(new LatexCommandSignature("\\addcontentsline{}{}{}"));
    list.add(new LatexCommandSignature("\\addtocontents{}"));
    list.add(new LatexCommandSignature("\\addtocounter{}{}"));
    list.add(new LatexCommandSignature("\\addtokomafont{}{}"));
    list.add(new LatexCommandSignature("\\@addtoreset{}{}"));
    list.add(new LatexCommandSignature("\\addtotheorempostheadhook{}"));
    list.add(new LatexCommandSignature("\\addxcontentsline{}{}{}"));
    list.add(new LatexCommandSignature("\\algdef{}[]{}{}"));
    list.add(new LatexCommandSignature("\\algnewcommand{}{}"));
    list.add(new LatexCommandSignature("\\algrenewcommand{}{}"));
    list.add(new LatexCommandSignature("\\arabic{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\AtBeginEnvironment{}{}"));
    list.add(new LatexCommandSignature("\\AtEndEnvironment{}{}"));
    list.add(new LatexCommandSignature("\\autocite{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\autocite[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\autocite[][]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Autocite{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Autocite[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Autocite[][]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\autocite*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\autocite*[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\autocite*[][]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Autocite*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Autocite*[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Autocite*[][]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\autocites{}", LatexCommandSignature.Action.DUMMY,
        DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\autocites{}{}", LatexCommandSignature.Action.DUMMY,
        DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\autocites{}{}{}", LatexCommandSignature.Action.DUMMY,
        DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\autocites{}{}{}{}", LatexCommandSignature.Action.DUMMY,
        DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\autocites{}{}{}{}{}", LatexCommandSignature.Action.DUMMY,
        DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\autocites()()[][]{}", LatexCommandSignature.Action.DUMMY,
        DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\autocites()()[][]{}[][]{}",
        LatexCommandSignature.Action.DUMMY, DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\autocites()()[][]{}[][]{}[][]{}",
        LatexCommandSignature.Action.DUMMY, DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\autocites()()[][]{}[][]{}[][]{}[][]{}",
        LatexCommandSignature.Action.DUMMY, DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\autocites()()[][]{}[][]{}[][]{}[][]{}[][]{}",
        LatexCommandSignature.Action.DUMMY, DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\Autocites{}", LatexCommandSignature.Action.DUMMY,
        DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\Autocites{}{}", LatexCommandSignature.Action.DUMMY,
        DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\Autocites{}{}{}", LatexCommandSignature.Action.DUMMY,
        DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\Autocites{}{}{}{}", LatexCommandSignature.Action.DUMMY,
        DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\Autocites{}{}{}{}{}", LatexCommandSignature.Action.DUMMY,
        DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\Autocites()()[][]{}", LatexCommandSignature.Action.DUMMY,
        DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\Autocites()()[][]{}[][]{}",
        LatexCommandSignature.Action.DUMMY, DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\Autocites()()[][]{}[][]{}[][]{}",
        LatexCommandSignature.Action.DUMMY, DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\Autocites()()[][]{}[][]{}[][]{}[][]{}",
        LatexCommandSignature.Action.DUMMY, DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\Autocites()()[][]{}[][]{}[][]{}[][]{}[][]{}",
        LatexCommandSignature.Action.DUMMY, DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\automark{}"));
    list.add(new LatexCommandSignature("\\automark[]{}"));
    list.add(new LatexCommandSignature("\\automark*{}"));
    list.add(new LatexCommandSignature("\\automark*[]{}"));
    list.add(new LatexCommandSignature("\\autopageref{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\autopageref*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\autoref{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\autoref*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\babeltags{}"));
    list.add(new LatexCommandSignature("\\bibitem{}"));
    list.add(new LatexCommandSignature("\\bibliography{}"));
    list.add(new LatexCommandSignature("\\bibliographystyle{}"));
    list.add(new LatexCommandSignature("\\captionof{}"));
    list.add(new LatexCommandSignature("\\captionsetup{}"));
    list.add(new LatexCommandSignature("\\captionsetup[]{}"));
    list.add(new LatexCommandSignature("\\cite{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\cite[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\cite[][]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Cite{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Cite[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Cite[][]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\cite*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\cite*[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\cite*[][]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\citealp{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\citealp[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\citealp*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\citealp*[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\citealt{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\citealt[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\citealt*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\citealt*[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\citeauthor{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\citeauthor[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\citeauthor[][]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Citeauthor{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Citeauthor[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Citeauthor[][]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\citeauthor*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\citeauthor*[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\citeauthor*[][]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Citeauthor*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Citeauthor*[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Citeauthor*[][]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\citedate{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\citedate[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\citedate[][]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\citedate*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\citedate*[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\citedate*[][]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\citep{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\citep[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\citep*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\citep*[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\cites{}", LatexCommandSignature.Action.DUMMY,
        DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\cites{}{}", LatexCommandSignature.Action.DUMMY,
        DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\cites{}{}{}", LatexCommandSignature.Action.DUMMY,
        DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\cites{}{}{}{}", LatexCommandSignature.Action.DUMMY,
        DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\cites{}{}{}{}{}", LatexCommandSignature.Action.DUMMY,
        DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\cites()()[][]{}", LatexCommandSignature.Action.DUMMY,
        DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\cites()()[][]{}[][]{}",
        LatexCommandSignature.Action.DUMMY, DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\cites()()[][]{}[][]{}[][]{}",
        LatexCommandSignature.Action.DUMMY, DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\cites()()[][]{}[][]{}[][]{}[][]{}",
        LatexCommandSignature.Action.DUMMY, DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\cites()()[][]{}[][]{}[][]{}[][]{}[][]{}",
        LatexCommandSignature.Action.DUMMY, DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\Cites{}", LatexCommandSignature.Action.DUMMY,
        DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\Cites{}{}", LatexCommandSignature.Action.DUMMY,
        DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\Cites{}{}{}", LatexCommandSignature.Action.DUMMY,
        DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\Cites{}{}{}{}", LatexCommandSignature.Action.DUMMY,
        DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\Cites{}{}{}{}{}", LatexCommandSignature.Action.DUMMY,
        DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\Cites()()[][]{}", LatexCommandSignature.Action.DUMMY,
        DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\Cites()()[][]{}[][]{}",
        LatexCommandSignature.Action.DUMMY, DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\Cites()()[][]{}[][]{}[][]{}",
        LatexCommandSignature.Action.DUMMY, DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\Cites()()[][]{}[][]{}[][]{}[][]{}",
        LatexCommandSignature.Action.DUMMY, DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\Cites()()[][]{}[][]{}[][]{}[][]{}[][]{}",
        LatexCommandSignature.Action.DUMMY, DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\citet{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\citet[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\citet*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\citet*[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\citetitle{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\citetitle[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\citetitle[][]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\citetitle*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\citetitle*[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\citetitle*[][]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\citeurl{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\citeurl[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\citeurl[][]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\citeyear{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\citeyear[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\citeyear[][]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\citeyear*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\citeyear*[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\citeyear*[][]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\clearfield{}"));
    list.add(new LatexCommandSignature("\\color{}"));
    list.add(new LatexCommandSignature("\\color[]{}"));
    list.add(new LatexCommandSignature("\\colorbox{}"));
    list.add(new LatexCommandSignature("\\colorlet{}{}"));
    list.add(new LatexCommandSignature("\\cref{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Cref{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\crefname{}{}{}"));
    list.add(new LatexCommandSignature("\\Crefname{}{}{}"));
    list.add(new LatexCommandSignature("\\DeclareAcronym{}{}"));
    list.add(new LatexCommandSignature("\\DeclareCaptionFormat{}{}"));
    list.add(new LatexCommandSignature("\\DeclareCaptionLabelFormat{}{}"));
    list.add(new LatexCommandSignature("\\DeclareCiteCommand{}{}{}{}{}"));
    list.add(new LatexCommandSignature("\\DeclareCiteCommand{}[]{}{}{}{}"));
    list.add(new LatexCommandSignature("\\DeclareFieldFormat{}{}"));
    list.add(new LatexCommandSignature("\\DeclareFieldFormat[]{}{}"));
    list.add(new LatexCommandSignature("\\DeclareFieldFormat*{}{}"));
    list.add(new LatexCommandSignature("\\DeclareGraphicsExtensions{}"));
    list.add(new LatexCommandSignature("\\DeclareMathAlphabet{}{}{}{}{}"));
    list.add(new LatexCommandSignature("\\DeclareMathOperator{}{}"));
    list.add(new LatexCommandSignature("\\DeclareMathOperator*{}{}"));
    list.add(new LatexCommandSignature("\\DeclareNameAlias{}{}"));
    list.add(new LatexCommandSignature("\\DeclareNewTOC{}"));
    list.add(new LatexCommandSignature("\\DeclareNewTOC[]{}"));
    list.add(new LatexCommandSignature("\\DeclareSIUnit{}{}"));
    list.add(new LatexCommandSignature("\\DeclareSIUnit[]{}{}"));
    list.add(new LatexCommandSignature("\\declaretheorem{}"));
    list.add(new LatexCommandSignature("\\declaretheorem[]{}"));
    list.add(new LatexCommandSignature("\\declaretheoremstyle{}"));
    list.add(new LatexCommandSignature("\\declaretheoremstyle[]{}"));
    list.add(new LatexCommandSignature("\\DeclareTOCStyleEntry{}"));
    list.add(new LatexCommandSignature("\\DeclareTOCStyleEntry[]{}{}"));
    list.add(new LatexCommandSignature("\\defbibcheck{}{}"));
    list.add(new LatexCommandSignature("\\defbibenvironment{}{}{}{}"));
    list.add(new LatexCommandSignature("\\defbibfilter{}{}"));
    list.add(new LatexCommandSignature("\\defbibheading{}{}"));
    list.add(new LatexCommandSignature("\\defbibheading{}[]{}"));
    list.add(new LatexCommandSignature("\\defbibnote{}{}"));
    list.add(new LatexCommandSignature("\\definecolor{}{}{}"));
    list.add(new LatexCommandSignature("\\definespotcolor{}{}{}"));
    list.add(new LatexCommandSignature("\\directlua{}"));
    list.add(new LatexCommandSignature("\\DisableLigatures{}"));
    list.add(new LatexCommandSignature("\\documentclass{}"));
    list.add(new LatexCommandSignature("\\documentclass[]{}"));
    list.add(new LatexCommandSignature("\\email{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\enlargethispage{}"));
    list.add(new LatexCommandSignature("\\eqref{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\etocsetlevel{}{}"));
    list.add(new LatexCommandSignature("\\etocsetnexttocdepth{}"));
    list.add(new LatexCommandSignature("\\etocsettocstyle{}{}"));
    list.add(new LatexCommandSignature("\\fcolorbox{}"));
    list.add(new LatexCommandSignature("\\floatname{}{}"));
    list.add(new LatexCommandSignature("\\floatstyle{}"));
    list.add(new LatexCommandSignature("\\footcite{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\footcite[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\footcite[][]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\footcitetext{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\footcitetext[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\footcitetext[][]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\footnote{}"));
    list.add(new LatexCommandSignature("\\footnote[]{}"));
    list.add(new LatexCommandSignature("\\foreignlanguage{}{}",
        LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\foreignlanguage[]{}{}",
        LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\GenericWarning{}{}"));
    list.add(new LatexCommandSignature("\\geometry{}"));
    list.add(new LatexCommandSignature("\\glsaddstoragekey{}{}{}"));
    list.add(new LatexCommandSignature("\\graphicspath{}"));
    list.add(new LatexCommandSignature("\\href{}{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\hyperref[]"));
    list.add(new LatexCommandSignature("\\hypersetup{}"));
    list.add(new LatexCommandSignature("\\iac{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Iac{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\iac[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Iac[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\iac*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Iac*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\iac*[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Iac*[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\iaca{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Iaca{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\iaca[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Iaca[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\iaca*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Iaca*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\iaca*[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Iaca*[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\iacf{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Iacf{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\iacf[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Iacf[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\iacf*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Iacf*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\iacf*[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Iacf*[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\iacl{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Iacl{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\iacl[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Iacl[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\iacl*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Iacl*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\iacl*[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Iacl*[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\iacs{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Iacs{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\iacs[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Iacs[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\iacs*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Iacs*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\iacs*[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Iacs*[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\ifcurrentfield{}"));
    list.add(new LatexCommandSignature("\\ifentrytype{}"));
    list.add(new LatexCommandSignature("\\@ifpackageloaded{}"));
    list.add(new LatexCommandSignature("\\iftoggle{}"));
    list.add(new LatexCommandSignature("\\include{}"));
    list.add(new LatexCommandSignature("\\includegraphics{}"));
    list.add(new LatexCommandSignature("\\includegraphics[]{}"));
    list.add(new LatexCommandSignature("\\includepdf{}"));
    list.add(new LatexCommandSignature("\\includepdf[]{}"));
    list.add(new LatexCommandSignature("\\input{}"));
    list.add(new LatexCommandSignature("\\KOMAoptions{}"));
    list.add(new LatexCommandSignature("\\KOMAScript", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\label{}"));
    list.add(new LatexCommandSignature("\\LaTeX", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\lettrine{}{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\lettrine[]{}{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\linespread{}"));
    list.add(new LatexCommandSignature("\\luadirect{}"));
    list.add(new LatexCommandSignature("\\luaexec{}"));
    list.add(new LatexCommandSignature("\\mdfdefinestyle{}{}"));
    list.add(new LatexCommandSignature("\\multicolumn{}{}"));
    list.add(new LatexCommandSignature("\\multirow{}{}"));
    list.add(new LatexCommandSignature("\\newboolean{}"));
    list.add(new LatexCommandSignature("\\newcolumntype{}{}"));
    list.add(new LatexCommandSignature("\\newcommand{}{}"));
    list.add(new LatexCommandSignature("\\newcommand{}[]{}"));
    list.add(new LatexCommandSignature("\\newcommand{}[][]{}"));
    list.add(new LatexCommandSignature("\\newcommand*{}{}"));
    list.add(new LatexCommandSignature("\\newcommand*{}[]{}"));
    list.add(new LatexCommandSignature("\\newcommand*{}[][]{}"));
    list.add(new LatexCommandSignature("\\newcounter{}"));
    list.add(new LatexCommandSignature("\\newenvironment{}{}{}"));
    list.add(new LatexCommandSignature("\\newenvironment{}[]{}{}"));
    list.add(new LatexCommandSignature("\\newenvironment*{}{}{}"));
    list.add(new LatexCommandSignature("\\newenvironment*{}[]{}{}"));
    list.add(new LatexCommandSignature("\\newfloat{}{}{}"));
    list.add(new LatexCommandSignature("\\newfloat{}{}{}[]"));
    list.add(new LatexCommandSignature("\\newgeometry{}"));
    list.add(new LatexCommandSignature("\\newglossaryentry{}{}"));
    list.add(new LatexCommandSignature("\\newglossarystyle{}{}"));
    list.add(new LatexCommandSignature("\\newrefcontext[]"));
    list.add(new LatexCommandSignature("\\newtheorem{}{}"));
    list.add(new LatexCommandSignature("\\newtheorem{}{}[]"));
    list.add(new LatexCommandSignature("\\newtheorem{}[]{}"));
    list.add(new LatexCommandSignature("\\newtheorem{}[]{}[]"));
    list.add(new LatexCommandSignature("\\newtheorem*{}{}"));
    list.add(new LatexCommandSignature("\\newtoggle{}"));
    list.add(new LatexCommandSignature("\\nolinkurl{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\PackageWarning{}{}"));
    list.add(new LatexCommandSignature("\\pagecolor{}"));
    list.add(new LatexCommandSignature("\\pagenumbering{}"));
    list.add(new LatexCommandSignature("\\pageref{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\pageref*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\pagestyle{}"));
    list.add(new LatexCommandSignature("\\parencite{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\parencite[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\parencite[][]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\parencite*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\parencite*[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\parencite*[][]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Parencite{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Parencite[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Parencite[][]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\pdfbookmark{}{}"));
    list.add(new LatexCommandSignature("\\pdfbookmark[]{}{}"));
    list.add(new LatexCommandSignature("\\pgfdeclaredecoration{}{}{}"));
    list.add(new LatexCommandSignature("\\pgfmathsetseed{}"));
    list.add(new LatexCommandSignature("\\printacronyms"));
    list.add(new LatexCommandSignature("\\printacronyms[]"));
    list.add(new LatexCommandSignature("\\printbibliography"));
    list.add(new LatexCommandSignature("\\printbibliography[]"));
    list.add(new LatexCommandSignature("\\printglossary"));
    list.add(new LatexCommandSignature("\\printglossary[]"));
    list.add(new LatexCommandSignature("\\providecommand{}{}"));
    list.add(new LatexCommandSignature("\\providecommand{}[]{}"));
    list.add(new LatexCommandSignature("\\providecommand*{}{}"));
    list.add(new LatexCommandSignature("\\providecommand*{}[]{}"));
    list.add(new LatexCommandSignature("\\raisebox{}"));
    list.add(new LatexCommandSignature("\\RedeclareSectionCommand{}"));
    list.add(new LatexCommandSignature("\\RedeclareSectionCommand[]{}"));
    list.add(new LatexCommandSignature("\\RedeclareSectionCommands{}"));
    list.add(new LatexCommandSignature("\\RedeclareSectionCommands[]{}"));
    list.add(new LatexCommandSignature("\\ref{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\ref*{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\renewbibmacro{}{}"));
    list.add(new LatexCommandSignature("\\renewbibmacro*{}{}"));
    list.add(new LatexCommandSignature("\\renewcommand{}{}"));
    list.add(new LatexCommandSignature("\\renewcommand{}[]{}"));
    list.add(new LatexCommandSignature("\\renewcommand{}[][]{}"));
    list.add(new LatexCommandSignature("\\renewcommand*{}{}"));
    list.add(new LatexCommandSignature("\\renewcommand*{}[]{}"));
    list.add(new LatexCommandSignature("\\renewcommand*{}[][]{}"));
    list.add(new LatexCommandSignature("\\renewenvironment{}{}{}"));
    list.add(new LatexCommandSignature("\\renewenvironment{}[]{}{}"));
    list.add(new LatexCommandSignature("\\renewenvironment*{}{}{}"));
    list.add(new LatexCommandSignature("\\renewenvironment*{}[]{}{}"));
    list.add(new LatexCommandSignature("\\renewlist{}{}{}"));
    list.add(new LatexCommandSignature("\\RequirePackage{}"));
    list.add(new LatexCommandSignature("\\scalebox{}"));
    list.add(new LatexCommandSignature("\\selectlanguage{}"));
    list.add(new LatexCommandSignature("\\setboolean{}"));
    list.add(new LatexCommandSignature("\\setcopyright{}"));
    list.add(new LatexCommandSignature("\\setcounter{}{}"));
    list.add(new LatexCommandSignature("\\setenumerate{}"));
    list.add(new LatexCommandSignature("\\setglossarystyle{}"));
    list.add(new LatexCommandSignature("\\setitemize{}"));
    list.add(new LatexCommandSignature("\\setkomafont{}{}"));
    list.add(new LatexCommandSignature("\\setkomavar{}{}"));
    list.add(new LatexCommandSignature("\\setkomavar{}[]{}"));
    list.add(new LatexCommandSignature("\\setkomavar*{}{}"));
    list.add(new LatexCommandSignature("\\setkomavar*{}[]{}"));
    list.add(new LatexCommandSignature("\\setlength{}{}"));
    list.add(new LatexCommandSignature("\\setlist{}"));
    list.add(new LatexCommandSignature("\\setlist[]{}"));
    list.add(new LatexCommandSignature("\\SetMathAlphabet{}{}{}{}{}{}"));
    list.add(new LatexCommandSignature("\\setparsizes{}{}{}"));
    list.add(new LatexCommandSignature("\\@setplength{}{}"));
    list.add(new LatexCommandSignature("\\setstretch{}"));
    list.add(new LatexCommandSignature("\\setuptoc{}{}"));
    list.add(new LatexCommandSignature("\\sisetup{}"));
    list.add(new LatexCommandSignature("\\smartcite{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\smartcite[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\smartcite[][]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Smartcite{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Smartcite[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Smartcite[][]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\stepcounter{}"));
    list.add(new LatexCommandSignature("\\supercite{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\SweaveInput{}"));
    list.add(new LatexCommandSignature("\\SweaveOpts{}"));
    list.add(new LatexCommandSignature("\\SweaveSyntax{}"));
    list.add(new LatexCommandSignature("\\TeX", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\textcite{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\textcite[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\textcite[][]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Textcite{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Textcite[]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\Textcite[][]{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\textcites{}", LatexCommandSignature.Action.DUMMY,
        DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\textcites{}{}", LatexCommandSignature.Action.DUMMY,
        DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\textcites{}{}{}", LatexCommandSignature.Action.DUMMY,
        DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\textcites{}{}{}{}", LatexCommandSignature.Action.DUMMY,
        DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\textcites{}{}{}{}{}", LatexCommandSignature.Action.DUMMY,
        DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\textcites()()[][]{}", LatexCommandSignature.Action.DUMMY,
        DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\textcites()()[][]{}[][]{}",
        LatexCommandSignature.Action.DUMMY, DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\textcites()()[][]{}[][]{}[][]{}",
        LatexCommandSignature.Action.DUMMY, DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\textcites()()[][]{}[][]{}[][]{}[][]{}",
        LatexCommandSignature.Action.DUMMY, DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\textcites()()[][]{}[][]{}[][]{}[][]{}[][]{}",
        LatexCommandSignature.Action.DUMMY, DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\Textcites{}", LatexCommandSignature.Action.DUMMY,
        DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\Textcites{}{}", LatexCommandSignature.Action.DUMMY,
        DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\Textcites{}{}{}", LatexCommandSignature.Action.DUMMY,
        DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\Textcites{}{}{}{}", LatexCommandSignature.Action.DUMMY,
        DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\Textcites{}{}{}{}{}", LatexCommandSignature.Action.DUMMY,
        DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\Textcites()()[][]{}", LatexCommandSignature.Action.DUMMY,
        DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\Textcites()()[][]{}[][]{}",
        LatexCommandSignature.Action.DUMMY, DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\Textcites()()[][]{}[][]{}[][]{}",
        LatexCommandSignature.Action.DUMMY, DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\Textcites()()[][]{}[][]{}[][]{}[][]{}",
        LatexCommandSignature.Action.DUMMY, DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\Textcites()()[][]{}[][]{}[][]{}[][]{}[][]{}",
        LatexCommandSignature.Action.DUMMY, DummyGenerator.getDefault(true)));
    list.add(new LatexCommandSignature("\\textcolor{}"));
    list.add(new LatexCommandSignature("\\textcolor[]{}"));
    list.add(new LatexCommandSignature("\\textproc{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\theoremstyle{}"));
    list.add(new LatexCommandSignature("\\thispagestyle{}"));
    list.add(new LatexCommandSignature("\\tikz{}"));
    list.add(new LatexCommandSignature("\\tikzset{}"));
    list.add(new LatexCommandSignature("\\todo{}"));
    list.add(new LatexCommandSignature("\\todo[]{}"));
    list.add(new LatexCommandSignature("\\togglefalse{}"));
    list.add(new LatexCommandSignature("\\toggletrue{}"));
    list.add(new LatexCommandSignature("\\unsettoc{}{}"));
    list.add(new LatexCommandSignature("\\url{}", LatexCommandSignature.Action.DUMMY));
    list.add(new LatexCommandSignature("\\usebibmacro{}"));
    list.add(new LatexCommandSignature("\\usekomafont{}"));
    list.add(new LatexCommandSignature("\\usepackage{}"));
    list.add(new LatexCommandSignature("\\usepackage[]{}"));
    list.add(new LatexCommandSignature("\\usetikzlibrary{}"));
    list.add(new LatexCommandSignature("\\value{}"));
    list.add(new LatexCommandSignature("\\vspace{}"));
    list.add(new LatexCommandSignature("\\vspace*{}"));
    list.add(new LatexCommandSignature("\\WarningFilter{}{}"));

    Map<String, String> babelLanguageMap = LatexFragmentizer.getBabelLanguageMap();

    for (String language : babelLanguageMap.keySet()) {
      String languageTag = LatexFragmentizer.convertBabelLanguageToLanguageTag(language);
      list.add(new LatexCommandSignature("\\text" + languageTag + "{}",
          LatexCommandSignature.Action.DUMMY));
    }

    return list;
  }

  private static List<LatexEnvironmentSignature> createDefaultLatexEnvironmentSignatures() {
    List<LatexEnvironmentSignature> list = new ArrayList<>();

    list.add(new LatexEnvironmentSignature("lstlisting"));
    list.add(new LatexEnvironmentSignature("otherlanguage"));
    list.add(new LatexEnvironmentSignature("otherlanguage*"));
    list.add(new LatexEnvironmentSignature("tikzpicture"));
    list.add(new LatexEnvironmentSignature("verbatim"));

    Map<String, String> babelLanguageMap = LatexFragmentizer.getBabelLanguageMap();

    for (String language : babelLanguageMap.keySet()) {
      String languageTag = LatexFragmentizer.convertBabelLanguageToLanguageTag(language);
      list.add(new LatexEnvironmentSignature(language));

      if (languageTag.length() != language.length()) {
        list.add(new LatexEnvironmentSignature(languageTag));
      }
    }

    return list;
  }

  private LatexAnnotatedTextBuilderDefaults() {
  }

  public static List<LatexCommandSignature> getDefaultLatexCommandSignatures() {
    return Collections.unmodifiableList(defaultLatexCommandSignatures);
  }

  public static List<LatexEnvironmentSignature> getDefaultLatexEnvironmentSignatures() {
    return Collections.unmodifiableList(defaultLatexEnvironmentSignatures);
  }
}
