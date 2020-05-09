package org.bsplines.ltex_ls.markdown;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;

import org.bsplines.ltex_ls.Tools;
import org.languagetool.markup.AnnotatedText;

public class MarkdownAnnotatedTextBuilder {
  private org.languagetool.markup.AnnotatedTextBuilder builder =
      new org.languagetool.markup.AnnotatedTextBuilder();

  private String text;
  private int pos;
  private int dummyCounter;
  private Stack<String> nodeTypeStack = new Stack<>();

  public String language = "en-US";
  public List<String> ignoreNodeTypes = new ArrayList<>();
  public List<String> dummyNodeTypes = new ArrayList<>();

  public void visit(Document document) {
    text = document.getChars().toString();
    pos = 0;
    dummyCounter = 0;
    nodeTypeStack.clear();
    visitChildren(document);
    if (pos < text.length()) addMarkup(text.length());
  }

  private void visitChildren(final Node node) {
    node.getChildren().forEach(this::visit);
  }

  private boolean isInNodeType(String nodeType) {
    return nodeTypeStack.contains(nodeType);
  }

  private boolean isInNodeType(List<String> nodeTypes) {
    for (String nodeType : nodeTypeStack) {
      if (nodeTypes.contains(nodeType)) return true;
    }

    return false;
  }

  private void addMarkup(int newPos) {
    boolean inParagraph = isInNodeType("Paragraph");

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

    if (newPos > pos) {
      builder.addMarkup(text.substring(pos, newPos));
      pos = newPos;
    }
  }

  private void addText(int newPos) {
    if (newPos > pos) {
      builder.addText(text.substring(pos, newPos));
      pos = newPos;
    }
  }

  private String generateDummy() {
    return Tools.generateDummy(language, dummyCounter++);
  }

  private void visit(Node node) {
    String nodeType = node.getClass().getSimpleName();

    if (nodeType.equals("Text")) {
      if (isInNodeType(ignoreNodeTypes)) {
        addMarkup(node.getEndOffset());
      } else {
        addMarkup(node.getStartOffset());
        addText(node.getEndOffset());
      }
    } else if (dummyNodeTypes.contains(nodeType)) {
      addMarkup(node.getStartOffset());
      int newPos = node.getEndOffset();

      if (newPos > pos) {
        builder.addMarkup(text.substring(pos, newPos), generateDummy());
        pos = newPos;
      }
    } else {
      if (nodeType.equals("Paragraph")) {
        addMarkup(node.getStartOffset());
      }

      nodeTypeStack.push(nodeType);
      visitChildren(node);
      nodeTypeStack.pop();
    }
  }

  public AnnotatedText getAnnotatedText() {
    return builder.build();
  }
}
