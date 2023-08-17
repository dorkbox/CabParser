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
import dorkbox.cabParser.CabStreamSaver
import dorkbox.cabParser.CorruptCabException
import java.io.EOFException
import java.io.IOException
import java.io.InputStream

class CabHeader
    (
    /** (optional) name of next cabinet file , 1*n bytes  */ //final String szCabinetNext;
    /** (optional) name of next disk , 1*n bytes  */ //final String szDiskNext;
    private val decoder: CabStreamSaver
) : CabConstants {
    /** reserved , 4 bytes  */
    var reserved1: Long = 0

    /** size of this cabinet file in bytes , 4 bytes  */
    var cbCabinet: Long = 0

    /** reserved, 4 bytes  */
    var reserved2: Long = 0

    /** offset of the first CFFILE entry , 4 bytes  */
    var coffFiles: Long = 0

    /** reserved, 4 bytes */
    var reserved3: Long = 0

    /** cabinet file format version, minor/major , 1 bytes*2  */
    var version = 0

    /** number of CFFOLDER entries in this cabinet , 2 bytes  */
    var cFolders = 0

    /** number of CFFILE entries in this cabinet , 2 bytes  */
    var cFiles = 0

    /** cabinet file option indicators , 2 bytes  */
    var flags = 0

    /** must be the same for all cabinets in a set , 2 bytes  */
    var setID = 0

    /** number of this cabinet file in a set , 2 bytes  */
    var iCabinet = 0

    /** (optional) size of per-cabinet reserved area , 2 bytes  */
    var cbCFHeader = 0

    /** (optional) size of per-folder reserved area , 1 bytes  */
    var cbCFFolder = 0

    /** (optional) size of per-datablock reserved area , 1 bytes  */
    var cbCFData = 0

    /** (optional) per-cabinet reserved area , 1*n bytes  */ //final short abReserve[];
    /** (optional) name of previous cabinet file , 1*n bytes  */ //final String szCabinetPrev;
    /** (optional) name of previous disk , 1*n bytes  */ //final String szDiskPrev;

    @Throws(IOException::class, CabException::class)
    fun read(input: InputStream) {
        val i = input.read()
        val j = input.read()
        val k = input.read()
        val m = input.read()

        // Contains the characters "M", "S", "C", and "F" (bytes 0x4D, 0x53, 0x43, 0x46). This field is used to ensure that the file is a cabinet (.cab) file.
        if (i != 77 || j != 83 || k != 67 || m != 70) {
            throw CorruptCabException("Missing header signature")
        }
        reserved1 = LittleEndian.UInt_.from(input).toLong() // must be 0
        cbCabinet = LittleEndian.UInt_.from(input).toLong() // Specifies the total size of the cabinet file, in bytes.
        reserved2 = LittleEndian.UInt_.from(input).toLong() // must be 0
        coffFiles = LittleEndian.UInt_.from(input).toLong() // Specifies the absolute file offset, in bytes, of the first CFFILE field entry.
        reserved3 = LittleEndian.UInt_.from(input).toLong() // must be 0


        // Currently, versionMajor = 1 and versionMinor = 3
        version = LittleEndian.UShort_.from(input).toInt()
        cFolders = LittleEndian.UShort_.from(input).toInt()
        cFiles = LittleEndian.UShort_.from(input).toInt()
        flags = LittleEndian.UShort_.from(input).toInt()
        setID = LittleEndian.UShort_.from(input).toInt()
        iCabinet = LittleEndian.UShort_.from(input).toInt()

        if (flags and CabConstants.FLAG_RESERVE_PRESENT == CabConstants.FLAG_RESERVE_PRESENT) {
            cbCFHeader = LittleEndian.UShort_.from(input).toInt()
            cbCFFolder = input.read()
            cbCFData = input.read()
        }

        if (flags and CabConstants.FLAG_PREV_CABINET == CabConstants.FLAG_PREV_CABINET || flags and CabConstants.FLAG_NEXT_CABINET == CabConstants.FLAG_NEXT_CABINET) {
            throw CabException("Spanned cabinets not supported")
        }

        // not supported
//        if (prevCabinet()) {
//            szCabinetPrev = bytes.readString(false);
//            szDiskPrev = bytes.readString(false);
//        } else {
//            szCabinetPrev = null;
//            szDiskPrev = null;
//        }
//
//        if (nextCabinet()) {
//            szCabinetNext = bytes.readString(false);
//            szDiskNext = bytes.readString(false);
//        } else {
//            szCabinetNext = null;
//            szDiskNext = null;
//        }

        if (cbCFHeader != 0) {
            if (decoder.saveReservedAreaData(null, cbCFHeader) == true) {
                val data = ByteArray(cbCFHeader)
                if (cbCFHeader != 0) {
                    var readTotal = 0
                    while (readTotal < cbCFHeader) {
                        val read = input.read(data, readTotal, cbCFHeader - readTotal)
                        if (read < 0) {
                            throw EOFException()
                        }
                        readTotal += read
                    }
                }
                decoder.saveReservedAreaData(data, cbCFHeader)
                return
            }
            input.skip(cbCFHeader.toLong())
        }
    }
}
