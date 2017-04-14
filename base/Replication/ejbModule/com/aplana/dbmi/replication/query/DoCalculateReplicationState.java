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
package com.aplana.dbmi.replication.query;

import com.aplana.dbmi.model.*;
import com.aplana.dbmi.model.util.Init;
import com.aplana.dbmi.replication.action.CalculateReplicationState;
import com.aplana.dbmi.replication.action.LinkResolver;
import com.aplana.dbmi.replication.packageconfig.PackageType;
import com.aplana.dbmi.replication.packageconfig.ReplicationPackage;
import com.aplana.dbmi.replication.processors.ReplicationConfiguration;
import com.aplana.dbmi.replication.processors.beans.CopyChangesFromLocalCardHandler;
import com.aplana.dbmi.replication.templateconfig.AttributeConditionType;
import com.aplana.dbmi.replication.templateconfig.ReplicationTemplateConfig.Template;
import com.aplana.dbmi.replication.templateconfig.TemplateFilter;
import com.aplana.dbmi.replication.tool.CardRelationUtils;
import com.aplana.dbmi.replication.tool.ReplicationCardHandler;
import com.aplana.dbmi.replication.tool.ReplicationUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.query.WriteQuery;
import org.springframework.dao.DataAccessException;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.*;

public class DoCalculateReplicationState extends ActionQueryBase implements WriteQuery {
	private static final long serialVersionUID = 2L;

	protected Card currentCard;
	protected Card mainCard;

	protected Template templateConfig;
	protected Template mainTemplateConfig;

	@Override
	public Object processQuery() throws DataException {
		CalculateReplicationState action = getAction();
		currentCard = action.getCard();
		try {
			recalculateState(currentCard);
		} catch (JAXBException ex) {
			throw new DataException(ex);
		} catch (IOException ex) {
			throw new DataException(ex);
		}
		return null;
	}

	/**
	 * Производит работу по актуализации текущего состояния для обрабатываемой
	 * карточки:
	 * <ol>
	 * <li>Если конфигурация для шаблона обрабатываемой карточки не задан, то
	 * действий не производим</li>
	 * <li>Если задана настройка для определения "основной" карточки, то
	 * вычисляем ее (и настройку для нее), иначе в качестве основной используем
	 * текущую</li>
	 * <li>Проверяем условия остановки для "основной" карточки и если
	 * выполняются, то для всех адресатов останавливаем репликацию</li>
	 * <li>Вычисляем адресатов на основе конфигурации</li>
	 * <li>Если условия для вычисления не заданы, то для "независимой" карточки
	 * это означает, что они реплицируются всегда, для "зависимой" - что решение
	 * о старте репликации на основе этой карточки не может приниматься</li>
	 * <li>Если условия для вычисления прошли проверку, то стартуем репликацию
	 * для определенных адресатов через "основную"А карточку</li>
	 * </ol>
	 *
	 * @param changedCard
	 *            измененная карточка
	 * @throws JAXBException
	 * @throws IOException
	 * @throws DataException
	 */
	private void recalculateState(Card changedCard) throws JAXBException, IOException, DataException {
		if(!initTemplateConfig(changedCard)) {
			return;
		}

		List<String> addressee = calculateConfiguredAddressee(changedCard);
		if (addressee.isEmpty()) {
			if (logger.isDebugEnabled()) {
				logger.debug("There is no configured addressees for [" + getCardDescription(changedCard) + "]");
			}
			return;
		}

		boolean isCurrentCardForReplication;
		if (templateConfig.getStartCondition() == null) {
			isCurrentCardForReplication = ReplicationConfiguration.isTemplateIndependent(templateConfig);
		} else {
			isCurrentCardForReplication = checkFilters(templateConfig.getStartCondition(), changedCard);
		}
		if (isCurrentCardForReplication) {
			if (logger.isDebugEnabled()) {
				logger.debug("Mark card [" + getCardDescription(mainCard) + "] for replication. Addressees: "
						+ addressee);
			}
			markForReplication(addressee);
		}

	}

