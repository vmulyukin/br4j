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
package com.aplana.dbmi.card.actionhandler;

import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.card.CardPortletCardInfo;
import com.aplana.dbmi.card.Parametrized;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.AsyncDataServiceBean.ExecuteOption;
import com.aplana.dbmi.service.DataException;

public class LinkToCreatedCardCheckPermsActionHandler extends CardPortletAttributeEditorActionHandler implements
		Parametrized {

	public static final String PARAM_CARDLINKATTR_ID = "cardLinkAttrId";
	public static final String PARAM_TEMPLATE_ID = "template";

	protected ObjectId templateId;
	protected ObjectId cardLinkAttrId;

	@Override
	public boolean isApplicableForUser() {
		CreateCard createAction;
		try {
			createAction = new CreateCard(this.templateId);
			return serviceBean.canDo(createAction);
		} catch (Exception e) {
			logger.info("Problem with check permissions", e);
		}
		return true;
	}

	@Override
	protected void process(Attribute attr, List cardIds, ActionRequest request, ActionResponse response)
			throws DataException {
		CardPortletCardInfo cardInfo = getCardPortletSessionBean().getActiveCardInfo();
		ResourceBundle rb = ResourceBundle.getBundle("com.aplana.dbmi.card.actionhandler.nl.ActionHandlerResource",
				request.getLocale());

		try {
			Card child = createCard();
			CardLinkAttribute links = null;
			links = (CardLinkAttribute) child.getAttributeById(cardLinkAttrId);

			if (links == null) {
				throw new DataException(MessageFormat.format(
						rb.getString("AddLinkedCardCheckPermsActionHandlerErrorMsg"), child.getId().getId(),
						cardLinkAttrId.getId()));
			}
			links.addLinkedId(cardInfo.getCard().getId());

			ObjectId childId = serviceBean.saveObject(child, ExecuteOption.SYNC);
			child.setId(((Long) childId.getId()).longValue());

			cardInfo.setCard((Card) serviceBean.getById(cardInfo.getCard().getId()));
			cardInfo.setRefreshRequired(true);
			getCardPortletSessionBean().openNestedCard(child, null, true);
		} catch (Exception e) {
			logger.error("Error creating child card", e);
			getCardPortletSessionBean().setMessage(e.getMessage());
		}
	}

	protected Card createCard() throws Exception {
		CreateCard createCard = new CreateCard();
		createCard.setTemplate(templateId);
		createCard.setLinked(true);
		createCard.setParent(getCardPortletSessionBean().getActiveCard());
		return (Card) serviceBean.doAction(createCard);
	}

	public void setParameter(String name, String value) {
		if (PARAM_CARDLINKATTR_ID.equals(name)) {
			cardLinkAttrId = ObjectIdUtils.getObjectId(CardLinkAttribute.class, value, false);
		} else if (PARAM_TEMPLATE_ID.equals(name)) {
			templateId = ObjectIdUtils.getObjectId(Template.class, value, true);
		}
	}


}
