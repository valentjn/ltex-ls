/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.latex

import org.bsplines.ltexls.parsing.CodeFragment
import org.bsplines.ltexls.parsing.CodeFragmentizer
import org.bsplines.ltexls.parsing.RegexCodeFragmentizer
import org.bsplines.ltexls.settings.Settings
import org.bsplines.ltexls.tools.I18n
import org.bsplines.ltexls.tools.Logging

class LatexFragmentizer(codeLanguageId: String) : CodeFragmentizer(codeLanguageId) {
  private val commentFragmentizer = RegexCodeFragmentizer(codeLanguageId, COMMENT_REGEX)

  override fun fragmentize(code: String, originalSettings: Settings): List<CodeFragment> {
    var fragments: List<CodeFragment> = listOf(
      CodeFragment(codeLanguageId, code, 0, originalSettings),
    )

    fragments = commentFragmentizer.fragmentize(fragments)
    fragments = fragmentizeBabelUsePackageCommands(fragments)
    fragments = fragmentizeBabelSwitchCommands(fragments)
    fragments = fragmentizeBabelInlineCommands(fragments)
    fragments = fragmentizeBabelEnvironments(fragments)
    fragments = fragmentizeExtraCommands(fragments)

    return fragments
  }

  @Suppress("LoopWithTooManyJumpStatements", "NestedBlockDepth")
  private fun fragmentizeBabelUsePackageCommands(
    fragments: List<CodeFragment>,
  ): List<CodeFragment> {
    val newFragments = ArrayList<CodeFragment>()

    for ((_, oldFragmentCode: String, fromPos: Int, oldFragmentSettings: Settings) in fragments) {
      USE_PACKAGE_COMMAND_SIGNATURE_MATCHER.startMatching(
        oldFragmentCode,
        getIgnoreCommandPrototypes(oldFragmentSettings),
      )

      var prevFromPos = 0
      var prevSettings: Settings = oldFragmentSettings

      while (true) {
        val match: LatexCommandSignatureMatch =
            USE_PACKAGE_COMMAND_SIGNATURE_MATCHER.findNextMatch() ?: break

        val packageName: String = match.getArgumentContents(1)
        if (packageName != "babel") continue
        val packageOptions: List<LatexPackageOption> =
            LatexPackageOptionsParser.parse(match.getArgumentContents(0))

        var babelLanguage: String? = null

        for (packageOption: LatexPackageOption in packageOptions) {
          val keyAsPlainText: String = packageOption.keyInfo.plainText
          if (BABEL_LANGUAGE_MAP.containsKey(keyAsPlainText)) babelLanguage = keyAsPlainText
        }

        if (babelLanguage == null) continue
        val languageShortCode: String? = BABEL_LANGUAGE_MAP[babelLanguage]

        if (languageShortCode == null) {
          Logging.logger.warning(I18n.format("unknownBabelLanguage", babelLanguage))
          continue
        }

        val nextFromPos: Int = match.fromPos
        val nextSettings: Settings = prevSettings.copy(_languageShortCode = languageShortCode)

        newFragments.add(
          CodeFragment(
            codeLanguageId,
            oldFragmentCode.substring(prevFromPos, nextFromPos),
            fromPos + prevFromPos,
            prevSettings,
          )
        )

        prevFromPos = nextFromPos
        prevSettings = nextSettings
      }

      newFragments.add(
        CodeFragment(
          codeLanguageId,
          oldFragmentCode.substring(prevFromPos),
          fromPos + prevFromPos,
          prevSettings,
        )
      )
    }

    return newFragments
  }

