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
package com.aplana.dbmi.groupadmin;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.PortletService;
import com.aplana.dbmi.common.utils.portlet.PortletMessage.PortletMessageType;
import com.aplana.dbmi.action.CheckLock;
import com.aplana.dbmi.action.DeleteSystemGroupAction;
//import com.aplana.dbmi.action.DeleteSystemGroupAction;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.SystemGroup;
import com.aplana.dbmi.model.SystemRole;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.AsyncDataServiceBean;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ObjectLockedException;
import com.aplana.dbmi.service.ObjectNotLockedException;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.AsyncDataServiceBean.ExecuteOption;

public class GroupAdminPortlet extends GenericPortlet {

	private final static Log logger = LogFactory.getLog(GroupAdminPortlet.class);
	public static final String JSP_FOLDER    = "/WEB-INF/jsp/";    			// JSP folder name

	public static final String VIEW_JSP      = "GroupAdminPortletView";         // JSP file name to be rendered on the view mode
	public static final String EDIT_JSP      = "GroupAdminPortletEdit";         // JSP file name to be rendered on the edit mode
	
	public static final String SESSION_BEAN  = "GroupAdminPortletSessionBean";  // Bean name for the portlet session
	
	public static final String GROUP_CODE_FIELD 	= "GROUP_CODE_FIELD"; 
	public static final String GROUP_RUS_NAME_FIELD 	= "GROUP_RUS_NAME_FIELD"; 
	public static final String GROUP_EN_NAME_FIELD 	= "GROUP_EN_NAME_FIELD";
	                                                                                     
	public static final String GROUP_ROLES_SELECT_FIELD 	= "GROUP_ROLES_SELECT_FIELD";
	public static final String GROUP_ROLES_TABLE_FIELD 	= "GROUP_ROLES_TABLE_FIELD";
	public static final String SELECTED_ROLES_FIELD 	= "SELECTED_ROLES_FIELD";
	public static final String GROUP_USERS_TABLE = "GROUP_USERS_TABLE";

	
	public static final String OPEN_FOR_EDIT_FIELD = "MI_OPEN_FOR_EDIT";
	public static final String OPEN_EDIT_MODE_FIELD = "MI_OPEN_EDIT_MODE_FIELD";
	public static final String MI_OPEN_ENTITY_ID = "MI_OPEN_ENTITY_ID";
	public static final String MI_CREATE_ENTITY = "MI_CREATE_ENTITY";
	public static final String BACK_URL_FIELD = "MI_BACK_URL_FIELD";

	//Actions
	public static final String ACTION_FIELD = "MI_ACTION_FIELD";
	public static final String STORE_GROUP_ACTION = "MI_STORE_GROUP_ACTION";
	public static final String DELETE_GROUP_ACTION = "MI_DELETE_GROUP_ACTION";
	public static final String BACK_ACTION = "MI_BACK_ACTION";
	public static final String CLOSE_EDIT_MODE_ACTION = "MI_CLOSE_EDIT_MODE_ACTION";
	public static final String OPEN_EDIT_MODE_ACTION = "MI_OPEN_EDIT_MODE_ACTION";
	
	// GUI field name
	public static final String EDIT_FORM_NAME = "EditGroupForm";
	public static final String VIEW_FORM_NAME = "ViewGroupForm";

	public static final String MSG_PARAM_NAME = "MI_GROUP_MSG_PARAM_NAME";

	protected PortletService portletService;
    /**
	 * @see javax.portlet.Portlet#init()
	 */
	public void init() throws PortletException{
		super.init();
		portletService = Portal.getFactory().getPortletService();
	}

	/**
	 * Serve up the <code>view</code> mode.
	 * 
	 * @see javax.portlet.GenericPortlet#doView(javax.portlet.RenderRequest, javax.portlet.RenderResponse)
	 */
	public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		// Set the MIME type for the render response
		response.setContentType(request.getResponseContentType());

		// Set user locale
		ContextProvider.getContext().setLocale(request.getLocale()); 
		
		// Check if portlet session exists
		GroupAdminPortletSessionBean sessionBean = getSessionBean(request);
		if( sessionBean == null ) {
			response.getWriter().println("<b>NO PORTLET SESSION YET</b>");
			return;
		}
		
		String backURL = portletService.getUrlParameter(request,
				BACK_URL_FIELD);
		if (backURL != null && !backURL.isEmpty()) {
			sessionBean.setBackURL(backURL);
		}

