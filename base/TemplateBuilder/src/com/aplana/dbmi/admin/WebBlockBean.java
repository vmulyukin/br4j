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
package com.aplana.dbmi.admin;

import com.aplana.dbmi.action.CheckRolesForUser;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TemplateBlock;
import com.aplana.dbmi.model.web.WebSearchBean;
import com.aplana.dbmi.service.AsyncDataServiceBean;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;
import com.jenkov.prizetags.tree.itf.ITree;

import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

public class WebBlockBean extends TemplateBlock {
    private static final long serialVersionUID = 1L;

    public static final String CLOSE_ACTION = "BLOCKS_CLOSE";

    public static final String SAVE_ACTION = "SAVE";

    public static final String ADD_ATTRIBUTE_ACTION = "ADD_ATTRIBUTE";

    public static final String NEW_LIST_ACTION = "NEW_LIST";

    public static final String LOAD_LIST_ACTION = "LOAD_LIST";

    public static final String PARAM_PREFIX = "tree1_";

    public static final String ADD_REFERENCE_VALUE_ACTION = "ADD_REFERENCE_VALUE";

    public static final String ATTRIBUTE_TYPE_ACTION = "ATTRIBUTE_TYPE";

    public static final String ATTRIBUTE_UP_ACTION = "ATTRIBUTE_UP_ACTION";

    public static final String ATTRIBUTE_DOWN_ACTION = "ATTRIBUTE_DOWN_ACTION";

    public static final String ATTRIBUTE_RIGHT_ACTION = "ATTRIBUTE_RIGHT_ACTION";

    public static final String TREE_NODE_UP_ACTION = "TREE_NODE_UP_ACTION";

    public static final String TREE_NODE_DOWN_ACTION = "TREE_NODE_DOWN_ACTION";

    public static final String TREE_NODE_LEFT_ACTION = "TREE_NODE_LEFT_ACTION";

    public static final String TREE_NODE_RIGHT_ACTION = "TREE_NODE_RIGHT_ACTION";

    private Collection blocks = new ArrayList();

    private List availableAttributes;

    private List availableListAttributes;

    private WebAttribute attribute = new WebAttribute();

    private WebReferenceValue referenceValue = new WebReferenceValue();

    private Collection availableNodes = new ArrayList();

    private String action;

    private String fromAttributeCode;

    private ITree tree;

    private String tmpId;

    private String message;
    
    private WebSearchBean webSearchBean = new WebSearchBean();

    private AsyncDataServiceBean dataService;
    
	private String editAccessRoles;
	private boolean editAccessExists=false;

	public AsyncDataServiceBean getDataService() {
		return dataService;
	}
	
	public void setDataService(AsyncDataServiceBean dataService) {
		this.dataService = dataService;
	}

	public Collection getBlocks() {
        return blocks;
    }

    public void setBlocks(Collection blocks) {
        this.blocks = blocks;
    }

    public Collection getAvailableAttributes() {
        return availableAttributes;
    }

    public void setAvailableAttributes(List availableAttributes) {
        this.availableAttributes = availableAttributes;
    }

    public List getAvailableListAttributes() {
        return availableListAttributes;
    }

    public void setAvailableListAttributes(List availableListAttributes) {
        this.availableListAttributes = availableListAttributes;
    }

    public WebAttribute getAttribute() {
        return attribute;
    }

    public void setAttribute(WebAttribute attribute) {
        this.attribute = attribute;
    }

    public Collection getAvailableNodes() {
        return availableNodes;
    }

    public void setAvailableNodes(Collection availableNodes) {
        this.availableNodes = availableNodes;
    }

    public ITree getTree() {
        return tree;
    }

