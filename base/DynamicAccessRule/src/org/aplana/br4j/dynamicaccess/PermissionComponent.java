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
/*����� ���������� �������� ���� � ����������� ��*/
package org.aplana.br4j.dynamicaccess;

import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;

import org.aplana.br4j.dynamicaccess.xmldef.Attribute;
import org.aplana.br4j.dynamicaccess.xmldef.AttributePermissionType;
import org.aplana.br4j.dynamicaccess.xmldef.Attributes;
import org.aplana.br4j.dynamicaccess.xmldef.Operation;
import org.aplana.br4j.dynamicaccess.xmldef.Permission;
import org.aplana.br4j.dynamicaccess.xmldef.Rules;
import org.aplana.br4j.dynamicaccess.xmldef.Template;
import org.aplana.br4j.dynamicaccess.xmldef.WfMove;
import org.aplana.br4j.dynamicaccess.xmldef.WfMoves;
import org.aplana.br4j.dynamicaccess.xmldef.types.Action;
import org.aplana.br4j.dynamicaccess.xmldef.types.OperationType;
import org.aplana.br4j.dynamicaccess.xmldef.types.PermissionWrapper.RuleType;

import com.borland.jbcl.layout.XYConstraints;
import com.borland.jbcl.layout.XYLayout;

public class PermissionComponent extends JPanel {

    JLabel label1 = new JLabel();
    JComboBox cmbPermissions = new JComboBox();
    JList listAllWfMoves = new JList();
    JList listWfMoves = new JList();
    JButton btAddXPermissions = new JButton();
    JButton btDeleteXPermissions = new JButton();
    JList listAllAttribute = new JList();
    JList listAttribute = new JList();
    JButton btAddAction = new JButton();
    JButton btRemoveAction = new JButton();
    XYLayout xYLayout1 = new XYLayout();
    private Permission m_permission;
    private Attributes m_attributes;
    private AttributePermissionType m_attributepermissiontype;
    private Rules m_rules;
    private Template m_template;
    private RuleType ruleType;
    private String m_status;
    JScrollPane jScrollPane1 = new JScrollPane();
    JScrollPane jScrollPane2 = new JScrollPane();
    JScrollPane jScrollPane3 = new JScrollPane();
    JScrollPane jScrollPane4 = new JScrollPane();
	private Map<Long, Set<Permission>> allBasePermissions;

    public PermissionComponent() {
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        this.setLayout(xYLayout1);
        label1.setText("�����");
        btAddAction.setText(">");
        btAddAction.addActionListener(new PermissionComponent_btAddAction_actionAdapter(this));
        btRemoveAction.setText("<");
        btRemoveAction.addActionListener(new PermissionComponent_btRemoveAction_actionAdapter(this));
        xYLayout1.setWidth(740);
        xYLayout1.setHeight(328);
        listAttribute.setBackground(SystemColor.controlLtHighlight);
        listAllWfMoves.setBackground(SystemColor.controlLtHighlight);
        listWfMoves.setBackground(SystemColor.controlLtHighlight);
        listAllAttribute.setBackground(SystemColor.controlLtHighlight);
        btDeleteXPermissions.setText("<");
        btDeleteXPermissions.addActionListener(new PermissionComponent_btDeleteXPermissions_actionAdapter(this));
        btAddXPermissions.setText(">");
        btAddXPermissions.addActionListener(new PermissionComponent_btAddXPermissions_actionAdapter(this));
        
        listAttribute.addMouseListener(new PermissionComponent_listAttribute_actionAdapter(this));
        listAllWfMoves.addMouseListener(new PermissionComponent_listAllWfMoves_actionAdapter(this));
        listAllAttribute.addMouseListener(new PermissionComponent_listAllAttribute_actionAdapter(this));
        listWfMoves.addMouseListener(new PermissionComponent_listWfMoves_actionAdapter(this));
        this.add(cmbPermissions, new XYConstraints(195, 9, 480, 25));
        jScrollPane2.getViewport().add(listAttribute);
        jScrollPane1.getViewport().add(listAllAttribute);
        jScrollPane4.getViewport().add(listWfMoves);
        jScrollPane3.getViewport().add(listAllWfMoves);
        this.add(btAddXPermissions, new XYConstraints(348, 60, 45, 20));
        this.add(btDeleteXPermissions, new XYConstraints(348, 89, 45, 20));
        this.add(btAddAction, new XYConstraints(348, 194, 45, 20));
        this.add(btRemoveAction, new XYConstraints(348, 221, 45, 20));
        this.add(label1, new XYConstraints(10, 10, 100, 20));
        this.add(jScrollPane3, new XYConstraints(10, 44, 332, 127));
        this.add(jScrollPane1, new XYConstraints(10, 182, 332, 137));
        this.add(jScrollPane2, new XYConstraints(400, 182, 328, 137));
        this.add(jScrollPane4, new XYConstraints(400, 44, 328, 127));
    }

