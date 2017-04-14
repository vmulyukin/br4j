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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.aplana.dbmi.card.hierarchy.CardHierarchyItem;
import com.aplana.dbmi.card.hierarchy.GroupingHierarchyItem;
import com.aplana.dbmi.card.hierarchy.HierarchicalCardList;
import com.aplana.dbmi.card.hierarchy.Hierarchy;
import com.aplana.dbmi.card.hierarchy.HierarchyItem;
import com.aplana.dbmi.card.hierarchy.HierarchyLoader;
import com.aplana.dbmi.card.hierarchy.descriptor.HierarchyDescriptor;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

public class CardHierarchyServlet extends AbstractDBMIAjaxServlet {
	// inherit: logger = LogFactory.getLog(CardHierarchyServlet.class);
	private static final long serialVersionUID = 1L;
	public static final String PARAM_CALLER = "caller";
	public static final String PARAM_CHECKED_CARDS = "checkedCards";
	public static final String PARAM_HIERARCHY_KEY = "hierarchyKey";
	public static final String PARAM_REQUEST_TYPE = "requestType";
	public static final String PARAM_FILTER_QUERY = "filterQuery";

	// ��������� ���� �������� � ��������
	public static final String REQUEST_OPEN_HIERARCHY = "openHierarchy";	// ������� ����� ��������
	public static final String REQUEST_FILTER_HIERARCHY = "filterHierarchy";	// ������������� ��������
	public static final String REQUEST_ADD_ITEMS = "addItems";				// �������� ���� � ��� �������� ��������
	public static final String REQUEST_LOAD_CHILDREN = "loadChildren";		//TODO ��������� �������� ����
	public static final String REQUEST_ALL_ITEMS = "allItems";				// �������� ��� ���� �� ��� �������� ��������

