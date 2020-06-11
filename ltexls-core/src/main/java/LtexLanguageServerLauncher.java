import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.bsplines.ltexls.LtexLanguageServer;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;

public class LtexLanguageServerLauncher {
  /**
   * Launch the LTeX language server.
   *
   * @param in InputStream to listen for client input
   * @param out OutputStream to write server output to
   */
  public static void launch(InputStream in, OutputStream out) throws
        InterruptedException, ExecutionException {
    LtexLanguageServer server = new LtexLanguageServer();
    Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(server, in, out);

    LanguageClient client = launcher.getRemoteProxy();
    server.connect(client);

    Future<Void> listener = launcher.startListening();
    listener.get();
  }

  /**
   * Main method. Checks command-line arguments and launches the LTeX language server with
   * @c System.in and @c System.out as streams.
   *
   * @param args command-line arguments
   */
  public static void main(String[] args) throws InterruptedException, ExecutionException {
    for (String arg : args) {
      if (arg.equals("--version")) {
        Package ltexLsPackage = LtexLanguageServer.class.getPackage();
        JsonObject jsonObject = new JsonObject();

        if (ltexLsPackage != null) {
          String ltexLsVersion = ltexLsPackage.getImplementationVersion();
          if (ltexLsVersion != null) jsonObject.addProperty("ltex-ls", ltexLsVersion);
        }

        String javaVersion = System.getProperty("java.version");
        if (javaVersion != null) jsonObject.addProperty("java", javaVersion);

        Gson gsonBuilder = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gsonBuilder.toJson(jsonObject));
        return;
      }
    }

    launch(System.in, System.out);
  }
}
