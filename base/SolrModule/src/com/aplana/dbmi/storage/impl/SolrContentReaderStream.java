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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.solr.common.util.ContentStream;
import org.apache.solr.common.util.ContentStreamBase;

import com.aplana.dbmi.storage.content.ContentReader;

/**
 * {@link ContentStream} implementation that wraps a {@link ContentReader}.
 *
 * @author <a href="mailto:ogalkin@aplana.com">Oleg Galkin</a>
 */
public class SolrContentReaderStream extends ContentStreamBase 
{
	private ContentReader reader;
	
	/**
	 * Constructs a new object by specified {@link ContentReader}.
	 *
	 * @param reader  the content reader to read content from
	 */
	public SolrContentReaderStream(ContentReader reader, String contentTyp, String contentName) {
		this.reader = reader;
		setContentType( (contentTyp != null)? contentTyp : reader.getContentType());
		setSize(reader.getSize());
		if (contentName != null)
			setName( contentName);
		if (reader.getContentUrl() != null)
			setSourceInfo( reader.getContentUrl().toExternalForm() );
	}

	public SolrContentReaderStream(ContentReader reader, String contentType) {
		this( reader, contentType, null);
	}


	/* (non-Javadoc)
	 * @see org.apache.solr.common.util.ContentStream#getStream()
	 */
	public InputStream getStream() throws IOException {
		return reader.getContentInputStream();
	}

	/* (non-Javadoc)
	 * @see org.apache.solr.common.util.ContentStreamBase#getReader()
	 */
	@Override
	public Reader getReader() throws IOException {
		return 
			(reader.getEncoding() != null)
			? new InputStreamReader(getStream(), reader.getEncoding())
			: new InputStreamReader(getStream());
	}
}
