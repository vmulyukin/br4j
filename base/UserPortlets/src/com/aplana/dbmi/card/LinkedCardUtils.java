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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.card.CardPortletSessionBean.PortletMessage.PortletMessageType;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardComparator;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.LinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

/**
 * Utility-�����, ������������ � ���������� ���������, ������� �������������
 * ����������� ���������� ����� �������� � CardLinkAttribute.
 * TODO: � ������� �������� ����� �������� ����������� � ��������� ���� ���
 * � ������� ����������� �����, ��������������� �� AttributeEditor
 * @author DSultanbekov
 */
public class LinkedCardUtils {

	private static Log logger = LogFactory.getLog(LinkedCardUtils.class);

	// ���� ��� �������� ������� � �������� � ����
	// TODO:  ������������� - ��� ��������� ������ ���� � ���� �� AttributeEditor'��
	public static String ATTR_LINK_COLUMNS_LIST = "ATTR_LINK_COLUMNS_LIST";
	public static String ATTR_LINK_CARDS_LIST   = "ATTR_LINK_CARDS_LIST";
	// ����� ��� ��� �������� label-������� �� ���������� ��� ������ �� ��������� ��������
	public static String ATTR_LINK_LABEL_COLUMNS_LIST   = "ATTR_LINK_LABEL_COLUMNS_LIST";
	// ����� ����� ��� �������� � ��������� �������������� �������� ��� ������ �� ��������� ��������, �� ������� �������
	public static String ATTR_LINK_SECONDARY_COLUMNS_MANAGER   = "ATTR_LINK_SECONDARY_COLUMNS_MANAGER";


	/**
	 * ������-������������ �������� id-��, ������ ��� ������������� � sql-��������.
	 * @param cardsOrIds ������ �������� (Card[]) ��� �������� id (ObjectId[]).
	 * @return
	 */
	static String makeIdList(final Collection cardsOrIds)
	{
		if (cardsOrIds == null) return "";

		final StringBuffer buf = new StringBuffer();
		for (Iterator itr = cardsOrIds.iterator(); itr.hasNext(); ) {
			final Object item = itr.next();
			final ObjectId id = (item instanceof Card)
				? ((Card) item).getId()
				: (ObjectId) item;
			if (item == null || "".equals(id.getId())) continue;
			if (buf.length() > 0)
				buf.append(",");
			buf.append(id.getId());
		}
		return buf.toString();
	}

	/**
	 * ��������� �������� ��������� �������� ��� �������� attr, ��� �������� 
	 * ������������� � attr.getIdsLinked
	 * @param request
	 * @param attr �������, ��� �������� ���� ��������� ��������;
	 * (!) ��������� ������������� � attr (id � label's ��������) � � 
	 * sessionBean (������ ��������, ������ �������)
	 */
	public static void reloadLinks(PortletRequest request, CardLinkAttribute attr)
	{
		final CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(request);
		try {
			// beanGetCardLinkCardList(sessionBean, attr.getId());
			// >>> (2010/03, RuSA) ��� "���������" ��� ����, ����� ��� ���������� 
			// ��������, ��� ����� ����� ���� �������� ������� ����������...
			final String ids = attr.getLinkedIds();
			final String idsToLoad = (ids == null || ids.equals("") ? "-1" : ids );
			// <<<
			reloadLinks(request, attr, idsToLoad);

		} catch (Exception e) {
			logger.error("Error adding card links", e);
			if (sessionBean != null)
				sessionBean.setMessageWithType("edit.link.error.create", new Object[] { e.getMessage() }, PortletMessageType.ERROR);
		}
	}


