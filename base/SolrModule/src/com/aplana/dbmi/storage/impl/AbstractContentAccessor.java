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

import java.net.URL;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.storage.ContentStorage;
import com.aplana.dbmi.storage.content.ContentAccessor;

/**
 * Base {@link ContentAccessor} implementation.
 * 
 * @author <a href="mailto:ogalkin@aplana.com">Oleg Galkin</a>
 * @author <a href="mailto:rabdullin@aplana.com">Ruslan Abdullin</a>
 */
public abstract class AbstractContentAccessor implements ContentAccessor {
	protected final Log logger = LogFactory.getLog(getClass());
	public static final String DEFAULT_CHARSET = "utf-8";

	protected URL contentUrl = null;
	protected String contentType = null;
	protected String encoding = null;
	protected Locale locale = Locale.getDefault();
	protected long lastModified = 0;
	protected ContentStorage ownerStorage = null;

	public AbstractContentAccessor()
	{
	}


	protected AbstractContentAccessor( ContentStorage ownerStorage, URL contentUrl, String contentType,
			Locale locale, long lastModified) {
		this();
		this.ownerStorage = ownerStorage;
		this.contentUrl = contentUrl;
		this.contentType = contentType;
		this.locale = locale;
		this.lastModified = lastModified;
	}

	protected AbstractContentAccessor( ContentStorage ownerStorage, URL contentUrl, String contentType,
			long lastModified) {
		this( ownerStorage, contentUrl, contentType, Locale.getDefault(), lastModified);
	}

	public AbstractContentAccessor( ContentStorage ownerStorage, URL contentUrl, String contentType) {
		this( ownerStorage, contentUrl, contentType, Locale.getDefault(), 0);
	}


	public AbstractContentAccessor(ContentStorage ownerStorage, URL contentUrl) {
		this( ownerStorage, contentUrl, null, Locale.getDefault(), 0);
	}


	//---------------------------------------------------------------------
	public static String getCharsetFromContentType( String contentType )
	{
		if( contentType != null ) {
			int idx = contentType.toLowerCase().indexOf( "charset=" );
			if( idx > 0 ) {
				return contentType.substring( idx + "charset=".length() ).trim();
			}
		}

		return null;
	}


	public String getContentType() {
		return this.contentType;
	}


	public URL getContentUrl() {
		return this.contentUrl;
	}

	public long getLastModified() {
		return this.lastModified;
	}


	public Locale getLocale() {
		return this.locale;
	}


	public String getEncoding() {
		return encoding;
	}


	public void setEncoding(String encoding) {
		// Validate.notNull(encoding, "Encoding must not be null");
		this.encoding = encoding;
	}


	public ContentStorage getStorage()
	{
		return this.ownerStorage;
	}


	abstract public long getSize();
	abstract public boolean isExists();


//	private String encoding;
//	
//	/**
//	 * Constructs a new accessor by the specific content URL.
//	 * @param contentUrl  the content URL the accessor references
//	 */
//	protected AbstractContentAccessor(String contentUrl) {
//		Validate.notEmpty(contentUrl, "Content URL must not be empty");
//		this.contentUrl = contentUrl;
//		
//		// the default encoding is Java's default encoding
//		this.encoding = DEFAULT_ENCODING;
//		// the default locale
//		this.locale = Locale.getDefault();
//	}
//
//	public void setContentType(String mimetype) {
//		this.contentType = mimetype;
//	}
//	
//	public void setLocale(Locale locale) {
//		this.locale = locale;
//	}
//
//	@Override
//	public String toString() {
//		return new ToStringBuilder(this).append("contentUrl", contentUrl).toString();
//	}

}
