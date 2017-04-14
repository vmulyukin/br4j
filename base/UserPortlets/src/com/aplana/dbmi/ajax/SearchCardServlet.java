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
package com.aplana.dbmi.ajax;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import com.aplana.dbmi.action.BulkFetchChildrenCards;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.SearchResult.Column;
import com.aplana.dbmi.card.CardLinkPickerAttributeEditor;
import com.aplana.dbmi.card.hierarchy.descriptor.LinkDescriptor;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.portlet.BrowsingReportsSearchParameters;
import com.aplana.dbmi.service.DataServiceBean;

public class SearchCardServlet extends AbstractDBMIAjaxServlet {
	private static final long serialVersionUID = 1L;
	public static final long EMPTY_CARD_ID = -1;	
	public static final String PARAM_CALLER = "caller";
	public static final String PARAM_IGNORE = "ignore";
	public static final String PARAM_BYCODES = "byCodes";	
	public static final String PARAM_QUICHR_OPTIONS = "options";

	protected void generateResponse(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		int wholeSize = 0;
		DataServiceBean serviceBean = getDataServiceBean(request);

		String p = request.getParameter(PARAM_CALLER);
		SearchCardServletParameters params;
		if (CardLinkPickerSearchParameters.CALLER.equals(p)) {
			params = new CardLinkPickerSearchParameters();
		} else if (CardLinkPickerSearchFilterParameters.CALLER.equals(p)) {
			params = new CardLinkPickerSearchFilterParameters();
		} else if (QuickResolutionSearchPersonParameters.CALLER.equals(p)) {
			params = new QuickResolutionSearchPersonParameters();			
		} else if (QuickIndepResolutionSearchPersonParameters.CALLER.equals(p)) {
			params = new QuickIndepResolutionSearchPersonParameters();
		} else if (MassResolutionSearchPersonParameters.CALLER.equals(p)) {
			params = new MassResolutionSearchPersonParameters();
		} else if (RequestToChangeSearchPersonParameters.CALLER.equals(p)) {
			params = new RequestToChangeSearchPersonParameters();
		} else if (AdditionalEndorsementSearchPersonParameters.CALLER.equals(p)) {
			params = new AdditionalEndorsementSearchPersonParameters();			
		} else if (BrowsingReportsSearchParameters.CALLER.equals(p)) { 
			params = new BrowsingReportsSearchParameters();
		} else if (UserRolesSearchInternalPersonParameters.CALLER.equals(p)) {
			params = new UserRolesSearchInternalPersonParameters();
		} else {
			throw new ServletException("Unknown caller: " + p);
		}
		params.initialize(request, serviceBean);

		p = request.getParameter(CardLinkPickerAttributeEditor.FIELD_LABEL);
		if (p == null) {
			p = "";
		}			
		if (p.endsWith("*")){
			// Filter clause comes in form 'Name*', we need to remove trailing asterisk
			p = p.substring(0, p.length() - 1);
		}

		String searchPattern = p;

		int startFrom = 0;
		p = request.getParameter("start"); 
		if (p != null) {
			startFrom = Integer.parseInt(p);
		}
		int count = -1;
		p = request.getParameter("count");
		if (p != null) {
			count = Integer.parseInt(p);
		}

		Set<ObjectId> ignore = Collections.emptySet();
		p = request.getParameter(PARAM_IGNORE);
		if (p != null) {
			String[] ids = p.split(CardLinkPickerAttributeEditor.ID_DELIMITER);
			ignore = new HashSet<ObjectId>(ids.length);
			for (int i = 0; i < ids.length; ++i) {
				String st = ids[i];
				if (!"".equals(st)) {
					ObjectId cardId = new ObjectId(Card.class, Long.parseLong(st));
					ignore.add(cardId);
				}
			}
		}

		// �������� id ��������, ����� ���������� ��������������� ���������� �������� � FilteringSelect
		String byIds = request.getParameter("id");
		if (byIds == null) {
			// �������� PARAM_BYCODES ��������, ����� ����������� �������� ��� ������ ��������
			byIds = request.getParameter(PARAM_BYCODES);	
		}
		if ("".equals(byIds)) {
			byIds = "-1";	// non-existant cardId
		}
		logger.debug("Request received: [searchPattern = '" + searchPattern + "', start = '" + startFrom + "', count = '" + count + "', ignored = '" + p + "'");

		List<Card> cards = null;
		Map<String, ArrayList<Card>> labelColumnsForCards = null;	// ������ label-������� �� ���������� ��� ������ �� ��������� �������� (������ �������� ������ ���� <%Column.attributeId%>-><%Column.fullLabelAttrId%>) 
		Collection<Column> columns = null;				// ������ ������� ������ 
		ObjectId labelAttrId = params.getLabelAttrId();
		Search search;
		if ("".equals(searchPattern) && count <= 0 && byIds == null) {
			ObjectId dummyCardId = new ObjectId(Card.class, EMPTY_CARD_ID);
			Card dummyCard = DataObject.createFromId(dummyCardId);
			dummyCard.setAttributes(new ArrayList<DataObject>(0));
			cards = Collections.singletonList(dummyCard);
		} else
			do {
				search = params.getSearch().makeCopy();
				if (byIds != null) {
					search.setByAttributes(false);
					search.setWords(byIds);
					search.setByCode(true);
					search.setSqlXmlName(null);
				} else if (search.isByAttributes()){
					search.setWords(searchPattern);
					search.addAttribute(labelAttrId, Boolean.TRUE);
				} else if (search.isBySql()){
					search.setWords(searchPattern);
				}
				search.setIgnoredIds(ignore);
				final SearchResult.Column orderColumn1 = new SearchResult.Column();
				//���������� �� ������� (����� ���� ������)
				orderColumn1.setAttributeId(Card.ATTR_TEMPLATE);
				orderColumn1.setSorting(SearchResult.Column.SORT_ASCENDING);	// � ����� � 17655 ��������� ������� ���������� �����������, ����� ������� ������� � ��� ��������� ��������
				search.getFilter().setOrderColumn(orderColumn1);
				final SearchResult.Column orderColumn2 = new SearchResult.Column();
				orderColumn2.setAttributeId(labelAttrId);
				orderColumn2.setSorting(SearchResult.Column.SORT_ASCENDING);
				search.getFilter().addOrderColumn(orderColumn2, 2);
				search.getFilter().setPageSize(count);
				search.getFilter().setPage(startFrom/count+1);
				SearchResult result;
				try {
					result = serviceBean.doAction(search);
				} catch (Exception e) {
					logger.error("Error searching cards for " + request.getParameter(PARAM_CALLER), e);
					throw new ServletException(e);
				}
				wholeSize = search.getFilter().getWholeSize();
				cards = result.getCards();
				labelColumnsForCards = result.getLabelColumnsForCards();
				columns = result.getColumns();
				if (byIds != null && cards.size() > 0) {
					// ���� ��� ����� �� �������� ���������������, �� ����������������� ��������,
					// ����� ��� ���� � ��� �� �������, � ������� �������������
					Map<ObjectId, Card> cardMap = ObjectIdUtils.collectionToObjectIdMap(cards);
					List<ObjectId> cardIds = ObjectIdUtils.commaDelimitedStringToNumericIds(byIds, Card.class);
					cards = new ArrayList<Card>(cards.size());
					Iterator<ObjectId> j = cardIds.iterator();
					while (j.hasNext()) {
						Card tempObject = cardMap.get(j.next());
						if(tempObject!=null){
							cards.add(tempObject);
						}
					}
				}
				// ���� ����� �� �� ���������, �� ��������� �������
				if ((search.isByCode() || search.isBySql() || search.isByMaterial()) && !"".equals(searchPattern)) {
					Iterator<Card> i = cards.iterator();
					while (i.hasNext()) {
						Card card = i.next();
						String searchAttrValue = card.getAttributeById(labelAttrId).getStringValue();
						if (searchAttrValue == null || !searchAttrValue.toUpperCase().contains(searchPattern.toUpperCase()))
							i.remove();
					}
				}
			} while(cards.size() == 0 && params instanceof SearchCardServletParametersEx &&
					((SearchCardServletParametersEx) params).nextSearch(search));

		//���� �������� - ������ ��������, �������� �������� ������

		// PPanichev 23.10.2014
		// ��������� ���� �������� � �������� �� LinkDescriptor
		LinkDescriptor descr = params.getList();

		ChildrenResult processChildrenResult = null;

		if(descr != null){
			processChildrenResult = processChildren(columns, cards, descr, ignore, request.getParameter(PARAM_CALLER), serviceBean);
		} else {
			processChildrenResult = new ChildrenResult(cards);
		}

		boolean hasNext = true;
		if (count < 0 || startFrom + count >= wholeSize){
			hasNext = false;
		}
		writeCardList(response, processChildrenResult, labelAttrId, columns, labelColumnsForCards, hasNext);
	}
	
