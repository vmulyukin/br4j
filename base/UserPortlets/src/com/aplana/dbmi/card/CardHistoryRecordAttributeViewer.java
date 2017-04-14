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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.LinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.AttrUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

public class CardHistoryRecordAttributeViewer extends JspAttributeViewer {
	/* ������������.
	 * code, ��� code - ������� attribute_code �� objectid.properties
	 * ��� ������ ���� CardLinkAttribute
	*/ 
	private static final String PARAM_LINK = "link";
	
	/* �����������.
	 * type: code -> name [,type: code -> name]*
	 * ��� type ���� �� �������� ���� �������� ����������� � AttrUtils
	 * code - ������� attribute_code �� objectid.properties
	 * name - ��� ��������(�������) ������ ��� �������������� �������� � �������������� �������
	 */
	private static final String PARAM_ATTRIBUTES = "attributes";
	
	/* �����������.
	 * type: code -> name1, name2,..., nameN
	 * ��� type ���� �� �������� ���� �������� ����������� � AttrUtils
	 * code - ������� attribute_code �� objectid.properties
	 * name1,..., nameN - ����� �������� � ������ ������ ��� �������������� �������� � �������������� �������
	 */
	private static final String PARAM_RECORDS = "records";
	
	/* ������������. ������ �������� ���������� ��������
	 * name: title
	 * ��� name - ��� �������
	 * title - ��������� ������� ������������ � gui
	 */
	private static final String PARAM_COLUMNS = "columns";
	
	/* �����������.
	 * name: type
	 * ��� name - ��� �������
	 * type - ��� �������� �������. ��� ������� ���� ������������ 
	 * ���� ������������ ��� ������ ��������. ���� ��� ������� �� ������
	 * �� ��������� ��� string. ������������� ����������� ��� ����� string � date
	 */
	private static final String PARAM_TYPES = "types";
	
	/* �����������.
	 * name
	 * ��� name - ��� �������
	 */
	private static final String PARAM_SORT = "sort";
	
	/* �����������
	 * order
	 * ��� order - ������� ����������. ���� ����� desc
	 * �� ��������� �� ��������, � ��������� ������ �� �����������
	 */
	private static final String PARAM_ORDER_SORT = "orderSort";
	
	/* �����������
	 * showTime
	 * ��� showTime - ���������� �� ����. �� ��������� - true
	 */
	private static final String PARAM_SHOW_TIME = "showTime";
	
	private static final String PATTERN_DATE = "yyyy-MM-dd'T'HH:mm:ss";
	private static final String PATTERN_VIEW_DATE_TIME = "dd.MM.yyyy HH:mm";
	private static final String PATTERN_VIEW_DATE = "dd.MM.yyyy";
	
	public static final String KEY_CONTENT_TABLE = "contentTable";
	public static final String KEY_HEAD_TABLE = "headTable";
	public static final String KEY_TYPES = "types";
	public static final String KEY_SHOW_TIME = "showType";
	
	private static final String TYPE_DATE = "date";
	
	private static final String ORDER_SORT_DESC = "desc";
	
	// id ��������, ���� CardLinkAttribute, ������� ��������� �� �������� ��������
	private ObjectId linkId = null; 
	/* id-�� ��������� �������� ��������, �������� ������� �� ������ ��������
	 * ������������ ����� ����� id-�� � ���������� ���������� 
	 * ��������������� ��� �������������� ��������
	 */ 
	private HashMap/*ObjectId code -> String name*/ defAttr = new HashMap();
	// id �������� �������� �������� ���������� ������� �������
	private ObjectId recordId = null;
	// ����� �������� ������� �������
	private List<String> columnsRecord = null;  

	private LinkedHashMap/*name -> title*/ defColumns = new LinkedHashMap();
	
	private HashMap<String, String> types = new HashMap<String, String>();
	
	private String sortColumn = null;
	private boolean descSort = false;
	private boolean showTime = true;
	
	private CardPortletSessionBean sessionBean;
	private DataServiceBean serviceBean;
	
	
	public CardHistoryRecordAttributeViewer() {
		setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/CardHistoryRecordView.jsp");
	}

	protected DataServiceBean getServiceBean() {
		return serviceBean;
	}
	
