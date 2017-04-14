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

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.jbr.processors.ProcessCard;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.SystemRole;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.SystemUser;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.QueryFactory;
import com.aplana.dbmi.service.impl.UserData;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.EmptyResultDataAccessException;

/**
 *	����� ������������� �������� ��������� �����������, �������, �����������, ������������, ��� ����������(������ � ������� �������� ������������) � ��������� ��� � ���
 *  ���� �������� ������ �������, �� ������ ���������� �� ����� �������� ������������, ��� ��������� - �� ����� �������
 */
public class FillCuratorDeveloperExecutorPostProcessor extends ProcessCard {
	private static final long serialVersionUID = 1L;
	
	public static final ObjectId PERSON_DEPARTMENT = ObjectId.predefined(CardLinkAttribute.class, "jbr.person.dept");
	public static final ObjectId PARENT_DEPARTMENT = ObjectId.predefined(CardLinkAttribute.class, "jbr.department.parentDepartment");
	public static final ObjectId DEVELOPER = ObjectId.predefined(CardLinkAttribute.class, "jbr.doc.fromDepartment");
	public static final ObjectId EXECUTOR = ObjectId.predefined(PersonAttribute.class, "jbr.resolutionExecutor");
	public static final ObjectId NPA_SIGNATORY = ObjectId.predefined(PersonAttribute.class, "jbr.outcoming.signatory");
	public static final ObjectId DEP_CURATOR = ObjectId.predefined(PersonAttribute.class, "jbr.department.curator");
	public static final ObjectId NPA_CURATOR = ObjectId.predefined(PersonAttribute.class, "jbr.npa.curator");
	//public static final ObjectId GUBERNATOR = ObjectId.predefined(Person.class, "gubernator");
	public static final ObjectId PERSON_SIGNATORY = ObjectId.predefined(SystemRole.class, "signatory.ord.npa");
	public static final ObjectId MANAGER = ObjectId.predefined(PersonAttribute.class, "jbr.manager");
	public static final ObjectId DEP_CHIEF = ObjectId.predefined(CardLinkAttribute.class, "jbr.department.chief");
	
	private static final String ONLY_FIRST_SIGNATORY_PERSON = "onlyFirstSignatoryPerson";
	private static final String IS_FIRST_SET_SIGNATORY_PERSON = "isFirstSet";
	
	private Boolean onlyFirstSignatoryPerson = Boolean.TRUE;
	private Boolean isFirstSet  = Boolean.FALSE;
	
	protected final Log logger = LogFactory.getLog(getClass());
	
	private ObjectId getTopLevelDepartmentId(ObjectId departmentId){
		int top = getJdbcTemplate().queryForInt(
			"with recursive t as ( \n"
			+ "		select cast(? as numeric)  as id, 0 as level \n"
			+ "		union \n"
			+ "		select cast(a.number_value as numeric), t.level + 1 as id from t \n"
			+ "		join attribute_value a on a.card_id = t.id and attribute_code = ? \n"
			+ ") select id from t where level = (select case when max(level) - 1 > 0 then max(level) - 1 else 0 end from t)", 
			new Object[]{departmentId.getId(), PARENT_DEPARTMENT.getId()}, 
			new int[]{Types.NUMERIC, Types.VARCHAR}
		);
		return top == 0 ? null : new ObjectId(Card.class, top);
	}
	
