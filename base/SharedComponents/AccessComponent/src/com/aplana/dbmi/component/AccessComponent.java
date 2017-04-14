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
package com.aplana.dbmi.component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import com.aplana.dbmi.model.AccessListItem;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.Reference;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.Role;
import com.aplana.dbmi.model.filter.UserIdFilter;
import com.aplana.dbmi.service.DataServiceBean;

public class AccessComponent {

	public static final String SELECTED_LIST_JSP      = "SelectedListAccessComponent";         // JSP file name to be rendered on the view mode
	
        public static final String ACCESS_HANDLER  = "AccessComponentHandler";  // Bean name for the portlet session
        public static final String PERSON_HANDLER  = "PersonComponentHandler";  // Bean name for the portlet session
        public static final String CURRENT_HANDLER  = "CurrentAccessComponentHandler";  // Bean name for the portlet session
	public static final String MSG_PARAM_NAME  = "AC_MSG_PARAM";  
	
	// GUI action
	public static final String ADD_ACTION  		= "AC_ADD_ACTION";     
	public static final String REMOVE_ACTION  	= "AC_REMOVE_ACTION";     
	public static final String FIND_ACTION  	= "AC_FIND_ACTION";     
	public static final String STORE_ACTION  	= "AC_STORE_ACTION";     
	public static final String BACK_ACTION  	= "AC_BACK_ACTION";     
	
	public static final String EDIT_DEPARTMENT_ACCESS_ACTION  	= "AC_EDIT_DEPARTMENT_ACCESS_ACTION";     
        public static final String EDIT_INDIVIDUAL_ACCESS_ACTION        = "AC_EDIT_INDIVIDUAL_ACCESS_ACTION";     
        public static final String EDIT_PERSON_ACTION        = "AC_EDIT_PERSON_ACTION";     
	public static final String TREE_EXPAND_ACTION  	= "AC_TREE_EXPAND_ACTION";     
	public static final String TREE_COLLAPSE_ACTION  	= "AC_TREE_COLLAPSE_ACTION";     
	
	// GUI field
	public static final String ACTION_FIELD 		= "AC_ACTION_FIELD";     
	public static final String ID_FIELD 		= "AC_ID_FIELD";     
	public static final String SEARCH_FIELD 		= "AC_SEARCH_FIELD";     

	public static final String ADD_ITEM_ID_FIELD 		= "AC_ADD_ITEM_ID_FIELD_";     
	public static final String REMOVE_ITEM_ID_FIELD 		= "AC_REMOVE_ITEM_ID_FIELD_";     
	
	// ACCESS TYPE ID agree for ResourceBundle !!!
	public static final String ALL_ACCESS_TYPE_ID  = "access.type.all";  
	public static final String MANGER_1_ACCESS_TYPE_ID  = "access.type.manager1";  
	public static final String MANAGER_2_ACCESS_TYPE_ID  = "access.type.manager2";  
	public static final String DEPARTMENT_ACCESS_TYPE_ID  = "access.type.department";  
	public static final String INDIVIDUAL_ACCESS_TYPE_ID  = "access.type.individual";  

//	private Tree accessTree = null;
	private List accessDepartmentList = null;
	private List accessIndividualList = null;

	private List selectedDepartmentList = null;
	private List selectedIndividualList = null;
	
	private List departmentList = null;
	private List userList = null;
	
	private List accessItemList = null;
	
	
	private boolean isDepartmentEdit = true;
	
	private boolean isAccessHandlerAction = false;
	
	private String searchTemplate = "";
	private int listSize = 20;

	private DataServiceBean serviceBean = null;
	
	private String formName = "";

	private boolean isAllAccess = false;
	private boolean isManager1 = false;
	private boolean isManager2 = false;
	private boolean isDepartment = false;
	private boolean isIndividual = false;
  
        private String attributeId;
	
	public boolean isAllAccess() {
		return isAllAccess;
	}

	public void setAllAccess(boolean isAllAccess) {
		this.isAllAccess = isAllAccess;
	}

	public boolean isDepartment() {
		return isDepartment;
	}

	public void setDepartment(boolean isDepartment) {
		this.isDepartment = isDepartment;
	}

	public boolean isIndividual() {
		return isIndividual;
	}

	public void setIndividual(boolean isIndividual) {
		this.isIndividual = isIndividual;
	}

	public boolean isManager1() {
		return isManager1;
	}

	public void setManager1(boolean isManager1) {
		this.isManager1 = isManager1;
	}

