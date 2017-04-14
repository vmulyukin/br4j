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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;

import com.aplana.dbmi.storage.ContentStorage;
import com.aplana.dbmi.storage.content.ContentWriter;
import com.aplana.dbmi.storage.content.exceptions.ContentException;

/**
 * Base implementation of {@link ContentWriter} interface.
 * 
 * @author <a href="mailto:ogalkin@aplana.com">Oleg Galkin</a>
 */
public abstract class AbstractContentWriter 
		extends AbstractContentAccessor 
		implements ContentWriter 
{
	protected AbstractContentWriter() {
		super();
	}

	protected AbstractContentWriter(ContentStorage ownerStorage, URL contentUrl, String contentType,
			Locale locale, long lastModified) {
		super(ownerStorage, contentUrl, contentType, locale, lastModified);
	}

	protected AbstractContentWriter(ContentStorage ownerStorage, URL contentUrl, String contentType,
			long lastModified) {
		super(ownerStorage, contentUrl, contentType, lastModified);
	}

	protected AbstractContentWriter(ContentStorage ownerStorage, URL contentUrl, String contentType) {
		super(ownerStorage, contentUrl, contentType);
	}

	/**
	 * Constructs a new content writer by the specific content URL.
	 *  
	 * @param contentUrl  the content URL the reader references
	 */
	protected AbstractContentWriter(ContentStorage ownerStorage, URL contentUrl) {
		super(ownerStorage, contentUrl);
	}


	public void setContentType(String value) {
		super.contentType = value;
	}


	public void setLocale(Locale value) {
		super.locale = value;
	}


	public abstract OutputStream getContentOutputStream() throws ContentException;
	public abstract URL copy(URL destUrl) throws ContentException;
	public abstract boolean delete() throws ContentException;


	public void putContent(ByteBuffer buf) throws ContentException
	{
		final OutputStream os = getContentOutputStream();
		try {
			IOUtils.write( buf.array(), os);
		} catch (IOException e) {
			throw new ContentException( MessageFormat.format( 
					"Failed to copy content from byte array info \"{0}\"", this), 
					e);
		} finally {
			IOUtils.closeQuietly(os);
		}
	}

	public void putContent(InputStream src, int destOffset, int maxWriteLength)
			throws ContentException 
	{
		if (src == null) return;
		try {
			final OutputStream osFull = this.getContentOutputStream();
			try {
				final OutputStream os = new OutputStreamByOffset( osFull, destOffset, maxWriteLength);
				try {
					IOUtils.copy(src, os);
				} catch (IOException e) {
					throw new ContentException( MessageFormat.format( 
							"Failed to copy content from byte array info \"{0}\"", this), 
							e);
				} finally {
					IOUtils.closeQuietly(os);
				}
			} finally {
				IOUtils.closeQuietly(osFull);
			}
		} catch (IOException ex) {
			throw new ContentException( MessageFormat.format("Fail to get writer for offset={0}, length={1}", 
						destOffset, maxWriteLength), ex);
		}
	}


	/**
	 * ����������� ��� ������ � ������ �����, ������� � ��������� ��������.
	 * ���� ����������� ������������� reset, �� ����� ��������� � �������� ��� 
	 * ���������� reset � ��-�������� �������.
	 * @author RAbdullin
	 */
	public static class OutputStreamByOffset 
		extends OutputStream
	{
		private final OutputStream destStream;

		/**
		 * �������� �������� ������������ ������ ������ destStream.
		 * ��������� ��� ���������� � ��� ���������� rewrite. 
		 */
		private final long offset;

		/**
		 * ������������ ���-�� ����, ��������� ��� ������, ������� � offset;
		 * (<0)=������������.
		 */
		private final long length;

		/**
		 * ������� ��������� "�������" ������ destStream, ������������ offset. 
		 */
		private long position = 0;

		/** 
		 * @param destOffset the first zero-based writing position,
		 * (>=0) from the begin (1 means "start from the second byte"),
		 * (<0) offset from the end of the stream ( (-1) means "start from the last byte").
		 *  
		 * @param  length  the maximum number of bytes to save from reader, (-1) = get all bytes from reader.
		 */
		public OutputStreamByOffset( OutputStream srcStream, int dstOffset,
				int writeMaxLenth) 
			throws IOException 
		{
			Validate.notEmpty( new Object[] { srcStream }, "Content output stream must not be null");
			if (dstOffset < 0)
				throw new IOException( MessageFormat.format( "Output content offset must be positive ({0})", 
					new Object[] { dstOffset }));

			this.destStream = srcStream;
			this.offset = dstOffset;
			this.length = writeMaxLenth;
			this.seekStream(this.destStream, this.offset);
		}


		@Override
		public void flush() throws IOException {
			this.destStream.flush();
		}


		@Override
		public void close() throws IOException {
			// this.destStream.close();
			// ����� ������ �������� flush!
			flush();
			super.close();
		}


		@Override
		public void write(byte b[], int off, int len) throws IOException 
		{
			if (this.length >= 0) {
				final long maxLenLeft = this.length - this.position; 
				if (len > maxLenLeft)
					// len = (int) maxLenLeft;
					throw new ContentException( MessageFormat.format(
								"Can not write {0} bytes into bounded content (still avail only {1})",
								len, maxLenLeft
								));
			}
			this.destStream.write( b, off, len);
			this.position += len;
		}


		@Override
		public void write(int b) throws IOException {
			if (this.length >= 0 && (this.position >= this.length) ) 
				throw new ContentException( "Can not write outside bounded content " + this);
			++this.position;
			this.destStream.write(b);
		}


		/**
		 * ���������������� ������. 
		 * ���� ����������� ������������� reset, �� ����� ��������� � �������� ��� 
		 * ���������� reset � ��-�������� �������.
		 * @param stm
		 * @param seekOffset
		 * @throws IOException
		 */
		protected void seekStream(OutputStream stm, long seekOffset) 
			throws IOException
		{
			if (stm instanceof FileOutputStream) {
				final FileOutputStream fileStream = (FileOutputStream) stm;
				fileStream.getChannel().position(seekOffset);
			} else 
				throw new ContentException( MessageFormat.format( 
						"Unsupported operation reset at content \"{0}\"", this)); 
		}


	}


//	/* (non-Javadoc)
//	 * @see com.aplana.dbmi.content.ContentWriter#putContent(com.aplana.dbmi.content.ContentReader)
//	 */
//	public final void putContent(ContentReader reader) throws ContentException {
//		InputStream is = reader.getContentInputStream();
//		try {
//			putContent(is);
//		} finally {
//			IOUtils.closeQuietly(is);
//		}
//	}
//
//	/* (non-Javadoc)
//	 * @see com.aplana.dbmi.content.ContentWriter#putContent(java.io.InputStream)
//	 */
//	public final void putContent(InputStream is) throws ContentException {
//		OutputStream os = getContentOutputStream();
//		try {
//			IOUtils.copy(is, os);
//		} catch (IOException e) {
//			throw new ContentException("Failed to copy content from input stream", e);
//		} finally {
//			IOUtils.closeQuietly(os);
//		}
//	}
//
//
//	/* (non-Javadoc)
//	 * @see com.aplana.dbmi.content.ContentWriter#putContent(java.io.File)
//	 */
//	public final void putContent(File file) throws ContentException {
//		InputStream is = null;
//		try {
//			is = new FileInputStream(file);
//			putContent(is);
//		} catch (IOException e) {
//			throw new ContentException("Failed to copy content from file " + file, e);
//		} finally {
//			IOUtils.closeQuietly(is);
//		}
//	}
//
//	/* (non-Javadoc)
//	 * @see com.aplana.dbmi.content.ContentWriter#putContent(java.lang.String)
//	 */
//	public final void putContent(String content) throws ContentException {
//		OutputStream os = getContentOutputStream();
//		try {
//			IOUtils.write(content, os, getEncoding());
//		} catch (IOException e) {
//			throw new ContentException("Failed to copy content from string", e);
//		} finally {
//			IOUtils.closeQuietly(os);
//		}
//	}
}
