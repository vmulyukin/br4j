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
package com.aplana.dbmi.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class SearchBean {
    private String searchText;
    private Boolean searchStrictWords =  Boolean.FALSE;
    private Boolean searchCurrentYear = Boolean.FALSE;
    private Boolean number;
    private Boolean property;
    private Boolean fullText;
    private Boolean registernumber;
    private Boolean byId;
    
    //true - ����� �� ���� ����, false - ����� �� ������� �������
    private Boolean wholeBase = Boolean.FALSE;
    
    private Map templates = new HashMap();
    private Map attributes  = new HashMap();
    private Boolean lastVersionSearch;
    private Boolean lastVersionInclude;    
    
    private Collection dbTemplates;
    private Collection dbAttributes;
   
	private Collection materialTypes;
    private Collection states = new LinkedList();
	/**
	 * resets the fields for reuse bean.
	 */
	public void reset(){
	}
	
	

	public Boolean getSearchCurrentYear() {
		return searchCurrentYear;
	}



	public void setSearchCurrentYear(Boolean searchCurrentYear) {
		this.searchCurrentYear = searchCurrentYear;
	}



	public Boolean getSearchStrictWords() {
		return searchStrictWords;
	}



	public void setSearchStrictWords(Boolean searchStrictWords) {
		this.searchStrictWords = searchStrictWords;
	}



	public Boolean getRegisternumber() {
		return registernumber;
	}
	
	public void setRegisternumber(Boolean registernumber) {
		this.registernumber = registernumber;
	}
	
	public Boolean getWholeBase() {
		return wholeBase;
	}
	
	public void setWholeBase(Boolean wholeBase) {
		this.wholeBase = wholeBase;
	}
	
	public String getSearchText() {
		return searchText;
	}

	public void setSearchText(String searchText) {
		this.searchText = searchText;
	}

	public Boolean getFullText() {
		return fullText;
	}

	public void setFullText(Boolean fullText) {
		this.fullText = fullText;
	}

	public Boolean getNumber() {
		return number == null ? false : number;
	}

	public void setNumber(Boolean number) {
		this.number = number;
	}

	public Boolean getProperty() {
		return property;
	}

	public void setProperty(Boolean property) {
		this.property = property;
	}


	public Map getTemplates() {
		return templates;
	}

	public void setTemplates(Map templates) {
		this.templates = templates;
	}
	
	public Boolean getById() {
		return byId;
	}
	
	public void setById(Boolean id) {
		this.byId = id;
	}

	public Boolean getLastVersionSearch() {
		return lastVersionSearch;
	}

	public void setLastVersionSearch(Boolean lastVersionSearch) {
		this.lastVersionSearch = lastVersionSearch;
	}


	public Boolean getLastVersionInclude() {
		return lastVersionInclude;
	}

	public void setLastVersionInclude(Boolean lastVersionInclude) {
		this.lastVersionInclude = lastVersionInclude;
	}


	public Collection getDbTemplates() {
		return dbTemplates;
	}

	public void setDbTemplates(Collection dbTemplates) {
		this.dbTemplates = dbTemplates;
	}

	public Map getAttributes() {
		return attributes;
	}

	public void setAttributes(Map attributes) {
		this.attributes = attributes;
	}

	public Collection getDbAttributes() {
		return dbAttributes;
	}

	public void setDbAttributes(Collection dbAttributes) {
		this.dbAttributes = dbAttributes;
	}

	public Collection getMaterialTypes() {
		return materialTypes;
	}

	public void setMaterialTypes(Collection materialTypes) {
		this.materialTypes = materialTypes;
	}
	
	public Collection getStates() {
		return states;
	}

	public void setStates(Collection states) {
		this.states = states;
	}
}
