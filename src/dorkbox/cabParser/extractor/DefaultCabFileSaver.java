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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import dorkbox.cabParser.structure.CabFileEntry;

/**
 * Saves extracted file content to a file.
 */
public class DefaultCabFileSaver implements CabFileSaver {

    private static final Pattern FILENAME_PATTERN = Pattern.compile("\\.(?=[^\\.]+$)");
    private final File extractDirectory;
    private AtomicInteger succeeded = new AtomicInteger(0);
    private AtomicInteger failed = new AtomicInteger(0);

    /**
     * Creates {@link CabFileSaver}.
     * 
     * @param baseDirectory
     *            base directory where a sub-directory will be created
     * @param cabFile
     *            to define a sub-directory for extracting files from CAB
     */
    public DefaultCabFileSaver(File baseDirectory, File cabFile) {
        this.extractDirectory = new File(null == baseDirectory ? new File(".") : baseDirectory,
                getFileNameBase(cabFile));
        if (!this.extractDirectory.exists())
            this.extractDirectory.mkdirs();
    }

    /**
     * Creates {@link CabFileSaver}.
     * 
     * @param extractDirectory
     *            target directory where the extracted files will be created
     */
    public DefaultCabFileSaver(File extractDirectory) {
        this.extractDirectory = (null == extractDirectory ? new File(".") : extractDirectory);
        if (!this.extractDirectory.exists())
            this.extractDirectory.mkdirs();
    }

    @Override
    public void save(ByteArrayOutputStream outputStream, CabFileEntry cabFile) {
        if (outputStream != null) {
            try {
                File file = new File(getExtractDirectory(), cabFile.getName().replace("\\", File.separator));
                file.getParentFile().mkdirs();
                FileOutputStream writer = new FileOutputStream(file);
                try {
                    outputStream.writeTo(writer);
                } finally {
                    writer.close();
                }
                succeeded.incrementAndGet();
            } catch (IOException e) {
                failed.incrementAndGet();
            } finally {
                try {
                    outputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    @Override
    public int getSucceeded() {
        return succeeded.get();
    }

    @Override
    public int getFailed() {
        return failed.get();
    }

    public File getExtractDirectory() {
        return extractDirectory;
    }

    public static String getFileNameBase(File file) {
        return FILENAME_PATTERN.split(file.getName())[0];
    }
}
