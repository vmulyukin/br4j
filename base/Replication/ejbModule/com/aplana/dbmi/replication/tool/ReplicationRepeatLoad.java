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
package com.aplana.dbmi.replication.tool;

import com.aplana.dbmi.action.*;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.replication.action.GetPerson;
import com.aplana.dbmi.replication.packageconfig.PackageType;
import com.aplana.dbmi.replication.packageconfig.ReplicationPackage;
import com.aplana.dbmi.replication.packageconfig.ReplicationPackage.Card.Attribute.PersonValue;
import com.aplana.dbmi.replication.packageconfig.ReplicationPackage.IncompleteCards;
import com.aplana.dbmi.replication.processors.ReplicationConfiguration;
import com.aplana.dbmi.replication.query.DoGetPerson.PersonWrapper;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.utils.StrUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.bind.JAXBException;
import java.util.*;

public class ReplicationRepeatLoad {

	private DataServiceBean serviceBean;
	private Log logger = LogFactory.getLog(getClass());

	public ReplicationRepeatLoad(DataServiceBean serviceBean) {
		this.serviceBean = serviceBean;
	}

	public int repeatLoad() {
		int result = sendRequestForIncompleteCards();
		sendRequestForErrorAndDraftCards();
		processQueue();
		return result;
	}

	private int sendRequestForIncompleteCards() {
		int result = 0;
		Search search = new Search();
		search.setByAttributes(true);
		search.setTemplates(Collections.singleton(DataObject.createFromId(Template.class, "jbr.replication")));
		Collection<ObjectId> states = IdUtils.makeStateIdsList("jbr.replication.needRequest, jbr.replication.requestSent");
		search.setStates(states);
		ObjectId reqSentState = ObjectId.state("jbr.replication.requestSent");
		try {
			SearchResult sr = serviceBean.doAction(search);

			for (int i = 0; i < sr.getCards().size(); i++) {
				ObjectId cardId = sr.getCards().get(i).getId();
				try {
					Card replicCard = serviceBean.getById(cardId);
					ReplicationPackage requestPackage = createRequestPackageForRequestSentCard(replicCard);

					if (null == requestPackage) {
						moveCardToState(replicCard, "jbr.replication.notProcessed");
						result++;
					} else if (!reqSentState.equals(replicCard.getState())) {
						ReplicationUtils.packageToFile(requestPackage);
						moveCardToState(replicCard, "jbr.replication.requestSent");
					}
				} catch (Exception ex) {
					if (logger.isErrorEnabled()) {
						logger.error("Error during sending request for incomplete replication card [" + cardId.getId()
								+ "]", ex);
					}
				}
			}
		} catch (Exception ex) {
			logger.error("Error during searching for incomplete cards", ex);
		}
		return result;
	}

	private void sendRequestForErrorAndDraftCards() {
		ObjectId retriesAttrId = ObjectId.predefined(IntegerAttribute.class, "jbr.replication.retries");
		
		Search search = new Search();
		search.setByAttributes(true);
		search.addIntegerAttribute(retriesAttrId, 0, 0);
		search.setTemplates(Collections.singleton(DataObject.createFromId(Template.class, "jbr.replication")));
		
		Collection<ObjectId> states = IdUtils.makeStateIdsList("jbr.replication.error, draft");
		search.setStates(states);
		try {
			SearchResult sr = serviceBean.doAction(search);
			for (Card card : sr.getCards()) {
				ObjectId cardId = card.getId();
				try {
					Card replicCard = serviceBean.getById(cardId);
					ReplicationPackage replicationPackage = ReplicationUtils.getReplicationPackageFromCard(replicCard, serviceBean);
					if (replicationPackage == null) {
						continue;
					}
					
					IntegerAttribute retriesAttr = replicCard.getAttributeById(retriesAttrId);
					retriesAttr.setValue(retriesAttr.getValue()+1);
					serviceBean.doAction(new LockObject(cardId));
					try {
						OverwriteCardAttributes save = new OverwriteCardAttributes();
						save.setCardId(cardId);
						save.setAttributes(Collections.singleton(retriesAttr));
						serviceBean.doAction(save);
					} finally {
						serviceBean.doAction(new UnlockObject(cardId));
					}
					
					String server = ReplicationConfiguration.getReplicationNodeConfig().getServerGUID();
					String owner  = ReplicationUtils.getOwnerGuid(replicationPackage);
					if (server.equals(owner)) {
						continue;
					}
					
					String replicCardGuid = replicCard.<StringAttribute>getAttributeById(CardRelationUtils.REPLIC_GUID).getValue();
					ReplicationPackage requestPackage = createRequestPackage(replicationPackage, Collections.singleton(replicCardGuid));
					ReplicationUtils.packageToFile(requestPackage);
				} catch (Exception ex) {
					if (logger.isErrorEnabled()) {
						logger.error("Error during sending request for error or draft replication card ["+cardId.getId()+"]", ex);
					}
				}
			}
		} catch (Exception ex) {
			logger.error("Error during searching for draft and error cards", ex);
		}
	}
	
