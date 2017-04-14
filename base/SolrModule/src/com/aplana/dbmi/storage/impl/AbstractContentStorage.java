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

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.aplana.dbmi.storage.ContentStorage;
import com.aplana.dbmi.storage.ContentStorageManager;
import com.aplana.dbmi.storage.content.ContentReader;
import com.aplana.dbmi.storage.content.ContentWriter;
import com.aplana.dbmi.storage.content.exceptions.UnsupportedContentUrlException;
import com.aplana.dbmi.storage.impl.url.URLStorageStreamHandler;

public abstract class AbstractContentStorage 
	extends org.springframework.jndi.JndiObjectLocator
	implements ContentStorage //, URLStreamHandlerFactory
{

	final protected Log logger = super.logger;


	/**
	 * �������������� ��������� (����� � ������ ��������).
	 */
	protected final Set<String> protocolsSupported = new HashSet<String>();

	protected URLStorageStreamHandler urlHandler;

	private ContentStorageManager manager;

	/**
	 * ��������� ��������� �������� (� �������� ������� (��������) ������� ��������)
	 */
	protected String rootLocation = ".";

	final private String[] convertAim = new String[]{};  
	/**
	 * ������ �������������� ���������� � ������ ��������.
	 */
	public String[] getSupportedProtocols() {
		return protocolsSupported.toArray( convertAim );
	}


	public URLStreamHandler createURLStreamHandler(String protocol) {
		if (!protocolsSupported.contains( StorageUtils.normalizeProtocol(protocol) ))
			return null;
		// if (this.urlHandler == null) this.urlHandler = new URLStorageStreamHandler( null, this);
		return this.urlHandler;
	}


	/**
	 * ����� ����������� ������ ��������� ��������� ��� ������������ URL.
	 * 
	 * @param contentUrl ����������� URL, ���������� ���� ��������� ��������� ����� ����� 
	 * ���������� ����� ��� � ��� (@link StorageUtls.normalizeProtocol), �.�. ��� 
	 * �������� ��-��������� (@link  StorageConst.defaultProtocol / "filestore").
	 * 
	 * @throws MalformedURLException 
	 */
	public boolean isUrlSupported(String contentUrl) 
	{
		try {
			final URL url = new URL( null, StorageUtils.normalizeUrl(contentUrl), this.getUrlHandler());
			return isUrlSupported(url);
		} catch (MalformedURLException e) {
			return false;
		}
	}

	public boolean isUrlSupported(URL url) 
	{
		return (url != null) 
			&& (url.getProtocol() != null) 
			&& this.protocolsSupported.contains( 
						StorageUtils.normalizeProtocol( url.getProtocol())
					);
	}


	public String getRootLocation() {
		return this.rootLocation;
	}


	/**
	 * ����� �������, �� �� ���������� ContentStorage.
	 * @param newRootLocation
	 */
	
	public void setRootLocation(String newRootLocation) {
		this.rootLocation = newRootLocation;
	}


	abstract public ContentReader getReader(String contentUrl) throws UnsupportedContentUrlException;
	abstract public ContentWriter getWriter(String contentUrl) throws UnsupportedContentUrlException;


	protected void addSupportedProtocol(String protocol) {
		if (protocol == null) return;
		final String nrm = StorageUtils.normalizeProtocol( protocol);
		this.protocolsSupported.add( nrm);
		// if (manager != null) manager.refresh(this); // TODO: add this methos to ContentStorageManager
	}


	public URLStorageStreamHandler getUrlHandler() {
		if (this.urlHandler == null)
			this.urlHandler = new URLStorageStreamHandler( null, this);
		return this.urlHandler;
	}


	public void setUrlHandler(URLStorageStreamHandler storageStreamHandler) {
		this.urlHandler = storageStreamHandler;	
	}


	public ContentStorageManager getManager() {
		return this.manager;
	}


	public void setManager(ContentStorageManager manager) {
		this.manager = manager;
	}


}
