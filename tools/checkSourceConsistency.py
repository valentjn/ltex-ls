#!/usr/bin/python3

import pathlib
import re
from typing import Literal, Union



def compareLatexCommandPrototypes(commandPrototype1: str,
      commandPrototype2: str) -> Union[Literal[-1], Literal[0], Literal[1]]:
  nameRegex = re.compile(r"\\([A-Za-z*@]+)")

  nameRegexMatch1 = nameRegex.search(commandPrototype1)
  assert nameRegexMatch1 is not None
  name1 = nameRegexMatch1.group(1)

  nameRegexMatch2 = nameRegex.search(commandPrototype2)
  assert nameRegexMatch2 is not None
  name2 = nameRegexMatch2.group(1)

  if name1 < name2:
    return -1
  elif name1 > name2:
    return 1
  elif len(commandPrototype1) < len(commandPrototype2):
    return -1
  elif len(commandPrototype1) > len(commandPrototype2):
    return 1
  elif commandPrototype1 < commandPrototype2:
    return -1
  elif commandPrototype1 > commandPrototype2:
    return 1
  else:
    return 0



def checkLatexCommandSignatures(filePath: pathlib.Path) -> None:
  print(f"Checking '{filePath}'...")
  with open(filePath, "r") as f: code = f.read()
  regexMatch = re.search(r"DEFAULT_LATEX_COMMAND_SIGNATURES.*?\r?\n[ \t]*\r?\n", code,
      flags=re.DOTALL)
  assert regexMatch is not None
  commandPrototypes = re.findall(r"LatexCommandSignature\([ \t\r\n]*\"(.*?)\"", regexMatch.group())

  for i in range(len(commandPrototypes) - 1):
    comparison = compareLatexCommandPrototypes(commandPrototypes[i], commandPrototypes[i + 1])

    if comparison == 0:
      raise RuntimeError(f"{filePath}: Duplicate LaTeX command prototype '{commandPrototypes[i]}'")
    elif comparison == 1:
      raise RuntimeError(f"{filePath}: LaTeX command prototype '{commandPrototypes[i]}' must come "
          f"after '{commandPrototypes[i + 1]}'")



def checkProperties(filePath: pathlib.Path) -> None:
  print(f"Checking '{filePath}'...")
  with open(filePath, "r") as f: code = f.read()
  propertyNames = re.findall(r"^([0-9A-Za-z]+) =", code, flags=re.MULTILINE)

  for i in range(len(propertyNames) - 1):
    if propertyNames[i] == propertyNames[i + 1]:
      raise RuntimeError(f"{filePath}: Duplicate property name '{propertyNames[i]}'")
    elif propertyNames[i] > propertyNames[i + 1]:
      raise RuntimeError(f"{filePath}: Property name '{propertyNames[i]}' must come "
          f"after '{propertyNames[i + 1]}'")



def main() -> None:
  repoDirPath = pathlib.Path(__file__).parent.parent.resolve()

  checkLatexCommandSignatures(repoDirPath.joinpath("src", "main", "kotlin", "org", "bsplines",
      "ltexls", "parsing", "latex", "LatexAnnotatedTextBuilderDefaults.kt"))

  resourcesDirPath = repoDirPath.joinpath("src", "main", "resources")

  for path in resourcesDirPath.iterdir():
    if path.is_file() and path.suffix == ".properties": checkProperties(path)



if __name__ == "__main__":
  main()
