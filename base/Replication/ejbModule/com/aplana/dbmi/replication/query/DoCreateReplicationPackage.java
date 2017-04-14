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

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.DownloadFile;
import com.aplana.dbmi.action.Material;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.replication.action.CreateReplicationPackage;
import com.aplana.dbmi.replication.action.GetPerson;
import com.aplana.dbmi.replication.action.LinkResolver;
import com.aplana.dbmi.replication.packageconfig.PackageType;
import com.aplana.dbmi.replication.packageconfig.ReplicationPackage;
import com.aplana.dbmi.replication.packageconfig.ReplicationPackage.Card.Attribute.ListValue;
import com.aplana.dbmi.replication.packageconfig.ReplicationPackage.Card.Attribute.MaterialValue;
import com.aplana.dbmi.replication.packageconfig.ReplicationPackage.Card.Attribute.PersonValue;
import com.aplana.dbmi.replication.packageconfig.ReplicationPackage.IncompleteCards;
import com.aplana.dbmi.replication.processors.ReplicationConfiguration;
import com.aplana.dbmi.replication.task.ReplicationTask;
import com.aplana.dbmi.replication.templateconfig.ReplicationTemplateConfig.Template;
import com.aplana.dbmi.replication.tool.*;
import com.aplana.dbmi.replication.tool.ReplicationCardHandler.ProcessType;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dbmi.service.client.InternalPartLoader;
import com.aplana.dbmi.service.client.MaterialStream;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.query.WriteQuery;
import com.aplana.dbmi.utils.StrUtils;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.*;

public class DoCreateReplicationPackage extends ActionQueryBase implements WriteQuery {
	private static final long serialVersionUID = 1L;

	@Override
	public Object processQuery() throws DataException {
		try {
			// Получение списка карточек, которые кладем в пакет
			List<ObjectId> cardIds = getObjectsToReplicate();
			List<String> addresseeGuids = getAddressee();
			ReplicationActiveProcessInfo.putAllCardForReplication(getPrimaryQuery().getUid(), addresseeGuids, cardIds.toArray(new ObjectId[cardIds.size()]));
			Set<Card> replicatedCards = createReplicateXmls(cardIds, addresseeGuids);
			return replicatedCards;
		} catch (DataException ex) {
			logger.error("Error on execute do process in " + this.getClass().getName(), ex);
			throw ex;
		} catch (Exception ex) {
			logger.error("Error on execute do process in " + this.getClass().getName(), ex);
			throw new DataException("Error on execute do process in " + this.getClass().getName(), ex);
		}
	}

