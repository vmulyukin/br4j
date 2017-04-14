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
package com.aplana.dmsi;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.ImportCardFromXml.ImportCard.Result;
import com.aplana.dbmi.action.ImportCardFromXml.ImportCard.StatusDescription;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TypeStandard;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.query.WriteQuery;
import com.aplana.dmsi.GOSTImporter.Processor;
import com.aplana.dmsi.action.ImportCardByGOST;
import com.aplana.dmsi.card.handling.CardFacade;
import com.aplana.dmsi.types.AddDocumentsType;
import com.aplana.dmsi.types.DocTransfer;
import com.aplana.dmsi.types.DocumentType;
import com.aplana.dmsi.types.FolderAddType;
import com.aplana.dmsi.types.Header;
import com.aplana.dmsi.types.HeaderMessageEnumType;
import com.aplana.dmsi.types.ImportedDocumentType;
import com.aplana.dmsi.types.IsPrimeEnumType;
import com.aplana.dmsi.types.Referred;
import com.aplana.dmsi.types.ReferredRetypeEnumType;
import com.aplana.dmsi.types.RegNumber;
import com.aplana.dmsi.types.Task;
import com.aplana.dmsi.types.TaskListType;
import com.aplana.dmsi.types.TaskNumber;
import com.aplana.dmsi.types.AddDocumentsType.Folder;
import com.aplana.dmsi.types.common.DeliveryMethod;
import com.aplana.dmsi.util.ServiceUtils;

public class DoImportCardByGOST extends ActionQueryBase implements WriteQuery {

	private static final long serialVersionUID = 1L;
	/**
	 * Identifier of 'DoImportCardByGOST' action to be used in system log
	 */
	public static final String EVENT_ID = "IMPORT_CARD_BY_GOST";

	@Override
	public String getEvent() {
		return EVENT_ID;
	}

