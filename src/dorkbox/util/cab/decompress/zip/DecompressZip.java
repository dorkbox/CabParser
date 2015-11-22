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
package dorkbox.util.cab.decompress.zip;

import dorkbox.util.cab.CabException;
import dorkbox.util.cab.CorruptCabException;
import dorkbox.util.cab.decompress.Decompressor;

public final class DecompressZip implements Decompressor {
    private static final int[] ar1 = {3,4,5,6,7,8,9,10,11,13,15,17,19,23,27,31,
                                    35,43,51,59, 67,83,99,115,131,163,195,227,258};

    private static final int[] ar2 = {1,2,3,4,5,7,9,13,17,25,33,49,65,97,129,193,257,385,513,
                                    769,1025,1537,2049,3073,4097,6145,8193,12289,16385,24577};

    private static final int[] ar3 = {16,17,18,0,8,7,9,6,10,5,11,4,12,3,13,2,14,1,15};

    private byte[] bytes = new byte[320];
    private byte[] inputBytes;
    private byte[] outputBytes;

    private int index;
    private int inputPlus4;

    int         int1;

    private int int2;
    private int int3;
    private int outputLength;

    private DecompressZipState state1;
    private DecompressZipState state2;
    private DecompressZipState state3;

    @Override
    public void init(int windowBits) {
        this.state1 = new DecompressZipState(288, 9, this);
        this.state2 = new DecompressZipState(32, 7, this);
        this.state3 = new DecompressZipState(19, 7, this);
    }

    @Override
    public void decompress(byte[] inputBytes, byte[] outputBytes, int inputLength, int outputLength) throws CabException {
        this.inputBytes = inputBytes;
        this.outputBytes = outputBytes;

        if (this.inputBytes[0] != 67 || this.inputBytes[1] != 75) {
            throw new CorruptCabException();
        }
        if (outputBytes.length < 33027) {
            throw new CabException();
        }
        if (inputBytes.length < 28) {
            throw new CabException();
        }

        this.index = 2;
        this.inputPlus4 = inputLength + 4;
        this.outputLength = outputLength;
        this.int3 = 0;

        maybeDecompress();
        while (this.int3 < this.outputLength) {
            decompressMore();
        }
    }

    @Override
    public int getMaxGrowth() {
        return 28;
    }

    @Override
    public void reset(int windowBits) {
    }

    private void maybeDecompress() throws CabException {
        if (this.index + 4 > this.inputPlus4) {
            throw new CorruptCabException();
        }
        this.int1 = readShort() | readShort() << 16;
        this.int2 = 16;
    }

    void add(int paramInt) {
        this.int1 >>>= paramInt;
        this.int2 -= paramInt;
        if (this.int2 <= 0) {
            this.int2 += 16;
            this.int1 |= (this.inputBytes[this.index] & 0xFF | (this.inputBytes[this.index + 1] & 0xFF) << 8) << this.int2;
            this.index += 2;
        }
    }

    private void check() throws CabException {
        if (this.index > this.inputPlus4) {
            throw new CorruptCabException();
        }
    }

