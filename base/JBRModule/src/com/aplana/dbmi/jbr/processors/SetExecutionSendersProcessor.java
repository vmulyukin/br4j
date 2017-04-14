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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

/**
 * ������������������ ���������, ������������� ������������ ���� "������� ����� ���������� �� ����������." ��� ��������� -
 * ��������� ���������� � ��� ����������� �� �������� ����, ���� ���� �� ����������������� �������� ������� ������ ���������.
 */
public class SetExecutionSendersProcessor extends ProcessCard {

	
	private static final long serialVersionUID = 1L;
	public static final ObjectId VISAS_ID = ObjectId.predefined(CardLinkAttribute.class, "jbr.visa.set.hidden");
	public static final ObjectId RESOLUTIONS_ID = ObjectId.predefined(BackLinkAttribute.class, "jbr.resolutions");
	public static final ObjectId AUTHOR_ID = ObjectId.predefined(PersonAttribute.class, "author");
	public static final ObjectId VISA_PERSON_ID = ObjectId.predefined(PersonAttribute.class, "jbr.visa.person");
	public static final ObjectId EXECUTION_SENDERS_ID = ObjectId.predefined(PersonAttribute.class, "jbr.resolution.execution_senders.hidden");
	public static final ObjectId OWNER_ID = ObjectId.predefined(PersonAttribute.class, "boss.owner");
	public static final ObjectId ASSISTANTS_ID = ObjectId.predefined(PersonAttribute.class, "boss.assistant");
	public static final ObjectId WORKPLACE_ID = ObjectId.predefined(Template.class, "boss.settings");
	public static final ObjectId APPROVING_ID = ObjectId.predefined(CardState.class, "agreement");
	public static final List<ObjectId> VISAS_ALLOWED_STATES = Arrays.asList(
			new ObjectId[]{
					ObjectId.predefined(CardState.class, "jbr.visa.waiting"),
					ObjectId.predefined(CardState.class, "jbr.visa.boss.agreed"),
					ObjectId.predefined(CardState.class, "jbr.visa.assistent")
			}
	);
	
	@SuppressWarnings("unchecked")
	@Override
	public Object process() throws DataException {
		Card card = getCard();
		
		//�������� ���������
		Collection<ObjectId> resCardIds = CardUtils.getCardIdsByBackLink(RESOLUTIONS_ID, card.getId(), 
					getQueryFactory(), getDatabase(), getSystemUser());
		if(resCardIds == null || resCardIds.isEmpty()){
			logger.info("No resulutions. Exiting.");
			return null;
		}
		Collection<Card> resolutions 
			= fetchCards(resCardIds, Arrays.asList(new ObjectId[]{AUTHOR_ID, EXECUTION_SENDERS_ID}), true);
		
		//���� ������ ���������-��������� - �� ������������, ���������� �������
		if (!card.getState().equals(APPROVING_ID)){
			logger.info("State of main document isn't approving. The dynamic role will be cleared.");
			for(Card res : resolutions){
				PersonAttribute executionSenders = (PersonAttribute) res.getAttributeById(EXECUTION_SENDERS_ID);
				executionSenders.setValues(null);
				doOverwriteCardAttributes(res.getId(), executionSenders);
			}			
			return null;
		}
		
		//���� (���)
		Collection<ObjectId> visasIds = ((CardLinkAttribute)card.getAttributeById(VISAS_ID)).getIdsLinked();
		if(visasIds == null) {
			logger.info("The main document (" + card.getId() + ") don't have any linked visas. Exiting.");
			return null;
		}
		Collection<Card> visas = fetchCards(visasIds, Collections.singleton(VISA_PERSON_ID), true);
		
		for(Card res : resolutions){
			
			//�������� ������ � ���� ������� ����� ���������� �� ����������
			Person author = ((PersonAttribute) res.getAttributeById(AUTHOR_ID)).getPerson();
			PersonAttribute executionSenders = (PersonAttribute) res.getAttributeById(EXECUTION_SENDERS_ID);
			executionSenders.setValues(null);
			
			for(Card visa : visas) visasBlock:{
				//���� ���� �� � �������� �������, ������ �� ��� �� ���������
				if(!VISAS_ALLOWED_STATES.contains(visa.getState())) continue;
				
				//�����������
				List<Person> visaPersons = (List<Person>) ((PersonAttribute) visa.getAttributeById(VISA_PERSON_ID)).getValues();
				if(visaPersons != null && !visaPersons.isEmpty()) {
					
					//���� �� ��������� ��� ����������
					List<Person> assistants = findAssistants(visaPersons.get(0).getId());
					if(assistants != null && !assistants.isEmpty()) visaPersons.addAll(assistants);
					for(Person person : visaPersons){
						
						//���� ����� ��� ���� ����� ���������
						if(person.getId().equals(author.getId())){
							executionSenders.setValues(visaPersons);	
							break visasBlock;
						}
					}
				}
			}
			
			doOverwriteCardAttributes(res.getId(), executionSenders);
		}	
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private List<Person> findAssistants(ObjectId personId) throws DataException{
		Search search = new Search();
		search.setByAttributes(true);
		search.setTemplates(Collections.singleton(WORKPLACE_ID));
		search.addPersonAttribute(OWNER_ID, personId);
		SearchResult.Column column = new SearchResult.Column();
		column.setAttributeId(ASSISTANTS_ID);
		search.setColumns(Collections.singleton(column));
		ActionQueryBase searchQuery = getQueryFactory().getActionQuery(Search.class);
		searchQuery.setAction(search);
		SearchResult result = (SearchResult) getDatabase().executeQuery(getSystemUser(), searchQuery);
		if(result.getCards() == null || result.getCards().isEmpty()) return null;
		PersonAttribute assistants = (PersonAttribute)((Card) result.getCards().get(0)).getAttributeById(ASSISTANTS_ID);
		return assistants != null ? (List<Person>) assistants.getValues() : null;
	}
}
