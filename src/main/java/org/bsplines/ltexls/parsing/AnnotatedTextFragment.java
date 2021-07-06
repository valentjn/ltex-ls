/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.bsplines.ltexls.server.LtexTextDocumentItem;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.markup.TextPart;

public class AnnotatedTextFragment {
  private AnnotatedText annotatedText;
  private CodeFragment codeFragment;
  private LtexTextDocumentItem document;
  private @MonotonicNonNull String plainText;
  private @MonotonicNonNull AnnotatedText inverseAnnotatedText;

  private @MonotonicNonNull Field annotatedTextMappingField;
  private @MonotonicNonNull Method mappingValueGetTotalPositionMethod;

  public AnnotatedTextFragment(AnnotatedText annotatedText, CodeFragment codeFragment,
        LtexTextDocumentItem document) {
    this.annotatedText = annotatedText;
    this.codeFragment = codeFragment;
    this.document = document;
  }

  public AnnotatedText getAnnotatedText() {
    return this.annotatedText;
  }

  public CodeFragment getCodeFragment() {
    return this.codeFragment;
  }

  public LtexTextDocumentItem getDocument() {
    return this.document;
  }

  public String getSubstringOfPlainText(int fromPos, int toPos) {
    if (this.plainText == null) this.plainText = this.annotatedText.getPlainText();

    if (this.inverseAnnotatedText == null) {
      AnnotatedTextBuilder inverseAnnotatedTextBuilder = new AnnotatedTextBuilder();
      List<TextPart> textParts = this.annotatedText.getParts();

      for (int i = 0; i < textParts.size(); i++) {
        TextPart textPart = textParts.get(i);

        switch (textPart.getType()) {
          case TEXT: {
            inverseAnnotatedTextBuilder.addText(textPart.getPart());
            break;
          }
          case MARKUP: {
            String markup;

            if ((i < textParts.size() - 1)
                  && (textParts.get(i + 1).getType() == TextPart.Type.FAKE_CONTENT)) {
              markup = textParts.get(i + 1).getPart();
              i++;
            } else {
              markup = "";
            }

            inverseAnnotatedTextBuilder.addMarkup(markup, textPart.getPart());
            break;
          }
          case FAKE_CONTENT: {
            inverseAnnotatedTextBuilder.addMarkup(textPart.getPart());
            break;
          }
          default: {
            break;
          }
        }
      }

      this.inverseAnnotatedText = inverseAnnotatedTextBuilder.build();
    }

    return this.plainText.substring(
        getOriginalTextPosition(this.inverseAnnotatedText, fromPos, false),
        getOriginalTextPosition(this.inverseAnnotatedText, toPos, true));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private int getOriginalTextPosition(AnnotatedText annotatedText, int plainTextPosition,
        boolean isToPos) {
    if (this.annotatedTextMappingField == null) {
      try {
        this.annotatedTextMappingField = annotatedText.getClass().getDeclaredField("mapping");
      } catch (NoSuchFieldException | SecurityException e) {
        throw new RuntimeException(e);
      }

      this.annotatedTextMappingField.setAccessible(true);
    }

    if (this.mappingValueGetTotalPositionMethod == null) {
      Class<?> mappingValueClass;

      try {
        mappingValueClass = Class.forName("org.languagetool.markup.MappingValue");
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }

      try {
        this.mappingValueGetTotalPositionMethod =
            mappingValueClass.getDeclaredMethod("getTotalPosition");
      } catch (NoSuchMethodException | SecurityException e) {
        throw new RuntimeException(e);
      }

      this.mappingValueGetTotalPositionMethod.setAccessible(true);
    }

    @NonNull Map<Integer, ?> mapping;

    try {
      @SuppressWarnings("assignment.type.incompatible")
      @NonNull Map<Integer, ?> mapping2 = (Map)this.annotatedTextMappingField.get(annotatedText);
      mapping = mapping2;
    } catch (IllegalArgumentException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }

    for (Map.Entry<Integer, ?> entry : mapping.entrySet()) {
      if (entry.getKey() == plainTextPosition) {
        try {
          @SuppressWarnings("unboxing.of.nullable")
          int totalPosition = (int)this.mappingValueGetTotalPositionMethod.invoke(
              entry.getValue());
          return totalPosition;
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
          throw new RuntimeException(e);
        }
      }
    }

    return annotatedText.getOriginalTextPositionFor(plainTextPosition, isToPos);
  }
}
