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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.util.CollectionUtils;

import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.LinkAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.service.DataException;

/**
 * ��������� ���� �� � ��������, ��� � ������������ �������� ������ ������� ����,
 * ����������� ������� � ��������� ���������.
 * @author ppolushkin
 */
public class ComparationListAttr extends ProcessCard {

	private static final long serialVersionUID = 1L;
	/**
	 * �������������� ��������. ������ ����-������� � ������� �������� � ��� ��������, 
	 * ��� ������� ����� ���������� ������
	 * ���� �� ��� ���.
	 */
	private static final String PARAM_TEST_ATTR = "test_attr";
	/**
	 * �������������� ��������. ���� �� ����� - ������� ������� ��������
	 * ������ ���� � ������� �������� ��� ���������
	 */
	private static final String PARAM_COMPARE_PATH = "compare_path";
	/**
	 * �������������� ��������. ������ �������� �� ��������
	 */
	protected static final String PARAM_IGNORED_STATES = "ignored_states";
	/**
	 * ������������ ��������. ������������ � �������� ��������, �������� ���������� � ������� ������
	 */
	protected static final String PARAM_COMPARE_PERSON = "compare_person";
	/**
	 * �������������� ��������. �������� � �������� ��������, �� ����������� ������� ����� ����������� ������� �������� ������ ��� ���
	 */
	protected static final String PARAM_COMPARE_ATTR = "compare_attr";
	/**
	 * ��������������. ������ ��������� �� ������
	 */
	protected static final String PARAM_ERROR_MESSAGE = "error_message";
	
	
	private ObjectId testAttrId;
	private List<ObjectId> testAttrValues;
	private List<ObjectId> comparePath = new ArrayList<ObjectId>();
	private Set<ObjectId> ignoredStateIds;
	private ObjectId personAttrId;
	private ObjectId compareAttrId;
	private ReferenceValue compareAttrValue;
	private String errorMsg;

	@Override
	public Object process() throws DataException {

		Card card = getCard();

		// �������� ���������� �������� ��������� ��������
		final ListAttribute testAttr = (ListAttribute) (testAttrId != null 
				? card.getAttributeById(testAttrId) 
						: null);
		if(testAttr != null && testAttr.getValue() != null
				&& !testAttrValues.contains(testAttr.getValue().getId())) {
			return null;
		}

		//������� � �������� ��� ���������
		Card compareCard = card;
		List<Card> load = Collections.singletonList(compareCard);
		for(Iterator<ObjectId> iter = comparePath.iterator(); iter.hasNext();) {
			ObjectId node = iter.next();
			// TODO: ��������������. ������� �� ��� ��������, � ������ ����������� ��������
			ObjectId compareCardId = compareCard.getId();
			LinkAttribute compareAttr = compareCard.getAttributeById(node);
			// �������� ����� ���� ��� �� ���������
			if(compareCardId != null) {
				load = loadAllLinkedCardsByAttr(compareCardId, compareAttr);
			} else if(compareAttr != null && compareAttr.getIdsLinked() != null) {
				load = new ArrayList<Card>();
				Card tempC;
				for(ObjectId idObj : compareAttr.getIdsLinked()) {
					tempC = loadCardById(idObj);
					if(tempC != null) {
						load.add(tempC);
					}
				}
			} else {
				throw new DataException("Linked attr " + node.getId() + " not found");
			}
			if(CollectionUtils.isEmpty(load)) {
				throw new DataException("Attribute " + node.getId() + " is empty in card " + compareCard.getId());
			}
			if(iter.hasNext()) {
				compareCard = load.get(0);
			}
		}

		// �������� ��������� �������� �� ������������ ��������, ������������� � �� ��������� ��������
		final Person currentUser = getUser().getPerson();
		Card cc;
		PersonAttribute personAttr;
		Person user;
		ListAttribute compareAttr;
		ReferenceValue rv;
		List<Person> assistOfUser = null;
		for(Iterator<Card> iter = load.iterator(); iter.hasNext();) {
			cc = iter.next();
			if (personAttrId != null) {
				personAttr = cc.getAttributeById(personAttrId);
				user = personAttr.getPerson();
				assistOfUser = getAssistants(user);
				if(assistOfUser == null) {
					assistOfUser = new ArrayList<Person>();
				}
				assistOfUser.add(user);
			}

			compareAttr = (ListAttribute) (compareAttrId != null 
					? cc.getAttributeById(compareAttrId) 
							: null);
			rv = compareAttr != null 
					? compareAttr.getValue() 
							: null;
			if((ignoredStateIds != null && ignoredStateIds.contains(cc.getState()))
					|| (personAttrId != null && !assistOfUser.contains(currentUser))
					|| (compareAttrId != null && rv != null && !rv.getId().equals(compareAttrValue.getId()))) {
				iter.remove();
			}
		}

		// ��������� �� ��� �����������
		if(load.size() > 1) {
			logger.warn("More then 1 card for current user " + currentUser);
		}
		if(load.size() == 0 && errorMsg != null) {
			throw new DataException(errorMsg);
		} else if(load.size() == 0 && errorMsg == null) {
			throw new DataException("Attribute " + personAttrId + " check failed");
		}
		
		return null;
	}

