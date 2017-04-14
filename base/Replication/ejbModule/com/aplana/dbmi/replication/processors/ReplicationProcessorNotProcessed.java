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
package com.aplana.dbmi.replication.processors;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.replication.action.GetPerson;
import com.aplana.dbmi.replication.action.LinkResolver;
import com.aplana.dbmi.replication.packageconfig.ReplicationPackage;
import com.aplana.dbmi.replication.packageconfig.ReplicationPackage.Card.Attribute;
import com.aplana.dbmi.replication.packageconfig.ReplicationPackage.Card.Attribute.PersonValue;
import com.aplana.dbmi.replication.query.DoGetPerson.PersonWrapper;
import com.aplana.dbmi.replication.tool.ReplicationCardHandler;
import com.aplana.dbmi.replication.tool.ReplicationUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.utils.StrUtils;

import java.util.ArrayList;
import java.util.List;

public class ReplicationProcessorNotProcessed extends ReplicationProcessorTemp {
	private static final long serialVersionUID = 1L;

	private enum NextState {
		TO_ACCEPTED,
		TO_NEED_REQUEST,
		TO_QUEUE
	}
	
	@Override
	public Object process() throws DataException {
		logger.info("-------Start ReplicationProcessorNotProcessed-------");
		try {

			Card replicationCard = getCard();
			ObjectQueryBase objectQuery = getQueryFactory().getFetchQuery(Card.class);
			objectQuery.setId(replicationCard.getId());
			replicationCard = getDatabase().executeQuery(getSystemUser(), objectQuery);
			IntegerAttribute isInProcess = replicationCard.getAttributeById(ObjectId.predefined(IntegerAttribute.class, "jbr.replication.replicIsInProcess"));
			if (isInProcess.getValue() != ReplicationCardHandler.REPLICATION_IN_PROCESS_TRUE) {
				throw new DataException("jbr.replication.stopped");
			}

			DataServiceFacade service = new DataServiceFacade(getDatabase(), getSystemUser(), getQueryFactory());
			ReplicationPackage replicationPackage = ReplicationUtils.getReplicationPackageFromCard(replicationCard, service);
			if(replicationPackage == null) {
				logger.warn("Replication package is null --- return null");
				return null;
			}

			//анализ куда дальше вести картчоку репликации
			//по умолчанию (не найдено никаких проблем) - в статус Принято
			NextState next = NextState.TO_ACCEPTED;
			outerloop:
			for (Attribute attribute : replicationPackage.getCard().getAttribute()) {
				if (!attribute.getCardLinkValue().isEmpty() || !attribute.getPersonValue().isEmpty()) {
					ArrayList<String> guids = new ArrayList<String>(attribute.getCardLinkValue());
					for (PersonValue pv : attribute.getPersonValue()) {
						GetPerson gp = new GetPerson(pv.getLogin(), pv.getEmail(), pv.getFullName(), pv.getUuid());
						ActionQueryBase aqb = getQueryFactory().getActionQuery(gp);
						aqb.setAction(gp);
						PersonWrapper wrap = getDatabase().executeQuery(getSystemUser(), aqb);
						if (wrap == null && !StrUtils.isStringEmpty(pv.getCard())) {
							guids.add(pv.getCard());
						}
					}
					for (String guid : guids) {
						//Ищем по заданному UUID карточку (любого шаблона)
						List<ObjectId> cards = searchCardByGuid(guid, 0);
						if (cards.size() > 1) {
							//UUID должен быть уникальным в системе и соответствовать одной карточке
							throw new DataException("Too much cards (count="+cards.size()+") with UUID = " + guid);
						}
						//если не найдена карточка, то выходим из внешнего цикла и
						//затем переводим репликацию в статус Необходим дозапрос
						if (cards.size() == 0) {
							next = NextState.TO_NEED_REQUEST;
							break outerloop;
						} else {
							//если найдена одна карточка, то проверяем прошла ли она уже репликацию
							//если не прошла (находится в статусе 10), то устанавливаем следующий статус = В очередь,
							//при этом продолжаем идти по следующим значениям атрибутов, т.к. возможно что-нибудь 
							//опять не будет найдено, и придется переводить репликацию в статус Необходим дозапрос
							LinkResolver<ObjectId> resolveState = new LinkResolver<ObjectId>();
							resolveState.setCardId(cards.get(0));
							resolveState.setLink(Card.ATTR_STATE.getId().toString());
							ActionQueryBase aqb = getQueryFactory().getActionQuery(resolveState);
							aqb.setAction(resolveState);
							List<ObjectId> cardStateId = getDatabase().executeQuery(getSystemUser(), aqb);
							ObjectId cardStateCurrent  = cardStateId.get(0);
							ObjectId cardStateNotCompl = ObjectId.state("notComplReplic");
							if (cardStateCurrent.equals(cardStateNotCompl)) {
								next = NextState.TO_QUEUE;
							}
						}
					}
				}
			}

			ChangeState changeState = new ChangeState();
			WorkflowMove workflowMove = new WorkflowMove();
			switch (next) {
				case TO_ACCEPTED:
					// новый статус - Принят
					workflowMove.setId("jbr.replication.notProcessedToAccepted");
					break;
				case TO_NEED_REQUEST:
					// новый статус - Отправлен дозапрос
					workflowMove.setId("jbr.replication.notProcessedToNeedRequest");
					break;
				case TO_QUEUE:
					// новый статус - В очередь
					workflowMove.setId("jbr.replication.notProcessedToQueued");
			}
			changeState.setCard(replicationCard);
			changeState.setWorkflowMove(workflowMove);
			ActionQueryBase actionQuery = getQueryFactory().getActionQuery(changeState);
			actionQuery.setAction(changeState);
			getDatabase().executeQuery(getSystemUser(), actionQuery);

//		} catch (DataException ex) {
//			logger.error("Error on execute do process in " + this.getClass().getName(), ex);
//			throw ex;
		} catch (Exception ex) {
			if(logger.isErrorEnabled())
				logger.error("Error on execute do process in " + this.getClass().getName(), ex);
			sendErrorNotification(ex);
//			throw new DataException("Error on execute do process in " + this.getClass().getName(), ex);
		}
		return null;
	}
}