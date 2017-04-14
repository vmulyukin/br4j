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
import com.aplana.dbmi.action.Search.TextSearchConfigValue;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.replication.action.GetReservCardId;
import com.aplana.dbmi.replication.packageconfig.ReplicationPackage;
import com.aplana.dbmi.replication.processors.ReplicationConfiguration;
import com.aplana.dbmi.replication.templateconfig.ReplicationTemplateConfig;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dbmi.service.impl.Database;
import com.aplana.dbmi.service.impl.QueryFactory;
import com.aplana.dbmi.service.impl.UserData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.*;

/**
 * Класс используется для работы с карточкой Репликации. Уникальным ключом
 * карточки Репликация является набор, состоящий из Уникального идентификатора,
 * Отправителя и Получателя. Карточка-основание вычисляется на основании Пакета
 * по GUID либо по Ключевым словам. При этом если карточка-основание не найдена,
 * то ID для нее все равно резервируется.
 */
public class ReplicationCardHandler {

	public enum ProcessType {
		RECEIVE,
		SEND
	}
	
	public static final int REPLICATION_IN_PROCESS_TRUE = 1;
	protected final Log logger = LogFactory.getLog(getClass());
	private DataServiceFacade dataService;

	public ReplicationCardHandler(QueryFactory queryFactory, Database database, UserData user) {
		this.dataService = new DataServiceFacade(database, user, queryFactory);
	}
	
	public ReplicationCardHandler(DataServiceFacade dataService) {
		this.dataService = dataService;
	}

	public Card processPackage(ReplicationPackage pcg, ProcessType type) throws DataException, JAXBException, IOException {
		List<Card> cards = searchReplicationCard(pcg);
		Card result;
		if (cards.size() == 0) {
			result = createNewCard(pcg, type);
		} else {
			Card card = dataService.getById(cards.get(0).getId());
			result = changeCard(pcg, card, type, false);
		}
		return result;
	}

	public List<Card> searchReplicationCard(ReplicationPackage pcg) throws DataException {
		ReplicationPackage.Card cardXML = pcg.getCard();
		Search search = new Search();
		search.setTemplates(Collections.singleton(DataObject.createFromId(Template.class, "jbr.replication")));
		search.setByAttributes(true);

		search.addStringAttribute(CardRelationUtils.REPLIC_GUID, 	  cardXML.getGuid(),  TextSearchConfigValue.EXACT_MATCH);
		search.addStringAttribute(CardRelationUtils.REPLIC_ADDRESSEE, pcg.getAddressee(), TextSearchConfigValue.EXACT_MATCH);
		search.addStringAttribute(CardRelationUtils.REPLIC_SENDER, 	  pcg.getSender(),	  TextSearchConfigValue.EXACT_MATCH);

		SearchResult cards = dataService.doAction(search);
		return cards.getCards();
	}

	public List<Card> getActiveReplicationCards(Card card, ObjectId... attributes) throws DataException {
		String cardGuid;
		StringAttribute guidAttr = card.getAttributeById(CardRelationUtils.REPLICATION_UUID);
		if (guidAttr != null && guidAttr.getStringValue() != null && guidAttr.getStringValue().length() > 0) {
			cardGuid = guidAttr.getStringValue();
		} else {
			return Collections.emptyList();
		}
		return searchActiveReplicationCardsByGuid(cardGuid, attributes);
	}

	public List<Card> searchActiveReplicationCardsByGuid(String guid, ObjectId... attributes) throws DataException {
		Search search = new Search();
		search.setTemplates(Collections.singleton(DataObject.createFromId(Template.class, "jbr.replication")));
		search.setByAttributes(true);
		search.addStringAttribute(CardRelationUtils.REPLIC_GUID, guid, TextSearchConfigValue.EXACT_MATCH);
		search.addIntegerAttribute(ObjectId.predefined(IntegerAttribute.class, "jbr.replication.replicIsInProcess"),
				REPLICATION_IN_PROCESS_TRUE, REPLICATION_IN_PROCESS_TRUE);
		search.setColumns(CardUtils.createColumns(attributes));
		SearchResult cards = dataService.doAction(search);
		return cards.getCards();
	}
	
