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
package org.aplana.br4j.dynamicaccess.rule_attribute;

import java.awt.*;
import javax.swing.*;
import org.aplana.br4j.dynamicaccess.xmldef.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.aplana.br4j.dynamicaccess.config.ConfigurationFacade;
import org.aplana.br4j.dynamicaccess.DialogBase;
import org.aplana.br4j.dynamicaccess.MixedTableModel;
import org.aplana.br4j.dynamicaccess.TableUtility;


public class RuleFormAttribute extends JDialog
{
    private final static boolean debug = false;
    JPanel contentPane;
    BorderLayout borderLayout1 = new BorderLayout();
    JPanel jpnlTemplate = new JPanel();
    JPanel jpnlTableAttribute = new JPanel();
    JPanel jpnlActionButton = new JPanel();
    JLabel jLabel1 = new JLabel();
    BorderLayout borderLayout2 = new BorderLayout();
    JComboBox jcbTemplateName = new JComboBox();
    JButton jbtnSelected = new JButton();
    JButton jbtnCancel = new JButton();
    JTable jtblAttributeRule = new JTable();
    JScrollPane jScrollPane1 = new JScrollPane();
    BorderLayout borderLayout3 = new BorderLayout();

    String defString;
    public String returnString = null;
    private java.util.List<StaticRole> selectedStaticRoles =  new ArrayList<StaticRole>();

    private AccessConfig m_config = null;
    private Template m_template = null;
    private int editField;
    private int comboBoxIndex;
    private int m_indexChoiseRule;

