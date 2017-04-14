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
package com.aplana.scanner.ui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * An implementation of <code>Transferable</code> that allows you to specify
 * the <code>DataFlavor</code> and data for the transfer.
 *
 * @author <a href="mailto:ogalkin@aplana.com">Oleg Galkin</a>
 */
public class DefaultTransferable implements Transferable {
	private ObjectFlavorPair[] data;
	
	/**
	 * A pair that contains an object which represents the data to be transferred and
	 * its <code>DataFlavor</code>.
	 */
	public static final class ObjectFlavorPair {
		private final Object data;
		private final DataFlavor flavor;
		
		/**
		 * Constructs an object instance.
		 *
		 * @param flavor  the <code>DataFlavor</code> of the data
		 * @param data    the data to be transferred
		 */
		public ObjectFlavorPair(DataFlavor flavor, Object data) {
			this.data = data;
			this.flavor = flavor;
		}
		
		/**
		 * Gets the data to be transferred.
		 */
		public Object getData() {
			return data;
		}
		
		/**
		 * Gets the <code>DataFlavor</code> of the data.
		 */
		public DataFlavor getDataFlavor() {
			return flavor;
		}
	}
	
	/**
	 * Constructs an object instance by the specified <code>DataFlavor</code> and its data.
	 *
	 * @param flavor  the <code>DataFlavor</code> this <code>Transferable</code> supports
	 * @param data    the data for <code>DataFlavor</code>
	 */
	public DefaultTransferable(DataFlavor flavor, Object data) {
		this(new ObjectFlavorPair(flavor, data));
	}
	
	/**
	 * Constructs an object instance by several {@link ObjectFlavorPair ObjectFlavorPairs}.
	 *
	 * @param data  the pairs that contain an object which represents the data to be transferred and
	 *              its <code>DataFlavor</code>.
	 */
	public DefaultTransferable(ObjectFlavorPair... data) {
		this.data = data;
	}

	/* (non-Javadoc)
	 * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
	 */
	public Object getTransferData(DataFlavor flavor)
					throws UnsupportedFlavorException, IOException {
		for (ObjectFlavorPair pair : data) {
			if (pair.getDataFlavor().equals(flavor))
				return pair.getData();
		}
		throw new UnsupportedFlavorException(flavor);
	}

	/* (non-Javadoc)
	 * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
	 */
	public DataFlavor[] getTransferDataFlavors() {
		DataFlavor[] flavors = new DataFlavor[data.length];
		for (int i = 0; i < data.length; i++)
			flavors[i] = data[i].getDataFlavor();
		return flavors;
	}

	/* (non-Javadoc)
	 * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
	 */
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		for (ObjectFlavorPair pair : data) {
			if (pair.getDataFlavor().equals(flavor))
				return true;
		}
		return false;
	}
}
