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
package com.aplana.dbmi.card;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.portlet.ActionRequest;
import javax.portlet.PortletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.service.DataException;

/**
 * ������� ��� ��������� ���������� ������.
 * ���������� ����� � ���� ������� � ����������� ������ � ������ �� ����������.
 * 
 * ������ �������� � ��������� �������� � ���� ����� XML.
 * ����� ������������ XML ����������� �� �����, ���������� � {@link #PARAM_SCHEMA_LOCATION}.
 * ���� �������� �� ��������, ���������� ������������ � ���� ������ ��� ����.
 * 
 * 
 * ��. {@link XSLTReportAttributeEditor}
 * 
 * @author dstarostin
 *
 */
public class XSLTReportAttributeViewer extends XSLTTextAttributeBase {

	private static final String PARAM_SCHEMA_LOCATION = "schemaLocation";
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	

	private String schemaLocation;

	public XSLTReportAttributeViewer() {
		setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/XSLTReportView.jsp");
	}
	
	@Override
	protected String getXSLTOutput(Document xml, PortletRequest request, Attribute attr)
			throws Exception {
		return transform(xml, request.getPortletSession().getPortletContext().getRealPath(xsltLocation));
	}

	private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	private static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";	
	private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
	
	private static final ErrorHandler saxHandler = new ErrorHandler() {
		public void error(SAXParseException e) throws SAXException {throw e;}
		public void fatalError(SAXParseException e) throws SAXException {throw e;}
		public void warning(SAXParseException arg0) throws SAXException {}
	};
	
	@Override
	protected Document prepareDocument(PortletRequest request, Attribute attr)
			throws Exception {
		String text = attr.getStringValue();
		if (text == null)
			text = "";
		
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			if (schemaLocation != null) {
				dbf.setNamespaceAware(true);
				dbf.setValidating(true);
				dbf.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
				dbf.setAttribute(JAXP_SCHEMA_SOURCE, new File(request.getPortletSession().getPortletContext().getRealPath(schemaLocation)));
			}
			DocumentBuilder builder = dbf.newDocumentBuilder();			
			builder.setErrorHandler(saxHandler);
			if (text.isEmpty())
				return createReport(text);
			else
				return builder.parse(new ByteArrayInputStream(text.getBytes("UTF-8")));
		} catch (SAXException e) {
			logger.warn("Exception while parsing XML from TextAttribute "
					+ attr.getId() + ": treating attribute value as text. " + e.getMessage());
			return createReport(text);
		}
	}
	
	private Document createReport(String t) throws ParserConfigurationException {
		Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		Element report = xml.createElement("report");
		xml.appendChild(report);
		report.appendChild(createPart(xml, t, null));
		return xml;
	}

	private Element createPart(Document xml, String text, Date date) {
		Element part = xml.createElement("part");
		part.setAttribute("timestamp", date == null ? "-" : DATE_FORMAT.format(date));
		part.setTextContent(text == null ? "" : text);
		return part;
	}


	@Override
	public boolean gatherData(ActionRequest request, Attribute attr)
			throws DataException {
		return false;
	}
	
	@Override
	public void setParameter(String name, String value) {
		if (PARAM_SCHEMA_LOCATION.equals(name))
			schemaLocation = value;
		else
			super.setParameter(name, value);
	}

}
