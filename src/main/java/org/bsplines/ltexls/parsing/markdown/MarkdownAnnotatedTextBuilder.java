package org.bsplines.ltexls.parsing.markdown;

import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.bsplines.ltexls.Settings;
import org.bsplines.ltexls.parsing.CodeAnnotatedTextBuilder;
import org.bsplines.ltexls.parsing.DummyGenerator;

public class MarkdownAnnotatedTextBuilder extends CodeAnnotatedTextBuilder {
  private String code;
  private int pos;
  private int dummyCounter;
  private Stack<String> nodeTypeStack = new Stack<>();

  public String language = "en-US";
  public List<String> ignoreNodeTypes = new ArrayList<>();
  public List<String> dummyNodeTypes = new ArrayList<>();

  public MarkdownAnnotatedTextBuilder() {
    this.code = "";
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
      if ((pos >= code.length()) || (pos >= newPos)) break;
      int curPos = code.indexOf('\r', pos);

      if ((curPos == -1) || (curPos >= newPos)) {
        curPos = code.indexOf('\n', pos);
        if ((curPos == -1) || (curPos >= newPos)) break;
      }

      if (curPos > pos) super.addMarkup(code.substring(pos, curPos));
      super.addMarkup(code.substring(curPos, curPos + 1), (inParagraph ? " " : "\n"));

      pos = curPos + 1;
    }

    if (newPos > pos) {
      super.addMarkup(code.substring(pos, newPos));
      pos = newPos;
    }
  }

  private void addText(int newPos) {
    if (newPos > pos) {
      super.addText(code.substring(pos, newPos));
      pos = newPos;
    }
  }

  private String generateDummy() {
    return DummyGenerator.getDefault().generate(language, dummyCounter++);
  }

  public MarkdownAnnotatedTextBuilder addCode(String code) {
    Parser parser = Parser.builder().build();
    Document document = parser.parse(code);
    visit(document);
    return this;
  }

  private void visit(Document document) {
    this.code = document.getChars().toString();
    this.pos = 0;
    this.dummyCounter = 0;
    this.nodeTypeStack.clear();
    visitChildren(document);
    if (pos < code.length()) addMarkup(code.length());
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
        super.addMarkup(code.substring(pos, newPos), generateDummy());
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

  @Override
  public void setSettings(Settings settings) {
    language = settings.getLanguageShortCode();
    dummyNodeTypes.addAll(settings.getDummyMarkdownNodeTypes());
    ignoreNodeTypes.addAll(settings.getIgnoreMarkdownNodeTypes());
  }
}
