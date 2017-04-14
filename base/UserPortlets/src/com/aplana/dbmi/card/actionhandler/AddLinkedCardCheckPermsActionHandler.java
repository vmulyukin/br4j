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
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.card.CardPortlet;
import com.aplana.dbmi.card.CardPortletCardInfo;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.AsyncDataServiceBean.ExecuteOption;

public class AddLinkedCardCheckPermsActionHandler extends
		AddLinkedCardImmediateActionHandler {

	public static final String PARAM_CARDLINKATTR_ID = "cardLinkAttrId";
	protected ObjectId cardLinkAttrId;

	@Override
	public boolean isApplicableForUser() {
		//������� �������� �� ����������� �������� �������� ������� ������� (templateId)
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
	protected void process(Attribute attr, List cardIds, ActionRequest request,
			ActionResponse response) throws DataException {
		CardPortletCardInfo parent = getCardPortletSessionBean().getActiveCardInfo();
		ResourceBundle rb = ResourceBundle.getBundle("com.aplana.dbmi.card.actionhandler.nl.ActionHandlerResource",
				request.getLocale());
		boolean locked = false;
		try {
			if (!CardPortlet.CARD_EDIT_MODE.equals(parent.getMode())) {
				serviceBean.doAction(new LockObject(parent.getCard()));
				locked = true;
			}

			Card child = createCard();
			ObjectId childId = serviceBean.saveObject(child, ExecuteOption.SYNC);
			child.setId(((Long) childId.getId()).longValue());	//*****

			CardLinkAttribute links = null;
			if(cardLinkAttrId == null){
				links = (CardLinkAttribute) attr;
			}
			else{
				links = (CardLinkAttribute) parent.getCard().getAttributeById(cardLinkAttrId);
			}
			// ���� ������� �� ���������, ������ �������� cardLinkAttrId � ������� �������� ���
			if (links==null){
				throw new DataException(MessageFormat.format(rb.getString("AddLinkedCardCheckPermsActionHandlerErrorMsg"), child.getId().getId(), cardLinkAttrId.getId()));
			}
			links.addLinkedId(childId);

			ObjectId id = serviceBean.saveObject(parent.getCard(), ExecuteOption.SYNC);
			parent.setCard((Card) serviceBean.getById(id));
			parent.setRefreshRequired(true);

			getCardPortletSessionBean().openNestedCard(child, null, true);
		} catch (Exception e) {
			logger.error("Error creating child card", e);
			getCardPortletSessionBean().setMessage(e.getMessage());
		} finally {
			if (locked)
				try {
					serviceBean.doAction(new UnlockObject(parent.getCard()));
				} catch (Exception e) {
					logger.error("Error unlocking parent card " + parent.getCard().getId().getId(), e);
					//getCardPortletSessionBean().setMessage(e.getMessage());
				}
		}
	}

	@Override
	public void setParameter(String name, String value) {
		if(PARAM_CARDLINKATTR_ID.equals(name)){
			cardLinkAttrId = ObjectIdUtils.getObjectId(CardLinkAttribute.class, value, false);
		} else {
			super.setParameter(name, value);
		}

	}
}
