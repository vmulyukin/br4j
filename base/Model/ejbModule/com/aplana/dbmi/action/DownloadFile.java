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
package com.aplana.dbmi.action;

import com.aplana.dbmi.action.file.ActionPerformer;
import com.aplana.dbmi.action.file.ContinuousAction;
import com.aplana.dbmi.action.file.DownloadFileStream;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataService;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.User;

/**
 * {@link Action} implementation used to download
 * material file attached to given {@link Card} object.
 * Also this action could be used to download previous version of attached file,
 * if {@link #setVersionId(int) versionId} was specified.
 * <br>
 * Returns {@link Material} object representing material being downloaded.
 * Content of file could be received through {@link Material#getData()} method call.
 */
public class DownloadFile implements ObjectAction<Material>, ContinuousAction
{
	private static final long serialVersionUID = 3L;

	private ObjectId cardId;
	private int versionId = Material.CURRENT_VERSION;

	private transient ActionPerformer service;

	/**
	 * Gets identifier of card to download material from
	 * @return identifier of card to download material from
	 */
	public ObjectId getCardId() {
		return cardId;
	}

	/**
	 * Sets identifier of card to download material from
	 * @param cardId identifier of card to download material from
	 */
	public void setCardId(ObjectId cardId) {
		if (cardId == null || !Card.class.equals(cardId.getType()))
			throw new IllegalArgumentException("Not a card ID");
		this.cardId = cardId;
	}

	/**
	 * @see Action#getResultType()
	 */
	public Class<?> getResultType() {
		return Material.class;
	}

	/**
	 * @see ObjectAction#getObjectId()
	 */
	public ObjectId getObjectId() {
		return getCardId();
	}

	/**
	 * @see ContinuousAction#setService(DataService, User)
	 */
	public void setService(DataService service, User user) {
		DataServiceBean serviceBean = new DataServiceBean();
		serviceBean.setService(service, user);
		this.service = serviceBean;
	}

	/**
	 * @see ContinuousAction#setService(DataServiceBean)
	 */
	public void setService(ActionPerformer service) {
		this.service = service;
	}

	/**
	 * Initialization method. Simply returns true
	 */
	public boolean beforeMainAction() throws DataException, ServiceException {
		return true;
	}

	/**
	 * Finalization method.
	 * Initializes {@link Material#setData(java.io.InputStream) data} property of
	 * {@link Material} object created during execution of this action
	 */
	public void afterMainAction(Object result) throws DataException, ServiceException {
		if(result==null){
			throw new IllegalArgumentException("result cannot be null!");
		}
		Material file = (Material) result;
		file.setData(new DownloadFileStream(file, service));
	}

	/**
	 * Gets version of material to be downloaded.
	 * If returned value is equal to {@link Material#CURRENT_VERSION} then
	 * current version will be used. Otherwise file from {@link com.aplana.dbmi.model.CardVersion} with
	 * given {@link com.aplana.dbmi.model.CardVersion#getVersion() version number} will be downloaded.
	 * @return version of material to be downloaded
	 */
	public int getVersionId() {
		return versionId;
	}

	/**
	 * Sets version of material to be downloaded.
	 * By default it is equals to {@link Material#CURRENT_VERSION} which means that current version
	 * of file is required.
	 * @param versionId version of material to be downloaded
	 */
	public void setVersionId(int versionId) {
		this.versionId = versionId;
	}
}
