/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.tools

import java.io.InputStream
import java.io.OutputStream

class TeeInputStream(
  val inputStream: InputStream,
  val teedOutputStream: OutputStream,
) : InputStream() {
  override fun read(): Int {
    val b: Int = this.inputStream.read()
    if (b != -1) this.teedOutputStream.write(b)
    return b
  }

  override fun read(bytes: ByteArray): Int {
    val length: Int = this.inputStream.read(bytes)
    if (length != -1) this.teedOutputStream.write(bytes)
    return length
  }

  override fun read(bytes: ByteArray, offset: Int, length: Int): Int {
    val actualLength: Int = this.inputStream.read(bytes, offset, length)
    if (actualLength != -1) this.teedOutputStream.write(bytes, offset, actualLength)
    return actualLength
  }

  override fun readAllBytes(): ByteArray {
    val bytes: ByteArray = this.inputStream.readAllBytes()
    this.teedOutputStream.write(bytes)
    return bytes
  }

  override fun readNBytes(length: Int): ByteArray {
    val bytes: ByteArray = this.inputStream.readNBytes(length)
    this.teedOutputStream.write(bytes)
    return bytes
  }

  override fun readNBytes(bytes: ByteArray, offset: Int, length: Int): Int {
    val actualLength: Int = this.inputStream.readNBytes(bytes, offset, length)
    if (actualLength != -1) this.teedOutputStream.write(bytes, offset, actualLength)
    return actualLength
  }

  override fun skip(length: Long): Long {
    return this.inputStream.skip(length)
  }

  override fun available(): Int {
    return this.inputStream.available()
  }

  override fun close() {
    this.inputStream.close()
    this.teedOutputStream.close()
  }
}