  @Suppress("LoopWithTooManyJumpStatements")
  private fun fragmentizeBabelSwitchCommands(fragments: List<CodeFragment>): List<CodeFragment> {
    val newFragments = ArrayList<CodeFragment>()

    for ((_, oldFragmentCode: String, fromPos: Int, oldFragmentSettings: Settings) in fragments) {
      BABEL_SWITCH_COMMAND_SIGNATURE_MATCHER.startMatching(
        oldFragmentCode,
        getIgnoreCommandPrototypes(oldFragmentSettings)
      )

      var prevFromPos = 0
      var prevSettings: Settings = oldFragmentSettings

      while (true) {
        val match: LatexCommandSignatureMatch =
            BABEL_SWITCH_COMMAND_SIGNATURE_MATCHER.findNextMatch() ?: break

        val babelLanguage: String = match.getArgumentContents(0)
        val languageShortCode: String? = BABEL_LANGUAGE_MAP[babelLanguage]

        if (languageShortCode == null) {
          Logging.logger.warning(I18n.format("unknownBabelLanguage", babelLanguage))
          continue
        }

        val nextFromPos: Int = match.fromPos
        val nextSettings: Settings = prevSettings.copy(_languageShortCode = languageShortCode)

        newFragments.add(
          CodeFragment(
            codeLanguageId,
            oldFragmentCode.substring(prevFromPos, nextFromPos),
            fromPos + prevFromPos,
            prevSettings,
          )
        )

        prevFromPos = nextFromPos
        prevSettings = nextSettings
      }

      newFragments.add(
        CodeFragment(
          codeLanguageId,
          oldFragmentCode.substring(prevFromPos),
          fromPos + prevFromPos,
          prevSettings,
        )
      )
    }

    return newFragments
  }

  @Suppress("LoopWithTooManyJumpStatements")
  private fun fragmentizeBabelInlineCommands(fragments: List<CodeFragment>): List<CodeFragment> {
    val newFragments = ArrayList<CodeFragment>()

    for (oldFragment: CodeFragment in fragments) {
      val oldFragmentCode: String = oldFragment.code
      val oldFragmentSettings: Settings = oldFragment.settings
      BABEL_INLINE_COMMAND_SIGNATURE_MATCHER.startMatching(
        oldFragmentCode,
        getIgnoreCommandPrototypes(oldFragmentSettings)
      )

      var curSettings: Settings = oldFragmentSettings

      while (true) {
        val match: LatexCommandSignatureMatch =
            BABEL_INLINE_COMMAND_SIGNATURE_MATCHER.findNextMatch() ?: break

        var languageShortCode: String? = BABEL_INLINE_COMMAND_SIGNATURE_MAP[match.commandSignature]
        var babelLanguage = ""

        if (languageShortCode == null) {
          val commandPrototype = match.commandSignature.commandPrototype
          Logging.logger.warning(I18n.format("invalidBabelInlineCommand", commandPrototype))
          continue
        } else if (languageShortCode.isEmpty()) {
          babelLanguage = match.getArgumentContents(match.getArgumentsSize() - 2)
          languageShortCode = BABEL_LANGUAGE_MAP[babelLanguage]
        }

        if (languageShortCode == null) {
          Logging.logger.warning(I18n.format("unknownBabelLanguage", babelLanguage))
        } else {
          curSettings = curSettings.copy(_languageShortCode = languageShortCode)
        }

        val contents: String = match.getArgumentContents(match.getArgumentsSize() - 1)
        val contentsFromPos: Int = match.getArgumentContentsFromPos(match.getArgumentsSize() - 1)

        newFragments.add(
          CodeFragment(
            codeLanguageId,
            contents,
            oldFragment.fromPos + contentsFromPos,
            curSettings,
          )
        )
      }

      newFragments.add(oldFragment)
    }

    return newFragments
  }

