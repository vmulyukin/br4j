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
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.RenderRequest;

import com.aplana.dbmi.action.BulkFetchChildrenCards;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

/**
 * Base class for Card List Data Providers that provides search functionality
 * It is used as DataProvider at
 * {@link com.aplana.dbmi.gui.ListEditor}
 * 
 * @author skashanski
 * 
 */
public class FilteredCards extends BaseListDataProvider implements SearchableListDataProvider{


	public static final String JSP_SEARCH_PATH = "/WEB-INF/jsp/html/CardSearchForm.jsp";
	public static final String FIELD_WORDS = "words";
	public static final String FIELD_BY_NUMBER = "byId";
	public static final String FIELD_BY_ATTR = "byAttr";

	public static final String FIELD_BY_TEXT = "byText";

	public String getFormJspPath() {
		return JSP_SEARCH_PATH;
	}

	public FilteredCards(DataServiceBean service) 
		throws DataException, ServiceException {
		super(service);
	}	

	public FilteredCards( DataServiceBean service, Search filter, String title) 
			throws DataException, ServiceException {
		super(service, filter, title);
	}


	public void initSearchForm(RenderRequest request) {

		final Search filter = getFilter();

		request.setAttribute(FIELD_WORDS, filter.getWords());

		if (filter.isByCode())
			request.setAttribute(FIELD_BY_NUMBER, "true");

		if (filter.isByAttributes())
			request.setAttribute(FIELD_BY_ATTR, "true");

		if (filter.isByMaterial())
			request.setAttribute(FIELD_BY_TEXT, "true");
	}



	public void processSearch(ActionRequest request) {

		try {

			initSearchParameters(request);

			final SearchResult result = doSearch();

			setCards( result.getCards());

			titleList = result.getName();

		} catch (Exception e) {
			logger.error("Cards search error", e);
			request.setAttribute(ListEditor.ATTR_MESSAGE, e.getMessage());
		}
	}

	protected void initSearchParameters(ActionRequest request) {
		final Search filter = getFilter();
		filter.setWords(request.getParameter(FIELD_WORDS));
		filter.setByCode(request.getParameter(FIELD_BY_NUMBER) != null);
		filter.setByAttributes(request.getParameter(FIELD_BY_ATTR) != null);
		filter.setByMaterial(request.getParameter(FIELD_BY_TEXT) != null);
	}
	
	/**
	 * ��������� ���� �� ������� ��� ��������� ��������.
	 * @param ids : ������ id ��������.
	 * @return ������ � ���� ����� cardId -> ������ �������� �������.
	 * @throws DataException
	 */
	@SuppressWarnings("unchecked")
	protected Map<ObjectId, List<Card>> loadReportsInfo(ObjectId reportAttrId,
			final Collection<ObjectId> ids,
			final ObjectId reportFiles,
			final ObjectId files) 
			throws DataException, ServiceException 
	{
		final BulkFetchChildrenCards bfcc = new BulkFetchChildrenCards();
		bfcc.setReverseLink(false);
		bfcc.setParentCardIds(ids);
		bfcc.setLinkAttributeId(reportAttrId);

		/*
		 * ������: "����� �� ����������"
		 */
		ArrayList<SearchResult.Column> columns = new ArrayList<SearchResult.Column>();
		
		SearchResult.Column col1 = new SearchResult.Column();
		col1.setAttributeId(reportFiles);
		columns.add(col1);

		SearchResult.Column col2 = new SearchResult.Column();
		col2.setAttributeId(files);
		columns.add(col2);

		
		bfcc.setColumns(columns);

		BulkFetchChildrenCards.Result actionResult = service.doAction(bfcc);
		
		return actionResult.getCards();
	}

}