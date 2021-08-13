/* Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.program

data class ProgramCommentRegexs(
  val blockCommentStartRegexString: String?,
  val blockCommentEndRegexString: String?,
  val lineCommentRegexString: String?,
) {
  fun getCommentBlockRegex(): Regex {
    val builder = StringBuilder()

    if ((this.blockCommentStartRegexString != null)
          && (this.blockCommentEndRegexString != null)) {
      if (builder.isNotEmpty()) builder.append("|")
      builder.append("^[ \t]*" + this.blockCommentStartRegexString
          + "(?:[ \t]|$)(?<blockComment>(?:(?!" + this.blockCommentEndRegexString
          + ").|\r?\n)*?)(?:[ \t]|^)" + this.blockCommentEndRegexString + "[ \t]*$")
    }

    if (this.lineCommentRegexString != null) {
      if (builder.isNotEmpty()) builder.append("|")
      builder.append("(?<lineComment>(?:^[ \t]*" + this.lineCommentRegexString
          + "[ \t](?:.*?)$(?:\r?\n)?)+)")
    }

    return Regex(builder.toString(), RegexOption.MULTILINE)
  }

  fun getMagicCommentRegex(): Regex {
    val builder = StringBuilder()

    if ((this.blockCommentStartRegexString != null)
          && (this.blockCommentEndRegexString != null)) {
      if (builder.isNotEmpty()) builder.append("|")
      builder.append("^[ \t]*" + this.blockCommentStartRegexString
          + "[ \t]*(?i)ltex(?-i):(.*?)[ \t]*" + this.blockCommentEndRegexString + "[ \t]*$")
    }

    if (this.lineCommentRegexString != null) {
      if (builder.isNotEmpty()) builder.append("|")
      builder.append(
          "^[ \t]*" + this.lineCommentRegexString
          + "[ \t]*(?i)ltex(?-i):(.*?)[ \t]*$")
    }

    return Regex(builder.toString(), RegexOption.MULTILINE)
  }

  companion object {
    @Suppress("LongMethod")
    fun fromCodeLanguageId(codeLanguageId: String): ProgramCommentRegexs {
      var blockCommentStartRegexString: String? = null
      var blockCommentEndRegexString: String? = null
      val lineCommentRegexString: String?

      when (codeLanguageId) {
        "c", "cpp", "csharp", "dart", "fsharp", "go", "groovy", "java", "javascript",
        "javascriptreact", "kotlin", "php", "rust", "scala", "swift", "typescript",
        "typescriptreact", "verilog" -> {
          blockCommentStartRegexString = "/\\*\\*?"
          blockCommentEndRegexString = "\\*\\*?/"
          lineCommentRegexString = "///?"
        }
        "elixir", "python" -> {
          blockCommentStartRegexString = "\"\"\""
          blockCommentEndRegexString = "\"\"\""
          lineCommentRegexString = "##?"
        }
        "powershell" -> {
          blockCommentStartRegexString = "<#"
          blockCommentEndRegexString = "#>"
          lineCommentRegexString = "##?"
        }
        "coffeescript", "julia", "perl", "perl6", "puppet", "r", "ruby", "shellscript" -> {
          lineCommentRegexString = "##?"
        }
        "lua" -> {
          blockCommentStartRegexString = "--\\[\\["
          blockCommentEndRegexString = "\\]\\]"
          lineCommentRegexString = "---?"
        }
        "elm", "haskell" -> {
          blockCommentStartRegexString = "\\{-"
          blockCommentEndRegexString = "-\\}"
          lineCommentRegexString = "---?"
        }
        "sql" -> {
          lineCommentRegexString = "---?"
        }
        "clojure", "lisp" -> {
          lineCommentRegexString = ";;?"
        }
        "matlab" -> {
          blockCommentStartRegexString = "%\\{"
          blockCommentEndRegexString = "%\\}"
          lineCommentRegexString = "%%?"
        }
        "erlang" -> {
          lineCommentRegexString = "%%?"
        }
        "fortran-modern" -> {
          lineCommentRegexString = "c"
        }
        "vb" -> {
          lineCommentRegexString = "''?"
        }
        else -> {
          blockCommentStartRegexString = null
          blockCommentEndRegexString = null
          lineCommentRegexString = null
        }
      }

      return ProgramCommentRegexs(
        blockCommentStartRegexString,
        blockCommentEndRegexString,
        lineCommentRegexString,
      )
    }

    fun isSupportedCodeLanguageId(codeLanguageId: String): Boolean {
      val regexs = fromCodeLanguageId(codeLanguageId)
      return ((regexs.blockCommentStartRegexString != null)
          || (regexs.blockCommentEndRegexString != null)
          || (regexs.lineCommentRegexString != null))
    }
  }
}
