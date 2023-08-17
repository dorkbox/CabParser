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
package dorkbox.cabParser.structure

import dorkbox.bytes.LittleEndian
import dorkbox.cabParser.CabException
import dorkbox.cabParser.CorruptCabException
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.util.*

class CabFileEntry {
    /** uncompressed size of this file in bytes , 4bytes  */
    var cbFile: Long = 0

    /** uncompressed offset of this file in the folder , 4bytes  */
    var offFolderStart: Long = 0

    /** index into the CFFOLDER area , 2bytes  */
    var iFolder = 0

    /** time/date stamp for this file , 2bytes  */
    var date = Date()

    /** attribute flags for this file , 2bytes  */
    var attribs = 0

    /** name of this file , 1*n bytes  */
    lateinit var name: String

    var applicationData: Any? = null

    @Throws(IOException::class, CabException::class)
    fun read(input: InputStream) {
        val arrayOfByte = ByteArray(256)
        cbFile = LittleEndian.UInt_.from(input).toLong()
        offFolderStart = LittleEndian.UInt_.from(input).toLong()
        iFolder = LittleEndian.UShort_.from(input).toInt()
        val timeA: Int = LittleEndian.UShort_.from(input).toInt()
        val timeB: Int = LittleEndian.UShort_.from(input).toInt()
        date = getDate(timeA, timeB)
        attribs = LittleEndian.UShort_.from(input).toInt()

        var i = 0
        i = 0
        while (i < arrayOfByte.size) {
            val m = input.read()
            if (m == -1) {
                throw CorruptCabException("EOF reading cffile")
            }
            arrayOfByte[i] = m.toByte()
            if (m == 0) {
                break
            }
            i++
        }
        if (i >= arrayOfByte.size) {
            throw CorruptCabException("cffile filename not null terminated")
        }
        name = if (attribs and NAME_IS_UTF == NAME_IS_UTF) {
            readUtfString(arrayOfByte) ?: throw CorruptCabException("invalid name utf8 code")
        }
        else {
            String(arrayOfByte, 0, i, US_ASCII).trim { it <= ' ' }
        }
    }

    var isReadOnly: Boolean
        get() = attribs and READONLY != 0
        set(bool) {
            if (bool) {
                attribs = attribs or 1
                return
            }
            attribs = attribs and -2
        }
    var isHidden: Boolean
        get() = attribs and HIDDEN != 0
        set(bool) {
            if (bool) {
                attribs = attribs or 2
                return
            }
            attribs = attribs and -3
        }
    var isSystem: Boolean
        get() = attribs and SYSTEM != 0
        set(bool) {
            if (bool) {
                attribs = attribs or 4
                return
            }
            attribs = attribs and -5
        }
    var isArchive: Boolean
        get() = attribs and ARCHIVE != 0
        set(bool) {
            if (bool) {
                attribs = attribs or 32
                return
            }
            attribs = attribs and -33
        }
    var size: Long
        get() = cbFile
        set(size) {
            cbFile = size.toInt().toLong()
        }

    private fun getDate(dateInfo: Int, timeInfo: Int): Date {
        val i = dateInfo and 0x1F
        val j = (dateInfo ushr 5) - 1 and 0xF
        val k = (dateInfo ushr 9) + 80
        val m = timeInfo and 0x1F shl 1
        val n = timeInfo ushr 5 and 0x3F
        val i1 = timeInfo ushr 11 and 0x1F
        return Date(k, j, i, i1, n, m)
    }

    override fun toString(): String {
        return name
    }

    companion object {
        val US_ASCII = Charset.forName("US-ASCII")

        /** file is read-only (in HEX)  */
        const val READONLY = 0x01

        /** file is hidden (in HEX)  */
        const val HIDDEN = 0x02

        /** file is a system file (in HEX)  */
        const val SYSTEM = 0x04

        /** file modified since last backup (in HEX)  */
        const val ARCHIVE = 0x20

        /** szName[] contains UTF (in HEX)  */
        const val NAME_IS_UTF = 0x80

        private fun readUtfString(stringBytes: ByteArray): String? {
            var j = 0
            var stringSize = 0
            var k = 0

            // count the size of the string
            stringSize = 0
            while (stringBytes[stringSize].toInt() != 0) {
                stringSize++
            }
            val stringChars = CharArray(stringSize)
            k = 0
            while (stringBytes[j].toInt() != 0) {
                var m = (stringBytes[j++].toInt() and 0xFF).toChar().code
                if (m < 128) {
                    stringChars[k] = m.toChar()
                }
                else {
                    if (m < 192) {
                        return null
                    }
                    if (m < 224) {
                        stringChars[k] = (m and 0x1F shl 6).toChar()
                        m = (stringBytes[j++].toInt() and 0xFF).toChar().code
                        if (m < 128 || m > 191) {
                            return null
                        }
                        stringChars[k] = (stringChars[k].code or (m and 0x3F).toChar().code).toChar()
                    }
                    else if (m < 240) {
                        stringChars[k] = (m and 0xF shl 12).toChar()
                        m = (stringBytes[j++].toInt() and 0xFF).toChar().code
                        if (m < 128 || m > 191) {
                            return null
                        }
                        stringChars[k] = (stringChars[k].code or (m and 0x3F shl 6).toChar().code).toChar()
                        m = (stringBytes[j++].toInt() and 0xFF).toChar().code
                        if (m < 128 || m > 191) {
                            return null
                        }
                        stringChars[k] = (stringChars[k].code or (m and 0x3F).toChar().code).toChar()
                    }
                    else {
                        return null
                    }
                }
                k++
            }
            return String(stringChars, 0, k)
        }
    }
}