	/**
	 * �����, ������� �������� �� ��������� ��������� �������� (����� ���� <list ... /> � �������� ��������)
	 * 
	 * ���� �������� linkReversed="true", �� ���������� ����������� ����� {@link processChildrenRecursive}, ����� {@link processChildrenOnce}
	 * 
	 */
	private ChildrenResult processChildren( Collection<Column> columns,
								  List<Card> cards,
								  LinkDescriptor descr,
								  Set<ObjectId> ignore,
								  String caller,
								  DataServiceBean serviceBean) throws ServletException {

		Map<ObjectId, List<Card>> resultCards = new HashMap<ObjectId, List<Card>>(0);
		Map<String, ArrayList<Card>> childrenCardLabelsForColumn = new HashMap<String, ArrayList<Card>>(0);
		List<Card> finalCards = null;

		if(descr.isRecursive()) {
			processChildrenRecursive(resultCards, childrenCardLabelsForColumn, columns, null, cards, descr, ignore, caller, serviceBean);
			finalCards = cards;

			//��������� � ������ ������� ��������� �������� �������� � ������ ������
			for (Card rootCard : cards) {
				List<Card> children  = resultCards.get(rootCard.getId());
				if (children != null) {
					children.add(0, rootCard);
				}
			}
		} else {
			ChildrenResult res = processChildrenOnce(columns, cards, descr, ignore, caller, serviceBean);
			resultCards.putAll(res.getResultCards());
			childrenCardLabelsForColumn.putAll(res.getChildrenCardLabelsForColumn());
			finalCards = res.getCards();
		}

		return new ChildrenResult(finalCards, resultCards, childrenCardLabelsForColumn);

	}