	private Set<Card> createReplicateXmls(List<ObjectId> cardIds, List<String> addresseeGuids)
			throws ServiceException, DataException, IOException, JAXBException {
		Set<Card> allReplicatedCards = new HashSet<Card>();
		for (String addresseeGuid : addresseeGuids) {
			Set<Card> replicatedCards = new HashSet<Card>();
			for (int i = 0; i < cardIds.size(); i++) {
				List<MaterialInfo> materialInfos = new ArrayList<MaterialInfo>();
				ReplicationPackage pcg = new ReplicationPackage();
				CreateReplicationPackage action = getAction();
				pcg.setPackageType(PackageType.CARD);
				pcg.setAddressee(addresseeGuid);
				pcg.setSender(ReplicationConfiguration.getReplicationNodeConfig().getServerGUID());
				pcg.setDateSent(ReplicationUtils.newXMLGregorianCalendar());
				if (action.getReplicationCardGuid() != null) {
					pcg.setIncompleteCards(new IncompleteCards());
					pcg.getIncompleteCards().setGuid(action.getReplicationCardGuid());
				}
				if (action.getPackageType() != null) {
					pcg.setPackageType(action.getPackageType());
				}

				ObjectId cardId = cardIds.get(i);
				ReplicationPackage.Card pcgCard = new ReplicationPackage.Card();
				pcg.setCard(pcgCard);

				ObjectQueryBase query = getQueryFactory().getFetchQuery(Card.class);
				query.setId(cardId);
				Card card = getDatabase().executeQuery(getUser(), query);

				Template templateConfig = ReplicationConfiguration.getTemplateConfig((Long) card.getTemplate().getId());
				if (templateConfig == null) {
					continue;
				}

				// проверка на необходимость репликации
				// если флаг взведен, тогда не выгружаем, а меняем флаг и продолжаем работу с другими
				int replicFlag = card.<IntegerAttribute>getAttributeById(CardRelationUtils.REPLIC_FLAG).getValue();
				if (replicFlag == ReplicationTask.REPLICATION_FLAG_TRUE) {

					getJdbcTemplate().update("insert into attribute_value (card_id, attribute_code, number_value) values(?, ?, ?)",
							new Object[] { card.getId().getId(), CardRelationUtils.REPLIC_FLAG.getId(), ReplicationTask.REPLICATION_FLAG_FALSE },
							new int[] { Types.NUMERIC, Types.VARCHAR, Types.NUMERIC });
					continue;
				}
				DataServiceFacade facade = new DataServiceFacade(getDatabase(), getSystemUser(), getQueryFactory());

				if (action.isUpdateVersion() && cardId.equals(action.getCard().getId())) {
					updateReplicationVersion(card);
				}
				// Проверяем заполненность атрибута REPLICATION_GUID, если не
				// заполнен то формируем его
				// Получаем значение атрибута REPLICATION_GUID
				StringAttribute guidAttr = card.getAttributeById(CardRelationUtils.REPLICATION_UUID);
				if (guidAttr != null && guidAttr.getStringValue() != null && guidAttr.getStringValue().length() > 0) {
					pcgCard.setGuid(guidAttr.getStringValue());
				} else {
					String cardGuid = ReplicationUtils.formUuidAttribute(card, facade, getJdbcTemplate());
					pcgCard.setGuid(cardGuid);
				}

				setReplicatingDocType(card);

				// Заполняем статус, процесс, и кард id
				pcgCard.setCardId((Long) card.getId().getId());
				pcgCard.setStatus((Long) card.getState().getId());
				pcgCard.setTemplateId((Long) card.getTemplate().getId());

				for (DataObject block : card.getAttributes()) {
					if (block instanceof AttributeBlock) {
						for (Attribute attribute : ((AttributeBlock) block).getAttributes()) {
							final boolean isAttributeExclude = templateConfig.getAttributes() != null
									&& templateConfig.getAttributes().getExclude().contains(attribute.getId().getId().toString());
							if (isAttributeExclude || (attribute instanceof PseudoAttribute)) {
								continue;
							}

							ReplicationPackage.Card.Attribute pcgAttr = new ReplicationPackage.Card.Attribute();
							pcgAttr.setCode((String) attribute.getId().getId());

							if (attribute instanceof HtmlAttribute) {
								HtmlAttribute htmlAttribute = (HtmlAttribute) attribute;
								pcgAttr.getHtmlValue().add(htmlAttribute.getStringValue());
							} else if (attribute instanceof StringAttribute) {
								StringAttribute strAttribute = (StringAttribute) attribute;
								pcgAttr.getStringValue().add(strAttribute.getStringValue());
							} else if (attribute instanceof PersonAttribute) {
								PersonAttribute prsAttribute = (PersonAttribute) attribute;
								if (prsAttribute.getValues() == null)
									continue;
								for (Person person : prsAttribute.getValues()) {
									PersonValue pv = new PersonValue();
									pv.setEmail(person.getEmail());
									pv.setLogin(person.getLogin());
									pv.setFullName(person.getFullName());

									GetPerson gp = new GetPerson(person.getLogin(), person.getEmail(), person.getFullName());
									DoGetPerson.PersonWrapper wrapper = facade.doAction(gp);

									//UUID у персоны еще не задан, формируем, пишем в базу и в модель
									if (StrUtils.isStringEmpty(wrapper.getUuid())) {
										String uuid = ReplicationUtils.formUuidAttribute(person, getJdbcTemplate());
										pv.setUuid(uuid);
									} else {
										pv.setUuid(wrapper.getUuid());
									}

									if (person.getCardId() != null) {
										List<String> personCardUuid = CardRelationUtils.resolveLink(person.getCardId(), facade, CardRelationUtils.REPLICATION_UUID.getId());
										//если карточка пользователя уже реплицировалась, то прилагаем в пакет ее UUID
										if (!personCardUuid.isEmpty()) {
											pv.setCard(personCardUuid.get(0));
										} else {
											//если еще не реплицировалась, но не трогаем ее. Она будет реплицироваться только в случае уже ее личного изменения.
											logger.debug("Person's card without UUID. Skipping. Will be replicated only in case changing this card.");

											//Card personCard = facade.getById(person.getCardId());
											//String newGuid = ReplicationUtils.formUuidAttribute(personCard, getJdbcTemplate());
											//pv.setCard(newGuid);
											//cardIds.add(personCard.getId());
											//ReplicationActiveProcessInfo.putCardForReplication(getPrimaryQuery().getUid(), personCard.getId());
										}
									}
									pcgAttr.getPersonValue().add(pv);
								}
							} else if (attribute instanceof IntegerAttribute) {
								IntegerAttribute intAttribute = (IntegerAttribute) attribute;
								pcgAttr.getNumberValue().add(BigInteger.valueOf(intAttribute.getValue()));
							} else if (attribute instanceof DateAttribute) {
								DateAttribute dtAttribute = (DateAttribute) attribute;
								if (dtAttribute.getValue() != null) {
									GregorianCalendar calendar = (GregorianCalendar) GregorianCalendar.getInstance();
									calendar.setTime(dtAttribute.getValue());
									pcgAttr.getDateValue().add(ReplicationUtils.newXMLGregorianCalendar());
								}
							} else if (attribute instanceof CardLinkAttribute) {
								CardLinkAttribute clAttribute = (CardLinkAttribute) attribute;
								// Получение значений идентификаторов
								ObjectId[] clAttrsValue = clAttribute.getIdsArray();
								if (clAttrsValue != null) {
									for (ObjectId childCardId : clAttrsValue) {
										// По кажлому элементу получаем карточку
										if (childCardId != null && childCardId.getId() != null && ((Long) childCardId.getId()) > 0) {
											query.setId(childCardId);
											Card childCard = getDatabase().executeQuery(getUser(), query);
											// Исключаем из пакета репликации ссылки на карточки шаблонов, которые не участвуют в обмене
											if (null != ReplicationConfiguration
													.getTemplateConfig((Long) childCard.getTemplate().getId())) {
												// Получаем значение атрибута
												// REPLICATION_GUID
												guidAttr = childCard.getAttributeById(CardRelationUtils.REPLICATION_UUID);
												if (guidAttr != null && guidAttr.getStringValue() != null && guidAttr.getStringValue().length() > 0) {
													// GUID уже сформирован, в пакет
													// репликации
													// добавляем только GUID
													pcgAttr.getCardLinkValue().add(guidAttr.getStringValue());
												} else {
													// Формируем GUID карточке, а саму
													// карточку
													// добавляем в коллекцию реплицируемых
													// карточек
													String newGuid = ReplicationUtils.formUuidAttribute(childCard, facade, getJdbcTemplate());
													pcgAttr.getCardLinkValue().add(newGuid);
													cardIds.add(childCard.getId());
													ReplicationActiveProcessInfo.putAllCardForReplication(getPrimaryQuery().getUid(), addresseeGuids, childCard.getId());
												}
											} else {
												logger.warn(MessageFormat
														.format("Replication Template config: template ''{0}'' is not configured for " +
																		"child card ''{1}'' from CardLinkAttribute ''{2}'' of main card ''{3} ",
																childCard.getTemplate().getId().toString(), childCardId.getId().toString(),
																clAttribute.getName(), card.getId().getId().toString()));
											}
										}
									}
								}
							} else if (attribute instanceof MaterialAttribute) {
								MaterialAttribute mtAttribute = (MaterialAttribute) attribute;
								if (mtAttribute.getMaterialName() != null && mtAttribute.getName().length() > 0) {
									MaterialStream materialStream = getMaterial(cardId, 0);
									MaterialValue mv = new MaterialValue();
									pcgAttr.getMaterialValue().add(mv);

									mv.setName(mtAttribute.getMaterialName());
									mv.setType(BigInteger.valueOf(mtAttribute.getMaterialType()));
									mv.setVersion(BigInteger.valueOf(mtAttribute.getMaterialVersion()));

									String fileName = UUID.randomUUID().toString();

									MaterialInfo mi = new MaterialInfo();
									mi.setMaterialStream(materialStream);
									mi.setFileName(fileName);

									materialInfos.add(mi);

									mv.setFile(fileName);
								}
							} else if (attribute instanceof ListAttribute) {
								ListAttribute listAttribute = (ListAttribute) attribute;
								if (listAttribute.getValue() != null) {
									long listValueId = (Long) listAttribute.getValue().getId().getId();
									ListValue lv = new ListValue();
									lv.setId(listValueId);
									pcgAttr.getListValue().add(lv);
								}
							} else if (attribute instanceof TreeAttribute) {
								TreeAttribute treeAttribute = (TreeAttribute) attribute;
								if (treeAttribute.getValues() != null) {
									for (Object obj : treeAttribute.getValues()) {
										Long listId = ObjectIdUtils.getIdLongFrom(obj);
										ListValue lv = new ListValue();
										lv.setId(listId);
										if (listId == 0l) {
											lv.setAnotherValue(treeAttribute.getAnotherValue().getValue());
										}
										pcgAttr.getListValue().add(lv);
									}
								}
							}
							final boolean isValueNotEmpty = pcgAttr.getCardLinkValue().size() > 0 || pcgAttr.getDateValue().size() > 0
									|| pcgAttr.getMaterialValue().size() > 0 || pcgAttr.getNumberValue().size() > 0
									|| pcgAttr.getPersonValue().size() > 0 || pcgAttr.getStringValue().size() > 0
									|| pcgAttr.getListValue().size() > 0 || pcgAttr.getHtmlValue().size() > 0;
							if (isValueNotEmpty) {
								pcgCard.getAttribute().add(pcgAttr);
							}
						}
					}
				}
				if (ReplicationActiveProcessInfo.isCardReplicationActive(getPrimaryQuery().getUid(), cardId, addresseeGuid)) {
					ReplicationUtils.packageToFile(pcg, materialInfos);

					if (pcg.getPackageType().equals(PackageType.CARD) || pcg.getPackageType().equals(PackageType.RESPONSE)) {
						ReplicationCardHandler replicCardHandler = new ReplicationCardHandler(getQueryFactory(), getDatabase(), getUser());
						Card replicCard = replicCardHandler.processPackage(pcg, ProcessType.SEND);
						replicCardHandler.setReplicCardRootLink(card, replicCard);

						if (!replicCard.getState().equals(ObjectId.state("sent"))) {
							// новый статус - Отправлен
							ChangeState changeState = new ChangeState();
							WorkflowMove workflowMove = new WorkflowMove();
							if (replicCard.getState().equals(ObjectId.state("draft"))) {
								workflowMove.setId("jbr.replication.draftToSent");
							} else if (replicCard.getState().equals(ObjectId.state("jbr.state_service.notificationAccepted"))) {
								workflowMove.setId("jbr.replication.acceptedToSent");
							} else if (replicCard.getState().equals(ObjectId.state("jbr.replication.error"))) {
								workflowMove.setId("jbr.replication.errorToSent");
							}
							if (workflowMove.getId() != null) {
								changeState.setCard(replicCard);
								changeState.setWorkflowMove(workflowMove);
								ActionQueryBase actionQuery = getQueryFactory().getActionQuery(changeState);
								actionQuery.setAction(changeState);
								getDatabase().executeQuery(getUser(), actionQuery);
							}
						}
					}

					ReplicationActiveProcessInfo.markCardReplicated(getPrimaryQuery().getUid(), cardId, addresseeGuid);
					replicatedCards.add(card);
				}
			}
			allReplicatedCards.addAll(replicatedCards);
		}
		return allReplicatedCards;
	}

