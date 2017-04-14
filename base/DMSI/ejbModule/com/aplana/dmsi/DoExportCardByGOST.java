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

import static org.apache.commons.lang.StringUtils.defaultString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.TypeStandard;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.query.WriteQuery;
import com.aplana.dmsi.action.ExportCardByGOST;
import com.aplana.dmsi.card.handling.CardFacade;
import com.aplana.dmsi.expansion.ExpansionManager;
import com.aplana.dmsi.object.DMSIObjectFactory;
import com.aplana.dmsi.types.AddDocumentsType;
import com.aplana.dmsi.types.AddDocumentsType.Folder;
import com.aplana.dmsi.types.Addressee;
import com.aplana.dmsi.types.DistributionListElement;
import com.aplana.dmsi.types.DocTransfer;
import com.aplana.dmsi.types.DocumentType;
import com.aplana.dmsi.types.Executor;
import com.aplana.dmsi.types.ExecutorEnumType;
import com.aplana.dmsi.types.ExportedIncomeDocumentType;
import com.aplana.dmsi.types.ExportedOGDocumentType;
import com.aplana.dmsi.types.ExportedORDDocumentType;
import com.aplana.dmsi.types.FolderAddType;
import com.aplana.dmsi.types.Header;
import com.aplana.dmsi.types.IsPrimeEnumType;
import com.aplana.dmsi.types.Note;
import com.aplana.dmsi.types.Organization;
import com.aplana.dmsi.types.PrivatePerson;
import com.aplana.dmsi.types.Referred;
import com.aplana.dmsi.types.ReferredRetypeEnumType;
import com.aplana.dmsi.types.Task;
import com.aplana.dmsi.types.TaskListType;
import com.aplana.dmsi.types.TransferEnumType;

public class DoExportCardByGOST extends ActionQueryBase implements WriteQuery {

	private static final long serialVersionUID = 1L;

	@Override
	public Object processQuery() throws DataException {
		ObjectFactory.startWork(TypeStandard.GOST);
		try {
			return doProcess();
		} catch (DMSIException ex) {
			throw new DataException(ex);
		} finally {
			ObjectFactory.finishWork();
		}
	}

	private Object doProcess() throws DataException, DMSIException {
		ExportCardByGOST action = (ExportCardByGOST) getAction();
		ObjectId cardId = action.getCardId();
		ObjectId recipientId = action.getRecipientId();
		GOSTExporter exporter = new GOSTExporter();
		DistributionListElement distributionListElement = getDocumentRecipient(recipientId);
		exporter.setProcessor(new GostExporterProcessor(distributionListElement.getRecipient()));
		exporter.setDataServiceBean(getDataServiceBean());
		ObjectId headerId = getHeaderId(recipientId);
		exporter.setHeaderCardId(headerId);
		ExpansionManager expansionManager = Configuration.instance().getExpansionManager();
		exporter.setExpansionProcessor(expansionManager.getProcessor(TypeStandard.GOST));

		switch (distributionListElement.getMessageType()) {
		case DOCUMENT:
			exporter.setCardId(cardId);
			break;
		case ACKNOWLEDGEMENT:
			exporter.setCardId(getAckId(headerId));
			break;
		default:
			throw new UnsupportedOperationException(distributionListElement.getMessageType()
					+ " message type is not supported now");
		}
		return exporter.exportCard();
	}

	private ObjectId getHeaderId(ObjectId recipientId) throws DMSIException {
		CardFacade cardFacade = new CardFacade(getDataServiceBean(), recipientId);
		ObjectId[] gostMsgIds = (ObjectId[]) cardFacade.getAttributeValue(ObjectId.predefined(CardLinkAttribute.class,
				"jbr.distributionItem.msgGOST"));
		if (gostMsgIds.length > 1) {
			throw new DMSIException("export.headerCard.moreThanOne");
		}
		if (gostMsgIds.length == 0) {
			throw new DMSIException("export.headerCard.notFound");
		}
		return gostMsgIds[0];
	}

	private ObjectId getAckId(ObjectId headerId) throws DMSIException {
		CardFacade cardFacade = new CardFacade(getDataServiceBean(), headerId);
		ObjectId[] ackIds = (ObjectId[]) cardFacade.getAttributeValue(ObjectId.predefined(CardLinkAttribute.class,
				"jbr.gost.msg.acks"));
		if (ackIds.length > 1) {
			throw new DMSIException("export.ackCard.moreThanOne");
		}
		if (ackIds.length == 0) {
			throw new DMSIException("export.ackCard.notFound");
		}
		return ackIds[0];
	}