	@Override
	public void setParameter(String name, String value) {
		if (PARAM_LINK.equals(name)) {
			linkId = IdUtils.smartMakeAttrId(value.trim(), CardLinkAttribute.class);
		} else if (PARAM_ATTRIBUTES.equals(name)) {
			String[] codeNames = value.split(",");
			for (int i=0; i < codeNames.length; i++) {
				String[] param = codeNames[i].split("->");
				ObjectId attrId = getAttributeId(param[0].trim());
				defAttr.put(attrId, param[1].trim());
			}
			
		} else if (PARAM_RECORDS.equals(name)) {
			String[] codeNames = value.split("->");
			recordId = getAttributeId(codeNames[0].trim());
			columnsRecord = Arrays.asList(codeNames[1].trim().split("\\s*,\\s*"));
			
		} else if (PARAM_COLUMNS.equals(name)) {
			String[] columns = value.split(",");
			for (int i=0; i < columns.length; i++) {
				String[] param = columns[i].split(":");
				defColumns.put(param[0].trim(), param[1].trim());
			}
		} else if (PARAM_TYPES.equals(name)) {
			String[] params  = value.split(",");
			for (int i=0; i < params.length; i++) {
				String[] param = params[i].split(":");
				types.put(param[0].trim(), param[1].trim());
			}
		} else if (PARAM_SORT.equals(name)) {
			sortColumn = value;
		} else if (PARAM_ORDER_SORT.equals(name)) {
			if (ORDER_SORT_DESC.equals(value)) {
				descSort = true;
			}
		} else if (PARAM_SHOW_TIME.equals(name)) {
			if ("false".equalsIgnoreCase(value.trim())) {
				showTime = false;
			}
		} else {
			super.setParameter(name, value);
		}
	}
	
	@Override
	public void initEditor(PortletRequest request, Attribute attr)
			throws DataException {
		List/*HashMap (name -> value)*/ table = new LinkedList();
		sessionBean = getCardPortletSessionBean(request);
		serviceBean = sessionBean.getServiceBean();
		
		Card activeCard = sessionBean.getActiveCard();
		try {
			List cards = getChildrenCards(activeCard, linkId, defAttr.keySet(), recordId);
			
			Iterator iter = cards.iterator();
			while (iter.hasNext()) {
				Card card = (Card) iter.next();
				List/*HashMap (name -> value)*/ nameValues = getValuesFromCard(card, defAttr, recordId, columnsRecord);
				table.addAll(nameValues);
			}
			
			if (sortColumn != null) {
				Collections.sort(table, new ComparatorRow(sortColumn, descSort));
			}
			sessionBean.getActiveCardInfo().setAttributeEditorData(attr.getId(), KEY_CONTENT_TABLE, table);
			sessionBean.getActiveCardInfo().setAttributeEditorData(attr.getId(), KEY_HEAD_TABLE, defColumns);
			sessionBean.getActiveCardInfo().setAttributeEditorData(attr.getId(), KEY_TYPES, types);
			sessionBean.getActiveCardInfo().setAttributeEditorData(attr.getId(), KEY_SHOW_TIME, showTime);
		} catch (Exception e) {
			e.printStackTrace();// ������ ���������� ��������� �� ������
		}
		
	}

	protected List/*Card*/ getChildrenCards(Card parentCard, ObjectId linkId, Collection/*<ObjectId>*/ attrIds, ObjectId recordId) throws DataException, ServiceException {
		/* �� ����� ������������ FetchChildrenCards, �.�. ������� linkId
		 * ����������� �� �������� �������� �������� ��� �� ����� ��������
		 */
		Search search = new Search();
		search.setByCode(true); // ����� ��?
		
		LinkAttribute link = (LinkAttribute) parentCard.getAttributeById(linkId);
		search.setWords(link.getLinkedIds());
		
		List columns = new ArrayList();
		Iterator iter = attrIds.iterator();
		while (iter.hasNext()) {
			SearchResult.Column col = new SearchResult.Column();
			col.setAttributeId((ObjectId)iter.next());
			columns.add(col);
		}
		if (recordId != null) {
			SearchResult.Column col = new SearchResult.Column();
			col.setAttributeId(recordId);
			columns.add(col);
		}
		search.setColumns(columns);
		SearchResult result = (SearchResult) serviceBean.doAction(search);
		return result.getCards();
		
	}
	
