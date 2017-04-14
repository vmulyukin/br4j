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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;

import com.aplana.dbmi.storage.ContentStorage;
import com.aplana.dbmi.storage.content.ContentWriter;
import com.aplana.dbmi.storage.content.exceptions.ContentException;

/**
 * @author <a href="mailto:ogalkin@aplana.com">Oleg Galkin</a>
 * @author <a href="mailto:rabdullin@aplana.com">Ruslan Abdullin</a>
*/
public class FileContentWriter extends AbstractContentWriter {
	private File file;

	public FileContentWriter( ContentStorage ownerStorage, File file, URL contentUrl, String contentType) {
		super( ownerStorage, contentUrl, contentType, (file.exists() ? file.lastModified() : 0));
		this.file = file;
	}

	public FileContentWriter( ContentStorage ownerStorage, File file, URL contentUrl)
	{
		this( ownerStorage, file, contentUrl, null);
	}

//	public FileContentWriter(File file) {
//		this(file, new URL( StorageConst.DEFAULT_PROTOCOL + StorageConst.PROTOCOL_DELIMITER + file.getAbsolutePath()) );
//	}


	@Override
	public OutputStream getContentOutputStream() throws ContentException {
		try {
			boolean flAppend = file.exists(); // fileOffset > 0;
			final FileOutputStream outStm = new OOoOutputStream( file, flAppend);
			return outStm;
		} catch (Exception e) {
			throw new ContentException("Failed to get output stream for file " + file);
		}
	}


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
	public boolean delete() throws ContentException {
		return isExists() && file.delete();
	}

	@Override
	public URL copy(URL destUrl) throws ContentException {
		// �����������
		if (this.getStorage() == null) 
			throw new ContentException( "No storage base: fail to copy '"+ this.getContentUrl() 
					+ "' into '"+ destUrl+ "'");

		final ContentWriter dest = 
			this.getStorage().getWriter( (destUrl == null) ? null : destUrl.toExternalForm());
		if (destUrl == null)
			destUrl = dest.getContentUrl();

		dest.setContentType( this.getContentType() );
		dest.setLocale( this.getLocale() );
		if (this.isExists()) { 
			try {
				final FileInputStream fin = new FileInputStream( this.file);
			try {
					dest.putContent( fin, 0, -1);
				} finally {
					IOUtils.closeQuietly(fin);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		return destUrl;
	}

}