    @SuppressWarnings({"NumericCastThatLosesPrecision", "Duplicates"})
    private void bits() throws CabException {
        int i = this.int3;
        int j = this.outputLength;
        byte[] arrayOfByte1 = this.outputBytes;
        int[] arrayOfInt1 = this.state1.intA2;
        int[] arrayOfInt2 = this.state1.intA3;
        int[] arrayOfInt3 = this.state1.intA4;
        byte[] arrayOfByte2 = this.state1.byteA;
        int[] arrayOfInt4 = this.state2.intA2;
        int[] arrayOfInt5 = this.state2.intA3;
        int[] arrayOfInt6 = this.state2.intA4;
        byte[] arrayOfByte3 = this.state2.byteA;
        int k = this.int1;
        int m = this.int2;

        do {
            if (this.index > this.inputPlus4) {
                break;
            }
            int n = arrayOfInt1[k & 0x1FF];
            int i2;
            while (n < 0) {
                i2 = 512;
                do {
                    n = -n;
                    if ((k & i2) == 0) {
                        n = arrayOfInt2[n];
                    } else {
                        n = arrayOfInt3[n];
                    }
                    i2 <<= 1;
                } while (n < 0);
            }
            int i1 = arrayOfByte2[n];
            k >>>= i1;
            m -= i1;
            if (m <= 0) {
                m += 16;
                k |= (this.inputBytes[this.index] & 0xFF | (this.inputBytes[this.index + 1] & 0xFF) << 8) << m;
                this.index += 2;
            }
            if (n < 256) {
                arrayOfByte1[i++] = (byte) n;
            } else {
                n -= 257;
                if (n < 0) {
                    break;
                }
                if (n < 8) {
                    n += 3;
                } else if (n != 28) {
                    int i4 = n - 4 >>> 2;
                    n = ar1[n] + (k & (1 << i4) - 1);
                    k >>>= i4;
                    m -= i4;
                    if (m <= 0) {
                        m += 16;
                        k |= (this.inputBytes[this.index] & 0xFF | (this.inputBytes[this.index + 1] & 0xFF) << 8) << m;
                        this.index += 2;
                    }
                } else {
                    n = 258;
                }
                i2 = arrayOfInt4[k & 0x7F];
                while (i2 < 0) {
                    int i5 = 128;
                    do {
                        i2 = -i2;
                        if ((k & i5) == 0) {
                            i2 = arrayOfInt5[i2];
                        } else {
                            i2 = arrayOfInt6[i2];
                        }
                        i5 <<= 1;
                    } while (i2 < 0);
                }
                i1 = arrayOfByte3[i2];
                k >>>= i1;
                m -= i1;
                if (m <= 0) {
                    m += 16;
                    k |= (this.inputBytes[this.index] & 0xFF | (this.inputBytes[this.index + 1] & 0xFF) << 8) << m;
                    this.index += 2;
                }
                int i4 = i2 - 2 >> 1;
                int i3;
                if (i4 > 0) {
                    i3 = ar2[i2] + (k & (1 << i4) - 1);
                    k >>>= i4;
                    m -= i4;
                    if (m <= 0) {
                        m += 16;
                        k |= (this.inputBytes[this.index] & 0xFF | (this.inputBytes[this.index + 1] & 0xFF) << 8) << m;
                        this.index += 2;
                    }
                } else {
                    i3 = i2 + 1;
                }
                do {
                    arrayOfByte1[i] = arrayOfByte1[i - i3 & 0x7FFF];
                    i++;
                    n--;
                } while (n != 0);
            }
        } while (i <= j);
        this.int3 = i;
        this.int1 = k;
        this.int2 = m;
        check();
    }

    private void readStuffcommon() throws CabException {
        this.state1.main();
        this.state2.main();
    }

    private void setup() {
        int i = 0;

        do {
            this.state1.byteA[i] = (byte) 8;
            i++;
        } while (i <= 143);

        i = 144;
        do {
            this.state1.byteA[i] = (byte) 9;
            i++;
        } while (i <= 255);
        i = 256;

        do {
            this.state1.byteA[i] = (byte) 7;
            i++;
        } while (i <= 279);
        i = 280;

        do {
            this.state1.byteA[i] = (byte) 8;
            i++;
        } while (i <= 287);

        i = 0;
        do {
            this.state2.byteA[i] = (byte) 5;
            i++;
        } while (i < 32);
    }

    private int makeShort(byte byte1, byte byte2) {
        return byte1 & 0xFF | (byte2 & 0xFF) << 8;
    }

