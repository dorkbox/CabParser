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

import java.io.IOException;
import java.io.InputStream;

import dorkbox.util.bytes.LittleEndian;

public final class CabFolderEntry implements CabConstants {
    /** offset of the first CFDATA block in this folder, 4bytes */
    public long coffCabStart;
    /** number of CFDATA blocks in this folder, 2bytes */
    public int  cCFData;
    /** compression type indicator , 2bytes */
    public int  compressionMethod = 0;

    public int getCompressionMethod() {
        return this.compressionMethod & 0xF;
    }

    public void read(InputStream input) throws IOException {
        this.coffCabStart = LittleEndian.UInt_.from(input).longValue();
        this.cCFData = LittleEndian.UShort_.from(input).intValue();
        this.compressionMethod = LittleEndian.UShort_.from(input).intValue();
    }

    public int getCompressionWindowSize() {
        if (this.compressionMethod == COMPRESSION_TYPE_NONE) {
            return 0;
        }
        if (this.compressionMethod == COMPRESSION_TYPE_MSZIP) {
            return 16;
        }
        return (this.compressionMethod & 0x1F00) >>> 8;
    }

    public String compressionToString() {
        switch (getCompressionMethod()) {
            default :
                return "UNKNOWN";
            case 0 :
                return "NONE";
            case 1 :
                return "MSZIP";
            case 2 :
                return "QUANTUM:" + Integer.toString(getCompressionWindowSize());
            case 3 :
        }
        return "LZX:" + Integer.toString(getCompressionWindowSize());
    }

    public void setCompression(int a, int b) {
        this.compressionMethod = b << 8 | a;
    }
}

