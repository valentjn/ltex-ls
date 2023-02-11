/* Copyright (C) 2019-2023 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.languagetool

import org.apache.commons.text.StringEscapeUtils
import org.bsplines.ltexls.parsing.AnnotatedTextFragment
import org.bsplines.ltexls.tools.I18n
import org.bsplines.ltexls.tools.Logging
import org.bsplines.ltexls.tools.Tools
import org.languagetool.AnalyzedSentence
import org.languagetool.AnalyzedTokenReadings
import org.languagetool.JLanguageTool
import org.languagetool.Language
import org.languagetool.Languages
import org.languagetool.ResultCache
import org.languagetool.RuleMatchListener
import org.languagetool.UserConfig
import org.languagetool.rules.Rule
import org.languagetool.rules.RuleMatch
import org.languagetool.rules.patterns.AbstractPatternRule
import org.xml.sax.SAXException
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.io.PrintStream
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import javax.xml.parsers.ParserConfigurationException

class LanguageToolJavaInterface(
  languageShortCode: String,
  motherTongueShortCode: String,
  sentenceCacheSize: Long,
  dictionary: Set<String>,
) : LanguageToolInterface() {
  private val resultCache =
      ResultCache(sentenceCacheSize, RESULT_CACHE_EXPIRE_AFTER_MINUTES, TimeUnit.MINUTES)
  private val languageTool: JLanguageTool?

  init {
    if (Languages.isLanguageSupported(languageShortCode)) {
      val language: Language = Languages.getLanguageForShortCode(languageShortCode)
      val motherTongue: Language? = if (motherTongueShortCode.isNotEmpty()) {
        Languages.getLanguageForShortCode(motherTongueShortCode)
      } else {
        null
      }
      val userConfig = UserConfig(dictionary.toList())

      this.languageTool = JLanguageTool(language, motherTongue, this.resultCache, userConfig)
    } else {
      Logging.LOGGER.severe(I18n.format("notARecognizedLanguage", languageShortCode))
      this.languageTool = null
    }
  }

  override fun isInitialized(): Boolean {
    return (this.languageTool != null)
  }

  @Suppress("INACCESSIBLE_TYPE")
  override fun checkInternal(
    annotatedTextFragment: AnnotatedTextFragment,
  ): List<LanguageToolRuleMatch> {
    val languageTool: JLanguageTool? = this.languageTool

    if (languageTool == null) {
      Logging.LOGGER.warning(I18n.format("skippingTextCheckAsLanguageToolHasNotBeenInitialized"))
      return emptyList()
    }

    logResultCache()

    val ruleMatchListener: RuleMatchListener? = null
    val ruleLevel: JLanguageTool.Level =
    if (annotatedTextFragment.codeFragment.settings.enablePickyRules) {
      JLanguageTool.Level.PICKY
    } else {
      JLanguageTool.Level.DEFAULT
    }

    annotatedTextFragment.document.raiseExceptionIfCanceled()
    languageTool.setCheckCancelledCallback(
      JLanguageTool.CheckCancelledCallback {
        annotatedTextFragment.document.raiseExceptionIfCanceled()
        false
      },
    )

    val matches: List<RuleMatch> = try {
      // workaround bugs like https://github.com/languagetool-org/languagetool/issues/3181,
      // in which LT prints to stdout instead of stderr (this messes up the LSP communication
      // and results in a deadlock) => temporarily discard output to stdout
      val stdout: PrintStream = System.out
      System.setOut(
        PrintStream(
          object : OutputStream() {
            override fun write(b: Int) {
            }
          },
          false,
          "utf-8",
        ),
      )

      try {
        languageTool.check(
          annotatedTextFragment.annotatedText,
          true,
          JLanguageTool.ParagraphHandling.NORMAL,
          ruleMatchListener,
          JLanguageTool.Mode.ALL,
          ruleLevel,
        )
      } finally {
        System.setOut(stdout)
      }
    } catch (e: IOException) {
      Tools.rethrowCancellationException(e)
      Logging.LOGGER.severe(I18n.format("languageToolFailed", e))
      return emptyList()
    }

    annotatedTextFragment.document.raiseExceptionIfCanceled()
    val result = ArrayList<LanguageToolRuleMatch>()

    for (match: RuleMatch in matches) {
      result.add(LanguageToolRuleMatch.fromLanguageTool(match, annotatedTextFragment))
    }

    return result
  }

  @Suppress("INACCESSIBLE_TYPE")
  private fun logResultCache() {
    if (Logging.LOGGER.isLoggable(Level.FINER)) {
      Logging.LOGGER.finer("matchesCache.size() = " + this.resultCache.matchesCache.size())
      Logging.LOGGER.finer(
        "remoteMatchesCache.size() = " + this.resultCache.remoteMatchesCache.size(),
      )
      Logging.LOGGER.finer("sentenceCache.size() = " + this.resultCache.sentenceCache.size())

      if (Logging.LOGGER.isLoggable(Level.FINEST)) {
        Logging.LOGGER.finest(
          "matchesCache = " + mapToString(this.resultCache.matchesCache.asMap()),
        )
        Logging.LOGGER.finest(
          "remoteMatchesCache = " + mapToString(this.resultCache.remoteMatchesCache.asMap()),
        )
        Logging.LOGGER.finest(
          "sentenceCache = " + mapToString(this.resultCache.sentenceCache.asMap()),
        )
      }
    }
  }

  override fun activateDefaultFalseFriendRules() {
    val languageTool: JLanguageTool = (this.languageTool ?: return)

    // from JLanguageTool.activateDefaultFalseFriendRules (which is private)
    val falseFriendRulePath: String =
        JLanguageTool.getDataBroker().rulesDir + "/" + JLanguageTool.FALSE_FRIEND_FILE
    var exception: Exception? = null

    try {
      val falseFriendRules: List<AbstractPatternRule> =
          languageTool.loadFalseFriendRules(falseFriendRulePath)
      for (rule: Rule in falseFriendRules) languageTool.addRule(rule)
    } catch (e: IOException) {
      exception = e
    } catch (e: ParserConfigurationException) {
      exception = e
    } catch (e: SAXException) {
      exception = e
    }

    if (exception != null) {
      Logging.LOGGER.warning(
        I18n.format("couldNotLoadFalseFriendRules", exception, falseFriendRulePath),
      )
    }
  }

  override fun activateLanguageModelRules(languageModelRulesDirectory: String) {
    val languageTool: JLanguageTool = (this.languageTool ?: return)

    try {
      languageTool.activateLanguageModelRules(File(languageModelRulesDirectory))
    } catch (e: IOException) {
      Logging.LOGGER.warning(
        I18n.format("couldNotLoadLanguageModel", e, languageModelRulesDirectory),
      )
    }
  }

  override fun activateNeuralNetworkRules(neuralNetworkRulesDirectory: String) {
    val languageTool: JLanguageTool = (this.languageTool ?: return)

    try {
      languageTool.activateNeuralNetworkRules(File(neuralNetworkRulesDirectory))
    } catch (e: IOException) {
      Logging.LOGGER.warning(
        I18n.format("couldNotLoadNeuralNetworkModel", e, neuralNetworkRulesDirectory),
      )
    }
  }

  override fun activateWord2VecModelRules(word2vecRulesDirectory: String) {
    val languageTool: JLanguageTool = (this.languageTool ?: return)

    try {
      languageTool.activateWord2VecModelRules(File(word2vecRulesDirectory))
    } catch (e: IOException) {
      Logging.LOGGER.warning(I18n.format("couldNotLoadWord2VecModel", e, word2vecRulesDirectory))
    }
  }

  override fun enableRules(ruleIds: Set<String>) {
    val languageTool: JLanguageTool = (this.languageTool ?: return)

    // for strange reasons, there is no JLanguageTool.enableRules
    for (ruleId: String in ruleIds) {
      languageTool.enableRule(ruleId)
    }
  }

  override fun enableEasterEgg() {
    val languageTool: JLanguageTool = (this.languageTool ?: return)

    languageTool.addRule(
      object : Rule() {
        override fun getId(): String {
          return "bspline"
        }

        override fun getDescription(): String {
          return "Unknown basis function"
        }

        override fun match(sentence: AnalyzedSentence): Array<RuleMatch> {
          val matches = ArrayList<RuleMatch>()

          for (token: AnalyzedTokenReadings in sentence.tokens) {
            if (token.token.equals("hat", ignoreCase = true)) {
              matches.add(
                RuleMatch(
                  this,
                  sentence,
                  token.startPos,
                  token.endPos,
                  "Unknown basis function. Did you mean <suggestion>B-spline</suggestion>?",
                ),
              )
            }
          }

          return matches.toTypedArray()
        }
      },
    )

    languageTool.addRule(
      object : Rule() {
        override fun getId(): String {
          return "ungendered"
        }

        override fun getDescription(): String {
          return "Ungendered variant"
        }

        override fun match(sentence: AnalyzedSentence): Array<RuleMatch> {
          val matches = ArrayList<RuleMatch>()

          for (token: AnalyzedTokenReadings in sentence.tokens) {
            val tokenString: String = token.token

            if (
              (tokenString.length >= 2)
              && tokenString.substring(tokenString.length - 2).equals("er", ignoreCase = true)
            ) {
              matches.add(
                RuleMatch(
                  this,
                  sentence,
                  token.startPos,
                  token.endPos,
                  "Ungendered variant detected. "
                  + "Did you mean <suggestion>$tokenString*in</suggestion>?",
                ),
              )
            }
          }

          return matches.toTypedArray()
        }
      },
    )
  }

  companion object {
    private const val RESULT_CACHE_EXPIRE_AFTER_MINUTES = 60L

    fun mapToString(map: Map<*, *>?): String {
      if (map == null) return "null"
      val builder = StringBuilder("{")

      for ((key: Any?, value: Any?) in map) {
        if (builder.length > 1) builder.append(", ")
        appendObjectToBuilder(key, builder)
        builder.append(": ")
        appendObjectToBuilder(value, builder)
      }

      builder.append("}")
      return builder.toString()
    }

    private fun appendObjectToBuilder(obj: Any?, builder: StringBuilder) {
      if (obj != null) {
        builder.append("\"")
        builder.append(StringEscapeUtils.escapeJava(obj.toString()))
        builder.append("\"")
      } else {
        builder.append("null")
      }
    }
  }
}
