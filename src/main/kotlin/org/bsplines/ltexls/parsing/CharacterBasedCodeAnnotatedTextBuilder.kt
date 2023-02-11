/* Copyright (C) 2019-2023 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing

import org.bsplines.ltexls.settings.Settings
import org.bsplines.ltexls.tools.I18n
import org.bsplines.ltexls.tools.Logging

abstract class CharacterBasedCodeAnnotatedTextBuilder(
  codeLanguageId: String,
) : CodeAnnotatedTextBuilder(codeLanguageId) {
  protected var code = ""
  protected var pos = 0
  protected var curChar = '\u0000'
  protected var curString = ""
  protected var isStartOfLine = false

  protected var dummyGenerator = DummyGenerator.getInstance()
  protected var dummyCounter = 0

  protected var language: String = "en-US"

  var isPreventingInfiniteLoops = false

  override fun setSettings(settings: Settings) {
    super.setSettings(settings)
    this.language = settings.languageShortCode
  }

  override fun addText(text: String?): CharacterBasedCodeAnnotatedTextBuilder {
    if (text?.isNotEmpty() == true) {
      super.addText(text)
      this.pos += text.length
    }

    return this
  }

  override fun addMarkup(markup: String?): CharacterBasedCodeAnnotatedTextBuilder {
    if (markup?.isNotEmpty() == true) {
      super.addMarkup(markup)
      this.pos += markup.length
    }

    return this
  }

  override fun addMarkup(
    markup: String?,
    interpretAs: String?,
  ): CharacterBasedCodeAnnotatedTextBuilder {
    if (interpretAs?.isNotEmpty() == true) {
      super.addMarkup((markup ?: ""), interpretAs)
      this.pos += (markup?.length ?: 0)
    } else {
      addMarkup(markup)
    }

    return this
  }

  override fun addCode(code: String): CharacterBasedCodeAnnotatedTextBuilder {
    this.pos = this.code.length
    this.code += code

    while (this.pos < this.code.length) {
      val lastPos: Int = this.pos
      this.curChar = this.code[this.pos]
      this.curString = this.curChar.toString()
      this.isStartOfLine = ((this.pos == 0) || this.code[this.pos - 1] == '\n')
      processCharacter()

      if (this.pos <= lastPos) {
        if (this.isPreventingInfiniteLoops) {
          throw IllegalStateException(
            I18n.format("characterBasedCodeAnnotatedTextBuilderInfiniteLoop"),
          )
        } else {
          Logging.logger.warning(
            I18n.format("characterBasedCodeAnnotatedTextBuilderPreventedInfiniteLoop"),
          )
          this.pos++
        }
      }
    }

    return this
  }

  protected abstract fun processCharacter()

  protected fun matchFromPosition(regex: Regex, pos: Int = this.pos): MatchResult? {
    val matchResult: MatchResult? = regex.find(this.code.substring(pos))
    return if ((matchResult != null) && matchResult.value.isNotEmpty()) matchResult else null
  }

  protected open fun generateDummy(): String {
    return this.dummyGenerator.generate(this.language, this.dummyCounter++)
  }
}
