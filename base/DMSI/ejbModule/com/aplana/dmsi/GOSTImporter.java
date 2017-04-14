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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dmsi.card.CardHandler;
import com.aplana.dmsi.expansion.ExpansionManager;
import com.aplana.dmsi.expansion.ExpansionProcessor;
import com.aplana.dmsi.types.AckResult;
import com.aplana.dmsi.types.AcknowledgementType;
import com.aplana.dmsi.types.Addressee;
import com.aplana.dmsi.types.Author;
import com.aplana.dmsi.types.DMSIObject;
import com.aplana.dmsi.types.DocTransfer;
import com.aplana.dmsi.types.DocumentType;
import com.aplana.dmsi.types.Header;
import com.aplana.dmsi.types.HeaderMessageEnumType;
import com.aplana.dmsi.types.ImportedDocumentType;
import com.aplana.dmsi.types.LinkDocTransfer;
import com.aplana.dmsi.types.Organization;
import com.aplana.dmsi.types.OrganizationOnly;
import com.aplana.dmsi.types.OrganizationWithSign;
import com.aplana.dmsi.types.Task;
import com.aplana.dmsi.types.TaskListType;

public class GOSTImporter {

	private Log logger = LogFactory.getLog(getClass());

	private Processor processor;
	private Map<HeaderMessageEnumType, List<Processor>> processorsByType = new HashMap<HeaderMessageEnumType, List<Processor>>();

	public GOSTImporter() {
		addProcessor(HeaderMessageEnumType.DOCUMENT, commonDocProcessor);
		addProcessor(HeaderMessageEnumType.ACKNOWLEDGEMENT, commonAckProcessor);
	}

	private DataServiceFacade dataServiceBean;
	private ObjectId importedCardId;
	private Map<ObjectId, String> paths;

	public static interface Processor {
		void preProcess(Header header) throws DMSIException;

		void postProcess(ObjectId cardId) throws DMSIException;
	}

	public Processor getProcessor() {
		return this.processor;
	}

	public void setProcessor(Processor processor) {
		this.processor = processor;
	}

	public List<Processor> getProcessors(HeaderMessageEnumType type) {
		List<Processor> typeProcessors = processorsByType.get(type);
		if (typeProcessors == null) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(typeProcessors);
	}

	public void addProcessor(HeaderMessageEnumType type, Processor typeProcessor) {
		if (type == null) {
			return;
		}
		List<Processor> typeProcessors = processorsByType.get(type);
		if (typeProcessors == null) {
			typeProcessors = new ArrayList<Processor>();
			processorsByType.put(type, typeProcessors);
		}
		typeProcessors.add(typeProcessor);
	}

	public void importCard(Long headerCardId, byte[] data) throws GOSTException {
		Header header = new Header();
		try {
			GOSTValidator validator = new GOSTValidator();
			validator.validate(new ByteArrayInputStream(data));
			header = unmarshallData(new ByteArrayInputStream(data));
			validator.validate(header);
			ExpansionManager expansionManager = Configuration.instance().getExpansionManager();
			ExpansionProcessor expansionProcessor = expansionManager.getProcessor(header.getExpansion());
			expansionProcessor.importPreProcess(dataServiceBean, header);
			preProcess(header);
			fillHeaderCard(header, headerCardId);
			switch (header.getMsgType()) {
				case DOCUMENT:
					loadDocument(header.getDocument());
					break;
				case ACKNOWLEDGEMENT:
					loadAcknowledgement(header.getAcknowledgement());
					break;
				default:
					throw new UnsupportedOperationException(header.getMsgType() + " message type is not supported now");
			}
			expansionProcessor.importPostProcess(dataServiceBean, header);
			postProcess(header, getImportedCardId());
		} catch (JAXBException e) {
			throw new GOSTException(e);
		} catch (GOSTException e) {
			throw e;
		} catch (DMSIException e) {
			throw new GOSTException(e);
		}
	}

