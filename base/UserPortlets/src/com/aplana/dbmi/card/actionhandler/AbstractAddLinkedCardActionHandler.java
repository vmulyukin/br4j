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

import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import com.aplana.dbmi.card.AttributeEditor;
import com.aplana.dbmi.card.CardPortletCardInfo;
import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.card.CardPortletSessionBean.PortletMessage.PortletMessageType;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;

public abstract class AbstractAddLinkedCardActionHandler
		extends CardPortletAttributeEditorActionHandler
{
	
	protected static class NewCardLinkItemCloseHandler implements CardPortletCardInfo.CloseHandler {
		private ObjectId cardLinkId;
		public NewCardLinkItemCloseHandler(ObjectId cardLinkId) {
			this.cardLinkId = cardLinkId;
		}
		
		public void afterClose(CardPortletCardInfo closedCardInfo, CardPortletCardInfo previousCardInfo) {
			ObjectId newCardId = closedCardInfo.getCard().getId(); 
			if (newCardId != null) {
				CardLinkAttribute attr = (CardLinkAttribute)previousCardInfo.getCard().getAttributeById(cardLinkId);
				if (attr.isMultiValued()) {
					attr.addLinkedId(newCardId);
				} else { // �������� ������ ���� ��������
					attr.addSingleLinkedId(newCardId);
				}				
				previousCardInfo.setAttributeEditorData(cardLinkId, AttributeEditor.KEY_VALUE_CHANGED, Boolean.TRUE);
			}
		}
	}

	@Override
	protected void process(Attribute attr, List<ObjectId> cardIds, ActionRequest request, ActionResponse response)
			throws DataException {
    	CardPortletSessionBean sessionBean = getCardPortletSessionBean();
    	try {
	    	sessionBean.openNestedCard(
	    			createCard(),
	    			new NewCardLinkItemCloseHandler(attr.getId()),
	    			true
	    	);			
		} catch (Exception e) {
			logger.error("Can't redirect to card editing page", e);
			sessionBean.setMessageWithType("edit.link.error.create", new Object[] { e.getMessage() }, PortletMessageType.ERROR);
		}
	}

	abstract protected Card createCard() throws Exception;
}
