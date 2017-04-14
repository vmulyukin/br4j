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
package com.aplana.dbmi.card.actionhandler.jbr;

import java.util.*;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.OverwriteCardAttributes;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.card.CardPortlet;
import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.card.Parametrized;
import com.aplana.dbmi.card.actionhandler.CardPortletAttributeEditorActionHandler;
import com.aplana.dbmi.card.delivery.SendDeliveryDispatcher;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;

public class DistributionRemoveRecipientHandler extends CardPortletAttributeEditorActionHandler implements Parametrized
{
	protected static final String PARAM_SEND_STATES_ALLOWED = "sendStatesAllowed";

	protected Collection<ObjectId> sendStatesAllowed;

	private static final ObjectId OUTCOMING_RECEIVER =
			ObjectId.predefined(CardLinkAttribute.class, "jbr.outcoming.receiver");

	protected void process(Attribute attr, List cardIds, ActionRequest request,
			ActionResponse response) throws DataException{
		CardLinkAttribute links = (CardLinkAttribute) attr;
		Card card = getCardPortletSessionBean().getActiveCard();

		CardLinkAttribute receiverLinks = card.getAttributeById(OUTCOMING_RECEIVER);
		for (Iterator itr = cardIds.iterator(); itr.hasNext(); ) {
			ObjectId id = (ObjectId) itr.next();

			// ������� ���������� �� �������� ���������� �������� ����� �������� 
			if (!links.removeLinkedId(id)) {
				logger.warn("Card " + id.getId() + " is not linked to this card by attribute " + links.getId().getId());
			}
			// ������� ����� �� ���������� �� �������� ����������� ����������
			if (!receiverLinks.removeLinkedId(id)) {
				logger.warn("Card " + id.getId() + " is not linked to this card by attribute " + OUTCOMING_RECEIVER.getId());
			}
		}

		if(CardPortlet.CARD_VIEW_MODE.equals(getCardPortletSessionBean().getActiveCardInfo().getMode())){
			OverwriteCardAttributes overwriteCardAttributes = new OverwriteCardAttributes();
			overwriteCardAttributes.setCardId(card.getId());
			List<Attribute> attrsToSave = new LinkedList<Attribute>();
			attrsToSave.add(links);
			attrsToSave.add(receiverLinks);
			overwriteCardAttributes.setAttributes(attrsToSave);
			try{
				if(getCardPortletSessionBean().getServiceBean().canChange(card.getId())) {
					getCardPortletSessionBean().getServiceBean().doAction(new LockObject(card.getId()));
				} else {
					logger.error("Cant modify card " + card.getId().getId());
					throw new DataException("general.access");
				}
			} catch (ServiceException e) {
				logger.error("Error getting service",e);
				throw new DataException(e);
			}
			try {
				getCardPortletSessionBean().getServiceBean().doAction(overwriteCardAttributes);
			} catch (ServiceException e) {
				logger.error("Error getting service",e);
				throw new DataException(e);
			} finally {
				try {
					getCardPortletSessionBean().getServiceBean().doAction(new UnlockObject(card.getId()));
				} catch (ServiceException e) {
					logger.error("Error getting service",e);
					throw new DataException(e);
				}
			}
		}
		getCardPortletSessionBean().getActiveCardInfo().setRefreshRequired(true);
	}
	
	@Override
	public boolean isApplicableForUser() {
		final Card card = getCardPortletSessionBean().getActiveCard();
		final ListAttribute sendingState = card.getAttributeById(SendDeliveryDispatcher.SENDING_STATE_ATTR);
		boolean isAllowedSendState = (sendStatesAllowed == null || sendStatesAllowed.contains(sendingState.getValue().getId()));
		
		if(!isAllowedSendState)
			return false;
		
		return super.isApplicableForUser();
	}
	
	@Override
	public void setParameter(String name, String value) {
		if(PARAM_SEND_STATES_ALLOWED.equalsIgnoreCase(name)){
			sendStatesAllowed = ObjectIdUtils.commaDelimitedStringToIds(value, ReferenceValue.class);
		}
	}
}
