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
/*���� ����� ��������� �����, ����������� � ������ ��� ���������� ������� �� ������� "�����"*/
/*� ������ ���������� ��� ������ �� Permission - workflow,attribute*/
package org.aplana.br4j.dynamicaccess;

import javax.swing.table.TableCellRenderer;
import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.JTable;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import java.awt.Color;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aplana.br4j.dynamicaccess.xmldef.Operation;
import org.aplana.br4j.dynamicaccess.xmldef.Permission;
import org.aplana.br4j.dynamicaccess.xmldef.Template;
import org.aplana.br4j.dynamicaccess.xmldef.WfMove;
import org.aplana.br4j.dynamicaccess.xmldef.types.Action;

import java.awt.Font;
import java.util.List;

/**
 *
 * <p>Title: PermissionCellRenderer</p>
 *
 * <p>Description: ����� ���������� CardPermissionType � ������</p>
 *
 * <p>Copyright: Copyright (c) 2011</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class PermissionCellRenderer extends JPanel implements TableCellRenderer
{
	protected final Log logger = LogFactory.getLog(getClass());

	Color addColor = new Color(0, 0, 255);
	Color removeColor = new Color(255, 0, 0);
    private Template m_template = null;

    /**
     * ����� ������������ �������� � ������ �������, � ����� ��������� ��������� ������ ������� �� ������ �������.
     * �� ���� ��������� ����� ������ � ���������� �� ����
     * @param table JTable
     * @param value Object
     * @param isSelected boolean
     * @param hasFocus boolean
     * @param row int
     * @param column int
     * @return Component
     */
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column)
    {
        this.removeAll();
        
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		if (value != null) {
            List permissionSet = (List) value;
            if(permissionSet.size() > 0) {
            	Permission typedPermission = (Permission)permissionSet.get(0);
            
	            for(Operation operation: typedPermission.getOperations().getOperations()){
	                JLabel labelPermissionName = new JLabel();
	                if(Action.ADD.equals(operation.getAction())){
	                	labelPermissionName.setForeground(addColor);
	                } else if(Action.REMOVE.equals(operation.getAction())){
	                	labelPermissionName.setForeground(removeColor);
	                }
	                labelPermissionName.setText(operation.getOperationType().toString() + (operation.isAuto() ? "*" : ""));
	                labelPermissionName.setAlignmentX(Component.CENTER_ALIGNMENT);
	                add(labelPermissionName);
	            }
	            
	            for (int i = 0; i < typedPermission.getWfMoves().getWfMoveCount(); i++) {
	            	String wfm = "";
	            	JLabel labelPermissionName = new JLabel();
	                for (int j = 0; j < m_template.getWFMoveTypeCount(); j++) {
	                    if (typedPermission.getWfMoves().getWfMove(i).getWfm_id().equals(m_template.getWFMoveType(j).getWfm_id())) {
	                    	WfMove wfMove = typedPermission.getWfMoves().getWfMove(i);
	                        if(Action.ADD.equals(wfMove.getAction())){
	                        	labelPermissionName.setForeground(addColor);
	                        } else if(Action.REMOVE.equals(wfMove.getAction())){
	                        	labelPermissionName.setForeground(removeColor);
	                        }
	                        wfm = m_template.getWFMoveType(j).getWfm_from() + "->" + m_template.getWFMoveType(j).getWfm_to()+ (wfMove.isAuto() ? "*" : "") + "�����";
	                        labelPermissionName.setText(wfm);
	                        Font font = new Font("Calibri", Font.PLAIN, 11);
	                        labelPermissionName.setFont(font);
	                        labelPermissionName.setAlignmentX(Component.CENTER_ALIGNMENT);
	                        add(labelPermissionName);
	                    }
	                }
	            }
            }
        }
        if (isSelected) {
            setBackground(new Color(150, 150, 150));
        }
        else {
            setBackground(new Color(255, 255, 255));
        }
        return this;
    }

    public PermissionCellRenderer(Template template)
    {
        m_template = template;
        setOpaque(true);
    }
    
}
