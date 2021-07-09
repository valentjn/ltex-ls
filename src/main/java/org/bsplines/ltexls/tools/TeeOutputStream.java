/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.tools;

import java.io.IOException;
import java.io.OutputStream;

public class TeeOutputStream extends OutputStream {
  OutputStream outputStream;
  OutputStream teedOutputStream;

  public TeeOutputStream(OutputStream outputStream, OutputStream teedOutputStream) {
    this.outputStream = outputStream;
    this.teedOutputStream = teedOutputStream;
  }

  @Override
  public void write(int b) throws IOException {
    this.outputStream.write(b);
    this.teedOutputStream.write(b);
  }

  @Override
  public void write(byte[] bytes) throws IOException {
    this.outputStream.write(bytes);
    this.teedOutputStream.write(bytes);
  }

  @Override
  public void write(byte[] bytes, int offset, int length) throws IOException {
    this.outputStream.write(bytes, offset, length);
    this.teedOutputStream.write(bytes, offset, length);
  }

  @Override
  public void flush() throws IOException {
    this.outputStream.flush();
    this.teedOutputStream.flush();
  }

  @Override
  public void close() throws IOException {
    this.outputStream.close();
    this.teedOutputStream.close();
  }
}
