#!/usr/bin/python3

# Copyright (C) 2020 Julian Valentin, LTeX Development Community
#
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at https://mozilla.org/MPL/2.0/.

import os
import pathlib
import re
from typing import Any, Callable, Dict, Sequence, Union



def processFiles(dirPaths: Sequence[pathlib.Path], extensions: Union[str, Sequence[str]],
      processFunction: Callable[[pathlib.Path, Dict[str, Any]], None]) -> Dict[str, Any]:
  if isinstance(extensions, str): extensions = [extensions]
  result: Dict[str, Any] = {}

  for dirPath in dirPaths:
    for rootDirPath, dirNames, fileNames in os.walk(dirPath):
      dirNames.sort()
      fileNames.sort()

      for fileName in fileNames:
        if pathlib.Path(fileName).suffix not in extensions: continue
        filePath = pathlib.Path(rootDirPath, fileName)
        processFunction(filePath, result)

  return result



def processSourceFile(filePath: pathlib.Path, result: Dict[str, Any]) -> None:
  with open(filePath, "r") as f: contents = f.read()
  match = re.search(r"^package (.*);$", contents, flags=re.MULTILINE)
  className = filePath.stem
  package = ("{}.{}".format(match.group(1), className) if match is not None else className)
  dependencies = re.findall(r"^import (org\.bsplines\..*);$", contents, flags=re.MULTILINE)

  result[package] = {
        "path" : filePath,
        "mtime" : filePath.stat().st_mtime,
        "dependencies" : dependencies,
      }



def processClassFile(filePath: pathlib.Path, result: Dict[str, Any]) -> None:
  className = filePath.stem
  package = className
  curFilePath = filePath.parent

  while True:
    curFilePath, curDirName = curFilePath.parent, curFilePath.name
    if curDirName in ["classes", "test-classes"]: break
    package = f"{curDirName}.{package}"

  result[package] = {
        "path" : filePath,
        "mtime" : filePath.stat().st_mtime,
      }



def getOutdatedClassFilePaths(sourceFiles: Dict[str, Dict[str, Any]],
      classFiles: Dict[str, Dict[str, Any]]) -> Sequence[pathlib.Path]:
  outdatedPackageClassNames = []

  for packageClassName in classFiles:
    classFilePath = classFiles[packageClassName]["path"]
    if "$" in packageClassName: packageClassName = packageClassName[:packageClassName.index("$")]

    if packageClassName not in sourceFiles:
      print(f"Removing '{classFilePath}' as its source file doesn't exist anymore...")
      outdatedPackageClassNames.append(packageClassName)
    elif classFiles[packageClassName]["mtime"] < sourceFiles[packageClassName]["mtime"]:
      print(f"Removing '{classFilePath}' as its source file is newer...")
      outdatedPackageClassNames.append(packageClassName)

  transitiveOutdatedPackageClassNames = set(outdatedPackageClassNames)

  for outdatedPackageClassName in outdatedPackageClassNames:
    for packageClassName in sourceFiles:
      if outdatedPackageClassName in sourceFiles[packageClassName]["dependencies"]:
        classFilePath = classFiles[packageClassName]["path"]
        print(f"Removing '{classFilePath}' as it imports '{outdatedPackageClassName}'...")
        transitiveOutdatedPackageClassNames.add(packageClassName)

  transitiveOutdatedPackageClassNamesList = sorted(list(transitiveOutdatedPackageClassNames))
  transitiveOutdatedClassFilePaths = [classFiles[x]["path"]
      for x in transitiveOutdatedPackageClassNamesList]
  if len(transitiveOutdatedClassFilePaths) == 0: print("All class files are up-to-date.")

  return transitiveOutdatedClassFilePaths



def removeFiles(filePaths: Sequence[pathlib.Path]) -> None:
  for filePath in filePaths: filePath.unlink()



def main() -> None:
  repoDirPath = pathlib.Path(__file__).parent.parent.resolve()
  mainSourceDirPath = repoDirPath.joinpath("ltexls", "src", "main", "java")
  testSourceDirPath = repoDirPath.joinpath("ltexls", "src", "test", "java")
  mainClassDirPath = repoDirPath.joinpath("ltexls", "target", "classes")
  testClassDirPath = repoDirPath.joinpath("ltexls", "target", "test-classes")

  sourceFiles = processFiles([mainSourceDirPath, testSourceDirPath], ".java", processSourceFile)
  classFiles = processFiles([mainClassDirPath, testClassDirPath], ".class", processClassFile)
  outdatedClassFilePaths = getOutdatedClassFilePaths(sourceFiles, classFiles)
  removeFiles(outdatedClassFilePaths)



if __name__ == "__main__":
  main()
