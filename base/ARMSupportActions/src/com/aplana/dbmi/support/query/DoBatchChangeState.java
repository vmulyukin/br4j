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
package com.aplana.dbmi.support.query;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.OverwriteCardAttributes;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.SaveQueryBase;
import com.aplana.dbmi.service.impl.UserData;
import com.aplana.dbmi.service.impl.query.SmartQuery;
import com.aplana.dbmi.service.impl.query.WriteQuery;
import com.aplana.dbmi.support.action.BatchChangeState;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;

public class DoBatchChangeState extends ActionQueryBase implements WriteQuery, SmartQuery {
	private static final long serialVersionUID = 1L;
	protected final Log logger = LogFactory.getLog(getClass());
	public static final ObjectId PREV_APPEALS_ID = ObjectId.predefined(CardLinkAttribute.class, "jbr.previous.appeals");
	private ObjectId documentLinkId = null;
	private TreeSet<String> cards;
	
	@Override
	public String getEvent() {
		return "BATCH_CHANGE_STATE";
	}
	
	@Override
	public Object processQuery() throws DataException {
		BatchChangeState action = getAction();
		Card card = loadCardById(getSystemUser(), action.getCard().getId());
			
		ChangeState changeStateAction = new ChangeState();
		changeStateAction.setCard(card);
		changeStateAction.setWorkflowMove(action.getWorkflowMove());
		changeStateAction.setLastDialogOk(action.isLastDialogOk());
		
		// ��������� �������
		if (!action.isHaveReservationRequests()){
			ActionQueryBase aqb = getQueryFactory().getActionQuery(changeStateAction);
			aqb.setAccessChecker(null);
			aqb.setAction(changeStateAction);
			getDatabase().executeQuery(getUser(), aqb);
		}		
		return null;
	}

	protected boolean storeCardSyncHandler(Card card) throws DataException {
		SaveQueryBase aqb = getQueryFactory().getSaveQuery(card);
		aqb.setObject(card);
		aqb.setAsync(false);
		aqb.setSessionId(getSessionId());
		final ObjectId id = getDatabase().executeQuery(getUser(), aqb);
		card.setId(Long.parseLong("" + id.getId()));
		return true;
	}

	private Card loadCardById(UserData user,  ObjectId cardId) throws DataException {
		if (cardId == null || cardId.getId() == null)
			return null;
		final ObjectQueryBase cardQuery = getQueryFactory().getFetchQuery(Card.class);
		// TODO: ����� �������� �������� ���� ������� ��� � �����-�� ���, ����� ��������� ���-�� ������
		cardQuery.setAccessChecker(null);
		cardQuery.setId(cardId);
		cardQuery.setSessionId(getSessionId());
		return (Card) getDatabase().executeQuery( user, cardQuery);
	}
	
	/**
	 * �������������� ����� �������� ������, ����� ������� � ��������� ���������� ������� �������� �
	 * ��������� ��������, � ����� ������������� ���������� � ��������� ����� ����� �������
	 */
	@Override
	public void validate() throws DataException {
		super.validate();
		BatchChangeState action = getAction();

		Card card = action.getCard();
		//���� � ������ ��������������, �� ��������� �������� 
		if (action.isEditMode()) {
			storeCardSyncHandler(card);
			card = loadCardById(getSystemUser(), action.getCard().getId());
		}
		
		//��������� ��� �������� ������������� �� �������������� ��������
		// (���������� � ����� ������ ����� � ����������� ����� ���� �������� ��������
		for (Card activeCard : action.getOpenActiveCards()) {
			storeCardSyncHandler(activeCard);
		}
		
		if(action.getDublicates()!=null&&!action.getDublicates().isEmpty()){
			try{	
				CardLinkAttribute prevAppeals = card.getAttributeById(PREV_APPEALS_ID);
				prevAppeals.setIdsLinked(action.getDublicates());
				final OverwriteCardAttributes writer = new OverwriteCardAttributes();
				writer.setCardId(card.getId());
				writer.setAttributes(Collections.singletonList(prevAppeals));
				ActionQueryBase aqb = getQueryFactory().getActionQuery(writer);
				aqb.setAction(writer);
				aqb.setAsync(false);
				aqb.setSessionId(getSessionId());
				getDatabase().executeQuery(getUser(), aqb);
			} catch (DataException e) {
				throw e;
			} catch (Exception e) {
				throw new DataException(e);
			}
		}
		
		ChangeState changeStateAction = new ChangeState();
		changeStateAction.setCard(card);
		changeStateAction.setWorkflowMove(action.getWorkflowMove());
		changeStateAction.setLastDialogOk(action.isLastDialogOk());
			
		// ��������� ����� ����������� ��������������� ��� ���� ��������. ��� ���������� ��� ����, ����� ����� ����������� ������� ����� ������� ������� ����������� ���������� �������� ��� ����������� 
		if (!action.isHaveReservationRequests()){
			ActionQueryBase aqb = getQueryFactory().getActionQuery(changeStateAction);
			aqb.setAccessChecker(null);
			aqb.setAction(changeStateAction);
			aqb.setUser(getUser());
			aqb.setSessionId(getSessionId());
			getDatabase().validate(getUser(), aqb);
		}
	}

