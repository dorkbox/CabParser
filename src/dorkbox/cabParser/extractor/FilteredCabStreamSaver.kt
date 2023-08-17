/*
 * Copyright 2023 dorkbox, llc
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
package dorkbox.cabParser.extractor

import dorkbox.cabParser.CabStreamSaver
import dorkbox.cabParser.structure.CabFileEntry
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream

/**
 * Implementation of [CabStreamSaver] that filters files to extract.
 * [CabFileFilter] and saves them using [CabFileSaver].
 */
class FilteredCabStreamSaver(

    /**
     * defines how to save the [ByteArrayOutputStream] corresponding to [CabFileEntry]
     */
    val saver: CabFileSaver = DefaultCabFileSaver(null),

    /**
     * which files to extract (extract all files if `null`)
     */
    val filter: CabFileFilter = CabFilePatternFilter(".+")) : CabStreamSaver {

    /**
     * To save some files to defined extract directory.
     *
     * @param extractDirectory directory to extract files (no sub-directory will be created)
     * @param filter which files to extract (extract all files if `null`)
     */
    constructor(extractDirectory: File?, filter: CabFileFilter) : this(DefaultCabFileSaver(extractDirectory), filter)

    override fun closeOutputStream(outputStream: OutputStream, entry: CabFileEntry) {
        saver.save(outputStream as ByteArrayOutputStream, entry)
    }

    override fun openOutputStream(entry: CabFileEntry): OutputStream? {
        return if (filter.test(entry)) {
            ByteArrayOutputStream(entry.size.toInt())
        }
        else {
            null
        }
    }

    override fun saveReservedAreaData(data: ByteArray?, dataLength: Int): Boolean {
        return false
    }

    /**
     * @return amount successfully saved files
     */
    val succeeded: Int
        get() = saver.succeeded


    /**
     * @return amount of failed save attempts
     */
    val failed: Int
        get() = saver.failed
}