	/**
	 *  ����� ���� ��������� �������� ��� ����� �������� 
	 */
	private ChildrenResult processChildrenOnce(Collection<Column> columns,
											   List<Card> cards,
											   LinkDescriptor descr,
											   Set<ObjectId> ignore,
											   String caller,
											   DataServiceBean serviceBean) throws ServletException {

		Map<ObjectId, List<Card>> resultCards = null;
		Map<String, ArrayList<Card>> childrenCardLabelsForColumn = null;

		BulkFetchChildrenCards action = new BulkFetchChildrenCards();
		action.setParentCardIds(ObjectIdUtils.getIdsFromObjects(cards));
		action.setColumns(columns);
		action.setLinkAttributeId(descr.getCardLinkAttr());
		action.setReverseLink(descr.isReverse());
		action.setChildrenTemplates(descr.getTemplates());
		action.setChildrenStates(descr.getStatuses());
		try {
			BulkFetchChildrenCards.Result childrenCardResult = serviceBean.doAction(action);
			resultCards = childrenCardResult.getCards();
		    childrenCardLabelsForColumn = childrenCardResult.getLabelColumnsMap();
		} catch (Exception e) {
			logger.error("Error searching cards for " + caller, e);
			throw new ServletException(e);
		}

		//������� �� ������ ������������ ids � ���������� ������ ������
		for(Iterator<List<Card>> parentIter = resultCards.values().iterator(); parentIter.hasNext();) {
			List<Card> children = parentIter.next();

			if(children == null || children.isEmpty()) {
				parentIter.remove();
			} else {
				for(Iterator<Card> childrenIter = children.iterator(); childrenIter.hasNext();) {
					if (childrenIter.hasNext()) {
						if(ignore.contains(childrenIter.next().getId())) {
							childrenIter.remove();
						}
					}
				}
			}
		}
		if (null != resultCards && null != childrenCardLabelsForColumn) {
			return new ChildrenResult(cards, resultCards, childrenCardLabelsForColumn);
		}

		return ChildrenResult.getEmpty();

	}

