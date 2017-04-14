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
package com.aplana.dbmi.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

/**
 * Base class for Card List Data Providers It is used as DataProvider at
 * {@link com.aplana.dbmi.gui.ListEditor}
 * 
 * @author skashanski
 * 
 */
public class BaseListDataProvider implements ListDataProvider {

	protected Log logger = LogFactory.getLog(getClass());

	protected DataServiceBean service;

	private Search filter = new Search();
	private List<Card> cards;

	/** contains all possible cards including selected cards*/
	final private Map<ObjectId, Card> storedCards = new HashMap<ObjectId, Card>();

	// (2010/02, RuSA) � ���� ��� ��� id-����� (ObjectId), �� �� DataObject
	final private List<ObjectId> selected = new ArrayList<ObjectId>();
	/** flag to indicate if we need to add ID column at ListForm */
	// TODO: (2011/04/05, RuSA) ���� ������ ���, � ��������� ����������� - ����������� ��������������� ��� �� AttributeEditors.
	private boolean addIdColumn = false;

	final private Map<String, ObjectId> colIds = new HashMap<String, ObjectId>();
	final private Map<String, String> colTitles = new HashMap<String, String>();
	final private List<String> columns = new ArrayList<String>();
	final private Map<String, Integer> colWidths = new HashMap<String, Integer>();
	final private Map<String, Boolean> colLinked = new HashMap<String, Boolean>();

	protected String titleList;
	private String titleSel;

	/** constants */
	private static final String ID_ID = "_id";


	public BaseListDataProvider(DataServiceBean service) 
		throws DataException, ServiceException 
	{
		initializeDataService(service);
	}

	public BaseListDataProvider(DataServiceBean service, Search filter,
			String title) throws DataException, ServiceException {

		initializeDataService(service);

		initializeFilter(filter);

		SearchResult result = doSearch();

		initializeDisplayParameters(result, title);

		cards = result.getCards();

		refreshStoredCards();
	}

	/**
	 * @param list
	 */
	private void refreshStoredCards() {
		if (cards != null)
			for (Card card : cards) {
				if (card != null && card.getId() != null)
					this.storedCards.put( card.getId(), card);
			}
	}

	protected void initializeDataService(DataServiceBean aservice)
			throws DataException, ServiceException
	{
		this.service = aservice;
	}

	protected SearchResult doSearch()
			throws DataException, ServiceException {
		
		return (SearchResult) service.doAction(this.filter);

	}

	public boolean isAddIdColumn() {
		return addIdColumn;
	}

	public void setAddIdColumn(boolean addIdColumn) {
		if (this.addIdColumn == addIdColumn)
			return;
		this.addIdColumn = addIdColumn;
		if (this.addIdColumn)
			addIDColumn();
		else
			remIDColumn();
	}

	protected void initializeDisplayParameters(SearchResult result, String title) {

		this.titleSel = title;

		if (addIdColumn)
			addIDColumn();

		for( SearchResult.Column column : (Collection<SearchResult.Column>) result.getColumns() ) 
		{
			final String id = column.getAttributeId().getId().toString();
			columns.add(id);
			if (isLinked(column))
				colLinked.put(id, Boolean.TRUE);

			colTitles.put(id, column.getName());
			colIds.put(id, column.getAttributeId());
			colWidths.put(id, new Integer(column.getWidth()));
		}

		titleList = result.getName();
	}


	/**
	 * Returns true if given column is linked
	 */
	protected boolean isLinked(SearchResult.Column column) {
		return column.isLinked();
	}


	/**
	 * Adds ID column (if it is not present yet) 
	 */
	protected void addIDColumn() {
		if (columns.indexOf(ID_ID) == -1) {
			columns.add(ID_ID);
			colTitles.put(ID_ID, ContextProvider.getContext().getLocaleMessage( "search.column.id"));
			colWidths.put(ID_ID, new Integer(5));
		}
	}

	/**
	 * Rem ID column (if it is present only) 
	 */
	protected void remIDColumn() {
		final int i = columns.indexOf(ID_ID);
		if (i >= 0) {
			columns.remove(i);
			colTitles.remove(ID_ID);
			colWidths.remove(ID_ID);
		}
	}

	/**
	 * 
	 * @param theFilter
	 * @throws DataException
	 */
	protected void initializeFilter(Search theFilter) 
		throws DataException 
	{
		if (theFilter == null) {
			theFilter = new Search();
			theFilter.setWords("");
			theFilter.setByAttributes(true);
			theFilter.setTemplates(new ArrayList());
			theFilter.getFilter().setPageSize(1024);
			logger.error("Undefined Search = null");
		}

		setFilter( theFilter);
	}

	public String getColumnTitle(String column) {
		return colTitles.get(column);
	}

	public List<String> getColumns() {
		return this.columns;
	}

	public List<Card> getListData() {
		return cards;
	}

	public String getListTitle() {
		return titleList;
	}

	public List<ObjectId> getSelectedListData() {
		return selected;
	}

	public String getSelectedListTitle() {
		return titleSel;
	}

	public String getValue(ObjectId item, String column) {

		if (item == null)
			return "";

		if (ID_ID.equals(column))
			// (2010/02) OLD: return ((Card) item).getId().getId().toString();
			return item.getId().toString();

		final Card card = findCardById(item);
		if (card == null)
			return "";

		final Attribute attr = card.getAttributeById( colIds.get(column));
		if (attr == null)
			return "";

		return attr.getStringValue();
	}

	private Card findCardById(ObjectId cardId) {
		/*
		if (cardId != null && this.storedCards != null) {
			for (Iterator iterator = this.storedCards.iterator(); iterator.hasNext();) {
				final Card card = (Card) iterator.next();
				if (cardId.equals(card.getId()))
					return card; // FOUND
			}
		}
		return null; // NOT FOUND
		 */
		if (this.storedCards == null)
			return null;
		if (this.storedCards.size() == 0) {
			// ������� �������������� storedCards ...
			if (this.cards != null && this.cards.size() > 0)
				refreshStoredCards();
		}
		return (this.storedCards != null) 
					? this.storedCards.get(cardId)
					: null;
	}

	public void setSelectedList(List/*<ObjectId>*/ data) {
		this.selected.clear();
		if (data != null)
			this.selected.addAll(data);
	}

	public int getColumnWidth(String column) {
		return ((Number) colWidths.get(column)).intValue();
	}

	public boolean isColumnLinked(String column) {
		return colLinked.containsKey(column);
	}	

	protected List<Card> getCards() {
		return this.cards;
	}

	protected void setCards(List<Card> list) {
		this.cards = list;
		refreshStoredCards();
	}

	protected Search getFilter() {
		return this.filter;
	}

	protected void setFilter(Search newFilter) {
		if (this.filter != newFilter) {
			this.filter = newFilter;
		}
	}
}
