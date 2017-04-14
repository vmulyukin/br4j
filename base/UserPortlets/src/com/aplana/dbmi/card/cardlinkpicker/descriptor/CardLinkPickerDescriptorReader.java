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
package com.aplana.dbmi.card.cardlinkpicker.descriptor;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.Search.Filter;
import com.aplana.dbmi.action.xml.SearchXmlHelper;
import com.aplana.dbmi.actionhandler.descriptor.ActionHandlerDescriptor;
import com.aplana.dbmi.actionhandler.descriptor.SelectionType;
import com.aplana.dbmi.card.AttributeEditorFactory;
import com.aplana.dbmi.card.CardLinkPickerAttributeEditor;
import com.aplana.dbmi.card.cardlinkpicker.descriptor.CardLinkPickerVariantDescriptor.SearchDependency;
import com.aplana.dbmi.card.hierarchy.CardFilterCondition;
import com.aplana.dbmi.card.hierarchy.Messages;
import com.aplana.dbmi.card.hierarchy.descriptor.ConditionReader;
import com.aplana.dbmi.card.hierarchy.descriptor.HierarchyDescriptor;
import com.aplana.dbmi.card.hierarchy.descriptor.HierarchyDescriptorReader;
import com.aplana.dbmi.card.hierarchy.descriptor.LinkDescriptor;
import com.aplana.dbmi.card.hierarchy.descriptor.MessagesReader;
import com.aplana.dbmi.card.hierarchy.descriptor.ReplaceDescriptor;
import com.aplana.dbmi.card.hierarchy.descriptor.StylingDescriptor;
import com.aplana.dbmi.card.util.AbstractXmlDescriptorReader;
import com.aplana.dbmi.card.util.SearchUtils;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.model.util.AttrUtils;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

public class CardLinkPickerDescriptorReader extends AbstractXmlDescriptorReader {

	private XPathExpression variantsRootExpr;
	private XPathExpression variantsExpr;
	private XPathExpression searchExpr;
	private XPathExpression searchDependenciesExpr;
	private XPathExpression dependenciesExpr;
	private XPathExpression messagesExpr;
	private XPathExpression hierarchyDialogExpr;
	private XPathExpression selectableCardsExpr;
	private XPathExpression dropDawnListsRoot;
	private XPathExpression dropDawnList;
	private XPathExpression mapItems;
	private XPathExpression mapItemsTemplates;
	private XPathExpression mapItemsAttributes;
	private XPathExpression variantConditionsExpr;
	private XPathExpression conditionExpr;
	private XPathExpression listExpr;
	private XPathExpression replaceExpr;

	private MessagesReader messagesReader;
	private ConditionReader conditionReader;
	private HierarchyDescriptorReader hierarchyDescriptorReader;
	private DataServiceBean serviceBean;

	public CardLinkPickerDescriptorReader(DataServiceBean serviceBean) throws XPathExpressionException {
		this.variantsRootExpr = xpath.compile("./variants");
		this.variantsExpr = xpath.compile("./variant");
		this.variantConditionsExpr = xpath.compile("./conditions");
		this.conditionExpr = xpath.compile("./condition");
		this.searchExpr = xpath.compile("./search");
		this.dependenciesExpr = xpath.compile("./searchDependencies/dependency");
		this.searchDependenciesExpr = xpath.compile("./searchDependencies");
		this.hierarchyDialogExpr = xpath.compile("./hierarchyDialog");
		this.selectableCardsExpr = xpath.compile("./selectableCardCondition");
		this.dropDawnListsRoot = xpath.compile("./dropdawnlists");
		this.dropDawnList = xpath.compile("./dropdawnlist");
		this.mapItems = xpath.compile("./mapItems");
		this.mapItemsTemplates = xpath.compile("./templates");
		this.mapItemsAttributes = xpath.compile("./attributes");
		this.listExpr = xpath.compile("./list");
		this.replaceExpr = xpath.compile("./replace");

		this.messagesExpr = xpath.compile("./messages");
		this.messagesReader = new MessagesReader(xpath);
		this.conditionReader = new ConditionReader(xpath);
		this.hierarchyDescriptorReader = new HierarchyDescriptorReader(xpath, messagesReader);
		this.serviceBean = serviceBean;
	}