	private ObjectId getSupervisorId(ObjectId departmentId) throws DataException{
		try {
			int sv = getJdbcTemplate().queryForInt(
				"with recursive t as ( \n"
						+ "	select cast(? as numeric) as dep_id, \n"
						+ " cast((select number_value from attribute_value where card_id = ? and attribute_code = ?) as numeric) as sv_id \n"
						+ "	union \n"
						+ "	select cast(p.number_value as numeric), cast(svp.number_value as numeric) from t \n"
						+ "	join attribute_value p on t.sv_id is null and p.card_id = t.dep_id and p.attribute_code = ? \n"
						+ "	left join attribute_value svp on svp.card_id = p.number_value and svp.attribute_code = ? \n"
						// + ")select max(sv_id) from t",
						// (BR4J00034368) ������ ������������� id-���� �������� ���� ������� �� ������ ��������� � �������� �������������, ������� � ������������� ������ � �������� � �����������
						+ ")select sv_id from t where (coalesce(sv_id, -1) <> -1) limit 1",
						new Object[]{departmentId.getId(), departmentId.getId(),  DEP_CURATOR.getId(), PARENT_DEPARTMENT.getId(), DEP_CURATOR.getId(),}, 
						new int[]{Types.NUMERIC, Types.NUMERIC, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR}
					);
			return sv == 0 ? null : new ObjectId(Person.class, sv);
		} catch(EmptyResultDataAccessException e) {
			throw new DataException("docflow.processor.empty.curator", e);
		} catch(Exception e) {
			throw new DataException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object process() throws DataException {
		Card card = getCard();
		final boolean isCardNotExist = (card.getId() == null); 
		if(isCardNotExist){
			saveCard(card, getUser()); //���� �������� �����, ��������� ��
			reloadCard(getUser());
		}
		
		//������������� ���������� � ����������� ���������
		Person executor = ((PersonAttribute) card.getAttributeById(EXECUTOR)).getPerson();
		if(executor == null){
			executor = getUser().getPerson();
			((PersonAttribute) card.getAttributeById(EXECUTOR)).setPerson(executor);
		}		
		
		// ��������� ��� ���������� ������ ��� �������� ��������
		if(isFirstSet){
			final List<Person> signatoryList = filterActivePersons(getPersonByRole(PERSON_SIGNATORY));
			if(!CollectionUtils.isEmpty(signatoryList)) {
				PersonAttribute pa = (PersonAttribute) card.getAttributeById(NPA_SIGNATORY);
				Person person = signatoryList.get(0);
				if(onlyFirstSignatoryPerson) {
					pa.setPerson(person);
				} else {
					if(!pa.isMultiValued()) {
						logger.info(getClass().getName() + " attribute " + pa.getId().getId() 
								+ " is Singlevalued. Taken only first value of " + PERSON_SIGNATORY.getId());
						pa.setPerson(person);
					} else {
						pa.clear();
						pa.getValues().addAll(signatoryList);
					}
				}
			}
		}
		//((PersonAttribute) card.getAttributeById(NPA_SIGNATORY)).setPerson(getPerson(GUBERNATOR));
		
		
		//�������� id ������������� �����������
		Card personCard = fetchSingleCard(executor.getCardId(), Collections.singleton(PERSON_DEPARTMENT), true);
		ObjectId departmentId = ((CardLinkAttribute) personCard.getAttributeById(PERSON_DEPARTMENT)).getSingleLinkedId();
		if (departmentId != null && departmentId.getId() != null){
			
			//�������� id ������������ ������������� ������� ������
			ObjectId topLevelDepartmentId = getTopLevelDepartmentId(departmentId);
			
			//�������� id ������������ ������������ ������������� ������� ������ � ������������� ��� ��� ������������ �����������
			Card topDepartmentCard = fetchSingleCard(topLevelDepartmentId, Collections.singleton(DEP_CHIEF), false);
			Set<Person> personList = getPersonsList(topDepartmentCard, DEP_CHIEF, false);
			if (personList != null && !personList.isEmpty()) {
				Person manager = personList.iterator().next();
				((PersonAttribute) card.getAttributeById(MANAGER)).setPerson(manager);
			}
			
			//������������� ��������
			((PersonAttribute) card.getAttributeById(NPA_CURATOR)).setPerson(getPerson(getSupervisorId(departmentId)));
			
			//������������� ������������ ��� ����������� ������������� ������� ������
			((CardLinkAttribute) card.getAttributeById(DEVELOPER)).addSingleLinkedId(topLevelDepartmentId);
		}
		saveCard(card, getSystemUser());
		reloadCard(getUser());
		return card;
	}
	
	@Override
	public void setParameter(String name, String value) 
	{
		if (name == null) return;
		
		if(ONLY_FIRST_SIGNATORY_PERSON.equalsIgnoreCase(name)) {
			onlyFirstSignatoryPerson = Boolean.parseBoolean(value);
		} else if(IS_FIRST_SET_SIGNATORY_PERSON.equalsIgnoreCase(name)) {
			isFirstSet = Boolean.parseBoolean(value);
		} else {
			super.setParameter(name, value);
		}
	}
	
	/**
	 * Filters persons by person card state (card state id: 20 - active user)
	 * 
	 * @param persons
	 * @return
	 * @throws DataException
	 */
	private List<Person> filterActivePersons(List<Person> persons) throws DataException {
		StringBuilder ids = new StringBuilder();
		for(Person p : persons) {
			if(p.getCardId() != null)
				ids.append(p.getCardId().getId());
		}
		Search search = new Search();
		SearchResult.Column state = new SearchResult.Column();
		state.setAttributeId(Card.ATTR_STATE);
		search.setColumns(Collections.singletonList(state));
		search.setByCode(true);
		search.setWords(ids.toString());
		
		ActionQueryBase searchQuery = getQueryFactory().getActionQuery(search);
		searchQuery.setAction(search);
		SearchResult result = getDatabase().executeQuery(getSystemUser(), searchQuery);
		List<Card> cards = result.getCards();
		final List<Person> resultPersons = new ArrayList<Person>();
		if(cards != null) {
			for(Card c : cards) {
				if(c.getState() != null
						&& c.getState().equals(ObjectId.predefined(CardState.class, "user.active"))) {
					for(Person p : persons) {
						if(p.getCardId() != null
								&& p.getCardId().equals(c.getId())) {
							resultPersons.add(p);
						}
					}
				}
			}
		}
		return resultPersons;
	}
}
