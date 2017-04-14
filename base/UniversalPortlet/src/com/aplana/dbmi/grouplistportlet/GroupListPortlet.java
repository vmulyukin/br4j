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
package com.aplana.dbmi.grouplistportlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.PortletService;
import com.aplana.dbmi.action.DeleteSystemGroupAction;
import com.aplana.dbmi.importrolesportlet.RolesImportPortlet;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.SystemGroup;
import com.aplana.dbmi.common.utils.portlet.PortletMessage;
import com.aplana.dbmi.common.utils.portlet.PortletMessage.PortletMessageType;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.universalportlet.UniversalPortlet;
import com.aplana.dbmi.universalportlet.UniversalPortletSessionBean;

public class GroupListPortlet extends UniversalPortlet {

	public static final String JSP_FOLDER = "/_GroupListPortlet/jsp/"; // JSP folder name
    public static final String VIEW_JSP = "GroupListPortletView"; // JSP file name to be rendered on the view mode
    
	public static final String OPEN_FOR_EDIT_FIELD = "MI_OPEN_FOR_EDIT";
	public static final String OPEN_EDIT_MODE_FIELD = "MI_OPEN_EDIT_MODE_FIELD";
	public static final String MI_OPEN_ENTITY_ID = "MI_OPEN_ENTITY_ID";
	public static final String MI_CREATE_ENTITY = "MI_CREATE_ENTITY";
	public static final String BACK_URL_FIELD = "MI_BACK_URL_FIELD";
	public static final String MI_DELETE_ENTITY_ID = "MI_DELETE_ENTITY_ID";
	public static final String IMPORT_GROUP_ACTION = "MI_IMPORT_GROUP_ACTION";
	
	//Actions
	public static final String ACTION_FIELD = "MI_ACTION_FIELD";
	public static final String DELETE_GROUP_ACTION = "MI_DELETE_GROUP_ACTION";
	
	public static final String MSG_PARAM_NAME = "MI_GROUP_MSG_PARAM_NAME";
    
    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);
        setJspView(VIEW_JSP);
        setJspFolder(JSP_FOLDER);
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
    
    public void processAction(ActionRequest request, ActionResponse response) throws PortletException, java.io.IOException {
        Locale requestLocale = request.getLocale();
        ContextProvider.getContext().setLocale(requestLocale);
        final String formAction = request.getParameter(ACTION_FIELD);
        if (DELETE_GROUP_ACTION.equals(formAction)) {
        	deleteGroupHandler(request, response);
        } else 
        if(IMPORT_GROUP_ACTION.equals(formAction)) {
			importNewGroups(request, response);
		} else {
        	super.processAction(request, response);
        }
    }
    
    private void deleteGroupHandler(ActionRequest request, ActionResponse response) throws PortletException {
		UniversalPortletSessionBean sessionBean = getSessionBean(request, response);
		DataServiceBean serviceBean = sessionBean.getServiceBean(request);
		ObjectId groupId = null;
		String groupIdStr = request.getParameter(MI_DELETE_ENTITY_ID);
		if (groupIdStr != null && !groupIdStr.isEmpty()) {
			groupId = new ObjectId(SystemGroup.class, groupIdStr);
		} else {
			logger.error("Cannot get group id parameter from request");
			return;
		}
		
		DeleteSystemGroupAction deleteAction = new DeleteSystemGroupAction(groupId);

		try {
			serviceBean.doAction(deleteAction);
			String msg = getPortletConfig().getResourceBundle(request.getLocale()).getString("group.form.delete.success.msg");
			request.getPortletSession().setAttribute(MSG_PARAM_NAME, new PortletMessage(msg, PortletMessageType.INFO), PortletSession.APPLICATION_SCOPE);
			if (logger.isDebugEnabled()){
				logger.debug("System group " + groupId.getId() + " deleted successfully");
			}
		} catch (Exception e) {
			request.getPortletSession().setAttribute(MSG_PARAM_NAME, new PortletMessage(e.getMessage(), PortletMessageType.ERROR), PortletSession.APPLICATION_SCOPE);
			logger.error("System group " + groupId.getId() + " cannot be deleted", e);
		}
	}
    
    private void importNewGroups(ActionRequest request, ActionResponse response) throws PortletException, IOException {
		PortletService portletService = Portal.getFactory().getPortletService();
		UniversalPortletSessionBean sessionBean = getSessionBean(request, response);
		String groupImportPageId = "dbmi.Group.Import";
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
		final String importGroupURL = portletService.generateLink(groupImportPageId, "dbmi.Card.Import.Group", urlParams, request, response);
		response.sendRedirect(importGroupURL);
	}
}