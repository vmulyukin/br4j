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
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.replication.action.CreateReplicationPackage;
import com.aplana.dbmi.replication.packageconfig.PackageType;
import com.aplana.dbmi.replication.packageconfig.ReplicationPackage;
import com.aplana.dbmi.replication.packageconfig.StatusType;
import com.aplana.dbmi.replication.tool.ReplicationNotificationHandler;
import com.aplana.dbmi.replication.tool.ReplicationUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;

import java.util.List;

public class ReplicationProcessorCollision extends ReplicationProcessorTemp {
	private static final long serialVersionUID = 1L;

	@Override
	public Object process() throws DataException {
		logger.info("-------Start ReplicationProcessorCollision-------");
		try {
			Card replicCard = getCard();
			ObjectQueryBase objectQuery = getQueryFactory().getFetchQuery(Card.class);
			objectQuery.setId(replicCard.getId());
			replicCard = getDatabase().executeQuery(getSystemUser(), objectQuery);

			DataServiceFacade service = new DataServiceFacade(getDatabase(), getSystemUser(), getQueryFactory());
			ReplicationPackage replicationPackage = ReplicationUtils.getReplicationPackageFromCard(replicCard, service);
			if(replicationPackage == null) {
				logger.warn("Replication package is null --- return null");
				return null;
			}
			List<ObjectId> cards = searchCardByGuid(replicationPackage.getCard().getGuid(), (replicationPackage.getCard().getTemplateId()));
			ObjectQueryBase queryObject = getQueryFactory().getFetchQuery(Card.class);
			queryObject.setId(cards.get(0));
			Card card = getDatabase().executeQuery(getSystemUser(), queryObject);

			String ownerGuid = ReplicationUtils.getOwnerGuid(replicationPackage);
			String serverGuid = ReplicationConfiguration.getReplicationNodeConfig().getServerGUID();

			// принимается версия карточки от создателя
			if (serverGuid.equals(ownerGuid)){
				// не меняем карточку
				// отправителю посылаем карточку для измеения в его базе
				CreateReplicationPackage create = new CreateReplicationPackage();
				create.setAddressee(replicationPackage.getAddressee());
				create.setCard(card);
				create.setPackageType(PackageType.COLLISION);
				create.setUpdateVersion(false);

				ActionQueryBase query = getQueryFactory().getActionQuery(create);
				query.setAction(create);
				getDatabase().executeQuery( getSystemUser(), query);

				// повторная загрузка
				//repeatLoad(replicationPackage);
			}else {
				// меняем карточку
				// отправителю отправляем уведомление о принятии
				card = changeCard(replicationPackage, card, UpdateType.UPDATE);

				ReplicationNotificationHandler hander = new ReplicationNotificationHandler(service);
				// Отправитель и получатель уже поменялись местами в ReplicationTask
				hander.sendNotification(replicationPackage, StatusType.UPDATE);

				ChangeState changeState = new ChangeState();
				WorkflowMove workflowMove = new WorkflowMove();
				workflowMove.setId("jbr.replication.collisionToAccepted");
				changeState.setCard(replicCard);
				changeState.setWorkflowMove(workflowMove);
				ActionQueryBase actionQuery = getQueryFactory().getActionQuery(changeState);
				actionQuery.setAction(changeState);
				getDatabase().executeQuery(getSystemUser(), actionQuery);
			}

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
