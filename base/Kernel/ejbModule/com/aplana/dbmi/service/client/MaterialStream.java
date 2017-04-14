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
package com.aplana.dbmi.service.client;

import java.io.IOException;
import java.io.InputStream;

/**
 * InputStream implementation used by {@link MaterialUtil}
 * to work with material files.<br>
 * Files are read form database by parts of fixed size (currently 1MB) 
 * which allows to reduce memory consumption while working with a large files. <br>
 * All logic regarding reading part of file from file storage is carried out 
 * in separate interface {@link MaterialPartLoader}. Implementation of {@link MaterialPartLoader}
 * to be used by MaterialStream instance must be supplied in constructor call. 
 * @see MaterialPartLoader
 * @see MaterialUtil
 */
public class MaterialStream extends InputStream
{
	private static final int PART_SIZE = 1024 * 1024;
	private byte[] data = new byte[0];
	private int localPos = 0;
	private int globalPos = 0;
	private int length;
	private MaterialPartLoader loader;
	
	/**
	 * Creates new MaterialStream instance
	 * @param length length of material file
	 * @param loader {@link MaterialPartLoader} implementation to be used for
	 * reading parts of file from file storage.
	 */
	public MaterialStream(int length, MaterialPartLoader loader) {
		this.length = length;
		this.loader = loader;
	}

	/**
     * Reads the next byte of data from the input stream. The value byte is
     * returned as an <code>int</code> in the range <code>0</code> to
     * <code>255</code>. If no byte is available because the end of the stream
     * has been reached, the value <code>-1</code> is returned. This method
     * blocks until input data is available, the end of the stream is detected,
     * or an exception is thrown.
     *
     * @return     the next byte of data, or <code>-1</code> if the end of the
     *             stream is reached.
     * @exception  IOException  if an I/O error occurs.
	 */	
	public int read() throws IOException {
		if (localPos == data.length)
			readPart();
		if (data.length == 0)
			return -1;
		return data[localPos++];
	}

    /**
     * Reads some number of bytes from the input stream and stores them into
     * the buffer array <code>b</code>. The number of bytes actually read is
     * returned as an integer.
     * @param      b   the buffer into which the data is read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> is there is no more data because the end of
     *             the stream has been reached.
     * @exception  IOException  if an I/O error occurs.
     * @exception  NullPointerException  if <code>b</code> is <code>null</code>.
     * @see        java.io.InputStream#read(byte[], int, int)
     */	
	public int read(byte[] b, int off, int len) throws IOException {
		int read = 0;
		if (localPos == data.length)
			readPart();
		if (data.length == 0)
			return -1;
		while (data.length - localPos < len) {
			System.arraycopy(data, localPos, b, off, data.length - localPos);
			read += data.length - localPos;
			off += data.length - localPos;
			len -= data.length - localPos;
			try {
				readPart();
			} catch (IOException e) {
				return read;
			}
			if (data.length == 0)
				return read;
		}
		System.arraycopy(data, localPos, b, off, len);
		read += len;
		localPos += len;
		return read;
	}

	private void readPart() throws IOException {
		localPos = 0;
		if (globalPos == length) {
			data = new byte[0];
			return;
		}
		data = loader.readPart(globalPos, PART_SIZE);
		globalPos += data.length;
	}
	
	/**
	 * Returns size of material file represented by this MaterialStream instance
	 * @return size of material file
	 */
	public int getLength() {
		return length;
	}
}
