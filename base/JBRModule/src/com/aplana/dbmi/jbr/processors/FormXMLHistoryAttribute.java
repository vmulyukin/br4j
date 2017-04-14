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
package com.aplana.dbmi.jbr.processors;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.OverwriteCardAttributes;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.HtmlAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

public class FormXMLHistoryAttribute extends ProcessCard {
	private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	private static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";	
	private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

	private final static String PARAM_XML_ATTR_ID= "reportAttrId";
	private final static String PARAM_PERSON_ATTR_ID= "personAttrId";

	// ������� ������� � ������� ��������
	private final static String PARAM_ROUND_ATTR_ID= "roundAttrId";

	// �������� ��������� ��� ���������� �������
	private final static String PARAM_ROUND_NUM_START_BASE = "roundStartBase";

	// ��������� ������� ��-���������
	private final static String ROUND_NUM_START_BASE_DEFAULT = "1"; // (!) � (2010/12/05, RuSA) ������� ��������, � �� ����.

	// private static final String PARAM_SCHEMA_LOCATION = "schemaLocation";

	private ObjectId xmlAttrId;
	private ObjectId roundAttrId;
	private ObjectId personAttrId;
	private String schemaLocation;
	private Card card;
	// ������������ XML �����
	// ������ ���� ��� ���������� (xsd:dateTime)
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	@Override
	public Object process() throws DataException {

		final ObjectId cardId = getCardId();
		if (cardId == null){
			logger.error("Card id is not set for this processor. Exiting.");
			return null;
		}

		xmlAttrId = getAttrIdParameter(PARAM_XML_ATTR_ID, HtmlAttribute.class, false);
		if (xmlAttrId == null){
			logger.error("Parameter is not set or can't be resolved. Exiting.");
			return null;
		}

		roundAttrId = getAttrIdParameter(PARAM_ROUND_ATTR_ID, IntegerAttribute.class, false);
		personAttrId = getAttrIdParameter(PARAM_PERSON_ATTR_ID, PersonAttribute.class, false);

		final Search search = new Search();
		search.setByCode(true);
		search.setWords(cardId.getId().toString());
		search.setColumns(CardUtils.createColumns(xmlAttrId, roundAttrId, personAttrId));

		final List<Card> cards = 
			CardUtils.execSearchCards(search, getQueryFactory(), getDatabase(), getSystemUser());
		if (cards == null || cards.size() == 0){
			logger.error("Can't find this card "+ cardId.getId() +". Exiting.");
			return null;
		}
		if (cards.size() > 1){
			logger.error("There is something strange: more than 1 card wih ID " 
					+ cardId.getId() 
					+ ". Exiting.");
			return null;
		}

		card = cards.get(0);
		final HtmlAttribute xmlAttr = (HtmlAttribute)card.getAttributeById(xmlAttrId);
		if (xmlAttr == null || xmlAttr.isEmpty()){
			logger.info("Attribute" 
					+ xmlAttrId.getId() 
					+ " card " 
					+ cardId.getId() 
					+ " is empty. Nothing to do for me. Exiting.");
			return null;
		}

		// ������� ����� �������� ...
		final String roundDefault = getParameterTrimmed(PARAM_ROUND_NUM_START_BASE, ROUND_NUM_START_BASE_DEFAULT);
		final String round = getCardAttrValue( card, roundAttrId, roundDefault, "������ ��� ������������ XML �������� " + xmlAttr);

		// ������� �������...
		final String person = getCardAttrValue( card, personAttrId, "", "������ ��� ������������ XML �������� " + xmlAttr);

		try {
			xmlAttr.setValue( formDocument(xmlAttr.getValue(), round, person));
		} catch (Exception e) {
			logger.error("������ ��� �������� XML �������� " + xmlAttr 
					+ "� �������� " + cardId.getId() + ":", e);
			return null;
		}

		final OverwriteCardAttributes action = new OverwriteCardAttributes();
		action.setCardId(cardId);
		action.setAttributes(Collections.singletonList(xmlAttr));
		action.setInsertOnly(false);
		final ActionQueryBase query = getQueryFactory().getActionQuery(action);
		query.setAction(action);

		execAction(new LockObject(cardId));
		try {
			getDatabase().executeQuery(getSystemUser(), query);
		} finally {
			execAction(new UnlockObject(cardId));
		}

		return null;
	}

	/**
	 * � �������� �������� string-�������� ���������� ��������.
	 * @param acard �������� (� ����������) 
	 * @param attrId id ��������
	 * @param attrDefaultStrVal �������� ��-���������, �.�. ���� �������� ��� ���
	 * �� ����� null-getStringValue.
	 * @return
	 */
	private String getCardAttrValue(Card acard, ObjectId attrId,
			String attrDefaultStrVal,
			String errInfo) 
	{
		String result = null;

		final Attribute attr = acard.getAttributeById(attrId);
		if (attr == null) {
			logger.error( errInfo + " � �������� " + acard.getId() 
					+ ": ����������� ������� "+ attrId);
		} else {
			result = attr.getStringValue();
			if( result == null) {
				logger.error( errInfo + " � �������� " + acard.getId() 
						+ ": ������� "+ attrId + " ����� ������������/������ ��������");
			}
		}
		return (result != null) ? result : attrDefaultStrVal; 
	}

	private static final ErrorHandler saxHandler = new ErrorHandler() {
		public void error(SAXParseException e) throws SAXException {throw e;}
		public void fatalError(SAXParseException e) throws SAXException {throw e;}
		public void warning(SAXParseException arg0) throws SAXException {}
	};
	
	String formDocument(String originalValue, String round, String person) 
		throws UnsupportedEncodingException, IOException, 
				XPathExpressionException, SAXException, 
				ParserConfigurationException, TransformerFactoryConfigurationError, 
				TransformerException{
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();		

		Document xmldoc;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		if (schemaLocation != null) {
			dbf.setNamespaceAware(true);
			dbf.setValidating(true);
			dbf.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
			dbf.setAttribute(JAXP_SCHEMA_SOURCE, new File(schemaLocation));
		}
		DocumentBuilder builder = dbf.newDocumentBuilder();
		builder.setErrorHandler(saxHandler);
		// ������ XML �� ������
		xmldoc = builder.parse(new ByteArrayInputStream(originalValue.getBytes("UTF-8")));
		Element root = xmldoc.getDocumentElement();
		String rawReport = (String)xpath.evaluate("./raw-part/text()", root, XPathConstants.STRING);
		NodeList list = (NodeList)xpath.evaluate("./raw-part", root, XPathConstants.NODESET);
		for (int i=0; i<list.getLength(); i++){
			Node rawReportTag = list.item(i);
			root.removeChild(rawReportTag);
		}

		final Date date = new Date();
		final Element part = xmldoc.createElement("part");
		part.setAttribute("round", round);
		part.setAttribute("timestamp", (date == null) ? "-" : DATE_FORMAT.format(date));
		part.setAttribute("fact-user", person);
		part.setTextContent(trimAndNewlineRight(rawReport));
		root.appendChild(part);

		final StringWriter stw = new StringWriter();
		final Transformer serializer = TransformerFactory.newInstance().newTransformer();
		serializer.transform(new DOMSource(xmldoc), new StreamResult(stw));

		return stw.toString();
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
