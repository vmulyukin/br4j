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
import com.aplana.dbmi.jbr.processors.ProcessCard;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.replication.action.CalculateReplicationState;
import com.aplana.dbmi.replication.tool.CardRelationUtils;
import com.aplana.dbmi.replication.tool.ReplicationActiveProcessInfo;
import com.aplana.dbmi.replication.tool.ReplicationInfo;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;

import java.util.List;

public class ReplicationProcessor extends ProcessCard {
	private static final long serialVersionUID = 1L;

	@Override
	public Object process() throws DataException {
		//репликация отключена - сразу выход
		if (!ReplicationConfiguration.isReplicationActive()) {
			return null;
		}
		
		/*
		 * вызвано для карточки репликация - выход (специфик по шаблону в queries.xml поставить намного сложнее,
		 * т.к. автоматика висит на OverwriteAttributes, в котором не определить шаблон карточки. Можно сделать
		 * новый класс селектора, который будет по айдишнику карточки проверять ее шаблон.
		 */
		Card currentCard = getCard();
		DataServiceFacade service = new DataServiceFacade(getDatabase(), getSystemUser(), getQueryFactory());
		List<ObjectId> template = CardRelationUtils.resolveLink(currentCard.getId(), service, Card.ATTR_TEMPLATE.getId());
		if (template.get(0).equals(DataObject.createFromId(Template.class, "jbr.replication").getId())) {
			return null;
		}
		
		try {
			calculateReplicationState(currentCard);
			if (needReplication(currentCard)) {
				if (ReplicationActiveProcessInfo.putOnlyCardForReplication(getPrimaryQuery().getUid(), currentCard.getId())) {
					//если карточку пометили для репликации, то добавляем для нее процессор,
					//который выполнит непосредственно процесс репликации для этой карточки
					ExecuteReplication proc = new ExecuteReplication(currentCard);
					proc.setBeanFactory(getBeanFactory());
					//приоритет в очереди пост-процессоров (должен идти после ExecuteCopyFromLocalCard процессора)
					proc.setQueueOrder(1000);
					getPrimaryQuery().addPostProcessor(proc);
				}
			}
			return null;
		} catch (DataException ex) {
			logger.error("Error on execute do process in " + getClass().getName(), ex);
			throw ex;
		} catch (Exception ex) {
			logger.error("Error on execute do process in " + getClass().getName(), ex);
			throw new DataException("jbr.replication.common", new Object[] { "" }, ex);
		}
	}

	private void calculateReplicationState(Card card) throws DataException {
		CalculateReplicationState calcAction = new CalculateReplicationState();
		calcAction.setCard(card);
		execAction(calcAction);
	}

	private boolean needReplication(Card card) throws DataException {
		ReplicationInfo replicationInfo = new ReplicationInfo(card,
				new DataServiceFacade(getDatabase(), getSystemUser(), getQueryFactory()));
		return replicationInfo.needReplication();
	}

	@Override
	public Card getCard() throws DataException {
		if (getAction() instanceof ChangeState) {
			return super.loadCardById(getCardId());
		}
		return super.getCard();
	}
}