	private void setReplicatingDocType(Card card) {
		ObjectId replicatingDoctypeSrcDocValueId = ObjectId.predefined(ReferenceValue.class,
				"jbr.replication.sourceDocument");

		ListAttribute replicatingDocTypeAttr = card.getAttributeById(CardRelationUtils.REPLIC_DOC_TYPE);
		if (replicatingDocTypeAttr == null) {
			return;
		}
		if (replicatingDocTypeAttr.getValue() == null) {
			getJdbcTemplate().update(
					"INSERT INTO attribute_value(card_id, attribute_code, value_id) VALUES (?, ?, ?);",
					new Object[] { card.getId().getId(), CardRelationUtils.REPLIC_DOC_TYPE.getId(),
							replicatingDoctypeSrcDocValueId.getId() },
					new int[] { Types.NUMERIC, Types.VARCHAR, Types.NUMERIC });
			replicatingDocTypeAttr.setValue((ReferenceValue) DataObject.createFromId(replicatingDoctypeSrcDocValueId));
		}
	}

	private void updateReplicationVersion(Card card) {
		int replicFlag = card.<IntegerAttribute>getAttributeById(CardRelationUtils.REPLIC_FLAG).getValue();
		if (replicFlag == ReplicationTask.REPLICATION_FLAG_FALSE) {
			String guid = UUID.randomUUID().toString();
			getJdbcTemplate().update("delete from attribute_value where card_id = ? and attribute_code = ?",
					new Object[] { card.getId().getId(), CardRelationUtils.REPLIC_VERSION.getId() }, 
					new int[] { Types.NUMERIC, Types.VARCHAR });
			getJdbcTemplate().update("insert into attribute_value (card_id, attribute_code, string_value) values(?, ?, ?)",
					new Object[] { card.getId().getId(), CardRelationUtils.REPLIC_VERSION.getId(), guid }, 
					new int[] { Types.NUMERIC, Types.VARCHAR, Types.VARCHAR });

			card.<StringAttribute>getAttributeById(CardRelationUtils.REPLIC_VERSION).setValue(guid);
		} else {
			getJdbcTemplate().update("insert into attribute_value (card_id, attribute_code, number_value) values(?, ?, ?)",
					new Object[] { card.getId().getId(), CardRelationUtils.REPLIC_FLAG.getId(), ReplicationTask.REPLICATION_FLAG_FALSE },
					new int[] { Types.NUMERIC, Types.VARCHAR, Types.NUMERIC });

			card.<IntegerAttribute>getAttributeById(CardRelationUtils.REPLIC_FLAG).setValue(ReplicationTask.REPLICATION_FLAG_FALSE);
		}
	}

