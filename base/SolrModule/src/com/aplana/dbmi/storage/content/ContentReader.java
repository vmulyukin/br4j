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
import java.io.Reader;
import java.nio.ByteBuffer;

import com.aplana.dbmi.storage.content.exceptions.ContentException;

/**
 * Interface to read specific content.
 * 
 * @author <a href="mailto:ogalkin@aplana.com">Oleg Galkin</a>
 * @author <a href="mailto:rabdullin@aplana.com">Ruslan Abdullin</a>
 * 
 * @see ContentWriter
 */
public interface ContentReader extends ContentAccessor {


	/**
	 * Gets a stream to read the underlying content. The user is responsible for closing the stream.
	 * 
	 * @return an input stream onto the underlying content
	 * @throws ContentException if an IO error occurs 
	 */
	public InputStream getContentInputStream() throws ContentException;


	/**
	 * Gets content from the repository to a byte array.
	 * <p/>
	 * <b>WARNING:</b> This should only be used when the size of the content is known in advance,
	 * use {@link #getContentBytes(int, int)} otherwise
	 * 
	 * @return a byte array containing the content
	 * @throws ContentException if an IO error occurs
	 * 
	 * @see #getContentBytes(int)
	 * @see #getContentInputStream()
	 */
	public ByteBuffer getContentBytes() throws ContentException;


	/**
	 * Gets content from the repository to a byte array, but limiting the array size to a 
	 * given length.
	 * 
	 * @param  offset  the zero-based start position to retrieve: 
	 * (>=0) from the begin (1 means "start from the second byte"),
	 * (<0) offset from the end of the stream ( (-1) means "start from the last byte").
	 * 
	 * @param  length  the maximum number of bytes to retrieve, 
	 * (-1) = get everything from offset till the end.
	 * 
	 * @return a byte array containing the truncated or full content.
	 * 
	 * @throws ContentException if an IO error occurs
	 * @throws IllegalArgumentException if the length is < 0 or > {@link Integer#MAX_VALUE}
	 * 
	 * @see #getContentBytes()
	 * @see #getContentInputStream()
	 */
	public InputStream getContentPart(int offset, int length) throws ContentException;

}
