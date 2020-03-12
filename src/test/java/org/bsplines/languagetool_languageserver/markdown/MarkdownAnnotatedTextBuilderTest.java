package org.bsplines.languagetool_languageserver.markdown;

import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.parser.Parser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.languagetool.markup.AnnotatedText;

import java.io.IOException;

public class MarkdownAnnotatedTextBuilderTest {
    @Test
    void test() throws IOException {
        Parser p = Parser.builder().build();

        Document document = p.parse("# Heading\n" +
                "Paragraph with\n" +
                "multiple lines and [link](example.com)");

        MarkdownAnnotatedTextBuilder builder = new MarkdownAnnotatedTextBuilder();
        builder.visit(document);
        AnnotatedText text = builder.getAnnotatedText();

        Assertions.assertEquals("Heading\nParagraph with multiple lines and link", text.getPlainText());
    }
}
