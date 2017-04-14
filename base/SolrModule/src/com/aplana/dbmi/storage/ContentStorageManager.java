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
import java.net.URL;

import org.jboss.security.auth.spi.UsersRolesLoginModule;

import com.aplana.dbmi.storage.content.exceptions.ContentException;
import com.aplana.dbmi.storage.impl.StorageConst;

/**
 * Factory interface to get the {@link ContentStore} that handles a given protocol. 
 *
 * @author <a href="mailto:ogalkin@aplana.com">Oleg Galkin</a>
 * @author <a href="mailto:rabdullin@aplana.com">Ruslan Abdullin</a>
 */
public interface ContentStorageManager
{
	// TODO: (!) ���� ������������ URL ��� ��� �������� ��� ��������� � XURL ���, ��� ����� ��� ���������.
	
	/**
	 * Gets {@link ContentStore} that was registered under assumed unique name.
	 *
	 * @param  storageName the content storage location name.
	 * 
	 * @return the registered content storage or <code>null</code> if nothing located under the name.
	 */
	public ContentStorage getContentStorageByName(String storageName) 
			throws ContentException;


	/**
	 * Gets {@link ContentStore} that supports a given protocol.
	 *
	 * @param  protocol  the protocol to get info, null or empty string are used to get
	 * default storage.
	 * 
	 * @return the content store that supports the protocol or <code>null</code> if no content
	 *  store supports it.
	 */
	public ContentStorage[] getContentStoragesByProtocol(String protocol)
			throws ContentException;


	/**
	 * Gets {@link ContentStore} array that can handles the content URL.
	 *
	 * @param  contentUrl  the protocol and path to where the content is located, 
	 * the storage pint name can present here. 
	 * 	Example URL: "filestore://localhost/$default/123/45/6.doc"
	 * 
	 * @return the content store for the <code>contentUrl</code> or <code>null</code> if 
	 * no content storage can handle the content URL.
	 */
	public ContentStorage[] getContentStorageByUrl(String contentUrl)
			throws ContentException, MalformedURLException;


	/**
	 * Registers a {@link ContentStore} to the content storage under assumed unique name.
	 *
	 * @param name the unique name (case insensitive) for the storage, "" or null is
	 * equal to defaultStorageName. 
	 * 
	 * @param store  the content store to be registered, null is like unregister.
	 * 
	 * @return the registration result:
	 * 		EStorageResult._not_registered  	if store == null,
	 * 		EStorageResult._already_registered	if store != null and another storage was replaced by store,
	 * 		EStorageResult._success				if store was registered first time.
	 */
	public EStorageResult registerContentStorage(String name, ContentStorage store)
			throws ContentException;


	/**
	 * Unregisters the content storage.
	 *
	 * @param name the unique name (case insensitive) of the registered storage. 
	 * 
	 * @return the result of the unreg operation:
	 * 		EStorageResult._success			if the storage was unregistered,
	 * 		EStorageResult._not_registered	if no storage was registered under the name.
	 */
	public EStorageResult unregisterContentStorage(String name)
			throws ContentException;


	/**
	 * �������� URL ������ � ��������� ���������.
	 * @param storage: ���������, null = ������������ ��������� ��-���������.
	 * @param localUrl: ��������� ��� ��������� URL.
	 * @return
	 */
	public String makeExternalUrl( ContentStorage storage, URL localUrl);


	/**
	 * ���� ���������� �������� ����������.
	 */
	public enum EStorageResult { _none, _success, _not_registered, _already_registered };

}
