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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.LinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.CardLinkLoader;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.Database;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.Parametrized;
import com.aplana.dbmi.service.impl.ProcessorBase;
import com.aplana.dbmi.service.impl.UserData;

/**
 * @comment RAbdullin
 * ������� ���������-��������� � ������� ��������� � ������������� ��� ������
 * ���-���������-������������ � ��������� ��������.
 */

public class DocumentTrigger extends ProcessorBase implements Parametrized {
	private static final long serialVersionUID = 1L;
	
	public static final String PARAM_PROCESSOR_BEAN = "processorBean";
	public static final String PARAM_CONFG_BEAN = "configBean";

	protected DocumentProcessor processor;
	private VisaConfiguration config;
	private UserData systemUser;
	private WorkflowMoveCalculator workflowMoveCalculator;


	protected WorkflowMoveCalculator getWorkflowMoveCalculator() {
		if (this.workflowMoveCalculator == null) {
			this.workflowMoveCalculator = new WorkflowMoveCalculator(getQueryFactory(), getDatabase());
		}
		return this.workflowMoveCalculator;
	}

	public void setParameter(String name, String value) {
		if (PARAM_PROCESSOR_BEAN.equalsIgnoreCase(name))
			processor = (DocumentProcessor) getBeanFactory().getBean(value);
		else if (PARAM_CONFG_BEAN.equalsIgnoreCase(name))
			config = (VisaConfiguration) getBeanFactory().getBean(value);
		else
			throw new IllegalArgumentException("Unknown parameter: " + name);
	}

	@Override
	public Object process() throws DataException {
		if (config == null)
			throw new IllegalStateException("Config parameter must be set");
		//if (processor == null)
		//	throw new IllegalStateException("Processor bean must be set");

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

		logger.info("[" + config.getName() + "] Triggered for document " + cardId.getId());
		// 1. Move all visas to the assigned state (thus validate them)
		Collection<Card> visas = getAllVisas(card);
		for (Iterator<Card> itr = visas.iterator(); itr.hasNext(); ) {
			Card visa = (Card) itr.next();
			assignVisa(visa);
		}
		logger.info("Document " + cardId.getId() + ": " +
				visas.size() + " visa(s) assigned to their destinations");

		// 2. Start asynchronous process of sending visas
		if (processor != null) {
			processor.setDocumentId(cardId);
			processor.process();
		}
		return getResult();
	}

	@SuppressWarnings("unchecked")
	private Collection<Card> getAllVisas(Card card) throws DataException {
		final Set<ObjectId> ids = config.getObjectIdSet(CardLinkAttribute.class, VisaConfiguration.ATTR_VISAS);
		final ArrayList<Card> visas = new ArrayList<Card>();
		for (Iterator<ObjectId> itr = ids.iterator(); itr.hasNext(); ) {
			final ObjectId attrId = itr.next();

			// >>> (2010/02, RuSA) getValues() updated
			LinkAttribute attr = (LinkAttribute) card.getAttributeById(attrId);

			if (CardLinkAttribute.class.isAssignableFrom(attrId.getType())) {
				if (attr == null || attr.getLabelLinkedMap() == null) {
					logger.info("[" + config.getName() + "] Fetching visa document " + card.getId().getId());
					ObjectQueryBase query = getQueryFactory().getFetchQuery(Card.class);
					query.setId(card.getId());
					card = (Card) getDatabase().executeQuery(getSystemUser(), query);
					attr = (LinkAttribute) card.getAttributeById(attrId);
					if (attr == null || attr.getLabelLinkedMap() == null)
						throw new DataException("docflow.document.noattr",
								new Object[] { card.getId().getId().toString(), "@attribute." + attrId.getId() });
				}
				visas.addAll(CardLinkLoader.loadCardsByLink((CardLinkAttribute) attr, getSystemUser(), getQueryFactory(), getDatabase()));
			} else if(BackLinkAttribute.class.isAssignableFrom(attrId.getType())) {
				if (attr == null || attr.getLabelLinkedMap() == null) {
					throw new IllegalStateException("Attribute " + attrId.getId() + " must contains at least one card_id");
				}
				visas.addAll(CardLinkLoader.loadCardsByIds(attr.getIdsLinked(), null, getSystemUser(), getQueryFactory(), getDatabase()));
			} else {
				throw new ClassCastException("Attribute " + attrId.getId() + " must be CardLink or BackLink");
			}
			logger.info("[" + config.getName() + "] Document " + card.getId().getId() + " has " +
					attr.getLinkedCount() + " linked visa cards under " + attrId);
			// <<< (2010/02, RuSA)
		}
		return visas;
	}

