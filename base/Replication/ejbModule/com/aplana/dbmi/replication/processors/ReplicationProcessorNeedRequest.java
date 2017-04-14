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

import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.replication.packageconfig.ReplicationPackage;
import com.aplana.dbmi.replication.tool.CardRelationUtils;
import com.aplana.dbmi.replication.tool.ReplicationUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.Types;
import java.util.List;

public class ReplicationProcessorNeedRequest extends ReplicationProcessorTemp {
	private static final long serialVersionUID = 1L;

	@Override
	public Object process() throws DataException {
		logger.info("-------Start ReplicationProcessorNeedRequest-------");
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

			if (cards.size() == 0) {
				cards = searchCardByKeywords(replicationPackage.getCard());
				// Устанавливаем в карточку UUID
				if (cards.size() > 0) {
					Card card = service.getById(cards.get(0));
					setGuidCard(replicationPackage,  card);
				}
			}

			if (cards.size() == 0) {
				// новая карточка
				try {
					int intCardId = replicCard.<IntegerAttribute>getAttributeById(CardRelationUtils.REPLIC_CARDID).getValue();
					ReplicationPackage.Card cardFromXml = replicationPackage.getCard();
					String owner = ReplicationUtils.getOwnerGuid(replicationPackage);
					String server = ReplicationConfiguration.getReplicationNodeConfig().getServerGUID();
					ObjectId templateId = new ObjectId(Template.class, cardFromXml.getTemplateId());
					boolean isOriginal = owner.equals(server);
					boolean isIndep = ReplicationConfiguration.getIndependentTemplates().contains(templateId);
					getJdbcTemplate().update("insert into card (card_id, template_id, status_id, is_active) " +
									"select ?, ?, ?, ? " +
									"where not exists (select 1 from card where card_id = ?)",
							new Object[] { intCardId, cardFromXml.getTemplateId(),  
								ObjectId.state("notComplReplic").getId(), (isOriginal || isIndep) ? 1 : 0, intCardId },
							new int[] { Types.NUMERIC, Types.NUMERIC, Types.NUMERIC, Types.NUMERIC });

					CreateCard createCard = new CreateCard(templateId);
					ActionQueryBase query = getQueryFactory().getActionQuery(createCard);
					query.setAction(createCard);
					Card card = getDatabase().executeQuery(getSystemUser(), query);
					card.setId(new ObjectId(Card.class, intCardId));
					
					insertCardAttributes(cardFromXml, card);
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}

			// ReplicationPackage replicationPackage = getPackageFromCard(card);
			//
			// ReplicationPackage requestPackage = new ReplicationPackage();
			// requestPackage.setPackageType(PackageType.REQUEST);
			// requestPackage.setAddressee(replicationPackage.getAddressee());
			// requestPackage.setSender(replicationPackage.getSender());
			// requestPackage.setDateSent(new XMLGregorianCalendarImpl(new
			// GregorianCalendar()));
			// IncompleteCards incompleteCards = new IncompleteCards();
			// incompleteCards.setGuid(replicationPackage.getCard().getGuid());
			//
			// for (Attribute attribute :
			// replicationPackage.getCard().getAttribute()){
			// if (!attribute.getCardLinkValue().isEmpty()){
			// for (String guid : attribute.getCardLinkValue()){
			// if(searchCardByGuid(guid, 0).size() == 0 &&
			// searchIncomplidCards(guid).size() == 0){
			// if (!incompleteCards.getCardGuid().contains(guid)){
			// incompleteCards.getCardGuid().add(guid);
			// }
			// }
			// }
			// }
			// }
			//
			// requestPackage.setIncompleteCards(incompleteCards);
			//
			// ReplicationUtils.packageToFile(requestPackage, null);

			// } catch (DataException ex) {
			// logger.error("Error on execute do process in " +
			// this.getClass().getName(), ex);
			// throw ex;
		} catch (Exception ex) {
			if(logger.isErrorEnabled())
				logger.error("Error on execute do process in " + this.getClass().getName(), ex);
			sendErrorNotification(ex);
			// throw new DataException("Error on execute do process in " +
			// this.getClass().getName(), ex);
		}
		return null;
	}
	
	@Override
	protected void insertCardLinkAttribute(Card card, Attribute attribute, ReplicationPackage.Card.Attribute replAttr)
			throws DataException, ServiceException, JAXBException, IOException {
		for (String guid : replAttr.getCardLinkValue()) {
			List<ObjectId> cardsList = searchCardByGuid(guid, 0);
			if (cardsList.size() > 0) {
				getJdbcTemplate().update("INSERT INTO attribute_value(card_id, attribute_code, number_value,template_id) VALUES (?, ?, ?, ?);",
					new Object[] { card.getId().getId(), attribute.getId().getId(), cardsList.get(0).getId(), card.getTemplate().getId() },
					new int[] { Types.NUMERIC, Types.VARCHAR, Types.NUMERIC, Types.NUMERIC });
			}
		}
	}
}
