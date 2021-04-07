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

    bibtexFields.put("author", false);
    bibtexFields.put("category", false);
    bibtexFields.put("date", false);
    bibtexFields.put("doi", false);
    bibtexFields.put("edition", false);
    bibtexFields.put("editor", false);
    bibtexFields.put("eid", false);
    bibtexFields.put("file", false);
    bibtexFields.put("isbn", false);
    bibtexFields.put("keywords", false);
    bibtexFields.put("month", false);
    bibtexFields.put("note", false);
    bibtexFields.put("number", false);
    bibtexFields.put("options", false);
    bibtexFields.put("origlanguage", false);
    bibtexFields.put("owner", false);
    bibtexFields.put("pages", false);
    bibtexFields.put("parent", false);
    bibtexFields.put("publisher", false);
    bibtexFields.put("pubstate", false);
    bibtexFields.put("see", false);
    bibtexFields.put("seealso", false);
    bibtexFields.put("shorthand", false);
    bibtexFields.put("timestamp", false);
    bibtexFields.put("translator", false);
    bibtexFields.put("url", false);
    bibtexFields.put("version", false);
    bibtexFields.put("volume", false);
    bibtexFields.put("year", false);

    return bibtexFields;
  }

  private BibtexFragmentizerDefaults() {
  }

  public static Map<String, Boolean> getDefaultBibtexFields() {
    return Collections.unmodifiableMap(defaultBibtexFields);
  }
}
