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
package com.aplana.dbmi.action.file;

import java.io.IOException;
import java.io.InputStream;

import com.aplana.dbmi.action.Material;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

/**
 * {@link InputStream} implementation used by {@link com.aplana.dbmi.action.DownloadFile}
 * action to fetch card material file from database.<br>
 * Material file is downloaded by parts of fixed size (currently 1MB), every part
 * is fetched by execution of {@link DownloadFilePart} action.
 */
public class DownloadFileStream extends InputStream
{
	private static final int PART_SIZE = 1024 * 1024 * 10;
	private Material material;
	private ActionPerformer service;
	private byte[] data = new byte[0];
	private int localPos = 0;
	private int globalPos = 0;

	/**
	 * Creates new DownloadFileStream instance
	 * @param material {@link Material} object containing information about
	 * location of required file in database
	 * @param service {@link DataServiceBean} instance initialized with information
	 * about user who is trying to download file. This bean will be used to execute
	 * {@link DownloadFilePart} actions during file fetching.
	 */
	public DownloadFileStream(Material material, ActionPerformer service) {
		this.material = material;
		this.service = service;
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
	@Override
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
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int read = 0;
		if (localPos == data.length)
			readPart();
		if (data.length == 0)
			return -1;
		int rdLen;
		while ( (rdLen = data.length - localPos) < len) {
			System.arraycopy(data, localPos, b, off, rdLen);
			read += rdLen;
			off += rdLen;
			len -= rdLen;
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

    /**
     * Reads some number of bytes from the input stream and stores them into
     * the buffer array <code>b</code>. The number of bytes actually read is
     * returned as an integer.  This method blocks until input data is
     * available, end of file is detected, or an exception is thrown.
     * @param      b   the buffer into which the data is read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> is there is no more data because the end of
     *             the stream has been reached.
     * @exception  IOException  if an I/O error occurs.
     * @exception  NullPointerException  if <code>b</code> is <code>null</code>.
	 * @see #read(byte[], int, int)
	 */
	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	private void readPart() throws IOException {
		localPos = 0;
		if (globalPos == material.getLength()) {
			data = new byte[0];
			return;
		}
		DownloadFilePart part = new DownloadFilePart();
		part.setCardId(material.getCardId());
		part.setOffset(globalPos);
		part.setLength(PART_SIZE);
		part.setVersionId(material.getVersionId());
		part.setUrl(material.getUrl());
		try {
			data = service.doAction(part);
		} catch (DataException e) {
			e.printStackTrace();
			throw new DatabaseIOException(e);
		} catch (ServiceException e) {
			e.printStackTrace();
			throw new IOException("Error reading file from data service: " + e.getMessage());
		}
		globalPos += data.length;
	}
	
	@Override
	public void reset() {
		data = new byte[0];
		globalPos = 0;
		localPos = 0;
	}
}