    public void setTree(ITree tree) {
        this.tree = tree;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getFromAttributeCode() {
        return fromAttributeCode;
    }

    public void setFromAttributeCode(String fromAttributeCode) {
        this.fromAttributeCode = fromAttributeCode;
    }

    public Object getRealId() {
        return super.getId() == null || super.getId().getId() == null ? null : super.getId().getId();
    }

    public void setRealId(Object id) {
        ObjectId objectId = new ObjectId(Template.class, id);
        super.setId(objectId);
    }

    public WebReferenceValue getReferenceValue() {
        return referenceValue;
    }

    public void setReferenceValue(WebReferenceValue referenceValue) {
        this.referenceValue = referenceValue;
    }

    public Boolean getIsListAttribute() {
        return Attribute.TYPE_LIST.equals(this.getAttribute().getType()) || Attribute.TYPE_TREE.equals(this.getAttribute().getType()) ? Boolean.TRUE : Boolean.FALSE;

    }

    public boolean getIsNewListAttribute() {
        return getIsListAttribute().booleanValue() && getAttribute().getRealId().toString().indexOf(WebUtils.ID_PREFIX) >= 0;

    }

    public String getTmpId() {
        return tmpId;
    }

    public void setTmpId(String tmpId) {
        this.tmpId = tmpId;
    }

    public WebSearchBean getWebSearchBean() {
        return webSearchBean;
    }

    public void setWebSearchBean(WebSearchBean webSearchBean) {
        this.webSearchBean = webSearchBean;
    }

    // BDMI00000045 fix 21.05.08 �.�.
    public Collection getAttributes() {
        if (super.getAttributes() == null)
            super.setAttributes(new ArrayList());
        return super.getAttributes();
    }

    // BDMI00000045 fix

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void upAttribute(String code) {
        List attributes = (List) getAttributes();
        int index = getAttributeIndex(code);
        if (index > 0) {
            Attribute tmpAttribute = (Attribute) attributes.remove(index);
            attributes.add(index-1, tmpAttribute);
        }
    }

    public void downAttribute(String code) {
        List attributes = (List) getAttributes();
        int index = getAttributeIndex(code);
        if (index < attributes.size() - 1) {
            Attribute tmpAttribute = (Attribute) attributes.remove(index);
            attributes.add(index+1, tmpAttribute);
        }
    }

    public Attribute getAttribute(String code) {
        Collection attributes = getAttributes();
        return getAttribute(code, attributes);
    }

    private Attribute getAttribute(String code, Collection attributes) {
        if (attributes == null) {
            return null;
        }
        for (Iterator it = attributes.iterator(); it.hasNext();) {
            Attribute attribute = (Attribute) it.next();
            if (code.equals(attribute.getId().getId())) {
                return attribute;
            }
        }
        return null;
    }

    public int getAttributeIndex(String code) {
        Collection attributes = getAttributes();
        return getAttributeIndex(code, attributes);
    }

    private int getAttributeIndex(String code, Collection attributes) {
        if (attributes == null) {
            return -1;
        }
        int index = 0;
        for (Iterator it = attributes.iterator(); it.hasNext(); index++) {
            Attribute attribute = (Attribute) it.next();
            if (code.equals(attribute.getId().getId())) {
                return index;
            }
        }
        return -1;
    }
    
    public void moveAttributeFromAvailableToCurrent(String code) {
        int index = getAttributeIndex(code, availableAttributes);
        Attribute tmpAttribute = (Attribute) availableAttributes.remove(index); 
        getAttributes().add(tmpAttribute);
    }

    public void moveAttributeFromCurrentToAvailable(String code) {
        int index = getAttributeIndex(code);
        Attribute tmpAttribute = (Attribute) ((List) getAttributes()).remove(index); 
        availableAttributes.add(tmpAttribute);
    }
    
    public boolean isNewAttribute() {
        return getAttribute().getRealId().toString().indexOf(WebUtils.ID_PREFIX) >= 0;
    }


    public String getEditAccessRoles() {
		return editAccessRoles;
	}

	public void setEditAccessRoles(String editAccessRoles) {
		this.editAccessRoles = editAccessRoles;
	}

	/**
	 * ��������� ������� � �������� ������������ ���� �� �������������� �������� � ��������.
	 * ����� ���������� ���������� ���������� � ������ ������ � ����� ��� ����, 
	 * ����� ��� ���������/������������ ���� � ������ ������ ���� ��� ����� ������ ���������/��������� ��� ������������ 
	 * @return true - ����� ����, false - ���� ���
	 */
	public boolean isEditAccessExists() throws DataException, ServiceException{
		if (editAccessRoles != null&&!editAccessRoles.isEmpty()){
			CheckRolesForUser checkAction = new CheckRolesForUser();
			checkAction.setPersonLogin(dataService.getUserName());
			checkAction.setRoles(editAccessRoles);
			editAccessExists = (Boolean)dataService.doAction(checkAction);
		} else
			// ���� ��������������� ���� �� ������, �� ����� �� �������������� � �������� ������������ ����
			editAccessExists = true;
		return editAccessExists;
	}
}
