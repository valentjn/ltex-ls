/* Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.markdown

object MarkdownAnnotatedTextBuilderDefaults {
  val defaultMarkdownNodeSignatures: List<MarkdownNodeSignature> = listOf(
    MarkdownNodeSignature("AutoLink", MarkdownNodeSignature.Action.Dummy),
    MarkdownNodeSignature("Code", MarkdownNodeSignature.Action.Dummy),
    MarkdownNodeSignature("CodeBlock"),
    MarkdownNodeSignature("FencedCodeBlock"),
    MarkdownNodeSignature("GitLabInlineMath", MarkdownNodeSignature.Action.Dummy),
    MarkdownNodeSignature("IndentedCodeBlock"),
    MarkdownNodeSignature("LtexMarkdownDisplayMath"),
    MarkdownNodeSignature("LtexMarkdownInlineMath", MarkdownNodeSignature.Action.Dummy),
    MarkdownNodeSignature("TableSeparator"),
  )
}
