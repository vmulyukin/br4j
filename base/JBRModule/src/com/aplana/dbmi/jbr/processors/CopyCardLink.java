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
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.aplana.dbmi.jbr.util.CheckingAttributes;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.LinkItem;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
/**
 * ���������, �������������� ����������� CardLinkAttribute / TypedCardLinkAttribute �� ������ ��������� �������� � ������.
 * �������� �� ��������-���������� ������������ � ���� ���������, ������� ���������� � ��������-���� � ����������
 * �������� ��������, ���� �������� merge = false, ��� ������������, ���� merge = true;
 * ��� ��������� �������� ������������ �� ��������� ������� �������� �� ���������, ��������� � ���������� from � to.
 * ������ ���������� from � to: <br/> value = [path], "@", attributeId; <br/> path = {directionOfLink, attributeId}; </br>
 * directionOfLink = "<-" | "->"; <br/> attributeId = ? CardLinkAttribute ��� TypedCardLinkAttribute ���������?
 * @author erentsov
 *
 */
public class CopyCardLink extends ProcessCardWithConditions {
	private static final long serialVersionUID = 1L;

	private static final String PARAM_FROM_CONDITION = "from_condition";
	
	ObjectId fromId;
	ObjectId toId;
	List<LinkItem> fromPath;
	List<LinkItem> toPath;
	private boolean merge = true;
	// ������� �������� ��������� � �������� from
	CheckingAttributes fromConditions;
	
	@Override
	public Object process() throws DataException {
		
		if(fromId == null || getToIds().isEmpty()) logger.info("Mandatory parameter isn't set. Exiting.");
		
		Card card = getCard();
		
		if(!checkContidions(card)){
			return card;
		}
			
		Collection<ObjectId> fromCardsIds = traverseCardLinksChain(card.getId(), fromPath);
		Collection<ObjectId> toCardsIds = traverseCardLinksChain(card.getId(), toPath);
		Collection<Card> fromCards = this.fetchCards(fromCardsIds, Collections.singleton(fromId), true);
		Collection<Card> toCards = this.fetchCards(toCardsIds, getToIds(), true);

		CardLinkAttribute copyAttr;
		try{copyAttr = (CardLinkAttribute) getToIds().iterator().next().getType().newInstance();} catch(Exception e) {throw new DataException(e);}
		for(Card c: fromCards) addFromValuesToAttribute(copyAttr, c);

		for(Card c: toCards){
			if(c.equals(card)) c = card;
			for (CardLinkAttribute linkAttr : getToAttrs(c)) {
				logger.info("param merge is " + merge);
				if(merge) {
					/* ���� ���� merge == true, �� ������� ����� ���� SingleValued
					 * � ����� ������ ��� ����� ���������� ���������� � ����������.
					 * ������ �������� ����������, ����� �������� ����������� single/multi-valued ��������� ������ �����,
					 * �.�. ��� �������� ��� ���������� ��������� �� ��, ��� ��� ������������ ���������� �������� merge ���� ���������� �����
					 * � ������������ � s/m-valued ����� ������������� ��������
					 */
					if(linkAttr.isMultiValued()) {
						linkAttr.addValuesFromAttribute(copyAttr);
						logger.warn("attribute  " + linkAttr.getId().getId() + " is MiltiValued, new values will be appended");
					} else {
						linkAttr.setIdLinked(copyAttr.getFirstIdLinked());
						logger.warn("attribute  " + linkAttr.getId().getId() + " is SingleValued, old values will be replaced with first current value");
					}
				} else {
					// ����������� �������� �� single/multi-valued, ����� ����� �� ����������� ������ ������ ��������, ���� SingleValued
					if(linkAttr.isMultiValued()) {
						linkAttr.setIdsLinked(copyAttr.getIdsLinked());
						logger.warn("attribute  " + linkAttr.getId().getId() + " is MiltiValued, old values will be replaced with current values");
					} else {
						linkAttr.setIdLinked(copyAttr.getFirstIdLinked());
						logger.warn("attribute  " + linkAttr.getId().getId() + " is SingleValued, old values will be replaced with first current value");
					}
				}
				doOverwriteCardAttributes(c.getId(), linkAttr);
			}
		}

		logger.info(
				"Joined values from attribute " + fromId.getId() + " of cards " + ObjectIdUtils.numericIdsToCommaDelimitedString(fromCardsIds)
				+ " have been copied into attribute " + getToIds() + " of cards " + ObjectIdUtils.numericIdsToCommaDelimitedString(toCardsIds)
		);
		if (getResult() instanceof Card) {
			return card;
		}
		return getResult();
	}
	
