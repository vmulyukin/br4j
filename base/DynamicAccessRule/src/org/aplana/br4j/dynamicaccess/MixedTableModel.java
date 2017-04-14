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

import java.util.ArrayList;

import javax.swing.table.DefaultTableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MixedTableModel extends DefaultTableModel implements Reorderable {
  
	protected final Log logger = LogFactory.getLog(getClass());

	ArrayList<String> rowNames = new ArrayList<String>();

  public boolean isCellEditable(int row, int col) {
    return false;
  }

  public void addRow(String rowName, Object[] rowDate) {
    rowNames.add(rowName);
    addRow(rowDate);
  }

	public String getRowName(int rowIndex) {
		if (rowIndex >= 0 && rowIndex < rowNames.size()) {
			return (String) rowNames.get(rowIndex);
		} else {
			logger.debug("Row name for index " + rowIndex + " is empty");
			return "";
		}
	}
  
	public void reorder(int fromIndex, int toIndex) {
		toIndex = reorderRowHeader(fromIndex, toIndex);
		moveRow(fromIndex, fromIndex, toIndex);
	}

	private int reorderRowHeader(int fromIndex, int toIndex) {
		int maxIndex = rowNames.size();
		if (toIndex > maxIndex - 1) {
			toIndex = maxIndex -1;
		}
		String nameToMove = rowNames.remove(fromIndex);
		rowNames.add(toIndex, nameToMove);
		return toIndex;
	}
}
