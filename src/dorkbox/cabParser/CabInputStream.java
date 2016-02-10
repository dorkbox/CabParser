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
package dorkbox.cabParser;

import java.io.IOException;
import java.io.InputStream;

final class CabInputStream extends InputStream {
    private InputStream inputStream;

    private long        position;
    private long        a;

    private boolean     markSupported;

    @Override
    public void close() throws IOException {
        this.inputStream.close();
    }

    @Override
    public synchronized void reset() throws IOException {
        if (this.markSupported) {
            this.inputStream.reset();
            this.position = this.a;
            return;
        }
        throw new IOException();
    }

    CabInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
        this.position = 0L;
        this.markSupported = this.inputStream.markSupported();
    }

    @Override
    public int read() throws IOException {
        int i = this.inputStream.read();
        if (i >= 0) {
            this.position += 1L;
        }
        return i;
    }

    @Override
    public int read(byte[] bytes) throws IOException {
        int i = this.inputStream.read(bytes);
        if (i > 0) {
            this.position += i;
        }
        return i;
    }

    @Override
    public int read(byte[] bytes, int offset, int length) throws IOException {
        int i = this.inputStream.read(bytes, offset, length);
        if (i > 0) {
            this.position += i;
        }

        return i;
    }

    @Override
    public int available() throws IOException {
        throw new IOException();
    }

    @Override
    public synchronized void mark(int paramInt) {
        if (this.markSupported) {
            this.a = this.position;
            this.inputStream.mark(paramInt);
        }
    }

    public long getCurrentPosition() {
        return this.position;
    }

    @Override
    public boolean markSupported() {
        return this.markSupported;
    }

    public void seek(long location) throws IOException {
        if (location < this.position) {
            throw new IOException("Cannot seek backwards");
        }

        if (location > this.position) {
            skip(location - this.position);
        }
    }

    @Override
    public long skip(long ammount) throws IOException {
        long l = this.inputStream.skip(ammount);
        this.position += (int) l;
        return l;
    }
}
