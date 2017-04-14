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

import java.util.Arrays;

import javax.swing.JTable;

/**
 * Stores the arrays of {@link ColumnRowIndex} for columns and rows. It is used to order columns and rows in
 * {@link JTable} according to their new visible index.
 * 
 * @author atsvetkov
 * 
 */
public class ColumnRowIndexesOrder {	
	 private int columnCount;
	 private ColumnRowIndex[] columnIndexes;
	 private JTable table;
	 private ColumnRowIndex[] rowIndexes;	 
	 private int rowCount;
	 	 
	 
	 public ColumnRowIndexesOrder(JTable table, int columnCount, int rowCount){
		this.table = table;
		
		this.columnCount = 1;
		if (columnCount > 1) {
			this.columnCount = columnCount;
		}
		this.rowCount = 1;
		if (rowCount > 1) {
			this.rowCount = rowCount;
		}
		this.columnIndexes = new ColumnRowIndex[columnCount];
		this.rowIndexes = new ColumnRowIndex[rowCount];
	 }
	    
	public int getCountItems() {
		return columnCount;
	}

	public void orderColumns() {
		if (table != null) {
			//sort rowIndexes using column index as sort criteria. This is necessary to load columns in right order specified in config files.  
			Arrays.sort(columnIndexes);
			
			for (int i = 0; i < columnCount; i++) {
				if (columnIndexes[i] != null) {
					for (int j = 0; j < table.getColumnCount(); j++) {
						if (columnIndexes[i].name == table.getColumnName(j)) {
							table.moveColumn(j, columnIndexes[i].index);
							break;
						}
					}
				}
			}
		}
	}

	public void orderRows() {
		if (table != null) {
			//sort rowIndexes using row index as sort criteria. This is necessary to load rows in right order specified in config files. 
			Arrays.sort(rowIndexes);
			
			for (int i = 0; i < rowCount; i++) {
				if (rowIndexes[i] != null) {
					for (int j = 0; j < table.getModel().getRowCount(); j++) {						
						if (rowIndexes[i].name.equals(((MixedTableModel)table.getModel()).getRowName(j))) {							
							((Reorderable)table.getModel()).reorder(j, rowIndexes[i].index);
							break;
						}
					}
				}
			}
		}
	}

	public void addColumnItem(int index, String name, int columnIndex) {
		columnIndexes[index] = new ColumnRowIndex(name, columnIndex);
	}

	public void addRowItem(int index, String name, int rowIndex) {
		rowIndexes[index] = new ColumnRowIndex(name, rowIndex);
	}
	
	public ColumnRowIndex getItem(int index) {
		ColumnRowIndex ret = null;
		if (index < columnCount && index >= 0)
			ret = columnIndexes[index];
		return ret;
	}
}