	@Override
	protected void generateResponse(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		try {
			String caller = request.getParameter(PARAM_CALLER);
			CardHierarchyServletParameters params;
			if (HierarchicalCardListAttributeParameters.CALLER.equals(caller)) {
				params = new HierarchicalCardListAttributeParameters();				
			} else if (CardLinkPickerHierarchyParameters.CALLER.equals(caller)) {
				params = new CardLinkPickerHierarchyParameters();
			} else if (DocumentPickerParameters.CALLER.equals(caller)) {
				params = new DocumentPickerParameters();
			} else if (ResponseTransmittedParameters.CALLER.equals(caller)) {
				params = new ResponseTransmittedParameters();
			} else {
				throw new ServletException("Unknown caller: " + caller);
			}
			params.init(request);
			ResourceBundle messages = ResourceBundle.getBundle("com.aplana.dbmi.card.nl.CardHierarchyResource", request.getLocale());
			String noDocMsgParameter = "noDocsMsg";
			// � ������ ���� ��� �������� ������� � ��� ����� �������� ���� ������������� �������� ��������� noDocsMsg, ����� ������������ ��� 
			try{
				ObjectId attrId = (ObjectId)params.getClass().getDeclaredMethod("getAttrId").invoke(params);
				if (attrId!=null){
					noDocMsgParameter = noDocMsgParameter+"."+attrId.getId(); 
				}
				if (messages.getString(noDocMsgParameter)==null)
					noDocMsgParameter = "noDocsMsg";
			} catch (Exception e){
				// ������ �� ������
			}
			String noDocMsg = null;
			// �������� ��������� � ������ ���������� (���� ��� �� ����� ������ ������, ����� ��������� ��������� �� ��������� "No documents found")
			try{noDocMsg = messages.getString(noDocMsgParameter);}	catch(MissingResourceException e){logger.warn(noDocMsgParameter + " is not defined.");}
			String req_type = request.getParameter(PARAM_REQUEST_TYPE);
			String jsonResponse;
			if (req_type == null || REQUEST_OPEN_HIERARCHY.equalsIgnoreCase(req_type)) {
				// ��� �������� ������ ������������� ���������� ��������� ����� ��������,
				// �.�. ������ ��� ������� �������� ����� �������� � ������ ������ �������� 
				// ����� ������ � ������ � ��������. 
				final boolean all_at_once = (req_type == null);
				final Set<Long> checkedIds = idSetStringToLong(request.getParameter(PARAM_CHECKED_CARDS));
				final HierarchyConnection hconn = createHierarchyConnection(getDataServiceBean(request), 
						Collections.EMPTY_SET, request.getParameter(PARAM_FILTER_QUERY), params);
				final JSONObject jo = new JSONObject();
				jo.put("data", hierarchyToJSONObject(hconn, checkedIds, all_at_once));
				jo.put("notShownCards", idSetLongToJSON(checkedIds));
				hconn.setNotShownItems(checkedIds);
				params.storeHierarchyConnection(hconn);
				jo.put("endOfData", !hconn.hasNextTopLevelItems());
				jo.put("noDocsMsg", noDocMsg);
				jsonResponse = jo.toString();
			} else if (
						REQUEST_ADD_ITEMS.equalsIgnoreCase(req_type)
						|| REQUEST_ALL_ITEMS.equalsIgnoreCase(req_type)
					) 
			{
				final boolean all_at_once = REQUEST_ALL_ITEMS.equalsIgnoreCase(req_type);
				final HierarchyConnection hconn = getHierarchyConnection(getDataServiceBean(request), Collections.EMPTY_SET, null, params);
				final JSONObject jo = new JSONObject();
				jo.put("data", hierarchyItemsToJSONArray(hconn, hconn.getNotShownItems(), all_at_once)); // ����� ����-������� -- ���������� hconn.getNotShownItems() ����������
				jo.put("notShownCards", idSetLongToJSON(hconn.getNotShownItems()));
				jo.put("endOfData", !hconn.hasNextTopLevelItems());
				jo.put("noDocsMsg", noDocMsg);
				jsonResponse = jo.toString();
			} else if (REQUEST_FILTER_HIERARCHY.equalsIgnoreCase(req_type)) {
				Set<Long> checkedIds = idSetStringToLong(request.getParameter(PARAM_CHECKED_CARDS));
				HierarchyConnection hconn = getHierarchyConnection(getDataServiceBean(request), 
						Collections.EMPTY_SET, request.getParameter(PARAM_FILTER_QUERY), params);
				JSONObject jo = new JSONObject();
				checkedIds.addAll(hconn.getNotShownItems());
				jo.put("data", hierarchyToJSONObject(hconn, checkedIds, false));
				jo.put("notShownCards", idSetLongToJSON(checkedIds));
				hconn.setNotShownItems(checkedIds);
				jo.put("endOfData", !hconn.hasNextTopLevelItems());
				jo.put("noDocsMsg", noDocMsg);
				jsonResponse = jo.toString();
			} else if (REQUEST_LOAD_CHILDREN.equalsIgnoreCase(req_type)) {
				throw new ServletException("Request is not supported yet: " + req_type);
			} else {
				throw new ServletException("Unknown request type: " + req_type);
			}
			response.getWriter().print(jsonResponse);
		} catch (Exception e) {
			logger.error("Exception caught while loading card hierarchy", e);
		}
	}

	private JSONObject selectItems(JSONObject node, Set<Long> checkedIds) 
		// throws JSONException 
	{
		try {
			if ("card".equals(node.getString("type"))) {
				long id = Long.parseLong(node.get("cardId").toString());
				if (checkedIds.remove(id)) {
					node.put("checked", true);
				}
			}
		} catch (JSONException e) {}
		try {
			JSONArray children = node.getJSONArray("children");
			if (children != null) {
				int l = children.length();
				for (int i = 0; i < l; i++) {
					selectItems(children.getJSONObject(i), checkedIds);
				}
			}
		} catch (JSONException e) {}
		return node;
	}