	protected DistributionListElement getDocumentRecipient(ObjectId recipientId) throws DMSIException {
		DMSIObjectFactory objectFactory = DMSIObjectFactory.newInstance(getDataServiceBean(), "Recipient");
		return (DistributionListElement) objectFactory.newDMSIObject(recipientId);
	}

	private DataServiceFacade getDataServiceBean() {
		DataServiceFacade serviceBean = new DataServiceFacade();
		serviceBean.setUser(getUser());
		serviceBean.setDatabase(getDatabase());
		serviceBean.setQueryFactory(getQueryFactory());
		return serviceBean;
	}

	private static final class GostExporterProcessor implements GOSTExporter.Processor {
		private Organization recipient;

		public GostExporterProcessor(Organization recipient) {
			this.recipient = recipient;
		}

		private static interface FolderConfig {
			List<DocTransfer> getDocTransfer();

			FolderAddType getFolderAddType(IsPrimeEnumType isPrimeId);

			Referred getReferred();
		}

		public void postProcess(Header header) {
			processDocument(header);
			processTaskList(header);
			fillAddDocuments(header);
		}

		private void processDocument(Header header) {
			DocumentType doc = header.getDocument();
			// TODO: enum
			if (doc instanceof ExportedIncomeDocumentType || doc instanceof ExportedOGDocumentType
					|| doc instanceof ExportedORDDocumentType) {
				doc.getAddressee().clear();
				Addressee addressee = new Addressee();
				addressee.setContainedObject(this.recipient);
				doc.getAddressee().add(addressee);
			}
		}

		private void processTaskList(Header header) {
			DocumentType doc = header.getDocument();
			String recipientId = recipient == null ? "" : recipient.getId();

			if (doc != null) {
				TaskListType taskList = doc.getTaskList();
				header.setTaskList(taskList);
				if (taskList != null) {
					// ��� ���� ����� ���������� ��� ���������
					// filterTaskList(taskList.getTask(), recipientId);
				}
			}
		}

		private void filterTaskList(List<Task> tasks, String recipientId) {
			for (Iterator<Task> iterator = tasks.iterator(); iterator.hasNext();) {
				Task task = iterator.next();
				boolean isSuperfluous = true;
				executors: for (Executor taskExecutor : task.getExecutor()) {
					String id = taskExecutor.getOrganization().getId();
					if (recipientId.equals(id)) {
						isSuperfluous = false;
						break executors;
					}
				}
				if (isSuperfluous) {
					iterator.remove();
				}
			}
		}

		private void fillAddDocuments(Header header) {
			AddDocumentsType addDocuments = header.getAddDocuments();
			if (addDocuments == null) {
				addDocuments = new AddDocumentsType();
			}
			List<Folder> folders = addDocuments.getFolder();
			final DocumentType document = header.getDocument();
			if (document != null) {
				folders.addAll(moveDocTransfersToFolders(new FolderConfig() {

					public List<DocTransfer> getDocTransfer() {
						return document.getDocTransfer();
					}
					// ��� �������� ���������� ��������� �� ���� � ����������� XML � ���� Folder:
					// - ��� ������, � ������� � �� "����" � �������� "�������� ��������" ������� "��"
					// 		� �������� add_type ��������� �������� "0";
					// - ��� ������, � ������� � �� "����" � �������� "�������� ��������" ������� "���"
					// 		� �������� add_type ��������� �������� "1".
					public FolderAddType getFolderAddType(IsPrimeEnumType isPrimeId) {
						FolderAddType addType = null;
						switch (isPrimeId)
						{
							case YES:
								addType = FolderAddType.DOC_ATTACHMENTS;
								break;
							case NO:
								addType = FolderAddType.INFO_MATERIALS;
								break;
						}
						return addType;
					}

					public Referred getReferred() {
						Referred ref = new Referred();
						ref.setIdnumber(document.getIdnumber());
						ref.setRetype(ReferredRetypeEnumType.DOCUMENT);
						ref.setRegNumber(document.getRegNumber());
						return ref;
					}
				}));
			}
			TaskListType taskList = header.getTaskList();
			if (taskList != null) {
				for (final Task task : taskList.getTask()) {
					folders.addAll(moveDocTransfersToFolders(new FolderConfig() {

						public List<DocTransfer> getDocTransfer() {
							return task.getDocTransfer();
						}
						public FolderAddType getFolderAddType(IsPrimeEnumType isPrimeId) {
							return FolderAddType.DOC_ATTACHMENTS;
						}

						public Referred getReferred() {
							Referred ref = new Referred();
							ref.setIdnumber(task.getIdnumber());
							ref.setRetype(ReferredRetypeEnumType.TASK);
							ref.setTaskNumber(task.getTaskNumber());
							return ref;
						}

					}));
					mergeTaskExecutors(task);
				}
			}
			header.setAddDocuments(addDocuments);
		}

