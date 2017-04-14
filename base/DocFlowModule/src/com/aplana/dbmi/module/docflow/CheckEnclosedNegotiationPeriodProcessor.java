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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.ObjectAction;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.NeedConfirmationException;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.ParametrizedProcessor;

/**
 * ����� ���������� 'ignoreStates' �������� ������ ������������ ��������
 * �� ����������� � ���� ������� ����� �������.
 * ��������, draft,execution,delo
 * @author desu
 */
public class CheckEnclosedNegotiationPeriodProcessor extends ParametrizedProcessor {
	private static final long serialVersionUID = 2L;
	public static final String IGNORE_STATES = "ignoreStates";	
	private List<ObjectId> ignoreStateList = new ArrayList<ObjectId>();
	
	//����: ������� ������������
	static final ObjectId orderAttrId = ObjectId.predefined(IntegerAttribute.class, "jbr.visa.order");
	//����: ���� �����������
	static final ObjectId negotiationPeriodAttrId = ObjectId.predefined(IntegerAttribute.class, "jbr.visa.negotiation_period");
	//����: �������������� �����������
	static final ObjectId enclosedSetAttrId = ObjectId.predefined(CardLinkAttribute.class, "jbr.visa.enclosedSet");

	@Override
	public Object process() throws DataException {
		ChangeState changeStateAction = (ChangeState)getAction();
		if (!changeStateAction.isLastDialogOk()) {
			final Card visaCard = loadCard(getCardId());
			final CardLinkAttribute enclosedSetAttr = visaCard.getAttributeById(enclosedSetAttrId);
			final IntegerAttribute visaNegotiationPeriod = visaCard.getAttributeById(negotiationPeriodAttrId);
			HashMap<Integer, Integer> orderPeriodMap = new HashMap<Integer, Integer>();
			if (enclosedSetAttr.getLinkedCount() > 0) {
				for(Iterator<ObjectId> iterator = enclosedSetAttr.getIdsLinked().iterator(); iterator.hasNext();){					
					Card childVisa =  loadCard(iterator.next());
					if (ignoreStateList.contains(childVisa.getState())) {					
						continue;
					}
					IntegerAttribute orderAttr = childVisa.getAttributeById(orderAttrId);
					IntegerAttribute negotiationPeriodAttr = childVisa.getAttributeById(negotiationPeriodAttrId);					
					if (orderPeriodMap.containsKey(orderAttr.getValue())) {
						int oldOrderPeriod = orderPeriodMap.get(orderAttr.getValue());
						if (oldOrderPeriod < negotiationPeriodAttr.getValue()) {
							orderPeriodMap.put(orderAttr.getValue(), negotiationPeriodAttr.getValue());
						}						
					} else {
						orderPeriodMap.put(orderAttr.getValue(), negotiationPeriodAttr.getValue());
					}
				}
				int enclosedNegotiationPeriod = 0;
				for (Iterator<Integer> iterator = orderPeriodMap.keySet().iterator(); iterator.hasNext();) {
					enclosedNegotiationPeriod += orderPeriodMap.get(iterator.next()); 
				}
				if (visaNegotiationPeriod.getValue() < enclosedNegotiationPeriod) {
					throw new NeedConfirmationException("negotiation.enclosedPeriod.confirmation", new Object[] {}, "negotiation.enclosedPeriod.confirmation.title");
				}
			}
		}
		return null;
	}

	private ObjectId getCardId() {
		if (getObject() != null) {
			return getObject().getId();
		}
		Action action = getAction(); 

		if (action instanceof ChangeState) {
			return ((ChangeState) action).getObjectId();
		} if (action instanceof ObjectAction) {
			ObjectAction objectAction = (ObjectAction)action;
			if (objectAction.getObjectId().getType().equals(Card.class)) {
				return objectAction.getObjectId();	
			}
		}
		return null;
	}
	
	private Card loadCard(ObjectId cardId) throws DataException {
		final ObjectQueryBase cardQuery = getQueryFactory().getFetchQuery(Card.class);
		cardQuery.setAccessChecker(null);
		cardQuery.setId(cardId);
		return getDatabase().executeQuery(getSystemUser(), cardQuery);
	}	
	
	@Override
	public void setParameter(String name, String value) {
		if (name == null || value == null)
			return;		
		if (IGNORE_STATES.equalsIgnoreCase(name)) {
			ignoreStateList = IdUtils.stringToAttrIds(value, CardState.class);				
		} else {
			super.setParameter(name, value);
		}
	}	
}