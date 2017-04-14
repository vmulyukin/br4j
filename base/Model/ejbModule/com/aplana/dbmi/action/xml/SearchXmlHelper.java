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
package com.aplana.dbmi.action.xml;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.Search.*;
import com.aplana.dbmi.action.Search.IntegerSearchConfigValue.SearchType;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.model.util.AttrUtils;
import com.aplana.dbmi.model.util.Init;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.xpath.domapi.XPathEvaluatorImpl;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.xpath.XPathEvaluator;
import org.w3c.dom.xpath.XPathResult;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Utility class used to initialize/store {@link Search}
 * object from/into a XML string.
 * <br>
 * In most cases it is preferable not to use this class directly, but use
 * {@link Search#initFromXml(InputStream)} and {@link Search#storeToXml(OutputStream)}
 * instead.
 * <br>
 * Format of XML files used to store serialized {@link Search} object
 * could be described by XSD schema located in /doc/search.xsd
 */
public class SearchXmlHelper
{
	private static final String SEARCH_TYPE = "searchType";
	private static final String ALL_PERMISSIONS = "allPermissions";
	private static final String TAG_ROOT = "search";
	private static final String TAG_NAME = "name";
	private static final String TAG_WORDS = "words";
	private static final String TAG_STATUS = "status";
	private static final String TAG_TEMPLATE = "template";
	//�� ��������� -1
	//������/������ 0
	//������ 2
	//������ 3
	private static final String TAG_PERMISSION = "permission";
	private static final String TAG_MATERIAL = "material";
	private static final String TAG_ATTRIBUTE = "attribute";
	private static final String TAG_VALUE = "value";
	private static final String TAG_FETCH = "fetch";
	private static final String TAG_COLUMN = "column";
	private static final String ATTR_LANG = "lang";
	
	private static final String ATTR_BY_CODE = "byCode";
	private static final String ATTR_BY_ATTRIBUTES = "byAttr";
	private static final String ATTR_BY_MATERIAL = "byText";
	private static final String ATTR_BY_SPECIAL_SQL = "specialSQL";
	private static final String ATTR_PARAM_SPECIAL_SQL = "parametersSQL";
	private static final String ATTR_CUSTOM_CARD_SORT_ORDER = "customCardSortOrder";
	
	private static final String ATTR_ID = "id";
	private static final String ATTR_TYPE = "type";
	private static final String ATTR_VAR = "var";
	private static final String ATTR_CONDITION = "condition";
	private static final String ATTR_SORT = "sort";
	private static final String ATTR_SORTABLE = "sortable";
	private static final String ATTR_SORT_ATTR_ID = "sortAttrId";
	private static final String ATTR_SORT_ORDER = "sortOrder";
	private static final String ATTR_WIDTH = "width";
	private static final String ATTR_TEXT_LENGTH="textLength";
	private static final String ATTR_LINK = "link";
	private static final String ATTR_BACK = "back";
	private static final String ATTR_LIST_ORDER_TYPE = "listValueOrder";
	private static final String ATTR_LINK_CARDS_STATES_IGNORE = "linkCardsStatesIgnore";

	/**
	 * 29.09.09 add download action to search 
	 */
	private static final String ATTR_ACTION = "action";
	
	/**
	 * (2009/12/10, RuSA) add display captions for card links
	 */
	private static final String ATTR_LABEL_ATTR_ID = "labelAttrId";
	private static final String ATTR_IS_REPLACE_VALUE_EMPTY="isReplaceValueEmpty";
	private static final String ATTR_FULL_REPLACE_ATTR_ID = "fullReplaceAttrId";// (YNikitin, 2012/08/03) ������ ���������� �������� ��������, ������ �������� ����� ���������� ������ ������� (�������� ������ ���� �������� ����� �����������)
	private static final String ATTR_REPLACE_STATUS_ID = "replaceStatusId"; 	// (YNikitin, 2012/08/03) ������ ������� ��������, ��� ������� ������ ������� ����� �������������� ������ ����������
	private static final String ATTR_IS_PARENT_NAME = "isParentName";			// (YNikitin, 2011/04/20) ��������� ����������� ������������ ������ �������, ������������ �������� �� ��������� ��������
	private static final String ATTR_SECONDARY_COLUMN = "secondaryColumn";		// (PPolushkin, 2014/08/07) ��������� ������������ �������������� ������� ��� ������ ������ � ����������� �� ������� secondaryColumnsSingleCond
	private static final String ATTR_SECONDARY_COLUMNS_SINGLE_COND = "secondaryColumnsSingleCond"; // (PPolushkin, 2014/08/07) ������� ��� ������������� secondaryColumn. ����� � ������� �������� �������� ������� ������� or ��� and
	private static final String ATTR_TITLE_RU = "columnTitleRu";
	private static final String ATTR_TITLE_EN = "columnTitleEn";
	private static final String ATTR_NULL_VALUE_RU = "nullValueRu";
	private static final String ATTR_NULL_VALUE_EN = "nullValueEn";
	
	/* ��������� ��� ���������� ������� */
	private static final String COND_STATE = "state";
	private static final String COND_TEMPLATE = "template";
	
	private static final String ATTR_VALUE_TYPE_START = "start";
	private static final String ATTR_VALUE_TYPE_END = "end";
	
	/* the attribute represents whether we need to set time to zero (cut it) in date attribute
		true - setting time in date to zero 00:00:00
	*/ 
	private static final String ATTR_ZERO_TIME = "zeroTime";
	
	// private static final String ATTR_VALUE_COND_EQUALS = "equals";
	private static final String ATTR_VALUE_COND_EMPTY = "empty";
	private static final String ATTR_VALUE_COND_EXIST = "exist";
	private static final String ATTR_LANG_RU = "ru";
	private static final String ATTR_LANG_EN = "en";
	private static final String ATTR_VAR_CURRENT = "current";
	private static final String ATTR_VAR_CURRENT_YEAR = "currentYear";
	//private static final String ATTR_SORT_NONE = "none";
	private static final String ATTR_SORT_ASC = "asc";
	private static final String ATTR_SORT_DESC = "desc";
	private static final String ATTR_LIST_ORDER_TYPE_STRING_NAME = "byName";
	private static final String ATTR_LIST_ORDER_TYPE_ORDER_IN_LEVEL = "byOrderInLevel";
	private static final String ATTR_TIMEPATTERN = "timePattern";	
	private static final String ATTR_USEDEFAULT_TIME_PATTERN = "useDefaultTimePattern";	
	private static final String ATTR_EXCEL_IGNORE = "excelIgnore";
	private static final String ATTR_EXPORT_ONLY = "exportOnly";
	
	private static final String ATTR_ACTION_DOWNLOAD = "download";
	private static final String ATTR_HIDDEN = "hidden";
	
	private static final DateFormat FORMAT_DATE = new SimpleDateFormat("yyyyMMdd");
	
	/**
	 * Initializes given empty {@link Search} object with data from XML file
	 * @param search empty {@link Search} to be initialized
	 * @param xml InputStream containing XML representation of {@link Search} object
	 * @throws DataException in case of any error occurred during XML processing
	 * @see Search#initFromXml(InputStream)
	 */
	public static void initFromXml(Search search, InputStream xml) throws DataException
	{
		try {
			final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml);
			final Element searchElem = doc.getDocumentElement();
			if (!TAG_ROOT.equals(searchElem.getNodeName()))
				throw new Exception(TAG_ROOT + " element expected");
			initFromNode(search, searchElem);
		} catch (Exception e) {
			throw new DataException("action.search.init", e);
		}
	}
	
	/**
	 * Initializes given empty {@link Search} object with data from XML element
	 * @param search empty {@link Search} to be initialized
	 * @param searchElem xml element containing search settings
	 * @throws ParseException 
	 */
	public static void initFromNode(Search search, Element searchElem) throws ParseException {
		
		search.setNameRu(getTagContent(searchElem, "./" + TAG_NAME, ATTR_LANG, ATTR_LANG_RU));
		search.setNameEn(getTagContent(searchElem, "./" + TAG_NAME, ATTR_LANG, ATTR_LANG_EN));
		search.setWords(getTagContent(searchElem, "./" + TAG_WORDS, null, null));
		
		search.setByCode(getAttributeBoolean(searchElem, "./@" + ATTR_BY_CODE));
		search.setByAttributes(getAttributeBoolean(searchElem, "./@" + ATTR_BY_ATTRIBUTES));
		search.setByMaterial(getAttributeBoolean(searchElem, "./@" + ATTR_BY_MATERIAL));
		search.setSqlXmlName(searchElem.getAttribute(ATTR_BY_SPECIAL_SQL));
		search.setSqlParametersName(searchElem.getAttribute(ATTR_PARAM_SPECIAL_SQL));
		
		Collection<Node> nodes = getNodeList(searchElem, "./" + TAG_STATUS, null, null);
		if (nodes.size() == 0)
			search.setStates(new ArrayList());
		else {
			final ArrayList<Object> states = new ArrayList<Object>(nodes.size());
			for (Node node : nodes) {
				String state = getTagContent(node);
				ObjectId id = ObjectId.predefined(CardState.class, state);
				states.add(id == null ? state : id.getId());
			}
			search.setStates(states);
		}
		nodes = getNodeList(searchElem, "./" + TAG_TEMPLATE, null, null);
		if (nodes.size() == 0)
			search.setTemplates(new ArrayList());
		else {
			final ArrayList<Template> templates = new ArrayList<Template>(nodes.size());
			for (Node node : nodes) {
				final String id = getTagContent(node);
				ObjectId tplId = ObjectId.predefined(Template.class, id);
				if (tplId == null)
					tplId = new ObjectId(Template.class, Long.parseLong(id));
				templates.add(DataObject.<Template>createFromId(tplId));
			}
			search.setTemplates(templates);
		}
		nodes = getNodeList(searchElem, "./" + TAG_PERMISSION, null, null);
		if (nodes.size() == 0)
			search.getFilter().setCurrentUserRestrict(-1l);
		else {
			final String perm = getTagContent(nodes.iterator().next());
			search.getFilter().setCurrentUserRestrict(Long.valueOf(perm));
		}
		nodes = getNodeList(searchElem, "./" + TAG_MATERIAL, null, null);
		if (nodes.size() == 0)
			search.setMaterialTypes(new ArrayList());
		else {
			final ArrayList<Integer> mtrl_types = new ArrayList<Integer>(nodes.size());
			for (Node node : nodes) {
				final String type = getTagContent(node);
				final ObjectId id = ObjectId.predefined(MaterialType.class, type);
				mtrl_types.add(id == null ? new Integer(type) : Integer.valueOf(id.getId().toString()));
			}
			search.setMaterialTypes(mtrl_types);
		}
		nodes = getNodeList(searchElem, "./" + TAG_ATTRIBUTE, null, null);
		for (Node node : nodes) {
			String id = ((Element) node).getAttribute(ATTR_ID);

			final String type = ((Element) node).getAttribute(ATTR_TYPE);
			final Class clazz = AttrUtils.getAttrClass(type);
			ObjectId attrId = ObjectId.predefined(clazz, id);
			if (attrId == null)
				attrId = new ObjectId(clazz, id);

			id = (String) attrId.getId();

			if (!getNodeList(node, "./" + TAG_VALUE, ATTR_CONDITION, ATTR_VALUE_COND_EMPTY).isEmpty()) {
				search.addAttribute(attrId, EmptyAttribute.INSTANCE);
			} else if (!getNodeList(node, "./" + TAG_VALUE, ATTR_CONDITION, ATTR_VALUE_COND_EXIST).isEmpty()) {
				search.addAttribute(attrId, ExistAttribute.INSTANCE);
			} else if (StringAttribute.class.equals(clazz) || TextAttribute.class.equals(clazz)) {
				Element valueNode = (Element) getTagNode(node, "./" + TAG_VALUE, null, null);
				if (valueNode == null)
					search.addAttribute(attrId, Boolean.TRUE);
				else {
					Attr searchTypeAttr = valueNode.getAttributeNode(SEARCH_TYPE);
					search.addStringAttribute(attrId, valueNode.getFirstChild().getNodeValue(), Integer.parseInt(searchTypeAttr.getValue()));
				}
			} else if (IntegerAttribute.class.equals(clazz)) {
				Element valueNode = (Element) getTagNode(node, "./" + TAG_VALUE, null, null);
				Attr searchTypeAttr = valueNode.getAttributeNode(SEARCH_TYPE);
				if (searchTypeAttr != null) {
					search.addIntegerAttribute(attrId, Integer.parseInt(valueNode.getFirstChild().getNodeValue()), SearchType.valueOf(searchTypeAttr.getValue()));
				} else {
					search.addAttribute(attrId, new Interval(
							Integer.parseInt(getTagContent(node, "./" + TAG_VALUE, ATTR_TYPE, ATTR_VALUE_TYPE_START)),
							Integer.parseInt(getTagContent(node, "./" + TAG_VALUE, ATTR_TYPE, ATTR_VALUE_TYPE_END))));
				}
			} else if (DateAttribute.class.equals(clazz)) {
				search.addAttribute(attrId, new DatePeriod(
						getDateValue(getTagNode(node, "./" + TAG_VALUE, ATTR_TYPE, ATTR_VALUE_TYPE_START)),
						getDateValue(getTagNode(node, "./" + TAG_VALUE, ATTR_TYPE, ATTR_VALUE_TYPE_END))));
			} else if (ListAttribute.class.equals(clazz) || TreeAttribute.class.equals(clazz)) {
				Collection<Node> valNodes = getNodeList(node, "./" + TAG_VALUE, null, null);
				ArrayList<ReferenceValue> values = new ArrayList<ReferenceValue>(valNodes.size());
				for (Node valNode : valNodes) {
					String valueId = getTagContent(valNode);

					try {
						ObjectId refValId = ObjectId.predefined(ReferenceValue.class, valueId);
						if (refValId == null)
							refValId = new ObjectId(ReferenceValue.class, Long.parseLong(valueId));
						values.add(ReferenceValue.<ReferenceValue>createFromId(refValId));
					} catch (NumberFormatException e) { // if value is not a number then considering it's an another value
						values.add(ReferenceValue.newAnotherValue(valueId));
					}
				}
				search.addListAttribute(attrId, values);
			} else if (PersonAttribute.class.equals(clazz)) {
				Collection<Node> valNodes = getNodeList(node, "./" + TAG_VALUE, null, null);
				if (valNodes == null)
					throw new ParseException("Node '" + node.getNodeName() + "' has no 'value'", 0);
				for (Node valNode : valNodes) {
					Element personAttrNode = (Element) valNode;
					if (personAttrNode.hasAttribute(ATTR_VAR)) {
						if (ATTR_VAR_CURRENT.equals(personAttrNode.getAttribute(ATTR_VAR)))
							search.addPersonAttribute(attrId, Person.ID_CURRENT);
						else
							throw new ParseException("Unknown variable name: " + personAttrNode.getAttribute(ATTR_VAR), 0);
					} else {
						String personId = getTagContent(personAttrNode);
						ObjectId personAttrId = new ObjectId(PersonAttribute.class, id);
						ObjectId newPersonId = new ObjectId(Person.class, Long.parseLong(personId));
						search.addPersonAttribute(personAttrId, newPersonId);
					}
				}
			} else if (CardLinkAttribute.class.equals(clazz)) {
				Collection<Node> valNodes = getNodeList(node, "./" + TAG_VALUE, null, null);
				if (valNodes == null)
					throw new ParseException("Node '" + node.getNodeName() + "' has no 'value'", 0);
				for (Node valNode : valNodes) {
					Element cardLinkAttrNode = (Element) valNode;
					String cardId = getTagContent(cardLinkAttrNode);
					ObjectId newCardId = new ObjectId(Card.class, Long.parseLong(cardId));
					search.addCardLinkAttribute(attrId, newCardId);

				}
			} else if (BackLinkAttribute.class.equals(clazz)) {
				Collection<Node> valNodes = getNodeList(node, "./" + TAG_VALUE, null, null);
				if (valNodes == null)
					throw new ParseException("Node '" + node.getNodeName() + "' has no 'value'", 0);
				for (Node valNode : valNodes) {
					Element backLinkAttrNode = (Element) valNode;
					String cardId = getTagContent(backLinkAttrNode);
					ObjectId newCardId = new ObjectId(Card.class, Long.parseLong(cardId));
					search.addBackLinkAttribute(attrId, newCardId);

				}
			}
		}
		
		nodes = getNodeList(searchElem, "./" + TAG_FETCH, null, null);
		if (nodes.size() > 0) {
			Element node = (Element) nodes.iterator().next();
			boolean back = getAttributeBoolean(node, "./@" + ATTR_BACK);
			ObjectId attrId = ObjectId.predefined(back ? BackLinkAttribute.class : CardLinkAttribute.class,
					node.getAttribute(ATTR_LINK));
			if (attrId == null)
				attrId = new ObjectId(back ? BackLinkAttribute.class : CardLinkAttribute.class,
						node.getAttribute(ATTR_LINK));
			search.setFetchLink(attrId);
		}

		search.setColumns(readColumnsDefinition(searchElem, "./" + TAG_COLUMN));

		final String sortOrder = searchElem.getAttribute(ATTR_CUSTOM_CARD_SORT_ORDER);
		if (sortOrder != null && sortOrder.length() > 0)
			search.getFilter().setCustomCardSortOrder( Boolean.parseBoolean(sortOrder));
		setSorting(search);

		/*
		if (!search.getFilter().isCustomCardSortOrder())
			// ������ ���� �� ������ custom-����������
			setSorting(search);
		else {
			search.getFilter().setOrderColumn(null);
		}
		 */
	}

	public static void setSorting(Search search) {
		if (search.getColumns() == null || search.getFilter() == null)
			return;
		search.getFilter().getOrderedColumns().clear();
		int countSortingCol = 0;
		SearchResult.Column sortingCol = null;
		final ArrayList<SearchResult.Column> newColumns = new ArrayList<SearchResult.Column>();
		for (final SearchResult.Column col : search.getColumns()) {
			// ���� ���� ����������
			if (col.getSorting() != SearchResult.Column.SORT_NONE) {
				countSortingCol++;
				if (countSortingCol == 1) {
					sortingCol = col;
				}
				// ���� � ���������� ������� groupId ����� �� ��� � �������
				if (newColumns.size() > 0 && col.getGroupId() > 0) {
					if (col.getGroupId() == newColumns.get(newColumns.size() - 1).getGroupId()) {
						col.setSorting(0);
						countSortingCol--;
					}
				}
			} // if
			newColumns.add(col);
		} // for
		if (countSortingCol == 1 && sortingCol != null) {
			search.getFilter().addOrderColumn(sortingCol.copy(), sortingCol.getSortOrder());
			search.getFilter().sortOrderColumns();
		}
		search.getColumns().clear();
		search.setColumns(newColumns);
	}
	
	/**
	 * @deprecated use {@link AttrUtils#getAttrClass(String)} instead
	 * @param attrType String representation of attribute type
	 * @return type of attribute
	 */
	public static Class getAttrClass(final String attrType) {
		return AttrUtils.getAttrClass(attrType);
	}
	
	public static List<SearchResult.Column> readColumnsDefinition(Node node, String path) {
		final Collection<Node> nodes = getNodeList(node, path, null, null);
		if (nodes.size() > 0) {
			ArrayList<SearchResult.Column> columns = new ArrayList<SearchResult.Column>(nodes.size());
			Iterator<Node> itr = nodes.iterator();
			int groupId = 1;
			while (itr.hasNext()) {
				Element tag = (Element) itr.next();
				SearchResult.Column col = new SearchResult.Column();
				SearchResult.Action act = new SearchResult.Action();
				String id = tag.getAttribute(ATTR_ID);
				if (id.contains(":")) {
					StringTokenizer tokenizer = new StringTokenizer(id, ":");
					while (tokenizer.hasMoreTokens()) {
						col = new SearchResult.Column();
						act = new SearchResult.Action();
						String elementId = tokenizer.nextToken();
						initColumn(elementId, col, tag, act, groupId);
						col.setGroupId(groupId);
						columns.add(col);
					}
					groupId++;
				} else {
					initColumn(id, col, tag, act, groupId);
					columns.add(col);
				}
				
				/*ObjectId attrId = anyAttribute(id);
				// ����� �� ����� ��������� ������������ Attribute.class � �������� ���� � ObjectId				
				// � ������ ������ ��� �������� �� �����!!!
				// ���, � �����, ����� ����� ����� ������������� �� ������ �� ���� ������� � ����� ���������
				// ������� ������ �������������� SQL ��� ��������� ���������� ���� �������� !!!
				col.setAttributeId(attrId == null ? new ObjectId(Attribute.class, id) : attrId);
				if (tag.hasAttribute(ATTR_WIDTH)){
					col.setWidth(Integer.parseInt(tag.getAttribute(ATTR_WIDTH)));
				}
				if (tag.hasAttribute(ATTR_SORT)) {
					final String sort = tag.getAttribute(ATTR_SORT).trim();
					if (ATTR_SORT_ASC.equalsIgnoreCase(sort))
						col.setSorting(SearchResult.Column.SORT_ASCENDING);
					else if (ATTR_SORT_DESC.equalsIgnoreCase(sort))
						col.setSorting(SearchResult.Column.SORT_DESCENGING);
				}
				if (tag.hasAttribute(ATTR_SORT_ATTR_ID)) {
					final String sortAttrId = tag.getAttribute(ATTR_SORT_ATTR_ID).trim();
					col.setSortAttrId( (sortAttrId == null || sortAttrId.length() == 0) 
							? null
							: new ObjectId(Attribute.class, sortAttrId)
						);
				}

				if (tag.hasAttribute(ATTR_LINK)) {
					final String value = tag.getAttribute(ATTR_LINK).trim();
					col.setLinked("true".equalsIgnoreCase(value) ||
							"yes".equalsIgnoreCase(value) ||
							"1".equalsIgnoreCase(value));
				}
				
				if (tag.hasAttribute(ATTR_ISPARENTNAME)) {
					final String value = tag.getAttribute(ATTR_ISPARENTNAME).trim();
					col.setIsParentName("true".equalsIgnoreCase(value) ||
							"yes".equalsIgnoreCase(value) ||
							"1".equalsIgnoreCase(value));
				}
				
				if (tag.hasAttribute(ATTR_TITLE_EN)) {
					final String value = tag.getAttribute(ATTR_TITLE_EN);
					col.setNameEn(value);
					col.setUseGivenTitle(true);
				}
				
				if (tag.hasAttribute(ATTR_NULL_VALUE_EN)) {
					final String value = tag.getAttribute(ATTR_NULL_VALUE_EN);
					col.setNullValueEn(value);
				}

				if (tag.hasAttribute(ATTR_NULL_VALUE_RU)) {
					final String value = tag.getAttribute(ATTR_NULL_VALUE_RU);
					col.setNullValueRu(value);
				}

				if (tag.hasAttribute(ATTR_SHOW_TIME)) {
					final String value = tag.getAttribute(ATTR_SHOW_TIME);
					col.setShowTime("true".equalsIgnoreCase(value) ||
							"yes".equalsIgnoreCase(value) ||
							"1".equalsIgnoreCase(value));
				}
				
				if (tag.hasAttribute(ATTR_TITLE_RU)) {
					final String value = tag.getAttribute(ATTR_TITLE_RU);
					col.setNameRu(value);
					col.setUseGivenTitle(true);
				}

				if (tag.hasAttribute(ATTR_LABELATTRID)) {
					final String labelID = tag.getAttribute(ATTR_LABELATTRID).trim();
					col.setFullLabelAttrId(labelID);	// ��������� ����� � ������� �������� �������� ATTR_LABELATTRID � ����� �������� 
					// if (caption.length() > 1)
					final String[] pathPcs = labelID.split("->");
					if (pathPcs.length > 1){
						if (col.getPathToLabelAttr() == null)
							col.setPathToLabelAttr(new ArrayList());
						col.getPathToLabelAttr().clear();
						for (int i=0; i<=pathPcs.length-2; i++){
							col.getPathToLabelAttr().add(safeMakeId(pathPcs[i].trim()));
						}
						col.setLabelAttrId(safeMakeId(pathPcs[pathPcs.length-1].trim()));
					} else {
					col.setLabelAttrId( safeMakeId(labelID));
						col.setPathToLabelAttr(null);
					}
				}
				
				if (tag.hasAttribute(ATTR_LIST_ORDER_TYPE)) {
					final String valOrder = tag.getAttribute(ATTR_LIST_ORDER_TYPE).trim();
					if (ATTR_LIST_ORDER_TYPE_STRING_NAME.equalsIgnoreCase(valOrder))
						col.setValueOrder(SearchResult.Column.LIST_ORDER_SORT_BY_STRING_NAME);
					else if (ATTR_LIST_ORDER_TYPE_ORDER_IN_LEVEL.equalsIgnoreCase(valOrder))
						col.setValueOrder(SearchResult.Column.LIST_ORDER_SORT_BY_ORDER_IN_LEVEL);
				}
				//������� ���� ��� ���������� �� value_rus/value_eng ��� ReferenceAttribute
				else {
					col.setValueOrder(SearchResult.Column.LIST_ORDER_SORT_BY_STRING_NAME);
				}
				*/
				if (tag.hasAttribute(ATTR_ACTION)){
					final String action = tag.getAttribute(ATTR_ACTION).trim();
					if(ATTR_ACTION_DOWNLOAD.equals(action))
						col.setDownloadMaterial(true);
				}
				/*
				setIcons(tag, col);
				columns.add(col);*/
			}
			// �������� � ����� ������ ������� ��� �� � �������������� �������
			List<SearchResult.Column> secondaryColumns = new ArrayList<SearchResult.Column>();
			for (SearchResult.Column itc : columns) {
				if (!CollectionUtils.isEmpty(itc.getSecondaryColumns())) {
					secondaryColumns.addAll(itc.getSecondaryColumns());
				}
			}
			columns.addAll(secondaryColumns);
			return columns;
		}
		return null;
	}
	
		
		private static void initColumn(String id, SearchResult.Column col, Element tag, SearchResult.Action act, int groupId) {
			ObjectId attrId = anyAttribute(id);
			// ����� �� ����� ��������� ������������ Attribute.class � �������� ���� � ObjectId				
			// � ������ ������ ��� �������� �� �����!!!
			// ���, � �����, ����� ����� ����� ������������� �� ������ �� ���� ������� � ����� ���������
			// ������� ������ �������������� SQL ��� ��������� ���������� ���� �������� !!!
			col.setAttributeId(attrId == null ? new ObjectId(Attribute.class, id) : attrId);
			if (tag.hasAttribute(ATTR_WIDTH)){
				col.setWidth(Integer.parseInt(tag.getAttribute(ATTR_WIDTH)));
			}
			if (tag.hasAttribute(ATTR_TEXT_LENGTH)){
				col.setTextLength(Integer.parseInt(tag.getAttribute(ATTR_TEXT_LENGTH)));
			}
			if (tag.hasAttribute(ATTR_SORT_ORDER)) {
				int sortOrder = Integer.parseInt(tag.getAttribute(ATTR_SORT_ORDER).trim());
				col.setSortOrder(sortOrder);
			}
			if (tag.hasAttribute(ATTR_SORT)) {
				final String sort = tag.getAttribute(ATTR_SORT).trim();
				if (ATTR_SORT_ASC.equalsIgnoreCase(sort))
					col.setSorting(SearchResult.Column.SORT_ASCENDING);
				else if (ATTR_SORT_DESC.equalsIgnoreCase(sort))
					col.setSorting(SearchResult.Column.SORT_DESCENGING);
			}
			if (tag.hasAttribute(ATTR_SORTABLE)) {
				final String value = tag.getAttribute(ATTR_SORTABLE).trim();
				col.setSortable("true".equalsIgnoreCase(value) ||
						"yes".equalsIgnoreCase(value) ||
						"1".equalsIgnoreCase(value));
			}
			if (tag.hasAttribute(ATTR_SORT_ATTR_ID)) {
				final String sortAttrPaths = tag.getAttribute(ATTR_SORT_ATTR_ID).trim();
				List<List<ObjectId>> sortAttrPathsList = new ArrayList<List<ObjectId>>();
				for(String sortAttrPath: sortAttrPaths.split(";")){
					List<ObjectId> sortAttrPathList = new ArrayList<ObjectId>();
					for(String sortAttrNode: sortAttrPath.trim().split(Attribute.LABEL_ATTR_PARTS_SEPARATOR)){
						if(sortAttrNode.trim().length()>0){
							sortAttrPathList.add(safeMakeId(sortAttrNode.trim()));
						}
					}
					if(sortAttrPathList.size()>0){
						sortAttrPathsList.add(sortAttrPathList);
					}
				}
				col.setSortAttrPaths(sortAttrPathsList);
			}
			
			if (tag.hasAttribute(ATTR_LINK)) {
				final String value = tag.getAttribute(ATTR_LINK).trim();
				col.setLinked("true".equalsIgnoreCase(value) ||
						"yes".equalsIgnoreCase(value) ||
						"1".equalsIgnoreCase(value));
			}
			
			if (tag.hasAttribute(ATTR_HIDDEN)) {
				final String value = tag.getAttribute(ATTR_HIDDEN).trim();
				col.setHidden("true".equalsIgnoreCase(value) ||
						"yes".equalsIgnoreCase(value) ||
						"1".equalsIgnoreCase(value));
			}
			
			if (tag.hasAttribute(ATTR_IS_PARENT_NAME)) {
				final String value = tag.getAttribute(ATTR_IS_PARENT_NAME).trim();
				col.setIsParentName("true".equalsIgnoreCase(value) ||
						"yes".equalsIgnoreCase(value) ||
						"1".equalsIgnoreCase(value));
			}
			
			if (tag.hasAttribute(ATTR_TITLE_EN)) {
				final String value = tag.getAttribute(ATTR_TITLE_EN);
				col.setNameEn(value);
				col.setUseGivenTitle(true);
			}
			
			if (tag.hasAttribute(ATTR_NULL_VALUE_EN)) {
				final String value = tag.getAttribute(ATTR_NULL_VALUE_EN);
				col.setNullValueEn(value);
			}

			if (tag.hasAttribute(ATTR_NULL_VALUE_RU)) {
				final String value = tag.getAttribute(ATTR_NULL_VALUE_RU);
				col.setNullValueRu(value);
			}

			if (tag.hasAttribute(ATTR_TIMEPATTERN)) {
				final String value = tag.getAttribute(ATTR_TIMEPATTERN);
				col.setTimePattern(value);
			}
			
			if (tag.hasAttribute(ATTR_USEDEFAULT_TIME_PATTERN)) {
				final String value = tag.getAttribute(ATTR_USEDEFAULT_TIME_PATTERN);
				col.setUseDefaulTimetPattern("true".equalsIgnoreCase(value) ||
						"yes".equalsIgnoreCase(value) ||
						"1".equalsIgnoreCase(value));
			}
			
			if (tag.hasAttribute(ATTR_TITLE_RU)) {
				final String value = tag.getAttribute(ATTR_TITLE_RU);
				col.setNameRu(value);
				col.setUseGivenTitle(true);
			}

			if (tag.hasAttribute(ATTR_LABEL_ATTR_ID)) {
				final String labelID = tag.getAttribute(ATTR_LABEL_ATTR_ID).trim();
				col.setFullLabelAttrId(labelID);	// ��������� ����� � ������� �������� �������� ATTR_LABEL_ATTR_ID � ����� �������� 
				// if (caption.length() > 1)
				final String[] pathPcs = labelID.split(Attribute.LABEL_ATTR_PARTS_SEPARATOR);
				if (pathPcs.length > 1){
					if (col.getPathToLabelAttr() == null)
						col.setPathToLabelAttr(new ArrayList<ObjectId>());
					col.getPathToLabelAttr().clear();
					for (int i=0; i<=pathPcs.length-2; i++){
						col.getPathToLabelAttr().add(safeMakeId(pathPcs[i].trim()));
					}
					col.setLabelAttrId(safeMakeId(pathPcs[pathPcs.length-1].trim()));
				} else {
					col.setLabelAttrId(safeMakeId(labelID));
					col.setPathToLabelAttr(null);
				}
			}
			
			// (YNikitin, 2011/08/06) ����������� ������ �������� ���������� ��������
			if (tag.hasAttribute(ATTR_FULL_REPLACE_ATTR_ID)){
				final String labelID = tag.getAttribute(ATTR_FULL_REPLACE_ATTR_ID).trim();
				col.setFullReplaceAttrId(labelID);	 
			}
			
			// (YNikitin, 2011/08/06) ����������� ������ �������� ��� ������ ���������� ��������
			if (tag.hasAttribute(ATTR_REPLACE_STATUS_ID)){
				final String replaceStatusId = tag.getAttribute(ATTR_REPLACE_STATUS_ID);
				col.setReplaceStatusId((replaceStatusId == null || replaceStatusId.trim().length() == 0)
						? null
						: ObjectIdUtils.getObjectId(CardState.class, replaceStatusId, true)
					);
			}
			
			if(tag.hasAttribute(ATTR_IS_REPLACE_VALUE_EMPTY)){
				final Boolean empty = Boolean.valueOf(tag.getAttribute(ATTR_IS_REPLACE_VALUE_EMPTY));
				col.setEmptyReplace(empty);
			}

			if (tag.hasAttribute(ATTR_LIST_ORDER_TYPE)) {
				final String valOrder = tag.getAttribute(ATTR_LIST_ORDER_TYPE).trim();
				if (ATTR_LIST_ORDER_TYPE_STRING_NAME.equalsIgnoreCase(valOrder))
					col.setValueOrder(SearchResult.Column.LIST_ORDER_SORT_BY_STRING_NAME);
				else if (ATTR_LIST_ORDER_TYPE_ORDER_IN_LEVEL.equalsIgnoreCase(valOrder))
					col.setValueOrder(SearchResult.Column.LIST_ORDER_SORT_BY_ORDER_IN_LEVEL);
			}
			//������� ���� ��� ���������� �� value_rus/value_eng ��� ReferenceAttribute
			else {
				col.setValueOrder(SearchResult.Column.LIST_ORDER_SORT_BY_STRING_NAME);
			}
			
			if (tag.hasAttribute(ATTR_EXCEL_IGNORE)){
				final String value = tag.getAttribute(ATTR_EXCEL_IGNORE).trim();
				col.setExcelIgnore(Boolean.valueOf(value));
			}
			if (tag.hasAttribute(ATTR_EXPORT_ONLY)){
				final String value = tag.getAttribute(ATTR_EXPORT_ONLY).trim();
				col.setExportOnly(Boolean.valueOf(value));
			}
			
			if (tag.hasAttribute(ATTR_LINK_CARDS_STATES_IGNORE)) {
				final String ignoreStates = tag.getAttribute(ATTR_LINK_CARDS_STATES_IGNORE).trim();
				List<ObjectId> states = ObjectIdUtils.commaDelimitedStringToIds(ignoreStates.trim(), CardState.class);
				col.setLinkCardsStatesIgnore(states);
			}
			
			//��������� �������������� �������
			StringBuilder secondaryColumn = new StringBuilder();
			List<SearchResult.Column> secondaryList = null;
			for(int i = 0; i < Integer.MAX_VALUE; i++) {
				secondaryColumn.delete(0, secondaryColumn.length());
				secondaryColumn.append(ATTR_SECONDARY_COLUMN);
				if(i > 0) {
					secondaryColumn.append(i);
				}
				if (!tag.hasAttribute(secondaryColumn.toString())){
					break;
				} else {
					final String value = tag.getAttribute(secondaryColumn.toString()).trim();
					final SearchResult.Column newCol = new SearchResult.Column();
					
					newCol.setGroupId(groupId);
					col.setGroupId(groupId);
					newCol.setTimePattern(col.getTimePattern());
					newCol.setNullValueEn(col.getNullValueEn());
					newCol.setNullValueRu(col.getNullValueRu());
					
					final String[] paths = value.split(Attribute.LABEL_ATTR_PARTS_SEPARATOR);
					if(paths.length > 0) {
						ObjectId newAttr = anyAttribute(paths[0]);
						newCol.setAttributeId(newAttr == null ? new ObjectId(Attribute.class, paths[0]) : newAttr);
					}
					if (paths.length > 1) {
						final String newLabel = value.substring(value.indexOf(Attribute.LABEL_ATTR_PARTS_SEPARATOR) + 2);
						newCol.setFullLabelAttrId(newLabel);
						if (paths.length > 2){
							if (newCol.getPathToLabelAttr() == null)
								newCol.setPathToLabelAttr(new ArrayList<ObjectId>());
							newCol.getPathToLabelAttr().clear();
							for (int j = 1; j<=paths.length-2; j++){
								newCol.getPathToLabelAttr().add(safeMakeId(paths[j].trim()));
							}
							newCol.setLabelAttrId(safeMakeId(paths[paths.length-1].trim()));
						} else {
							newCol.setLabelAttrId(safeMakeId(newLabel));
							newCol.setPathToLabelAttr(null);
						}
					}
					
					String secondarySingleCond = ATTR_SECONDARY_COLUMNS_SINGLE_COND;
					if(i > 0) {
						secondarySingleCond += i;
					}
					if (tag.hasAttribute(secondarySingleCond)){
						final String str = tag.getAttribute(secondarySingleCond).trim();
						final String[] values = str.split(":");
						if(COND_STATE.equalsIgnoreCase(values[0])) {
							col.setColumnCond(SearchResult.Column.Condition.STATE);
							newCol.setCondValues(ObjectIdUtils.commaDelimitedStringToIds(values[1], CardState.class));
						} else if(COND_TEMPLATE.equalsIgnoreCase(values[0])) {
							col.setColumnCond(SearchResult.Column.Condition.TEMPLATE);
							newCol.setCondValues(ObjectIdUtils.commaDelimitedStringToIds(values[1], Template.class));
						}
					}
					newCol.setPrimaryColumn(col);
					if(secondaryList == null) {
						secondaryList = new ArrayList<SearchResult.Column>();
					}
					secondaryList.add(newCol);
				}
			}
			
			col.setSecondaryColumns(secondaryList);
			
		//	if (tag.hasAttribute(ATTR_ACTION)){
				setAction(tag, act);
				col.setAction(act);
		//	}
			
			setIcons(tag, col);
			setColumnViewer(tag, col);
		}
		
		
	private static void setColumnViewer(Element tag, SearchResult.Column col){
		Element elSearch = (Element)getTagNode(tag, "./column_viewer", null, null);
		if(elSearch!=null){
			String editorFileName=elSearch.getAttribute(SearchResult.Column.COLUNM_FILE_VIEWER);
			col.addParam(SearchResult.Column.COLUNM_FILE_VIEWER, editorFileName);
		}
	}

	private static void setAction(Element tag, SearchResult.Action act) {
		Element elAct = (Element)getTagNode(tag, "./action", null, null);
		if (elAct != null) {
			Map<String, String> confDef = new HashMap<String, String>();
			act.setId(elAct.getAttribute("id"));
			Element elParams = (Element)getTagNode(elAct, "./parameters", null, null);
			if (elParams!= null) {
				Collection elParam = (Collection)getNodeList(elParams, "./parameter", null, null);
				if (elParam.size()>0) {
					for (Object anElParam : elParam) {
						Element el = (Element) anElParam;
						confDef.put(el.getAttribute("name"), el.getAttribute("value"));
					}
				}
			}
			act.setParametrs(confDef);	
		}
		
	}

	private static void setIcons(Element tag, SearchResult.Column col) {
		Collection<Node> icons = getNodeList(tag, "./icon", null, null);
		if (icons.size() > 0) {
			Map<String, Map<String, String>> map = Init.hashMap();
			for (Node icon : icons) {
				Element el = (Element) icon;
				Map<String, String> conf = createConfIcon(el);
				String value = el.getAttribute("value");
				map.put(value, conf);
			}
			col.setIcons(map);
		}
		
		Element elDef = (Element)getTagNode(tag, "./default_icon", null, null);
		if (elDef != null) {
			col.setDefaultIcon(createConfIcon(elDef));
		}
		
		Element elEmpt = (Element)getTagNode(tag, "./empty_icon", null, null);
		if (elEmpt != null) {
			col.setEmptyIcon(createConfIcon(elEmpt));
		}
	}
	
	private static Map<String, String> createConfIcon(Element element){
		Map<String, String> conf = new HashMap<String, String>();
		conf.put("image", element.getAttribute("image"));
		conf.put("tooltipRu", element.getAttribute("tooltipRu"));
		conf.put("tooltipEn", element.getAttribute("tooltipEn"));
		checkAttributes(element, conf);
		return conf;
	}

	private static void checkAttributes(Element element, Map<String, String> conf){
		String[] attrs = new String[] {"tooltipStyle", "dialogStyle", "dialogTitle", "isLinkedCheck"};
		for (String attr : attrs) {
			if (checkAttribute(element, attr)) {
				conf.put(attr, element.getAttribute(attr));
			}
		}
	}

	private static boolean checkAttribute(Element element, String attrName){
		String value = element.getAttribute(attrName);
		return value != null && !value.isEmpty();
	}
	
	public static ObjectId safeMakeId(String sid) {
		final ObjectId result = anyAttribute(sid);
		return (result != null) ? result : new ObjectId(StringAttribute.class, sid);
	}
	
	/**
	 * Writes XML representation of given {@link Search} object into OutputStream
	 * @param search {@link Search} object to be serialized
	 * @param xml OutputStream object to write XML-representation of {@link Search} object into
	 * @see Search#storeToXml(OutputStream)
	 */
	public static void storeToXml(Search search, OutputStream xml) {
		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.getDOMImplementation().createDocument(null, TAG_ROOT, null);
			Element root = doc.getDocumentElement();
			//root.setNodeValue(TAG_ROOT);
			Element tag;
			if (search.isByAttributes())
				root.setAttribute(ATTR_BY_ATTRIBUTES, "true");
			if (search.isByMaterial())
				root.setAttribute(ATTR_BY_MATERIAL, "true");
			if (search.isByCode())
				root.setAttribute(ATTR_BY_CODE, "true");
			
			if (search.isBySql())
				root.setAttribute(ATTR_BY_SPECIAL_SQL, search.getSqlXmlName());
			
			if (search.getNameRu() != null && search.getNameRu().length() != 0) {
				tag = doc.createElement(TAG_NAME);
				tag.setAttribute(ATTR_LANG, ATTR_LANG_RU);
				tag.appendChild(doc.createTextNode(search.getNameRu()));
				root.appendChild(tag);
			}
			if (search.getNameEn() != null && search.getNameEn().length() != 0) {
				tag = doc.createElement(TAG_NAME);
				tag.setAttribute(ATTR_LANG, ATTR_LANG_EN);
				tag.appendChild(doc.createTextNode(search.getNameEn()));
				root.appendChild(tag);
			}
			if (search.getWords() != null && search.getWords().length() != 0) {
				tag = doc.createElement(TAG_WORDS);
				tag.appendChild(doc.createTextNode(search.getWords()));
				root.appendChild(tag);
			}
			if (search.getStates() != null) {
				for (Object o : search.getStates()) {
					tag = doc.createElement(TAG_STATUS);
					tag.appendChild(doc.createTextNode(o.toString()));
					root.appendChild(tag);
				}
			}
			if (search.getTemplates() != null) {
				for (Object o : search.getTemplates()) {
					Template template = (Template) o;
					tag = doc.createElement(TAG_TEMPLATE);
					tag.appendChild(doc.createTextNode(template.getId().getId().toString()));
					root.appendChild(tag);
				}
			}
			Element subTag;
			if (search.getAttributes() != null) {
				for (Object o : search.getFullAttributes()) {
					Map.Entry attr = (Map.Entry) o;
					tag = doc.createElement(TAG_ATTRIBUTE);
					ObjectId attributeId = (ObjectId) attr.getKey();
					Object attributeValue = attr.getValue();
					tag.setAttribute(ATTR_ID, (String) attributeId.getId());
					if (attributeValue instanceof Boolean) {
						if (TextAttribute.class.equals(attributeId.getType())) {
							tag.setAttribute(ATTR_TYPE, AttrUtils.getAttrTypeString(TextAttribute.class));
						} else {
							tag.setAttribute(ATTR_TYPE, AttrUtils.getAttrTypeString(StringAttribute.class));
						}
					} else if (attributeValue instanceof TextSearchConfigValue) {
						tag.setAttribute(ATTR_TYPE, AttrUtils.getAttrTypeString(attributeId.getType()));
						TextSearchConfigValue searchValue = (TextSearchConfigValue) attributeValue;
						subTag = doc.createElement(TAG_VALUE);
						subTag.setAttribute(SEARCH_TYPE, String.valueOf(searchValue.searchType));
						subTag.appendChild(doc.createTextNode(searchValue.value));
						tag.appendChild(subTag);

					} else if (attributeValue instanceof DatePeriod) {
						tag.setAttribute(ATTR_TYPE, AttrUtils.getAttrTypeString(DateAttribute.class));
						DatePeriod attrData = (DatePeriod) attributeValue;
						if (attrData.start != null) {
							subTag = doc.createElement(TAG_VALUE);
							subTag.setAttribute(ATTR_TYPE, ATTR_VALUE_TYPE_START);
							subTag.appendChild(doc.createTextNode(FORMAT_DATE.format(attrData.start)));
							tag.appendChild(subTag);
						}
						if (attrData.end != null) {
							subTag = doc.createElement(TAG_VALUE);
							subTag.setAttribute(ATTR_TYPE, ATTR_VALUE_TYPE_END);
							subTag.appendChild(doc.createTextNode(FORMAT_DATE.format(attrData.end)));
							tag.appendChild(subTag);
						}
					} else if (attributeValue instanceof Interval) {
						tag.setAttribute(ATTR_TYPE, AttrUtils.getAttrTypeString(IntegerAttribute.class));
						Interval attrData = (Interval) attributeValue;
						if (attrData.min != Integer.MIN_VALUE) {
							subTag = doc.createElement(TAG_VALUE);
							subTag.setAttribute(ATTR_TYPE, ATTR_VALUE_TYPE_START);
							subTag.appendChild(doc.createTextNode(String.valueOf(attrData.min)));
							tag.appendChild(subTag);
						}
						if (attrData.max != Integer.MAX_VALUE) {
							subTag = doc.createElement(TAG_VALUE);
							subTag.setAttribute(ATTR_TYPE, ATTR_VALUE_TYPE_END);
							subTag.appendChild(doc.createTextNode(String.valueOf(attrData.max)));
							tag.appendChild(subTag);
						}
					} else if (PersonAttribute.class.equals(attributeId.getType())) {
						if (attributeValue instanceof NumericIdList) {
							storeAtributeNumericIdListValue(doc, tag, attributeId, attributeValue);
						} else {
							continue;
						}
					} else if (CardLinkAttribute.class.equals(attributeId.getType())
							|| BackLinkAttribute.class.equals(attributeId.getType())) {
						if (attributeValue instanceof NumericIdList) {
							storeAtributeNumericIdListValue(doc, tag, attributeId, attributeValue);
						} else {
							continue;
						}
					} else if (ListAttribute.class.equals(attributeId.getType())) {
						storeAttributeReferenceValues(doc, tag, attributeId, attributeValue);
					} else if (TreeAttribute.class.equals(attributeId.getType())) {
						storeAttributeReferenceValues(doc, tag, attributeId, attributeValue);
					} else if (attributeValue instanceof Collection) {
						storeAttributeReferenceValues(doc, tag, attributeId, attributeValue);
					} else if (attributeValue instanceof IntegerSearchConfigValue) {
						tag.setAttribute(ATTR_TYPE, AttrUtils.getAttrTypeString(IntegerAttribute.class));
						IntegerSearchConfigValue searchValue = (IntegerSearchConfigValue) attributeValue;
						subTag = doc.createElement(TAG_VALUE);
						subTag.setAttribute(SEARCH_TYPE, String.valueOf(searchValue.searchType));
						subTag.appendChild(doc.createTextNode(searchValue.value.toString()));
						tag.appendChild(subTag);
					} else if (attributeValue instanceof ExistAttribute) {
						tag.setAttribute(ATTR_TYPE, AttrUtils.getAttrTypeString(attributeId.getType()));
						subTag = doc.createElement(TAG_VALUE);
						subTag.setAttribute(ATTR_CONDITION, ATTR_VALUE_COND_EXIST);
						tag.appendChild(subTag);
					} else if (attributeValue instanceof EmptyAttribute) {
						tag.setAttribute(ATTR_TYPE, AttrUtils.getAttrTypeString(attributeId.getType()));
						subTag = doc.createElement(TAG_VALUE);
						subTag.setAttribute(ATTR_CONDITION, ATTR_VALUE_COND_EMPTY);
						tag.appendChild(subTag);
					}
					root.appendChild(tag);
				}
			}
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(xml);
			Transformer serializer = TransformerFactory.newInstance().newTransformer();
			serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			serializer.setOutputProperty(OutputKeys.INDENT, "yes");
			serializer.transform(source, result);
		} catch (Exception e) {
			throw new RuntimeException("Error writing search to XML", e);
		}
	}

	private static void storeAttributeReferenceValues(Document doc,
			Element tag, ObjectId attributeId, Object attributeValue) {
		Element subTag;
		tag.setAttribute(ATTR_TYPE, AttrUtils.getAttrTypeString(attributeId.getType()));
		for (Object o : ((Collection) attributeValue)) {
			ReferenceValue value = (ReferenceValue) o;
			subTag = doc.createElement(TAG_VALUE);
			if (ReferenceValue.ID_ANOTHER.equals(value.getId())) {
				subTag.appendChild(doc.createTextNode(value.getValueRu()));
			} else subTag.appendChild(doc.createTextNode(value.getId().getId().toString()));
			tag.appendChild(subTag);
		}
	}

	private static void storeAtributeNumericIdListValue(Document doc,
			Element tag, ObjectId attributeId, Object attributeValue) {
		Element subTag;
		tag.setAttribute(ATTR_TYPE, AttrUtils.getAttrTypeString(attributeId.getType()));
		if (!(attributeValue instanceof NumericIdList))
			throw new RuntimeException("Invalid attribute value type. It should be instance of NumericIdList!");
		
		NumericIdList numericIdList = (NumericIdList)attributeValue;
		for(ObjectId valueId : numericIdList.getNumericIds()) {
			subTag = doc.createElement(TAG_VALUE);
			subTag.appendChild(doc.createTextNode(valueId.getId().toString()));
			tag.appendChild(subTag);
		}
	}
	
	public static String getTagContent(Node doc, String tag, String attr, String val)
	{
		final XPathEvaluator xpath = new XPathEvaluatorImpl();
		String query = tag;
		if (attr != null && val != null)
			query += "[@" + attr + "='" + val + "']";
		query += "/text()";
		final XPathResult result = (XPathResult) xpath.evaluate(query,
				doc, null, XPathResult.STRING_TYPE, null);
		return result != null ? result.getStringValue() : null;
	}
	
	public static Collection<Node> getNodeList(Node doc, String tag, String attr, String val)
	{
		final XPathEvaluator xpath = new XPathEvaluatorImpl();
		String query = tag;
		if (attr != null && val != null)
			query += "[@" + attr + "='" + val + "']";
		final XPathResult result = (XPathResult) xpath.evaluate(query,
				doc, null, XPathResult.UNORDERED_NODE_ITERATOR_TYPE, null);
		final ArrayList<Node> nodes = new ArrayList<Node>();
		while (true) {
			Node node = result.iterateNext();
			if (node == null)
				return nodes;
			nodes.add(node);
		}
	}
	
	public static String getTagContent(Node tag)
	{
		final XPathEvaluator xpath = new XPathEvaluatorImpl();
		final XPathResult result = (XPathResult) xpath.evaluate("text()",
				tag, null, XPathResult.STRING_TYPE, null);
		return (result != null) ? result.getStringValue() : null;
		/*
		NodeList children = tag.getChildNodes();
		for (int i = 0; i < children.getLength(); i++)
			if (children.item(i).getNodeType() == Node.TEXT_NODE)
				return ((Text) children.item(i)).getData();
		throw new RuntimeException("XML error: " + tag + " tag should contain a text");
		 */
	}
	
	public static Node getTagNode(Node doc, String tag, String attr, String val)
	{
		final XPathEvaluator xpath = new XPathEvaluatorImpl();
		String query = tag;
		if (attr != null && val != null)
			query += "[@" + attr + "='" + val + "']";
		final XPathResult result = (XPathResult) xpath.evaluate(query,
				doc, null, XPathResult.ANY_UNORDERED_NODE_TYPE, null);
		return (result != null) ? result.getSingleNodeValue() : null;
	}
	
	public static boolean getAttributeBoolean(Element elem, String attr)
	{
		final XPathEvaluator xpath = new XPathEvaluatorImpl();
		final XPathResult result = (XPathResult) xpath.evaluate(attr,
				elem, null, XPathResult.STRING_TYPE, null);
		return Boolean.valueOf(result.getStringValue());
	}

	public static String getAttributeString( final Element elem, final String attrName)
	{
		final XPathEvaluator xpath = new XPathEvaluatorImpl();
		final XPathResult result = (XPathResult) xpath.evaluate( attrName,
				elem, null, XPathResult.STRING_TYPE, null);
		return result.getStringValue();
	}
	
	public static Date getDateValue(Node node) throws ParseException
	{
		if (node == null)
			return null;
		final String valueText = getTagContent(node);
		if (valueText == null)
			throw new ParseException("Null value is invalid for the date", 0);
		
		if (!((Element) node).hasAttribute(ATTR_VAR)) {
			return FORMAT_DATE.parse(valueText);
		}
		String var = ((Element) node).getAttribute(ATTR_VAR);
		boolean skipTime = false; // do we need to skip the time in date (set to zero 00:00:00)
		if (((Element) node).hasAttribute(ATTR_ZERO_TIME)) {
			skipTime = Boolean.parseBoolean(((Element) node).getAttribute(ATTR_ZERO_TIME));
		}
		if (ATTR_VAR_CURRENT.equals(var)) {
			
			final String offset = valueText.trim();
			
			// (RuSA) check "relative offset MUST start with +/-" 
			// (don't ask me why) ...
			if ( -1 == "+-".indexOf(offset.charAt(0)) )
				throw new ParseException("Invalid offset for current date: " + offset, 0);
			
			double days = Double.parseDouble(offset);
			final Calendar calendar = Calendar.getInstance();
			if(skipTime) { // setting time to zero (00:00:00)
				calendar.set(Calendar.HOUR_OF_DAY, 0);
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MILLISECOND, 0);
			}
			calendar.add(Calendar.DAY_OF_YEAR, (int)days);
			Date date = calendar.getTime();
			return date;
		} else if(ATTR_VAR_CURRENT_YEAR.equals(var)) {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.DAY_OF_YEAR, 1);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);			
			return calendar.getTime();
		}
		throw new ParseException("Unknown variable name: " + var, 0);
	}

	private final static Class[] types = new Class[] {
		StringAttribute.class, 		TextAttribute.class, 
		IntegerAttribute.class, 	DateAttribute.class,
		ListAttribute.class, 		TreeAttribute.class, 
		PersonAttribute.class, 		CardLinkAttribute.class,
		BackLinkAttribute.class,	TypedCardLinkAttribute.class,
		HtmlAttribute.class,		DatedTypedCardLinkAttribute.class
	};

	public static ObjectId anyAttribute(String id) {
		for (Class type : types) {
			final ObjectId translated = ObjectId.predefined(type, id);
			if (translated != null) {
				return translated;
			}
		}
		return null;
	}
}
