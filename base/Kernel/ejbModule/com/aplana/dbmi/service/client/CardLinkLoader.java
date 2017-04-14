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
package com.aplana.dbmi.service.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.Database;
import com.aplana.dbmi.service.impl.QueryFactory;
import com.aplana.dbmi.service.impl.UserData;
import com.aplana.dbmi.service.impl.query.ExecFetchCards;
import com.aplana.dbmi.utils.SimpleDBUtils;

/**
 * @author RAbdullin
 * ���������� �������� ��������� �������� ��� ��������(��) CardLinkAttribute.
 */
public class CardLinkLoader {

	/**
	 * ��������, ����������� ����������� ��� �������� (���� �� ��� � ������������� 
	 * ������, ��� ��� ����� ����� ���������).
	 */
	final static private ObjectId[] PERSISTENT_IDS = // {"_TEMPLATE", "_STATE", "NAME" } 
		{
			Attribute.ID_NAME,
			new ObjectId( Attribute.class, ExecFetchCards.ATTR_TEMPLATE),
			new ObjectId( Attribute.class, ExecFetchCards.ATTR_STATE)
		};

	/**
	 * ������� ��������� ������ ��� ��������� ��������.
	 * @param cardIdsList ������� ������ ������� id-�������� � ���� "1,23,344";
	 * @param attributes ������ id ����������� ���������.
	 * @return ������� ������ ��� ������ � FetchQuery.
	 */
	public static Search getFetchAction(String cardIdsList, ObjectId[] attributes) {

		final Search search = new Search();

		search.setByAttributes(false);
		search.setByMaterial(false);
		search.setByCode(true);
		search.setWords(cardIdsList); // (!) �������� ������ ���� ��������� ������������ ���

		// ������� �������� ������� ������ ������ �� attributes
		final int len = (attributes == null) ? 3 : attributes.length; 
		final ArrayList<SearchResult.Column> columns =
			new ArrayList<SearchResult.Column>(len);
		final HashSet<ObjectId> added = new HashSet<ObjectId>(len); // ����������� id
		if (attributes != null) {
			for (ObjectId attribute : attributes) {
				addColumn(columns, attribute);
				added.add(attribute);
			}
		}

		// ���������� ������������ ���������...
		for (ObjectId PERSISTENT_ID : PERSISTENT_IDS) {
			if (!added.contains(PERSISTENT_ID))
				addColumn(columns, PERSISTENT_ID);
		}

		search.setColumns(columns);

		return search;
	}

	private static void addColumn(ArrayList<SearchResult.Column> columns, ObjectId attrId) {
		if (columns != null && attrId != null) {
			final SearchResult.Column col = new SearchResult.Column();
			col.setAttributeId(attrId);
			columns.add(col);
		}
	}

	/**
	 * �������� ��������� ������ ��� �������� �������� ref-��������.
	 * @param link �������, ������ �������� �������� �������� ���� ������.
	 * @param attributes ������ �������� ���������.
	 * @return ������� ������ ��� ������ � FetchQuery
	 */
	public static Search getLinkedCardsFetchAction(CardLinkAttribute link,
			ObjectId[] attributes) 
	{
		if (link == null) return null;
		return getFetchAction( link.getLinkedIds(), attributes);
	}

	static public Collection<Card> loadCardsByLink(
			CardLinkAttribute link, 
			UserData user, 
			QueryFactory factory, 
			Database db) 
		throws DataException
	{
		if (link == null || link.getIdsLinked() == null)
			return null;
		final ObjectId[] attributes = (link.getFilter() != null) 
				? makeAttributesByColumns( link.getFilter().getColumns() )
				: null;
		return loadCardsByIds(link.getIdsLinked(), attributes, user, factory, db);
	}
	
	private static ObjectId[] makeAttributesByColumns(Collection columns) {
		if (columns == null || columns.isEmpty()) return null;
		final ObjectId[] result = new ObjectId[ columns.size() ];
		int i = 0;
		for (Iterator iterator = columns.iterator(); iterator.hasNext(); i++) {
			SearchResult.Column col = (SearchResult.Column) iterator.next();
			result[i] = col.getAttributeId();
		}
		return result;
	}

	/**
	 * ��������� �������� ��������� ��������.
	 * @param link ref-�������, �������� �������� ���� ���������.
	 * @param attributes ������ �������� ���������.
	 * @param user ������������, �� ����� �������� ��������� ��������.
	 * @param factory ������� ��������.
	 * @param db
	 * @return ������ �������� ��� null, ���� ��� �� �����.
	 * @throws DataException
	 * Example: 
	 * 	return CardLinkLoader.loadCardsByLink( attr, null, getUser(), getQueryFactory(), getDatabase());
	 */
	static public Collection<Card> loadCardsByLink(
			CardLinkAttribute link, 
			ObjectId[] attributes, 
			UserData user, 
			QueryFactory factory, 
			Database db) 
		throws DataException
	{
		if (link == null || link.getIdsLinked() == null)
			return null;
		return loadCardsByIds(link.getIdsLinked(), attributes, user, factory, db);
	}

	/**
	 * ��������� �������� �������� �� ������ �� id.
	 * ������ loadCardsByLink.
	 * @param ids
	 * @param attributes
	 * @param user
	 * @param factory
	 * @param db
	 * @return
	 * @throws DataException
	 */
	static public Collection<Card> loadCardsByIds(
			Collection/*<ObjectId>*/ ids, 
			ObjectId[] attributes, 
			UserData user, 
			QueryFactory factory, 
			Database db) 
		throws DataException
	{
		final SearchResult result = searchCardsByIds(ids, attributes, user, factory, db);
		return result.getCards();
	}

	/**
	 * ��������� �������� �������� �� �� id.
	 * @param link ref-�������, �������� �������� ���� ���������.
	 * @param attributes ������ �������� ���������.
	 * @param user ������������, �� ����� �������� ��������� ��������.
	 * @param factory ������� ��������.
	 * @param db
	 * @return ������ �������� ��� null, ���� ��� �� �����.
	 * @throws DataException
	 * Example: 
	 * 	return CardLinkLoader.searchCardsByIds( attr, null, getUser(), getQueryFactory(), getDatabase());
	 */
	static public SearchResult searchCardsByIds( 
			Collection/*<ObjectId>*/ ids, 
			ObjectId[] attributes, 
			UserData user, 
			QueryFactory factory, 
			Database db) 
		throws DataException
	{
		final Search fetch = getFetchAction(
				SimpleDBUtils.stringIdentifiersToCommaSeparatedSqlString(ids),
				attributes);
		final ActionQueryBase query = factory.getActionQuery(fetch);
		query.setAction(fetch);
		final SearchResult result = db.executeQuery(user, query);
		return result;
	}
}
