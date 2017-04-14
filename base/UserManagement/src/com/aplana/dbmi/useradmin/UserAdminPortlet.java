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
package com.aplana.dbmi.useradmin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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

import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.Reference;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.Role;
import com.aplana.dbmi.model.SystemRole;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.filter.UserIdFilter;
import com.aplana.dbmi.service.AsyncDataServiceBean.ExecuteOption;
import com.jenkov.prizetags.tree.impl.Tree;
import com.jenkov.prizetags.tree.impl.TreeNode;
import com.jenkov.prizetags.tree.itf.ITree;
import com.jenkov.prizetags.tree.itf.ITreeNode;

/**
 * A sample portlet based on GenericPortlet
 */
public class UserAdminPortlet extends GenericPortlet {
	private final static Log logger = LogFactory.getLog(UserAdminPortlet.class);
	public static final String JSP_FOLDER    = "/_UserAdminPortlets/jsp/";    // JSP folder name

	public static final String LIST_VIEW_JSP      = "UserListPortletView";         // JSP file name to be rendered on the view mode
	public static final String DETAIL_VIEW_JSP      = "UserDetailPortletView";         // JSP file name to be rendered on the view mode
	public static final String SESSION_BEAN  = "UserAdminPortletSessionBean";  // Bean name for the portlet session
	
	//Action
	public static final String GET_USER_LIST_ACTION  	= "MI_GET_USER_LIST_ACTION";     
	public static final String EDIT_USER_ACTION  	= "MI_EDIT_USER_ACTION";     
//	public static final String REMOVE_USER_ACTION  	= "MI_REMOVE_USER_ACTION";     
	public static final String REMOVE_USER_ROLE_ACTION  	= "MI_REMOVE_USER_ACTION";     
	public static final String STORE_USER_ACTION  	= "MI_STORE_USER_ACTION";     
	public static final String EDIT_USER_ROLE_ACTION  	= "MI_EDIT_USER_ROLE_ACTION";     
	public static final String ADD_USER_ROLE_ACTION  	= "MI_ADD_USER_ROLE_ACTION";     
	// GUI action
	public static final String BACK_ACTION  	= "MI_BACK_ACTION";     
	public static final String TREE_EXPAND_ACTION  	= "MI_TREE_EXPAND_ACTION";     
	public static final String TREE_COLLAPSE_ACTION  	= "MI_TREE_COLLAPSE_ACTION";     
	
	// GUI field
	public static final String ACTION_FIELD 		= "MI_ACTION_FIELD";     
	public static final String SEARCH_TEMPLATE_ID_FIELD 	= "MI_SEARCH_TEMPLATE_ID_FIELD"; 
	public static final String SELECT_USER_ID_FIELD 	= "MI_SELECT_USER_ID_FIELD"; 
	public static final String ATTR_ID_FIELD 	= "MI_ATTR_ID_FIELD"; 

	public static final String DEPARTMENT_FIELD 	= "MI_DEPARTMENT_FIELD_"; 
	public static final String ROLE_TYPE_FIELD 	= "MI_ROLE_TYPE_FIELD_"; 
	public static final String TEMPLATE_CARD_FIELD 	= "MI_TEMPLATE_CARD_FIELD_"; 
	public static final String REGION_FIELD 	= "MI_REGION_FIELD_"; 
	
	// Request Parameter
	public static final String MSG_PARAM_NAME = "MI_MSG_PARAM_NAME"; 
	
	public static final int ROOT_ID = -1;
	
    public static final String TREE_VARIABLE_VALUE_TYPE = "TREE_VARIABLE_VALUE";

    public static final String TREE_CONSTANT_VALUE_TYPE = "TREE_CONSTANT_VALUE";

