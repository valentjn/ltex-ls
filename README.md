<!--
   - Copyright (C) 2019-2023 Julian Valentin, LTeX Development Community
   -
   - This Source Code Form is subject to the terms of the Mozilla Public
   - License, v. 2.0. If a copy of the MPL was not distributed with this
   - file, You can obtain one at https://mozilla.org/MPL/2.0/.
   -->

# LT<sub>E</sub>X LS — LT<sub>E</sub>X Language Server

[![version number](https://badgen.net/github/release/valentjn/ltex-ls/stable)![release date](https://badgen.net/github/last-commit/valentjn/ltex-ls/release?label=)](https://github.com/valentjn/ltex-ls/releases)

[![vscode-ltex](https://badgen.net/github/license/valentjn/vscode-ltex?label=vscode-ltex)](https://github.com/valentjn/vscode-ltex)&nbsp;
[![CI status](https://github.com/valentjn/vscode-ltex/workflows/CI/badge.svg?branch=release)](https://github.com/valentjn/vscode-ltex/actions?query=workflow%3A%22CI%22+branch%3Arelease)&nbsp;
[![stars](https://badgen.net/github/stars/valentjn/vscode-ltex)](https://github.com/valentjn/vscode-ltex)&nbsp;
[![open issues](https://badgen.net/github/open-issues/valentjn/vscode-ltex?label=open/closed%20issues&color=blue)](https://github.com/valentjn/vscode-ltex/issues)&nbsp;[![closed issues](https://badgen.net/github/closed-issues/valentjn/vscode-ltex?label=)](https://github.com/valentjn/vscode-ltex/issues)\
[![ltex-ls](https://badgen.net/github/license/valentjn/ltex-ls?label=ltex-ls)](https://github.com/valentjn/ltex-ls)&nbsp;
[![CI status](https://github.com/valentjn/ltex-ls/workflows/CI/badge.svg?branch=release)](https://github.com/valentjn/ltex-ls/actions?query=workflow%3A%22CI%22+branch%3Arelease)&nbsp;
[![coverage](https://badgen.net/coveralls/c/github/valentjn/ltex-ls/release)](https://coveralls.io/github/valentjn/ltex-ls)&nbsp;
[![stars](https://badgen.net/github/stars/valentjn/ltex-ls)](https://github.com/valentjn/ltex-ls)&nbsp;
[![open issues](https://badgen.net/github/open-issues/valentjn/ltex-ls?label=open/closed%20issues&color=blue)](https://github.com/valentjn/ltex-ls/issues)&nbsp;[![closed issues](https://badgen.net/github/closed-issues/valentjn/ltex-ls?label=)](https://github.com/valentjn/ltex-ls/issues)

LT<sub>E</sub>X LS (LT<sub>E</sub>X Language Server) implements a language server according to the [Language Server Protocol (LSP)](https://microsoft.github.io/language-server-protocol/) and provides grammar and spelling errors in markup documents (L<sup>A</sup>T<sub>E</sub>X, Markdown, etc.). The documents are checked with [LanguageTool](https://languagetool.org/).

Typically, you start the language server (either locally or remotely), you send the language server your L<sup>A</sup>T<sub>E</sub>X or Markdown document, and it will respond with a list of the grammar and spelling errors in it. To use LT<sub>E</sub>X LS in this way, you have to use a language client (usually an editor or an extension of the editor) that communicates with LT<sub>E</sub>X LS according to the LSP.

However, it is also possible to supply LT<sub>E</sub>X LS paths to files and directories to be checked as command-line arguments. In this mode, LT<sub>E</sub>X LS will print the results to standard output, and no language client is necessary.

The reference language client of LT<sub>E</sub>X LS is the [LT<sub>E</sub>X extension for Visual Studio Code (vscode-ltex)](https://valentjn.github.io/ltex), whose development LT<sub>E</sub>X LS follows closely and vice versa.

Find more information (how to install, how to use, etc.) at the [website of LT<sub>E</sub>X](https://valentjn.github.io/ltex).

## Features

- **Supported markup languages:** BibT<sub>E</sub>X, ConT<sub>E</sub>Xt, Git commit messages, L<sup>A</sup>T<sub>E</sub>X, Markdown, Org, Quarto, reStructuredText, R Markdown, R Sweave, XHTML
- Comment checking in **many popular programming languages** (optional, opt-in)
- Comes with **everything included,** no need to install Java or LanguageTool
- **Offline checking:** Does not upload anything to the internet
- Supports **over 20 languages:** English, French, German, Dutch, Chinese, Russian, etc.
- **Replacement suggestions** via quick fixes
- **Completion support** for English and German
- **User dictionaries**
- **Multilingual support** with babel commands or magic comments
- Possibility to use **external LanguageTool servers**
- **[Extensive documentation](https://valentjn.github.io/ltex)**