	public boolean setRecipientOrganization(Card mainCard, Card replicationCard) throws DataException {
		StringAttribute addressAtt = replicationCard.getAttributeById(CardRelationUtils.REPLIC_SENDER);
		String address = addressAtt.getValue();
		
		StrictSearch search = new StrictSearch();
		search.addStringAttribute(ObjectId.predefined(StringAttribute.class, "jbr.organisation.replUUID"), address);
		search.addTemplate(DataObject.createFromId(Template.class, "jbr.organization"));
		List<ObjectId> res = dataService.doAction(search);
		
		if (res != null && !res.isEmpty()) {
			CardLinkAttribute orgRecipient = mainCard.getAttributeById(ObjectId.predefined(CardLinkAttribute.class, "jbr.replication.orgRecipient"));
			if (orgRecipient != null) {
				orgRecipient.addIdsLinked(res);
			}
			return true;
		}
		return false;
	}
	
	public boolean setSenderOrganization(Card mainCard, Card replicationCard) throws DataException {
		/**
		 * Для организации-отправителя значение атрибута CardRelationUtils.REPLIC_ADDRESSEE не подходит.
		 * В атрибуте "jbr.replication.replicAddressee" указывается GUID хоста-отправителя, который 
		 * может отличаться от GUID организации-отправителя и в этом случае организация не будет найдена.
		 * Из документа берется "Автор", который автоматически проставляется при создании документа,
		 * а потом у этой персоны берётся организация и подставляется в качестве организации-отправителя.  
		*/
		List<ObjectId> res = CardRelationUtils.resolveLink(mainCard.getId(), dataService, "AUTHOR.JBR_PERS_ORG");
		if (res != null && !res.isEmpty()) {
			CardLinkAttribute orgSender = mainCard.getAttributeById(ObjectId.predefined(CardLinkAttribute.class, "jbr.replication.orgSender"));
			if (orgSender != null) {
				orgSender.addIdsLinked(res);
				return true;
			}
		}
		return false;
	}

	public Card createBlankCard(ReplicationPackage pcg) throws JAXBException, IOException, DataException {
		Card card = createCard("jbr.replication");
		fillMainAttributes(pcg, card, ProcessType.SEND, true);
		return saveCard(card);
	}
	
