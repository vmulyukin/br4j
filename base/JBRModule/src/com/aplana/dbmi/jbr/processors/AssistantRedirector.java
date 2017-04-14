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

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.ProcessorBase;

/**
 * @comment AbdullinR
 * ��������: ������� �������� ��� ��������� ����������.
 * (!) ����������� ������������� ��� �������� ��������� (365) � 
 * ������������� (504) (����� ���������� �Cannot process template ��).
 * ��������:
 * �	���������� ������� ���������� (��� ������ 365) ��� ���������������� 
 * (��� ������� 504);
 * �	 ���� ������������ �� ������ ������������ ���������� �No person to process card ��;
 * �	�����, ������ ��������� ������ ������ (����� �������� ������� 
 * ���������� ��� ��� �������������(544), ��� ��������  ���������� ���� 
 * ������� ��������� �������);
 * �	���� ��������� ��� � ������ �� ������������,
 * �	���� ���� � ����������� ������� ������� �������� ��� �� 
 * ��������� ����������.
 */
public class AssistantRedirector extends ProcessorBase
{
	public static final ObjectId STATE_ASSISTANT = ObjectId.predefined(CardState.class, "boss.assistant");
	public static final ObjectId TEMPL_SETTINGS = ObjectId.predefined(Template.class, "boss.settings");
	public static final ObjectId TEMPL_EXAMINE = ObjectId.predefined(Template.class, "jbr.examination"); // 504
	public static final ObjectId TEMPL_SIGN = ObjectId.predefined(Template.class, "jbr.sign"); // 365
	public static final ObjectId ATTR_BOSS = ObjectId.predefined(PersonAttribute.class, "boss.owner");
	public static final ObjectId ATTR_ASSISTANT = ObjectId.predefined(PersonAttribute.class, "boss.assistant");
	public static final ObjectId ATTR_EXAMINATOR = ObjectId.predefined(PersonAttribute.class, "jbr.exam.person");
	public static final ObjectId ATTR_SIGNER = ObjectId.predefined(PersonAttribute.class, "jbr.sign.person");
	
	@Override
	public Object process() throws DataException {
		ChangeState move = (ChangeState) getAction();
		ObjectQueryBase query = getQueryFactory().getFetchQuery(Card.class);
		query.setId(move.getCard().getId());
		Card card = (Card) getDatabase().executeQuery(getSystemUser(), query);
		Person person = getTargetPerson(card);
		if (getAssistants(person).isEmpty())
			return move;
		return redirectToAssistant(move);
	}

	private ChangeState redirectToAssistant(ChangeState move) throws DataException {
		final ArrayList<WorkflowMove> foundList = new ArrayList<WorkflowMove>(6);
		final WorkflowMove wfm = CardUtils.findWorkFlowMoveX(
				move.getCard().getId(), STATE_ASSISTANT, foundList, 
				getQueryFactory(), getDatabase(), getSystemUser());
		if (wfm != null) {
			final ChangeState modified = move;
			modified.setWorkflowMove(wfm);
			modified.setCard(move.getCard());
			logger.info("Card " + move.getCard().getId().getId() + " redirected to assistant state "+ STATE_ASSISTANT);
			return modified;
		}

		// (!) ��� ��������: �������� �������� ��� � ������ ���������...
		if (!foundList.isEmpty()) {
			final WorkflowMove awfm = foundList.get(0);
			if ( STATE_ASSISTANT.equals(awfm.getFromState())) { // ��� � ������ ���������...
				logger.warn( "Card " + move.getCard().getId().getId() + " already at assitant-state: "+ STATE_ASSISTANT );
				return move;
			}
		}

		throw new IllegalStateException("No workflow move found for sending card " +
				move.getCard().getId().getId() + " to assistant");
		//return move;
	}

	private Collection<Person> getAssistants(Person person) throws DataException {
		final Collection<Person> assistants = new ArrayList<Person>();

		final Search search = new Search();
		search.setByAttributes(true);
		search.setTemplates(Collections.singletonList(DataObject.createFromId(TEMPL_SETTINGS)));
		search.addPersonAttribute(ATTR_BOSS, person.getId());

		search.setColumns(Collections.singletonList( CardUtils.createColumn(ATTR_ASSISTANT)));

		final List<Card> cards = CardUtils.execSearchCards(search, getQueryFactory(), getDatabase(), getSystemUser());
		if (cards == null || cards.isEmpty())
			return assistants;

		try {
			assistants.addAll( CardUtils.getAttrPersons( cards.get(0), ATTR_ASSISTANT));
		} catch (NullPointerException e) {}
	
		return assistants;

	}

	private Person getTargetPerson(Card card) throws DataException {
		Person person;
		if (TEMPL_EXAMINE.equals(card.getTemplate())) {
			person = ((PersonAttribute) card.getAttributeById(ATTR_EXAMINATOR)).getPerson();
		} else if (TEMPL_SIGN.equals(card.getTemplate())) {
			person = ((PersonAttribute) card.getAttributeById(ATTR_SIGNER)).getPerson();
		} else
			throw new IllegalStateException("Can't process template " + card.getTemplateName());
		if (person == null)
			throw new IllegalStateException("No person to process card " + card.getId().getId());
		ObjectQueryBase query = getQueryFactory().getFetchQuery(Person.class);
		query.setId(person.getId());
		return (Person) getDatabase().executeQuery(getSystemUser(), query);
	}
}
