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
package com.aplana.dbmi.card.doclinked;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xpath.domapi.XPathEvaluatorImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.xpath.XPathEvaluator;
import org.w3c.dom.xpath.XPathResult;

import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Reference;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

/**
 * @author RAbdullin
 * ������ ��� ����������� �������� �������� ����������:
 * 	- ��� ��������, � �������� ����� ���������� �����,
 * 	- ������ ����������� ��������,
 * 	- ������ ����� ������.
 */
public class DoclinkCreateData {

	static Log logger = LogFactory.getLog(DoclinkCreateData.class);

	// id ������� -> �������������� ��������
	private Map<ObjectId, String> mapTemplates;

	// id ������� ��� ������ ����� ����� (���� values_list � reference_list)
	private ObjectId refCodeId;

	// �������� ��� ����� -> �������������� ��������
	private List<ReferenceValue> types;

	// ��� �������� �����
	private ObjectId attrBackLinkId;
	private String attrBackLinkCodeOrKey;


	/**
	 * @return ��� �������� (���� backlink ��� �������) � �������� ��������, � 
	 * ������ �������� ������ ������� ����������� ��������.
	 */
	public ObjectId getAttrBackLinkId() {
		return this.attrBackLinkId;
	}

	/**
	 * @param value ��� �������� (���� backlink) � �������� ��������, � 
	 * ������ �������� ������ ������� ����������� ��������.
	 * @throws DataException 
	 */
	public void setAttrBackLinkId(ObjectId value) throws DataException 
	{
		if (value != null && !BackLinkAttribute.class.isAssignableFrom(value.getType()))
			//store.cardaccess.wrong.class=�������� ��� ���������\: �������� ''{0}'', �� ��������� ''{1}'' 
			throw new DataException( "store.cardaccess.wrong.class", 
					new Object[] {BackLinkAttribute.class, value.getType()} );
		this.attrBackLinkId = value;
		this.attrBackLinkCodeOrKey = (value == null) ? null : (String) value.getId();
	}

	/**
	 * @return ��� �������� �������� ��������, ������� ����� ������� ������ 
	 * ������, ������� ����� ���� ����� ���� � ���� ����, ���� � ���� ����� �� 
	 * objectids.properties.
	 */
	public String getAttrBackLinkCode() {
		// return (this.attrLinkCodeId == null) ? null : (String) this.attrLinkCodeId.getId();
		return this.attrBackLinkCodeOrKey;
	}

	/**
	 * @param attributeCode ��� �������� �������� ��������, ������� ����� 
	 * ������� ������ ������, ��� ������� ����� ���� ����� ���� � ���� ����, 
	 * ���� � ���� ����� �� objectids.properties.
	 * @throws DataException 
	 */
	public void setAttrBackLinkCode(String attrBackLinkKeyOrCode) throws DataException {
		setAttrBackLinkId( DoclinkUtils.tryFindPredefinedObjectId(attrBackLinkKeyOrCode));
		this.attrBackLinkCodeOrKey = attrBackLinkKeyOrCode;
	}


	/**
	 * @return ������������ id ������� -> �������������� ��������
	 */
	public Map<ObjectId, String> getMapTemplates() {
		return this.mapTemplates;
	}

	/**
	 * @param value ������������ id ������� -> �������������� ��������.
	 */
	public void setMapTemplates(Map<ObjectId, String> mapTemplates) {
		this.mapTemplates = mapTemplates;
	}

	/**
	 * @return id ������� ��� ��������� ��������� ���� �����.
	 */
	public ObjectId getRefCodeId() {
		return this.refCodeId;
	}

	/**
	 * @param value id ������� ��� ��������� ���� �����.
	 */
	public void setRefCodeId(ObjectId value) {
		this.refCodeId = value;
	}

	/**
	 * @return �������� ������� ��� ��������� ��������� ���� �����.
	 */
	public String getRefCode() {
		return (this.refCodeId == null) ? null: (String) this.refCodeId.getId();
	}

	/**
	 * @param value the �������� ������� ��� ��������� ���� �����.
	 */
	public void setRefCode(String value) {
		setRefCodeId( (value == null) ? null : ObjectIdUtils.getObjectId(Reference.class, value, false));
	}

