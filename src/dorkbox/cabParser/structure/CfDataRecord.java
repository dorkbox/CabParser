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
package dorkbox.cabParser.structure;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import dorkbox.cabParser.CabException;
import dorkbox.cabParser.Checksum;
import dorkbox.cabParser.CorruptCabException;
import dorkbox.bytes.LittleEndian;

public final class CfDataRecord {
    /** checksum of this CFDATA entry , 4bytes */
    private int csum;

    /** number of compressed bytes in this block , 2bytes */
    public int cbData;

    /** number of uncompressed bytes in this block , 2bytes */
    public int cbUncomp;

    private int sizeOfBlockData;

    public CfDataRecord(int sizeOfBlockData) {
        this.sizeOfBlockData = sizeOfBlockData;
    }

    public void read(InputStream input, byte[] bytes) throws IOException, CabException {
        this.csum = LittleEndian.Int_.from(input); // safe to use signed here, since checksum also returns signed
        this.cbData = LittleEndian.UShort_.from(input).intValue();
        this.cbUncomp = LittleEndian.UShort_.from(input).intValue();

        if (this.cbData > bytes.length) {
            throw new CorruptCabException("Corrupt cfData record");
        }

        if (this.sizeOfBlockData != 0) {
            input.skip(this.sizeOfBlockData);
        }


        int readTotal = 0;
        while (readTotal < this.cbData) {
            int read = input.read(bytes, readTotal, this.cbData - readTotal);
            if (read < 0) {
                throw new EOFException();
            }
            readTotal += read;
        }
    }

    private int checksum(byte[] bytes) {
        byte[] arrayOfByte = new byte[4];
        arrayOfByte[0] = (byte) (this.cbData & 0xFF);
        arrayOfByte[1] = (byte) (this.cbData >>> 8 & 0xFF);
        arrayOfByte[2] = (byte) (this.cbUncomp & 0xFF);
        arrayOfByte[3] = (byte) (this.cbUncomp >>> 8 & 0xFF);

        return Checksum.calculate(bytes, this.cbData, Checksum.calculate(arrayOfByte, 4, 0));
    }

    public boolean validateCheckSum(byte[] bytesToCheck) {
        return checksum(bytesToCheck) == this.csum;
    }
}
