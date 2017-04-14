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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.OverwriteCardAttributes;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.card.AttributeEditor;
import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.card.actionhandler.CardPortletAttributeEditorActionHandler;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.AsyncDataServiceBean.ExecuteOption;

import edu.emory.mathcs.backport.java.util.Collections;

public class ReservationRequestCreateHandler extends CardPortletAttributeEditorActionHandler{

	public static final ObjectId reservationRequestTemplateId = ObjectId.predefined(
			Template.class, "jbr.reservationRequest");
	public static final ObjectId reservationRequestsAttrId = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.doc.reservationRequests");
	public static final ObjectId numberAttrId = ObjectId.predefined(
			StringAttribute.class, "regnumber");
	public static final ObjectId beforeRegistrationStatusId = ObjectId.predefined(
			CardState.class, "before-registration");
	public static final ObjectId requestPublishedStatusId = ObjectId.predefined(
			CardState.class, "jbr.reservationRequest.published");

	@Override
	protected void process(Attribute attr, List<ObjectId> cardIds,
			ActionRequest request, ActionResponse response)
			throws DataException {
		CardPortletSessionBean sessionBean = getCardPortletSessionBean();
		ObjectId requestId = createReservationRequest();
		CardLinkAttribute requests = (CardLinkAttribute) sessionBean.getActiveCard().getAttributeById(reservationRequestsAttrId);
		requests.addLinkedId(requestId);
		try{
			//��������� CardLink � �������� ��������, ����� �������� BackLink � ��������������
			OverwriteCardAttributes action = new OverwriteCardAttributes();
			action.setAttributes(Collections.singleton(requests));
			action.setCardId(sessionBean.getActiveCard().getId());
			serviceBean.doAction(action);
		} catch(ServiceException e){throw new DataException(e);}
		sessionBean.getActiveCardInfo().setAttributeEditorData(reservationRequestsAttrId, AttributeEditor.KEY_VALUE_CHANGED, Boolean.TRUE);
	}
	
	@Override
	public boolean isApplicableForUser() {
		boolean result = true;
		try {
			CardPortletSessionBean sessionBean = getCardPortletSessionBean();
			StringAttribute number = (StringAttribute) sessionBean.getActiveCard().getAttributeById(numberAttrId);
			if (number.getStringValue() != null && number.getStringValue().length() > 0) {
				result = false;
			}
			if (result) {
				result = !beforeRegistrationStatusId.equals(sessionBean.getActiveCard().getState());
			}
			if (result) {
				CardLinkAttribute requests = (CardLinkAttribute) sessionBean.getActiveCard().getAttributeById(reservationRequestsAttrId);
				Search search = new Search();
				search.setByCode(true);
				search.setWords(requests.getLinkedIds());
				List columns = new ArrayList();
				SearchResult.Column col = new SearchResult.Column();
				col.setAttributeId(new ObjectId(IntegerAttribute.class, "_STATE"));
				columns.add(col);
				search.setColumns(columns);
				SearchResult searchResult = (SearchResult) sessionBean.getServiceBean().doAction(search);
				List requestCards = searchResult.getCards();
				for (int i = 0 ; i < requestCards.size(); i++) {
					if (requestPublishedStatusId.equals(((Card) requestCards.get(i)).getState())) {
						result = false;
					}
				}
			}
			if (result) {
				CreateCard action = new CreateCard();
				action.setTemplate(reservationRequestTemplateId);
				result = sessionBean.getServiceBean().canDo(action);
			}
		} catch (Exception e) {
			logger.error("Exception caught while checking user permissions for template", e);
			result = false;
		}
		return result;
	}

	protected ObjectId createReservationRequest() throws DataException {
		ObjectId result = null;
		CardPortletSessionBean sessionBean = getCardPortletSessionBean();
		CreateCard createCardAction = new CreateCard();
		createCardAction.setTemplate(reservationRequestTemplateId);
		try {
			Card card = (Card) sessionBean.getServiceBean().doAction(createCardAction);
			result = sessionBean.getServiceBean().saveObject(card, ExecuteOption.SYNC);
			sessionBean.getServiceBean().doAction(new UnlockObject(result));
		} catch (ServiceException e) {
			throw new DataException("Cant create reservation request", e);
		}
		return result;
	}

}
