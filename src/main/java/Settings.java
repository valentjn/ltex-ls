import java.util.ArrayList;
import java.util.List;

public class Settings {
  public String languageShortCode = "en-US";
  public List<String> dictionary = null;
  public List<String> dummyCommandPrototypes = null;
  public List<String> ignoreCommandPrototypes = null;

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
}
