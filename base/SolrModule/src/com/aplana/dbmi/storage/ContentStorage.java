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
package com.aplana.dbmi.storage;

import java.net.MalformedURLException;

import com.aplana.dbmi.storage.content.ContentAccessor;
import com.aplana.dbmi.storage.content.ContentReader;
import com.aplana.dbmi.storage.content.ContentWriter;
import com.aplana.dbmi.storage.content.exceptions.ContentException;
import com.aplana.dbmi.storage.content.exceptions.UnsupportedContentUrlException;
import com.aplana.dbmi.storage.impl.url.URLStorageStreamHandler;

/**
 * This interface provides low-level retrieval of content {@link ContentReader readers} and
 * {@link ContentWriter writers}.
 * <p/>
 * Implementations of this interface should be solely responsible for providing persistence and
 * retrieval of the content against a content URL.
 * <p/>
 * Content URLs must consist of a prefix or protocol followed by an implementation-specific
 * identifier. For example, the content URL format for file stores is
 * <code>filestore:year/month/day/hour/min/UUID.dat</code>, where
 * <ul>
 *   <li><code>filestore</code> &ndash; protocol identifying a file store;</li>
 *   <li><code>year</code> &ndash; year;</li>
 *   <li><code>month</code> &ndash; 1-based month of the year;</li>
 *   <li><code>day</code> &ndash; 1-based day of the month;</li>
 *   <li><code>hour</code> &ndash; 0-based hour of the day;</li>
 *   <li><code>minute</code> &ndash; 0-based minute of the hour;</li>
 *   <li><code>UUID</code> &ndash; a unique identifier.</li>
 * </ul>
 * 
 * @author <a href="mailto:ogalkin@aplana.com">Oleg Galkin</a>
 * @author <a href="mailto:rabdullin@aplana.com">Ruslan Abdullin</a>
 */
public interface ContentStorage {

	/**
	 * Gets all the protocol supported by the content store.
	 *
	 * @return an array of protocols the content store supports
	 */
	public String[] getSupportedProtocols();

	/**
	 * Checks if the content URL supported by the content store.
	 * 
	 * @param  contentUrl  the content URL to check
	 * @return <code>true</code> if the content URL supported, <code>false</code> otherwise
	 * @throws MalformedURLException 
	 */
	public boolean isUrlSupported(String contentUrl) 
		throws MalformedURLException;

	/**
	 * Gets the location where the store is rooted. The format of the returned value will depend on
	 * the specific implementation of the store.
	 * 
	 * @return the store's root location or '<code>.</code>' if no information is available
	 */
	public String getRootLocation();

	/**
	 * Gets a {@link ContentReader} with which to read from the content at the given URL.
	 * 
	 * @param contentUrl  the path to where the content is located
	 * @return the {@link ContentReader} for the given URL or <code>null</code> if there is no content
	 *         at the given URL
	 * @throws UnsupportedContentUrlException if the content URL is not supported by the store
	 * @throws ContentException if an IO error occurs
	 */
	public ContentReader getReader(String contentUrl) throws ContentException;

	/**
	 * Gets a {@link ContentWriter} with which to write content to a location within the store.
	 * The location may be specified but must, in that case, be a valid and unused URL.
	 * <p/>
	 * The store will ensure that the {@link ContentAccessor#getContentUrl()} will be valid for all
	 * subsequent read attempts.
	 * 
	 * @param contentUrl  the path to where the content should be located or <code>null</code> to
	 *                    create a new URL
	 * @return the {@link ContentWriter} to write content
	 * @throws UnsupportedContentUrlException if the content URL is not supported by the store
	 * @throws ContentExistsException if the content URL is already in use
	 * @throws ContentException if an IO error occurs
	 * 
	 * @see ContentWriter#getContentUrl()
	 */
	public ContentWriter getWriter(String contentUrl) throws ContentException;


	/**
	 * @return handler for url creation form like in example: 
	 * 		new Url( "myprotocol", "myhost", 1234, "/a/b/c", storage.getUrlHandler() );.
	 */
	public URLStorageStreamHandler getUrlHandler();

	public void setUrlHandler(URLStorageStreamHandler storageStreamHandler);
}
