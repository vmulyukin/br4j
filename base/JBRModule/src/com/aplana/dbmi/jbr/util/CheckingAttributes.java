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
package com.aplana.dbmi.jbr.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.Database;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.QueryFactory;
import com.aplana.dbmi.service.impl.UserData;

public class CheckingAttributes {
	// �������� �������� ������������ ��� �������(��� �������� �������� person)
	public static final String CURRENT_USER = "CURRENT_USER";
	// �������� �������� ������������ ��� �������(��� �������� �������� cardlink)
	public static final String CURRENT_USER_CARD = "CURRENT_USER_CARD";
	// �������� - ������������ �������� ������������ (��� �������� �������� person)
	public static final String BOSS = "BOSS";
	// �������� - ������������ �������� ������������ (��� �������� �������� cardlink)
	public static final String BOSS_CARD = "BOSS_CARD";
	// �������� - ���������� �������� ������������ (��� �������� �������� person)
	public static final String ASSISTENT = "ASSISTENT";
	// �������� - ���������� �������� ������������ (��� �������� �������� cardlink)
	public static final String ASSISTENT_CARD = "ASSISTENT_CARD";

	private static final ObjectId ATTR_BOSS = ObjectId.predefined(PersonAttribute.class, "jbr.arm.manager");
	private static final ObjectId ATTR_ASSISTENT = ObjectId.predefined(PersonAttribute.class, "boss.assistant");

	protected Log logger = LogFactory.getLog(getClass());

	private QueryFactory factory;
	private Database database;
	private UserData systemUser;

	private List<List<AttributeSelector>> andConditions;
	private List<Person> boss;
	private List<Person> assistent;
	private boolean bossFetched = false;
	private boolean assistentFetched = false;

	public CheckingAttributes(QueryFactory factory, Database database, UserData systemUser) {
		andConditions = new LinkedList<List<AttributeSelector>>();
		this.factory = factory;
		this.database = database;
		this.systemUser = systemUser;
	}

	public void addCondition(String condition) throws DataException {
		// condition ���� ����1=��������11, ��������12 ; a���2=�������21, ��������22
		// ����� ����� "���", �.�. ������ ����������� ���� �� ���� �������:
		// ��� ����1=��������11 ��� ����1=��������12 ��� ����2=��������21 ��� ����2=��������22
		String[] orAttrs = condition.split(";");
		List<AttributeSelector> orSelectors = new LinkedList<AttributeSelector>();
		for (String attrCondition : orAttrs) {
			AttributeSelector shareSelector = AttributeSelector.createSelector(attrCondition);
			String[] values = shareSelector.getValue2Compare().split(",");
			for (String value : values) {
				// �������� �������� ����� � ��������, �� �������� ������� �� ����-��
				// � ������� ������ ������

				try {
					AttributeSelector selector= shareSelector.clone();
					selector.setValue(value.trim());
					selector.setQueryFactory(this.factory);
					selector.setDatabase(this.database);
					orSelectors.add(selector);
				} catch (CloneNotSupportedException ex) {
					throw new IllegalStateException(ex);
				}

			}
		}
		andConditions.add(orSelectors);
	}

	public void addAndConditions(String andConditions) throws DataException {
		String[] andConds = andConditions.split("AND");
		for (int i = 0; i < andConds.length; i++) {
			addCondition(andConds[i].trim());
		}
	}

	public boolean check(Card card, UserData user) throws DataException {
		boolean cardFetched = false;
		for (List<AttributeSelector> orConditions : andConditions) {
			boolean orTrue = false;
			orCond: for (AttributeSelector statCond : orConditions) {
				List<AttributeSelector> dinConds = updateRuntimeValue(statCond, user);
				// dinConds ����� ���� ������, ����� ��������� � ���������� ������� "���"
				for (AttributeSelector cond : dinConds) {
					if (!cardFetched) {
						// ���������� �������� ���� ��� �� ������...
						if ( 	(cond instanceof AttributeSelector)
								&& (null == card.getAttributeById( (cond).getAttrId() )||card.getAttributeById( (cond).getAttrId() ).getStringValue()==null||card.getAttributeById( (cond).getAttrId() ).getStringValue().length()==0) )
						{
							ObjectQueryBase cardQuery = factory.getFetchQuery(Card.class);
							cardQuery.setId(card.getId());
							card = (Card)database.executeQuery(systemUser, cardQuery);
							cardFetched = true;
						}
					}
					orTrue = cond.satisfies(card);
					if (orTrue) break orCond;
				}
			}
			if (!orTrue) return false;
		}
		return true;
	}