    public RuleFormAttribute(Frame owner, String title, boolean modal, int  choiceField)
    {
        super(owner, title, modal);
        editField = choiceField;
        try {
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            jbInit();
            pack();
            setAutoSize();
            DialogBase.addEscapeListener(this);
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }
    
    public java.util.List<StaticRole> getSelectedStaticRoles() {
		return selectedStaticRoles;
	}

	public void setAutoSize()
    {
        int maxX = 0;
        int maxY = 0;
        for (int i = 0; i < contentPane.getComponents().length; i++) {
            Component component = contentPane.getComponent(i);
            if (maxX < component.getX() + component.getWidth()) {
                maxX = component.getX() + component.getWidth();
            }
            if (maxY < component.getY() + component.getHeight()) {
                maxY = component.getY() + component.getHeight();
            }
        }
        this.setSize(maxX + 10, maxY + contentPane.getHeight() + 5);

        double x = ((getOwner().getLocation().getX() + getOwner().getWidth()) / 2) - (getWidth() / 2);
        double y = ((getOwner().getLocation().getY() + getOwner().getHeight()) / 2) - (getHeight() / 2);
        setLocation((new Double(x)).intValue(), (new Double(y)).intValue());
    }


    public RuleFormAttribute(Frame parent, AccessConfig config, Template template, String s1, String s2, int choiceField, int index,
            int indexChoiseRule)
    {
        //s1 - ������ ��������� ��������� �����
        this(parent, s1, true, choiceField);
        m_config = config;
        m_template = template;
        comboBoxIndex = index;
        m_indexChoiseRule = indexChoiseRule;
        defString = s2;
        returnString = defString;
        comboBoxFilling();
    }


    public void this_windowOpened(WindowEvent e)
    {
        //������������� ������� �� ������ � ������������ � ��������� ������ �� ������� �����
        if (m_template != null) {
            jcbTemplateName.setSelectedIndex(comboBoxIndex);
        }

    }


    public void jcbTemplateName_itemStateChanged(ItemEvent e)
    {
        String currentTemplateName = e.getItem().toString();
        for (Template templatenames : m_config.getTemplate()) {
            if (currentTemplateName.equals(templatenames.getName())) {
                m_template = templatenames;
            }
        }
        tableFilling();
    }

    private void jbInit() throws Exception
    {
        contentPane = (JPanel)this.getContentPane();
        contentPane.setLayout(borderLayout1);
        contentPane.setSize(new Dimension(100, 100));
        contentPane.setPreferredSize(new Dimension(700, 200));

        jpnlTemplate.setLayout(borderLayout2);
        jbtnSelected.setText("�������");
        jbtnSelected.addActionListener(new RuleFormAttribute_jbtnSelected_actionAdapter(this));
        jbtnCancel.setText("������");
        jbtnCancel.addActionListener(new RuleFormAttribute_jbtnCancel_actionAdapter(this));
        jcbTemplateName.addItemListener(new RuleFormAttribute_jcbTemplateName_itemAdapter(this));
        jpnlTableAttribute.setLayout(borderLayout3);
        jpnlTemplate.add(jLabel1, java.awt.BorderLayout.WEST);
        jpnlTemplate.add(jcbTemplateName, java.awt.BorderLayout.CENTER);

        jScrollPane1.getViewport().add(jtblAttributeRule);
        if (editField != 6) {
        jtblAttributeRule.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        } else {
            jtblAttributeRule.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        }
        jtblAttributeRule.setAutoCreateRowSorter(true);
        
        jpnlActionButton.add(jbtnSelected);
        jpnlActionButton.add(jbtnCancel);
        jLabel1.setText("������:");
        contentPane.add(jpnlTableAttribute, java.awt.BorderLayout.CENTER);
        contentPane.add(jpnlTemplate, java.awt.BorderLayout.NORTH);
        contentPane.add(jpnlActionButton, java.awt.BorderLayout.SOUTH);
        jpnlTableAttribute.add(jScrollPane1, java.awt.BorderLayout.CENTER);
        this.addWindowListener(new RuleFormAttribute_this_windowAdapter(this));
    }

    private void comboBoxFilling()
    {
        if (m_config != null) {
            Template[] templates = m_config.getTemplate();
            Set<String> templateNames = new TreeSet<String>();
            if (templates != null) {
                for (Template template : templates) {
                    templateNames.add(template.getName());
                }
            }
            for (String templateName : templateNames) {
                jcbTemplateName.addItem(templateName);
            }
        }

    }

    private void tableFilling()
    {

        jtblAttributeRule.removeAll();

        MixedTableModel attributeRule = new MixedTableModel();

        attributeRule.addColumn("������������ �����");
        attributeRule.addColumn("������������ ��������");
        attributeRule.addColumn("��� ��������");
        attributeRule.addColumn("��� ��������");
        //��������� ������ �������� � �������������
        for (int i = 0; i < m_template.getAttributeRuleCount(); i++) {
            Object[] row = new Object[4];
            row[0] = m_template.getAttributeRule(i).getBlock_name_rus();
            row[1] = m_template.getAttributeRule(i).getAttr_name_rus();
            row[2] = m_template.getAttributeRule(i).getAttribute_code();
            row[3] = m_template.getAttributeRule(i).getData_type();

            //�������
            if (m_indexChoiseRule == 1) {
                //������� ������������
                if (editField != 2 && editField != 3 && editField != 4 && editField != 5 && editField != 6
                        && !m_template.getAttributeRule(i).getData_type().equals("B")
                        && !m_template.getAttributeRule(i).getData_type().equals("C")
                        && !m_template.getAttributeRule(i).getData_type().equals("E")
                        && !m_template.getAttributeRule(i).getData_type().equals("STAT")) {
                    attributeRule.addRow(row);
                }
                //������� ����� � ������� ������������� �����
                if (editField != 1 && editField != 4 && editField != 5 && editField != 6
                        // ���� ����, ����� � �� � ��� � ���������� � ������������ �������� �������������� ������-��������, ��������� ������� ���������� ��������������
                		&& !m_template.getAttributeRule(i).getData_type().equals("U")
                        && !m_template.getAttributeRule(i).getData_type().equals("STAT")) {
                    attributeRule.addRow(row);
                }
                if (editField == 6 && m_template.getAttributeRule(i).getData_type().equals("STAT")){
                    attributeRule.addRow(row);
                }
            }
            //����
            if (m_indexChoiseRule == 2) {
                //������� ������������
                if (m_indexChoiseRule == 2 && m_template.getAttributeRule(i).getData_type().equals("STAT")) {
                    attributeRule.addRow(row);
                }
            }
            //�������
            if (m_indexChoiseRule == 3) {
                //������� �������
                if (editField != 2 && editField != 3 && editField != 4 && editField != 5 && editField != 6) {
                    //attributeRule.addRow(row);
                    attributeRule = addAttributeProfile(attributeRule);	// ��������� �������������� ��������, �������� � ����������� �����
                    break;
                }

                //��������� �������
                if (editField != 1 && editField != 3 && editField != 4 && editField != 5 && editField != 6
                        && !m_template.getAttributeRule(i).getData_type().equals("B")
                        && !m_template.getAttributeRule(i).getData_type().equals("E")
                        && !m_template.getAttributeRule(i).getData_type().equals("STAT")
                        ) {
                    if (m_template.getAttributeRule(i).getData_type().equals("U")) {
                        attributeRule.addRow(row);
                    }
                    else {
                        attributeRule = addTargetAttribute(attributeRule, m_template.getAttributeRule(i).getAttribute_code().toString(), row);
                    }
                }
                //��������� ������� � ������� ���������
                if (editField != 1 && editField != 3 && editField != 4 && editField != 5 && editField != 6
                		&& i == m_template.getAttributeRuleCount()-1){
                	attributeRule = addProfileCardAttribute(attributeRule);
                	break;
                }
                //������� ����� � ������� ������������� �����
                if (editField != 1 && editField != 2 && editField != 5 && editField != 6
                		// ���� ����, ����� � �� � ��� � ���������� � ������������ �������� �������������� ������-��������, ��������� ������� ���������� ��������������
                        && !m_template.getAttributeRule(i).getData_type().equals("U")
                        && !m_template.getAttributeRule(i).getData_type().equals("STAT")) {
                    attributeRule.addRow(row);
                }
                //����������� ����
                if (editField != 1 && editField != 2 && editField != 3 && editField != 4 && editField != 5
                        && m_template.getAttributeRule(i).getData_type().equals("STAT")) {
                    attributeRule.addRow(row);
                }


            }
            //�������������
            if (m_indexChoiseRule == 4) {
                //������� �����
                if (editField != 2 && editField != 3 && editField != 4
                        && !m_template.getAttributeRule(i).getData_type().equals("U")
                        && !m_template.getAttributeRule(i).getData_type().equals("STAT")) {
                    attributeRule.addRow(row);
                }
            }

        }
        //���������� ������� �������
        jtblAttributeRule.setModel(attributeRule);
        TableUtility.toggleFirstColumnRowSorter(jtblAttributeRule);
        
        if (m_template.getAttributeRule() != null) {

        }
    }

    public void jbtnCancel_actionPerformed(ActionEvent e)
    {
        returnString = defString;
        selectedStaticRoles = null;
        this.hide();
    }

    public void jbtnSelected_actionPerformed(ActionEvent e) {
		if (editField != 6) {
        int selectedRow = jtblAttributeRule.getSelectedRow();
        if (jtblAttributeRule.isRowSelected(selectedRow)) {
            returnString = jtblAttributeRule.getValueAt(selectedRow, 2).toString();
			}
		} else {
            
			if (jtblAttributeRule.getSelectedRowCount() > 0) {
				int[] selectedRows = jtblAttributeRule.getSelectedRows();
				for (int selectedRow : selectedRows) {
					String roleCode = jtblAttributeRule.getValueAt(selectedRow, 2).toString();
					String roleDesc = jtblAttributeRule.getValueAt(selectedRow, 1).toString();
					StaticRole selectedStaticRole = new StaticRole(roleCode, roleDesc);
					selectedStaticRoles.add(selectedStaticRole);
				}
			}
        }
        this.hide();
    }


    private MixedTableModel addTargetAttribute(MixedTableModel mtm, String strTargetAttribute, Object[] row)
    {
        for (int i = 0; i < ConfigurationFacade.getInstance().getPersonCardLinkAttributes().size(); i++) {
            if (ConfigurationFacade.getInstance().getPersonCardLinkAttributes().get(i).equals(strTargetAttribute)) {
                mtm.addRow(row);
                break;
            }
        }
        return mtm;
    }


//����� ������������ ������� ����� ��������� �� ����� ������������ ��� ���� ������� �������
    private MixedTableModel addAttributeProfile(MixedTableModel mtm)
    {
        for (int i = 0; i < ConfigurationFacade.getInstance().getProfileAttributes().size(); i++) {
            Object[] row = new Object[4];
            row[0] = "-";
            row[1] = "-";
            row[2] = ConfigurationFacade.getInstance().getProfileAttributes().get(i).toString();
            row[3] = "-";

            mtm.addRow(row);
        }

        return mtm;
    }
    
    private MixedTableModel addProfileCardAttribute(MixedTableModel mtm)
    {
        for (int i = 0; i < ConfigurationFacade.getInstance().getProfileCardAttributes().size(); i++) {
            Object[] row = new Object[4];
            row[0] = "-";
            row[1] = "-";
            row[2] = ConfigurationFacade.getInstance().getProfileCardAttributes().get(i).toString();
            row[3] = "-";

            mtm.addRow(row);
        }

        return mtm;
    }

    /**
     * DTO used to pass values between {@link RuleFormAttribute} form and {@link EditConfigMainForm}.
     * @author atsvetkov
     *
     */
    public static class StaticRole {
        
    	private String roleCode;    	
    	private String roleDesc;
		
    	public StaticRole(String roleCode, String roleDesc) {
			this.roleCode = roleCode;
			this.roleDesc = roleDesc;
		}

		public String getRoleCode() {
			return roleCode;
		}

		public String getRoleDesc() {
			return roleDesc;
		}    	

        @Override
        public int hashCode() {
            return roleCode.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof StaticRole)) return false;
            StaticRole anotherRole = (StaticRole) obj;
            String anotherRoleCode = anotherRole.getRoleCode();
            return anotherRoleCode != null ? anotherRoleCode.equals(this.roleCode) : roleCode==null;
        }