	/**
	 * ��������� ����, ���� �� ������� ��� ������� ��� ���������� query � �������.
	 * ������� ����� ����������� ����� ����� ������ DoBatchAsyncExecution (�� ���������
	 * SmartQuery), ������� ������������� � ���� ��� �������� ���������\������������ � �����.
	 * ������ DoBatchChangeState ���� ��������� SmartQuery, �� ������������ ����� ������ ��� ��������
	 * ��������� �� ���������� ��� ������������ �� ���������� (������� � ������ ����).
	 * �� ������ BR4J00035440 (����� ������������� �������� � ���������� ��������� � ������������
	 * �� ������ � ���� �� �� �� ��������� � ����������� ����������������� � lockManagement).
	 */
	@Override
	public Object calcDependencies() throws DataException {
		BatchChangeState action = getAction();
		if (action == null) {
			logger.error("Action is null. QueryId: " + getUid());
			return null;
		}
		Card c = action.getCard();
		WorkflowMove wfm = action.getWorkflowMove();
		
		if (documentLinkId == null && action.getAttrToParent() == null) {
			logger.error("AttrToParent is not set. QueryId: " + getUid());
			return null;
		}
		if (action.getAttrToParent() != null) {
			documentLinkId = action.getAttrToParent();
		}
		
		if ((c.getTemplate().equals(ObjectId.predefined(Template.class, "jbr.resolution"))
		    && wfm.getId().equals(ObjectId.predefined(WorkflowMove.class, "jbr.commission.execute1"))) || 
		    (c.getTemplate().equals(ObjectId.predefined(Template.class, "jbr.rassm"))
				    && wfm.getId().equals(ObjectId.predefined(WorkflowMove.class, "jbr.exam.execute")))) {
			if (cards == null) {
				Attribute attrToParent = c.getAttributeById(documentLinkId);
				if (attrToParent == null)
					return null;
				cards = new TreeSet<String>();
				if(BackLinkAttribute.class.equals(documentLinkId.getType())) {
					ListProject lp = new ListProject(c.getId());
					lp.setAttribute(attrToParent.getId());
				
					ActionQueryBase aqb = getQueryFactory().getActionQuery(lp);
					aqb.setAccessChecker(null);
					aqb.setAction(lp);
					aqb.setUser(getUser());
					aqb.setSessionId(getSessionId());
					SearchResult sr = getDatabase().executeQuery(getUser(), aqb);
					for (Card cc : sr.getCards()) {
						cards.add(cc.getId().getId().toString());
					}
				} else if(CardLinkAttribute.class.equals(documentLinkId.getType())) {
					for (ObjectId cc : ((CardLinkAttribute) attrToParent).getIdsLinked()) {
						cards.add(cc.getId().toString());
					}
				} else {
					logger.error("AttrToParent must be LinkAttribute. QueryId: " + getUid());
					return null;
				}
			}
			return cards;
		}
		return null;
	}
	
	@Override
	public boolean isPossibleToAdd(SmartQuery sm) throws DataException {
		Object oDep = sm.calcDependencies();
		if (oDep instanceof Collection<?>) {
			Collection<?> param = (Collection<?>) oDep;
			Collection<?> myDep = (Collection<?>) calcDependencies();
			if (myDep != null
				&& !param.isEmpty() && !myDep.isEmpty()
				&& CollectionUtils.containsAny(param, myDep)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		BatchChangeState action = getAction();
		Card c = action.getCard();
		WorkflowMove wfm = action.getWorkflowMove();
		ObjectQueryBase oqb;
		CardState statusFrom = null;
		CardState statusTo = null;
		try {
			oqb = getQueryFactory().getFetchQuery(CardState.class);
			oqb.setId(wfm.getFromState());
			statusFrom = getDatabase().executeQuery(getUser(), oqb);
			oqb.setId(wfm.getToState());
			statusTo   = getDatabase().executeQuery(getUser(), oqb);
		} catch (DataException e) {
			logger.error("Can't fetch CardStates of workflow move " + wfm.getId());
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		String startDate = sdf.format(getQueryContainer().getCreationTime());
		
		StringBuilder sb = new StringBuilder();
		sb.append("� ").append(startDate);
		sb.append(" ������������ ").append(getUser().getPerson().getFullName());
		sb.append(" ��������(�) ��������: \"����� �������\" ��������� \"");
		sb.append(c.getAttributeById(ObjectId.predefined(StringAttribute.class, "name")).getStringValue());
		sb.append("\" (��� = ").append(c.getId().getId()).append(")");
		sb.append(" [�� ������� ");
		sb.append(statusFrom!=null ? statusFrom.getName() : wfm.getFromState().getId());
		sb.append(" � ");
		sb.append(statusTo!=null ? statusTo.getName() : wfm.getToState().getId());
		sb.append("]");
		return sb.toString();
	}
}