<!--
   - Copyright (C) 2020 Julian Valentin, LTeX Development Community
   -
   - This Source Code Form is subject to the terms of the Mozilla Public
   - License, v. 2.0. If a copy of the MPL was not distributed with this
   - file, You can obtain one at https://mozilla.org/MPL/2.0/.
   -->

# LT<sub>E</sub>X LS — LT<sub>E</sub>X Language Server

[![version number](https://badgen.net/github/release/valentjn/ltex-ls/stable)![release date](https://badgen.net/github/last-commit/valentjn/ltex-ls/release?label=)](https://github.com/valentjn/ltex-ls/releases)

<span style="opacity:0.3;">[![vscode-ltex](https://badgen.net/github/license/valentjn/vscode-ltex?label=vscode-ltex)](https://github.com/valentjn/vscode-ltex)&nbsp;
[![CI status](https://github.com/valentjn/vscode-ltex/workflows/CI/badge.svg?branch=release)](https://github.com/valentjn/vscode-ltex/actions?query=workflow%3A%22CI%22+branch%3Arelease)&nbsp;
[![stars](https://badgen.net/github/stars/valentjn/vscode-ltex)](https://github.com/valentjn/vscode-ltex)&nbsp;
[![open issues](https://badgen.net/github/open-issues/valentjn/vscode-ltex?label=open/closed%20issues&color=blue)](https://github.com/valentjn/vscode-ltex/issues)&nbsp;[![closed issues](https://badgen.net/github/closed-issues/valentjn/vscode-ltex?label=)](https://github.com/valentjn/vscode-ltex/issues)</span>\
[![ltex-ls](https://badgen.net/github/license/valentjn/ltex-ls?label=ltex-ls)](https://github.com/valentjn/ltex-ls)&nbsp;
[![CI status](https://github.com/valentjn/ltex-ls/workflows/CI/badge.svg?branch=release)](https://github.com/valentjn/ltex-ls/actions?query=workflow%3A%22CI%22+branch%3Arelease)&nbsp;
[![coverage](https://badgen.net/coveralls/c/github/valentjn/ltex-ls/release)](https://coveralls.io/github/valentjn/ltex-ls)&nbsp;
[![stars](https://badgen.net/github/stars/valentjn/ltex-ls)](https://github.com/valentjn/ltex-ls)&nbsp;
[![open issues](https://badgen.net/github/open-issues/valentjn/ltex-ls?label=open/closed%20issues&color=blue)](https://github.com/valentjn/ltex-ls/issues)&nbsp;[![closed issues](https://badgen.net/github/closed-issues/valentjn/ltex-ls?label=)](https://github.com/valentjn/ltex-ls/issues)

LT<sub>E</sub>X LS (LT<sub>E</sub>X Language Server) implements a language server according to the [Language Server Protocol (LSP)](https://microsoft.github.io/language-server-protocol/) and provides grammar and spelling errors in L<sup>A</sup>T<sub>E</sub>X and Markdown documents. The documents are checked with [LanguageTool](https://languagetool.org/).

Simply put, you start the language server (either locally or remotely), you send the language server your L<sup>A</sup>T<sub>E</sub>X or Markdown document, and it will respond with a list of the grammar and spelling errors in it (if there are any). To use LT<sub>E</sub>X LS, you have to use a language client (usually an editor or an extension of the editor) that communicates with LT<sub>E</sub>X LS according to the LSP.

The reference client of LT<sub>E</sub>X LS is the [LT<sub>E</sub>X extension for Visual Studio Code (vscode-ltex)](https://valentjn.github.io/vscode-ltex), whose development LT<sub>E</sub>X LS follows closely and vice versa.

Find more information about LT<sub>E</sub>X at the [website of vscode-ltex](https://valentjn.github.io/vscode-ltex).

## Current List of Language Clients

In order to use LT<sub>E</sub>X LS, you need a language client. For some editors, language clients are already available, see the following list. If your editor is in the list, read the installation instructions of the language client first; it might download LT<sub>E</sub>X LS automatically or tell you where to store LT<sub>E</sub>X LS. The rest of this document is only relevant if you want to implement your own language client.

- VS Code/reference client: [LT<sub>E</sub>X for VS Code (valentjn/vscode-ltex)](https://valentjn.github.io/vscode-ltex)
- Emacs using `eglot`: [emacs-languagetool/eglot-ltex](https://github.com/emacs-languagetool/eglot-ltex)
- Emacs using `lsp-mode`: [emacs-languagetool/lsp-ltex](https://github.com/emacs-languagetool/lsp-ltex)
- Sublime Text: [LDAP/LSP-ltex-ls](https://github.com/LDAP/LSP-ltex-ls)

## Requirements

- 64-bit operating system
- Java 11 or later
- Language client supporting LSP 3.15 or later

## Installation

1. Download the [latest release](https://github.com/valentjn/ltex-ls/releases/latest) from GitHub.
2. Extract the archive to an arbitrary location on your computer.

## Startup

It is recommended to use the startup scripts `bin/ltex-ls` (Linux, Mac) and `bin\ltex-ls.bat` (Windows) to start LT<sub>E</sub>X LS. These scripts are only part of the released versions. The startup scripts can be controlled by the following environment variables:

- `JAVA_HOME`: Path to the directory of the JRE or JDK to use (contains `bin`, `lib`, and other subdirectories)
- `JAVA_OPTS`: Java arguments to be fed to `java`
- Any command-line arguments supplied to the startup scripts are fed to LT<sub>E</sub>X LS. The following arguments are supported:
  - `--version`: Print a JSON string with versioning information to the standard output and exit. The format is a JSON object with `"java"` and `"ltex-ls"` keys and string values. A key may be missing if no information about the corresponding version could be retrieved.

You can also start LT<sub>E</sub>X LS directly without the startup scripts (not recommended). In this case, make sure that when supplying all JAR files in the `lib` directory to Java's class path, `ltexls-languagetool-patch-LTEXLSVERSION.jar` is listed as the first JAR file (where `LTEXLSVERSION` is the version of LT<sub>E</sub>X LS; Java's class path does not support wildcards in the middle of filenames). It does not suffice to supply `lib/*` to the class path, as the order in which the JAR files are included in wildcards is not specified by Java, so this will fail randomly. The startup scripts take care of this peculiarity.

## Checking Documents with the LSP

Once started, the language server may be used according to the [Language Server Protocol (LSP)](https://microsoft.github.io/language-server-protocol/) to check documents.

Communicate with the server via standard input and standard output. Logging messages are printed to the standard error output.

## Settings

See the website of vscode-ltex for a [list of all supported settings](https://valentjn.github.io/vscode-ltex/docs/settings.html).

Note that some settings listed on the linked page are client-specific and do not affect LT<sub>E</sub>X LS.

## Quickfixes

- `quickfix.ltex.acceptSuggestions`: Replace the text of the diagnostic with the specified suggestion.
- `quickfix.ltex.addToDictionary`: Trigger the `ltex.addToDictionary` command.
- `quickfix.ltex.disableRules`: Trigger the `ltex.disableRules` command.
- `quickfix.ltex.hideFalsePositives`: Trigger the `ltex.hideFalsePositives` command.

## Commands

Some commands are handled by LT<sub>E</sub>X LS, while others must be handled by the language client. This is in contrast to the [LSP specification](https://microsoft.github.io/language-server-protocol/specification), which recommends that the server handles all commands. However, handling of some commands by the client is necessary as these commands change the client configuration, which the LSP does not allow server-side.

All commands are prefixed with `ltex.` during usage. As arguments, all commands take an array with exactly one element, whose type is specified by the respective `CommandParams` interface.

The result of all commands handled by the client is `null`.

The result of all commands handled by the server implements at least the following interface:

```typescript
interface ServerCommandResult {
  /**
   * Whether the command was executed successfully.
   */
  success: boolean;

  /**
   * Optional error message if `success` is `false`.
   */
  errorMessage?: string;
}
```

### `ltex.addToDictionary` (Client)

`ltex.addToDictionary` is executed by the client when it should add words to the dictionary by adding them to [`ltex.dictionary`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexdictionary).

```typescript
interface AddToDictionaryCommandParams {
  /**
   * URI of the document.
   */
  uri: string;

  /**
   * Words to add to the dictionary, specified as lists of strings by language.
   */
  words: {
    [language: string]: string[];
  };
}

type AddToDictionaryCommandResult = null;
```

### `ltex.disableRules` (Client)

`ltex.disableRules` is executed by the client when it should disable rules by adding the rule IDs to [`ltex.disabledRules`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexdisabledrules).

```typescript
interface DisableRulesCommandParams {
  /**
   * URI of the document.
   */
  uri: string;

  /**
   * IDs of the LanguageTool rules to disable, specified as lists of strings by language.
   */
  ruleIds: {
    [language: string]: string[];
  };
}

type DisableRulesCommandResult = null;
```

### `ltex.hideFalsePositives` (Client)

`ltex.hideFalsePositives` is executed by the client when it should hide false positives by adding them to [`ltex.hiddenFalsePositives`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexhiddenfalsepositives).

```typescript
interface HideFalsePositivesCommandParams {
  /**
   * URI of the document.
   */
  uri: string;

  /**
   * False positives to hide, specified as lists of JSON strings by language.
   */
  falsePositives: {
    [language: string]: string[];
  };
}

type HideFalsePositivesCommandResult = null;
```

### `ltex.checkDocument` (Server)

`ltex.checkDocument` is executed by the server to trigger the check of a specific document. The result will be sent to the client with a `textDocument/publishDiagnostics` notification.

```typescript
interface CheckDocumentCommandParams {
  /**
   * URI of the document.
   */
  uri: string;

  /**
   * Code language ID of the document (e.g., `latex`). Will be determined by the file extension
   * of `uri` if missing.
   */
  codeLanguageId?: string;

  /**
   * Text to check. Will be determined as the contents of the file at `uri` if missing.
   */
  text?: string;

  /**
   * Range inside `text` (or the contents of the file at `uri` if missing) if only a part
   * of the document should be checked. Will be set to the range spanning all of `text` if missing.
   */
  range?: Range;
}

type CheckDocumentCommandResult = ServerCommandResult;
```

### `ltex.getServerStatus` (Server)

`ltex.getServerStatus` is executed by the server to return information about the current resource consumption of LT<sub>E</sub>X LS. Some information might not be available. Executions of `ltex.getServerStatus` can only be handled if LT<sub>E</sub>X LS is not currently busy (e.g., checking a document).

```typescript
type GetServerStatusCommandParams = null;

interface GetServerStatusCommandResult extends ServerCommandResult {
  /**
   * Process ID of the Java process.
   */
  processId: number;

  /**
   * Wall-clock duration in seconds since the start of LTeX LS.
   */
  wallClockDuration: number;

  /**
   * Current CPU usage as a fraction between 0 and 1.
   */
  cpuUsage?: number;

  /**
   * Duration in seconds during which the CPU was occupied.
   */
  cpuDuration?: number;

  /**
   * Memory in bytes of all currently allocated Java objects (this is a part of `totalMemory`).
   */
  usedMemory: number;

  /**
   * Total memory in bytes currently taken by the JVM.
   */
  totalMemory: number;
}
```

## Custom LSP Extensions

LT<sub>E</sub>X LS supports the following custom features that are not specified by the LSP:

- Custom initialization options
- Custom requests and notifications

To use custom LSP extensions, the client has to pass a `CustomInitializationOptions` object to the `InitializeParams.initializationOptions` field when sending the [`initialize`](https://microsoft.github.io/language-server-protocol/specification#initialize) request. If no such object is passed, LT<sub>E</sub>X LS will fallback to an LSP-compliant mode and not use any custom LSP extensions.

### Custom Initialization Options

Custom initialization options are directly specified in the fields of a `CustomInitializationOptions` object defined as follows:

```typescript
interface CustomInitializationOptions {
  /**
   * Locale of UI messages (strings that are sent to the client and that are meant to be
   * displayed in the user interface of the client), specified as an IETF BCP 47 language tag.
   *
   * Logging messages are always in English. Diagnostic messages are always in the language of
   * the checked text they belong to.
   *
   * If not specified, the default locale of the JVM will be used. If the specified locale
   * cannot be resolved to a supported i18n language of LTeX LS, then English will be used.
   */
  locale?: string;

  /**
   * Capabilities for custom requests and notifications.
   */
  customCapabilities?: CustomCapabilities;
}

interface CustomCapabilities {
  /**
   * Whether `ltex/workspaceSpecificConfiguration` requests are supported.
   */
  workspaceSpecificConfiguration?: boolean;
}
```

### Custom Requests and Notifications

Support for custom server-to-client requests and notifications has to be announced by setting the value that corresponds to the name of the custom request or notification in `CustomInitializationOptions.customCapabilities` to `true`. Client-to-server requests and notifications are always enabled.

All custom requests and notifications are prefixed with `ltex/` during usage.

#### `ltex/workspaceSpecificConfiguration` (⮎)

`ltex/workspaceSpecificConfiguration` is a server-to-client request. Parameters and result are exactly like [`workspace/configuration`](https://microsoft.github.io/language-server-protocol/specification#workspace_configuration) (i.e., `ConfigurationParams` and `any[]`, respectively).

If enabled, LT<sub>E</sub>X LS will not only send a [`workspace/configuration`](https://microsoft.github.io/language-server-protocol/specification#workspace_configuration) request to the client every time a document is checked, but also an `ltex/workspaceSpecificConfiguration` request. Some settings used for the check are then taken from the result of the `ltex/workspaceSpecificConfiguration` request instead of the [`workspace/configuration`](https://microsoft.github.io/language-server-protocol/specification#workspace_configuration) request. These settings are:

- [`ltex.dictionary`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexdictionary)
- [`ltex.disabledRules`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexdisabledrules)
- [`ltex.enabledRules`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexenabledrules)
- [`ltex.hiddenFalsePositives`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexhiddenfalsepositives)

The reason of the existence of `ltex/workspaceSpecificConfiguration` is that some clients like VS Code have different configuration scopes (e.g., user and workspace). When a configuration like [`ltex.dictionary`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexdictionary) appears in multiple scopes, the value in the scope with the higher precedence will override the other values (e.g., workspace scope will override user scope). `ltex/workspaceSpecificConfiguration` makes it possible for the client to implement a merging mechanism instead, without having to change [`workspace/configuration`](https://microsoft.github.io/language-server-protocol/specification#workspace_configuration).
