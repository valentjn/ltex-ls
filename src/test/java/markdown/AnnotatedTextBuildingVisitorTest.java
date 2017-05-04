package markdown;

import com.vladsch.flexmark.ast.Document;
import com.vladsch.flexmark.ast.Node;
import com.vladsch.flexmark.parser.Parser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.languagetool.markup.AnnotatedText;

import java.io.IOException;

public class AnnotatedTextBuildingVisitorTest {
    @Test
    void test() throws IOException {
        Parser p = Parser.builder().build();

        Node document = p.parse("# Heading\n" +
                "Paragraph with\n" +
                "multiple lines and [link](example.com)");

        AnnotatedTextBuildingVisitor visitor = new AnnotatedTextBuildingVisitor();
        visitor.visit((Document) document);
        AnnotatedText text = visitor.getText();

        Assertions.assertEquals("Heading\nParagraph with multiple lines and link", text.getPlainText());
    }
}
