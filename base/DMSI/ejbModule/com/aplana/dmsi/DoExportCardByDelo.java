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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang.StringUtils;

import com.aplana.dbmi.action.ExportCardToXml.Result;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TypeStandard;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.query.WriteQuery;
import com.aplana.dbmi.utils.StrUtils;
import com.aplana.dmsi.action.ExportCardByDelo;
import com.aplana.dmsi.card.handling.CardFacade;
import com.aplana.dmsi.object.DMSIObjectFactory;
import com.aplana.dmsi.types.AckResult;
import com.aplana.dmsi.types.AckType;
import com.aplana.dmsi.types.AcknowledgementType;
import com.aplana.dmsi.types.Department;
import com.aplana.dmsi.types.DistributionListElement;
import com.aplana.dmsi.types.DocTransfer;
import com.aplana.dmsi.types.DocumentType;
import com.aplana.dmsi.types.Executor;
import com.aplana.dmsi.types.ExecutorEnumType;
import com.aplana.dmsi.types.ExportedDocumentType;
import com.aplana.dmsi.types.File;
import com.aplana.dmsi.types.FileEnumType;
import com.aplana.dmsi.types.Header;
import com.aplana.dmsi.types.HeaderAsknowEnumType;
import com.aplana.dmsi.types.HeaderMessageEnumType;
import com.aplana.dmsi.types.LinkDocTransfer;
import com.aplana.dmsi.types.Organization;
import com.aplana.dmsi.types.OrganizationOnly;
import com.aplana.dmsi.types.PrivatePerson;
import com.aplana.dmsi.types.Referred;
import com.aplana.dmsi.types.ReferredRetypeEnumType;
import com.aplana.dmsi.types.RegNumber;
import com.aplana.dmsi.types.Task;
import com.aplana.dmsi.types.TaskListType;
import com.aplana.dmsi.types.delo.ExpansionType;
import com.aplana.dmsi.util.ServiceUtils;
import com.aplana.dmsi.util.XMLException;
import com.aplana.dmsi.util.XmlUtils;
import com.aplana.dmsi.value.converters.DateConverter;

public class DoExportCardByDelo extends ActionQueryBase implements WriteQuery {


	private static final long serialVersionUID = 1L;
	public static final ObjectId ADDRESSEE_ATTRIBUTE_ID = ObjectId.predefined(PersonAttribute.class, "jbr.incoming.addressee");

    @Override
    public Object processQuery() throws DataException {
		Configuration.setMode("delo");
		ObjectFactory.startWork(TypeStandard.DELO);
		try {
			return doProcess();
		} catch (DMSIException ex) {
			throw new DataException(ex);
		} finally {
			Configuration.reset();
			ObjectFactory.finishWork();
		}
	}

