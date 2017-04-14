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

import java.util.Iterator;
import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ObjectQueryBase;


/*
 * ����� ����������, ��������� ��� �������� ��������� �� ������� "������������" � ������ "����������" 
 * ���������� ����� �������� ����� ��������� �� ������� "��������" � ������ "����������".
 */
public class ChangingOfFilesStatusToOutdated extends ProcessCard {
	private static final long serialVersionUID = 1L;
	
	public static final ObjectId DEFAULT_FILE_LINKS = ObjectId.predefined(CardLinkAttribute.class, "jbr.files");
	public static final ObjectId MOVE_ID = ObjectId.predefined(WorkflowMove.class, "jbr.files.active.to.outdated");
	public static final ObjectId ACTIVE_STATE_ID = ObjectId.predefined(CardState.class, "active");
	
	private static final String PARAM_FILE_LINK="filelink";
	
	private ObjectId fileLinkId = null;

	@Override
	public Object process() throws DataException {
		Card document = getCard();
		
		if(fileLinkId==null) {
			fileLinkId=DEFAULT_FILE_LINKS;
		}
				
		CardLinkAttribute fileLinks = document.getAttributeById(fileLinkId);
		
		if (fileLinks != null) {
			Iterator<ObjectId> iter = fileLinks.getIdsLinked().iterator();
	        while (iter.hasNext()) {
	            ObjectId fileId = iter.next();
	            
	            ObjectQueryBase objectQuery = getQueryFactory().getFetchQuery(Card.class);
				objectQuery.setId(fileId);
				// ��������� �������� ����� �� ����� ���������� ������������
				Card file = getDatabase().executeQuery(getSystemUser(), objectQuery);
				
				if(ACTIVE_STATE_ID.equals(file.getState()))	{
					execAction( new LockObject(fileId), getSystemUser());
					try {
						changeStatus(file, MOVE_ID);
					} finally {			
						execAction( new UnlockObject(fileId), getSystemUser());
					}
				}				
	        }
		}
		return null;
	}
	
	@Override
	public void setParameter(String name, String value) {
		if(name.equals(PARAM_FILE_LINK)) {
			fileLinkId = ObjectId.predefined(CardLinkAttribute.class, value);
		} else {
			super.setParameter(name, value);
		}
	}
	
	/*
	 * ����� ��������� ������� ����� � ������ "����������"
	*/ 
	private Object changeStatus(Card card, ObjectId moveId) throws DataException {
		ChangeState action = new ChangeState();
		action.setCard(card);
		ObjectQueryBase query = getQueryFactory().getFetchQuery(WorkflowMove.class);
		query.setId(moveId);
		WorkflowMove move = getDatabase().executeQuery(getSystemUser(), query);
		action.setWorkflowMove(move);
		return execAction(action, getSystemUser());
    }
}
