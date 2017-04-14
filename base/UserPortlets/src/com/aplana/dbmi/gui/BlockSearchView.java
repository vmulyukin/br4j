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

import java.util.List;

import com.aplana.dbmi.model.BlockViewParam;

/**
 * Represents block inside extended search form
 * @author skashanski
 *
 */
public class BlockSearchView {
	
    private String name = "";
    
    private List<SearchAttributeView> searchAttributes;
    
    private Boolean show = Boolean.TRUE;
    
    private Object id;
    
	private int currentState = BlockViewParam.OPEN;
    
	private String divClass;
	
	private int columnsNumber = 1;
	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public List<SearchAttributeView> getSearchAttributes() {
		return searchAttributes;
	}

	public void setSearchAttributes(List<SearchAttributeView> searchAttributes) {
		this.searchAttributes = searchAttributes;
	}

	public Boolean getShow() {
		return show;
	}

	public void setShow(Boolean show) {
		this.show = show;
	}

	public Object getId() {
		return id;
	}

	public void setId(Object id) {
		this.id = id;
	}

	public int getCurrentState() {
		return currentState;
	}

	public void setCurrentState(int currentState) {
		this.currentState = currentState;
	}

	public String getDivClass() {
		return divClass;
	}

	public void setDivClass(String divClass) {
		this.divClass = divClass;
	}

	public int getColumnsNumber() {
		return columnsNumber;
	}

	public void setColumnsNumber(int columnsNumber) {
		this.columnsNumber = columnsNumber;
	}	
	
}