    @SuppressWarnings("NumericCastThatLosesPrecision")
    private void expand() throws CabException {
        check();
        int i = calc(5) + 257;
        int j = calc(5) + 1;
        int k = calc(4) + 4;
        for (int n = 0; n < k; n++) {
            this.state3.byteA[ar3[n]] = (byte) calc(3);
            check();
        }
        for (int n = k; n < ar3.length; n++) {
            this.state3.byteA[ar3[n]] = (byte) 0;
        }
        this.state3.main();
        int m = i + j;
        int n = 0;
        while (n < m) {
            check();
            int i1 = (byte) this.state3.read();
            if (i1 <= 15) {
                this.bytes[n++] = (byte) i1;
            } else {
                int i3;
                int i2;
                if (i1 == 16) {
                    if (n == 0) {
                        throw new CorruptCabException();
                    }
                    i3 = this.bytes[n - 1];
                    i2 = calc(2) + 3;
                    if (n + i2 > m) {
                        throw new CorruptCabException();
                    }
                    for (int i4 = 0; i4 < i2; i4++) {
                        this.bytes[n++] = (byte) i3;
                    }
                } else if (i1 == 17) {
                    i2 = calc(3) + 3;
                    if (n + i2 > m) {
                        throw new CorruptCabException();
                    }
                    for (i3 = 0; i3 < i2; i3++) {
                        this.bytes[n++] = (byte) 0;
                    }
                } else {
                    i2 = calc(7) + 11;
                    if (n + i2 > m) {
                        throw new CorruptCabException();
                    }
                    for (i3 = 0; i3 < i2; i3++) {
                        this.bytes[n++] = (byte) 0;
                    }
                }
            }
        }

        System.arraycopy(this.bytes, 0, this.state1.byteA, 0, i);
        for (n = i; n < 288; n++) {
            this.state1.byteA[n] = (byte) 0;
        }
        for (n = 0; n < j; n++) {
            this.state2.byteA[n] = this.bytes[n + i];
        }
        for (n = j; n < 32; n++) {
            this.state2.byteA[n] = (byte) 0;
        }
        if (this.state1.byteA[256] == 0) {
            throw new CorruptCabException();
        }
    }

    private int readShort() {
        int i = this.inputBytes[this.index] & 0xFF | (this.inputBytes[this.index + 1] & 0xFF) << 8;
        this.index += 2;
        return i;
    }

    private int calc(int paramInt) {
        int i = this.int1 & (1 << paramInt) - 1;
        add(paramInt);
        return i;
    }

    private void decompressMore() throws CabException {
        @SuppressWarnings("unused")
        int i = calc(1);
        int j = calc(2);
        if (j == 2) {
            expand();
            readStuffcommon();
            bits();
            return;
        }
        if (j == 1) {
            setup();
            readStuffcommon();
            bits();
            return;
        }
        if (j == 0) {
            verify();
            return;
        }

        throw new CabException();
    }

    private void verify() throws CabException {
        mod();
        if (this.index >= this.inputPlus4) {
            throw new CorruptCabException();
        }
        int i = makeShort(this.inputBytes[this.index], this.inputBytes[this.index + 1]);
        int j = makeShort(this.inputBytes[this.index + 2], this.inputBytes[this.index + 3]);

        //noinspection NumericCastThatLosesPrecision
        if ((short) i != (short) (~j)) {
            throw new CorruptCabException();
        }

        if (this.index + i > this.inputPlus4 || this.int3 + i > this.outputLength) {
            throw new CorruptCabException();
        }

        maybeDecompress();

        System.arraycopy(this.inputBytes, this.index, this.outputBytes, this.int3, i);
        this.int3 += i;
        if (this.int3 < this.outputLength) {
            maybeDecompress();
        }
    }

    private void mod() {
        if (this.int2 == 16) {
            this.index -= 4;
        } else if (this.int2 >= 8) {
            this.index -= 3;
        } else {
            this.index -= 2;
        }
        if (this.index < 0) {
            this.index = 0;
        }
        this.int2 = 0;
    }
}
