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
package org.aplana.br4j.dynamicaccess;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;

import org.aplana.br4j.dynamicaccess.xmldef.Permission;
import org.aplana.br4j.dynamicaccess.xmldef.Template;
import org.aplana.br4j.dynamicaccess.xmldef.WfMoves;
import org.aplana.br4j.dynamicaccess.xmldef.types.OperationType;
import org.aplana.br4j.dynamicaccess.xmldef.types.PermissionCloner;
import org.aplana.br4j.dynamicaccess.xmldef.types.PermissionWrapper;
import org.aplana.br4j.dynamicaccess.xmldef.types.PermissionWrapper.RuleType;

import com.borland.jbcl.layout.XYConstraints;

/**
 *
 * <p>Title:����� ��� ������� ����� </p>
 *
 * <p>Description: ����� ��������� ���������� ���� ��� �������������� �����(permission) </p>
 *
 * <p>Copyright: Copyright (c) 2011</p>
 *
 * <p>Company: MySoft</p>
 */
public class ObjectPermissionForm extends DialogBase
{
    //������� �������� �� ������ PermissionComponent ��� ������������ ��������� � ��� �������
    PermissionComponent permissionComponent = new PermissionComponent();
    private Template m_template; //���������� ��� �������� ��������� �������
    private Permission m_permission; //���������� ��� �������� ��������� �����(permission)
    private List<PermissionWrapper> permissionList; //���������� ��� �������� ��������� �����(permission)
    
    //stores the rule type of selected Permission
    private RuleType ruleType;
    private boolean isOneRuleSelected;
    
    private String m_state;//���������� ��� �������� ������������ ������ �������

    /**
     * ����� ������������ � ������������� ������ ��� ����������� ���� �������
     * @param owner Frame
     * @param title String
     * @param modal boolean
     */
    public ObjectPermissionForm(Frame owner, String title, boolean modal)
    {
        super(owner, title, modal);
        try {
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            jbInit();
            pack();
            setAutoSize();
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public ObjectPermissionForm(Frame parent, Template template, Permission permission, String role,
    	    String state)
    	    {
    	        this(parent, "�����", true);
    	        m_permission = permission;
    	        m_template = template;
    	        m_state = state;
    	        /* if (m_permission != null) {
    	             m_permissionSet = m_objectPermissionSet.getPermissionSet();
    	         }*/
    	    }

	public ObjectPermissionForm(Frame parent, Template template, List<PermissionWrapper> permissionList,
			RuleType ruleType, String state, boolean isOneRuleSelected) {
        this(parent, "�����", true);

        this.permissionList = permissionList;
        this.m_template = template;
        this.ruleType = ruleType;
        this.m_state = state;
        this.isOneRuleSelected = isOneRuleSelected;
        /* if (m_permission != null) {
             m_permissionSet = m_objectPermissionSet.getPermissionSet();
         }*/
    }

    private void jbInit() throws Exception
    {
        permissionComponent.setBorder(BorderFactory.createEtchedBorder());
        this.addWindowListener(new PermissionForm_this_windowAdapter(this));
        panelInputControls.add(permissionComponent, new XYConstraints(5, 5, -1, -1));
    }

    public boolean onOk()
    {
		for (PermissionWrapper m_permissionWrapper : permissionList) {
	        m_permissionWrapper.setPermission(PermissionCloner.clonePermission(m_permissionWrapper.getPermission()));
	        //m_permissionWrapper.getPermission().setRule(m_permissionWrapper.getRule());
	        //���� ��������� ��� ��������������� ���������� m_state �.�. �� ���� ���� �������� ������������ �������
	        //� ������ ��� ID
	        for (int i = 0; i < m_template.getStatusCount(); i++) {
	            if (m_template.getStatus(i).getName().equals(m_state)) {
	                m_state = m_template.getStatus(i).getStatus_id();
	            }
	        }
	        Permission componentPermission = permissionComponent.getPermission();
	        // ���� ������������� ����� read, �� ������� ��������
	        
	        m_permissionWrapper.getPermission().setWfMoves(componentPermission.getWfMoves());
	        m_permissionWrapper.getPermission().setAttributes(componentPermission.getAttributes());
	        
	        m_permissionWrapper.getPermission().setStatus(m_state);
	        m_permissionWrapper.getPermission().setOperations(componentPermission.getOperations());
	        
    	}        
        return true;
    }

    public Permission getPermission()
    {
//        return m_permission;
        return permissionComponent.getPermission();
    }
    
    public List<PermissionWrapper> getPermissionList() {
		return permissionList;
	}

	public void this_windowOpened(WindowEvent e)
    {
        //permissionComponent.setActions(m_permission.getAttributes());
    	if(isOneRuleSelected && permissionList.size() > 0){
    		if(permissionList.get(0) != null){
    			m_permission = PermissionCloner.clonePermission(permissionList.get(0).getPermission());
    		}
    	} else {
			// if more more then one rule is selected or if permissionList does not contain any Permissions
			// the Edit Permission Dialog should be empty
    		m_permission = new Permission();    		
    	}
    	
        permissionComponent.setPermissionSet(m_permission, m_template, ruleType, m_state);

       /* if (m_template.getPermission() != null) {
            for (int i = 0; i < m_template.getPermissionCount(); i++) {
            }
        }

         if (m_permission != null && m_permission.getCardPermission() != null) {
             rbObjectPermission.setSelected(true);
         }
         else {
             rbNamedPermission.setSelected(true);
             if (m_permission != null) {
             }
         }

         setEnabled();
         */
    }
	
	public void clearPermissionList() {
		getPermissionList().clear();
	}
	
	@Override
    public void btCancel_actionPerformed(ActionEvent e)
    {
        super.btCancel_actionPerformed(e);
        clearPermissionList();
    }

	public void setAllBasePermissions(
			Map<Long, Set<Permission>> allBasePermissions) {
        permissionComponent.setAllBasePermissions(allBasePermissions);
	}
}


class PermissionForm_this_windowAdapter extends WindowAdapter
{
    private ObjectPermissionForm adaptee;
    PermissionForm_this_windowAdapter(ObjectPermissionForm adaptee)
    {
        this.adaptee = adaptee;
    }

    public void windowOpened(WindowEvent e)
    {
        adaptee.this_windowOpened(e);
    }
    
    @Override
    public void windowClosing(WindowEvent e) {
    	//when closing window, no need to edit any Permissions selected
    	adaptee.clearPermissionList();
    }

}