	/**
	 *  ����� ���� ��������� �������� � ������ ��������, �������� ��������� �������� � ���� ����� root ��������, 
	 *  ����� ������� ����������� ��������� ������������� � ������. <br/>
	 *  ��������, ���� � �� ������� ���������: <br/>
	 *  <pre>                               
	 *  root1                root2          
	 *     |                    |           
	 *     child1               child1      
	 *     |   |                |   |       
	 *     |   child1.2         |   child1.2
	 *     |   child1.3         |   child1.3
	 *     |                    |           
	 *     child2               child2      
	 *         |                    |       
	 *         child2.1             child2.1
	 *         child2.2             child2.2
	 *
	 *  ������� ����� ������������� �
	 *
	 *  root1                root2      
	 *      |                    |      
	 *     child1               child1  
	 *     child1.2             child1.2
	 *     child1.3             child1.3
	 *     child2               child2  
	 *     child2.1             child2.1
	 *     child2.2             child2.2
	 *   
	 *  </pre>
	 *
	 */
	private void processChildrenRecursive(Map<ObjectId, List<Card>> resultCards,
										  Map<String, ArrayList<Card>> childrenCardLabelsForColumn,
										  Collection<Column> columns,
										  ObjectId rootId,
										  List<Card> cards,
										  LinkDescriptor descr,
										  Set<ObjectId> ignore,
										  String caller,
										  DataServiceBean serviceBean) throws ServletException {

		ChildrenResult res = processChildrenOnce(columns, cards, descr, ignore, caller, serviceBean);

		if (rootId != null) {
			List<Card> rootChildList  = resultCards.get(rootId);
			for (List<Card> value : res.getResultCards().values()) {
				rootChildList.addAll(value);
			}
		} else {
			resultCards.putAll(res.getResultCards());
		}

		mergeMapWithListValues(res.getChildrenCardLabelsForColumn(), childrenCardLabelsForColumn);

		for (Entry<ObjectId, List<Card>> resEntry : res.getResultCards().entrySet()) {
			//���� �� ����� �������� �������, ������ ��� ������ ������, ����� ��� ���-�������� ������� � ��������� ���������
			ObjectId parent = rootId != null ? rootId : resEntry.getKey();
			if (null != resEntry.getValue()) {
				processChildrenRecursive(resultCards, childrenCardLabelsForColumn, columns, parent, resEntry.getValue(), descr, ignore, caller, serviceBean);
			}
		}
	}

	private static <T> void mergeMapWithListValues(Map<String, ArrayList<T>> source, Map<String, ArrayList<T>> destination) {
		for (String key : source.keySet()) {
			if (destination.containsKey(key)) {
				destination.get(key).addAll(source.get(key));
			} else {
				destination.put(key, source.get(key));
			}
		}
	}

