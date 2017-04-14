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
package com.aplana.dbmi.search.workstation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSecurityException;
import javax.portlet.PortletSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

import com.aplana.cms.PagedList;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.SearchResult.Column;
import com.aplana.dbmi.card.util.ARMUtils;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.web.WebSearchBean;
import com.aplana.dbmi.search.SearchFilterPortlet;
import com.aplana.dbmi.search.SearchFilterPortletSessionBean;

/**
 * Represents portlet for handling advanced search at Supervisor/Minister Workstation  
 * @author skashanski
 *
 */
public class WorkstationAdvancedSearchPortlet extends SearchFilterPortlet {
	
	public static final String SORT_ATTR_CODE = "SORT_ATTR_CODE";
	public static final String SORT_ASC = "SORT_ASC";
	public static final String WORKSTATION_ADVANCED_SEARCH_SESSION_BEAN = "WorkstationAdvancedSearchPortletSessionBean";
	private static final String WORKSTATION_SEARCH_JSP = "workstationAdvancedSearch";
	
	public static final String ADVANCED_SEARCH_BEAN = "ADVANCED_SEARCH_BEAN";
	public static final String NEW_SEARCH_ACTION = "NEW_SEARCH_ACTION";
	public static final String KEEP_SEARCH_ALIVE = "keep_search"; // param represents whether we need to keep search object in session
	
	private static Log logger = LogFactory.getLog(WorkstationAdvancedSearchPortlet.class);

	@Override
	protected String getSearchFilterJsp() {
		return WORKSTATION_SEARCH_JSP;
	}
	
	/**
	 * Process an action request.
	 *
	 * @see javax.portlet.Portlet#processAction(javax.portlet.ActionRequest,
	 *      javax.portlet.ActionResponse)
	 */
	@Override
	public void processAction(ActionRequest request, ActionResponse response)
			throws PortletException, PortletSecurityException, IOException {

		// Set user locale
		ContextProvider.getContext().setLocale(request.getLocale());

		String action = request.getParameter(ACTION_FIELD);
		
		if (NEW_SEARCH_ACTION.equals(action)) {
			searchActionHandler(request, response, true);
		} else super.processAction(request, response);


	}

	@Override
	protected SearchFilterPortletSessionBean createSessionBean(
			PortletRequest request) {

		SearchFilterPortletSessionBean returnSessionBean = null;
		// retrieve portletScopeSessionBean
		returnSessionBean = getSessionBean(request);
		if (returnSessionBean == null) {
			// retrieve applicationScopeSessionBean
			returnSessionBean =  getApplicationScopeSessionBean(request);
			if(returnSessionBean == null) {
				returnSessionBean =  super.createSessionBean(request);
				returnSessionBean.setPage(SearchFilterPortletSessionBean.PageState.WORKSTATION_EXTENDED_SEARCH_PAGE);
				returnSessionBean.setSwitchNavigatorLink(ARMUtils.retrieveSwitchNavigatorButton(request, returnSessionBean.getServiceBean()));
				
				setApplicationScopeSessionBean(request, returnSessionBean);
			} else {
				PortletSession session = request.getPortletSession(true);
				session.setAttribute(SESSION_BEAN, returnSessionBean);
				setApplicationScopeSessionBean(request, null);
			}
		}
		returnSessionBean.setHeader(getResourceBundle(request.getLocale()).getString("workstation.search.header"));
		return returnSessionBean;
	}
	
	/**
	 * Stores created Search bean in portlet session for MIShowListPortlet
	 * @param search created Search object
	 */
	protected void storeSearchBean(PortletRequest request, Search search) {

		request.getPortletSession().setAttribute(ADVANCED_SEARCH_BEAN, search,
				PortletSession.APPLICATION_SCOPE);

	}
	
	protected void clearSearchBean(PortletRequest request) {

		request.getPortletSession().setAttribute(ADVANCED_SEARCH_BEAN, null, PortletSession.APPLICATION_SCOPE);

	}
	