	@Override
	public Object processQuery() throws DataException {
		ObjectFactory.startWork(TypeStandard.GOST);
		try {
			ImportCardByGOST action = (ImportCardByGOST) getAction();
			long gostMessageCardId = action.getGostMessageCardId();
			byte[] streamData = action.getStreamData();
			ServiceUtils.clearEventLog();
			final DataServiceFacade serviceBean = getDataServiceBean();
			GOSTImporter importer = new GOSTImporter();
			importer.setDataServiceBean(serviceBean);
			final long packetCardId = action.getPacketCardId();
			importer.addProcessor(HeaderMessageEnumType.DOCUMENT, new Processor() {

				final ObjectId directionAttributeId = ObjectId.predefined(ListAttribute.class, "jbr.gost.msg.direction");
				final ObjectId incomeDirectionValue = ObjectId.predefined(ReferenceValue.class, "jbr.gost.msg.direction.income");
				final ObjectId gostMessageTemplateId = ObjectId.predefined(Template.class, "jbr.gost.msg");

				public void preProcess(Header header) throws GOSTException {
					ImportedDocumentType doc = (ImportedDocumentType) header.getDocument();
					doc.setDeliveryMethod(DeliveryMethod.GOST);
					validate(header);
					doc.setTaskList(header.getTaskList());
				}

				private void validate(Header header) throws GOSTException {
					try {
						String uuid = header.getMsgId();
						Search search = new Search();
						search.setByAttributes(true);
						search.setTemplates(Collections.singleton(gostMessageTemplateId));
						search.addStringAttribute(Attribute.ID_UUID, uuid, Search.TextSearchConfigValue.EXACT_MATCH_NOT_CASE_SENSITIVE);
						search.addListAttribute(directionAttributeId, Collections.singleton(DataObject.createFromId(incomeDirectionValue)));
						Collection<Card> cards = ServiceUtils.searchCards(serviceBean, search, null);
						if (cards.size() > 0) {
							throw new GOSTException("document.preProcess.docAlreadyLoaded");
						}
					} catch (GOSTException ex) {
						throw ex;
					} catch (DMSIException ex) {
						throw new GOSTException("document.preProcess.docAlreadyLoaded", ex);
					}
				}

				public void postProcess(ObjectId cardId) throws DMSIException {

				}
			});
			importer.addProcessor(HeaderMessageEnumType.ACKNOWLEDGEMENT, new Processor() {

				private ObjectId distributionListElemId;

				public void preProcess(Header header) throws GOSTException {
					try {
						String uuid = header.getAcknowledgement().getMsgId();
						Search search = new Search();
						search.setByAttributes(true);
						search.addStringAttribute(ObjectId.predefined(StringAttribute.class,
								"jbr.distributionItem.uuid"), uuid);
						search.setTemplates(Collections.singleton(DataObject.createFromId(ObjectId.predefined(
								Template.class, "jbr.DistributionListElement"))));
						Collection<Card> cards = ServiceUtils.searchCards(serviceBean, search, null);
						if (cards.size() == 0) {
							throw new GOSTException("acknowledgement.preProcess.sourceNotFound");
						}
						if (cards.size() > 1) {
							throw new GOSTException("acknowledgement.preProcess.sourceMoreThanOneFound");
						}
						this.distributionListElemId = cards.iterator().next().getId();
					} catch (GOSTException ex) {
						throw ex;
					} catch (DMSIException ex) {
						throw new GOSTException("acknowledgement.preProcess.sourceNotFound", ex);
					}

				}

				public void postProcess(ObjectId cardId) throws DMSIException {
					CardFacade cardFacade = new CardFacade(serviceBean, distributionListElemId);
					cardFacade.addAttributeValue(ObjectId.predefined(CardLinkAttribute.class,
							"jbr.distributionItem.sendInfo"), cardId);
					cardFacade.updateCard();
				}
			});
			importer.setProcessor(new Processor() {

				Map<String, Task> tasksByIdnumber;
				Map<TaskNumber, Task> tasksByTaskNumber;
				Map<String, DocumentType> documentsByIdnumber;
				Map<RegNumber, DocumentType> documentsByRegNumber;

				public void preProcess(Header header) throws DMSIException {
					AddDocumentsType addDocuments = header.getAddDocuments();
					if (addDocuments == null) {
						return;
					}

					initReferInformation(header);

					List<Folder> folders = addDocuments.getFolder();
					for (Folder folder : folders) {
						if (FolderAddType.DOC_ATTACHMENTS.equals(folder.getAddType()) ||
								FolderAddType.INFO_MATERIALS.equals(folder.getAddType())) {
							processDocAttachments(folder);
						}
					}
				}

				private void processDocAttachments(Folder folder) {
					List<Referred> refs = folder.getReferred();
					for (Referred ref : refs) {
						if (ref.getRetype() != null && StringUtils.isNotBlank(ref.getIdnumber())) {
							if (ReferredRetypeEnumType.TASK.equals(ref.getRetype())) {
								Task referredTask = tasksByIdnumber.get(ref.getIdnumber());
								if (referredTask != null) {
									referredTask.getDocTransfer().addAll(folder.getDocTransfer());
								}
							} else {
								DocumentType referredDoc = documentsByIdnumber.get(ref.getIdnumber());
								if (referredDoc != null) {
									updatePrimeInDocTransfers(folder);
									referredDoc.getDocTransfer().addAll(folder.getDocTransfer());
								}
							}
						} else {
							if (ref.getTaskNumber() != null) {
								Task referredTask = tasksByTaskNumber.get(ref.getTaskNumber());
								if (referredTask != null) {
									referredTask.getDocTransfer().addAll(folder.getDocTransfer());
								}
							} else if (ref.getRegNumber() != null) {
								DocumentType referredDoc = documentsByRegNumber.get(ref.getRegNumber());
								if (referredDoc != null) {
									updatePrimeInDocTransfers(folder);
									referredDoc.getDocTransfer().addAll(folder.getDocTransfer());
								}
							}
						}
					}
				}
				
				private void updatePrimeInDocTransfers(Folder folder)
				{
					// ��� ������� ��������� �� ���� � ����������� �������� �� ������ XML � ���� Folder: 
					// - ��� ������, � ������� � ���� Folder � �������� add_type ������� �������� "0" 
					// 		� ����������� �� "����" � �������� "�������� ��������" ��������� �������� "��";
					// - ��� ������, � ������� � ���� Folder � �������� add_type ������� �������� "1" 
					// 		� ����������� �� "����" � �������� "�������� ��������" ��������� �������� "���".
					switch (folder.getAddType())
					{
						case DOC_ATTACHMENTS:
							Iterator<DocTransfer> it = folder.getDocTransfer().iterator();
							while (it.hasNext()) {
								it.next().setIsPrimeId(IsPrimeEnumType.YES);
							}
							break;
						case INFO_MATERIALS:
							Iterator<DocTransfer> iter = folder.getDocTransfer().iterator();
							while (iter.hasNext()) {
								iter.next().setIsPrimeId(IsPrimeEnumType.NO);
							}
							break;
					}
				}

				private void initReferInformation(Header header) {
					tasksByIdnumber = new HashMap<String, Task>();
					tasksByTaskNumber = new HashMap<TaskNumber, Task>();
					documentsByIdnumber = new HashMap<String, DocumentType>();
					documentsByRegNumber = new HashMap<RegNumber, DocumentType>();

					DocumentType doc = header.getDocument();
					if (doc != null) {
						documentsByIdnumber.put(doc.getIdnumber(), doc);
						documentsByRegNumber.put(doc.getRegNumber(), doc);
					}

					TaskListType taskList = header.getTaskList();
					if (taskList != null) {
						for (Task task : taskList.getTask()) {
							tasksByIdnumber.put(task.getIdnumber(), task);
							tasksByTaskNumber.put(task.getTaskNumber(), task);
						}
					}
				}

				public void postProcess(ObjectId cardId) throws DMSIException {
					CardFacade cardFacade = new CardFacade(serviceBean, cardId);
					cardFacade.addAttributeValue(ObjectId.predefined(CardLinkAttribute.class, "jbr.original.source"),
							packetCardId);
					cardFacade.updateCard();
				}
			});
			importer.importCard(gostMessageCardId, streamData);
			Result result = new Result(importer.getImportedCardId(), importer.getPaths());
			ServiceUtils.setLogIntoDatabase(getBeanFactory(), gostMessageCardId, getUser(), EVENT_ID);
			return result;
		} catch (GOSTException ex) {
			logger.error(ex.getMessage(), ex);
			Result res = new Result();
			StatusDescription status = new StatusDescription();
			status.setStatusCode(ex.getErrorCode());
			status.setResult(ex.getMessage());
			res.setStatusDescription(status);
			return res;
		} finally {
			ObjectFactory.finishWork();
    }
}

	private DataServiceFacade getDataServiceBean() {
		DataServiceFacade serviceBean = new DataServiceFacade();
		serviceBean.setUser(getUser());
		serviceBean.setDatabase(getDatabase());
		serviceBean.setQueryFactory(getQueryFactory());
		return serviceBean;
	}
}