	private void writeCardList(HttpServletResponse response,
			ChildrenResult processChildrenResult,
			ObjectId labelAttrId,
			Collection<Column> columns,
			Map<String, ArrayList<Card>> labelColumnsForCards,
			boolean hasNext) throws ServletException {

		try {
			List<Card> cards = processChildrenResult.getCards();
			Map<String, ArrayList<Card>> childrenCardLabelsForColumn = processChildrenResult.getChildrenCardLabelsForColumn();
			Map<ObjectId, List<Card>> lists = processChildrenResult.getResultCards();

			JSONWriter jw = new JSONWriter(response.getWriter());
			jw.object();
			jw.key("identifier").value(CardLinkPickerAttributeEditor.FIELD_CARD_ID);
			jw.key("label").value(CardLinkPickerAttributeEditor.FIELD_LABEL);
			JSONArray items = new JSONArray();
			final Iterator<Card> i = cards.iterator();
			while (i.hasNext()) {
				final Card card = i.next();
				List<Card> children = lists == null ? null : lists.get(card.getId());
				items.put(getCardJSON(card, labelAttrId, columns, labelColumnsForCards, childrenCardLabelsForColumn, children));
			}
			jw.key("items").value(items);
			jw.key("hasNext").value(hasNext);			
			jw.endObject();
		} catch (Exception e) {
			logger.error("Error generating response", e);
			throw new ServletException(e);
		}
	}

	private Attribute chkGetAttr(Card card, ObjectId id)
	{
		final Attribute result = card.getAttributeById(id);
		if (result == null || result.getId() == null || result.getId().getId() == null) {
			logger.warn("No attribute inside card "+ card.getId() 
					+ ": getAttribute( "
					+ id + ") returns " 
					+ ( (result == null) ? "NULL" : result.getId() )
				);
		} 
		return result;
	}

	private JSONObject getCardJSON(Card card, 
			ObjectId searchAttrId, 
			Collection<Column> searchColumns, 
			Map<String, ArrayList<Card>> labelColumnsForCards, 
			Map<String, ArrayList<Card>> childrenCardLabelsForColumn, 
			List<?> children) throws JSONException {
		final JSONObject result = new JSONObject();
		result.put(
			CardLinkPickerAttributeEditor.FIELD_CARD_ID, 
			((Long)card.getId().getId()).longValue()
		);
		final Attribute searchAttr = card.getAttributeById(searchAttrId); 
		result.put(
				CardLinkPickerAttributeEditor.FIELD_TEMPLATE,			
			card.getTemplate() == null ? "" : card.getTemplate().getId().toString()
		);
		String temporaryLabel;
		result.put(
				CardLinkPickerAttributeEditor.FIELD_LABEL,			
				searchAttr == null ? "" : (temporaryLabel=searchAttr.getStringValue()).length() > 170 ? temporaryLabel.substring(0, 170) : temporaryLabel
			);
		List<SearchResult.Column> execColumns = new ArrayList<SearchResult.Column>(); 
		final JSONObject columns = new JSONObject();
		// ���������������� ���������� �������� ��������� �������� (������������������ - ������� ����� ��������)
		if (searchColumns==null||searchColumns.isEmpty()){
			// ���� �� ���� �� ��������� ������ �������, �� ����������� �� ���� ����������� ��������� ��������
			for( Iterator<Attribute> i = card.<Attribute>getAttributes().iterator(); i.hasNext(); ) {
				final Attribute attr = i.next();
				columns.put((String)attr.getId().getId(), attr.getStringValue());
			}
		} else {
			// ����� ����������� �� ������ ������� � ��, ������� ������������ � ��������, ���������
			// ��������� ��� ����, ��� ��������� ������� ����� ��������� labelAttrId => �������� �������� ����������� �� �� ����� ��������, � �� �� ����� � labelColumnsForCards ��� ��������������� �������
			for( Iterator<SearchResult.Column> i = searchColumns.iterator(); i.hasNext(); ) 
			{
				final SearchResult.Column originalColumn = i.next();
				Attribute attr = null;
				String columnName = "";
				// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
				// (YNikitin, 2012/08/06) ��� ��� ������ ��������� ��������� �� ���������
				String originalColumnName = "";
				SearchResult.Column column = null;
				if (originalColumn.isReplaceAttribute()){	// �� ��������, �� ������� ���� �������� ������, � ������ ���� �� ������������ 
					continue;
				}

				column = SearchResult.getRealColumnForCardIfItReplaced(originalColumn, card, searchColumns);	// �������� ������������ ������� �� ���������� (���� � ������ ��� ���������, �� ������������ ���� �������)
				execColumns.add(column);
				Attribute origAttr;
				// ��������� �������� ������������ �������
				if (originalColumn.getLabelAttrId()==null){
					// (fix) Avoid NULL Pointer exception and log problem attribute id.
					origAttr = chkGetAttr(card, originalColumn.getAttributeId());
					if (origAttr != null && origAttr.getId() != null && origAttr.getId().getId() != null)
						originalColumnName = (String)origAttr.getId().getId();
				} else {
					originalColumnName = originalColumn.getFullColumnName();
				}
				// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
				if (column.getLabelAttrId()==null){
					// (fix) Avoid NULL Pointer exception and log problem attribute id.
					attr = chkGetAttr(card, column.getAttributeId());
					if (attr != null && attr.getId() != null && attr.getId().getId() != null)
						columnName = (String)attr.getId().getId();
				} else {
					List<Card> labelCards = SearchResult.getCardsListForLabelColumn(
								labelColumnsForCards, column);
					if(labelCards == null || labelCards.isEmpty())
						labelCards = SearchResult.getCardsListForLabelColumn(
								childrenCardLabelsForColumn, column);
					if(labelCards != null) {
						for (Card c: labelCards){
							if (c != null && c.getId().getId().equals(card.getId().getId())){		// ������� �������������� �������� ����� ��������
								// (fix) Avoid NULL Pointer exception and log problem attribute id.
								attr = chkGetAttr( c, column.getAttributeId() );
								break;
							}
						}
					}
					columnName = column.getFullColumnName();
				}
				// ���� �������� ������� ��������, ������ ��� ����� � �������� � columns  
				if (attr != null && columnName != null) {
					columns.put((originalColumn!=column)?originalColumnName:columnName, getAttrValue(attr, column));
				}
			}
		}

		result.put(CardLinkPickerAttributeEditor.FIELD_COLUMNS, columns);
		if(children != null){
			JSONArray list = new JSONArray();
			for(Object child : children){
//				list.put(getCardJSON((Card) child, searchAttrId, searchColumns, labelColumnsForCards, null));
				list.put(getCardJSON((Card) child, searchAttrId, execColumns, labelColumnsForCards, childrenCardLabelsForColumn, null));

			}
			result.put(CardLinkPickerAttributeEditor.FIELD_CHILDREN, list);
		}
		return result;
	}