	private JSONObject hierarchyToJSONObject(HierarchyConnection hconn, 
			Set<Long> checkedIds, boolean all_at_once
		) throws JSONException 
	{
		JSONObject jo = new JSONObject();
		jo.put("label", "label");
		jo.put("identifier", "id");
		jo.put("items", hierarchyItemsToJSONArray(hconn, checkedIds, all_at_once));
		return jo;
	}

	/**
	 * 
	 * @param hconn
	 * @param checkedIds
	 * @param all_at_once: true = ������ ����� ��;
	 * @return
	 * @throws JSONException
	 */
	private JSONArray hierarchyItemsToJSONArray(HierarchyConnection hconn, 
				Set<Long> checkedIds, boolean all_at_once) 
			throws JSONException 
	{
		final JSONArray result = new JSONArray(); // + while not getNextLevel .size > 0

		// final int maxBlocks = 100; // ������ �� ���������
		// int curBlock = 0;
		do {
			// if (++curBlock > maxBlocks) throw new JSONException( MessageFormat.format( "too many blocks (more than {0}) in hierarchy",	new Object[]{maxBlocks})); // ������ �� ���������
			final List<HierarchyItem> items = hconn.getNextTopLevelItems();
			if (items != null) { 
				for (HierarchyItem item: items) {
					result.put(selectItems(item.toJSONObject(), checkedIds));
				} // for
			} // if
		} while ( all_at_once && hconn.hasNextTopLevelItems() );

		return result;
	}

	// �������� ���������� � ��������� ��� ������� �����
	protected HierarchyConnection getHierarchyConnection(DataServiceBean serviceBean, 
			Set<ObjectId> checkedIds, String filterQuery, CardHierarchyServletParameters params) 
		throws DataException, ServiceException 
	{
		HierarchyConnection hconn = params.getHierarchyConnection();
		if (hconn == null) {
			hconn = createHierarchyConnection(serviceBean, checkedIds, filterQuery, params);
			params.storeHierarchyConnection(hconn);
		} else if (filterQuery != null) {
			hconn.reset(filterQuery);
		}
		return hconn;
	}

	private Set<Long> idSetStringToLong(String commaSeparatedCardIds) {
		if (commaSeparatedCardIds != null && !commaSeparatedCardIds.equals("")) {
			String[] ids = commaSeparatedCardIds.split(",");
			Set<Long> checkedIds = new HashSet<Long>((int)(ids.length * 1.3));
			for (int i = 0; i < ids.length; ++i) {
				checkedIds.add(Long.parseLong(ids[i]));
			}
			return checkedIds;
		}
		return new HashSet<Long>();
	}

//	private Set<ObjectId> idSetLongToObjectId(Set<Long> idSet) {
//		Set<ObjectId> checkedIds = new HashSet<ObjectId>(idSet.size());
//		for (Long id: idSet)
//			checkedIds.add(new ObjectId(Card.class, id));
//		return checkedIds;
//	}

	private JSONArray idSetLongToJSON(Set<Long> idSet) {
		JSONArray notShown = new JSONArray();
		for (Long id: idSet) {
			notShown.put(id.longValue());
		}
		return notShown;
	}

	// ������� ����� ���������� � ���������
	protected HierarchyConnection createHierarchyConnection(DataServiceBean serviceBean, 
			Set<ObjectId> checkedIds, String filterQuery, 
			CardHierarchyServletParameters params
		) throws DataException, ServiceException 
	{
		final HierarchyDescriptor descriptor = params.getHierarchyDescriptor();
		Hierarchy hierarchy = new Hierarchy(descriptor);
		HierarchyLoader hl = new HierarchyLoader();
		hl.setCheckedCardIds(checkedIds);
		hl.setServiceBean(serviceBean);
		hl.setHierarchy(hierarchy);
		hl.initializeActions(params.getActionsManager());
		String hierarchySQL = hierarchy.getDescriptor().getHierarchySQL();
		Collection<?> cardIds = null;
		if(hierarchySQL==null || hierarchySQL.isEmpty()){
			cardIds = params.getStoredCards();
		}
		try {
			hl.load(cardIds); 
			if (filterQuery != null && !filterQuery.equals(""))
				hierarchy = filterHierarchy(hierarchy, filterQuery);
		} catch (Exception e) {
			logger.error("Exception caught during loading of hierarchical card list", e);
			throw new DataException(e);
		}
		return new HierarchyConnection(hierarchy);
	}

