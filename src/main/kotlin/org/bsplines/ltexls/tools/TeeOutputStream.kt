/* Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.tools

import java.io.OutputStream

class TeeOutputStream(
  val outputStream: OutputStream,
  val teedOutputStream: OutputStream,
) : OutputStream() {
  override fun write(b: Int) {
    this.outputStream.write(b)
    this.teedOutputStream.write(b)
  }

  override fun write(bytes: ByteArray) {
    this.outputStream.write(bytes)
    this.teedOutputStream.write(bytes)
  }

  override fun write(bytes: ByteArray, offset: Int, length: Int) {
    this.outputStream.write(bytes, offset, length)
    this.teedOutputStream.write(bytes, offset, length)
  }

  override fun flush() {
    this.outputStream.flush()
    this.teedOutputStream.flush()
  }

  override fun close() {
    this.outputStream.close()
    this.teedOutputStream.close()
  }
}
