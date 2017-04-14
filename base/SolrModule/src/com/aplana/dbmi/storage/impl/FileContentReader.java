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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.aplana.dbmi.storage.ContentStorage;
import com.aplana.dbmi.storage.content.exceptions.ContentException;

/**
 * @author <a href="mailto:ogalkin@aplana.com">Oleg Galkin</a>
 * @author <a href="mailto:rabdullin@aplana.com">Ruslan Abdullin</a>
 */
public class FileContentReader extends AbstractContentReader {

	private final File file;

	public FileContentReader( ContentStorage ownerStorage, File file, URL contentUrl, String contentType) {
		super( ownerStorage, contentUrl, contentType, (file.exists() ? file.lastModified() : 0));
		this.file = file;
	}

	public FileContentReader(ContentStorage ownerStorage, File file, URL contentUrl)
	{
		this( ownerStorage, file, contentUrl, null);
	}

	/*
	public FileContentReader(File file) 
	{
		this(file, new URL( StorageConst.DEFAULT_PROTOCOL + StorageConst.PROTOCOL_DELIMITER + file.getAbsolutePath()) );
	}
	 */

	/**
	 * @return the file
	 */
	public File getFile() {
		return this.file;
	}


	@Override
	public boolean isExists() {
		return (file != null) && file.exists();
	}


	@Override
	public long getSize() {
		return isExists() ? file.length() : -1;
	}


	@Override
	public long getLastModified() {
		return file.exists() ? file.lastModified() : 0;
	}


	@Override
	public InputStream getContentInputStream() 
		throws ContentException 
	{
		if (!file.exists()) 
			return null;

		try {
			return new FileInputStream(file);
		} catch (IOException e) {
			throw new ContentException("Failed to get input stream for file " + file);
		}
	}

}