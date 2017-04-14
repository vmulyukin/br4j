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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class is a client for CardImportService (see appropriate project to
 * details)
 */
public class CardImportClient {

    protected final static Log logger = LogFactory
	    .getLog(CardImportClient.class);

    /** URI of card import service */
    private final static String ENDPOINT = "http://localhost:8080/CardImportService/cardImport?wsdl";

    /**
     * Sends SOAP request to the import service. If <code>file</code> is null,
     * method call is same as callImportCardService(bytes, new byte[]{}, ""). If
     * input is not null new FileInputStream is created and
     * {@link #callImportCardService(byte[],InputStream, String)} method is
     * called.
     * 
     * @param bytes -
     *                source XML file
     * @param input -
     *                included file if necessary or null in other case
     * @return id of created card read from response or -1 in case of error
     */
    public static long callImportCardService(byte[] bytes, File file) {
	if (file == null)
	    return callImportCardService(bytes, new byte[] {}, "");
	InputStream stream = null;
	try {
	    stream = new FileInputStream(file);
	    return callImportCardService(bytes, stream, file.getName());
	} catch (FileNotFoundException ex) {
	    logger.error("Error during import of file: " + file.getName(), ex);
	} finally {
	    if (stream != null) {
		try {
		    stream.close();
		} catch (IOException ex) {
		    logger.warn("Error during file input stream closing for: "
			    + file.getName(), ex);
		}
	    }
	}
	return -1;
    }

    /**
     * Sends SOAP request to the import service. If <code>input</code> is
     * null, method call is same as callImportCardService(bytes, new byte[]{},
     * ""). If <code>input</code> is not null bytes are read from it and
     * {@link #callImportCardService(byte[], byte[], String)} method is called.
     * 
     * @param bytes -
     *                source XML file
     * @param input -
     *                input stream with included file if necessary or null in
     *                other case
     * @param fileName -
     *                file name of included file if necessary
     * @return id of created card read from response or -1 in case of error
     */
    public static long callImportCardService(byte[] bytes, InputStream input,
	    String fileName) {
	if (input == null) {
	    return callImportCardService(bytes, new byte[] {}, "");
	}
	byte[] fileBytes = null;
	try {
	    ByteArrayOutputStream bout = new ByteArrayOutputStream();
	    int available;
	    while ((available = input.available()) != 0) {
		byte[] buffer = new byte[available];
		input.read(buffer);
		bout.write(buffer);
	    }
	    fileBytes = bout.toByteArray();
	    return callImportCardService(bytes, fileBytes, fileName);
	} catch (IOException ex) {
	    logger.error("Error during reading from stream", ex);
	}
	return -1;
    }

    /**
     * Method sends SOAP request to the service. Main parameters for this
     * request are:
     * <ul>
     * <li>xmlInString - source XML coded to base64. This XML is represented by
     * <code>bytes</code> parameter of method </li>
     * <li>inFileName - if card should contain included file (file document)</li>
     * <li>inFileBase64 - coded in base64 included in document file (see
     * inFileName)</li>
     * </ul>
     * <p>
     * The 'inFileName' and 'inFileBase64' parameters are send in case if File
     * is not null.
     * </p>
     * <p>
     * If web service has finished unsuccessfully, fault message in response is
     * parsed and is logged using default logger. The following fields are
     * logged: fault code, fault string, detail entries if they appears.
     * </p>
     * <p>
     * If web service has finished successfully, response is parsed and is
     * logged. Response should contain 'cardId' element under the
     * 'importCardFromXMLResponse' one.
     * </p>
     * 
     * @param bytes -
     *                source XML file
     * @param fileBytes -
     *                bytes of included file if necessary or empty array in
     *                other case
     * @param fileName -
     *                file name of included file if necessary
     * @return id of created card read from response or -1 in case of error
     */
    public static long callImportCardService(byte[] bytes, byte[] fileBytes,
	    String fileName) {

	String codedValue = Base64.byteArrayToBase64(bytes);

	SOAPConnection connection = null;
	long cardId = -1;
	try {
	    SOAPConnectionFactory factory = SOAPConnectionFactory.newInstance();
	    connection = factory.createConnection();

	    // Request message creation
	    MessageFactory messageFactory = MessageFactory.newInstance();
	    SOAPMessage message = messageFactory.createMessage();
	    SOAPBody body = message.getSOAPBody();

	    SOAPBodyElement bodyElement = body.addBodyElement(new QName(
		    "http://aplana.com/dbmi/ws/CardImportService",
		    "importCardFromXML", "m"));
	    bodyElement.addChildElement(new QName("encoding")).addTextNode(
		    "UTF-8");
	    bodyElement.addChildElement(new QName("xmlInString")).addTextNode(
		    codedValue);

	    // If file should be included, read it, code to base64 and add to
	    // request
	    if (fileBytes.length > 0) {
		String codedFile = Base64.byteArrayToBase64(fileBytes);
		bodyElement.addChildElement(new QName("inFileName"))
			.addTextNode(fileName);
		bodyElement.addChildElement(new QName("inFileBase64"))
			.addTextNode(codedFile);
	    }

	    SOAPMessage response = connection.call(message, ENDPOINT);

	    // Response message handling

	    body = response.getSOAPBody();
	    // In case of error log the sent fault message
	    if (body.hasFault()) {
		SOAPFault newFault = body.getFault();
		QName code = newFault.getFaultCodeAsQName();
		String string = newFault.getFaultString();

		StringBuilder errorMessage = new StringBuilder();
		errorMessage.append(String.format("Fault code: %s\n", code
			.toString()));
		errorMessage
			.append(String.format("Fault string: %s\n", string));

		Detail newDetail = newFault.getDetail();
		if (newDetail != null) {
		    @SuppressWarnings("unchecked")
		    Iterator<DetailEntry> entries = newDetail
			    .getDetailEntries();
		    while (entries.hasNext()) {
			DetailEntry newEntry = entries.next();
			errorMessage.append(String.format("Detail entry: %s\n",
				newEntry.getValue()));
		    }
		}
		logger.error("Service returned fault report:\n"
			+ errorMessage.toString());
		return -1;
	    }
	    // Else extract info about result: cardId - and log it
	    @SuppressWarnings("unchecked")
	    Iterator<SOAPBodyElement> bodyIterator = body
		    .getChildElements(new QName(
			    "http://aplana.com/dbmi/ws/CardImportService",
			    "importCardFromXMLResponse", "m"));

	    if (bodyIterator.hasNext()) {
		SOAPBodyElement responseElement = bodyIterator.next();

		@SuppressWarnings("unchecked")
		Iterator<SOAPElement> cardIdsIterator = responseElement
			.getChildElements(new QName("cardId"));

		if (cardIdsIterator.hasNext()) {
		    String result = cardIdsIterator.next().getValue();
		    try {
			cardId = Long.parseLong(result);
		    } catch (NumberFormatException ex) {
			logger.warn("Incorrect format of returned cardId", ex);
		    }
		    logger.info("Card was imported with cardId: " + cardId);
		}
	    }
	} catch (SOAPException ex) {
	    logger.error("Error during import card service calling", ex);
	    return -1;
	} finally {
	    try {
		if (connection != null)
		    connection.close();
	    } catch (SOAPException e) {
		logger.warn("Error during soap connection close", e);
	    }
	}
	return cardId;
    }

}