	public CardLinkPickerDescriptor read(InputStream stream, Attribute attr) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException, ParseException, DataException, ServiceException {
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
		final CardLinkPickerDescriptor result = new CardLinkPickerDescriptor();
		result.setVariants(new ArrayList<CardLinkPickerVariantDescriptor>());
		Element root = doc.getDocumentElement();
		Messages messages = messagesReader.read((Element)messagesExpr.evaluate(root, XPathConstants.NODE));
		result.setMessages(messages);
		Element variantsRoot = (Element)variantsRootExpr.evaluate(root, XPathConstants.NODE);
		String choiceAttrCode = variantsRoot.getAttribute("choiceAttr");
		if (!"".equals(choiceAttrCode)) {
			if("LOCAL".equals(choiceAttrCode)){
				result.setChoiceAttrId(new ObjectId(Attribute.class, "LOCAL"));
				result.setLocalChoice(true);
			} else {
				result.setChoiceAttrId(ObjectIdUtils.getObjectId(ListAttribute.class, choiceAttrCode, false));
			}
		}
		
		String sharedValuesString = variantsRoot.getAttribute("sharedValues");
		if (!"".equals(sharedValuesString)) {
			result.setSharedValues(Boolean.parseBoolean(sharedValuesString));
		}
		
		NodeList variantNodes = (NodeList)variantsExpr.evaluate(variantsRoot, XPathConstants.NODESET);
		for (int i = 0; i < variantNodes.getLength(); ++i) {
			Element variantElem = (Element)variantNodes.item(i);
			String p = variantElem.getAttribute("filename");
			CardLinkPickerVariantDescriptor d;
			if ("".equals(p)) {
				d = readVariantDescriptorFromNode(variantElem, attr, messages, result);
			} else {
				d = readVariantDescriptorFromFile(attr, p, result);
			}

			result.getVariants().add(d);
		}
		try {
			Element dropdawnlistsRoot = (Element)dropDawnListsRoot.evaluate(root, XPathConstants.NODE);
			if (dropdawnlistsRoot != null) {
				NodeList dropdawnlistNodes = (NodeList)dropDawnList.evaluate(dropdawnlistsRoot, XPathConstants.NODESET);
				HashMap<String, Collection<String>> mapDropDawnList = new HashMap<String, Collection<String>>(); 
				for (int i = 0; i < dropdawnlistNodes.getLength(); ++i) {
					Element dropdawnlistElem = (Element)dropdawnlistNodes.item(i);
					String id = dropdawnlistElem.getAttribute("id");
					if (!"".equals(id)) {
						Collection<String> listDropDawn = new HashSet<String>();
						NodeList listDropDawnNodes = (NodeList)xpath.compile("./item").evaluate(dropdawnlistElem, XPathConstants.NODESET);
						for (int j = 0; j < listDropDawnNodes.getLength(); ++j) {
							Element itemElem = (Element)listDropDawnNodes.item(j);
							String valueDropDawn = itemElem.getAttribute("value");
							if (!"".equals(valueDropDawn)) {
								listDropDawn.add(valueDropDawn);
							}
						}
						mapDropDawnList.put(id, listDropDawn);
					}	
				}
				result.setDropDownItems(mapDropDawnList);
			}
		} catch(Exception e) {
			logger.error("Error parse tag: ./dropdawnlist in xml. ", e);
		}
		try {
			HashMap<String, String> mapItemsTemplatesHS = new HashMap<String, String>();
			HashMap<String, String> mapItemsAttributesHS = new HashMap<String, String>();
			Element mapItemsRoot = (Element)mapItems.evaluate(root, XPathConstants.NODE);
			if (mapItemsRoot != null) {
				setItems(mapItemsRoot, mapItemsTemplates, mapItemsTemplatesHS);
				setItems(mapItemsRoot, mapItemsAttributes, mapItemsAttributesHS);
				result.setMapItemsTemplates(mapItemsTemplatesHS);
				result.setMapItemsAttributes(mapItemsAttributesHS);
			}

		} catch(Exception e) {
			logger.error("Error parse tag: ./mapItems/templates in xml. ", e);
		}
		return result;
	}