		String editModeStr = request.getParameter(OPEN_FOR_EDIT_FIELD);
		if (editModeStr != null && !editModeStr.isEmpty()) {
			sessionBean.setEditMode(true);
			sessionBean.setOpenedInEditMode(true);
		}
		
		String openEditMode = request.getParameter(OPEN_EDIT_MODE_FIELD);
		if (openEditMode != null && !openEditMode.isEmpty()) {
			sessionBean.setEditMode(true);
			sessionBean.setOpenedInEditMode(false);
		}
		
		String newMode = request.getParameter(MI_CREATE_ENTITY);
		if (newMode != null && !newMode.isEmpty()) {
			boolean openInNewMode = "true".equals(newMode);
			sessionBean.setNewMode(openInNewMode);
		}
		
		String entityId = request.getParameter(MI_OPEN_ENTITY_ID);
		if (entityId != null && !entityId.isEmpty()) {
			SystemGroup systemGroup = null;
			try {
				ObjectId groupId = new ObjectId(SystemGroup.class, entityId);
				systemGroup = (SystemGroup)sessionBean.getServiceBean(request).getById(groupId);
				Collection<SystemRole> groupRoles = sessionBean.getServiceBean(request).listChildren(systemGroup.getId(), SystemRole.class);
				systemGroup.setSystemRoles(groupRoles);
				sessionBean.setSystemGroup(systemGroup);
				if (sessionBean.isEditMode()) {
					try {
						sessionBean.getServiceBean(request).doAction(new CheckLock(groupId));
					} catch (ObjectLockedException ex) {
						String msg = getPortletConfig().getResourceBundle(request.getLocale()).getString("role.form.store.lock.msg");
						MessageFormat.format(msg, ex.getLocker().getFullName());
						sessionBean.setMessageWithType(msg , PortletMessageType.ERROR);
						logger.debug("System group " + systemGroup.getId().getId() + " is locked by " + ex.getLocker().getFullName(), ex);
						sessionBean.setEditMode(false);
					} catch (ObjectNotLockedException ex) {
					}
				}
			} catch (DataException e) {
				sessionBean.setMessageWithType(e.getMessage() , PortletMessageType.ERROR);
				logger.error("System group " + entityId + " cannot be loaded", e);
			} catch (Exception e) {
					sessionBean.setMessageWithType(new DataException(e).getMessage() , PortletMessageType.ERROR);
					logger.error("System group " + entityId + " cannot be loaded", e);
			}
		}
		String jspFile = VIEW_JSP;
		if (sessionBean.isEditMode() || sessionBean.isNewMode()) {
			jspFile = EDIT_JSP;
		}

