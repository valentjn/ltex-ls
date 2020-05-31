package org.bsplines.ltex_ls.parsing;

public class DummyGenerator {
  private static DummyGenerator defaultDummyGenerator = new DummyGenerator();

  public DummyGenerator() {
  }

  public static DummyGenerator getDefault() {
    return defaultDummyGenerator;
  }

  public String generate(String language, int number) {
    if (language.equalsIgnoreCase("fr")) {
      return "Jimmy-" + number;
    } else {
      return "Dummy" + number;
    }
  }
}