	private void setItems(Element mapItemsRoot, XPathExpression expr, HashMap<String, String> map) throws XPathExpressionException {
		NodeList mapItemsTemplatesNodes = (NodeList)expr.evaluate(mapItemsRoot, XPathConstants.NODESET);
		for (int i = 0; i < mapItemsTemplatesNodes.getLength(); ++i) {
			Element mapItemsTemplatesElem = (Element)mapItemsTemplatesNodes.item(i);
			if (mapItemsTemplatesElem != null) {
				NodeList itemNodes = (NodeList)xpath.compile("./item").evaluate(mapItemsTemplatesElem, XPathConstants.NODESET);
				if (itemNodes != null)
				for (int j = 0; j < itemNodes.getLength(); ++j) {
					Element itemElem = (Element)itemNodes.item(j);
					String key = itemElem.getAttribute("key");
					String value = itemElem.getAttribute("value");
					if (!("".equals(key) || "".equals(value) || key == null || value == null)) {
						map.put(key, value);
					}
				}
			}
		}
	}
	
	private CardLinkPickerVariantDescriptor readVariantDescriptorFromFile(Attribute attr, String filename, 
			CardLinkPickerDescriptor result) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException, DataException, ServiceException, ParseException {
		InputStream stream = Portal.getFactory().getConfigService().loadConfigFile(AttributeEditorFactory.CONFIG_FOLDER + filename);
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
		Element root = doc.getDocumentElement();
		Messages messages = messagesReader.read((Element)messagesExpr.evaluate(root, XPathConstants.NODE));
		Element variantElem = (Element)variantsExpr.evaluate(root, XPathConstants.NODE);
		return readVariantDescriptorFromNode(variantElem, attr, messages, result);
	}

