package org.bsplines.ltex_ls.parsing;

public class DummyGenerator {
  private boolean isPlural_;

  private static DummyGenerator default_ = new DummyGenerator();
  private static DummyGenerator defaultPlural = new DummyGenerator(true);

  public DummyGenerator() {
    this(false);
  }

  public DummyGenerator(boolean isPlural_) {
    this.isPlural_ = isPlural_;
  }

  public static DummyGenerator getDefault() {
    return getDefault(false);
  }

  public static DummyGenerator getDefault(boolean isPlural_) {
    return (isPlural_ ? defaultPlural : default_);
  }

  public String generate(String language, int number) {
    if (language.equalsIgnoreCase("fr")) {
      return "Jimmy-" + number;
    } else {
      return (isPlural_ ? "Dummies" : ("Dummy" + number));
    }
  }

  public boolean isPlural() { return isPlural_; }
}
