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

import com.aplana.dbmi.action.StrictSearch;
import com.aplana.dbmi.action.file.ActionPerformer;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.replication.query.DoGetPerson.PersonWrapper;
import com.aplana.dbmi.replication.action.GetPerson;
import com.aplana.dbmi.replication.action.GetReplicationHistoryForCard;
import com.aplana.dbmi.replication.packageconfig.ReplicationPackage;
import com.aplana.dbmi.replication.packageconfig.ReplicationPackage.Card.Attribute.PersonValue;
import com.aplana.dbmi.replication.processors.ReplicationConfiguration;
import com.aplana.dbmi.replication.templateconfig.ReplicationTemplateConfig;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dbmi.service.client.MaterialStream;
import com.aplana.dbmi.utils.StrUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.*;
import java.math.BigInteger;
import java.sql.Types;
import java.util.*;

public class ReplicationUtils {
	private final static Log logger = LogFactory.getLog(ReplicationUtils.class);
	static JAXBContext context;
	static DatatypeFactory dataXmlFactory;
	
	static {
		try {
			context = JAXBContext.newInstance("com.aplana.dbmi.replication.packageconfig", ReplicationUtils.class.getClassLoader());
			dataXmlFactory = DatatypeFactory.newInstance();
		} catch (JAXBException e) {
			logger.error("Can't create JAXB context", e);
			throw new RuntimeException("Can't create JAXB context", e);
		} catch (DatatypeConfigurationException e) {
			logger.error("Can't initialize DatatypeFactory", e);
		}
	}

	public static XMLGregorianCalendar newXMLGregorianCalendar() {
		return dataXmlFactory.newXMLGregorianCalendar(new GregorianCalendar());
	}
	
	/**
	 * Создание XML файла из пакета репликации.
	 *
	 * @param rpg Пакет репликации
	 * @throws JAXBException
	 * @throws IOException
	 */
	public static void packageToFile(ReplicationPackage rpg) throws JAXBException, IOException{
		packageToFile(rpg, null);
	}
	