    /**
	 * @see javax.portlet.Portlet#init()
	 */
	public void init() throws PortletException{
		super.init();
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
		UserAdminPortletSessionBean sessionBean = getSessionBean(request);
		if( sessionBean==null ) {
			response.getWriter().println("<b>NO PORTLET SESSION YET</b>");
			return;
		}

		String jspFile = LIST_VIEW_JSP;
		if (sessionBean.isDetailViewMode() 
				|| sessionBean.isEditMode()) {
			jspFile = DETAIL_VIEW_JSP;
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
			
			if(action.equals(GET_USER_LIST_ACTION)) {
				getUserListHandler(request, response);
			}
			if(action.equals(EDIT_USER_ACTION)) {
				getUserHandler(request, response);				
			}
//			if(action.equals(REMOVE_USER_ACTION)) {
//				removeUserHandler(request, response);
//			}
			if(action.equals(REMOVE_USER_ROLE_ACTION)) {
				removeUserRoleHandler(request, response);
			}

			if(action.equals(STORE_USER_ACTION)) {
				storeUserHandler(request, response);
			}
			if(action.equals(TREE_COLLAPSE_ACTION)
					|| action.equals(TREE_EXPAND_ACTION)) {				
				
				treeActionHandler(request);				
			}
			if(action.equals(BACK_ACTION)) {
				changeViewModeHandler(request, response);
			}
			if(action.equals(EDIT_USER_ROLE_ACTION)) {
				getUserRoleHandler(true, request, response);				
			}
			if(action.equals(ADD_USER_ROLE_ACTION)) {
				getUserRoleHandler(false, request, response);				
			}
		}		
	}

	private void changeViewModeHandler(ActionRequest request, ActionResponse response) {
		UserAdminPortletSessionBean sessionBean = getSessionBean(request);
		if (sessionBean.isEditMode()) {
			sessionBean.setDetailViewMode(true);
			sessionBean.setEditMode(false);			
			
		} else if (sessionBean.isDetailViewMode()) {
			
			try {
				sessionBean.setDetailViewMode(false);
				sessionBean.setEditMode(false);
				Person user = sessionBean.getUser();
				if (user != null) {
				// unlock obj DB
				sessionBean.getServiceBean(request).doAction(new UnlockObject(user));
				}
			} catch (Exception e) {
				String msg = getPortletConfig().getResourceBundle(request.getLocale()).getString("user.form.db.side.error.msg");
				response.setRenderParameter(MSG_PARAM_NAME, msg + e.getMessage());
			}
		}
	}
	
	
	private void storeUserHandler(ActionRequest request, ActionResponse response) {
		UserAdminPortletSessionBean sessionBean = getSessionBean(request);
		
		try {
			if (sessionBean.isEditMode()) {
				
				fillRole(request);
				Role userRole = sessionBean.getRole();
				Person user = sessionBean.getUser();
				userRole.setPerson(user.getId());
				prepareRoleForStore(userRole, sessionBean);
				ObjectId id = sessionBean.getServiceBean(request).saveObject(userRole, ExecuteOption.SYNC);
				
				// reload role
				userRole = (Role)sessionBean.getServiceBean(request).getById(id);
				prepareRoleForView(userRole, sessionBean);
				sessionBean.setRole(userRole);
				// reload role list
				user.setRoles(sessionBean.getServiceBean(request).listChildren(user.getId(), Role.class));
			
			} else {
				fillUser(request);
				Person user = sessionBean.getUser();
				ObjectId id = sessionBean.getServiceBean(request).saveObject(user, ExecuteOption.SYNC);
				// reload
				user = (Person)sessionBean.getServiceBean(request).getById(id);
				sessionBean.setUser(user);
				
			}

			String msg = getPortletConfig().getResourceBundle(request.getLocale()).getString("user.form.store.success.msg");
			response.setRenderParameter(MSG_PARAM_NAME, msg);
		
		} catch (Exception e) {
			String msg = getPortletConfig().getResourceBundle(request.getLocale()).getString("user.form.db.side.error.msg");
			response.setRenderParameter(MSG_PARAM_NAME, msg + e.getMessage());
		}
		
	}