	public boolean isManager2() {
		return isManager2;
	}

	public void setManager2(boolean isManager2) {
		this.isManager2 = isManager2;
	}

	public AccessComponent(DataServiceBean serviceBean, List accessItemList, String formName) {
		this.serviceBean = serviceBean;
		this.formName = formName;
		this.accessItemList = accessItemList;
		init();		
	}
	
	private void init() {

		isAllAccess = false;
		isManager1 = false;
		isManager2 = false;
		isDepartment = false;
		isIndividual = false;

		accessDepartmentList = new ArrayList();
		accessIndividualList = new ArrayList();
		
		if (accessItemList == null || accessItemList.isEmpty()) {
			isAllAccess = true;
		} else {
			for (Iterator iter = accessItemList.iterator(); iter.hasNext();) {
				AccessListItem element = (AccessListItem) iter.next();
				if (element.getType() == AccessListItem.TYPE_DEPARTMENT) {
					isDepartment =true;
					accessDepartmentList.add(element.getDepartment());
				} else if (element.getType() == AccessListItem.TYPE_PERSON) {
					isIndividual = true;
					accessIndividualList.add(element.getPerson());
				} else if (element.getType() == AccessListItem.TYPE_ROLE) {
					if (element.getRoleType().equals(Role.MANAGER_1)) {
						isManager1 = true;
					} else {
						isManager2 = true;
					}
				}
			}
		}
	}
	
/*	
	private void init() {
		this.accessTree = buildTree();
		if (accessItemList == null) {
			accessTree.select(ALL_ACCESS_TYPE_ID);
		} else {
			for (Iterator iter = accessItemList.iterator(); iter.hasNext();) {
				AccessListItem element = (AccessListItem) iter.next();
				if (element.getType() == AccessListItem.TYPE_NONE) {
					accessTree.select(ALL_ACCESS_TYPE_ID);
				} else if (element.getType() == AccessListItem.TYPE_DEPARTMENT) {
					accessTree.select(DEPARTMENT_ACCESS_TYPE_ID);
					if (accessDepartmentList == null) {
						accessDepartmentList = new ArrayList();
					}
					accessDepartmentList.add(element.getDepartment());
				} else if (element.getType() == AccessListItem.TYPE_PERSON) {
					accessTree.select(INDIVIDUAL_ACCESS_TYPE_ID);
					if (accessIndividualList == null) {
						accessIndividualList = new ArrayList();
					}
					accessIndividualList.add(element.getPerson());
				} else if (element.getType() == AccessListItem.TYPE_ROLE) {
					if (element.getRoleType().equals(Role.MANAGER_1)) {
						accessTree.select(MANGER_1_ACCESS_TYPE_ID);
					} else {
						accessTree.select(MANAGER_2_ACCESS_TYPE_ID);
					}
				}
			}
		}
	}
*/
	public List getAccessItemList() {
		accessItemList = new ArrayList();
		if (!isAllAccess) {
			if (isDepartment) {
				for (Iterator iterator = getAccessDepartmentList().iterator(); iterator.hasNext();) {
					AccessListItem item = new AccessListItem();
					item.setDepartment((ReferenceValue) iterator.next());
					accessItemList.add(item);
				}
			} 
			if (isIndividual) {
				for (Iterator iterator = getAccessIndividualList().iterator(); iterator.hasNext();) {
					AccessListItem item = new AccessListItem();
					item.setPerson((Person) iterator.next());
					accessItemList.add(item);
				}
			} 
			if (isManager1) {
				AccessListItem item = new AccessListItem();
				item.setRoleType(Role.MANAGER_1);	
				accessItemList.add(item);
			} 
			if (isManager2) {
				AccessListItem item = new AccessListItem();
				item.setRoleType(Role.MANAGER_2);	
				accessItemList.add(item);
			}
		}
		return this.accessItemList;
	}
	
/*	
	public List getAccessItemList() {
	
		if (accessItemList == null) {
			accessItemList = new ArrayList();
		}
		for (Iterator iter = accessTree.getSelectedNodes().iterator(); iter.hasNext();) {
			ITreeNode element = (ITreeNode) iter.next();
			if (element.getId().equalsIgnoreCase(ALL_ACCESS_TYPE_ID)) {
			} else if (element.getId().equalsIgnoreCase(DEPARTMENT_ACCESS_TYPE_ID)) {
				for (Iterator iterator = getAccessDepartmentList().iterator(); iterator.hasNext();) {
					AccessListItem item = new AccessListItem();
					item.setDepartment((ReferenceValue) iterator.next());
					accessItemList.add(item);
				}
			} else if (element.getId().equalsIgnoreCase(INDIVIDUAL_ACCESS_TYPE_ID)) {
				for (Iterator iterator = getAccessIndividualList().iterator(); iterator.hasNext();) {
					AccessListItem item = new AccessListItem();
					item.setPerson((Person) iterator.next());
					accessItemList.add(item);
				}
			} else if (element.getId().equalsIgnoreCase(MANGER_1_ACCESS_TYPE_ID)) {
				AccessListItem item = new AccessListItem();
				item.setRoleType(Role.MANAGER_1);	
				accessItemList.add(item);
			} else if (element.getId().equalsIgnoreCase(MANAGER_2_ACCESS_TYPE_ID)) {
				AccessListItem item = new AccessListItem();
				item.setRoleType(Role.MANAGER_2);	
				accessItemList.add(item);
			}
		}
		return this.accessItemList;
	}
*/	
	public void parseRequest(ActionRequest request, ActionResponse response) {
		if (!isAccessHandlerAction) {
			parseRequest2Model(request);
		}

		String action = request.getParameter(ACTION_FIELD); 
		if( action != null ) {
			if(action.equals(ADD_ACTION)) {
				addActionHandler(request);
			}
			if(action.equals(REMOVE_ACTION)) {
				removeActionHandler(request);
			}
			if(action.equals(FIND_ACTION)) {
				findActionHandler(request, response);
			}
			if(action.equals(STORE_ACTION)) {
				storeActionHandler(request, response);
			}
			if(action.equals(BACK_ACTION)) {
				isAccessHandlerAction = false;
			}
			if(action.equals(EDIT_DEPARTMENT_ACCESS_ACTION)) {
				accessEditHandler(response, true);
			}
			if(action.equals(EDIT_INDIVIDUAL_ACCESS_ACTION) || action.equals(EDIT_PERSON_ACTION)) {
				accessEditHandler(response, false);
			}
/*			
			if(action.equals(TREE_EXPAND_ACTION)) {
				ITree tree = getAccessTree();
				tree.expand(tree.getRoot().getId());
			}
			if(action.equals(TREE_COLLAPSE_ACTION)) {
				ITree tree = getAccessTree();
				tree.collapse(tree.getRoot().getId());
			}
*/			
		}
	}
	