	/**
	 * @return �������� ������ ���� �����. 
	 */
	public List<ReferenceValue> getTypes() {
		return this.types;
	}

	/**
	 * @param value �������� ������ ���� �����.
	 */
	public void setTypes(List<ReferenceValue> value) {
		this.types = value;
	}

	/**
	 * �������� ����� � XML
	 */
	static final String XML_NODE_LINK_ATTRIBUTE = "link_attribute";
	static final String XML_ATTR_BACKLINK_ATTRIBUTE_CODE = "backLinkAttributeCode";

	// ������ ��������
	static final String XML_LIST_TEMPLATES = "templates";
	static final String XML_NODE_TEMPLATE = "template";
	static final String XML_ATTR_ID = "id";

	// ��������� ������ ����� ������
	static final String XML_NODE_LINK_TYPE = "link_type";
	static final String XML_ATTR_REF_CODE = "ref_code";
	static final String XML_LIST_FILTER_TEMPLATES = "filter_link_type_for_templates";
	static final String XML_ATTR_TYPES = "types";

	private static XPathFactory xml_factory;
	private static XPath xml_xpath;
	static XPath defaultXPath()
	{
		if (xml_xpath == null) {
			if (xml_factory == null)
				xml_factory = XPathFactory.newInstance();
			xml_xpath = xml_factory.newXPath();
		}
		return xml_xpath;
	}

	private static XPathEvaluator _xpath;
	public static String getTagContent(Node tag, String sDefault)
	{
		if (tag == null) return sDefault;
		if (_xpath == null) _xpath = new XPathEvaluatorImpl(); 
		final XPathResult result = (XPathResult) _xpath.evaluate("text()",
					tag, null, XPathResult.STRING_TYPE, null);
		return (result != null) ? result.getStringValue() : sDefault;
	}

	public static DoclinkCreateData xmlStreamLoad(InputStream stream, 
			DataServiceBean dataService
		) throws DataException {
		return xmlStreamLoad(stream, dataService, null);
	}
		
	public static DoclinkCreateData xmlStreamLoad(InputStream stream, 
			DataServiceBean dataService, ObjectId templateId
		) throws DataException
	{
		if (stream == null || dataService == null) 
			return null;
		try {
			final DoclinkCreateData result = new DoclinkCreateData();

			final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
			final Element root = doc.getDocumentElement();

			// ������� ��� ��������� ����������...
			final Element elemAttrCode = (Element) defaultXPath().evaluate("./" + XML_NODE_LINK_ATTRIBUTE, root, XPathConstants.NODE);
			result.setAttrBackLinkCode( elemAttrCode.getAttribute(XML_ATTR_BACKLINK_ATTRIBUTE_CODE)); 

			// �������...
			final Element elemTemplates = (Element) defaultXPath().evaluate("./"+ XML_LIST_TEMPLATES, root, XPathConstants.NODE);
			final List<ObjectId> templateIds = xmlLoadTemplates(elemTemplates);
			result.setMapTemplates( makeMapTemplates(templateIds, dataService) );

			// ������ ���������� ��������� �� �������� 
			final Element elemTemplateValues = (Element) defaultXPath().evaluate("./"+ XML_LIST_FILTER_TEMPLATES, root, XPathConstants.NODE);
			final Map<ObjectId, Set> templateValuesIds = xmlLoadTemplateValues(elemTemplateValues);
			
			// ���� ������...
			final Element elemRefCode = (Element) defaultXPath().evaluate("./"+ XML_NODE_LINK_TYPE, root, XPathConstants.NODE);
			if (elemRefCode != null) result.setRefCode( elemRefCode.getAttribute(XML_ATTR_REF_CODE)); 
			result.setTypes( loadLinkTypes(result.getRefCodeId(), dataService, (templateValuesIds!=null?templateValuesIds.get(templateId):null)));

			return result;
		} catch (Exception ex) {
			throw new DataException("general.unique", 
						new Object[] { "XMLoad DoclinkCreateData" }, 
						ex);
		}
	}

