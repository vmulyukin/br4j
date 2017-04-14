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
package com.aplana.dbmi.module;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.Material;
import com.aplana.dbmi.action.file.DatabaseIOException;
import com.aplana.dbmi.action.file.DownloadFilePart;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardVersion;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.client.MaterialPartLoader;
import com.aplana.dbmi.service.impl.ProcessorBase;

/**
 * Implementation of {@link MaterialPartLoader} interface used to read fragments of file stored 
 * in database. 
 * Could be used to read parts of file attached to current version of {@link Card} object 
 * or from one of {@link CardVersion previous versions} of a card.<br> 
 * Uses {@link DownloadFilePart} action to perform queries to database.
 */
public class DataServiceBeanPartLoader implements MaterialPartLoader
{
	private ObjectId cardId;
	private int versionId;
	private String url;
	private DataServiceBean service;
	
	protected final Log logger = LogFactory.getLog(getClass());
	
	/**
	 * Creates new InternalPartLoader instance
	 * @param client {@link ProcessorBase} descendant for whih this MaterialStream instance is created
	 * @param cardId identifier of {@link Card} to read material from
	 * @param versionId number of previous card {@link CardVersion version}
	 * to read material from. If it is equals to {@link Material#CURRENT_VERSION} then
	 * current version of {@link Card} will be used.
	 * @throws DataException if query factory is not configured properly 
	 */
	
	public DataServiceBeanPartLoader(DataServiceBean service, ObjectId cardId, int versionId, String url)
			throws DataException {
		this.cardId = cardId;
		this.versionId = versionId;
		this.url = url;
		this.service = service;
	}
	
	
	/**
	 * @see MaterialPartLoader#readPart(int, int)
	 */
	public byte[] readPart(int offset, int length) throws IOException {
		DownloadFilePart part = new DownloadFilePart();
		part.setCardId(cardId);
		part.setVersionId(versionId);
		part.setUrl(url);
		part.setOffset(offset);
		part.setLength(length);
		try {
			return (byte[]) service.doAction(part);
		} catch (DataException e) {
			logger.error(e.getMessage(),e);
			throw new DatabaseIOException(e);
		} catch (ServiceException e) {
			logger.error(e.getMessage(),e);
			throw new IOException(e);
		}
	}
}
