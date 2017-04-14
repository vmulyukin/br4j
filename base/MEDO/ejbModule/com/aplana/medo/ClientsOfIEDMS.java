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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * 
 * @version alpha
 */
public class ClientsOfIEDMS {
    private static ClientsOfIEDMS instance = null;

    private Document document;

    private static final String BY_NAME_FORMAT = "//dictionary/clients/entry[starts-with(value,'%s')]";
    private static final String BY_UID_FORMAT = "//dictionary/clients/entry[uniqueId='%s']";

    private Log logger = LogFactory.getLog(getClass());

    public static ClientsOfIEDMS instance() {
	if (instance == null) {
	    instance = new ClientsOfIEDMS();
	}
	return instance;
    }

    private ClientsOfIEDMS() {
	try {
	    DocumentBuilder builder = DocumentBuilderFactory.newInstance()
		    .newDocumentBuilder();
	    String path = MedoConfiguration.instance().getValue(
		    MedoConfiguration.CLIENTS_FILE);
	    File templateFile = new File(path);
	    document = builder.parse(templateFile);
	} catch (ParserConfigurationException ex) {
	    throw new IllegalStateException("Unable to create DOM document", ex);
	} catch (MedoConfigurationException ex) {
	    throw new IllegalStateException("Configuration is incorrect", ex);
	} catch (SAXException ex) {
	    throw new IllegalStateException("Incorrect template file", ex);
	} catch (IOException ex) {
	    throw new IllegalStateException("Unable to read template file", ex);
	}
    }

    public class Organization {
	private String name;
	private String value;
	private String uid;
	private String mail;

	/**
	 * @param name
	 * @param uid
	 * @param mail
	 */
	public Organization(String name, String value, String uid, String mail) {
	    this.name = name;
	    this.value = value;
	    this.uid = uid;
	    this.mail = mail;
	}

	/**
	 * @return the name
	 */
	public String getName() {
	    return this.name;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
	    return this.value;
	}

	/**
	 * @return the uid
	 */
	public String getUid() {
	    return this.uid;
	}

	/**
	 * @return the mail
	 */
	public String getMail() {
	    return this.mail;
	}
    }

    public Organization findByUID(String uid) {
	XPath xPath = XPathFactory.newInstance().newXPath();
	Object foundNode = null;
	try {
	    foundNode = xPath.evaluate(String.format(BY_UID_FORMAT, uid
		    .toUpperCase()), document, XPathConstants.NODE);
	} catch (XPathExpressionException ex) {
	    logger.error("Error during searching by UID", ex);
	}
	if (foundNode == null) {
	    return new Organization("", "", uid, "");
	}
	return createOrganizationFromNode((Element) foundNode);
    }

    public Organization findByValue(String value) {
	XPath xPath = XPathFactory.newInstance().newXPath();
	Object foundNode = null;
	try {
	    foundNode = xPath.evaluate(String.format(BY_NAME_FORMAT, value),
		    document, XPathConstants.NODE);
	} catch (XPathExpressionException ex) {
	    logger.error("Error during searching by value", ex);
	}
	if (foundNode == null) {
	    return new Organization("", value, "", "");
	}
	return createOrganizationFromNode((Element) foundNode);
    }

    public Organization findByName(String name) {
	return findByValue(getValueOfName(name));
    }

    private Organization createOrganizationFromNode(Element element) {
	String value = "";
	String uid = "";
	String mail = "";
	NodeList valueList = element.getElementsByTagName("xdms:value");
	if (valueList.getLength() > 0) {
	    value = valueList.item(0).getTextContent();
	}
	NodeList uidList = element.getElementsByTagName("xdms:uniqueId");
	if (uidList.getLength() > 0) {
	    uid = uidList.item(0).getTextContent();
	}
	NodeList mailList = element.getElementsByTagName("xdms:mail");
	if (mailList.getLength() > 0) {
	    mail = mailList.item(0).getTextContent();
	}
	return new Organization(getNameOfValue(value), value, uid, mail);
    }

    private String getNameOfValue(String value) {
	return value;
    }

    private String getValueOfName(String name) {
	return name;
    }
}
