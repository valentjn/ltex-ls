#!/usr/bin/python3

# Copyright (C) 2020 Julian Valentin, LTeX Development Community
#
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at https://mozilla.org/MPL/2.0/.

import argparse
import pathlib
import subprocess
import sys
import tarfile
import tempfile
import urllib.request



def downloadIdea(tmpDirPath: pathlib.Path) -> pathlib.Path:
  print("Downloading IntelliJ IDEA...")
  archiveFilePath = tmpDirPath.joinpath("idea.tar.gz")
  urllib.request.urlretrieve("https://download.jetbrains.com/idea/ideaIC-2021.2.tar.gz",
      archiveFilePath)

  print("Extracting IntelliJ IDEA...")
  with tarfile.open(archiveFilePath) as f: f.extractall(tmpDirPath)
  archiveFilePath.unlink()

  return tmpDirPath.joinpath("idea-IC-212.4746.92")



def runIdea(ideaDirPath: pathlib.Path, tmpDirPath: pathlib.Path) -> None:
  repoDirPath = pathlib.Path(__file__).parent.parent.resolve()
  resultsDirPath = tmpDirPath.joinpath("results")

  print("Running IntelliJ IDEA...")
  subprocess.run([str(ideaDirPath.joinpath("bin", "idea.sh")), "inspect", str(repoDirPath),
      repoDirPath.joinpath(".idea", "inspectionProfiles", "Project_Default.xml"),
      str(resultsDirPath), "-v2", "-d", "src"])

  hasProblems = False

  for childPath in sorted(resultsDirPath.iterdir()):
    if childPath.name == ".descriptions.xml": continue

    if not hasProblems:
      print("")
      print("Found problems with IntelliJ IDEA!")

    hasProblems = True
    print("")
    print(f"{childPath.name}:")
    print("")
    with open(childPath, "r") as f: print(f.read())

  if hasProblems:
    sys.exit(1)
  else:
    print("")
    print("No problems found with IntelliJ IDEA.")



def main() -> None:
  parser = argparse.ArgumentParser(description="Inspect Code with IntelliJ IDEA.")
  parser.add_argument("--idea-path", type=pathlib.Path,
      help="Directory to IntelliJ IDEA to use; will be downloaded if omitted")
  arguments = parser.parse_args()

  with tempfile.TemporaryDirectory() as tmpDirPathStr:
    tmpDirPath = pathlib.Path(tmpDirPathStr)
    ideaDirPath = (arguments.idea_path if arguments.idea_path is not None else
        downloadIdea(tmpDirPath))
    runIdea(ideaDirPath, tmpDirPath)



if __name__ == "__main__":
  main()
