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
package com.aplana.dbmi.service.client;

import java.io.IOException;

import com.aplana.dbmi.action.Material;
import com.aplana.dbmi.action.file.DatabaseIOException;
import com.aplana.dbmi.action.file.DownloadFilePart;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardVersion;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.Database;
import com.aplana.dbmi.service.impl.ProcessorBase;
import com.aplana.dbmi.service.impl.QueryFactory;
import com.aplana.dbmi.service.impl.UserData;

/**
 * Implementation of {@link MaterialPartLoader} interface used to read fragments of file stored 
 * in database. 
 * Could be used to read parts of file attached to current version of {@link Card} object 
 * or from one of {@link CardVersion previous versions} of a card.<br> 
 * Uses {@link DownloadFilePart} action to perform queries to database.
 */
public class InternalPartLoader implements MaterialPartLoader
{
	private ObjectId cardId;
	private int versionId;
	private String url;
	private ActionQueryBase query;
	Database database;
	UserData userData;
	
	/**
	 * Creates new InternalPartLoader instance
	 * @param client {@link ProcessorBase} descendant for whih this MaterialStream instance is created
	 * @param cardId identifier of {@link Card} to read material from
	 * @param versionId number of previous card {@link CardVersion version}
	 * to read material from. If it is equals to {@link Material#CURRENT_VERSION} then
	 * current version of {@link Card} will be used.
	 * @throws DataException if query factory is not configured properly 
	 */
	public InternalPartLoader(ProcessorBase client, ObjectId cardId, int versionId, String url)
			throws DataException {
		this.cardId = cardId;
		this.versionId = versionId;
		this.url = url;
		this.query = client.getQueryFactory().getActionQuery(DownloadFilePart.class);
		this.database = client.getDatabase();
		this.userData = client.getUser();
	}
	
	public InternalPartLoader(QueryFactory queryFactory, Database database, UserData userData, ObjectId cardId, int versionId, String url)
			throws DataException {
		this.cardId = cardId;
		this.versionId = versionId;
		this.url = url;
		this.query = queryFactory.getActionQuery(DownloadFilePart.class);
		this.database = database;
		this.userData = userData;
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
			query.setAction(part);
			return (byte[]) database.executeQuery(userData, query);
		} catch (DataException e) {
			e.printStackTrace();
			throw new DatabaseIOException(e);
		}
	}
}
