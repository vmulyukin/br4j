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
package com.aplana.distrmanager.handlers;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.distrmanager.exceptions.DocBaseException;

public class CheckDocBase {

	private final Log logger = LogFactory.getLog(getClass());

	private static final String BACKLINK_DOCBASE_ERROR = "jbr.DistributionManager.CheckDocBase.notFound.backLinkDocBase";
	private static final String DOCBASE_ERROR = "jbr.DistributionManager.CheckDocBase.notFound.outcomeCard";

	private ObjectId docBaseId = null;
	private DataServiceFacade serviceBean;

	private CheckDocBase(DataServiceFacade serviceBean) {
		this.serviceBean = serviceBean;
	}

	public static CheckDocBase instance(DataServiceFacade serviceBean) {
		return new CheckDocBase(serviceBean);
	}

	public Card handle(Card elmCard, ObjectId backLinkDocBase) throws DocBaseException {
		Exception exDocBase;
		try {
			// �������� ���-���������
			if (backLinkDocBase == null){
				exDocBase = new DataException(BACKLINK_DOCBASE_ERROR);
				logError(elmCard, BACKLINK_DOCBASE_ERROR, exDocBase);
				throw exDocBase;
			}
			ListProject fetcher = new ListProject();
			fetcher.setAttribute(backLinkDocBase);
			fetcher.setCard(elmCard.getId());
			List<Card> foundCards = ((SearchResult) serviceBean.doAction(fetcher)).getCards();
			if (foundCards.size() != 1) {
				throw new DataException(DOCBASE_ERROR);
			}
			return (Card) serviceBean.getById(foundCards.get(0).getId());
		} catch(Exception exDocbase) {
			exDocBase = exDocbase;
			logError(elmCard, DOCBASE_ERROR, exDocBase);
			// ���� �� ������� �� ������ ��������� ���������, �������� ������� ��������� "�������� ������ ��������"
			throw new DocBaseException(exDocBase);
		}
	}

	private void logError(Card elmCard, String msgError, Exception e) {
		String error = String.
			format("{%s} docBaseId: {%s}; elmId: {%s};",
					(null == msgError)?"null":msgError,
					(null == docBaseId)?"null":docBaseId.getId(),
					(null == elmCard)?"null":elmCard.getId().getId()
			);
			logger.error(error, e);

	}
}
