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

import com.borland.jbcl.layout.VerticalFlowLayout;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aplana.br4j.dynamicaccess.config.ConfigurationFacade;
import org.aplana.br4j.dynamicaccess.db_export.*;
import org.aplana.br4j.dynamicaccess.db_export.objects.AccessListDao;
import org.aplana.br4j.dynamicaccess.db_export.objects.ConnectionFactory;
import org.aplana.br4j.dynamicaccess.db_export.objects.TemplateDao;
import org.aplana.br4j.dynamicaccess.db_import.DoImportBase;
import org.aplana.br4j.dynamicaccess.db_import.DoImportTemplate;
import org.aplana.br4j.dynamicaccess.db_import.DoLoadTemplateList;
import org.aplana.br4j.dynamicaccess.rule_attribute.FindAttributeName;
import org.aplana.br4j.dynamicaccess.xmldef.*;
import org.aplana.br4j.dynamicaccess.xmldef.types.Action;
import org.aplana.br4j.dynamicaccess.xmldef.types.*;
import org.aplana.br4j.dynamicaccess.xmldef.types.PermissionWrapper.RuleType;

import javax.accessibility.AccessibleContext;
import javax.sql.DataSource;
import javax.swing.*;
import javax.swing.RowSorter.SortKey;
import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;

//����������� � ������ ��������������� �� xsd - �����

/**
 * <p>Title:������� ����� ���������� ������������� ���� ������� </p>
 *
 * <p>Description:������������ ��� ��������� ���������� �������� ���� � �������� 
 * ������ �������������� � ������� �������� </p>
 *
 * <p>Copyright: Copyright (c) 2011</p>
 *
 * <p>Company:MySoft </p>
 *
 */
public class EditConfigMainForm extends JFrame {

    private static final String STATUS_NAME_SEPARATOR = ",";
    private final static int TEMPLATE_ID_COLUMN_INDEX = 1;
    private final static int STATUS_ID_COLUMN_INDEX = 1;
    private final static int WFMOVE_ID_COLUMN_INDEX = 1;
    private final static int PERSON_LINKED_STATUS_COLUMN_ID = 4; 
    private final static int PERSON_STATIC_ROLE_COLUMN_ID = 5;    
    private final static int PROFILE_LINKED_STATUS_COLUMN_ID = 5; 
    private final static int PROFILE_STATIC_ROLE_COLUMN_ID = 6; 
    
    public final static String KEY_URL = "url";
    public final static String KEY_USER = "username";
    public final static String KEY_PASSWORD = "password";
    public static final String KEY_ENABLE = "enable";
    
    private ProgressMonitor progressMonitor;
    
	protected final Log logger = LogFactory.getLog(getClass());
	public static Map<Long,Set<Permission>> allBasePermissions = new HashMap<Long,Set<Permission>>();

    JPanel contentPane;
    BorderLayout borderLayout1 = new BorderLayout();
    BorderLayout borderLayout2 = new BorderLayout();
    BorderLayout borderLayout3 = new BorderLayout();
    BorderLayout borderLayout4 = new BorderLayout();
    BorderLayout borderLayout5 = new BorderLayout();
    BorderLayout borderLayout6 = new BorderLayout();
    BorderLayout borderLayout7 = new BorderLayout();
    BorderLayout borderLayout9 = new BorderLayout();
    JTabbedPane jTabbedPane1 = new JTabbedPane();
    JTabbedPane jTabbedPane2 = new JTabbedPane();
    JPanel jpnlPermission1 = new JPanel();
    JPanel jpnlTemplates = new JPanel();
    JPanel jpnlAdmin = new JPanel();
    JPanel jpnlButton = new JPanel();
    JPanel jPanel1 = new JPanel();
    JPanel jpnlPermission2 = new JPanel();
    JPanel jpnlStatus = new JPanel();
    JPanel jpnlRules = new JPanel();
    JButton jbtnClose = new JButton();
    JButton jbtnSave = new JButton();
    JComboBox jcbTemplateType = new JComboBox();
    JLabel jLabel1 = new JLabel();
    JScrollPane jscpPermission = new JScrollPane();
    JScrollPane jscpStatus = new JScrollPane();
    JScrollPane jscpRules = new JScrollPane();
    JScrollPane jscpRuleEdit = new JScrollPane();
    //������� ������� ������� �����
    MixedTable jtblPermission = new MixedTable();
    JTable jtblStatus = new JTable();
    JTable jtblRules = new JTable();
    JTable jtblTemplate = new JTable();
    //��������� ���������� ��� ��������� � xml
    AccessConfig m_config = null;
    Template m_template = null;
    Permission m_permission = null;
    AttributePermissionType m_attributetype = null;
    String m_file = null;
    JPanel jpnlTemplateEditButton = new JPanel();
    VerticalFlowLayout verticalFlowLayout1 = new VerticalFlowLayout();
    VerticalFlowLayout verticalFlowLayout2 = new VerticalFlowLayout();
    JPanel jpnlRulesEdit = new JPanel();
    VerticalFlowLayout verticalFlowLayout3 = new VerticalFlowLayout();
    JButton jbtnRulesDelete = new JButton();
    JButton jbtnRulesEdit = new JButton();
    JButton jbtnRulesCopy = new JButton();
    JButton jbtnRulesPaste = new JButton();
    JButton jbtnCopyRules = new JButton();
    JCheckBox keepOldRules = new JCheckBox("Chin");
    JButton jbtnRulesAdd = new JButton();
    JPanel jpnlAttribute = new JPanel();
    JScrollPane jScrollPane1 = new JScrollPane();
    JTable jtblAttribute = new JTable();
    JPanel jpnlWorkflowMove = new JPanel();
    JScrollPane jScrollPane2 = new JScrollPane();
    BorderLayout borderLayout8 = new BorderLayout();
    JTable jtblWorkflowMove = new JTable();
    JPanel jpnlAttributeEdit = new JPanel();
    VerticalFlowLayout verticalFlowLayout4 = new VerticalFlowLayout();
    VerticalFlowLayout verticalFlowLayout5 = new VerticalFlowLayout();
    JButton jbtnAttributeDelete = new JButton();
    JButton jbtnAttributeEdit = new JButton();
    JButton jbtnAttributeAdd = new JButton();
    JPanel jpnlRadioButtonGroup = new JPanel();
    ButtonGroup btnGroupRules = new ButtonGroup();
    JRadioButton jrbtnProfileRuleType = new JRadioButton();
    JRadioButton jrbtnRoleRuleType = new JRadioButton();
    JRadioButton jrbtnPersonRuleType = new JRadioButton();
    JRadioButton jrbtnDelegationRuleType = new JRadioButton();
    VerticalFlowLayout verticalFlowLayout6 = new VerticalFlowLayout();
    JLabel jLabel3 = new JLabel();
    JPanel jPanel2 = new JPanel();
    VerticalFlowLayout verticalFlowLayout7 = new VerticalFlowLayout();
    SplitButton jbtnSaveAs = new SplitButton();
    SplitButton jbtnToInstal = new SplitButton();
    SplitButton jbtnCalculateAccessList = new SplitButton();
    
    JButton jbtnGetTemplateList = new JButton();
    
    BorderLayout borderLayout10 = new BorderLayout();
    JFileChooser fileopen = new JFileChooser();
    JPanel jPanel3 = new JPanel();
    JLabel jLabel2 = new JLabel();
    JTextField jtxtfURL = new JTextField();
    JButton jbtnOpenFile = new JButton();
    JButton jbtnLoadFrom = new JButton();
    JButton jbtnShowSecondaryDb = new JButton();
    JTextField jtxtfUserName = new JTextField();
    JTextField jtxtfPassword = new JTextField();
    VerticalFlowLayout verticalFlowLayout8 = new VerticalFlowLayout();
    JLabel jLabel4 = new JLabel();
    JLabel jLabel5 = new JLabel();
    JLabel jLabel6 = new JLabel();
    JLabel jLabelAppVersion = new JLabel();
    JScrollPane jScrollPane3 = new JScrollPane();
    JPopupMenu jpupmDeletePermission = new JPopupMenu();
    JPopupMenu permissionPopupMenu = new JPopupMenu();
    JPopupMenu rulesPopupMenu = new JPopupMenu();
    JPopupMenu templatesTablePopupMenu = new JPopupMenu(); 
    private FindAttributeName attributeNames;
    private ConfigurationFacade.JdbcSettings jdbcSettings = null;

    public Clipboard localClipboard = new Clipboard("localClipboard");
	private EditConfigMainForm mainForm;

