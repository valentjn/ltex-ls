/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.program;

import java.util.regex.Pattern;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ProgramCommentPatterns {
  private @MonotonicNonNull String blockCommentStartPatternString;
  private @MonotonicNonNull String blockCommentEndPatternString;
  private @MonotonicNonNull String lineCommentPatternString;

  public ProgramCommentPatterns(String codeLanguageId) {
    if (codeLanguageId.equals("c")
          || codeLanguageId.equals("cpp")
          || codeLanguageId.equals("csharp")
          || codeLanguageId.equals("dart")
          || codeLanguageId.equals("fsharp")
          || codeLanguageId.equals("go")
          || codeLanguageId.equals("groovy")
          || codeLanguageId.equals("java")
          || codeLanguageId.equals("javascript")
          || codeLanguageId.equals("javascriptreact")
          || codeLanguageId.equals("kotlin")
          || codeLanguageId.equals("php")
          || codeLanguageId.equals("rust")
          || codeLanguageId.equals("scala")
          || codeLanguageId.equals("swift")
          || codeLanguageId.equals("typescript")
          || codeLanguageId.equals("typescriptreact")
          || codeLanguageId.equals("verilog")) {
      this.blockCommentStartPatternString = "/\\*\\*?";
      this.blockCommentEndPatternString = "\\*\\*?/";
      this.lineCommentPatternString = "///?";
    } else if (codeLanguageId.equals("elixir")
          || codeLanguageId.equals("python")) {
      this.blockCommentStartPatternString = "\"\"\"";
      this.blockCommentEndPatternString = "\"\"\"";
      this.lineCommentPatternString = "##?";
    } else if (codeLanguageId.equals("powershell")) {
      this.blockCommentStartPatternString = "<#";
      this.blockCommentEndPatternString = "#>";
      this.lineCommentPatternString = "##?";
    } else if (codeLanguageId.equals("coffeescript")
          || codeLanguageId.equals("julia")
          || codeLanguageId.equals("perl")
          || codeLanguageId.equals("perl6")
          || codeLanguageId.equals("puppet")
          || codeLanguageId.equals("r")
          || codeLanguageId.equals("ruby")
          || codeLanguageId.equals("shellscript")) {
      this.lineCommentPatternString = "##?";
    } else if (codeLanguageId.equals("lua")) {
      this.blockCommentStartPatternString = "--\\[\\[";
      this.blockCommentEndPatternString = "\\]\\]";
      this.lineCommentPatternString = "---?";
    } else if (codeLanguageId.equals("elm")
          || codeLanguageId.equals("haskell")) {
      this.blockCommentStartPatternString = "\\{-";
      this.blockCommentEndPatternString = "-\\}";
      this.lineCommentPatternString = "---?";
    } else if (codeLanguageId.equals("sql")) {
      this.lineCommentPatternString = "---?";
    } else if (codeLanguageId.equals("clojure")
          || codeLanguageId.equals("lisp")) {
      this.lineCommentPatternString = ";;?";
    } else if (codeLanguageId.equals("matlab")) {
      this.blockCommentStartPatternString = "%\\{";
      this.blockCommentEndPatternString = "%\\}";
      this.lineCommentPatternString = "%%?";
    } else if (codeLanguageId.equals("erlang")) {
      this.lineCommentPatternString = "%%?";
    } else if (codeLanguageId.equals("fortran-modern")) {
      this.lineCommentPatternString = "c";
    } else if (codeLanguageId.equals("vb")) {
      this.lineCommentPatternString = "''?";
    }
  }

  public static boolean isSupportedCodeLanguageId(String codeLanguageId) {
    ProgramCommentPatterns patterns = new ProgramCommentPatterns(codeLanguageId);
    return ((patterns.blockCommentStartPatternString != null)
        || (patterns.blockCommentEndPatternString != null)
        || (patterns.lineCommentPatternString != null));
  }

  public @Nullable String getBlockCommentStartPatternString() {
    return this.blockCommentStartPatternString;
  }

  public @Nullable String getBlockCommentEndPatternString() {
    return this.blockCommentEndPatternString;
  }

  public @Nullable String getLineCommentPatternString() {
    return this.lineCommentPatternString;
  }

  public Pattern getCommentBlockPattern() {
    StringBuilder patternStringBuilder = new StringBuilder();

    if ((this.blockCommentStartPatternString != null)
          && (this.blockCommentEndPatternString != null)) {
      if (patternStringBuilder.length() > 0) patternStringBuilder.append("|");
      patternStringBuilder.append("^[ \t]*" + this.blockCommentStartPatternString
          + "(?:[ \t]|$)(?<blockComment>(?:(?!" + this.blockCommentEndPatternString
          + ").|\r?\n)*?)(?:[ \t]|^)" + this.blockCommentEndPatternString + "[ \t]*$");
    }

    if (this.lineCommentPatternString != null) {
      if (patternStringBuilder.length() > 0) patternStringBuilder.append("|");
      patternStringBuilder.append("(?<lineComment>(?:^[ \t]*" + this.lineCommentPatternString
          + "[ \t](?:.*?)$(?:\r?\n)?)+)");
    }

    return Pattern.compile(patternStringBuilder.toString(), Pattern.MULTILINE);
  }

  public Pattern getMagicCommentPattern() {
    StringBuilder patternStringBuilder = new StringBuilder();

    if ((this.blockCommentStartPatternString != null)
          && (this.blockCommentEndPatternString != null)) {
      if (patternStringBuilder.length() > 0) patternStringBuilder.append("|");
      patternStringBuilder.append("^[ \t]*" + this.blockCommentStartPatternString
          + "[ \t]*(?i)ltex(?-i):(.*?)[ \t]*" + this.blockCommentEndPatternString + "[ \t]*$");
    }

    if (this.lineCommentPatternString != null) {
      if (patternStringBuilder.length() > 0) patternStringBuilder.append("|");
      patternStringBuilder.append(
          "^[ \t]*" + this.lineCommentPatternString
          + "[ \t]*(?i)ltex(?-i):(.*?)[ \t]*$");
    }

    return Pattern.compile(patternStringBuilder.toString(), Pattern.MULTILINE);
  }
}
