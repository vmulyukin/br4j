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

import com.aplana.dbmi.action.CheckIsLocked;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.replication.packageconfig.ReplicationPackage;
import com.aplana.dbmi.replication.packageconfig.ReplicationPackage.Card.Attribute.ListValue;
import com.aplana.dbmi.replication.packageconfig.StatusType;
import com.aplana.dbmi.replication.tool.*;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ObjectLockedException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dbmi.service.impl.ObjectQueryBase;

import java.util.Date;
import java.util.List;

public class ReplicationProcessorAccepted extends ReplicationProcessorTemp {
	private static final long serialVersionUID = 1L;

	@Override
	public Object process() throws DataException {
		logger.info("-------Start ReplicationProcessorAccepted-------");
		try {
			boolean isQueued = false;
			boolean needSetGuid = false;
			Card replicCard = getCard();
			ObjectQueryBase objectQuery = getQueryFactory().getFetchQuery(Card.class);
			objectQuery.setId(replicCard.getId());
			replicCard = getDatabase().executeQuery(getSystemUser(), objectQuery);
			
			Card card;
			DataServiceFacade service = new DataServiceFacade(getDatabase(), getSystemUser(), getQueryFactory());
			ReplicationPackage replicationPackage = ReplicationUtils.getReplicationPackageFromCard(replicCard, service);
			if(replicationPackage == null) {
				logger.warn("Replication package is null --- return null");
				return null;
			}
			StatusType statusType = StatusType.UPDATE;

			//корректируем тип документа (Первоначальный, Промежуточный) в зависимости от значений GUID отправителя и GUID текущей системы.
			ObjectId replDocTypeId     = CardRelationUtils.REPLIC_DOC_TYPE;
			ObjectId replDocTypeSource = ObjectId.predefined(ReferenceValue.class, "jbr.replication.sourceDocument");
			ObjectId replDocTypeReplic = ObjectId.predefined(ReferenceValue.class, "jbr.replication.replicatedDocument");
			ReplicationPackage.Card cardFromXml = replicationPackage.getCard();
			if (cardFromXml != null) {
				ReplicationPackage.Card.Attribute replAttr = ReplicationUtils.getReplicationAttribute(cardFromXml, (String) replDocTypeId.getId());
				if (replAttr != null) {
					replAttr.getListValue().clear();
					ListValue lv = new ListValue();
					lv.setId((Long) replDocTypeReplic.getId());
					replAttr.getListValue().add(lv);
				}
			}

			List<ObjectId> cards = searchCardByGuid(replicationPackage.getCard().getGuid(), (replicationPackage.getCard().getTemplateId()));
			if (cards.size() == 0) {
				cards = searchCardByKeywords(replicationPackage.getCard());
				needSetGuid = true;
				logger.error("Search card by keywords with UUID = " + replicationPackage.getCard().getGuid());
			}
			if (cards.size() == 0) {
				// новая карточка
				int intCardId = replicCard.<IntegerAttribute>getAttributeById(CardRelationUtils.REPLIC_CARDID).getValue();
				card = createCard(replicationPackage, intCardId, UpdateType.NEW);
				ReplicationCardHandler replicCardHandler = new ReplicationCardHandler(getQueryFactory(), getDatabase(), getUser());
				replicCardHandler.setReplicCardRootLink(card, replicCard);
				statusType = StatusType.CREATED;
			} else if (cards.size() > 1) {
				throw new Exception("There is more than one card with UUID = " + replicationPackage.getCard().getGuid());
			} else {
				ObjectQueryBase object = getQueryFactory().getFetchQuery(Card.class);
				object.setId(cards.get(0));
				card = getDatabase().executeQuery(getSystemUser(), object);
				ReplicationInfo replicationInfo = new ReplicationInfo(card, service);
				
				// Проверка документа на то, что он заблокирован (isLocked = true/false - заблокирован/разблокирован)
				Boolean lockedSuccess = execAction(new CheckIsLocked(card));
				if (lockedSuccess) {
					try {
						if (needSetGuid) {
							card = setGuidCard(replicationPackage, card);
						}
						if (!replicationInfo.canBeLoaded()) {
							throw new DataException("jbr.replication.stopped");
						}
						Date changeDateRpg = getChangeDateFromPackage(replicationPackage);
						Date changeDateCard = null;
						if (!card.<DateAttribute>getAttributeById(Attribute.ID_CHANGE_DATE).isEmpty()){
							changeDateCard = card.<DateAttribute>getAttributeById(Attribute.ID_CHANGE_DATE).getValue();
						}
						
						StringAttribute ownerAttr = card.getAttributeById(CardRelationUtils.REPLIC_OWNER);
						if (ownerAttr != null && ownerAttr.getValue() != null) {
							String ownerValue = ownerAttr.getValue();
							ReplicationPackage.Card.Attribute replAttr = ReplicationUtils.getReplicationAttribute(cardFromXml, (String) replDocTypeId.getId());
							if (replAttr != null) {
								if (ownerValue.equals(ReplicationConfiguration.getReplicationNodeConfig().getServerGUID())) {
									replAttr.getListValue().clear();
									ListValue lv = new ListValue();
									lv.setId((Long) replDocTypeSource.getId());
									replAttr.getListValue().add(lv);
								}
							}
						}
						
						if (card.getState().equals(ObjectId.state("notComplReplic"))) {
							card = changeCard(replicationPackage, card, UpdateType.UPDATE);
							statusType = StatusType.UPDATE;
						} else {
							if (changeDateCard != null && changeDateRpg != null && changeDateCard.after(changeDateRpg)) {
								changeCardStatus(replicCard, "jbr.replication.acceptedToCollision");
								statusType = StatusType.COLLISION;
							} else if (changeDateRpg == null || !changeDateRpg.equals(changeDateCard)) {
								card = changeCard(replicationPackage, card, UpdateType.UPDATE);
								statusType = StatusType.UPDATE;
							}
						}
					} catch (ObjectLockedException error) {
						if (logger.isErrorEnabled()) {
							logger.error("An unexpected exception occurred while trying to LOCK the card with ID: " + card.getId().getId(), error);
						}
						throw error;
					} finally {
						try {
							execAction(new UnlockObject(card));
						} catch (Exception error) {
							if(logger.isErrorEnabled()) {
								logger.error("An unexpected exception occurred while trying to UNLOCK the card with ID: " + card.getId().getId(), error);
							}
						}
					}
				} else {
					if(logger.isWarnEnabled()) {
						logger.warn("Cannot execute '" + this.getClass().getSimpleName() + ".process()' because card (ID: " + card.getId().getId() + ") is [locked].");
					}
					changeCardStatus(replicCard, "jbr.replication.acceptedToQueued");
					if(logger.isWarnEnabled()) {
						logger.warn("Replication card (ID: " + replicCard.getId().getId() + ") moved into status '" +
								ObjectId.state("jbr.replication.queued") + "'");
					}
					isQueued = true;
				}
			}

			// отправка уведомления, в случае если не коллизия
			if (!StatusType.COLLISION.equals(statusType) && !isQueued) {
				ReplicationNotificationHandler handler = new ReplicationNotificationHandler(service);
				// Отправитель и получатель для пакета уже поменялись местами в ReplicationTask
				handler.sendNotification(replicationPackage, statusType);
			}
			// } catch (DataException ex) {
			// logger.error("Error on execute do process in " +
			// this.getClass().getName(), ex);
			// throw ex;
		} catch (Exception ex) {
			if(logger.isErrorEnabled()) {
				logger.error("Error on execute do process in " + this.getClass().getName(), ex);
			}
			sendErrorNotification(ex);
			// throw new DataException("Error on execute do process in " +
			// this.getClass().getName(), ex);
		}
		return null;
	}

	private Date getChangeDateFromPackage(ReplicationPackage rpg) {
		Date result = null;
		for (ReplicationPackage.Card.Attribute attribute : rpg.getCard().getAttribute()) {
			if (attribute.getCode().equals(Attribute.ID_CHANGE_DATE.getId().toString())) {
				result = attribute.getDateValue().get(0).toGregorianCalendar().getTime();
			}
		}
		return result;
	}

	@SuppressWarnings("unused")
	private String getReplicationVersion(ReplicationPackage rpg) {
		String result = null;
		for (ReplicationPackage.Card.Attribute attribute : rpg.getCard().getAttribute()) {
			if (attribute.getCode().equals(CardRelationUtils.REPLIC_VERSION.getId())) {
				result = attribute.getStringValue().get(0);
			}
		}
		return result;
	}
}