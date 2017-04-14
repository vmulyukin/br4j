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
import java.util.List;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.GetWorkflowMovesFromTargetState;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.HtmlAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.Database;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.ProcessorBase;
import com.aplana.dbmi.service.impl.QueryFactory;
import com.aplana.dbmi.service.impl.UserData;

/**
 * �������������, ������������ ����� ���������� �������� "��������� �� ���������" �� �������� ����
 */
public class ReturnForRevisionNegotiationFromVisa extends ProcessorBase {
	private static final long serialVersionUID = 1L;
	
	public static final ObjectId VISA_PARENT = ObjectId.predefined(BackLinkAttribute.class, "jbr.visa.parent");
	public static final ObjectId DOC_PREPARATION_STATUS = ObjectId.predefined(CardState.class, "preparation");
	public static final ObjectId NEGOTIATION_WEITING_TO_DRAFT = ObjectId.predefined(WorkflowMove.class, "jbr.document.visa.weiting.to.draft");
	public static final ObjectId NEGOTIATION_PROCESSING_ASSISTENT_TO_DRAFT = ObjectId.predefined(WorkflowMove.class, "jbr.document.visa.processing.assistent.to.draft");
	public static final ObjectId NEGOTIATION_VISA_CEASE = ObjectId.predefined(WorkflowMove.class, "jbr.visa.cease");
	public static final ObjectId VISA_DECISION = ObjectId.predefined(HtmlAttribute.class, "jbr.visa.decision");
	public static final ObjectId VISA_LIST = ObjectId.predefined(CardLinkAttribute.class, "jbr.visa.set");
	public static final ObjectId ASSIGNED = ObjectId.predefined(CardState.class, "jbr.visa.assigned");
	public static final ObjectId WAITING = ObjectId.predefined(CardState.class, "jbr.visa.waiting");
	public static final ObjectId BOSS_ASSISTENT = ObjectId.predefined(CardState.class, "boss.assistant");
	
	/**
	 * ����� ����������
	 */
	@SuppressWarnings("unchecked")
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
		ObjectQueryBase q = queryFactory.getFetchQuery(Card.class);

		ChangeState action = (ChangeState)getAction();

		//�������� ��������
		ObjectQueryBase objectQuery = queryFactory.getFetchQuery(Card.class);
		objectQuery.setId(action.getObjectId());
		Card visa = (Card)database.executeQuery(user, objectQuery);
		
		final BackLinkAttribute documentBackLinkAttr = (BackLinkAttribute) visa.getAttributeById(VISA_PARENT);
		ListProject listAction = new ListProject();
		listAction.setCard(visa.getId());
		listAction.setAttribute(documentBackLinkAttr.getId());
		Object execResult = execAction(listAction);
		if (execResult instanceof SearchResult) {
			SearchResult searchResult = (SearchResult)execResult;
			//�������� �� ������� ���������� � ���������. ���� ��� �� ������ ����� ��������� ��������
			//��� ���������� ���������
			if (searchResult.getCards().size() > 0){
				ObjectId result = null;
				Card document = (Card)searchResult.getCards().get(0);
				result = document.getId();
				
				objectQuery = queryFactory.getFetchQuery(Card.class);
				objectQuery.setId(result);
				Card doc = (Card)database.executeQuery(user, objectQuery);
				
				//�������� ������� �. 08.01.2011
				//������������� �������� ��� ��������� �������� � ������� action GetWorkflowMovesFromTargetState
				//������� ��� ���� ����� �� ������ ��������� �������� ��� ����� ����� � ������ �� ������ �������� �������
				//��������� ��� ��� ���� �������.
				
				//�������� ������������� ��������
				ActionQueryBase getWorkflowQuery = queryFactory.getActionQuery(GetWorkflowMovesFromTargetState.class);
				GetWorkflowMovesFromTargetState getWorkflowMovesAction = new GetWorkflowMovesFromTargetState();  
				getWorkflowMovesAction.setCard(doc);
				getWorkflowMovesAction.setToStateId(DOC_PREPARATION_STATUS);
				getWorkflowQuery.setAction(getWorkflowMovesAction);
				List<Long> moveIds = (List<Long>)database.executeQuery(user, getWorkflowQuery);
				
				if (moveIds.size() < 1){
					throw new DataException("Can not find any workflow moves for card=" + doc.getId() + " to status=" + DOC_PREPARATION_STATUS);
				}
				
				//�������� �������
				ObjectQueryBase wfMoveQuery = queryFactory.getFetchQuery(WorkflowMove.class);					
				wfMoveQuery.setId(new ObjectId(CardState.class, moveIds.get(0).longValue()));
				WorkflowMove wfMove = (WorkflowMove)database.executeQuery(user, wfMoveQuery);
				
				//������ ������ ���������
				ChangeState changeState = new ChangeState();
				changeState.setCard(doc);
				changeState.setWorkflowMove(wfMove);
				ActionQueryBase changeStateQuery = queryFactory.getActionQuery(changeState);
				changeStateQuery.setAction(changeState);
				execAction(new LockObject(doc));
				try {
					database.executeQuery(user, changeStateQuery);
			
					//�������� ������ �� ����
					CardLinkAttribute linkVISA = (CardLinkAttribute) doc.getAttributeById(VISA_LIST);
					if (linkVISA != null) {
						Boolean doChangeState = false;
						//���� �� ���� �����
						Iterator<ObjectId> iterVISA = linkVISA.getIdsLinked().iterator();
						while (iterVISA.hasNext()) {
							ObjectId visaId = iterVISA.next();
							q.setId(visaId);
							//�������� �������� ����
							Card visaCard = (Card)database.executeQuery(user, q);
							//���� �������� ��������� � ����� �� �������� "�������� ������������", "������������", "��������� ����������", ��
							//������������� ������� �� ����� ������� � "��������" 
							if (visaCard.getState().equals(BOSS_ASSISTENT)){
								wfMoveQuery.setId(NEGOTIATION_PROCESSING_ASSISTENT_TO_DRAFT);
								doChangeState=true;
							}
							else if (visaCard.getState().equals(ASSIGNED)){
								wfMoveQuery.setId(NEGOTIATION_WEITING_TO_DRAFT);		   					
								doChangeState=true;
							}
							else if (visaCard.getState().equals(WAITING)){
								wfMoveQuery.setId(NEGOTIATION_VISA_CEASE);
								doChangeState=true;
							}
							//���� ������ � ���� �� ���������� ������� ��������� �������
							if(doChangeState){
								doChangeState=false;
								//�������� �������
								wfMove = (WorkflowMove)database.executeQuery(user, wfMoveQuery);	
			   				   				
								//������ ������ ����
								changeState = new ChangeState();
								changeStateQuery = queryFactory.getActionQuery(ChangeState.class);
								changeState.setCard(visaCard);
								changeState.setWorkflowMove(wfMove);
								changeStateQuery.setAction(changeState);
								execAction(new LockObject(visaCard));
								try {
									database.executeQuery(user, changeStateQuery);
								} finally {
									execAction(new UnlockObject(visaCard));
								}
							}
						}
					}
				} finally {
					execAction(new UnlockObject(doc));				}
			}
		}
		
		setUser(currentUser); // �������� ���������, ��������� ����� ������ �� �����
		return null;
		
	}
	
	private Object execAction(Action action) throws DataException {
		final ActionQueryBase query = getQueryFactory().getActionQuery(action);
		query.setAction(action);
		return getDatabase().executeQuery(getSystemUser(), query);
	}
}