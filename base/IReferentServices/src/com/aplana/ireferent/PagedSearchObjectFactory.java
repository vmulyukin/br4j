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
package com.aplana.ireferent;

import java.util.Collection;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.ireferent.types.WSOCollection;
import com.aplana.ireferent.util.ServiceUtils;

/**
 * 
 * ������������ ����� ����� �������� ������ ��� ��� ��������,
 * � ������� � ������������ ���� ������, �� ������������ ������������� ��������� (�������� "���������").
 * ��. {@link DoSearchWithDelegatingAccess}
 * 
 * @author PPanichev
 * @version 1.0
 * @since   2014-07-28
 */

public class PagedSearchObjectFactory extends EntityFactory {
	
	private int page;
	private int pageSize;
	private String keywords;
	private int totalCount;
	
	PagedSearchObjectFactory(int page, int pageSize, String keywords) {
		this.page = page;
		this.pageSize = pageSize;
		this.keywords = keywords;
	}

	@Override
	public WSOCollection newWSOCollection(Search search)
		    throws IReferentException {
		Collection<ObjectId> requiredAttributes = getRequiredAttributes();
		logger.info("newWSOCollection(search):searchCards begin:");
		search.setWords(keywords);
    	search.getFilter().setPage(page);
    	search.getFilter().setPageSize(pageSize);
    	Collection<Card> collectionCards =  ServiceUtils.searchCards(serviceBean, search,
				requiredAttributes);
    	totalCount = search.getFilter().getWholeSize();
		logger.info("newWSOCollection(search):searchCards end.");
		logger.info("newWSOCollection(search):create begin:");
		logger.info("newWSOCollection(search):create size = " + collectionCards.size());
		logger.info("newWSOCollection(search):total count = " + totalCount);
		WSOCollection wsCollection = newWSOCollection(collectionCards);
		logger.info("newWSOCollection(search):create end.");
		return wsCollection;
	}

	public int getTotalCount() {
		return totalCount;
	}
}