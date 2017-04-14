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
package com.aplana.dbmi.card.actionhandler.multicard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.portlet.RenderRequest;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.card.CardPortlet;
import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.card.CardPortletSessionBean.PortletMessage.PortletMessageType;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.AsyncDataServiceBean.ExecuteOption;
import com.aplana.dbmi.support.action.ProcessGroupResolution;

public class GroupResolutionHandler extends SpecificCustomStoreHandler {

	public GroupResolutionHandler(CardPortletSessionBean sessionBean,
			RenderRequest request) {
		super(sessionBean, request);
	}

	private final String TEXT_TO_EXECUTION_BUTTON = "edit.page.to.execution.button";

	protected final String DOCS_PARAM = "DOCS_GROUP";

	private final static ObjectId DOC_LIST_ATTR_ID = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.doc.list");

	protected List<ObjectId> docs;

	public void storeCard() throws DataException {
		List<ObjectId> userDocs = sessionBean.getActiveCard()
				.getCardLinkAttributeById(DOC_LIST_ATTR_ID).getIdsLinked();


		try {
			for(ObjectId userDoc: userDocs){
				ProcessGroupResolution groupResolution = new ProcessGroupResolution();
				groupResolution.setCurrentResolution(sessionBean.getActiveCard());
				groupResolution.setDocs(Collections.singletonList(userDoc));
				sessionBean.getServiceBean().doAction(groupResolution);
			}
			sessionBean.setMessageWithType(sessionBean.getResourceBundle()
					.getString("edit.page.async.change.state.success.msg"),
					PortletMessageType.EVENT);
		} catch (ServiceException e) {
			e.printStackTrace();
			throw new DataException(e);
		}
	}

	public String getStoreButtonTitle() {
		return TEXT_TO_EXECUTION_BUTTON;
	}

	public String getCloseActionString() {
		return CardPortlet.CLOSE_CARD_ACTION;
	}

	@Override
	protected void preProcessCard() {
		Card templateCard = sessionBean.getActiveCard();
		CardLinkAttribute reportCards = (CardLinkAttribute) templateCard
				.getAttributeById(DOC_LIST_ATTR_ID);
		reportCards.addIdsLinked(docs);
	}

	@Override
	protected void processParameters(RenderRequest request) {
		String docString = portletService.getUrlParameter(request, DOCS_PARAM);
		for (String doc : docString.split("_")) {
			if (docs == null) {
				docs = new ArrayList<ObjectId>();
			}
			docs.add(new ObjectId(Card.class, Long.parseLong(doc)));
		}
	}

}
