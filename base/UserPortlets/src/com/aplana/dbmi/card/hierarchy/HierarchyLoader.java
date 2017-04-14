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
package com.aplana.dbmi.card.hierarchy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.BulkFetchChildrenCards;
import com.aplana.dbmi.action.FetchChildrenCards;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.actionhandler.ActionHandler;
import com.aplana.dbmi.actionhandler.ActionsManager;
import com.aplana.dbmi.actionhandler.descriptor.ActionHandlerDescriptor;
import com.aplana.dbmi.actionhandler.descriptor.SelectionType;
import com.aplana.dbmi.card.hierarchy.descriptor.CardItemsMergeMode;
import com.aplana.dbmi.card.hierarchy.descriptor.CardSetDescriptor;
import com.aplana.dbmi.card.hierarchy.descriptor.GroupingDescriptor;
import com.aplana.dbmi.card.hierarchy.descriptor.HierarchyDescriptor;
import com.aplana.dbmi.card.hierarchy.descriptor.LinkDescriptor;
import com.aplana.dbmi.card.hierarchy.descriptor.SortOrder;
import com.aplana.dbmi.card.hierarchy.descriptor.StylingDescriptor;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.LocalizedString;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.TreeAttribute;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

public class HierarchyLoader {
	private final static Long EMPTY_ID = new Long(-1);
	private Log logger = LogFactory.getLog(getClass());
	private final static int MAX_CHILDREN_LEVEL = 30;
	
	private Hierarchy hierarchy;
	private DataServiceBean serviceBean;
	private Set checkedCardIds;
	
	// Local cache of initialized actionHandlers
	private Map activeActionHandlers;
	
	// Local cache for attribute definitions
	// For now used for TreeAttributes only
	private Map attributeDefinitions = new HashMap();
	
	/**
	 * Utility class used while fetching parents of stored cardset
	 */
	private static class LoadingItem {
		private Card card;
		private Map children;
		private String mainChildrenAlias;
		private String alias;
		private boolean orphan = true;
		
		public Card getCard() {
			return card;
		}
		public void setCard(Card card) {
			this.card = card;
		}
		public Map getChildren() {
			return children;
		}
		public void setChildren(Map children) {
			this.children = children;
		}
		/**
		 * @return alias of cardset to which this card belongs
		 */		
		public String getAlias() {
			return alias;
		}
		public void setAlias(String alias) {
			this.alias = alias;
		}
		/**
		 * @return true if this card was taken from stored cardset.
		 * Note that there is could exists cards with same alias as in stored cardset
		 * but with storedCard == false (for example if stored cardset 
		 * references itself as a parent)
		 */		
		public boolean isStoredCard() {
			return mainChildrenAlias == null;
		}
		/**
		 * @return alias of children card items which are linked with this one through direct parent link
		 * Children could have different alias if they are added to this item during nodes merge
		 * operation 
		 */		
		public String getMainChildrenAlias() {
			return mainChildrenAlias;
		}
		public void setMainChildrenAlias(String mainChildrenAlias) {
			this.mainChildrenAlias = mainChildrenAlias;
		}
		
		public boolean isOrphan(){
			return orphan;
		}
		
		public void setOrphan(boolean orphan){
			this.orphan = orphan;
		}
	}	
	
