/* Copyright (C) 2019-2023 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.tools

import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

object FileIo {
  private val TILDE_PATH_REGEX = Regex("^~($|/|\\\\)")

  fun normalizePath(path: String): String {
    val homeDirPath: String? = System.getProperty("user.home")

    return if (homeDirPath != null) {
      path.replaceFirst(TILDE_PATH_REGEX, Regex.escapeReplacement(homeDirPath) + "$1")
    } else {
      path
    }
  }

  fun readFile(filePath: Path): String? {
    return try {
      readFileWithException(filePath)
    } catch (e: IOException) {
      Logging.LOGGER.warning(I18n.format("couldNotReadFile", e, filePath.toString()))
      null
    }
  }

  fun readFileWithException(filePath: Path): String {
    return String(Files.readAllBytes(filePath), StandardCharsets.UTF_8)
  }

  fun writeFile(filePath: Path, text: String) {
    try {
      writeFileWithException(filePath, text)
    } catch (e: IOException) {
      Logging.LOGGER.warning(I18n.format("couldNotWriteFile", e, filePath.toString()))
    }
  }

  fun writeFileWithException(filePath: Path, text: String) {
    Files.write(
      filePath,
      text.toByteArray(StandardCharsets.UTF_8),
      StandardOpenOption.CREATE,
      StandardOpenOption.TRUNCATE_EXISTING,
      StandardOpenOption.WRITE,
      StandardOpenOption.SYNC,
    )
  }

  @Suppress("ComplexCondition", "ComplexMethod", "LongMethod")
  fun getCodeLanguageIdFromPath(path: Path): String? {
    val fileName: String = path.fileName.toString()

    return if (fileName.endsWith(".bib")) {
      "bibtex"
    } else if (
      fileName.endsWith(".c")
      || fileName.endsWith(".h")
    ) {
      "c"
    } else if (fileName.endsWith(".clj")) {
      "clojure"
    } else if (fileName.endsWith(".coffee")) {
      "coffeescript"
    } else if (
      fileName.endsWith(".cc")
      || fileName.endsWith(".cpp")
      || fileName.endsWith(".cxx")
      || fileName.endsWith(".hh")
      || fileName.endsWith(".hpp")
      || fileName.endsWith(".inl")
    ) {
      "cpp"
    } else if (fileName.endsWith(".cs")) {
      "csharp"
    } else if (fileName.endsWith(".dart")) {
      "dart"
    } else if (fileName.endsWith(".ex")) {
      "elixir"
    } else if (fileName.endsWith(".elm")) {
      "elm"
    } else if (fileName.endsWith(".erl")) {
      "erlang"
    } else if (fileName.endsWith(".f90")) {
      "fortran-modern"
    } else if (fileName.endsWith(".fs")) {
      "fsharp"
    } else if (fileName.endsWith(".go")) {
      "go"
    } else if (fileName.endsWith(".groovy")) {
      "groovy"
    } else if (fileName.endsWith(".hs")) {
      "haskell"
    } else if (
      fileName.endsWith(".htm")
      || fileName.endsWith(".html")
      || fileName.endsWith(".xht")
      || fileName.endsWith(".xhtml")
    ) {
      "html"
    } else if (fileName.endsWith(".java")) {
      "java"
    } else if (fileName.endsWith(".js")) {
      "javascript"
    } else if (fileName.endsWith(".jl")) {
      "julia"
    } else if (fileName.endsWith(".kt")) {
      "kotlin"
    } else if (fileName.endsWith(".tex")) {
      "latex"
    } else if (fileName.endsWith(".lisp")) {
      "lisp"
    } else if (fileName.endsWith(".lua")) {
      "lua"
    } else if (fileName.endsWith(".md")) {
      "markdown"
    } else if (fileName.endsWith(".m")) {
      "matlab"
    } else if (fileName.endsWith(".org")) {
      "org"
    } else if (fileName.endsWith(".pl")) {
      "perl"
    } else if (fileName.endsWith(".php")) {
      "php"
    } else if (fileName.endsWith(".txt")) {
      "plaintext"
    } else if (fileName.endsWith(".ps1")) {
      "powershell"
    } else if (fileName.endsWith(".pp")) {
      "puppet"
    } else if (fileName.endsWith(".py")) {
      "python"
    } else if (fileName.endsWith(".r")) {
      "r"
    } else if (fileName.endsWith(".Rmd")
          || fileName.endsWith(".rmd")) {
    "rmd"
    } else if (fileName.endsWith(".rst")) {
      "restructuredtext"
    } else if (
      fileName.endsWith(".Rnw")
      || fileName.endsWith(".rnw")
    ) {
      "rsweave"
    } else if (fileName.endsWith(".rb")) {
      "ruby"
    } else if (fileName.endsWith(".rs")) {
      "rust"
    } else if (fileName.endsWith(".scala")) {
      "scala"
    } else if (fileName.endsWith(".sh")) {
      "shellscript"
    } else if (fileName.endsWith(".sql")) {
      "sql"
    } else if (fileName.endsWith(".swift")) {
      "swift"
    } else if (fileName.endsWith(".ts")) {
      "typescript"
    } else if (fileName.endsWith(".vb")) {
      "vb"
    } else if (fileName.endsWith(".v")) {
      "verilog"
    } else {
      null
    }
  }
}
