import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class App {
  public static void main(String[] args) {
    String port = args[0];
    Socket socket = null;

    try {
      socket = new Socket("localhost", Integer.parseInt(port));

      InputStream in = socket.getInputStream();
      OutputStream out = socket.getOutputStream();

      LanguageToolLanguageServer server = new LanguageToolLanguageServer();
      Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(server, in, out);

      LanguageClient client = launcher.getRemoteProxy();
      server.connect(client);

      Future<Void> listener = launcher.startListening();
      listener.get();
    } catch (IOException | InterruptedException | ExecutionException e) {
      e.printStackTrace();
    } finally {
      try {
        if (socket != null) socket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