	public Hierarchy getHierarchy() {
		return hierarchy;
	}
	public void setHierarchy(Hierarchy hierarchy) {
		this.hierarchy = hierarchy;
	}
	public void setServiceBean(DataServiceBean serviceBean) {
		this.serviceBean = serviceBean;
	}
	public void initializeActions(ActionsManager actionsManager) {
		if (actionsManager == null) {
			activeActionHandlers = new HashMap(0);
			return;
		}
		activeActionHandlers = new HashMap(actionsManager.getActiveActionIds().size());
		Iterator i = actionsManager.getActiveActionIds().iterator();
		while (i.hasNext()) {
			String actionId = (String)i.next();
			ActionHandlerDescriptor ad = actionsManager.getActionsDescriptor().getActionHandlerDescriptor(actionId);
			if (!SelectionType.NONE.equals(ad.getSelectionType())) {
				ActionHandler ah = actionsManager.createInstance(actionId);
				activeActionHandlers.put(actionId, ah);
			}
		}
	}	
	public void load(Collection cardIds) throws DataException, ServiceException {
		HierarchyDescriptor hierarchyDescr = (HierarchyDescriptor)hierarchy.getDescriptor();
		
		CardSetDescriptor storedDescr = hierarchyDescr.getStoredDescriptor();
		Search search = new Search();
		search.setColumns(getSearchColumns(hierarchyDescr, storedDescr));
		search.setSqlXmlName(hierarchyDescr.getHierarchySQL());
		if(!search.isBySql()){
		search.setByCode(true);
			search.setWords(ObjectIdUtils.numericIdsToCommaDelimitedString(cardIds));
		}
		SearchResult searchResult = (SearchResult)serviceBean.doAction(search);
		List storedCards = (List)searchResult.getCards();
		
		hierarchy.setRoots(new ArrayList());		
		if (storedDescr.getParentLinks() != null) {
			Map currentLevel = new HashMap(storedCards.size());
			Map viewedCards = new HashMap();
			Iterator k = storedCards.iterator();
			while (k.hasNext()) {
				Card c = (Card)k.next();
				LoadingItem item = new LoadingItem();
				item.setCard(c);
				item.setAlias(storedDescr.getAlias());
				item.setMainChildrenAlias(null);
				currentLevel.put(c.getId(), item);
				String key = getCardKey(c, storedDescr);
				if (key != null) {
					viewedCards.put(key, item);
				}
			}
			fetchParents(currentLevel, viewedCards, storedDescr, /*new ArrayList(),*/ 0);	
		} else {
			HierarchicalCardList root = createHierarchicalCardList(storedCards, storedDescr);
			fetchChildren(root.getCardItems(), storedDescr, 1);
			hierarchy.getRoots().add(root);
		}
		
	}
	
	private String getCardKey(Card c, CardSetDescriptor csd) {
		if (CardItemsMergeMode.CARDSET.equals(hierarchy.getDescriptor().getParentMergeMode())) {
			return c.getId().getId() + ":" + csd.getAlias();	
		} else {
			return null;
		}
	}	
	
	private Map fetchParents(Map currentLevel, Map viewedCards, CardSetDescriptor cardSetDescriptor, int count) throws DataException, ServiceException {
		Map addedRootItems = new HashMap();
		Set skippedItems = new HashSet();

		if (count >= MAX_CHILDREN_LEVEL) {
			logger.warn("Too many parent levels. Ignoring parents with level greater than " + MAX_CHILDREN_LEVEL);
			return addedRootItems;
		}
		
		HierarchyDescriptor hierarchyDescr = (HierarchyDescriptor)hierarchy.getDescriptor();
		
		if(cardSetDescriptor.getParentLinks() != null && !cardSetDescriptor.getParentLinks().isEmpty()){
			
			for(LinkDescriptor parentLink : cardSetDescriptor.getParentLinks()){
				
				CardSetDescriptor parentCardSetDescriptor = hierarchyDescr.getCardSetDescriptor(parentLink.getTargetSetAlias());
				Map parents = new HashMap();			
				BulkFetchChildrenCards action = new BulkFetchChildrenCards();
				action.setColumns(getSearchColumns(hierarchyDescr, parentCardSetDescriptor));
				action.setLinkAttributeId(parentLink.getCardLinkAttr());				 
				action.setReverseLink(parentLink.isReverse());
				action.setParentCardIds(new ArrayList(currentLevel.keySet()));
				BulkFetchChildrenCards.Result actionResult = serviceBean.doAction(action);
				Map parentsByChildren = actionResult.getCards(); 
	
				Iterator i = currentLevel.values().iterator();
				while (i.hasNext()) {
					LoadingItem item = (LoadingItem)i.next();
					if (skippedItems.contains(item.getCard().getId()))
						continue;
					Collection currentItemParents = (Collection)parentsByChildren.get(item.getCard().getId());
					if (currentItemParents != null && !currentItemParents.isEmpty()) {
						item.setOrphan(false);
						// �� ���� � ������ ������ � �������� ����� ���� ������ ���� ������������, ��
						// ��� ����������������� � �������, ����� � ������ ����������� ��������� ���������
						// ��� ��������, ����� �� �����-�� ������� ��� ����� ���� � �� �� �������� ���������
						// �� "stored" cardSet'a ��������� � ��������� "�����������" ��������
						Iterator j = currentItemParents.iterator();
						while (j.hasNext()) {
							Card pc = (Card)j.next();
							LoadingItem parent = null;
							try {
								if (pc != null) parent = (LoadingItem)parents.get(pc.getId());
							} catch (Exception e) {
								logger.error("Exception caught during loading of hierarchical card list", e);
								throw new DataException(e);
							}
							if (parent == null) {
								String key = null;
								if (pc != null)
								key = getCardKey(pc, parentCardSetDescriptor);
								if (key != null && viewedCards.containsKey(key)) {
									parent = (LoadingItem)viewedCards.get(key);
								} else {
									parent = new LoadingItem();
									parent.setCard(pc);
									parent.setChildren(new HashMap());
									parent.setMainChildrenAlias(cardSetDescriptor.getAlias());
									parent.setAlias(parentCardSetDescriptor.getAlias());
									if (pc != null)
									parents.put(pc.getId(), parent);
									if (key != null) {
										viewedCards.put(key, parent);
									}
								}
							}
							parent.getChildren().put(item.getCard().getId(), item);
						}
						if (parentLink.isSkipNextIfFound()){
							// �������� ��� ������ ������ �� ������� ����� ��� ������, 
							// �� ����� ��������� ��� ������ � ������ ��������� �� ��������� ������
							skippedItems.add(item.getCard().getId());
						}
					}
				}

				if(parents != null && !parents.isEmpty()) {
					addedRootItems.putAll(fetchParents(parents, viewedCards, parentCardSetDescriptor, count + 1));
				}
			}
		}

		Map orphans = new HashMap();
		for(LoadingItem item : (Collection<LoadingItem>) currentLevel.values()) {
			if(item.isOrphan() && !addedRootItems.containsKey(item.getCard().getId())) // avoid duplicate roots
				orphans.put(item.getCard().getId(), item);
		}
		if(!orphans.isEmpty())
			hierarchy.getRoots().addAll(convertToHierarchicalCardList(orphans, cardSetDescriptor.getAlias()));
		return orphans;
	}
	
