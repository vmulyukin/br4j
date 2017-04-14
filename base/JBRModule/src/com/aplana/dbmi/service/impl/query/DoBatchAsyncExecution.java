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
package com.aplana.dbmi.service.impl.query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

import org.springframework.util.CollectionUtils;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.BatchAsyncExecution;
import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;

public class DoBatchAsyncExecution extends ActionQueryBase implements WriteQuery, SmartQuery {
	private static final long serialVersionUID = 2L;
	private ObjectId documentLinkId = ObjectId.predefined(CardLinkAttribute.class, "jbr.main.doc");
	private TreeSet<String> cards;
	//������ �������� ������������ ������, ���. ����� �������� ���� � ���� ���������� �� ������������
	//������ � ������� ������ �������
	private int actionInExecute = 0;
	private ArrayList<ActionQueryBase> queries;
	/**
	 * � ������ �������� ������ ���������� �������� (����� ���� � ������ ����) � ��������� �� ������
	 * ��������������� ���� �� ������ (�������������� ��� ����������� ����� � �����. ������)
	 */
	@Override
	public Object processQuery() throws DataException {
		BatchAsyncExecution<?> action = getAction();
		for (ActionQueryBase aqb : queries) {
			getDatabase().executeQuery(getUser(), aqb);
			if (actionInExecute < action.getActions().size()-1)
				actionInExecute++;
		}
		queries = null;
		return null;
	}
	
	/**
	 * ���������� ���� � ������� ������������� ������
	 */
	@Override
	public String getEvent() {
		BatchAsyncExecution<?> action = getAction();
		if (action == null)
			return "chgState.action=null";
		if (action.getActions() == null || action.getActions().size() == 0)
			return "chgState.actions=null";
		final ObjectId logActionId = ((ChangeState)action.getActions().get(actionInExecute)).getWorkflowMove().getLogAction();
		if (logActionId == null)
			return "chgState.wfmId="+ ((ChangeState)action.getActions().get(actionInExecute)).getWorkflowMove().getId();
		if (logActionId.getId() == null)
			return "chgState.logId=null,wfmId="+ ((ChangeState)action.getActions().get(actionInExecute)).getWorkflowMove().getId();
		return (String) logActionId.getId();
	}
	
	/**
	 * ���������� ���� � ������� ������������� ������ �� ������ ���� ��������
	 */
	@Override
	public ObjectId getEventObject() {
		BatchAsyncExecution<?> action = getAction();
		return ((ChangeState)action.getActions().get(actionInExecute)).getObjectId();
	}

	@Override
	public void validate() throws DataException {
		super.validate();
		if (queries == null) {
			BatchAsyncExecution<?> action = getAction();
			queries = new ArrayList<ActionQueryBase>(action.getActions().size());
			for (Action<?> act : action.getActions()) {
				ActionQueryBase aqb = getQueryFactory().getActionQuery(act);
				aqb.setAccessChecker(null);
				aqb.setAction(act);
				aqb.setUser(getUser());
				aqb.setSessionId(getSessionId());
				queries.add(aqb);
				getDatabase().validate(getUser(), aqb);
			}
		}
	}

	@Override
	public Object calcDependencies() throws DataException {
		BatchAsyncExecution<?> action = getAction();
		if (action == null) {
			logger.error("Action is null. QueryId: " + getUid());
			return null;
		}
		if (documentLinkId == null && action.getAttrToParent() == null) {
			logger.error("AttrToParent is not set. QueryId: " + getUid());
			return null;
		}
		if (action.getAttrToParent() != null) {
			documentLinkId = action.getAttrToParent();
		}
		if (action.getActions() == null || action.getActions().isEmpty()) {
			logger.error("List of actions is "+action.getActions() == null ? "null" : "empty" + ". QueryId: " + getUid());
			return null;
		}
		if (cards == null) {
			cards = new TreeSet<String>();
			for (Action<?> act : action.getActions()) {
				Card c = ((ChangeState)act).getCard();
				Attribute attrToParent = c.getAttributeById(documentLinkId);
				if (attrToParent == null)
					continue;
				if(BackLinkAttribute.class.isAssignableFrom(documentLinkId.getType())) {
					ListProject lp = new ListProject(c.getId());
					lp.setAttribute(attrToParent.getId());
					ActionQueryBase aqb = getQueryFactory().getActionQuery(lp);
					aqb.setAccessChecker(null);
					aqb.setAction(lp);
					aqb.setUser(getUser());
					aqb.setSessionId(getSessionId());
					SearchResult sr = (SearchResult) getDatabase().executeQuery(getUser(), aqb);
					for (Object cc : sr.getCards()) {
						cards.add(((Card)cc).getId().getId().toString());
					}
				} else if(CardLinkAttribute.class.isAssignableFrom(documentLinkId.getType())) {
					for (ObjectId cc : ((CardLinkAttribute) attrToParent).getIdsLinked()) {
						cards.add(cc.getId().toString());
					}
				} else {
					logger.error("AttrToParent must be LinkAttribute. QueryId: " + getUid());
					return null;
				}
			}
		}
		return cards;
	}
	
	@Override
	public boolean isPossibleToAdd(SmartQuery sm) throws DataException {
		Object oDep = sm.calcDependencies();
		if (oDep instanceof Collection<?>) {
			Collection<?> param = (Collection<?>) oDep;
			Collection<?> myDep = (Collection<?>) calcDependencies();
			if (param != null && myDep != null 
				&& !param.isEmpty() && !myDep.isEmpty()
				&& CollectionUtils.containsAny(param, myDep)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		BatchAsyncExecution<Action<?>> action = getAction();
		ArrayList<Action<?>> actions = action.getActions();
		WorkflowMove wfm = ((ChangeState)actions.get(0)).getWorkflowMove();
		ObjectQueryBase oqb = null;
		CardState statusFrom = null;
		CardState statusTo = null;
		try {
			oqb = getQueryFactory().getFetchQuery(CardState.class);
			oqb.setId(wfm.getFromState());
			statusFrom = (CardState)getDatabase().executeQuery(getUser(), oqb);
			oqb.setId(wfm.getToState());
			statusTo   = (CardState)getDatabase().executeQuery(getUser(), oqb);
		} catch (DataException e) {
			logger.error("Can't fetch CardStates of workflow move " + wfm.getId());
}
		
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		String startDate = sdf.format(getQueryContainer().getCreationTime());
		
		StringBuilder sb = new StringBuilder();
		sb.append("� ").append(startDate);
		sb.append(" ������������ ").append(getUser().getPerson().getFullName());
		sb.append(" ��������(�) ��������: \"����� �������\" ��� ���������� � �������� ");
		for (Action<?> cs : actions) {
			sb.append(((ChangeState)cs).getCard().getId().getId());
			sb.append(", ");
		}
		sb.append(" [�� ������� ");
		sb.append(statusFrom!=null ? statusFrom.getName() : wfm.getFromState().getId());
		sb.append(" � ");
		sb.append(statusTo!=null ? statusTo.getName() : wfm.getToState().getId());
		sb.append("]");
		return sb.toString();
	}
}
