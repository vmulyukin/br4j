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

import java.io.IOException;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.aplana.dbmi.component.AccessComponent;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.SecurityAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;

public class SecurityAttributeEditor extends JspAttributeEditor implements PortletForm
{
    public static final String EDIT_FORM_NAME = "EditCardForm";
    public static final String JSP_PATH = "/WEB-INF/jsp/html/";
    public static final String JSP_EXT = ".jsp";
    
	public SecurityAttributeEditor() {
		setParameter(PARAM_JSP, getJspPath("AccessComponent"));
	}

	public boolean gatherData(ActionRequest request, Attribute attr)
			throws DataException {
		getAccessComponent(request).parseRequest(request, null);
		((SecurityAttribute) attr).setAccessList(getAccessComponent(request).getAccessItemList());
		return true;
	}

	public void initEditor(PortletRequest request, Attribute attr)
			throws DataException {
    	getAccessComponent(request).setAccessItemList((List) ((SecurityAttribute) attr).getAccessList());
	}

	public boolean processAction(ActionRequest request, ActionResponse response,
    		Attribute attr) throws DataException {
        String accessComponentAction = request.getParameter(AccessComponent.ACTION_FIELD);
        if (AccessComponent.EDIT_DEPARTMENT_ACCESS_ACTION.equals(accessComponentAction)
                || AccessComponent.EDIT_INDIVIDUAL_ACCESS_ACTION.equals(accessComponentAction)) {
            getAccessComponent(request).setAccessHandlerAction(true);
            request.getPortletSession().setAttribute(AccessComponent.CURRENT_HANDLER, getAccessComponent(request));
            CardPortlet.getSessionBean(request).openForm(this);
        }
        return false;
	}

	public void doFormView(RenderRequest request, RenderResponse response)
			throws IOException, PortletException {
		PortletRequestDispatcher rd = request.getPortletSession().getPortletContext()
				.getRequestDispatcher(getJspPath(AccessComponent.SELECTED_LIST_JSP));
		rd.include(request, response);
	}

	public void processFormAction(ActionRequest request, ActionResponse response)
			throws IOException, PortletException {
		AccessComponent component = getAccessComponent(request);
		component.parseRequest(request, response);
		if (!component.isAccessHandlerAction())
			CardPortlet.getSessionBean(request).closeForm();
	}

	private AccessComponent getAccessComponent(PortletRequest request) {
        PortletSession session = request.getPortletSession();
        if (session == null)
            return null;
        AccessComponent component = (AccessComponent) session.getAttribute(AccessComponent.ACCESS_HANDLER);
        if (component == null) {
            CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(request);
            DataServiceBean serviceBean = sessionBean.getServiceBean();
            component = new AccessComponent(serviceBean, null, EDIT_FORM_NAME);
            session.setAttribute(AccessComponent.ACCESS_HANDLER, component);
        }
        return component;
    }
	
	private static String getJspPath(String name) {
		return JSP_PATH + name + JSP_EXT;
	}
}
