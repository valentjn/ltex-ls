import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.bsplines.ltex_ls.LtexLanguageServer;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;

public class LtexLanguageServerLauncher {
  public static void launch(InputStream in, OutputStream out) throws
        InterruptedException, ExecutionException {
    LtexLanguageServer server = new LtexLanguageServer();
    Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(server, in, out);

    LanguageClient client = launcher.getRemoteProxy();
    server.connect(client);

    Future<Void> listener = launcher.startListening();
    listener.get();
  }

  public static void main(String[] args) {
    for (String arg : args) {
      if (arg.equals("--version")) {
        System.out.println("ltex-ls " +
            LtexLanguageServer.class.getPackage().getImplementationVersion());
        System.exit(0);
      }
    }

    try {
      launch(System.in, System.out);
      System.exit(0);
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}
