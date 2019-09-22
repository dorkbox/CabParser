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

import java.util.regex.Pattern;

import dorkbox.cabParser.structure.CabFileEntry;

/**
 * Selects files to extract using case-insensitive regular expression pattern.
 * Note that the file names (passed to regular expression) may contain paths
 * delimited with backslash character ('\\').
 */
public class CabFilePatternFilter implements CabFileFilter {

    private final Pattern pattern;

    /**
     * Creates {@link CabFilePatternFilter}.
     * 
     * @param regex
     *            regular expression to match file name to extract (will be
     *            compiled as case-insensitive)
     */
    public CabFilePatternFilter(String regex) {
        this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    /**
     * Creates {@link CabFilePatternFilter}.
     * 
     * @param pattern
     *            regular expression pattern to match file name to extract (will
     *            be turned to case-insensitive)
     */
    public CabFilePatternFilter(Pattern pattern) {
        this.pattern = Pattern.compile(pattern.pattern(), Pattern.CASE_INSENSITIVE);
    }

    @Override
    public boolean test(CabFileEntry cabFile) {
        return pattern.matcher(cabFile.getName()).matches();
    }

}
