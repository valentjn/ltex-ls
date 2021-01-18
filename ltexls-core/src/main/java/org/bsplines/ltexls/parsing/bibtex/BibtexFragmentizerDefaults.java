/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.bibtex;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class BibtexFragmentizerDefaults {
  private static final Map<String, Boolean> defaultBibtexFields = createDefaultBibtexFields();

  private static Map<String, Boolean> createDefaultBibtexFields() {
    Map<String, Boolean> bibtexFields = new HashMap<>();

    bibtexFields.put("see", false);
    bibtexFields.put("see-also", false);

    return bibtexFields;
  }

  private BibtexFragmentizerDefaults() {
  }

  public static Map<String, Boolean> getDefaultBibtexFields() {
    return Collections.unmodifiableMap(defaultBibtexFields);
  }
}