    //����� ������������ ������� ������ ���������� ��������� � ����� �������������� �����
    public void btAddXPermissions_actionPerformed(ActionEvent e) {
    	AddElementInListWfMoves();
    }

    private void AddElementInListWfMoves(){
        //�������� ������ �� ������ ��������� ��� �������� ����������
        String xPermission = listAllWfMoves.getSelectedValue().toString();
        //��������� ��� �� ������ � ������ ���� ������� � ��� �� ��� ��� �� �������������� �
        //����������� �������
        if (xPermission != null
                && !existInModel(listWfMoves.getModel(), xPermission)) {
            ((DefaultListModel) listWfMoves.getModel()).addElement(xPermission);
        }
    }
    /**
     * ����� �������� ������ ��������� � ������� �� ���������� ����������� ������
     * @param listModel ListModel - ������ ���������� �����(permission)
     * @param value String - ��������, ������� ����� ��������
     * @return boolean - ������������ �������� - ������� ������� ������
     */
    private boolean existInModel(ListModel listModel, String value) {
        for (int i = 0; i < listModel.getSize(); i++) {
            String itemValue = listModel.getElementAt(i).toString();
            if (value.equalsIgnoreCase(itemValue)) {
                return true;
            }
        }
        return false;
    }

    /**
     * ����� ������������ ������� ������ �������� ��������� �� ������ �����
     * @param e ActionEvent
     */
    public void btDeleteXPermissions_actionPerformed(ActionEvent e) {
    	RemoveElementInListWfMoves();
    }

    private void RemoveElementInListWfMoves(){
        Object xPermission = listWfMoves.getSelectedValue();
        ((DefaultListModel) listWfMoves.getModel()).removeElement(xPermission);    
    }
    /**
     * ����� ������������ ������� ������ ���������� ��������� � ������ �����
     * �� ����� �������������� �����.
     * @param e ActionEvent
     */
    public void btAddAction_actionPerformed(ActionEvent e) {
        AddElementInListAttribute();
    }

    private void AddElementInListAttribute(){
    	//��������� ������ �� ������ ���������
        String action = listAllAttribute.getSelectedValue().toString();
        //��������� ��� �� ������ ���� ������� � ������ �������� � ����� ��� �� ��������������
        //� ������ ��������� �������� � �����
        if (action != null && !existInModel(listAttribute.getModel(), action)) {
            ((DefaultListModel) listAttribute.getModel()).addElement(action);
        }
    }
    /**
     * ����� �������� ��������� �� ������ �����
     * @param e ActionEvent
     */
    public void btRemoveAction_actionPerformed(ActionEvent e) {
    	RemoveElementInListAttribute();
    }

