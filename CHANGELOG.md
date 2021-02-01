<!--
   - Copyright (C) 2020 Julian Valentin, LTeX Development Community
   -
   - This Source Code Form is subject to the terms of the Mozilla Public
   - License, v. 2.0. If a copy of the MPL was not distributed with this
   - file, You can obtain one at https://mozilla.org/MPL/2.0/.
   -->

# Changelog

## 10.0.0 (upcoming)

- Remove support for settings that are deprecated since 8.0.0: `ltex.ignoreInRuleSentence`, `ltex.commands.ignore`, `ltex.commands.dummy`, `ltex.environments.ignore`, `ltex.markdown.ignore`, and `ltex.markdown.dummy`
- Add support for magic comments inside HTML comments in Markdown (`<!-- ltex: SETTINGS -->`)
- Check documents even if their code language is not supported
- Fix removing items in settings with a hyphen prefix sometimes not working
- Fix manually checking BibT<sub>E</sub>X documents not working

## 9.2.0 (January 29, 2021)

- Add support for Pandoc-style inline math (`$...$`) and display math (`$$...$$` with `$$` being at the beginning/end of a Markdown block) to Markdown parser (fixes [vscode-ltex#210](https://github.com/valentjn/vscode-ltex/issues/210))
- Fix false positives for words added by `Add to dictionary` for Slovak rule IDs `MUZSKY_ROD_NEZIV_A`, `ZENSKY_ROD_A`, and `STREDNY_ROD_A` (fixes [vscode-ltex#221](https://github.com/valentjn/vscode-ltex/issues/221))
- Fix BibT<sub>E</sub>X field `seealso` not ignored, ignore `category` and `parent` (see [vscode-ltex#211](https://github.com/valentjn/vscode-ltex/issues/211))
- Disable `UPPERCASE_SENTENCE_START` in BibT<sub>E</sub>X files (see [vscode-ltex#211](https://github.com/valentjn/vscode-ltex/issues/211))
- Move rule ID to the end of diagnostic messages as VS Code truncates the messages if the Problems panel is narrow (fixes [vscode-ltex#233](https://github.com/valentjn/vscode-ltex/issues/233))
- Fix regression that messages of possible spelling mistakes are not prepended with the respective unknown words (see [vscode-ltex#161](https://github.com/valentjn/vscode-ltex/issues/161))
- Fix crash when using `\begin` or `\end` without an argument (fixes [vscode-ltex#236](https://github.com/valentjn/vscode-ltex/issues/236))
- Change `$/progress` tokens to include a UUID instead of a counter

## 9.1.0 (January 24, 2021)

- Add support for BibT<sub>E</sub>X files (language code `bibtex`, fixes [vscode-ltex#211](https://github.com/valentjn/vscode-ltex/issues/211))
- Add setting [`ltex.bibtex.fields`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexbibtexfields) to control which BibT<sub>E</sub>X fields should be checked
- Add support for [GitLab Flavored Markdown](https://gitlab.com/gitlab-org/gitlab/blob/master/doc/user/markdown.md), especially inline math (e.g., ``$`E = mc^2`$``, see [vscode-ltex#210](https://github.com/valentjn/vscode-ltex/issues/210))
- Add support for Markdown tables as in [GitHub Flavored Markdown](https://github.github.com/gfm/#tables-extension-) (fixes [vscode-ltex#218](https://github.com/valentjn/vscode-ltex/issues/218))
- Add support for more commands of the `glossaries` L<sup>A</sup>T<sub>E</sub>X package
- Enable `Add to dictionary` quick fix for Slovak rule IDs `MUZSKY_ROD_NEZIV_A`, `ZENSKY_ROD_A`, and `STREDNY_ROD_A` (fixes [vscode-ltex#221](https://github.com/valentjn/vscode-ltex/issues/221))
- Remove superfluous spaces in messages of diagnostics
- Fix handling of `\r\n` (Windows) line terminators in Markdown
- Use Flexmark's YAML Front Matter extension to ignore YAML front matter in Markdown instead of own handling
- Print Flexmark AST of Markdown documents to log when [`ltex.ltex-ls.logLevel`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexltex-lsloglevel) is `"finest"`

## 9.0.1 (January 13, 2021)

- Ignore `\pgfmathsetmacro`, `\setmainfont`, and `\theoremstyle`
- Fix accent commands such as `\O` in math mode resulting in diagnostics (fixes [vscode-ltex#216](https://github.com/valentjn/vscode-ltex/issues/216))

## 9.0.0 (January 3, 2021)

- Make versioning independent of vscode-ltex; LT<sub>E</sub>X LS now adheres to [Semantic Versioning 2.0.0](https://semver.org/spec/v2.0.0.html)
- Require support of LSP 3.15
- Update LanguageTool to 5.2 (see [LT 5.2 release notes](https://github.com/languagetool-org/languagetool/blob/v5.2/languagetool-standalone/CHANGES.md#52-released-2020-12-29))
- Add [`ltex.additionalRules.enablePickyRules`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexadditionalrulesenablepickyrules) to still be able to detect false friends after the update of LanguageTool (default: `false`)
- Replace `ltex/progress` with `$/progress` (fixes [#34](https://github.com/valentjn/ltex-ls/issues/34))
- Add `customCapabilities` in `InitializeParams.initializationOptions`
- Remove unneeded command arguments `type` and `command`
- Replace `\dots` with Unicode ellipsis `…` instead of three dots `...` to fix some false positives
- Add documentation

## 8.1.1 (November 24, 2020)

- Migrate from Travis CI to GitHub Actions

## 8.1.0 (November 15, 2020)

- Prepend messages of possible spelling mistakes with the respective unknown words (fixes [vscode-ltex#161](https://github.com/valentjn/vscode-ltex/issues/161))
- Add support for optional arguments of `\newtheorem`
- Fix wrong position of diagnostics when using a recognized L<sup>A</sup>T<sub>E</sub>X command with a non-recognized set of arguments due to an infinite loop (fixes [vscode-ltex#167](https://github.com/valentjn/vscode-ltex/issues/167))
- Update LSP4J to 0.10.0

## 8.0.0 (November 1, 2020)

- Upgrade from Java 8 to Java 11 (see [announcement](https://valentjn.github.io/vscode-ltex/docs/deprecation-of-java-8.html), fixes [vscode-ltex#39](https://github.com/valentjn/vscode-ltex/issues/39))
- Add workaround to eliminate the need for workspace-specific setting names; [`ltex.dictionary`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexdictionary), [`ltex.disabledRules`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexdisabledrules), and [`ltex.enabledRules`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexenabledrules) can now be used in multiple setting scopes (user settings, workspace settings, and workspace folder settings) at the same time without overriding each other; instead, the settings of the different scopes will be properly merged (see [documentation](https://valentjn.github.io/vscode-ltex/docs/advanced-usage.html#multi-scope-settings))
- Rename settings:
  - `ltex.workspaceDictionary`, `ltex.workspaceFolderDictionary` → [`ltex.dictionary`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexdictionary)
  - `ltex.workspaceDisabledRules`, `ltex.workspaceFolderDisabledRules` → [`ltex.disabledRules`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexdisabledrules)
  - `ltex.workspaceEnabledRules`, `ltex.workspaceFolderEnabledRules` → [`ltex.enabledRules`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexenabledrules)
  - `ltex.ignoreInRuleSentence` → [`ltex.hiddenFalsePositives`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexhiddenfalsepositives)
  - `ltex.commands.ignore`, `ltex.commands.dummy` → [`ltex.latex.commands`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexlatexcommands)
  - `ltex.environments.ignore` → [`ltex.latex.environments`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexlatexenvironments)
  - `ltex.markdown.ignore`, `ltex.markdown.dummy` → [`ltex.markdown.nodes`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexmarkdownnodes)
- Change format of [`ltex.latex.commands`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexlatexcommands), [`ltex.latex.environments`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexlatexenvironments), [`ltex.markdown.nodes`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexmarkdownnodes) to be objects (with key = command and value = action, e.g., `"ignore"`, `"dummy"`, etc.) instead of arrays
- Rename object keys of [`ltex.configurationTarget`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexconfigurationtarget):
  - `addToDictionary` → `dictionary`
  - `disableRule` → `disabledRules`
  - `ignoreRuleInSentence` → `hiddenFalsePositives`
- Add `userExternalFile`, `workspaceExternalFile`, and `workspaceFolderExternalFile` enumeration values to [`ltex.configurationTarget`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexconfigurationtarget), which enables saving settings to external files (see [documentation](https://valentjn.github.io/vscode-ltex/docs/advanced-usage.html#external-setting-files), fixes [vscode-ltex#144](https://github.com/valentjn/vscode-ltex/issues/144) and [vscode-ltex#145](https://github.com/valentjn/vscode-ltex/issues/145))
- Change default of [`ltex.configurationTarget`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexconfigurationtarget) for `dictionary`, `disabledRules`, and `hiddenFalsePositives` to `workspaceFolderExternalFile`
- Add [`ltex.checkFrequency`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexcheckfrequency) to control when LT<sub>E</sub>X checks documents (fixes [vscode-ltex#142](https://github.com/valentjn/vscode-ltex/issues/142))
- Add [`LTeX: Show status information`](https://valentjn.github.io/vscode-ltex/docs/commands.html#ltex-show-status-information) command to show information about the status of LT<sub>E</sub>X
- Add support for `\usepackage[LANGUAGE]{babel}` if in the same file as the text to be checked (fixes [vscode-ltex#140](https://github.com/valentjn/vscode-ltex/issues/140))
- Add support for more BibL<sup>A</sup>T<sub>E</sub>X commands such as `\autocite`, `\citeauthor`, etc. (fixes [vscode-ltex#143](https://github.com/valentjn/vscode-ltex/issues/143))
- Add support for overriding hard-coded command signatures (fixes [valentjn/ltex-ls#27](https://github.com/valentjn/ltex-ls/issues/27))
- Move handling of external setting files from ltex-ls to vscode-ltex
- Increase duration before sentences expire in the result cache to 60 minutes
- Fix many settings changes cleared sentence cache, which led to performance issues, e.g., changing the [`ltex.enabled`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexenabled) setting via magic comments (see [vscode-ltex#134](https://github.com/valentjn/vscode-ltex/issues/134))
- Remove dependency on `org.apache.httpcomponents:httpclient` by using the HTTP client that comes with Java 11 when connecting to an HTTP LanguageTool server

## 7.3.1 (October 12, 2020)

- Fix delayed publication of diagnostics by adding workaround to guess the caret position
- Fix recheck being triggered when generating list of quick fixes; this should improve speed

## 7.3.0 (October 10, 2020)

- Add support for `\ell` as well as `\mathcal`, `\mathfrak`, etc. to vowel detection (fixes [vscode-ltex#131](https://github.com/valentjn/vscode-ltex/issues/131))
- Add setting [`ltex.ltex-ls.logLevel`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexltex-lsloglevel) to control the verbosity of the server log
- Fix diagnostics sometimes not lined up with the text with switching back from incremental to full document updates; unfortunately, this disables the delayed publication of diagnostics at the caret position
- Restructure and simplify internal quickfix and command structure, removing the need for pseudo-telemetry notifications

## 7.2.0 (September 27, 2020)

- Update LanguageTool to 5.1 (see [LT 5.1 release notes](https://github.com/languagetool-org/languagetool/blob/v5.1/languagetool-standalone/CHANGES.md#51-released-2020-09-25))
- Add support for HTML entities such as `&auml;` and `&copy;` in Markdown
- Fix missing tilde expansion for external dictionary files
- Improve logging

## 7.1.2 (September 22, 2020)

- Fix performance issue with multiple languages in one document via magic comments due to LanguageTool being reinitialized on each keystroke (fixes [vscode-ltex#124](https://github.com/valentjn/vscode-ltex/issues/124))

## 7.1.1 (September 20, 2020)

- Fix `NullPointerException` when supplying relative paths to external dictionary files
- Fix German log messages

## 7.1.0 (September 20, 2020)

- Add support for external dictionary files (fixes [vscode-ltex#118](https://github.com/valentjn/vscode-ltex/issues/118))
- Add support for enabling/disabling LT<sub>E</sub>X only for specific file types via [`ltex.enabled`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexenabled) (see [vscode-ltex#19](https://github.com/valentjn/vscode-ltex/issues/19))
- Add support for `acro` commands such as `\DeclareAcronym` and `\ac` (see [vscode-ltex#19](https://github.com/valentjn/vscode-ltex/issues/19))
- Add support for `\addcontentsline` (see [vscode-ltex#19](https://github.com/valentjn/vscode-ltex/issues/19))
- Add support for `\printbibliography` and `\printglossary` without argument
- Ignore parenthesis arguments of `textblock`s (see [vscode-ltex#19](https://github.com/valentjn/vscode-ltex/issues/19))
- Fix optional argument of heading commands such as `\section` parsed incorrectly (fixes [vscode-ltex#123](https://github.com/valentjn/vscode-ltex/issues/123))
- Include stack traces when logging exceptions

## 7.0.0 (September 13, 2020)

- Change scope of [`ltex.dictionary`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexdictionary), [`ltex.disabledRules`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexdisabledrules), and [`ltex.enabledRules`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexenabledrules) to `application`; these are now user-specific settings that can only be configured in user settings
- Add settings `ltex.workspaceDictionary`, `ltex.workspaceDisabledRules`, and `ltex.workspaceEnabledRules` with `window` scope to amend the corresponding user-specific settings; these are workspace-specific settings that should be configured in workspace settings
- Add settings `ltex.workspaceFolderDictionary`, `ltex.workspaceFolderDisabledRules`, and `ltex.workspaceFolderEnabledRules` with `resource` scope to amend the corresponding user-specific and workspace-specific settings; these are workspace-folder-specific settings that should be configured in workspace folder settings
- Rename `"global"` value for [`ltex.configurationTarget`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexconfigurationtarget) to `"user"` (`"global"` is still supported, but deprecated)
- Remove deprecated settings `ltex.javaHome`, `ltex.performance.initialJavaHeapSize`, `ltex.performance.maximumJavaHeapSize`, `ltex.performance.sentenceCacheSize`, `ltex.*.dictionary`, `ltex.*.enabledRules`, and `ltex.*.disabledRules` (deprecation since 5.0.0)
- Update LanguageTool to 5.0.2 (see [LT 5.0.2 release notes](https://github.com/languagetool-org/languagetool/blob/v5.0.2/languagetool-standalone/CHANGES.md#502-2020-08-28))
- Fix skipping of YAML front matter (fixes [vscode-ltex#104](https://github.com/valentjn/vscode-ltex/issues/104))

## 6.3.0 (August 22, 2020)

- Add support for `an` article when before a formula starting with a vowel (e.g., `an $n$-dimensional problem`, fixes [vscode-ltex#92](https://github.com/valentjn/vscode-ltex/issues/92))
- Add support for `~/` and `~\` in settings (fixes [vscode-ltex#99](https://github.com/valentjn/vscode-ltex/issues/99))

## 6.2.0 (August 7, 2020)

- Add commands [`LTeX: Check current document`](https://valentjn.github.io/vscode-ltex/docs/commands.html#ltex-check-current-document) and [`LTeX: Check all documents in workspace`](https://valentjn.github.io/vscode-ltex/docs/commands.html#ltex-check-all-documents-in-workspace) (fixes [vscode-ltex#84](https://github.com/valentjn/vscode-ltex/issues/84))
- Add setting [`ltex.clearDiagnosticsWhenClosingFile`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexcleardiagnosticswhenclosingfile)
- Skip front matter in Markdown
- Ignore more L<sup>A</sup>T<sub>E</sub>X preamble commands (e.g., `\automark`, `\color`, `\DeclareSIUnit`, `\directlua`, `\setuptoc`)
- Add support for German babel hyphenation commands `"-`, `""`, `"|`, `"=`, `"~`
- Use non-breaking space for `~`

## 6.1.1 (July 26, 2020)

- Fix another problem with spaces in paths on Windows (fixes [vscode-ltex#80](https://github.com/valentjn/vscode-ltex/issues/80))

## 6.1.0 (July 26, 2020)

- LT<sub>E</sub>X LS support for Java 8 will end on November 1, 2020 (see [documentation](https://valentjn.github.io/vscode-ltex/docs/deprecation-of-java-8.html) and [vscode-ltex#39](https://github.com/valentjn/vscode-ltex/issues/39))
- Add support for babel commands (see [documentation](https://valentjn.github.io/vscode-ltex/docs/advanced-usage.html#multilingual-latex-documents-with-the-babel-package), fixes [vscode-ltex#81](https://github.com/valentjn/vscode-ltex/issues/81))
- Fix problems with spaces in paths on Windows (fixes [vscode-ltex#80](https://github.com/valentjn/vscode-ltex/issues/80))

## 6.0.2 (July 11, 2020)

- Make Windows startup script (`ltex-ls.bat`) honor `JAVA_HOME` (fixes [vscode-ltex#75](https://github.com/valentjn/vscode-ltex/issues/75))

## 6.0.1 (July 2, 2020)

- Fix freezes when checking German text by working around [languagetool-org/languagetool#3181](https://github.com/languagetool-org/languagetool/issues/3181) introduced by LanguageTool 5.0 (fixes [vscode-ltex#68](https://github.com/valentjn/vscode-ltex/issues/68))

## 6.0.0 (June 28, 2020)

- Update LanguageTool to 5.0 (see [LT 5.0 release notes](https://github.com/languagetool-org/languagetool/blob/v5.0/languagetool-standalone/CHANGES.md#50-2020-06-27))
- Delay diagnostics at the current caret position (e.g., incomplete word or sentence) until the user has finished typing (fixes [vscode-ltex#46](https://github.com/valentjn/vscode-ltex/issues/46))
- Add `enabled` to magic comments (fixes [vscode-ltex#67](https://github.com/valentjn/vscode-ltex/issues/67))
- Fix `\todo` couldn't be ignored (fixes [vscode-ltex#63](https://github.com/valentjn/vscode-ltex/issues/63))
- Fix wrong language-dependent settings used for magic comments
- Fix add to dictionary and disable rule quick fixes using wrong language when used with magic comments
- Improve code quality by fixing hundreds of Checkstyle, SpotBugs, and Checker Framework warnings
- Migrate from Gradle to Maven
- Update Maven dependencies

## 5.0.0 (June 1, 2020)

- Include all languages in LT<sub>E</sub>X LS; this removes the need for language support extensions (fixes [vscode-ltex#6](https://github.com/valentjn/vscode-ltex/issues/6))
- Adhere to [semantic versioning](https://semver.org/). This means that the version of LT<sub>E</sub>X LS is not tied to the version of LanguageTool anymore, as the version of LanguageTool is not a semantic version. LT<sub>E</sub>X LS 5.0.0 uses LanguageTool 4.9.
- Rename settings:
  - `ltex.<LANGUAGE>.dictionary` to [`ltex.dictionary`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexdictionary) (object with `<LANGUAGE>` keys)
  - `ltex.<LANGUAGE>.disabledRules` to [`ltex.disabledRules`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexdisabledrules) (object with `<LANGUAGE>` keys)
  - `ltex.<LANGUAGE>.enabledRules` to [`ltex.enabledRules`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexenabledrules) (object with `<LANGUAGE>` keys)
  - `ltex.javaHome` to [`ltex.java.path`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexjavapath)
  - `ltex.performance.initialJavaHeapSize` to [`ltex.java.initialHeapSize`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexjavainitialheapsize)
  - `ltex.performance.maximumJavaHeapSize` to [`ltex.java.maximumHeapSize`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexjavamaximumheapsize)
  - `ltex.performance.sentenceCacheSize` to [`ltex.sentenceCacheSize`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexsentencecachesize)
- Add support for external LanguageTool HTTP servers (fixes [vscode-ltex#36](https://github.com/valentjn/vscode-ltex/issues/36))
- Add support for magic comments, enables changing the language in the middle of documents (fixes [vscode-ltex#21](https://github.com/valentjn/vscode-ltex/issues/21))
- Check `\footnote` and `\todo` contents separately, preventing “double period” warnings (fixes [vscode-ltex#42](https://github.com/valentjn/vscode-ltex/issues/42))
- Add support for more BibL<sup>A</sup>T<sub>E</sub>X citation commands, add support for plural dummies, add support for `\eg`, `\egc`, `\ie`, `\iec` (fixes [vscode-ltex#43](https://github.com/valentjn/vscode-ltex/issues/43))
- Add visual feedback in status bar during startup and checks that take a long time
- Remove `null` types and default values from settings, use empty string/array/object instead (fixes [vscode-ltex#41](https://github.com/valentjn/vscode-ltex/issues/41))
- Use proper server/client model for language server/client
- Make documentation of vscode-ltex more extensive, put it on own [website](https://valentjn.github.io/vscode-ltex/)

## 4.9.3 (May 7, 2020)

- Revert back to Java 8
- Remove support for external LanguageTool HTTP servers

## 4.9.2 (May 6, 2020)

- Update required version of Java (now 11 or newer)
- Add support for external LanguageTool HTTP servers (fixes [vscode-ltex#36](https://github.com/valentjn/vscode-ltex/issues/36))
- Add support for `\autoref`, `\pageref`, `\autopageref` (fixes [vscode-ltex#37](https://github.com/valentjn/vscode-ltex/issues/37))

## 4.9.1 (May 1, 2020)

- Fix sentence cache was invalidated when a single ignore sentence rule was present (fixes [vscode-ltex#29](https://github.com/valentjn/vscode-ltex/issues/29))
- Use thin non-breaking space for `\,` (fixes [vscode-ltex#35](https://github.com/valentjn/vscode-ltex/issues/35))

## 4.9.0 (March 28, 2020)

- Update to LanguageTool 4.9 (see [LT 4.9 release notes](https://github.com/languagetool-org/languagetool/blob/v4.9/languagetool-standalone/CHANGES.md#49-2020-03-24))
- Update other Java dependencies
- Reduce file size (omitting unneeded dependencies)

## 4.7.10 (March 12, 2020)

- Fix spelling errors for French dummies (fixes [vscode-ltex#27](https://github.com/valentjn/vscode-ltex/issues/27))
- Fix `\dots` in math mode being interpreted as `...`

## 4.7.9 (February 29, 2020)

- Update the Markdown parser flexmark-java to 0.60.2; this increases the speed of parsing Markdown
- Add possibility to ignore Markdown elements or replace them by dummy words via `ltex.markdown.ignore` and `ltex.markdown.dummy` (fixes [vscode-ltex#26](https://github.com/valentjn/vscode-ltex/issues/26))
- Ignore Markdown code blocks by default
- Replace auto-links and inline Markdown code with dummy words by default
- Fix match positions were sometimes off by one, especially in Markdown documents
- Rewrite `MarkdownAnnotatedTextBuilder`

## 4.7.8 (February 16, 2020)

- Add support for R Sweave `.rnw` files (fixes [vscode-ltex#22](https://github.com/valentjn/vscode-ltex/issues/22))
- Enable fixing multiple diagnostics at once (fixes [vscode-ltex#23](https://github.com/valentjn/vscode-ltex/issues/23))
- Add support for `\euro` (fixes [vscode-ltex#25](https://github.com/valentjn/vscode-ltex/issues/25))

## 4.7.7 (November 23, 2019)

- No changes, dummy release

## 4.7.6 (November 10, 2019)

- Add `ltex.performance` settings to give users more control over Java's RAM usage
- Change default initial Java heap size to 64 MB
- Change default maximum Java heap size to 512 MB
- Change default sentence cache size from 10000 to 2000 sentences

## 4.7.5 (October 22, 2019)

- Enable ignoring environments such as `lstlisting` and `verbatim`
- Add `ltex.environments.ignore` setting for defining own environments to ignore

## 4.7.4 (October 15, 2019)

- Add `disabledRules` and `enabledRules` settings (requires update of language extensions)
- Add `disable rule` quick fix
- Fix a bug where the `codeAction` request gets stuck in infinite loop
- Fix another `NullPointerException` for word2vec

## 4.7.3 (October 7, 2019)

- Fix null pointer error for word2vec quick fixes (fixes [vscode-ltex#12](https://github.com/valentjn/vscode-ltex/issues/12))

## 4.7.2 (October 2, 2019)

- Add missing error message if legacy false friends could not be loaded

## 4.7.1 (October 2, 2019)

- Add [`ltex.additionalRules.motherTongue`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexadditionalrulesmothertongue) setting to enable detection of false friends (fixes [vscode-ltex#11](https://github.com/valentjn/vscode-ltex/issues/11))
- Change defaults for `ltex.additionalRules` settings from `""` to `null`

## 4.7.0 (October 1, 2019)

- Update to LanguageTool 4.7 (see [LT 4.7 release notes](https://github.com/languagetool-org/languagetool/blob/v4.7/languagetool-standalone/CHANGES.md#47-2019-09-28))
- Support multi-root workspaces, all configuration settings except [`ltex.enabled`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexenabled) are now resource-specific (fixes [vscode-ltex#7](https://github.com/valentjn/vscode-ltex/issues/7))
- Save dictionary settings under full language short code (e.g., `en-US` instead of `en`). If you already have a dictionary under `ltex.en.dictionary` and use `en-US` as language (not `en`), you have to rename the settings name to `ltex.en-US.dictionary` (similarly for other languages).
- Remove diagnostics when a file is closed
- Prevent insertion of text in TikZ mode
- Add support for more commands such as `\newenvironment`, `\newgeometry`, and `\pagenumbering`

## 4.6.13 (September 26, 2019)

- Fix LT<sub>E</sub>X LS not reinitialized after a language extension has been installed (which was missing during initialization)

## 4.6.12 (September 25, 2019)

- Patch LanguageTool's `AnnotatedText` with linear interpolation to hopefully fix the `fromPos must be less than toPos` LT errors for good
- Fix `\footnote` in math mode messed up text mode and math mode
- Increase robustness in case locale or settings are not provided
- Ignore all brace and bracket arguments after `\begin{environment}` (`tabular`, `array`, etc.)
- Add support for some more commands and environments such as `\pagestyle` and `eqnarray`

## 4.6.11 (September 23, 2019)

- Detect and prevent infinite loops in `LatexAnnotatedTextBuilder`
- Fix infinite loop with other line endings than `\n`
- Fix some more `fromPos must be less than toPos` LT errors
- Check for interrupts to avoid 100% CPU usage on timeout (this doesn't fix any bugs though)
- Add support for `\email`, `\href`, and `\verb|...|`
- Add support for more citation commands (`\citep`, `\citet`, etc.)
- Add support for float/theorem definition commands and starred sectioning commands

## 4.6.10 (September 18, 2019)

- Fix `NullPointerException` if LanguageTool has not been initialized (fixes [#1](https://github.com/valentjn/ltex-ls/issues/1))

## 4.6.9 (September 8, 2019)

- Fix `NullPointerException` in `main`

## 4.6.8 (September 7, 2019)

- Initial release