		// Invoke the JSP to render
		PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher(getJspFilePath(request, jspFile));
		rd.include(request,response);
	}

	/**
	 * Process an action request.
	 * 
	 * @see javax.portlet.Portlet#processAction(javax.portlet.ActionRequest, javax.portlet.ActionResponse)
	 */
	public void processAction(ActionRequest request, ActionResponse response) throws PortletException, java.io.IOException {
		// Set user locale
		ContextProvider.getContext().setLocale(request.getLocale()); 
	
		String action = request.getParameter(ACTION_FIELD); 
		if( action != null ) {
			GroupAdminPortletSessionBean sessionBean = getSessionBean(request);
			if(action.equals(STORE_GROUP_ACTION)) {
				storeGroupHandler(request, response);
			} else if(action.equals(DELETE_GROUP_ACTION)) {
				deleteGroupHandler(request, response);				
			} else if(action.equals(BACK_ACTION)) {
				leaveGroupPortlet(request, response);
			} else if(action.equals(CLOSE_EDIT_MODE_ACTION)) {
				sessionBean.setEditMode(false);
				if (sessionBean.isOpenedInEditMode() || sessionBean.isNewMode()) {
					leaveGroupPortlet(request, response);
				}
			} else if(action.equals(OPEN_EDIT_MODE_ACTION)) {
				sessionBean.setEditMode(true);			
			}
		}
	}


	private void storeGroupHandler(ActionRequest request, ActionResponse response) {
		GroupAdminPortletSessionBean sessionBean = getSessionBean(request);
		AsyncDataServiceBean serviceBean = sessionBean.getServiceBean(request);
		try {
			fillGroup(request);
			validateGroup(request);
			SystemGroup systemGroup = sessionBean.getSystemGroup();
			if (sessionBean.isEditMode()) {
				boolean isLocked = false;
				try {
					serviceBean.doAction(new LockObject(systemGroup.getId()));
					isLocked = true;

					serviceBean.saveObject(systemGroup, ExecuteOption.SYNC);
					
					String msg = getPortletConfig().getResourceBundle(request.getLocale()).getString("group.form.store.success.msg");
					sessionBean.setMessageWithType(msg , PortletMessageType.EVENT);
			
					logger.debug("System group " + systemGroup.getId().getId() + " saved successfully");
					
					sessionBean.setEditMode(false);
					if (sessionBean.isOpenedInEditMode()) {
						leaveGroupPortlet(request, response);
					}
					
				} catch (ObjectLockedException ex) {
					String msg = getPortletConfig().getResourceBundle(request.getLocale()).getString("group.form.store.lock.msg");
					MessageFormat.format(msg, ex.getLocker().getFullName());
					sessionBean.setMessageWithType(msg , PortletMessageType.ERROR);
					logger.debug("System group " + systemGroup.getId().getId() + " is locked by " + ex.getLocker().getFullName(), ex);
				} finally {
					if (isLocked) {
						serviceBean.doAction(new UnlockObject(systemGroup.getId()));
					}
				}
			} else if (sessionBean.isNewMode()) {
				ObjectId id = serviceBean.saveObject(systemGroup, ExecuteOption.SYNC);
				systemGroup.setId(id);
				String msg = getPortletConfig().getResourceBundle(request.getLocale()).getString("group.form.store.success.msg");
				sessionBean.setMessageWithType(msg , PortletMessageType.EVENT);
			
				logger.debug("System role " + systemGroup.getId().getId() + " saved successfully");
				sessionBean.setNewMode(false);
				sessionBean.setEditMode(true);
				sessionBean.setOpenedInEditMode(true);
			}
		} catch (DataException e) {
			sessionBean.setMessageWithType(e.getMessage() , PortletMessageType.ERROR);
			logger.error("System role cannot be saved", e);
		} catch (Exception e) {
			sessionBean.setMessageWithType(new DataException(e).getMessage() , PortletMessageType.ERROR);
			logger.error("System role cannot be saved", e);
		}
	}


	private SystemGroup fillGroup(ActionRequest request) throws DataException, ServiceException {		
		GroupAdminPortletSessionBean sessionBean = getSessionBean(request);
		SystemGroup systemGroup = sessionBean.getSystemGroup();
		if (null == systemGroup) {
			systemGroup = new SystemGroup();
			sessionBean.setSystemGroup(systemGroup);
		}
		if( request.getParameter(GROUP_CODE_FIELD) != null ) {
			String groupCode = request.getParameter(GROUP_CODE_FIELD).trim();
			systemGroup.setGroupCode(groupCode);
		}
		
		if( request.getParameter(GROUP_RUS_NAME_FIELD) != null ) {
			String groupRusName = request.getParameter(GROUP_RUS_NAME_FIELD).trim();
			systemGroup.setNameRu(groupRusName);
		}
		if( request.getParameter(GROUP_EN_NAME_FIELD) != null ) {
			String groupEnName = request.getParameter(GROUP_EN_NAME_FIELD).trim();
			systemGroup.setNameEn(groupEnName);
		}
		if( request.getParameter(SELECTED_ROLES_FIELD) != null ) {
			String assignedRoles = request.getParameter(SELECTED_ROLES_FIELD).trim();
			final List<ObjectId> groupRoleIds = ObjectIdUtils.commaDelimitedStringToIds(assignedRoles, SystemRole.class);
			List<SystemRole> groupRoles = new ArrayList<SystemRole>();
			for (ObjectId roleId : groupRoleIds) {
				SystemRole role = sessionBean.getServiceBean(request).getById(roleId);
				groupRoles.add(role);
			}
			systemGroup.setSystemRoles(groupRoles);
		}
		return systemGroup;
	}

	private void validateGroup(ActionRequest request) throws DataException {
		GroupAdminPortletSessionBean sessionBean = getSessionBean(request);
		SystemGroup group = sessionBean.getSystemGroup();
		if (group != null) {
			if (sessionBean.isNewMode()) {
				if (group.getGroupCode() == null || group.getGroupCode().isEmpty()) {
					throw new DataException ("group.mandatory.field.empty", 
							new Object[]{getPortletConfig().getResourceBundle(request.getLocale()).getString("groupadmin.group.code")});
				}
			}
			if (group.getNameRu() == null || group.getNameRu().isEmpty()) {
				throw new DataException ("group.mandatory.field.empty",  
						new Object[]{getPortletConfig().getResourceBundle(request.getLocale()).getString("groupadmin.group.name_ru")});
			} else if (group.getNameEn() == null || group.getNameEn().isEmpty()) {
				throw new DataException ("group.mandatory.field.empty", 
						new Object[]{getPortletConfig().getResourceBundle(request.getLocale()).getString("groupadmin.group.name_en")});
			} else if (group.getSystemRoles() == null || group.getSystemRoles().isEmpty()) {
				throw new DataException ("group.mandatory.field.empty",  
						new Object[]{getPortletConfig().getResourceBundle(request.getLocale()).getString("groupadmin.group.roles")});
			}
		}
	}


	private void deleteGroupHandler(ActionRequest request, ActionResponse response) {
		GroupAdminPortletSessionBean sessionBean = getSessionBean(request);
		DataServiceBean serviceBean = sessionBean.getServiceBean(request);
		SystemGroup systemGroup = sessionBean.getSystemGroup();
		DeleteSystemGroupAction deleteAction = new DeleteSystemGroupAction(systemGroup.getId());

		try {
		
			serviceBean.doAction(deleteAction);
	
			String msg = getPortletConfig().getResourceBundle(request.getLocale()).getString("group.form.delete.success.msg");
			sessionBean.setMessageWithType(msg , PortletMessageType.EVENT);
			if (logger.isDebugEnabled()) {
				logger.debug("System group " + systemGroup.getId().getId() + " deleted successfully");
			}

			leaveGroupPortlet(request, response);
		} catch (IOException e) {
			logger.error("Cannot leave group portlet due to exception", e);
		} catch (Exception e) {
			sessionBean.setMessageWithType(e.getMessage() , PortletMessageType.ERROR);
			logger.error("System group " + systemGroup.getId().getId() + " cannot be deleted", e);
		}
	}
	
	private void leaveGroupPortlet(ActionRequest request, ActionResponse response) throws IOException{
		GroupAdminPortletSessionBean sessionBean = getSessionBean(request);
		String backURL = sessionBean.getBackURL();
		if (sessionBean.getMessage() != null){
			PortletSession session = request.getPortletSession();
			session.setAttribute(MSG_PARAM_NAME, sessionBean.getPortletMessage(), PortletSession.APPLICATION_SCOPE);
		}
		request.getPortletSession().removeAttribute(SESSION_BEAN);
		if (backURL == null) {
			redirectToPortalDefaultPage(request, response);
		} else {
			response.sendRedirect(backURL);
		}
	}
	
	/**
	 * ������������� �� ��������� �������� �������
	 */
	private void redirectToPortalDefaultPage(ActionRequest request,
			ActionResponse response) throws IOException {
		String backURL = portletService.generateLink("dbmi.defaultPage", null,
				null, request, response);
		response.sendRedirect(backURL);
	}

	/**
	 * Get SessionBean.
	 * 
	 * @param request PortletRequest
	 * @return UserAdminPortletSessionBean
	 */
	private GroupAdminPortletSessionBean getSessionBean(PortletRequest request) {
		PortletSession session = request.getPortletSession();
		if( session == null )
			return null;
		GroupAdminPortletSessionBean sessionBean = (GroupAdminPortletSessionBean)session.getAttribute(SESSION_BEAN);
		if( sessionBean == null ) {
			sessionBean = new GroupAdminPortletSessionBean();
			session.setAttribute(SESSION_BEAN,sessionBean);
		}
		return sessionBean;
	}

	
	/**
	 * Returns JSP file path.
	 * 
	 * @param request Render request
	 * @param jspFile JSP file name
	 * @return JSP file path
	 */
	private static String getJspFilePath(RenderRequest request, String jspFile) {
		String markup = request.getProperty("wps.markup");
		if( markup == null )
			markup = getMarkup(request.getResponseContentType());
		return JSP_FOLDER + markup + "/" + jspFile + "." + getJspExtension(markup);
	}

	/**
	 * Convert MIME type to markup name.
	 * 
	 * @param contentType MIME type
	 * @return Markup name
	 */
	private static String getMarkup(String contentType) {
		if( "text/vnd.wap.wml".equals(contentType) )
			return "wml";
        else
            return "html";
	}

	/**
	 * Returns the file extension for the JSP file
	 * 
	 * @param markupName Markup name
	 * @return JSP extension
	 */
	private static String getJspExtension(String markupName) {
		return "jsp";
	}
}
