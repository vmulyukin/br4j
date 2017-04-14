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
package com.aplana.dbmi.storage.content.exceptions;

import com.aplana.dbmi.storage.ContentStorage;

/**
 * Exception produced when a content URL is not supported by a particular {@link ContentStore}
 * implementation.
 * 
 * @author <a href="mailto:ogalkin@aplana.com">Oleg Galkin</a>
 */
public class UnsupportedContentUrlException extends ContentException {
	private static final long serialVersionUID = -1005484961926535713L;
	
	private String contentUrl;
	private ContentStorage store;


	/**
	 * Constructs an exception with specified detail message.
	 *
	 * @param store       the originating content store
	 * @param contentUrl  the offending content URL
	 * @param message     the error message
	 * @param cause       the previous exception in the chain
	 */
	public UnsupportedContentUrlException(ContentStorage store, String contentUrl, String message, Throwable cause) {
		super(message, cause);
		this.store = store;
		this.contentUrl = contentUrl;
	}


	/**
	 * Constructs an exception for specified {@link ContentStorage} and content URL with default
	 * detail message.
	 *
	 * @param store       the originating content store
	 * @param contentUrl  the offending content URL
	 */
	public UnsupportedContentUrlException(ContentStorage store, String contentUrl, Throwable cause) {
		this( store, contentUrl, 
				"The content URL <" + contentUrl + "> is not supported by the content store " + store.getClass().getName(),
				cause);
	}

	public UnsupportedContentUrlException(ContentStorage store, String contentUrl) {
		this(store, contentUrl, (Exception) null);
	}

	/**
	 * Gets the content URL that results in the exception.
	 * 
	 * @return the offending content URL
	 */
	public String getContentUrl() {
		return contentUrl;
	}

	/**
	 * Gets the {@link ContentStorage} that originates the exception.
	 * 
	 * @return the originating content store
	 */
	public ContentStorage getContentStore() {
		return store;
	}
}
