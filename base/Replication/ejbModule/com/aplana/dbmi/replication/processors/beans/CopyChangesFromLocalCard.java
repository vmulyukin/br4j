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
package com.aplana.dbmi.replication.processors.beans;

import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.GetCardIdByUUID;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.replication.action.LinkResolver;
import com.aplana.dbmi.replication.tool.CardRelationUtils;
import com.aplana.dbmi.replication.tool.ReplicationCardHandler;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dbmi.service.impl.DatabaseClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class CopyChangesFromLocalCard extends CopyChangesFromLocalCardHandler implements BeanFactoryAware, DatabaseClient {

	private List<ObjectId> copiedAttributes;
	private BeanFactory beanFactory;
	private DataServiceFacade dataService;
	private JdbcTemplate jdbc;
	private Log logger = LogFactory.getLog(getClass());
	private List<ObjectId> dictionaryTemplates;

	private String guid;
	private String addresseeGuid;
	private String senderGuid;
	private Card changedCard;
	private Card destinationCard;
	private Card replicationCard;
	private boolean isFiltered;

	@Override
	public void cardCreated(Card card) throws DataException {
		cardChanged(card, false);
	}

	@Override
	public void cardChanged(Card card) throws DataException {
		cardChanged(card, true);
	}

	protected void cardChanged(Card card, boolean isAttributesFiltered) throws DataException {
		isFiltered = isAttributesFiltered;
		boolean isInitialized = initialize(card);
		if (!isInitialized) {
			return;
		}
		calculateDestinationCard();

		if (destinationCard.getId() != null) {
			dataService.doAction(new LockObject(destinationCard));
		}
		try {
			// Делаем предсохранение карточки, чтобы не затирались атрибуты при копировании (напр. Автор)
			ObjectId destCardId = dataService.saveObject(destinationCard);
			destinationCard.setId(destCardId);
			copyChanges();
			destCardId = dataService.saveObject(destinationCard);
			destinationCard.setId(destCardId);
			afterDestinationCardSaved();
		} catch (Exception e) {
			logger.error("Error while saving changes into card " + destinationCard.getId());
			if (e instanceof DataException) {
				throw (DataException)e;
			} else {
				throw new DataException(e);
			}
		} finally {
			if (destinationCard.getId() != null) {
				dataService.doAction(new UnlockObject(destinationCard));
			}
		}
	}

	private boolean initialize(Card source) throws DataException {
		if (source == null) {
			throw new NullPointerException("Changed card could not be null");
		}
		changedCard = source;
		destinationCard = null;

		ObjectId replicationCardId = CardRelationUtils.getReplicationCardForLocalCopy(changedCard, dataService);
		if (replicationCardId == null) {
			return false;
		}
		replicationCard = dataService.getById(replicationCardId);
		StringAttribute replicationUidAttr = replicationCard.getAttributeById(CardRelationUtils.REPLIC_GUID);
		guid = replicationUidAttr == null ? null : replicationUidAttr.getValue();
		if (guid == null || guid.isEmpty()) {
			logger.error("Some strange: replication GUID is empty in card " + replicationCardId.getId());
			return false;
		}
		StringAttribute replicationAddressee = replicationCard.getAttributeById(CardRelationUtils.REPLIC_ADDRESSEE);
		addresseeGuid = replicationAddressee == null ? null : replicationAddressee.getValue();
		StringAttribute replicationSender = replicationCard.getAttributeById(CardRelationUtils.REPLIC_SENDER);
		senderGuid = replicationSender == null ? null : replicationSender.getValue();
		return true;
	}

	private void calculateDestinationCard() throws DataException {
		GetCardIdByUUID getAction = new GetCardIdByUUID();
		getAction.setUuid(guid);
		getAction.setAttrId(CardRelationUtils.REPLICATION_UUID);
		ObjectId destCardId = dataService.doAction(getAction);
		if (destCardId == null) {
			CreateCard createCardAction = new CreateCard(changedCard.getTemplate());
			destinationCard = dataService.doAction(createCardAction);
			StringAttribute guidAttribute = destinationCard.getAttributeById(CardRelationUtils.REPLICATION_UUID);
			guidAttribute.setValue(guid);
			destinationCard.setActive(false);
		} else {
			destinationCard = dataService.getById(destCardId);
		}
	}

	public void copyChanges() throws DataException {
		specificCopy();
		if (isFiltered()) {
			copyChangesByFilteredList();
		} else {
			copyChangesByAllAttributes();
		}
	}

	protected void copyChangesByFilteredList() throws DataException {
		for (ObjectId attributeId : getCopiedAttributes()) {
			Attribute sourceAttr = changedCard.getAttributeById(attributeId);
			Attribute destAttr = destinationCard.getAttributeById(attributeId);
			if (sourceAttr == null || destAttr == null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Attribute [" + attributeId
							+ "] is configured to copy but is not found in source card ["
							+ getCardDescription(changedCard) + "] or destination card ["
							+ getCardDescription(destinationCard) + "]");
				}
				continue;
			}
			copyAttribute(sourceAttr, destAttr);
		}
	}

	protected void copyChangesByAllAttributes() throws DataException {
		for (DataObject block : changedCard.getAttributes()) {
			if (block instanceof Attribute) {
				processSourceAttribute((Attribute) block);
			} else if (block instanceof AttributeBlock) {
				for (Attribute attribute : ((AttributeBlock) block).getAttributes()) {
					processSourceAttribute(attribute);
				}
			}
		}
	}
	
	private boolean processSourceAttribute(Attribute sourceAttr) throws DataException {
		Attribute destAttr = destinationCard.getAttributeById(sourceAttr.getId());
		if (destAttr == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Attribute [" + sourceAttr.getId() + "] is present in source card ["
						+ getCardDescription(changedCard) + "] but is not found in  destination card ["
						+ getCardDescription(destinationCard) + "]");
			}
			return false;
		}
		if (destAttr.getId().equals(CardRelationUtils.REPLICATION_UUID)) {
			logger.error("Attribute " + CardRelationUtils.REPLICATION_UUID + " not copied");
			return false;
		}
		copyAttribute(sourceAttr, destAttr);
		return true;
	}

	protected void specificCopy() throws DataException {
	}

	protected void copyAttribute(Attribute source, Attribute destination) throws DataException {
		if (source instanceof StringAttribute || source instanceof IntegerAttribute || source instanceof LongAttribute
				|| source instanceof PersonAttribute || source instanceof ListAttribute
				|| source instanceof DateAttribute) {
			destination.setValueFromAttribute(source);
		} else if (source instanceof CardLinkAttribute) {
			try {
				copyFromCardLink((CardLinkAttribute) source, (CardLinkAttribute) destination);
			} catch (DataException ex) {
				throw ex;
			} catch (Exception ex) {
				throw new DataException(ex);
			}
		} else if (logger.isDebugEnabled()) {
			logger.debug("Attribute [" + source.getId().getId() + "] is not copied to card [" + getDestinationCardId()
					+ "] because type of attribute [" + source.getClass() + "] is not supported now");
		}
	}

	protected void copyFromCardLink(CardLinkAttribute sourceAttribute, CardLinkAttribute destinationAttribute)
			throws DataException, BeansException, JAXBException, IOException {
		ObjectId[] sourceIds = sourceAttribute.getIdsArray();
		//удаляем старые значения
		destinationAttribute.clear();
		//если нечего копировать, то сразу выходим
		if (sourceIds == null) {
			return;
		}

		//добавляем новые значения
		for (ObjectId id : sourceIds) {
			LinkResolver<ObjectId> resolveTemplate = new LinkResolver<ObjectId>();
			resolveTemplate.setCardId(id);
			resolveTemplate.setLink(Card.ATTR_TEMPLATE.getId().toString());
			List<ObjectId> template = dataService.doAction(resolveTemplate);
			ObjectId templateId = template.get(0);
			//если справочник, то просто копируем значение
			if (dictionaryTemplates.contains(templateId)) {
				destinationAttribute.addLinkedId(id);

				if (sourceAttribute instanceof TypedCardLinkAttribute && 
					destinationAttribute instanceof TypedCardLinkAttribute) {
					((TypedCardLinkAttribute) destinationAttribute).addType((Long) id.getId(),
							(Long) ((TypedCardLinkAttribute) sourceAttribute).getCardType(id).getId());
					if (sourceAttribute instanceof DatedTypedCardLinkAttribute &&
						destinationAttribute instanceof DatedTypedCardLinkAttribute) {
						((DatedTypedCardLinkAttribute) destinationAttribute).addDate((Long) id.getId(),
						((DatedTypedCardLinkAttribute) sourceAttribute).getCardDate(id));
					}
				}
			} else {
				//Не справочник. тогда смотрим это уже итоговый док пришел? если да, то вставляем
				//в промежуточную карточку (dest атрибут) соответ. промежуточную карточку этой итоговой.
				//Иначе новая карточка - делаем ее как итоговую, создаем новую соответ. промежуточную
				//и ее (промежут.) цепляем в dest атрибут.
				
				ObjectId localCard = CardRelationUtils.getLocalCardId(id, dataService);
				if (localCard != null) {
					//достаем промежут. карточку и ее вставляем в атрибут
					ObjectId baseCardId = CardRelationUtils.getBaseCardForLocalCard(localCard, dataService);
					destinationAttribute.addLinkedId(baseCardId);
				} else {
					//создаем новый промежут. док, связываем его с новым итоговым и заносим в атрибут
					Card linkedCard = dataService.getById(id);
					CopyChangesFromLocalCardHandler handler = createHandler(linkedCard, beanFactory, jdbc);
					if (handler == null) {
						continue;
					}
					CardRelationUtils.createReplicationCardForLocalCopy(linkedCard, senderGuid, addresseeGuid, dataService, getJdbcTemplate());
					handler.cardCreated(linkedCard);
					if (handler.getDestinationCardId() != null) {
						destinationAttribute.addLinkedId(handler.getDestinationCardId());
						if (sourceAttribute instanceof TypedCardLinkAttribute
								&& destinationAttribute instanceof TypedCardLinkAttribute) {
							((TypedCardLinkAttribute) destinationAttribute).addType((Long) handler
									.getDestinationCardId().getId(), (Long) ((TypedCardLinkAttribute) sourceAttribute)
									.getCardType(id).getId());
						}
					}
				}
			}
		}
	}

	protected void afterDestinationCardSaved() throws DataException, ServiceException, JAXBException, IOException {
		ObjectId cardId = getDestinationCardId();
		ObjectId cardState = getChangedCard().getState();
		getJdbcTemplate().update("UPDATE card set status_id = ? where card_id = ?",
				new Object[] { cardState.getId(), cardId.getId() }, 
				new int[] { Types.NUMERIC, Types.NUMERIC });
		
		ReplicationCardHandler handler = new ReplicationCardHandler(dataService);
		handler.setReplicCardRootLink(changedCard, replicationCard);
	}

	private static String getCardDescription(Card card) {
		return card.getId() == null ? "empty id" : String.valueOf(card.getId().getId()) + ", template = "
				+ card.getTemplate().getId() + ", status = " + card.getState().getId();
	}

	public List<ObjectId> getCopiedAttributes() {
		if (copiedAttributes == null) {
			copiedAttributes = new ArrayList<ObjectId>();
		}
		return copiedAttributes;
	}

	public void setCopiedAttributes(List<ObjectId> copiedAttributes) {
		this.copiedAttributes = copiedAttributes;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	public BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public DataServiceFacade getDataService() {
		return dataService;
	}

	public void setDataService(DataServiceFacade dataService) {
		this.dataService = dataService;
	}

	public void setAddresseeGuid(String guid) {
		this.addresseeGuid = guid;
	}
	
	public String getAddresseeGuid() {
		return addresseeGuid;
	}

	public void setSenderGuid(String guid) {
		this.senderGuid = guid;
	}

	protected Card getChangedCard() {
		return changedCard;
	}

	protected Card getDestinationCard() {
		return destinationCard;
	}

	@Override
	public ObjectId getDestinationCardId() {
		return destinationCard == null ? null : destinationCard.getId();
	}

	@Override
	public void setJdbcTemplate(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	protected JdbcTemplate getJdbcTemplate() {
		return jdbc;
	}

	protected boolean isFiltered() {
		return isFiltered || !getCopiedAttributes().isEmpty();
	}

	public List<ObjectId> getDictionaryTemplates() {
		return dictionaryTemplates;
	}

	public void setDictionaryTemplates(List<ObjectId> dictionaryTemplates) {
		this.dictionaryTemplates = dictionaryTemplates;
	}
}
