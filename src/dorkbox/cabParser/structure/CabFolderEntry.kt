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
import java.io.IOException
import java.io.InputStream

class CabFolderEntry : CabConstants {
    /** offset of the first CFDATA block in this folder, 4bytes  */
    var coffCabStart: Long = 0

    /** number of CFDATA blocks in this folder, 2bytes  */
    var cCFData = 0

    /** compression type indicator , 2bytes  */
    var compressionMethod = 0

    @Throws(IOException::class)
    fun read(input: InputStream) {
        coffCabStart = LittleEndian.UInt_.from(input).toLong()
        cCFData = LittleEndian.UShort_.from(input).toInt()
        compressionMethod = LittleEndian.UShort_.from(input).toInt()
    }

    val compressionWindowSize: Int
        get() {
            if (compressionMethod == CabConstants.COMPRESSION_TYPE_NONE) {
                return 0
            }
            return if (compressionMethod == CabConstants.COMPRESSION_TYPE_MSZIP) {
                16
            }
            else compressionMethod and 0x1F00 ushr 8
        }

    fun compressionToString(): String {
        when (compressionMethod) {
            0    -> return "NONE"
            1    -> return "MSZIP"
            2    -> return "QUANTUM:" + compressionWindowSize.toString()
            3    -> {}
            else -> return "UNKNOWN"
        }
        return "LZX:" + compressionWindowSize.toString()
    }

    fun setCompression(a: Int, b: Int) {
        compressionMethod = b shl 8 or a
    }
}