	private void fillUser(ActionRequest request) {
		// TODO: �������� ����� �������� �������
		/*
		UserAdminPortletSessionBean sessionBean = getSessionBean(request);
		sessionBean.getUser().setDepartment(null);
		if( request.getParameter(DEPARTMENT_FIELD) != null ) {
			String departmentId = request.getParameter(DEPARTMENT_FIELD).trim();
			if (!departmentId.equalsIgnoreCase("" + ROOT_ID)) {
				ReferenceValue department = new ReferenceValue();
				department.setId(Long.parseLong(departmentId));
				sessionBean.getUser().setDepartment(department);
			}
		}
		*/
	}	

	private void fillRole(ActionRequest request) {		
		UserAdminPortletSessionBean sessionBean = getSessionBean(request);
		Role role = sessionBean.getRole();
		if( request.getParameter(ROLE_TYPE_FIELD) != null ) {
			String typeId = request.getParameter(ROLE_TYPE_FIELD).trim();
			role.setSystemRole(sessionBean.getSystemRole(typeId));
		}
		boolean isTemplateRootSelected = false;
		boolean isRegionRootSelected = false;
//		role.setTemplates(null);			
//		role.setRegions(new ArrayList());			
		ITree tree = (ITree)sessionBean.getReferenceEntitiesEditMode().get(REGION_FIELD);
		// check root elements
		if( request.getParameter(TEMPLATE_CARD_FIELD + ROOT_ID) != null ) {
			isTemplateRootSelected = true;
//			role.setTemplates(new ArrayList());			
		}
		
		if( request.getParameter(REGION_FIELD + ROOT_ID) != null ) {
			isRegionRootSelected = true;
			tree.selectAll();
		} else {
			clearTree(tree, tree.getRoot());			
		}

		if (!isTemplateRootSelected || !isRegionRootSelected) {
			Map params = request.getParameterMap();	    
		    for( Iterator itert = params.keySet().iterator(); itert.hasNext(); ) {
	            String key = (String)itert.next();
	            
	            if (key.startsWith(TEMPLATE_CARD_FIELD) && !isTemplateRootSelected) {
//	            	Collection template = role.getTemplates();
//	            	if (template  == null) {
//	            		template = new ArrayList();
//	            		role.setTemplates(template);
//	            	}
//	            	template.add(request.getParameter(key).trim());
	            }
	            
	            if (key.startsWith(REGION_FIELD) && !isRegionRootSelected) {
					tree.select(request.getParameter(key).trim());            		    	
	            }
		    }			
		}
	}	

