package org.bsplines.ltexls.tools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.bsplines.ltexls.server.LtexLanguageServer;
import org.checkerframework.checker.nullness.qual.Nullable;
import picocli.CommandLine;

public class VersionProvider implements CommandLine.IVersionProvider {
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