	protected boolean initTemplateConfig(Card changedCard) throws DataException, JAXBException, IOException {
		if (changedCard == null) {
			return false;
		}

		Long templateId = (Long) changedCard.getTemplate().getId();
		templateConfig = ReplicationConfiguration.getTemplateConfig(templateId);
		if (templateConfig == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Replication for template [" + templateId + "] is not configured. Skip card ["
						+ getCardDescription(changedCard) + "]");
			}
			return false;
		}

		DataServiceFacade facade = new DataServiceFacade(getDatabase(), getUser(), getQueryFactory());
		ObjectId replicationCardForLocalCopy = CardRelationUtils.getReplicationCardForLocalCopy(changedCard, facade);

		// if this card is local copy
		if (replicationCardForLocalCopy != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Changed card [" + getCardDescription(changedCard) + "] is local copy of replicated card");
			}
			return false;
		}

		if (templateConfig.getRoot() != null && templateConfig.getRoot().getPath() != null) {
			String rootPathSetting = templateConfig.getRoot().getPath();
			mainCard = CardRelationUtils.getLinkedCard(changedCard.getId(), rootPathSetting, facade);
			if (mainCard == null) {
				if (logger.isWarnEnabled()) {
					logger.warn("Root path [" + rootPathSetting + "] is configured for ["
							+ getCardDescription(changedCard)
							+ "] but actually was not calculated. Skip calculation for this card.");
				}
				return false;
			} else if (logger.isDebugEnabled()) {
				logger.debug("Root path [" + rootPathSetting + "] is configured for ["
						+ getCardDescription(changedCard) + "]. Main card is calculated ["
						+ getCardDescription(mainCard) + "]");
			}

			replicationCardForLocalCopy = CardRelationUtils.getReplicationCardForLocalCopy(mainCard, facade);
			// if main card is local copy
			if (replicationCardForLocalCopy != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Main card [" + getCardDescription(changedCard) + "] is local copy of replicated card");
				}

				CopyChangesFromLocalCardHandler handler = CopyChangesFromLocalCardHandler.createHandler(changedCard, getBeanFactory(), getJdbcTemplate());
				if (handler != null) {
					List<String> addressees = CardRelationUtils.resolveLink(replicationCardForLocalCopy, facade, CardRelationUtils.REPLIC_ADDRESSEE.getId());
					List<String> senders = CardRelationUtils.resolveLink(replicationCardForLocalCopy, facade, CardRelationUtils.REPLIC_SENDER.getId());

					//String sender = ReplicationConfiguration.getReplicationNodeConfig().getServerGUID();
					String sender = senders.get(0);
					String addressee = addressees.get(0);

					CardRelationUtils.createReplicationCardForLocalCopy(changedCard, sender, addressee, facade, getJdbcTemplate());
					handler.cardCreated(changedCard);
				}

				return false;
			}
			mainTemplateConfig = ReplicationConfiguration.getTemplateConfig((Long) mainCard.getTemplate().getId());
		} else {
			mainCard = changedCard;
			mainTemplateConfig = templateConfig;
		}

		if (mainTemplateConfig == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Replication for template [" + templateId + "] (main card) is not configured. Skip card ["
						+ getCardDescription(changedCard) + "]");
			}
			return false;
		}
		return true;
	}

	protected static String getCardDescription(Card card) {
		return card.getId() == null ? "empty id" : String.valueOf(card.getId().getId()) + ", template = "
				+ card.getTemplate().getId() + ", status = " + card.getState().getId();
	}

	@SuppressWarnings("unused")
	private Card getParentCard(Card changedCard, String path) throws DataException {
		return CardRelationUtils.getLinkedCard(changedCard.getId(), path, new DataServiceFacade(getDatabase(), getUser(), getQueryFactory()));
	}

	protected boolean checkFilters(TemplateFilter filter, Card card) throws DataException {
		boolean result = true;
		LinkResolver<Object> resolver = new LinkResolver<Object>();
		ActionQueryBase resolverQuery = getQueryFactory().getActionQuery(LinkResolver.class);
		resolverQuery.setAction(resolver);
		// Проверка фильтров
		for (TemplateFilter.Attribute attributeFilter : filter.getAttribute()) {
			resolver.setCardId(card.getId());
			resolver.setLink(attributeFilter.getName());
			List<Object> attrValues = getDatabase().executeQuery(getUser(), resolverQuery);
			if (attributeFilter.getCondition() == AttributeConditionType.EMPTY) {
				result = attrValues.size() == 0;
			} else if (attributeFilter.getCondition() == AttributeConditionType.ENUM) {
				// Положительный если есть хотя бы одлно совпадение
				boolean find = false;
				conditionValues: for (Object condValue : resolveValues(attributeFilter.getValue())) {
					for (Object attrValue : attrValues) {
						Object val = attrValue instanceof ObjectId ? ((ObjectId)attrValue).getId().toString() : attrValue.toString();
						if (condValue.equals(val)) {
							find = true;
							break conditionValues;
						}
					}
				}
				result = find;
			} else if (attributeFilter.getCondition() == AttributeConditionType.NOT_EMPTY) {
				result = attrValues.size() != 0;
			} else if (attributeFilter.getCondition() == AttributeConditionType.NOT_ENUM) {
				// Положительный когда нет ни одного совпадения
				boolean find = false;
				conditionValues: for (Object condValue : resolveValues(attributeFilter.getValue())) {
					for (Object attrValue : attrValues) {
						Object val = attrValue instanceof ObjectId ? ((ObjectId)attrValue).getId().toString() : attrValue.toString();
						if (condValue.equals(val)) {
							find = true;
							break conditionValues;
						}
					}
				}
				result = !find;
			}

			result = result && checkFilters(attributeFilter, card);

			if (result) {
				break;
			}
		}
		return result;
	}

	/**
	 * В качестве допустимых условий для сравнения, кроме примитивных значений
	 * (строк), допустимы также выражения вида ${expression}, где expression в
	 * формате, принимаемом {@link LinkResolver}. В качестве базовой карточки
	 * для вычисления используется обрабатываемая карточка, но не базовая.
	 *
	 * @param condValues
	 *            условия, допускающие вычислимые выражения
	 * @return вычисленные примитивные условия
	 * @throws DataException
	 */
	private List<Object> resolveValues(List<String> condValues) throws DataException {
		List<Object> resultValues = Init.arrayList();
		LinkResolver<Object> resolver = new LinkResolver<Object>();
		ActionQueryBase resolverQuery = getQueryFactory().getActionQuery(LinkResolver.class);
		resolverQuery.setAction(resolver);
		for (String condValue : condValues) {
			if (condValue == null) {
				continue;
			}
			if (condValue.startsWith("${") && condValue.endsWith("}")) {
				resolver.setCardId(currentCard.getId());
				resolver.setLink(condValue.substring(2, condValue.length() - 1));
				List<Object> list = getDatabase().executeQuery(getUser(), resolverQuery);
				resultValues.addAll(list);
			} else {
				resultValues.add(condValue);
			}
		}
		return resultValues;
	}

	private List<String> calculateConfiguredAddressee(Card card) throws DataException, JAXBException, IOException {
		List<String> result = Init.arrayList();
		Template templateConfig = ReplicationConfiguration.getTemplateConfig((Long) card.getTemplate().getId());
		if (templateConfig.getAddressee() == null) {
			if (ReplicationConfiguration.isTemplateIndependent(templateConfig)
					&& ReplicationConfiguration.getReplicationNodeConfig().getReplicationMember() != null) {
				result = ReplicationConfiguration.getReplicationNodeConfig().getReplicationMember().getGUID();
			}
		} else {
			LinkResolver<String> resolver = new LinkResolver<String>();
			ActionQueryBase actionQuery = getQueryFactory().getActionQuery(LinkResolver.class);
			actionQuery.setAction(resolver);
			for (String attrGUID : templateConfig.getAddressee().getAttributeGUID()) {
				resolver.setCardId(card.getId());
				resolver.setLink(attrGUID);
				List<String> values = getDatabase().executeQuery(getUser(), actionQuery);
				for (String value : values) {
					if (!result.contains(value)) {
						result.add(value);
					}
				}
			}
		}

		// Удаляем адресатов из нашей системы для исключения отправки самому себе
		if (result.contains(ReplicationConfiguration.getReplicationNodeConfig().getServerGUID())) {
			result.remove(ReplicationConfiguration.getReplicationNodeConfig().getServerGUID());
		}
		// Однако, не удаляем адресатов-организации, чтобы изменения попадали из одной организации в другую
		// в рамках одной системы. Это еще нужно все проверить в рамках тестирования
		// TODO: пока выключаем возможность пересылки между орг. в рамках одной системы
		if (ReplicationConfiguration.getReplicationNodeConfig().getOrganizations() != null) {
			for (String orgUid : ReplicationConfiguration.getReplicationNodeConfig().getOrganizations().getGUID()) {
				result.remove(orgUid);
			}
		}
		return result;
	}

	private void markForReplication(List<String> addressee) throws DataException,
			DataAccessException, JAXBException, IOException {
		DataServiceFacade service = new DataServiceFacade(getDatabase(), getUser(), getQueryFactory());
		ReplicationCardHandler replicationHandler = new ReplicationCardHandler(service);
		String cardGuid;
		StringAttribute guidAttr = mainCard.getAttributeById(CardRelationUtils.REPLICATION_UUID);
		if (guidAttr != null && guidAttr.getStringValue() != null && guidAttr.getStringValue().length() > 0) {
			cardGuid = guidAttr.getStringValue();
		} else {
			cardGuid = ReplicationUtils.formUuidAttribute(mainCard, service, getJdbcTemplate());
		}

		Set<String> goalAddressees = Init.hashSet(addressee);
		List<Card> replicationCards = replicationHandler.searchActiveReplicationCardsByGuid(cardGuid,
				CardRelationUtils.REPLIC_ADDRESSEE);

		Set<String> allowedAddressee = Init.hashSet(replicationCards.size());
		for (Card replicCard : replicationCards) {
			StringAttribute addresseeAttr = replicCard.getAttributeById(CardRelationUtils.REPLIC_ADDRESSEE);
			if (addresseeAttr != null) {
				goalAddressees.remove(addresseeAttr.getValue());
				allowedAddressee.add(addresseeAttr.getValue());
			}
		}

		if (!allowedAddressee.isEmpty()) {
			ListAttribute docTypeAttr = mainCard.getAttributeById(CardRelationUtils.REPLIC_DOC_TYPE);
			if (ReplicationConfiguration.isTemplateIndependent(mainTemplateConfig)) {
				StringAttribute ownerAttr = mainCard.getAttributeById(CardRelationUtils.REPLIC_OWNER);
				if (ownerAttr != null) {
					String owner = ownerAttr.getValue();
					if (!ReplicationConfiguration.getReplicationNodeConfig().getServerGUID().equals(owner)) {
						return;
					}
				}
			} else if (docTypeAttr != null && !docTypeAttr.isEmpty()) {
				ObjectId replDocTypeReplic = ObjectId.predefined(ReferenceValue.class, "jbr.replication.replicatedDocument");
				if (replDocTypeReplic.equals(docTypeAttr.getValue().getId())) {
					return;
				}
			}
		}

		for (String goalAddressee : goalAddressees) {
			ReplicationPackage pcg = new ReplicationPackage();
			pcg.setPackageType(PackageType.CARD);
			pcg.setAddressee(goalAddressee);
			pcg.setSender(ReplicationConfiguration.getReplicationNodeConfig().getServerGUID());
			ReplicationPackage.Card pcgCard = new ReplicationPackage.Card();
			pcg.setCard(pcgCard);
			pcgCard.setGuid(cardGuid);
			replicationHandler.createBlankCard(pcg);
		}
	}
}
