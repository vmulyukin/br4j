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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JTable;

import org.apache.log4j.Logger;

/**
 * Utility class to store index and width of columns and respectivly index and height of rows in {@link JTable}.
 * @author atsvetkov
 *
 */
public class ColumnRowConfig {

  	protected final static Logger logger = Logger.getLogger(ColumnRowConfig.class);
    
	private static final String COLUMN_WIDTH_PART = "-colwidth-";
	private static final String COLUMN_INDEX_PART = "-colindex-";
	private static final String ROW_INDEX_PART = "-rowindex-";
	private static final String ROW_HEIGHT_PART = "-rowheight-";
	
	private static final String CONFIG_FILE_NAME = "_columnRowConfiguration.xml";
	private static final String CONFIG_FOLDER_NAME = "config";	
	
	public static void loadAdjustments(JTable currentTable, String templateName) {		
		Properties pr = new Properties();
		try {
			pr.loadFromXML(new FileInputStream(CONFIG_FOLDER_NAME + File.separator + templateName + CONFIG_FILE_NAME));
			
			int columnCout = currentTable.getColumnCount();
			int rowCount = currentTable.getModel().getRowCount();					
			ColumnRowIndexesOrder columnIndexes = new ColumnRowIndexesOrder(currentTable, columnCout, rowCount);				
			loadColumnConfiguration(currentTable, templateName, pr, columnIndexes);
			
			loadRowConfiguration(currentTable, templateName, pr, columnIndexes);			
			
		} catch (IOException ioe) {
			logger.debug(ioe.getMessage());
		} catch (Throwable e) {
			// catch any exception as loading column configuration is of minimal
			// priority and should not break main functionality
			logger.debug(e.getMessage());
		}
		
	}

	private static void loadColumnConfiguration(JTable currentTable, String templateName, Properties pr,
			ColumnRowIndexesOrder columnIndexes) {
		for (int i = 0; i < currentTable.getColumnCount(); i++) {
			String columnName = currentTable.getColumnName(i);
			String columnWidthProperty = pr.getProperty(templateName + COLUMN_WIDTH_PART + columnName, null);
			String columnIndexProperty = pr.getProperty(templateName + COLUMN_INDEX_PART + columnName, null);							
			
			//don't adjust if properties are not provided in xml
			if(columnWidthProperty == null || columnIndexProperty == null){
				continue;
			}				
			int colunWidth = Integer.decode(columnWidthProperty);
			int columnIndex = Integer.decode(columnIndexProperty);
			//Don't load the width of columns as the width is calculated automatically in {@link CellWidthHandler}.
//			currentTable.getColumnModel().getColumn(i).setPreferredWidth(colunWidth);
			columnIndexes.addColumnItem(i, columnName, columnIndex);
		}
		columnIndexes.orderColumns();
	}

	private static void loadRowConfiguration(JTable currentTable, String templateName, Properties pr,
			ColumnRowIndexesOrder columnIndexes) {
		for (int i = 0; i < currentTable.getModel().getRowCount(); i++) {
			String rowName = ((MixedTableModel)currentTable.getModel()).getRowName(i);				
			String rowIndexProperty = pr.getProperty(templateName + ROW_INDEX_PART + rowName, null);
			String rowHeightProperty = pr.getProperty(templateName + ROW_HEIGHT_PART + rowName, null);
			//don't adjust if properties are not provided in xml
			if(rowIndexProperty == null || rowHeightProperty == null){
				continue;
			}				
			int rowIndex = Integer.decode(rowIndexProperty);
			//int rowHeight = Integer.decode(rowHeightProperty);
			//do not load row's height as it is calculated automatically			
			columnIndexes.addRowItem(i, rowName, rowIndex);				
		}
		columnIndexes.orderRows();		
	}
	
	public static void saveAdjustments(JTable currentTable, String templateName) { 
		Properties pr = new Properties();
		createConfigFolder();		
		collectColumnConfiguration(currentTable, templateName, pr);
		collectRowConfiguration(currentTable, templateName, pr);
		try {
			FileOutputStream output = new FileOutputStream(CONFIG_FOLDER_NAME + File.separator + templateName
					+ CONFIG_FILE_NAME);
			pr.storeToXML(output,
					"Configuration columns layout for template: " + templateName);
			output.flush();
			output.close();			
		} catch (IOException ioe) {
			ioe.printStackTrace();
			logger.debug(ioe.getMessage());
		} catch (Throwable e) {
			logger.debug(e.getMessage());
			//e.printStackTrace();
		}        
    }

	private static void collectRowConfiguration(JTable currentTable, String templateName, Properties pr) {
		for (int i = 0; i < currentTable.getModel().getRowCount(); i++) {
			String rowName = ((MixedTableModel)currentTable.getModel()).getRowName(i);				
			int rowHeight = currentTable.getRowHeight(i);			
			pr.setProperty(templateName + ROW_INDEX_PART + rowName, String.valueOf(i));
			pr.setProperty(templateName + ROW_HEIGHT_PART + rowName, String.valueOf(rowHeight));
		}
	}

	private static void collectColumnConfiguration(JTable currentTable, String templateName, Properties pr) {
		for (int i = 0; i < currentTable.getColumnCount(); i++) {
			String columnName = currentTable.getColumnName(i);
			int width = currentTable.getColumnModel().getColumn(i).getWidth();
			pr.setProperty(templateName + COLUMN_WIDTH_PART + columnName, String.valueOf(width));						
			int index = currentTable.getColumnModel().getColumnIndex(
					currentTable.getColumnModel().getColumn(i).getIdentifier());
			pr.setProperty(templateName + COLUMN_INDEX_PART + columnName, String.valueOf(index));			
		}
	}

	/**
	 * Creates {@link ColumnRowConfig#CONFIG_FOLDER_NAME} folder containg files with column configuration
	 */
	private static void createConfigFolder() {
		File configFolder = new File(CONFIG_FOLDER_NAME);
		if(!configFolder.exists()){
			configFolder.mkdir();
		}
	}	
}
