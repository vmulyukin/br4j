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

import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Cell renderer for linked status cell in Rules table.
 * @author atsvetkov
 *
 */
public class LinkedStatusCellRenderer extends JPanel implements TableCellRenderer {
			
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
        this.removeAll();
        
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		if (value != null) {
			String[] statusNames = String.valueOf(value).split(",");
						
			for (String name : statusNames) {				
				JLabel statusLabel = new JLabel();
	            statusLabel.setText(name);
	            statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
	            add(statusLabel);
	            if (isSelected) {
	            	statusLabel.setBackground(table.getSelectionBackground());
	            	statusLabel.setForeground(table.getSelectionForeground());					
		        }
			}
			
			if (isSelected) {
	            setBackground(table.getSelectionBackground());
	            setForeground(table.getSelectionForeground());
	        }
	        else {
	            setBackground(table.getBackground());
	        }
		}
        return this;
    }
}
