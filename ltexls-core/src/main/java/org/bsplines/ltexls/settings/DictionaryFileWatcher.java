/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.settings;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchKey;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.bsplines.ltexls.Tools;
import org.checkerframework.checker.nullness.qual.Nullable;

public class DictionaryFileWatcher {
  private Map<Path, WatchKey> watchKeyMap;
  private Map<Path, @Nullable String> fileContentsMap;
  private Set<String> watchedDictionary;
  private @Nullable DictionaryFileWatcherRunnable dictionaryFileWatcherRunnable;
  private @Nullable Thread watcherThread;

  public DictionaryFileWatcher() {
    this.watchKeyMap = new HashMap<Path, WatchKey>();
    this.fileContentsMap = new HashMap<Path, @Nullable String>();
    this.watchedDictionary = Collections.emptySet();
    this.dictionaryFileWatcherRunnable = null;
    this.watcherThread = null;
  }

  public synchronized void setWatchedDictionary(Collection<? extends String> watchedDictionary) {
    if (this.dictionaryFileWatcherRunnable == null) {
      this.dictionaryFileWatcherRunnable = new DictionaryFileWatcherRunnable(this);
    }

    if (this.watcherThread == null) {
      this.watcherThread = new Thread(this.dictionaryFileWatcherRunnable);
      this.watcherThread.start();
    }

    this.watchedDictionary = new HashSet<>(watchedDictionary);

    Set<Path> directoryPaths = new HashSet<>();
    Set<Path> filePaths = new HashSet<>();

    for (String word : watchedDictionary) {
      if (word.startsWith(":")) {
        Path filePath = Paths.get(word.substring(1));
        filePaths.add(filePath);
        directoryPaths.add(filePath.getParent());
      }
    }

    Iterator<Map.Entry<Path, WatchKey>> watchKeyIterator = this.watchKeyMap.entrySet().iterator();

    while (watchKeyIterator.hasNext()) {
      Map.Entry<Path, WatchKey> entry = watchKeyIterator.next();

      if (!directoryPaths.contains(entry.getKey())) {
        entry.getValue().cancel();
        watchKeyIterator.remove();
      }
    }

    Iterator<Map.Entry<Path, @Nullable String>> fileContentsIterator =
        this.fileContentsMap.entrySet().iterator();

    while (fileContentsIterator.hasNext()) {
      Map.Entry<Path, @Nullable String> entry = fileContentsIterator.next();
      if (!filePaths.contains(entry.getKey())) fileContentsIterator.remove();
    }

    for (Path filePath : filePaths) {
      if (!this.fileContentsMap.containsKey(filePath)) {
        @Nullable String fileContents = Tools.readFile(filePath);
        this.fileContentsMap.put(filePath, fileContents);
      }
    }


    for (Path directoryPath : directoryPaths) {
      if (!this.watchKeyMap.containsKey(directoryPath)) {
        // this.dictionaryFileWatcherRunnable cannot be null at this point
        if (this.dictionaryFileWatcherRunnable == null) continue;
        @Nullable WatchKey watchKey =
            this.dictionaryFileWatcherRunnable.registerPath(directoryPath);
        if (watchKey == null) continue;
        this.watchKeyMap.put(directoryPath, watchKey);
      }
    }
  }

  public synchronized Set<String> getFullDictionary() {
    Set<String> fullDictionary = new HashSet<>();

    for (String word : this.watchedDictionary) {
      @Nullable String fileContents = null;

      if (word.startsWith(":")) {
        Path filePath = Paths.get(word.substring(1));
        fileContents = this.fileContentsMap.get(filePath);
      }

      if (fileContents != null) {
        for (String fileWord : fileContents.split("\\r?\\n")) {
          if (!fileWord.isEmpty()) fullDictionary.add(fileWord);
        }
      } else {
        fullDictionary.add(word);
      }
    }

    return fullDictionary;
  }

  public synchronized void setFileContents(Path filePath, @Nullable String fileContents) {
    @Nullable String oldFileContents = this.fileContentsMap.get(filePath);

    if (((oldFileContents == null) && (fileContents != null))
          || ((oldFileContents != null) && !oldFileContents.equals(fileContents))) {
      this.fileContentsMap.put(filePath, fileContents);
    }
  }
}
