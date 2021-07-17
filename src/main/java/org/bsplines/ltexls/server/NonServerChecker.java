/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.bsplines.ltexls.languagetool.LanguageToolRuleMatch;
import org.bsplines.ltexls.parsing.AnnotatedTextFragment;
import org.bsplines.ltexls.settings.SettingsManager;
import org.bsplines.ltexls.tools.Tools;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.lsp4j.Position;
import org.eclipse.xtext.xbase.lib.Pair;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Color;
import org.fusesource.jansi.AnsiConsole;

public class NonServerChecker {
  private LtexLanguageServer languageServer;

  public NonServerChecker() {
    this.languageServer = new LtexLanguageServer();
    this.languageServer.getSettingsManager().setSettings(
        this.languageServer.getSettingsManager().getSettings().withLogLevel(Level.WARNING));
  }

  public void loadSettings(Path settingsFilePath) throws IOException {
    String settingsJson = Tools.readFileWithException(settingsFilePath);
    JsonObject jsonSettings = JsonParser.parseString(settingsJson).getAsJsonObject();
    SettingsManager settingsManager = this.languageServer.getSettingsManager();

    if (!jsonSettings.has("ltex-ls")) {
      jsonSettings.add("ltex-ls", new JsonObject());
    }

    JsonObject ltexLsJsonSettings = jsonSettings.getAsJsonObject("ltex-ls");
    if (!ltexLsJsonSettings.has("logLevel")) ltexLsJsonSettings.addProperty("logLevel", "warning");

    settingsManager.setSettings(jsonSettings, jsonSettings);
  }

  public int check(List<Path> paths) throws IOException {
    int numberOfMatches = 0;

    for (Path path : paths) {
      numberOfMatches += check(path);
    }

    return numberOfMatches;
  }

  public int check(Path path) throws IOException {
    @Nullable String text = null;
    File file = path.toFile();
    @Nullable String codeLanguageId = null;

    if (path.toString().equals("-")) {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024];
      int length;

      while ((length = System.in.read(buffer)) != -1) {
        outputStream.write(buffer, 0, length);
      }

      text = outputStream.toString(StandardCharsets.UTF_8);
      codeLanguageId = "plaintext";
    } else if (file.isDirectory()) {
      int numberOfMatches = 0;
      boolean plainTextEnabled = this.languageServer.getSettingsManager()
          .getSettings().getEnabled().contains("plaintext");

      for (Path childPath : Files.walk(path).collect(Collectors.toList())) {
        if (childPath.toFile().isFile()) {
          codeLanguageId = Tools.getCodeLanguageIdFromPath(childPath);

          if ((codeLanguageId != null)
                && (!codeLanguageId.equals("plaintext") || plainTextEnabled)) {
            numberOfMatches += check(childPath);
          }
        }
      }

      return numberOfMatches;
    } else {
      text = Tools.readFileWithException(file.toPath());
      codeLanguageId = Tools.getCodeLanguageIdFromPath(path);
      if (codeLanguageId == null) codeLanguageId = "plaintext";
    }

    LtexTextDocumentItem document = new LtexTextDocumentItem(this.languageServer,
        path.toUri().toString(), codeLanguageId, 1, text);
    Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>> checkingResult =
        this.languageServer.getDocumentChecker().check(document);
    List<AnnotatedTextFragment> annotatedTextFragments = checkingResult.getValue();

    int terminalWidth = AnsiConsole.getTerminalWidth();
    if (terminalWidth <= 1) terminalWidth = Integer.MAX_VALUE;

    for (LanguageToolRuleMatch match : checkingResult.getKey()) {
      printMatch(path, document, match, annotatedTextFragments, terminalWidth);
    }

    return checkingResult.getKey().size();
  }

  private void printMatch(Path path, LtexTextDocumentItem document, LanguageToolRuleMatch match,
        List<AnnotatedTextFragment> annotatedTextFragments, int terminalWidth) {
    int fragmentIndex = CodeActionGenerator.findAnnotatedTextFragmentWithMatch(
        annotatedTextFragments, match);

    if (fragmentIndex == -1) {
      Tools.logger.warning(Tools.i18n("couldNotFindFragmentForMatch"));
      return;
    }

    final String text = document.getText();
    final int fromPos = match.getFromPos();
    final int toPos = match.getToPos();
    final Position fromPosition = document.convertPosition(fromPos);
    final String message = match.getMessage().replaceAll("<suggestion>(.*?)</suggestion>", "'$1'");

    Color color = Color.BLUE;
    String typeString = "grammar";

    switch (match.getType()) {
      case UnknownWord: {
        color = Color.RED;
        typeString = "spelling";
        break;
      }
      case Hint: {
        color = Color.BLUE;
        typeString = "style";
        break;
      }
      case Other: {
        color = Color.YELLOW;
        typeString = "grammar";
        break;
      }
      default: {
        color = Color.BLUE;
        typeString = "style";
        break;
      }
    }

    if (match.isUnknownWordRule()) {
      color = Color.RED;
      typeString = "spelling";
    }

    @Nullable String ruleId = match.getRuleId();
    if (ruleId == null) ruleId = "";

    System.out.println(Ansi.ansi().bold().a(path.toString()).a(":")
        .a(fromPosition.getLine() + 1).a(":").a(fromPosition.getCharacter() + 1).a(": ")
        .fg(color).a(typeString).a(":").reset().bold().a(" ").a(message)
        .a(" [").a(ruleId).a("]").reset());

    int lineStartPos = document.convertPosition(new Position(fromPosition.getLine(), 0));
    int lineEndPos = document.convertPosition(new Position(fromPosition.getLine() + 1, 0));
    String line = text.substring(lineStartPos, lineEndPos);

    System.out.println(Ansi.ansi().a(line.substring(0, fromPos - lineStartPos)).bold().fg(color)
        .a(line.substring(fromPos - lineStartPos, toPos - lineStartPos)).reset()
        .a(line.substring(toPos - lineStartPos).replaceFirst("[ \t\r\n]+$", "")));

    int indentationSize = 0;

    for (int pos = lineStartPos; pos < fromPos; pos++) {
      if (text.charAt(pos) == '\t') {
        indentationSize = (int)(Math.ceil((indentationSize + 1) / 8.0) * 8);
      } else {
        indentationSize++;
      }

      if (indentationSize >= terminalWidth) indentationSize = 0;
    }

    List<String> suggestedReplacements = new ArrayList<>();

    for (int i = 0; i < Integer.min(match.getSuggestedReplacements().size(), 5); i++) {
      String suggestedReplacement = match.getSuggestedReplacements().get(i);
      if (indentationSize + suggestedReplacement.length() > terminalWidth) indentationSize = 0;
      suggestedReplacements.add(suggestedReplacement);
    }

    String indentation = " ".repeat(indentationSize);

    for (String suggestedReplacement : suggestedReplacements) {
      System.out.println(Ansi.ansi().a(indentation).fg(Color.GREEN)
          .a(suggestedReplacement).reset());
    }
  }
}