	private List<AttributeSelector> updateRuntimeValue(AttributeSelector oldCondition, UserData user) throws DataException {
		List<AttributeSelector> result = new LinkedList<AttributeSelector>();
		if (!isRuntimeCondition(oldCondition)) {
			result.add(oldCondition);
		} else {
			String oldValue = oldCondition.getValue2Compare();
			List<String> newValues = new LinkedList<String>();
			if (CURRENT_USER.equals(oldValue)) {
				newValues.add(user.getPerson().getId().getId().toString());
			} else if (CURRENT_USER_CARD.equals(oldValue)) {
				try {
					newValues.add(user.getPerson().getCardId().getId().toString());
				} catch (NullPointerException e) {
					logger.error(e.getMessage(), e);
				}
			} else if (BOSS.equals(oldValue) || BOSS_CARD.equals(oldValue)) {
				if (!bossFetched) {
					boss = getBoss(user);
					bossFetched = true;
				}
				if (boss != null) {
					for (Person b : boss) {
						if (BOSS.equals(oldValue)) {
							newValues.add(b.getId().getId().toString());
						} else {
							try {
								newValues.add(b.getCardId().getId().toString());
							} catch (NullPointerException e){
								logger.error(e.getMessage(), e);
							}
						}
					}
				}
			} else if (ASSISTENT.equals(oldValue) || ASSISTENT_CARD.equals(oldValue)) {
				if (!assistentFetched) {
					assistent = getAssistent(user);
					assistentFetched = true;
				}
				if (assistent != null) {
					for (Person b : assistent) {
						if (ASSISTENT.equals(oldValue)) {
							newValues.add(b.getId().getId().toString());
						} else {
							try {
								newValues.add(b.getCardId().getId().toString());
							} catch (NullPointerException e){
								logger.error(e.getMessage(), e);
							}
						}
					}
				}
			} else {
				// ����������� �������� �����
				logger.error("Unknow key word for runtime condition");
				return result;
			}
			for (String newV : newValues) {
				try {
					AttributeSelector newCond = oldCondition.clone();
					newCond.setValue(newV);
					result.add(newCond);
				} catch (CloneNotSupportedException ex) {
					throw new IllegalStateException(ex);
				}
			}
		}
		return result;
	}

	private boolean isRuntimeCondition(AttributeSelector cond) {
		String value = cond.getValue2Compare();
		return
			CURRENT_USER.equals(value) ||
			CURRENT_USER_CARD.equals(value) ||
			BOSS.equals(value) ||
			BOSS_CARD.equals(value) ||
			ASSISTENT.equals(value) ||
			ASSISTENT_CARD.equals(value);
	}

	// ��������� ������ Person ������������� ��� null ���� ��� �� ������
	private List<Person> getBoss(UserData user) throws DataException {
		final List<Card> arms =
			CardUtils.getArmSettingsCardsByAssistent( user.getPerson(),
					factory, database, user);
		if (arms == null || arms.size() == 0) return null;
		List<Person> persons = new ArrayList<Person>(arms.size());
		for( Card arm : arms) { // ������� ����� ���� ���������� ���������� �������������
			PersonAttribute attrBoss = (PersonAttribute)arm.getAttributeById(ATTR_BOSS);
			// ��������� ������
			ObjectQueryBase personQuery = factory.getFetchQuery(Person.class);
			personQuery.setId(attrBoss.getPerson().getId());
			Person p = (Person)database.executeQuery(systemUser, personQuery);
			persons.add(p);
		}
		return persons;
	}

	private List<Person> getAssistent(UserData user) throws DataException {
		final List<Card> arms =
			CardUtils.getArmSettingsCardsByBoss( user.getPerson(),
					factory, database, user);
		if (arms == null || arms.size() == 0) return null;
		List<Person> persons = new ArrayList<Person>(arms.size());
		for( Card arm : arms) {
			PersonAttribute attrAssistent = (PersonAttribute)arm.getAttributeById(ATTR_ASSISTENT);
			if (attrAssistent == null) continue;
			ObjectQueryBase personQuery = factory.getFetchQuery(Person.class);
			for (Iterator i = attrAssistent.getValues().iterator(); i.hasNext();) {
				personQuery.setId(((Person)i.next()).getId());
				Person p = (Person)database.executeQuery(systemUser, personQuery);
				persons.add(p);
			}
		}
		return persons;
	}
}