	public static List<ObjectId> xmlLoadTemplates( Element templatesNode) 
			throws XPathExpressionException 
	{
		if (templatesNode == null) return null;

		final NodeList templates = (NodeList) defaultXPath().evaluate("./" + XML_NODE_TEMPLATE, templatesNode, XPathConstants.NODESET);
		if (templates == null || templates.getLength() < 1) return null;

		final ArrayList<ObjectId> result = new ArrayList<ObjectId>();
		for (int i = 0; i < templates.getLength(); ++i) {
			final Element template = (Element) templates.item(i);
			final String id = template.getAttribute(XML_ATTR_ID);
			result.add( ObjectIdUtils.getObjectId(Template.class, id, true));
		}
		return result;
	} 

	public static Map<ObjectId, Set> xmlLoadTemplateValues( Element templateValuesNode) 
			throws XPathExpressionException 
	{
		if (templateValuesNode == null) return null;

		final NodeList templates = (NodeList) defaultXPath().evaluate("./" + XML_NODE_TEMPLATE, templateValuesNode, XPathConstants.NODESET);
		if (templates == null || templates.getLength() < 1) return null;

		final Map<ObjectId, Set> result = new HashMap<ObjectId, Set>();
		for (int i = 0; i < templates.getLength(); ++i) {
			final Element template = (Element) templates.item(i);
			final String id = template.getAttribute(XML_ATTR_ID);
			final String types = template.getAttribute(XML_ATTR_TYPES);
			String[] ids = types.split(",");
			Set<Long> setIds = new HashSet<Long>();
			for(String valueId: ids){
				setIds.add(Long.parseLong(valueId.trim()));
			}
			result.put( ObjectIdUtils.getObjectId(Template.class, id, true), setIds);
		}
		return result;
	} 

	/**
	 * @param refCodeId: id �������� ��� �������� ���������.
	 * @param dataService 
	 * @return
	 * @throws ServiceException 
	 * @throws DataException 
	 */
	@SuppressWarnings("unchecked")
	public static List<ReferenceValue> loadLinkTypes(ObjectId refCodeId, DataServiceBean dataService) 
		throws DataException, ServiceException 
	{
		return loadLinkTypes(refCodeId, dataService, null);
	}

	/**
	 * @param refCodeId: id �������� ��� �������� ���������.
	 * @param dataService 
	 * @param filterValues - ������ ���������, ������� ����� ��������
	 * @return
	 * @throws ServiceException 
	 * @throws DataException 
	 */
	@SuppressWarnings("unchecked")
	public static List<ReferenceValue> loadLinkTypes(ObjectId refCodeId, DataServiceBean dataService, Set<Long> filterValues) 
		throws DataException, ServiceException 
	{
		if (dataService == null || refCodeId == null)
			return null;
		Collection<ReferenceValue> allValues = dataService.listChildren(refCodeId, ReferenceValue.class);
		List<ReferenceValue> result = new ArrayList<ReferenceValue>(0);
		if (filterValues!=null&&!filterValues.isEmpty()){
			for (ReferenceValue value : allValues){
				if (filterValues.contains(value.getId().getId())){
					result.add(value);
				}
			}
		} else
			result.addAll(allValues);
		return result;
	}

	/**
	 * @param templateIds
	 * @param dataService 
	 * @return
	 */
	public static Map<ObjectId, String> makeMapTemplates(
			List<ObjectId> templateIds, DataServiceBean dataService) 
	{
		// DONE: ��������� ��������� ��������� ��������...
		if (dataService == null || templateIds == null || templateIds.isEmpty())
			return null;

		final LinkedHashMap<ObjectId, String> result = new LinkedHashMap<ObjectId, String>();
		for (ObjectId templateId : templateIds) {
			try {
				final Template template = (Template) dataService.getById(templateId);
				result.put( templateId, template.getName());
			} catch (Exception ex)
			{
				// ex.printStackTrace();
				logger.warn( 
					MessageFormat.format( 
						"makeMapTemplates:: templateId={0} caused a load error and skipped. Error is:\n",
						templateId),
					ex);
			}
		}
		return result;
	}

}