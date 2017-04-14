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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import org.aplana.br4j.dynamicaccess.xmldef.AccessConfig;
import org.aplana.br4j.dynamicaccess.xmldef.Template;


/**
 * 
 * @author atsvetkov
 *
 */
public class TemplatesForm extends JDialog {

	private final static int TEMPLATE_ID_COLUMN_INDEX = 1;
	private static String FORM_TITLE = "����� �������";
	private JPanel contentPane;
	private JButton selectBtn = new JButton();
    private JButton cancelBtn = new JButton();
    private JPanel buttonsPanel = new JPanel();
    private JTable templateTable = new JTable();
    private JScrollPane templateScrolPane = new JScrollPane();
    private AccessConfig accessConfig;
    private Template currentTemplate;
    private String selectedTemplateId;
    
    public TemplatesForm(JFrame owner, AccessConfig accessConfig, Template currentTemplate){
    	super(owner, FORM_TITLE, true);
    	this.accessConfig = accessConfig;
    	this.currentTemplate = currentTemplate;
    	
    	setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        jbInit();
        pack();
        setAutoSize();
        DialogBase.addEscapeListener(this);
    }
    
    private void jbInit() {
        contentPane = (JPanel)this.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.setSize(new Dimension(100, 100));
        contentPane.setPreferredSize(new Dimension(700, 200));

        selectBtn.setText("�������");
        selectBtn.addActionListener(new OnSelectActionAdapter(this));
        cancelBtn.setText("������");
        cancelBtn.addActionListener(new OnCancelActionAdapter(this));
        buttonsPanel.add(selectBtn);
        buttonsPanel.add(cancelBtn);


        templateTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        templateTable.setAutoCreateRowSorter(true);
        
        templateScrolPane.setHorizontalScrollBar(null);
        templateScrolPane.getViewport().add(templateTable);                       
        
        contentPane.add(templateScrolPane, BorderLayout.CENTER);
        contentPane.add(buttonsPanel, BorderLayout.SOUTH);
        this.addWindowListener(new TemplateFormWindowAdapter(this));
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

    private void fillTemplateTable(){
        MixedTableModel tableModel = new MixedTableModel();       
        tableModel.addColumn("������������");
        tableModel.addColumn("�������������");

        Template[] templates = accessConfig.getTemplate();

        Set<Template> orderedTemplates = new TreeSet<Template>(new TemplateComparator());

        for(Template template : templates){
        	orderedTemplates.add(template);
        }
        	
        for (Template template : orderedTemplates) {
			if (currentTemplate != null && template.getTemplate_id().equals(currentTemplate.getTemplate_id())) {
				continue;
			}
			Object[] row = new Object[2];
			row[0] = template.getName();
			row[1] = template.getTemplate_id();
			tableModel.addRow(row);
		}
        
        templateTable.setModel(tableModel);

        templateTable.setAutoCreateRowSorter(true);
        TableUtility.addIntegerSorter(templateTable, tableModel, new Integer[] {TEMPLATE_ID_COLUMN_INDEX});
        TableUtility.toggleFirstColumnRowSorter(templateTable);
    }
    
	public void windowOpened(WindowEvent e) {
		fillTemplateTable();
	}		

	public String getSelectedTemplateId() {
		return selectedTemplateId;
	}

	public void onSelect(ActionEvent e) {
		int selectedRow = templateTable.getSelectedRow();
		if (templateTable.isRowSelected(selectedRow)) {
			selectedTemplateId = templateTable.getValueAt(selectedRow, 1).toString();
		}
		this.hide();	       
	}

	public void onCancel(ActionEvent e) {
		selectedTemplateId = null;
		this.hide();	       	
	}
	
	/**
	 * 
	 * @author atsvetkov
	 *
	 */
	class TemplateFormWindowAdapter extends WindowAdapter {
		private TemplatesForm adaptee;

		TemplateFormWindowAdapter(TemplatesForm adaptee) {
			this.adaptee = adaptee;
		}

		public void windowOpened(WindowEvent e) {
			adaptee.windowOpened(e);
		}
	}
	
	/**
	 * 
	 * @author atsvetkov
	 *
	 */
	class OnSelectActionAdapter implements ActionListener {
		private TemplatesForm adaptee;

		OnSelectActionAdapter(TemplatesForm adaptee) {
			this.adaptee = adaptee;
		}

		public void actionPerformed(ActionEvent e) {
			adaptee.onSelect(e);
		}
	}	

	/**
	 * 
	 * @author atsvetkov
	 *
	 */
	class OnCancelActionAdapter implements ActionListener {
		private TemplatesForm adaptee;

		OnCancelActionAdapter(TemplatesForm adaptee) {
			this.adaptee = adaptee;
		}

		public void actionPerformed(ActionEvent e) {
			adaptee.onCancel(e);
		}
	}	
}
