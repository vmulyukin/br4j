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

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.jbr.processors.DoDependentChangeState;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.DatabaseClient;
import org.springframework.jdbc.core.JdbcTemplate;

public class DoExamDependentChangeState extends DoDependentChangeState implements DatabaseClient {

    private JdbcTemplate jdbcTemplate;

	private static final long serialVersionUID = 2072344873997869939L;
	
	// personattribute.jbr.exam.person=JBR_RASSM_PERSON = "���������������"
	static final ObjectId examPersonAttrId = ObjectId.predefined(PersonAttribute.class, "jbr.exam.person");
	// AUTHOR = "����� ��������"
	static final ObjectId authorAttrId = new ObjectId(PersonAttribute.class, "AUTHOR");
	// jbr.resolution.FioSign = "��������� �������"
	static final ObjectId signAttrId = ObjectId.predefined(PersonAttribute.class, "jbr.resolution.FioSign");

	static final ObjectId examStateId = ObjectId.predefined(CardState.class, "consideration");
	static final ObjectId executionStateId = ObjectId.predefined(CardState.class, "execution");
	static final ObjectId draftStateId = ObjectId.predefined(CardState.class, "draft");

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    //TODO: ����������� ����� ���� � ��� �� �������� ����������� � ������� processDependentCard � getDependentCards. ��-����� ���������� ������ ���� ��� ���� getDependentCards
    // 		� processDependentCard ����� �������� ������ ����� ������� ��������, � ��� �������� (�������) ������ ��������� � getDependentCards
    //		��. ��� ���������� ��� ������ � ������������ ������ DoDependentChangeState
	protected void processDependentCard(Card card, Card actionCard, HashSet<ObjectId> sourceStateIds, ObjectId targetStateId, Card parentCard) throws DataException {
		final PersonAttribute examPerson = (PersonAttribute)actionCard.getAttributeById(examPersonAttrId);
		final PersonAttribute assistantsPerson = CardUtils.retrieveAssistantsByProfile(examPerson, jdbcTemplate );
		final Card actualCard = CardUtils.loadCard(card.getId(), getQueryFactory(), getDatabase(), getSystemUser());
		final PersonAttribute author = (PersonAttribute) actualCard.getAttributeById(authorAttrId);
		final PersonAttribute signer = (PersonAttribute) actualCard.getAttributeById(signAttrId);
		// (YNikitin, 2013/05/20) �������� �������, ��� ��������������� ��� ��� �������� ������ ��������� �� ������ � ������� ���������, �� � � ����������� ������� (��� ������-������� ����������� �� ������ �������� ���������, ������� � �������� ����, ������������ ���������)
		if (author.intersectionValue(examPerson) || author.intersectionValue(assistantsPerson)||signer.intersectionValue(examPerson) || signer.intersectionValue(assistantsPerson)) {
			if (examStateId.equals(parentCard.getState())) {
				doSafeChangeState(parentCard, findMove(parentCard, executionStateId));
				// ���, ����� �� ������� � ���������������� �������� � ������������ ������
				parentCard.setState(executionStateId);
			}
			super.processDependentCard(card, actionCard, sourceStateIds, targetStateId, parentCard);
		}
	}
	
	/**
	 * �������������� ������ �������� ��������� ��������. � ������ ��������� �������� ������� ������ ��,
	 * � ������� ����� ��� ��������� ������� ��������� � ���������������\���������� ���������������� � � ������� �������� ��� ����������,
	 * �.�. ������ ��������������� (��� ��������) ��� �������� ����� �������� ������������ ������ ������� ��������� ��� ���� � ��� �����������.
	 * �� ������� BR4J00026261
	 */
	@Override
	protected Collection<Card> getDependentCards(Card baseCard) throws DataException {
		Collection<Card> linkedCards;
		if (CardLinkAttribute.class.equals(linkId.getType())) {
			linkedCards = getLinkedCards((CardLinkAttribute) baseCard.getAttributeById(linkId));
		} else {
			/*BackLinkAttribute*/
			linkedCards = getBackLinkedCards(linkId, baseCard.getId());
		}
		ArrayList<Card> result = new ArrayList<Card>();
		Card actionCard = ((ChangeState) getAction()).getCard();
		final PersonAttribute examPerson = (PersonAttribute)actionCard.getAttributeById(examPersonAttrId);
		final PersonAttribute assistantsPerson = CardUtils.retrieveAssistantsByProfile(examPerson, jdbcTemplate);
		for (Card card : linkedCards) {
			final Card actualCard = CardUtils.loadCard(card.getId(), getQueryFactory(), getDatabase(), getSystemUser());
			final PersonAttribute author = (PersonAttribute) actualCard.getAttributeById(authorAttrId);
			final PersonAttribute signer = (PersonAttribute) actualCard.getAttributeById(signAttrId);
			// (YNikitin, 2013/05/20) �������� �������, ��� ��������������� ��� ��� �������� ������ ��������� �� ������ � ������� ���������, �� � � ����������� ������� (��� ������-������� ����������� �� ������ �������� ���������, ������� � �������� ����, ������������ ���������)
			if (author.intersectionValue(examPerson) || author.intersectionValue(assistantsPerson)|| signer.intersectionValue(examPerson) || signer.intersectionValue(assistantsPerson)) {
				if (draftStateId.equals(card.getState()) || executionStateId.equals(card.getState())) {
					result.add(card);
				}
			}
		}
		return result;
	}
}
