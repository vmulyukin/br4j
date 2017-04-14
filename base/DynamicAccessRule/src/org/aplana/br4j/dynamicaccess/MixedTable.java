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

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MixedTable extends JTable
{
	protected final Log logger = LogFactory.getLog(getClass());
	
    public static final int rowWidth = 100;
    public static final int MIN_ROW_HEIGHT = 50;    

    private JTable rowHeader = null;
    private boolean visibleRowHeader = true;

    public TableCellRenderer getCellRenderer(int row, int column)
    {
        Object value = getValueAt(row, column);
        TableCellRenderer result = null;
        if (value != null) {
            result = getDefaultRenderer(value.getClass());
        }
        if (result == null) {
            result = getDefaultRenderer(Object.class);
        }
        return result;
    }

    public void setRowHeader(TableModel tm)
    {
 
    	rowHeader = new JTable(tm);
    	rowHeader.createDefaultColumnsFromModel();     	
        rowHeader.setForeground(getTableHeader().getForeground());
        rowHeader.setBackground(getTableHeader().getBackground());
        rowHeader.setSelectionBackground(getTableHeader().getBackground());
        rowHeader.setSelectionForeground(getTableHeader().getForeground());   
        rowHeader.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        rowHeader.setAutoscrolls(true);        
        rowHeader.setDragEnabled(true);        
        rowHeader.setDropMode(DropMode.ON_OR_INSERT);
        
        rowHeader.addMouseListener(new MouseAdapter(){        	
        	@Override
        	public void mouseDragged(MouseEvent e) {
        		JComponent c = (JComponent)e.getSource();
    	        TransferHandler handler = c.getTransferHandler();
    	        handler.exportAsDrag(c, e, TransferHandler.COPY);
    	        c.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    	        rowHeader.setBackground(getTableHeader().getBackground());
    	        super.mouseDragged(e);
        	}
        });

        rowHeader.setTransferHandler(new RowHeaderTransferHandler(rowHeader, this));      
        new RowHeaderDragListener(rowHeader);
        rowHeader.setDefaultRenderer(String.class, new RowHeaderRenderer(this));
        //rowHeader.setDefaultRenderer(Object.class, new RowHeaderRenderer(this));
        
        //rowHeader.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);   
         rowHeader.setSelectionModel(this.getSelectionModel());

        JViewport headerViewPort = new JViewport();
        headerViewPort.setView(rowHeader);
        headerViewPort.setPreferredSize(new Dimension (150, MIN_ROW_HEIGHT));
        ((JScrollPane) getParent().getParent()).setRowHeader(headerViewPort);
    }

      
    public JTable getRowHeader()
    {
        return rowHeader;
    }

    public void setVisibleRowHeader(boolean visible)
    {
        visibleRowHeader = visible;
        if (visible) {
            if (rowHeader == null) {
                repaint();
            }
            else {
                ((JScrollPane) getParent().getParent()).setRowHeaderView(rowHeader);
            }
        }
        else {
            ((JScrollPane) getParent().getParent()).setRowHeaderView(null);
        }
    }

    public void removeRowHeader()
    {
        ((JScrollPane) getParent().getParent()).setRowHeaderView(null);
        rowHeader = null;
    }

    public void repaint()
    {
        if ((getParent() != null) &&
                (rowHeader == null) &&
                (getRowCount() > 0) &&
                (visibleRowHeader)) {

			initRowHeader();
        }

        if (rowHeader != null) {            
            rowHeader.revalidate();
            rowHeader.repaint();
        }
        super.revalidate();
        super.repaint();
    }

    /**
     * Initializes the row header in parent table.
     */
	public void initRowHeader() {
		TableModel lm = new DefaultTableModel()	{		
			@Override				
			 public boolean isCellEditable(int rowIndex, int columnIndex){
				return false;
			}
			
			@Override
			public String getColumnName(int col) {
				return "";
			}

			@Override
			public int getColumnCount() {
				return 1;
			}

			@Override
			public int getRowCount() {
				return getRowCountForHeader();
			}
			
			@Override
			public Object getValueAt(int row, int column) {
				return ((MixedTableModel) getModel()).getRowName(row);
			}
		};
		setRowHeader(lm);
	}    
    
    private int getRowCountForHeader(){
    	return getRowCount();
    }
}
