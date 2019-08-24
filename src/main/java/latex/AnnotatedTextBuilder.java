package latex;

import org.languagetool.markup.AnnotatedText;

public class AnnotatedTextBuilder {
  private org.languagetool.markup.AnnotatedTextBuilder builder =
      new org.languagetool.markup.AnnotatedTextBuilder();

  public void addCode(String text) {
    // TODO
    builder.addText(text);
  }

  public AnnotatedText getAnnotatedText() {
    return builder.build();
  }
}