	private void fillHeaderCard(Header header, Long headerCardId) throws DMSIException {
		if (headerCardId == null) {
			logger.info("Header card id is null. Header information will be not loaded");
			return;
		}
		header.setId(headerCardId.toString());
		updateCard(header);
	}

	private void preProcess(Header header) throws DMSIException {
		header.setName(header.getMsgId());
		List<Processor> typeProcessors = getProcessors(header.getMsgType());
		for (Processor typeProcessor : typeProcessors) {
			typeProcessor.preProcess(header);
		}
		if (processor != null) {
			processor.preProcess(header);
		}
	}

	private void loadDocument(DocumentType document) throws DMSIException {
		DataServiceFacade serviceBean = getDataServiceBean();
		CardHandler cardHandler = new CardHandler(serviceBean);
		ObjectId docId = cardHandler.createCard(document);
		document.setId(String.valueOf(docId.getId()));
		Map<ObjectId, String> fileCards = new HashMap<ObjectId, String>();
		List<DocTransfer> files = document.getDocTransfer();
		for (DocTransfer file : files) {
			if (file instanceof LinkDocTransfer) {
				LinkDocTransfer fileLink = (LinkDocTransfer) file;
				String fileId = fileLink.getId();
				String fileName = fileLink.getLink();
				ObjectId cardId = getCardId(fileId);
				fileCards.put(cardId, fileName);
			}
		}
		this.importedCardId = docId;
		this.paths = fileCards;
	}

	private void loadAcknowledgement(AcknowledgementType acknowledgement) throws DMSIException {
		DataServiceFacade serviceBean = getDataServiceBean();
		CardHandler cardHandler = new CardHandler(serviceBean);
		ObjectId ackId = cardHandler.createCard(acknowledgement);
		acknowledgement.setId(String.valueOf(ackId.getId()));
		this.importedCardId = ackId;
		this.paths = Collections.emptyMap();
	}

	private void postProcess(Header header, ObjectId cardId) throws DMSIException {
		List<Processor> typeProcessors = getProcessors(header.getMsgType());
		for (Processor typeProcessor : typeProcessors) {
			typeProcessor.postProcess(cardId);
		}
		if (processor != null) {
			processor.postProcess(cardId);
		}
	}

	private static ObjectId getCardId(String id) {
		try {
			return new ObjectId(Card.class, Long.parseLong(id));
		} catch (NumberFormatException ex) {
			throw new IllegalStateException("Id of card is invalid " + id);
		}
	}

	private void updateCard(DMSIObject object) throws DMSIException {
		DataServiceFacade serviceBean = getDataServiceBean();
		CardHandler cardHandler = new CardHandler(serviceBean);
		cardHandler.updateCard(object);
	}

	private static Header unmarshallData(InputStream sourceStream) throws JAXBException {
		JAXBContext context = Configuration.instance().getJAXBContext();
		Unmarshaller um = context.createUnmarshaller();
		Header header = (Header) um.unmarshal(sourceStream);
		return header;
	}

	private DataServiceFacade getDataServiceBean() {
		return this.dataServiceBean;
	}

	public void setDataServiceBean(DataServiceFacade serviceBean) {
		this.dataServiceBean = serviceBean;
	}

	public ObjectId getImportedCardId() {
		return this.importedCardId;
	}

	public Map<ObjectId, String> getPaths() {
		return this.paths;
	}

