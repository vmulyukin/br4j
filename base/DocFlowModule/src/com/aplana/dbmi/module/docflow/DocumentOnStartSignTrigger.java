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

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.Database;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.QueryFactory;
import com.aplana.dbmi.service.impl.UserData;

public class DocumentOnStartSignTrigger extends DocumentTrigger {
	private static final long serialVersionUID = 1L;
	
	public static final ObjectId authorAttrId = ObjectId.predefined(PersonAttribute.class, "author");
	public static final ObjectId signatoryAttrId = ObjectId.predefined(PersonAttribute.class, "jbr.outcoming.signatory");
	public static final ObjectId finishSignWfmId = ObjectId.predefined(WorkflowMove.class, "jbr.interndoc.sign.before-registration");
	
	public Object process() throws DataException {

		ChangeState move = (ChangeState) getAction();
		ObjectId cardId = null;
		if (move != null){
			cardId = move.getCard().getId();			
		}else{
			cardId = getObject().getId();
		}
		
		// ������������ �������� �� ����
		ObjectQueryBase cardQuery = getQueryFactory().getFetchQuery(Card.class);
		cardQuery.setId(cardId);
		Card card = (Card)getDatabase().executeQuery(getSystemUser(), cardQuery);

		PersonAttribute authorAttr = (PersonAttribute) card.getAttributeById(authorAttrId);
		PersonAttribute signatoryAttr = (PersonAttribute) card.getAttributeById(signatoryAttrId);
		
		boolean forceSign = (authorAttr.getPerson().getCardId().equals(signatoryAttr.getPerson().getCardId()));
		if (forceSign) {
			processor = null;
		}

		// � ����� ������ ���� ����� ��������� �� ��������� � ��������, ����� ��������� ��������� FillSignCommentAndMoveSingnToSignedState
		Object result = super.process();
		
		if (forceSign) {
			//��������� �������� �� �����������
			card = (Card)getDatabase().executeQuery(getSystemUser(), cardQuery);
			execAction(new LockObject(card), getSystemUser());
			try {
				QueryFactory queryFactory = getQueryFactory();
				Database database = getDatabase();
				ObjectQueryBase wfMoveQuery = queryFactory.getFetchQuery(WorkflowMove.class);
				wfMoveQuery.setId(finishSignWfmId);
				WorkflowMove wfMove = (WorkflowMove)database.executeQuery(getSystemUser(), wfMoveQuery);		
				ChangeState changeState = new ChangeState();
				changeState.setCard(card);
				changeState.setWorkflowMove(wfMove);
				ActionQueryBase changeStateQuery = queryFactory.getActionQuery(changeState);
				changeStateQuery.setAccessChecker(null);
				changeStateQuery.setAction(changeState);
				database.executeQuery(getSystemUser(), changeStateQuery);
			}
			finally {
				execAction(new UnlockObject(card.getId()), getSystemUser());
			}
		}
		
		return result;
	}

	public Object execAction(Action action, UserData user) throws DataException {
		final ActionQueryBase query = getQueryFactory().getActionQuery(action);
		query.setAction(action);
		return getDatabase().executeQuery(user, query);
	}
}