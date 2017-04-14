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

import javax.swing.JTable;
import javax.swing.table.TableRowSorter;

/**
 * Table utility class. Provides some usefull operation for {@JTable} (e.g. toggling sort icon).
 * @author atsvetkov
 *
 */
public class TableUtility {

	/**
	 * Toggles first column of {@link JTable} to be sorted.
	 * 
	 * @param table
	 */
	public static void toggleFirstColumnRowSorter(JTable table){
		if (table.getModel().getColumnCount() > 0 && table.getRowSorter() != null) {
			table.getRowSorter().toggleSortOrder(0);
		}
	}
	
	/**
	 * Adds sorter for column with integer values.
	 * 
	 * @param table
	 * @param tableModel
	 * @param columnsToApply
	 */
	public static void addIntegerSorter(JTable table, MixedTableModel tableModel, Integer[] columnsToApply) {
        TableRowSorter<MixedTableModel> rowSorter = new TableRowSorter<MixedTableModel>(tableModel);
        for(int columnIndex : columnsToApply) {
        	rowSorter.setComparator(columnIndex, new IntegerComparator());
        }
        table.setRowSorter(rowSorter);
	}

}
