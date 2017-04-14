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

import java.util.Date;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;

/**
 * @author Aleksandr Smirnov
 *
 * ������� �������� ���������� ���������. ����������� � ������ �������� ���������
 * � ������ "����������". �������� ��������� ��� �������, ��� �� ��������� �����������
 * � ������������� , � �������� ������� �����������.
 *
 */
public class CreateOutgoingDoc extends ProcessCard {

	@Override
	public Object process() throws DataException {

		Card oldCard = getCard();

		PersonAttribute executors = (PersonAttribute) oldCard.getAttributeById(ObjectId.predefined(PersonAttribute.class, "jbr.AssignmentExecutor"));
		PersonAttribute coExecutor = (PersonAttribute) oldCard.getAttributeById(ObjectId.predefined(PersonAttribute.class, "jbr.CoExecutor"));
		CardLinkAttribute extExecutor = (CardLinkAttribute) oldCard.getAttributeById(ObjectId.predefined(CardLinkAttribute.class, "jbr.ExtExecutor"));

		// �������� ������� ��� ��������
		if (executors.getValues().isEmpty() && coExecutor.getValues().isEmpty() && extExecutor.getStringValue() != null){
			createNewCard(oldCard);
		}

		return null;
	}

	// ������� ����� �������� ���������� ���������
	private void createNewCard(Card oldCard) throws DataException{

		CreateCard createCard = new CreateCard(ObjectId.predefined(Template.class, "jbr.outcoming"));
		Card newCard = (Card)execAction(createCard);

		((PersonAttribute) newCard.getAttributeById(Attribute.ID_AUTHOR)).setPerson(getUser().getPerson());
		((DateAttribute) newCard.getAttributeById(Attribute.ID_CREATE_DATE)).setValue(new Date());
		//���������� ����������
		((CardLinkAttribute)newCard.getAttributeById(ObjectId.predefined(CardLinkAttribute.class, "jbr.outcoming.receiver")))
			.addIdsLinked(((CardLinkAttribute) oldCard.getAttributeById(ObjectId.predefined(CardLinkAttribute.class, "jbr.ExtExecutor"))).getIdsLinked());
		// ���������
		((PersonAttribute)newCard.getAttributeById(ObjectId.predefined(PersonAttribute.class, "jbr.outcoming.signatory")))
			.setPerson(((PersonAttribute)oldCard.getAttributeById(ObjectId.predefined(PersonAttribute.class, "jbr.resolution.FioSign"))).getPerson());
		// �����������
		((PersonAttribute)newCard.getAttributeById(ObjectId.predefined(PersonAttribute.class, "jbr.resolutionExecutor")))
			.setPerson(((PersonAttribute)oldCard.getAttributeById(ObjectId.predefined(PersonAttribute.class, "jbr.resolution.FioSign"))).getPerson());
		// ��� ���������
		((CardLinkAttribute)newCard.getAttributeById(ObjectId.predefined(CardLinkAttribute.class, "jbr.reg.doctype")))
			.addLinkedId(ObjectId.predefined(Card.class, "jbr.outcoming_new.DocType"));
		// ������ ������������
		((CardLinkAttribute)newCard.getAttributeById(ObjectId.predefined(CardLinkAttribute.class, "index")))
			.addLinkedId(ObjectId.predefined(Card.class, "jbr.outcoming_new.RegdIndex"));
		// ������� ���������� (������������ ������)
		((TextAttribute)newCard.getAttributeById(ObjectId.predefined(TextAttribute.class, "jbr.document.title")))
			.setValue(((TextAttribute) oldCard.getAttributeById(ObjectId.predefined(TextAttribute.class, "jbr.accomp.letter"))).getStringValue());
		// ��������
		((CardLinkAttribute)newCard.getAttributeById(ObjectId.predefined(CardLinkAttribute.class, "jbr.files")))
			.addIdsLinked(((CardLinkAttribute) oldCard.getAttributeById(ObjectId.predefined(CardLinkAttribute.class, "jbr.accomp.files"))).getIdsLinked());

		// ��������� ����� �������
		ObjectId newCardId = saveCard(newCard,getSystemUser());
		logger.info("Create new card with id = " + newCardId.getId().toString());

		// ����� ������ - ���������������
		ChangeState changeState = new ChangeState();
		WorkflowMove workflowMove = new WorkflowMove();
		workflowMove.setId(ObjectId.predefined(WorkflowMove.class, "jbr.outcoming.preparation.registration"));

		changeState.setCard(newCard);
		changeState.setWorkflowMove(workflowMove);
		execAction(changeState);

		// ������� ���������� � ������ ����������
		UnlockObject unlockObject = new UnlockObject();
		unlockObject.setId(newCardId);
		execAction(unlockObject);
	}

}
