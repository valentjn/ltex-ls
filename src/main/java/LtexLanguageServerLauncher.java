import org.bsplines.ltex_ls.LtexLanguageServer;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class LtexLanguageServerLauncher {
  public static void main(String[] args) {
    try {
      LtexLanguageServer server = new LtexLanguageServer();
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