	private List convertToHierarchicalCardList(Map loadingItems, String mainChildrenAlias) throws DataException, ServiceException {
		Map cardsByCardSet = new HashMap();
		Iterator i = loadingItems.values().iterator();
		while (i.hasNext()) {
			LoadingItem item = (LoadingItem)i.next();
			String alias = item.getAlias();
			List list = (List)cardsByCardSet.get(alias);
			if (list == null) {
				list = new ArrayList();
				cardsByCardSet.put(alias, list);
			}
			list.add(item.getCard());
		}
		HierarchyDescriptor hd = hierarchy.getDescriptor();

		List result = new LinkedList();
		i = cardsByCardSet.keySet().iterator();
		while (i.hasNext()) {
			String alias = (String)i.next();
			List cards = (List)cardsByCardSet.get(alias);
			CardSetDescriptor descr = hd.getCardSetDescriptor(alias);			
			HierarchicalCardList hierarchicalCardList = createHierarchicalCardList(cards, descr);
			if (hd.getStoredDescriptor() == descr) {
				List storedCardItems = new ArrayList();
				Iterator j = hierarchicalCardList.getCardItems().iterator();
				while (j.hasNext()) {
					CardHierarchyItem ci = (CardHierarchyItem)j.next();
					LoadingItem item = (LoadingItem)loadingItems.get(ci.getCard().getId());
					if (item.isStoredCard()) {
						storedCardItems.add(ci);
					}
				}
				if (!storedCardItems.isEmpty()) {
					fetchChildren(storedCardItems, descr, 1);
				}
			}
			Iterator j = hierarchicalCardList.getCardItems().iterator();		
			while (j.hasNext()) {
				CardHierarchyItem ci = (CardHierarchyItem)j.next();
				LoadingItem item = (LoadingItem)loadingItems.get(ci.getCard().getId());
				if (item.getChildren() != null) {
					List itemChildren = convertToHierarchicalCardList(item.getChildren(), item.getMainChildrenAlias());
					if (ci.getChildren() == null) {
						ci.setChildren(itemChildren);
					} else {
						ci.getChildren().addAll(0, itemChildren);
					}
				}
			}
			
			if (alias.equals(mainChildrenAlias)) {
				result.add(hierarchicalCardList);
			} else {
				result.add(0, hierarchicalCardList);
			}
		}	

		return result;
	}

