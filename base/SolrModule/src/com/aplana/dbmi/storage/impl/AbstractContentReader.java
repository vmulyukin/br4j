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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;

import com.aplana.dbmi.storage.ContentStorage;
import com.aplana.dbmi.storage.content.ContentReader;
import com.aplana.dbmi.storage.content.exceptions.ContentException;

/**
 * @author <a href="mailto:ogalkin@aplana.com">Oleg Galkin</a>
 * @author <a href="mailto:rabdullin@aplana.com">Ruslan Abdullin</a>
*/
public abstract class AbstractContentReader 
	extends AbstractContentAccessor 
	implements ContentReader 
{


	protected AbstractContentReader() {
		super();
	}

	protected AbstractContentReader( ContentStorage ownerStorage, URL contentUrl, String contentType,
			Locale locale, long lastModified) {
		super(ownerStorage, contentUrl, contentType, locale, lastModified);
	}

	protected AbstractContentReader(ContentStorage ownerStorage, URL contentUrl, String contentType,
			long lastModified) {
		super(ownerStorage, contentUrl, contentType, lastModified);
	}

	protected AbstractContentReader(ContentStorage ownerStorage, URL contentUrl, String contentType) {
		super(ownerStorage, contentUrl, contentType);
	}

	protected AbstractContentReader(ContentStorage ownerStorage, URL contentUrl) {
		super( ownerStorage, contentUrl);
	}

	// @Override abstract public long getSize() { return -1; }
	// @Override public boolean isExists() { return false; }


	abstract public InputStream getContentInputStream() throws ContentException;


	public final ByteBuffer getContentBytes() throws ContentException 
	{
		final InputStream is = getContentInputStream();
		try {
			final ByteBuffer result = ByteBuffer.allocate(is.available());
			// return IOUtils.toByteArray(is);
			is.read( result.array() );
			return result;
		} catch (IOException e) {
			throw new ContentException( MessageFormat.format( 
							"Failed to copy content \"{0}\"to byte array", this), 
							e);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}


	public InputStream getContentPart(int offset, int length)
		throws ContentException 
	{
		try {
			return new InputStreamByOffset( getContentInputStream(), offset, length);
		} catch (IOException ex) {
			throw new ContentException( MessageFormat.format("Fail to get reader for offset={0}, length={1}", 
						offset, length), ex);
		}
	}


	/**
	 * ����������� ��� ��������� �� ������ ������ ��������� ��� �����.
	 * @author RAbdullin
	 */
	public static class InputStreamByOffset 
		extends InputStream
	{
		private final InputStream sourceStream;

		/**
		 * �������� �������� ������������ ������ ������ sourceStream.
		 */
		private final long offset;

		/**
		 * ������������ ���-�� ����, ��������� ��� ������, ������� � offset.
		 */
		private final long length;

		/**
		 * ������� ��������� "�������" ������ destStream, ������������ offset. 
		 */
		private long position = 0;

		public InputStreamByOffset( InputStream srcStream, int srcOffset,
				int srcMaxLenth) throws IOException 
		{
			Validate.notEmpty( new Object[] { srcStream }, "Content input stream must not be null");

			this.sourceStream = srcStream;
			final int srcLen = this.sourceStream.available();

			// ��������� "������ �������� ������"
			if ( (srcMaxLenth < 0) || (srcMaxLenth > srcLen))
				srcMaxLenth = srcLen;

			this.offset = calcPositiveOffset(srcOffset, srcLen);
			this.length = srcMaxLenth - this.offset;
		}


		@Override
		public int available() throws IOException {
			return (int) (this.length - this.position);
		}

		@Override
		public void reset() throws IOException {
			seekStream( this.sourceStream, this.offset);
			this.position = 0;
		}


		@Override
		public void close() throws IOException {
			super.close();
			// this.sourceStream.close();
		}


		@Override
		public int read(byte b[], int off, int len) throws IOException {
			// return this.sourceStream.read( b, off, len);
			final long maxLenLeft = this.length - this.position; 
			if (len > maxLenLeft)
				len = (int) maxLenLeft;
			int result = this.sourceStream.read( b, off, len);
			this.position += result;
			return result;
		}


		@Override
		public int read() throws IOException {
			if (this.position >= this.length) 
				return (-1);
			++this.position;
			return this.sourceStream.read();
		}


		/**
		 * ��������� �������� ������������ ������ ������:
		 * @param srcOffset: 0-based �������� : >= 0 �� ������, <0: �� �����,
		 * @param maxsize: ������������ ������ "������" �������� �������� srcOffset.
		 * @return ������������� �������� [0..maxsize - 1].
		 */
		public static long calcPositiveOffset( long srcOffset, long maxsize)
		{
			if (srcOffset < 0 ) {
				// ������ � �����
				srcOffset += maxsize;
				if (srcOffset < 0) srcOffset = 0;
			} else if (srcOffset >= maxsize) { // ������� ������� ��������...
				srcOffset = maxsize - 1;
			}
			return srcOffset;
		}


		/**
		 * ���������������� ������. ���� ����������� ������������� reset, �� 
		 * ����� ��������� � �������� ��� ���������� reset � ��-�������� �������.
		 * @param stm
		 * @param seekOffset
		 * @throws IOException
		 */
		protected void seekStream(InputStream stm, long seekOffset) 
			throws IOException
		{
			if (stm instanceof FileInputStream) {
				final FileInputStream fileStream = (FileInputStream) stm;
				fileStream.getChannel().position(seekOffset);
			} else {
				stm.reset();
				stm.skip(seekOffset);
			}
		} 

	}
}