	protected ObjectId getObjectIdByTemplate(Class<?> type, String key, ObjectId template) throws DataException {
		Map<Object, ObjectId> specific = config.getObjectIdMap(key + VisaConfiguration.INFIX_TEMPLATE,
				Template.class, type);
		if (specific.containsKey(template))
			return specific.get(template);
		return config.getObjectId(type, key);
	}

	/**
	 * ��������� �������� � ��������� "��������".
	 * ���������� true ���� �������� ���� ����������
	 * @param card
	 * @throws DataException
	 */
	private boolean assignVisa(Card card) throws DataException {

		final ObjectId cardStateId = card.getState();
		logger.info("[" + config.getName() + "] Sending visa card " + card.getId().getId() + " to appropriate person");
		final ObjectId desiredState =
			// config.getObjectId(CardState.class, VisaConfiguration.STATE_ASSIGNED);
			getObjectIdByTemplate(CardState.class, VisaConfiguration.STATE_ASSIGNED, card.getTemplate());
		if (desiredState.equals(cardStateId))
			return false;

		if (config.isListedId(cardStateId, VisaConfiguration.STATES_IGNORED)) {
			logger.info("[" + config.getName() + "] State changing of card " + card.getId().getId() + " is not required as it is in ignored state: " + cardStateId.getId());
			return false;
		}

		if (config.isListedId(cardStateId, VisaConfiguration.STATES_WAITING)) {
			logger.info("[" + config.getName() + "] State changing of card " + card.getId().getId() + " is not required as it is in whaiting state: " + cardStateId.getId());
			return false;
		}

		if (config.isListedId(cardStateId, VisaConfiguration.STATES_AGREED)) {
			logger.info("[" + config.getName() + "] State changing of card " + card.getId().getId() + " is not required as it is in agreed state: " + cardStateId.getId());
			return false;
		}

		final WorkflowMove wfm = getWorkflowMoveCalculator().findProperMove(card, desiredState);
		logger.info("[" + config.getName() + "] Workflow move to be " + wfm.getId().getId() + " for visa " + card.getId().getId());

		lockCard( card.getId());
		try {
			final ChangeState move = new ChangeState();
			move.setCard(card);
			move.setWorkflowMove(wfm);
			execAction(move);
		} finally {
			unlockCard(card.getId());
		}
		return true;
	}

	private void lockCard(ObjectId cardId) throws DataException
	{
		final LockObject lock = new LockObject(cardId);
		execAction(lock);
	}


	private void unlockCard(ObjectId cardId) throws DataException
	{
		final UnlockObject unlock = new UnlockObject(cardId);
		execAction(unlock);
	}

	private Object execAction(Action action) throws DataException
	{
		final ActionQueryBase query = getQueryFactory().getActionQuery(action);
		query.setAccessChecker(null);
		query.setAction(action);
		return getDatabase().executeQuery(getSystemUser(), query);
	}

	public UserData getSystemUser() throws DataException {
		if (systemUser == null) {
			systemUser = new UserData();
			systemUser.setPerson(getDatabase().resolveUser(Database.SYSTEM_USER));
			systemUser.setAddress("internal");
		}
		return systemUser;
	}
}