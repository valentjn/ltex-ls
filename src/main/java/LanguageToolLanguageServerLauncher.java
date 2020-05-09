import org.bsplines.languagetool_languageserver.LanguageToolLanguageServer;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class LanguageToolLanguageServerLauncher {
  public static void main(String[] args) {
    try {
      LanguageToolLanguageServer server = new LanguageToolLanguageServer();
      Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(
          server, System.in, System.out);

      LanguageClient client = launcher.getRemoteProxy();
      server.connect(client);

      Future<Void> listener = launcher.startListening();
      listener.get();
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
  }
}