	private void addActionHandler(ActionRequest request) {
		Map params = request.getParameterMap();	    
    	List outList = null;
    	List inList = null;
    	if (isDepartmentEdit) {
        	outList = getDepartmentList();
        	inList = getSelectedDepartmentList();
    	} else {
        	outList = getUserList();
        	inList = getSelectedIndividualList();
    	}
    	
		for( Iterator itert = params.keySet().iterator(); itert.hasNext(); ) {
            String key = (String)itert.next();
            if (key.startsWith(ADD_ITEM_ID_FIELD)) {
            	String itemId = request.getParameter(key).trim();
            	String id = "";
            	boolean isPresent = false;
            	for (Iterator iter = inList.iterator(); iter.hasNext();) {
                	if (isDepartmentEdit) {
                		id = ((ReferenceValue) iter.next()).getId().getId().toString();
                	} else {
                		id = ((Person) iter.next()).getId().getId().toString();
                	}
                	if (itemId.equalsIgnoreCase(id)) {
                		isPresent = true;
                	}
            	}
            	if (!isPresent) {
                	for (Iterator iter = outList.iterator(); iter.hasNext();) {
                		Object item = iter.next();
                    	if (isDepartmentEdit) {
                    		id = ((ReferenceValue) item).getId().getId().toString();
                    	} else {
                    		id = ((Person) item).getId().getId().toString();
                    	}
                    	if (itemId.equalsIgnoreCase(id)) {
                    		inList.add(item);
                    		break;
                    	}
                	}
            	}
            }
	    }			
	}

