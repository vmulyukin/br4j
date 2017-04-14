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
package com.aplana.dbmi.storage.content;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Locale;

import com.aplana.dbmi.storage.content.exceptions.ContentException;

/**
 * Interface to write specific content.
 * 
 * @author <a href="mailto:ogalkin@aplana.com">Oleg Galkin</a>
 * @author <a href="mailto:rabdullin@aplana.com">Ruslan Abdullin</a>
 * 
 * @see ContentWriter
 */
public interface ContentWriter extends ContentAccessor {

	/**
	 * Sets the content type that must be used for accessing the content.
	 * 
	 * @param contentType  the content mimetype
	 */
	public void setContentType(String contentType);


	/**
	 * Sets the <code>Locale</code> for this accessor.
	 * 
	 * @param locale  a java-recognised locale
	 */
	public void setLocale(Locale locale);


	/**
	 * Gets a stream to write to the underlying content. The user is responsible for closing
	 * the stream.
	 * 
	 * @return an output stream onto the underlying content
	 * @throws ContentException if an IO error occurs
	 */
	public OutputStream getContentOutputStream() throws ContentException;


	/**
	 * Puts content from a byte array.
	 *
	 * @param  bytes  the byte array acting as the source of the content
	 * @throws ContentException if an IO error occurs
	 */
	public void putContent(ByteBuffer bytes) throws ContentException;


	/**
	 * Copies content from a reader.
	 * 
	 * @param src  the {@link ContentReader} acting as the source of the content.
	 * 
	 * @param destOffset the first zero-based writing position,
	 * (>=0) from the begin (1 means "start from the second byte"),
	 * (<0) offset from the end of the stream ( (-1) means "start from the last byte").
	 *  
	 * @param  maxWriteLength  the maximum number of bytes to save from reader,
	 *  (-1) = get all bytes from reader.
	 * 
	 * @throws ContentException if an IO error occurs
	 */
	public void putContent(InputStream src, int destOffset, int maxWriteLength) throws ContentException;

	/**
	 * ������� ����/������.
	 * @return true, ���� ���� ������� ��� ������ � false ����� (��������, �� �����������).
	 * @throws ContentException
	 */
	public boolean delete() throws ContentException;

	/**
	 * Copy the content to another position;
	 * (!) all URL are LOCAL to the storage point.
	 * @param destUrl: the new position, null means create new copy of the current writer content.
	 * @return the destination posision :  
	 * 		if destUrl is null - the generated one,
	 * 		if destUrl is not null - destUrl return as result.
	 * @throws ContentException
	 */
	public URL copy(URL destUrl) throws ContentException;
}
