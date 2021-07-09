/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TeeInputStream extends InputStream {
  InputStream inputStream;
  OutputStream teedOutputStream;

  public TeeInputStream(InputStream inputStream, OutputStream teedOutputStream) {
    this.inputStream = inputStream;
    this.teedOutputStream = teedOutputStream;
  }

  @Override
  public int read() throws IOException {
    int b = this.inputStream.read();
    if (b != -1) this.teedOutputStream.write(b);
    return b;
  }

  @Override
  public int read(byte[] bytes) throws IOException {
    int length = this.inputStream.read(bytes);
    if (length != -1) this.teedOutputStream.write(bytes);
    return length;
  }

  @Override
  public int read(byte[] bytes, int offset, int length) throws IOException {
    int actualLength = this.inputStream.read(bytes, offset, length);
    if (actualLength != -1) this.teedOutputStream.write(bytes, offset, actualLength);
    return actualLength;
  }

  @Override
  public byte[] readAllBytes() throws IOException {
    byte[] bytes = this.inputStream.readAllBytes();
    this.teedOutputStream.write(bytes);
    return bytes;
  }

  @Override
  public byte[] readNBytes(int length) throws IOException {
    byte[] bytes = this.inputStream.readNBytes(length);
    this.teedOutputStream.write(bytes);
    return bytes;
  }

  @Override
  public int readNBytes(byte[] bytes, int offset, int length) throws IOException {
    int actualLength = this.inputStream.readNBytes(bytes, offset, length);
    if (actualLength != -1) this.teedOutputStream.write(bytes, offset, actualLength);
    return actualLength;
  }

  @Override
  public long skip(long length) throws IOException {
    return this.inputStream.skip(length);
  }

  @Override
  public int available() throws IOException {
    return this.inputStream.available();
  }

  @Override
  public void close() throws IOException {
    this.inputStream.close();
    this.teedOutputStream.close();
  }
}
