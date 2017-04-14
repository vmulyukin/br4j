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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import javax.activation.ActivationDataFlavor;
import javax.activation.DataHandler;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;

/**
 * Class designed for handling transfer rows in Permission tab. Transfers rows in
 * table and in row header panel as well.
 * 
 * @author atsvetkov
 * 
 */
public class RowHeaderTransferHandler  extends TransferHandler {
   	private final DataFlavor localObjectFlavor = new ActivationDataFlavor(Integer.class,
			DataFlavor.javaJVMLocalObjectMimeType, "Integer Row Index");
	 
    private int index = 0;
    private JTable rowHeaderSource;
    private JTable tableSource;

	public RowHeaderTransferHandler(JTable listSource, JTable tableSource){
		this.rowHeaderSource = listSource;
		this.tableSource = tableSource; 
	}
	
	public int getSourceActions(JComponent comp) {
        return COPY_OR_MOVE;
    }

    /**
     * Transfer row index on drag/drop operation.
     */
    public Transferable createTransferable(JComponent comp) {
        index = rowHeaderSource.getSelectedRow();
        if (index < 0 || index >= rowHeaderSource.getModel().getRowCount()) {
            return null;
        }
        return new DataHandler(new Integer(rowHeaderSource.getSelectedRow()), localObjectFlavor.getMimeType());
    }
    
    
    public boolean canImport(TransferHandler.TransferSupport support) {
		if (!support.isDrop() || !support.isDataFlavorSupported(localObjectFlavor)) {
			return false;
		}
		JTable.DropLocation dl = (JTable.DropLocation) support.getDropLocation();
		if (dl.getRow() == -1) {
			return false;
		} else {
			return true;
		}
	}

	public boolean importData(TransferHandler.TransferSupport support) {
		if (!canImport(support)) {
			return false;
		}
		Transferable transferable = support.getTransferable();			
		Integer index;
		try {
			index = (Integer) transferable.getTransferData(localObjectFlavor);
		} catch (Exception e) {
				e.printStackTrace();
			return false;
		}
		
		JTable.DropLocation dl = (JTable.DropLocation) support.getDropLocation();
		int dropTargetIndex = dl.getRow();
		try{				
			((Reorderable)tableSource.getModel()).reorder(index, dropTargetIndex);				
		}catch(Exception e){
			e.printStackTrace();
		}
		
		rowHeaderSource.setRowSelectionInterval(dropTargetIndex, dropTargetIndex);
		rowHeaderSource.requestFocusInWindow();
		
		RowHeightHandler handler = new PermissionRowHeightHandler((MixedTable)tableSource);
		handler.adjustCellsHeight();
		tableSource.setRowSelectionInterval(rowHeaderSource.getSelectedRow(), rowHeaderSource.getSelectedRow());
		return true;
	}
}
