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
import com.aplana.dbmi.action.GetCardIdByUUID;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.replication.action.GetReservCardId;
import com.aplana.dbmi.replication.packageconfig.ReplicationPackage;
import com.aplana.dbmi.replication.packageconfig.ReplicationPackage.Card.Attribute;
import com.aplana.dbmi.replication.processors.beans.CopyChangesToLocalCardHandler;
import com.aplana.dbmi.replication.tool.CardRelationUtils;
import com.aplana.dbmi.replication.tool.ReplicationCardHandler;
import com.aplana.dbmi.replication.tool.ReplicationUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ObjectLockedException;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.DatabaseClient;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import org.springframework.beans.BeansException;
import org.springframework.dao.DataAccessException;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.Types;
import java.util.List;
import java.util.UUID;

public class CopyChangesToLocalCardProcessor extends ReplicationProcessorTemp {
	private static final long serialVersionUID = 1L;
	private List<ObjectId> dictionaryTemplates;

	@Override
	public Object process() throws DataException {
		dictionaryTemplates = ReplicationConfiguration.getIndependentTemplates();
		ObjectId replicCardId = getCardId();

		logger.info("-------Start CopyChangesToLocalCardProcessor-------");
		try {
			Card replicCard = loadCardById(replicCardId);
			if (!ObjectId.state("jbr.replication.accepted").equals(replicCard.getState())) {
				logger.info("State of replication card is not 'Accepted'. Exit.");
				return null;
			}
			DataServiceFacade service = new DataServiceFacade(getDatabase(), getSystemUser(), getQueryFactory());
			ReplicationPackage replicationPackage = ReplicationUtils.getReplicationPackageFromCard(replicCard, service);
			if (replicationPackage == null || replicationPackage.getCard() == null) {
				return null;
			}

			String ownerGuid = ReplicationUtils.getOwnerGuid(replicationPackage);
			String serverGuid = ReplicationConfiguration.getReplicationNodeConfig().getServerGUID();

			// Пакеты с карточкой могут приходить как ответ на дозапрос или в иных случаях на
			// сервер-отправитель. В данных ситуациях не надо создавать итоговый документ на основе
			// промежуточного
			if (ownerGuid.equals(serverGuid)) {
				return null;
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Owner guid:  '" + ownerGuid  + "'. Current server guid: '" + serverGuid + "'");
			}

			CopyChangesToLocalCardHandler handler = createLocalCopyHandler(replicationPackage);
			if (handler == null) {
				return null;
			}

			StringAttribute localGuidAttr = replicCard.getAttributeById(CardRelationUtils.REPLIC_LOCAL_GUID);
			String localUid = localGuidAttr.getValue();
			boolean isLocalCardNew = false;
			if (localUid == null || localUid.length() == 0) {
				localUid = UUID.randomUUID().toString();
				localGuidAttr.setValue(localUid);
				getJdbcTemplate().update(
						"INSERT INTO attribute_value(card_id, attribute_code, string_value) VALUES (?, ?, ?);",
						new Object[] {replicCard.getId().getId(), CardRelationUtils.REPLIC_LOCAL_GUID.getId(), localUid},
						new int[] {Types.NUMERIC, Types.VARCHAR, Types.VARCHAR});
				isLocalCardNew = true;
			}

			GetCardIdByUUID getAction = new GetCardIdByUUID();
			getAction.setUuid(localUid);
			getAction.setAttrId(CardRelationUtils.REPLICATION_UUID);
			ObjectId destinationCardId = execAction(getAction);
			isLocalCardNew |= destinationCardId == null;

			setReplicationUuid(replicationPackage, localUid);
			ObjectId replicatedCardId = new ObjectId(Card.class, replicCard.<IntegerAttribute>getAttributeById(CardRelationUtils.REPLIC_CARDID).getValue());

			handler.setSourceCardId(replicatedCardId);
			handler.preProcessPackage();
			handler.setReplicationCardId(replicCard.getId());

			Card card;
			if (isLocalCardNew) {
				card = createLocalCard(replicationPackage);
				handler.postProcessPackageForNewCard(card);
				ReplicationCardHandler replicCardHandler = new ReplicationCardHandler(getQueryFactory(), getDatabase(), getUser());
				replicCardHandler.setReplicCardRootLink(card, replicCard);
			} else {
				card = updateCard(replicationPackage, replicCard, destinationCardId);
				handler.postProcessPackage(card);
			}
			saveCard(card, getSystemUser());
		} catch (Exception ex) {
			logger.error("Error on execute do process in " + this.getClass().getName(), ex);
			sendErrorNotification(ex);
		}
		return null;
	}

	protected Card createLocalCard(ReplicationPackage replicationPackage) throws DataException, ServiceException,
			JAXBException, IOException {
		GetReservCardId getReservCardId = new GetReservCardId();
		ActionQueryBase actionQueryBase = getQueryFactory().getActionQuery(GetReservCardId.class);
		actionQueryBase.setAction(getReservCardId);
		Long result = getDatabase().executeQuery(getSystemUser(), actionQueryBase);
		Card createdCard = createCard(replicationPackage, result, UpdateType.NEW_LOCAL);
		return loadCardById(createdCard.getId());
	}