        @Override
        public String toString() {
            return roleDesc;
        }
    }
}


class RuleFormAttribute_jbtnCancel_actionAdapter implements ActionListener
{
    private RuleFormAttribute adaptee;
    RuleFormAttribute_jbtnCancel_actionAdapter(RuleFormAttribute adaptee)
    {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e)
    {
        adaptee.jbtnCancel_actionPerformed(e);
    }
}


class RuleFormAttribute_jbtnSelected_actionAdapter implements ActionListener
{
    private RuleFormAttribute adaptee;
    RuleFormAttribute_jbtnSelected_actionAdapter(RuleFormAttribute adaptee)
    {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e)
    {
        adaptee.jbtnSelected_actionPerformed(e);
    }
}


class RuleFormAttribute_jcbTemplateName_itemAdapter implements ItemListener
{
    private RuleFormAttribute adaptee;
    RuleFormAttribute_jcbTemplateName_itemAdapter(RuleFormAttribute adaptee)
    {
        this.adaptee = adaptee;
    }

    public void itemStateChanged(ItemEvent e)
    {
        adaptee.jcbTemplateName_itemStateChanged(e);
    }
}


class RuleFormAttribute_this_windowAdapter extends WindowAdapter
{
    private RuleFormAttribute adaptee;
    RuleFormAttribute_this_windowAdapter(RuleFormAttribute adaptee)
    {
        this.adaptee = adaptee;
    }

    public void windowOpened(WindowEvent e)
    {
        adaptee.this_windowOpened(e);
    }
}
