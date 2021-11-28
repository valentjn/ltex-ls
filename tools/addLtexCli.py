#!/usr/bin/python3

# Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
#
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at https://mozilla.org/MPL/2.0/.

import os
import pathlib
import shutil
import tarfile
import tempfile
import urllib.request

lspCliVersion = "1.0.3"



def main() -> None:
  with tempfile.TemporaryDirectory() as tmpDirPathStr:
    tmpDirPath = pathlib.Path(tmpDirPathStr)

    lspCliArchiveName = f"lsp-cli-{lspCliVersion}.tar.gz"
    lspCliUrl = ("https://github.com/valentjn/lsp-cli/releases/download/"
        f"{lspCliVersion}/{lspCliArchiveName}")
    lspCliArchivePath = tmpDirPath.joinpath(lspCliArchiveName)
    print(f"Downloading lsp-cli {lspCliVersion} from '{lspCliUrl}' to '{lspCliArchivePath}'...")
    urllib.request.urlretrieve(lspCliUrl, lspCliArchivePath)

    print("Extracting lsp-cli archive...")
    with tarfile.open(lspCliArchivePath, "r:gz") as tarFile: tarFile.extractall(path=tmpDirPath)

    lspCliDirPath = tmpDirPath.joinpath(f"lsp-cli-{lspCliVersion}")
    targetDirPath = pathlib.Path(__file__).parent.parent.joinpath("target", "appassembler")

    print("Copying *.jar files...")

    for jarFilePath in lspCliDirPath.joinpath("lib").iterdir():
      shutil.copy(jarFilePath, targetDirPath.joinpath("lib"))

    print("Copying startup scripts...")

    for extension in ["", ".bat"]:
      targetFilePath = targetDirPath.joinpath("bin", f"ltex-cli{extension}")
      shutil.copyfile(lspCliDirPath.joinpath("bin", f"lsp-cli{extension}"), targetFilePath)

      if extension == "":
        mode = os.stat(targetFilePath).st_mode
        mode |= (mode & 0o444) >> 2
        os.chmod(targetFilePath, mode)

    print("Creating .lsp-cli.json...")
    lspCliJson = """
{
  "programName": "ltex-cli",
  "helpMessage": {
    "description": "LTeX CLI - Command-line interface for LTeX LS",
    "visibleArguments": [
      "--client-configuration",
      "--verbose"
    ]
  },
  "defaultValues": {
    "--hide-commands": true,
    "--server-command-line": "./ltex-ls"
  }
}
""".lstrip()
    with open(targetDirPath.joinpath("bin", ".lsp-cli.json"), "w") as f: f.write(lspCliJson)



if __name__ == "__main__":
  main()
