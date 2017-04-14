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
package com.aplana.medo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dmsi.DMSIException;
import com.aplana.dmsi.card.CardHandler;
import com.aplana.dmsi.types.common.Area;
import com.aplana.dmsi.types.common.Country;
import com.aplana.dmsi.types.common.Organization;
import com.aplana.dmsi.types.common.ReceiptSource;
import com.aplana.dmsi.types.common.Region;
import com.aplana.dmsi.types.common.RequestAuthor;
import com.aplana.dmsi.types.common.Town;
import com.aplana.dmsi.util.RubricUtils;
import com.aplana.dmsi.util.ServiceUtils;
import com.aplana.medo.Importer.CardImporter;
import com.aplana.medo.Importer.Data;
import com.aplana.medo.types.ImportedDocument;
import com.aplana.medo.types.OGDocument;
import com.aplana.medo.types.cardInfo.Address;
import com.aplana.medo.types.cardInfo.AddressExt;
import com.aplana.medo.types.cardInfo.XMLCardInfo;
import com.aplana.medo.types.document.AssociatedFile;
import com.aplana.medo.types.document.Communication;
import com.aplana.medo.types.document.CommunicationPartner;
import com.aplana.medo.types.document.DocumentNumber;
import com.aplana.medo.types.document.Communication.Files;
import com.aplana.medo.types.document.Communication.Header;

public class OGDocumentCardImporter implements CardImporter {

    private static final String CARDINFO_FILENAME_PART = "CardInfo";
    private static final String MEDO_CARDINFO_PACKAGE = "com.aplana.medo.types.cardInfo";
    private static final String MEDO_COMMUNICATION_PACKAGE = "com.aplana.medo.types.document";
    private static Log logger = LogFactory.getLog(OGDocumentCardImporter.class);
    private DataServiceFacade serviceBean;

    public long importCard(Data data, long importedDocCardId)
	    throws MedoException, DataException {
	try {
	    final File workingFolder = data.getFile().getParentFile();
	    Communication communication = parseCommunication(data.getDocument());
	    Files files = communication.getFiles();
	    @SuppressWarnings("unchecked")
	    List<AssociatedFile> associatedFiles = files == null ? Collections.EMPTY_LIST
		    : files.getFile();
	    XMLCardInfo cardInfo = extractCardInfo(workingFolder,
		    associatedFiles);
	    RequestAuthor author = createAuthorFromCardInfo(cardInfo);
	    OGDocument ogDocument = new OGDocument();
	    ogDocument.setAuthor(author);
	    ogDocument.getFiles().addAll(
		    readAttachments(workingFolder, associatedFiles));
	    com.aplana.medo.types.document.Document doc = getCommunicationDocument(communication);
	    ogDocument.setAnnotation(doc.getAnnotation());
	    ReceiptSource receiptSource = createReceiptSource(communication);
	    ogDocument.setReceiptSource(receiptSource);
	    ogDocument.setCorrespondent(receiptSource.getCorrespondent());
	    ogDocument.setRegNumber(receiptSource.getRegNumber());
	    ogDocument.setRegDate(receiptSource.getRegDate());
	    for (com.aplana.medo.types.cardInfo.Rubric rubric : cardInfo
		    .getRubricList()) {
		String rubricCode = rubric.getRubricCode();
		com.aplana.dmsi.types.common.Rubric docRubric;
		if (RubricUtils.isRubric(rubricCode)) {
		    docRubric = new com.aplana.dmsi.types.common.Rubric();
		    docRubric.setNumber(rubricCode);
		} else if (RubricUtils.isOldStyleRubric(rubricCode)) {
		    docRubric = new com.aplana.dmsi.types.common.Rubric();
		    docRubric.setCode(rubricCode);
		} else if (RubricUtils.isSubRubric(rubricCode)) {
		    docRubric = new com.aplana.dmsi.types.common.SubRubric();
		    docRubric.setNumber(rubricCode);
		} else {
		    throw new DMSIException("Incorrect format of rubric code: "
			    + rubricCode);
		}
		ogDocument.getRubric().add(docRubric);
	    }

		if (doc.getAddressees().getAddressee().get(0) != null && doc.getAddressees().getAddressee().get(0).getOrganization() != null) {
			Organization recipientOrg = new Organization(doc.getAddressees().getAddressee().get(0).getOrganization().getValue());
			ogDocument.setReceiver(recipientOrg);
		} else {
			logger.error("Unable to setup document receiver");
		}

	    ImportedDocument importedDocument = new ImportedDocument();
	    importedDocument.setId(String.valueOf(importedDocCardId));
	    importedDocument.setUid(doc.getUid());
	    ogDocument.setImportedDocument(importedDocument);
	    CardHandler cardHandler = new CardHandler(serviceBean);
	    cardHandler.updateCard(importedDocument);
	    return (Long) cardHandler.createCard(ogDocument).getId();
	} catch (JAXBException ex) {
	    throw new MedoException("jbr.medo.system", ex);
	} catch (DMSIException ex) {
	    throw new MedoException("jbr.medo.objectToCardError", ex);
	} catch (Exception ex) {
	    throw new MedoException("jbr.medo.system", ex);
	}
    }

    private Communication parseCommunication(Document document)
	    throws JAXBException {
	JAXBContext context = JAXBContext
		.newInstance(MEDO_COMMUNICATION_PACKAGE);
	Unmarshaller um = context.createUnmarshaller();
	Communication communication = (Communication) um.unmarshal(document);
	return communication;
    }

