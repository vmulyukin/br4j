/**
 *   Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to you under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.aplana.dbmi.storage.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.sun.star.io.BufferSizeExceededException;
import com.sun.star.io.NotConnectedException;
import com.sun.star.io.XOutputStream;

public class OOoOutputStream extends FileOutputStream implements XOutputStream {

	public OOoOutputStream(File file) throws FileNotFoundException {
		super(file);
	}

    public OOoOutputStream(File file, boolean flAppend) throws FileNotFoundException {
		super(file, flAppend);
	}

    //
    // Implement XOutputStream
    //
	public void writeBytes(byte[] values) throws NotConnectedException, BufferSizeExceededException, com.sun.star.io.IOException {
        try {
            this.write(values);
        }
        catch (java.io.IOException e) {
            throw(new com.sun.star.io.IOException(e.getMessage()));
        }
    }

    public void closeOutput() throws NotConnectedException, BufferSizeExceededException, com.sun.star.io.IOException {
        try {
            super.flush();
            super.close();
        }
        catch (java.io.IOException e) {
            throw(new com.sun.star.io.IOException(e.getMessage()));
        }
    }

    @Override
    public void flush() {
        try {
            super.flush();
        }
        catch (java.io.IOException e) {
        }
    }
}