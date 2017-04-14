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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;


/**
 * Represents Search Form configuration
 *   
 * 
 * @author skashanski
 *
 */
public class SearchFormDescription implements BeanFactoryAware, BeanNameAware {
	
	/**
	 * default search configuration file for searched form 
	 */
	private String defaultSearchConfigFile = null;
	
	private String name = null;
	
	private String title;
	
	private List<SearchBlockDescription> searchBlockDescriptions = new ArrayList<SearchBlockDescription>();
	
	private BeanFactory factory;
	
	/** search words string by attributes */
	private String searchWords = null;
	

	public void setBeanFactory(BeanFactory factory) throws BeansException {
		
		this.factory = factory;
	}

	

	public String getSearchWords() {
		return searchWords;
	}





	public void setSearchWords(String searchWords) {
		this.searchWords = searchWords;
	}



	public void setBeanName(String beanName) {
		this.name = beanName;
		
	}

	


	public String getTitle() {
		return title;
	}



	public void setTitle(String title) {
		this.title = title;
	}



	public String getName() {
		return name;
	}





	public String getDefaultSearchConfigFile() {
		return defaultSearchConfigFile;
	}



	public void setDefaultSearchConfigFile(String defaultSearchConfigFile) {
		this.defaultSearchConfigFile = defaultSearchConfigFile;
	}



	public List<SearchBlockDescription> getSearchBlockDescriptions() {
		return searchBlockDescriptions;
	}


	public void setSearchBlockDescriptions(
			List<SearchBlockDescription> searchBlockDescriptions) {
		this.searchBlockDescriptions = searchBlockDescriptions;
	}
	
	
	
	
	
	
	
	
	

}