	private static Processor commonDocProcessor = new Processor() {
		public void preProcess(Header header) {
			DocumentType doc = header.getDocument();
			defineSender(header);
			defineAddressee(header);
			TaskListType taskList = header.getTaskList();
			if (taskList != null) {
				for (Task task : taskList.getTask()) {
					task.fillAuthorsDescription();
					task.fillExecutorsDescription();
					task.fillCoExecutorsDescription();
				}
				doc.setTasksDescription(taskList.toString());
			}
		}

		/**
		 * (BR4J00018779) ����� ������ ��� ����������� ������������
		 * ����������� ���������. � ������, ����� � ��� �����������
		 * ����������� �� ��������� � ������� (��������, ����� ��������
		 * ��� �������� ������������� ��������), � ����������� ���������
		 * ���������� ��������� �� �����, ��� ���� �� �������
		 * OfficialPerson � �����������-�������, ������� �� ��������� �
		 * ������������.
		 */
		private void defineSender(Header header) {
			String fromOrganizationName = header.getFromOrganization();
			if (fromOrganizationName == null || "".equals(fromOrganizationName)) {
				return;
			}
			ImportedDocumentType doc = (ImportedDocumentType) header.getDocument();
			List<Author> authors = doc.getAuthor();
			List<Author> senderAuthors = findSenderAuthors(authors, fromOrganizationName);
			OrganizationOnly senderOrganization = null;
			if (senderAuthors.isEmpty()) {
				senderOrganization = new OrganizationOnly();
				senderOrganization.setFullname(fromOrganizationName);
			} else {
				Object containedObject = senderAuthors.get(0).getContainedObject();
				senderOrganization = ((OrganizationWithSign) containedObject).getOrganization();
			}
			doc.setSender(senderOrganization);
			List<Author> nonSenderAuthors = new ArrayList<Author>(authors);
			nonSenderAuthors.removeAll(senderAuthors);
			for (Author author : nonSenderAuthors) {
				Object containedObject = author.getContainedObject();
				if (!(containedObject instanceof OrganizationWithSign)) {
					continue;
				}
				authors.remove(author);
				((OrganizationWithSign) containedObject).getOfficialPersonWithSign().clear();
				authors.add(author);
			}
		}

		private List<Author> findSenderAuthors(Collection<Author> authors, String senderName) {
			List<Author> senderAuthors = new ArrayList<Author>();
			for (Author author : authors) {
				Object containedObject = author.getContainedObject();
				if (!(containedObject instanceof OrganizationWithSign)) {
					continue;
				}
				OrganizationOnly authorOrganization = ((OrganizationWithSign) containedObject).getOrganization();
				if (authorOrganization == null) {
					continue;
				}

				if (isNameOfOrganization(senderName, authorOrganization)) {
					senderAuthors.add(author);
				}
			}
			return senderAuthors;
		}

		private boolean isNameOfOrganization(String senderName, OrganizationOnly authorOrganization) {
			return senderName != null &&
				(senderName.equals(authorOrganization.getFullname())
						|| senderName.equals(authorOrganization.getShortname()));
		}

		private void defineAddressee(Header header) {
			ImportedDocumentType doc = (ImportedDocumentType) header.getDocument();
			String toOrganizationName = header.getToOrganization();
			OrganizationOnly receiver = null;

			for (Iterator<Addressee> iter = doc.getAddressee().iterator(); iter.hasNext();) {
				Addressee addressee = iter.next();
				Object containedObject = addressee.getContainedObject();
				if (containedObject instanceof Organization) {
					OrganizationOnly addresseeOrganization = ((Organization) containedObject).getSourcedOrganization();
					if (addresseeOrganization == null) {
						iter.remove();
						continue;
					}
					doc.getAddresseOrganizations().add(addresseeOrganization);
					if (isNameOfOrganization(toOrganizationName, addresseeOrganization)) {
						receiver = addresseeOrganization;
					} else {
						iter.remove();
					}
				}
			}

			if (receiver == null && toOrganizationName != null && !"".equals(toOrganizationName.trim())) {
				receiver = new OrganizationOnly();
				receiver.setFullname(toOrganizationName);
			}

			doc.setReceiver(receiver);
		}

		public void postProcess(ObjectId cardId) {
		}
	};

	private static Processor commonAckProcessor = new Processor() {
		public void preProcess(Header header) {
			AcknowledgementType ack = header.getAcknowledgement();
			List<AckResult> results = ack.getAckResult();
			if (results.size() > 0) {
				AckResult firstResult = results.get(0);
				ack.setAckResultDescription(firstResult.getValue());
				ack.setAckResultErrorCode(firstResult.getErrorcode().longValue());
			}
		}

		public void postProcess(ObjectId cardId) {
		}
	};

}
