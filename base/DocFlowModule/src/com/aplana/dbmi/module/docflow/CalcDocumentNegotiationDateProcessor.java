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
package com.aplana.dbmi.module.docflow;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.ParametrizedProcessor;

public class CalcDocumentNegotiationDateProcessor extends ParametrizedProcessor {
	private static final long serialVersionUID = 2L;
	static final ObjectId planNegotiationDateId = ObjectId.predefined(DateAttribute.class, "jbr.plan_negotiation_date"); 
	static final ObjectId documentBackLinkId = ObjectId.predefined(BackLinkAttribute.class, "jbr.visa.parent");
	static final String UPDATE_ATTR_PARAM = "updatePlanDate";
	
	public Object process() throws DataException {
		
		ObjectId documentId = getDocumentId();
		//���� �� ������� �������� ������������� ���������, �� ��������� �����, � ��������� ������ �� ��� ��� ���
		//� ������������� ������ �� ����. �������� ���������� ��� ���������� ���������.
		if (documentId != null){
			CalcNegotiationDate action = new CalcNegotiationDate(documentId);
			// ���� � ���������� ������, ��� �������� ���� ������������� �� ����, �� ������� �� ���� ������
			action.setUpdatePlanDate(getBooleanParameter(UPDATE_ATTR_PARAM, true));
			execAction(action);
		}
		return null;
	}
	
	private ObjectId getDocumentId() throws DataException {
		ObjectId result = null;
		
		ChangeState move = (ChangeState) getAction();
		if (move != null){
			ObjectId objectId = move.getCard().getId();
			ObjectQueryBase query = getQueryFactory().getFetchQuery(Card.class);
			query.setId(objectId);
			Card visa = (Card)getDatabase().executeQuery(getSystemUser(), query);
			setObject(visa);
		}
		
		if (getObject() instanceof Card) {
			final BackLinkAttribute documentBackLinkAttr = (BackLinkAttribute)((Card)getObject()).getAttributeById(documentBackLinkId);
			ListProject listAction = new ListProject();
			listAction.setCard(getObject().getId());
			listAction.setAttribute(documentBackLinkAttr.getId());
			Object execResult = execAction(listAction);
			if (execResult instanceof SearchResult) {
				SearchResult searchResult = (SearchResult)execResult;
				//�������� �� ������� ���������� � ���������. ���� ��� �� ������ ����� ��������� ��������
				//��� ���������� ���������
				if (searchResult.getCards().size() > 0){
					Card document = (Card)searchResult.getCards().get(0);
					result = document.getId();
				}
			}
		}
		return result;
	}
	
	private Object execAction(Action action) throws DataException {
		final ActionQueryBase query = getQueryFactory().getActionQuery(action);
		query.setAction(action);
		return getDatabase().executeQuery(getSystemUser(), query);
	}
}