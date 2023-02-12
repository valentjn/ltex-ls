#!/usr/bin/python3

# Copyright (C) 2019-2023 Julian Valentin, LTeX Development Community
#
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at https://mozilla.org/MPL/2.0/.

import json
import pathlib
import sys
from typing import cast, List
import urllib.parse
import xml.etree.ElementTree as et

sys.path.append(str(pathlib.Path(__file__).parent))
import common
import convertChangelog



def getVersions(changelogFilePath: pathlib.Path) -> List[str]:
  document = et.parse(changelogFilePath).getroot()
  releases = document.findall("./{http://maven.apache.org/changes/1.0.0}body"
      "/{http://maven.apache.org/changes/1.0.0}release")
  return [x.attrib["version"] for x in releases if x.attrib["date"] != "upcoming"]



def getReleaseIdOfGitHubRelease(version: str) -> int:
  apiUrl = (f"https://api.github.com/repos/{urllib.parse.quote_plus(common.organization)}/"
      f"{urllib.parse.quote_plus(common.repository)}/releases/tags/"
      f"{urllib.parse.quote_plus(version)}")
  response = common.requestFromGitHub(apiUrl)
  return cast(int, response["id"])



def updateDescriptionOfGitHubRelease(releaseId: int, description: str) -> None:
  apiUrl = (f"https://api.github.com/repos/{urllib.parse.quote_plus(common.organization)}/"
      f"{urllib.parse.quote_plus(common.repository)}/releases/{releaseId}")
  common.requestFromGitHub(apiUrl, method="PATCH", data=json.dumps({"body": description}))



def main() -> None:
  changelogFilePath = pathlib.Path(pathlib.Path(__file__).parent.parent.joinpath("changelog.xml"))
  versions = getVersions(changelogFilePath)

  for version in versions:
    print(f"Processing version '{version}'...")
    description = convertChangelog.convertChangelogFromXmlToMarkdown(changelogFilePath, version)
    print(description)
    releaseId = getReleaseIdOfGitHubRelease(version)
    updateDescriptionOfGitHubRelease(releaseId, description)



if __name__ == "__main__":
  main()