    /**
     * ����� ��������� � �������� ����� �� ������ ���������� � � ���������� �����
     * @param file String - �������� ����� � ���� ������ ���� � ����� xml
     */
    public EditConfigMainForm(String file) {
        try {
            //���������� ��� �������� ��� �������� ����� �� ���������� �������
            /*
            m_file = file; //���� ������� ����������� ���� �� ���������� �������
            
            //��������� ��� ����������� ������������ ������������ ��������� ���� �� ������ ����������
            //int ret = fileopen.showDialog(null, "������� ����");
            //if (ret == JFileChooser.APPROVE_OPTION) {
            //    m_file = fileopen.getSelectedFile().getAbsolutePath();
            //}
             */
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            //��������� ����� ������������� �����
            jbInit();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /**
     * ����� ��������� ���� � ����������, �������� �������������.
     * ������������ ��� ���������� ����� ������ �� ������ "������� XML"
     */
    private void openXMLFileInApplication() {
        try {
            //fileopen.setCurrentDirectory(new File(ConfigurationFacade.getInstance().getXmlFileHome()));
            //��������� ���������� ���� ������ �����
            int ret = fileopen.showDialog(null, "������� ����");
            //���� ������������ ������ ���� ��� �������� "������" ������ ������� 0
            //� ��������� ������ ����� �������� �� ����
            if (ret == JFileChooser.APPROVE_OPTION) { //���� �������� - 0
                //���������� ���� � �����
                m_file = fileopen.getSelectedFile().getAbsolutePath();
                //������� ��������� "��������" xml - ����� � ������ ���� � ����������
                InputStreamReader reader = new InputStreamReader(new FileInputStream(m_file), "UTF-8");
                m_config = (AccessConfig) AccessConfig.unmarshal(reader);
                //��������� "��������"
                reader.close();
                //� ��������� ����� ������� �� xml
                initForm();
                updateFileInfo();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
	    fileopen.setFileFilter(new FileNameExtensionFilter("Xml with rules","xml"));
        contentPane = (JPanel) getContentPane();
        contentPane.setLayout(borderLayout1);
        setSize(new Dimension(600, 400));
        setTitle("Access Rule");
        this.addWindowListener(new EditConfigMainForm_this_windowAdapter(this));
        contentPane.setPreferredSize(new Dimension(300, 300));
        jpnlTemplates.setLayout(borderLayout9);
        jpnlTemplateEditButton.setLayout(verticalFlowLayout1);
        jpnlTemplateEditButton.setPreferredSize(new Dimension(150, 150));
        jpnlRulesEdit.setLayout(verticalFlowLayout3);
        jbtnRulesDelete.setText("�������");
        jbtnRulesDelete.addActionListener(new EditConfigMainForm_jbtnRulesDelete_actionAdapter(this));
        jbtnRulesEdit.setText("�������������");
        jbtnRulesEdit.addActionListener(new EditConfigMainForm_jbtnRulesEdit_actionAdapter(this));
        jbtnRulesCopy.setText("����������");
        jbtnRulesCopy.addActionListener(new EditConfigMainForm_jbtnRulesCopy_actionAdapter(this));
        jbtnRulesPaste.setText("��������");
        jbtnRulesPaste.addActionListener(new EditConfigMainForm_jbtnRulesPaste_actionAdapter(this));
        jbtnCopyRules.setText("����������� ��...");
        jbtnCopyRules.addActionListener(new CopyRulesActionAdapter(this));
        
        keepOldRules.setText("�������� ������� �������");
        keepOldRules.setMnemonic(KeyEvent.VK_L); 
        keepOldRules.setSelected(false);

        jbtnRulesAdd.setText("��������");
        jbtnRulesAdd.addActionListener(new EditConfigMainForm_jbtnRulesAdd_actionAdapter(this));
        jbtnSave.addActionListener(new EditConfigMainForm_jbtnSave_actionAdapter(this));
        jtblPermission.addMouseListener(new EditConfigMainForm_jtblPermission_mouseAdapter(this));
        jtblPermission.addKeyListener(new EditConfigMainForm_jtblPermission_keyAdapter(this));
        jpnlAttribute.setLayout(borderLayout7);
        jpnlWorkflowMove.setLayout(borderLayout8);
        jpnlAttributeEdit.setLayout(verticalFlowLayout4);
        jbtnAttributeDelete.setText("�������");
        jbtnAttributeDelete.addActionListener(new EditConfigMainForm_jbtnAttributeDelete_actionAdapter(this));
        jbtnAttributeEdit.setText("�������������");
        jbtnAttributeEdit.addActionListener(new EditConfigMainForm_jbtnAttributeEdit_actionAdapter(this));
        jbtnAttributeAdd.setText("��������");
        jbtnAttributeAdd.addActionListener(new EditConfigMainForm_jbtnAttributeAdd_actionAdapter(this));
        jrbtnProfileRuleType.setText("�������");
        jrbtnProfileRuleType.addActionListener(new EditConfigMainForm_jrbtnProfileRuleType_changeAdapter(this));
        
        jrbtnRoleRuleType.setText("����");
        jrbtnRoleRuleType.addActionListener(new EditConfigMainForm_jrbtnRoleRuleType_changeAdapter(this));
        jrbtnPersonRuleType.setText("������������");
        jrbtnPersonRuleType.addActionListener(new EditConfigMainForm_jrbtnPersonRuleType_changeAdapter(this));
        jrbtnDelegationRuleType.addActionListener(new EditConfigMainForm_jrbtnDelegationRuleType_changeAdapter(this));
        jpnlRadioButtonGroup.setLayout(verticalFlowLayout6);

        jLabel3.setText("���� ������:");
        jPanel2.setLayout(verticalFlowLayout7);
        jbtnSaveAs.setText("��������� ���...");
        jbtnSaveAs.addActionListener(new EditConfigMainForm_jbtnSaveAs_actionAdapter(this));
        jbtnToInstal.setText("��������� � ��");
        jbtnToInstal.addActionListener(new EditConfigMainForm_jbtnToInstal_actionAdapter(this, false));
        
        jbtnCalculateAccessList.setText("�������� access list");
        jbtnCalculateAccessList.addActionListener(new EditConfigMainForm_jbtnCalculateAccessList_actionAdapter(this, false));

        jbtnGetTemplateList.setText("��������� ������ ��������");
        jbtnGetTemplateList.addActionListener(new EditConfigMainForm_jbtnGetTemplateList_actionAdapter(this));

        jpnlAdmin.setLayout(borderLayout10);
        jLabel2.setText("URL:");
        jtxtfURL.setPreferredSize(new Dimension(400, 19));

        try {
	        jdbcSettings = ConfigurationFacade.getInstance().getJdbcSettings();
	        jtxtfURL.setText(jdbcSettings.mainJdbcSetting.get(KEY_URL).toString());
	        jtxtfUserName.setText(jdbcSettings.mainJdbcSetting.get(KEY_USER).toString());
	        jtxtfPassword.setText(jdbcSettings.mainJdbcSetting.get(KEY_PASSWORD).toString());jtxtfURL.getText();
        } catch(IOException e) {
        	e.printStackTrace();
        }
        jbtnOpenFile.setPreferredSize(new Dimension(33, 22));
        jbtnOpenFile.setText("������� XML");
        jbtnOpenFile.addActionListener(new EditConfigMainForm_jbtnOpenFile_actionAdapter(this));
        jbtnLoadFrom.setText("��������� �� ��");
        jbtnLoadFrom.addActionListener(new EditConfigMainForm_jbtnLoadFrom_actionAdapter(this));
        jbtnShowSecondaryDb.setText("�������������� ��");
        jbtnShowSecondaryDb.addActionListener(new EditConfigMainForm_jbtnShowSecondaryDb_actionAdapter(this));
        jPanel3.setLayout(verticalFlowLayout8);
        jLabel4.setText("User name:");
        jLabel5.setText("Password:");

        jScrollPane3.setHorizontalScrollBar(null);
        jScrollPane3.setToolTipText("");
        jtblPermission.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        jrbtnDelegationRuleType.setText("�������������");

        contentPane.add(jTabbedPane1, java.awt.BorderLayout.CENTER);
        contentPane.add(jpnlButton, java.awt.BorderLayout.SOUTH);

        jpnlButton.setPreferredSize(new Dimension(10, 35));
        jpnlButton.add(jLabelAppVersion);
        jpnlButton.add(jbtnSave);
        jpnlButton.add(jbtnClose);
        jpnlButton.add(jLabel6);
        jPanel1.setLayout(borderLayout3);
        jPanel1.add(jLabel1, java.awt.BorderLayout.WEST);
        jPanel1.add(jcbTemplateType, java.awt.BorderLayout.CENTER);

        jpnlPermission1.setLayout(borderLayout2);
        jpnlPermission1.add(jPanel1, java.awt.BorderLayout.NORTH);
        jpnlPermission1.add(jTabbedPane2, java.awt.BorderLayout.CENTER);

        jpnlPermission2.setLayout(borderLayout4);
        jpnlStatus.setLayout(borderLayout5);
        jpnlRules.setLayout(borderLayout6);
        jbtnClose.setMaximumSize(new Dimension(87, 23));
        jbtnClose.setMinimumSize(new Dimension(87, 23));
        jbtnClose.setPreferredSize(new Dimension(87, 23));
        jbtnClose.setText("�������");
        jbtnClose.addActionListener(new EditConfigMainForm_jbtnClose_actionAdapter(this));
        jbtnSave.setText("���������");
        jLabel6.setText("���� �� �����");
        jLabel1.setText("������  ");

        jcbTemplateType.setPreferredSize(new Dimension(26, 22));
        jcbTemplateType.addItemListener(new EditConfigMainForm_jcbTemplateType_itemAdapter(this));
        jTabbedPane1.add(jpnlPermission1, "�����");
        jTabbedPane1.add(jpnlTemplates, "�������");
        jTabbedPane1.add(jpnlAdmin, "�����������������");
        jTabbedPane1.setSelectedIndex(2);
        jPanel2.add(jbtnOpenFile);
        jPanel2.add(jbtnSaveAs);
        jPanel2.add(jbtnLoadFrom);
        jPanel2.add(jbtnToInstal);
        jPanel2.add(jbtnCalculateAccessList);               
        jPanel2.add(jbtnGetTemplateList);
        jPanel2.add(jbtnShowSecondaryDb);
        jTabbedPane2.add(jpnlPermission2, "�����");
        jTabbedPane2.add(jpnlStatus, "�������");
        jscpStatus.getViewport().add(jtblStatus);
        jTabbedPane2.add(jpnlRules, "�������");
        jscpRules.getViewport().add(jtblRules);
        
        //adding popup menu for rules table
        jtblRules.addMouseListener(new TableRulesMouseAdapter(this));
        jtblRules.setRowSelectionAllowed(true);
        jtblRules.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        
        //adding popup menu for tamples table
        jtblTemplate.addMouseListener(new TableTemplatesMouseAdapter(this));
        //Prevent changing column order
        jtblTemplate.getTableHeader().setReorderingAllowed(false);

        jtblPermission.setBorder(null);
        jtblPermission.setCellSelectionEnabled(true);
        //jtblPermission.setRowHeight(40);
        jtblPermission.setRowSelectionAllowed(true);
        jtblPermission.setColumnSelectionAllowed(true);
        jtblPermission.setDragEnabled(true);
        jtblPermission.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        
		initializePermissionPopupMenu();
		initRulesPopupMenu();
		initTemplatesTablePupupMenu();
		initButtonsPupupMenu();
		
        jscpPermission.getViewport().add(jtblPermission);
        jpnlPermission2.add(jscpPermission, java.awt.BorderLayout.CENTER);
        jpnlStatus.add(jscpStatus, java.awt.BorderLayout.CENTER);
        jpnlRules.add(jscpRules, java.awt.BorderLayout.CENTER);

        jscpRuleEdit.setBorder(BorderFactory.createEmptyBorder());
        jscpRuleEdit.getViewport().add(jpnlRulesEdit);
        jpnlRules.add(jscpRuleEdit, java.awt.BorderLayout.EAST);
        
        jpnlRulesEdit.add(jbtnRulesAdd);
        jpnlRulesEdit.add(jbtnRulesEdit);
        jpnlRulesEdit.add(jbtnRulesDelete);
        jpnlRulesEdit.add(jbtnRulesCopy);
        jpnlRulesEdit.add(jbtnRulesPaste);
        jpnlRulesEdit.add(jpnlRadioButtonGroup);

        jpnlRulesEdit.add(new JSeparator(JSeparator.HORIZONTAL));
        JPanel copyRulesPanel = new JPanel(new GridLayout(0, 1));
        Border border = BorderFactory.createTitledBorder("���������� �������");
        copyRulesPanel.setBorder(border);
        
        copyRulesPanel.add(keepOldRules);
        copyRulesPanel.add(jbtnCopyRules);
        
        jpnlRulesEdit.add(copyRulesPanel);
        
        jTabbedPane2.add(jpnlAttribute, "��������");
        jScrollPane1.getViewport().add(jtblAttribute);
        jpnlAttribute.add(jScrollPane1, java.awt.BorderLayout.CENTER);
        jTabbedPane2.add(jpnlWorkflowMove, "��������");
        jpnlWorkflowMove.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        jScrollPane2.getViewport().add(jtblWorkflowMove);
        jpnlRadioButtonGroup.add(jLabel3);
        jpnlRadioButtonGroup.add(jrbtnPersonRuleType, null);
        jpnlRadioButtonGroup.add(jrbtnRoleRuleType, null);
        jpnlRadioButtonGroup.add(jrbtnProfileRuleType, null);
        jpnlRadioButtonGroup.add(jrbtnDelegationRuleType);
        //jscpPermission.setRowHeader(null);
        //jscpPermission.setColumnHeader(null);
        //jscpPermission.setPreferredSize(new Dimension(4, 4));

        btnGroupRules.add(jrbtnPersonRuleType);
        btnGroupRules.add(jrbtnRoleRuleType);
        btnGroupRules.add(jrbtnProfileRuleType);
        btnGroupRules.add(jrbtnDelegationRuleType);
        jpnlAdmin.add(jPanel2, java.awt.BorderLayout.WEST);
        jpnlAdmin.add(jPanel3, java.awt.BorderLayout.CENTER);
        jPanel3.add(jLabel2, null);
        jPanel3.add(jtxtfURL, null);
        jPanel3.add(jLabel4);
        jPanel3.add(jtxtfUserName, null);
        jPanel3.add(jLabel5);
        jPanel3.add(jtxtfPassword, null);
        jpnlTemplates.add(jScrollPane3, java.awt.BorderLayout.CENTER);
        jScrollPane3.getViewport().add(jtblTemplate);
        mainForm = this;
    }

	private void initRulesPopupMenu() {
		final JMenuItem updatePartialRuleItem = createRulesPopupLoadItem("��������� ��������� � ��",
				true);
		
		final JMenuItem updateFullRuleItem = createRulesPopupLoadItem("��������� ��� � ��",
				false);
		
		final JMenuItem deleteRuleItem = new JMenuItem("������� �� ��");
		deleteRuleItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae) {												
				int rowCount = jtblRules.getSelectedRowCount();				
				if(rowCount <= 0 ||  rowCount > 1){
					JOptionPane.showMessageDialog(EditConfigMainForm.this, "�������� ���� ������� ��� ��������.", "Warning",
		                    JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				int selectedRow = jtblRules.getSelectedRow();			
				String ruleName = (String)jtblRules.getValueAt(selectedRow, 0);
				if(m_template == null || ruleName == null) {
					return;
				}
				int result = JOptionPane.showConfirmDialog(EditConfigMainForm.this,
		                "������ ��� ������� " + ruleName + " ����� ������� �� ��.", "�������� !!!",
		                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

		        if (result == JOptionPane.YES_OPTION) {
		            try {
		            	progressMonitor = new ProgressMonitor(EditConfigMainForm.this, "�������� ������� " + ruleName + " � ����", "", 0, 100);
		                progressMonitor.setMillisToDecideToPopup(10);
		                progressMonitor.setMillisToPopup(10);
		                progressMonitor.setProgress(1);

		            	deleteRuleFromDataBase(selectedRow);
		                deleteRuleFromModel(selectedRow, ruleName);
		                initTypeTablesAndSaveLoadConfiguration();
		                progressMonitor.setProgress(100);

		            } catch (Exception ex) {
		                ex.printStackTrace();
		                JOptionPane.showMessageDialog(EditConfigMainForm.this, ex.getMessage(), "������ �������� �������.",
		                        JOptionPane.ERROR_MESSAGE);
		            }

		        }				
			}
			
		});
		
		final JMenuItem markToRemoveRuleItem = new JMenuItem("�������� ��� ��������");
		
		markToRemoveRuleItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae) {												
				int rowCount = jtblRules.getSelectedRowCount();				
				if(rowCount <= 0 ||  rowCount > 1){
					JOptionPane.showMessageDialog(EditConfigMainForm.this, "�������� ���� �� ���� �������", "Warning",
		                    JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				for(int selectedRow: jtblRules.getSelectedRows()){
					String ruleName = (String)jtblRules.getValueAt(selectedRow, 0);
					if(m_template == null || ruleName == null) {
						return;
					}
					List<Rule> rulesToDelete = RulesUtility.getRulesByRuleNameFromModel(ruleName, m_template);
					RulesUtility.markRuleAsRemoved(rulesToDelete, m_template, allBasePermissions);
				}
		        initTypeTablesAndSaveLoadConfiguration();
		        adjustRowsHeight();
			}
			
		});
		
		
		final JMenuItem clearChangesRuleItem = new JMenuItem("�������� ���������");
		clearChangesRuleItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae) {												
				int rowCount = jtblRules.getSelectedRowCount();				
				if(rowCount <= 0){
					JOptionPane.showMessageDialog(EditConfigMainForm.this, "�������� ���� �� ���� �������", "Warning",
		                    JOptionPane.WARNING_MESSAGE);
					return;
				}
				Set<Permission> templateBasePermissions = allBasePermissions.get(m_template.getTemplateIdLong());
				if(templateBasePermissions == null){
					return;
				}
				for(int selectedRow: jtblRules.getSelectedRows()){
					String ruleName = (String)jtblRules.getValueAt(selectedRow, 0);
					if(m_template == null || ruleName == null) {
						return;
					}
					List<Rule> rulesToUnMark = RulesUtility.getRulesByRuleNameFromModel(ruleName, m_template);
					for(Rule rule: rulesToUnMark){
						RulesUtility.clearChangesForRule(rule, m_template, allBasePermissions);
					}
				}
				
		        initTypeTablesAndSaveLoadConfiguration();
		        adjustRowsHeight();
			}
			
		});
		
		final JMenuItem markRuleToOverwrite = new JMenuItem("�������� ��� ����������");
		markRuleToOverwrite.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				int rowCount = jtblRules.getSelectedRowCount();				
				if(rowCount <= 0){
					JOptionPane.showMessageDialog(EditConfigMainForm.this, "�������� ���� �� ���� �������", "Warning",
		                    JOptionPane.WARNING_MESSAGE);
					return;
				}
				for(int selectedRow: jtblRules.getSelectedRows()){
					String ruleName = (String)jtblRules.getValueAt(selectedRow, 0);
					if(m_template == null || ruleName == null) {
						return;
					}
					List<Rule> rulesToOverwrite = RulesUtility.getRulesByRuleNameFromModel(ruleName, m_template);
					for(Rule rule: rulesToOverwrite){
						RulesUtility.markRuleForOverwriting(rule, m_template);
					}
				}
		        initTypeTablesAndSaveLoadConfiguration();
		        adjustRowsHeight();
			}
		});

		rulesPopupMenu.add(updateFullRuleItem);
		rulesPopupMenu.add(deleteRuleItem);
		rulesPopupMenu.addSeparator();
		rulesPopupMenu.add(updatePartialRuleItem);
		rulesPopupMenu.add(markRuleToOverwrite);
		rulesPopupMenu.add(markToRemoveRuleItem);
		rulesPopupMenu.add(clearChangesRuleItem);
		
		
	}

	private JMenuItem createRulesPopupLoadItem(String title,
			final boolean onlyChanges) {
		final JMenuItem updateRuleItem = new JMenuItem(title);
		updateRuleItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae) {												
				int rowCount = jtblRules.getSelectedRowCount();
				
		    	if (m_config.getPartial() && !onlyChanges) {
				    JOptionPane.showMessageDialog(mainForm, "��������� ��������� ������ ����! �������� �� � �������� ������ ���������.", "Warning",
				            JOptionPane.INFORMATION_MESSAGE);
				    return;
				}
		    	
				if(rowCount <= 0 ||  rowCount > 1){
					JOptionPane.showMessageDialog(EditConfigMainForm.this, "�������� ���� ������� ��� ��������.", "Warning",
		                    JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				int selectedRow = jtblRules.getSelectedRow();
				String ruleName = (String)jtblRules.getValueAt(selectedRow, 0);

				if(m_template == null || ruleName == null) {
					return;
				}
				int result = JOptionPane.showConfirmDialog(EditConfigMainForm.this,
		                "������ ��� ������� " + ruleName + " ����� ������������", "�������� !!!",
		                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

		        if (result == JOptionPane.YES_OPTION) {
		            try {
		            	List<String> ruleNames = new ArrayList<String>();
		            	ruleNames.add(ruleName);
		                final DoUpdateAccessRule updateAccessRule = new DoUpdateAccessRule(jtxtfURL.getText(), jtxtfUserName.getText(), jtxtfPassword.getText(), ruleNames, m_template);
		                updateAccessRule.setPartial(onlyChanges);
		                progressMonitor = new ProgressMonitor(EditConfigMainForm.this, "�������� ������� " + ruleName + " � ����", "", 0, 100);
		                progressMonitor.setMillisToDecideToPopup(10);
		                progressMonitor.setMillisToPopup(10);
		                progressMonitor.setProgress(0);

		                updateAccessRule.addPropertyChangeListener(new PropertyChangeListener() {
		                    public void propertyChange(PropertyChangeEvent evt) {
//		                        logger.debug("Property changed, property name: " + evt.getPropertyName());
		                    	if (progressMonitor.isCanceled()) {
		                            updateAccessRule.cancel(true);
		                            JOptionPane.showMessageDialog(EditConfigMainForm.this, "�������� �������������", "������ ��������", JOptionPane.ERROR_MESSAGE);
		                            updateRuleItem.setEnabled(true);
		                            return;
		                        }

		                        if ("progress".equals(evt.getPropertyName())) {
		                            int progress = (Integer) evt.getNewValue();
		                            progressMonitor.setProgress(progress);
		                        }

		                        if ("state".equals(evt.getPropertyName())) {
		                            if (updateAccessRule.isDone()) {
		                                Exception exception = null;
		                                try {
		                                    exception = updateAccessRule.get();
		                                } catch (Exception e1) {
		                                    exception = e1;
		                                }
		                                if (exception != null) {
		                                    // �������� � �������
		                                    exception.printStackTrace();
		                                    JOptionPane.showMessageDialog(EditConfigMainForm.this, exception.getMessage(), "������ ��������", JOptionPane.ERROR_MESSAGE);
		                                } else {
		                                    JOptionPane.showMessageDialog(EditConfigMainForm.this, "�������� ������� ���������", "Access Rule", JOptionPane.INFORMATION_MESSAGE);
		                                }
		                                updateRuleItem.setEnabled(true);
		                            }
		                        }
		                    }
		                });

		                // ��������� �������� � �� ��������� �������
		                updateAccessRule.execute();
		                updateRuleItem.setEnabled(false);

		            } catch (Exception ex) {
		                ex.printStackTrace();
		                JOptionPane.showMessageDialog(EditConfigMainForm.this, ex.getMessage(), "������ ��������",
		                        JOptionPane.ERROR_MESSAGE);
		            }

		        }				
			}
			
		});
		return updateRuleItem;
	}

	private void initializePermissionPopupMenu() {
		JMenuItem permissionEditItem = new JMenuItem("��������� �������������� ������");
		permissionEditItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				
				if(m_config.getPartial()){
		            JOptionPane.showMessageDialog(null, "��������� �������������� �������� ������ ��� ������ ������ ����.", "Warning",
		                    JOptionPane.WARNING_MESSAGE);
		            return;
				}
				
				if(jtblPermission.getSelectedRowCount() > 1){
		            JOptionPane.showMessageDialog(null, "��������� �������������� �������� ������ ��� ������ �������.", "Warning",
		                    JOptionPane.WARNING_MESSAGE);
		            return;
				}
				
				List<PermissionWrapper> permissionList = new ArrayList<PermissionWrapper>();
				
				int row = jtblPermission.getSelectedRow();				
				String statusName = ((MixedTableModel) jtblPermission.getModel()).getRowName(row);
				Set<RuleType> selectedRuleTypes = new HashSet<RuleType>();
				RuleType currectRuleType = null;
				
				for (int column : jtblPermission.getSelectedColumns()){
					List<Permission> permissionsInCell = (List<Permission>) jtblPermission.getValueAt(row, column);
					String ruleName = jtblPermission.getColumnName(column);
					
					currectRuleType = RuleTypeUtility.getRuleTypeByRuleName(ruleName, m_template);
					selectedRuleTypes.add(currectRuleType);
					
					getPermissionListForCell(permissionList, statusName, permissionsInCell, ruleName);
										
					
					//check that all permissions belong to one rule type
					if(selectedRuleTypes.size() > 1){
			            JOptionPane.showMessageDialog(null, "������������� ������� ����� ���� ������ ������ ����.", "Warning",
			                    JOptionPane.WARNING_MESSAGE);
			            return;						
					}					
				}
				if(currectRuleType == null) {
					return;
				}
				boolean inOneRuleSelected = jtblPermission.getSelectedColumnCount() == 1 ? true : false;
				editPermissionSet(permissionList, currectRuleType, statusName, inOneRuleSelected);
				adjustRowsHeight();				
			}
			
			
		});
		
		final JMenuItem uploadPartialPermissionItem = createUploadPermissionItem("��������� ��������� � �� ", true);
		final JMenuItem uploadFullPermissionItem = createUploadPermissionItem("��������� ��������� � �� ", false);
		
		JMenuItem markToOverwriteMenuItem = new JMenuItem("�������� ��� ����������");
		markToOverwriteMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				List<PermissionWrapper> permissionList = new ArrayList<PermissionWrapper>();
				for (int row : jtblPermission.getSelectedRows()){
					// � �� ���� ���������� ��������
					for (int column : jtblPermission.getSelectedColumns()){
										
						String statusName = ((MixedTableModel) jtblPermission.getModel()).getRowName(row);
						String ruleName = jtblPermission.getColumnName(column);
						List<Rule> rules = RulesUtility.getRulesByRuleNameFromModel(ruleName, m_template);
						for(Rule rule: rules){
							RulesUtility.markCellForOverwriting(m_template, rule, statusName);
						}
					}
				}
				initTypeTablesAndSaveLoadConfiguration();
			}
		});
		
		JMenuItem clearChangesPopupMenuItem = new JMenuItem("�������� ���������");
		clearChangesPopupMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				for (int row : jtblPermission.getSelectedRows()){
					// � �� ���� ���������� ��������
					for (int column : jtblPermission.getSelectedColumns()){
										
						String statusName = ((MixedTableModel) jtblPermission.getModel()).getRowName(row);
						String ruleName = jtblPermission.getColumnName(column);
						List<Rule> rules = RulesUtility.getRulesByRuleNameFromModel(ruleName, m_template);
						for(Rule rule: rules){
							RulesUtility.clearChangesForCell(statusName, rule, m_template, allBasePermissions);
						}
					}
				}
				initTypeTablesAndSaveLoadConfiguration();
			}
		});
		
		permissionPopupMenu.add(uploadFullPermissionItem);
		permissionPopupMenu.add(permissionEditItem);
		permissionPopupMenu.addSeparator();
		permissionPopupMenu.add(uploadPartialPermissionItem);
		permissionPopupMenu.add(markToOverwriteMenuItem);
		permissionPopupMenu.add(clearChangesPopupMenuItem);
		
		
	}

	private JMenuItem createUploadPermissionItem(String title, final boolean onlyChanges) {
		final JMenuItem uploadPermissionItem = new JMenuItem(title);
		uploadPermissionItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				int rowCount = jtblPermission.getSelectedRowCount();
				int columnCount = jtblPermission.getSelectedColumnCount();
				
		    	if (m_config.getPartial() && !onlyChanges) {
				    JOptionPane.showMessageDialog(mainForm, "��������� ��������� ������ ����! �������� �� � �������� ������ ���������.", "Warning",
				            JOptionPane.INFORMATION_MESSAGE);
				    return;
				}
								
				if(rowCount <= 0 ||  columnCount <= 0) {
					JOptionPane.showMessageDialog(EditConfigMainForm.this, "�������� ���� �� ���� ������ � ������� ����.", "Warning",
		                    JOptionPane.WARNING_MESSAGE);
					return;
				}
								
				final List<String> selectedRules = new ArrayList<String>();
				final List<String> selectedStatuses = new ArrayList<String>();
				
				for (int column : jtblPermission.getSelectedColumns()){
					selectedRules.add(jtblPermission.getColumnName(column));
				}
				
				for (int row : jtblPermission.getSelectedRows()){
					selectedStatuses.add(((MixedTableModel) jtblPermission.getModel()).getRowName(row));
					
				}
				
				if(m_template == null || selectedRules.isEmpty() || selectedStatuses.isEmpty()) {									
					return;
				}
				
				int result = JOptionPane.showConfirmDialog(EditConfigMainForm.this,
		                formatMessage("������ ��� ������ " + selectedRules + " ����� ������������."), "�������� !!!",
		                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

		        if (result == JOptionPane.YES_OPTION) {
		            try {
		            	List<String> statusIds = convertStatusNamesToStatusIds(selectedStatuses);
		            						
		                final DoUpdateAccessRuleByStatus updateAccessRuleByStatus = new DoUpdateAccessRuleByStatus(jtxtfURL.getText(), jtxtfUserName.getText(), jtxtfPassword.getText(), selectedRules, statusIds, m_template);
		                updateAccessRuleByStatus.setPartial(onlyChanges);
		                progressMonitor = new ProgressMonitor(EditConfigMainForm.this, "�������� ������ " + selectedRules + " � ����", "", 0, 100);
		                progressMonitor.setMillisToDecideToPopup(10);
		                progressMonitor.setMillisToPopup(10);
		                progressMonitor.setProgress(0);

		                updateAccessRuleByStatus.addPropertyChangeListener(new PropertyChangeListener() {
		                	boolean isButtonDisabled = false;
		                	
		                	private <T extends JComponent> List<T> getDescendantsOfType(Class<T> clazz, Container container) {
                		    	List<T> tList = new ArrayList<T>();
                		    	for (Component component : container.getComponents()) {
                		    		if (clazz.isAssignableFrom(component.getClass())) {
                		    			tList.add(clazz.cast(component));
                		    		} else {
                		    			tList.addAll(getDescendantsOfType(clazz, (Container) component));
                		    		}
                		    	}
                		    	return tList;
                			}
	                	   
		                	private JButton getCancelButton(JDialog dialog) {
		                		List<JButton> components = getDescendantsOfType(JButton.class, dialog);
		                		if (components.size() > 0) return components.get(0);
		                		
		                		return null;
		                	}

		                    public void propertyChange(PropertyChangeEvent evt) {
		                    	if (progressMonitor.isCanceled()) {
		                            updateAccessRuleByStatus.cancel(true);
		                            JOptionPane.showMessageDialog(EditConfigMainForm.this, "�������� �������������", "������ ��������", JOptionPane.ERROR_MESSAGE);
		                            uploadPermissionItem.setEnabled(true);
		                            return;
		                        }

		                        if ("progress".equals(evt.getPropertyName())) {
		                            int progress = (Integer) evt.getNewValue();		                             
		                            progressMonitor.setProgress(progress);
		                            
		                            //If in current ProgressMonitor dialog 'Cancel' button is in focus drop it
		                            if (!isButtonDisabled) {
			                            AccessibleContext ac = progressMonitor.getAccessibleContext();
			    		                JDialog dialog = (JDialog)ac.getAccessibleParent();
			    		                if (dialog != null) {
			    		                	JButton cancelButton = getCancelButton(dialog);
			    		                	if (cancelButton != null) {
			    		                		cancelButton.setFocusable(false);
			    		                		isButtonDisabled = true;
			    		                    }
			    		                }
		    		                }
		                        }

		                        if ("state".equals(evt.getPropertyName())) {
		                            if (updateAccessRuleByStatus.isDone()) {
		                                Exception exception = null;
		                                try {
		                                    exception = updateAccessRuleByStatus.get();
		                                } catch (Exception e1) {
		                                    exception = e1;
		                                }
		                                if (exception != null) {
		                                    // �������� � �������
		                                    exception.printStackTrace();
		                                    logger.error("Error loading access rule for rules " + selectedRules + " and statuses " + selectedStatuses + " : " + exception.getMessage());
		                                    JOptionPane.showMessageDialog(EditConfigMainForm.this, exception.getMessage(), "������ ��������", JOptionPane.ERROR_MESSAGE);
		                                } else {
		                                    JOptionPane.showMessageDialog(EditConfigMainForm.this, "�������� ������� ���������", "Access Rule", JOptionPane.INFORMATION_MESSAGE);
		                                }
		                                progressMonitor.setProgress(StateObserver.MAX_PROGRESS);
		                                uploadPermissionItem.setEnabled(true);
		                            }
		                        }
		                    }
		                });

		                // ��������� �������� � �� ��������� �������
		                updateAccessRuleByStatus.execute();
		                uploadPermissionItem.setEnabled(false);

		            } catch (Exception ex) {
		                ex.printStackTrace();
		                JOptionPane.showMessageDialog(EditConfigMainForm.this, ex.getMessage(), "������ ��������",
		                        JOptionPane.ERROR_MESSAGE);
		            }

		        }				
												
			}

		});
		return uploadPermissionItem;
	}

    private void initTemplatesTablePupupMenu() {
		templatesTablePopupMenu.add(createSaveTemplateToDBPopupMenu());
		templatesTablePopupMenu.add(creatLoadTemplateFromDBPopupMenuItem());
		templatesTablePopupMenu.addSeparator();
		templatesTablePopupMenu.add(martToOverwritePopupMenuItem());
		templatesTablePopupMenu.add(clearChangesPopupMenuItem());
    }

    private JMenuItem clearChangesPopupMenuItem() {
    	JMenuItem updateTemplateItem = new JMenuItem("�������� ���������");
    	updateTemplateItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				int rowCount = jtblTemplate.getSelectedRowCount();
				if(rowCount <= 0){
					JOptionPane.showMessageDialog(EditConfigMainForm.this, "�������� ���� �� ���� ������ ��� ��������.", "Warning",
		                    JOptionPane.WARNING_MESSAGE);
					return;
				}
				for(int selectedRow: jtblTemplate.getSelectedRows()){
					final String templateName = (String) jtblTemplate.getValueAt(selectedRow, 0);
					Template templateToUnMark = null;
					if (m_config != null) {
						for(Template template : m_config.getTemplate()){
		                    if (template.getName().equalsIgnoreCase(templateName)) {
		                    	templateToUnMark = template;
			                    break;
		                    }
						}
					}
		            if (templateToUnMark == null) {
						JOptionPane.showMessageDialog(EditConfigMainForm.this, "������� ������ ���������� ��������� �� ��!", "Warning",
			                    JOptionPane.WARNING_MESSAGE);
						return;
		            }
		            for(Rule rule: templateToUnMark.getRules().getRule()){
		            	RulesUtility.clearChangesForRule(rule, templateToUnMark, allBasePermissions);
		            }
				}
		        initTypeTablesAndSaveLoadConfiguration();
		        adjustRowsHeight();
			}
		});
    	return updateTemplateItem;
	}

	private JMenuItem martToOverwritePopupMenuItem() {
    	JMenuItem updateTemplateItem = new JMenuItem("�������� ��� ����������");
    	updateTemplateItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				int rowCount = jtblTemplate.getSelectedRowCount();
				if(rowCount <= 0){
					JOptionPane.showMessageDialog(EditConfigMainForm.this, "�������� ���� �� ���� ������ ��� ��������.", "Warning",
		                    JOptionPane.WARNING_MESSAGE);
					return;
				}
				for(int selectedRow: jtblTemplate.getSelectedRows()){
					final String templateName = (String) jtblTemplate.getValueAt(selectedRow, 0);
					Template templateToMark = null;
					if (m_config != null) {
						for(Template template : m_config.getTemplate()){
		                    if (template.getName().equalsIgnoreCase(templateName)) {
		                    	templateToMark = template;
			                    break;
		                    }
						}
					}
		            if (templateToMark == null) {
						JOptionPane.showMessageDialog(EditConfigMainForm.this, "������� ������ ���������� ��������� �� ��!", "Warning",
			                    JOptionPane.WARNING_MESSAGE);
						return;
		            }
		            for(Rule rule: templateToMark.getRules().getRule()){
		            	RulesUtility.markRuleForOverwriting(rule, templateToMark);
		            }
				}
		        initTypeTablesAndSaveLoadConfiguration();
		        adjustRowsHeight();
			}
		});
		return updateTemplateItem;
	}

	private JMenuItem createSaveTemplateToDBPopupMenu() {
		JMenuItem updateTemplateItem = new JMenuItem("��������� � ��");
		updateTemplateItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae) {
				int rowCount = jtblTemplate.getSelectedRowCount();
				if(rowCount <= 0 ||  rowCount > 1){
					JOptionPane.showMessageDialog(EditConfigMainForm.this, "�������� ���� ������ ��� ��������.", "Warning",
		                    JOptionPane.WARNING_MESSAGE);
					return;
				}

				int selectedRow = jtblTemplate.getSelectedRow();
				final String templateName = (String) jtblTemplate.getValueAt(selectedRow, 0);
				Template templateToSave = null;

	            if (m_config != null) {
					Template[] templates = m_config.getTemplate();
		            if (templates != null) {
		                for (int i = 0; i < templates.length; i++) {
		                    Template template = templates[i];
		                    if (template.getName().equalsIgnoreCase(templateName)) {
		                    	templateToSave = template;
			                    break;
		                    }
		                }
		            }
	            }

	            if (templateToSave == null) {
					JOptionPane.showMessageDialog(EditConfigMainForm.this, "������� ������ ���������� ��������� �� ��!", "Warning",
		                    JOptionPane.WARNING_MESSAGE);
					return;
	            }

				int result = JOptionPane.showConfirmDialog(EditConfigMainForm.this,
		                "�� ��������� ��������� ���������� ������� "+ templateName +" � ����.\r\n ��� ������ � ���� ��� ����� ������� ����� ������������!!!'", "��������!",
		                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

		        if (result == JOptionPane.YES_OPTION) {
		            try {
		                final DoSaveTemplate saveTemplate = new DoSaveTemplate(jtxtfURL.getText(), jtxtfUserName.getText(), jtxtfPassword.getText(), m_config, templateToSave);

		                progressMonitor = new ProgressMonitor(EditConfigMainForm.this, "�������� � ����", "", 0, 100);
		                progressMonitor.setMillisToDecideToPopup(10);
		                progressMonitor.setMillisToPopup(10);
		                progressMonitor.setProgress(1);

		                saveTemplate.addPropertyChangeListener(new PropertyChangeListener() {
		                    public void propertyChange(PropertyChangeEvent evt) {
		                        if (progressMonitor.isCanceled()) {
		                        	saveTemplate.cancel(true);
		                            JOptionPane.showMessageDialog(EditConfigMainForm.this, "�������� �������������", "������ ��������", JOptionPane.ERROR_MESSAGE);
		                            jbtnToInstal.setEnabled(true);
		                            return;
		                        }

		                        if ("progress".equals(evt.getPropertyName())) {
		                            int progress = (Integer) evt.getNewValue();
		                            progressMonitor.setProgress(progress);
		                        }

		                        if ("state".equals(evt.getPropertyName())) {
		                            if (saveTemplate.isDone()) {
		                                Exception exception = null;
		                                try {
		                                    exception = saveTemplate.get();
		                                } catch (Exception e1) {
		                                    exception = e1;
		                                }
		                                if (exception != null) {
		                                    // �������� � �������
		                                    exception.printStackTrace();
		                                    JOptionPane.showMessageDialog(EditConfigMainForm.this, exception.getMessage(), "������ ��������", JOptionPane.ERROR_MESSAGE);
		                                } else {
		                                    JOptionPane.showMessageDialog(EditConfigMainForm.this, "�������� "+ templateName +" ������� ���������", "Access Rule", JOptionPane.INFORMATION_MESSAGE);
		                                    progressMonitor.setProgress(100);
		                                }
		                            }
		                        }
		                    }
		                });

		                // ��������� �������� � �� ��������� �������
		                saveTemplate.execute();
		                
		            } catch (Exception ex) {
		                ex.printStackTrace();
		                JOptionPane.showMessageDialog(EditConfigMainForm.this, "������ �������� ������: " + ex.getMessage(), "������ ��������",
		                        JOptionPane.ERROR_MESSAGE);
		            }
		        }
			}
		});
		return updateTemplateItem;
    }

    private JMenuItem creatLoadTemplateFromDBPopupMenuItem() {
		JMenuItem templateItem = new JMenuItem("��������� �� ��");
		templateItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				int rowCount = jtblTemplate.getSelectedRowCount();
				if(rowCount <= 0 || rowCount > 1){
					JOptionPane.showMessageDialog(EditConfigMainForm.this, "�������� ���� ������.", "Warning",
		                    JOptionPane.WARNING_MESSAGE);
					return;
				}

		    	int result = JOptionPane.showConfirmDialog(EditConfigMainForm.this,
		                "�� ��������� ��������� �������� ������� �� ��.\r\n ��� ������ � ������� ��� ����� ������� ����� ������������!'", "��������!",
		                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

			    if (result == JOptionPane.YES_OPTION) {
			    	if (m_config == null) {
		    			m_config = new AccessConfig();
					}

					int selectedRow = jtblTemplate.getSelectedRow();
					final String templateName = (String) jtblTemplate.getValueAt(selectedRow, 0);
					final String templateId   = (String) jtblTemplate.getValueAt(selectedRow, 1);
					try {
						final DoImportTemplate doImportTemplate = new DoImportTemplate(jtxtfURL.getText(), jtxtfUserName.getText(), jtxtfPassword.getText(), m_config, templateName, templateId);

						progressMonitor = new ProgressMonitor(EditConfigMainForm.this, "��������� ������� " + templateName + " �� ����", "", 0, 100);
						progressMonitor.setMillisToDecideToPopup(10);
						progressMonitor.setMillisToPopup(10);
						progressMonitor.setProgress(1);

						doImportTemplate.addPropertyChangeListener(new PropertyChangeListener() {
							public void propertyChange(PropertyChangeEvent evt) {
								if (progressMonitor.isCanceled()) {
		                    		doImportTemplate.cancel(true);
		                    		JOptionPane.showMessageDialog(EditConfigMainForm.this, "�������� �������������", "������ ��������", JOptionPane.ERROR_MESSAGE);
		                            return;
		                        }

		                        if ("progress".equals(evt.getPropertyName())) {
		                            int progress = (Integer) evt.getNewValue();
			                        progressMonitor.setProgress(progress);
		                        }

		                        if ("state".equals(evt.getPropertyName())) {
		                            if (doImportTemplate.isDone()) {
		                                Exception exception = null;
		                                try {
		                                    exception = doImportTemplate.get();
		                                } catch (Exception e1) {
		                                    exception = e1;
		                                }

		                                if (exception != null) {
		                                    // �������� � �������
		                                    exception.printStackTrace();
			                                logger.error("Error loading " + templateName + " template");
		                                    JOptionPane.showMessageDialog(EditConfigMainForm.this, exception.getMessage(), "������ ��������", JOptionPane.ERROR_MESSAGE);
		                                } else {
		                                    JOptionPane.showMessageDialog(EditConfigMainForm.this, "�������� ������� ���������", "Template loading", JOptionPane.INFORMATION_MESSAGE);
		                                    updateAttributeList();
		                                    addTemplateNameToTemplateTypeComboBox(templateName);
		                                    initTypeTables();
		                                    loadLayoutConfiguration();
		                                }
		                                progressMonitor.setProgress(StateObserver.MAX_PROGRESS);
		                            }
		                        }
							}
						});

						doImportTemplate.execute();
					} catch (Exception ex) {
						ex.printStackTrace();
						 JOptionPane.showMessageDialog(EditConfigMainForm.this, ex.getMessage(), "������ ��������", JOptionPane.ERROR_MESSAGE);
					}
			    }
			}
		});
		return templateItem;
    }

    private void initButtonsPupupMenu() {
        jbtnSaveAs.addMenuItem(createSaveAsPopupMenu());
        createToInstalPopupMenu();
        jbtnCalculateAccessList.addMenuItem(createCalculateAccessListPopupMenu());
        jbtnCalculateAccessList.addMenuItem(createMultiCalculateAccessListPopupMenu());
        
    }
    
    private JMenuItem createSaveAsPopupMenu() {
    	JMenuItem saveAsCheckPopupItem = new JMenuItem("��������� ����������");
    	saveAsCheckPopupItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae) {
				AccessConfig partialAccessConfig = new AccessConfig();
				partialAccessConfig.setPartial(true);
				boolean hasChanges = false;
				for(Template template: m_config.getTemplate()){
					for(Permission permission : template.getPermission()){
						if(!permission.getOperationChanges().isEmpty()
								|| !permission.getWfmChanges().isEmpty()){
							hasChanges = true;
							Permission partialPermission = new Permission();
							partialPermission.setRule(permission.getRule());
							partialPermission.setStatus(permission.getStatus());
							partialPermission.getOperations().setOperations(permission.getOperationChanges());
							partialPermission.getWfMoves().setWfMove(permission.getWfmChanges());
							if(!partialAccessConfig.containsTemplate(template.getTemplate_id())){
								Template partialTemplate = new Template();
								partialTemplate.setTemplate_id(template.getTemplate_id());
								partialTemplate.setName(template.getName());
								partialTemplate.setStatus(template.getStatus());
								partialTemplate.setRules(template.getRules());
								partialTemplate.setWFMoveType(template.getWFMoveType());
								partialAccessConfig.addTemplate(partialTemplate);
							}
							partialAccessConfig.getTemplate(template.getTemplate_id()).addPermission(partialPermission);
						}
					}
					for(Rule rule: template.getRules().getRule()){
						if(rule.getAction() != null){
							hasChanges = true;
						}
					}
				}
				if(!hasChanges){
					JOptionPane.showMessageDialog(EditConfigMainForm.this, "������ ���� �� �������� ��������� �� ��������� � ��������!", "Warning",
		                    JOptionPane.WARNING_MESSAGE);
					return;
				}
				try{
	            //��������� ���������� ���� ���������� �����
		            int ret = fileopen.showSaveDialog(mainForm);
		            //���� ��� ������ ������� � ���� ��� ����������
		            if (ret == JFileChooser.APPROVE_OPTION) {
		                //���������� ���� � �����
		                m_file = fileopen.getSelectedFile().getAbsolutePath();

			            if(!fileopen.getSelectedFile().exists()) {
				            if (!m_file.endsWith(".xml")) {
					            m_file = m_file + ".xml";
				            }
			            }
		                partialAccessConfig.validate();
		                OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(m_file), "UTF-8");
		                partialAccessConfig.marshal(writer);
		                writer.close();
		                updateFileInfo();
		                //dispose();
		            }
		        } catch (Exception ex) {
		            JOptionPane.showMessageDialog(mainForm, "������ ���������� ������: " + ex.getMessage(), "Error",
		                    JOptionPane.ERROR_MESSAGE);
		            ex.printStackTrace();
		        }

			}
		});
		return saveAsCheckPopupItem;
    }

    private void createToInstalPopupMenu() {
    	JMenuItem toInstalCheckPopupItem = new JMenuItem("��������� � �� ����������");
    	toInstalCheckPopupItem.addActionListener(new EditConfigMainForm_jbtnToInstal_actionAdapter(this, true));
    	jbtnToInstal.addMenuItem(toInstalCheckPopupItem);
    	
    	JMenuItem multiDbInstallPopupMenu = new JMenuItem("�������� � ���. �� (������ ����������)");
    	multiDbInstallPopupMenu.addActionListener(new EditConfigMainForm_jbtnMultiDbToInstal_actionAdapter(this));
    	jbtnToInstal.addMenuItem(multiDbInstallPopupMenu);
    }

    private JMenuItem createCalculateAccessListPopupMenu() {
    	JMenuItem calculateAccessListToCheckPopupItem = new JMenuItem("�������� access_list ��� ����������");
    	calculateAccessListToCheckPopupItem.addActionListener(new EditConfigMainForm_jbtnCalculateAccessList_actionAdapter(this,true));
		return calculateAccessListToCheckPopupItem;
    }
    
    private JMenuItem createMultiCalculateAccessListPopupMenu() {
    	JMenuItem calculateMultiAccessListToCheckPopupItem = new JMenuItem("�������� access_list ��� ���������� �� ���� ���. ��");
    	calculateMultiAccessListToCheckPopupItem.addActionListener(new EditConfigMainForm_jbtnMultiCalculateAccessList_actionAdapter(this));
		return calculateMultiAccessListToCheckPopupItem;
    }
    
    /** 
     * Add new template name to combo box
     */
    private void addTemplateNameToTemplateTypeComboBox(String templateName) {
        if (templateName != null && !templateName.trim().isEmpty()) {
            for (int i = 0; i < jcbTemplateType.getItemCount(); i++) {
            	String currentTemplate = (String) jcbTemplateType.getItemAt(i);
            	//Exit if such element already present
            	if (templateName.equals(currentTemplate)) {
            		return;
            	//Add new element in ascendant order
            	} else if (templateName.compareTo(currentTemplate) < 0) {
            		jcbTemplateType.insertItemAt(templateName, i);
            		return;
            	}
            }
            //We are trying to add 'biggest' template which is not already present in the list so just add it at the end
            jcbTemplateType.addItem(templateName);
        }
    }

	private String formatMessage(String message) {
		final int ROW_LENGTH = 50;
		StringBuffer buffer = new StringBuffer(message);
		int messageLength = buffer.length();
		if(messageLength > ROW_LENGTH) {
			int numberOfBreakLines = messageLength / ROW_LENGTH;
			int currentPosition = ROW_LENGTH;
			for(int i = 1; i <= numberOfBreakLines; i++) {
				
				buffer.insert(currentPosition, "\n\r");
				currentPosition = ROW_LENGTH * i;
			}
		}
		return buffer.toString();
	}					

	private void getPermissionListForCell(List<PermissionWrapper> permissionList, String statusName,
			List<Permission> permissions, String ruleName) {
		if(permissions.size() > 0){
			for(Rule rule : getRulesByRuleName(ruleName)) {
		        PermissionWrapper permissionWrapper = null;                    				        
		        Permission clonedPermission = PermissionCloner.clonePermission(permissions.get(0));
		        clonedPermission.setRule(rule.getName());
		        permissionWrapper = getPermissionWrapperForPermission(clonedPermission, rule.getName(), statusName, false);				        
		        permissionList.add(permissionWrapper);            		            		
			}
			            	
		} else {
			for(Rule rule : getRulesByRuleName(ruleName)) {
		        PermissionWrapper permissionWrapper = null;				        
		        permissionWrapper = getPermissionWrapperForPermission(null, rule.getName(), statusName, false);
		        permissionList.add(permissionWrapper);            		            		
			}
		}
	}
	
	private List<String> convertStatusNamesToStatusIds(List<String> selectedStatuses) {
		List<String> statusIds = new ArrayList<String>();
		for(String status : selectedStatuses){
			String statusId = RulesUtility.getStatusIdByStatusNameForTemplate(status, m_template);
			if(statusId != null){
				statusIds.add(statusId);
			}
		}
		return statusIds;
	}
	
    /**
     * ����� ������������ ������ � ����� � ����� ��� �������� ����������
     * @param e WindowEvent
     */
    public void this_windowOpened(WindowEvent e) {
        try {
            if (m_file != null) {
                InputStreamReader reader = new InputStreamReader(new FileInputStream(m_file), "UTF-8");
                m_config = (AccessConfig) AccessConfig.unmarshal(reader);
                reader.close();
                initForm();
                updateFileInfo();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            m_config = new AccessConfig();
        }
    }

    /**
     * ����� ��������� ���������� ������ �� ������� ����� � �� ������� ������� ��������� �������
     */
    private void initForm() {   //��� ���������� ���������� ������ �������, ������� ������ ��� ����������� �������� �� ������� "�������"
        updateAttributeList();
        Template[] templates = m_config.getTemplate();
        Set<Template> orderedTemplates = sortTemplates(templates);

    	//����� ������� ����������� ������ ��������� ��� ������
        //�������������� ������� ���������� ������
        jcbTemplateType.removeAllItems();
        for (Template template : orderedTemplates) {
            jcbTemplateType.addItem(template.getName());
        }

        initTemplateTable(orderedTemplates);
    }

    /*
     * Initialize Template table using temlate list which contains template id and name
     */
    public void initTemplateTable(Set<Template> orderedTemplates) {

        MixedTableModel tableModel = new MixedTableModel();
        tableModel.addColumn("������������");
        tableModel.addColumn("�������������");

        for(Template template : orderedTemplates){
            Object[] row = new Object[2];
            row[0] = template.getName();
            row[1] = template.getTemplate_id();            
            tableModel.addRow(row);
        }

        //�� ���� ����� ���� ������������ ������� � ���������, ������� � ���������� �� ������� �������
        jtblTemplate.setModel(tableModel);

        jtblTemplate.setAutoCreateRowSorter(true);
        TableUtility.addIntegerSorter(jtblTemplate, tableModel, new Integer[] {TEMPLATE_ID_COLUMN_INDEX});
        TableUtility.toggleFirstColumnRowSorter(jtblTemplate);
    }

    private Set<Template> sortTemplates(Template[] unsortedTemplates) {
        Set<Template> orderedTemplates = new TreeSet<Template>(new TemplateComparator());
        for(Template template : unsortedTemplates){
        	orderedTemplates.add(template);
        }
        return orderedTemplates;
    }

    //��������� ����������
    public void jbtnClose_actionPerformed(ActionEvent e) {
        if (searchChanges()) {
            int result = JOptionPane.showConfirmDialog(this,
                    "���� �� ����������� ���������.\r\n ������ ��������� ���������?'", "�����?",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (result == JOptionPane.YES_OPTION) {
                jbtnSaveAs_actionPerformed(e);
                dispose();
            } else {
                dispose();
            }

        } else {
            dispose();
        }
        saveLayoutConfiguration();
    }

    public boolean searchChanges() {
        if (m_config != null) {
            for (int i = 0; i < m_config.getTemplateCount(); i++) {
                for (int j = 0; j < m_config.getTemplate(i).getPermissionCount(); j++) {
                	for(Operation operation  :m_config.getTemplate(i).getPermission(j).getOperations().getOperations()){
                		if(operation.getAction() != null){
                			return true;
                		}
                	}
                	for(WfMove wfMove :m_config.getTemplate(i).getPermission(j).getWfMoves().getWfMove()){
                		if(wfMove.getAction() != null){
                			return true;
                		}
                	}
                }
            }
        }
        return false;
    }

    /**
     * ���� ������� ������������� ����� ��������� ����������� ������
     * @param e ItemEvent
     */
    public void jcbTemplateType_itemStateChanged(ItemEvent e) {
        String currentTemplateName = e.getItem().toString();
        // save column configuration of the current table
        // preserve double saving
        if (m_template != null && currentTemplateName.equals(m_template.getName())) {
            saveLayoutConfiguration();
        }
        for (Template templatenames : m_config.getTemplate()) {
            if (currentTemplateName.equals(templatenames.getName())) {
                m_template = templatenames;
            }
        }
        if(m_template == null){
        	logger.error("Template is null");
        }
        jrbtnPersonRuleType.setSelected(true);
        initTypeTables();
        //load column configuration for new loaded table
        loadLayoutConfiguration();
    }

    /**
     * Rounds invocaction of initTypeTables() methods with save and load layout
     * config. As initTypeTables() changes the table model, before this we need
     * to save column configuration and after that we need to load it from configuration
     * files.
     */
    private void initTypeTablesAndSaveLoadConfiguration() {
        saveLayoutConfiguration();
        initTypeTables();
        loadLayoutConfiguration();
    }   	

    /**
     * ����� ��������� ������������ � ���������� ������ ��� ������� ����������,
     * � ��� �� ��� �������� ��������� � ������ �������.
     */
    private void initTypeTables() {
        //�����
        //��� ������� "�����" ������� ����� ��������� �������-������
        MixedTableModel tableModelPermission = new MixedTableModel();
        //��������� ������ 
        
        m_template = m_config.getTemplate(m_template.getTemplate_id()); 
        		
        String addIntoArray = "";
        Map<String, List<Rule>> ruleNameToRules = new LinkedHashMap<String, List<Rule>>();
        for(Rule rule : m_template.getRules().getRule()) {
        	if(rule.getName() != null){
        		String adjustedRuleName = RulesUtility.getRuleName(rule.getName());
        		List<Rule> rulesWithTheSameName = getRulesByRuleName(adjustedRuleName);
        		ruleNameToRules.put(adjustedRuleName, rulesWithTheSameName);
        	}
        }
                
        
        int numberRulesToDisplay = ruleNameToRules.keySet().size();
        //� ��������� � ������ ������� ������� �� ����� ������� ���������� �����
        String[] toolTipStr = new String[numberRulesToDisplay];
        int statusIndex = 0;
        for (String ruleName : ruleNameToRules.keySet()) {
            //��������� ������� � ������������� ����
            tableModelPermission.addColumn(ruleName);
            RuleType ruleType = RuleTypeUtility.getRuleTypeByRuleName(ruleName, m_template);
            addIntoArray = getToolTipTextForRule(ruleName, ruleType);            
            toolTipStr[statusIndex] = addIntoArray;
            statusIndex++; 
        }
                
        //����� ������� �������� ��������� ������ ������ ���������� ��������
        for (int i = 0; i < m_template.getStatusCount(); i++) {
            //���������� ����� � ������ ������������ ����������� ������� �����
            Object[] row = new Object[numberRulesToDisplay];
            //�� ����������� �������� ������������ �����(permission)
            int index = 0;
            for (String ruleName : ruleNameToRules.keySet()) {
                //System.out.println("j-" + j + "-i-" + i);
                //���������� � ����� �������� ����� �������� �� ����������� i-�� ������� � j-�� ����
                row[index] = findPermission(ruleName, m_template.getStatus(i));
                index++;
            }
            //��������� ������ � ������ ������� � ������������� �������
            tableModelPermission.addRow((String) (m_template.getStatus(i).getName()), row);
        }
        //���������� ������� �����
        jtblPermission.setModel(tableModelPermission);
        //������������ ���������
        jtblPermission.setDefaultRenderer(ArrayList.class, new PermissionCellRenderer(m_template));        
		jtblPermission.getTableHeader().setDefaultRenderer (
				new TableHeaderCellRenderer(m_template));
		
        jtblPermission.initRowHeader();

        JTableHeader header = jtblPermission.getTableHeader();

        PermissionColumnHeaderToolTips tips = new PermissionColumnHeaderToolTips();

        // Assign a tooltip for each of the columns
        for (int c = 0; c < jtblPermission.getColumnCount(); c++) {
            TableColumn col = jtblPermission.getColumnModel().getColumn(c);
            tips.setToolTip(col, toolTipStr[c]);
        }
        header.addMouseMotionListener(new PermissionColumnHeaderMouseListener(tips));
        
        RowHeightHandler handler = new PermissionRowHeightHandler(jtblPermission);
        handler.adjustCellsHeight();
        CellWidthHandler.autoResizeColWidth(jtblPermission);

        //�������
        //��� ������� "�������" ������� ����� ��������� �������-������
        MixedTableModel tableModelStatus = new MixedTableModel();
        //��������� �������
        tableModelStatus.addColumn("��������");
        tableModelStatus.addColumn("�������������");
        //��������� ������ �������� � �������������
        for (int i = 0; i < m_template.getStatusCount(); i++) {
            Object[] row = new Object[2];
            row[0] = m_template.getStatus(i).getName();
            row[1] = m_template.getStatus(i).getStatus_id();
            tableModelStatus.addRow(row);
        }
        //���������� ������� �������
        jtblStatus.setModel(tableModelStatus);
        jtblStatus.setAutoCreateRowSorter(true);
        
        TableUtility.addIntegerSorter(jtblStatus, tableModelStatus, new Integer[] {STATUS_ID_COLUMN_INDEX});
        TableUtility.toggleFirstColumnRowSorter(jtblStatus);        

        //�������
        MixedTableModel tableModelRules = new MixedTableModel();
        tableModelRules.addColumn("������������");
        Set<String> orderedRuleNames = new TreeSet<String>();
        orderedRuleNames.addAll(ruleNameToRules.keySet());
        
        if (jrbtnPersonRuleType.isSelected()) {
            tableModelRules.addColumn("������� ������������");
            tableModelRules.addColumn("������� �����");
            tableModelRules.addColumn("������� ������������� �����");
            tableModelRules.addColumn("������� ������");
            tableModelRules.addColumn("����������� ����");

            for (String ruleName : orderedRuleNames) {
            	List<Rule> correspondingRules = getRulesByRuleName(ruleName);
            	
            	List<String> personStatusIds = RulesUtility.getPersonLinkedStatusIds(correspondingRules);
            	List<String> personStaticRoleCodes = RulesUtility.getPersonStaticRoleCodes(correspondingRules);            	
            	
            	String linkedStatusNames = getStatusNames(personStatusIds);
            	String staticRoleNames = getStaticRoleNames(personStaticRoleCodes);            	
            	//String linkedStatusIds = RulesUtility.join(correspondingRules);
            	if(correspondingRules.size() > 0){            		
	            	Rule firstRule = correspondingRules.get(0);
	                if (firstRule.getRulePerson() != null) {
	                    Object[] row = new Object[6];
	                    row[0] = ruleName;
	                    row[1] = firstRule.getRulePerson().getPersonAttributeCode()!=null?getAttributeName(firstRule.getRulePerson().getPersonAttributeCode())+" / "+firstRule.getRulePerson().getPersonAttributeCode():getAttributeName(firstRule.getRulePerson().getPersonAttributeCode());
	                    row[2] = firstRule.getRulePerson().getLink()!=null?getAttributeName(firstRule.getRulePerson().getLink())+" / "+firstRule.getRulePerson().getLink():getAttributeName(firstRule.getRulePerson().getLink());
	                    row[3] = firstRule.getRulePerson().getIntermedAttributeCode()!=null?getAttributeName(firstRule.getRulePerson().getIntermedAttributeCode())+" / "+firstRule.getRulePerson().getIntermedAttributeCode():getAttributeName(firstRule.getRulePerson().getIntermedAttributeCode());
	                    row[4] = linkedStatusNames;
	                    row[5] = staticRoleNames;
	                    tableModelRules.addRow(row);
	                }
            	}

            }
            
        } else if (jrbtnRoleRuleType.isSelected()) {
            tableModelRules.addColumn("������������� ����");
            for (String ruleName : orderedRuleNames) {
            	List<Rule> correspondingRules = getRulesByRuleName(ruleName);
            	if(correspondingRules.size() > 0){            		
	            	Rule firstRule = correspondingRules.get(0);
	            	   if (firstRule.getRuleRole() != null) {
	                       Object[] row = new Object[2];
	                       row[0] = firstRule.getRuleRole().getName();
	                       row[1] = firstRule.getRuleRole().getRoleCode();
	                       tableModelRules.addRow(row);	            	   
	            	   }
            	}
            	
            }
            
        } else if (jrbtnProfileRuleType.isSelected()) {
            tableModelRules.addColumn("������� �������");
            tableModelRules.addColumn("��������� �������");
            tableModelRules.addColumn("������� �����");
            tableModelRules.addColumn("������� ������������� �����");
            tableModelRules.addColumn("������� ������");
            tableModelRules.addColumn("����������� ����");
            for (String ruleName : orderedRuleNames) {
            	List<Rule> correspondingRules = getRulesByRuleName(ruleName);            	
            	List<String> profileStatusIds = RulesUtility.getProfileLinkedStatusIds(correspondingRules);
            	List<String> personStaticRoleCodes = RulesUtility.getProfileStaticRoleCodes(correspondingRules);
            	
            	String linkedStatusNames = getStatusNames(profileStatusIds);
            	String staticRoleNames = getStaticRoleNames(personStaticRoleCodes);
            	
            	if(correspondingRules.size() > 0){            		
	            	Rule firstRule = correspondingRules.get(0);
	            	if (firstRule.getRuleProfile() != null) {
	                    Object[] row = new Object[7];
	                    row[0] = ruleName;
	                    row[1] = firstRule.getRuleProfile().getProfileAttributeCode()!=null?getAttributeName(firstRule.getRuleProfile().getProfileAttributeCode())+" / "+firstRule.getRuleProfile().getProfileAttributeCode():getAttributeName(firstRule.getRuleProfile().getProfileAttributeCode());
	                    row[2] = firstRule.getRuleProfile().getTargetAttributeCode()!=null?getAttributeName(firstRule.getRuleProfile().getTargetAttributeCode())+" / "+firstRule.getRuleProfile().getTargetAttributeCode():getAttributeName(firstRule.getRuleProfile().getTargetAttributeCode());
	                    row[3] = firstRule.getRuleProfile().getLinkAttributeCode()!=null?getAttributeName(firstRule.getRuleProfile().getLinkAttributeCode())+" / "+firstRule.getRuleProfile().getLinkAttributeCode():getAttributeName(firstRule.getRuleProfile().getLinkAttributeCode());
	                    row[4] = firstRule.getRuleProfile().getIntermedAttributeCode()!=null?getAttributeName(firstRule.getRuleProfile().getIntermedAttributeCode())+" / "+firstRule.getRuleProfile().getIntermedAttributeCode():getAttributeName(firstRule.getRuleProfile().getIntermedAttributeCode());
	                    row[5] = linkedStatusNames;
	                    row[6] = staticRoleNames ;
	                    tableModelRules.addRow(row);
	                }
            	}
            	
            }
            
        } else if (jrbtnDelegationRuleType.isSelected()) {
            tableModelRules.addColumn("������� �����");
            for (String ruleName : orderedRuleNames) {
            	List<Rule> correspondingRules = getRulesByRuleName(ruleName);
            	if(correspondingRules.size() > 0){            		
	            	Rule firstRule = correspondingRules.get(0);
	            	if (firstRule.getRuleDelegation() != null) {
	                    Object[] row = new Object[2];
	                    row[0] = firstRule.getRuleDelegation().getName();
	                    row[1] = firstRule.getRuleDelegation().getLinkAttributeCode()!=null?getAttributeName(firstRule.getRuleDelegation().getLinkAttributeCode())+" / "+firstRule.getRuleDelegation().getLinkAttributeCode():getAttributeName(firstRule.getRuleDelegation().getLinkAttributeCode());
	                    tableModelRules.addRow(row);	            		
	            	}
	            	
            	}
            }            
        }
        List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
        
        if(jtblRules.getRowSorter() != null){
        	sortKeys = (List<SortKey>) jtblRules.getRowSorter().getSortKeys();
        } else {
        	int columnIndexToSort = 0;
        	sortKeys.add(new RowSorter.SortKey(columnIndexToSort, SortOrder.ASCENDING));
        }
        
        jtblRules.setModel(tableModelRules);
        
        TableRowSorter<MixedTableModel> sorter = new TableRowSorter<MixedTableModel>(tableModelRules);
        jtblRules.setRowSorter(sorter);
         
        sorter.setSortKeys(sortKeys);
        sorter.sort();
        
		if (jrbtnPersonRuleType.isSelected()) {
			jtblRules.getColumnModel().getColumn(PERSON_LINKED_STATUS_COLUMN_ID).setCellRenderer(new MultipleValueCellRenderer());
			jtblRules.getColumnModel().getColumn(PERSON_STATIC_ROLE_COLUMN_ID).setCellRenderer(new MultipleValueCellRenderer());
			
		}else if (jrbtnProfileRuleType.isSelected()){
			jtblRules.getColumnModel().getColumn(PROFILE_LINKED_STATUS_COLUMN_ID).setCellRenderer(new MultipleValueCellRenderer());
			jtblRules.getColumnModel().getColumn(PROFILE_STATIC_ROLE_COLUMN_ID).setCellRenderer(new MultipleValueCellRenderer());

		}
        RowHeightHandler heightHandler = new RowHeightHandler(jtblRules);
        heightHandler.adjustCellsHeight();
        
        //��������
        MixedTableModel tableModelAttributeType = new MixedTableModel();
        tableModelAttributeType.addColumn("�������� �����");
        tableModelAttributeType.addColumn("��� ��������");
        tableModelAttributeType.addColumn("�������� ��������");
        tableModelAttributeType.addColumn("��� ��������");
        for (int i = 0; i < m_template.getAttributeRuleCount(); i++) {
            Object[] row = new Object[4];
            row[0] = m_template.getAttributeRule(i).getBlock_name_rus();
            row[1] = m_template.getAttributeRule(i).getAttribute_code();
            row[2] = m_template.getAttributeRule(i).getAttr_name_rus();
            row[3] = m_template.getAttributeRule(i).getData_type();

            tableModelAttributeType.addRow(row);
        }
        jtblAttribute.setModel(tableModelAttributeType);
        jtblAttribute.setAutoCreateRowSorter(true);
        
        //��������
        MixedTableModel tableModelWFMoveType = new MixedTableModel();
        tableModelWFMoveType.addColumn("������������");
        tableModelWFMoveType.addColumn("�������������");
        tableModelWFMoveType.addColumn("��������� ������");
        tableModelWFMoveType.addColumn("�������� ������");

        for (int i = 0; i < m_template.getWFMoveTypeCount(); i++) {
            Object[] row = new Object[4];
            row[0] = m_template.getWFMoveType(i).getName();
            row[1] = m_template.getWFMoveType(i).getWfm_id();
            row[2] = m_template.getWFMoveType(i).getWfm_from();
            row[3] = m_template.getWFMoveType(i).getWfm_to();
            tableModelWFMoveType.addRow(row);
        }
        jtblWorkflowMove.setModel(tableModelWFMoveType);
        jtblWorkflowMove.setAutoCreateRowSorter(true);        
        TableUtility.addIntegerSorter(jtblWorkflowMove, tableModelWFMoveType, new Integer[] {WFMOVE_ID_COLUMN_INDEX});
        TableUtility.toggleFirstColumnRowSorter(jtblWorkflowMove);
    }

    private String getAttributeName(String attributeCode) {
        return attributeNames.getAttributeName(attributeCode);
    }

    private String getStatusNames(List<String> personStatusIds) {
    	Set<String> statusNames = getStatusNamesByStatusIds(personStatusIds, m_config);
		return convertToString(statusNames);
	}

	public static Set<String> getStatusNamesByStatusIds(List<String> personStatusIds, AccessConfig accessConfig) {
		Set<String> statusNames = new HashSet<String>();
		for(String statusId : personStatusIds) {
			String statusName = AccessConfigUtility.getStatusNameByStatusId(statusId, accessConfig);
			statusNames.add(statusName);
		}
		return statusNames;
	}

	private String convertToString(Set<String> statusNames) {
		StringBuffer buffer = new StringBuffer();
		int index = 0;
		for(String statusName : statusNames) {
			if(statusName != null){
				buffer.append(statusName);
				if(index < statusNames.size() - 1) {
					buffer.append(STATUS_NAME_SEPARATOR);
				}
			index++; 
		}
		}
		return buffer.toString();
	} 
    
	private String getStaticRoleNames(List<String> roleCodes) {
		Set<String> roleNames = getRoleNamesByRoleCodes(roleCodes, m_config);
		return convertToString(roleNames);
	}

	private Set<String> getRoleNamesByRoleCodes(List<String> roleCodes, AccessConfig accessConfig) {
		Set<String> roleNames = new HashSet<String>();
		for(String roleCode : roleCodes) {
			String roleName = getAttributeName(roleCode);
			roleNames.add(roleName);			
		}
		return roleNames;
	}

	private String getToolTipTextForRule(String ruleName, RuleType ruleType) {
		String prefix = null;
		if ("NO_RULE".equals(ruleName)) {
			return "����������� - ��� ������������ �������";
		}		
		switch(ruleType){
			case Person: 
				prefix = "������������ - ";
				break;
			case Profile: 
				prefix = "���������� - ";
				break;
			case Role: 
				prefix = "����������� - " ;
				break;
			case Delegation: 
				prefix = "������������� - ";
				break;
		}
		
		String suffix = getNumberOfRules(ruleName);
		return prefix + ruleName + suffix;		
	}

	private String getNumberOfRules(String ruleName) {
		String suffix = "";
		List<Rule> rules = RulesUtility.getRulesByRuleNameFromModel(ruleName, m_template);
		if(rules.size() > 1) {
			suffix = "(" + rules.size() + ")";
		}
		return suffix;
	}
	
    //����� ���������� ������� ���� �� ��������� ��� ������������� ���� � �������
    private List<Permission> findPermission(String ruleName, Status status) {
    	
		List<Rule> rules = RulesUtility.getRulesByRuleNameFromModel(ruleName, m_template);
		List<Permission> permissions = new ArrayList<Permission>();
		
		for(Rule rule : rules){
			permissions.addAll(RulesUtility.getPermissionsByRuleNameAndStatus(rule.getName(), status.getStatus_id(), m_template));
		}

        return permissions;
    }

    /**
     * ������� ������� ������� �� ������� ���� 
     */
    public void jtblPermission_keyTyped(KeyEvent e) {
        int row = jtblPermission.getSelectedRow();
        int col = jtblPermission.getSelectedColumn();
		if (row < 0 || col < 0 || m_config.getPartial()) {
			return;
		}
		if (e.getKeyChar() == 'r'||e.getKeyChar() == '�'){  
			setPermissionForCurrentCells(OperationType.READ);
   			return;
		} else if (e.getKeyChar() == 'w'||e.getKeyChar() == '�'){
			setPermissionForCurrentCells(OperationType.WRITE);
   			return;
		} else if (e.getKeyChar() == 'e'||e.getKeyChar() == '�'){
   			setPermissionForCurrentCells(OperationType.EMPTY);
   			return;
    	} else if (e.getKeyChar() == 'c'||e.getKeyChar() == '�'){
   			setPermissionForCurrentCells(OperationType.CREATE);
   			return;
    	}
    }
    
    /**
     * ���������� ����� ����� �� ���������� ������� � ������� 
     */
    private void setPermissionForCurrentCells(OperationType type){
		List<PermissionWrapper> permissionList = new ArrayList<PermissionWrapper>();
		// �������� �� ���� ���������� ��������
		for (int row : jtblPermission.getSelectedRows()){
			// � �� ���� ���������� ��������
			for (int column : jtblPermission.getSelectedColumns()){
								
				String statusName = ((MixedTableModel) jtblPermission.getModel()).getRowName(row);
				String ruleName = jtblPermission.getColumnName(column);
				List<Rule> rules = RulesUtility.getRulesByRuleNameFromModel(ruleName, m_template);
				if(OperationType.CREATE.equals(type) 
						&& (rules.get(0).getRuleDelegation() != null 
						|| rules.get(0).getRulePerson() !=null || rules.get(0).getRuleProfile() != null)){
					continue;
				}

				List<Permission> permissionsInCell = (List<Permission>) jtblPermission.getValueAt(row, column);
				
				getPermissionListForCell(permissionList, statusName, permissionsInCell, ruleName);
				
		    	Long templateId = Long.parseLong(m_template.getTemplate_id());
		    	if(!allBasePermissions.containsKey(templateId)){
		    		allBasePermissions.put(templateId, new HashSet<Permission>());
		    	}
		    	for(PermissionWrapper permissionForCell : permissionList){
		    		if(!allBasePermissions.get(templateId).contains(permissionForCell.getPermission())){
		    			allBasePermissions.get(templateId).add(PermissionCloner.clonePermission(permissionForCell.getPermission()));
		    		}
		    	}
			}
		}
		
		if (permissionList==null||permissionList.isEmpty())
			return;
		
        // ����� ���������� ������ ����, ��������� �� �����
		for(PermissionWrapper permissionWrapper : permissionList){
        	String rule = permissionWrapper.getPermission().getRule();
			String statusIdLocal = permissionWrapper.getPermission().getStatus();
			logger.debug("RuleName : " + rule + ", statusIdLocal: " + statusIdLocal);
            // ���� ������ - NO_STATUS, � ����� - read ��� read/write
			if ((type.equals(OperationType.READ) || type.equals(OperationType.WRITE))
					&&statusIdLocal.equals(TemplateDao.NO_STATUS)){
				// �� ��������� �������, ��������� � ����������
				continue;
            }
			//���� ����� CREATE � ��� �� NO_STATUS
			if (type.equals(OperationType.CREATE) && !statusIdLocal.equals(TemplateDao.NO_STATUS)){
				continue;
            }

			permissionWrapper.setPermission(PermissionCloner.clonePermission(permissionWrapper.getPermission()));
	
	        if (type.equals(OperationType.READ) || type.equals(OperationType.EMPTY)){
	        	permissionWrapper.getPermission().setWfMoves(new WfMoves());
	        }
	        
	        permissionWrapper.getPermission().setStatus(statusIdLocal);
	        
	        setOperation(permissionWrapper.getPermission(), type, m_template);
	        Iterator<WfMove> iteratorWfm = permissionWrapper.getPermission().getWfMoves().getWfMoveList().iterator();
	        while(iteratorWfm.hasNext()){
	        	WfMove wfMove = iteratorWfm.next();
	        	if(Action.REMOVE.equals(wfMove.getAction())){
	        		iteratorWfm.remove();
	        	}
	        }
	        
	        //permissionWrapper.getPermission().setCardPermission(type); echirkov
	        RulesUtility.createOperationsModification(permissionWrapper.getPermission(), m_template, allBasePermissions);
	        
            //���� ��� ��������� empty �� ������ ������� ���� �������
	        if (type.equals(OperationType.EMPTY) && permissionWrapper.getPermission().getOperations().getOperationCount() == 0)
            {
	           	logger.debug("Empty option is selected");
	           	// ������� �� ������������ ���� � �������������� ������� �������
                for (int i = 0; i < m_template.getPermissionCount(); i++)
                {
	               	if(m_template.getPermission(i) == null){
	               		continue;
	               	}
						
					if (m_template.getPermission(i).getRule().equals(rule)
						&& m_template.getPermission(i).getStatus().equals(statusIdLocal)) {
						logger.debug("Deleting permission: " + permissionWrapper.getPermission());
						m_template.removePermission(i);
						break;
					}
                }
            } //���� ����� permission (���� �����, ���-�� ���������) �� ��������� ��� � ������
            else if (permissionWrapper.isNew() && isPermissionValid(permissionWrapper.getPermission())) {
				RulesUtility.deletePermission(permissionWrapper.getPermission(), m_template);
				logger.debug("Adding permission: " + permissionWrapper.getPermission());
					
				m_template.addPermission(permissionWrapper.getPermission());
			} else {
				for (int i = 0; i < m_template.getPermissionCount(); i++) {
					if (m_template.getPermission(i) == null) {
						continue;
					}
					if (m_template.getPermission(i).getRule().equals(rule)
						&& m_template.getPermission(i).getStatus().equals(statusIdLocal)
						&& isPermissionValid(permissionWrapper.getPermission())) {
						logger.debug("Replacing permission: " + permissionWrapper.getPermission());
						m_template.setPermission(i, permissionWrapper.getPermission());
						break;
					}
				}
	        }
        }
        initTypeTablesAndSaveLoadConfiguration();
        adjustRowsHeight();
        //highlightSelectedCell(row, col);
    }
    
    //TODO: ���������� ���������� ��� ��������� �������
    protected static void setOperation(Permission permission,
			OperationType type, Template template) {
    	List<Operation> operations = new ArrayList<Operation>();
    	if(OperationType.READ.equals(type)){
    		operations.add(new Operation(OperationType.READ));
    	} else if(OperationType.WRITE.equals(type)){
    		operations.add(new Operation(OperationType.READ));
    		operations.add(new Operation(OperationType.WRITE));
    	} else if(OperationType.CREATE.equals(type)){
    		operations.add(new Operation(OperationType.CREATE));
    	}
    	if(permission.getOperations() == null){
    		permission.setOperations(new Operations());
    	}
    	permission.getOperations().setOperations(operations);
    	permission.generatePermHashes(template);
	}

	public void jbtnSave_actionPerformed(ActionEvent e) {
        try {
        	if(m_config == null){
		        JOptionPane.showMessageDialog(this, "��������� ������ ���� �� �� ��� �������� �� �����", "���������� ������ ����",
				        JOptionPane.INFORMATION_MESSAGE);
		        return;
	        }
	        m_config.validate();
	        if(m_file == null) {
		        int ret = fileopen.showSaveDialog(this);
		        //���� ��� ������ ������� � ���� ��� ����������
		        if (ret == JFileChooser.APPROVE_OPTION) {
			        //���������� ���� � �����
			        m_file = fileopen.getSelectedFile().getAbsolutePath();

			        if (!fileopen.getSelectedFile().exists()) {
				        if (!m_file.endsWith(".xml")) {
					        m_file = m_file + ".xml";
				        }
			        }
		        }
	        }
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(m_file), "UTF-8");
            m_config.marshal(writer);
            writer.close();
            updateFileInfo();
            //dispose();
        } catch (Exception ex) {
        	ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            System.out.println(ex.getMessage());
        }
    }
	
	public void jtblPermission_mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && !m_config.getPartial()) {
            int row = jtblPermission.getSelectedRow();
            int col = jtblPermission.getSelectedColumn();
			if (row < 0 || col < 0) {
				return;
			}
            String ruleName = jtblPermission.getColumnName(col);
            RuleType ruleType = RuleTypeUtility.getRuleTypeByRuleName(ruleName, m_template);
            String statusName = ((MixedTableModel)jtblPermission.getModel()).getRowName(row);
            
            List<Permission> permissions = (List<Permission>) jtblPermission.getValueAt(row, col);  
            List<PermissionWrapper> permissionList = new ArrayList<PermissionWrapper>();
            
            getPermissionListForCell(permissionList, statusName, permissions, ruleName);
            
			editPermissionSet(permissionList, ruleType, statusName, true);            
            adjustRowsHeight();
            highlightSelectedCell(row, col);
        }
    }

	private PermissionWrapper getPermissionWrapperForPermission(Permission permissionItem, String rule, String statusName, boolean isNew) {
		PermissionWrapper permissionWrapper;
		if (permissionItem == null) {
			Permission newPermission = new Permission();
			newPermission.setRule(rule);
			String statusId = getStatusIdByStatusName(statusName);			
			newPermission.setStatus(statusId);
			permissionWrapper = new PermissionWrapper(newPermission, true);
			return permissionWrapper;
		} else {
			permissionWrapper = new PermissionWrapper(permissionItem, false);
		}
		if(isNew){
			permissionWrapper = new PermissionWrapper(permissionItem, true);			
		}
		//It is expected that rule is stored in {@link Permission} object while reading from xml. But if rule is not stored in xml (by any reason),
		// we will define it explicitly after editing {@link Permission} in Edit Permission form. //TODO Get rid of this code
		permissionWrapper.setRule(rule);
		return permissionWrapper;
	}

    private void highlightSelectedCell(int row, int col)
    {
        jtblPermission.setRowSelectionInterval(row, row);
        jtblPermission.setColumnSelectionInterval(col, col);
    }

    private void adjustRowsHeight() {
    	RowHeightHandler handler = new PermissionRowHeightHandler(jtblPermission);
        handler.adjustCellsHeight();
    }

    private void editPermissionSet(List<PermissionWrapper> permissionList, RuleType ruleType, String status, boolean isOneRule)
    {        
    	Long templateId = Long.parseLong(m_template.getTemplate_id());
    	if(!allBasePermissions.containsKey(templateId)){
    		allBasePermissions.put(templateId, new HashSet<Permission>());
    	}
    	for(PermissionWrapper permissionWrapper : permissionList){
    		if(!allBasePermissions.get(templateId).contains(permissionWrapper.getPermission())){
    			allBasePermissions.get(templateId).add(PermissionCloner.clonePermission(permissionWrapper.getPermission()));
    		}
    	}
    	
        ObjectPermissionForm permissionForm = new ObjectPermissionForm(this, m_template, permissionList, ruleType, status, isOneRule);
        permissionForm.setAllBasePermissions(allBasePermissions);
        permissionForm.setVisible(true);
        
        for(PermissionWrapper permissionWrapper : permissionList){
        	//System.out.println("Editing permission " + permissionWrapper.getPermission());       	
        	
        	String rule = permissionWrapper.getPermission().getRule();
			String statusIdLocal = permissionWrapper.getPermission().getStatus();
			logger.debug("RuleName : " + rule + ", statusIdLocal: " + statusIdLocal);
        	
			Permission perm = permissionForm.getPermission();
			permissionWrapper.getPermission().generatePermHashes(m_template);
	        RulesUtility.createOperationsModification(permissionWrapper.getPermission(), m_template, EditConfigMainForm.allBasePermissions);
        	//��������� ���� ������ �������
	        if (perm != null)
	        {        	
	            //���� ��� ��������� empty �� ������ ������� ���� �������
	            if ("empty".equalsIgnoreCase(perm.resolveOperationType().toString()) && perm.getOperations().getOperationCount() == 0)
	            {
	            	logger.debug("Empty option is selected");
	                //������� �� ������������ ���� � �������������� ������� �������
                	for (int i = 0; i < m_template.getPermissionCount(); i++)
	                {
	                	if(m_template.getPermission(i) == null){
	                		continue;
	                	}
						
						if (m_template.getPermission(i).getRule().equals(rule)
								&& m_template.getPermission(i).getStatus().equals(statusIdLocal)) {
							logger.debug("Deleting permission: " + permissionWrapper.getPermission());
							m_template.removePermission(i);
							break;
						}
	                }
	            } //���� ������ ��� �������� �� ��������� ��� � ������
	            else if (permissionWrapper.isNew() && isPermissionValid(permissionWrapper.getPermission())) {
						RulesUtility.deletePermission(permissionWrapper.getPermission(), m_template);
						logger.debug("Adding permission: " + permissionWrapper.getPermission());
						
						m_template.addPermission(permissionWrapper.getPermission());
					} else {
					for (int i = 0; i < m_template.getPermissionCount(); i++) {
						if (m_template.getPermission(i) == null) {
							continue;
						}
						if (m_template.getPermission(i).getRule().equals(rule)
								&& m_template.getPermission(i).getStatus().equals(statusIdLocal)
								&& isPermissionValid(permissionWrapper.getPermission())) {
							logger.debug("Replacing permission: " + permissionWrapper.getPermission());
							m_template.setPermission(i, permissionWrapper.getPermission());
							break;
						}
					}
	            }
	        }
        }
        initTypeTablesAndSaveLoadConfiguration();
    }

	private String getStatusIdByStatusName(String status) {
		for (int i = 0; i < m_template.getStatusCount(); i++) {
			if (status.equals(m_template.getStatus(i).getName())) {
				return m_template.getStatus(i).getStatus_id();
			}
		}
		return null;
	}

	/**
	 * Validates that all required fields in {@link Permission} are filled in.
	 * @param permission {@link Permission} to be validated
	 * @return <code> true </code> if valid, <code> false </code> otherwise.
	 */
	private boolean isPermissionValid(Permission permission) {
		boolean isValid = true;
		isValid &= permission.getRule() != null;
		isValid &= permission.getStatus() != null;
		isValid &= permission.getOperations().getOperationCount() > 0;
		if(!isValid) {
			System.out.println("Permission not valid: " + permission);			
		}
		return isValid;
	}

    //����� ���������� ����
    public void jbtnRulesAdd_actionPerformed(ActionEvent e) {
        //���������� �������� ������ ���������� ���� ���� new RulesForm(this, m_template, -1, 1); -��������� �����
        //� ���������� �����
    	List<Rule> listOfRules = new ArrayList<Rule>();

    	int selectedRoleType = getSelectedRuleType();
        if (selectedRoleType != RulesUtility.UNDEFINED_RULE_TYPE) {
            RulesForm rulesForm = new RulesForm(this, m_config, m_template, listOfRules, attributeNames, selectedRoleType, jcbTemplateType.getSelectedIndex());
            rulesForm.setVisible(true);
        }

        initTypeTablesAndSaveLoadConfiguration();
    }

    /**
     * Opens new window for editing rule (set of rules).
     * @param e
     */
    public void jbtnRulesEdit_actionPerformed(ActionEvent e) {
        editRule();
    }
    
    /**
     * Opens edit rule dialog, which is also used in Rule Table double mouse click event handler. 
     */
	public void editRule() {
        int selectedRow = jtblRules.getSelectedRow();
		if (selectedRow < 0) {
			return;
		}
		String ruleName = null;
		if (selectedRow >= 0) {
			ruleName = (String)jtblRules.getValueAt(selectedRow, 0);
		}
		List<Rule> rulesToEdit = getRulesByRuleName(ruleName);
		
        int selectedRoleType = getSelectedRuleType();
        if (selectedRoleType != RulesUtility.UNDEFINED_RULE_TYPE) {
            RulesForm rulesForm = new RulesForm(this, m_config, m_template, rulesToEdit, attributeNames, selectedRoleType, jcbTemplateType.getSelectedIndex());
            rulesForm.setVisible(true);
        }
        initTypeTablesAndSaveLoadConfiguration();
    }

    /**
     * Method deletes selected rule with showing confirmation dialog
     * @param e
     */
    public void jbtnRulesDelete_actionPerformed(ActionEvent e) {
        if (jtblRules.getSelectedRow() != -1) {
            int result = JOptionPane.showConfirmDialog(this,
                    "�� ������������� ������� ������� �������� "
                    + jtblRules.getValueAt(jtblRules.getSelectedRow(), 0)
                    + "?", "�������������",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

    		int selectedRow = jtblRules.getSelectedRow(); // - 1;
    		String ruleName = (String)jtblRules.getValueAt(selectedRow, 0);
			if (result == JOptionPane.YES_OPTION) {
				deleteRuleFromModel(selectedRow, ruleName);
				initTypeTablesAndSaveLoadConfiguration();				
			}

            initTypeTablesAndSaveLoadConfiguration();
        }
    }

    /*
     * Copy selected rule to the local Clipboard
     */
    public void jbtnRulesCopy_actionPerformed(ActionEvent e) {
    	List<Rule> selectedRules = getSelectedRules();
		if (selectedRules.size() == 0) {
			return;
		}
		Rule rule = selectedRules.get(0);
		RuleContainer ruleContainer = new RuleContainer(RuleCloner.clone(rule));
		ruleContainer.setPermissions(RulesUtility.getPermissionsByRuleName(rule.getName(), m_template));
		
		localClipboard.setContents(ruleContainer, null);
		if (logger.isDebugEnabled()) {
			logger.debug("Rule " + ruleContainer.getRule().getName() + " is copied into local clipboard");
		}
    }

    /*
     * Paste rule from local Clipboard is appropriate Rule Type is selected
     */
    public void jbtnRulesPaste_actionPerformed(ActionEvent e) {

		RuleContainer ruleContainer = (RuleContainer) localClipboard.getContents(null);
		if (ruleContainer != null) {
			if (RulesUtility.getRuleType(ruleContainer.getRule()) == getSelectedRuleType()) {

				if (ruleContainer.getRule() != null) {
					Rule newRule = RuleCloner.clone(ruleContainer.getRule());
		        	for(Rule rule : m_template.getRules().getRule()){
		        		if(rule.getRuleHash().equals(newRule.getRuleHash())){
		        	        JOptionPane.showConfirmDialog(this,
		        	                "������ ������� ��� ����������", "������!!!",
		        	                JOptionPane.CLOSED_OPTION,
		        	                JOptionPane.WARNING_MESSAGE);
		        			return;
		        		}
		        	}
					
					String newRuleName = newRule.getName();
					
					while (getRulesByRuleName(newRuleName).size() > 0) {
						newRuleName = RulesUtility.addCopySuffix(newRuleName);
					}
					RulesUtility.updateRuleName(newRule, newRuleName);
					m_template.getRules().addRule(newRule);
					if (logger.isDebugEnabled()) {
						logger.debug("Adding rule: " + newRule.getName());
					}
					int count = 0;
					for(Permission permission: ruleContainer.getPermissions()){
						for(Status status: m_template.getStatus()){
							if(status.getStatus_id().equals(permission.getStatus())){
								Permission clonedPermission = PermissionCloner.clonePermission(permission);
								clonedPermission.setRule(newRuleName);
								clonedPermission.generatePermHashes(m_template);
								Iterator<Operation> iteratorOp = clonedPermission.getOperations().getOperationList().iterator();
								while(iteratorOp.hasNext()){
									Operation operation = iteratorOp.next();
									if(Action.REMOVE.equals(operation.getAction())){
										iteratorOp.remove();
									}
								}
								Iterator<WfMove> iteratorWfm = clonedPermission.getWfMoves().getWfMoveList().iterator();
								while(iteratorWfm.hasNext()){
									WfMove wfMove = iteratorWfm.next();
									if(Action.REMOVE.equals(wfMove.getAction())){
										iteratorWfm.remove();
									}
								}
								RulesUtility.createOperationsModification(clonedPermission, m_template, allBasePermissions);
								m_template.addPermission(clonedPermission);
								count ++;
								break;
							}
						}
					};
					if(count < ruleContainer.getPermissions().size()){
	        	        JOptionPane.showConfirmDialog(this,
	        	                "�� ��� ����� ��� ������� ���� ����������� ��-�� ����������� � ������� ��������", "����������!!!",
	        	                JOptionPane.CLOSED_OPTION,
	        	                JOptionPane.WARNING_MESSAGE);
					}
					initTypeTablesAndSaveLoadConfiguration();
				}
			}
		}
    }

	/**
	 * Method returns selected Role Type ("��� �������") which is represended as Radio Button element:
	 * 1 - ������������
	 * 2 - ����
	 * 3 - �������
	 * 4 - �������������
	 * @return selected role type as int value
	 */
    public int getSelectedRuleType() {
        int type = RulesUtility.UNDEFINED_RULE_TYPE;

		if (jrbtnPersonRuleType.isSelected()) {
            type = RulesUtility.PERSON_RULE_TYPE;
        } else if (jrbtnRoleRuleType.isSelected()) {
        	type = RulesUtility.ROLE_RULE_TYPE;
        } else if (jrbtnProfileRuleType.isSelected()) {
            type = RulesUtility.PROFILE_RULE_TYPE;
        } else if (jrbtnDelegationRuleType.isSelected()) {
            type = RulesUtility.DELEGATION_RULE_TYPE;
        }
		return type;
	}

	/**
	 * Method returns selected roles from table. If no one rule is selected Empty list will be returned.
	 * @return List of Rules which are selected or emptyList if it's not
	 */
	private List<Rule> getSelectedRules() {
        int selectedRow = jtblRules.getSelectedRow();
		if (selectedRow < 0) {
			return Collections.emptyList();
		}
		String ruleName = null;
		if (selectedRow >= 0) {
			ruleName = (String)jtblRules.getValueAt(selectedRow, 0);
		}
		return getRulesByRuleName(ruleName);
	}
	
    public void copyRules(ActionEvent e) {
    	TemplatesForm templateForm = new TemplatesForm(this, m_config, m_template);
        templateForm.setVisible(true);
        String selectedTemplateId = templateForm.getSelectedTemplateId();
        Template selectedTemplate = findTemplateById(selectedTemplateId);
        
        List<Rule> clonedRules = RulesUtility.copyRules(selectedTemplate);
		if (!keepOldRules.isSelected()) {
			m_template.getRules().setRule(clonedRules.toArray(new Rule[] {}));
		} else {
			addRulesToTemplate(clonedRules, selectedTemplate);
		}
        initTypeTablesAndSaveLoadConfiguration();
    }

	private void addRulesToTemplate(List<Rule> clonedRules, Template selectedTemplate) {
		for(Rule newRule : clonedRules){
			String newRuleName = newRule.getName() + "_" + selectedTemplate.getName();
			newRule.setName(newRuleName);
			deleteExistingRule(newRuleName);
			m_template.getRules().addRule(newRule);
		}
	}

	/**
	 * Deletes existing rules from {@link Template} with the name newRuleName. Prevents adding rules from the same {@link Template} more than one time.
	 * @param newRuleName the name of {@link Rule} which will be added/
	 * @return
	 */
	private void deleteExistingRule(String newRuleName) {
		int index = 0;
		for (Rule rule : m_template.getRules().getRule()) {
			if (newRuleName.equals(rule.getName())) {
				m_template.getRules().removeRule(index);
			}
			index++;
		}
	}

	private Template findTemplateById(String selectedTemplateId) {
		for(Template template : m_config.getTemplate()){
        	if(template.getTemplate_id().equals(selectedTemplateId)){
        		return template;
        	}
        }
		throw new IllegalArgumentException("No one template found with templateId: " + selectedTemplateId);		
	}

    /**
     * Finds {@link Rule}s with name which begins with the passed ruleName.
     * @param ruleName used to find rules.
     * @return {@link List} of found {@link Rule}.
     */
	private List<Rule> getRulesByRuleName(String ruleName) {
		return RulesUtility.getRulesByRuleNameFromModel(ruleName, m_template);		
	}

    //���������� ����� � ������ �������������� ����
    private void renameRules(String oldName, String newName) {
        for (int j = 0; j < m_template.getPermissionCount(); j++) {
            if (m_template.getPermission(j).getRule().equalsIgnoreCase(oldName)) {
                m_template.getPermission(j).setRule(newName);
            }
        }
    }

	private void deleteRuleFromDataBase(int selectedRow) throws DbException {
		String ruleName = (String)jtblRules.getModel().getValueAt(selectedRow, 0);
		try {
			DataSource dataSource = ConnectionFactory.getDataSource(jtxtfURL.getText(), jtxtfUserName.getText(), jtxtfPassword.getText());
            AccessListDao accessListDao = AccessListDao.getInstance(dataSource);
			Set<Rule> rulesToDelete = accessListDao.getRulesByRuleName(ruleName, m_template);
			accessListDao.clearRulesByRuleName(rulesToDelete, m_template, false);
		}catch(DbException ex) {
			throw ex;
		}
	}

	private void deleteRuleFromModel(int ruleIndex, String ruleName) {
		
		List<Rule> rulesToDelete = RulesUtility.getRulesByRuleNameFromModel(ruleName, m_template);
		List<Permission> permissionsToDelete = new ArrayList<Permission>();
		
		for(Rule rule : rulesToDelete){
			permissionsToDelete.addAll(RulesUtility.getPermissionsByRuleName(rule.getName(), m_template));
		}
			
		int removedPermissions = 0;
		for(Permission permission : permissionsToDelete) {
			removedPermissions += RulesUtility.deletePermission(permission, m_template);
		}
		RulesUtility.deleteRules(rulesToDelete, m_template);
				
	}
	
	/**
	 * ��������� ���������� � ������ �������
	 */
	public void updateAppVersion(String version) {
		jLabelAppVersion.setText(version);
	}

    /**
     **��������� ���������� � ������� �����, � ������� �������� xml ����
     */
	private void updateFileInfo(){
    	File f = new File(m_file);
    	if (f==null){
    		jLabel6.setText("���� �� �����");
    		return;
    	}
    	String name = f.getName();
    	String modified = new Date(f.lastModified()).toString();
		jLabel6.setText(MessageFormat.format("����: {0}. ��������� ���������: {1}", name, modified));
    }
	
	//����� ���������� ��������
    public void jbtnAttributeAdd_actionPerformed(ActionEvent e) {
        AttributeForm rulesForm = new AttributeForm(this, m_template, -1);
        rulesForm.setVisible(true);
        initTypeTables();
    }

    //����� �������������� ��������
    public void jbtnAttributeEdit_actionPerformed(ActionEvent e) {
        int attributeIndex = jtblAttribute.getSelectedRow();
        String oldName = m_template.getAttributePermissionType(attributeIndex).getName();
        String oldAttributeCode = m_template.getAttributePermissionType(attributeIndex).getAttr_code();
        String oldOperCode = m_template.getAttributePermissionType(attributeIndex).getOper_code();
        String oldDescr = m_template.getAttributePermissionType(attributeIndex).getDescription();

        AttributeForm attributeForm = new AttributeForm(this, m_template, jtblAttribute.getSelectedRow());
        attributeForm.setVisible(true);
        String newName = m_template.getAttributePermissionType(attributeIndex).getName();
        String newAttributeCode = m_template.getAttributePermissionType(attributeIndex).getAttr_code();
        String newOperCode = m_template.getAttributePermissionType(attributeIndex).getOper_code();
        String newDescr = m_template.getAttributePermissionType(attributeIndex).getDescription();

        if (!oldName.equalsIgnoreCase(newName) || !oldDescr.equalsIgnoreCase(newDescr)
                || !oldAttributeCode.equalsIgnoreCase(newAttributeCode) || !oldOperCode.equalsIgnoreCase(newOperCode)) {
            renameAttribute(oldName, oldAttributeCode, oldOperCode, newName, newAttributeCode, newOperCode);
        }
        initTypeTables();
    }

    //����� �������������� ��������, ���������� ������ �������������� ��������
    private void renameAttribute(String oldName, String oldAttributeCode, String oldOperCode, String newName,
            String newOperCode, String newAttributeCode) {
        for (int i = 0; i < m_template.getPermissionCount(); i++) {
            for (int j = 0; j < m_template.getPermission(i).getAttributes().getAttributeCount(); j++) {
                if (m_template.getPermission(i).getAttributes().getAttribute(j).getName().equalsIgnoreCase(oldName)
                        && m_template.getPermission(i).getAttributes().getAttribute(j).getAttr_code().equalsIgnoreCase(
                        oldAttributeCode)
                        && m_template.getPermission(i).getAttributes().getAttribute(j).getOper_code().equalsIgnoreCase(
                        oldOperCode)) {
                    m_template.getPermission(i).getAttributes().getAttribute(j).setName(newName);
                    m_template.getPermission(i).getAttributes().getAttribute(j).setAttr_code(newAttributeCode);
                    m_template.getPermission(i).getAttributes().getAttribute(j).setOper_code(newOperCode);
                }
            }
        }
    }

    //����� �������� ��������
    public void jbtnAttributeDelete_actionPerformed(ActionEvent e) {
        if (jtblAttribute.getSelectedRow() != -1) {
            int result = JOptionPane.showConfirmDialog(this,
                    "�� ������������� ������� ������� �������� '"
                    + jtblAttribute.getValueAt(jtblAttribute.getSelectedRow(), 0)
                    + "'?", "�������������",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (result == JOptionPane.YES_OPTION) {
                int attributeIndex = jtblAttribute.getSelectedRow(); // - 1;
                String attributeName = m_template.getAttributePermissionType(attributeIndex).getName();
                m_template.removeAttributePermissionType(attributeIndex);
                deleteAttribute(attributeName);
                initTypeTables();
            }
        }
    }

    //����� ���������� ������ �������� �������� ������� �������� �� ����(permission)
    private void deleteAttribute(String attributeName) {
        for (int i = 0; i < m_template.getPermissionCount(); i++) {
            for (int j = 0; j < m_template.getPermission(i).getAttributes().getAttributeCount(); j++) {
                if (m_template.getPermission(i).getAttributes().getAttribute(j).getName().toString().equals(attributeName)) {
                    m_template.getPermission(i).getAttributes().removeAttribute(j);
                }
            }
        }
    }

    public void jrbtnPersonRuleType_stateChanged(ActionEvent e) {
        if (jrbtnPersonRuleType.isSelected()) {
            jrbtnRoleRuleType.setSelected(false);
            jrbtnProfileRuleType.setSelected(false);
            jrbtnDelegationRuleType.setSelected(false);
            initTypeTables();
        }        
    }

    public void jrbtnRoleRuleType_stateChanged(ActionEvent e) {
        if (jrbtnRoleRuleType.isSelected()) {
            jrbtnPersonRuleType.setSelected(false);
            jrbtnProfileRuleType.setSelected(false);
            jrbtnDelegationRuleType.setSelected(false);
            initTypeTables();
        }
        
    }

    public void jrbtnProfileRuleType_stateChanged(ActionEvent e) {
        if (jrbtnProfileRuleType.isSelected()) {
            jrbtnRoleRuleType.setSelected(false);
            jrbtnPersonRuleType.setSelected(false);
            jrbtnDelegationRuleType.setSelected(false);
            initTypeTables();
        }        
    }

    public void jrbtnDelegationRuleType_stateChanged(ActionEvent e) {
        if (jrbtnDelegationRuleType.isSelected()) {
            jrbtnRoleRuleType.setSelected(false);
            jrbtnPersonRuleType.setSelected(false);
            jrbtnProfileRuleType.setSelected(false);
            initTypeTables();
        }
    }

    public void jbtnSaveAs_actionPerformed(ActionEvent e) {
        saveAccessRuleConfig();
    }

    /**
     * Saves current access rules config in a file.
     */
	private void saveAccessRuleConfig() {
		try {            
			if (m_config == null) {
			    JOptionPane.showMessageDialog(this, "������ ������! ������ ��� ���������, ��������� ����� �������.", "Warning",
			            JOptionPane.INFORMATION_MESSAGE);
			    return;
			}
            //��������� ���������� ���� ���������� �����
            int ret = fileopen.showSaveDialog(this);
            //���� ��� ������ ������� � ���� ��� ����������
            if (ret == JFileChooser.APPROVE_OPTION) {
                //���������� ���� � �����
                m_file = fileopen.getSelectedFile().getAbsolutePath();

	            if(!fileopen.getSelectedFile().exists()) {
		            if (!m_file.endsWith(".xml")) {
			            m_file = m_file + ".xml";
		            }
	            }

                m_config.validate();
                OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(m_file), "UTF-8");
                m_config.marshal(writer);
                writer.close();
                updateFileInfo();
                //dispose();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "������ ���������� ������: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /**
     * Saves the configuration (position and width) of colums of Permission {@JTable}.
     */
    public void saveLayoutConfiguration() {
        if (m_template != null && jtblPermission != null) {
            ColumnRowConfig.saveAdjustments(jtblPermission, m_template.getName());
        }
    }

    /**
     * Loads the configuration (position and width) of colums of Permission {@JTable}.
     */
    public void loadLayoutConfiguration() {
        if (m_template != null && jtblPermission != null) {
            ColumnRowConfig.loadAdjustments(jtblPermission, m_template.getName());
        }
    }

    public void jbtnOpenFile_actionPerformed(ActionEvent e) {
    	saveAccessRulesIfDirty();    	
    	openXMLFileInApplication();
    }

    public void jbtnToInstal_actionPerformed(ActionEvent e, boolean onlyChanges) {
    	if (m_config == null) {
		    JOptionPane.showMessageDialog(this, "������ ������! ������ ��� ���������, ��������� ����� �������.", "Warning",
		            JOptionPane.INFORMATION_MESSAGE);
		    return;
		}
    	
    	if (m_config.getPartial() && !onlyChanges) {
		    JOptionPane.showMessageDialog(this, "��������� ��������� ������ ����! �������� �� � �������� ������ ���������.", "Warning",
		            JOptionPane.INFORMATION_MESSAGE);
		    return;
		}
    	
    	String alarmString;
    	
    	if(onlyChanges){
    		alarmString = "�� ��������� ��������� ��������� ������� � ����.\r\n ����� ������������ ������ ��� ���������� ������!!!'";
    	} else {
    		alarmString = "�� ��������� ��������� �������� � ����.\r\n ��� ������ � ���� ����� ������������!!!'";
    	}
    	
        int result = JOptionPane.showConfirmDialog(this,
        		alarmString, "�������� !!!",
                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            try {
                final DoSaveAccessRules saveAccessRules = new DoSaveAccessRules(jtxtfURL.getText(), jtxtfUserName.getText(), jtxtfPassword.getText(), m_config);
                saveAccessRules.setOnlyChanges(onlyChanges);

                progressMonitor = new ProgressMonitor(this, "�������� � ����", "", 0, 100);
                progressMonitor.setMillisToDecideToPopup(10);
                progressMonitor.setMillisToPopup(10);
                progressMonitor.setProgress(0);

                saveAccessRules.addPropertyChangeListener(new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (progressMonitor.isCanceled()) {
                            saveAccessRules.cancel(true);
                            JOptionPane.showMessageDialog(EditConfigMainForm.this, "�������� �������������", "������ ��������", JOptionPane.ERROR_MESSAGE);
                            jbtnToInstal.setEnabled(true);
                            return;
                        }

                        if ("progress".equals(evt.getPropertyName())) {
                            int progress = (Integer) evt.getNewValue();
                            progressMonitor.setProgress(progress);
                            progressMonitor.setNote(saveAccessRules.getCurrentTemplate());
                        }

                        if ("state".equals(evt.getPropertyName())) {
                            if (saveAccessRules.isDone()) {
                                Exception exception = null;
                                try {
                                    exception = saveAccessRules.get();
                                } catch (Exception e1) {
                                    exception = e1;
                                }
                                if (exception != null) {
                                    // �������� � �������
                                    exception.printStackTrace();
                                    JOptionPane.showMessageDialog(EditConfigMainForm.this, exception.getMessage(), "������ ��������", JOptionPane.ERROR_MESSAGE);
                                } else {
                                    JOptionPane.showMessageDialog(EditConfigMainForm.this, "�������� ������� ���������", "Access Rule", JOptionPane.INFORMATION_MESSAGE);
                                }
                                jbtnToInstal.setEnabled(true);
                            }
                        }
                    }
                });

                // ��������� �������� � �� ��������� �������
                saveAccessRules.execute();
                jbtnToInstal.setEnabled(false);
                
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "������ �������� ������: " + ex.getMessage(), "������ ��������",
                        JOptionPane.ERROR_MESSAGE);
            }

        }
    }

    public void jbtnCalculateAccessList_actionPerformed(ActionEvent e, boolean onlyChanges) {
    	if(m_config == null){
    		JOptionPane.showMessageDialog(null, "��������� ������� ������������" , "Warning",
                    JOptionPane.WARNING_MESSAGE);
    		return;
    	}
    	
    	String alarmString;
    	if(onlyChanges){
    		alarmString = "�� ��������� ��������� ��������� ���� ��� ���������� ������.\r\n ����� ������������ ������ ������ ��� ���������� ������!!!";
    	} else {
    		alarmString = "�� ��������� ��������� ����������� ���� ��� ����.\r\n ��� ������ � ���� ����� ������������!!!";
    	}
    	
    	int result = JOptionPane.showConfirmDialog(this,
    			alarmString, "�������� !!!",
                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
    	
    	if (result == JOptionPane.YES_OPTION) {
    		try {

   			 	final DoUpdateAccessList recalculateAccesList = new DoUpdateAccessList(jtxtfURL.getText(),
    					jtxtfUserName.getText(), jtxtfPassword.getText(), m_config);
    			recalculateAccesList.setOnlyChanges(onlyChanges);
                progressMonitor = new ProgressMonitor(this, "�������� Access List", "", 0, 100);
                progressMonitor.setMillisToDecideToPopup(10);
                progressMonitor.setMillisToPopup(10);
                progressMonitor.setProgress(0);

                recalculateAccesList.addPropertyChangeListener(new PropertyChangeListener() {
                	boolean isCancelledByUser = false;
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (progressMonitor.isCanceled()) {                        	
                        	if(!isCancelledByUser){
                        		isCancelledByUser = true;	
                            	recalculateAccesList.cancel(true);
                        		JOptionPane.showMessageDialog(EditConfigMainForm.this, "�������� �������������", "������ ��������", JOptionPane.ERROR_MESSAGE);                                
                                jbtnCalculateAccessList.setEnabled(true);
                        	}
                            return;
                        }

                        if ("progress".equals(evt.getPropertyName())) {
                            int progress = (Integer) evt.getNewValue();
                            progressMonitor.setProgress(progress);
                       //     progressMonitor.setNote(recalculateAccesList.getRuleNote());
                        } else if ("ruleNote".equals(evt.getPropertyName())) {
                        	progressMonitor.setNote((String)evt.getNewValue());
                        }
                       

                        if ("state".equals(evt.getPropertyName())) {
                            if (recalculateAccesList.isDone()) {
                                Exception exception = null;
                                try {
                                    exception = recalculateAccesList.get();
                                } catch (Exception e1) {
                                    exception = e1;
                                }
                                if (exception != null) {
                                    // �������� � �������
                                	logger.error("Exception occured while processing update access list: " + exception.getMessage());
                                    exception.printStackTrace();
                                    JOptionPane.showMessageDialog(EditConfigMainForm.this, exception.getMessage(), "������ ��������", JOptionPane.ERROR_MESSAGE);
                                } else {
                                    JOptionPane.showMessageDialog(EditConfigMainForm.this, "�������� ������� ���������", "Access Rule", JOptionPane.INFORMATION_MESSAGE);
                                }
                                jbtnCalculateAccessList.setEnabled(true);
                            }
                        }
                    }
                });
                recalculateAccesList.execute();
                jbtnCalculateAccessList.setEnabled(false);

    		} catch (Exception ex) {
    			ex.printStackTrace();
    			JOptionPane.showMessageDialog(this, ex.getMessage(), "������ ��������", JOptionPane.ERROR_MESSAGE);
    		}	
    	}
		
    }

    public void jbtnGetTemplateList_actionPerformed(ActionEvent e) {
    	try {

    		//Load template list
    		final DoLoadTemplateList templatesLoader = new DoLoadTemplateList(jtxtfURL.getText(), jtxtfUserName.getText(), jtxtfPassword.getText());

            progressMonitor = new ProgressMonitor(this, "�������� �� ���� ", "", 0, 100);
            progressMonitor.setMillisToDecideToPopup(10);
            progressMonitor.setMillisToPopup(10);
            progressMonitor.setProgress(0);

            templatesLoader.addPropertyChangeListener(new PropertyChangeListener() {
            	public void propertyChange(PropertyChangeEvent evt) {
                    if (progressMonitor.isCanceled()) {
                    		templatesLoader.cancel(true);
                    		JOptionPane.showMessageDialog(EditConfigMainForm.this, "�������� �������������", "������ ��������", JOptionPane.ERROR_MESSAGE);                                
                    		jbtnLoadFrom.setEnabled(true);
                   	}
                    if ("progress".equals(evt.getPropertyName())) {
                        int progress = (Integer) evt.getNewValue();
                        progressMonitor.setProgress(progress);
                    } else if ("state".equals(evt.getPropertyName())) {
            			//Show loaded template list in jtblTemplate
                        if (templatesLoader.isDone()) {
                            Exception exception = null;
                            try {
                                exception = templatesLoader.get();
                            } catch (Exception e1) {
                                exception = e1;
                            }
							if (exception != null) {
	                            	logger.error("Exception occured while loading from database: " + exception.getMessage());
	                            	exception.printStackTrace();
                                JOptionPane.showMessageDialog(EditConfigMainForm.this, exception.getMessage(), "������ ��������", JOptionPane.ERROR_MESSAGE);
							} else {
								initTemplateTable(sortTemplates(templatesLoader.getTemplates()));
                                JOptionPane.showMessageDialog(EditConfigMainForm.this, "�������� ������� ���������", "Access Rule", JOptionPane.INFORMATION_MESSAGE);                                	
                            }
                            jbtnLoadFrom.setEnabled(true);
                        }
            		}
				}

            });
            templatesLoader.execute();
            jbtnLoadFrom.setEnabled(false);
    		
    	} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, ex.getMessage(), "������ ��������", JOptionPane.ERROR_MESSAGE);
		}
    }
    
    public void jbtnLoadFrom_actionPerformed(ActionEvent e) {
    	saveAccessRulesIfDirty();
    	int result = JOptionPane.showConfirmDialog(this,
                "�� ��������� ��������� �������� �� ����.\r\n ��� ������ � ������� ����� ������������!!!'", "�������� !!!",
                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {            
        	m_config = new AccessConfig();        
            try {    			
    			final DoImportBase baseImporter = new DoImportBase(jtxtfURL.getText(), jtxtfUserName.getText(), jtxtfPassword.getText(), m_config);
    			
                progressMonitor = new ProgressMonitor(this, "�������� �� ���� ", "", 0, 100);
                progressMonitor.setMillisToDecideToPopup(10);
                progressMonitor.setMillisToPopup(10);
                progressMonitor.setProgress(0);

                baseImporter.addPropertyChangeListener(new PropertyChangeListener() {
                	boolean isCancelledByUser = false;
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (progressMonitor.isCanceled()) {                        	
                        	if(!isCancelledByUser){
                        		isCancelledByUser = true;	
                            	baseImporter.cancel(true);
                        		JOptionPane.showMessageDialog(EditConfigMainForm.this, "�������� �������������", "������ ��������", JOptionPane.ERROR_MESSAGE);                                
                        		jbtnLoadFrom.setEnabled(true);
                        	}
                            return;
                        }

                        if ("progress".equals(evt.getPropertyName())) {
                            int progress = (Integer) evt.getNewValue();
                            progressMonitor.setProgress(progress); 
                            progressMonitor.setNote(baseImporter.getCurrentTemplate());
                        }

                        if ("state".equals(evt.getPropertyName())) {
                            if (baseImporter.isDone()) {
                                Exception exception = null;
                                try {
                                    exception = baseImporter.get();                                    
                                } catch (Exception e1) {
                                    exception = e1;
                                }
								if (exception != null) {
                                    // �������� � �������
                                	logger.error("Exception occured while loading from database: " + exception.getMessage());
                                    exception.printStackTrace();
                                    JOptionPane.showMessageDialog(EditConfigMainForm.this, exception.getMessage(), "������ ��������", JOptionPane.ERROR_MESSAGE);
								} else {
									initForm();
									initTypeTables();
                                    JOptionPane.showMessageDialog(EditConfigMainForm.this, "�������� ������� ���������", "Access Rule", JOptionPane.INFORMATION_MESSAGE);                                	
                                }
                                jbtnLoadFrom.setEnabled(true);
                            }
                        }
                    }
                });
                baseImporter.execute();
                jbtnLoadFrom.setEnabled(false);

    		} catch (Exception ex) {
    			ex.printStackTrace();
    			JOptionPane.showMessageDialog(this, ex.getMessage(), "������ ��������", JOptionPane.ERROR_MESSAGE);
    		}
    		
         }
    }
    
    public void jbtnShowSecondaryDb_actionPerformed(ActionEvent e) {
    	SecondaryDatabasesDialog dialogBase = new SecondaryDatabasesDialog(EditConfigMainForm.this, "������ ���. ��", true, jdbcSettings.secondaryJdbcSettings);
    	dialogBase.setAutoSize();
    	dialogBase.setVisible(true);
    }

    /**
     * Remainds to save current access rules before loading new accerss rules.
     */
	private void saveAccessRulesIfDirty() {
		if (m_config != null) {
	    	int result = JOptionPane.showConfirmDialog(this,
	                "��������� ������� �����?", "Info",
	                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

	        if (result == JOptionPane.YES_OPTION) {            
	        	saveAccessRuleConfig();
	        }
		}
	}

	public JPopupMenu getPermissionPopupMenu() {
		return permissionPopupMenu;
	}

	public JPopupMenu getRulesPopupMenu() {
		return rulesPopupMenu;
	}
	
	public JTable getRulesTable() {
		return jtblRules;
	}

	public JPopupMenu getTemplatePopupMenu() {
		return templatesTablePopupMenu;
	}
	public JTable getTemplateTable() {
		return jtblTemplate;
	}
	
	private void updateAttributeList() {
		if (m_config != null) {
			attributeNames = new FindAttributeName(m_config);
		} else {
			logger.error("Cannot update attributeNames due to m_config is null!");
		}
	}

	public void jbtnMultiDbToInstal_actionPerformed(ActionEvent e) {
		if (m_config == null) {
		    JOptionPane.showMessageDialog(this, "������ ������! ������ ��� ���������, ��������� ����� �������.", "Warning",
		            JOptionPane.INFORMATION_MESSAGE);
		    return;
		}
		
		if (!m_config.getPartial()) {
		    JOptionPane.showMessageDialog(this, "������� ������������� ������ ��� ��������� ������ ����. \n" +
		    		"��������� ��������� � ��������� ������, ��������� � ����� ��������� �� ��� ��!", "Warning",
		            JOptionPane.INFORMATION_MESSAGE);
		    return;
		}
		
        int result = JOptionPane.showConfirmDialog(this,
        		"����� ������������ ������������� ������� ��������� ������ ����.", "�������� !!!",
                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
        if (result == JOptionPane.YES_OPTION) {
        	MultiDbProgressDialog multiDbProgressDialog = new MultiDbProgressDialog(this, "������������� ��������", false);
        	for(String dbName : jdbcSettings.secondaryJdbcSettings.keySet()){
        		Map<String,Object> dbConfig = jdbcSettings.secondaryJdbcSettings.get(dbName);
        		if((Boolean)dbConfig.get(KEY_ENABLE)){
					final DoSaveAccessRules saveAccessRules = 
							 new DoSaveAccessRules((String)dbConfig.get(KEY_URL), 
									 (String)dbConfig.get(KEY_USER), (String)dbConfig.get(KEY_PASSWORD), m_config);
					multiDbProgressDialog.model.addDb(dbName, saveAccessRules);
            	}
            }
        	multiDbProgressDialog.setAutoSize();
        	multiDbProgressDialog.setVisible(true);
        	multiDbProgressDialog.executeAll();
        }
		
	}

	public void jbtnMultiCalculateAccessList_actionPerformed(ActionEvent e) {
		if (m_config == null) {
		    JOptionPane.showMessageDialog(this, "������ ������! ������ ��� ���������, ��������� ����� �������.", "Warning",
		            JOptionPane.INFORMATION_MESSAGE);
		    return;
		}
		
		if (!m_config.getPartial()) {
		    JOptionPane.showMessageDialog(this, "������� ������������� ������ ��� ��������� ������ ����. \n" +
		    		"��������� ��������� � ��������� ������, ��������� � ����� ��������� �� ��� ��!", "Warning",
		            JOptionPane.INFORMATION_MESSAGE);
		    return;
		}
		
        int result = JOptionPane.showConfirmDialog(this,
        		"����� ������������ ������������� ������� ��������� ������ ����.", "�������� !!!",
                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
        if (result == JOptionPane.YES_OPTION) {
        	MultiDbProgressDialog multiDbProgressDialog = new MultiDbProgressDialog(this, "������������� ��������", false);
        	for(String dbName : jdbcSettings.secondaryJdbcSettings.keySet()){
        		Map<String,Object> dbConfig = jdbcSettings.secondaryJdbcSettings.get(dbName);
        		if((Boolean)dbConfig.get(KEY_ENABLE)){
       			 	final DoUpdateAccessList recalculateAccesList = new DoUpdateAccessList((String)dbConfig.get(KEY_URL), 
							 (String)dbConfig.get(KEY_USER), (String)dbConfig.get(KEY_PASSWORD), m_config);
        			recalculateAccesList.setOnlyChanges(true);
					multiDbProgressDialog.model.addDb(dbName, recalculateAccesList);
            	}
            }
        	multiDbProgressDialog.setAutoSize();
        	multiDbProgressDialog.setVisible(true);
        	multiDbProgressDialog.executeAll();
        }
	}
}

class EditConfigMainForm_jbtnLoadFrom_actionAdapter implements ActionListener {

    private EditConfigMainForm adaptee;

    EditConfigMainForm_jbtnLoadFrom_actionAdapter(EditConfigMainForm adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jbtnLoadFrom_actionPerformed(e);
    }
}

class EditConfigMainForm_jbtnShowSecondaryDb_actionAdapter implements ActionListener {

    private EditConfigMainForm adaptee;

    EditConfigMainForm_jbtnShowSecondaryDb_actionAdapter(EditConfigMainForm adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jbtnShowSecondaryDb_actionPerformed(e);
    }
}

class EditConfigMainForm_jbtnToInstal_actionAdapter implements ActionListener {

    private EditConfigMainForm adaptee;
	private boolean onlyChanges;

    EditConfigMainForm_jbtnToInstal_actionAdapter(EditConfigMainForm adaptee, boolean onlyChanges) {
        this.adaptee = adaptee;
        this.onlyChanges = onlyChanges;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jbtnToInstal_actionPerformed(e, onlyChanges);
    }
}

class EditConfigMainForm_jbtnMultiDbToInstal_actionAdapter implements ActionListener {

    private EditConfigMainForm adaptee;
	private boolean onlyChanges;

	EditConfigMainForm_jbtnMultiDbToInstal_actionAdapter(EditConfigMainForm adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jbtnMultiDbToInstal_actionPerformed(e);
    }
}

class EditConfigMainForm_jbtnCalculateAccessList_actionAdapter implements ActionListener {

    private EditConfigMainForm adaptee;
	private boolean onlyChanges;

    EditConfigMainForm_jbtnCalculateAccessList_actionAdapter(EditConfigMainForm adaptee, boolean onlyChanges) {
    	this.onlyChanges = onlyChanges;
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jbtnCalculateAccessList_actionPerformed(e, onlyChanges);
    }
}

class EditConfigMainForm_jbtnMultiCalculateAccessList_actionAdapter implements ActionListener {

    private EditConfigMainForm adaptee;

    EditConfigMainForm_jbtnMultiCalculateAccessList_actionAdapter(EditConfigMainForm adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jbtnMultiCalculateAccessList_actionPerformed(e);
    }
}

class EditConfigMainForm_jbtnGetTemplateList_actionAdapter implements ActionListener {

    private EditConfigMainForm adaptee;

    EditConfigMainForm_jbtnGetTemplateList_actionAdapter(EditConfigMainForm adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jbtnGetTemplateList_actionPerformed(e);
    }
}

class EditConfigMainForm_jbtnOpenFile_actionAdapter implements ActionListener {

    private EditConfigMainForm adaptee;

    EditConfigMainForm_jbtnOpenFile_actionAdapter(EditConfigMainForm adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jbtnOpenFile_actionPerformed(e);
    }
}

class EditConfigMainForm_jbtnSaveAs_actionAdapter implements ActionListener {

    private EditConfigMainForm adaptee;

    EditConfigMainForm_jbtnSaveAs_actionAdapter(EditConfigMainForm adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jbtnSaveAs_actionPerformed(e);
    }
}

class EditConfigMainForm_jrbtnProfileRuleType_changeAdapter implements ActionListener {

    private EditConfigMainForm adaptee;

    EditConfigMainForm_jrbtnProfileRuleType_changeAdapter(EditConfigMainForm adaptee) {
        this.adaptee = adaptee;
    }    

	public void actionPerformed(ActionEvent e) {
        adaptee.jrbtnProfileRuleType_stateChanged(e);		
	}
}

class EditConfigMainForm_jrbtnRoleRuleType_changeAdapter implements ActionListener {

    private EditConfigMainForm adaptee;

    EditConfigMainForm_jrbtnRoleRuleType_changeAdapter(EditConfigMainForm adaptee) {
        this.adaptee = adaptee;
    }

	public void actionPerformed(ActionEvent e) {
        adaptee.jrbtnRoleRuleType_stateChanged(e);
    }
}

class EditConfigMainForm_jrbtnPersonRuleType_changeAdapter implements ActionListener {

    private EditConfigMainForm adaptee;

    EditConfigMainForm_jrbtnPersonRuleType_changeAdapter(EditConfigMainForm adaptee) {
        this.adaptee = adaptee;
    }

	public void actionPerformed(ActionEvent e) {
        adaptee.jrbtnPersonRuleType_stateChanged(e);
    }
}

class EditConfigMainForm_jrbtnDelegationRuleType_changeAdapter implements ActionListener {

    private EditConfigMainForm adaptee;

    EditConfigMainForm_jrbtnDelegationRuleType_changeAdapter(EditConfigMainForm adaptee) {
        this.adaptee = adaptee;
    }

	public void actionPerformed(ActionEvent e) {
        adaptee.jrbtnDelegationRuleType_stateChanged(e);
    }
}

class EditConfigMainForm_jbtnAttributeDelete_actionAdapter implements ActionListener {

    private EditConfigMainForm adaptee;

    EditConfigMainForm_jbtnAttributeDelete_actionAdapter(EditConfigMainForm adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jbtnAttributeDelete_actionPerformed(e);
    }
}

class EditConfigMainForm_jbtnAttributeEdit_actionAdapter implements ActionListener {

    private EditConfigMainForm adaptee;

    EditConfigMainForm_jbtnAttributeEdit_actionAdapter(EditConfigMainForm adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jbtnAttributeEdit_actionPerformed(e);
    }
}

class EditConfigMainForm_jbtnAttributeAdd_actionAdapter implements ActionListener {

    private EditConfigMainForm adaptee;

    EditConfigMainForm_jbtnAttributeAdd_actionAdapter(EditConfigMainForm adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jbtnAttributeAdd_actionPerformed(e);
    }
}

class EditConfigMainForm_jbtnRulesEdit_actionAdapter implements ActionListener {

    private EditConfigMainForm adaptee;

    EditConfigMainForm_jbtnRulesEdit_actionAdapter(EditConfigMainForm adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jbtnRulesEdit_actionPerformed(e);
    }
}

class EditConfigMainForm_jbtnRulesCopy_actionAdapter implements ActionListener {

    private EditConfigMainForm adaptee;

    EditConfigMainForm_jbtnRulesCopy_actionAdapter(EditConfigMainForm adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jbtnRulesCopy_actionPerformed(e);
    }
}

class EditConfigMainForm_jbtnRulesPaste_actionAdapter implements ActionListener {

    private EditConfigMainForm adaptee;

    EditConfigMainForm_jbtnRulesPaste_actionAdapter(EditConfigMainForm adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jbtnRulesPaste_actionPerformed(e);
    }
}

/**
 * 
 * @author atsvetkov
 * 
 */
class CopyRulesActionAdapter implements ActionListener {

	private EditConfigMainForm adaptee;

	CopyRulesActionAdapter(EditConfigMainForm adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.copyRules(e);
	}
}

class EditConfigMainForm_jbtnRulesDelete_actionAdapter implements ActionListener {

    private EditConfigMainForm adaptee;

    EditConfigMainForm_jbtnRulesDelete_actionAdapter(EditConfigMainForm adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jbtnRulesDelete_actionPerformed(e);
    }
}

class EditConfigMainForm_jbtnRulesAdd_actionAdapter implements ActionListener {

    private EditConfigMainForm adaptee;

    EditConfigMainForm_jbtnRulesAdd_actionAdapter(EditConfigMainForm adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jbtnRulesAdd_actionPerformed(e);
    }
}

class EditConfigMainForm_jtblPermission_mouseAdapter extends MouseAdapter {
    private EditConfigMainForm adaptee;

    EditConfigMainForm_jtblPermission_mouseAdapter(EditConfigMainForm adaptee) {
        this.adaptee = adaptee;
    }

    public void mouseClicked(MouseEvent e) {
        adaptee.jtblPermission_mouseClicked(e);
    }

	public void mousePressed(MouseEvent e) {
		showPopup(e);
	}

	public void mouseReleased(MouseEvent e) {
		showPopup(e);
	}

	private void showPopup(MouseEvent e) {
		if (e.isPopupTrigger()) {
			adaptee.logger.debug("Popup is opened");
			adaptee.getPermissionPopupMenu().show(e.getComponent(), e.getX(), e.getY());
		}
	}
}

class TableRulesMouseAdapter extends MouseAdapter {
    private EditConfigMainForm adaptee;

    TableRulesMouseAdapter(EditConfigMainForm adaptee) {
        this.adaptee = adaptee;
    }

	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			adaptee.editRule();
		}
	}

	public void mousePressed(MouseEvent e) {
		showPopup(e);
	}

	public void mouseReleased(MouseEvent e) {
		showPopup(e);
	}

	private void showPopup(MouseEvent e) {
		int rowAtPoint = adaptee.getRulesTable().rowAtPoint(e.getPoint());
		if (rowAtPoint >= 0 && rowAtPoint < adaptee.getRulesTable().getRowCount()) {
			adaptee.getRulesTable().setRowSelectionInterval(rowAtPoint, rowAtPoint);
		} else {
			adaptee.getRulesTable().clearSelection();
		}

        int rowindex = adaptee.getRulesTable().getSelectedRow();
        if (rowindex < 0) {
            return;
        }
        if (e.isPopupTrigger() && e.getComponent() instanceof JTable ) {
			adaptee.logger.debug("Update rule in database popup is opened");
        	adaptee.getRulesPopupMenu().show(e.getComponent(), e.getX(), e.getY());
        }        
	}
}

