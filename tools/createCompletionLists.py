#!/usr/bin/python3

# Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
#
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at https://mozilla.org/MPL/2.0/.

import argparse
import functools
import multiprocessing
import pathlib
import subprocess
import tempfile
from typing import Sequence, Tuple
import urllib.request
import xml.etree.ElementTree
import zipfile



def getLanguageToolVersion() -> str:
  print("Getting LanguageTool version...")

  pomFilePath =  pathlib.Path(__file__).parent.parent.joinpath("pom.xml")
  tree = xml.etree.ElementTree.parse(pomFilePath)
  dependencyElements = tree.findall("./{http://maven.apache.org/POM/4.0.0}dependencies/"
      "{http://maven.apache.org/POM/4.0.0}dependency")

  for dependencyElement in dependencyElements:
    groupIdElement = dependencyElement.find("./{http://maven.apache.org/POM/4.0.0}groupId")
    assert groupIdElement is not None
    artifactIdElement = dependencyElement.find("./{http://maven.apache.org/POM/4.0.0}artifactId")
    assert artifactIdElement is not None

    if ((groupIdElement.text == "org.languagetool")
          and (artifactIdElement.text == "languagetool-core")):
      versionElement = dependencyElement.find("./{http://maven.apache.org/POM/4.0.0}version")
      assert versionElement is not None

      version = versionElement.text
      assert version is not None

      return version

  raise RuntimeError(f"Could not determine LanguageTool version in '{pomFilePath}'")



def downloadLanguageTool(tmpDirPath: pathlib.Path) -> pathlib.Path:
  languageToolVersion = getLanguageToolVersion()

  print("Downloading LanguageTool...")

  archiveFileName = f"LanguageTool-{languageToolVersion}.zip"
  url = f"https://languagetool.org/download/{archiveFileName}"
  archiveFilePath = tmpDirPath.joinpath(archiveFileName)

  opener = urllib.request.build_opener()
  opener.addheaders = [("User-Agent",
      "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:93.0) Gecko/20100101 Firefox/93.0")]
  urllib.request.install_opener(opener)
  urllib.request.urlretrieve(url, archiveFilePath)

  with zipfile.ZipFile(archiveFilePath, "r") as file: file.extractall(tmpDirPath)
  return tmpDirPath.joinpath(f"LanguageTool-{languageToolVersion}")



def searchForDictionaries(languageToolDirPath: pathlib.Path) \
      -> Sequence[Tuple[str, pathlib.Path, pathlib.Path]]:
  print("Searching for dictionaries...")

  resourceDirPath = languageToolDirPath.joinpath("org", "languagetool", "resource")
  dictionaries = []

  for languageDirPath in resourceDirPath.iterdir():
    if not languageDirPath.is_dir(): continue
    hunspellDirPath = languageDirPath.joinpath("hunspell")
    if not hunspellDirPath.is_dir(): continue

    for path in hunspellDirPath.iterdir():
      if not path.suffix == ".dict": continue
      language = path.stem.replace("_", "-")
      infoFilePath = hunspellDirPath.joinpath(f"{path.stem}.info")
      assert infoFilePath.is_file(), \
          f".info file '{infoFilePath}' does not exist for .dict file '{path}'"
      dictionaries.append((language, path, infoFilePath))

  # in other languages, dictionaries are either missing (e.g., French), very large (e.g., Breton),
  # or don't have a delimiter between the entries (e.g., Italian)
  dictionaries = [x for x in dictionaries if x[0].startswith("de-") or x[0].startswith("en-")]
  print("Found languages {}.".format(", ".join(f"'{x[0]}'" for x in dictionaries)))

  return dictionaries



def createCompletionList(languageToolJarFilePath: pathlib.Path, tmpDirPath: pathlib.Path,
      targetDirPath: pathlib.Path, dictionary: Tuple[str, pathlib.Path, pathlib.Path]) -> None:
  language, dictFilePath, infoFilePath = dictionary
  print(f"Creating completion list for language '{language}'...")

  dictTextFilePath = tmpDirPath.joinpath(f"dict.{language}.txt")
  subprocess.run(["java", "-cp", str(languageToolJarFilePath),
      "org.languagetool.tools.DictionaryExporter", "-i", str(dictFilePath),
      "-info", str(infoFilePath), "-o", str(dictTextFilePath)])

  targetFilePath = targetDirPath.joinpath(f"completionList.{language}.txt")

  # explicitly use UTF-8 for Windows
  with open(dictTextFilePath, "r", encoding="utf-8") as file: dictText = file.read()
  dictText = "\n".join([line[2:] for line in dictText.splitlines()[1::2]]) + "\n"
  with open(targetFilePath, "w", encoding="utf-8") as file: file.write(dictText)



def main() -> None:
  parser = argparse.ArgumentParser(
      description="Create dictionary lists for (auto-)completion.")
  parser.add_argument("--languagetool-path", type=pathlib.Path, metavar="PATH",
      help="Path to standalone version of LanguageTool; will be downloaded if omitted")
  arguments = parser.parse_args()

  targetDirPath = pathlib.Path(__file__).parent.parent.joinpath("src", "main", "resources")

  with tempfile.TemporaryDirectory() as tmpDirPathStr:
    tmpDirPath = pathlib.Path(tmpDirPathStr)
    languageToolDirPath = (arguments.languagetool_path
        if arguments.languagetool_path is not None else downloadLanguageTool(tmpDirPath))

    dictionaries = searchForDictionaries(languageToolDirPath)
    languageToolJarFilePath = languageToolDirPath.joinpath("languagetool.jar")

    with multiprocessing.Pool() as pool:
      pool.map(functools.partial(createCompletionList,
          languageToolJarFilePath, tmpDirPath, targetDirPath), dictionaries)



if __name__ == "__main__":
  main()
