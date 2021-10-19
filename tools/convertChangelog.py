#!/usr/bin/python3

# Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
#
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at https://mozilla.org/MPL/2.0/.

import argparse
import datetime
import pathlib
import re
import sys
from typing import Optional, Tuple
import xml.etree.ElementTree as et
import xml.dom.minidom

sys.path.append(str(pathlib.Path(__file__).parent))
import common



def parseIssue(issue: str) -> Tuple[str, str, int, str]:
  issueMatch = re.search(r"(?:(.*?)/)?(.*?)?#([0-9]+)", issue)
  assert issueMatch is not None, issue
  issueOrganization = (issueMatch.group(1) if issueMatch.group(1) is not None else
      common.organization)
  issueRepository = (issueMatch.group(2) if issueMatch.group(2) != "" else common.repository)
  issueNumber = int(issueMatch.group(3))

  if issueOrganization == common.organization:
    if issueRepository == common.repository:
      normalizedIssue = f"#{issueNumber}"
    else:
      normalizedIssue = f"{issueRepository}#{issueNumber}"
  else:
    normalizedIssue = f"{issueOrganization}/{issueRepository}#{issueNumber}"

  return issueOrganization, issueRepository, issueNumber, normalizedIssue



def replaceUnicodeWithXmlEntities(string: str) -> str:
  return re.sub(r"[\u007f-\U0010ffff]", (lambda x: f"&#x{ord(x.group()):04x};"), string)



def convertChangelogFromXmlToMarkdown(xmlFilePath: pathlib.Path,
      version: Optional[str] = None) -> str:
  document = et.parse(xmlFilePath).getroot()

  if version is None:
    markdown = """<!--
   - Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
   -
   - This Source Code Form is subject to the terms of the Mozilla Public
   - License, v. 2.0. If a copy of the MPL was not distributed with this
   - file, You can obtain one at https://mozilla.org/MPL/2.0/.
   -->

"""

    title = document.findtext("./{http://maven.apache.org/changes/1.0.0}properties"
        "/{http://maven.apache.org/changes/1.0.0}title")
    markdown += f"# {title}\n"
  else:
    markdown = ""

  releases = document.findall("./{http://maven.apache.org/changes/1.0.0}body"
      "/{http://maven.apache.org/changes/1.0.0}release")

  for release in releases:
    curVersion = release.attrib["version"]
    if version == "latest": version = curVersion
    if (version is not None) and (curVersion != version): continue

    description = release.attrib.get("description", "")
    if len(description) > 0: description = f" \u2014 \u201c{description}\u201d"
    dateStr = release.attrib["date"]
    dateStr = (datetime.datetime.strptime(dateStr, "%Y-%m-%d").strftime("%B %d, %Y")
        if dateStr != "upcoming" else dateStr)
    if version is None: markdown += f"\n## {curVersion}{description} ({dateStr})\n\n"

    for action in release.findall("./{http://maven.apache.org/changes/1.0.0}action"):
      type_ = action.attrib["type"]
      typeEmoji = {
            "add" : "\u2728",
            "fix": "\U0001f41b",
            "remove" : "\U0001f5d1",
            "update" : "\U0001f527",
          }[type_]
      typeStr = {"add" : "New", "fix": "Bug fix", "remove" : "Removal", "update" : "Change"}[type_]

      additionalInfo = ""

      if "issue" in action.attrib:
        for issue in action.attrib["issue"].split(","):
          issueOrganization, issueRepository, issueNumber, issue = parseIssue(issue)
          additionalInfo += (" \u2014 " if additionalInfo == "" else ", ")
          additionalInfo += (f"[{issue}](https://github.com/{issueOrganization}/"
              f"{issueRepository}/issues/{issueNumber})")

      if "due-to" in action.attrib:
        for author in action.attrib["due-to"].split(","):
          additionalInfo += (" \u2014 " if additionalInfo == "" else ", ")
          userMatch = re.search(r"@([^)]+)", author)
          if userMatch is not None: author = f"[{author}](https://github.com/{userMatch.group(1)})"
          additionalInfo += author

      change = action.text
      assert change is not None
      change = change.strip()
      change = change.replace("LaTeX", "L<sup>A</sup>T<sub>E</sub>X")
      change = re.sub(r"(?<!`L)TeX", "T<sub>E</sub>X", change)

      markdown += f"- {typeEmoji} *{typeStr}:* {change}{additionalInfo}\n"

  markdown = replaceUnicodeWithXmlEntities(markdown)
  return markdown



