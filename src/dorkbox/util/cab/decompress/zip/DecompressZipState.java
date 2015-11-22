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

import dorkbox.util.cab.CorruptCabException;

final class DecompressZipState {
    private int               intA;
    private int[]             intA1;

    private int               intB;
    private int               intC;
    private int               intD;

    private DecompressZip     decompressZipImpl;

    byte[]                    byteA;
    int[]                     intA2;
    int[]                     intA3;
    int[]                     intA4;

    DecompressZipState(int paramInt1, int paramInt2, DecompressZip decompressZipImpl) {
        this.intA = paramInt1;
        this.decompressZipImpl = decompressZipImpl;
        this.byteA = new byte[paramInt1];
        this.intA1 = new int[paramInt1];
        this.intB = paramInt2;
        this.intC = 1 << this.intB;
        this.intD = this.intC - 1;
        this.intA2 = new int[1 << this.intB];
        this.intA3 = new int[this.intA * 2];
        this.intA4 = new int[this.intA * 2];
    }

    void main() throws CorruptCabException {
        int[] arrayOfInt1 = new int[17];
        int[] arrayOfInt2 = new int[17];
        int k = 0;
        do {
            arrayOfInt1[k] = 0;
            k++;
        } while (k <= 16);
        for (k = 0; k < this.intA; k++) {
            arrayOfInt1[this.byteA[k]] += 1;
        }
        int m;
        for (k = this.intB; k <= 16; k++) {
            if (arrayOfInt1[k] > 0) {
                for (m = 0; m < this.intC; m++) {
                    this.intA2[m] = 0;
                }
                break;
            }
        }
        int i = 0;
        arrayOfInt1[0] = 0;
        k = 1;
        do {
            i = i + arrayOfInt1[k - 1] << 1;
            arrayOfInt2[k] = i;
            k++;
        } while (k <= 16);
        for (k = 0; k < this.intA; k++) {
            m = this.byteA[k];
            if (m > 0) {
                this.intA1[k] = shiftAndOtherStuff(arrayOfInt2[m], m);
                arrayOfInt2[m] += 1;
            }
        }
        int j = this.intA;
        for (k = 0; k < this.intA; k++) {
            int n = this.byteA[k];
            m = this.intA1[k];
            if (n > 0) {
                int i1;
                int i2;
                int i3;
                if (n <= this.intB) {
                    i1 = 1 << this.intB - n;
                    i2 = 1 << n;
                    if (m >= i2) {
                        throw new CorruptCabException();
                    }
                    for (i3 = 0; i3 < i1; i3++) {
                        this.intA2[m] = k;
                        m += i2;
                    }
                } else {
                    i1 = n - this.intB;
                    i2 = 1 << this.intB;
                    int i4 = m & this.intD;
                    i3 = 2;
                    do {
                        int i5;
                        if (i3 == 2) {
                            i5 = this.intA2[i4];
                        } else if (i3 == 1) {
                            i5 = this.intA4[i4];
                        } else {
                            i5 = this.intA3[i4];
                        }
                        if (i5 == 0) {
                            this.intA3[j] = 0;
                            this.intA4[j] = 0;
                            if (i3 == 2) {
                                this.intA2[i4] = -j;
                            } else if (i3 == 1) {
                                this.intA4[i4] = -j;
                            } else {
                                this.intA3[i4] = -j;
                            }
                            i5 = -j;
                            j++;
                        }
                        i4 = -i5;
                        if ((m & i2) == 0) {
                            i3 = 0;
                        } else {
                            i3 = 1;
                        }
                        i2 <<= 1;
                        i1--;
                    } while (i1 != 0);
                    if (i3 == 0) {
                        this.intA3[i4] = k;
                    } else {
                        this.intA4[i4] = k;
                    }
                }
            }
        }
    }

    private int shiftAndOtherStuff(int paramInt1, int paramInt2) {
        int i = 0;
        do {
            i |= paramInt1 & 0x1;
            i <<= 1;
            paramInt1 >>>= 1;
            paramInt2--;
        } while (paramInt2 > 0);

        return i >>> 1;
    }

    int read() {
        int i = this.intA2[this.decompressZipImpl.int1 & this.intD];
        while (i < 0) {
            int j = 1 << this.intB;
            do {
                i = -i;
                if ((this.decompressZipImpl.int1 & j) == 0) {
                    i = this.intA3[i];
                } else {
                    i = this.intA4[i];
                }
                j <<= 1;
            } while (i < 0);
        }
        this.decompressZipImpl.add(this.byteA[i]);
        return i;
    }
}