	/**
	 * Returns SessionBean data
	 * 
	 * @param request
	 * @return
	 */
	public static SearchFilterPortletSessionBean getApplicationScopeSessionBean(PortletRequest request) {
		PortletSession session = request.getPortletSession();
		if (session == null) {
			logger.warn("Portlet session is not exists yet.");
			return null;
		}
		return (SearchFilterPortletSessionBean) session
				.getAttribute(WORKSTATION_ADVANCED_SEARCH_SESSION_BEAN, PortletSession.APPLICATION_SCOPE);
	}
	
	
	/**
	 * Stores session bean data within APPLICATION_SCOPE.
	 * 
	 * @param sessionBean
	 * @param request
	 */
	protected void setApplicationScopeSessionBean(PortletRequest request, SearchFilterPortletSessionBean sessionBean) {
		PortletSession session = request.getPortletSession();
		if (session == null) {
			logger.warn("Portlet session is not exists yet.");
		}
		session.setAttribute(WORKSTATION_ADVANCED_SEARCH_SESSION_BEAN, sessionBean, PortletSession.APPLICATION_SCOPE);
	}

	@Override
	protected void searchActionHandler(ActionRequest request, ActionResponse response) throws IOException {
		searchActionHandler(request, response, false);
	}

	private void searchActionHandler(ActionRequest request, ActionResponse response, boolean newSearch) throws IOException {
		
		if(newSearch)
			super.searchActionHandler(request);
		
		//redirect to definite page
		String searchResultPathPage = portletService.getPageProperty("searchResultPathPage", request, response);
		String searchWindow = portletService.getPageProperty("searchWindow", request, response);
		String extendedSearchForm = portletService.getPageProperty("extendedSearchForm", request, response);
		String area = portletService.getPageProperty("area", request, response);
		
		String pageStr = request.getParameter(PagedList.PARAM_PAGE);
		if(!StringUtils.hasLength(pageStr)) {
			pageStr = "1";
		}
		Map params = new HashMap();
		params.put("area", area);
		// if extendedSearchForm parameter exist, portlet clears view
		//params.put("extendedSearchForm", extendedSearchForm);
		params.put("page", pageStr);
		params.put(KEEP_SEARCH_ALIVE , true);
		
		prepareSearch(request, pageStr);
		
		String redirectUrl = portletService.generateLink(searchResultPathPage, searchWindow, params, request, response);
		response.sendRedirect(redirectUrl);
		return;	
	}
	
	public static void prepareSearch(ActionRequest request, String pageStr) {
		final String sortAttrCode = request.getParameter(SORT_ATTR_CODE);
		final String sortAsc = request.getParameter(SORT_ASC);

		Search search = (Search) request.getPortletSession().getAttribute(ADVANCED_SEARCH_BEAN, PortletSession.APPLICATION_SCOPE);
		if (search != null && search.getColumns() != null && search.getFilter() != null) {
			// prepare sorting
			if (StringUtils.hasLength(sortAttrCode)) {
				search.getFilter().getOrderedColumns().clear();
				int j = 2;
				for (Iterator<SearchResult.Column> i = search.getColumns().iterator(); i.hasNext(); j++) {
					final SearchResult.Column col = i.next();
					if (sortAttrCode.equals(col.getAttributeId().getId())) {
						Column orderCol = col.copy();
						boolean isAsc = true;
						if (StringUtils.hasLength(sortAsc)) {
							isAsc = Boolean.parseBoolean(sortAsc);
						}
						orderCol.setSorting(isAsc ? SearchResult.Column.SORT_ASCENDING : SearchResult.Column.SORT_DESCENGING);
						search.getFilter().addOrderColumn(orderCol, j);
					}
				}
				search.getFilter().sortOrderColumns();
			}
			
			// prepare paging
			try {
				Integer page = Integer.valueOf(pageStr);
				search.getFilter().setPageSize(20);
				search.getFilter().setPage(page.intValue());
			} catch (NumberFormatException e) {
				logger.warn("Exception thrown during preparing advanced search item: " + e);
			}
		}

	}

	@Override
	protected void clearActionHandler(ActionRequest request,
			ActionResponse response) throws IOException {
		super.clearActionHandler(request, response);
		//��� ������� ��������� ����� � ���� �� ��������� ����������� ���� �������
		request.getPortletSession().removeAttribute(CLEAR_ATTR, PortletSession.APPLICATION_SCOPE);
	}
	
	
}