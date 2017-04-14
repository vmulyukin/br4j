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
import java.util.Collection;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import com.aplana.dbmi.card.AttributeEditor;
import com.aplana.dbmi.card.CardPortlet;
import com.aplana.dbmi.card.CardPortletCardInfo;
import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.card.CardPortletSessionBean.PortletMessage.PortletMessageType;
import com.aplana.dbmi.card.actionhandler.AddLinkedCardActionHandler;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.service.AsyncDataServiceBean;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.AsyncDataServiceBean.ExecuteOption;

/**
 * ����� ������������ �������� ������ �������� �� �������
 * 
 * @author dstarostin
 *
 */

public class GeneratedCardsAddHandler extends AddLinkedCardActionHandler {

	/**
	 * ��� ��������, �������� �������� ������� ��������������� ����� ��������� ����������
	 */
	public static final String PARAM_SPECIAL_ATTRIBUTE = "special_attribute";
	/**
	 * ��������� ������, �� ������� ������� ���������� ��������� ��������
	 */
	public static final String PARAM_BUTTON_TITLE = "button_title";
	
	private ObjectId specAttrId;
	private String buttonTitle;

	/**
	 * ��������� ��� ���������� �������� ������ ��������������� ��������������� ��������
	 */
	private Collection<ObjectId> storedCardIds;
	
	private GenerateCardsHandler handler;
	

	/**
	 * ���������� �������� �������� ��������������
	 * ��������� � ������������ �������� ������ �� ��������������� ��������
	 *
	 */
	protected class NewCardLinkItemCloseHandler implements CardPortletCardInfo.CloseHandler {
		private ObjectId cardLinkId;
		public NewCardLinkItemCloseHandler(ObjectId cardLinkId) {
			this.cardLinkId = cardLinkId;
		}
		
		public void afterClose(CardPortletCardInfo closedCardInfo, CardPortletCardInfo previousCardInfo) {
			boolean first = true;
			for (ObjectId newCardId: storedCardIds) {
				CardLinkAttribute attr = (CardLinkAttribute)previousCardInfo.getCard().getAttributeById(cardLinkId);
				if (first) {
					first = false;
					previousCardInfo.setAttributeEditorData(cardLinkId, AttributeEditor.KEY_VALUE_CHANGED, Boolean.TRUE);
					if (!attr.isMultiValued()) {
						attr.addSingleLinkedId(newCardId);
						break;
					}
				}
				attr.addLinkedId(newCardId);
			}
		}
	}
	
	protected class GenerateCardsHandler implements CardPortletCardInfo.CustomStoreHandler {
		private CardPortletSessionBean session;
		
		public GenerateCardsHandler(CardPortletSessionBean session) {
			this.session = session;
		}
		public String getStoreButtonTitle() {
			return buttonTitle;
		}

		public void storeCard() throws DataException {
			Card card = session.getActiveCard();
			AsyncDataServiceBean service = session.getServiceBean();
			try {
				PersonAttribute spec = (PersonAttribute)card.getAttributeById(specAttrId);
				Collection<Person> singlePersonList = new ArrayList<Person>(1);
				storedCardIds = new ArrayList<ObjectId>();
				for (Person person: (Collection<Person>)spec.getValues()) {
					singlePersonList.clear();
					singlePersonList.add(person);
					spec.setValues(singlePersonList);
					storedCardIds.add(service.saveObject(card, ExecuteOption.SYNC));
					card.clearId();
				}
			} catch (ClassCastException e) {
				throw new DataException(e.getMessage(), e);
			} catch (ServiceException e) {
				throw new DataException(e.getMessage(), e);
			} 
		}

		public ObjectId getSpecialAttributeId() {
			return specAttrId;
		}
		
		public String getCloseActionString() {
			return CardPortlet.CLOSE_EDIT_MODE_ANYWAY_ACTION;
		}
	}
	
	@Override
	protected Card createCard() throws DataException, ServiceException {
		Card card = super.createCard();
		try {
			if (handler != null) {
				PersonAttribute attribute = 
					(PersonAttribute)card.getAttributeById(handler.getSpecialAttributeId());
				attribute.setMultiValued(true);				
			}
		} catch (ClassCastException e) {
			throw new DataException(e);
		}
		return card; 
	}

	@Override
	protected void process(Attribute attr, List cardIds, ActionRequest request,
			ActionResponse response) throws DataException {
		if (specAttrId == null)
			throw new DataException("Parameter \"" + PARAM_SPECIAL_ATTRIBUTE + "\" required");
		if (buttonTitle == null)
			throw new DataException("Parameter \"" + PARAM_BUTTON_TITLE + "\" required");
    	CardPortletSessionBean sessionBean = getCardPortletSessionBean();
    	handler = new GenerateCardsHandler(sessionBean);
    	try {
	    	sessionBean.openNestedCard(
	    			createCard(),
	    			new NewCardLinkItemCloseHandler(attr.getId()),
	    			true
	    	);		
	    	sessionBean.getActiveCardInfo().setStoreHandler(handler);
		} catch (Exception e) {
			logger.error("Can't redirect to card editing page", e);
			sessionBean.setMessageWithType("edit.link.error.create", new Object[] { e.getMessage() }, PortletMessageType.ERROR);
		}
	}

	@Override
	public void setParameter(String name, String value) {
		if (PARAM_SPECIAL_ATTRIBUTE.equals(name)) {
			specAttrId = ObjectId.predefined(PersonAttribute.class, value);
		} else if (PARAM_BUTTON_TITLE.equals(name)) {
			buttonTitle = value;
		} else {
			super.setParameter(name, value);			
		}
	}
	
}
