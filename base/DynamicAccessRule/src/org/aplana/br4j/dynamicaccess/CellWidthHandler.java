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
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * Handler for autoresizing column width in {@link JTable}
 * @author atsvetkov
 *
 */
public class CellWidthHandler {

	private static final int MIN_WIDTH = 100;
	
	public static JTable autoResizeColWidth(JTable table) {
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		int margin = 0;

		for (int i = 0; i < table.getColumnCount(); i++) {
			int vColIndex = i;
			DefaultTableColumnModel colModel = (DefaultTableColumnModel) table.getColumnModel();
			TableColumn col = colModel.getColumn(vColIndex);
			int width = 0;

			// Get width of column header
			TableCellRenderer renderer = col.getHeaderRenderer();

			if (renderer == null) {
				renderer = table.getTableHeader().getDefaultRenderer();
			}

			Component comp = renderer.getTableCellRendererComponent(table, col.getHeaderValue(), false, false, 0, 0);

			width = comp.getPreferredSize().width;

			// Get maximum width of column data
			for (int r = 0; r < table.getRowCount(); r++) {
				renderer = table.getCellRenderer(r, vColIndex);
				comp = renderer.getTableCellRendererComponent(table, table.getValueAt(r, vColIndex), false, false, r,
						vColIndex);
				width = Math.max(width, comp.getPreferredSize().width);
				width = Math.max(width, MIN_WIDTH);
			}

			width += 2 * margin;

			col.setPreferredWidth(width);
		}

//		table.getTableHeader().getDefaultRenderer())
//				.setHorizontalAlignment(SwingConstants.LEFT);

		// table.setAutoCreateRowSorter(true);
		return table;
	}
}
