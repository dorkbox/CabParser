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

import java.util.HashSet;
import java.util.Set;

import dorkbox.cabParser.structure.CabFileEntry;

/**
 * Selects files to extract (case-insensitive). Note that the file names in CAB
 * file may contain paths delimited with backslash character ('\\').
 */
public class CabFileSetFilter implements CabFileFilter {

    private final Set<String> fileNames;

    /**
     * Creates {@link CabFileSetFilter}.
     * 
     * @param fileName
     *            single file name to extract (case-insensitive)
     */
    public CabFileSetFilter(final String fileName) {
        this.fileNames = new HashSet<String>();
        this.fileNames.add(fileName.toLowerCase());
    }

    /**
     * Creates {@link CabFileSetFilter}.
     * 
     * @param fileNames
     *            file names to extract (case-insensitive)
     */
    public CabFileSetFilter(final Iterable<String> fileNames) {
        this.fileNames = new HashSet<String>();
        for (String i : fileNames) {
            this.fileNames.add(i.toLowerCase());
        }
    }

    @Override
    public boolean test(CabFileEntry cabFile) {
        String fileName = cabFile.getName().toLowerCase();
        return fileNames.contains(fileName);
    }

}
