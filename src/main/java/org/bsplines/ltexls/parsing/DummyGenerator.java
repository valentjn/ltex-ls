package org.bsplines.ltexls.parsing;

public class DummyGenerator {
  private boolean plural;

  private static DummyGenerator defaultGenerator = new DummyGenerator();
  private static DummyGenerator defaultGeneratorPlural = new DummyGenerator(true);

  public DummyGenerator() {
    this(false);
  }

  public DummyGenerator(boolean plural) {
    this.plural = plural;
  }

  public static DummyGenerator getDefault() {
    return getDefault(false);
  }

  public static DummyGenerator getDefault(boolean plural) {
    return (plural ? defaultGeneratorPlural : defaultGenerator);
  }

  /**
   * Generate dummy.
   *
   * @param language short code of the language
   * @param number counter for the dummy, should be increased after calling this function
   * @return dummy
   */
  public String generate(String language, int number) {
    if (language.equalsIgnoreCase("fr")) {
      return "Jimmy-" + number;
    } else {
      return (plural ? "Dummies" : ("Dummy" + number));
    }
  }

  public boolean isPlural() {
    return plural;
  }
}