	private List<Person> getAssistants(Person person) throws DataException 
	{
		List<Person> assistants = new ArrayList<Person>();
		if(person == null) 
			return assistants;
		final List<Card> arm =
			CardUtils.getArmSettingsCardsByBoss(person, getQueryFactory(), getDatabase(), getSystemUser());

		if (arm == null || arm.isEmpty())
			return assistants;

		final Card card = arm.iterator().next();
		final Attribute aperson = card.getAttributeById(CardUtils.ATTR_ASSISTANT);
		if (aperson instanceof PersonAttribute)
			try {
				assistants.addAll( CardUtils.getAttrPersons((PersonAttribute) aperson));
			} catch (NullPointerException e) {	}
		
		return assistants;
	}

	@Override
	public void setParameter(String name, String value) {
		if (PARAM_TEST_ATTR.equalsIgnoreCase(name)) {
			String[] values = value.trim().split("=");
			if(values.length < 2) {
				throw new IllegalArgumentException("Attribute " + name + " must have literal =");
			}
			testAttrId = IdUtils.smartMakeAttrId(values[0], ListAttribute.class);
			String[] refs = values[1].trim().split(",");
			testAttrValues = new ArrayList<ObjectId>(refs.length);
			ObjectId ref;
			for(String rf : refs) {
				ref = IdUtils.smartMakeAttrId(rf, ReferenceValue.class);
				testAttrValues.add(ref);
			}
		} else if(PARAM_COMPARE_PATH.equalsIgnoreCase(name)) {
			String[] values = value.trim().split("->");
			for(String val : values) {
				comparePath.add(IdUtils.smartMakeAttrId(val, CardLinkAttribute.class));
			}
		} else if(PARAM_IGNORED_STATES.equalsIgnoreCase(name)) {
			ignoredStateIds = IdUtils.makeStateIdsList(value);
		} else if(PARAM_COMPARE_PERSON.equalsIgnoreCase(name)) {
			personAttrId = IdUtils.smartMakeAttrId(value, PersonAttribute.class);
		} else if(PARAM_COMPARE_ATTR.equalsIgnoreCase(name)) {
			String[] values = value.trim().split("=");
			if(values.length < 2) {
				throw new IllegalArgumentException("Attribute " + name + " must have literal =");
			}
			compareAttrId = IdUtils.smartMakeAttrId(values[0], ListAttribute.class);
			ObjectId ref = IdUtils.smartMakeAttrId(values[1], ReferenceValue.class);
			compareAttrValue = new ReferenceValue();
			compareAttrValue.setId(ref);
		} else if(PARAM_ERROR_MESSAGE.equalsIgnoreCase(name)) {
			errorMsg = value.trim();
		} else {
			super.setParameter(name, value);
		}
	}

}
