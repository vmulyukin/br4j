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

import java.util.Collection;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.card.CardPortletCardInfo;
import com.aplana.dbmi.card.Parametrized;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.support.action.CreateResolution;

public class AddLinkedCardImmediateActionHandler extends CardPortletAttributeEditorActionHandler
		implements Parametrized
{
	public static final String PARAM_TEMPLATE_ID = "template";
	protected static final String PARAM_STATES_ALLOWED = "statesAllowed";
	protected static final String PARAM_STATES_NOT_ALLOWED = "statesNotAllowed";

	/*private class Undo implements CardPortletCardInfo.CloseHandler
	{
		public void afterClose(CardPortletCardInfo closedCardInfo,
				CardPortletCardInfo previousCardInfo) {
			if (!closedCardInfo.isRefreshRequired()) {
				//*****
			}
		}
	}*/

	protected ObjectId templateId;
	protected Collection<ObjectId> statesAllowed;
	protected Collection<ObjectId> statesNotAllowed;

	@Override
	protected void process(Attribute attr, List<ObjectId> cardIds, ActionRequest request,
			ActionResponse response) throws DataException {
		CardPortletCardInfo parent = getCardPortletSessionBean().getActiveCardInfo();
		//boolean locked = false;
		try {
			/*if (!CardPortlet.CARD_EDIT_MODE.equals(parent.getMode())) {
				serviceBean.doAction(new LockObject(parent.getCard()));
				locked = true;
			}*/

			CreateResolution createRes = new CreateResolution(parent.getCard(), templateId, attr);
			ObjectId childId = serviceBean.doAction(createRes);
			Card child = serviceBean.getById(childId);
			parent.setRefreshRequired(true);
			
			getCardPortletSessionBean().openNestedCard(child, null, true);
		} catch (Exception e) {
			logger.error("Error creating child card", e);
			getCardPortletSessionBean().setMessage(e.getMessage());
		} /*finally {
			if (locked)
				try {
					serviceBean.doAction(new UnlockObject(parent.getCard()));
				} catch (Exception e) {
					logger.error("Error unlocking parent card " + parent.getCard().getId().getId(), e);
					//getCardPortletSessionBean().setMessage(e.getMessage());
				}
		}*/
	}

	protected Card createCard() throws Exception {
		CreateCard createCard = new CreateCard();
		createCard.setTemplate(templateId);
		createCard.setLinked(true);
		// �� �������� � AddLinkedCardActionHandler
		createCard.setParent(getCardPortletSessionBean().getActiveCard()); // op, oppa gangnam style
		return serviceBean.doAction(createCard);
	}

	public void setParameter(String name, String value) {
		if (PARAM_TEMPLATE_ID.equals(name)) {
			templateId = ObjectIdUtils.getObjectId(Template.class, value, true);
		} else  if (PARAM_STATES_ALLOWED.equalsIgnoreCase(name)){
			statesAllowed = ObjectIdUtils.commaDelimitedStringToNumericIds(value, CardState.class);
		} else  if (PARAM_STATES_NOT_ALLOWED.equalsIgnoreCase(name)){
			statesNotAllowed = ObjectIdUtils.commaDelimitedStringToNumericIds(value, CardState.class);
		}
	}
	
	@Override
	public boolean isApplicableForUser() {
		try {
			Card card = getCardPortletSessionBean().getActiveCard();
			if(getAttribute() instanceof BackLinkAttribute && card.getId() == null){
				return false;
			} 
			boolean isStatesAllowed = ((statesAllowed == null || statesAllowed.contains(card.getState()))
					&& (statesNotAllowed == null || !statesNotAllowed.contains(card.getState())) );
			if(!isStatesAllowed)
				return false;
			CreateCard action = new CreateCard();
			action.setTemplate(templateId);
			return serviceBean.canDo(action);
		} catch (Exception e) {
			logger.error("Exception caught while checking user permissions for template", e);
			return false;
		}
	}
}