	protected boolean checkCardConditions(ObjectId cardId, CheckingAttributes conditions) throws DataException {
		if (conditions == null)
			return true;
		final ObjectQueryBase cardQuery = getQueryFactory().getFetchQuery(
			Card.class);
		cardQuery.setAccessChecker(null);
		cardQuery.setId(cardId);
		final Card card = (Card) getDatabase().executeQuery(getSystemUser(),
			cardQuery);
		return checkConditions(conditions, card);
	}

	private boolean checkConditions(CheckingAttributes conds, Card card) throws DataException {
		if (conds == null || card == null)
			return true;
		if(!conds.check(card, getUser())){
			logger.debug("Card " + card.getId().getId()
					+ " did not satisfies coditions" + conds);
			return false;
		}
		return true;
	}

	protected Set<ObjectId> getToIds() {
		if (toId == null) {
			return Collections.emptySet();
		}
		return Collections.singleton(toId);
	}

	protected <T extends CardLinkAttribute> Set<T> getToAttrs(Card c) {
		return Collections.singleton(c.<T>getAttributeById(toId));
	}

	protected void addFromValuesToAttribute(CardLinkAttribute copyAttr, Card c) throws DataException {
		CardLinkAttribute srcAttr = (CardLinkAttribute)c.getAttributeById(fromId);
		for(ObjectId id : srcAttr.getIdsLinked()) {
			if(checkCardConditions(id, fromConditions))
				copyAttr.addLinkedId(id);
		}
		if(srcAttr instanceof TypedCardLinkAttribute
				&& copyAttr instanceof TypedCardLinkAttribute)
			((TypedCardLinkAttribute)copyAttr).setTypes(((TypedCardLinkAttribute)srcAttr).getTypes());
	}

	protected ObjectId getFromId() {
		return this.fromId;
	}

	protected ObjectId getToId() {
		return this.toId;
	}

	@Override
	public void setParameter(String name, String value){
		if(name.equalsIgnoreCase("from")){
			String[] args = value.trim().split("@", 2);
			if(args.length < 2) return;
			fromId = IdUtils.smartMakeAttrId(args[1].trim(), CardLinkAttribute.class);
			String[] array = args[0].trim().split("(?<=[\\w\\.]*)(?=(->|<-)[\\w\\.]*)");
			fromPath = new ArrayList<LinkItem>();
			for(String as : array) {
				String[] record = as.split("(?=[\\w\\.])|(?<=(->|<-))", 2);
				if(record.length < 2) continue;
				LinkItem item = new LinkItem();
				item.setReversed(record[0].equals("<-"));
				item.setLinkId(IdUtils.smartMakeAttrId(record[1], CardLinkAttribute.class));
				fromPath.add(0, item);
			}
		} else if(name.equalsIgnoreCase("to")){
			String[] args = value.trim().split("@", 2);
			if(args.length < 2) return;
			toId = IdUtils.smartMakeAttrId(args[1].trim(), CardLinkAttribute.class);
			String[] array = args[0].trim().split("(?<=[\\w\\.]*)(?=(->|<-)[\\w\\.]*)");
			toPath = new ArrayList<LinkItem>();
			for(String as : array) {
				String[] record = as.split("(?=[\\w\\.])|(?<=(->|<-))", 2);
				if(record.length < 2) continue;
				LinkItem item = new LinkItem();
				item.setReversed(record[0].equals("<-"));
				item.setLinkId(IdUtils.smartMakeAttrId(record[1], CardLinkAttribute.class));
				toPath.add(0, item);
			}
		} else if(name.equalsIgnoreCase("merge")){
			merge = Boolean.parseBoolean(value);
		} else if (name.startsWith(PARAM_FROM_CONDITION)) {
			try {
				if (fromConditions == null) 
					fromConditions = new CheckingAttributes(getQueryFactory(), getDatabase(), getSystemUser());
				fromConditions.addCondition(value);
			} catch (DataException e) {
				e.printStackTrace();
			}
		} else super.setParameter(name, value);
	}


}
