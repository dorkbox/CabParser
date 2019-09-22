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

import dorkbox.cabParser.structure.CabFileEntry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * provide default CabStreamSaver
 */
public class DefaultCabStreamSaver implements CabStreamSaver{
    private File extractPath;

    public DefaultCabStreamSaver(File extractPath) throws CabException {
        this.extractPath = extractPath;
        if(extractPath!=null){
            if(!extractPath.exists()){
                extractPath.mkdirs();
            }else if(!extractPath.isDirectory()){
                throw new CabException("extractPath is not directory");
            }
        }
    }

    @Override
    public OutputStream openOutputStream(CabFileEntry entry) {
        return new ByteArrayOutputStream((int) entry.getSize());
    }

    @Override
    public void closeOutputStream(OutputStream outputStream, CabFileEntry entry) {
        if (outputStream != null) {
            try {
                ByteArrayOutputStream bos = (ByteArrayOutputStream)outputStream;
                File cabEntityFile = new File(extractPath, entry.getName().replace("\\", File.separator));
                cabEntityFile.getParentFile().mkdirs();
                FileOutputStream fileOutputStream = new FileOutputStream(cabEntityFile);
		try {
		    bos.writeTo(fileOutputStream);
                } finally {
                    fileOutputStream.close();
                }
            } catch (FileNotFoundException e) {
            } catch (IOException e) {
            } finally {
                try {
                    outputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    @Override
    public boolean saveReservedAreaData(byte[] data, int dataLength) {
        return false;
    }
}
