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
import java.io.OutputStream;

import com.aplana.dbmi.action.UploadFile;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;

/**
 * {@link OutputStream} implementation used in {@link UploadFile} action
 * to upload body of material file into database.
 * <br>
 * Material file is uploaded by parts of fixed size (currently 1MB). Each parts is
 * stored in temporary table in database. This is performed by executing
 * {@link UploadFilePart} action.
 * <br>
 * These parts later could be combined into single BLOB and stored into CARD table.
 */
public class UploadFileStream extends OutputStream
{
	private static final int PART_SIZE = 1024 * 1024; // 1mb
	private UploadFile upload;
	private ActionPerformer service;
	private byte[] data = new byte[PART_SIZE];
	private int localPos = 0;
	private int globalPos = 0;

	private String url;

	public UploadFileStream(UploadFile upload, ActionPerformer service) {
		this.upload = upload;
		this.service = service;
	}

	/**
     * Writes the specified byte to this output stream. The general
     * contract for <code>write</code> is that one byte is written
     * to the output stream. The byte to be written is the eight
     * low-order bits of the argument <code>b</code>. The 24
     * high-order bits of <code>b</code> are ignored.
     * <p>
     * @param      b   the <code>byte</code>.
     * @exception  IOException  if an error occurs
     * (in case of database error it would be an DatabaseIOException).
	 * @see OutputStream#write(int)
	 */
	@Override
	public void write(int b) throws IOException {
		data[localPos++] = (byte) b;
		if (localPos == PART_SIZE)
			flush();
	}

    /**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to this output stream.
     * The general contract for <code>write(b, off, len)</code> is that
     * some of the bytes in the array <code>b</code> are written to the
     * output stream in order; element <code>b[off]</code> is the first
     * byte written and <code>b[off+len-1]</code> is the last byte written
     * by this operation.
     * @param      b     the data.
     * @param      off   the start offset in the data.
     * @param      len   the number of bytes to write.
     * @exception  IOException  if an I/O error occurs
     * (in case of database error it would be an DatabaseIOException).
     */
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		while (localPos + len >= PART_SIZE) {
			final int wrLen = PART_SIZE - localPos;
			System.arraycopy(b, off, data, localPos, wrLen);
			off += wrLen;
			len -= wrLen;
			localPos = PART_SIZE;
			flush();
		}
		System.arraycopy(b, off, data, localPos, len);
		localPos += len;
	}

    /**
     * Writes <code>b.length</code> bytes from the specified byte array
     * to this output stream. The general contract for <code>write(b)</code>
     * is that it should have exactly the same effect as the call
     * <code>write(b, 0, b.length)</code>.
     * @param      b   the data.
     * @exception  IOException  if an I/O error occurs
     * (in case of database error it would be an DatabaseIOException).
     * @see        java.io.OutputStream#write(byte[])
     */
	@Override
	public void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}


	public String getUrl() {
		return url;
	}

	public void setUrl(String value) {
		this.url = value;
	}

	/**
	 * Flushes this output stream.
	 * In particular it uploads current data buffer into database by executing
	 * {@link UploadFilePart} action.
	 * @throws IOException if errors occured (in case of database error it would be an DatabaseIOException)
	 */
	@Override
	public void flush() throws IOException {
		final UploadFilePart part = new UploadFilePart();

		part.setCardId(upload.getCardId());
		part.setData(data, localPos);
		part.setOffset(globalPos);
		part.setUrl(this.getUrl());
		try {
			service.doAction(part);

			// ��������� ���������������� �����
			if (this.getUrl() == null)
				this.setUrl( part.getUrl());

		} catch (DataException e) {
			throw new DatabaseIOException(e);
		} catch (ServiceException e) {
			throw new IOException("Service error while reading data: " + e.getMessage());
		}
		globalPos += localPos;
		localPos = 0;
	}
}