	private String getAttrValue(Attribute attr, SearchResult.Column col) {

		if ((attr instanceof DateAttribute) && (col.getTimePattern() != null)) {
			return ((DateAttribute)attr).getStringValue(col.getTimePattern());
		}

		return attr.getStringValue();
	}

	/**
	 *  �����-������� ��� ����������� ����������� ��������� ������ ��������� ��� ������ processChildren
	 */
	private static class ChildrenResult {
		private Map<ObjectId, List<Card>> resultCards;
		private Map<String, ArrayList<Card>> childrenCardLabelsForColumn;
		private List<Card> cards;

		private static final ChildrenResult EMPTY = new ChildrenResult();

		private ChildrenResult() {}

		private ChildrenResult(List<Card> cards) {
			this.cards = cards;
		}

		public ChildrenResult(List<Card> cards, Map<ObjectId, List<Card>> resultCards, Map<String, ArrayList<Card>> childrenCardLabelsForColumn) {
			this.resultCards = resultCards;
			this.childrenCardLabelsForColumn = childrenCardLabelsForColumn;
			this.cards = cards;
		}

		public Map<ObjectId, List<Card>> getResultCards() {
			if (null != resultCards)
				return resultCards;
			return Collections.emptyMap();
		}

		public Map<String, ArrayList<Card>> getChildrenCardLabelsForColumn() {
			if (null != childrenCardLabelsForColumn)
				return childrenCardLabelsForColumn;
			return Collections.emptyMap();
		}

		public List<Card> getCards() {
			if (null != cards)
				return cards;
			return Collections.emptyList();
		}

		public static ChildrenResult getEmpty() {
			return EMPTY;
		}
	}
}
