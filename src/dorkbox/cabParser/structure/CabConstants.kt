/*
 * Copyright 2012 dorkbox, llc
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

interface CabConstants {
    companion object {
        const val CAB_BLOCK_SIZE = 32768
        const val CAB_BLOCK_SIZE_THRESH = 32767
        const val COMPRESSION_TYPE_NONE = 0
        const val COMPRESSION_TYPE_MSZIP = 1
        const val COMPRESSION_TYPE_QUANTUM = 2
        const val COMPRESSION_TYPE_LZX = 3
        const val RESERVED_CFHEADER = 1
        const val RESERVED_CFFOLDER = 2
        const val RESERVED_CFDATA = 3
        const val CAB_PROGRESS_INPUT = 1

        /**
         * FLAG_PREV_CABINET is set if this cabinet file is not the first in a set
         * of cabinet files. When this bit is set, the szCabinetPrev and szDiskPrev
         * fields are present in this CFHEADER.
         */
        const val FLAG_PREV_CABINET = 0x0001

        /**
         * FLAG_NEXT_CABINET is set if this cabinet file is not the last in a set of
         * cabinet files. When this bit is set, the szCabinetNext and szDiskNext
         * fields are present in this CFHEADER.
         */
        const val FLAG_NEXT_CABINET = 0x0002

        /**
         * FLAG_RESERVE_PRESENT is set if this cabinet file contains any reserved
         * fields. When this bit is set, the cbCFHeader, cbCFFolder, and cbCFData
         * fields are present in this CFHEADER.
         */
        const val FLAG_RESERVE_PRESENT = 0x0004
    }
}