	private Object doProcess() throws DataException, DMSIException {
	ExportCardByDelo action = (ExportCardByDelo) getAction();
		final ObjectId exportingCardId = action.getCardId();
	final ObjectId recipientId = action.getRecipientId();

		final DistributionListElement distributionListElement = getDocumentRecipient(recipientId);
		//�������� ����������� ���� �������� �������, ������ ���������� ��������� ������ ���������
		final HeaderMessageEnumType messageType = HeaderMessageEnumType.DOCUMENT;

		GOSTExporter exporter = new GOSTExporter() {
			@Override
			protected Header exportHeader() throws DMSIException {
				Configuration config = Configuration.instance();
				Header header = ObjectFactory.createHeader();
				header.setStandart(config.getStandart());
				header.setVersion(config.getVersion());
				header.setFromSysId(config.getSysId());
				header.setFromSystem(config.getSystem());
				header.setFromSystemDetails(config.getSystemDetails());
				header.setTime((XMLGregorianCalendar) new DateConverter().convert(new Date()));
				header.setMsgAcknow(HeaderAsknowEnumType.ALWAYS);
				header.setMsgType(messageType);
				Organization recipientOrganization = distributionListElement.getRecipient();
				header.setRecipient(recipientOrganization);
				return header;
			}
		};

		exporter.addProcessor(HeaderMessageEnumType.DOCUMENT, new GOSTExporter.Processor() {

			public Map<ObjectId, String> collectFiles(Header header) {
				return Collections.emptyMap();
			}

			public Map<String, Object> getAdditions(Header header) {
				return Collections.emptyMap();
			}

			public void postProcess(Header header) throws DMSIException {
				Department senderDepartment = null;
				OrganizationOnly senderOrganization = null;
				PrivatePerson signer = ((ExportedDocumentType) header.getDocument()).getSigner();
				if (signer != null) {
					senderDepartment = signer.getDepartament();
					senderOrganization = signer.getOrganization();
				}
				if (senderOrganization == null) {
					senderOrganization = getDefaultOrganization();
				}
				if (senderOrganization != null) {
					header.setFromOrganization(senderOrganization.getFullname());
					header.setFromOrgId(senderOrganization.getOrganizationid());
				}
				if (senderDepartment != null) {
					header.setFromDepartment(senderDepartment.getFullName());
				}
			}

		});
		exporter.addProcessor(HeaderMessageEnumType.ACKNOWLEDGEMENT, new GOSTExporter.Processor() {

			public Map<ObjectId, String> collectFiles(Header header) {
				return Collections.emptyMap();
			}

			public Map<String, Object> getAdditions(Header header) {
				return Collections.emptyMap();
			}

			public void postProcess(Header header) throws DMSIException {
				DataServiceFacade dataService = getDataServiceBean();
				CardFacade facade = new CardFacade(dataService, exportingCardId);
				ObjectId[] addresseesIds = (ObjectId[]) facade.getAttributeValue(ADDRESSEE_ATTRIBUTE_ID);
				OrganizationOnly senderOrganization = null;
				Department senderDepartment = null;
				if (addresseesIds != null && addresseesIds.length > 0) {
					DMSIObjectFactory factory = DMSIObjectFactory.newInstance(dataService, "PrivatePerson");
					factory.setAllIgnoredExcept("organization", "department", "fullname", "organizationid");
					@SuppressWarnings("unchecked")
					Collection<? extends PrivatePerson> persons = (Collection<? extends PrivatePerson>) factory.newCollection(addresseesIds);
					for (PrivatePerson person : persons) {
						senderOrganization = person.getOrganization();
						senderDepartment = person.getDepartament();
						if (senderOrganization != null) {
							break;
						}
					}
				}
				if (senderOrganization == null) {
					senderOrganization = getDefaultOrganization();
				}
				if (senderOrganization != null) {
					header.setFromOrganization(senderOrganization.getFullname());
					header.setFromOrgId(senderOrganization.getOrganizationid());
				}
				if (senderDepartment != null) {
					header.setFromDepartment(senderDepartment.getFullName());
				}
			}
		});
		exporter.setDataServiceBean(getDataServiceBean());
		exporter.setProcessor(new DeloExportProcessor(distributionListElement.getUid()));
		exporter.setCardId(exportingCardId);
		Result exportResult = exporter.exportCard();
		InputStream docInGOSTFormatStream = exportResult.getData();
		InputStream transformerStream;
		try {
			transformerStream = ServiceUtils.readConfig("delo_export_template.xsl");
			Transformer transformer = XmlUtils.createTransformer(transformerStream);
			ByteArrayOutputStream resultStream = new ByteArrayOutputStream();
			transformer.transform(new StreamSource(docInGOSTFormatStream), new StreamResult(resultStream));

			/*
			ByteArrayOutputStream result = new ByteArrayOutputStream();
			for (int i = 0; i < resultStream.toByteArray().length; i++) {
				result.write(new byte[] { resultStream.toByteArray()[i + 1], resultStream.toByteArray()[i] });
				i++;
			}
			*/

			Result res =  new Result(new ByteArrayInputStream(resultStream.toByteArray()), exportResult.getFiles());
			for (Entry<String, Object> infoEntry : exportResult.getInfos().entrySet()) {
				res.addInfo(infoEntry.getKey(), infoEntry.getValue());
			}
			return res;
		} catch (IOException ex) {
			throw new DataException(ex);
		} catch (XMLException ex) {
			throw new DataException(ex);
		} catch (TransformerException ex) {
			throw new DataException(ex);
		}
	}

	private static class DeloExportProcessor implements GOSTExporter.Processor {

		private String distributionUuid;

		public DeloExportProcessor(String distributionUuid) {
			this.distributionUuid = distributionUuid;
		}

