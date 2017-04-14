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
/**
 *
 */
package com.aplana.dbmi.card.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class ReportXMLEditor {

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
	    "yyyy-MM-dd'T'HH:mm:ss");

    protected static final String REPORT_ELEMENT = "report";
    protected static final String PART_ELEMENT = "part";
    protected static final String ROUND_ATTRIBUTE = "round";
    protected static final String FACT_USER_ATTRIBUTE = "fact-user";
    protected static final String TIMESTAMP_ATTRIBUTE = "timestamp";
    protected static final String COLUMNS_ELEMENT = "columns";
    protected static final String COLUMN_ELEMENT = "column";

    protected Document document;

    private Log logger = LogFactory.getLog(getClass());

    private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    private static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
    private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

    private static final ErrorHandler saxHandler = new ErrorHandler() {
	public void error(SAXParseException e) throws SAXException {
	    throw e;
	}

	public void fatalError(SAXParseException e) throws SAXException {
	    throw e;
	}

	public void warning(SAXParseException arg0) throws SAXException {
	}
    };

    public Document getDocument() {
	return document;
    }

    public ReportXMLEditor(String text, File schema) {
	DocumentBuilder builder;
	try {
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    builder = dbf.newDocumentBuilder();
	    if (schema != null) {
		dbf.setNamespaceAware(true);
		dbf.setValidating(true);
		dbf.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
		dbf.setAttribute(JAXP_SCHEMA_SOURCE, schema);
	    }
	} catch (ParserConfigurationException ex) {
	    throw new IllegalStateException(
		    "Unable to create DOM document builder.", ex);
	}
	if (text != null && !"".equals(text)) {
	    try {
		builder.setErrorHandler(saxHandler);
		document = builder.parse(new ByteArrayInputStream(text
			.getBytes("UTF-8")));
		return;
	    } catch (SAXException e) {
		logger.warn("Exception while parsing XML from " + text, e);
	    } catch (UnsupportedEncodingException ex) {
		throw new IllegalStateException(ex);
	    } catch (IOException ex) {
		throw new IllegalStateException(ex);
	    }
	}
	document = builder.newDocument();
	Element report = document.createElement(REPORT_ELEMENT);
	document.appendChild(report);
	appendPart("", null, "", text);
    }

    public void addColumns(List<String> columnNames) {
	Element columnsElement = document.createElement(COLUMNS_ELEMENT);
	for (String columnName : columnNames) {
	    Element columnElement = document.createElement(COLUMN_ELEMENT);
	    columnElement.setTextContent(columnName);
	    columnsElement.appendChild(columnElement);
	}
	document.getDocumentElement().appendChild(columnsElement);
    }

    public void deleteColumns() {
	XPathExpression columnsExpression;
	try {
	    columnsExpression = XPathFactory.newInstance().newXPath().compile(
		    String.format("/%s/%s", REPORT_ELEMENT, COLUMNS_ELEMENT));
	    Element columnsElement = (Element) columnsExpression.evaluate(
		    document, XPathConstants.NODE);
	    document.getDocumentElement().removeChild(columnsElement);
	} catch (XPathExpressionException ex) {
	    throw new IllegalStateException(ex);
	}
    }

    public void appendPart(String round, Date date, String person, String text) {
	Element part = document.createElement(PART_ELEMENT);
	part.setAttribute(ROUND_ATTRIBUTE, round);
	part.setAttribute(TIMESTAMP_ATTRIBUTE, date == null ? "-" : DATE_FORMAT
		.format(date));
	part.setAttribute(FACT_USER_ATTRIBUTE, person);
	part.setTextContent(trimAndNewlineRight(text));
	document.getDocumentElement().appendChild(part);
    }

    public Element extractLastPart() {
	NodeList parts = document.getDocumentElement().getElementsByTagName(
		PART_ELEMENT);
	Element lastPart;
	if (parts.getLength() > 0) {
	    lastPart = (Element) parts.item(parts.getLength() - 1);
	    document.getDocumentElement().removeChild(lastPart);
	} else {
	    lastPart = document.createElement(PART_ELEMENT);
	}
	return lastPart;
    }

    public String serialize() {
	StringWriter stw = new StringWriter();
	try {
	    Transformer serializer = TransformerFactory.newInstance()
		    .newTransformer();
	    serializer
		    .transform(new DOMSource(document), new StreamResult(stw));
	    return stw.toString();
	} catch (TransformerException ex) {
	    logger.error("Error during report serialization", ex);
	    return "";
	}
    }

    protected static String trimAndNewlineRight(String input) {
	StringBuilder sb = new StringBuilder();
	sb.append(input);
	int len = input.length();
	for (int i = len - 1; i >= 0; i--) {
	    char c = sb.charAt(i);
	    if (!Character.isWhitespace(c)) {
		if (i < len - 1)
		    sb.replace(i + 1, len, "\n");
		return sb.toString();
	    }
	}
	return "";
    }
}