	private void clearTree(ITree tree, ITreeNode node) {
		if (tree.isExpanded(node.getId())) {
			tree.unSelect(node.getId());									
			for (Iterator it = node.getChildren().iterator(); it.hasNext();) {
				TreeNode childrenNode = (TreeNode) it.next();
				tree.unSelect(childrenNode.getId());
				clearTree(tree, childrenNode);
			}
		}		
	}
	
	
	private void treeActionHandler(ActionRequest request) {
		UserAdminPortletSessionBean sessionBean = getSessionBean(request);
		
		fillRole(request);
	
		String nodeId = request.getParameter(ATTR_ID_FIELD);
		String action = request.getParameter(ACTION_FIELD);
		
		ITree tree = (ITree)sessionBean.getReferenceEntitiesEditMode().get(REGION_FIELD);
		
		if(action.equals(TREE_COLLAPSE_ACTION)) {
			tree.collapse(nodeId);
		} else {
			tree.expand(nodeId);			
		}

	}
	
	
	private void getUserListHandler(ActionRequest request, ActionResponse response) {
		UserAdminPortletSessionBean sessionBean = getSessionBean(request);
		// search template field
		if( request.getParameter(SEARCH_TEMPLATE_ID_FIELD) != null ) {
			sessionBean.setSearchTemplate(request.getParameter(SEARCH_TEMPLATE_ID_FIELD));
		}
// DB		
		List userList = new ArrayList();
		try {
			userList = (List)sessionBean.getServiceBean(request).filter(Person.class, new UserIdFilter(sessionBean.getSearchTemplate()));			
		} catch (Exception e) {
			String msg = getPortletConfig().getResourceBundle(request.getLocale()).getString("user.form.db.side.error.msg");
			response.setRenderParameter(MSG_PARAM_NAME, msg + e.getMessage());
		}
		sessionBean.setUserList(userList);
	}

	
	private void getUserRoleHandler(boolean isEdit, ActionRequest request, ActionResponse response) {

		UserAdminPortletSessionBean sessionBean = getSessionBean(request);
		
		fillUser(request);
		
		sessionBean.setDetailViewMode(false);
		sessionBean.setEditMode(true);

		// init ref
		try {
			sessionBean.setRoleTypes(getSessionBean(request).getServiceBean(request).listAll(SystemRole.class));
		} catch (Exception e) {
			logger.error("Couldn't get system role list", e);
			sessionBean.setRoleTypes(new ArrayList(0));
		}
		
		ArrayList template = new ArrayList();
		Collection regionList = new ArrayList();

//		 DB		
		try {
			// load ref
			regionList = sessionBean.getServiceBean(request).listChildren(Reference.ID_REGION, ReferenceValue.class);
			template = (ArrayList)sessionBean.getServiceBean(request).listAll(Template.class);
		} catch (Exception e) {
			logger.error("Couldn't get region list", e);
			String msg = getPortletConfig().getResourceBundle(request.getLocale()).getString("user.form.db.side.error.msg");
			response.setRenderParameter(MSG_PARAM_NAME, msg + e.getLocalizedMessage());
		}
		sessionBean.getReferenceEntitiesEditMode().put(TEMPLATE_CARD_FIELD, template);
		sessionBean.getReferenceEntitiesEditMode().put(REGION_FIELD, buildTree(regionList));

		Role userRole = new Role();
		userRole.setSystemRole(null);
//		userRole.setRegions(new ArrayList());
//		userRole.setTemplates(null);

		if (isEdit) {
			if (request.getParameter(ATTR_ID_FIELD) != null ) {
				String roleId = request.getParameter(ATTR_ID_FIELD).trim();
//				 DB		
				try {
					ObjectId roleObjId = new ObjectId(Role.class, Long.parseLong(roleId));
					userRole = (Role)sessionBean.getServiceBean(request).getById(roleObjId);
					prepareRoleForView(userRole, sessionBean);
					
				} catch (Exception e) {
					String msg = getPortletConfig().getResourceBundle(request.getLocale()).getString("user.form.db.side.error.msg");
					response.setRenderParameter(MSG_PARAM_NAME, msg + e.getLocalizedMessage());
				}
			}			
		} else {
			userRole.setSystemRole(sessionBean.getSystemRole(Role.MANAGER_1));
		}
		sessionBean.setRole(userRole);
	}	

	private ITree buildTree(Collection nodeList) {
		
		ITree tree = new Tree();
		tree.setSingleSelectionMode(false);

		ResourceBundle resourceBundleRu = getPortletConfig().getResourceBundle(new Locale("ru"));
		ResourceBundle resourceBundleEn = getPortletConfig().getResourceBundle(new Locale("en"));
		
		String rootKey = "region.tree.root.name";
		ReferenceValue rootObj = new ReferenceValue();
		rootObj.setId(ROOT_ID);
		rootObj.setValueEn(resourceBundleEn.getString(rootKey));
		rootObj.setValueRu(resourceBundleRu.getString(rootKey));
		
		
		ITreeNode root = new TreeNode(REGION_FIELD + ROOT_ID, "root", TREE_CONSTANT_VALUE_TYPE);
		root.setObject(rootObj);
        tree.setRoot(root);
        tree.expand(root.getId());
		
		for (Iterator iter = nodeList.iterator(); iter.hasNext();) {
			ReferenceValue reference = (ReferenceValue) iter.next();
			String nodeKey = REGION_FIELD + reference.getId().getId(); 
			ITreeNode node = new TreeNode(nodeKey, reference.getValue(), TREE_VARIABLE_VALUE_TYPE);
			node.setObject(reference);
			root.addChild(node);
			if (reference.getChildren() != null && !reference.getChildren().isEmpty()) {
				fillChildrenNode(node, reference.getChildren());
			}
		}
		return tree;
	}
	
