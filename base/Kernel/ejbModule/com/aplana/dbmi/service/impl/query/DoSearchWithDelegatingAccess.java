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
package com.aplana.dbmi.service.impl.query;

import com.aplana.dbmi.action.CheckDelegatingReadAccess;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.SearchWithDelegatingAccess;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

import java.util.List;

/**
 * Query used to perform {@link com.aplana.dbmi.action.SearchWithDelegatingAccess} action.
 * <br>
 * ����� �������� ��������� search ������ ������������ ���������� �������� ���� ��� ������.
 * ����� ���������� �������� ����������� � ������ ���� ����, � ��� ����� ������������.
 * <br>
 * TODO ������ ��� ������ �� ������������ ������������ �����.
 * ����� ���������� ������� ������ ����� ���� ����������� ������ �� ��������� 
 * � ������� ������� �� ���� �������������� �� ������������ ������.
 * 
 * @author valexandrov
 * @version 1.0
 * @since   2014-06-19
 */
public class DoSearchWithDelegatingAccess extends DoSearch {

	/**
	 * Performs given {@link Search} action
	 *
	 * @return {@link SearchResult} object representing result of {@link Search}
	 *         action
	 */
	public SearchResult processQuery() throws DataException {
		final SearchWithDelegatingAccess searchWithDelegatingAccess = getAction();
		Search search = searchWithDelegatingAccess.getSearch();
		final Search.Filter filter = search.getFilter();
		if (filter!= null) {
			// ��������� �������� ���� ��� ������
			filter.setCurrentUserRestrict(Search.Filter.CU_DONT_CHECK_PERMISSIONS);
		}
		setAction(search);
		SearchResult searchResult = super.processQuery();

		// ��������� ��� �� �������������� �������� ���� ����� �������, ������� ������������.
		List<Card> listCheckCards = searchResult.getCards();
		
		CheckDelegatingReadAccess chkDelegatingAccessAction = new CheckDelegatingReadAccess();
		chkDelegatingAccessAction.setCheckCards(listCheckCards);
		ActionQueryBase queryChkDelegatingAccess = getQueryFactory().getActionQuery(CheckDelegatingReadAccess.class);
		queryChkDelegatingAccess.setAction(chkDelegatingAccessAction);
		List<Card> listVerifiedCards = getDatabase().executeQuery(getSystemUser(), queryChkDelegatingAccess);
		searchResult.setCards(listVerifiedCards);

		return searchResult;
	}
}