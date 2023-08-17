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
package dorkbox.cabParser.structure

import dorkbox.cabParser.CabParser
import java.util.*

class CabEnumerator(private val cabParser: CabParser, private val b: Boolean) : Enumeration<Any> {
    private var fileCount = 0
    private var folderCount = 0
    private var folderIndex: Int

    init {
        folderIndex = -2
    }

    override fun nextElement(): Any {
        if (!b) {
            if (fileCount < cabParser.header.cFiles) {
                return cabParser.files[fileCount++]
            }
            throw NoSuchElementException()
        }
        if (cabParser.files[fileCount].iFolder != folderIndex) {
            folderIndex = cabParser.files[fileCount].iFolder
            if (folderCount < cabParser.folders.size) {
                return cabParser.folders[folderCount++]
            }
        }
        if (fileCount < cabParser.header.cFiles) {
            return cabParser.files[fileCount++]
        }
        throw NoSuchElementException()
    }

    override fun hasMoreElements(): Boolean {
        return fileCount < cabParser.header.cFiles
    }
}
