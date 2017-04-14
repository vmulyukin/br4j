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
package com.aplana.dbmi.jbr.processors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.UserData;

/**
 * @comment
 * ����� ��� ����������� �������� ������ ��������� �� ������ �������� ����� � ������� ��������
 **/
public class CopyPersonsFromFarParent extends AbstractCopyPersonProcessor {

	private static final long serialVersionUID = 1L;

	private ObjectId backLinkAttrId;	// backlink-������� ��� ���������� �����
	private ObjectId refLinkAttrId;		// cardlink-������ �� ������� �������� � ��������� ������� ������ (������� ������, ����� ��������� ���� ������ �� backlink-� �� ��� ������� ��������, � ��� ��������� ������� ��������)
	private ObjectId dstPersonAttrId;	// ������� �������
	private List<ObjectId> srcPersonAttrIds;	// �������� � �����, �� ������� ���������� ������ 
	private boolean append = false;				// ��������� ������� ������� ��� ��������
	private HashSet<ObjectId> oldValues = new HashSet<ObjectId>();	// ������ �������� �������� ���������

	@SuppressWarnings({ "unchecked"})
	@Override
	public Object process() throws DataException {
		long startTime = System.currentTimeMillis();
		logger.info( "Start processing processor '"+this.getClass().getName()+"': time = "+ startTime);
		ObjectId cardId = getCardId();
		UserData user = getSystemUser();

		if (backLinkAttrId == null){
			logger.error("Parameter backLinkAttrId is not set. Exit.");
			return null;
		}
		if (dstPersonAttrId == null){
			logger.error("Parameter dstPersonAttrId is not set. Exit.");
			return null;
		}
		if ((srcPersonAttrIds == null)||(srcPersonAttrIds.isEmpty())){
			logger.error("Parameter srcPersonAttrIds is not set. Exit.");
			return null;
		}

		List<Card> firstLayerCards = new ArrayList<Card>();
		if (refLinkAttrId == null){	// ���� refLinkAttrId �� �����
			logger.warn("Parameter refLinkAttrId is not set. I will use backLinkAttrId only.");
			Card currCard = new Card();
			currCard.setId(((Long)cardId.getId()).longValue());
			firstLayerCards.add(currCard);	// ������� �������� � ���� ��������� ������� ������
		} else {
			firstLayerCards.addAll( getLinkedCards(cardId, refLinkAttrId, true, new ArrayList<ObjectId>()) );	// ����� ������ ��
			if (firstLayerCards.isEmpty()){
				logger.debug("No first layer parent card found. Exit.");
				return null;
			}
		}
		
		final Card firstLayerCard = firstLayerCards.iterator().next(); 	// ������������ ������ ������� �������� �� ��������������� ������
		if (firstLayerCards.size() > 1){
			logger.warn("(!) WARNING : There are more than 1 first layer parent cards - only the first "+ 
					firstLayerCard.getId()+ "will be used.");
		}

		if (append)
			oldValues.addAll(loadCardPersonAttributeValues(cardId, dstPersonAttrId));	// ���������� ������ �������� �������� ��������

		// �� ������ backlink-� ���� ����� ������� ��������� � ��������� � ��� �������� ���������, �������� � srcPersonAttrIds 
		ListProject action = new ListProject();
		action.setAttribute(backLinkAttrId);
		action.setCard(firstLayerCard.getId());
		List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>();
		for (ObjectId id : srcPersonAttrIds) {
			SearchResult.Column col = new SearchResult.Column();
			col.setAttributeId(id);
			columns.add(col);
		}
		SearchResult.Column col = new SearchResult.Column();
		col.setAttributeId(Card.ATTR_STATE);
		columns.add(col);
		action.setColumns(columns);
		ActionQueryBase aqb = getQueryFactory().getActionQuery(action);
		aqb.setAction(action);
		aqb.setUser(user);
		final Collection<Card> linkedCards = 
			((SearchResult)getDatabase().executeQuery(user, aqb)).getCards();
		if (linkedCards.isEmpty()){
			logger.debug("No far parent card found. Exit.");
			return null;
		}

		final Card mainCard = linkedCards.iterator().next(); 	// �� ������ ��������� ����� �������
		if (linkedCards.size() > 1){
			logger.warn("(!) WARNING : There are more than 1 origin cards - only the first "+ 
					mainCard.getId()+ "will be used.");
		}
		final Set<ObjectId> persons = new HashSet<ObjectId>();
		final Set<ObjectId> personCards = new HashSet<ObjectId>();
		for (ObjectId id : srcPersonAttrIds) {
			final Attribute attr = mainCard.getAttributeById(id);
			if (attr != null)
				if (attr instanceof PersonAttribute)
					ObjectIdUtils.fillObjectIdSetFromCollection(persons, ((PersonAttribute)attr).getValues());
				else if (attr instanceof CardLinkAttribute)
					personCards.addAll(((CardLinkAttribute)attr).getIdsLinked());
		}
		// ���������� �������� id ������������ �������� � ���� ������...
		ObjectIdUtils.fillObjectIdSetFromCollection(persons, super.getPersonsByCards(personCards));
		
		oldValues.addAll(persons);	// ��������� ������ �������� � �������������� ������
		
		/*
		 CardUtils.dropAttributes(getJdbcTemplate(),		// ������� ������ ��������
				new Object[] { dstPersonAttrId, Attribute.ID_CHANGE_DATE }, cardId);
		 */
		// SQL-������� � ��������� "����������" �������� ...
		insertCardPersonAttributeValues(cardId, dstPersonAttrId, oldValues, true);
		markChangeDate(cardId);	// ������� ������� ���������
		
		logger.info( "Finish processing processor '"+this.getClass().getName()+"': workingtime = "+ (System.currentTimeMillis()-startTime));
		return null;
	}

	@Override
	public void setParameter(String name, String value) {
		if ("backLinkAttrId".equals(name)) {
			this.backLinkAttrId = ObjectIdUtils.getObjectId(BackLinkAttribute.class, value, false);
		} else if ("refLinkAttdId".equals(name)){
			this.refLinkAttrId = ObjectIdUtils.getObjectId(CardLinkAttribute.class, value, false);
		} else if ("dstPersonAttrId".equals(name)){
			this.dstPersonAttrId = ObjectIdUtils.getObjectId(PersonAttribute.class, value, false);
		} else if ("srcPersonAttrIds".equals(name)){
			this.srcPersonAttrIds = stringToAttrIds(PersonAttribute.class, value);;
		} else if ("writeOperation".equalsIgnoreCase(name)) {
			if ("append".equalsIgnoreCase(value))
				append = true;
		} else {
			super.setParameter(name, value);
		}
	}
}
