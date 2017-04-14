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
package com.aplana.dbmi.gui;

import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;

/**
 * Backbean for the {@link LinkChooser} class.
 * @author Mnagni
 **/
public class LinkChooserBean {
	
	/** The Object selected from the user */
	// private DataObject selected;
	private ObjectId selected;
	/** The name of the calling JSP element */	
	private String selectedEditor = "";
	/** Optional value to return */	
	private String linkType;
	private boolean dataAvailable = false;
	
	/**
	 * Returns the linkType associated to this instance.
	 * @return the linkType <code>String</code>, otherwise 
	 *    an empty <code>String</code> (not <code>null</code>) if the 
	 *    code is not set or {@link #isDataAvailable()} returns <code>false</code> 
	 * @see {@link #isDataAvailable()}  
	 * */
	public String getLinkType() {
		String ret = "";
		if (isDataAvailable()) {
			ret = linkType;
		}
		return ret;
	}

	public void setLinkType(String linkType) {
		this.linkType = linkType;
	}

	/** 
	 * Returns if the instance contains active data
	 * @return <code>true</code> if the instance contains consistent data, 
	 * <code>false</code> otherwise
	 **/
	public boolean isDataAvailable() {
		return dataAvailable;
	}

	/** 
	 * Marks the instance as containing active data
	 * @param dataAvailable <code>true</code> if the instance contains consistent data, 
	 * <code>false</code> otherwise
	 **/
	public void setDataAvailable(boolean dataAvailable) {
		this.dataAvailable = dataAvailable;
	}

	/**
	 * Returns the editor's managed attribute.  
	 * @return the name of the attribute under the editor control, otherwise 
	 *    an empty <code>String</code> (not <code>null</code>) if the 
	 *    editor is not set or {@link #isDataAvailable()} returns <code>false</code> 
	 * @see {@link #isDataAvailable()} 
	 **/
	public String getSelectedEditor() {
		String ret = "";
		if (isDataAvailable()) {
			ret = new String(selectedEditor);
			//setSelectedEditor("");
		}
		return ret;
	}

	public void setSelectedEditor(String selectedEditor) {
		if (!isDataAvailable()) {
			this.selectedEditor = selectedEditor;
		}
	}

	/**
	 * Returns the objectId code of the selected DataObject. After the call to this method 
	 * the {@link #isDataAvailable()} returns <code>false</code> (read-once allow to use 
	 * this method in javascript code without problems if the page is refreshed later).
	 * @return the objectId <code>String</code>, otherwise 
	 *    an empty <code>String</code> (not <code>null</code>) if the 
	 *    code is not set or {@link #isDataAvailable()} returns <code>false</code> 
	 * @see {@link #isDataAvailable()}  
	 * */
	public String getSelected() {
		String ret = "";
		if (selected != null && isDataAvailable()) {
			ret = ((Long)selected.getId()).toString();
			setDataAvailable(false);
		}
		return ret;
	}

	//public void setSelected(DataObject selected) {
	public void setSelected(ObjectId value) {
		if (!isDataAvailable()) {
			this.selected = value;
		}
	}
}
