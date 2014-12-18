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

import java.util.Enumeration;
import java.util.NoSuchElementException;

import dorkbox.util.cab.CabDecoder;

public final class CabEnumerator implements Enumeration<Object> {
    private int        fileCount = 0;
    private int        folderCount = 0;

    private CabDecoder cabDecoder;

    private boolean    b;
    private int        folderIndex;

    @Override
    public Object nextElement() {
        if (!this.b) {
            if (this.fileCount < this.cabDecoder.header.cFiles) {
                return this.cabDecoder.files[this.fileCount++];
            }
            throw new NoSuchElementException();
        }

        if (this.cabDecoder.files[this.fileCount].iFolder != this.folderIndex) {
            this.folderIndex = this.cabDecoder.files[this.fileCount].iFolder;

            if (this.folderCount < this.cabDecoder.folders.length) {
                return this.cabDecoder.folders[this.folderCount++];
            }
        }

        if (this.fileCount < this.cabDecoder.header.cFiles) {
            return this.cabDecoder.files[this.fileCount++];
        }

        throw new NoSuchElementException();
    }

    public CabEnumerator(CabDecoder decoder, boolean b) {
        this.cabDecoder = decoder;
        this.b = b;
        this.folderIndex = -2;
    }

    @Override
    public boolean hasMoreElements() {
        return this.fileCount < this.cabDecoder.header.cFiles;
    }
}
