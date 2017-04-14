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
package com.aplana.dbmi.storage.impl.url;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.apache.commons.lang.Validate;

import com.aplana.dbmi.storage.ContentStorage;
import com.aplana.dbmi.storage.content.ContentReader;
import com.aplana.dbmi.storage.content.ContentWriter;
import com.aplana.dbmi.storage.impl.StorageConst;
import com.aplana.dbmi.storage.impl.StorageUtils;
import com.aplana.dbmi.storage.impl.beans.ContentStorageManagerBean;


/**
 * ���������� ��� ��������� URL ������ ���������� storage.
 * @author RAbdullin
 */
public class URLStorageStreamHandler extends URLStreamHandler {

	final protected ContentStorage storage;
	final protected String storageName;


	public URLStorageStreamHandler( String name, ContentStorage storage) {
		//Validate.notNull( name, "Storage name must be set");
		Validate.notNull( storage, "Storage must be set");
		this.storageName = StorageUtils.normalizeStorageName(name);
		this.storage = storage;
	}


	@Override
	public URLConnection openConnection(URL url) 
		throws IOException 
	{
		if (url == null) 
			return null;

		// ��������� ������ ����� ����:
		//		1) ������������� "./xxx";
		//		2) ������ ������� ���������: "$storageName/xxx";
		if (url.getPath() != null) {
			if (url.getPath().startsWith( StorageConst.ROOT_OF_STORAGE)) {
				// ������� - ��� ������������� url...
			} else { 
				final ContentStorageManagerBean.URLStorageLocation location = 
					new ContentStorageManagerBean.URLStorageLocation( url.toExternalForm(), null);
				if ( this.storageName != null 
						&& !this.storageName.equals(location.getStorageName()))
					// error: ��� ������� ����� ���������...
					throw new IOException( "openConnection(storage='"+this.storageName+"'): invalid storage name inside url '"+ url +"'");
				url = location.getURL(); // �������� ������ ����� url ��� ����� ���������...
			}
		}
		return new StorageURLConnection( url);
	};


	ContentStorage getStorage() {
		return storage;
	}


	@Override
	protected String toExternalForm(URL u) {
		// return super.toExternalForm(u);
		// ���������� ����� ���������...
		return ensureUrlStorageName(this.storageName, u);
	}


	/**
	 * �������� �������� ���������, ������ ���� ��� ��� � url.
	 * @param name �������� ���������, ������� ���� �������� ������ � ����� url.
	 * @param url
	 * @return
	 */
	
	public static String ensureUrlStorageName( String name, URL url) {

		if (name == null || name.equals(""))
			return StorageUtils.compileURL( url);

		name = StorageConst.PREFIX_STORAGENAME + name;

		if ( (url.getPath() != null) && url.getPath().toLowerCase().indexOf(name.toLowerCase()) >= 0)
			// ��� ���� ��� -> ������ �� ���� ���������...
			return StorageUtils.compileURL( url); 

		// ���� ��������...
		return StorageUtils.compileURL( url,
					// ��������� ����� ��������� ����� ������ � �����������...
					name + StorageConst.DELIMITER_URL_LEVELS+ url.getFile()
				);
	}


//	/**
//	 * @return ������ �� null �������� � ������ �������. 
//	 */
//	private String getNamePrefix() {
//		return ((this.storageName == null) || "".equals(this.storageName))
//					? ""
//					: StorageConst.PREFIX_STORAGENAME + this.storageName + StorageConst.DELIMITER_URL_LEVELS;
//	}


	/**
	 * @author RAbdullin
	 */
	public class StorageURLConnection
		extends URLConnection
	{

//		final private ContentStorage storage;
//		public StorageURLConnection( ContentStorage storage, URL url) {
//			super( url );
//			this.storage = storage;
//		}


		public StorageURLConnection( URL url) {
			super( url );
		}


		@Override
		public void connect() throws IOException {
			// TODO: �������� ���� ����� ��������� ��� ���-����� ��������
			if (!storage.isUrlSupported(url.toExternalForm()))
				throw new IOException( "Connection problem: storage unsupported url '"+ url +"'");
		}


		@Override
		public InputStream getInputStream() throws IOException {
			setDoInput(true);
			final ContentReader reader = storage.getReader( this.url.toExternalForm() );
			// return new BufferedInputStream( reader.getContentInputStream() );
			return reader.getContentInputStream();
		}


		@Override
		public OutputStream getOutputStream() throws IOException {
			setDoOutput(true);
			final ContentWriter writer = storage.getWriter( this.url.toExternalForm() );
			return writer.getContentOutputStream();
		}

	}

}