class TableTemplatesMouseAdapter extends MouseAdapter {
    private EditConfigMainForm adaptee;

    TableTemplatesMouseAdapter(EditConfigMainForm adaptee) {
        this.adaptee = adaptee;
    }

	public void mouseReleased(MouseEvent e) {
		showPopup(e);
	}

	private void showPopup(MouseEvent e) {
		int rowindex = adaptee.getTemplateTable().rowAtPoint(e.getPoint());
        if (rowindex < 0) {
            return;
        }
		adaptee.getTemplateTable().setRowSelectionInterval(rowindex, rowindex);
        if (e.isPopupTrigger() && e.getComponent() instanceof JTable ) {
			adaptee.logger.debug("Update template in database popup is opened");
        	adaptee.getTemplatePopupMenu().show(e.getComponent(), e.getX(), e.getY());
        }        
	}
}


class EditConfigMainForm_jtblPermission_keyAdapter extends KeyAdapter {
    private EditConfigMainForm adaptee;

    EditConfigMainForm_jtblPermission_keyAdapter(EditConfigMainForm adaptee) {
        this.adaptee = adaptee;
    }

    public void keyTyped(KeyEvent e) {
        adaptee.jtblPermission_keyTyped(e);
	}
    
    public void keyPressed(KeyEvent e) {
		
	}
    /*
     * Invoked when a key has been released.
     */
    public void keyReleased(KeyEvent e)  {
		
	}
}


