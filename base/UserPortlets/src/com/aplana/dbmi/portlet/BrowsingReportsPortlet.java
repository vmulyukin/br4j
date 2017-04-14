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
package com.aplana.dbmi.portlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSecurityException;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.card.util.ARMUtils;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.service.*;

public class BrowsingReportsPortlet extends GenericPortlet {
	private static final String JSP_FOLDER = "/WEB-INF/jsp/";
	private static final String JSP_PAGE = "BrowsingReports.jsp";
	public static final String SESSION_BEAN = "browsingReportsPortletSessionBean";
	private static final String APPLICATION_SESSION_BEAN_PREFIX = "browsingReportsPortletSessionBean:";
	
	static final String INIT_PARAM_REPORTS_FILE = "configReports";
	
	public static final ObjectId TEMPLATE_PERSON = ObjectId.predefined(
			Template.class, "jbr.internalPerson");
	public static final ObjectId CARDSTATE_ACTIVE_USER = ObjectId.predefined(
			CardState.class, "user.active");
	
	public static final ObjectId ATTR_LAST_NAME = ObjectId.predefined(
			StringAttribute.class, "jbr.person.lastName");
	public static final ObjectId ATTR_FIRST_NAME = ObjectId.predefined(
			StringAttribute.class, "jbr.person.firstName");
	public static final ObjectId ATTR_MIDDLE_NAME = ObjectId.predefined(
			StringAttribute.class, "jbr.person.middleName");
    public static final ObjectId ORGANIZATION_ID = ObjectId.predefined(
            CardLinkAttribute.class, "jbr.person.organization");

	@Override
	protected void doView(RenderRequest request, RenderResponse response)
			throws PortletException, PortletSecurityException, IOException {
		// Set user locale
		ContextProvider.getContext().setLocale(request.getLocale());
		
		String confFile =
                Portal.getFactory().getPortletService().getPageProperty(INIT_PARAM_REPORTS_FILE, request, response);

		BrowsingReportsPortletSessionBean sessionBean = getSessionBean(request, confFile);
		String key = getApplicationSessionBeanKey(response.getNamespace());
		PortletSession session = request.getPortletSession();
		session.setAttribute(key, sessionBean, PortletSession.APPLICATION_SCOPE);
		
		response.setContentType("text/html");
		PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher(
				JSP_FOLDER + JSP_PAGE);
		rd.include(request, response);
	}
	
	@Override
	public void processAction(ActionRequest request, ActionResponse response)
			throws PortletException, PortletSecurityException, IOException {
		// TODO Auto-generated method stub
		super.processAction(request, response);
	}
	
	public static BrowsingReportsPortletSessionBean getSessionBean(
			HttpServletRequest request, String namespace) {
		HttpSession session = request.getSession();
		String key = getApplicationSessionBeanKey(namespace);
		return (BrowsingReportsPortletSessionBean) session.getAttribute(key);
	}
	
	private static String getApplicationSessionBeanKey(String namespace) {
		return APPLICATION_SESSION_BEAN_PREFIX + namespace;
	}
	
	private BrowsingReportsPortletSessionBean getSessionBean(
			PortletRequest request, String reportsFile) {
		PortletSession session = request.getPortletSession();
		BrowsingReportsPortletSessionBean result = (BrowsingReportsPortletSessionBean) session
				.getAttribute(SESSION_BEAN);
		if (result == null) {
			result = createSessionBean(request);
			session.setAttribute(SESSION_BEAN, result);
		}
		/* TO-DO �������� ����� ��������� � createSessionBean,
		 * ����� ������ ���������, ��� ��� ������������ ��������� �������� �� ���������� ���� sessionBean 
		 */
		updateConfig(request, result, reportsFile);
		return result;
	}
	
	private BrowsingReportsPortletSessionBean createSessionBean(PortletRequest request) {
		BrowsingReportsPortletSessionBean sessionBean = new BrowsingReportsPortletSessionBean();

        DataServiceBean serviceBean = PortletUtil.createService(request);
        String userName = (String) request.getPortletSession().getAttribute(DataServiceBean.USER_NAME, PortletSession.APPLICATION_SCOPE);
        if (userName != null) {
            serviceBean.setUser(new UserPrincipal(userName));
            serviceBean.setIsDelegation(true);
            serviceBean.setRealUser(request.getUserPrincipal());
        } else {
            serviceBean.setUser(request.getUserPrincipal());
            serviceBean.setIsDelegation(false);
        }

        sessionBean.setServiceBean(serviceBean);
		sessionBean.setEmployeesSearch(getEmployeesSearch());
        sessionBean.setSwitchNavigatorLink(ARMUtils.retrieveSwitchNavigatorButton(request, serviceBean));
        sessionBean.setHeader(getResourceBundle(request.getLocale()).getString("header"));

        return sessionBean;
	}
	
	private void updateConfig(PortletRequest request, BrowsingReportsPortletSessionBean sessionBean, String confFile) {
		try {
			DataServiceBean dataService = PortletUtil.createService(request);
			BrowsingReportsPortletConfReader reader = new BrowsingReportsPortletConfReader(confFile, dataService);
			
			String exportType = reader.getExportType();
			sessionBean.setExportType(exportType);
			
			String jsonReports = reader.getJSONConfReports();
			sessionBean.setJsonReports(jsonReports);
			
		} catch (Exception e) {
			System.out.println("Exception: error in the configuration of poretlet BrowsingReportsPortlet");
			e.printStackTrace();
		}
	}	
	
	private Search getEmployeesSearch() {
		Search search = new Search();
		search.setColumns(getColumnsName());

		search.setByAttributes(true);
		search.setWords(null);
		List templates = new ArrayList(1);
		templates.add(DataObject.createFromId(TEMPLATE_PERSON));
		search.setTemplates(templates);
		search.addStringAttribute(Attribute.ID_NAME);

		List states = new ArrayList(1);
		states.add(CARDSTATE_ACTIVE_USER.getId().toString());
		search.setStates(states);

		return search;
	}
	
	private List getColumnsName() {
		List columns = new ArrayList();
		SearchResult.Column col = new SearchResult.Column();
		col.setAttributeId(ATTR_FIRST_NAME);
		columns.add(col);
		col = new SearchResult.Column();
		col.setAttributeId(ATTR_MIDDLE_NAME);
		columns.add(col);
		col = new SearchResult.Column();
		col.setAttributeId(ATTR_LAST_NAME);
		columns.add(col);
		col = new SearchResult.Column();
		col.setAttributeId(Attribute.ID_NAME);
		columns.add(col);
		return columns;
	}
}
