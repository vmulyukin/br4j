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

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;

/**
 * @comment AbdullinR
 * ������������ ��� ��������������� �������� ������� �������� � �����������. 
 * ������� �� ����������� ���� �������� �� �������� (�.�. ������� ����������, 
 * �����, � �������  ��� �������� = ���).
 */
public class CommissionApprovalPassthrough extends AbstractCardProcessor
{
	private static final ObjectId ATTR_TERM =
		ObjectId.predefined(DateAttribute.class, "jbr.resolutionTerm");
	private static final ObjectId ATTR_INSPECTOR =
		ObjectId.predefined(PersonAttribute.class, "jbr.commission.inspector");
	private static final ObjectId ATTR_SUPERVISED =
		ObjectId.predefined(ListAttribute.class, "jbr.oncontrol");
	private static final ObjectId VALUE_SUPERVISED =
		ObjectId.predefined(ReferenceValue.class, "jbr.commission.control.yes");
	private static final ObjectId MOVE_APPROVE =
		ObjectId.predefined(WorkflowMove.class, "jbr.commission.confirm");

	@Override
	public Object process() throws DataException {
		
		Card card = getCard();
		DateAttribute term = (DateAttribute) card.getAttributeById(ATTR_TERM);
		ListAttribute supervised = (ListAttribute) card.getAttributeById(ATTR_SUPERVISED);
		PersonAttribute inspector = (PersonAttribute) card.getAttributeById(ATTR_INSPECTOR);
		// ���� "�� ��������" = "��", ������ "���������" � "����" - �� ��������� ���������
		// � ������� "���������"
		if (supervised.getValue() != null && VALUE_SUPERVISED.equals(supervised.getValue().getId()) &&
				inspector.getValues() != null && inspector.getValues().size() > 0 &&
				term.getValue() != null)
			return null;
		
		// ����� - ��������� � ������ "����������"
		ChangeState move = new ChangeState();
		move.setCard(card);
		move.setWorkflowMove((WorkflowMove) DataObject.createFromId(MOVE_APPROVE));
		ActionQueryBase query = getQueryFactory().getActionQuery(move);
		query.setAction(move);
		
		execAction(new LockObject(card));
		try {
			getDatabase().executeQuery(getSystemUser(), query);
		} finally {
			execAction(new UnlockObject(card));
		}
		
		logger.info("Commission " + getCardId() + " proceeded to 'Approved' state " +
				"because its attribute 'Term' is not set");
		return null;
	}

	private Card getCard() throws DataException {
		ChangeState move = (ChangeState) getAction();
		Card card = move.getCard();
		if (card.getAttributes() != null &&
				card.getAttributeById(ATTR_TERM) != null &&
				card.getAttributeById(ATTR_SUPERVISED) != null &&
				card.getAttributeById(ATTR_INSPECTOR) != null)
			return card;
		ObjectQueryBase query = getQueryFactory().getFetchQuery(Card.class);
		query.setId(move.getCard().getId());
		return (Card) getDatabase().executeQuery(getSystemUser(), query);
	}

}
