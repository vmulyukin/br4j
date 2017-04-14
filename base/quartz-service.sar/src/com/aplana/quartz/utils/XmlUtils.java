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
package com.aplana.quartz.utils;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.aplana.dbmi.service.DataException;

/**
 * @author rabdullin
 *
 */
public final class XmlUtils {

	final static Log logger = LogFactory.getLog( XmlUtils.class);

	/**
	 * �� xml, ��������� � ���� ������, �������� ���������.
	 * XML �������� ������ ����:
	 * 		<root>   -- ������������ ��������-��������
	 * 			
	 * 		</root>
	 * @param xmlText
	 * @param encoding
	 * @return
	 * @throws DataException
	 */
	public static Map<String, String> xmlToParams(String xmlText, String encoding)
			throws DataException
	{
		if (xmlText == null || xmlText.length() == 0)
			return null;
		try {
			final ByteArrayInputStream buf = new ByteArrayInputStream(xmlText.getBytes(encoding));
			final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(buf);
			return xmlGetElementsAsMap( doc.getDocumentElement(), "parameter", "name", "value");
		} catch (Exception ex) {
			throw new DataException(ex);
		}
	}

	/**
	 * �������� ��������� �� xml, ��������� ������� ������ � ��������� UTF8.
	 * @param xmlText
	 * @return
	 */
	public static Map<String, String> xmlToParams(String xmlText)
			throws DataException
	{
		return xmlToParams(xmlText, "utf-8");
	}

	/**
	 * �������� �� ���� ��������� �������� � ���� �����  
	 * @param root  ����, �� ������� ��������� ��������.
	 * @param xmlNodeItemTags  xml-�������� ��������� ������ root-����, �� �������
	 * ���� ��������� ������ map.
	 * @param xmlItemAttrName  � �������� �������� xml-��������, c ��������� ��� map.
	 * @param xmlItemAttrValue  ��������������� ��� xmlItemAttrName �������� � ��������.
	 * @return ����� <���,��������>, ��������� �� ���-��������� root-����.
	 */
	public static Map<String, String> xmlGetElementsAsMap( Element root,
			String xmlNodeItemTags, 
			String xmlItemAttrName, 
			String xmlItemAttrValue) 
	{
		if (root == null)
			return null;

		final NodeList paramList = root.getElementsByTagName(xmlNodeItemTags);
		if (paramList == null)
			return null;

		final Map<String, String> result = new HashMap<String, String>();
		for (int i=0; i < paramList.getLength(); i++) {
			final Element paramEl = (Element) paramList.item(i);
			result.put( paramEl.getAttribute(xmlItemAttrName), paramEl.getAttribute(xmlItemAttrValue));
		}
		return result;
	}

}
