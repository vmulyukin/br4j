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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

import javax.xml.transform.Transformer;
import javax.xml.validation.Schema;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dmsi.util.ServiceUtils;
import com.aplana.medo.FormatsManager.Format;
import com.aplana.medo.MEDODocumentImporter.Config;
import com.aplana.medo.cards.CardException;
import com.aplana.medo.cards.DistributionItemCardHandler;
import com.aplana.medo.cards.ImportedDocumentCardHandler;
import com.aplana.medo.cards.NotificationCardHandler;
import com.aplana.medo.cards.TicketCardHandler;
import com.aplana.medo.converters.Converter;
import com.aplana.medo.processors.Processor;

/**
 * The <code>Importer</code> class allows to import files of IEDMS.
 */
public class Importer {

    public static final String TICKET_NAME_SUFFIX = ".ini";

    protected final Log logger = LogFactory.getLog(getClass());

    private Config config = new Config();
    private DataServiceFacade serviceBean;

    public static interface CardImporter {
	long importCard(Data data, long importedDocCardId)
		throws MedoException, DataException;
	void setDataServiceBean(DataServiceFacade serviceFacade);
    }

    public static class Data {
	protected final Log logger = LogFactory.getLog(getClass());

	private File file;
	private byte[] rawData;
	private Document doc;

	public Data(File file) {
	    this.file = file;
	}

	public File getFile() {
	    return this.file;
	}

	public byte[] getRawData() throws MedoException {
	    if (this.rawData == null) {
		this.rawData = readDataFromFile();
	    }
	    return this.rawData;
	}

	private byte[] readDataFromFile() throws MedoException {
	    try {
		return ServiceUtils.readFile(this.file);
	    } catch (IOException ex) {
		throw new MedoException(ex);
	    }
	}

	public Document getDocument() throws MedoException {
	    if (this.doc == null) {
		this.doc = createDocumentFromBytes(getRawData());
	    }
	    return this.doc;
	}

	private Document createDocumentFromBytes(byte[] dataBytes) {
	    return XmlUtils.createDOMDocument(new ByteArrayInputStream(
		    dataBytes));
	}

    }

    /**
     * Processes ticket file to update appropriate card with necessary
     * information:
     * <ol>
     * <li>Calculates date when file was last modified.</li>
     * <li>Calculates UID of card by ticket file name.</li>
     * <li>Finds 'Distribution item' card by UID.</li>
     * <li>Creates or updates 'Send info' card of found 'Distribution item"
     * card.</li>
     * </ol>
     *
     * @param file -
     *                ticket file
     * @throws CardException
     * @see #decodeTicketId(String)
     * @see DistributionItemCardHandler#DistributionItemCardHandler(UUID)
     *
     *
     */
    public void importTicket(File file) throws CardException {
	Date lastModifiedDate = new Date(file.lastModified());
	UUID uid = decodeTicketId(file.getName());

	long ticketCardId = new TicketCardHandler(uid, lastModifiedDate)
		.createCard();

	if (ticketCardId == -1) {
	    logger.error("Ticket card was not created.");
	    throw new CardException();
	}

	long cardId = new DistributionItemCardHandler(uid).getCardId();
	if (cardId != -1) {
	    DistributionItemCardHandler cardHandler = new DistributionItemCardHandler(
		    cardId);
	    cardHandler.appendNotification(ticketCardId);
	    cardHandler.appendTicketToParentDoc(ticketCardId);

	} else {
	    cardId = new NotificationCardHandler(uid).getCardId();
	    if (cardId != -1) {
		NotificationCardHandler cardHandler = new NotificationCardHandler(
			cardId);
		cardHandler.linkTicket(ticketCardId);
		cardHandler.linkParentDocToTicket(ticketCardId);
	    }
	}
	// Necessary to perform processors
	new TicketCardHandler(ticketCardId).saveCard();
    }

