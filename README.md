<!--
   - Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
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

The reference language client of LT<sub>E</sub>X LS is the [LT<sub>E</sub>X extension for Visual Studio Code (vscode-ltex)](https://valentjn.github.io/vscode-ltex), whose development LT<sub>E</sub>X LS follows closely and vice versa.

Find more information about LT<sub>E</sub>X at the [website of vscode-ltex](https://valentjn.github.io/vscode-ltex).

## Features

- **Supported markup languages:** BibT<sub>E</sub>X, L<sup>A</sup>T<sub>E</sub>X, Markdown, Org, reStructuredText, R Sweave, XHTML
- Comment checking in **many popular programming languages** (optional, opt-in)
- Comes with **everything included,** no need to install Java or LanguageTool
- **Offline checking:** Does not upload anything to the internet
- Supports **over 20 languages:** English, French, German, Dutch, Chinese, Russian, etc.
- **Replacement suggestions** via quick fixes
- **User dictionaries**
- **Multilingual support** with babel commands or magic comments
- Possibility to use **external LanguageTool servers**
- **Extensive [documentation](https://valentjn.github.io/vscode-ltex) of reference client**

## Current List of Language Clients

In order to use LT<sub>E</sub>X LS with an editor, it is recommended to use a language client. For some editors, language clients are already available, see the following list. If your editor is in the list, read the installation instructions of the language client first; it might download LT<sub>E</sub>X LS automatically or tell you where to store LT<sub>E</sub>X LS. The rest of this document is only relevant if you want to implement your own language client, or if you want to use LT<sub>E</sub>X LS standalone as a file-based checker.

- VS Code/reference client: [LT<sub>E</sub>X for VS Code (valentjn/vscode-ltex)](https://valentjn.github.io/vscode-ltex)
- Emacs using `eglot`: [emacs-languagetool/eglot-ltex](https://github.com/emacs-languagetool/eglot-ltex)
- Emacs using `lsp-mode`: [emacs-languagetool/lsp-ltex](https://github.com/emacs-languagetool/lsp-ltex)
- Neovim using `nvim-lspconfig`: [lbiaggi/ltex.lua](https://gist.github.com/lbiaggi/a3eb761ac2fdbff774b29c88844355b8)
- Sublime Text: [LDAP/LSP-ltex-ls](https://github.com/LDAP/LSP-ltex-ls)

## Requirements

- 64-bit operating system
- If you want to use LT<sub>E</sub>X LS with a language client: Language client supporting LSP 3.15 or later

## Installation

1. Download the [latest release](https://github.com/valentjn/ltex-ls/releases/latest) from GitHub.
   - It's recommended that you choose the archive corresponding to your platform (these archives are standalone, no Java installation necessary).
   - If you choose the platform-independent file `ltex-ls-VERSION.tar.gz`, then you need Java 11 or later on your computer.
2. Extract the archive to an arbitrary location on your computer.

## Startup

It is recommended to use the startup scripts `bin/ltex-ls` (Linux, Mac) and `bin\ltex-ls.bat` (Windows) to start LT<sub>E</sub>X LS. These scripts are only part of the released versions. The startup scripts can be controlled by the following environment variables:

- `JAVA_HOME`: Path to the directory of the JRE or JDK to use (contains `bin`, `lib`, and other subdirectories). If set, this overrides the included Java distribution when using a platform-dependent LT<sub>E</sub>X LS archive.
- `JAVA_OPTS`: Java arguments to be fed to `java` (e.g., `-Xmx1024m`)

It is also possible to start LT<sub>E</sub>X LS directly without the startup scripts (not recommended).

### Command-Line Arguments

Any command-line arguments supplied to the startup scripts are processed by LT<sub>E</sub>X LS itself. The possible arguments are as follows:

- `--[no-]endless`: Keep the server alive when the client terminates the connection to allow reuse by the next client.
- `-h`, `--help`: Show help message and exit.
- `--host=<host>`: Listen for TCP connections on host `<host>` (IP address or hostname; default is `localhost`). Only relevant if server type is `tcpSocket`.
- `--input-documents=<path> <path> ...`: Instead of running as server, check the documents at the given paths, print the results to standard output, and exit. Directories are traversed recursively. If `-` is given, standard input will be checked as plain text.
- `--log-file=<logFile>`: Tee server/client communication and server log to `<logFile>`. `${PID}` is replaced by the process ID of LT<sub>E</sub>X LS. The parent directory of `<logFile>` must exist. If `<logFile>` is an existing directory, then `ltex-ls-${PID}.log` is used as filename.
- `--port=<port>`: Listen for TCP connections on port `<port>`. Only relevant if server type is `tcpSocket`. A value of `0` (default) will have the system automatically determine a free port (the actual port number will be printed to the log).
- `--server-type=<serverType>`: Run the server as type `<serverType>`. Valid values are:
  - `standardStream` (default): Communicate with clients over standard input and standard output.
  - `tcpSocket`: Communicate with clients over a TCP socket.
- `--settings-file=<settingsFile>`: Use the settings stored in the JSON file `<settingsFile>`. Only relevant when using `--input-documents`. The format is either nested JSON objects (`{"latex": {"commands": ...}}`) or a flattened JSON object (`{"latex.commands": ...}`). Setting names may be prefixed by a top level named `ltex` (e.g., `{"ltex.latex.commands": ...}` is accepted as well).
- `-V`, `--version`: Print version information as JSON to the standard output and exit. The format is a JSON object with `"java"` and `"ltex-ls"` keys and string values. A key may be missing if no information about the corresponding version could be retrieved.

Instead of using the equals sign `=` to separate option names and values, it is also possible to use one or more spaces.

### Exit Codes

- 0: LT<sub>E</sub>X LS exited successfully. When using `--input-documents`: No grammar/spelling errors were found.
- 1: An exception was thrown during the execution of LT<sub>E</sub>X LS.
- 2: An invalid command-line argument was supplied to LT<sub>E</sub>X LS.
- 3: When using `--input-documents`: At least one grammar/spelling error was found.

## Checking Documents with the LSP

Once started, the language server may be used according to the [Language Server Protocol (LSP)](https://microsoft.github.io/language-server-protocol/) to check documents.

Communication with the server is by default via standard input and standard output (except when the server type is `tcpSocket`). Logging messages are always printed to the standard error output.

## Settings

See the website of vscode-ltex for a [list of all supported settings](https://valentjn.github.io/vscode-ltex/docs/settings.html).

Note that some settings listed on the linked page are client-specific and do not affect LT<sub>E</sub>X LS.

## Quick fixes

- `quickfix.ltex.acceptSuggestions`: Replace the text of the diagnostic with the specified suggestion.
- `quickfix.ltex.addToDictionary`: Trigger the `_ltex.addToDictionary` command.
- `quickfix.ltex.disableRules`: Trigger the `_ltex.disableRules` command.
- `quickfix.ltex.hideFalsePositives`: Trigger the `_ltex.hideFalsePositives` command.

## Commands

Some commands are handled by LT<sub>E</sub>X LS, while others must be handled by the language client. This is in contrast to the [LSP specification](https://microsoft.github.io/language-server-protocol/specification), which recommends that the server handles all commands. However, handling of some commands by the client is necessary as these commands change the client configuration, which the LSP does not allow server-side.

All commands are prefixed with `_ltex.` during usage. The purpose of the leading underscore is that in some clients, commands are directly exposed to the user of the client (e.g., for assigning keyboard shortcuts), which is not desirable for internal commands that require arguments. The leading underscore signals that the commands are internal.

As arguments, all commands take an array with exactly one element, whose type is specified by the respective `CommandParams` interface.

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

### `_ltex.addToDictionary` (Client)

`_ltex.addToDictionary` is executed by the client when it should add words to the dictionary by adding them to [`ltex.dictionary`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexdictionary).

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

### `_ltex.disableRules` (Client)

`_ltex.disableRules` is executed by the client when it should disable rules by adding the rule IDs to [`ltex.disabledRules`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexdisabledrules).

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

### `_ltex.hideFalsePositives` (Client)

`_ltex.hideFalsePositives` is executed by the client when it should hide false positives by adding them to [`ltex.hiddenFalsePositives`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexhiddenfalsepositives).

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

### `_ltex.checkDocument` (Server)

`_ltex.checkDocument` is executed by the server to trigger the check of a specific document. The result will be sent to the client with a `textDocument/publishDiagnostics` notification.

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

### `_ltex.getServerStatus` (Server)

`_ltex.getServerStatus` is executed by the server to return information about the current resource consumption of LT<sub>E</sub>X LS. Some information might not be available.

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

  /**
   * Whether LTeX LS is currently busy checking text.
   */
  isChecking: boolean;

  /**
   * URI of the document currently being checked.
   * This field may still be missing even if `isChecking` is true.
   */
  documentUriBeingChecked?: string;
}
```

## Custom LSP Extensions

LT<sub>E</sub>X LS supports the following custom features that are not specified by the LSP:

- Custom initialization options
- Custom requests and notifications

To use custom LSP extensions, the client has to pass a `CustomInitializationOptions` object to the `InitializeParams.initializationOptions` field when sending the [`initialize`](https://microsoft.github.io/language-server-protocol/specification#initialize) request. If no such object is passed, LT<sub>E</sub>X LS will fall back to an LSP-compliant mode and not use any custom LSP extensions.

### Custom Initialization Options

Custom initialization options are directly specified in the fields of a `CustomInitializationOptions` object defined as follows:

```typescript
interface CustomInitializationOptions {
  /**
   * Possibility to supply the locale of the client, if LSP 3.15 is used.
   * For LSP 3.16 and later, use `InitializeParams.locale` instead.
   * @deprecated This will be removed once LTeX LS requires LSP 3.16.
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
