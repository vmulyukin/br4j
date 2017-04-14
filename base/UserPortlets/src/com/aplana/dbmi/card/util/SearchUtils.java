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
package com.aplana.dbmi.card.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

public class SearchUtils {
	
	final static Log logger = LogFactory.getLog(SearchUtils.class);

	public static List<SearchResult.Column> initializeColumns(
			List<SearchResult.Column> columns, DataServiceBean serviceBean) 
		throws DataException, ServiceException 
	{
		final Search search = new Search();
		search.setColumns(columns);
		search.setByCode(true);
		search.setWords("-1");	// non-existant card_id to initialize column headers
		final SearchResult r = (SearchResult)serviceBean.doAction(search);
		final List<SearchResult.Column> dblist = (List<SearchResult.Column>)r.getColumns();
		if (columns != null && !columns.isEmpty()) {
			for( SearchResult.Column fileCol: columns) {
				for( SearchResult.Column dbCol: dblist ) {
					if (dbCol.getAttributeId().getId().equals(fileCol.getAttributeId().getId())) {
						dbCol.setAction(fileCol.getAction());
						dbCol.setLinked(fileCol.isLinked());
					}
				}
			}
		}
		return dblist;
	}

	public static List<SearchResult.Column> getNotReplaceColumns(
			List<SearchResult.Column> columns) 
		throws DataException, ServiceException 
	{
		final List<SearchResult.Column> notReplaceColumns = new ArrayList<SearchResult.Column>(columns);
		for( SearchResult.Column col: columns) {
			if (col.isReplaceAttribute())	// (YNikitin, 2012/08/07) ���������� �������� ������ �������������� ��� �����������
				notReplaceColumns.remove(col);
		}
		return notReplaceColumns;
	}

	public static JSONArray getColumnsJSON(Collection<SearchResult.Column> columns) 
			throws JSONException 
	{
		final JSONArray result = new JSONArray();
		for( SearchResult.Column c : columns ) {
			JSONObject jc = new JSONObject();
			jc.put("width", c.getWidth() + "px");
			jc.put("title", c.getName());
			// ��� �������, � ������� ��������� ���� LabelAttrId, ��������� ��� ������� � ������ FullLabelAttrId  
			if (c.getLabelAttrId()!=null){
				jc.put("attrId", c.getAttributeId().getId().toString()+Attribute.LABEL_ATTR_PARTS_SEPARATOR+c.getFullLabelAttrId().toString());
			} else
				jc.put("attrId", c.getAttributeId().getId());
			if (c.isLinked()) {
				jc.put("linkAction", "open");
			} else if ("download".equals(c.getAction()) || c.isDownloadMaterial()) {
				jc.put("linkAction", "download");
			}
			result.put(jc);
		}
		return result;
	}

	public static String getWordsByCodes(Collection<Card> cards) {
		final StringBuffer w = new StringBuffer();
		final Iterator<Card> i = cards.iterator();
		while (i.hasNext()) {
			final Card c = i.next();
			w.append(c.getId().getId());
			if (i.hasNext())
				w.append(',');
		}
		return w.toString();
	}

	/**
	 * ���������� ��� �������� ������� ��� ���������.
	 * @param attrId �������
	 * @return ������� � ���� ���������.
	 */
	public static SearchResult.Column createColumn(ObjectId attrId) 
	{
		final SearchResult.Column col = new SearchResult.Column();
		col.setAttributeId(attrId);
		return col;
	}

	/**
	 * �������� ������ ������� {@link SearchResult.Column}[] ��� ���������� 
	 * ������ ���������.
	 * @param attrIds id ���������, ��� ������� ��������� �������� ��������� �������.
	 * @return ����� ������ �������.
	 */
	public static List<SearchResult.Column> createColumns(ObjectId... attrIds)
	{
		return (attrIds == null)
					? null
					: addColumns( new ArrayList<SearchResult.Column>(attrIds.length), attrIds);
	}

	/**
	 * �������� ��������� ������� ��������� ��������� � ������ �������. 
	 * @param dest ������� ������, � ������� ��������.
	 * @param attrIds id ���������, ��� ������� ��������� �������� ��������� �������.
	 * @return dest.
	 */
	public static List<SearchResult.Column> addColumns(List<SearchResult.Column> dest, ObjectId... attrIds)
	{
		if (dest != null && attrIds != null) {
			for (ObjectId id : attrIds) {
				if (id != null)
					dest.add(createColumn(id));
			}
		}
		return dest;
	}

