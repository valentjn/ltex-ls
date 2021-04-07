/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing;

public class DummyGenerator {
  private boolean plural;

  private static final DummyGenerator defaultGenerator = new DummyGenerator();
  private static final DummyGenerator defaultGeneratorPlural = new DummyGenerator(true);

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

  public String generate(String language, int number) {
    return generate(language, number, false);
  }

  public String generate(String language, int number, boolean startsWithVowel) {
    if (language.equalsIgnoreCase("fr")) {
      return "Jimmy-" + number;
    } else {
      if (this.plural) {
        return "Dummies";
      } else {
        return (startsWithVowel ? "Ina" : "Dummy") + number;
      }
    }
  }

  public boolean isPlural() {
    return this.plural;
  }
}
