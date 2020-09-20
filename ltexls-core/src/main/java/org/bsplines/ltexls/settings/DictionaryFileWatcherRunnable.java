/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.settings;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import org.bsplines.ltexls.Tools;
import org.checkerframework.checker.nullness.qual.Nullable;

public class DictionaryFileWatcherRunnable implements Runnable {
  private DictionaryFileWatcher dictionaryFileWatcher;
  private @Nullable WatchService watchService;

  public DictionaryFileWatcherRunnable(DictionaryFileWatcher dictionaryFileWatcher) {
    this.dictionaryFileWatcher = dictionaryFileWatcher;

    try {
      this.watchService = FileSystems.getDefault().newWatchService();
    } catch (IOException e) {
      Tools.logger.warning(Tools.i18n("couldNotInitializeWatchService", e));
      this.watchService = null;
    }
  }

  @Override
  public void run() {
    while (true) {
      WatchKey watchKey;

      try {
        if (this.watchService == null) return;
        watchKey = this.watchService.take();
      } catch (InterruptedException x) {
        break;
      }

      for (WatchEvent<?> event : watchKey.pollEvents()) {
        WatchEvent.Kind<?> eventKind = event.kind();
        if (eventKind == StandardWatchEventKinds.OVERFLOW) continue;

        @Nullable Path directoryPath = (@Nullable Path)(watchKey.watchable());
        if (directoryPath == null) continue;

        @Nullable Path fileName = (@Nullable Path)(event.context());
        if (fileName == null) continue;

        Path filePath = directoryPath.resolve(fileName);
        @Nullable String fileContents = null;

        if (eventKind != StandardWatchEventKinds.ENTRY_DELETE) {
          fileContents = Tools.readFile(filePath);
        }

        this.dictionaryFileWatcher.setFileContents(filePath, fileContents);
      }

      watchKey.reset();
    }
  }

  public @Nullable WatchKey registerPath(Path directoryPath) {
    if ((this.watchService == null) || (directoryPath == null)) return null;

    try {
      return directoryPath.register(this.watchService,
          StandardWatchEventKinds.ENTRY_CREATE,
          StandardWatchEventKinds.ENTRY_DELETE,
          StandardWatchEventKinds.ENTRY_MODIFY);
    } catch (IOException e) {
      Tools.logger.warning(Tools.i18n("couldNotRegisterDictionaryDirectory", e,
          directoryPath.toString()));
      return null;
    }
  }
}