	/**
	 * Loads children records 
	 * @param h
	 * @param cardItems
	 * @param csd
	 * @param hd
	 * @param serviceBean
	 * @throws DataException
	 * @throws ServiceException
	 */
	private void fetchChildren(List cardItems, CardSetDescriptor csd, int level) throws DataException, ServiceException {
		if (level > MAX_CHILDREN_LEVEL) {
			logger.warn("Too many children levels. All children after " + MAX_CHILDREN_LEVEL + "th level will be ignored");
			return;
		}
		if (cardItems.size() == 0) {
			logger.info("Empty card set skipped");
			return;
		}
		
		Iterator i = cardItems.iterator();
		HierarchyDescriptor hd = hierarchy.getDescriptor();
		List parentIds = new ArrayList(cardItems.size());
		while (i.hasNext()) {
			CardHierarchyItem ci = (CardHierarchyItem)i.next();
			ci.setChildren(new ArrayList(csd.getChildrenLinks().size()));
			parentIds.add(ci.getCard().getId());
		}
		
		i = csd.getChildrenLinks().iterator();
		while (i.hasNext()) {
			LinkDescriptor ld = (LinkDescriptor)i.next();
			CardSetDescriptor childrenDescriptor = hd.getCardSetDescriptor(ld.getTargetSetAlias());
			BulkFetchChildrenCards a = new BulkFetchChildrenCards();
			a.setColumns(getSearchColumns(hd, childrenDescriptor));
			a.setParentCardIds(parentIds);
			a.setReverseLink(ld.isReverse());
			a.setLinkAttributeId(ld.getCardLinkAttr());
			a.setChildrenTemplates(ld.getTemplates());
			a.setChildrenStates(ld.getStatuses());
			BulkFetchChildrenCards.Result actionResult = serviceBean.doAction(a);
			Map childrenMap = actionResult.getCards();
			Iterator j = cardItems.iterator();
			while (j.hasNext()) {
				CardHierarchyItem ci = (CardHierarchyItem)j.next();
				HierarchicalCardList childrenCardSet = createHierarchicalCardList((List)childrenMap.get(ci.getCard().getId()), childrenDescriptor);
				ci.getChildren().add(childrenCardSet);
				fetchChildren(childrenCardSet.getCardItems(), childrenDescriptor, level + 1);
			}
		}
		
		fetchInfoItems(cardItems, csd, level);
	}
	
	private void fetchInfoItems(List cardItems, CardSetDescriptor csd, int level) throws DataException, ServiceException {
		if (level > MAX_CHILDREN_LEVEL) {
			logger.warn("Too many children levels. All children after " + MAX_CHILDREN_LEVEL + "th level will be ignored");
			return;
		}
		HierarchyDescriptor hd = hierarchy.getDescriptor();
		List parentIds = new ArrayList(cardItems.size());
		for (Iterator i = cardItems.iterator(); i.hasNext(); ) {
			CardHierarchyItem ci = (CardHierarchyItem) i.next();
			ci.setInfoItems(new ArrayList(csd.getInfoLinks().size()));
			parentIds.add(ci.getCard().getId());
		}
		
		for (Iterator i = csd.getInfoLinks().iterator(); i.hasNext(); ) {
			LinkDescriptor ld = (LinkDescriptor) i.next();
			CardSetDescriptor infoDescr = hd.getCardSetDescriptor(ld.getTargetSetAlias());
			BulkFetchChildrenCards a = new BulkFetchChildrenCards();
			a.setColumns(getSearchColumns(hd, infoDescr));
			a.setParentCardIds(parentIds);
			a.setReverseLink(ld.isReverse());
			a.setLinkAttributeId(ld.getCardLinkAttr());
			BulkFetchChildrenCards.Result actionResult = serviceBean.doAction(a);
			Map infoItemMap = actionResult.getCards();
			for (Iterator ii = cardItems.iterator(); ii.hasNext(); ) {
				CardHierarchyItem ci = (CardHierarchyItem) ii.next();
				HierarchicalCardList infoSet = createHierarchicalCardList((List) infoItemMap.get(ci.getCard().getId()), infoDescr);
				ci.getInfoItems().add(infoSet);
				fetchChildren(infoSet.getCardItems(), infoDescr, level + 1);
			}
		}
	}
	
