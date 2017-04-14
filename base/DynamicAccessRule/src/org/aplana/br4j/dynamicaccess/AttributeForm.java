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
import com.borland.jbcl.layout.*;
import javax.swing.JLabel;
import javax.swing.JTextField;
import org.aplana.br4j.dynamicaccess.xmldef.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

public class AttributeForm extends DialogBase
{
    //JLabel jLabel1 = new JLabel();
    JLabel jLabel2 = new JLabel();
    JLabel jLabel3 = new JLabel();
    JLabel jLabel4 = new JLabel();
    //JTextField txtAttributeName = new JTextField();
    JTextField txtAttributeDescr = new JTextField();
    JTextField txtAttrCode = new JTextField();
    JTextField txtOperCode = new JTextField();
    private Template m_template;
    private int m_attributeIndex;
    private AttributePermissionType m_attribute;

    public AttributeForm(Frame owner, String title, boolean modal)
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

    public AttributeForm(Frame parent, Template template, int attributeIndex)
    {
        this(parent, "��������", true);
        m_template = template;
        m_attributeIndex = attributeIndex;
        if (m_attributeIndex != -1) {
            m_attribute = m_template.getAttributePermissionType(m_attributeIndex);
        }
    }

    private void jbInit() throws Exception
    {
        //jLabel1.setText("������������");
        jLabel2.setText("��������");
        jLabel3.setText("��� ��������");
        jLabel4.setText("��� ��������");
        this.addWindowListener(new AttributeForm_this_windowAdapter(this));
        //panelInputControls.add(jLabel1, new XYConstraints(5, 5, 120, 25));
        panelInputControls.add(jLabel3, new XYConstraints(5, 5, 120, 25));
        panelInputControls.add(jLabel4, new XYConstraints(5, 35, 120, 25));
        panelInputControls.add(jLabel2, new XYConstraints(5, 65, 120, 25));
        //panelInputControls.add(txtAttributeName, new XYConstraints(130, 5, 250, 25));
        panelInputControls.add(txtAttributeDescr, new XYConstraints(130, 65, 250, 25));
        panelInputControls.add(txtAttrCode, new XYConstraints(130, 5, 250, 25));
        panelInputControls.add(txtOperCode, new XYConstraints(130, 35, 250, 25));
        //txtAttributeName.setText("");
        txtAttributeDescr.setText("");
        txtAttrCode.setText("");
        txtOperCode.setText("");
    }

    public boolean onOk()
    {
        /*�� ������ ������ ���� ������������ � ��������� ������- �� ������� ������ ������� �����*/
        if (m_attributeIndex == -1) {
            m_attribute = new AttributePermissionType();
            m_template.addAttributePermissionType(m_attribute);
            m_attribute.setName(txtAttrCode.getText());
            m_attribute.setAttr_code(txtAttrCode.getText());
            m_attribute.setOper_code(txtOperCode.getText());
            m_attribute.setDescription(txtAttributeDescr.getText());
        }
        if (m_attributeIndex > -1) {
            m_attribute.setName(txtAttrCode.getText());
            m_attribute.setAttr_code(txtAttrCode.getText());
            m_attribute.setOper_code(txtOperCode.getText());
            m_attribute.setDescription(txtAttributeDescr.getText());
        }
        return true;
    }

    public void this_windowOpened(WindowEvent e)
    {
        if (m_attribute != null) {
            //txtAttributeName.setText(m_attribute.getName());
            txtAttrCode.setText(m_attribute.getAttr_code());
            txtOperCode.setText(m_attribute.getOper_code());
            txtAttributeDescr.setText(m_attribute.getDescription());
        }
    }
}


class AttributeForm_this_windowAdapter extends WindowAdapter
{
    private AttributeForm adaptee;
    AttributeForm_this_windowAdapter(AttributeForm adaptee)
    {
        this.adaptee = adaptee;
    }

    public void windowOpened(WindowEvent e)
    {
        adaptee.this_windowOpened(e);
    }
}