	private void removeActionHandler(ActionRequest request) {
		Map params = request.getParameterMap();	    
		List inList = null;
		List outList = new ArrayList();
		
    	if (isDepartmentEdit) {
        	inList = getSelectedDepartmentList();
    	} else {
        	inList = getSelectedIndividualList();
    	}
		for( Iterator itert = params.keySet().iterator(); itert.hasNext(); ) {
            String key = (String)itert.next();
            if (key.startsWith(REMOVE_ITEM_ID_FIELD)) {
            	String itemId = request.getParameter(key).trim();
            	String id = "";
            	for (Iterator iter = inList.iterator(); iter.hasNext();) {
            		Object item = iter.next();
                	if (isDepartmentEdit) {
                		id = ((ReferenceValue) item).getId().getId().toString();
                	} else {
                		id = ((Person) item).getId().getId().toString();
                	}
                	if (!itemId.equalsIgnoreCase(id)) {
                		outList.add(item);
                	}
            	}
            	inList = outList;
            	outList = new ArrayList();
            }
	    }
		
    	if (isDepartmentEdit) {
        	selectedDepartmentList = inList;
    	} else {
        	selectedIndividualList = inList;
    	}
		
	}
/*
	private void storeActionHandler(ActionRequest request, ActionResponse response) {
		
		if (isDepartmentEdit && !getAccessDepartmentList().isEmpty()) {			
			accessTree.select(DEPARTMENT_ACCESS_TYPE_ID);
		} 
		if (!isDepartmentEdit && !getAccessIndividualList().isEmpty()) {
			accessTree.select(INDIVIDUAL_ACCESS_TYPE_ID);
		}
	}
*/
	private void storeActionHandler(ActionRequest request, ActionResponse response) {
		if (isDepartmentEdit) {			
			isDepartment = true;
			accessDepartmentList = new ArrayList();
			for (Iterator iter = selectedDepartmentList.iterator(); iter.hasNext();) {
				accessDepartmentList.add(iter.next());
			}

		} else {
			isIndividual = true;
			accessIndividualList = new ArrayList();
			for (Iterator iter = selectedIndividualList.iterator(); iter.hasNext();) {
				accessIndividualList.add(iter.next());
			}
		}		
	}
	
	private void accessEditHandler( ActionResponse response, boolean isDepartmentAccess) {
//		this.isAccessHandlerAction = true;
		isDepartmentEdit = isDepartmentAccess;
		
		this.userList = new ArrayList();
		this.departmentList = new ArrayList();
		this.selectedDepartmentList = new ArrayList();
		this.selectedIndividualList = new ArrayList();
		
		try {
			if (isDepartmentEdit) {
				
				departmentList = (List)serviceBean.listChildren(Reference.ID_DEPARTMENT, ReferenceValue.class);
				for (Iterator iter = accessDepartmentList.iterator(); iter.hasNext();) {
					getSelectedDepartmentList().add(iter.next());
				}
			} else {
				userList = (List)serviceBean.listAll(Person.class);
				for (Iterator iter = accessIndividualList.iterator(); iter.hasNext();) {
					getSelectedIndividualList().add(iter.next());
				}
			}
		} catch (Exception e) {
			response.setRenderParameter(MSG_PARAM_NAME, e.getMessage());
		}
	}	

	private void findActionHandler(ActionRequest request, ActionResponse response) {

            try {
			String template = request.getParameter(SEARCH_FIELD);
			userList = (List) serviceBean.filter(Person.class, new UserIdFilter(template));
		} catch (Exception e) {
			response.setRenderParameter(MSG_PARAM_NAME, e.getMessage());
		}

	}
	