	private void fillChildrenNode(ITreeNode parent, Collection childrenNodeList) {
		for (Iterator iter = childrenNodeList.iterator(); iter.hasNext();) {
			ReferenceValue reference = (ReferenceValue) iter.next();
			String nodeKey = REGION_FIELD + reference.getId().getId(); 
			ITreeNode node = new TreeNode(nodeKey, reference.getValue(), TREE_VARIABLE_VALUE_TYPE);
			node.setObject(reference);
			parent.addChild(node);
			if (reference.getChildren() != null && !reference.getChildren().isEmpty()) {
				fillChildrenNode(node, reference.getChildren());
			}
		}		
	}
		
	private void getUserHandler(ActionRequest request, ActionResponse response) {
		UserAdminPortletSessionBean sessionBean = getSessionBean(request);
		// search template field
		if( request.getParameter(SEARCH_TEMPLATE_ID_FIELD) != null ) {
			sessionBean.setSearchTemplate(request.getParameter(SEARCH_TEMPLATE_ID_FIELD));
		}
		if (request.getParameter(SELECT_USER_ID_FIELD) != null ) {
			String userId = request.getParameter(SELECT_USER_ID_FIELD).trim();
			
			Collection departmentList = new ArrayList();
			Person user = null;
//			 DB		
			try {
				// load ref
				departmentList = sessionBean.getServiceBean(request).listChildren(Reference.ID_DEPARTMENT, ReferenceValue.class);
				// load user
				user = (Person)sessionBean.getServiceBean(request).getById(new ObjectId(Person.class, Long.parseLong(userId)));
				// lock obj DB
				sessionBean.getServiceBean(request).doAction(new LockObject(user));
				sessionBean.setDetailViewMode(true);
				sessionBean.setEditMode(false);
			} catch (Exception e) {
				String msg = getPortletConfig().getResourceBundle(request.getLocale()).getString("user.form.db.side.error.msg");
				response.setRenderParameter(MSG_PARAM_NAME, msg + e.getLocalizedMessage());
			}

			sessionBean.getReferenceEntitiesEditMode().put(DEPARTMENT_FIELD, parseDepartmentTree2List(departmentList));
			sessionBean.setUser(user);
			
		}
	}
	
	private List parseDepartmentTree2List(Collection departmentTree) {
		List departmentList = new ArrayList();
		for (Iterator iter = departmentTree.iterator(); iter.hasNext();) {
			ReferenceValue department = (ReferenceValue) iter.next();
			if (department.isActive()) {
				departmentList.add(department);
				if (department.getChildren() != null && !department.getChildren().isEmpty()) {
					fillDepartmentChildren(departmentList, department.getChildren());
				}
			}
		}
		return departmentList;
	} 
	
	private void fillDepartmentChildren(List departmentList, Collection departmentChildrenList) {
		for (Iterator iter = departmentChildrenList.iterator(); iter.hasNext();) {
			ReferenceValue department = (ReferenceValue) iter.next();
			if (department.isActive()) {
				departmentList.add(department);
				if (department.getChildren() != null && !department.getChildren().isEmpty()) {
					fillDepartmentChildren(departmentList, department.getChildren());
				}
			}
		}		
	}
	