	protected List/*HashMap (name -> value)*/ getValuesFromCard(Card card, HashMap defAttr, ObjectId recordId, List<String> columnsRecord) {
		List rows = new LinkedList/*HashMap (name -> value)*/();
		
		HashMap commonValues = new HashMap();
		Iterator iter = defAttr.keySet().iterator();
		while (iter.hasNext()) {
			ObjectId id = (ObjectId) iter.next();
			Attribute attr = card.getAttributeById(id);
			if (attr != null)
				commonValues.put(defAttr.get(id), attr.getStringValue());
			else
				commonValues.put(defAttr.get(id), "...");
		}
		
		List<List<Object>> records = getRecordsFromAttribute(card.getAttributeById(recordId));
		
		iter = records.iterator();
		while (iter.hasNext()) {
			HashMap row = new HashMap();
			row.putAll(commonValues);
			List valuesRecord = (List)iter.next();
			for (int i=0; i < valuesRecord.size(); i++) {
				if (valuesRecord.get(i) != null && !valuesRecord.get(i).toString().isEmpty()) {
					row.put(columnsRecord.get(i), valuesRecord.get(i));
				}else if (!commonValues.containsKey(columnsRecord.get(i))) {
					row.put(columnsRecord.get(i), "");
				}
			}
			rows.add(row);
		}
		return rows;
	}
	
	protected List<List<Object>> getRecordsFromAttribute(Attribute attr) {
		final List<List<Object>> records = new LinkedList<List<Object>>();
		if(attr == null) return records;
		final String text = attr.getStringValue();
		try {
			final Document doc = readDocument(text);
			if (doc != null) {
				final NodeList parts = doc.getElementsByTagName("part");
				for (int i = 0; i < parts.getLength(); i++) {
					final Element el = (Element)parts.item(i);
					final Date date = parseDate(el.getAttribute("timestamp"));
					final String round = el.getAttribute("round");
					final String order = el.getAttribute("order");
					final String content = el.getTextContent();
					final String fact_user = el.getAttribute("fact-user");
					final String plan_user = el.getAttribute("plan-user");
					final String action = el.getAttribute("action");
					final List<Object> rec = new ArrayList<Object>(7);
					rec.add(round);
					rec.add(date);
					rec.add(fact_user);
					rec.add(plan_user);
					rec.add(content);
					rec.add(action);
					rec.add(order);
					records.add(rec);
				}	
			}
		} catch (Exception e) {
			e.printStackTrace();// ������� ���������� ����� ���������� �� ������
		}
		return records;
	}
	
	protected Date parseDate(String text) throws ParseException {
		return (new SimpleDateFormat(PATTERN_DATE)).parse(text);
	}
	
	public static String formatValue(String type, Object value, boolean showTime) {
		if (TYPE_DATE.equals(type)) {
			if(!(value instanceof Date)) {
				return "";
			}
			SimpleDateFormat formatDate = new SimpleDateFormat(showTime ? PATTERN_VIEW_DATE_TIME : PATTERN_VIEW_DATE);
			return formatDate.format((Date)value);
		} else {
			return value.toString();
		}
	}

	/* ��������� ObjectId ��������, �������� � ������ "type: code"
	 * ��� type ���� �� �������� ���� �������� ����������� � AttrUtils
	 * � code - ������� attribute_code �� objectid.properties 
	 */
	private ObjectId getAttributeId(String typeCode) {
		String[] params = typeCode.split(":");
		Class<?> type = AttrUtils.getAttrClass(params[0].trim());
		return ObjectId.predefined(type, params[1].trim());
	}
	
	protected Document readDocument(String text) throws ParserConfigurationException, UnsupportedEncodingException, SAXException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = dbf.newDocumentBuilder();
		if (text == null || text.length() < 1)
			return null;
		return builder.parse(new ByteArrayInputStream(text.getBytes("UTF-8")));
	}
	
	protected List<String> getColumnsRecord() {
		return columnsRecord;
	}

	protected void setColumnsRecord(List<String> columnsRecord) {
		this.columnsRecord = columnsRecord;
	}

	protected HashMap<String, String> getTypes() {
		return types;
	}

	protected void setTypes(HashMap<String, String> types) {
		this.types = types;
	}
	
	protected String getTypesValue(String key) {
		return types.get(key);
	}

	private class ComparatorRow implements Comparator {
		String name;
		boolean dec; // �� ��������
		
		public ComparatorRow(String name, boolean dec) {
			super();
			this.name = name;
			this.dec = dec;
		}

		public int compare(Object arg0, Object arg1) {
			Comparable val0 = (Comparable)((Map)arg0).get(name);
			Object val1 = ((Map)arg1).get(name);
			int compare = val0.compareTo(val1);
			if (dec) {
				compare *= -1;
			}
			return compare;
		}
	}
}