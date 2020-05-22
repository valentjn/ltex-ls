/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool.markup;

import org.apache.commons.lang3.StringUtils;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A text with markup and with a mapping so error positions will refer to the original
 * position that includes the markup, even though only the plain text parts are checked.
 * Use {@link AnnotatedTextBuilder} to create objects of this type.
 * @since 2.3
 */
public class AnnotatedText {

  /**
   * @since 3.9
   */
  public enum MetaDataKey {
    DocumentTitle,
    EmailToAddress,
    EmailNumberOfAttachments
  }

  private class MappingEntryComparator implements Comparator<Map.Entry<Integer, Integer>> {
    @Override
    public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
      int result = o1.getKey().compareTo(o2.getKey());
      if (result == 0) result = o1.getValue().compareTo(o2.getValue());
      return result;
    }
  }

  private List<TextPart> parts;
  private List<Map.Entry<Integer, Integer>> mapping;  // plain text position to original text (with markup) position
  private MappingEntryComparator mappingEntryComparator;
  private Map<MetaDataKey, String> metaData;
  private Map<String, String> customMetaData;

  public AnnotatedText(List<TextPart> parts, List<Map.Entry<Integer, Integer>> mapping,
      Map<MetaDataKey, String> metaData, Map<String, String> customMetaData) {
    initialize(parts, mapping, metaData, customMetaData);
  }

  public AnnotatedText(List<TextPart> parts, Map<Integer, MappingValue> mapping,
      Map<MetaDataKey, String> metaData, Map<String, String> customMetaData) {
    List<Map.Entry<Integer, Integer>> integerMapping = new ArrayList<>();

    for (Map.Entry<Integer, MappingValue> entry : mapping.entrySet()) {
      integerMapping.add(new AbstractMap.SimpleEntry<>(
          entry.getKey(), entry.getValue().getTotalPosition()));
    }

    initialize(parts, integerMapping, metaData, customMetaData);
  }

  public void initialize(List<TextPart> parts, List<Map.Entry<Integer, Integer>> mapping,
      Map<MetaDataKey, String> metaData, Map<String, String> customMetaData) {
    this.parts = Objects.requireNonNull(parts);
    this.mapping = copyMapping(Objects.requireNonNull(mapping));
    this.mappingEntryComparator = new MappingEntryComparator();
    Collections.sort(this.mapping, this.mappingEntryComparator);
    this.metaData = Objects.requireNonNull(metaData);
    this.customMetaData = Objects.requireNonNull(customMetaData);
  }

  /**
   * Get the plain text, without markup and content from {@code interpretAs}.
   * @since 4.3
   */
  public String getOriginalText() {
    StringBuilder sb = new StringBuilder();
    for (TextPart part : parts) {
      if (part.getType() == TextPart.Type.TEXT) {
        sb.append(part.getPart());
      }
    }
    return sb.toString();
  }

  /**
   * Get the plain text, without markup but with content from {@code interpretAs}.
   */
  public String getPlainText() {
    StringBuilder sb = new StringBuilder();
    for (TextPart part : parts) {
      if (part.getType() == TextPart.Type.TEXT || part.getType() == TextPart.Type.FAKE_CONTENT) {
        sb.append(part.getPart());
      }
    }
    return sb.toString();
  }

  /**
   * @since 4.3
   */
  public String getTextWithMarkup() {
    StringBuilder sb = new StringBuilder();
    for (TextPart part : parts) {
      if (part.getType() != TextPart.Type.FAKE_CONTENT) {
        sb.append(part.getPart());
      }
    }
    return sb.toString();
  }

  /**
   * Return a copy of the internal mapping.
   * @return copy of the internal mapping
   */
  public List<Map.Entry<Integer, Integer>> getMapping() {
    return copyMapping(mapping);
  }

  /**
   * Return a copy of the specified mapping.
   * @param mapping arbitrary mapping
   * @return copy of mapping
   */
  private static List<Map.Entry<Integer, Integer>> copyMapping(
      List<Map.Entry<Integer, Integer>> mapping) {
    List<Map.Entry<Integer, Integer>> result = new ArrayList<>();

    for (Map.Entry<Integer, Integer> entry : mapping) {
      result.add(new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue()));
    }

    return result;
  }

  /**
   * Return a copy of the parts.
   * @return copy of the parts
   */
  public List<TextPart> getParts() {
    return parts;
  }

  /**
   * Internally used by LanguageTool to adjust error positions to point to the
   * original location with markup, even though markup was ignored during text checking.
   * @param plainTextPosition the position in the plain text (no markup) that was checked
   * @return an adjusted position of the same location in the text with markup
   */
  public int getOriginalTextPositionFor(int plainTextPosition) {
    if (plainTextPosition < 0) {
      throw new IllegalArgumentException("plainTextPosition must be >= 0: " + plainTextPosition);
    } else if (mapping.isEmpty()) {
      throw new IllegalArgumentException("mapping must be non-empty");
    } else if (mapping.size() == 1) {
      return mapping.get(0).getValue();
    }

    Map.Entry<Integer, Integer> entry =
        new AbstractMap.SimpleEntry<>(plainTextPosition, Integer.MAX_VALUE);
    int i = -Collections.binarySearch(mapping, entry, mappingEntryComparator) - 1;
    if (i <= 0) i = 1;
    if (i >= mapping.size()) i = mapping.size() - 1;

    Map.Entry<Integer, Integer> lowerNeighbor = mapping.get(i - 1);
    Map.Entry<Integer, Integer> upperNeighbor = mapping.get(i);

    if (lowerNeighbor.getKey() == plainTextPosition) {
      return lowerNeighbor.getValue();
    } else {
      float t = (float)(plainTextPosition - lowerNeighbor.getKey()) /
          (float)(upperNeighbor.getKey() - lowerNeighbor.getKey());
      int result = Math.round((1 - t) * lowerNeighbor.getValue() + t * upperNeighbor.getValue());
      return result;
    }
  }

  /**
   * Internally used by LanguageTool to adjust error positions to point to the
   * original location with markup, even though markup was ignored during text checking.
   * @param plainTextPosition the position in the plain text (no markup) that was checked
   * @param isToPos the from/to position needed (ignored)
   * @return an adjusted position of the same location in the text with markup
   */
  public int getOriginalTextPositionFor(int plainTextPosition, boolean isToPos) {
    return getOriginalTextPositionFor(plainTextPosition);
  }

  /**
   * @since 3.9
   */
  public String getGlobalMetaData(String key, String defaultValue) {
    return customMetaData.getOrDefault(key, defaultValue);
  }

  /**
   * @since 3.9
   */
  public String getGlobalMetaData(MetaDataKey key, String defaultValue) {
    return metaData.getOrDefault(key, defaultValue);
  }

  @Override
  public String toString() {
    return StringUtils.join(parts, "");
  }

  public Map<MetaDataKey, String> getMetaData() { return metaData; }
  public Map<String, String> getCustomMetaData() { return customMetaData; }

}