    private void RemoveElementInListAttribute(){
        Object action = listAttribute.getSelectedValue();
        ((DefaultListModel) listAttribute.getModel()).removeElement(action);
    }
    /*    public void setActions(Attributes attributes)
    {
    m_attributes = attributes;
    init();
    }
     */
    /**
     * ����� ������������ ��� ��������� ��������� ����� � ����������� ������������� ��� �������� ���
     * ������ ���������, ��������� � ���� ��������.
     * @param permission Permission - ��������� �����
     * @param template Template - ��������� ������� ��� ���������� ������� ��������� � ���������
     * @param rule String - ������ ���������� ������������ �������
     * @param status String - ������ ���������� �������� ����������� �������
     */
    public void setPermissionSet(Permission permission, Template template, RuleType ruleType, String status) {
        m_template = template; //���������� ������������ � ��������� ������� ��������� � ���������
        m_permission = permission; //���������� ������������ ��� ���������� ������� ����������� �����
        m_rules = template.getRules(); //���������� ������������ ��� �������� ������������ �����
        this.ruleType = ruleType;
        m_status = status; //���������� ��� �������� ������ �������
        init();
    }

    /**
     * �����, ����������� ����� �����. ����������� ���������� ������, ������ ���������.
     */
    private void init() {
        //������� ���������� ������
        cmbPermissions.removeAllItems();
        //������� ���������� �������������� ���� �� ����� ���� ��������
        Enumeration operationType = OperationType.enumerate();

        //�������������� ��������� ���������� ������ ������ �����
        while (operationType.hasMoreElements()) {
            //�������� ������� �� ������ ����� ����
            String type = operationType.nextElement().toString();
            //��������� ���������� ������ ����� ������, ����� "create"
            if (type.equals("create")) {
                continue;
            } else {
                cmbPermissions.addItem(type);
            }
        }

        //������ ���������� ��������� �������, � ������� ��������, �� ����� �������� ��������
        //��� �������� ����� ��������� �� ���� �������� �������

        // if rule type is {@link RuleType#Role} empty and create rights are
		// available
		if ((ruleType.isRoleType() || ruleType.isUndefinedType()) && m_status.equals("NO_STATUS")) {
			// ������ ���������� ������
			cmbPermissions.removeAllItems();
			// ��������� ��� ��� ������� �������
			cmbPermissions.addItem("empty");
			// � ��������� � ���� ��� �� �������� �����
			cmbPermissions.addItem("create");
		}

		if (ruleType.isNoStaticType() && m_status.equals("NO_STATUS")) {
			// ������ ���������� ������
			cmbPermissions.removeAllItems();
			// ��������� ��� ��� ������� �������
			cmbPermissions.addItem("empty");
		}

        //cmbPermissions.setSelectedItem(m_permission.getCardPermission().getType());

        //������� ����� ������ ���������
        DefaultListModel listModelWorkflow = new DefaultListModel();
        listWfMoves.setModel(listModelWorkflow);
        //������� ������ ���������
        DefaultListModel listModelAttribute = new DefaultListModel();
        listAttribute.setModel(listModelAttribute);
        //���� ������ �� ������, �� ��������� ������
        //��������� � ���������
        if (m_template != null) {
            int k = 0;
            //������� id �������
            String currentStatus = null;
            for (int i = 0; i < m_template.getStatusCount(); i++) {
                if (m_status.equals(m_template.getStatus(i).getName())) {
                    currentStatus = m_template.getStatus(i).getStatus_id();
                }
            }
            DefaultListModel listModelWorkflowTypes = new DefaultListModel();
            if (m_template.getWFMoveType(k).getWfm_to_status() != null) {
                while (k < m_template.getWFMoveTypeCount()) {
                    //������� ������� ������� �������� ��������������� �������� �������
                    //������������ ���������: <������������ ��������>-<id>-><� ������>
                    if (currentStatus.equals(m_template.getWFMoveType(k).getWfm_from_status())) {
                        listModelWorkflowTypes.addElement(m_template.getWFMoveType(k).getName() + "->"
                                + m_template.getWFMoveType(k).getWfm_to());
                    }
                    k++;
                }
            }/*else{
            while (k < m_template.getWFMoveTypeCount()) {
            listModelWorkflowTypes.addElement(m_template.getWFMoveType(k++).getName());
            }
            }*/
            listAllWfMoves.setModel(listModelWorkflowTypes);

            k = 0;
            DefaultListModel listModelAllAttribute = new DefaultListModel();
            while (k < m_template.getAttributePermissionTypeCount()) {
                listModelAllAttribute.addElement(m_template.getAttributePermissionType(k).getName());
                k++;
            }
            listAllAttribute.setModel(listModelAllAttribute);
            //������ ���������� ��������� �������, � ������� ��������, �� ����� �������� ��������
            //��� �������� ����� ��������� �� ���� �������� �������            
            //if rule type is delegation clear all wfls
			if (ruleType.isDelegationType()) {
				// ������ ������ ���������
				listModelWorkflowTypes.clear();
				listAllWfMoves.setModel(listModelWorkflowTypes);
			}
			
            //if rule type is role and status is "NO_STATUS"  clear all wfls
			if (ruleType.isRoleType() && m_status.equals("NO_STATUS")) {
				// ������ ������ ���������
				listModelWorkflowTypes.clear();
				listAllWfMoves.setModel(listModelWorkflowTypes);
			}
        }
        

        //��������� ������ ����� ���������� � ���������� �� �����
        cmbPermissions.setSelectedItem(m_permission.resolveOperationType().toString());
        
        if (m_permission != null && m_permission.getWfMoves() != null) {
            if (m_permission.getWfMoves() != null) {
                for (int i = 0; i < m_permission.getWfMoves().getWfMoveCount(); i++) {
                    for (int k = 0; k < m_template.getWFMoveTypeCount(); k++) {
                        if (m_permission.getWfMoves().getWfMove(i).getName().equals(m_template.getWFMoveType(k).getName())
                        		&& !Action.REMOVE.equals(m_permission.getWfMoves().getWfMove(i).getAction())) {
                            listModelWorkflow.addElement(m_permission.getWfMoves().getWfMove(i).getName() + "->" 
                                    + m_template.getWFMoveType(k).getWfm_to());
                        }
                    }

                }
            }
            if (m_permission.getAttributes() != null) {
                for (int i = 0; i < m_permission.getAttributes().getAttributeCount(); i++) {
                    listModelAttribute.addElement(m_permission.getAttributes().getAttribute(i).getName());
                }
            }
        }
    }

