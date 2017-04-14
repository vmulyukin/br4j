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
package com.aplana.dbmi.roleadmin;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

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
import com.aplana.dbmi.action.CheckLock;
import com.aplana.dbmi.action.DeleteSystemRoleAction;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.common.utils.portlet.PortletMessage.PortletMessageType;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.Group;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.Reference;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.Role;
import com.aplana.dbmi.model.SystemGroup;
import com.aplana.dbmi.model.SystemRole;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.filter.UserIdFilter;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.AsyncDataServiceBean;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ObjectLockedException;
import com.aplana.dbmi.service.ObjectNotLockedException;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.UserPrincipal;
import com.aplana.dbmi.service.AsyncDataServiceBean.ExecuteOption;
import com.aplana.dbmi.showlist.MIShowListPortlet;
import com.aplana.dbmi.support.action.GetRulesInfoByRole;

/**
 * A Role Administration portlet based on GenericPortlet
 */
public class RoleAdminPortlet extends GenericPortlet {
	private final static Log logger = LogFactory.getLog(RoleAdminPortlet.class);
	public static final String JSP_FOLDER    = "/WEB-INF/jsp/";    			// JSP folder name

	public static final String VIEW_JSP      = "RoleAdminPortletView";         // JSP file name to be rendered on the view mode
	public static final String EDIT_JSP      = "RoleAdminPortletEdit";         // JSP file name to be rendered on the edit mode
	
	public static final String SESSION_BEAN  = "RoleAdminPortletSessionBean";  // Bean name for the portlet session
	
	public static final String ROLE_CODE_FIELD 	= "ROLE_CODE_FIELD"; 
	public static final String ROLE_RUS_NAME_FIELD 	= "ROLE_RUS_NAME_FIELD"; 
	public static final String ROLE_EN_NAME_FIELD 	= "ROLE_EN_NAME_FIELD";
	public static final String ROLE_GROUPS_SELECT_FIELD 	= "ROLE_GROUPS_SELECT_FIELD";
	public static final String ROLE_GROUPS_TABLE_FIELD 	= "ROLE_GROUPS_TABLE_FIELD";
	public static final String SELECTED_GROUPS_FIELD 	= "SELECTED_GROUPS_FIELD";
	public static final String ROLE_USERS_TABLE = "ROLE_USERS_TABLE";
	public static final String ROLE_RULES_TABLE = "ROLE_RULES_TABLE";
	
	public static final String OPEN_FOR_EDIT_FIELD = "MI_OPEN_FOR_EDIT";
	public static final String OPEN_EDIT_MODE_FIELD = "MI_OPEN_EDIT_MODE_FIELD";
	public static final String MI_OPEN_ENTITY_ID = "MI_OPEN_ENTITY_ID";
	public static final String MI_CREATE_ENTITY = "MI_CREATE_ENTITY";
	public static final String BACK_URL_FIELD = "MI_BACK_URL_FIELD";

	//Actions
	public static final String ACTION_FIELD = "MI_ACTION_FIELD";
	public static final String STORE_ROLE_ACTION = "MI_STORE_ROLE_ACTION";
	public static final String DELETE_ROLE_ACTION = "MI_DELETE_ROLE_ACTION";
	public static final String BACK_ACTION = "MI_BACK_ACTION";
	public static final String CLOSE_EDIT_MODE_ACTION = "MI_CLOSE_EDIT_MODE_ACTION";
	public static final String OPEN_EDIT_MODE_ACTION = "MI_OPEN_EDIT_MODE_ACTION";
	
	// GUI field name
	public static final String EDIT_FORM_NAME = "EditRoleForm";
	public static final String VIEW_FORM_NAME = "ViewRoleForm";

