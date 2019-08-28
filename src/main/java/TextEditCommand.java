import org.eclipse.lsp4j.*;

import java.util.Collections;

class TextEditCommand extends Command {

  static final String CommandName = "langugageTool.acceptSuggestion";

  public TextEditCommand(String title, Range range, TextDocumentItem document) {
    this.setCommand(CommandName);

    VersionedTextDocumentIdentifier id = new VersionedTextDocumentIdentifier(document.getUri(), document.getVersion());
    this.setArguments(
    Collections.singletonList(
    new TextDocumentEdit(id, Collections.singletonList(new TextEdit(range, title)))));
    this.setTitle(title);
  }
}