    /**
     * ����� ���������� ����������� ��������� �����(permission)
     * @return Permission
     */
    public Permission getPermission() {
        //�������� �� ������������� �����, ���� ���� �� ������� �����, ����� ����������� ������
        if (m_permission == null) {
            m_permission = new Permission();
        }
        
        //�� ����������� ������ ����� ��������� ��� �����
        OperationType operationType = OperationType.valueOf(cmbPermissions.getSelectedItem().toString());
        EditConfigMainForm.setOperation(m_permission, operationType, m_template);
        
        //���� � ������ ��������� ������������ �������� ��������� �� � ��������� �����
        if (listWfMoves.getModel().getSize() > 0 && operationType.equals(OperationType.WRITE)) {
            WfMoves wfmoves = new WfMoves();
            for (int i = 0; i < listWfMoves.getModel().getSize(); i++) {
                WfMove wfMove = new WfMove();
                //��� ���������� ������� � ������������ �������� ������ ������� ������ ��� ���������
                //������������ �������� �� ����� �������
                wfMove.setName(getElementName(listWfMoves.getModel().getElementAt(i).toString()));

                for (int j = 0; j < m_template.getWFMoveTypeCount(); j++) {
                    //String
                    if (m_template.getWFMoveType(j).getName().equals(wfMove.getName())) {
                        wfMove.setWfm_id(m_template.getWFMoveType(j).getWfm_id());
                    }
                }
                wfmoves.addWfMove(wfMove);
            }
            m_permission.setWfMoves(wfmoves);
        } else { //����� ������ �� ���������
            m_permission.setWfMoves(new WfMoves());
        }
        //����� �� ������� ��� � � ���������� �������� ��� ����������
        if (listAttribute.getModel().getSize() > 0) {
            Attributes attributes = new Attributes();
            for (int i = 0; i < listAttribute.getModel().getSize(); i++) {
                Attribute attribute = new Attribute();
                attribute.setName(listAttribute.getModel().getElementAt(i).toString());

                for (int j = 0; j < m_template.getAttributePermissionTypeCount(); j++) {
                    if (m_template.getAttributePermissionType(j).getName().equals(attribute.getName())) {
                        attribute.setAttr_code(m_template.getAttributePermissionType(j).getAttr_code());
                        attribute.setOper_code(m_template.getAttributePermissionType(j).getOper_code());
                    }
                }
                attributes.addAttribute(attribute);
            }
            m_permission.setAttributes(attributes);
        } else {
            m_permission.setAttributes(null);
        }

        return m_permission; //� ���������� ��������� ��������� �����(permission)
    }