	public static final String MSG_PARAM_NAME = "MI_ROLE_MSG_PARAM_NAME";

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
		RoleAdminPortletSessionBean sessionBean = getSessionBean(request);
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
			SystemRole systemRole = null;
			try {
				ObjectId roleId = new ObjectId(SystemRole.class, entityId);
				sessionBean.setRoleRulesInfo((List<Map<String, Object>>) sessionBean.getServiceBean(request).doAction(new GetRulesInfoByRole(roleId)));
				systemRole = (SystemRole)sessionBean.getServiceBean(request).getById(roleId);
				Collection<SystemGroup> roleGroups = sessionBean.getServiceBean(request).listChildren(systemRole.getId(), SystemGroup.class);
				systemRole.setRoleGroups((List<SystemGroup>)roleGroups);
				sessionBean.setSystemRole(systemRole);
				if (sessionBean.isEditMode()) {
					try {
						sessionBean.getServiceBean(request).doAction(new CheckLock(roleId));
					} catch (ObjectLockedException ex) {
						String msg = getPortletConfig().getResourceBundle(request.getLocale()).getString("role.form.store.lock.msg");
						MessageFormat.format(msg, ex.getLocker().getFullName());
						sessionBean.setMessageWithType(msg , PortletMessageType.ERROR);
						logger.debug("System role " + systemRole.getId().getId() + " is locked by " + ex.getLocker().getFullName(), ex);
						sessionBean.setEditMode(false);
					} catch (ObjectNotLockedException ex) {
					}
				}
			} catch (DataException e) {
				sessionBean.setMessageWithType(e.getMessage() , PortletMessageType.ERROR);
				logger.error("System role " + entityId + " cannot be loaded", e);
			} catch (Exception e) {
					sessionBean.setMessageWithType(new DataException(e).getMessage() , PortletMessageType.ERROR);
					logger.error("System role " + entityId + " cannot be loaded", e);
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
			RoleAdminPortletSessionBean sessionBean = getSessionBean(request);
			if(action.equals(STORE_ROLE_ACTION)) {
				storeRoleHandler(request, response);
			} else if(action.equals(DELETE_ROLE_ACTION)) {
				deleteRoleHandler(request, response);				
			} else if(action.equals(BACK_ACTION)) {
				leaveRolePortlet(request, response);
			} else if(action.equals(CLOSE_EDIT_MODE_ACTION)) {
				sessionBean.setEditMode(false);
				if (sessionBean.isOpenedInEditMode() || sessionBean.isNewMode()) {
					leaveRolePortlet(request, response);
				}
			} else if(action.equals(OPEN_EDIT_MODE_ACTION)) {
				sessionBean.setEditMode(true);			
			}
		}
	}


	private void storeRoleHandler(ActionRequest request, ActionResponse response) {
		RoleAdminPortletSessionBean sessionBean = getSessionBean(request);
		AsyncDataServiceBean serviceBean = sessionBean.getServiceBean(request);
		try {
			fillRole(request);
			validateRole(request);
			SystemRole systemRole = sessionBean.getSystemRole();
			if (sessionBean.isEditMode()) {
				boolean isLocked = false;
				try {
					serviceBean.doAction(new LockObject(systemRole.getId()));
					isLocked = true;

					serviceBean.saveObject(systemRole, ExecuteOption.SYNC);
					
					String msg = getPortletConfig().getResourceBundle(request.getLocale()).getString("role.form.store.success.msg");
					sessionBean.setMessageWithType(msg , PortletMessageType.EVENT);
			
					logger.debug("System role " + systemRole.getId().getId() + " saved successfully");
					
					sessionBean.setEditMode(false);
					if (sessionBean.isOpenedInEditMode()) {
						leaveRolePortlet(request, response);
					}
					
				} catch (ObjectLockedException ex) {
					String msg = getPortletConfig().getResourceBundle(request.getLocale()).getString("role.form.store.lock.msg");
					MessageFormat.format(msg, ex.getLocker().getFullName());
					sessionBean.setMessageWithType(msg , PortletMessageType.ERROR);
					logger.debug("System role " + systemRole.getId().getId() + " is locked by " + ex.getLocker().getFullName(), ex);
				} finally {
					if (isLocked) {
						serviceBean.doAction(new UnlockObject(systemRole.getId()));
					}
				}
			} else if (sessionBean.isNewMode()) {
				ObjectId id = serviceBean.saveObject(systemRole, ExecuteOption.SYNC);
				systemRole.setId(id);
				String msg = getPortletConfig().getResourceBundle(request.getLocale()).getString("role.form.store.success.msg");
				sessionBean.setMessageWithType(msg , PortletMessageType.EVENT);
			
				logger.debug("System role " + systemRole.getId().getId() + " saved successfully");
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


	private SystemRole fillRole(ActionRequest request) throws DataException, ServiceException {		
		RoleAdminPortletSessionBean sessionBean = getSessionBean(request);
		SystemRole systemRole = sessionBean.getSystemRole();
		if (null == systemRole) {
			systemRole = new SystemRole();
			sessionBean.setSystemRole(systemRole);
		}
		if( request.getParameter(ROLE_CODE_FIELD) != null ) {
			String roleCode = request.getParameter(ROLE_CODE_FIELD).trim();
			systemRole.setRoleCode(roleCode);
		}
		
		if( request.getParameter(ROLE_RUS_NAME_FIELD) != null ) {
			String roleRusName = request.getParameter(ROLE_RUS_NAME_FIELD).trim();
			systemRole.setNameRu(roleRusName);
		}
		if( request.getParameter(ROLE_EN_NAME_FIELD) != null ) {
			String roleEnName = request.getParameter(ROLE_EN_NAME_FIELD).trim();
			systemRole.setNameEn(roleEnName);
		}
		if( request.getParameter(SELECTED_GROUPS_FIELD) != null ) {
			final List<String> roleGroupCodes = new ArrayList<String>();
			String assignedGroups = request.getParameter(SELECTED_GROUPS_FIELD).trim();
			final List<ObjectId> roleGroupIds = ObjectIdUtils.commaDelimitedStringToIds(assignedGroups, SystemGroup.class);
			List<SystemGroup> roleGroups = new ArrayList<SystemGroup>();
			for (ObjectId groupId : roleGroupIds) {
				SystemGroup group = sessionBean.getServiceBean(request).getById(groupId);
				roleGroups.add(group);
			}
			systemRole.setRoleGroups(roleGroups);
		}
		return systemRole;
	}

	private void validateRole(ActionRequest request) throws DataException {
		RoleAdminPortletSessionBean sessionBean = getSessionBean(request);
		SystemRole role = sessionBean.getSystemRole();
		if (role != null) {
			if (sessionBean.isNewMode()) {
				if (role.getRoleCode() == null || role.getRoleCode().isEmpty()) {
					throw new DataException ("role.mandatory.field.empty", 
							new Object[]{getPortletConfig().getResourceBundle(request.getLocale()).getString("roleadmin.role.code")});
				}
			}
			if (role.getNameRu() == null || role.getNameRu().isEmpty()) {
				throw new DataException ("role.mandatory.field.empty",  
						new Object[]{getPortletConfig().getResourceBundle(request.getLocale()).getString("roleadmin.role.name_ru")});
			} else if (role.getNameEn() == null || role.getNameEn().isEmpty()) {
				throw new DataException ("role.mandatory.field.empty", 
						new Object[]{getPortletConfig().getResourceBundle(request.getLocale()).getString("roleadmin.role.name_en")});
			}
		}
	}


	private void deleteRoleHandler(ActionRequest request, ActionResponse response) {
		RoleAdminPortletSessionBean sessionBean = getSessionBean(request);
		DataServiceBean serviceBean = sessionBean.getServiceBean(request);
		SystemRole systemRole = sessionBean.getSystemRole();
		DeleteSystemRoleAction deleteAction = new DeleteSystemRoleAction(systemRole.getId());

		try {
		
			serviceBean.doAction(deleteAction);
	
			String msg = getPortletConfig().getResourceBundle(request.getLocale()).getString("role.form.delete.success.msg");
			sessionBean.setMessageWithType(msg , PortletMessageType.EVENT);
			if (logger.isDebugEnabled()) {
				logger.debug("System role " + systemRole.getId().getId() + " deleted successfully");
			}

			leaveRolePortlet(request, response);
		} catch (IOException e) {
			logger.error("Cannot leave role portlet due to exception", e);
		} catch (Exception e) {
			sessionBean.setMessageWithType(e.getMessage() , PortletMessageType.ERROR);
			logger.error("System role " + systemRole.getId().getId() + " cannot be deleted", e);
		}
	}
	
	private void leaveRolePortlet(ActionRequest request, ActionResponse response) throws IOException{
		RoleAdminPortletSessionBean sessionBean = getSessionBean(request);
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
	private RoleAdminPortletSessionBean getSessionBean(PortletRequest request) {
		PortletSession session = request.getPortletSession();
		if( session == null )
			return null;
		RoleAdminPortletSessionBean sessionBean = (RoleAdminPortletSessionBean)session.getAttribute(SESSION_BEAN);
		if( sessionBean == null ) {
			sessionBean = new RoleAdminPortletSessionBean();
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
