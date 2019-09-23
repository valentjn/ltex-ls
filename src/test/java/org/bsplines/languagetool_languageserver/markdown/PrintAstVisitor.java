package org.bsplines.languagetool_languageserver.markdown;

import com.vladsch.flexmark.ast.Node;

class PrintAstVisitor {
    public void visit(Node node, int indent) {
        String i = "";

        for (int j = 0; j < indent; j++) {
            i += "  ";
        }

        System.out.println(i + node.toAstString(true));

        node.getChildIterator().forEachRemaining(n -> this.visit(n, indent + 1));
    }
}