	private List getSearchColumns(HierarchyDescriptor hierarchyDescriptor, CardSetDescriptor cardSetDescriptor) {
		Set columnAttrs = new HashSet();
		Iterator i = hierarchyDescriptor.getColumns(cardSetDescriptor.getColumnsKey()).iterator();
		while (i.hasNext()) {
			SearchResult.Column c = (SearchResult.Column)i.next();
			columnAttrs.add(c.getAttributeId());
		}
		i = cardSetDescriptor.getGrouping().iterator();
		while (i.hasNext()) {
			GroupingDescriptor gd = (GroupingDescriptor)i.next();
			columnAttrs.add(gd.getAttr());
		}
		i = hierarchyDescriptor.getStyles().iterator();
		while (i.hasNext()) {
			StylingDescriptor sd = (StylingDescriptor)i.next();
			CardFilterCondition condition = sd.getCondition();
			addConditionColumns(condition, columnAttrs);
		}
		
		i = activeActionHandlers.values().iterator();
		while (i.hasNext()) {
			ActionHandler ah = (ActionHandler)i.next();			
			if (ah.getCondition() != null) {
				addConditionColumns(ah.getCondition(), columnAttrs);
			}
		}
		if (cardSetDescriptor.getCondition() != null) {
			addConditionColumns(cardSetDescriptor.getCondition(), columnAttrs);
		}
		columnAttrs.add(cardSetDescriptor.getLabelAttr());
		if (cardSetDescriptor.getSecondaryLabelAttr() != null) {
			columnAttrs.add(cardSetDescriptor.getSecondaryLabelAttr());
		}
		if (cardSetDescriptor.getSortAttr() != null) {
			columnAttrs.add(cardSetDescriptor.getSortAttr());
		}
		List columns = new ArrayList();
		i = columnAttrs.iterator();
		while (i.hasNext()) {
			ObjectId attrId = (ObjectId)i.next();
			SearchResult.Column c = new SearchResult.Column();
			c.setAttributeId(attrId);
			if (cardSetDescriptor.getLinkedLabelAttr() != null &&
					attrId.equals(cardSetDescriptor.getLabelAttr()))
				c.setLabelAttrId(cardSetDescriptor.getLinkedLabelAttr());
			columns.add(c);
		}
		return columns;
	}
	
	private void addConditionColumns(CardFilterCondition condition, Set columnAttrs) {
		Iterator j = condition.getAttributes().keySet().iterator();
		while (j.hasNext()) {
			columnAttrs.add(j.next());
		}
		if (!condition.getStates().isEmpty()) {
			columnAttrs.add(Card.ATTR_STATE);		
		}
		if (!condition.getTemplates().isEmpty()) {
			columnAttrs.add(Card.ATTR_TEMPLATE);				
		}
	}
	
	private CardHierarchyItem cardToHierarchyItem(Card card, CardSetDescriptor cardSetDescriptor) {
		CardHierarchyItem ci = new CardHierarchyItem(hierarchy, serviceBean);
		ci.setCard(card);
		ci.setCollapsed(cardSetDescriptor.isCollapsed());
		Iterator j = activeActionHandlers.entrySet().iterator();
		List allowedActions = new ArrayList();
		while (j.hasNext()) {
			Map.Entry entry = (Map.Entry)j.next();
			String actionId = (String)entry.getKey();
			ActionHandler ah = (ActionHandler)entry.getValue();
			if (ah.isApplicableForCard(card)) {
				allowedActions.add(actionId);
			}
		}
		ci.setActions(allowedActions);
		Attribute labelAttr = (card == null) ? null 
				: card.getAttributeById(cardSetDescriptor.getLabelAttr());
		if (labelAttr == null && cardSetDescriptor.getSecondaryLabelAttr() != null) {
			labelAttr = (card == null) ? null : card.getAttributeById(cardSetDescriptor.getSecondaryLabelAttr());
		}
		ci.setLabelAttr(labelAttr);
		ci.setCheckChildren(cardSetDescriptor.isCheckChildren());
		ci.setTerminalNodesOnly(hierarchy.getDescriptor().isTerminalNodesOnly());
		if (cardSetDescriptor.getLabelFormat() != null)
			ci.setLabelFormat(cardSetDescriptor.getLabelFormat().getValue());
		
		ci.setLabelMaxLength(cardSetDescriptor.getLabelMaxLength());
		
		ci.setLabelAsLink(cardSetDescriptor.isLabelAsLink());
		ci.setLabelAsDownloadLink(cardSetDescriptor.isLabelAsDownloadLink());
		ci.setColumnsKey(cardSetDescriptor.getColumnsKey());
		if (card != null && checkedCardIds != null && checkedCardIds.contains(card.getId()) 
				|| (cardSetDescriptor.isCheckAll())) {
			ci.setChecked(true);
		}
		ci.setShowOrg(cardSetDescriptor.isShowOrg());
		return ci;
	}
	
	private List convertCardsToCardItems(List cards, CardSetDescriptor descr) {
		List cardItems = new ArrayList(cards.size());
		Iterator i = cards.iterator();
		while (i.hasNext()) {
			Card c = (Card)i.next();
			if (c == null) continue;
			if (descr.getCondition() != null && !descr.getCondition().check(c)) {
				continue;
			}
			CardHierarchyItem ci = cardToHierarchyItem(c, descr);
			cardItems.add(ci);
		}
		return cardItems;
	}
	
