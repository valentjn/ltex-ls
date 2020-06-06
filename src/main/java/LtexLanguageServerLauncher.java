import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.bsplines.ltexls.LtexLanguageServer;
import org.bsplines.ltexls.Tools;
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

  public static void main(String[] args) throws InterruptedException, ExecutionException {
    for (String arg : args) {
      if (arg.equals("--version")) {
        Package package_ = LtexLanguageServer.class.getPackage();
        if (package_ == null) throw new RuntimeException(Tools.i18n("couldNotGetPackage"));
        System.out.println("ltex-ls " + package_.getImplementationVersion());
        return;
      }
    }

    launch(System.in, System.out);
  }
}
