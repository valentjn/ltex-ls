import java.util.ArrayList;
import java.util.List;

import com.google.gson.*;

public class Settings {
  private String languageShortCode = "en-US";
  private List<String> dictionary = null;
  private List<String> dummyCommandPrototypes = null;
  private List<String> ignoreCommandPrototypes = null;

  private static JsonElement getSettingFromJSON(JsonElement jsonSettings, String name) {
    for (String component : name.split("\\.")) {
      jsonSettings = jsonSettings.getAsJsonObject().get(component);
    }

    return jsonSettings;
  }

  private static List<String> convertJsonArrayToList(JsonArray array) {
    List<String> result = new ArrayList<>();
    for (JsonElement element : array) result.add(element.getAsString());
    return result;
  }

  public void setSettings(JsonElement jsonSettings) {
    languageShortCode = getSettingFromJSON(jsonSettings, "ltex.language").getAsString();

    String languagePrefix = languageShortCode;
    int dashPos = languagePrefix.indexOf("-");
    if (dashPos != -1) languagePrefix = languagePrefix.substring(0, dashPos);
    dictionary = convertJsonArrayToList(
        getSettingFromJSON(jsonSettings, "ltex." + languagePrefix + ".dictionary").
        getAsJsonArray());

    dummyCommandPrototypes = convertJsonArrayToList(
        getSettingFromJSON(jsonSettings, "ltex.commands.dummy").getAsJsonArray());
    ignoreCommandPrototypes = convertJsonArrayToList(
        getSettingFromJSON(jsonSettings, "ltex.commands.ignore").getAsJsonArray());
  }

  @Override
  public Object clone() {
    Settings obj = new Settings();
    obj.languageShortCode = languageShortCode;
    obj.dictionary = ((dictionary == null) ? null : new ArrayList<>(dictionary));
    obj.dummyCommandPrototypes = ((dummyCommandPrototypes == null) ? null :
        new ArrayList<>(dummyCommandPrototypes));
    obj.ignoreCommandPrototypes = ((ignoreCommandPrototypes == null) ? null :
        new ArrayList<>(ignoreCommandPrototypes));
    return obj;
  }

  @Override
  public boolean equals(Object obj) {
    if ((obj == null) || !Settings.class.isAssignableFrom(obj.getClass())) return false;
    Settings other = (Settings) obj;

    if ((languageShortCode == null) ? (other.languageShortCode != null) :
        !languageShortCode.equals(other.languageShortCode)) {
      return false;
    }

    if ((dictionary == null) ? (other.dictionary != null) :
        !dictionary.equals(other.dictionary)) {
      return false;
    }

    if ((dummyCommandPrototypes == null) ? (other.dummyCommandPrototypes != null) :
        !dummyCommandPrototypes.equals(other.dummyCommandPrototypes)) {
      return false;
    }

    if ((ignoreCommandPrototypes == null) ? (other.ignoreCommandPrototypes != null) :
        !ignoreCommandPrototypes.equals(other.ignoreCommandPrototypes)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 53 * hash + (languageShortCode != null ? languageShortCode.hashCode() : 0);
    hash = 53 * hash + (dictionary != null ? dictionary.hashCode() : 0);
    hash = 53 * hash + (dummyCommandPrototypes != null ? dummyCommandPrototypes.hashCode() : 0);
    hash = 53 * hash + (ignoreCommandPrototypes != null ? ignoreCommandPrototypes.hashCode() : 0);
    return hash;
  }

  public String getLanguageShortCode() {
    return languageShortCode;
  }

  public List<String> getDictionary() {
    return dictionary;
  }

  public List<String> getDummyCommandPrototypes() {
    return dummyCommandPrototypes;
  }

  public List<String> getIgnoreCommandPrototypes() {
    return ignoreCommandPrototypes;
  }
}
