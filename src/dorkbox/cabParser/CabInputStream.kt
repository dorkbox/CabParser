/*
 * Copyright 2023 dorkbox, llc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dorkbox.cabParser

import java.io.IOException
import java.io.InputStream

internal class CabInputStream(private val inputStream: InputStream) : InputStream() {
    companion object {
        /**
         * Reliably skips over and discards n bytes of data from the input stream
         * @param is input stream
         * @param n the number of bytes to be skipped
         * @return the actual number of bytes skipped
         *
         * @throws IOException
         */
        @Throws(IOException::class)
        fun betterSkip(`is`: InputStream, n: Long): Long {
            var left = n
            while (left > 0) {
                val l = `is`.skip(left)
                if (l > 0) {
                    left -= l
                }

                else if (l == 0L) { // should we retry? lets read one byte
                    if (`is`.read() == -1) // EOF
                        break
                    else left--
                }
                else {
                    throw IOException("skip() returned a negative value. This should never happen")
                }
            }
            return n - left
        }
    }

    var currentPosition = 0L
        private set

    private var a: Long = 0
    private val markSupported: Boolean

    init {
        markSupported = inputStream.markSupported()
    }

    @Throws(IOException::class)
    override fun close() {
        inputStream.close()
    }

    @Synchronized
    @Throws(IOException::class)
    override fun reset() {
        if (markSupported) {
            inputStream.reset()
            currentPosition = a
            return
        }
        throw IOException()
    }

    @Throws(IOException::class)
    override fun read(): Int {
        val i = inputStream.read()
        if (i >= 0) {
            currentPosition += 1L
        }
        return i
    }

    @Throws(IOException::class)
    override fun read(bytes: ByteArray): Int {
        val i = inputStream.read(bytes)
        if (i > 0) {
            currentPosition += i.toLong()
        }
        return i
    }

    @Throws(IOException::class)
    override fun read(bytes: ByteArray, offset: Int, length: Int): Int {
        val i = inputStream.read(bytes, offset, length)
        if (i > 0) {
            currentPosition += i.toLong()
        }
        return i
    }

    @Throws(IOException::class)
    override fun available(): Int {
        throw IOException()
    }

    @Synchronized
    override fun mark(paramInt: Int) {
        if (markSupported) {
            a = currentPosition
            inputStream.mark(paramInt)
        }
    }

    override fun markSupported(): Boolean {
        return markSupported
    }

    @Throws(IOException::class)
    fun seek(location: Long) {
        if (location < currentPosition) {
            throw IOException("Cannot seek backwards")
        }
        if (location > currentPosition) {
            skip(location - currentPosition)
        }
    }

    @Throws(IOException::class)
    override fun skip(ammount: Long): Long {
        val l = betterSkip(inputStream, ammount)
        currentPosition += l.toInt().toLong()
        return l
    }


}
