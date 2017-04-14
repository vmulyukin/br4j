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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.jbr.action.GetAssistants;
import com.aplana.dbmi.jbr.processors.DoDependentChangeState;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

/*
 * ��������� ������������ ��� �������� ��� "���������" � ������ "������", ����� "������������" ���������-���������
 * ����������� "� ����". ������� �������������� ������ ��� ��� ��� "���������", ��� ������� 
 * ��������� "���������" � ��� �������� � "���������������" � ��� ���������� � ��� "������������".
 */
public class DoArchiveDependentChangeState extends DoDependentChangeState {
	private static final long serialVersionUID = 1L;
	// personattribute.jbr.exam.person=JBR_RASSM_PERSON = "���������������"
	static final ObjectId examPersonAttrId = 
			ObjectId.predefined(PersonAttribute.class, "jbr.exam.person");
	// personattribute.jbr.hidden.examinerAssistants=ADMIN_479194 = "��������� ����������������"
	/*static final ObjectId examAssistantsAttrId =
			ObjectId.predefined(PersonAttribute.class, "jbr.hidden.examinerAssistants");
	 */
	// JBR_INFD_SGNEX_LINK = "���������" ��� "���������"
	static final ObjectId signerAttrId = ObjectId.predefined(PersonAttribute.class, "jbr.resolution.FioSign");

	protected void processDependentCard(Card card, Card actionCard, HashSet<ObjectId> sourceStateIds, ObjectId targetStateId, Card parentCard) throws DataException {
		final PersonAttribute examPerson = (PersonAttribute)loadAttribute(actionCard.getId(), examPersonAttrId);
		// ��������� ����������
		final List<ObjectId> assistantsIds = getAssistants(examPerson.getValues(), null);
		//final PersonAttribute assistantsPerson = (PersonAttribute)loadAttribute(actionCard.getId(), examAssistantsAttrId);
		final PersonAttribute resolutionPerson = (PersonAttribute)loadAttribute(card.getId(), signerAttrId);
		if(resolutionPerson == null || resolutionPerson.getValues() == null) {
			logger.error("Card " + card.getId() + " do not consists attribute's " + signerAttrId + " value");
			return;
		}
		Set<ObjectId> examPersonSet = new HashSet<ObjectId>(); 
		if (examPerson != null){
			examPersonSet.addAll(getIdsFromCollection(examPerson.getValues()));
		}
		if (assistantsIds != null){
			examPersonSet.addAll(assistantsIds);
		}
		if (examPersonSet.contains(getSinglePersonId(resolutionPerson.getValues()))){
			super.processDependentCard(card, actionCard, sourceStateIds, targetStateId, parentCard);
		}
		
	}

	private Attribute loadAttribute(ObjectId cardId, ObjectId attrId) throws DataException{
		final Search search = CardUtils.getFetchAction(cardId,
				new ObjectId[] { attrId, Card.ATTR_STATE, Card.ATTR_TEMPLATE });
		final List<Card> found = CardUtils.execSearchCards(search, getQueryFactory(), getDatabase(), 
				getOperUser() );
		if (found == null || found.size() != 1)
			throw new DataException("jbr.linked.parent.fetch",
					new Object[] { cardId.getId().toString() });
		Card card = found.get(0);
		return card.getAttributeById(attrId);
	}
	
	private Set<ObjectId> getIdsFromCollection(Collection<?> c){
		Set<ObjectId> result = new HashSet<ObjectId>();
		Iterator<?> i = c.iterator();
		while(i.hasNext()){
			result.add(((Person)i.next()).getId());
		}
		return result;
	}

	/**
	 * @param chiefIds ������ id-������ ������, ��� ������� ���� �������� �����������. 
	 * @return ������ �����������, �������� ����� � ������ chiefPersonAttr.
	 * @throws DataException 
	 */
	@SuppressWarnings("unchecked")
	private List<ObjectId> getAssistants( Collection<?> chiefIds, 
				Collection<ObjectId> filterByRoles
		) throws DataException 
	{
		final List<ObjectId> result = new ArrayList<ObjectId>();
		if (!chiefIds.isEmpty()) {
			// ����� action ��� ��������� ������ ����������
			GetAssistants search = new GetAssistants();
			search.setChiefIds(chiefIds);
			search.setChiefRoleIds(filterByRoles);
			
			ActionQueryBase query = getQueryFactory().getActionQuery(search);
			query.setAction(search);
			Collection<ObjectId> assistantIds =
					(Collection<ObjectId>) getDatabase().executeQuery(getSystemUser(), query);

			if (assistantIds != null) {
				for (Object obj : assistantIds) {
					result.add( ObjectIdUtils.getIdFrom(obj));
				}
			}
		}
		return result;
	}

	private ObjectId getSinglePersonId(Collection<?> c){
		Iterator<?> i = c.iterator();
		return ((Person)i.next()).getId();
	}
}

