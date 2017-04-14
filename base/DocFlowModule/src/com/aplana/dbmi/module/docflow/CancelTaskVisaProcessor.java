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

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.Database;
import com.aplana.dbmi.service.impl.UserData;

public class CancelTaskVisaProcessor extends DataServiceClient implements DocumentProcessor {

	private ObjectId docId;
	private VisaConfiguration taskConfig;
	private VisaConfiguration config;
	private String name;

	private UserData systemUser;
	private WorkflowMoveCalculator workflowMoveCalculator;

	protected WorkflowMoveCalculator getWorkflowMoveCalculator() {
		if (this.workflowMoveCalculator == null) {
			this.workflowMoveCalculator = new WorkflowMoveCalculator(getQueryFactory(), getDatabase());
		}
		return this.workflowMoveCalculator;
	}

	public void setConfig(VisaConfiguration config) {
		this.config = config;
	}

	public void setTaskConfig(VisaConfiguration config) {
		this.taskConfig = config;
	}

	public void setDocumentId(ObjectId docId) {
		this.docId = docId;
	}

	public ObjectId getDocumentId() {
		return this.docId;
	}

	public void setBeanName(String beanName) {
		this.name = beanName;
	}

	public String getName() {
		return this.name;
	}

	private ObjectId getVisaSetAttr() throws DataException {
		return config.getObjectId(CardLinkAttribute.class, VisaConfiguration.ATTR_VISAS);
	}

	public void process() throws DataException {
		final ObjectId cancelState = taskConfig.getObjectId(CardState.class, VisaConfiguration.STATE_ASSIGNED);
		Collection<Card> tasks = getTasks();
		Collection<ObjectId> affectedStates = taskConfig.getObjectIdSet(CardState.class,VisaConfiguration.STATES_WAITING);
		for (Card task : tasks) {
			if (affectedStates.contains(task.getState())) {
				cancelVisas(task);
				changeState(task, cancelState);
			}
		}
	}

	private Collection<Card> getTasks() throws DataException {
		logger.info("[" + getName() + ":" + docId.getId() + "] Fetching tasks (without filtering)");
		ObjectId visaSetAttr = getVisaSetAttr();
		Search search = CardUtils.getFetchAction(docId, new ObjectId[] { visaSetAttr, Card.ATTR_STATE });
		search.setFetchLink(taskConfig.getObjectId(CardLinkAttribute.class, VisaConfiguration.ATTR_VISAS));
		Collection<Card> cards = CardUtils.execSearchCards(search, getQueryFactory(), getDatabase(), getSystemUser());
		if (cards == null) {
			cards = Collections.emptyList();
		}
		logger.info("[" + getName() + ":" + docId.getId() + "] Fetched " + cards.size() + " tasks");
		return cards;
	}

	private void cancelVisas(Card task) throws DataException {
		Collection<Card> visas = getVisas(task);
		ObjectId cancelState = config.getObjectId(CardState.class, VisaConfiguration.STATE_ASSIGNED);
		Set<ObjectId> ignoredStates = config.getObjectIdSet(CardState.class, VisaConfiguration.STATES_IGNORED);
		for (Card visa : visas) {
			if (!ignoredStates.contains(visa.getState())) {
				changeState(visa, cancelState);
			}
		}
	}

	private Collection<Card> getVisas(Card task) throws DataException {
		logger.info("[" + getName() + ":" + task.getId() + "] Fetching task's visas (without filtering)");
		final ObjectId id = getVisaSetAttr();
		CardLinkAttribute visaAttribute = task.getCardLinkAttributeById(id);
		Search search = CardUtils.getFetchAction(visaAttribute, new ObjectId[] { Card.ATTR_STATE });
		Collection<Card> cards = CardUtils.execSearchCards(search, getQueryFactory(), getDatabase(), getSystemUser());
		if (cards == null) {
			cards = Collections.emptyList();
		}
		logger.info("[" + getName() + ":" + task.getId() + "] Fetched " + cards.size() + " visas");
		return cards;
	}

	private void changeState(Card card, ObjectId toState) throws DataException {
		logger.info("[" + config.getName() + "] Trying to find move to " + toState.getId() + " state for card "
				+ card.getId().getId());
		final WorkflowMove wfm = getWorkflowMoveCalculator().findProperMove(card, toState);
		logger.info("[" + config.getName() + "] Workflow move to be " + wfm.getId().getId() + " for card "
				+ card.getId().getId());
		lockCard(card.getId());
		try {
			final ChangeState move = new ChangeState();
			move.setCard(card);
			move.setWorkflowMove(wfm);
			execAction(move);
		} finally {
			unlockCard(card.getId());
		}
		logger.info("[" + config.getName() + "] State is changed to " + wfm.getId().getId() + " for card "
				+ card.getId().getId());
	}

	private void lockCard(ObjectId cardId) throws DataException {
		final LockObject lock = new LockObject(cardId);
		execAction(lock);
	}

	private void unlockCard(ObjectId cardId) throws DataException {
		final UnlockObject unlock = new UnlockObject(cardId);
		execAction(unlock);
	}

	private Object execAction(Action action) throws DataException {
		final ActionQueryBase query = getQueryFactory().getActionQuery(action);
		query.setAccessChecker(null);
		query.setAction(action);
		return getDatabase().executeQuery(getSystemUser(), query);
	}

	@Override
	public UserData getSystemUser() throws DataException {
		if (systemUser == null) {
			systemUser = new UserData();
			systemUser.setPerson(getDatabase().resolveUser(Database.SYSTEM_USER));
			systemUser.setAddress("internal");
		}
		return systemUser;
	}

}
