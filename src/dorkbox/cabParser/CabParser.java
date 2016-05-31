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
package dorkbox.cabParser;

import dorkbox.cabParser.decompress.CabDecompressor;
import dorkbox.cabParser.structure.CabEnumerator;
import dorkbox.cabParser.structure.CabFileEntry;
import dorkbox.cabParser.structure.CabFolderEntry;
import dorkbox.cabParser.structure.CabHeader;
import dorkbox.util.process.NullOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

public final class CabParser {
    private CabInputStream cabInputStream;

    private CabStreamSaver streamSaver;
    private ByteArrayOutputStream outputStream = null;

    public CabHeader header;

    public CabFolderEntry[] folders;
    public CabFileEntry[]   files;

    public
    CabParser(InputStream inputStream, final String fileNameToExtract) throws CabException, IOException {
        if (fileNameToExtract == null || fileNameToExtract.isEmpty()) {
            throw new IllegalArgumentException("Filename must be valid!");
        }

        this.cabInputStream = new CabInputStream(inputStream);
        this.streamSaver = new CabStreamSaver() {
            @Override
            public boolean saveReservedAreaData(byte[] data, int dataLength) {
                return false;
            }

            @Override
            public OutputStream openOutputStream(CabFileEntry cabFile) {
                String name = cabFile.getName();
                if (fileNameToExtract.equalsIgnoreCase(name)) {
                    CabParser.this.outputStream = new ByteArrayOutputStream((int) cabFile.getSize());
                    return CabParser.this.outputStream;
                } else {
                    return null;
                }
            }

            @Override
            public void closeOutputStream(OutputStream outputStream, CabFileEntry cabFile) {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        };

        readData();
    }

    public
    CabParser(InputStream inputStream, CabStreamSaver streamSaver) throws CabException, IOException {
        this.streamSaver = streamSaver;
        this.cabInputStream = new CabInputStream(inputStream);

        readData();
    }

    /**
     * Gets the version number.
     */
    public static
    String getVersion() {
        return "2.9";
    }

    public Enumeration<Object> entries() {
        return new CabEnumerator(this, false);
    }

    public Enumeration<Object> entries(boolean b) {
        return new CabEnumerator(this, b);
    }

    private void readData() throws CabException, IOException {
        this.header = new CabHeader(this.streamSaver);
        this.header.read(this.cabInputStream);

        if (this.header.cbCabinet <= 2147483647L) {
            this.cabInputStream.mark((int) this.header.cbCabinet);
        }

        this.folders = new CabFolderEntry[this.header.cFolders];
        for (int i = 0; i < this.header.cFolders; i++) {
            this.folders[i] = new CabFolderEntry();
            this.folders[i].read(this.cabInputStream);
        }

        this.files = new CabFileEntry[this.header.cFiles];
        this.cabInputStream.seek(this.header.coffFiles);

        for (int i = 0; i < this.header.cFiles; i++) {
            this.files[i] = new CabFileEntry();
            this.files[i].read(this.cabInputStream);
        }

        if (this.header.cbCabinet <= 2147483647L) {
            this.cabInputStream.mark((int) this.header.cbCabinet);
        }
    }

    public ByteArrayOutputStream extractStream() throws CabException, IOException {
        int folderCount = -1;
        int currentCount = 0;
        int totalCount = 0;
        boolean init = true;

        CabDecompressor extractor = new CabDecompressor(this.cabInputStream, this.header.cbCFData);

        for (int fileIndex = 0; fileIndex < this.header.cFiles; fileIndex++) {
            CabFileEntry entry = this.files[fileIndex];

            if (entry.iFolder != folderCount) {
                if (folderCount + 1 >= this.header.cFolders) {
                    throw new CorruptCabException();
                }

                folderCount++;
                init = true;
                currentCount = 0;
                totalCount = 0;
            }

            OutputStream localOutputStream = this.streamSaver.openOutputStream(entry);
            if (localOutputStream != null) {
                if (init) {
                    CabFolderEntry cabFolderEntry = this.folders[folderCount];

                    this.cabInputStream.seek(cabFolderEntry.coffCabStart);
                    extractor.initialize(cabFolderEntry.compressionMethod);
                    init = false;
                }

                if (currentCount != totalCount) {
                    extractor.read(totalCount - currentCount, new NullOutputStream());
                    currentCount = totalCount;
                }

                extractor.read(entry.cbFile, localOutputStream);
                this.streamSaver.closeOutputStream(localOutputStream, entry);
                currentCount = (int) (currentCount + entry.cbFile);
            }

            totalCount = (int) (totalCount + entry.cbFile);
        }

        return this.outputStream;
    }
}

