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
package dorkbox.cabparser.structure;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Date;

import dorkbox.cabparser.CabException;
import dorkbox.cabparser.CorruptCabException;
import dorkbox.util.bytes.LittleEndian;

public final class CabFileEntry {
    public static final Charset US_ASCII = Charset.forName("US-ASCII");

    /** file is read-only (in HEX) */
    static final int READONLY = 0x01;

    /** file is hidden (in HEX) */
    static final int HIDDEN = 0x02;

    /** file is a system file (in HEX) */
    static final int SYSTEM = 0x04;

    /** file modified since last backup (in HEX) */
    static final int ARCHIVE = 0x20;

    /** szName[] contains UTF (in HEX) */
    static final int NAME_IS_UTF = 0x80;

    /** uncompressed size of this file in bytes , 4bytes */
    public long           cbFile;
    /** uncompressed offset of this file in the folder , 4bytes */
    public long           offFolderStart;
    /** index into the CFFOLDER area , 2bytes */
    public int            iFolder;
    /** time/date stamp for this file , 2bytes */
    public  Date           date = new Date();
    /** attribute flags for this file , 2bytes */
    public int            attribs;

    /** name of this file , 1*n bytes */
    public String         szName;

    private Object objectOrSomething;

    public void read(InputStream input) throws IOException, CabException {
        byte[] arrayOfByte = new byte[256];

        this.cbFile = LittleEndian.UInt_.from(input).longValue();
        this.offFolderStart = LittleEndian.UInt_.from(input).longValue();
        this.iFolder = LittleEndian.UShort_.from(input).intValue();

        int timeA = LittleEndian.UShort_.from(input).intValue();
        int timeB = LittleEndian.UShort_.from(input).intValue();
        this.date = getDate(timeA, timeB);

        this.attribs = LittleEndian.UShort_.from(input).intValue();

        int i = 0;

        for (i = 0; i < arrayOfByte.length; i++) {
            int m = input.read();
            if (m == -1) {
                throw new CorruptCabException("EOF reading cffile");
            }
            arrayOfByte[i] = (byte) m;
            if (m == 0) {
                break;
            }
        }

        if (i >= arrayOfByte.length) {
            throw new CorruptCabException("cffile filename not null terminated");
        }

        if ((this.attribs & NAME_IS_UTF) == NAME_IS_UTF) {
            this.szName = readUtfString(arrayOfByte);

            if (this.szName == null) {
                throw new CorruptCabException("invalid utf8 code");
            }
        } else {
            this.szName = new String(arrayOfByte, 0, i, US_ASCII).trim();
        }
    }

    public boolean isReadOnly() {
        return (this.attribs & READONLY) != 0;
    }

    public void setReadOnly(boolean bool) {
        if (bool) {
            this.attribs |= 1;
            return;
        }

        this.attribs &= -2;
    }

    public boolean isHidden() {
        return (this.attribs & HIDDEN) != 0;
    }

    public void setHidden(boolean bool) {
        if (bool) {
            this.attribs |= 2;
            return;
        }

        this.attribs &= -3;
    }

    public boolean isSystem() {
        return (this.attribs & SYSTEM) != 0;
    }

    public void setSystem(boolean bool) {
        if (bool) {
            this.attribs |= 4;
            return;
        }
        this.attribs &= -5;
    }

    public boolean isArchive() {
        return (this.attribs & ARCHIVE) != 0;
    }

    public void setArchive(boolean bool) {
        if (bool) {
            this.attribs |= 32;
            return;
        }
        this.attribs &= -33;
    }

    public String getName() {
        return this.szName;
    }

    public long getSize() {
        return this.cbFile;
    }

    public void setName(String name) {
        this.szName = name;
    }

    public void setSize(long size) {
        this.cbFile = (int) size;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date paramDate) {
        this.date = paramDate;
    }

    @SuppressWarnings("deprecation")
    private Date getDate(int dateInfo, int timeInfo) {
        int i = dateInfo & 0x1F;
        int j = (dateInfo >>> 5) - 1 & 0xF;
        int k = (dateInfo >>> 9) + 80;
        int m = (timeInfo & 0x1F) << 1;
        int n = timeInfo >>> 5 & 0x3F;
        int i1 = timeInfo >>> 11 & 0x1F;
        return new Date(k, j, i, i1, n, m);
    }

    public Object getApplicationData() {
        return this.objectOrSomething;
    }

    public void setApplicationData(Object obj) {
        this.objectOrSomething = obj;
    }

    @Override
    public String toString() {
        return this.szName;
    }

    private static String readUtfString(byte[] stringBytes) {
        int j = 0;
        int stringSize = 0;
        int k = 0;

        // count the size of the string
        for (stringSize = 0; stringBytes[stringSize] != 0; stringSize++) {}

        char[] stringChars = new char[stringSize];
        for (k = 0; stringBytes[j] != 0; k++) {
            int m = (char) (stringBytes[j++] & 0xFF);

            if (m < 128) {
                stringChars[k] = (char) m;
            } else {
                if (m < 192) {
                    return null;
                }

                if (m < 224) {
                    stringChars[k] = (char) ((m & 0x1F) << 6);
                    m = (char) (stringBytes[j++] & 0xFF);
                    if (m < 128 || m > 191) {
                        return null;
                    }
                    stringChars[k] = (char) (stringChars[k] | (char) (m & 0x3F));
                }
                else if (m < 240) {
                    stringChars[k] = (char) ((m & 0xF) << 12);
                    m = (char) (stringBytes[j++] & 0xFF);
                    if (m < 128 || m > 191) {
                        return null;
                    }

                    stringChars[k] = (char) (stringChars[k] | (char) ((m & 0x3F) << 6));
                    m = (char) (stringBytes[j++] & 0xFF);
                    if (m < 128 || m > 191) {
                        return null;
                    }
                    stringChars[k] = (char) (stringChars[k] | (char) (m & 0x3F));
                } else {
                    return null;
                }
            }
        }

        return new String(stringChars, 0, k);
    }
}