class EditConfigMainForm_jbtnSave_actionAdapter implements ActionListener {

    private EditConfigMainForm adaptee;

    EditConfigMainForm_jbtnSave_actionAdapter(EditConfigMainForm adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jbtnSave_actionPerformed(e);
    }
}

class EditConfigMainForm_this_windowAdapter extends WindowAdapter {

    private EditConfigMainForm adaptee;

    EditConfigMainForm_this_windowAdapter(EditConfigMainForm adaptee) {
        this.adaptee = adaptee;
    }

    public void windowOpened(WindowEvent e) {
        adaptee.this_windowOpened(e);
    }

    public void windowClosing(WindowEvent e) {
        adaptee.saveLayoutConfiguration();
    }
}

class EditConfigMainForm_jcbTemplateType_itemAdapter implements ItemListener {

    private EditConfigMainForm adaptee;

    EditConfigMainForm_jcbTemplateType_itemAdapter(EditConfigMainForm adaptee) {
        this.adaptee = adaptee;
    }

    public void itemStateChanged(ItemEvent e) {
        adaptee.jcbTemplateType_itemStateChanged(e);
    }
}

class EditConfigMainForm_jbtnClose_actionAdapter implements ActionListener {

    private EditConfigMainForm adaptee;

    EditConfigMainForm_jbtnClose_actionAdapter(EditConfigMainForm adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jbtnClose_actionPerformed(e);
    }    
}

