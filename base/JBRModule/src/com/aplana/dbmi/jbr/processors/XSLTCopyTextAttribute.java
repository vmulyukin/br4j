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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.jbr.util.AttributeLocator;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ObjectQueryBase;


/**
 * ���������, ���������� �������� ������ ���������� �������� � ������
 * � XSLT-���������������.
 * 
 * @author dstarostin
 *
 */
public class XSLTCopyTextAttribute extends ProcessCard {

	// ������� � �������� ���������
	public static final String PARAM_ATTR_FROM = "attrFrom";
	// ������� ���� ������������ ��������������� ��������
	public static final String PARAM_ATTR_TO = "attrTo";
	// ���� � ����� xslt (������������ Portal/conf)
	public static final String PARAM_XSLT_LOCATION = "xsltLocation";
	
	private String attrFrom;
	private String attrTo;
	private String xsltLocation;
	
	
	@Override
	public void setParameter(String name, String value) {
		//Portal.getFactory().getConfigService().loadConfigFile(arg0)
		if (PARAM_ATTR_FROM.equals(name))
			attrFrom = value;
		else if (PARAM_ATTR_TO.equals(name))
			attrTo = value;
		else if (PARAM_XSLT_LOCATION.equals(name))
			xsltLocation = value;
		else
			super.setParameter(name, value);
	}


	protected Document prepareDocument(String text) {
		if (text == null || text.trim().length() < 1) {
			// text = ""; <- ��� �������� � ������� ������ builder.parse, � �������� null, ��� ��� ����� ������ �����...
			logger.warn( "Empty document XML");
			return null;
		}
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			return builder.parse(new ByteArrayInputStream(text.getBytes("UTF-8")));
		} catch (Exception e) {
			logger.error("Exception while preparing document >>> \n"+text + "\n<<<", e);
			return null;
		}
	}
	
	@Override
	public Object process() throws DataException {
		
		try {
			Card card = getCard();
			
			//�������������� ��������, ��� ��� ��� ����������
			/*
			ObjectQueryBase cardQuery = getQueryFactory().getFetchQuery(Card.class);
			cardQuery.setId(card.getId());
			card = (Card) getDatabase().executeQuery(getUser(), cardQuery);
			*/

			
			final StringAttribute srcAttr = (StringAttribute)card.getAttributeById(new AttributeLocator(attrFrom).getAttrId());
			if (srcAttr == null) {
				logger.warn( MessageFormat.format( MSG_CARD_0_HAS_NO_ATTRIBUTE_1, card.getId(), attrFrom));
				return null;
			}
			final StringAttribute dstAttr = (StringAttribute)card.getAttributeById(new AttributeLocator(attrTo).getAttrId());
			if (dstAttr == null) {
				logger.warn( MessageFormat.format( MSG_CARD_0_HAS_NO_ATTRIBUTE_1, card.getId(), attrTo));
				return null;
			}
			final String xmlText = srcAttr.getStringValue();
			final Document xmlDoc = prepareDocument(xmlText);
			String result = xmlText;
			if (xmlDoc != null) {
				final InputStream in;
				try {
					in = Portal.getFactory().getConfigService().loadConfigFile(xsltLocation);
				} catch (IOException e) {
					throw new DataException("jbr.xsltcopytextattribute.xsltFileError", new Object[] {xsltLocation, e.getMessage()}, e);
				}
				try {
					result = transform(xmlDoc, in);
				} finally {
					in.close();
				}
			}
			//dstAttr.setValue(result);
			
			if (result != null && result.length() > 0) {
				int updatedRowsNr = getJdbcTemplate().update("update attribute_value set string_value = ? where card_id = ? and attribute_code = ?", 
						new Object[] { result, card.getId().getId(), dstAttr.getId().getId() });
				if (updatedRowsNr < 1) {
					getJdbcTemplate().update("insert into attribute_value (card_id, attribute_code, string_value) values(?, ?, ?)", 
							new Object[] { card.getId().getId(), dstAttr.getId().getId(), result });
				}
			}
		} catch (Exception e) {
			logger.error("Exception caught", e);
			if (e instanceof DataException)
				throw (DataException)e;
			throw new DataException("jbr.xsltcopytextattribute.error", new Object[] {e.getMessage()}, e);
		}
		return null;
	}

	/**
	 * ������������ �������������� xslt
	 * @param xml �������� ��������
	 * @param xsltStream ������� ����� ��� ����� xslt
	 * @return ������ ���������� ��������������� ��������
	 * @throws TransformerException ��� ������ ��������������
	 */
	public static String transform(Document xml, InputStream xsltStream) throws TransformerException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		getTransformer(xsltStream).transform(new DOMSource(xml), new StreamResult(baos));
		try {
			return baos.toString("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static synchronized Transformer getTransformer(InputStream sourceStream) throws TransformerConfigurationException {
		Source xsltSource = new StreamSource(sourceStream);
	    TransformerFactory transFact = TransformerFactory.newInstance();
		return transFact.newTransformer(xsltSource);
	}
}