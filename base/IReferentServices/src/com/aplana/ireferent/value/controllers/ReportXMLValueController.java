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
package com.aplana.ireferent.value.controllers;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.aplana.ireferent.IReferentException;
import com.aplana.ireferent.util.XMLException;
import com.aplana.ireferent.util.XmlUtils;

public class ReportXMLValueController implements ValueController {

    protected static final String REPORT_ELEMENT = "report";
    protected static final String PART_ELEMENT = "part";
    protected static final String ROUND_ATTRIBUTE = "round";
    protected static final String FACT_USER_ATTRIBUTE = "fact-user";
    protected static final String TIMESTAMP_ATTRIBUTE = "timestamp";

    protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
	    "yyyy-MM-dd'T'HH:mm:ss");

    private Document document;
    private Log logger = LogFactory.getLog(getClass());

    public ReportXMLValueController() {
    }

    protected Document getDocument() {
	return document;
    }

    public void setValue(Object value) throws IReferentException {
	document = null;
	String text = (String) value;
	if (text != null && !"".equals(text)) {
	    document = parseDocument(text);
	}
	if (document == null) {
	    document = createNewDocument();
	}
    }

    private Document createNewDocument() {
	try {
	    Document newDocument = XmlUtils.createDocument();
	    Element report = newDocument.createElement(REPORT_ELEMENT);
	    newDocument.appendChild(report);
	    return newDocument;
	} catch (XMLException ex) {
	    throw new IllegalStateException("It is impossible to create XML",
		    ex);
	}
    }

    private Document parseDocument(String text) {
	try {
	    return XmlUtils.parseDocument(new ByteArrayInputStream(text
		    .getBytes("UTF-8")));
	} catch (XMLException ex) {
	    logger.error("It is impossible to parse XML from text " + text, ex);
	    return null;
	} catch (UnsupportedEncodingException ex) {
	    throw new IllegalStateException(ex.getMessage(), ex);
	}
    }

    public void appendValue(ValuePart value) {
	Element partElement = createPart((ReportValuePart) value);
	document.getDocumentElement().appendChild(partElement);
    }

    protected Element createPart(ReportValuePart valuePart) {
	Element part = document.createElement(PART_ELEMENT);
	part.setAttribute(FACT_USER_ATTRIBUTE, valuePart.getFactUser());
	part.setAttribute(ROUND_ATTRIBUTE, valuePart.getRound());
	part.setAttribute(TIMESTAMP_ATTRIBUTE, DATE_FORMAT.format(valuePart
		.getTime()));
	part.setTextContent(trimAndNewlineRight(valuePart.getValue()));
	return part;
    }

    protected String trimAndNewlineRight(String input) {
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

    public String getValue() throws IReferentException {
	try {
	    return XmlUtils.serialize(document).toString("UTF-8");
	} catch (XMLException ex) {
	    throw new IReferentException(
		    "Error during serialization of result XML", ex);
	} catch (UnsupportedEncodingException ex) {
	    throw new IllegalStateException(ex.getMessage(), ex);
	}
    }

    public ValuePart[] getParts() throws IReferentException {
	NodeList partElements = XmlUtils.getSubElements(document
		.getDocumentElement(), PART_ELEMENT);
	ValuePart[] parts = new ValuePart[partElements.getLength()];
	for (int i = 0; i < partElements.getLength(); ++i) {
	    parts[i] = createValuePart((Element) partElements.item(i));
	}
	return parts;
    }

    protected ValuePart createValuePart(Element partElement) {
	ReportValuePart valuePart = new ReportValuePart();
	valuePart.setFactUser(partElement.getAttribute(FACT_USER_ATTRIBUTE));
	valuePart.setRound(partElement.getAttribute(ROUND_ATTRIBUTE));
	Date time;
	try {
	    time = DATE_FORMAT.parse(partElement
		    .getAttribute(TIMESTAMP_ATTRIBUTE));
	    valuePart.setTime(time);
	} catch (ParseException ex) {
	    logger.error("Error during parse date of report attribute", ex);
	}
	valuePart.setValue(partElement.getTextContent());
	return valuePart;
    }
}
