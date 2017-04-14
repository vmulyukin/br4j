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

import com.aplana.dbmi.action.*;
import com.aplana.dbmi.jbr.processors.ProcessCard;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.replication.query.DoGetPerson.PersonWrapper;
import com.aplana.dbmi.replication.action.GetPerson;
import com.aplana.dbmi.replication.action.SendErrorNotification;
import com.aplana.dbmi.replication.packageconfig.ReplicationPackage;
import com.aplana.dbmi.replication.packageconfig.ReplicationPackage.Card.Attribute.ListValue;
import com.aplana.dbmi.replication.packageconfig.ReplicationPackage.Card.Attribute.MaterialValue;
import com.aplana.dbmi.replication.packageconfig.ReplicationPackage.Card.Attribute.PersonValue;
import com.aplana.dbmi.replication.task.ReplicationTask;
import com.aplana.dbmi.replication.tool.CardRelationUtils;
import com.aplana.dbmi.replication.tool.ReplicationCardHandler;
import com.aplana.dbmi.replication.tool.ReplicationUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.utils.StrUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataAccessException;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.*;

public abstract class ReplicationProcessorTemp extends ProcessCard {
	private static final long serialVersionUID = 1L;

	public enum UpdateType {
		NEW,
		NEW_LOCAL,
		UPDATE
	}

	// поиск карточек по Guid
	protected List<ObjectId> searchCardByGuid(String guid, long templateId) throws DataException, ServiceException {
		StrictSearch search = new StrictSearch();
		if (templateId != 0) {
			search.setTemplates(Collections.singleton(Template.createFromId(new ObjectId(Template.class, templateId))));
		}
		//search.setByAttributes(true);
		search.addStringAttribute(CardRelationUtils.REPLICATION_UUID, guid);
		ActionQueryBase query = getQueryFactory().getActionQuery(search);
		query.setAction(search);
		List<ObjectId> cards = getDatabase().executeQuery(getSystemUser(), query);

		return cards;
	}

	protected List<ObjectId> searchCardByKeywords(ReplicationPackage.Card cardFromXml)
			throws DataException, JAXBException, IOException {
		DataServiceFacade dataService = new DataServiceFacade(getDatabase(), getSystemUser(), getQueryFactory());
		StrictSearch search = ReplicationUtils.makeSearchByKeywords(cardFromXml, dataService);
		if (search == null) {
			return new ArrayList<ObjectId>();
		}

		List<ObjectId> result = ReplicationUtils.execSearchByKeywords(search, cardFromXml, dataService);
		return result;
	}

	//создание новой карточки из xml-карточки
	protected Card createCard(ReplicationPackage rpg, long cardId, UpdateType type)
			throws DataException, ServiceException, JAXBException, IOException {
		ReplicationPackage.Card cardFromXml = rpg.getCard();
		CreateCard createCard = new CreateCard(new ObjectId(Template.class, cardFromXml.getTemplateId()));
		ActionQueryBase query = getQueryFactory().getActionQuery(createCard);
		query.setAction(createCard);
		Card card = getDatabase().executeQuery(getSystemUser(), query);
		//Добавляем зарезервированный идентификатор
		card.setReserveId(cardId);
		return changeCard(rpg, card, type);
	}

