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
package com.aplana.dbmi.search;

import java.util.ArrayList;
import java.util.List;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BlockViewParam;

/**
 * Search Block configuration, that represents group of searched attributes
 * 
 * @author skashanski
 *
 */
public class SearchBlockDescription {
	
	/**
	 * Unique Identifier of Block
	 */
	private Object id = null;
	
	/** Search Block displayed name  */
	private String name = "";
	
	/** initializer for Names*/
	private SearchAttributeNameInitializer nameInitializer = null;

	/**
	 * list of searched attributes 
	 */
	private List<Attribute> searchAttributes = new ArrayList<Attribute>();
	
	
	/**
	 * Attributes id's to span editors on the view.
	 */
	private List<String> spanedAttributeIds = new ArrayList<String>();
	
	
	/** Search Block displayed name  */
	private String divClass = "";
	
	private int columnsNumber = 1;
	

	
	/**
	 * Initialize search attribute names from resource bundle
	 * It is called after spring initialization   
	 */
	public void initializeNames() {
		
		for(Attribute searchAttribute : searchAttributes) {
			if (nameInitializer != null)
				nameInitializer.initialize(searchAttribute);
		}
			
	}
	
	public void setNameInitializer(SearchAttributeNameInitializer nameInitializer) {
		this.nameInitializer = nameInitializer;
	}

	public Object getId() {
		return id;
	}

	public void setId(Object id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Attribute> getSearchAttributes() {
		return searchAttributes;
	}

	public void setSearchAttributes(List<Attribute> searchAttributes) {
		this.searchAttributes = searchAttributes;
	}

	public List<String> getSpanedAttributeIds() {
		return spanedAttributeIds;
	}

	public void setSpanedAttributeIds(List<String> spanedAttributeIds) {
		this.spanedAttributeIds = spanedAttributeIds;
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
