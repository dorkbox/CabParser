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
package dorkbox.cabparser.decompress;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import dorkbox.cabparser.CabException;
import dorkbox.cabparser.CorruptCabException;
import dorkbox.cabparser.decompress.lzx.DecompressLzx;
import dorkbox.cabparser.decompress.none.DecompressNone;
import dorkbox.cabparser.decompress.zip.DecompressZip;
import dorkbox.cabparser.structure.CabConstants;
import dorkbox.cabparser.structure.CfDataRecord;

public final class CabDecompressor implements CabConstants {
    private byte[]          readBuffer;
    private byte[]          bytes;

    private long            uncompressedDataSize;

    private int             outputOffset;
    private int             compressionMethod;

    private InputStream     inputStream;
    private Decompressor    decompressor;
    private CfDataRecord    cfDataRecord;

    public CabDecompressor(InputStream paramInputStream, int sizeOfBlockData) {
        this.inputStream = paramInputStream;
        this.uncompressedDataSize = 0L;
        this.outputOffset = 0;
        this.compressionMethod = -1;
        this.bytes = new byte[33028];
        this.cfDataRecord = new CfDataRecord(sizeOfBlockData);
    }

    public void read(long size, OutputStream outputStream) throws IOException, CabException {
        if (this.uncompressedDataSize >= size) {
            outputStream.write(this.bytes, this.outputOffset, (int) size);
            this.uncompressedDataSize -= size;
            this.outputOffset = (int) (this.outputOffset + size);
            return;
        }

        if (this.uncompressedDataSize > 0L) {
            outputStream.write(this.bytes, this.outputOffset, (int) this.uncompressedDataSize);
        }

        size -= this.uncompressedDataSize;
        this.outputOffset = 0;
        this.uncompressedDataSize = 0L;

        while (size > 0L) {
            this.cfDataRecord.read(this.inputStream, this.readBuffer);

            if (!this.cfDataRecord.validateCheckSum(this.readBuffer)) {
                throw new CorruptCabException("Invalid CFDATA checksum");
            }

            this.decompressor.decompress(this.readBuffer, this.bytes, this.cfDataRecord.cbData, this.cfDataRecord.cbUncomp);
            this.uncompressedDataSize = this.cfDataRecord.cbUncomp;
            this.outputOffset = 0;

            if (this.uncompressedDataSize >= size) {
                outputStream.write(this.bytes, this.outputOffset, (int) size);
                this.outputOffset = (int) (this.outputOffset + size);
                this.uncompressedDataSize -= size;
                size = 0L;
            } else {
                outputStream.write(this.bytes, this.outputOffset, (int) this.uncompressedDataSize);
                size -= this.uncompressedDataSize;
                this.outputOffset = 0;
                this.uncompressedDataSize = 0L;
            }
        }
    }

    public void initialize(int compressionMethod) throws CabException {
        int type = compressionMethod & 0xF;
        int windowBits = (compressionMethod & 0x1F00) >>> 8;

        if (compressionMethod == this.compressionMethod) {
            this.decompressor.reset(windowBits);
            return;
        }

        switch (type) {
            case COMPRESSION_TYPE_NONE :
                this.decompressor = new DecompressNone();
                break;
            case COMPRESSION_TYPE_MSZIP :
                this.decompressor = new DecompressZip();
                break;
            case COMPRESSION_TYPE_LZX :
                this.decompressor = new DecompressLzx();
                break;

            case COMPRESSION_TYPE_QUANTUM :
            default :
                throw new CabException("Unknown compression type " + type);
        }

        this.readBuffer = new byte[CabConstants.CAB_BLOCK_SIZE + this.decompressor.getMaxGrowth()];
        this.decompressor.init(windowBits);
        this.compressionMethod = compressionMethod;
    }
}