	public void setReplicCardRootLink(Card card, Card replicCard) throws DataException, ServiceException, JAXBException, IOException {

		ReplicationTemplateConfig.Template templateConfig = ReplicationConfiguration.getTemplateConfig((Long) card.getTemplate().getId());
		CardLinkAttribute rootDocs = replicCard.getCardLinkAttributeById(ObjectId.predefined(CardLinkAttribute.class, "jbr.replication.replicRootDoc"));
		if (null != templateConfig && null != templateConfig.getRoot() && null != templateConfig.getRoot().getPath()) {
			String rootPathSetting = templateConfig.getRoot().getPath();
			Card mainCard = CardRelationUtils.getLinkedCard(card.getId(), rootPathSetting, dataService);
			if (mainCard == null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Root path [" + rootPathSetting + "] is configured for ["
							+ getCardDescription(card)
							+ "] but actually was not calculated. Skip replication card link update for this card.");
				}
			}else {
				if (logger.isDebugEnabled()) {
					logger.debug("Root path [" + rootPathSetting + "] is configured for ["
							+ getCardDescription(card) + "]. Main card is calculated ["
							+ getCardDescription(mainCard) + "]");
				}
				if (rootDocs != null) {
					rootDocs.addLinkedId(mainCard.getId());
					doOverwriteCardAttributes(replicCard.getId(), rootDocs);
				}
			}
		}
	}

	private Card createNewCard(ReplicationPackage pcg, ProcessType type) throws DataException, JAXBException, IOException {
		Card card = createCard("jbr.replication");
		return changeCard(pcg, card, type, true);
	}

	private Card changeCard(ReplicationPackage pcg, Card card, ProcessType type, boolean isNew) throws JAXBException, DataException, IOException {
		fillMainAttributes(pcg, card, type, isNew);

		Card cardHistoru = createCard("jbr.replicationHistory");
		cardHistoru.<HtmlAttribute>getAttributeById(
				ObjectId.predefined(HtmlAttribute.class,"jbr.replicationHistory.replicXml")
			).setValue(ReplicationUtils.packageToString(pcg));

		cardHistoru = saveCard(cardHistoru);

		// card link
		card.<CardLinkAttribute>getAttributeById(
				ObjectId.predefined(CardLinkAttribute.class,"jbr.replication.replicHistAttr")
			).addLinkedId(cardHistoru.getId());

		long cardId = getCardId(pcg.getCard());
		if (card.<IntegerAttribute>getAttributeById(CardRelationUtils.REPLIC_CARDID).getValue() == 0) {
			card.<IntegerAttribute>getAttributeById(CardRelationUtils.REPLIC_CARDID).setValue((int) cardId);
		}
		CardLinkAttribute linkToReplicatedDoc = card.getAttributeById(CardRelationUtils.REPLIC_BASEDOC_LNK);
		if (linkToReplicatedDoc.isEmpty()) {
			linkToReplicatedDoc.addLinkedId(cardId);
		}

		return saveCard(card);
	}

	private void fillMainAttributes(ReplicationPackage pcg, Card card, ProcessType type, boolean isNew) throws JAXBException, IOException {
		String server = ReplicationConfiguration.getReplicationNodeConfig().getServerGUID();
		
		if (isNew) {
			card.<StringAttribute>getAttributeById(CardRelationUtils.REPLIC_GUID).setValue(pcg.getCard().getGuid());
			card.<StringAttribute>getAttributeById(CardRelationUtils.REPLIC_OWNER).setValue(server);
		}

		card.<StringAttribute>getAttributeById(CardRelationUtils.REPLIC_ADDRESSEE).setValue(pcg.getAddressee());
		card.<StringAttribute>getAttributeById(CardRelationUtils.REPLIC_SENDER).setValue(pcg.getSender());
		
		switch (type) {
			case SEND:
				card.<DateAttribute>getAttributeById(CardRelationUtils.REPLIC_DATE_SENT).setValue(new Date());
				break;
			case RECEIVE:
				card.<DateAttribute>getAttributeById(CardRelationUtils.REPLIC_DATE_RECEIVE).setValue(new Date());
				break;
		}
	}

	private Card createCard(String template) throws DataException {
		CreateCard createCard = new CreateCard(ObjectId.predefined(Template.class, template));
		Card card = dataService.doAction(createCard);
		return card;
	}

	private Card saveCard(Card card) throws DataException {
		ObjectId cardId = card.getId();
		if (cardId != null) {
			LockObject lo = new LockObject(card);
			dataService.doAction(lo);
		}
		try {
			cardId = dataService.saveObject(card);
		} finally {
			if (cardId != null) {
				UnlockObject uo = new UnlockObject(cardId);
				dataService.doAction(uo);
			}
		}
		return card;
	}
	
	private void doOverwriteCardAttributes(ObjectId cardId, Attribute... attributes) throws DataException
	{	
		if (null == cardId) {
			return;
		}
		dataService.doAction(new LockObject(cardId));
		try {
			final OverwriteCardAttributes writer = new OverwriteCardAttributes();
			final Collection<Attribute> colAttr = Arrays.asList(attributes);
			writer.setCardId(cardId);
			writer.setAttributes(colAttr);

			dataService.doAction(writer);
		} finally {
			dataService.doAction(new UnlockObject(cardId));
		}
	}

	private long getCardId(ReplicationPackage.Card cardFromXML) throws DataException, JAXBException, IOException {
		Long result;
		GetCardIdByUUID byUUID = new GetCardIdByUUID();
		byUUID.setUuid(cardFromXML.getGuid());
		byUUID.setAttrId(CardRelationUtils.REPLICATION_UUID);
		
		ObjectId cardId = dataService.doAction(byUUID);
		if (cardId != null) {
			result = Long.parseLong(cardId.getId().toString());
		} else {
			List<ObjectId> cards = searchCardByKeywords(cardFromXML);
			if (cards.size() != 0) {
				result = Long.parseLong(cards.get(0).getId().toString());
			} else {
				GetReservCardId getReservCardId = new GetReservCardId();
				result = dataService.doAction(getReservCardId);
			}
		}

		return result;
	}

	private List<ObjectId> searchCardByKeywords(ReplicationPackage.Card cardFromXml) throws DataException, JAXBException, IOException {
		StrictSearch search = ReplicationUtils.makeSearchByKeywords(cardFromXml, dataService);
		if (search == null) {
			return new ArrayList<ObjectId>();
		}
		
		List<ObjectId> result = ReplicationUtils.execSearchByKeywords(search, cardFromXml, dataService);
		return result;
	}
	
	private static String getCardDescription(Card card) {
		return card.getId() == null ? "empty id" : card.getId().getId() + ", template = " + card.getTemplate().getId() + ", status = " + card.getState().getId();
	}

}