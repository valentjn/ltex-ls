/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.bsplines.ltexls.client.LtexLanguageClient;
import org.bsplines.ltexls.server.LtexLanguageServer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;

@DefaultQualifier(NonNull.class)
public class LtexLanguageServerLauncher {
  public static void launch(InputStream in, OutputStream out) throws
        InterruptedException, ExecutionException {
    LtexLanguageServer server = new LtexLanguageServer();
    Launcher<LtexLanguageClient> launcher = (new LSPLauncher.Builder<LtexLanguageClient>())
        .setLocalService(server).setRemoteInterface(LtexLanguageClient.class)
        .setInput(in).setOutput(out).create();

    LanguageClient client = launcher.getRemoteProxy();
    server.connect(client);

    Future<Void> listener = launcher.startListening();
    listener.get();
  }

  public static void main(String[] args) throws InterruptedException, ExecutionException {
    for (String arg : args) {
      if (arg.equals("--version")) {
        @Nullable Package ltexLsPackage = LtexLanguageServer.class.getPackage();
        JsonObject jsonObject = new JsonObject();

        if (ltexLsPackage != null) {
          @Nullable String ltexLsVersion = ltexLsPackage.getImplementationVersion();
          if (ltexLsVersion != null) jsonObject.addProperty("ltex-ls", ltexLsVersion);
        }

        @Nullable String javaVersion = System.getProperty("java.version");
        if (javaVersion != null) jsonObject.addProperty("java", javaVersion);

        Gson gsonBuilder = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gsonBuilder.toJson(jsonObject));
        return;
      }
    }

    launch(System.in, System.out);
  }
}
