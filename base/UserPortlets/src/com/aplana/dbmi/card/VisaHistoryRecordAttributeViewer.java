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
import java.util.Iterator;
import java.util.List;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;

public class VisaHistoryRecordAttributeViewer extends
		CardHistoryRecordAttributeViewer {

	static final ObjectId enclosedSetId = ObjectId.predefined(CardLinkAttribute.class, "jbr.visa.enclosedSet"); 
	static final ObjectId orderId = ObjectId.predefined(IntegerAttribute.class, "jbr.visa.order"); 
	
	@Override
	protected List/*Card*/ getChildrenCards(Card parentCard, ObjectId linkId, Collection/*<ObjectId>*/ attrIds, ObjectId recordId) throws DataException, ServiceException {
		Search search = new Search();
		search.setByCode(true);
		
		CardLinkAttribute link = (CardLinkAttribute) parentCard.getAttributeById(linkId);
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

		SearchResult.Column col = new SearchResult.Column();
		col.setAttributeId(enclosedSetId);
		columns.add(col);
		
		search.setColumns(columns);
		SearchResult searchResult = (SearchResult) getServiceBean().doAction(search);
		
		ArrayList result = new ArrayList();
		List rootVisaCards = searchResult.getCards();
		for (int i = 0 ; i < rootVisaCards.size(); i++) {
			Card rootVisaCard = (Card) rootVisaCards.get(i);
			CardLinkAttribute enclosedSetAttribute = (CardLinkAttribute) rootVisaCard.getAttributeById(enclosedSetId);
			int rootOrder = ((IntegerAttribute) rootVisaCard.getAttributeById(orderId)).getValue();
			if (enclosedSetAttribute != null) {
				result.addAll(getEnclosedSetCards(enclosedSetAttribute.getLinkedIds(), rootOrder, attrIds, recordId));
			}
			result.add(rootVisaCard);
		}
		return result;
	}
	
	private List getEnclosedSetCards(String linkedIds, int rootOrder, Collection/*<ObjectId>*/ attrIds, ObjectId recordId) throws DataException, ServiceException {
		Search search = new Search();
		search.setByCode(true);
		
		search.setWords(linkedIds);
		
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

		SearchResult.Column col = new SearchResult.Column();
		col.setAttributeId(enclosedSetId);
		columns.add(col);

		search.setColumns(columns);
		SearchResult searchResult = (SearchResult) getServiceBean().doAction(search);
		ArrayList result = new ArrayList();
		List visaCards = searchResult.getCards();
		for (int i = 0 ; i < visaCards.size(); i++) {
			Card visaCard = (Card) visaCards.get(i);
			CardLinkAttribute enclosedSetAttribute = (CardLinkAttribute) visaCard.getAttributeById(enclosedSetId);
			
			((IntegerAttribute) visaCard.getAttributeById(orderId)).setValue(rootOrder);
			
			if (enclosedSetAttribute != null) {
				result.addAll(getEnclosedSetCards(enclosedSetAttribute.getLinkedIds(), rootOrder, attrIds, recordId));
			}
			result.add(visaCard);
		}
		return result;
	}
}