  @Suppress("LongMethod", "LoopWithTooManyJumpStatements", "NestedBlockDepth")
  private fun fragmentizeBabelEnvironments(fragments: List<CodeFragment>): List<CodeFragment> {
    val newFragments = ArrayList<CodeFragment>()

    for (oldFragment: CodeFragment in fragments) {
      val oldFragmentCode: String = oldFragment.code
      val oldFragmentSettings: Settings = oldFragment.settings
      BABEL_ENVIRONMENT_COMMAND_SIGNATURE_MATCHER.startMatching(
        oldFragmentCode,
        getIgnoreCommandPrototypes(oldFragmentSettings)
      )

      val settingsStack = ArrayDeque<Settings>()
      val fromPosStack = ArrayDeque<Int>()
      settingsStack.addLast(oldFragmentSettings)
      fromPosStack.addLast(0)

      while (settingsStack.isNotEmpty()) {
        val match: LatexCommandSignatureMatch =
            BABEL_ENVIRONMENT_COMMAND_SIGNATURE_MATCHER.findNextMatch() ?: break

        val commandPrototype: String = match.commandSignature.commandPrototype
        val isBegin: Boolean = commandPrototype.startsWith("\\begin")

        if (isBegin) {
          var languageShortCode: String? =
              BABEL_ENVIRONMENT_COMMAND_SIGNATURE_MAP[match.commandSignature]
          var babelLanguage = ""

          if (languageShortCode == null) {
            Logging.logger.warning(I18n.format("invalidBabelEnvironment", commandPrototype))
            continue
          } else if (languageShortCode.isEmpty()) {
            babelLanguage = match.getArgumentContents(match.getArgumentsSize() - 1)
            languageShortCode = BABEL_LANGUAGE_MAP[babelLanguage]
          }

          var newSettings: Settings = settingsStack.last()

          if (languageShortCode == null) {
            Logging.logger.warning(I18n.format("unknownBabelLanguage", babelLanguage))
          } else {
            newSettings = newSettings.copy(_languageShortCode = languageShortCode)
          }

          settingsStack.addLast(newSettings)
          fromPosStack.addLast(match.toPos)
        } else if (settingsStack.size <= 1) {
          // shouldn't happen, as then there is an unmatched \end
          break
        } else {
          val prevSettings: Settings = settingsStack.removeLast()
          val prevFromPos: Int = fromPosStack.removeLast()

          newFragments.add(
            CodeFragment(
              codeLanguageId,
              oldFragmentCode.substring(prevFromPos, match.fromPos),
              oldFragment.fromPos + prevFromPos,
              prevSettings,
            )
          )
        }
      }

      val stackSize = settingsStack.size

      if (stackSize > 1) {
        // shouldn't happen, as then there is an unmatched \begin
        for (i in 0 until stackSize - 1) {
          val prevSettings: Settings = settingsStack.removeLast()
          val prevFromPos: Int = fromPosStack.removeLast()

          newFragments.add(
            CodeFragment(
              codeLanguageId,
              oldFragmentCode.substring(prevFromPos),
              oldFragment.fromPos + prevFromPos,
              prevSettings,
            )
          )
        }
      }

      newFragments.add(oldFragment)
    }

    return newFragments
  }

  private fun fragmentizeExtraCommands(fragments: List<CodeFragment>): List<CodeFragment> {
    val newFragments = ArrayList<CodeFragment>()

    for (oldFragment: CodeFragment in fragments) {
      val oldFragmentCode: String = oldFragment.code
      val oldFragmentSettings: Settings = oldFragment.settings
      EXTRA_COMMAND_SIGNATURE_MATCHER.startMatching(
        oldFragmentCode,
        getIgnoreCommandPrototypes(oldFragmentSettings)
      )

      while (true) {
        val match: LatexCommandSignatureMatch =
            EXTRA_COMMAND_SIGNATURE_MATCHER.findNextMatch() ?: break
        val contents: String = match.getArgumentContents(match.getArgumentsSize() - 1)
        val contentsFromPos: Int = match.getArgumentContentsFromPos(match.getArgumentsSize() - 1)

        newFragments.add(
          CodeFragment(
            codeLanguageId,
            contents,
            oldFragment.fromPos + contentsFromPos,
            oldFragmentSettings,
          )
        )
      }

      newFragments.add(oldFragment)
    }

    return newFragments
  }

