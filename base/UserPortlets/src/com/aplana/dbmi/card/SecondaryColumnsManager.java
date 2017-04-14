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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;

/**
 * ����� ��� ���������� ��������������� ��������� ��� ���������� ������� ��������
 * @author ppolushkin
 * @since 08.08.2014
 *
 */
public class SecondaryColumnsManager {
	
	public SecondaryColumnsManager() {}
	
	// ������� ��� �������� �������������� �������� � ������� <��������, <�������, ��������>>
	private Map<ObjectId, Map<SearchResult.Column, Attribute>> secondaryColumns = new HashMap<ObjectId, Map<SearchResult.Column, Attribute>>();

	public Map<ObjectId, Map<SearchResult.Column, Attribute>> getSecondaryColumns() {
		return secondaryColumns;
	}

	public void setSecondaryColumns(
			Map<ObjectId, Map<SearchResult.Column, Attribute>> secondaryColumns) {
		this.secondaryColumns = secondaryColumns;
	}
	
	
	/**
	 * �������� ������� ������� �� ��������������, ���� ����� ������ ������ ������� �� ��������������
	 * @param sr ��������� ������
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void fetchColumns(SearchResult sr) {
		if(sr.getColumns() == null)
			return;
		Collection<SearchResult.Column> newCols = new ArrayList<SearchResult.Column>();
		for(Iterator it = sr.getColumns().iterator(); it.hasNext();) {
			SearchResult.Column col = (SearchResult.Column) it.next();
			if(col.getPrimaryColumn() != null) {
				newCols.add(col);
			}
		}
		sr.getColumns().removeAll(newCols);
		if(sr.getCards() == null)
			return;
		for(Iterator it = sr.getCards().iterator(); it.hasNext();) {
			Card card = (Card) it.next();
			for(Iterator it2 = sr.getColumns().iterator(); it2.hasNext();) {
				SearchResult.Column col = (SearchResult.Column) it2.next();
				if(col.getSecondaryColumns() == null)
					continue;
				Attribute attrLink = null;
				switch(col.getColumnCond()) {
				case STATE:
					attrLink = checkSecondaryStates(col, card);
					if(attrLink != null) {
						addSecondaryColumnData(card, col, attrLink);
					}
					break;
				case TEMPLATE:
					attrLink = checkSecondaryTemplates(col, card);
					if(attrLink != null) {
						addSecondaryColumnData(card, col, attrLink);
					}
					break;
				default:
					attrLink = getCurrentAttr(col, card);
					if(attrLink == null) {
						attrLink = checkSecondaryNulls(col, card);
					}
					if(attrLink != null) {
						addSecondaryColumnData(card, col, attrLink);
					}
					break;
				}
				
			}
		}	
		
	}
	
	/**
	 * ��������� ������ � ���, ������ ��� ����� ������� � �������� �������
	 * @param secondaryColumns ��� ��� �������� �������������� ��������
	 * @param card ������� ��������
	 * @param col ������� �������
	 * @param attr ������� ������ �� �������� ���������� ���������
	 */
	private void addSecondaryColumnData(Card card, SearchResult.Column col, Attribute attr) {
		Map<SearchResult.Column, Attribute> cardMap = secondaryColumns.get(card.getId());
		if(cardMap == null) {
			cardMap = new HashMap<SearchResult.Column, Attribute>();
			secondaryColumns.put(card.getId(), cardMap);
		}
		cardMap.put(col, attr);
	}
	
	/**
	 * ��������� ����� �� ��������� � ��� ������ ��� �������� ������� �� ��������������� �������� (�������� �� ������� ������� ��������)
	 * @param col ������� ������� 
	 * @param card ������� ��������
	 * @return ������� � �������������� ���������
	 */
	private Attribute checkSecondaryStates(SearchResult.Column col, Card card) {
		if(col.getSecondaryColumns() == null)
			return null;
		for(Iterator<SearchResult.Column> it = col.getSecondaryColumns().iterator(); it.hasNext();) {
			SearchResult.Column newCol = it.next();
			if(newCol.getCondValues() != null && newCol.getCondValues().contains(card.getState())) {
				return getCurrentAttr(newCol, card);
			}
		}
		return null;
	}
	
	/**
	 * ��������� ����� �� ��������� � ��� ������ ��� �������� ������� �� ��������������� �������� (�������� �� ������� ������� ��������)
	 * @param col ������� ������� 
	 * @param card ������� ��������
	 * @return ������� � �������������� ���������
	 */
	private Attribute checkSecondaryTemplates(SearchResult.Column col, Card card) {
		if(col.getSecondaryColumns() == null)
			return null;
		for(Iterator<SearchResult.Column> it = col.getSecondaryColumns().iterator(); it.hasNext();) {
			SearchResult.Column newCol = it.next();
			if(newCol.getCondValues() != null && newCol.getCondValues().contains(card.getTemplate())) {
				return getCurrentAttr(newCol, card);
			}
		}
		return null;
	}
	
	/**
	 * ��������� ����� �� ��������� � ��� ������ ��� �������� ������� �� ��������������� �������� (�������� �� null �������� ��������)
	 * @param col ������� ������� 
	 * @param card ������� ��������
	 * @return ������� � �������������� ���������
	 */
	private Attribute checkSecondaryNulls(SearchResult.Column col, Card card) {
		if(col.getSecondaryColumns() == null)
			return null;
		Attribute attrLink = null;
		for(Iterator<SearchResult.Column> it = col.getSecondaryColumns().iterator(); it.hasNext();) {
			SearchResult.Column newCol = it.next();
			attrLink = getCurrentAttr(newCol, card);
			if(attrLink != null) {
				return attrLink;
			}
		}
		return null;
	}
	
	/**
	 * �������� ������� �� �������� ��� �������� �������
	 * @param col ������� �������
	 * @param card ������� ��������
	 * @return
	 */
	private Attribute getCurrentAttr(SearchResult.Column col, Card card) {
		Attribute attrLink = null;
		if(card != null && col.getAttributeId() != null) {
			attrLink = card.getAttributeById(col.getAttributeId());
		}
		return attrLink;
	}
	
	/**
	 * �������� ����� �� ������������ �������������� �������� ������ ��������
	 * @param col ������� �������
	 * @param card ������� ��������
	 * @return �������������� ������� �� ��������� ���� null
	 */
	public Attribute getSecondaryColumnValueIfExists(SearchResult.Column col, Card card) {
		 if(secondaryColumns.get(card.getId()) != null && (secondaryColumns.get(card.getId())).get(col) != null)
		 	return (secondaryColumns.get(card.getId())).get(col);
		 return null;
	}

}