	private CardLinkPickerVariantDescriptor readVariantDescriptorFromNode(Element variantElem, Attribute attr, 
			Messages messages, CardLinkPickerDescriptor result) throws DataException, ServiceException, XPathExpressionException, ParseException {
		CardLinkPickerVariantDescriptor d = new CardLinkPickerVariantDescriptor();

		
		if(result.isLocalChoice() && result.getDefaultVariantDescriptor() == null){
			result.setDefaultVariantDescriptor(d);
		}
		
		String p = variantElem.getAttribute("choiceValue");
		if ("".equals(p)) {
			if (result.getDefaultVariantDescriptor() != null) {
				throw new IllegalStateException("Multiple default variant definitions found");
			}
			d.setChoiceReferenceValueId(null);
			boolean hideAllValues = Boolean.valueOf(variantElem.getAttribute("hideAllValues"));
			d.setHideAllValues(hideAllValues);
			result.setDefaultVariantDescriptor(d);
		} else {
			if(result.isLocalChoice()){
				d.setChoiceReferenceValueId(new ObjectId(ReferenceValue.class, p));
			} else {
				d.setChoiceReferenceValueId(ObjectIdUtils.getObjectId(ReferenceValue.class, p, true));
			}
		}
		
		p = variantElem.getAttribute("title");
		if ("".equals(p)) {
			if(result.isLocalChoice()){
				throw new IllegalStateException("RadioButtonTitle must be set in local choice mode");
			}
		} else {
			d.setTitle(messages.getMessage(p).getValue());
		}
		
		ObjectId searchAttrId = ObjectIdUtils.getObjectId(
			AttrUtils.getAttrClass(variantElem.getAttribute("searchAttrType")),
			variantElem.getAttribute("searchAttr"),
			false
		);
		d.setSearchAttrId(searchAttrId);
		
		if(variantElem.getAttribute("requiredPermissions") != null) {
			d.setRequiredPermissions(convertToUserPermission(variantElem.getAttribute("requiredPermissions")));
		}

		final List<SearchResult.Column> columns = SearchXmlHelper.readColumnsDefinition(variantElem, "./columns/column");
		final List<SearchResult.Column> resultColumns = SearchUtils.initializeColumns(columns, serviceBean); 
		d.setColumns(SearchUtils.getNotReplaceColumns(resultColumns));
		Search search = new Search();
		SearchXmlHelper.initFromNode(search, (Element)searchExpr.evaluate(variantElem, XPathConstants.NODE));
		search.setColumns(resultColumns);
		if (PersonAttribute.class.equals(searchAttrId.getType())) {
			search.addPersonAttribute(searchAttrId, Person.ID_CURRENT);
		} else {
			search.addStringAttribute(searchAttrId);
		}
		search.getFilter().setCurrentUserRestrict(d.getRequiredPermissions());
		d.setSearch(search);

		// ��������� ������ �������� type
		final Element searchDependencies = (Element)searchDependenciesExpr.evaluate(variantElem, XPathConstants.NODE);
		if (searchDependencies!=null&&searchDependencies.hasAttribute("type"))
			d.setUseSoftSearch(searchDependencies.getAttribute("type").equals("softDependency"));
		final NodeList dependencies = (NodeList)dependenciesExpr.evaluate(variantElem, XPathConstants.NODESET);
		final List<SearchDependency> depList = new ArrayList<SearchDependency>(dependencies.getLength());
		for (int i = 0; i < dependencies.getLength(); ++i) {
			Element elem = (Element)dependencies.item(i);
			//������ �������� alternative 
			boolean alternative = false;
			if(elem.hasAttribute("alternative")&&elem.getAttribute("alternative").equals("true")){
				d.setUseAltSearch(true);
				alternative = true;
			}
			if (elem.hasAttribute("userParam"))
				depList.add(new SearchDependency(
					SearchXmlHelper.safeMakeId(elem.getAttribute("filterAttrId")),
					elem.getAttribute("userParam"),alternative
				));
			else {
				SearchDependency sd = null;
				Element mapperElement = (Element) elem.getElementsByTagName("mapper").item(0); 
				if (mapperElement != null) {
					String mapperClassPath = null;
					Map<String, String> mapperParameterMap = new HashMap<String, String>();
					mapperClassPath = mapperElement.getAttribute("class");
					NodeList mapperChildList = mapperElement.getElementsByTagName("parameter");
					for (int k = 0; k < mapperChildList.getLength(); k++) {
						Element parameterElement = (Element)mapperChildList.item(k);
						mapperParameterMap.put(parameterElement.getAttribute("name"), parameterElement.getTextContent());
					}

					Class valueAttrClass = CardLinkAttribute.class;
					if (elem.hasAttribute("valueAttributeClass")) {
						try {
							valueAttrClass = Class.forName("com.aplana.dbmi.model." + elem.getAttribute("valueAttributeClass"));
						}
						catch (ClassNotFoundException e) {
							throw new DataException("Can't get valueAttributeClass");
						}
					}
					sd = new SearchDependency(
							ObjectIdUtils.getObjectId(CardLinkAttribute.class, elem.getAttribute("filterAttrId"), false),
							ObjectIdUtils.getObjectId(valueAttrClass, elem.getAttribute("valueAttrId"), false),
							mapperClassPath, mapperParameterMap, elem.hasAttribute("alternative"));
				} else {
					Class valueAttrClass = CardLinkAttribute.class;
					if (elem.hasAttribute("valueAttributeClass")) {
						try {
							valueAttrClass = Class.forName("com.aplana.dbmi.model." + elem.getAttribute("valueAttributeClass"));
						}
						catch (ClassNotFoundException e) {
							throw new DataException("Can't get valueAttributeClass");
						}
					}
					sd = new SearchDependency(
							ObjectIdUtils.getObjectId(CardLinkAttribute.class, elem.getAttribute("filterAttrId"), false),
							ObjectIdUtils.getObjectId(valueAttrClass, elem.getAttribute("valueAttrId"), false),
							elem.getAttribute("mapperClass"),
							null,
							elem.hasAttribute("alternative")
						);
				}

				if (sd != null) {
					if (elem.hasAttribute("referenceToCard")) {
						sd.setUseParent(true);
					// TODO ����������� ������� �� ������� ����������/���������, ��������� SearchDependency.referenceToCard
					}
					depList.add(sd);
				}
			}
		}
		d.setSearchDependencies(depList);

		if ((!search.isByAttributes() || search.isBySql()) && !depList.isEmpty()) {
			//���� ���� �� ���� �� ���� ������ �� �������� ������������� �������, �� ���������� �����������
			for (SearchDependency sd : depList) {
				if (sd.getMapperClassPath() == null || sd.getMapperClassPath().isEmpty()) {
					throw new IllegalArgumentException("Search by attributes required to correctly use search dependencies");
				}
			}
		}

		Element hierarchyDialogElem = (Element)hierarchyDialogExpr.evaluate(variantElem, XPathConstants.NODE);
		if (hierarchyDialogElem != null) {
			HierarchyDescriptor hd = hierarchyDescriptorReader.readFromNode(hierarchyDialogElem, messages, serviceBean);
			d.setHierarchyDescriptor(hd);
			CardFilterCondition cond = conditionReader.readCondition(
				(Element)selectableCardsExpr.evaluate(hierarchyDialogElem, XPathConstants.NODE),
				serviceBean
			);
			d.setSelectableCardsCondition(cond);
			
			readStyling(hd, hierarchyDialogElem, serviceBean);

			// ��������� ��������� actionHandler'� ��� ����������� ������ 'Ok' � 'Cancel'
			ActionHandlerDescriptor ad = new ActionHandlerDescriptor();
			ad.setId(CardLinkPickerAttributeEditor.ACTION_ACCEPT);
			ad.setCondition(cond);
			ad.setHandlerClass(DummyActionHandler.class);
			ad.setSelectionType(attr.isMultiValued() ? SelectionType.MULTIPLE : SelectionType.SINGLE);
			ad.setTitle(d.getHierarchyDescriptor().getMessages().getMessage("acceptTitle"));
			hd.getActionsDescriptor().addItem(ad);

			ad = new ActionHandlerDescriptor();
			ad.setId(CardLinkPickerAttributeEditor.ACTION_CANCEL);
			ad.setCondition(null);
			ad.setHandlerClass(DummyActionHandler.class);
			ad.setSelectionType(SelectionType.NONE);
			ad.setTitle(d.getHierarchyDescriptor().getMessages().getMessage("cancelTitle"));
			hd.getActionsDescriptor().addItem(ad);
		}
		d.setConditions(
			readVariantConditionsFromNode(
					(Element)variantConditionsExpr.evaluate(variantElem, XPathConstants.NODE)));
		
		
		Element listElement = (Element) listExpr.evaluate(variantElem, XPathConstants.NODE);
		if(listElement != null){
			LinkDescriptor ld = new LinkDescriptor();
			String link = listElement.getAttribute("linkAttr");
			ld.setCardLinkAttr(ObjectIdUtils.getObjectId(
					Arrays.asList(PersonAttribute.class, CardLinkAttribute.class, TypedCardLinkAttribute.class), CardLinkAttribute.class, link, false
			));
			String reversed = listElement.getAttribute("linkReversed");
			ld.setReverse("true".equalsIgnoreCase(reversed));
			String skipNextIfFound = listElement.getAttribute("skipNextIfFound");
			ld.setSkipNextIfFound("true".equalsIgnoreCase(skipNextIfFound));
			String template = listElement.getAttribute("template");
			ld.setTemplates(IdUtils.stringToAttrIds(Template.class, template));
			String states = listElement.getAttribute("status");
			ld.setStatuses(IdUtils.stringToAttrIds(CardState.class, states));
			String recursive = listElement.getAttribute("recursive");
			ld.setRecursive(Boolean.parseBoolean(recursive));
			d.setList(ld);
		}

		Element replaceElement = (Element) replaceExpr.evaluate(variantElem, XPathConstants.NODE);
		if(replaceElement != null){
			ReplaceDescriptor rd = new ReplaceDescriptor();
			String attrId = replaceElement.getAttribute("anotherAttrId");
			rd.setAnotherAttrId(ObjectIdUtils.getObjectId(CardLinkAttribute.class, attrId, false));
			String templateId = replaceElement.getAttribute("templateId");
			rd.setTemplateId(ObjectIdUtils.getObjectId(Template.class, templateId, false));
			d.setReplaceAttr(rd);
		}
		return d;
	}