	private HierarchicalCardList createHierarchicalCardList(List cards, CardSetDescriptor descr) throws DataException, ServiceException {
		HierarchicalCardList result = new HierarchicalCardList(descr.getAlias());
		List topLevelItems;
		List cardItems = convertCardsToCardItems(cards, descr);
		if (descr.getGrouping().size() > 0) {
			topLevelItems = groupItems(cardItems, descr, 0);
		} else {
			topLevelItems = new ArrayList(cardItems.size());
			topLevelItems.addAll(cardItems);
			sortCardItems(topLevelItems, descr);
		}
		if (descr.getGroup() != null && topLevelItems.size() > 0) {
			GroupingHierarchyItem groupItem = new GroupingHierarchyItem(hierarchy);
			groupItem.setLabel(hierarchy.getDescriptor().getMessages().getMessage(descr.getGroup()));
			groupItem.setChildren(topLevelItems);
			topLevelItems = new ArrayList(1);
			topLevelItems.add(groupItem);
		}
		result.setTopLevelItems(topLevelItems);
		result.setCardItems(cardItems);
		
		return result;
	}

	private void sortCardItems(List<CardHierarchyItem> cardItems, CardSetDescriptor descr) {
		if (null == cardItems || cardItems.size() == 0 )
			return;
		if (descr.getSortAttr() != null && !SortOrder.AUTO.equals(descr.getSortOrder()) && !SortOrder.NONE.equals(descr.getSortOrder())) {
			Comparator comparator = null;
			if (descr.getSortOrderByParentAttr() != null && descr.getParentAttrLink() != null
						&& descr.getSortOrderByParentAttr().size() > 0) {
				Card parentCard = getParentCard(cardItems.get(0), descr);
				List<Attribute> parentSortAttrs = new ArrayList<Attribute>();
				if (null != parentCard) {
					for (ObjectId parentSortAttr: descr.getSortOrderByParentAttr()) {
						Attribute sortAttr = parentCard.getAttributeById(parentSortAttr);
						if (null != sortAttr) {
							parentSortAttrs.add(sortAttr);
						}
					}
				}
				if (parentSortAttrs.size() > 0) {
					comparator = new CardHierarchyByParentItemComparator(descr.getSortAttr(), 
							parentSortAttrs, SortOrder.DESCENDING.equals(descr.getSortOrder()));
				}else {
					comparator = new CardByIdComparator(false);
				}
			}else {
				comparator = new CardHierarchyItemComparator(descr.getSortAttr(), SortOrder.DESCENDING.equals(descr.getSortOrder()));
			}
			Collections.sort(cardItems, comparator);
		}
		else if(!SortOrder.NONE.equals(descr.getSortOrder())) {
			/* NOTE: (BR4J00004381) ������� ��� ����, ����� ���� �� ������ ������� ����������, �������� ������������� �� ������� �� ��������,
			   �.�. � ������� ����������� card_id
			   
			   UPDATE:
			   (23862) 19.03.13 ���� �� ������� ������� ����������, �� �� ��������� ����� SortOrder.AUTO ��� SortOrder.ASCENDING � ����������� �� ���� �������� �� sortAttr
			   																										��. HierarchyDescriptorReader.readCardSetDescriptors
			   ��� SortOrder.NONE ������ ���������� ��� ������ (���� ��������� ���� sortOrder="none")											
			*/
			CardByIdComparator byIdComparator = new CardByIdComparator(false);
			Collections.sort(cardItems, byIdComparator);
		}
	}
	
	private void sortGroupingItems(List groupingItems, GroupingDescriptor gd) {
		if (!SortOrder.NONE.equals(gd.getSortOrder()) && !SortOrder.AUTO.equals(gd.getSortOrder())) {
			GroupingHierarchyItemComparator comparator = new GroupingHierarchyItemComparator(
				SortOrder.DESCENDING.equals(gd.getSortOrder())
			);
			Collections.sort(groupingItems, comparator);
		}
	}
	
	/**
	 * Returns list of {@link HierarchyItem} descendants grouped
	 * as defined in given {@link GroupingDescriptor}
	 * @param cardItems
	 * @param groupDescr
	 * @return
	 * @throws ServiceException 
	 * @throws DataException 
	 */
	private List groupItems(List cardItems, CardSetDescriptor cardSetDescriptor, int groupIndex) throws DataException, ServiceException {		
		GroupingDescriptor gd = (GroupingDescriptor)cardSetDescriptor.getGrouping().get(groupIndex);
		ObjectId attrId = gd.getAttr();
		LocalizedString st = hierarchy.getDescriptor().getMessages().getMessage(gd.getDefaultItemKey());
		if (ListAttribute.class.equals(attrId.getType())) {
			return groupByListAttr(cardSetDescriptor, cardItems, st, groupIndex);
		} else if (TreeAttribute.class.equals(attrId.getType())) {
			return groupByTreeAttr(cardSetDescriptor, cardItems, st, groupIndex);
		} else {
			throw new IllegalArgumentException("Couldn't group by attribute of type: " + attrId.getType().getName());
		}
	}
	
