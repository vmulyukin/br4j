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

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
/**
 * Handles auto resizing of row height in {@link JTable}.
 * @author atsvetkov
 *
 */
public class RowHeightHandler {
	
	public static final int MIN_ROW_HEIGHT = 0;
	
	protected JTable table;
	
	public RowHeightHandler(JTable table) {
		this.table = table;
	}

	/**
	 * Adjust row height for cell specified by row and column.
	 * @param row containing the target cell
	 * @param col containing the target cell
	 */
	public void adjustRowHeight(int row, int col) {
		int height = getCellPreferredHeight(row, col);
		setRowHeight(row, height);
	}
	
	
	public JTable getTable() {
		return table;
	}

	protected int getMinRowHeight() {
		return MIN_ROW_HEIGHT;
	}
	/**
	 * Sets the height in both table and table header.
	 * @param row
	 * @param height
	 */
	protected void setRowHeight(int row, int height) {
		table.setRowHeight(row, height);
	}

	/**
	 * Adjusts the height of all rows in table.
	 */
	public void adjustCellsHeight() {
		for (int row = 0; row < table.getRowCount(); row++) {
			int maxRowHeight = getMinRowHeight();
			for (int j = 0; j < table.getColumnModel().getColumnCount(); j++) {
				int height = getCellPreferredHeight(row, j);
				if (maxRowHeight < height) {
					maxRowHeight = height;
				}				
			}
			setRowHeight(row, maxRowHeight);
		}
	}

	/**
	 * Returns the max value of row's preferred height or {@link MixedTable#MIN_ROW_HEIGHT}.
	 * @param row containing the target cell
	 * @param column containg the target cell
	 * @return max value of row's preferred height or {@link MixedTable#MIN_ROW_HEIGHT}
	 */
	private int getCellPreferredHeight(int row, int column) {
		int columnIndex = table.getColumnModel().getColumnIndex(
				table.getColumnModel().getColumn(column).getIdentifier());

		TableCellRenderer renderer = table.getCellRenderer(row, columnIndex);
		Component comp = renderer.getTableCellRendererComponent(table, table.getValueAt(row, columnIndex),
				false, false, row, columnIndex);

		int height = Math.max(getMinRowHeight(), comp.getPreferredSize().height);
		return height;
	}
		
}