	    public void postProcess(Header header) {
		if (header.getDocument() != null) {
		    processDocumentMessage(header);
		}
		if (header.getTaskList() != null) {
			processTaskList(header);
		}
			if (header.getAcknowledgement() != null) {
				AcknowledgementType ack = header.getAcknowledgement();
				header.setMsgId(ack.getMsgId());
				if (ack.getImportedDocCard() == null) {
					throw new IllegalStateException(
							"It is impossible to resolve uid of income document (imported document was not found)");
	    }
				ack.setMsgId(ack.getImportedDocCard().getUid());
			}
		}

	    private void processDocumentMessage(Header header) {
		DocumentType doc = header.getDocument();
			String uuid = distributionUuid != null && !distributionUuid.isEmpty() ? distributionUuid : doc.getUid();
			if (StringUtils.isEmpty(uuid)) {
				throw new IllegalStateException("It is impossible to export document due UUID is not defined");
			}
			header.setMsgId(uuid);
			createExpansionForDoc(header);
			header.setTaskList(doc.getTaskList());
		}

		private void createExpansionForDoc(Header header) {
			DocumentType doc = header.getDocument();
		List<DocTransfer> docTransfers = doc.getDocTransfer();
			ExpansionType expansion = (ExpansionType) header.getExpansion();
		expansion.setOrganization("���");
			expansion.setExpVer("8.11.2");

		List<File> files = expansion.getFiles();
		moveDocTransfersToFiles(docTransfers, files, FileEnumType.DOCUMENT);

		File passportFile = new File();
		passportFile.setDescription("������� ��");
		passportFile.setName("passport.xml");
		passportFile.setType(FileEnumType.PASSPORT);
		files.add(passportFile);

		expansion.getRubric().addAll(doc.getRubric());
	    }

		private void moveDocTransfersToFiles(List<DocTransfer> docTransfers, List<File> files, FileEnumType type) {
			for (DocTransfer docTransfer : docTransfers) {
				if (docTransfer instanceof LinkDocTransfer) {
					LinkDocTransfer linkTransfer = (LinkDocTransfer) docTransfer;
					File file = new File();
					file.setId(linkTransfer.getId());
					file.setDescription(linkTransfer.getDescription());
					file.setName(StrUtils.convertFileNameToDeloFormat(linkTransfer.getLink()));
					file.getEds().addAll(linkTransfer.getEds());
					file.setType(type);
					files.add(file);
				}
			}
			docTransfers.clear();
		}

		private void processTaskList(Header header) {
			TaskListType taskList = header.getTaskList();

			Organization recipient = header.getRecipient();
			String recipientOrganizationId = recipient == null ? "" : recipient.getId();

			filterTaskList(taskList.getTask(), recipientOrganizationId);

			DocumentType doc = header.getDocument();
			if (doc == null) {
				doc = new DocumentType();
			}

			Referred referred = createReferredToDocument(doc);
			ExpansionType expansion = (ExpansionType) header.getExpansion();
			List<File> files = expansion.getFiles();
			for (Task task : taskList.getTask()) {
				moveDocTransfersToFiles(task.getDocTransfer(), files, FileEnumType.TASK);
				updateTaskReferred(task, referred);
				mergeTaskExecutors(task);
			}
		}

		private void filterTaskList(List<Task> tasks, String recipientOrganizationId) {
			for (Iterator<Task> iterator = tasks.iterator(); iterator.hasNext();) {
				Task task = iterator.next();
				boolean isSuperfluous = true;
				executors: for (Executor taskExecutor : task.getExecutor()) {
					String id = taskExecutor.getOrganization().getId();
					if (recipientOrganizationId.equals(id)) {
						isSuperfluous = false;
						break executors;
					}
				}
				if (isSuperfluous) {
					iterator.remove();
				}
			}
		}

		private void updateTaskReferred(Task task, Referred referred) {
			List<Referred> referredList = task.getReferred();
			if (referredList.isEmpty()) {
				referredList.add(referred);
			}
		}