	/**
	 * Создание XML файла из пакета репликации.
	 *
	 * @param rpg Пакет репликации
	 * @param materialInfos Дополнительные материалы (может быть null)
	 * @throws JAXBException
	 * @throws IOException
	 */
	public static void packageToFile(ReplicationPackage rpg, List<MaterialInfo> materialInfos) throws JAXBException, IOException{
		UUID folderName = UUID.randomUUID();
		Date now = new Date();
		final File folder = new File(ReplicationConfiguration.getReplicationNodeConfig().getOutgoingFolder(), now.getTime() + "-" + folderName.toString());
		folder.mkdirs();

		final File folderLockFile = new File(folder, "folder.lock");
		folderLockFile.createNewFile();

		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.marshal(rpg, new File(folder, "replication-package.xml"));

		if (materialInfos != null){
			for (MaterialInfo mi : materialInfos){

				byte[] buffer = new byte[1024];
				int read;

				File file = new File(folder, mi.getFileName());
				FileOutputStream os = new FileOutputStream(file);
				file.createNewFile();
				MaterialStream materialStream = mi.getMaterialStream();

				while ((read = materialStream.read(buffer)) > 0) {
					os.write(buffer, 0, read);
				}
				os.flush();
				os.close();
			}
		}

		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
				Log logger = LogFactory.getLog(getClass());

				@Override
				public void afterCompletion(int status) {
					if (status == TransactionSynchronization.STATUS_COMMITTED) {
						try {
							FileUtils.forceDelete(folderLockFile);
						} catch (IOException ex) {
							logger.error("Unable to delete replication lock", ex);
						}
					} else {
						try {
							FileUtils.deleteDirectory(folder);
						} catch (IOException ex) {
							logger.error("Unable to delete replication directory", ex);
						}
					}
				}
			});
		} else {
			folderLockFile.delete();
		}
	}

	/**
	 * Перевод данных из файла в пакет репликации
	 * @param file Файл сданными
	 * @return Пакет репликации
	 * @throws JAXBException
	 */
	public static ReplicationPackage fileToPackage(File file) throws JAXBException{
		Unmarshaller unmarshaller = context.createUnmarshaller();
		return (ReplicationPackage) unmarshaller.unmarshal(file);

	}

	/**
	 * Перевод пакета репликации в текст
	 * @param rpg Пакет репликации
	 * @return Текст
	 * @throws JAXBException
	 */
	public static String packageToString(ReplicationPackage rpg) throws JAXBException{
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		StringWriter sw = new StringWriter();
		marshaller.marshal(rpg, sw);
		return sw.getBuffer().toString();
	}

	/**
	 * Перевод текста в пакет репликации
	 * @param text Текст
	 * @return Пакет репликации
	 * @throws JAXBException
	 */
	public static ReplicationPackage stringToPackage(String text) throws JAXBException{
		Unmarshaller unmarshaller = context.createUnmarshaller();
		return (ReplicationPackage) unmarshaller.unmarshal(new StringReader(text));
	}
	
	public static StrictSearch makeSearchByKeywords(ReplicationPackage.Card cardFromXml, DataServiceFacade dataService) throws DataException, JAXBException, IOException {
		StrictSearch search = new StrictSearch();
		search.setTemplates(Collections.singleton(Template.createFromId(new ObjectId(Template.class, cardFromXml.getTemplateId()))));
		ReplicationTemplateConfig.Template template = getReplicationTemplate(cardFromXml);
		if (template == null || template.getKeywords() == null || template.getKeywords().getKeyAttribute() == null || template.getKeywords().getKeyAttribute().isEmpty()) {
			return null;
		}
		for (String keyword : template.getKeywords().getKeyAttribute()) {
			ReplicationPackage.Card.Attribute attribute = getReplicationAttribute(cardFromXml, keyword);
			// если ключевый атрибут не был выгружен, тогда поиск не производится
			// и потом создается новая карточка
			if (attribute == null) {
				return null;
			}
			if (!attribute.getStringValue().isEmpty()) {
				for (String value : attribute.getStringValue()) {
					search.addStringAttribute(
							new ObjectId (StringAttribute.class, attribute.getCode()),
							value);
				}
			} else if (!attribute.getNumberValue().isEmpty()) {
				for (BigInteger value : attribute.getNumberValue()) {
					search.addIntegerAttribute(
							new ObjectId (IntegerAttribute.class, attribute.getCode()),
							value.intValue(),
							value.intValue());
				}
			} else if (!attribute.getDateValue().isEmpty()) {
				for (XMLGregorianCalendar value : attribute.getDateValue()){
					search.addDateAttribute(
							new ObjectId (DateAttribute.class, attribute.getCode()),
							value.toGregorianCalendar().getTime(),
							value.toGregorianCalendar().getTime());
				}
			} else if (!attribute.getPersonValue().isEmpty()) {
				for(PersonValue pv : attribute.getPersonValue()) {
					GetPerson getPerson = new GetPerson(pv.getLogin(), pv.getEmail(), pv.getFullName(), pv.getUuid());
					PersonWrapper wrap = dataService.doAction(getPerson);
					Person person = wrap != null ? wrap.getPerson() : null;
					if (person != null) {
						search.addPersonAttribute(
							new ObjectId (PersonAttribute.class, attribute.getCode()),
							person.getId());
					}
				}
			}
		}
		return search;
	}
	
	public static List<ObjectId> execSearchByKeywords(StrictSearch search, ReplicationPackage.Card cardFromXml, DataServiceFacade dataService) 
			throws DataException {
		List<ObjectId> result = dataService.doAction(search);
		
		//убираем карточки с заданными REPLICATION_UUID, т.к. они точно не те что мы ищем,
		//потому что поиск по атрибутам производится только если поиск по REPLICATION_UUID ничего не выдал,
		//значит в искомой карточке REPLICATION_UUID должен быть пустым
		logger.error("Filtering result by condition: attribute REPLICATION_UUID must be empty");
		List<ObjectId> filtered = new ArrayList<ObjectId>();
		for (ObjectId foundCardId : result) {
			List<String> res = CardRelationUtils.resolveLink(foundCardId, dataService, CardRelationUtils.REPLICATION_UUID.getId());
			if (res.isEmpty()) {
				if (logger.isErrorEnabled()) {
					logger.error("Filtered card with empty REPLICATIION_UUID. Id:" + foundCardId);
				}
				filtered.add(foundCardId);
			} else {
				if (logger.isErrorEnabled()) {
					logger.error("Ignoring card with not empty REPLICATION_UUID="+res.get(0)+". Id:" + foundCardId);
				}
			}
		}
		if (logger.isErrorEnabled()) {
			logger.error("Summary result size:" + result.size());
			logger.error("Summary filtered result size:" + filtered.size());
		}
		result = filtered;
		
		//если найдено больше 1, то доп. поиск по card_id из репл. пакета
		if (result.size() > 1) {
			if (logger.isErrorEnabled()) {
				logger.error("Found " + result.size() + " card by keyword strict search for card with UUID = " + cardFromXml.getGuid());
			}
			ObjectId replCardId = new ObjectId(Card.class, cardFromXml.getCardId());
			
			//попробуем найти по пришедшему в пакете card_id
			for (ObjectId foundCardId : result) {
				if (foundCardId.equals(replCardId)) {
					result = Collections.singletonList(foundCardId);
					if (logger.isErrorEnabled()) {
						logger.error("Filtered 1 card by card_id from repl. package (id = " + foundCardId + ")");
					}
					break;
				}
			}
		}
		
		//если все еще результат больше 1ой карточки, то берем в качестве результата первую карточку
		if (result.size() > 1) {
			if (logger.isErrorEnabled()) {
				logger.error("Result still contains more than 1 card. Getting first card as single result: " + result.get(0));
			}
			result = Collections.singletonList(result.get(0));
		}
		
		return result;
	}
	
	//получение конфига шаблона
	public static ReplicationTemplateConfig.Template getReplicationTemplate(ReplicationPackage.Card cardFromXml) 
			throws JAXBException, IOException {
		for(ReplicationTemplateConfig.Template template :
		ReplicationConfiguration.getReplicationTemplateConfig().getTemplate()) {
			if (template.getId() == cardFromXml.getTemplateId()) {
				return template;
			}
		}
		return null;
	}

	//получение атрибута из xml-карточки
	public static ReplicationPackage.Card.Attribute getReplicationAttribute(ReplicationPackage.Card cardFromXml, String nameAttr) {
		for (ReplicationPackage.Card.Attribute attribute : cardFromXml.getAttribute()) {
			if (attribute.getCode().equals(nameAttr)) {
				return attribute;
			}
		}
		return null;
	}
	
	public static String formUuidAttribute(Card card, DataServiceFacade service, JdbcTemplate jdbc) 
			throws DataAccessException, JAXBException, IOException, DataException {
		return formUuidAttribute(card, ReplicationConfiguration.getReplicationNodeConfig().getServerGUID(), service, jdbc);
	}
	
	public static String formUuidAttribute(Card card, String owner, DataServiceFacade service, JdbcTemplate jdbc) 
			throws DataAccessException, JAXBException, IOException, DataException {
		List<String> uuidInDb = CardRelationUtils.resolveLink(card.getId(), service, 
				CardRelationUtils.REPLIC_BASEDOC_LNK.getId(), 
				CardRelationUtils.REPLIC_GUID.getId());
		UUID uuid = UUID.randomUUID();
		String uuidStr = uuid.toString();
		if (!uuidInDb.isEmpty()) {
			uuidStr = uuidInDb.get(0);
		}

		//GUID карточки
		jdbc.update(
				"insert into attribute_value (card_id, attribute_code, string_value) values(?, ?, ?)",
				new Object[] { card.getId().getId(), CardRelationUtils.REPLICATION_UUID.getId(), uuidStr },
				new int[] { Types.NUMERIC, Types.VARCHAR, Types.VARCHAR });

		StringAttribute ownerAttr = card.getAttributeById(CardRelationUtils.REPLIC_OWNER);
		if (!StrUtils.isStringEmpty(ownerAttr.getValue())) {
			owner = ownerAttr.getValue();
		}
		// GUID владелец репликации
		jdbc.update(
				"insert into attribute_value (card_id, attribute_code, string_value) select ?, ?, ?" +
				"where not exists (select 1 from attribute_value where card_id=? and attribute_code=? and string_value=?)",
				new Object[] { 	card.getId().getId(), CardRelationUtils.REPLIC_OWNER.getId(), owner,
								card.getId().getId(), CardRelationUtils.REPLIC_OWNER.getId(), owner},
				new int[] { Types.NUMERIC, Types.VARCHAR, Types.VARCHAR,
							Types.NUMERIC, Types.VARCHAR, Types.VARCHAR });
		
		card.<StringAttribute>getAttributeById(CardRelationUtils.REPLICATION_UUID).setValue(uuidStr);
		card.<StringAttribute>getAttributeById(CardRelationUtils.REPLIC_OWNER).setValue(owner);
		return uuidStr;
	}
	
	public static String formUuidAttribute(Person person, JdbcTemplate jdbc) 
			throws DataAccessException, JAXBException, IOException {
		UUID uuid = UUID.randomUUID();

		//вставляем UUID персоны
		jdbc.update(
				"update person set replication_uuid=? where person_id=? and replication_uuid is null",
				new Object[] { uuid.toString(), person.getId().getId() },
				new int[] { Types.VARCHAR, Types.NUMERIC });

		return uuid.toString();
	}
	
	public static ReplicationPackage getReplicationPackageFromCard(Card card, ActionPerformer dataService) throws DataException, JAXBException, ServiceException {
		GetReplicationHistoryForCard action = new GetReplicationHistoryForCard();
		action.setCardId(card.getId());
		
		ObjectId cardHistId = dataService.doAction(action);

		if (cardHistId != null) {	
			Card cardHist = dataService.getById(cardHistId);
			HtmlAttribute attr = cardHist.getAttributeById(ObjectId.predefined(HtmlAttribute.class, "jbr.replicationHistory.replicXml"));
			String text = attr.getValue();
			ReplicationPackage replicationPackage = ReplicationUtils.stringToPackage(text);
			return replicationPackage;
		}
		logger.warn("Replication history not exists --- return null");
		return null;
	}
	
	public static String getOwnerGuid(ReplicationPackage replicationPackage){
		String result = "";
		for (ReplicationPackage.Card.Attribute attribute : replicationPackage.getCard().getAttribute()){
			if (attribute.getCode().equals(CardRelationUtils.REPLIC_OWNER.getId())){
				result = attribute.getStringValue().get(0);
			}
		}
		return result;
	}
	
	public static Date getChangeDateFromPackage(ReplicationPackage replicationPackage) {
		Date result = null;
		for (ReplicationPackage.Card.Attribute attribute : replicationPackage.getCard().getAttribute()) {
			if (attribute.getCode().equals(Attribute.ID_CHANGE_DATE.getId())) {
				result = attribute.getDateValue().get(0).toGregorianCalendar().getTime();
			}
		}
		return result;
	}
}
