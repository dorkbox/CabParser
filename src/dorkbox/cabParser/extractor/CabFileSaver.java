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

import dorkbox.cabParser.structure.CabFileEntry;

/**
 * To define how to handle (save) the file content, corresponding to each
 * individual file extracted from CAB.
 */
public interface CabFileSaver {

    /**
     * Save the extracted file content.
     * 
     * @param fileContent
     *            extracted file content
     * @param cabFile
     *            {@link CabFileEntry} defining file to be be saved
     */
    void save(ByteArrayOutputStream fileContent, CabFileEntry cabFile);

    /**
     * @return amount successfully saved files
     */
    int getSucceeded();

    /**
     * @return amount of failed save attempts
     */
    int getFailed();
}
