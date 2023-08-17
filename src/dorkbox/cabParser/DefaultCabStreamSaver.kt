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

import dorkbox.cabParser.structure.CabFileEntry
import java.io.*

/**
 * provide default CabStreamSaver
 */
class DefaultCabStreamSaver(private val extractPath: File?) : CabStreamSaver {
    init {
        if (extractPath != null) {
            if (!extractPath.exists()) {
                extractPath.mkdirs()
            }
            else if (!extractPath.isDirectory()) {
                throw CabException("extractPath is not directory")
            }
        }
    }

    override fun openOutputStream(entry: CabFileEntry): OutputStream? {
        return ByteArrayOutputStream(entry.size.toInt())
    }

    override fun closeOutputStream(outputStream: OutputStream, entry: CabFileEntry) {
        try {
            val bos = outputStream as ByteArrayOutputStream
            val cabEntityFile = File(extractPath, entry.name.replace("\\", File.separator))
            cabEntityFile.getParentFile().mkdirs()
            val fileOutputStream = FileOutputStream(cabEntityFile)
            try {
                bos.writeTo(fileOutputStream)
            }
            finally {
                fileOutputStream.close()
            }
        }
        catch (e: FileNotFoundException) {
        }
        catch (e: IOException) {
        }
        finally {
            try {
                outputStream.close()
            }
            catch (e: IOException) {
            }
        }
    }

    override fun saveReservedAreaData(data: ByteArray?, dataLength: Int): Boolean {
        return false
    }
}
