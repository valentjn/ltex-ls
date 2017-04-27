import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextEdit;

import java.util.Arrays;
import java.util.Collections;

class TextEditCommand extends Command {
    public TextEditCommand(String title, Range range, TextDocumentItem document) {
        this.setCommand("cSpell.editText");
        this.setArguments(
                Arrays.asList(
                        document.getUri(),
                        document.getVersion(),
                        Collections.singletonList(new TextEdit(range, title))));
        this.setTitle(title);
    }
}
