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
package com.aplana.dbmi.module.notif;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.DatedTypedCardLinkAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
/*
 * ������������ ��� ����������� ������� �� ����� ����������������:
 * ������� ���������� ����� ������������ � ������ ������� � ���� ����������, �������� ������������.
 */
public class ChangeConsiderationDeadlineRecipientGroup extends
		DataServiceClient implements RecipientGroup {

	private static final ObjectId EXAM_PERSON_ATTR_ID = ObjectId.predefined(PersonAttribute.class, "jbr.exam.person");

	private ObjectId prevConsidirationStateAttrId = ObjectId.predefined(DatedTypedCardLinkAttribute.class, "jbr.request.prev");
	
	private ObjectId newConsidirationStateAttrId = ObjectId.predefined(DatedTypedCardLinkAttribute.class, "jbr.request.new");
	
	private ObjectId changeConsDeadlineId = ObjectId.predefined(ReferenceValue.class, "jbr.considerator.change.term");
	
	private ObjectId requestTypeAttrId = ObjectId.predefined(ListAttribute.class, "jbr.request.type");
	private ObjectId requestCahngeDateAttrId = ObjectId.predefined(DateAttribute.class, "jbr.request.change");
	
	
	public Collection discloseRecipients(NotificationObject object) {
		Card card = ((SingleCardNotification) object).getCard();
		DatedTypedCardLinkAttribute prevConsidirationState = card.getAttributeById(prevConsidirationStateAttrId);
		DatedTypedCardLinkAttribute normalizedPrevConsidirationState;
		try {
			normalizedPrevConsidirationState = normalizePrevConsidirationAttr(prevConsidirationState);
		} catch (DataException e1) {
			logger.error("Error normalize PrevConsidirationState");
			return new ArrayList<Person>();
		}
		DatedTypedCardLinkAttribute newConsidirationState = card.getAttributeById(newConsidirationStateAttrId);
		ListAttribute changeConsDeadline = card.getAttributeById(requestTypeAttrId);

		List<ObjectId> personCardIds = new ArrayList<ObjectId>();
		
		if(changeConsDeadlineId.equals(changeConsDeadline.getValue().getId())){
			//�������� ����������� ��� "��������� ����� ������������" ������, �.�. ��������� ����������� �� ��������� �������. 
			/*DateAttribute requestChangeDate = card.getAttributeById(requestCahngeDateAttrId);
			if((normalizedPrevConsidirationState.getCardDate(normalizedPrevConsidirationState.getSingleLinkedId())!= null
					&& !DateUtils.isSameDay(normalizedPrevConsidirationState.getCardDate(normalizedPrevConsidirationState.getSingleLinkedId()),requestChangeDate.getValue()))){
				personCardIds.add(normalizedPrevConsidirationState.getSingleLinkedId());
			}*/
		} else {
			
			for(ObjectId id: normalizedPrevConsidirationState.getIdsLinked()){
				if ((newConsidirationState.getCardDate(id)!=null && normalizedPrevConsidirationState.getCardDate(id)!=null
						&& !DateUtils.isSameDay(newConsidirationState.getCardDate(id), normalizedPrevConsidirationState.getCardDate(id)))){
					personCardIds.add(id);
				}
			}
		}
		try {
			return CardUtils.getPersonsByCards(personCardIds, getQueryFactory(), getDatabase(), 
					getSystemUser());
		} catch (DataException e) {
			logger.error("Error fetching person cards");
			return new ArrayList<Person>();
		}
	}
	//������ id �������� ������������ �� id �������� ��������������� � ���� �������������
	private DatedTypedCardLinkAttribute normalizePrevConsidirationAttr(DatedTypedCardLinkAttribute attr) throws DataException{
		Search search = new Search();
		search.setByAttributes(false);
		search.setByCode(true);
		search.setWords(attr.getLinkedIds());
		
		search.setColumns(CardUtils.createColumns(EXAM_PERSON_ATTR_ID));
		SearchResult result = (SearchResult)execAction(search);
		
		DatedTypedCardLinkAttribute  normalizedAttr = new DatedTypedCardLinkAttribute();
		for(Card c: result.getCards()){
			ObjectId personCardId = ((PersonAttribute)c.getAttributeById(EXAM_PERSON_ATTR_ID)).getPerson().getCardId();
			normalizedAttr.addLinkedId(personCardId);
			normalizedAttr.addTypeDate((Long)personCardId.getId(), 
					(Long)attr.getType((Long)c.getId().getId()), attr.getDate((Long)c.getId().getId()));
		}
		return normalizedAttr;
		
	}
	
	private Object execAction(Action action) throws DataException {
		final ActionQueryBase query = getQueryFactory().getActionQuery(action);
		query.setAction(action);
		return getDatabase().executeQuery(getSystemUser(), query);
	}

}