		private Referred createReferredToDocument(DocumentType doc) {
			Referred referred = new Referred();
			referred.setRegNumber(doc.getRegNumber());
			referred.setIdnumber(doc.getIdnumber());
			referred.setRetype(ReferredRetypeEnumType.DOCUMENT);
			return referred;
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

	    public Map<ObjectId, String> collectFiles(Header header) {
		Map<ObjectId, String> resultFiles = new HashMap<ObjectId, String>();
			ExpansionType expansion = (ExpansionType) header.getExpansion();
		List<File> files = expansion.getFiles();
		for (File file : files) {
		    String fileIdString = file.getId();
		    if (fileIdString == null || "".equals(fileIdString))
			continue;
		    ObjectId fileId = GOSTExporter.getCardId(fileIdString);
				String fileName = convertFileNameToDeloFormat(file.getName());
		    file.setName(fileName);
		    resultFiles.put(fileId, fileName);
		}
		return resultFiles;
	    }

	    private String convertFileNameToDeloFormat(String fileName) {
		final int module = 100000000;
		String t[] = fileName.split("\\.");
		if (t.length > 1)
				fileName = fileName.substring(0, fileName.length() - t[t.length - 1].length() - 1);
		NumberFormat format = NumberFormat.getIntegerInstance();
		format.setMaximumIntegerDigits(8);
		format.setMinimumIntegerDigits(8);
		format.setGroupingUsed(false);
		int hashCode = fileName.hashCode();
			return format.format(hashCode < 0 ? module + hashCode % module : hashCode % module)
			+ (t.length > 1 ? "." + t[t.length - 1] : "");
	    }

		// TODO: ��������� �� ��������� ��������� � �����������
		public Map<String, Object> getAdditions(Header header) {
			Map<String, Object> additions = new HashMap<String, Object>();
			additions.put("msgType", header.getMsgType().toString());
			if (header.getDocument() != null) {
				DocumentType doc = header.getDocument();
				RegNumber regNumber = doc.getRegNumber() == null ? new RegNumber() : doc.getRegNumber();
				additions.put("regNumber", regNumber.getValue());
				additions.put("regDate", regNumber.getRegdate() == null ? null : regNumber.getRegdate().toGregorianCalendar().getTime());
			}
			if (header.getAcknowledgement() != null) {
				AcknowledgementType ack = header.getAcknowledgement();
				boolean isSuccessfull = true;
				StringBuilder errorDescriptionBuilder = new StringBuilder("");
				List<AckResult> ackResults = ack.getAckResult();
				for (AckResult ackResult : ackResults) {
					if (!BigInteger.valueOf(0).equals(ackResult.getErrorcode())) {
						isSuccessfull = false;
						if (errorDescriptionBuilder.length() > 0) {
							errorDescriptionBuilder.append("\n");
						}
						errorDescriptionBuilder.append(ackResult.getValue());
					}
				}

				final String ackTypeKey = "ackType";
				final String errorMessageKey = "errorMessage";
				if (AckType.RECIEVED.equals(ack.getAckType())) {
					if (isSuccessfull) {
						additions.put(ackTypeKey, "Recieved");
					} else {
						additions.put(ackTypeKey, "NotRecieved");
						additions.put(errorMessageKey, errorDescriptionBuilder.toString());
					}
				} else if (AckType.REGISTERED.equals(ack.getAckType())) {
					if (isSuccessfull) {
						additions.put(ackTypeKey, "Registered");
						RegNumber ackRegNumber = ack.getRegNumber() == null ? new RegNumber() : ack.getRegNumber();
						additions.put("ackRegNumber", ackRegNumber.getValue());
						additions.put("ackRegDate", ackRegNumber.getRegdate() == null ? null : ackRegNumber.getRegdate().toGregorianCalendar().getTime());
					} else {
						additions.put(ackTypeKey, "Refused");
						additions.put(errorMessageKey, errorDescriptionBuilder.toString());
					}
				}
			}
			return additions;
		}
	}

	protected DistributionListElement getDocumentRecipient(ObjectId recipientId) throws DMSIException {
		DMSIObjectFactory objectFactory = DMSIObjectFactory.newInstance(getDataServiceBean(), "Recipient");
		return (DistributionListElement) objectFactory.newDMSIObject(recipientId);
	}

	protected OrganizationOnly getDefaultOrganization() throws DMSIException {
		OrganizationOnly defaultSenderOrganization;
		ObjectId defaultOrgId = Configuration.instance().getDefaultOrganizationId();
		DMSIObjectFactory orgFactory = DMSIObjectFactory.newInstance(getDataServiceBean(), "OrganizationOnly");
		defaultSenderOrganization = (OrganizationOnly) orgFactory.newDMSIObject(defaultOrgId);
		return defaultSenderOrganization;
	}

	protected DataServiceFacade getDataServiceBean() {
		DataServiceFacade serviceBean = new DataServiceFacade();
		serviceBean.setUser(getUser());
		serviceBean.setDatabase(getDatabase());
		serviceBean.setQueryFactory(getQueryFactory());
		return serviceBean;

	    }
	    }