class PermissionColumnHeaderToolTips {
	// Maps TableColumn objects to tooltips
    Map<TableColumn, String> tips = new HashMap<TableColumn, String>();

    // If tooltip is null, removes any tooltip text.
    public void setToolTip(TableColumn col, String tooltip) {
        if (tooltip == null) {
            tips.remove(col);
        } else {
            tips.put(col, tooltip);
        }
    }
    
    public String getToolTip(TableColumn col) {
    	return tips.get(col);
    }
}

class PermissionColumnHeaderMouseListener extends MouseMotionAdapter {
    // Current column whose tooltip is being displayed.
    // This variable is used to minimize the calls to setToolTipText().

    TableColumn curCol;
    PermissionColumnHeaderToolTips toolTips;
    
    public PermissionColumnHeaderMouseListener(PermissionColumnHeaderToolTips toolTips) {
		this.toolTips = toolTips;
	}

	public void mouseMoved(MouseEvent evt) {
        TableColumn col = null;
        JTableHeader header = (JTableHeader) evt.getSource();
        JTable table = header.getTable();
        TableColumnModel colModel = table.getColumnModel();
        int vColIndex = colModel.getColumnIndexAtX(evt.getX());

        // Return if not clicked on any column header
        if (vColIndex >= 0) {
            col = colModel.getColumn(vColIndex);
        }

        if (col != curCol) {
            header.setToolTipText( toolTips.getToolTip(col));
            curCol = col;
        }
    }
}