def convertReleaseFromMarkdownToXml(body: et.Element, version: str, name: str, dateStr: str,
      changes: str) -> et.Element:
  attributes = {"version" : version}
  if len(name) > 0: attributes["description"] = name
  attributes["date"] = (datetime.datetime.strptime(dateStr, "%B %d, %Y").strftime("%Y-%m-%d")
      if dateStr != "upcoming" else dateStr)
  release = et.SubElement(body, "release", attributes)

  changes = changes.strip()

  for change in changes.split("\n"):
    change = re.sub(r"^- ", "", change).strip()
    attributes = {}

    if change.startswith("Remove "):
      attributes["type"] = "remove"
    elif (change.startswith("Add ") or ("support" in change.lower())
          or (change == "Initial release")):
      attributes["type"] = "add"
    elif (change.startswith("Fix ")
          or any(x in change.lower() for x in ["error", "warning", "prevent"])):
      attributes["type"] = "fix"
    else:
      attributes["type"] = "update"

    issues = []
    authors = []
    issueFound = True

    while issueFound:
      issueMatch1 = re.search(
          r" \((?:fixes|fixes part of|see) \[([^\]]*?#[0-9]+)\]\(.*?\)\)", change)
      issueMatch2 = re.search(
          r"(?:[;,]| and) (?:fixes |see )?\[([^\]]*?#[0-9]+)\]\(.*?\)", change)
      issueMatch3 = re.search(
          r" \((?:\[PR |PR \[)(.*?#[0-9]+)\]\([^\]]*?\) by \[([^\]]*?)\]\(.*?\)\)", change)
      issueMatch4 = re.search(
          r"(?:[;,]| and) \[PR (.*?#[0-9]+)\]\([^\]]*?\) by \[([^\]]*?)\]\(.*?\)", change)
      issueFound = False

      for issueMatch in [issueMatch1, issueMatch2, issueMatch3, issueMatch4]:
        if issueMatch is not None:
          issueFound = True
          _, _, _, issue = parseIssue(issueMatch.group(1))
          issues.append(issue)
          if len(issueMatch.groups()) > 1: authors.append(issueMatch.group(2))
          change = change[:issueMatch.start()] + change[issueMatch.end():]

    if len(issues) > 0: attributes["issue"] = ",".join(sorted(issues))
    if len(authors) > 0: attributes["due-to"] = ",".join(sorted(authors))

    change = change.replace("T<sub>E</sub>X", "TeX").replace("L<sup>A</sup>", "La")

    assert f"github.com/{common.organization}" not in change, change
    assert "  -" not in change, change

    action = et.SubElement(release, "action", attributes)
    action.text = f"\n        {change}\n      "

  return release



def convertChangelogFromMarkdownToXml(markdownFilePath: pathlib.Path,
      version: Optional[str] = None) -> str:
  with open(markdownFilePath, "r", encoding="utf-8", newline="\n") as f:
    changelog = f.read()

  document = et.Element("document", {
        "xmlns" : "http://maven.apache.org/changes/1.0.0",
        "xmlns:xsi" : "http://www.w3.org/2001/XMLSchema-instance",
        "xsi:schemaLocation" : "http://maven.apache.org/changes/1.0.0 "
          "https://maven.apache.org/xsd/changes-1.0.0.xsd"
      })

  properties = et.SubElement(document, "properties")
  title = et.SubElement(properties, "title")
  title.text = "Changelog"
  author = et.SubElement(properties, "author")
  author.text = "Julian Valentin, LTeX Development Community"

  body = et.SubElement(document, "body")

  regexMatches = re.findall(
      r"\n## ([^ ]+)(?: \u2014 \u201c(.*?)\u201d)? \((.*)\)\n\n((?:.|\n)+?)(?=$|\n## )", changelog)
  for regexMatch in regexMatches:
    curVersion = regexMatch[0]
    if version == "latest": version = curVersion
    if (version is not None) and (curVersion != version): continue
    release = convertReleaseFromMarkdownToXml(body, *regexMatch)

    if version is not None:
      document = release
      break

  xmlStr = et.tostring(document, encoding="unicode", xml_declaration=True)
  xmlStr = xml.dom.minidom.parseString(xmlStr).toprettyxml(indent="  ")
  xmlStr = re.sub(r"^<\?xml version=\"1.0\" \?>\n",
      ("""<?xml version="1.0" encoding="UTF-8"?>
<!--
   - Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
   -
   - This Source Code Form is subject to the terms of the Mozilla Public
   - License, v. 2.0. If a copy of the MPL was not distributed with this
   - file, You can obtain one at https://mozilla.org/MPL/2.0/.
   -->
""" if version is None else ""), xmlStr)
  xmlStr = replaceUnicodeWithXmlEntities(xmlStr)

  return xmlStr



def main() -> None:
  parser = argparse.ArgumentParser(
      description="Convert changelog from XML to Markdown and vice-versa.")
  xmlFileArgument = parser.add_argument("--xml-file", type=pathlib.Path, metavar="PATH",
      help="XML file to convert to Markdown")
  parser.add_argument("--markdown-file", type=pathlib.Path, metavar="PATH",
      help="Markdown file to convert to XML")
  parser.add_argument("--output-file", type=pathlib.Path, default=pathlib.Path("-"), metavar="PATH",
      help="Output file; '-' is standard output (default)")
  parser.add_argument("--version",
      help="Version to convert; 'latest' is permitted; all versions are converted if omitted")
  arguments = parser.parse_args()

  if arguments.xml_file is not None:
    output = convertChangelogFromXmlToMarkdown(arguments.xml_file, arguments.version)
  elif arguments.markdown_file is not None:
    output = convertChangelogFromMarkdownToXml(arguments.markdown_file, arguments.version)
  else:
    raise argparse.ArgumentError(xmlFileArgument,
        "One of --xml-file or --markdown-file is required")

  if str(arguments.output_file) == "-":
    print(output, end="")
  else:
    with open(arguments.output_file, "w", encoding="utf-8", newline="\n") as f:
      f.write(output)



if __name__ == "__main__":
  main()
