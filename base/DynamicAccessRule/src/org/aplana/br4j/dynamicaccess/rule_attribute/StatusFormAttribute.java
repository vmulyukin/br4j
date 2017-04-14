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

import org.aplana.br4j.dynamicaccess.DialogBase;
import org.aplana.br4j.dynamicaccess.MixedTableModel;
import org.aplana.br4j.dynamicaccess.TableUtility;

public class StatusFormAttribute extends JDialog
{
    private final static boolean debug = false;
    private final String NO_STATUS = "NO_STATUS";
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
    private java.util.List<LinkedStatus> selectedStatuses = new ArrayList<LinkedStatus>();


    private AccessConfig m_config = null;
    private Template m_template = null;
    private int comboBoxIndex;

    public StatusFormAttribute(Frame owner, String title, boolean modal)
    {
        super(owner, title, modal);
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

    public java.util.List<LinkedStatus> getSelectedStatuses() {
		return selectedStatuses;
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

    public StatusFormAttribute(Frame parent, AccessConfig config, Template template, String s1, String s2, int index)
    {
        //s1 - ������ ��������� ��������� �����
        this(parent, s1, true);
        m_config = config;
        m_template = template;
        comboBoxIndex = index;
        defString = s2;        
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
        contentPane.setPreferredSize(new Dimension(500, 200));

        jpnlTemplate.setLayout(borderLayout2);
        jbtnSelected.setText("�������");
        jbtnSelected.addActionListener(new StatusFormAttribute_jbtnSelected_actionAdapter(this));
        jbtnCancel.setText("������");
        jbtnCancel.addActionListener(new StatusFormAttribute_jbtnCancel_actionAdapter(this));
        jcbTemplateName.addItemListener(new StatusFormAttribute_jcbTemplateName_itemAdapter(this));
        jpnlTableAttribute.setLayout(borderLayout3);
        jpnlTemplate.add(jLabel1, java.awt.BorderLayout.WEST);
        jpnlTemplate.add(jcbTemplateName, java.awt.BorderLayout.CENTER);

        jScrollPane1.getViewport().add(jtblAttributeRule);
        jtblAttributeRule.setAutoCreateRowSorter(true);                
        
        jpnlActionButton.add(jbtnSelected);
        jpnlActionButton.add(jbtnCancel);
        jLabel1.setText("������:");
        contentPane.add(jpnlTableAttribute, java.awt.BorderLayout.CENTER);
        contentPane.add(jpnlTemplate, java.awt.BorderLayout.NORTH);
        contentPane.add(jpnlActionButton, java.awt.BorderLayout.SOUTH);
        jpnlTableAttribute.add(jScrollPane1, java.awt.BorderLayout.CENTER);
        this.addWindowListener(new StatusFormAttribute_this_windowAdapter(this));
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

        attributeRule.addColumn("������������ �������");
        attributeRule.addColumn("������������� �������");
        //��������� ������ �������� � �������������
        for (int i = 0; i < m_template.getStatusCount(); i++) {
            Object[] row = new Object[2];
            row[0] = m_template.getStatus(i).getName();
            row[1] = m_template.getStatus(i).getStatus_id();
            if (!m_template.getStatus(i).getName().equals(NO_STATUS)) {
                attributeRule.addRow(row);
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
        this.hide();
    }

    public void jbtnSelected_actionPerformed(ActionEvent e)
    {
    	if(jtblAttributeRule.getSelectedRowCount() > 0 ){
    		for(int row : jtblAttributeRule.getSelectedRows()) {
    			String statusId = jtblAttributeRule.getValueAt(row, 1).toString();
    			String statusDesc = jtblAttributeRule.getValueAt(row, 0).toString();
    			
    			selectedStatuses.add(new LinkedStatus(statusId, statusDesc));
    			
    		}
    		
    	}        
        this.hide();
    }
     /**
     * DTO used to pass values between {@link StatusFormAttribute} form and {@link EditConfigMainForm}.
     * @author atsvetkov
     *
     */
    public static class LinkedStatus {
    	
    	private String linkedStatusId;
    	
    	private String linkedStatusDesc;

    	public LinkedStatus(String linkedStatusId, String linkedStatusDesc) {
    		this.linkedStatusId = linkedStatusId;
    		this.linkedStatusDesc = linkedStatusDesc;
    	}

    	public String getLinkedStatusId() {
    		return linkedStatusId;
    	}

    	public String getLinkedStatusDesc() {
    		return linkedStatusDesc;
    	}
    	
    	@Override
    	public String toString() {
    		return linkedStatusDesc;
    	}
    	
    	@Override
    	public boolean equals(Object obj) {
    		if(!(obj instanceof LinkedStatus)){
    			return false;
    		}
    		return ((LinkedStatus)obj).getLinkedStatusId().equals(getLinkedStatusId());
    	}
    	
    	@Override
    	public int hashCode() {
    		return getLinkedStatusId().hashCode();
    	}
    }
}


class StatusFormAttribute_jbtnSelected_actionAdapter implements ActionListener
{
    private StatusFormAttribute adaptee;
    StatusFormAttribute_jbtnSelected_actionAdapter(StatusFormAttribute adaptee)
    {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e)
    {
        adaptee.jbtnSelected_actionPerformed(e);
    }
}


class StatusFormAttribute_jbtnCancel_actionAdapter implements ActionListener
{
    private StatusFormAttribute adaptee;
    StatusFormAttribute_jbtnCancel_actionAdapter(StatusFormAttribute adaptee)
    {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e)
    {
        adaptee.jbtnCancel_actionPerformed(e);
    }
}


class StatusFormAttribute_jcbTemplateName_itemAdapter implements ItemListener
{
    private StatusFormAttribute adaptee;
    StatusFormAttribute_jcbTemplateName_itemAdapter(StatusFormAttribute adaptee)
    {
        this.adaptee = adaptee;
    }

    public void itemStateChanged(ItemEvent e)
    {
        adaptee.jcbTemplateName_itemStateChanged(e);
    }
}


class StatusFormAttribute_this_windowAdapter extends WindowAdapter
{
    private StatusFormAttribute adaptee;
    StatusFormAttribute_this_windowAdapter(StatusFormAttribute adaptee)
    {
        this.adaptee = adaptee;
    }

    public void windowOpened(WindowEvent e)
    {
        adaptee.this_windowOpened(e);
    }
        
}
