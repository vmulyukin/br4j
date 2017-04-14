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
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.InvalidDnDOperationException;

import javax.activation.ActivationDataFlavor;
import javax.activation.DataHandler;
import javax.swing.JTable;

/**
 * Drag listener for Permission tab's rows. 
 * 
 * @author atsvetkov
 * 
 */
public class RowHeaderDragListener implements DragSourceListener, DragGestureListener {
	private final DataFlavor localObjectFlavor = new ActivationDataFlavor(Integer.class,
			DataFlavor.javaJVMLocalObjectMimeType, "Integer Row Index");  
	JTable header;

	DragSource ds = new DragSource();

	public RowHeaderDragListener(JTable list) {
		this.header = list;
		ds.createDefaultDragGestureRecognizer(list, DnDConstants.ACTION_MOVE, this);
	}

	public void dragGestureRecognized(DragGestureEvent dge) {
		DataHandler transferable = new DataHandler(new Integer(header.getSelectedRow()), localObjectFlavor.getMimeType());
		try{
			ds.startDrag(dge, DragSource.DefaultCopyDrop, transferable, this);
		} catch(InvalidDnDOperationException e){				
			//log
		}
	}

	public void dragEnter(DragSourceDragEvent dsde) {		
	}

	public void dragExit(DragSourceEvent dse) {
	}

	public void dragOver(DragSourceDragEvent dsde) {
	}

	public void dragDropEnd(DragSourceDropEvent dsde) {
	}

	public void dropActionChanged(DragSourceDragEvent dsde) {
	}
}