  companion object {
    private val COMMENT_REGEX = Regex(
      "^[ \t]*%[ \t]*(?i)ltex(?-i):(.*?)$",
      RegexOption.MULTILINE,
    )

    private val EXTRA_COMMAND_SIGNATURE_MATCHER = LatexCommandSignatureMatcher(
      listOf(
        LatexCommandSignature("\\footnote{}"),
        LatexCommandSignature("\\footnote[]{}"),
        LatexCommandSignature("\\todo{}"),
        LatexCommandSignature("\\todo[]{}"),
      )
    )

    private val LANGUAGE_TAG_REPLACEMENT_REGEX = Regex("[^A-Za-z]+")

    val BABEL_LANGUAGE_MAP: Map<String, String> = run {
      val map = HashMap<String, String>()

      map["ar"] = "ar"
      map["ast"] = "ast-ES"
      map["ast-ES"] = "ast-ES"
      map["be"] = "be-BY"
      map["be-BY"] = "be-BY"
      map["br"] = "br-FR"
      map["br-FR"] = "br-FR"
      map["ca"] = "ca-ES"
      map["ca-ES"] = "ca-ES"
      map["ca-ES-valencia"] = "ca-ES-valencia"
      map["da"] = "da-DK"
      map["da-DK"] = "da-DK"
      map["de"] = "de"
      map["de-AT"] = "de-AT"
      map["de-CH"] = "de-CH"
      map["de-DE"] = "de-DE"
      map["de-DE-x-simple-language"] = "de-DE-x-simple-language"
      map["el"] = "el-GR"
      map["el-GR"] = "el-GR"
      map["en"] = "en"
      map["en-AU"] = "en-AU"
      map["en-CA"] = "en-CA"
      map["en-GB"] = "en-GB"
      map["en-NZ"] = "en-NZ"
      map["en-US"] = "en-US"
      map["en-ZA"] = "en-ZA"
      map["eo"] = "eo"
      map["es"] = "es"
      map["fa"] = "fa"
      map["fr"] = "fr"
      map["ga"] = "ga-IE"
      map["ga-IE"] = "ga-IE"
      map["gl"] = "gl-ES"
      map["gl-ES"] = "gl-ES"
      // clash with \textit
      // map.put("it", "it");
      map["ja"] = "ja-JP"
      map["ja-JP"] = "ja-JP"
      map["km"] = "km-KH"
      map["km-KH"] = "km-KH"
      map["nl"] = "nl"
      map["pl"] = "pl-PL"
      map["pl-PL"] = "pl-PL"
      map["pt"] = "pt"
      map["pt-AO"] = "pt-AO"
      map["pt-BR"] = "pt-BR"
      map["pt-MZ"] = "pt-MZ"
      map["pt-PT"] = "pt-PT"
      map["ro"] = "ro-RO"
      map["ro-RO"] = "ro-RO"
      map["ru"] = "ru-RU"
      map["ru-RU"] = "ru-RU"
      map["sk"] = "sk-SK"
      map["sk-SK"] = "sk-SK"
      // clash with \textsl
      // map.put("sl", "sl-SI");
      map["sl-SI"] = "sl-SI"
      map["sv"] = "sv"
      map["ta"] = "ta-IN"
      map["ta-IN"] = "ta-IN"
      map["tl"] = "tl-PH"
      map["tl-PH"] = "tl-PH"
      map["uk"] = "uk-UA"
      map["uk-UA"] = "uk-UA"
      map["zh"] = "zh-CN"
      map["zh-CN"] = "zh-CN"
      map["arabic"] = "ar"
      map["asturian"] = "ast-ES"
      map["belarusian"] = "be-BY"
      map["brazil"] = "pt-BR"
      map["brazilian"] = "pt-BR"
      map["catalan"] = "ca-ES"
      map["danish"] = "da-DK"
      map["german"] = "de-DE"
      map["ngerman"] = "de-DE"
      map["greek"] = "el-GR"
      map["british"] = "en-GB"
      map["UKenglish"] = "en-GB"
      map["american"] = "en-US"
      map["english"] = "en-US"
      map["USenglish"] = "en-US"
      map["esperanto"] = "eo"
      map["estonian"] = "es"
      map["farsi"] = "fa"
      map["french"] = "fr"
      map["irish"] = "ga-IE"
      map["galician"] = "gl-ES"
      map["italian"] = "it"
      map["japanese"] = "ja-JP"
      map["khmer"] = "km-KH"
      map["dutch"] = "nl"
      map["polish"] = "pl-PL"
      map["portuges"] = "pt-PT"
      map["portuguese"] = "pt-PT"
      map["romanian"] = "ro-RO"
      map["russian"] = "ru-RU"
      map["slovak"] = "sk-SK"
      map["slovene"] = "sl-SI"
      map["swedish"] = "sv"
      map["tamil"] = "ta-IN"
      map["ukrainian"] = "uk-UA"
      map["chinese"] = "zh-CN"

      map
    }

    private val USE_PACKAGE_COMMAND_SIGNATURE = LatexCommandSignature("\\usepackage[]{}")
    private val USE_PACKAGE_COMMAND_SIGNATURE_MATCHER = LatexCommandSignatureMatcher(
      listOf(USE_PACKAGE_COMMAND_SIGNATURE)
    )

    private val BABEL_SWITCH_COMMAND_SIGNATURE = LatexCommandSignature("\\selectlanguage{}")
    private val BABEL_SWITCH_COMMAND_SIGNATURE_MATCHER = LatexCommandSignatureMatcher(
      listOf(BABEL_SWITCH_COMMAND_SIGNATURE)
    )

    private val BABEL_INLINE_COMMAND_SIGNATURE_MAP = run {
      val map = HashMap<LatexCommandSignature, String>()

      map[LatexCommandSignature("\\foreignlanguage{}{}")] = ""
      map[LatexCommandSignature("\\foreignlanguage[]{}{}")] = ""

      for ((key: String, value: String) in BABEL_LANGUAGE_MAP) {
        val languageTag: String = convertBabelLanguageToLanguageTag(key)
        map[LatexCommandSignature("\\text$languageTag{}")] = value
      }

      map
    }

    private val BABEL_INLINE_COMMAND_SIGNATURE_MATCHER = LatexCommandSignatureMatcher(
      BABEL_INLINE_COMMAND_SIGNATURE_MAP.keys.toList()
    )

    private val BABEL_ENVIRONMENT_COMMAND_SIGNATURE_MAP: Map<LatexCommandSignature, String> = run {
      val map = HashMap<LatexCommandSignature, String>()

      map[LatexCommandSignature("\\begin{otherlanguage}{}")] = ""
      map[LatexCommandSignature("\\begin{otherlanguage*}{}")] = ""
      map[LatexCommandSignature("\\begin{otherlanguage*}[]{}")] = ""
      map[LatexCommandSignature("\\end{otherlanguage}")] = ""
      map[LatexCommandSignature("\\end{otherlanguage*}")] = ""

      for ((languageTag0: String, value: String) in BABEL_LANGUAGE_MAP) {
        val languageTag1 = convertBabelLanguageToLanguageTag(languageTag0)
        for (i in 0 .. 1) {
          val languageTag = if (i == 0) languageTag0 else languageTag1
          if ((i == 1) && (languageTag0.length == languageTag1.length)) continue

          map[LatexCommandSignature("\\begin{$languageTag}")] = value
          map[LatexCommandSignature("\\begin{$languageTag}[]")] = value
          map[LatexCommandSignature("\\end{$languageTag}")] = value
        }
      }

      map
    }

    private val BABEL_ENVIRONMENT_COMMAND_SIGNATURE_MATCHER = LatexCommandSignatureMatcher(
      BABEL_ENVIRONMENT_COMMAND_SIGNATURE_MAP.keys.toList()
    )

    private fun getIgnoreCommandPrototypes(settings: Settings): Set<String> {
      val ignoreCommandPrototypes = HashSet<String>()

      for ((key, value) in settings.latexCommands) {
        if (value == "ignore") ignoreCommandPrototypes.add(key)
      }

      return ignoreCommandPrototypes
    }

    fun convertBabelLanguageToLanguageTag(language: String): String {
      return LANGUAGE_TAG_REPLACEMENT_REGEX.replace(language, "")
    }
  }
}
