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
package com.aplana.dbmi.storage.impl.beans;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.naming.NamingException;

import org.apache.commons.logging.Log;

import com.aplana.dbmi.storage.ContentStorage;
import com.aplana.dbmi.storage.ContentStorageManager;
import com.aplana.dbmi.storage.content.exceptions.ContentException;
import com.aplana.dbmi.storage.impl.StorageConst;
import com.aplana.dbmi.storage.impl.StorageUtils;
import com.aplana.dbmi.storage.impl.url.URLStorageStreamHandler;

/*
 * @author <a href="mailto:ogalkin@aplana.com">Oleg Galkin</a>
 * @author <a href="mailto:rabdullin@aplana.com">Ruslan Abdullin</a>
 */
public class ContentStorageManagerBean 
	extends org.springframework.jndi.JndiObjectLocator
	implements ContentStorageManager 
{

	final protected Log logger = super.logger;

	/**
	 * ��� �������� -> 	���������
	 */
	final private Map<String, ContentStorage> storageMap =
		Collections.synchronizedMap(new HashMap<String, ContentStorage>());

	/**
	 * �������� -> ����� ��������
	 */
	final private Map<String, Set<ContentStorage>> protocolsMap =
		Collections.synchronizedMap( new HashMap<String, Set<ContentStorage>>() );


	/* (non-Javadoc)
	 * @see org.springframework.jndi.JndiObjectLocator#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws IllegalArgumentException,
			NamingException {
		super.afterPropertiesSet();

		// ������� ���� � ����������, �� ������ ���� ��������� ���������� ��� bean-��������
		// protocolsMap.clear();
		//for( Map.Entry<String, ContentStorage> item : storageMap.entrySet()) {
		//	register( item.getKey(), item.getValue() );
		//}
	}


	private void register(String name, ContentStorage storage) {
		if (name == null || storage == null) return;

		name = StorageUtils.normalizeStorageName(name);
		final String[] arr = storage.getSupportedProtocols();
		if (arr == null || arr.length < 1)
			throw new ContentException( "Can not register storage as '"+ name+ "': storage has empty protocol list");

		storage.setUrlHandler( new URLStorageStreamHandler( name, storage) );

		storageMap.put( name, storage);

		for (int i = 0; i < arr.length; i++) {
			final String protocol = StorageUtils.normalizeProtocol(arr[i]);
			Set<ContentStorage> list = protocolsMap.get(protocol);
			if (list == null) 
				protocolsMap.put( protocol,  list = new HashSet<ContentStorage>() );
			list.add(storage);
		}
	}


	protected void unregister(String name, ContentStorage contentStorage) {
		// ������� �� ������� �� �������� ...
		name = StorageUtils.normalizeStorageName(name);
		storageMap.remove(name);

		// ������� �� ������� ���������� (��� ���������� ������� �������)...
		for ( Set<ContentStorage> list : protocolsMap.values() )
			if (list != null) list.remove(contentStorage);
	}


	public EStorageResult registerContentStorage(String name,
			ContentStorage storage) {

		name = StorageUtils.normalizeStorageName(name);
		final boolean present = storageMap.containsKey(name);

		EStorageResult result = (present) 
				? EStorageResult._already_registered 
				: EStorageResult._success;
		if (storage != null) {
			register(name, storage);
		} else { // ���������� null => �����������������
			result = EStorageResult._not_registered;
			if (present) unregister( name, storageMap.get(name));
		}
		return result;
	}


	public EStorageResult unregisterContentStorage(String name) {
		name = StorageUtils.normalizeStorageName(name);
		EStorageResult result = EStorageResult._success;
		if (storageMap.containsKey(name))
			unregister( name, storageMap.get(name));
		else
			result = EStorageResult._not_registered;
		return result;
	}


	public ContentStorage getContentStorageByName(String storageName) {
		return storageMap.get( StorageUtils.normalizeStorageName(storageName));
	}


	public ContentStorage[] getContentStoragesByProtocol(String protocol) {
		final Set<ContentStorage> list = protocolsMap.get( StorageUtils.normalizeProtocol(protocol));
		return (list == null || list.isEmpty()) 
						? null 
						: list.toArray(new ContentStorage[list.size()]);
	}


	public ContentStorage[] getContentStorageByUrl(String contentUrl)
			throws ContentException, MalformedURLException 
	{
		final URLStorageLocation location = new URLStorageLocation(contentUrl, this);

		if (location.getStorageName() != null) 
		{	// ���� ���� ��� ��������� ...
			final ContentStorage storage = getContentStorageByName( location.getStorageName());
			return (storage == null) ? null : new ContentStorage[] { storage };
		}

		return getContentStoragesByProtocol( location.getURL().getProtocol() );
	}


	public Set<ContentStorage> getContentStorageByUrl( URLStorageLocation location) 
	{
		if (location == null) return null;

		if (location.getStorageName() != null) 
		{	// ���� ���� ��� ��������� ...
			final ContentStorage storage = getContentStorageByName( location.getStorageName());
			return (storage == null) ? null : Collections.singleton( storage);
		}

		return protocolsMap.get( StorageUtils.normalizeProtocol(location.getURL().getProtocol() ));
	}


	/**
	 * ���������� ��� ��������� (���� ��� ����������������).
	 * @param storage
	 * @return
	 */
	public String getNameByStorage( ContentStorage storage )
	{
		if (storage != null)
		{
			for ( Entry<String, ContentStorage> item : this.storageMap.entrySet() ) {
				if ( storage.equals( item.getValue()))
					return item.getKey(); // FOUND
			}
		}
		return null; // NOT FOUND
	}


	/**
	 * ���������� ����� ��������� � ���������� URL, 
	 * ��������: "filestore://localhost/1/2/3/4.doc"  ->  "filestore://localhost/$default/1/2/3/4.doc"
	 * @param storage: ���������, ���� null �� ������������ ��������� ��-���������.
	 * @param localUrl: ��������� ��� ��������� storage URL. 
	 */
	public String makeExternalUrl(ContentStorage storage, URL url) {

		String name = StorageConst.STORAGE_NAME_DEFAULT; 
		if (storage != null) {
			// ����������� ������������ �������� ��������� storage
			name = getNameByStorage(storage);
			if (name == null)
				throw new ContentException( "Cannot find name of non-registered storage: "+ storage );
		}

		return URLStorageStreamHandler.ensureUrlStorageName(name, url);
	}


	/**
	 * ��� ������������� �� springs ����� ������������ Map.
	 * @param theMap
	 */
	public void setStorageMap(Map /*<String, ContentStorage>*/ theMap)
	{
		this.storageMap.clear();
		this.protocolsMap.clear();
		if (theMap == null) return;
		// this.storageMap.putAll( theMap);
		for (Object /*Map.Entry<String, ContentStorage>*/ item: theMap.entrySet() ) {
			final Map.Entry<String, ContentStorage> entry = (Map.Entry<String, ContentStorage>) item;
			this.register( entry.getKey(), entry.getValue());
		}
	}


	/**
	 * ����� ��� ��������� URL � ������� �������� � ����� ����� (URL.getPath()).
	 * ����������� URL � ������ ��������� ����� ���:
	 * 		<��������>://<�����>:<������>@<����>:<����>/<$��� ���������>/<��� �����>
	 * <��� ���������> -- ���� ����� � ������ ���������, ���������� � $.
	 * 
	 * ������ ����� �������� ��� ��������� �� ������������ URL � �������������
	 * ������������ :
	 * 		1) (fullURL) �������� ������ (�����������) URL-���, ���������� ��� ���������.
	 * 		2) (storageName) ��� ��������� (��� ���� ��������) � ������� ��������, 
	 * 		3) (url) URL ��� ����� ���������,
	 * 		4) ����� openConnection() ���������� ��������� ��� ��������� (storage).
	 * ��������:
	 * 		1) ������ URL: "filestore://localhost/$default/123/45/6.doc"
	 * 		2) ��� ���������: "default"
	 * 		3) ��������� URL: "filestore://localhost/123/45/6.doc"
	 * 
	 * @author <a href="mailto:rabdullin@aplana.com">Ruslan Abdullin</a>
	 */
	public static class URLStorageLocation extends URLStreamHandler
	{

		private final String fullURL;
		private final URL url;
		private final String storageName;
		private ContentStorage storage;
		private final ContentStorageManager manager;


		public URLStorageLocation( String extendedUrl, ContentStorageManager mangr) 
			throws MalformedURLException 
		{
			this.manager = mangr;
			this.fullURL = StorageUtils.normalizeUrl(extendedUrl);
			URL tryURL = new URL( null, this.fullURL, this); // (!) self-����������

			// ��������� ����� ��������� �� URL...
			String nameStorage = null; 

			// ���� name=����� ���������� � "<prefix><name>/..." ("$abc/..."), 
			// �� ����������� ��� � ����� url ������ �������� ��������� <name> ("abc").
			//
			final String sPath = tryURL.getPath();
			if (sPath != null 
				&& 	( sPath.startsWith( StorageConst.PREFIX_STORAGENAME)
						|| sPath.startsWith( StorageConst.DELIMITER_URL_LEVELS + StorageConst.PREFIX_STORAGENAME)
					)
				) 
			{	// ���� ������ ��� ��������� ...
				final int fromIndex = (sPath.startsWith( StorageConst.DELIMITER_URL_LEVELS)) 
							? StorageConst.DELIMITER_URL_LEVELS.length() // ���� ���� -> ������� '/'
							: 0; 
				int i = sPath.indexOf(StorageConst.DELIMITER_URL_LEVELS, fromIndex);
				if (i < 0) i = sPath.length();

				// ������ ��� ��� �������� � '/'
				nameStorage = sPath.substring( StorageConst.PREFIX_STORAGENAME.length() + fromIndex, i).trim();

				// ����� � ��������� ����� ����� �����...
				++i;
				final String filePart = (i < sPath.length()) ? sPath.substring(i) : null;
				try {
					tryURL = new URL( null, StorageUtils.compileURL( tryURL, filePart ), this);
				} catch (MalformedURLException e) {
					e.printStackTrace();
					tryURL = null;
				}
			}
			this.url = tryURL;
			this.storageName = StorageUtils.normalizeStorageName(
							(nameStorage != null) && !("".equals(nameStorage)) ? nameStorage : null
						);
		}


		/**
		 * ���������� ������ ����� fullUrl � path-������, �� ��� ����� ��������� � ���.
		 * @return
		 */
		public URL getURL() {
			return this.url;
		}

		/**
		 * @return ��� ��������� (��� ���� ��������) ��� null ���� ��� �� ���� ������.
		 */
		public String getStorageName() {
			return this.storageName;
		}


		/**
		 * @return URL-������ ���������� ����� ��� ��������� ����� �������� �������.
		 */
		public String getFullURL() {
			return this.fullURL;
		}


		/**
		 * @return ��������� � ������ url ���������, �������� ������ ����� ������ openConnection().
		 */
		public ContentStorage getStorage() {
			// if (storage == null) openConnection(null);
			return storage;
		}


		@Override
		protected URLConnection openConnection(URL u) throws IOException {
			// super.openConnection(u, p);
			if (u == null) 
				u = this.url;
			if (this.manager == null) 
				throw new IOException( MessageFormat.format("Manager is not set or unsupported url ''{0}''", this.fullURL));

			this.storage = this.manager.getContentStorageByName(this.storageName);
			if (this.storage == null || this.storage.getUrlHandler() == null)
				throw new IOException( MessageFormat.format("Storage not registered or unsupported url ''{0}''", this.fullURL));
			return this.storage.getUrlHandler().openConnection( url);
		}
	}


}
