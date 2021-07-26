/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.bibtex

object BibtexFragmentizerDefaults {
  val defaultBibtexFields: Map<String, Boolean> = run {
    val map = HashMap<String, Boolean>()

    map["author"] = false
    map["category"] = false
    map["date"] = false
    map["doi"] = false
    map["edition"] = false
    map["editor"] = false
    map["eid"] = false
    map["file"] = false
    map["isbn"] = false
    map["keywords"] = false
    map["month"] = false
    map["note"] = false
    map["number"] = false
    map["options"] = false
    map["origlanguage"] = false
    map["owner"] = false
    map["pages"] = false
    map["parent"] = false
    map["publisher"] = false
    map["pubstate"] = false
    map["see"] = false
    map["seealso"] = false
    map["shorthand"] = false
    map["timestamp"] = false
    map["translator"] = false
    map["url"] = false
    map["version"] = false
    map["volume"] = false
    map["year"] = false

    map
  }
}