	private List<String> getAddressee() throws DataException, JAXBException, IOException {
		List<String> result = new ArrayList<String>();
		CreateReplicationPackage action = getAction();
		String addressee = action.getAddressee();
		if (addressee == null) {
			Card card = action.getCard();
			ReplicationInfo replicationInfo = new ReplicationInfo(card, new DataServiceFacade(getDatabase(), getUser(), getQueryFactory()));
			result.addAll(replicationInfo.getAddressee());
		} else {
			result.add(addressee);
		}
		//Удаляем адресата для исключения отправки самому себе
		if (result.contains(ReplicationConfiguration.getReplicationNodeConfig().getServerGUID())){
			result.remove(ReplicationConfiguration.getReplicationNodeConfig().getServerGUID());
		}
		// Однако, не удаляем адресатов-организации, чтобы изменения попадали из одной организации в другую
		// в рамках одной системы. Это еще нужно все проверить в рамках тестирования
		// TODO: пока что не даем возможность отправлять самому себе
		if (ReplicationConfiguration.getReplicationNodeConfig().getOrganizations() != null) {
			for (String orgUid : ReplicationConfiguration.getReplicationNodeConfig().getOrganizations().getGUID()) {
				result.remove(orgUid);
			}
		}
		return result;
	}

	/**
	 * Получение идентификаторов карточек, которые так же требуется
	 * реплицировать
	 *
	 * @return list of objects to replicate
	 * @throws JAXBException
	 * @throws IOException
	 * @throws DataException
	 */
	private List<ObjectId> getObjectsToReplicate() throws JAXBException, IOException, DataException {
		List<ObjectId> result = new ArrayList<ObjectId>();
		CreateReplicationPackage action = getAction();
		Card card = action.getCard();
		result.add(card.getId());
		Template templateConfig = ReplicationConfiguration.getTemplateConfig((Long) card.getTemplate().getId());
		if (templateConfig != null && action.isUpdateVersion()) {
			LinkResolver<ObjectId> resolver = new LinkResolver<ObjectId>();
			ActionQueryBase actionQuery = getQueryFactory().getActionQuery(LinkResolver.class);
			actionQuery.setAction(resolver);

			if (templateConfig.getChildCards() != null && templateConfig.getChildCards().getLinkAttribute() != null) {
				for (String linkAttribute : templateConfig.getChildCards().getLinkAttribute()) {
					resolver.setCardId(card.getId());
					resolver.setLink(linkAttribute);
					List<ObjectId> cardIds = getDatabase().executeQuery(getUser(), actionQuery);
				cardIdsLoop:
					for (ObjectId cardId : cardIds) {
						//Загружаем только те, которые не были раньше загружены
						// (N.Zhegalin) Речь идет о выгрузке. Смысл этого я вижу в следующем:
						// сработала выгрузка для некой карточки, привязанной к этой с помощью Backlink
						// Допустим, сработала репликация для Отчета, но документ еще не отсылался.
						ObjectQueryBase query = getQueryFactory().getFetchQuery(Card.class);
						query.setId(cardId);
						Card childCard = getDatabase().executeQuery(getUser(), query);
						// Получаем значение атрибута
						// REPLICATION_GUID
						StringAttribute guidAttr = childCard.getAttributeById(CardRelationUtils.REPLICATION_UUID);
						if (guidAttr == null || guidAttr.getStringValue() == null || guidAttr.getStringValue().length() == 0) {
							result.add(cardId);
						} else {
							ReplicationCardHandler replicHandler = new ReplicationCardHandler(getQueryFactory(), getDatabase(), getUser());
							ObjectId testAttributeId = CardRelationUtils.REPLIC_CARDID;
							List<Card> replicationCards = replicHandler.getActiveReplicationCards(childCard, testAttributeId);
							for (Card replicCard : replicationCards) {
								IntegerAttribute testAttribute = replicCard.getAttributeById(testAttributeId);
								if (testAttribute == null || testAttribute.getValue() == 0) {
									result.add(cardId);
									continue cardIdsLoop;
								}
							}
						}
					}
				}
			}
		}
		return result;
	}

	public MaterialStream getMaterial(ObjectId cardId, int versionId) throws DataException {
		DownloadFile download = new DownloadFile();
		download.setCardId(cardId);
		ActionQueryBase query = getQueryFactory().getActionQuery(DownloadFile.class);
		query.setAction(download);
		Material material = getDatabase().executeQuery(getUser(), query);
		if (material == null)
			return null;
		return new MaterialStream(material.getLength(), new InternalPartLoader(getQueryFactory(), getDatabase(), getUser(), cardId, versionId,
				material.getUrl()));
	}
}
