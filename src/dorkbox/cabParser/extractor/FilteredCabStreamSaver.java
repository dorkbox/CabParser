/*
 * Copyright 2019 dorkbox, llc
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
package dorkbox.cabParser.extractor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;

import dorkbox.cabParser.CabStreamSaver;
import dorkbox.cabParser.structure.CabFileEntry;

/**
 * Implementation of {@link CabStreamSaver} that filters files to extract.
 * {@link CabFileFilter} and saves them using {@link CabFileSaver}.
 */
public class FilteredCabStreamSaver implements CabStreamSaver {

    private final CabFileSaver saver;
    private final CabFileFilter filter;

    /**
     * To save all files to defined extract directory.
     * 
     * @param extractDirectory
     *            directory to extract files (no sub-directory will be created)
     */
    public FilteredCabStreamSaver(File extractDirectory) {
        this(extractDirectory, null);
    }

    /**
     * To handle (save) all files using passed {@link CabFileSaver}.
     * 
     * @param saver
     *            defines how to save the {@link ByteArrayOutputStream}
     *            corresponding to {@link CabFileEntry}
     */
    public FilteredCabStreamSaver(CabFileSaver saver) {
        this(saver, null);
    }

    /**
     * To save some files to defined extract directory.
     * 
     * @param extractDirectory
     *            directory to extract files (no sub-directory will be created)
     * @param filter
     *            which files to extract (extract all files if
     *            <code>null</code>)
     */
    public FilteredCabStreamSaver(File extractDirectory, CabFileFilter filter) {
        this(new DefaultCabFileSaver(extractDirectory), filter);
    }

    /**
     * To handle (save) some files using passed {@link CabFileSaver}.
     * 
     * @param saver
     *            defines how to save the {@link ByteArrayOutputStream}
     *            corresponding to {@link CabFileEntry}
     * @param filter
     *            which files to extract (extract all files if
     *            <code>null</code>)
     */
    public FilteredCabStreamSaver(CabFileSaver saver, CabFileFilter filter) {
        this.saver = null == saver ? new DefaultCabFileSaver(null) : saver;
        this.filter = null == filter ? new CabFilePatternFilter(".+") : filter;
    }

    @Override
    public void closeOutputStream(OutputStream outputStream, CabFileEntry cabFile) {
        saver.save((ByteArrayOutputStream) outputStream, cabFile);
    }

    @Override
    public OutputStream openOutputStream(CabFileEntry cabFile) {
        if (filter.test(cabFile)) {
            return new ByteArrayOutputStream((int) cabFile.getSize());
        } else {
            return null;
        }
    }

    @Override
    public boolean saveReservedAreaData(byte[] data, int dataLength) {
        return false;
    }

    /**
     * @return amount successfully saved files
     */
    public int getSucceeded() {
        return saver.getSucceeded();
    }

    /**
     * @return amount of failed save attempts
     */
    public int getFailed() {
        return saver.getFailed();
    }

}