    private XMLCardInfo extractCardInfo(final File workingFolder,
	    List<AssociatedFile> filesCollection) throws FileNotFoundException,
	    JAXBException, IOException {
	XMLCardInfo cardInfo = null;
	for (Iterator<AssociatedFile> iter = filesCollection.iterator(); iter
		.hasNext();) {
	    AssociatedFile attachmentFile = iter.next();
	    String localName = attachmentFile.getLocalName();
	    if (isFileCardInfo(localName)) {
		iter.remove();
		InputStream cardInfoStream = null;
		try {
		    File cardInfoFile = new File(workingFolder, localName);
		    cardInfoStream = new FileInputStream(cardInfoFile);
		    JAXBContext cardInfoContext = JAXBContext
			    .newInstance(MEDO_CARDINFO_PACKAGE);
		    Unmarshaller cardInfoUnmarshaller = cardInfoContext
			    .createUnmarshaller();
		    cardInfo = (XMLCardInfo) cardInfoUnmarshaller
			    .unmarshal(cardInfoStream);
		} finally {
		    IOUtils.closeQuietly(cardInfoStream);
		}
		break;
	    }
	}
	return cardInfo;
    }

    private boolean isFileCardInfo(String localName) {
	return localName != null && localName.contains(CARDINFO_FILENAME_PART);
    }

    private RequestAuthor createAuthorFromCardInfo(XMLCardInfo cardInfo) {
	if (cardInfo == null)
	    return null;
	RequestAuthor author;
	author = new RequestAuthor();
	author.setFirstName(cardInfo.getFirstName());
	author.setMidleName(cardInfo.getMiddleName());
	author.setLastName(cardInfo.getLastName());
	author.setZipCode(cardInfo.getZipCode() == null ? null : cardInfo
		.getZipCode().toString());
	Address address = cardInfo.getAddress();
	if (address != null) {
	    author.setCountry(new Country(address.getCountry()));
	    author.setArea(new Area(address.getArea()));
	    author.setRegion(new Region(address.getRegion()));
	    author.setTown(new Town(address.getTown()));
	}
	AddressExt addressExt = cardInfo.getAddressExt();
	if (addressExt != null) {
	    author.setStreet(formatStreetName(addressExt));
	    author.setBuilding(addressExt.getBuilding());
	    author.setComment(addressExt.getComment());
	    author.setHouse(addressExt.getHouse());
	    BigInteger flatNumber = null;
	    try {
		flatNumber = BigInteger.valueOf(Long.parseLong(addressExt.getFlat()));
	    } catch (NumberFormatException ex) {
		logger.warn("It is impossible to parse flat number from value "
			+ addressExt.getFlat());
	    }
	    author.setFlat(flatNumber);
	}
	author.setEmail(cardInfo.getEmail());
	return author;
    }

    private String formatStreetName(AddressExt addressExt) {
	StringBuilder streetBuilder = new StringBuilder();
	if (addressExt.getStreetType() != null) {
	    streetBuilder.append(addressExt.getStreetType());
	}
	if (streetBuilder.length() > 0) {
	    streetBuilder.append(" ");
	}
	if (addressExt.getStreetName() != null) {
	    streetBuilder.append(addressExt.getStreetName());
	}
	if (streetBuilder.length() > 0) {
	    return streetBuilder.toString();
	}
	return null;
    }

    private Collection<com.aplana.dmsi.types.common.File> readAttachments(
	    final File workingFolder, List<AssociatedFile> filesCollection)
	    throws IOException {
	Collection<com.aplana.dmsi.types.common.File> readedFiles = new ArrayList<com.aplana.dmsi.types.common.File>(
		filesCollection.size());
	for (AssociatedFile associatedFile : filesCollection) {
	    String fileName = associatedFile.getLocalName();
	    File attachmentFile = new File(workingFolder, fileName);
	    byte[] attachmentData = ServiceUtils.readFile(attachmentFile);
	    com.aplana.dmsi.types.common.File attachment = new com.aplana.dmsi.types.common.File();
	    attachment.setFileName(fileName);
	    attachment.setImage(attachmentData);
	    readedFiles.add(attachment);
	}
	return readedFiles;
    }

    private com.aplana.medo.types.document.Document getCommunicationDocument(
	    Communication communication) {
	com.aplana.medo.types.document.Document doc = communication
		.getDocument() == null ? new com.aplana.medo.types.document.Document()
		: communication.getDocument();
	return doc;
    }

    private ReceiptSource createReceiptSource(Communication communication) {
	com.aplana.medo.types.document.Document doc = getCommunicationDocument(communication);
	ReceiptSource receiptSource = new ReceiptSource();
	DocumentNumber docNum = doc.getNum() == null ? new DocumentNumber()
		: doc.getNum();
	receiptSource.setRegDate(docNum.getDate().getValue());
	receiptSource.setRegNumber(docNum.getNumber());
	Header header = communication.getHeader() == null ? new Header()
		: communication.getHeader();
	CommunicationPartner source = header.getSource() == null ? new CommunicationPartner()
		: header.getSource();
	receiptSource.setCorrespondent(new Organization(source
		.getOrganization()));
	return receiptSource;
    }
    public void setDataServiceBean(DataServiceFacade serviceFacade) {
	this.serviceBean = serviceFacade;
}

}