		private List<Folder> moveDocTransfersToFolders(FolderConfig folderConfig) {
			List<Folder> folders = new ArrayList<Folder>();
			List<DocTransfer> docTransfers = folderConfig.getDocTransfer();
			for (DocTransfer docTransfer : docTransfers) {
				Folder folder = new Folder();
				folder.setAddType(folderConfig.getFolderAddType(docTransfer.getIsPrimeId()));
				folder.getDocTransfer().add(docTransfer);
				folder.setContents(docTransfer.getUid());
				Note note = new Note();
				note.setValue(formatNoteText(docTransfer));
				folder.getNote().add(note);
				Referred ref = folderConfig.getReferred();
				folder.getReferred().add(ref);
				folders.add(folder);
			}
			docTransfers.clear();
			return folders;
		}

		private void mergeTaskExecutors(Task task) {
			List<Executor> mainExecutors = task.getMainExecutor();
			List<Executor> executors = task.getExecutor();
			boolean isFirst = true;
			for (Executor executor : mainExecutors) {
				if (isFirst) {
					executor.setResponsible(ExecutorEnumType.RESPONSIBLE);
					isFirst = false;
				}
				executors.add(executor);
			}
		}

		private String formatNoteText(DocTransfer docTransfer) {
			Map<String, Object> infoPairs = new LinkedHashMap<String, Object>();
			infoPairs.put(docTransfer.getIsPrimeAttributeName(), docTransfer.getIsPrime());
			infoPairs.put(docTransfer.getIncomeDateAttributeName(), docTransfer.getIncomeDate());
			PrivatePerson author = docTransfer.getAuthor();
			infoPairs.put(docTransfer.getAuthorAttributeName(), author == null ? "" : author.getName().getValue());
			infoPairs.put(docTransfer.getNameAttributeName(), docTransfer.getName());
			StringBuilder noteBuilder = new StringBuilder();
			for (Entry<String, Object> infoPair : infoPairs.entrySet()) {
				String key = infoPair.getKey();
				Object value = infoPair.getValue();
				if ((key == null || "".equals(key)) && value == null) {
					continue;
				}
				if (noteBuilder.length() > 0) {
					noteBuilder.append("; ");
				}
				noteBuilder.append(String.format("%s: %s", defaultString(key),
						value == null ? "" : String.valueOf(value)));
			}
			return noteBuilder.toString();
		}

		public Map<ObjectId, String> collectFiles(Header header) {
			Map<ObjectId, String> files = new HashMap<ObjectId, String>();
			DocumentType doc = header.getDocument();
			if (doc != null) {
				addFilesToMapFromDocTransfers(doc.getDocTransfer(), files);
			}
			TaskListType tasks = header.getTaskList();
			if (tasks != null) {
				for (Task task : tasks.getTask()) {
					addFilesToMapFromDocTransfers(task.getDocTransfer(), files);
				}
			}
			AddDocumentsType addDocuments = header.getAddDocuments();
			if (addDocuments != null) {
				for (Folder folder : addDocuments.getFolder()) {
					addFilesToMapFromDocTransfers(folder.getDocTransfer(), files);
				}
			}
			return files;
		}

		private void addFilesToMapFromDocTransfers(Collection<DocTransfer> docTransfers, Map<ObjectId, String> files) {
			for (DocTransfer file : docTransfers) {
				if (TransferEnumType.LINK.equals(file.getTransfertype())) {
					String fileValue = new String(file.getValue());
					file.setDescription(fileValue);
					int i;
					fileValue = ((i = fileValue.lastIndexOf('.')) != -1) ? file.getId().concat(
							fileValue.substring(i, fileValue.length())) : file.getId();
					file.setValue(fileValue.getBytes());
					files.put(GOSTExporter.getCardId(file.getId()), fileValue);
				}
			}
		}

		public Map<String, Object> getAdditions(Header header) {
			return Collections.emptyMap();
		}
	}
}
