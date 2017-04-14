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
package com.aplana.dbmi.module.docflow;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.OverwriteCardAttributes;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.HtmlAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ProcessorBase;

public class AddTextToReportHistoryProcessor extends ProcessorBase {
	private static final long serialVersionUID = 1L;

	// ������ ���� ��� ���������� (xsd:dateTime)
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	public static final ObjectId currentTextAttrId = ObjectId.predefined(TextAttribute.class, "jbr.report.currentText");
	public static final ObjectId reportTextAttrId = ObjectId.predefined(HtmlAttribute.class, "jbr.report.text");
	
	@Override
	public Object process() throws DataException {
		Card reportCard = ((ChangeState)getAction()).getCard();
		TextAttribute currentTextAttr = (TextAttribute)reportCard.getAttributeById(currentTextAttrId);
		if(currentTextAttr==null || currentTextAttr.getStringValue().isEmpty()){
			return reportCard;
		}
		HtmlAttribute reportTextAttr = (HtmlAttribute)reportCard.getAttributeById(reportTextAttrId);
		addRecord(reportTextAttr, currentTextAttr.getValue());
		currentTextAttr.setValue(null);

		OverwriteCardAttributes action = new OverwriteCardAttributes();
		action.setCardId(reportCard.getId());
		ArrayList<HtmlAttribute> attrList = new ArrayList<HtmlAttribute>();
		attrList.add(reportTextAttr);
		//attrList.add(currentTextAttr);
		action.setAttributes(attrList);
		action.setInsertOnly(false);
		final ActionQueryBase query = getQueryFactory().getActionQuery(action);
		query.setAction(action);
		getDatabase().executeQuery(getSystemUser(), query);
		
		//�������������� ��������, ��� ��� ��� ����������
		/*ObjectQueryBase cardQuery = getQueryFactory().getFetchQuery(Card.class);
		cardQuery.setId(reportCard.getId());
		reportCard = (Card) getDatabase().executeQuery(getUser(), cardQuery);*/

		return reportCard;
	}

	public static void addRecord(Attribute attr, String addText) throws DataException {
		String text = attr.getStringValue();
		String actualAddText = addText;
		Document xmldoc;
		try {
			if (text == null || text.equals("")) {
				xmldoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
				Element report = xmldoc.createElement("report");
				xmldoc.appendChild(report);
			} else {
				DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				try {
					xmldoc = builder.parse(new ByteArrayInputStream(text.getBytes("UTF-8")));
				} catch(SAXException e) {
					xmldoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
					Element report = xmldoc.createElement("report");
					xmldoc.appendChild(report);
					actualAddText = text;
				}
			}
			
			Date date = new Date();
			Element part = xmldoc.createElement("part");
			part.setAttribute("timestamp", date == null ? "-" : DATE_FORMAT.format(date));
			part.setTextContent(trimAndNewlineRight(actualAddText));
			
			xmldoc.getDocumentElement().appendChild(part);
			StringWriter stw = new StringWriter();
			Transformer serializer = TransformerFactory.newInstance().newTransformer();
			serializer.transform(new DOMSource(xmldoc), new StreamResult(stw));
			((StringAttribute) attr).setValue(stw.toString());
		} catch (ParserConfigurationException e) {
			throw new DataException("Cant add record to report text history", e);
		} catch (UnsupportedEncodingException e) {
			throw new DataException("Cant add record to report text history", e);
		} catch (IOException e) {
			throw new DataException("Cant add record to report text history", e);
		} catch (TransformerConfigurationException e) {
			throw new DataException("Cant add record to report text history", e);
		} catch (TransformerFactoryConfigurationError e) {
			throw new DataException("Cant add record to report text history", e);
		} catch (TransformerException e) {
			throw new DataException("Cant add record to report text history", e);
		}
	}

	protected static String trimAndNewlineRight(String input) {
		if (input == null){
			return "";
		}
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