	private void processQueue() {
		Search search = new Search();
		search.setByAttributes(true);
		search.setTemplates(Collections.singleton(DataObject.createFromId(Template.class, "jbr.replication")));
		Collection<ObjectId> states = IdUtils.makeStateIdsList("jbr.replication.queued");
		search.setStates(states);

		try {
			SearchResult sr = serviceBean.doAction(search);
			if (logger.isDebugEnabled()) {
				logger.debug(this.getClass().getSimpleName() + ".processQueue()' found replication cards in queue: " + sr.getCards().size());
			}
			for (int i = 0; i < sr.getCards().size(); i++) {
				ObjectId cardId = sr.getCards().get(i).getId();
				try {
					Card replicCard = serviceBean.getById(cardId);					
					moveCardToState(replicCard, "jbr.replication.notProcessed");
				} catch (Exception error) {
					if(logger.isErrorEnabled())
						logger.error("An error has occurred while processing card (ID: "+ cardId.getId() + ") in status '" + ObjectId.state("jbr.replication.queued") + "'", error);
				}
			}
		} catch (Exception error) {
			if(logger.isErrorEnabled())
				logger.error("An error happened while searching for cards in status " + ObjectId.state("jbr.replication.queued"), error);
		}
	}
	
	private ReplicationPackage createRequestPackageForRequestSentCard(Card card) throws DataException,
			ServiceException, JAXBException {

		ReplicationPackage replicationPackage = ReplicationUtils.getReplicationPackageFromCard(card, serviceBean);
		if (replicationPackage == null) {
			return null;
		}
		ReplicationPackage result = null;
		HashSet<String> cardGuids = new HashSet<String>();
		for (ReplicationPackage.Card.Attribute attribute : replicationPackage.getCard().getAttribute()) {
			if (!attribute.getCardLinkValue().isEmpty() || !attribute.getPersonValue().isEmpty()) {
				ArrayList<String> guids = new ArrayList<String>(attribute.getCardLinkValue());
				for (PersonValue pv : attribute.getPersonValue()) {
					GetPerson gp = new GetPerson(pv.getLogin(), pv.getEmail(), pv.getFullName(), pv.getUuid());
					PersonWrapper wrap = serviceBean.doAction(gp);
					if (wrap == null && !StrUtils.isStringEmpty(pv.getCard())) {
						guids.add(pv.getCard());
					}
				}
				for (String guid : guids) {
					if (searchCardByGuid(guid) == null) {
						cardGuids.add(guid);
					}
				}
			}
		}

		if (cardGuids.size() != 0) {
			result = createRequestPackage(replicationPackage, cardGuids);
		}

		return result;
	}

	private ReplicationPackage createRequestPackage(ReplicationPackage replicationPackage, Collection<String> cardGuids) {
		ReplicationPackage requestPackage = new ReplicationPackage();
		requestPackage.setPackageType(PackageType.REQUEST);
		requestPackage.setAddressee(replicationPackage.getAddressee());
		requestPackage.setSender(replicationPackage.getSender());
		requestPackage.setDateSent(ReplicationUtils.newXMLGregorianCalendar());
		IncompleteCards incompleteCards = new IncompleteCards();
		incompleteCards.setGuid(replicationPackage.getCard().getGuid());
		for (String guid : cardGuids) {
			incompleteCards.getCardGuid().add(guid);
		}
		requestPackage.setIncompleteCards(incompleteCards);
		return requestPackage;
	}

	private Card moveCardToState(Card card, String state) throws DataException, ServiceException {
		GetWorkflowMovesFromTargetState getWorkflowMovesAction = new GetWorkflowMovesFromTargetState();
		getWorkflowMovesAction.setCard(card);
		getWorkflowMovesAction.setToStateId(ObjectId.state(state));
		List<Long> moveIds = serviceBean.doAction(getWorkflowMovesAction);
		WorkflowMove wfMove = serviceBean.getById(new ObjectId(WorkflowMove.class, moveIds.get(0).longValue()));
		ChangeState changeState = new ChangeState();
		changeState.setCard(card);
		changeState.setWorkflowMove(wfMove);
		serviceBean.doAction(changeState);
		return card;
	}

	// поиск карточек по Guid
	private ObjectId searchCardByGuid(String uuid) throws DataException, ServiceException {
		GetCardIdByUUID getCard = new GetCardIdByUUID();
		getCard.setAttrId(CardRelationUtils.REPLICATION_UUID);
		getCard.setUuid(uuid);
		ObjectId cardId = serviceBean.doAction(getCard);
		return cardId;
	}
}