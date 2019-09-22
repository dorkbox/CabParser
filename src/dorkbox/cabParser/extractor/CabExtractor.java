/*
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
package dorkbox.cabParser.extractor;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;

import dorkbox.cabParser.CabException;
import dorkbox.cabParser.CabParser;
import dorkbox.cabParser.CabStreamSaver;
import dorkbox.cabParser.structure.CabFileEntry;
import dorkbox.cabParser.structure.CabFolderEntry;
import dorkbox.cabParser.structure.CabHeader;

/**
 * Extracts the content of CAB (file or stream).
 */
public class CabExtractor {

    private final InputStream inputStream;
    private final CabParser parser;
    private final AtomicBoolean done = new AtomicBoolean(false);

    /**
     * To extract all files from CAB file and save to sub-directory (named after
     * CAB file name) of working directory.
     * 
     * @param cabFile
     *            CAB file
     */
    public CabExtractor(File cabFile) throws CabException, IOException {
        this(cabFile, null, new DefaultCabFileSaver(null, cabFile));
    }

    /**
     * To extract some files from CAB file and save to sub-directory (named
     * after CAB file name) of working directory.
     * 
     * @param cabFile
     *            CAB file
     * @param filter
     *            which files to extract (extract all files if
     *            <code>null</code>)
     */
    public CabExtractor(File cabFile, CabFileFilter filter) throws CabException, IOException {
        this(cabFile, filter, new DefaultCabFileSaver(null, cabFile));
    }

    /**
     * To extract some files from CAB file and save to defined extract
     * directory.
     * 
     * @param cabFile
     *            CAB file
     * @param filter
     *            which files to extract (extract all files if
     *            <code>null</code>)
     * @param extractDirectory
     *            directory to extract files (no sub-directory will be created)
     */
    public CabExtractor(File cabFile, CabFileFilter filter, File extractDirectory)
            throws CabException, IOException {
        this(cabFile, filter, new DefaultCabFileSaver(extractDirectory));
    }

    /**
     * To extract some files from CAB file and handle (save) them using
     * {@link CabFileSaver}.
     * 
     * @param cabFile
     *            CAB file
     * @param filter
     *            which files to extract (extract all files if
     *            <code>null</code>)
     * @param saver
     *            defines how to save the extracted
     *            {@link ByteArrayOutputStream} corresponding to
     *            {@link CabFileEntry}
     */
    public CabExtractor(File cabFile, CabFileFilter filter, CabFileSaver saver)
            throws CabException, IOException {
        this.inputStream = new BufferedInputStream(new FileInputStream(cabFile));
        this.parser = new CabParser(this.inputStream, new FilteredCabStreamSaver(saver, filter));
    }

    /**
     * To extract some files from CAB {@link InputStream} and handle (save) them
     * using {@link CabFileSaver}.
     * 
     * @param inputStream
     *            representing CAB file
     * @param filter
     *            which files to extract (extract all files if
     *            <code>null</code>)
     * @param saver
     *            defines how to save the extracted
     *            {@link ByteArrayOutputStream} corresponding to
     *            {@link CabFileEntry}
     */
    public CabExtractor(InputStream inputStream, CabFileFilter filter, CabFileSaver saver)
            throws CabException, IOException {
        this(inputStream, new FilteredCabStreamSaver(saver, filter));
    }

    /**
     * To extract files from CAB {@link InputStream} and handle (save) them
     * using {@link CabStreamSaver}.
     * 
     * @param inputStream
     *            representing CAB file
     * @param streamSaver
     *            defines which files to save and how to save them
     */
    public CabExtractor(InputStream inputStream, CabStreamSaver streamSaver)
            throws CabException, IOException {
        this.inputStream = inputStream;
        this.parser = new CabParser(this.inputStream, streamSaver);
    }

    /**
     * Extract files from CAB stream using the strategy defined by constructor
     * parameter(s). Only first invocation of this method extracts files (mostly
     * because we are extracting from potentially non-rewindable CAB stream, but
     * also because extracting with predefined parameters is an idempotent
     * operation).
     * 
     * @return <code>true</code> if files were extracted, <code>false</code>
     *         otherwise (if executed second time on the same
     *         {@link CabExtractor) object)
     */
    public boolean extract() throws CabException, IOException {
        boolean result = done.compareAndSet(false, true);
        if (result) {
            try {
                parser.extractStream();
            } finally {
                inputStream.close();
            }
        }
        return result;
    }

    public static String getVersion() {
        return CabParser.getVersion();
    }

    public Enumeration<Object> entries() {
        return parser.entries();
    }

    public Enumeration<Object> entries(boolean b) {
        return parser.entries(b);
    }

    public CabHeader getHeader() {
        return parser.header;
    }

    public CabFolderEntry[] getFolders() {
        return parser.folders;
    }

    public CabFileEntry[] getFiles() {
        return parser.files;
    }

}
