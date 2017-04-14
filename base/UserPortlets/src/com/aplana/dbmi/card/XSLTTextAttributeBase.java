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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.portlet.ActionRequest;
import javax.portlet.PortletRequest;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.service.DataException;

/**
 * �����-������ ��� ��������� ������� ���������� XSLT ��� ����������� �������� ��������
 * 
 * @author dstarostin
 *
 */
public abstract class XSLTTextAttributeBase extends JspAttributeEditor
{	
	// ��������� ��������������, ������������ � JSP
	public static final String KEY_XSLT_RESULT = "xsltResult";
	
	// ������������ ����� xslt � ������� UserPortlets
	//TODO ����������� ��� ����� � conf
	public static final String PARAM_XSLT_LOCATION = "xsltLocation"; 
	
	public static final String PARAM_COLUMN = "columns";
	
	// �������� �������� xml
	protected Document xmlDocument;
	protected CardPortletCardInfo cardInfo;
	// ������������ ����� xslt � ������� UserPortlets
	protected String xsltLocation;
	// �������� ��������
	private List/*<String>*/ nameColumns;
	
	@Override
	public boolean gatherData(ActionRequest request, Attribute attr) throws DataException {
		return false;
	}

	@Override
	public void setParameter(String name, String value) {
		if (PARAM_XSLT_LOCATION.equals(name)) {
			xsltLocation = value;
		} else if (PARAM_COLUMN.equals(name)) {
			String[] cols = value.split(";");
			nameColumns = new ArrayList(cols.length);
			for (int i = 0; i < cols.length; i++) {
				nameColumns.add(cols[i].trim());
			}
		} else {
			super.setParameter(name, value);
		}
	}

	public boolean isValueCollapsable() {
		return true;
	}
	
	@Override
	public void initEditor(PortletRequest request, Attribute attr) throws DataException {
		super.initEditor(request, attr);
		CardPortletSessionBean sessionBean = (CardPortletSessionBean)request.getPortletSession().getAttribute(CardPortlet.SESSION_BEAN);
		cardInfo = sessionBean.getActiveCardInfo();

		try {
			xmlDocument = prepareDocument(request, attr);
			cardInfo.setAttributeEditorData(attr.getId(), KEY_XSLT_RESULT, getXSLTOutput(xmlDocument, request, attr));
		} catch (Exception e) {
			throw new DataException("Exception while init " + this.getClass().getCanonicalName(), e);
		}
	}
	
	/**
	 * ����� ������ ��������� �������� XML � ���������� ��� � ���� ��������� DOM 
	 */
	protected abstract Document prepareDocument(PortletRequest request, Attribute attr) throws Exception;
	
	/**
	 * ����� ������ ��������� �������������� XSLT � ���������� ������-���������.
	 * ��� �������������� �� ����� ������������ ����� {@link #transform(Document, String)}
	 */
	protected abstract String getXSLTOutput(Document xml, PortletRequest request, Attribute attr) throws Exception;
	
	/**
	 * ��������������� ����� ��� ���������� �������������� XSLT
	 * @param xml ������������� ��������
	 * @param xsltPath �������� ���� � ��������� XSLT
	 * @return ������ � ����������� ��������������
	 * @throws TransformerException
	 */
	protected String transform(Document xml, String xsltPath) throws TransformerException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			addColumns(xml);
			getTransformer(xsltPath).transform(new DOMSource(xml), new StreamResult(baos));
			deleteColumns(xml);
			return baos.toString("UTF-8");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	protected void addColumns(Document xml) {
		Element columnsEl = xml.createElement("columns");
		Iterator iter = nameColumns.iterator();
		while (iter.hasNext()) {
			String nameColumn = (String)iter.next();
			Element columnEl = xml.createElement("column");
			columnEl.setTextContent(nameColumn);
			columnsEl.appendChild(columnEl);
		}
		xml.getDocumentElement().appendChild(columnsEl);
	}
	
	protected void deleteColumns(Document xml) throws XPathExpressionException {
		XPathExpression columnsExpression = XPathFactory.newInstance().newXPath().compile("/report/columns");
		Element columnsEl = (Element)columnsExpression.evaluate(xml, XPathConstants.NODE);
		xml.getDocumentElement().removeChild(columnsEl);
	}

	private static Map<String, Transformer> weakTrans = new WeakHashMap<String, Transformer>();
	private static synchronized Transformer getTransformer(String path) throws TransformerConfigurationException {
		Transformer trans = weakTrans.get(path);
		if (trans == null) {
			Source xsltSource = new StreamSource(new File(path));
	        TransformerFactory transFact = TransformerFactory.newInstance();
			trans = transFact.newTransformer(xsltSource);
	        weakTrans.put(path, trans);
		}
		return trans;
	}

}
