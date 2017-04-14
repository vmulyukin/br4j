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

import java.util.Iterator;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.HtmlAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.Database;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.ProcessorBase;
import com.aplana.dbmi.service.impl.QueryFactory;
import com.aplana.dbmi.service.impl.SaveQueryBase;
import com.aplana.dbmi.service.impl.UserData;

/**
 * �������������, ������������ ����� ����������� �������� �������� ������������
 * ��������� ��� ������������ � ��������
 *
 */
public class CancalNegotiation extends ProcessorBase {
	private static final long serialVersionUID = 2L;
	
	public static final ObjectId VISA_LIST = ObjectId.predefined(CardLinkAttribute.class, "jbr.visa.set");
	public static final ObjectId AGREEMENT_STATE = ObjectId.predefined(CardState.class, "agreement");
	public static final ObjectId BOSS_ASISTENT_STATE = ObjectId.predefined(CardState.class, "boss.assistant");
	public static final ObjectId VISA_CEASE_MOVE = ObjectId.predefined(WorkflowMove.class, "jbr.visa.cease");
	public static final ObjectId VISA_CEASE_MOVE_BOSS_ASISTENT = ObjectId.predefined(WorkflowMove.class, "jbr.visa.cancel.by.boss.assistent");
	public static final ObjectId VISA_DECISION = ObjectId.predefined(HtmlAttribute.class, "jbr.visa.decision");

	/**
	 * ����� ����������
	 */
	public Object process() throws DataException {
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
		/*ObjectQueryBase query = getQueryFactory().getActionQuery(unlock);
		query.setAction(unlock);
		getDatabase().executeQuery(user, query);*/
		/*--------------------------------------------------*/

		ChangeState action = (ChangeState)getAction();

		//�������� ��������
		ObjectQueryBase objectQuery = queryFactory.getFetchQuery(Card.class);
		objectQuery.setId(action.getObjectId());
		Card document = (Card)database.executeQuery(user, objectQuery);
		
		CardLinkAttribute linkVISA = (CardLinkAttribute) document.getAttributeById(VISA_LIST);


		
		//���� �� �����
		Iterator<ObjectId> visaIdsIter = linkVISA.getIdsLinked().iterator();
		while (visaIdsIter.hasNext()){
			ObjectId visaId = visaIdsIter.next();
			ActionQueryBase changeStateQuery = queryFactory.getActionQuery(ChangeState.class);
			ChangeState changeState = new ChangeState();

			//�������� ����
			ObjectQueryBase visaObjectQuery = queryFactory.getFetchQuery(Card.class);
			visaObjectQuery.setId(visaId);
			Card visa = (Card)database.executeQuery(user, visaObjectQuery);
			
			//������������ ������ ���� �� ������������
			if ((visa.getState().equals(AGREEMENT_STATE)) || (visa.getState().equals(BOSS_ASISTENT_STATE))){
				doAction(new LockObject(visa.getId()));
				
				//������������� �������� ���� �������
				try{
					((StringAttribute)visa.getAttributeById(VISA_DECISION)).setValue("Cancal");
					SaveQueryBase saveObjectQuery = queryFactory.getSaveQuery(visa);
					saveObjectQuery.setObject(visa);
					database.executeQuery(user, saveObjectQuery);
				}finally{
					doAction(new UnlockObject(visa.getId()));
				}
				
				//������ ������ ����
				if (visa.getState().equals(AGREEMENT_STATE)){
					//�������� �������
					ObjectQueryBase wfMoveQuery = queryFactory.getFetchQuery(WorkflowMove.class);
					wfMoveQuery.setId(VISA_CEASE_MOVE);
					WorkflowMove wfMove = (WorkflowMove)database.executeQuery(user, wfMoveQuery);		
					changeState.setCard(visa);
					changeState.setWorkflowMove(wfMove);
					changeStateQuery.setAction(changeState);
					database.executeQuery(user, changeStateQuery);
				}else{
					//�������� �������
					ObjectQueryBase wfMoveQuery = queryFactory.getFetchQuery(WorkflowMove.class);
					wfMoveQuery.setId(VISA_CEASE_MOVE_BOSS_ASISTENT);
					WorkflowMove wfMove = (WorkflowMove)database.executeQuery(user, wfMoveQuery);		
					changeState.setCard(visa);
					changeState.setWorkflowMove(wfMove);
					changeStateQuery.setAction(changeState);
					database.executeQuery(user, changeStateQuery);
				}
				
			}
		}
		setUser(currentUser); // �������� ���������, ��������� ����� ������ �� �����
		return null;
	}
	
	/**
	 * ����� ��������� ��������
	 * @param ����������� ��������
	 * @return
	 * @throws DataException
	 */
	protected Object doAction(Action action) throws DataException {
		ActionQueryBase query = getQueryFactory().getActionQuery(action);
		UserData user = getSystemUser();
		query.setAction(action);
		return getDatabase().executeQuery(user, query);
	}	
}
