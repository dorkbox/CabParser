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
import dorkbox.bytes.LittleEndian.Int_.from
import dorkbox.cabParser.CabException
import dorkbox.cabParser.Checksum
import dorkbox.cabParser.CorruptCabException
import java.io.EOFException
import java.io.IOException
import java.io.InputStream

class CfDataRecord(private val sizeOfBlockData: Int) {
    /** checksum of this CFDATA entry , 4bytes  */
    private var csum = 0

    /** number of compressed bytes in this block , 2bytes  */
    var cbData = 0

    /** number of uncompressed bytes in this block , 2bytes  */
    var cbUncomp = 0

    @Throws(IOException::class, CabException::class)
    fun read(input: InputStream, bytes: ByteArray) {
        csum = from(input) // safe to use signed here, since checksum also returns signed
        cbData = LittleEndian.UShort_.from(input).toInt()
        cbUncomp = LittleEndian.UShort_.from(input).toInt()

        if (cbData > bytes.size) {
            throw CorruptCabException("Corrupt cfData record")
        }
        if (sizeOfBlockData != 0) {
            input.skip(sizeOfBlockData.toLong())
        }

        var readTotal = 0
        while (readTotal < cbData) {
            val read = input.read(bytes, readTotal, cbData - readTotal)
            if (read < 0) {
                throw EOFException()
            }
            readTotal += read
        }
    }

    private fun checksum(bytes: ByteArray): Int {
        val arrayOfByte = ByteArray(4)
        arrayOfByte[0] = (cbData and 0xFF).toByte()
        arrayOfByte[1] = (cbData ushr 8 and 0xFF).toByte()
        arrayOfByte[2] = (cbUncomp and 0xFF).toByte()
        arrayOfByte[3] = (cbUncomp ushr 8 and 0xFF).toByte()
        return Checksum.calculate(bytes, cbData, Checksum.calculate(arrayOfByte, 4, 0))
    }

    fun validateCheckSum(bytesToCheck: ByteArray): Boolean {
        return checksum(bytesToCheck) == csum
    }
}
