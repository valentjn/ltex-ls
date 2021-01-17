<!--
   - Copyright (C) 2020 Julian Valentin, LTeX Development Community
   -
   - This Source Code Form is subject to the terms of the Mozilla Public
   - License, v. 2.0. If a copy of the MPL was not distributed with this
   - file, You can obtain one at https://mozilla.org/MPL/2.0/.
   -->

# LT<sub>E</sub>X LS — LT<sub>E</sub>X Language Server

LT<sub>E</sub>X LS implements a language server according to the [Language Server Protocol (LSP)](https://microsoft.github.io/language-server-protocol/) and provides grammar and spelling errors in L<sup>A</sup>T<sub>E</sub>X and Markdown documents. The documents are checked with [LanguageTool](https://languagetool.org/).

Simply put, you start the language server (either locally or remotely), you send the language server your L<sup>A</sup>T<sub>E</sub>X or Markdown document, and it will respond with a list of the grammar and spelling errors in it (if there are any). To communicate with a language server, you have to implement a language client according to the Language Server Protocol.

The reference client of LT<sub>E</sub>X LS is the [LT<sub>E</sub>X extension for Visual Studio Code (vscode-ltex)](https://valentjn.github.io/vscode-ltex), whose development LT<sub>E</sub>X LS follows closely.

Find more information about LT<sub>E</sub>X at the [website of vscode-ltex](https://valentjn.github.io/vscode-ltex).

## Requirements

- 64-bit operating system
- Java 11 or later
- Language client supporting LSP 3.15 or later

## Startup

It is recommended to use the startup scripts `bin/ltex-ls` (Linux, Mac) and `bin\ltex-ls.bat` (Windows) to start LT<sub>E</sub>X LS. The startup scripts can be controlled by the following environment variables:

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

All commands are handled by the language client.

- `ltex.addToDictionary`: Signals the client that it should add words to the dictionary by adding them to [`ltex.dictionary`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexdictionary).
- `ltex.disableRules`: Signals the client that it should disable rules by adding them to [`ltex.disabledRules`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexdisabledrules).
- `ltex.hideFalsePositives`: Signals the client that it should hide false positives by adding them to [`ltex.hiddenFalsePositives`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexhiddenfalsepositives).

The parameters of the commands are specified as follows:

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

The reason of the existence of `ltex/workspaceSpecificConfiguration` is that some clients like VS Code have different configuration scopes (e.g., user and workspace). When a configuration like [`ltex.dictionary`](https://valentjn.github.io/vscode-ltex/docs/settings.html#ltexdictionary) appears in multiple scopes, the value in the scope with the higher precedence will override the other values (e.g., workspace scope will override user scope). `ltex/workspaceSpecificConfiguration` makes it possible for the client to implement a merging mechanism instead without having to change [`workspace/configuration`](https://microsoft.github.io/language-server-protocol/specification#workspace_configuration).

#### `ltex/serverStatus` (⮌)

`ltex/serverStatus` is a client-to-server request. It has no parameters; its result is given by `LtexServerStatusResult` (see below).

`ltex/serverStatus` returns information about the current resource consumption of LT<sub>E</sub>X LS. Some information might not be available. `ltex/serverStatus` requests can only be handled if LT<sub>E</sub>X LS is not currently busy (e.g., checking a document).

```typescript
interface LtexServerStatusResult {
  /**
   * The process ID of the Java process.
   */
  processId: number;

  /**
   * The wall-clock duration in seconds since the start of LTeX LS.
   */
  wallClockDuration: number;

  /**
   * The current CPU usage as a fraction between 0 and 1.
   */
  cpuUsage?: number;

  /**
   * The duration in seconds during which the CPU was occupied.
   */
  cpuDuration?: number;

  /**
   * The memory in bytes of all currently allocated Java objects
   * (this is a part of `totalMemory`).
   */
  usedMemory: number;

  /**
   * The total memory in bytes currently taken by the JVM.
   */
  totalMemory: number;
}
```
