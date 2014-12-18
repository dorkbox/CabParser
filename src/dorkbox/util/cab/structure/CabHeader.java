/*
 * Copyright 2012 dorkbox, llc
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
package dorkbox.util.cab.structure;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import dorkbox.util.bytes.LittleEndian;
import dorkbox.util.cab.CabException;
import dorkbox.util.cab.CabStreamSaver;
import dorkbox.util.cab.CorruptCabException;

public final class CabHeader implements CabConstants {

    /** reserved , 4 bytes */
    public long reserved1;
    /** size of this cabinet file in bytes , 4 bytes */
    public long cbCabinet;
    /** reserved, 4 bytes */
    public long reserved2;
    /** offset of the first CFFILE entry , 4 bytes */
    public long coffFiles;
    /** reserved, 4 bytes*/
    public long reserved3;

    /** cabinet file format version, minor/major , 1 bytes*2 */
    public int version;

    /** number of CFFOLDER entries in this cabinet , 2 bytes */
    public int cFolders;
    /** number of CFFILE entries in this cabinet , 2 bytes */
    public int cFiles;

    /** cabinet file option indicators , 2 bytes */
    public int flags;
    /** must be the same for all cabinets in a set , 2 bytes */
    public int setID;
    /** number of this cabinet file in a set , 2 bytes */
    public int iCabinet;

    /** (optional) size of per-cabinet reserved area , 2 bytes */
    public int cbCFHeader = 0;
    /** (optional) size of per-folder reserved area , 1 bytes */
    public int cbCFFolder = 0;
    /** (optional) size of per-datablock reserved area , 1 bytes */
    public int cbCFData = 0;

    /** (optional) per-cabinet reserved area , 1*n bytes */
    //final short abReserve[];

    /** (optional) name of previous cabinet file , 1*n bytes */
    //final String szCabinetPrev;

    /** (optional) name of previous disk , 1*n bytes */
    //final String szDiskPrev;

    /** (optional) name of next cabinet file , 1*n bytes */
    //final String szCabinetNext;

    /** (optional) name of next disk , 1*n bytes */
    //final String szDiskNext;


    private CabStreamSaver decoder;

    public CabHeader(CabStreamSaver paramCabDecoderInterface) {
        this.decoder = paramCabDecoderInterface;
    }

    public void read(InputStream input) throws IOException, CabException {
        int i = input.read();
        int j = input.read();
        int k = input.read();
        int m = input.read();
        // MSCF
        if (i != 77 || j != 83 || k != 67 || m != 70) {
            throw new CorruptCabException("Missing header signature");
        }

        this.reserved1 = LittleEndian.UInt_.from(input).longValue();
        this.cbCabinet = LittleEndian.UInt_.from(input).longValue();
        this.reserved2 = LittleEndian.UInt_.from(input).longValue();
        this.coffFiles = LittleEndian.UInt_.from(input).longValue();
        this.reserved3 = LittleEndian.UInt_.from(input).longValue();


        // Currently, versionMajor = 1 and versionMinor = 3
        this.version = LittleEndian.UShort_.from(input).intValue();
        this.cFolders = LittleEndian.UShort_.from(input).intValue();
        this.cFiles = LittleEndian.UShort_.from(input).intValue();
        this.flags = LittleEndian.UShort_.from(input).intValue();
        this.setID = LittleEndian.UShort_.from(input).intValue();
        this.iCabinet = LittleEndian.UShort_.from(input).intValue();



        if ((this.flags & FLAG_RESERVE_PRESENT) == FLAG_RESERVE_PRESENT) {
            this.cbCFHeader = LittleEndian.UShort_.from(input).intValue();
            this.cbCFFolder = input.read();
            this.cbCFData = input.read();
        }

        if ((this.flags & FLAG_PREV_CABINET) == FLAG_PREV_CABINET ||
            (this.flags & FLAG_NEXT_CABINET) == FLAG_NEXT_CABINET) {
            throw new CabException("Spanned cabinets not supported");
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

        if (this.cbCFHeader != 0) {
            if (this.decoder.saveReservedAreaData(null, this.cbCFHeader) == true) {
                byte[] data = new byte[this.cbCFHeader];

                if (this.cbCFHeader != 0) {
                    int readTotal = 0;
                    while (readTotal < this.cbCFHeader) {
                        int read = input.read(data, readTotal, this.cbCFHeader - readTotal);
                        if (read < 0) {
                            throw new EOFException();
                        }
                        readTotal += read;
                    }
                }

                this.decoder.saveReservedAreaData(data, this.cbCFHeader);
                return;
            }

            input.skip(this.cbCFHeader);
        }
    }
}