	/**
	 * ��������� �������� ��������� �������� ��� �������� attr ��� id, 
	 * ������������� � ������-������ ids.
	 * @param request
	 * @param attr �������, ��� �������� ���� ��������� ��������;
	 * @param ids ������-������ �� �������� id �������� ����� �������.
	 * (!) ��������� ������������� � attr (id � label's ��������) � � 
	 * sessionBean (������ ��������, ������ �������)
	 */
	private static void reloadLinks(PortletRequest request, CardLinkAttribute attr,
			String ids) 
	{
		final CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(request);
		try {
			if ( (attr == null) || (ids == null) || "".equals(ids)) {
				// ������ ������ ������...
				clearBeanCardLink( sessionBean, attr.getId());
				return;
			}

			// >>> (2010/02, RuSA)
			final SearchResult sr = loadLinkedCardsList( attr, ids, sessionBean.getServiceBean());
			if (sr == null) return;

			Collection foundCards = sr.getCards();
			
			if (!attr.isMultiValued()) {
				// �������� ������ ���� ��������
				if (foundCards.size() > 1) {
					logger.warn("Too many fresh values for attribute " + attr.getId().getId() +
							": " + foundCards.size() + ". Ignoring all except first one");
					// ��������� ����� ����
					ensureSingleItem(foundCards);
				}
			}
			
			if (foundCards.size() > 0) {
				boolean sorted = false;
				ObjectId sortAttrId = null;
				int sortDirection = SearchResult.Column.SORT_NONE;
				for (SearchResult.Column column : (Collection<SearchResult.Column>)sr.getColumns()) {
					if (column.getSorting() != SearchResult.Column.SORT_NONE) {
						sortAttrId = column.getAttributeId();
						sortDirection = column.getSorting();
					}
				}
				
				if (sortAttrId != null) {
					try {
						List orderedCards = new ArrayList(foundCards.size());
						for (Iterator i = foundCards.iterator(); i.hasNext();) {
							orderedCards.add(i.next());
						}
						Collections.sort(orderedCards, new CardComparator(sortAttrId, sortDirection));

						foundCards = orderedCards;
						sr.setCards(orderedCards);
						
						sorted = true;
					}
					catch (Exception ex) {
						logger.warn("Error while sorting SearchResult for attr " + attr.getId() + ": " + ex.getMessage());
					}
				}
				
				if (!sorted) {
					// ��������� ������� ���������� ��������
					List orderedCards = new ArrayList(foundCards.size());
					Map cardsMap = ObjectIdUtils.collectionToObjectIdMap(foundCards);
					Iterator i = attr.getIdsLinked().iterator();
					while (i.hasNext()) {
						ObjectId cardId = (ObjectId)i.next();
						Card card = (Card)cardsMap.get(cardId);
						if (card != null) {
							orderedCards.add(card);
						} else {
							logger.warn("There was a link to non-existant card: " + cardId.getId());
						}
					}
					foundCards = orderedCards;
					sr.setCards(orderedCards);
				}
			}

			// TODO: DSultanbekov, � ����� �� ������ ���-�� ������ � ��������?
			// �� ������, ��� ��� �����
			attr.setIdsLinked( foundCards);
			// attr.setColumns(sr.getColumns());

			// ���������� ������ ��� ����...
			beanSetCardLinksFound(sessionBean, attr.getId(), sr);
			// <<< (2010/02, RuSA)

		} catch (Exception e) {
			logger.error("Error adding card links", e);
			if (sessionBean != null)
				sessionBean.setMessageWithType("edit.link.error.create", new Object[] { e.getMessage() }, PortletMessageType.ERROR);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void reloadLinks(PortletRequest request, PersonAttribute attribute, Collection<SearchResult.Column> columns){
		final CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(request);
		final DataServiceBean serviceBean = sessionBean.getServiceBean();
		CardPortletCardInfo info = sessionBean.getActiveCardInfo();
		SearchResult result = null;
		
		if(attribute != null && attribute.getValues() != null && !attribute.getValues().isEmpty()){
			ArrayList<ObjectId> cardIds = new ArrayList<ObjectId>();
			Collection<Person> persons = attribute.getValues();
			for(Person person : persons) {
				if(person.getCardId() != null) cardIds.add(person.getCardId());
				else logger.warn("Incompletely fetched Person object with id " + person.getId().getId() + " for attribute " + attribute.getId().getId());
			}
			Search search = new Search();
			search.setByCode(true);
			search.setColumns(columns);
			search.setWords(ObjectIdUtils.numericIdsToCommaDelimitedString(cardIds));
			try {
				result = (SearchResult) serviceBean.doAction(search);
			} catch (Exception e){
				logger.error("Exception during fetching persons' cards for attribute "+ attribute.getId().getId() + ": " + e.getMessage());
				e.printStackTrace();
			}
		}
		info.setAttributeEditorData(attribute.getId(), ATTR_LINK_CARDS_LIST, result != null ? result.getCards() : null);
		info.setAttributeEditorData(attribute.getId(), ATTR_LINK_COLUMNS_LIST, result != null ? result.getColumns() : null);
		info.setAttributeEditorData(attribute.getId(), ATTR_LINK_LABEL_COLUMNS_LIST, result != null ? result.getLabelColumnsForCards() : null);
	}

	   public static void reloadLinks(PortletRequest request, CardLinkAttribute attribute, Collection<SearchResult.Column> columns){
	        final CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(request);
	        final DataServiceBean serviceBean = sessionBean.getServiceBean();
	        CardPortletCardInfo info = sessionBean.getActiveCardInfo();
	        SearchResult result = null;
	        final SecondaryColumnsManager scm = new SecondaryColumnsManager();
	        
	        if(attribute != null && attribute.getId() != null && attribute.getId().getId()!=null){
	            Collection cardIds = attribute.getIdsLinked(); 
	            
	            Search search = new Search();
	            search.setByCode(true);
	            search.setColumns(columns);
	            search.setWords(ObjectIdUtils.numericIdsToCommaDelimitedString(cardIds));
	            try {
	                result = (SearchResult) serviceBean.doAction(search);
	                scm.fetchColumns(result);
	            } catch (Exception e){
	                logger.error("Exception during fetching persons' cards for attribute "+ attribute.getId().getId() + ": " + e.getMessage());
	                e.printStackTrace();
	            }
	        }
	        info.setAttributeEditorData(attribute.getId(), ATTR_LINK_CARDS_LIST, result != null ? result.getCards() : null);
	        info.setAttributeEditorData(attribute.getId(), ATTR_LINK_COLUMNS_LIST, result != null ? result.getColumns() : null);
	        info.setAttributeEditorData(attribute.getId(), ATTR_LINK_LABEL_COLUMNS_LIST, result != null ? result.getLabelColumnsForCards() : null);
	        info.setAttributeEditorData(attribute.getId(), LinkedCardUtils.ATTR_LINK_SECONDARY_COLUMNS_MANAGER, scm);
	    }

	/**
	 * @param sessionBean
	 * @param id
	 * @param arrayList
	 */
	private static void clearBeanCardLink(CardPortletSessionBean sessionBean,
			ObjectId id) 
	{
		if (sessionBean != null && id != null) {
			beanSetCardLinkCardList( sessionBean, id, new ArrayList());
			beanSetCardLinkColumns( sessionBean, id, new ArrayList());
			beanSetLabelColumns( sessionBean, id, new HashMap());
		}
	}


	static void ensureSingleItem(Collection col)
	{
		if (col == null || col.size() <= 1) return; 

		// �������� ������ ���� �������...
		final Object first = col.iterator().next();
		col.clear();
		col.add(first);
	}

	static SearchResult loadLinkedCardsList( 
			CardLinkAttribute attr,
			String idList,
			DataServiceBean dataService ) 
		throws UnsupportedEncodingException, DataException, ServiceException
	{
		if (attr == null || idList == null) return null;

		Search search = attr.getFilter();
		if (search != null)
			search = search.makeCopy();
		else {
			search = new Search();
			if (attr.getFilterXml() != null)
				search.initFromXml(new ByteArrayInputStream(attr.getFilterXml().getBytes("UTF-8")));
		}
		
		search.setByAttributes(false);
		search.setByMaterial(false);
		search.setByCode(true); // (!) � runtime, ���� ���� ���� filter, ��� ��� byXXX ����� ���� false. 
		search.setWords( idList);

		// ���������� ������� � ��������� ������� ...
		//ensureLabelAttr(attr, search);

		return (SearchResult) dataService.doAction(search);
	}


	/**
	 * @param attr
	 * @param search
	 */
	public static void ensureLabelAttr(LinkAttribute attr, Search search) {
		if (attr.getLabelAttrId() != null) {
			// ���������� ������� � labelAttrId
			if (search.getColumns() == null)
				search.setColumns(new ArrayList(1));
			final SearchResult.Column col = new SearchResult.Column();
			col.setAttributeId(attr.getLabelAttrId());
			if (!search.getColumns().contains(col))
				search.getColumns().add(col);
		}
	}


	private static Collection<Card> beanGetCardLinkCardList(
			final CardPortletSessionBean sessionBean,
			final ObjectId id)
	{
		if (sessionBean == null || sessionBean.getActiveCardInfo() == null) return null;
		return (Collection) sessionBean.getActiveCardInfo().getAttributeEditorData( id, ATTR_LINK_CARDS_LIST);
	}


	private static void beanSetCardLinkCardList(
			final CardPortletSessionBean sessionBean,
			final ObjectId attrId,
			final Collection<Card> cards)
	{
		if (sessionBean != null) {
			sessionBean.setAttributeEditorData( attrId, ATTR_LINK_CARDS_LIST, cards);
			if (sessionBean.getActiveCardInfo() != null)
				sessionBean.getActiveCardInfo().setAttributeEditorData(attrId, ATTR_LINK_CARDS_LIST, cards);
		}
	}


	private static void beanSetCardLinksFound(
			final CardPortletSessionBean sessionBean,
			final ObjectId attrId,
			final SearchResult sr)
	{
		if (sessionBean != null) {
			sessionBean.setAttributeEditorData( attrId, ATTR_LINK_CARDS_LIST, (sr != null) ? sr.getCards() : null);
			sessionBean.setAttributeEditorData( attrId, ATTR_LINK_COLUMNS_LIST, (sr != null) ? sr.getColumns() : null);
			sessionBean.setAttributeEditorData( attrId, ATTR_LINK_LABEL_COLUMNS_LIST, (sr != null) ? sr.getLabelColumnsForCards() : null);
			final CardPortletCardInfo info = sessionBean.getActiveCardInfo();
			if (info != null) {
				info.setAttributeEditorData(attrId, ATTR_LINK_CARDS_LIST, (sr != null) ? sr.getCards() : null);
				info.setAttributeEditorData( attrId, ATTR_LINK_COLUMNS_LIST, (sr != null) ? sr.getColumns() : null);
				info.setAttributeEditorData( attrId, ATTR_LINK_LABEL_COLUMNS_LIST, (sr != null) ? sr.getLabelColumnsForCards() : null);
				
			}
		}
	}




	static void beanSetCardLinkColumns(
			final CardPortletSessionBean sessionBean, 
			final ObjectId id, 
			final Collection columns) 
	{
		if (sessionBean != null) {
			sessionBean.setAttributeEditorData( id, ATTR_LINK_COLUMNS_LIST, columns);
			if (sessionBean.getActiveCardInfo() != null)
				sessionBean.getActiveCardInfo().setAttributeEditorData(id, ATTR_LINK_COLUMNS_LIST, columns);
		}
	}

	static void beanSetLabelColumns(
			final CardPortletSessionBean sessionBean, 
			final ObjectId id, 
			final Map labels) 
	{
		if (sessionBean != null) {
			sessionBean.setAttributeEditorData( id, ATTR_LINK_LABEL_COLUMNS_LIST, labels);
			if (sessionBean.getActiveCardInfo() != null)
				sessionBean.getActiveCardInfo().setAttributeEditorData(id, ATTR_LINK_LABEL_COLUMNS_LIST, labels);
		}
	}
}