	// В пришедшем пакете будут присутствовать ссылки на другие карточки в виде UUID данных карточек.
	// В нашем случае мы имеем идентификаторы "промежуточных" карточек, поэтому при реализации по умолчанию
	// получится ситуация, что к текущей итоговой карточке привязываются промежуточные, что является неверным.
	// Данный метод переопределяет данный метод таким образом, что вводится дополнительный шаг по извлечению
	// Итоговой карточки с помощью local UUID, хранящегося в карточке Репликации. Однако описываемое выше требование
	// не актуально для особой категории карточек - "справочных". Для данного рода карточек нет необходимости создавать
	// локальную копию (это например, организация, персона и т.п.), а нужно использовать ту же "промежуточную" версию.
	// Почему здесь используется факт, что у нас в карточке Репликации уже есть ссылка на Итоговую. Это вызвано тем
	// фактом, что данный процессор начнет свою работу только когда Промежуточная карточка "Принята", т.е. уже
	// разрешены все зависимости (а соответственно уже созданы итоговые копии везде, где необходимо)
	@Override
	protected List<ObjectId> searchCardByGuid(String guid, long templateId) throws DataException, ServiceException {
		ReplicationCardHandler replicHandler = new ReplicationCardHandler(getQueryFactory(), getDatabase(), getUser());
		List<Card> replicCards = replicHandler.searchActiveReplicationCardsByGuid(guid, CardRelationUtils.REPLIC_LOCAL_GUID);
		StringAttribute localUidAttribute = replicCards.get(0).getAttributeById(CardRelationUtils.REPLIC_LOCAL_GUID);

		List<ObjectId> localCards = null;
		if (localUidAttribute != null && localUidAttribute.getValue() != null
				&& !localUidAttribute.getValue().isEmpty()) {
			localCards = super.searchCardByGuid(localUidAttribute.getValue(), templateId);
		}
		if (localCards == null || localCards.isEmpty()) {
			List<ObjectId> replicatedCardIds = super.searchCardByGuid(guid, templateId);
			if (replicatedCardIds != null) {
				for (ObjectId replicatedCardId : replicatedCardIds) {
					Card replicatedCard = loadCardById(replicatedCardId);
					if (!dictionaryTemplates.contains(replicatedCard.getTemplate())) {
						throw new IllegalStateException("Card [" + replicatedCard.getId() + "] of template ["
								+ replicatedCard.getTemplate() + "] should have local copy");
					}
				}
			}
			return replicatedCardIds;
		}
		return localCards;
	}

	protected Card updateCard(ReplicationPackage replicationPackage, Card replicationCard, ObjectId cardId) throws DataException, ServiceException, JAXBException, IOException {
		Card card = loadCardById(cardId);
		Boolean lockedSuccess = execAction(new CheckIsLocked(card));
		if (lockedSuccess) {
			try {
				changeCard(replicationPackage, card, UpdateType.UPDATE);
			} catch (ObjectLockedException error) {
				logger.error("An unexpected exception occurred while trying to LOCK the card with ID: " + card.getId().getId(), error);
				throw error;
			} finally {
				try {
					execAction(new UnlockObject(card));
				} catch (Exception error) {
					logger.error("An unexpected exception occurred while trying to UNLOCK the card with ID: " + card.getId().getId(), error);
				}
			}
		} else {
			logger.warn("Cannot execute '" + this.getClass().getSimpleName() + ".updateCard()' because card (ID: " + card.getId().getId() + ") is [locked].");
			changeCardStatus(replicationCard, "jbr.replication.acceptedToQueued");
			logger.warn("Replication card (ID: " + replicationCard.getId().getId() + ") moved into status '" + ObjectId.state("jbr.replication.queued") + "'");
		}
		return loadCardById(card.getId());
	}

	protected void setReplicationUuid(ReplicationPackage replicationPackage, String uuid) throws IllegalStateException {
		for (Attribute attribute : replicationPackage.getCard().getAttribute()) {
			if (CardRelationUtils.REPLICATION_UUID.getId().equals(attribute.getCode())) {
				List<String> values = attribute.getStringValue();
				if (values.size() != 1) {
					throw new IllegalStateException("REPLICATION_UUID should have exact one value");
				}
				values.clear();
				values.add(uuid);
			}
		}
		replicationPackage.getCard().setGuid(uuid);
	}

	protected CopyChangesToLocalCardHandler createLocalCopyHandler(ReplicationPackage replicationPackage)
			throws BeansException {
		final String requiredBeanName = "createLocalCopy" + replicationPackage.getCard().getTemplateId();
		CopyChangesToLocalCardHandler handler = null;
		if (getBeanFactory().containsBean(requiredBeanName)) {
			handler = (CopyChangesToLocalCardHandler) getBeanFactory().getBean(requiredBeanName);
			handler.setReplicationPackage(replicationPackage);
			if (handler instanceof DatabaseClient) {
				((DatabaseClient) handler).setJdbcTemplate(getJdbcTemplate());
			}
		}
		return handler;
	}

	@Override
	public Card loadCardById(ObjectId id) throws DataException {
		ObjectQueryBase objQuery = getQueryFactory().getFetchQuery(Card.class);
		objQuery.setId(id);
		objQuery.setDatabase(getDatabase());
		return getDatabase().executeQuery(getUser(), objQuery);
	}

	@Override
	protected void cleanAttributesInDatabase(Card card, ReplicationPackage.Card cardFromXml) throws DataAccessException {
		for (Attribute xmlAttribute : cardFromXml.getAttribute()) {
			getJdbcTemplate().update("DELETE FROM attribute_value WHERE card_id = ? AND attribute_code = ?",
					new Object[] {card.getId().getId(), xmlAttribute.getCode()},
					new int[] {Types.NUMERIC, Types.VARCHAR});
		}
	}

	@Override
	protected ObjectId getReplicationGuidAttr() {
		return CardRelationUtils.REPLIC_LOCAL_GUID;
	}
}