    /**
     * Converts file name of ticket file to UUID.
     *
     * @param value -
     *                ticket file name
     * @return UUID instance
     */
    // Modified according to new requirements: filename has UUID format now
    private UUID decodeTicketId(String value) {
	StringBuilder uidBuilder = new StringBuilder(value);
	uidBuilder.delete(uidBuilder.length() - TICKET_NAME_SUFFIX.length(),
		uidBuilder.length());
	return UUID.fromString(uidBuilder.toString());
    }

    /**
     * Processes XML file in IEDMS format to convert and add it to system:
     * <ol>
     * <li>Validate source XML by schema</li>
     * <li>Transforms source XML in IEDMS format with XSL transformation file.</li>
     * <li>Checks whether cardId element is present and fill 'id' XML attribute
     * of 'card' element according to it. Appropriate converter is used before
     * it.</li>
     * <li>Performs processing of transformed file using defined converters.</li>
     * <li>Performs post-processing of converted file using defined processor.
     * Attribute of 'card' element with name equals to
     * {@link #PROCESSOR_ATTRIBUTE_NAME} define it.
     * {@link #DEFAULT_PROCESSOR_NAME}</li>
     * is used as default value.
     * </ol>
     *
     * @param file
     *                XML file in IEDMS format that should be processed
     * @return id of created card
     * @throws MedoException
     * @see Processor
     * @see Converter
     */
    public void importXml(File file) throws Exception {
	long importedDocCardId = -1;
	try {
	    Data data = new Data(file);

	    // Create 'Imported document' card
	    importedDocCardId = new ImportedDocumentCardHandler(
		    new ByteArrayInputStream(data.getRawData()), file.getName())
		    .createCard();

	    if (importedDocCardId == -1)
		throw new MedoException(
			"jbr.medo.importer.importedDocumentNotCreated");
	    long cardId = -1;

	    CardImporter cardImporter = createImporter(data);
	    cardImporter.setDataServiceBean(serviceBean);
	    cardId = cardImporter.importCard(data, importedDocCardId);

	    if (cardId == -1) {
	    	throw new MedoException("jbr.medo.importer.notImported");
	    }
	    new ImportedDocumentCardHandler(importedDocCardId).setProcessed();
	} catch (Exception ex) {
	    logger.error("Error during import is occurred", ex);
	    if (importedDocCardId == -1) {
		throw new MedoException(
			"Imported document card was not created");
	    }
	    try {
		new ImportedDocumentCardHandler(importedDocCardId).setFailed(ex
			.getLocalizedMessage());
	    } catch (CardException e) {
		throw new MedoException(
			"Error during updating of Imported document card");
	    }
	    throw ex;
	}
    }

    private CardImporter createImporter(Data data) throws MedoException {
	Format medoType = FormatsManager.instance().resolve(data);
	switch (medoType) {
	case MEDO:
	    MEDODocumentImporter medoImporter = new MEDODocumentImporter();
	    medoImporter.setConfig(config);
	    return medoImporter;
	case MEDO_OG:
	    return new OGRequestCardImporter();
	case OG_DOC:
	    return new OGDocumentCardImporter();
	default:
	    throw new MedoException("jbr.medo.import.unknownFormat");
	}

	    }

    public Schema getSchema() {
	return config.getSchema();
    }

    public void setSchema(Schema schema) {
	config.setSchema(schema);
    }

    public Transformer getTransformer() {
	return config.getTransformer();
    }

    public void setTransformer(Transformer transformer) {
	config.setTransformer(transformer);
    }

    public Transformer getImportedDocTransformer() {
	return config.getImportedDocTransformer();
    }

    public void setImportedDocTransformer(Transformer importedDocTransformer) {
	config.setImportedDocTransformer(importedDocTransformer);
    }

    public Properties getProperties() {
	return config.getProperties();
    }

    public void setProperties(Properties properties) {
	config.setProperties(properties);
    }

    public DataServiceFacade getServiceBean() {
        return this.serviceBean;
}

    public void setServiceBean(DataServiceFacade serviceBean) {
        this.serviceBean = serviceBean;
    }
}
