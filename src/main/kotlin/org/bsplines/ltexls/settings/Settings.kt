/* Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.settings

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import org.bsplines.ltexls.tools.FileIo
import org.eclipse.lsp4j.DiagnosticSeverity
import java.util.logging.Level

data class Settings(
  private val _enabled: Set<String>? = null,
  private val _languageShortCode: String? = null,
  private val _allDictionaries: Map<String, Set<String>>? = null,
  private val _allDisabledRules: Map<String, Set<String>>? = null,
  private val _allEnabledRules: Map<String, Set<String>>? = null,
  private val _allHiddenFalsePositives: Map<String, Set<HiddenFalsePositive>>? = null,
  private val _bibtexFields: Map<String, Boolean>? = null,
  private val _latexCommands: Map<String, String>? = null,
  private val _latexEnvironments: Map<String, String>? = null,
  private val _markdownNodes: Map<String, String>? = null,
  private val _enablePickyRules: Boolean? = null,
  private val _motherTongueShortCode: String? = null,
  private val _languageModelRulesDirectory: String? = null,
  private val _neuralNetworkModelRulesDirectory: String? = null,
  private val _word2VecModelRulesDirectory: String? = null,
  private val _languageToolHttpServerUri: String? = null,
  private val _languageToolOrgUsername: String? = null,
  private val _languageToolOrgApiKey: String? = null,
  private val _logLevel: Level? = null,
  private val _sentenceCacheSize: Long? = null,
  private val _diagnosticSeverity: Map<String, DiagnosticSeverity>? = null,
  private val _checkFrequency: CheckFrequency? = null,
  private val _clearDiagnosticsWhenClosingFile: Boolean? = null,
) {
  val enabled: Set<String>
    get() = (this._enabled ?: DEFAULT_ENABLED)
  val languageShortCode: String
    get() = (this._languageShortCode ?: "en-US")
  val dictionary: Set<String>
    get() = (this._allDictionaries?.get(this.languageShortCode) ?: setOf())
  val disabledRules: Set<String>
    get() = (this._allDisabledRules?.get(this.languageShortCode) ?: setOf())
  val enabledRules: Set<String>
    get() = (this._allEnabledRules?.get(this.languageShortCode) ?: setOf())
  val hiddenFalsePositives: Set<HiddenFalsePositive>
    get() = (this._allHiddenFalsePositives?.get(this.languageShortCode) ?: setOf())
  val bibtexFields: Map<String, Boolean>
    get() = (this._bibtexFields ?: mapOf())
  val latexCommands: Map<String, String>
    get() = (this._latexCommands ?: mapOf())
  val latexEnvironments: Map<String, String>
    get() = (this._latexEnvironments ?: mapOf())
  val markdownNodes: Map<String, String>
    get() = (this._markdownNodes ?: mapOf())
  val enablePickyRules: Boolean
    get() = (this._enablePickyRules ?: false)
  val motherTongueShortCode: String
    get() = (this._motherTongueShortCode ?: "")
  val languageModelRulesDirectory: String
    get() = FileIo.normalizePath(this._languageModelRulesDirectory ?: "")
  val neuralNetworkModelRulesDirectory: String
    get() = FileIo.normalizePath(this._neuralNetworkModelRulesDirectory ?: "")
  val word2VecModelRulesDirectory: String
    get() = FileIo.normalizePath(this._word2VecModelRulesDirectory ?: "")
  val languageToolHttpServerUri: String
    get() = (this._languageToolHttpServerUri ?: "")
  val languageToolOrgUsername: String
    get() = (this._languageToolOrgUsername ?: "")
  val languageToolOrgApiKey: String
    get() = (this._languageToolOrgApiKey ?: "")
  val logLevel: Level
    get() = (this._logLevel ?: Level.FINE)
  val sentenceCacheSize: Long
    get() = (this._sentenceCacheSize ?: DEFAULT_SENTENCE_CACHE_SIZE)
  val diagnosticSeverity: Map<String, DiagnosticSeverity>
    get() = (this._diagnosticSeverity ?: DEFAULT_DIAGNOSTIC_SEVERITY)
  val checkFrequency: CheckFrequency
    get() = (this._checkFrequency ?: CheckFrequency.Edit)
  val clearDiagnosticsWhenClosingFile: Boolean
    get() = (this._clearDiagnosticsWhenClosingFile ?: true)

  fun getDifferencesRelevantForLanguageTool(other: Settings?): Set<SettingsDifference> {
    val differences = HashSet<SettingsDifference>()

    if (other == null) {
      differences.add(SettingsDifference("settings", "non-null", "null"))
      return differences
    }

    if (enabledRules != other.enabledRules) {
      differences.add(SettingsDifference("enabledRules", this.enabledRules, other.enabledRules))
    }

    if (motherTongueShortCode != other.motherTongueShortCode) {
      differences.add(SettingsDifference("additionalRules.motherTongue",
          this.motherTongueShortCode, other.motherTongueShortCode))
    }

    if (languageModelRulesDirectory != other.languageModelRulesDirectory) {
      differences.add(SettingsDifference("additionalRules.languageModel",
          this.languageModelRulesDirectory, other.languageModelRulesDirectory))
    }

    if (neuralNetworkModelRulesDirectory != other.neuralNetworkModelRulesDirectory) {
      differences.add(SettingsDifference("additionalRules.neuralNetworkModel",
          this.neuralNetworkModelRulesDirectory, other.neuralNetworkModelRulesDirectory))
    }

    if (word2VecModelRulesDirectory != other.word2VecModelRulesDirectory) {
      differences.add(SettingsDifference("additionalRules.word2VecModel",
          this.word2VecModelRulesDirectory, other.word2VecModelRulesDirectory))
    }

    if (languageToolHttpServerUri != other.languageToolHttpServerUri) {
      differences.add(SettingsDifference("ltex-ls.languageToolHttpServerUri",
          this.languageToolHttpServerUri, other.languageToolHttpServerUri))
    }

    if (sentenceCacheSize != other.sentenceCacheSize) {
      differences.add(SettingsDifference("sentenceCacheSize",
          this.sentenceCacheSize, other.sentenceCacheSize))
    }

    return differences
  }

  fun getModifiedDictionary(dictionary: Set<String>): Map<String, Set<String>> {
    val allDictionaries = HashMap<String, Set<String>>(this._allDictionaries ?: emptyMap())
    allDictionaries[this.languageShortCode] = dictionary
    return allDictionaries
  }

  fun getModifiedDisabledRules(disabledRules: Set<String>): Map<String, Set<String>> {
    val allDisabledRules = HashMap<String, Set<String>>(this._allDisabledRules ?: emptyMap())
    allDisabledRules[this.languageShortCode] = disabledRules
    return allDisabledRules
  }

  @Suppress("unused")
  fun getModifiedEnabledRules(enabledRules: Set<String>): Map<String, Set<String>> {
    val allEnabledRules = HashMap<String, Set<String>>(this._allEnabledRules ?: emptyMap())
    allEnabledRules[this.languageShortCode] = enabledRules
    return allEnabledRules
  }

  @Suppress("unused")
  fun getModifiedHiddenFalsePositives(
    hiddenFalsePositives: Set<HiddenFalsePositive>,
  ): Map<String, Set<HiddenFalsePositive>> {
    val allHiddenFalsePositives =
        HashMap<String, Set<HiddenFalsePositive>>(this._allHiddenFalsePositives ?: emptyMap())
    allHiddenFalsePositives[this.languageShortCode] = hiddenFalsePositives
    return allHiddenFalsePositives
  }

  enum class CheckFrequency {
    Edit,
    Save,
    Manual,
  }

  @Suppress("TooManyFunctions")
  companion object {
    val DEFAULT_ENABLED = setOf(
        "bibtex", "latex", "html", "markdown", "org", "restructuredtext", "rsweave")
    private const val DEFAULT_SENTENCE_CACHE_SIZE = 2000L
    private val DEFAULT_DIAGNOSTIC_SEVERITY: Map<String, DiagnosticSeverity> =
        mapOf(Pair("default", DiagnosticSeverity.Information))

    @Suppress("LongMethod")
    fun fromJson(
      jsonSettings: JsonElement,
      jsonWorkspaceSpecificSettings: JsonElement? = null,
    ): Settings {
      val jsonWorkspaceSpecificSettings2 = jsonWorkspaceSpecificSettings ?: jsonSettings

      val enabled: Set<String>? = getEnabledFromJson(jsonSettings)
      val languageShortCode: String? = getSettingFromJsonAsString(jsonSettings, "language")
      val allDictionaries: Map<String, Set<String>>? =
          mergeMapOfListsIntoMapOfSets(convertJsonObjectToMapOfLists(
            getSettingFromJsonAsJsonObject(jsonWorkspaceSpecificSettings2, "dictionary")))
      val allDisabledRules: Map<String, Set<String>>? =
          mergeMapOfListsIntoMapOfSets(convertJsonObjectToMapOfLists(
            getSettingFromJsonAsJsonObject(jsonWorkspaceSpecificSettings2, "disabledRules")))
      val allEnabledRules: Map<String, Set<String>>? =
          mergeMapOfListsIntoMapOfSets(convertJsonObjectToMapOfLists(
            getSettingFromJsonAsJsonObject(jsonWorkspaceSpecificSettings2, "enabledRules")))
      val allHiddenFalsePositives: Map<String, Set<HiddenFalsePositive>>? =
          getAllHiddenFalsePositivesFromJson(jsonWorkspaceSpecificSettings2)
      val bibtexFields: Map<String, Boolean>? = convertJsonObjectToMapOfBooleans(
          getSettingFromJsonAsJsonObject(jsonSettings, "bibtex.fields"))
      val latexCommands: Map<String, String>? = convertJsonObjectToMapOfStrings(
          getSettingFromJsonAsJsonObject(jsonSettings, "latex.commands"))
      val latexEnvironments: Map<String, String>? = convertJsonObjectToMapOfStrings(
          getSettingFromJsonAsJsonObject(jsonSettings, "latex.environments"))
      val markdownNodes: Map<String, String>? = convertJsonObjectToMapOfStrings(
          getSettingFromJsonAsJsonObject(jsonSettings, "markdown.nodes"))
      val enablePickyRules: Boolean? =
          getSettingFromJsonAsBoolean(jsonSettings, "additionalRules.enablePickyRules")
      val motherTongueShortCode: String? =
          getSettingFromJsonAsString(jsonSettings, "additionalRules.motherTongue")
      val languageModelRulesDirectory: String? =
          getSettingFromJsonAsString(jsonSettings, "additionalRules.languageModel")
      val neuralNetworkModelRulesDirectory: String? =
          getSettingFromJsonAsString(jsonSettings, "additionalRules.neuralNetworkModel")
      val word2VecModelRulesDirectory: String? =
          getSettingFromJsonAsString(jsonSettings, "additionalRules.word2VecModel")
      val languageToolHttpServerUri: String? =
          getSettingFromJsonAsString(jsonSettings, "ltex-ls.languageToolHttpServerUri")
      val languageToolOrgUsername: String? =
          getSettingFromJsonAsString(jsonSettings, "ltex-ls.languageToolOrgUsername")
      val languageToolOrgApiKey: String? =
          getSettingFromJsonAsString(jsonSettings, "ltex-ls.languageToolOrgApiKey")
      val logLevel: Level? = getSettingFromJsonAsEnum(
        jsonSettings,
        "ltex-ls.logLevel",
        arrayOf(
          Level.SEVERE,
          Level.WARNING,
          Level.INFO,
          Level.CONFIG,
          Level.FINE,
          Level.FINER,
          Level.FINEST,
        ),
      )
      val sentenceCacheSize: Long? = getSettingFromJsonAsLong(jsonSettings, "sentenceCacheSize")
      val diagnosticSeverity: Map<String, DiagnosticSeverity>? =
          getDiagnosticSeverityFromJson(jsonSettings)
      val checkFrequency: CheckFrequency? = getSettingFromJsonAsEnum(
        jsonSettings,
        "checkFrequency",
        CheckFrequency::class.java.enumConstants,
      )
      val clearDiagnosticsWhenClosingFile: Boolean? =
          getSettingFromJsonAsBoolean(jsonSettings, "clearDiagnosticsWhenClosingFile")

      return Settings(
        enabled,
        languageShortCode,
        allDictionaries,
        allDisabledRules,
        allEnabledRules,
        allHiddenFalsePositives,
        bibtexFields,
        latexCommands,
        latexEnvironments,
        markdownNodes,
        enablePickyRules,
        motherTongueShortCode,
        languageModelRulesDirectory,
        neuralNetworkModelRulesDirectory,
        word2VecModelRulesDirectory,
        languageToolHttpServerUri,
        languageToolOrgUsername,
        languageToolOrgApiKey,
        logLevel,
        sentenceCacheSize,
        diagnosticSeverity,
        checkFrequency,
        clearDiagnosticsWhenClosingFile,
      )
    }

    private fun getEnabledFromJson(jsonSettings: JsonElement): Set<String>? {
      val jsonElement: JsonElement? = getSettingFromJsonAsJsonElement(jsonSettings, "enabled")

      return if (jsonElement == null) {
        null
      } else if (jsonElement.isJsonArray) {
        convertJsonArrayToSet(jsonElement.asJsonArray)
      } else if (jsonElement.isJsonPrimitive) {
        val jsonPrimitive: JsonPrimitive = jsonElement.asJsonPrimitive

        if (jsonPrimitive.isBoolean) {
          if (jsonPrimitive.asBoolean) DEFAULT_ENABLED else emptySet()
        } else {
          null
        }
      } else {
        null
      }
    }

    private fun getAllHiddenFalsePositivesFromJson(
      jsonWorkspaceSpecificSettings: JsonElement,
    ): Map<String, Set<HiddenFalsePositive>>? {
      val hiddenFalsePositiveJsonStringMap: Map<String, Set<String>>? =
          mergeMapOfListsIntoMapOfSets(convertJsonObjectToMapOfLists(
            getSettingFromJsonAsJsonObject(jsonWorkspaceSpecificSettings, "hiddenFalsePositives")))

      return if (hiddenFalsePositiveJsonStringMap != null) {
        val allHiddenFalsePositives = HashMap<String, HashSet<HiddenFalsePositive>>()

        for (
          (curLanguage: String, hiddenFalsePositiveJsonStrings: Set<String>)
          in hiddenFalsePositiveJsonStringMap
        ) {
          val curHiddenFalsePositives: HashSet<HiddenFalsePositive> =
              allHiddenFalsePositives[curLanguage] ?: run {
            val set = HashSet<HiddenFalsePositive>()
            allHiddenFalsePositives[curLanguage] = set
            set
          }

          for (hiddenFalsePositiveJsonString: String in hiddenFalsePositiveJsonStrings) {
            curHiddenFalsePositives.add(
                HiddenFalsePositive.fromJsonString(hiddenFalsePositiveJsonString))
          }
        }

        allHiddenFalsePositives
      } else {
        null
      }
    }

    private fun getDiagnosticSeverityFromJson(
      jsonSettings: JsonElement,
    ): Map<String, DiagnosticSeverity>? {
      val jsonElement: JsonElement? =
          getSettingFromJsonAsJsonElement(jsonSettings, "diagnosticSeverity")

      return if (jsonElement == null) {
        null
      } else if (jsonElement.isJsonObject) {
        convertJsonObjectToMapOfEnums(
          jsonElement.asJsonObject,
          DiagnosticSeverity::class.java.enumConstants,
        )
      } else if (jsonElement.isJsonPrimitive) {
        val jsonPrimitive: JsonPrimitive = jsonElement.asJsonPrimitive

        if (jsonPrimitive.isString) {
          val enumValue: DiagnosticSeverity? = convertStringToEnum(
            jsonPrimitive.asString,
            DiagnosticSeverity::class.java.enumConstants,
          )
          if (enumValue != null) mapOf(Pair("default", enumValue)) else null
        } else {
          null
        }
      } else {
        null
      }
    }

    private fun <T> getSettingFromJsonAsEnum(
      jsonSettings: JsonElement,
      name: String,
      enumValues: Array<T>,
    ): T? {
      val enumString: String = getSettingFromJsonAsString(jsonSettings, name) ?: return null
      return convertStringToEnum(enumString, enumValues)
    }

    private fun getSettingFromJsonAsJsonElement(
      jsonSettings: JsonElement,
      name: String,
    ): JsonElement? {
      if (!jsonSettings.isJsonObject) return null
      var curJsonSettings: JsonElement? = jsonSettings

      for (component: String in name.split(".")) {
        curJsonSettings = if ((curJsonSettings != null) && curJsonSettings.isJsonObject) {
          val curJsonSettingsObject: JsonObject = curJsonSettings.asJsonObject

          if (curJsonSettingsObject.has(component)) {
            curJsonSettingsObject.get(component)
          } else {
            null
          }
        } else {
          null
        }

        if (curJsonSettings == null) break
      }

      return if (curJsonSettings != null) {
        curJsonSettings
      } else {
        val jsonSettingsObject: JsonObject = jsonSettings.asJsonObject

        if (jsonSettingsObject.has(name)) {
          jsonSettingsObject.get(name)
        } else if (!name.startsWith("ltex.")) {
          getSettingFromJsonAsJsonElement(jsonSettings, "ltex.$name")
        } else {
          null
        }
      }
    }

    private fun getSettingFromJsonAsJsonObject(
      jsonSettings: JsonElement,
      name: String,
    ): JsonObject? {
      val jsonElement: JsonElement? = getSettingFromJsonAsJsonElement(jsonSettings, name)

      return if ((jsonElement != null) && jsonElement.isJsonObject) {
        jsonElement.asJsonObject
      } else {
        null
      }
    }

    private fun getSettingFromJsonAsJsonPrimitive(
      jsonSettings: JsonElement,
      name: String,
    ): JsonPrimitive? {
      val jsonElement: JsonElement? = getSettingFromJsonAsJsonElement(jsonSettings, name)

      return if ((jsonElement != null) && jsonElement.isJsonPrimitive) {
        jsonElement.asJsonPrimitive
      } else {
        null
      }
    }

    private fun getSettingFromJsonAsBoolean(jsonSettings: JsonElement, name: String): Boolean? {
      val jsonPrimitive: JsonPrimitive? = getSettingFromJsonAsJsonPrimitive(jsonSettings, name)

      return if ((jsonPrimitive != null) && jsonPrimitive.isBoolean) {
        jsonPrimitive.asBoolean
      } else {
        null
      }
    }

    private fun getSettingFromJsonAsLong(jsonSettings: JsonElement, name: String): Long? {
      val jsonPrimitive: JsonPrimitive? = getSettingFromJsonAsJsonPrimitive(jsonSettings, name)

      return if ((jsonPrimitive != null) && jsonPrimitive.isNumber) {
        jsonPrimitive.asLong
      } else {
        null
      }
    }

    private fun getSettingFromJsonAsString(jsonSettings: JsonElement, name: String): String? {
      val jsonPrimitive: JsonPrimitive? = getSettingFromJsonAsJsonPrimitive(jsonSettings, name)

      return if ((jsonPrimitive != null) && jsonPrimitive.isString) {
        jsonPrimitive.asString
      } else {
        null
      }
    }

    private fun convertJsonArrayToList(array: JsonArray?): List<String>? {
      if (array == null) return null
      val list = ArrayList<String>()

      for (element: JsonElement in array) {
        if (!element.isJsonPrimitive) return null
        val value: JsonPrimitive = element.asJsonPrimitive
        if (!value.isString) return null
        list.add(value.asString)
      }

      return list
    }

    private fun convertJsonArrayToSet(array: JsonArray?): Set<String>? {
      if (array == null) return null
      val set = HashSet<String>()

      for (element: JsonElement in array) {
        if (!element.isJsonPrimitive) return null
        val value: JsonPrimitive = element.asJsonPrimitive
        if (!value.isString) return null
        set.add(value.asString)
      }

      return set
    }

    private fun convertJsonObjectToMapOfStrings(obj: JsonObject?): Map<String, String>? {
      if (obj == null) return null
      val map = HashMap<String, String>()

      for (entry: Map.Entry<String, JsonElement> in obj.entrySet()) {
        if (!entry.value.isJsonPrimitive) return null
        val value: JsonPrimitive = entry.value.asJsonPrimitive
        if (!value.isString) return null
        map[entry.key] = entry.value.asString
      }

      return map
    }

    private fun convertJsonObjectToMapOfBooleans(obj: JsonObject?): Map<String, Boolean>? {
      if (obj == null) return null
      val map = HashMap<String, Boolean>()

      for (entry: Map.Entry<String, JsonElement> in obj.entrySet()) {
        if (!entry.value.isJsonPrimitive) return null
        val value: JsonPrimitive = entry.value.asJsonPrimitive
        if (!value.isBoolean) return null
        map[entry.key] = value.asBoolean
      }

      return map
    }

    private fun convertJsonObjectToMapOfLists(obj: JsonObject?): Map<String, List<String>>? {
      if (obj == null) return null
      val map = HashMap<String, List<String>>()

      for (entry: Map.Entry<String, JsonElement> in obj.entrySet()) {
        if (!entry.value.isJsonArray) return null
        val list: List<String> = convertJsonArrayToList(entry.value.asJsonArray) ?: return null
        map[entry.key] = list
      }

      return map
    }

    private fun <T> convertJsonObjectToMapOfEnums(
      obj: JsonObject?,
      enumValues: Array<T>,
    ): Map<String, T>? {
      if (obj == null) return null
      val map = HashMap<String, T>()

      for (entry: Map.Entry<String, JsonElement> in obj.entrySet()) {
        val enumValue: T? = convertJsonElementToEnum(entry.value, enumValues)
        if (enumValue != null) map[entry.key] = enumValue
      }

      return map
    }

    private fun <T> convertJsonElementToEnum(jsonElement: JsonElement, enumValues: Array<T>): T? {
      return if (jsonElement.isJsonPrimitive) {
        val jsonPrimitive: JsonPrimitive = jsonElement.asJsonPrimitive

        if (jsonPrimitive.isString) {
          convertStringToEnum(jsonPrimitive.asString, enumValues)
        } else {
          null
        }
      } else {
        null
      }
    }

    private fun <T> convertStringToEnum(enumString: String, enumValues: Array<T>): T? {
      for (enumValue: T in enumValues) {
        if (enumValue.toString().equals(enumString, ignoreCase = true)) return enumValue
      }

      return null
    }

    private fun mergeMapOfListsIntoMapOfSets(
      mapOfLists: Map<String, List<String>>?,
    ): Map<String, Set<String>>? {
      if (mapOfLists == null) return null
      val mapOfSets = HashMap<String, HashSet<String>>()

      for ((key: String, set2: List<String>) in mapOfLists) {
        val set1 = HashSet<String>()

        for (string: String in set2) {
          if (string.startsWith("-")) {
            set1.remove(string.substring(1))
          } else {
            set1.add(string)
          }
        }

        mapOfSets[key] = set1
      }

      return mapOfSets
    }
  }
}