	/**
	 * �������� ������ �������� �� ���������� ����������.
	 * @param sr
	 * @return �������� ������ �������� ��� null.
	 */
	public static List<Card> getCardsList( final SearchResult sr)
	{
		if (sr == null)
			return null;
		final List<Card> list = sr.getCards();
		return (list == null || list.isEmpty()) ? null : list;
	}


	/**
	 * �������� �������� �� ���������� �������.
	 * @Example: final List<Card> list = CardUtils.execSearchCards(search, getQueryFactory(), getDatabase(), getUser());
	 * @return �������� ������ �������� ��� null.
	 * @throws ServiceException
	 * @throws DataException 
	 */
	public static List<Card> execSearchCards(
			final Action search,
			DataServiceBean serviceBean
		) throws ServiceException, DataException 
	{
		if (search == null || serviceBean == null)
			return null;
		// ����������
		final SearchResult result = (SearchResult) serviceBean.doAction(search);
		return getCardsList(result);
	}

	/**
	 * �������� �������������� ������ ������ �� ��������.
	 * @param attr
	 * @return �������� ������ ��� null.
	 */
	@SuppressWarnings("unchecked")
	public static Collection<Person> getAttrPersons( PersonAttribute attr)  
	{
		if (attr == null || attr.getValues() == null || attr.getValues().isEmpty())
			return null;
		return attr.getValues();
	}

	/**
	 * �������� �������������� ������ ������ �� �������� ��������.
	 * @param card
	 * @param attrPerson: id Person-��������.
	 * @param throwIfNoAttr: ������������ ���� �������� ��� � ��������, 
	 * true=������� ���������� DataException, false=������� null.
	 * @return �������� ������ ������ ��� null, ���� ��� ������ �������� ��� ������ ������.
	 * @throws DataException 
	 */
	public static Collection<Person> getAttrPersons( Card card, 
			ObjectId attrPerson, boolean throwIfNoAttr
		) throws DataException
	{
		if (card == null || attrPerson == null)
			return null;

		final PersonAttribute pa = (PersonAttribute) card.getAttributeById(attrPerson);
		if (pa == null) {
			if (throwIfNoAttr)
				throw new DataException( "docflow.document.noattr", 
						new Object[] { "cardId " + card.getId(), attrPerson} );
			return null;
		}
		return getAttrPersons(pa);
	}

	public static Collection<Person> getAttrPersons( Card card, 
			ObjectId attrPerson
		) throws DataException {
		return getAttrPersons(card, attrPerson, false);
	}

	/**
	 * �������� �������������� ������ id �� cardlink-�������� ��������.
	 * @param card
	 * @param attrCardLink: id cardlink(typedlink)-��������.
	 * @param throwIfNoAttr: ������������ ���� �������� ��� � ��������, 
	 * true=������� ���������� DataException, false=������� null.
	 * @return �������� ������ ������ ��� null, ���� ��� ������ �������� ��� ������ ������.
	 * @throws DataException 
	 */
	public static Collection<ObjectId> getAttrLinks( Card card, 
			ObjectId attrCardLink, boolean throwIfNoAttr
		) throws DataException
	{
		if (card == null || attrCardLink == null)
			return null;

		final CardLinkAttribute pa = card.getCardLinkAttributeById(attrCardLink);
		if (pa == null)
		{
			if (throwIfNoAttr)
				throw new DataException( "jbr.processor.nodestattr_2", 
						new Object[] { "cardId " + card.getId(), attrCardLink} );
			return null;
		}
		return getAttrLinks( pa);
	}

	public static Collection<ObjectId> getAttrLinks( Card card, 
			ObjectId attrCardLink) throws DataException
	{
		return getAttrLinks(card, attrCardLink, false);
	}

	public static Collection<ObjectId> getAttrLinks( CardLinkAttribute attr
		)
	{
		if (attr == null || attr.getIdsLinked() == null || attr.getIdsLinked().isEmpty())
			return null;
		return attr.getIdsLinked();
	}

}
