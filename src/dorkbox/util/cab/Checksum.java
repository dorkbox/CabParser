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
package dorkbox.util.cab;

public final class Checksum {
    @SuppressWarnings("fallthrough")
    public static int calculate(byte[] bytes, int currentBlock, int seed) {
        int c1 = (byte) (seed & 0xFF);
        int c2 = (byte) (seed >>> 8 & 0xFF);
        int c3 = (byte) (seed >>> 16 & 0xFF);
        int c4 = (byte) (seed >>> 24 & 0xFF);

        int j = 0;
        int sizeOfBlock = currentBlock >>> 2;
        while (sizeOfBlock-- > 0) {
            c1 = (byte) (c1 ^ bytes[j++]);
            c2 = (byte) (c2 ^ bytes[j++]);
            c3 = (byte) (c3 ^ bytes[j++]);
            c4 = (byte) (c4 ^ bytes[j++]);
        }

        switch (currentBlock & 0x3) {
            case 3 :
                c3 = (byte) (c3 ^ bytes[j++]);
            case 2 :
                c2 = (byte) (c2 ^ bytes[j++]);
            case 1 :
                c1 = (byte) (c1 ^ bytes[j++]);
        }
        return c1 & 0xFF | (c2 & 0xFF) << 8 | (c3 & 0xFF) << 16 | (c4 & 0xFF) << 24;
    }
}
