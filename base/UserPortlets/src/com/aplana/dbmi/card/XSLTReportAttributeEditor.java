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
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.portlet.ActionRequest;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.service.DataException;

/**
 * ������� ��� �������������� ���������� ������.
 * ��� ������ � ��������� ������������� ������ ������������ � �������� ����,
 * ��� ���������� �������� ������ ����������� � ������� ������ � ����� ����������.
 * ������� �� ����� ������������ ����������� ������ ������������ ��� ��������� �����.
 * 
 * ������ �������� � ��������� �������� � ���� ����� XML.
 * ����� ������������ XML ����������� �� �����, ���������� � {@link #PARAM_SCHEMA_LOCATION}.
 * ���� �������� �� ��������, ���������� ������������ � ���� ������ ��� ����.
 * 
 * @author dstarostin
 *
 */
// TODO ������� ����� ����� � XSLTReportAttributeViewer
public class XSLTReportAttributeEditor extends XSLTTextAttributeBase {

	// ���������� ���������� ����
	public static final String KEY_EDITOR_DATA = "editorData";
	// ������� ���� ��� �������� ��� ��������
	public static final String KEY_EDITOR_LOADED = "editorLoaded";
	// ������ ���� ��� ���������� (xsd:dateTime)
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		
	// ������������ XML �����
	protected static final String PARAM_SCHEMA_LOCATION = "schemaLocation";
	protected String schemaLocation;

	public XSLTReportAttributeEditor() {
		setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/XSLTReportEdit.jsp");
	}
	

	public boolean gatherData(ActionRequest request, Attribute attr)
			throws DataException {
		String value = request.getParameter(getAttrHtmlId(attr));
		// ���� ����������� �� ��� ������� ������, ������� ��� �������� �� ����������
		if (value == null || value.trim().equals("")) {
			cardInfo.resetAttributeEditorData(attr.getId(), KEY_EDITOR_LOADED);
			return false;
		}
		// ����� ��������� ������ � ���� �������� xml � ��������� � 
		// ��������� �������� � ��������� ��������
		try {
			cardInfo.resetAttributeEditorData(attr.getId(), KEY_EDITOR_DATA);
			xmlDocument.getDocumentElement().appendChild(createPart(xmlDocument, value, new Date()));
			StringWriter stw = new StringWriter();
			Transformer serializer = TransformerFactory.newInstance().newTransformer();
			serializer.transform(new DOMSource(xmlDocument), new StreamResult(stw));
			((StringAttribute) attr).setValue(stw.toString());
			return true;
		} catch (TransformerException e) {
			throw new DataException("XSLT Exception", e);
		}

	}


	protected Element createPart(Document xml, String text, Date date) {
		Element part = xml.createElement("part");
		part.setAttribute("timestamp", date == null ? "-" : DATE_FORMAT.format(date));
		part.setTextContent(trimAndNewlineRight(text));
		return part;
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

	@Override
	protected String getXSLTOutput(Document xml, PortletRequest request, Attribute attr)
			throws Exception {
		if (cardInfo.getAttributeEditorData(attr.getId(), KEY_EDITOR_LOADED) == null) {
			cardInfo.setAttributeEditorData(attr.getId(), KEY_EDITOR_LOADED,
					Boolean.TRUE);
			cardInfo.setAttributeEditorData(attr.getId(), KEY_EDITOR_DATA, "");
		} else {
			Element edit = (Element) xml.getDocumentElement().getLastChild();
			xml.getDocumentElement().removeChild(edit);
			cardInfo.setAttributeEditorData(attr.getId(), KEY_EDITOR_DATA, edit.getTextContent());
		}
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

		Document xmldoc;
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
			// ������ XML �� ������
			return builder.parse(new ByteArrayInputStream(text.getBytes("UTF-8")));
		} catch (SAXException e) {
			// ���� ��� ��������� �������� ������ -- ������ ����� XML �� ���������
			// �������� � �������� �����������.
			logger.warn("Exception while parsing XML from "
					+ attr.getId() + ": treating attribute value as text", e);
			xmldoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element report = xmldoc.createElement("report");
			xmldoc.appendChild(report);
			report.appendChild(createPart(xmldoc, text, null));
			cardInfo.setAttributeEditorData(attr.getId(), KEY_EDITOR_LOADED, Boolean.TRUE);
		}
		return xmldoc;

	}

	
	@Override
	public void setParameter(String name, String value) {
		if (PARAM_SCHEMA_LOCATION.equals(name))
			schemaLocation = value;
		else
			super.setParameter(name, value);
	}
	
	public static void addRecord(Attribute attr, String addText) throws Exception {
		String text = attr.getStringValue();
		Document xmldoc;
		if (text == null || text.equals("")) {
			xmldoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element report = xmldoc.createElement("report");
			xmldoc.appendChild(report);
		} else {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			xmldoc = builder.parse(new ByteArrayInputStream(text.getBytes("UTF-8")));
		}
		
		Date date = new Date();
		Element part = xmldoc.createElement("part");
		part.setAttribute("timestamp", date == null ? "-" : DATE_FORMAT.format(date));
		part.setTextContent(trimAndNewlineRight(addText));
		
		xmldoc.getDocumentElement().appendChild(part);
		StringWriter stw = new StringWriter();
		Transformer serializer = TransformerFactory.newInstance().newTransformer();
		serializer.transform(new DOMSource(xmldoc), new StreamResult(stw));
		((StringAttribute) attr).setValue(stw.toString());
	}
	
	public void writeEditorCode(RenderRequest request, RenderResponse response,
			Attribute attr) throws IOException, PortletException {
		Object isLoaded = cardInfo.getAttributeEditorData(attr.getId(), KEY_EDITOR_DATA);
		if (isLoaded == null){
			try{
				initEditor(request, attr);
			}catch(Exception ex){
				throw new PortletException("Can not init editor", ex);
			}
		}
		
		super.writeEditorCode(request, response, attr);
	}

}