	private GroupingHierarchyItem createGroupingItem(GroupingDescriptor gd, ReferenceValue ref, LocalizedString defaultItem) {
		
		
		GroupingHierarchyItem group;
		if (ref == null && defaultItem == null) {
			group = new GroupingHierarchyItem(hierarchy, EMPTY_ID.longValue());
		} else {
			group = new GroupingHierarchyItem(hierarchy);
		}
		group.setChildren(new ArrayList());
		if (ref == null) {
			group.setLabel(defaultItem);
		} else {
			LocalizedString label = new LocalizedString();
			label.setValueRu(ref.getValueRu());
			label.setValueEn(ref.getValueEn());
			group.setLabel(label);
		}
		group.setCollapsed(gd.isCollapsed());
		return group;
	}
	
	private void addToMap(GroupingDescriptor gd, Map groups, CardHierarchyItem item, ReferenceValue ref, LocalizedString defaultItem) {
		Object key;
		if (ref == null) {
			key = EMPTY_ID;
		} else {
			key = ref.getId().getId();
		}
		GroupingHierarchyItem group = (GroupingHierarchyItem)groups.get(key);
		if (group == null) {
			group = createGroupingItem(gd, ref, defaultItem);
			groups.put(key, group);
		}
		group.getChildren().add(item);
	}
	
	private void regroupMap(Map groups, CardSetDescriptor cardSetDescriptor, int groupIndex) throws DataException, ServiceException {
		List groupingDescriptors = cardSetDescriptor.getGrouping();
		if (groupIndex < groupingDescriptors.size() - 1) {
			Iterator i = groups.keySet().iterator();
			while (i.hasNext()) {
				Object key = i.next();
				GroupingHierarchyItem group = (GroupingHierarchyItem)groups.get(key);
				group.setChildren(groupItems(group.getChildren(), cardSetDescriptor, groupIndex + 1));
			}
		} else {
			Iterator i = groups.keySet().iterator();
			while (i.hasNext()) {
				Object key = i.next();
				GroupingHierarchyItem group = (GroupingHierarchyItem)groups.get(key);
				sortCardItems(group.getChildren(), cardSetDescriptor);
			}			
		}
	}
	
	private List groupByListAttr(CardSetDescriptor cardSetDescriptor, List cardItems, LocalizedString defaultItem, int groupIndex) throws DataException, ServiceException {
		GroupingDescriptor gd = (GroupingDescriptor)cardSetDescriptor.getGrouping().get(groupIndex);
		ObjectId attrId = gd.getAttr();
		Map groups = new HashMap();		
		Iterator i = cardItems.iterator();
		while (i.hasNext()) {
			CardHierarchyItem ci = (CardHierarchyItem)i.next();
			ListAttribute attr = (ListAttribute)ci.getCard().getAttributeById(attrId);
			addToMap(gd, groups, ci, attr == null ? null : attr.getValue(), defaultItem);
		}

		regroupMap(groups, cardSetDescriptor, groupIndex);
		
		GroupingHierarchyItem emptyItem = (GroupingHierarchyItem)groups.get(EMPTY_ID);
		if (emptyItem != null && emptyItem.getLabel() == null) {
			groups.remove(EMPTY_ID);
		}
		List result = new ArrayList(groups.values());
		sortGroupingItems(result, gd);
		if (emptyItem != null && emptyItem.getLabel() == null) {
			result.addAll(result.size(), emptyItem.getChildren());
		}
		return result;
	}
	
	private Attribute getAttribute(ObjectId attrId) throws DataException, ServiceException {
		Attribute attr;
		if (attributeDefinitions.containsKey(attrId)) {
			attr = (Attribute)attributeDefinitions.get(attrId);
		} else {
			attr = (Attribute)serviceBean.getById(attrId);
			attributeDefinitions.put(attrId, attr);
		}
		return attr;
	}
	
