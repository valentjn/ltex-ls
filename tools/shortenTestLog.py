#!/usr/bin/python3

# Copyright (C) 2020 Julian Valentin, LTeX Development Community
#
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at https://mozilla.org/MPL/2.0/.

import os
import re



def main() -> None:
  testLogFilePath = os.path.abspath(os.path.join(os.path.dirname(__file__), "..",
      "ltexls", "src", "test", "resources", "LtexLanguageServerTestLog.txt"))
  with open(testLogFilePath, "r") as f: testLog = f.read()

  matches = list(re.finditer(r"^\[Trace -.*?'([^ ]+).*?'", testLog, flags=re.MULTILINE))
  resultTestLog = ""
  startPos = [x.start() for x in matches] + [len(testLog)]

  for i in range(len(startPos) - 1):
    part = testLog[startPos[i]:startPos[i + 1]]
    requestName = matches[i].group(1)

    if requestName == "initialize":
      part = re.sub(r"^ *\"(?:rootPath|rootUri)\": \".*\",\n", "", part, flags=re.MULTILINE)
      part = re.sub(r",\n    \"workspaceFolders\": \[$.*?^    \]", "", part,
          flags=re.MULTILINE | re.DOTALL)

    if requestName in ["ltex/workspaceSpecificConfiguration", "workspace/configuration",
          "workspace/didChangeConfiguration"]:
      for indent in [8 * " ", 12 * " "]:
        part = re.sub(r"^(" + indent + r"\"(?:"
            r"dictionary|workspaceDictionary|workspaceFolderDictionary|"
            r"disabledRules|workspaceDisabledRules|workspaceDisabledRules|"
            r"enabledRules|workspaceEnabledRules|workspaceEnabledRules|"
            r"hiddenFalsePositives|ignoreRuleInSentence|"
            r"commands|environments)\"): [\{\[]$.*?^" + indent + r"[\}\]]",
            r"\1: {}", part, flags=re.MULTILINE | re.DOTALL)

    resultTestLog += part

  with open(testLogFilePath, "w") as f: f.write(resultTestLog)



if __name__ == "__main__":
  main()