	private CardLinkPickerVariantCondition readVariantConditionsFromNode(Element conditionsElem) throws DataException, ServiceException, XPathExpressionException, ParseException {
		CardLinkPickerVariantCondition vc = new CardLinkPickerVariantCondition();
		if (conditionsElem != null){
			NodeList conditionNodes = (NodeList)conditionExpr.evaluate(conditionsElem,
																	XPathConstants.NODESET);
			for (int i = 0; i < conditionNodes.getLength(); ++i) {
				Element variantElem = (Element)conditionNodes.item(i);
				String p = variantElem.getAttribute("attrId");
				if ("".equals(p))
					continue;
				// TODO ���������� ������ ����������� ������������ ��� ���� ���������
				// ObjectId attrId = ObjectId.predefined(ListAttribute.class, p);
				ObjectId attrId = new ObjectId(ListAttribute.class, p);
				p = variantElem.getAttribute("values");
				if ("".equals(p))
					continue;
				vc.addCondition(attrId, ObjectIdUtils.commaDelimitedStringToNumericIds(p, ReferenceValue.class));
			}
		}

		return vc;
	}

	private Long convertToUserPermission(String requiredPermissons) {

		Long userPermisions = Filter.CU_READ_PERMISSION;

		if ("none".equalsIgnoreCase(requiredPermissons)) {
			userPermisions = Filter.CU_DONT_CHECK_PERMISSIONS;
		} else if ("read".equalsIgnoreCase(requiredPermissons)) {
			userPermisions = Filter.CU_READ_PERMISSION;
		} else if ("write".equalsIgnoreCase(requiredPermissons)) {
			userPermisions = Filter.CU_WRITE_PERMISSION;
		}
		return userPermisions;
	}
	
	private void readStyling(HierarchyDescriptor hd, Element rootElem, DataServiceBean serviceBean) throws XPathExpressionException {
		Element styling = (Element)xpath.evaluate("./styling", rootElem, XPathConstants.NODE);
		if (styling == null) {
			hd.setStyles(new ArrayList(0));
			return;
		}
		NodeList styles = (NodeList)xpath.evaluate("./style", styling, XPathConstants.NODESET);
		List result = new ArrayList(styles.getLength());
		for (int i = 0; i < styles.getLength(); ++i) {
			Element style = (Element)styles.item(i);
			StylingDescriptor d = new StylingDescriptor();
			String attr = style.getAttribute("value");
			d.setStyle("".equals(attr) ? null : attr);
			attr = style.getAttribute("icon");
			d.setIconPath("".equals(attr) ? null : attr);
			d.setCondition(conditionReader.readCondition(style, serviceBean));
			result.add(d);
		}
		hd.setStyles(result);
	}
}