	// ���������� ��������, �������� ������ �� ��������, ������ ������� �������� 
	// ������ query (������� �� �����)
	// (!) query ����� ��������� ��������� ��������, ����������� ��������� '|' ��� '%'
	static Hierarchy filterHierarchy(Hierarchy hierarchy, String query) 
	{ 
		if (query == null || "".equals(query))
			return hierarchy;

		final Hierarchy result = new Hierarchy(hierarchy.getDescriptor());

		/* ���������, ��� � ������ ������ ��������� ������� ����� ����������� 
		 * ��������� '|' ��� '%'.
		 * (!) ���� ��������� �� �������������, ����� ����� ������������ 
		 * ������ "\\s*[^\\\\][|%]\\s*" � ������ �������� �����������.
		 */
		//final String[] patterns = query.toLowerCase().split("\\s*[|%]\\s*");
		/* 19.08.2011
		 * ��������� ������� ����� ����������� � "". 	
		 * ������ ��� "+" - "�" 
		 * "-" - "��"
		 * "|" - "���"
		 */
		// final String reg = "[-\\s|+]*((\"(\\.|[^\"])*\")|[^-+|\\s]+)";
		final String reg = "\\s+[-|+\\s]*((\"(\\.|[^\"])*\")|[^\\s]+)";
		final List<String[]> patterns = new ArrayList<String[]>();	
		final Pattern pattern = Pattern.compile(reg);
		final Matcher matcher = pattern.matcher(" " + query);
		boolean first = true;		
		while (matcher.find()) {
			final String[] oper = new String[2];
			String word = matcher.group().replace("\"", "").trim();
			String operator = "";
			if (first)
				operator = "!";
			else if (word.matches("[-+|].*")) {	
				operator = word.substring(0, 1);
				word = word.substring(1, word.length()).trim();
			} else if (word.matches("[^\\s]+"))
				operator = "+";
			else 
				operator = "";
			oper[0] = word;
			oper[1] = operator;
			patterns.add(oper);
			first = false;
		}

		final List<HierarchicalCardList> roots = filterCardLists( hierarchy.getRoots(), patterns);

		result.setRoots( (roots == null) ? new ArrayList<HierarchicalCardList>() : roots);

		return result;
	}

	private static List<HierarchicalCardList> filterCardLists(List<HierarchicalCardList> cardLists, 
			final List<String[]> patterns) {
		if (cardLists == null || cardLists.isEmpty())
			return null;
		final List<HierarchicalCardList> result = new ArrayList<HierarchicalCardList>();
		for (HierarchicalCardList cardList: cardLists) {
			final List<HierarchyItem> topLevel = cardList.getTopLevelItems();
			final List<HierarchyItem> cards = cardList.getCardItems();
			final List<HierarchyItem> filteredCards = filterCardItems(cards, patterns);
			if (filteredCards != null) {
				final HierarchicalCardList newCardList = new HierarchicalCardList(cardList.getAlias());
				newCardList.setStored(cardList.isStored());
				if (topLevel.size() == cards.size()) {
					newCardList.setTopLevelItems(filteredCards);
					newCardList.setCardItems(filteredCards);
				} else {
					newCardList.setCardItems(filteredCards);
					newCardList.setTopLevelItems(
							filterGroupingItems(
									topLevel, 
									new HashSet<HierarchyItem>(filteredCards)
							));
				}
				result.add(newCardList);
			}
		}
		if (result.isEmpty())
			return null;
		return result;
	}

