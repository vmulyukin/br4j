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
package com.aplana.dbmi.card.download.actionhandler;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.action.UploadFile;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

/**
 * Uploads file, creates a card and uploads file into it. Outputs card_id of
 * created card or (-1 on error). Analog to CardUploadAction - same params, but
 * no import, just simple upload.
 * 
 * @author aminnekhanov
 * 
 */
public class FileUploadAction extends UploadAction {

	public final static ObjectId ID_TEMPLATE_FILE = ObjectId.predefined(
			Template.class, "jbr.file");

	private static final String CARD_ID_PARAM = "cardId";

	private ObjectId cardId = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.aplana.dbmi.card.download.actionhandler.UploadAction#process(javax
	 * .servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void process(HttpServletRequest request, HttpServletResponse response)
			throws DataException {
		long cardIdValue = getLongValueOfParameter(request, CARD_ID_PARAM);
		if (cardIdValue != -1) {
			cardId = new ObjectId(Card.class, cardIdValue);
		}
		super.process(request, response);
	}

	private long getLongValueOfParameter(HttpServletRequest request, String name)
			throws DataException {
		String paramValue = request.getParameter(name);
		if (paramValue != null) {
			try {
				return Long.parseLong(paramValue);
			} catch (NumberFormatException ex) {
			}
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.aplana.dbmi.card.download.actionhandler.UploadAction#processFile(
	 * java.io.InputStream, java.lang.String)
	 */
	@Override
	protected String processFile(InputStream sourceStream, String fileName,
			HttpServletRequest request) throws DataException {
		DataServiceBean dataServiceBean = getServiceBean();
		dataServiceBean.setSessionId(request.getSession().getId());
		try {
			if (cardId == null) {
				CreateCard createCard = new CreateCard(ID_TEMPLATE_FILE);
				Card card = (Card) dataServiceBean.doAction(createCard);
				cardId = card.getId();
				logDebugMessage("Created card id: " + cardId
						+ "; uploading file into it.");
			} else {
				LockObject lock = new LockObject(cardId);
				dataServiceBean.doAction(lock);
			}
			// dataServiceBean.saveObject(card);

			UploadFile uploadFile = new UploadFile();
			uploadFile.setCardId(cardId);
			uploadFile.setFileName(fileName);
			uploadFile.setData(sourceStream);
			dataServiceBean.doAction(uploadFile);
			return String.valueOf(cardId);
		} catch (ServiceException ex) {
			throw new DataException(ex);
		} finally {
			if (cardId != null) {
				try {
					UnlockObject unlockAction = new UnlockObject(cardId);
					dataServiceBean.doAction(unlockAction);
				} catch (ServiceException ex) {
					throw new DataException(ex);
				}
			}
		}
	}
}
