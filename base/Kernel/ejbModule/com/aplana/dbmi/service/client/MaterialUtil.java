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

import com.aplana.dbmi.action.DownloadFile;
import com.aplana.dbmi.action.Material;
import com.aplana.dbmi.action.UploadFile;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardVersion;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataService;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ProcessorBase;

/**
 * Utility class used to work with material files attached to cards
 * inside of {@link DataService} EJB.<br>
 * Should be used in {@link ProcessorBase} descendants which needs to work with
 * content of material.
 */
public class MaterialUtil
{
	/**
	 * Returns new {@link MaterialStream} instance representing file attached to 
	 * given {@link Card} (or to one of its {@link CardVersion}).
	 * @param cardId identifier of card to take material file from
	 * @param versionId number of card {@link CardVersion version} to take. If it is equals to
	 * {@link Material#CURRENT_VERSION} then current version of file will be used.
	 * @param caller {@link ProcessorBase} descendant performing this method call
	 * @return {@link MaterialStream} object representing material file attached to given card
	 * (or one of previous versions of this card)
	 * @throws DataException if any error occurs during work with QueryFactory 
	 * @throws IllegalArgumentException if given cardId is not {@link Card} identifier
	 */
	public static MaterialStream getMaterial(ObjectId cardId, int versionId, ProcessorBase caller)
			throws DataException
	{
		if (!Card.class.equals(cardId.getType()))
			throw new IllegalArgumentException("Not a card id");
		if (caller.getAction() instanceof DownloadFile &&
				cardId.equals(((DownloadFile) caller.getAction()).getCardId()) &&
				versionId == ((DownloadFile) caller.getAction()).getVersionId())
			return getCurrentMaterial(caller);
		DownloadFile download = new DownloadFile();
		download.setCardId(cardId);
		ActionQueryBase query = caller.getQueryFactory().getActionQuery(DownloadFile.class);
		query.setAction(download);
		Material material = (Material) caller.getDatabase().executeQuery(caller.getUser(), query);
		if (material == null)
			return null;
		return new MaterialStream(material.getLength(),
				new InternalPartLoader(caller, cardId, versionId, material.getUrl()));
	}
	
	/**
	 * Returns new {@link MaterialStream} instance representing current version of file attached to 
	 * given {@link Card}
	 * @param cardId identifier of {@link Card} to take material from
	 * @param caller {@link ProcessorBase} descendant performing this method call
	 * @return {@link MaterialStream} object representing material file attached to given card
	 * @throws DataException if any error occurs during work with QueryFactory
	 * @throws IllegalArgumentException if given cardId is not {@link Card} identifier
	 */
	public static MaterialStream getMaterial(ObjectId cardId, ProcessorBase caller) throws DataException
	{
		return getMaterial(cardId, Material.CURRENT_VERSION, caller);
	}
	
	/**
	 * This method could be used in post-processors of queries 
	 * processing {@link UploadFile} and {@link DownloadFile}
	 * actions or in any of pre/post-processors specified for query used to save
	 * {@link Card} object
	 * @param caller {@link ProcessorBase} descendant performing this method call
	 * @return MaterialStream representing material attached to {@link Card} or {@link CardVersion}
	 * processed by query to which given processor belongs
	 * @throws DataException if any error occurs during work with QueryFactory
	 */
	public static MaterialStream getCurrentMaterial(ProcessorBase caller) throws DataException
	{
		ObjectId cardId = null;
		int versionId = Material.CURRENT_VERSION;
		int length = 0;
		String url;
		
		if (caller.getAction() instanceof DownloadFile) {
			if (caller.getResult() == null)
				throw new IllegalStateException("Can't access material data while preprocessing download");
			DownloadFile download = (DownloadFile) caller.getAction();
			cardId = download.getCardId();
			versionId = download.getVersionId();
			Material material = (Material) caller.getResult();
			if (material == null)
				return null;
			length = material.getLength();
			url = material.getUrl();
		} else if (caller.getAction() instanceof UploadFile) {
			if (caller.getResult() == null) {
				throw new IllegalStateException("Can't access material data while preprocessing upload");
			}
			UploadFile upload = (UploadFile) caller.getAction();
			cardId = upload.getCardId();
			length = upload.getLength();
			url = upload.getUrl();
		} else if (caller.getObject() instanceof Card) {
			return getMaterial(caller.getObject().getId(), Material.CURRENT_VERSION, caller); 
		} else {
			throw new IllegalStateException("Not a card or material processing action");
		}
		return new MaterialStream(length, new InternalPartLoader(caller, cardId, versionId, url));
	}
}