	private static String getCardLabel(CardHierarchyItem cardItem) {
		String label = null;
		if (cardItem.getLabelAttr() != null)
			label = cardItem.getLabelAttr().getStringValue();
		if (cardItem.getLabelFormat() != null)
			label = MessageFormat.format(cardItem.getLabelFormat(), new Object[] { label });
		return (label != null) ? label.trim() : null;
	}
	
	/**
	 * ����� �������� ����� �����, ����� ������� �������� (��� �����) ������ 
	 * ������� �� patterns.
	 * @param items
	 * @param patterns ������ ����� ��� ������ � ������ items.
	 * @return
	 */
	private static List<HierarchyItem> filterCardItems(final List<HierarchyItem> items, 
			final List<String[]> patterns) {
		if (items == null || items.isEmpty())
			return null;
		final Set<HierarchyItem> result = new HashSet<HierarchyItem>();
		final HashMap<HierarchyItem, String> labels = new HashMap<HierarchyItem, String>();
		//items:
		for( HierarchyItem i : items ) {
			final CardHierarchyItem cardItem = (CardHierarchyItem) i;
			/* ���������� ������ �������� ��������� ������ � ��� �� ��������
			List<HierarchicalCardList> children = filterCardLists(cardItem.getChildren());
			cardItem.setChildren(children);
			String label = getCardLabel(cardItem);
			if (children != null || 
					(label != null && label.toLowerCase().contains(helperQuery)))
				result.add(cardItem);
			*/
			// ���������� �������� ��������� ������, ��� �� �������� � ��� �������� ��������
			final String label = getCardLabel(cardItem);
			final List<HierarchicalCardList> children = filterCardLists( cardItem.getChildren(), patterns);
			CardHierarchyItem newCardItem = cardItem;
			if (children != null) {
				newCardItem = cardItem.makeCopy();
				newCardItem.setChildren(children);
				newCardItem.setCollapsed(false);
				result.add( newCardItem);
			} else if (label != null) {
				// �������� ���� �������� ...
				final String lower = label.toLowerCase();
				/*	
				for (String pattern: patterns) {
					if (!lower.contains(pattern))
						continue items;
				}
				 */
				labels.put(cardItem, lower);
			}
		}

		for (String[] pattern : patterns) {
			final String word = pattern[0];
			final String operator = pattern[1];
			if (word == null) continue;
			final Set<HierarchyItem> temp = new HashSet<HierarchyItem>();
			for (Map.Entry<HierarchyItem, String> label : labels.entrySet()) {
				if (label.getValue().contains(word.toLowerCase()))
					temp.add(label.getKey());
			}
			if (operator.equals("+")) {
				result.retainAll(temp);
			} else if (operator.equals("-")) {
				result.removeAll(temp);
			} else if (operator.equals("|") || operator.equals("!")) {
				result.addAll(temp);
			}
		}
		return result.isEmpty() ? null : new ArrayList<HierarchyItem>(result);
	}

	private static List<HierarchyItem> filterGroupingItems(
				List<HierarchyItem> items, 
				Set<HierarchyItem> filteredCards) {
		if (items == null || items.isEmpty())
			return null;
		final List<HierarchyItem> result = new ArrayList<HierarchyItem>();
		for (HierarchyItem item: items) {
			if (item instanceof CardHierarchyItem) {
				if (filteredCards.contains(item))
					result.add(item);
			} else if (item instanceof GroupingHierarchyItem) {
				final GroupingHierarchyItem groupingItem = (GroupingHierarchyItem)item;
				final List<HierarchyItem> children = filterGroupingItems(groupingItem.getChildren(), filteredCards);
				if (children != null) {
					final GroupingHierarchyItem newGroupingItem = groupingItem.makeCopy();
					newGroupingItem.setChildren(children);
					newGroupingItem.setCollapsed(false);
					result.add(newGroupingItem);
				}
			} else {
				throw new IllegalStateException("Unknown item type: " + item.getClass().getCanonicalName());
			}
		}
		if (result.isEmpty())
			return null;
		return result;
	}
	
}