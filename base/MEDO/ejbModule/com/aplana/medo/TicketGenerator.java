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
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class TicketGenerator {

    private static final String GROUP_ELEMENT = "group";
    private static final String SETTING_ELEMENT = "setting";
    private static final String NAME_ATTRIBUTE = "name";

    private static final String SUBJECT_CODE = "subject";
    private static final String AUTOSEND_CODE = "autosend";
    private static final String ENCRYPTION_CODE = "encryption";
    private static final String SIGNATURE_CODE = "signature";
    private static final String SENT_CODE = "sent";
    private static final String READ_CODE = "read";
    private static final String DATE_CODE = "date";
    private static final String ADDRESSES_CODE = "addressees";
    private static final String TEXT_FILE_CODE = "file";
    private static final String FILES_CODE = "files";

    private int addresseeCount = 0;
    private int filesCount = 0;

    private Document doc;
    private XPath xpath;

    public TicketGenerator() {
	try {
	    DocumentBuilder builder = DocumentBuilderFactory.newInstance()
		    .newDocumentBuilder();
	    String path = MedoConfiguration.instance().getValue(
		    MedoConfiguration.TICKET_TEMPLATE);
	    File templateFile = new File(path);
	    doc = builder.parse(templateFile);
	} catch (ParserConfigurationException ex) {
	    throw new IllegalStateException("Unable to create DOM document", ex);
	} catch (MedoConfigurationException ex) {
	    throw new IllegalStateException("Configuration is incorrect", ex);
	} catch (SAXException ex) {
	    throw new IllegalStateException("Incorrect template file", ex);
	} catch (IOException ex) {
	    throw new IllegalStateException("Unable to read template file", ex);
	}
	xpath = XPathFactory.newInstance().newXPath();
    }

    public void setSubject(String subject) {
	getSetting(SUBJECT_CODE).setTextContent(subject);
    }

    public void setAutosend(boolean isEnable) {
	getSetting(AUTOSEND_CODE).setTextContent(
		String.valueOf(isEnable ? 1 : 0));
    }

    public void setEncrypting(boolean isEnable) {
	getSetting(ENCRYPTION_CODE).setTextContent(
		String.valueOf(isEnable ? 1 : 0));
    }

    public void setSignature(boolean isEnable) {
	getSetting(SIGNATURE_CODE).setTextContent(
		String.valueOf(isEnable ? 1 : 0));
    }

    public void setSent(boolean isEnable) {
	getSetting(SENT_CODE).setTextContent(String.valueOf(isEnable ? 1 : 0));
    }

    public void setRead(boolean isEnable) {
	getSetting(READ_CODE).setTextContent(String.valueOf(isEnable ? 1 : 0));
    }

    private void setNowDate() {
	DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	getSetting(DATE_CODE).setTextContent(dateFormat.format(new Date()));
    }

    public void addAddressee(String addressee) {
	Element addresseeElement = doc.createElement(SETTING_ELEMENT);
	addresseeElement.setAttribute(NAME_ATTRIBUTE, String
		.valueOf(addresseeCount++));
	addresseeElement.setTextContent(addressee);
	getGroup(ADDRESSES_CODE).appendChild(addresseeElement);
    }

    public void addFile(String file) {
	Element fileElement = doc.createElement(SETTING_ELEMENT);
	fileElement.setAttribute(NAME_ATTRIBUTE, String.valueOf(filesCount++));
	fileElement.setTextContent(file);
	getGroup(FILES_CODE).appendChild(fileElement);
    }

    public void setTextFile(String file) {
	getSetting(TEXT_FILE_CODE).setTextContent(file);
    }

    private Element getSetting(String code) {
	try {
	    return (Element) xpath.evaluate(String
		    .format("//%s/%s[@code='%s']", GROUP_ELEMENT,
			    SETTING_ELEMENT, code), doc, XPathConstants.NODE);
	} catch (XPathExpressionException ex) {
	    throw new IllegalStateException(ex);
	}
    }

    private Element getGroup(String code) {
	try {
	    return (Element) xpath.evaluate(String.format(
		    "//group[@code='%s']", code), doc, XPathConstants.NODE);
	} catch (XPathExpressionException ex) {
	    throw new IllegalStateException(ex);
	}
    }

    public void serialize(OutputStream out) throws IOException {
	setNowDate();
	NodeList groups = doc.getDocumentElement().getElementsByTagName(
		GROUP_ELEMENT);
	for (int i = 0; i < groups.getLength(); i++) {
	    Element group = (Element) groups.item(i);
	    out.write(String.format("[%s]\n",
		    group.getAttribute(NAME_ATTRIBUTE)).getBytes());
	    NodeList settings = group.getElementsByTagName(SETTING_ELEMENT);
	    for (int j = 0; j < settings.getLength(); j++) {
		Element setting = (Element) settings.item(j);
		String value = setting.getTextContent();
		if (!"".equals(value)) {
		    out.write(String.format("%s=%s\n",
			    setting.getAttribute(NAME_ATTRIBUTE), value)
			    .getBytes());
		}
	    }
	}
    }
    
    public void serializeWin1251(OutputStream os) throws IOException {
	OutputStreamWriter out = new OutputStreamWriter(os, Charset.forName("Cp1251"));
	setNowDate();
	NodeList groups = doc.getDocumentElement().getElementsByTagName(
		GROUP_ELEMENT);
	for (int i = 0; i < groups.getLength(); i++) {
	    Element group = (Element) groups.item(i);
	    out.write(String.format("[%s]\n",
		    group.getAttribute(NAME_ATTRIBUTE)));
	    NodeList settings = group.getElementsByTagName(SETTING_ELEMENT);
	    for (int j = 0; j < settings.getLength(); j++) {
		Element setting = (Element) settings.item(j);
		String value = setting.getTextContent();
		if (!"".equals(value)) {
		    out.write(String.format("%s=%s\n",
			    setting.getAttribute(NAME_ATTRIBUTE), value));
		}
	    }
	}
	out.close();
    }
}