	private List groupByTreeAttr(CardSetDescriptor cardSetDescriptor, List cardItems, LocalizedString defaultItem, int groupIndex) throws DataException, ServiceException {
		GroupingDescriptor gd = (GroupingDescriptor)cardSetDescriptor.getGrouping().get(groupIndex);
		ObjectId attrId = gd.getAttr();
		Map groups = new HashMap();
		Iterator i = cardItems.iterator();		
		while (i.hasNext()) {
			CardHierarchyItem ci = (CardHierarchyItem)i.next();
			TreeAttribute attr = (TreeAttribute)ci.getCard().getAttributeById(attrId);
			if (attr == null || attr.isEmpty()) {
				addToMap(gd, groups, ci, null, defaultItem);
			} else {
				Iterator j = attr.getValues().iterator();
				while (j.hasNext()) {
					ReferenceValue ref = (ReferenceValue)j.next();
					addToMap(gd, groups, ci, ref, defaultItem);
					if (j.hasNext()) {
						ci = ci.makeCopy();
					}
				}
			}
		}
		regroupMap(groups, cardSetDescriptor, groupIndex);
		
		TreeAttribute attrDeclaration = (TreeAttribute)getAttribute(attrId);
		i = attrDeclaration.getReferenceValues().iterator();
		while (i.hasNext()) {
			ReferenceValue ref = (ReferenceValue)i.next();
			regroupTreeChildren(gd, groups, ref);
		}
		GroupingHierarchyItem emptyItem = (GroupingHierarchyItem)groups.get(EMPTY_ID);
		if (emptyItem != null && emptyItem.getLabel() == null) {
			groups.remove(EMPTY_ID);
		}
		List result = new ArrayList(groups.values());
		sortGroupingItems(result, gd);
		if (emptyItem != null && emptyItem.getLabel() == null) {
			result.addAll(result.size(), emptyItem.getChildren());
		}
		return result;
	}
	
	/**
	 * ������������ ��� ��������������� ���������, ��������������� �� ��������� ���� tree
	 * ������������� �������� ����������� � ����������� ���������, � ������������ � �����������
	 * �����������.
	 */
	private void regroupTreeChildren(GroupingDescriptor gd, Map groups, ReferenceValue parent) {
		List childrenItems = new ArrayList();
		if (parent.getChildren() != null) {
			Iterator i = parent.getChildren().iterator();
			while (i.hasNext()) {
				ReferenceValue child = (ReferenceValue)i.next();
				regroupTreeChildren(gd, groups, child);
				Long childKey = (Long)child.getId().getId();
				GroupingHierarchyItem gi = (GroupingHierarchyItem)groups.get(childKey);
				if (gi != null) {
					groups.remove(childKey);
					childrenItems.add(gi);
				}
			}
		}
		
		Long parentKey = (Long)parent.getId().getId(); 
		GroupingHierarchyItem parentItem =  (GroupingHierarchyItem)groups.get(parentKey);
		if (parentItem == null && !childrenItems.isEmpty()) {
			parentItem = createGroupingItem(gd, parent, null);
			groups.put(parentKey, parentItem);
		}
		if (!childrenItems.isEmpty()) {
			sortGroupingItems(childrenItems, gd);
			parentItem.getChildren().addAll(0, childrenItems);
		}
	}
	public Set getCheckedCardIds() {
		return checkedCardIds;
	}
	public void setCheckedCardIds(Set checkedCardIds) {
		this.checkedCardIds = checkedCardIds;
	}
	
	protected Card getParentCard(CardHierarchyItem cardItem, CardSetDescriptor descr)
	{
		Card parentCard = null;
		final List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>();
		for (ObjectId attr: descr.getSortOrderByParentAttr()) {
			SearchResult.Column column = new SearchResult.Column();
			column.setAttributeId(attr);
			columns.add( column);
		}
		SearchResult result = null;
		try {
			if(BackLinkAttribute.class.isAssignableFrom(descr.getParentAttrLink().getClass())) {
				final ListProject fetcher = new ListProject();
				fetcher.setAttribute(descr.getParentAttrLink());
				fetcher.setCard(cardItem.getCard().getId());
				fetcher.setColumns(columns);
				result = (SearchResult) serviceBean.doAction(fetcher);
			} else {
				final FetchChildrenCards action = new FetchChildrenCards();
				action.setCardId(cardItem.getCard().getId());
				action.setLinkAttributeId(descr.getParentAttrLink());
				action.setReverseLink(descr.isParentAttrLinkReversed());
				action.setColumns(columns);
				result = (SearchResult)serviceBean.doAction(action);
			}
		} catch (DataException ex) {
			logger.error("Error while searching parent card", ex);
		} catch (ServiceException ex) {
			logger.error("Error while searching parent card", ex);
		}
		if (null != result && result.getCards().size() > 0) {
			parentCard = (Card)result.getCards().get(0);
		}
		return parentCard;
	}
}
