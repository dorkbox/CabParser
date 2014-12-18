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
package dorkbox.util.cab.decompress;

import dorkbox.util.cab.CabException;
import dorkbox.util.cab.structure.CabConstants;


public interface Decompressor extends CabConstants {
    public abstract void init(int windowBits) throws CabException;
    public abstract void decompress(byte[] inputBytes, byte[] outputBytes, int inputLength, int outputLength) throws CabException;
    public abstract int getMaxGrowth();
    public abstract void reset(int windowBits) throws CabException;
}
