package org.bsplines.languagetool_languageserver.markdown;

import java.util.Stack;
import com.vladsch.flexmark.ast.*;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;

import org.languagetool.markup.AnnotatedText;

public class MarkdownAnnotatedTextBuilder {
  private org.languagetool.markup.AnnotatedTextBuilder builder =
      new org.languagetool.markup.AnnotatedTextBuilder();

  private String text;
  private int pos;
  private Stack<Class<? extends Node>> nodeStack;

  public void visit(Document document) {
    text = document.getChars().toString();
    pos = 0;
    nodeStack = new Stack<>();
    visitChildren(document);
    if (pos < text.length()) addMarkup(text.length());
  }

  private void visitChildren(final Node node) {
    node.getChildren().forEach(this::visit);
  }

  private boolean isInParagraph() {
    return nodeStack.contains(Paragraph.class);
  }

  private void addMarkup(int newPos) {
    boolean inParagraph = isInParagraph();

    while (true) {
      if ((pos >= text.length()) || (pos >= newPos)) break;
      int curPos = text.indexOf('\r', pos);

      if ((curPos == -1) || (curPos >= newPos)) {
        curPos = text.indexOf('\n', pos);
        if ((curPos == -1) || (curPos >= newPos)) break;
      }

      if (curPos > pos) builder.addMarkup(text.substring(pos, curPos));
      builder.addMarkup(text.substring(curPos, curPos + 1), (inParagraph ? " " : "\n"));

      pos = curPos + 1;
    }

    if (pos < newPos) {
      builder.addMarkup(text.substring(pos, newPos));
      pos = newPos;
    }
  }

  private void addText(int newPos) {
    if (pos < newPos) {
      builder.addText(text.substring(pos, newPos));
      pos = newPos;
    }
  }

  private void visit(Node node) {
    if (node.getClass() == Text.class) {
      addMarkup(node.getStartOffset());
      addText(node.getEndOffset());
    } else {
      if (node.getClass() == Paragraph.class) {
        addMarkup(node.getStartOffset());
      }

      nodeStack.push(node.getClass());
      visitChildren(node);
      nodeStack.pop();
    }
  }

  public AnnotatedText getAnnotatedText() {
    return builder.build();
  }
}
