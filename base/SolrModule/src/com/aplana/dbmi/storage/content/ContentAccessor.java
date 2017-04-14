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

import java.net.URL;
import java.util.Locale;

import com.aplana.dbmi.storage.ContentStorage;

/**
 * Interface for instances that provide read and write access to content.
 *
 * @author <a href="mailto:ogalkin@aplana.com">Oleg Galkin</a>
  * @author <a href="mailto:rabdullin@aplana.com">Ruslan Abdullin</a>
*/
public interface ContentAccessor {

	/**
	 * @return the storage point of the accessor.
	 */
	public ContentStorage getStorage();


	/**
	 * Retrieves the URL LOCAL to storagepoint that this accessor references.
	 * 
	 * @return the content URL, (!) local to the storage point.
	 */
	public URL getContentUrl();


	/**
	 * Gets the size of the content that this accessor references.
	 * 
	 * @return the content byte length, or <code>null</code> if the content does not exist.
	 */
	public long getSize();


	/**
	 * Gets the content mimetype.
	 * 
	 * @return the content type
	 */
	public String getContentType();

	/**
	 * Gets the encoding of the content being accessed.
	 * 
	 * @return a valid java String encoding
	 */
	public String getEncoding();

	/**
	 * Sets the <code>String</code> encoding for this accessor.
	 * 
	 * @param encoding  a java-recognised encoding format
	 */
	public void setEncoding(String encoding);

	/**
	 * Gets the locale of the content being accessed.
	 *
	 * @return a valid java Locale
	 */
	public Locale getLocale();


	/**
	 * Checks if the {@link ContentAccessor#getContentUrl()} underlying content is present.
	 * 
	 * @return <code>true</code> if there is content at the URL referred to by this reader
	 */
	public boolean isExists();


	/**
	 * Gets the time of the last modification of the underlying content.
	 * 
	 * @return the last modification time using the standard <code>long</code> time, or
	 *         <code>0L</code> if the content does not exists
	 */
	public long getLastModified();

}