	private void removeUserRoleHandler(ActionRequest request, ActionResponse response) {
		UserAdminPortletSessionBean sessionBean = getSessionBean(request);
		if (request.getParameter(ATTR_ID_FIELD) != null ) {
			String roleId = request.getParameter(ATTR_ID_FIELD).trim();
//			 DB		
			try {
				ObjectId roleObjId = new ObjectId(Role.class, Long.parseLong(roleId));
				sessionBean.getServiceBean(request).deleteObject(roleObjId);
				// reload
				Person user = sessionBean.getUser();
				Collection roles = sessionBean.getServiceBean(request).listChildren(user.getId(), Role.class);
				user.setRoles(roles);
				String msg = getPortletConfig().getResourceBundle(request.getLocale()).getString("user.form.remove.success.msg");
				response.setRenderParameter(MSG_PARAM_NAME, msg);
			} catch (Exception e) {
				String msg = getPortletConfig().getResourceBundle(request.getLocale()).getString("user.form.db.side.error.msg");
				response.setRenderParameter(MSG_PARAM_NAME, msg + e.getLocalizedMessage());
			}
		}
	}

	private void prepareRoleForView(Role role, UserAdminPortletSessionBean sessionBean) {
//		Collection templates = role.getTemplates();
//		if (templates != null) {
//			ArrayList templateList = new ArrayList();
//			for (Iterator iter = templates.iterator(); iter.hasNext();) {
//				Template ref = (Template) iter.next();
//				templateList.add(TEMPLATE_CARD_FIELD + ref.getId().getId());
//			}
//			role.setTemplates(templateList);
//		}
//
//		Collection regions = role.getRegions();
//		ITree tree = (ITree)sessionBean.getReferenceEntitiesEditMode().get(REGION_FIELD);
//		if (regions == null || regions.isEmpty()) {
//			tree.selectAll();
//		} else {
//			for (Iterator iter = regions.iterator(); iter.hasNext();) {
//				ReferenceValue reference = (ReferenceValue) iter.next();
//				String nodeKey = REGION_FIELD + reference.getId().getId(); 
//				tree.select(nodeKey);
//			}	
//		}		
	}	

	private void prepareRoleForStore(Role role, UserAdminPortletSessionBean sessionBean) {
//		Collection templates = role.getTemplates();
//		if (templates != null && !templates.isEmpty()) {
//			ArrayList templateList = new ArrayList();
//			for (Iterator iter = templates.iterator(); iter.hasNext();) {
//				String attrId = iter.next().toString();
//				long refId = Long.parseLong(attrId.substring(TEMPLATE_CARD_FIELD.length()));
//				Template ref = new Template();
//				ref.setId(refId);
//				templateList.add(ref);
//			}
//			role.setTemplates(templateList);
//		}
		
		ITree tree = (ITree)sessionBean.getReferenceEntitiesEditMode().get(REGION_FIELD);
		if (tree.isSelected(tree.getRoot().getId())) {
//			role.setRegions(new ArrayList());
		} else {
			ArrayList regionList = new ArrayList();
			Set selectedNodes = tree.getSelectedNodes();
			for (Iterator iterator = selectedNodes.iterator(); iterator.hasNext();) {
				TreeNode node = (TreeNode) iterator.next();
				String nodeId = node.getId();
				String id = nodeId.substring(nodeId.lastIndexOf("_") + 1);
				ReferenceValue value = new ReferenceValue();
				value.setId(Long.parseLong(id));
				regionList.add(value);
			}
//			role.setRegions(regionList);
			
		}
	}

	/**
	 * Get SessionBean.
	 * 
	 * @param request PortletRequest
	 * @return UserAdminPortletSessionBean
	 */
	private static UserAdminPortletSessionBean getSessionBean(PortletRequest request) {
		PortletSession session = request.getPortletSession();
		if( session == null )
			return null;
		UserAdminPortletSessionBean sessionBean = (UserAdminPortletSessionBean)session.getAttribute(SESSION_BEAN);
		if( sessionBean == null ) {
			sessionBean = new UserAdminPortletSessionBean();
			session.setAttribute(SESSION_BEAN,sessionBean);
			List userList = new ArrayList();
			try {
				userList = (List)sessionBean.getServiceBean(request).listAll(Person.class);			
			} catch (Exception e) {
			}
			sessionBean.setUserList(userList);
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