    /*    public void setEnabled(boolean enabled)
    {
    super.setEnabled(enabled);
    for (int i = 0; i < getComponents().length; i++) {
    Component component = getComponents()[i];
    component.setEnabled(enabled);
    }
    }*/
    
    private String getElementName(String element){
        String name = element.substring(0, element.indexOf("->"));
        return name;
    }
    
	public void listAttribute_mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
        	this.RemoveElementInListAttribute();
        }
    }

	public void listAllAttribute_mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
        	this.AddElementInListAttribute();
        }
    }

	public void listAllWfMoves_mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
        	this.AddElementInListWfMoves();
        }
    }
	
	public void listWfMoves_mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
        	this.RemoveElementInListWfMoves();
        }
    }

	public void setAllBasePermissions(
			Map<Long, Set<Permission>> allBasePermissions) {
		this.allBasePermissions = allBasePermissions;
		
	}
}

class PermissionComponent_btRemoveAction_actionAdapter implements ActionListener {

    private PermissionComponent adaptee;

    PermissionComponent_btRemoveAction_actionAdapter(PermissionComponent adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.btRemoveAction_actionPerformed(e);
    }
}

class PermissionComponent_btAddAction_actionAdapter implements ActionListener {

    private PermissionComponent adaptee;

    PermissionComponent_btAddAction_actionAdapter(PermissionComponent adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.btAddAction_actionPerformed(e);
    }
}

class PermissionComponent_btDeleteXPermissions_actionAdapter implements ActionListener {

    private PermissionComponent adaptee;

    PermissionComponent_btDeleteXPermissions_actionAdapter(PermissionComponent adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.btDeleteXPermissions_actionPerformed(e);
    }
}

class PermissionComponent_btAddXPermissions_actionAdapter implements ActionListener {

    private PermissionComponent adaptee;

    PermissionComponent_btAddXPermissions_actionAdapter(PermissionComponent adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.btAddXPermissions_actionPerformed(e);
    }
}

class PermissionComponent_listAttribute_actionAdapter extends MouseAdapter {
    private PermissionComponent adaptee;

    PermissionComponent_listAttribute_actionAdapter(PermissionComponent adaptee) {
        this.adaptee = adaptee;
    }

    public void mouseClicked(MouseEvent e) {
        adaptee.listAttribute_mouseClicked(e);
    }
}

class PermissionComponent_listAllWfMoves_actionAdapter extends MouseAdapter {
    private PermissionComponent adaptee;

    PermissionComponent_listAllWfMoves_actionAdapter(PermissionComponent adaptee) {
        this.adaptee = adaptee;
    }

    public void mouseClicked(MouseEvent e) {
        adaptee.listAllWfMoves_mouseClicked(e);
    }
}

class PermissionComponent_listAllAttribute_actionAdapter extends MouseAdapter {
    private PermissionComponent adaptee;

    PermissionComponent_listAllAttribute_actionAdapter(PermissionComponent adaptee) {
        this.adaptee = adaptee;
    }

    public void mouseClicked(MouseEvent e) {
        adaptee.listAllAttribute_mouseClicked(e);
    }
}

class PermissionComponent_listWfMoves_actionAdapter extends MouseAdapter {
    private PermissionComponent adaptee;

    PermissionComponent_listWfMoves_actionAdapter(PermissionComponent adaptee) {
        this.adaptee = adaptee;
    }

    public void mouseClicked(MouseEvent e) {
        adaptee.listWfMoves_mouseClicked(e);
    }
}