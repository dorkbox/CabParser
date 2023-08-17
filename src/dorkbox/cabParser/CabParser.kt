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
package dorkbox.cabParser

import dorkbox.cabParser.decompress.CabDecompressor
import dorkbox.cabParser.structure.CabEnumerator
import dorkbox.cabParser.structure.CabFileEntry
import dorkbox.cabParser.structure.CabFolderEntry
import dorkbox.cabParser.structure.CabHeader
import dorkbox.updates.Updates.add
import java.io.*
import java.util.*

class CabParser {
    companion object {
        /**
         * Gets the version number.
         */
        const val version: String = "3.2"

        init {
            // Add this project to the updates system, which verifies this class + UUID + version information
            add(CabParser::class.java, "41f560ca51c04bfdbca21328e0cbf206", version)
        }
    }

    private var cabInputStream: CabInputStream
    private var streamSaver: CabStreamSaver
    private var outputStream: ByteArrayOutputStream? = null

    lateinit var header: CabHeader

    lateinit var folders: Array<CabFolderEntry>

    lateinit var files: Array<CabFileEntry>

    constructor(inputStream: InputStream, fileNameToExtract: String) {
        cabInputStream = CabInputStream(inputStream)
        streamSaver = object : CabStreamSaver {
            override fun saveReservedAreaData(data: ByteArray?, dataLength: Int): Boolean {
                return false
            }

            override fun openOutputStream(entry: CabFileEntry): OutputStream? {
                val name = entry.name
                return if (fileNameToExtract.equals(name, ignoreCase = true)) {
                    outputStream = ByteArrayOutputStream(entry.size.toInt())
                    outputStream!!
                }
                else {
                    null
                }
            }

            override fun closeOutputStream(outputStream: OutputStream, cabFile: CabFileEntry) {
                try {
                    outputStream.close()
                }
                catch (ignored: IOException) {
                }
            }
        }
        readData()
    }

    constructor(inputStream: InputStream, streamSaver: CabStreamSaver) {
        this.streamSaver = streamSaver
        cabInputStream = CabInputStream(inputStream)
        readData()
    }

    constructor(inputStream: InputStream, extractPath: File?) {
        streamSaver = DefaultCabStreamSaver(extractPath)
        cabInputStream = CabInputStream(inputStream)
        readData()
    }

    fun entries(): Enumeration<Any> {
        return CabEnumerator(this, false)
    }

    fun entries(b: Boolean): Enumeration<Any> {
        return CabEnumerator(this, b)
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(CabException::class, IOException::class)
    private fun readData() {
        header = CabHeader(streamSaver)
        header.read(cabInputStream)
        folders = arrayOfNulls<CabFolderEntry>(header.cFolders) as Array<CabFolderEntry>

        for (i in 0 until header.cFolders) {
            folders[i] = CabFolderEntry()
            folders[i].read(cabInputStream)
        }

        files = arrayOfNulls<CabFileEntry>(header.cFiles) as Array<CabFileEntry>
        cabInputStream.seek(header.coffFiles)
        for (i in 0 until header.cFiles) {
            files[i] = CabFileEntry()
            files[i].read(cabInputStream)
        }
    }

    @Throws(CabException::class, IOException::class)
    fun extractStream(): ByteArrayOutputStream? {
        var folderCount = -1
        var currentCount = 0
        var totalCount = 0
        var init = true
        val extractor = CabDecompressor(cabInputStream, header.cbCFData)
        for (fileIndex in 0 until header.cFiles) {
            val entry = files[fileIndex]
            if (entry.iFolder != folderCount) {
                if (folderCount + 1 >= header.cFolders) {
                    throw CorruptCabException()
                }

                folderCount++
                init = true
                currentCount = 0
                totalCount = 0
            }
            val localOutputStream = streamSaver.openOutputStream(entry)
            if (localOutputStream != null) {
                if (init) {
                    val cabFolderEntry = folders[folderCount]
                    cabInputStream.seek(cabFolderEntry.coffCabStart)
                    extractor.initialize(cabFolderEntry.compressionMethod)
                    init = false
                }
                if (currentCount != totalCount) {
                    extractor.read((totalCount - currentCount).toLong(), object : OutputStream() {
                        @Throws(IOException::class)
                        override fun write(i: Int) {
                            //do nothing
                        }

                        @Throws(IOException::class)
                        override fun write(b: ByteArray) {
                            //do nothing
                        }

                        @Throws(IOException::class)
                        override fun write(b: ByteArray, off: Int, len: Int) {
                            //do nothing
                        }

                        @Throws(IOException::class)
                        override fun flush() {
                            //do nothing
                        }
                    })
                    currentCount = totalCount
                }
                extractor.read(entry.cbFile, localOutputStream)
                streamSaver.closeOutputStream(localOutputStream, entry)
                currentCount = (currentCount + entry.cbFile).toInt()
            }
            totalCount = (totalCount + entry.cbFile).toInt()
        }
        return outputStream
    }
}