	private void parseRequest2Model(ActionRequest request) {
		boolean reset = false;
		isAllAccess = reset;
		isManager1 = reset;
		isManager2 = reset;
		isDepartment = reset;
		isIndividual = reset;
		String accessTypeId = request.getParameter(ALL_ACCESS_TYPE_ID);
		if( accessTypeId != null ) {
			isAllAccess = true;
		} else {
			accessTypeId = request.getParameter(MANGER_1_ACCESS_TYPE_ID);
			if( accessTypeId != null ) {
				isManager1 =true;
			}
			accessTypeId = request.getParameter(MANAGER_2_ACCESS_TYPE_ID);
			if( accessTypeId != null ) {
				isManager2 = true;
			}	
			accessTypeId= request.getParameter(DEPARTMENT_ACCESS_TYPE_ID);
			if( accessTypeId != null ) {
				isDepartment = true;
			}
			accessTypeId = request.getParameter(INDIVIDUAL_ACCESS_TYPE_ID);
			if( accessTypeId != null ) {
				isIndividual = true;
			}
		}
		
	} 
	
/*	
	// fill access tree
	private void parseRequest2Model(ActionRequest request) {
		ITree tree = getAccessTree();
		if (tree.isExpanded(tree.getRoot().getId())) {
			tree.unSelectAll();
			String childAccessTypeId = request.getParameter(MANGER_1_ACCESS_TYPE_ID);
			if( childAccessTypeId != null ) {
				tree.select(childAccessTypeId);
			}
			childAccessTypeId = request.getParameter(MANAGER_2_ACCESS_TYPE_ID);
			if( childAccessTypeId != null ) {
				tree.select(childAccessTypeId);
			}
			childAccessTypeId = request.getParameter(DEPARTMENT_ACCESS_TYPE_ID);
			if( childAccessTypeId != null ) {
				tree.select(childAccessTypeId);
			}
			childAccessTypeId = request.getParameter(INDIVIDUAL_ACCESS_TYPE_ID);
			if( childAccessTypeId != null ) {
				tree.select(childAccessTypeId);
			}
		} else {
			tree.unSelect(tree.getRoot().getId());
		}
		String accessTypeId = request.getParameter(ALL_ACCESS_TYPE_ID);
		if( accessTypeId != null ) {
			tree.select(accessTypeId);
		}
		
	} 
	
	private Tree buildTree() {
		
		Tree tree = new Tree();
		tree.setSingleSelectionMode(false);
		
		ITreeNode root = new TreeNode(ALL_ACCESS_TYPE_ID, ALL_ACCESS_TYPE_ID);
		tree.setRoot(root);
        tree.expand(root.getId());

        ITreeNode node = new TreeNode(MANGER_1_ACCESS_TYPE_ID, MANGER_1_ACCESS_TYPE_ID);
        root.addChild(node);
        node = new TreeNode(MANAGER_2_ACCESS_TYPE_ID, MANAGER_2_ACCESS_TYPE_ID);
        root.addChild(node);
        node = new TreeNode(DEPARTMENT_ACCESS_TYPE_ID, DEPARTMENT_ACCESS_TYPE_ID);
        root.addChild(node);
        node = new TreeNode(INDIVIDUAL_ACCESS_TYPE_ID, INDIVIDUAL_ACCESS_TYPE_ID);
        root.addChild(node);

        
        return tree;
	}
*/	
	
	
	public String getSearchTemplate() {
		return searchTemplate;
	}


	public void setSearchTemplate(String searchTemplate) {
		this.searchTemplate = searchTemplate;
	}


	public List getDepartmentList() {
		if (departmentList == null) departmentList = new ArrayList();
		return  this.departmentList ;
	}


	public void setDepartmentList(List departmentList) {
		this.departmentList = departmentList;
	}


	public boolean isDepartmentEdit() {
		return isDepartmentEdit;
	}


	public void setDepartmentEdit(boolean isDepartmentEdit) {
		this.isDepartmentEdit = isDepartmentEdit;
	}


	public List getAccessDepartmentList() {
		if (accessDepartmentList  == null) accessDepartmentList = new ArrayList(); 
		return  this.accessDepartmentList;
	}

	public List getAccessIndividualList() {
		if (accessIndividualList  == null ) accessIndividualList  = new ArrayList() ; 
		return this.accessIndividualList;
	}



	public List getUserList() {
		if (userList  == null ) userList  = new ArrayList();  
		return this.userList;
	}


	public void setUserList(List userList) {
		this.userList = userList;
	}

/*
	public ITree getAccessTree() {
		return accessTree;
	}


	public void setAccessTree(Tree accessTree) {
		this.accessTree = accessTree;
	}

*/
	public int getListSize() {
		return listSize;
	}


	public void setListSize(int listSize) {
		this.listSize = listSize;
	}


	public void setAccessHandlerAction(boolean isAccessHandlerAction) {
        this.isAccessHandlerAction = isAccessHandlerAction;
    }

    public boolean isAccessHandlerAction() {
		return isAccessHandlerAction;
	}

	public String getFormName() {
		return formName;
	}

	public List getSelectedDepartmentList() {
		if (selectedDepartmentList  == null) selectedDepartmentList = new ArrayList(); 
		return  this.selectedDepartmentList;
	}

	public void setSelectedDepartmentList(List selectedDepartmentList) {
		this.selectedDepartmentList = selectedDepartmentList;
	}

	public List getSelectedIndividualList() {
		if (selectedIndividualList  == null) selectedIndividualList = new ArrayList(); 
		return  this.selectedIndividualList;
	}

	public void setSelectedIndividualList(List selectedIndividualList) {
		this.selectedIndividualList = selectedIndividualList;
	}

	public void setAccessItemList(List accessItemList) {
		this.accessItemList = accessItemList;
		init();
	}

    public String getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(String attributeId) {
        this.attributeId = attributeId;
    }
	
}
