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
package com.aplana.dbmi.rolelistportlet;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.displaytag.util.SortingState;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.PortletService;
import com.aplana.dbmi.action.DeleteSystemRoleAction;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.common.utils.portlet.PortletMessage;
import com.aplana.dbmi.common.utils.portlet.PortletMessage.PortletMessageType;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.SystemGroup;
import com.aplana.dbmi.model.SystemRole;
import com.aplana.dbmi.importrolesportlet.RolesImportPortlet;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletConfig;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ObjectLockedException;
import com.aplana.dbmi.universalportlet.UniversalPortlet;
import com.aplana.dbmi.universalportlet.UniversalPortletSessionBean;

public class RoleListPortlet extends UniversalPortlet {

	public static final String JSP_FOLDER = "/_RoleListPortlet/jsp/"; // JSP folder name
    public static final String VIEW_JSP = "RoleListPortletView"; // JSP file name to be rendered on the view mode
    
	public static final String OPEN_FOR_EDIT_FIELD = "MI_OPEN_FOR_EDIT";
	public static final String OPEN_EDIT_MODE_FIELD = "MI_OPEN_EDIT_MODE_FIELD";
	public static final String MI_OPEN_ENTITY_ID = "MI_OPEN_ENTITY_ID";
	public static final String MI_CREATE_ENTITY = "MI_CREATE_ENTITY";
	public static final String BACK_URL_FIELD = "MI_BACK_URL_FIELD";
	public static final String MI_DELETE_ENTITY_ID = "MI_DELETE_ENTITY_ID";
	public static final String IMPORT_ROLE_ACTION = "MI_IMPORT_ROLE_ACTION";
	
	//Actions
	public static final String ACTION_FIELD = "MI_ACTION_FIELD";
	public static final String DELETE_ROLE_ACTION = "MI_DELETE_ROLE_ACTION";
	
	public static final String MSG_PARAM_NAME = "MI_ROLE_MSG_PARAM_NAME";

    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);
        setJspView(VIEW_JSP);
        setJspFolder(JSP_FOLDER);
    }
    public void processAction(ActionRequest request, ActionResponse response) throws PortletException, java.io.IOException {
        Locale requestLocale = request.getLocale();
        ContextProvider.getContext().setLocale(requestLocale);
        final String formAction = request.getParameter(ACTION_FIELD);
        if (DELETE_ROLE_ACTION.equals(formAction)) {
        	deleteRoleHandler(request, response);
        } else 
        if(IMPORT_ROLE_ACTION.equals(formAction)) {
			importNewRoles(request, response);
		} else {
        	super.processAction(request, response);
        }
    }
    
    /**
     * Serve up the <code>view</code> mode.
     *
     * @see javax.portlet.GenericPortlet#doView(javax.portlet.RenderRequest,
     *      javax.portlet.RenderResponse)
     */
    public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
    	super.doView(request, response);
    }
    
	private void deleteRoleHandler(ActionRequest request, ActionResponse response) throws PortletException {
		UniversalPortletSessionBean sessionBean = getSessionBean(request, response);
		DataServiceBean serviceBean = sessionBean.getServiceBean(request);
		ObjectId roleId = null;
		String roleIdStr = request.getParameter(MI_DELETE_ENTITY_ID);
		if (roleIdStr != null && !roleIdStr.isEmpty()) {
			roleId = new ObjectId(SystemRole.class, roleIdStr);
		} else {
			logger.error("Cannot get role id parameter from request");
			return;
		}
		
		DeleteSystemRoleAction deleteAction = new DeleteSystemRoleAction(roleId);

		try {
			serviceBean.doAction(deleteAction);
			String msg = getPortletConfig().getResourceBundle(request.getLocale()).getString("role.form.delete.success.msg");
			request.getPortletSession().setAttribute(MSG_PARAM_NAME, new PortletMessage(msg, PortletMessageType.INFO), PortletSession.APPLICATION_SCOPE);
			if (logger.isDebugEnabled()){
				logger.debug("System role " + roleId.getId() + " deleted successfully");
			}
		} catch (Exception e) {
			request.getPortletSession().setAttribute(MSG_PARAM_NAME, new PortletMessage(e.getMessage(), PortletMessageType.ERROR), PortletSession.APPLICATION_SCOPE);
			logger.error("System role " + roleId.getId() + " cannot be deleted", e);
		}
	}
	
	private void importNewRoles(ActionRequest request, ActionResponse response) throws PortletException, IOException {
		PortletService portletService = Portal.getFactory().getPortletService();
		UniversalPortletSessionBean sessionBean = getSessionBean(request, response);
		String roleImportPageId = "dbmi.Role.Import";
		final HashMap urlParams = new HashMap();
		if (sessionBean.getCustomImportTitle()!=null){
			urlParams.put(RolesImportPortlet.CUSTOM_IMPORT_TITLE_PARAM, sessionBean.getCustomImportTitle());
		}
		urlParams.put(RolesImportPortlet.BACK_URL_FIELD, request.getParameter(BACK_URL_FIELD));
		urlParams.put(RolesImportPortlet.CHECK_DOUBLETS_PARAM, true);
		urlParams.put(RolesImportPortlet.UPDATE_DOUBLETS_PARAM, false);
		urlParams.put(RolesImportPortlet.UPDATE_DOUBLETS_SUPPORT_PARAM, true);
		urlParams.put(RolesImportPortlet.CHECK_DOUBLETS_SUPPORT_PARAM, false);
		urlParams.put(RolesImportPortlet.INIT, true);
		final String importRoleURL = portletService.generateLink(roleImportPageId, "dbmi.Card.Import.Role", urlParams, request, response);
		response.sendRedirect(importRoleURL);
	}
}
