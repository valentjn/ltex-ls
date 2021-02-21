/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.bsplines.ltexls.client.LtexLanguageClient;
import org.bsplines.ltexls.server.LtexLanguageServer;
import org.bsplines.ltexls.tools.Tools;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@DefaultQualifier(NonNull.class)
@Command(name = "ltex-ls", mixinStandardHelpOptions = true, showDefaultValues = true,
    versionProvider = LtexLanguageServerLauncher.VersionProvider.class,
    description = "LTeX LS - LTeX Language Server")
public class LtexLanguageServerLauncher implements Callable<Integer> {
  public static class VersionProvider implements CommandLine.IVersionProvider {
    @Override
    public String[] getVersion() throws Exception {
      @Nullable Package ltexLsPackage = LtexLanguageServer.class.getPackage();
      JsonObject jsonObject = new JsonObject();

      if (ltexLsPackage != null) {
        @Nullable String ltexLsVersion = ltexLsPackage.getImplementationVersion();
        if (ltexLsVersion != null) jsonObject.addProperty("ltex-ls", ltexLsVersion);
      }

      @Nullable String javaVersion = System.getProperty("java.version");
      if (javaVersion != null) jsonObject.addProperty("java", javaVersion);

      Gson gsonBuilder = new GsonBuilder().setPrettyPrinting().create();
      return new String[]{gsonBuilder.toJson(jsonObject)};
    }
  }

  private enum ServerType {
    standardStream,
    tcpSocket,
  }

  @Option(names = {"--endless"}, negatable = true,
      description = "Keep server alive when client terminates.")
  private boolean endless = false;

  @Option(names = {"--server-type"},
      description = "Run the server as type <serverType>. Valid values: ${COMPLETION-CANDIDATES}")
  private ServerType serverType = ServerType.standardStream;

  @Option(names = {"--host"}, description = "Listen for TCP connections on host <host> "
      + "(IP address or hostname; only relevant if server type is tcpSocket).")
  private String host = "localhost";

  @Option(names = {"--port"}, description = "Listen for TCP connections on port <port> "
      + "(only relevant if server type is tcpSocket).")
  private Integer port = 0;

  @Override
  public Integer call() throws Exception {
    return internalCall();
  }

  private int internalCall() throws UnknownHostException, IOException, InterruptedException,
        ExecutionException {
    @Nullable ServerSocket serverSocket = null;

    try {
      if (this.serverType == ServerType.tcpSocket) {
        serverSocket = new ServerSocket(this.port, 50, InetAddress.getByName(this.host));
        this.port = serverSocket.getLocalPort();
      }

      do {
        InputStream inputStream = System.in;
        OutputStream outputStream = System.out;

        if (this.serverType == ServerType.tcpSocket) {
          if (serverSocket == null) throw new NullPointerException("serverSocket");
          Tools.logger.info(Tools.i18n("waitingForClientToConnectOnPort", this.port));
          Socket clientSocket = serverSocket.accept();
          Tools.logger.info(Tools.i18n("connectedToClientOnPort", this.port));
          inputStream = clientSocket.getInputStream();
          outputStream = clientSocket.getOutputStream();
        }

        launch(inputStream, outputStream);
      } while (this.endless);
    } finally {
      if (serverSocket != null) serverSocket.close();
    }

    return 0;
  }

  public static void launch(InputStream inputStream, OutputStream outputStream)
        throws InterruptedException, ExecutionException {
    LtexLanguageServer server = new LtexLanguageServer();
    ExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    Launcher<LtexLanguageClient> launcher = (new LSPLauncher.Builder<LtexLanguageClient>())
        .setLocalService(server).setRemoteInterface(LtexLanguageClient.class)
        .setInput(inputStream).setOutput(outputStream).setExecutorService(executorService).create();

    LanguageClient client = launcher.getRemoteProxy();
    server.connect(client);

    Future<Void> listener = launcher.startListening();
    listener.get();
  }

  public static void main(String[] arguments) {
    CommandLine commandLine = new CommandLine(new LtexLanguageServerLauncher());
    int exitCode = commandLine.execute(arguments);
    if (exitCode != 0) System.exit(exitCode);
  }

  public static int mainWithoutExit(String[] arguments) throws UnknownHostException, IOException,
        InterruptedException, ExecutionException {
    LtexLanguageServerLauncher launcher = new LtexLanguageServerLauncher();
    CommandLine commandLine = new CommandLine(launcher);
    commandLine.parseArgs(arguments);

    if (commandLine.isUsageHelpRequested()) {
      commandLine.usage(commandLine.getOut());
      return commandLine.getCommandSpec().exitCodeOnUsageHelp();
    } else if (commandLine.isVersionHelpRequested()) {
      commandLine.printVersionHelp(commandLine.getOut());
      return commandLine.getCommandSpec().exitCodeOnVersionHelp();
    } else {
      int exitCode = launcher.internalCall();
      commandLine.setExecutionResult(exitCode);
      return commandLine.getCommandSpec().exitCodeOnSuccess();
    }
  }
}
