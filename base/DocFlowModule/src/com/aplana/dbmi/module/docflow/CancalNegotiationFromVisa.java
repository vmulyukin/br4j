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
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.HtmlAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.Database;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.Parametrized;
import com.aplana.dbmi.service.impl.ProcessorBase;
import com.aplana.dbmi.service.impl.QueryFactory;
import com.aplana.dbmi.service.impl.UserData;

/**
 * �������������, �������� ������ �������� ����� ��� ���������� ����/�������.
 * 
 */
public class CancalNegotiationFromVisa extends ProcessorBase implements Parametrized{
	private static final long serialVersionUID = 1L;
	
	public static final ObjectId VISA_PARENT = ObjectId.predefined(BackLinkAttribute.class, "jbr.visa.parent");
	public static final ObjectId NEGOTIATION_CANCEL = ObjectId.predefined(WorkflowMove.class, "jbr.ord.cancal_negotiation_from_negotiation");
	public static final ObjectId VISA_DECISION = ObjectId.predefined(HtmlAttribute.class, "jbr.visa.decision");
	public static final ObjectId SIGN_PARENT = ObjectId.predefined(BackLinkAttribute.class, "jbr.sign.parent");
	public static final ObjectId SIGN_CANCEL = ObjectId.predefined(WorkflowMove.class, "jbr.ord.cancal_negotiation_from_signing");
	public static final String PARAM_LINK_TYPE = "typeOfLinkedCards";
	private int linkType = 1;
	/**
	 * ����� ����������
	 */
	public Object process() throws DataException {
		if(linkType == 0) return null;
		ObjectId wfMoveId;
		ObjectId backlinkAttr;
		if(linkType == 1) {
			wfMoveId = NEGOTIATION_CANCEL;
			backlinkAttr = VISA_PARENT;	
		} else { //if(linkType == 2)
			wfMoveId = SIGN_CANCEL;
			backlinkAttr = SIGN_PARENT;	
		}
		QueryFactory queryFactory = getQueryFactory();
		Database database = getDatabase();
		/*UserData user = getUser();*/
		/* �������� ��������� ��� ��������� �������������.
		 * �.�. 03.01.2010
		 */
		UserData currentUser = getUser();
		UserData user = new UserData();
		user.setPerson(getDatabase().resolveUser(Database.SYSTEM_USER));
		user.setAddress("internal");
		setUser(user);
		ChangeState action = (ChangeState)getAction();

		//�������� ��������
		ObjectQueryBase objectQuery = queryFactory.getFetchQuery(Card.class);
		objectQuery.setId(action.getObjectId());
		Card visa = (Card)database.executeQuery(user, objectQuery);
		
		ObjectId result = null;
		final BackLinkAttribute documentBackLinkAttr = (BackLinkAttribute) visa.getAttributeById(backlinkAttr);
		ListProject listAction = new ListProject();
		listAction.setCard(visa.getId());
		listAction.setAttribute(documentBackLinkAttr.getId());
		Object execResult = execAction(listAction);
		if (execResult instanceof SearchResult) {
			SearchResult searchResult = (SearchResult)execResult;
			//�������� �� ������� ���������� � ���������. ���� ��� �� ������ ����� ��������� ��������
			//��� ���������� ���������
			if (searchResult.getCards().size() > 0){
				Card document = (Card)searchResult.getCards().get(0);
				result = document.getId();
			}
		}

		objectQuery = queryFactory.getFetchQuery(Card.class);
		objectQuery.setId(result);
		Card doc = (Card)database.executeQuery(user, objectQuery);
		
		
		//�������� �������
		ObjectQueryBase wfMoveQuery = queryFactory.getFetchQuery(WorkflowMove.class);
		wfMoveQuery.setId(wfMoveId);
		WorkflowMove wfMove = (WorkflowMove)database.executeQuery(user, wfMoveQuery);	
		
		
		//������ ������ ���������
		execAction(new LockObject(doc));
		try {
			ChangeState changeState = new ChangeState();
			ActionQueryBase changeStateQuery = queryFactory.getActionQuery(ChangeState.class);
			changeState.setCard(doc);
			changeState.setWorkflowMove(wfMove);
			changeStateQuery.setAction(changeState);
			database.executeQuery(user, changeStateQuery);
			setUser(currentUser); // �������� ���������, ��������� ����� ������ �� �����
		} finally {
			execAction(new UnlockObject(doc));
		}
		
		return null;
	}
	
	private Object execAction(Action action) throws DataException {
		final ActionQueryBase query = getQueryFactory().getActionQuery(action);
		query.setAction(action);
		return getDatabase().executeQuery(getSystemUser(), query);
	}
	
	public void setParameter(String name, String value) {
		if (PARAM_LINK_TYPE.equalsIgnoreCase(name)) {
			if (value.equalsIgnoreCase("visa")) 
				linkType = 1;
			else if(value.equalsIgnoreCase("sign")) 
				linkType = 2;
			else 
				linkType = 0;
		}
	}
}