	// изменение карточки в базе
	protected Card changeCard(ReplicationPackage rpg, Card card, UpdateType type)
			throws DataException, ServiceException, JAXBException, IOException {
		ReplicationPackage.Card cardFromXml = rpg.getCard();
		List<MaterialValue> materials = new ArrayList<MaterialValue>();
		long cardId;

		try {
			if (type == UpdateType.NEW || type == UpdateType.NEW_LOCAL) {
				String owner = ReplicationUtils.getOwnerGuid(rpg);
				String server = ReplicationConfiguration.getReplicationNodeConfig().getServerGUID();
				ObjectId templateId = new ObjectId(Template.class, cardFromXml.getTemplateId());
				boolean isOriginal = owner.equals(server);
				boolean isIndep = ReplicationConfiguration.getIndependentTemplates().contains(templateId);

				getJdbcTemplate().update("INSERT INTO card( card_id, template_id, status_id, is_active) VALUES (?, ?, ?, ?)",
						new Object[] {
								card.getReserveId(),
								card.getTemplate().getId(),
								ObjectId.state("notComplReplic").getId(),
								(isOriginal || isIndep || type == UpdateType.NEW_LOCAL) ? 1 : 0},
						new int[] {Types.NUMERIC, Types.NUMERIC, Types.NUMERIC, Types.NUMERIC});
				cardId = card.getReserveId();
			} else {
				getAccessManager().cleanAccessListByCardAndSourceAttrs(card.getId());
				cleanAttributesInDatabase(card, cardFromXml);
				cardId = (Long) card.getId().getId();
			}
			card.setId(new ObjectId(Card.class, cardId));

			insertCardAttributes(cardFromXml, card);

			processMaterialAttributes(cardFromXml, card, materials);

			//lock, save, unlock of card was here

			setCardState(card, rpg.getCard().getStatus());
			//setCardModifDate(card, getChangeDateFromPackage(rpg));
			//repeatLoad(rpg);
			getAccessManager().updateAccessToCard(card.getId());

			return card;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	protected void insertCardAttributes(ReplicationPackage.Card cardFromXml, Card card)
			throws DataAccessException, DataException, ServiceException, JAXBException, IOException {
		long cardId = (Long) card.getId().getId();
		for (DataObject block : card.getAttributes()) {
			if (block instanceof AttributeBlock) {
				for (Attribute attribute : ((AttributeBlock) block).getAttributes()) {
					ReplicationPackage.Card.Attribute replAttr
							= ReplicationUtils.getReplicationAttribute(cardFromXml, (String) attribute.getId().getId());

					if (replAttr == null) {
						continue;
					}
					if (attribute instanceof HtmlAttribute) {
						if (!replAttr.getHtmlValue().isEmpty()) {
							getJdbcTemplate().update("INSERT INTO attribute_value(card_id, attribute_code, long_binary_value, template_id) VALUES (?, ?, ?, ?);",
									new Object[] {cardId, attribute.getId().getId(), replAttr.getHtmlValue().get(0).getBytes("UTF8"), card.getTemplate().getId()},
									new int[] {Types.NUMERIC, Types.VARCHAR, Types.BINARY, Types.NUMERIC});
						}
					} else if (attribute instanceof StringAttribute) {
						if (!replAttr.getStringValue().isEmpty()) {
							getJdbcTemplate().update("INSERT INTO attribute_value(card_id, attribute_code, string_value,template_id) VALUES (?, ?, ?, ?);",
									new Object[] {cardId, attribute.getId().getId(), replAttr.getStringValue().get(0), card.getTemplate().getId()},
									new int[] {Types.NUMERIC, Types.VARCHAR, Types.VARCHAR, Types.NUMERIC});
						}
					} else if (attribute instanceof PersonAttribute) {
						insertPersonAttribute(cardFromXml, card, attribute, replAttr);
					} else if (attribute instanceof IntegerAttribute) {
						if (!replAttr.getNumberValue().isEmpty()) {
							getJdbcTemplate().update("INSERT INTO attribute_value(card_id, attribute_code, number_value,template_id) VALUES (?, ?, ?, ?);",
									new Object[] {cardId, attribute.getId().getId(), replAttr.getNumberValue().get(0), card.getTemplate().getId()},
									new int[] {Types.NUMERIC, Types.VARCHAR, Types.NUMERIC, Types.NUMERIC});
						}
					} else if (attribute instanceof DateAttribute) {
						if (!replAttr.getDateValue().isEmpty()) {
							Date dt = replAttr.getDateValue().get(0).toGregorianCalendar().getTime();
							//обнуляем таймзону, чтоб время не сдвигалось
							Calendar calendar = Calendar.getInstance();
							calendar.setTimeInMillis(dt.getTime());
							dt = new Timestamp(calendar.getTimeInMillis() - calendar.get(Calendar.ZONE_OFFSET) - calendar.get(Calendar.DST_OFFSET));
							getJdbcTemplate().update("INSERT INTO attribute_value(card_id, attribute_code, date_value,template_id) VALUES (?, ?, ?, ?);",
									new Object[] {cardId, attribute.getId().getId(), dt, card.getTemplate().getId()},
									new int[] {Types.NUMERIC, Types.VARCHAR, Types.TIMESTAMP, Types.NUMERIC});
						}
					} else if (attribute instanceof CardLinkAttribute) {
						insertCardLinkAttribute(card, attribute, replAttr);
					} else if (attribute instanceof ListAttribute || attribute instanceof TreeAttribute) {
						if (!replAttr.getListValue().isEmpty()) {
							for (ListValue listVal : replAttr.getListValue()) {
								getJdbcTemplate().update("INSERT INTO attribute_value(card_id, attribute_code, value_id, another_value, template_id) VALUES (?, ?, ?, ?, ?);",
										new Object[] {cardId, attribute.getId().getId(), listVal.getId(), listVal.getAnotherValue(), card.getTemplate().getId()},
										new int[] {Types.NUMERIC, Types.VARCHAR, Types.NUMERIC, Types.VARCHAR, Types.NUMERIC});
							}
						}
					}
				}
			}
		}
	}

	protected void processMaterialAttributes(ReplicationPackage.Card cardFromXml, Card card, List<MaterialValue> materials)
			throws DataException {
		for (DataObject block : card.getAttributes()) {
			if (block instanceof AttributeBlock) {
				for (Attribute attribute : ((AttributeBlock) block).getAttributes()) {
					ReplicationPackage.Card.Attribute replAttr
							= ReplicationUtils.getReplicationAttribute(cardFromXml, (String) attribute.getId().getId());

					if (replAttr == null) {
						continue;
					}
					if (attribute instanceof MaterialAttribute) {
						for (MaterialValue materialValue : replAttr.getMaterialValue()) {
							if (materialValue.getName() != null || materialValue.getFile() != null) {
								materials.add(materialValue);
							}
						}
					}
				}
			}
		}

		DataServiceFacade facade = new DataServiceFacade(getDatabase(), getSystemUser(), getQueryFactory());
		for (MaterialValue materialValue : materials) {
			// ищем ранее загруженный материал
			Search search = new Search();
			search.setTemplates(Collections.singleton(DataObject.createFromId(Template.class, "jbr.file")));
			search.setByAttributes(true);
			search.addStringAttribute(ObjectId.predefined(StringAttribute.class, "jbr.materialName"), materialValue.getFile());
			SearchResult sr = facade.doAction(search);

			if (sr.getCards().size() != 0) {
				// Для отмены выгрузки репликации
				getJdbcTemplate().update("insert into attribute_value (card_id, attribute_code, number_value) values(?, ?, ?)",
						new Object[] {card.getId().getId(), CardRelationUtils.REPLIC_FLAG.getId(), ReplicationTask.REPLICATION_FLAG_TRUE},
						new int[] {Types.NUMERIC, Types.VARCHAR, Types.NUMERIC});

				Card file = sr.getCards().get(0);

				// получаем материал
				DownloadFile download = new DownloadFile();
				download.setCardId(file.getId());
				Material material = facade.doAction(download);

				// грузим материал в карточку
				UploadFile uploadFile = new UploadFile();
				uploadFile.setCardId(card.getId());
				uploadFile.setFileName(materialValue.getName());
				uploadFile.setData(material.getData());
				facade.doAction(uploadFile);
			}
		}
	}

	protected void insertCardLinkAttribute(Card card, Attribute attribute, ReplicationPackage.Card.Attribute replAttr)
			throws DataException, ServiceException, JAXBException, IOException {
		for (String guid : replAttr.getCardLinkValue()) {
			List<ObjectId> cards = searchCardByGuid(guid, 0);
			ObjectId linkedCardId = cards.get(0);
			getJdbcTemplate().update("INSERT INTO attribute_value(card_id, attribute_code, number_value,template_id) VALUES (?, ?, ?, ?);",
					new Object[] {card.getId().getId(), attribute.getId().getId(), linkedCardId.getId(), card.getTemplate().getId()},
					new int[] {Types.NUMERIC, Types.VARCHAR, Types.NUMERIC, Types.NUMERIC});

			ObjectQueryBase objectQuery = getQueryFactory().getFetchQuery(Card.class);
			objectQuery.setId(linkedCardId);
			Card linkedCard = getDatabase().executeQuery(getUser(), objectQuery);
			List<ObjectId> dictionaryTemplates = ReplicationConfiguration.getIndependentTemplates();

			if (!dictionaryTemplates.contains(linkedCard.getTemplate())) {
				StringAttribute guidAttr = linkedCard.getAttributeById(CardRelationUtils.REPLICATION_UUID);
				if (guidAttr != null && guidAttr.getStringValue() != null && guidAttr.getStringValue().length() > 0) {
					Search search = new Search();
					search.setTemplates(Collections.singleton(DataObject.createFromId(Template.class, "jbr.replication")));
					search.setByAttributes(true);
					search.addStringAttribute(getReplicationGuidAttr(), guidAttr.getStringValue());

					ActionQueryBase query = getQueryFactory().getActionQuery(search);
					query.setAction(search);
					SearchResult sr = getDatabase().executeQuery(getUser(), query);

					if (sr.getCards().size() == 1) {
						objectQuery.setId(sr.getCards().get(0).getId());
						Card replicCard = getDatabase().executeQuery(getUser(), objectQuery);
						ReplicationCardHandler replicCardHandler = new ReplicationCardHandler(getQueryFactory(), getDatabase(), getUser());
						replicCardHandler.setReplicCardRootLink(linkedCard, replicCard);
					}
				}
			}
		}
	}

	protected void insertPersonAttribute(ReplicationPackage.Card cardFromXml, Card card, Attribute attribute, ReplicationPackage.Card.Attribute replAttr)
			throws DataException, ServiceException, JAXBException, IOException {
		for (PersonValue pv : replAttr.getPersonValue()) {
			if (pv.getLogin() != null) {
				String login = StringUtils.trim(pv.getLogin());
				String email = StringUtils.trim(pv.getEmail());
				String fname = StringUtils.trim(pv.getFullName());
				String uuid = pv.getUuid();
				GetPerson getPerson = new GetPerson(login, email, fname, uuid);
				ActionQueryBase query = getQueryFactory().getActionQuery(getPerson);
				query.setAction(getPerson);
				PersonWrapper wrap = getDatabase().executeQuery(getSystemUser(), query);
				Person person = wrap != null ? wrap.getPerson() : null;

				ObjectId linkedCardId = findPersonCard(cardFromXml, card, pv);

				//создаем новую персону
				if (person == null) {
					long personId = getJdbcTemplate().queryForLong("SELECT nextval('seq_person_id')");
					getJdbcTemplate().update(
							"INSERT INTO person(person_id, person_login, full_name, email, card_id, replication_uuid, is_active) " +
									"VALUES (?, ?, ?, ?, ?, ?, 1);",
							new Object[] {personId, login + "_repl", fname, email, linkedCardId != null ? linkedCardId.getId() : null, uuid},
							new int[] {Types.NUMERIC, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.NUMERIC, Types.VARCHAR});
					person = new Person();
					person.setId(personId);
				} else {
					//обновляем существующую
					if (StrUtils.isStringEmpty(wrap.getUuid())) {
						//первая репликация, вставляем uuid в таблицу person
						getJdbcTemplate().update(
								"UPDATE person SET person_login=?, full_name=?, email=?, card_id=coalesce(?,card_id), replication_uuid=? " +
										"WHERE person_id = ?;",
								new Object[] {login, fname, email, linkedCardId != null ? linkedCardId.getId() : null, uuid, person.getId().getId()},
								new int[] {Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.NUMERIC, Types.VARCHAR, Types.NUMERIC});
					} else {
						//уже реплицировалась (т.к. есть uuid)
						//TODO пока ничего не делаем, в этом случае внутренности карточки могут меняться (например могут быть разные логины)
						//но репликация все равно будет работать т.к. карточки уже связаны по uuid

						//пока обновляем таким персонам только card_id (случай для новых созданных персон после синхронизации,
						//которые приходят на первую репликацию без card_id)
						if (wrap.getPerson().getCardId() == null) {
							getJdbcTemplate().update(
									"UPDATE person SET card_id=coalesce(?,card_id) WHERE person_id = ?;",
									new Object[] {linkedCardId != null ? linkedCardId.getId() : null, person.getId().getId()},
									new int[] {Types.NUMERIC, Types.NUMERIC});
						}
					}
				}

				getJdbcTemplate().update("INSERT INTO attribute_value(card_id, attribute_code, number_value,template_id) VALUES (?, ?, ?, ?);",
						new Object[] {card.getId().getId(), attribute.getId().getId(), person.getId().getId(), card.getTemplate().getId()},
						new int[] {Types.NUMERIC, Types.VARCHAR, Types.NUMERIC, Types.NUMERIC});
			}
		}
	}

	protected ObjectId findPersonCard(ReplicationPackage.Card cardFromXml, Card card, PersonValue pv) throws DataException, ServiceException {
		ObjectId linkedCardId = null;
		if (!StrUtils.isStringEmpty(pv.getCard())) {
			if (cardFromXml.getGuid().equals(pv.getCard())) {
				linkedCardId = card.getId();
			} else {
				StrictSearch search = new StrictSearch();
				search.setTemplates(Collections.singleton(Template.createFromId(new ObjectId(Template.class, 10))));
				search.addStringAttribute(CardRelationUtils.REPLICATION_UUID, pv.getCard());
				ActionQueryBase query = getQueryFactory().getActionQuery(search);
				query.setAction(search);
				List<ObjectId> cards = getDatabase().executeQuery(getSystemUser(), query);
				if (cards.size() > 0) {
					linkedCardId = cards.get(0);
				}
			}
		}
		return linkedCardId;
	}

	// Временное решение для работы наследника
	protected void cleanAttributesInDatabase(Card card, ReplicationPackage.Card cardFromXml) throws DataAccessException {
		for (ReplicationPackage.Card.Attribute xmlAttribute : cardFromXml.getAttribute()) {
			getJdbcTemplate().update("DELETE from attribute_value where card_id = ? and attribute_code = ?",
					new Object[] {card.getId().getId(), xmlAttribute.getCode()},
					new int[] {Types.NUMERIC, Types.VARCHAR});
		}
	}

	protected void sendErrorNotification(Exception ex) throws DataException {
		Card card = getCard();
		ObjectQueryBase objectQuery = getQueryFactory().getFetchQuery(Card.class);
		objectQuery.setId(card.getId());
		card = getDatabase().executeQuery(getSystemUser(), objectQuery);

		SendErrorNotification send = new SendErrorNotification();
		send.setCard(card);
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		send.setMessage(sw.toString());
		String reason = ex.getMessage();
		send.setReason(reason != null ? reason : ex.getClass().getSimpleName());

		ActionQueryBase query = getQueryFactory().getActionQuery(send);
		query.setAction(send);
		getDatabase().executeQuery(getSystemUser(), query);

		//перевод пакета репликации в статус "Error"
		ActionQueryBase getWorkflowQuery = getQueryFactory().getActionQuery(GetWorkflowMovesFromTargetState.class);
		GetWorkflowMovesFromTargetState getWorkflowMovesAction = new GetWorkflowMovesFromTargetState();
		getWorkflowMovesAction.setCard(card);
		getWorkflowMovesAction.setToStateId(ObjectId.state("jbr.replication.error"));
		getWorkflowQuery.setAction(getWorkflowMovesAction);
		List<Long> moveIds = getDatabase().executeQuery(getSystemUser(), getWorkflowQuery);

		//Получаем переход
		if (moveIds.size() != 0) {
			ObjectQueryBase wfMoveQuery = getQueryFactory().getFetchQuery(WorkflowMove.class);
			wfMoveQuery.setId(new ObjectId(CardState.class, moveIds.get(0).longValue()));
			WorkflowMove wfMove = getDatabase().executeQuery(getSystemUser(), wfMoveQuery);

			ChangeState changeState = new ChangeState();
			changeState.setCard(card);
			changeState.setWorkflowMove(wfMove);
			ActionQueryBase actionQuery = getQueryFactory().getActionQuery(changeState);
			actionQuery.setAction(changeState);
			getDatabase().executeQuery(getSystemUser(), actionQuery);
		}
	}

	protected void setCardState(Card card, Long cardState) {
		getJdbcTemplate().update("UPDATE card set status_id = ? where card_id = ?",
				new Object[] {cardState, card.getId().getId()},
				new int[] {Types.NUMERIC, Types.NUMERIC});
	}

	protected Card setGuidCard(ReplicationPackage pkg, Card card) throws DataException {
		card.<StringAttribute>getAttributeById(CardRelationUtils.REPLICATION_UUID).setValue(pkg.getCard().getGuid());
		card.<StringAttribute>getAttributeById(CardRelationUtils.REPLIC_OWNER).setValue(pkg.getAddressee());
		getJdbcTemplate().update("insert into attribute_value (card_id, attribute_code, string_value) values(?, ?, ?)",
				new Object[] {card.getId().getId(), CardRelationUtils.REPLICATION_UUID.getId(), pkg.getCard().getGuid()},
				new int[] {Types.NUMERIC, Types.VARCHAR, Types.VARCHAR});

		getJdbcTemplate().update("insert into attribute_value (card_id, attribute_code, string_value) values(?, ?, ?)",
				new Object[] {card.getId().getId(), CardRelationUtils.REPLIC_OWNER.getId(), pkg.getAddressee()},
				new int[] {Types.NUMERIC, Types.VARCHAR, Types.VARCHAR});
		return card;
	}

	protected void changeCardStatus(Card card, String key) throws DataException {
		ChangeState changeState = new ChangeState();
		WorkflowMove workflowMove = new WorkflowMove();
		workflowMove.setId(ObjectId.workflowMove(key));
		changeState.setCard(card);
		changeState.setWorkflowMove(workflowMove);
		ActionQueryBase actionQuery = getQueryFactory().getActionQuery(changeState);
		actionQuery.setAction(changeState);
		getDatabase().executeQuery(getSystemUser(), actionQuery);
	}

	protected ObjectId getReplicationGuidAttr() {
		return CardRelationUtils.REPLIC_GUID;
	}
}
